import { Outlet } from 'react-router-dom'
import { Header } from './Header'
import { Logo } from './Logo'

export function Layout() {
  return (
    <div className="app-shell">
      <Header />
      <main><Outlet /></main>
      <footer className="site-footer">
        <div><Logo /><p>A place for clear thinking and good writing.</p></div>
        <p>React · Spring Boot · PostgreSQL</p>
      </footer>
    </div>
  )
}
