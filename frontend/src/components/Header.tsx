import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth'
import { Logo } from './Logo'

export function Header() {
  const { user, logout } = useAuth()
  const [open, setOpen] = useState(false)
  const navigate = useNavigate()

  function signOut() {
    logout()
    setOpen(false)
    navigate('/')
  }

  return (
    <header className="site-header">
      <div className="header-inner">
        <Logo />
        <button className="mobile-menu" onClick={() => setOpen((value) => !value)} aria-label="Toggle menu">{open ? 'Close' : 'Menu'}</button>
        <nav className={open ? 'nav open' : 'nav'}>
          <NavLink to="/" onClick={() => setOpen(false)}>All Posts</NavLink>
          <div className="nav-actions">
            {user ? (
              <>
                <span className="user-chip" title={user.email}>Logged in as {user.name}</span>
                <Link to="/write" className="button button-small" onClick={() => setOpen(false)}>Add New Post</Link>
                <button className="button button-small" onClick={signOut}>Logout</button>
              </>
            ) : (
              <>
                <Link to="/login" onClick={() => setOpen(false)}>Login</Link>
                <Link to="/signup" onClick={() => setOpen(false)}>Sign Up</Link>
              </>
            )}
          </div>
        </nav>
      </div>
    </header>
  )
}
