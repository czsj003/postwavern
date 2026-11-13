import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return <div className="page-container"><div className="empty-state"><div className="error-number">404</div><h2>This page drifted away.</h2><p>Let’s get you back to the stories.</p><Link className="button button-dark" to="/">Return home</Link></div></div>
}
