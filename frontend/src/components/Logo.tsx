import { Waves } from 'lucide-react'
import { Link } from 'react-router-dom'

export function Logo() {
  return (
    <Link className="logo" to="/" aria-label="PostWave home">
      <span className="logo-mark"><Waves size={22} strokeWidth={2.3} /></span>
      <span>PostWave</span>
    </Link>
  )
}
