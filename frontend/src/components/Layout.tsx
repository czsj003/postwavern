import { Outlet } from 'react-router-dom'
import { Header } from './Header'
import { Logo } from './Logo'

export function Layout() {
  return (
    <div className="app-shell">
      <Header />
      <main><Outlet /></main>
      <footer className="site-footer">
        <div><Logo /><p>Thoughtful stories, shared in waves.</p></div>
        <p>Built with React, Spring Boot & PostgreSQL.</p>
      </footer>
    </div>
  )
}
