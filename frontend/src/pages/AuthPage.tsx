import { ArrowRight, LockKeyhole, Mail, UserRound } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api'
import { useAuth } from '../auth'
import { Logo } from '../components/Logo'

export function AuthPage({ mode }: { mode: 'login' | 'signup' }) {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)
  const { login, signup, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  if (isAuthenticated) return <Navigate to="/" replace />

  async function submit(event: FormEvent) {
    event.preventDefault()
    setError(''); setFieldErrors({}); setSubmitting(true)
    try {
      if (mode === 'signup') await signup(name, email, password)
      else await login(email, password)
      const from = (location.state as { from?: string } | null)?.from
      navigate(from || '/', { replace: true })
    } catch (err) {
      if (err instanceof ApiError) setFieldErrors(err.fieldErrors)
      setError(err instanceof Error ? err.message : 'Unable to continue.')
    } finally { setSubmitting(false) }
  }

  const isSignup = mode === 'signup'
  return (
    <div className="auth-page">
      <div className="auth-panel-art">
        <Link to="/"><Logo /></Link>
        <div className="auth-art-copy">
          <span className="eyebrow light">POSTWAVE COMMUNITY</span>
          <h1>{isSignup ? 'Your next idea deserves an audience.' : 'Welcome back to the conversation.'}</h1>
          <p>{isSignup ? 'Join writers and curious minds sharing thoughtful perspectives every day.' : 'Sign in to write, wave, and keep the best ideas moving.'}</p>
        </div>
        <div className="auth-wave" />
      </div>
      <div className="auth-form-panel">
        <div className="auth-form-wrap">
          <div className="mobile-auth-logo"><Logo /></div>
          <span className="section-kicker">{isSignup ? 'CREATE AN ACCOUNT' : 'MEMBER SIGN IN'}</span>
          <h2>{isSignup ? 'Join PostWave' : 'Good to see you'}</h2>
          <p className="muted">{isSignup ? 'Start sharing your stories in a minute.' : 'Enter your details to continue.'}</p>
          {error && <div className="form-alert">{error}</div>}
          <form onSubmit={submit} className="auth-form">
            {isSignup && <label>Full name<div className="input-wrap"><UserRound size={18} /><input value={name} onChange={(event) => setName(event.target.value)} placeholder="Ada Lovelace" autoComplete="name" required /></div>{fieldErrors.name && <small>{fieldErrors.name}</small>}</label>}
            <label>Email address<div className="input-wrap"><Mail size={18} /><input type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="you@example.com" autoComplete="email" required /></div>{fieldErrors.email && <small>{fieldErrors.email}</small>}</label>
            <label>Password<div className="input-wrap"><LockKeyhole size={18} /><input type="password" value={password} onChange={(event) => setPassword(event.target.value)} placeholder={isSignup ? '8–72 characters' : 'Your password'} autoComplete={isSignup ? 'new-password' : 'current-password'} minLength={isSignup ? 8 : undefined} required /></div>{fieldErrors.password && <small>{fieldErrors.password}</small>}</label>
            <button className="button button-accent button-full" disabled={submitting}>{submitting ? 'Please wait…' : isSignup ? 'Create my account' : 'Sign in'} {!submitting && <ArrowRight size={18} />}</button>
          </form>
          <p className="auth-switch">{isSignup ? 'Already have an account?' : 'New to PostWave?'} <Link to={isSignup ? '/login' : '/signup'}>{isSignup ? 'Sign in' : 'Create an account'}</Link></p>
        </div>
      </div>
    </div>
  )
}
