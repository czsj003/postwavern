import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api'
import { useAuth } from '../auth'

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
    <div className="simple-form-page">
      <h2>{isSignup ? 'Sign Up' : 'Login'}</h2>
      <form onSubmit={submit} className="simple-form">
        {isSignup && <label>Username<input value={name} onChange={(event) => setName(event.target.value)} placeholder="Username" autoComplete="name" required />{fieldErrors.name && <small>{fieldErrors.name}</small>}</label>}
        <label>Email<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="Email" autoComplete="email" required />{fieldErrors.email && <small>{fieldErrors.email}</small>}</label>
        <label>Password<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Password" autoComplete={isSignup ? 'new-password' : 'current-password'} minLength={isSignup ? 8 : undefined} required />{fieldErrors.password && <small>{fieldErrors.password}</small>}</label>
        <button className="button" disabled={submitting}>{submitting ? 'Please wait...' : isSignup ? 'Register' : 'Login'}</button>
      </form>
      {error && <p className="form-error">{error}</p>}
      <p className="form-switch">{isSignup ? 'Already registered?' : 'Need an account?'} <Link to={isSignup ? '/login' : '/signup'}>{isSignup ? 'Login' : 'Sign Up'}</Link></p>
    </div>
  )
}
