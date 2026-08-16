package com.futuretransform.hrm_payroll_backend.service.impl;

import com.futuretransform.hrm_payroll_backend.entity.EmailLog;
import com.futuretransform.hrm_payroll_backend.entity.Payslip;
import com.futuretransform.hrm_payroll_backend.exception.ResourceNotFoundException;
import com.futuretransform.hrm_payroll_backend.repository.EmailLogRepository;
import com.futuretransform.hrm_payroll_backend.service.EmailService;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    @Transactional
    public void sendPayslipEmail(Payslip payslip) {
        String recipientEmail = payslip.getEmployee().getEmail();

        EmailLog log = EmailLog.builder()
                .payslip(payslip)
                .sentTo(recipientEmail)
                .attemptCount(1)
                .build();

        if (recipientEmail == null || !EMAIL_REGEX.matcher(recipientEmail).matches()) {
            log.setStatus(EmailLog.Status.FAILED);
            log.setErrorCategory("INVALID_EMAIL");
            log.setRetryable(false);
            log.setErrorMessage("Recipient email address is missing or malformed: " + recipientEmail);
            emailLogRepository.save(log);
            return;
        }

        try {
            send(payslip, recipientEmail);
            log.setStatus(EmailLog.Status.SUCCESS);
            log.setRetryable(false); // nothing to retry once successful

        } catch (Exception e) {
            applyFailureClassification(log, e);
        }

        emailLogRepository.save(log);
    }

    @Override
    @Transactional
    public void retryFailedEmail(Long emailLogId) {
        EmailLog existingLog = emailLogRepository.findById(emailLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Email log not found: " + emailLogId));

        if (existingLog.getStatus() == EmailLog.Status.SUCCESS) {
            throw new IllegalArgumentException("This email was already sent successfully");
        }

        if (Boolean.FALSE.equals(existingLog.getRetryable())) {
            throw new IllegalArgumentException(
                    "This failure is not retryable (" + existingLog.getErrorCategory() +
                            "). Fix the underlying issue and generate a new payslip instead.");
        }

        if (existingLog.getAttemptCount() >= MAX_RETRY_ATTEMPTS) {
            // Exhausted retries — mark as permanently failed, stop trying automatically
            existingLog.setRetryable(false);
            existingLog.setErrorCategory("MAX_RETRIES_EXCEEDED");
            emailLogRepository.save(existingLog);
            throw new IllegalArgumentException(
                    "Maximum retry attempts (" + MAX_RETRY_ATTEMPTS + ") reached for this email. " +
                            "This has been marked as permanently failed and requires manual investigation " +
                            "(check SMTP config, network, or employee email address).");
        }

        Payslip payslip = existingLog.getPayslip();

        try {
            send(payslip, existingLog.getSentTo());
            existingLog.setStatus(EmailLog.Status.SUCCESS);
            existingLog.setErrorMessage(null);
            existingLog.setErrorCategory(null);
            existingLog.setRetryable(false);

        } catch (Exception e) {
            applyFailureClassification(existingLog, e);
        }

        existingLog.setAttemptCount(existingLog.getAttemptCount() + 1);
        existingLog.setLastAttemptAt(LocalDateTime.now());
        emailLogRepository.save(existingLog);
    }

    private void send(Payslip payslip, String recipientEmail) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String monthName = Month.of(payslip.getPayMonth()).getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);

        helper.setTo(recipientEmail);
        helper.setSubject("Payslip for " + monthName + " " + payslip.getPayYear());
        helper.setText(
                "Dear " + payslip.getEmployee().getName() + ",\n\n" +
                        "Please find attached your payslip for " + monthName + " " + payslip.getPayYear() + ".\n\n" +
                        "Net Pay: Rs. " + payslip.getNetSalary() + "\n\n" +
                        "Regards,\nHR Team\nFuture Transformation Company"
        );

        if (payslip.getPdfPath() != null) {
            File pdfFile = new File(payslip.getPdfPath());
            if (pdfFile.exists()) {
                helper.addAttachment(pdfFile.getName(), pdfFile);
            }
        }

        mailSender.send(message);
    }

    // This is the core of what you're asking for: map real exception types
    // to a category + a retryable/not-retryable decision, instead of one generic message.
    private void applyFailureClassification(EmailLog log, Exception e) {
        log.setStatus(EmailLog.Status.FAILED);

        Throwable cause = e.getCause() != null ? e.getCause() : e;

        if (e instanceof AddressException || cause instanceof AddressException) {
            log.setErrorCategory("INVALID_EMAIL");
            log.setRetryable(false); // address itself is bad — retrying won't help
            log.setErrorMessage("Invalid recipient address: " + e.getMessage());

        } else if (e instanceof MailAuthenticationException) {
            log.setErrorCategory("AUTH_ERROR");
            log.setRetryable(false); // SMTP credentials/config issue — needs admin fix, not a resend
            log.setErrorMessage("SMTP authentication failed: " + e.getMessage());

        } else if (cause instanceof java.net.SocketTimeoutException || e instanceof MailSendException) {
            log.setErrorCategory("TIMEOUT");
            log.setRetryable(true); // transient network issue — safe to retry
            log.setErrorMessage("Timed out sending email: " + e.getMessage());

        } else if (cause instanceof java.net.ConnectException || cause instanceof java.net.UnknownHostException) {
            log.setErrorCategory("CONNECTION_ERROR");
            log.setRetryable(true); // SMTP server unreachable — transient, worth retrying
            log.setErrorMessage("Could not connect to mail server: " + e.getMessage());

        } else {
            log.setErrorCategory("UNKNOWN");
            log.setRetryable(true); // default to retryable, but flagged distinctly for investigation
            log.setErrorMessage(truncate(e.getMessage(), 500));
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}