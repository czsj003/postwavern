import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return <div className="page-container"><div className="empty-state"><div className="error-number">404</div><h2>Page not found.</h2><p>The page may have moved or no longer exists.</p><Link className="button button-dark" to="/">Return home</Link></div></div>
}
