import { Component } from 'react';

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, info) {
    console.error('Unexpected error:', error, info);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="login-screen">
          <div className="login-card" style={{ textAlign: 'center' }}>
            <h1>Something went wrong</h1>
            <p className="login-subtitle">
              An unexpected error occurred. Try reloading the page.
            </p>
            <button className="btn-primary" onClick={() => window.location.reload()}>
              Reload
            </button>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}