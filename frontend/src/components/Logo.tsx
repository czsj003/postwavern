import { Link } from 'react-router-dom'

export function Logo() {
  return (
    <Link className="logo" to="/" aria-label="PostWave home">
      <h1>PostWave</h1>
    </Link>
  )
}
