import { Link, useLocation } from 'react-router-dom'
import './Header.css'

export default function Header() {
  const { pathname } = useLocation()

  return (
    <header className="header">
      <Link to="/" className="logo">
        <span className="logo-mark">LetsGetTesty</span>
      </Link>
      <nav className="nav">
        <Link to="/" className={pathname === '/' ? 'nav-link active' : 'nav-link'}>Events</Link>
        <Link to="/add-events" className={pathname === '/add-events' ? 'nav-link active' : 'nav-link'}>Add Event</Link>
        <Link to="/reservations" className={pathname === '/reservations' ? 'nav-link active' : 'nav-link'}>My Tickets</Link>
        <Link to="/auth" className="nav-cta">Sign In</Link>
      </nav>
    </header>
  )
}
