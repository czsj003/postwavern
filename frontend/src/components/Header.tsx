import { LogOut, Menu, PenLine, X } from 'lucide-react'
import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth'
import { initials } from '../utils'
import { Logo } from './Logo'

export function Header() {
  const { user, isAuthenticated, logout } = useAuth()
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
        <button className="mobile-menu" onClick={() => setOpen((value) => !value)} aria-label="Toggle menu">
          {open ? <X /> : <Menu />}
        </button>
        <nav className={open ? 'nav open' : 'nav'}>
          <NavLink to="/" onClick={() => setOpen(false)}>Discover</NavLink>
          {isAuthenticated && <NavLink to="/write" onClick={() => setOpen(false)}>Write</NavLink>}
          <div className="nav-actions">
            {user ? (
              <>
                <Link to="/write" className="button button-dark button-small" onClick={() => setOpen(false)}>
                  <PenLine size={16} /> New story
                </Link>
                <span className="user-chip" title={user.email}>
                  <span className="avatar avatar-small">{initials(user.name)}</span>
                  <span>{user.name}</span>
                </span>
                <button className="icon-button" onClick={signOut} title="Sign out" aria-label="Sign out"><LogOut size={18} /></button>
              </>
            ) : (
              <>
                <Link to="/login" className="text-link" onClick={() => setOpen(false)}>Sign in</Link>
                <Link to="/signup" className="button button-dark button-small" onClick={() => setOpen(false)}>Join PostWave</Link>
              </>
            )}
          </div>
        </nav>
      </div>
    </header>
  )
}
