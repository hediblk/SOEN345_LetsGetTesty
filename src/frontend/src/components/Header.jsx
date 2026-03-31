import { Link, useLocation } from 'react-router-dom'
import './Header.css'

export default function Header({ authSession, onLogout }) {
  const { pathname } = useLocation()
  const role = authSession?.role ?? null
  const isSignedIn = authSession !== null
  const homePath = role === 'ADMIN' ? '/admin/add-events' : '/'
  const navLinks = role === 'ADMIN'
    ? [
        { to: '/admin/add-events', label: 'Add Event' },
        { to: '/admin/edit-events', label: 'Edit Event' },
      ]
    : role === 'USER'
      ? [
          { to: '/', label: 'Events' },
          { to: '/reservations', label: 'My Tickets' },
        ]
      : []

  return (
    <header className="header">
      <Link to={homePath} className="logo">
        <span className="logo-mark">LetsGetTesty</span>
      </Link>
      <nav className="nav">
        {navLinks.map(link => (
          <Link
            key={link.to}
            to={link.to}
            className={pathname === link.to ? 'nav-link active' : 'nav-link'}
          >
            {link.label}
          </Link>
        ))}
        {isSignedIn ? (
          <button className="nav-cta" onClick={onLogout}>Logout</button>
        ) : (
          <>
            <Link to="/auth" className={pathname === '/auth' ? 'nav-link active' : 'nav-link'}>Customer</Link>
            <Link to="/admin" className={pathname === '/admin' ? 'nav-cta' : 'nav-link'}>Admin</Link>
          </>
        )}
      </nav>
    </header>
  )
}
