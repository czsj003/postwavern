import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth'
import { Logo } from './Logo'

export function Header() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function signOut() {
    logout()
    navigate('/login')
  }

  return (
    <header className="site-header">
      <Logo />
      <div className="header-session">
        {user ? <><span title={user.email}>Logged in as {user.name}</span> <button onClick={signOut}>Logout</button></> : <><Link to="/login">Login</Link> | <Link to="/signup">Sign Up</Link></>}
      </div>
    </header>
  )
}
