import { useCallback, useEffect, useState } from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import Header from './components/Header'
import HomePage from './pages/HomePage'
import AddEventsPage from './pages/AddEventsPage'
import EditEventsPage from './pages/EditEventsPage'
import AuthPage from './pages/AuthPage'
import ReservationsPage from './pages/ReservationsPage'
import { fetchEvents } from './api/eventsApi'
import {
  AUTH_CHANGE_EVENT,
  clearAuthSession,
  getStoredAuthSession,
  isUnauthorizedError,
} from './auth/authStorage'
import { mapEventFromApi } from './data/events'
import './App.css'

export default function App() {
  const [authSession, setAuthSession] = useState(() => getStoredAuthSession())
  const [events, setEvents] = useState([])
  const [eventsLoading, setEventsLoading] = useState(true)
  const [eventsError, setEventsError] = useState(null)

  const loadEvents = useCallback(async (options = {}) => {
    if (!authSession) {
      setEvents([])
      setEventsError(null)
      setEventsLoading(false)
      return
    }

    const silent = options.silent === true
    if (!silent) {
      setEventsLoading(true)
    }
    setEventsError(null)
    try {
      const raw = await fetchEvents()
      setEvents(raw.map(mapEventFromApi))
    } catch (err) {
      if (isUnauthorizedError(err)) {
        clearAuthSession()
        return
      }
      setEventsError(err.message || 'Failed to load events.')
    } finally {
      if (!silent) {
        setEventsLoading(false)
      }
    }
  }, [authSession])

  useEffect(() => {
    const syncAuthState = () => {
      setAuthSession(getStoredAuthSession())
    }

    window.addEventListener('storage', syncAuthState)
    window.addEventListener(AUTH_CHANGE_EVENT, syncAuthState)

    return () => {
      window.removeEventListener('storage', syncAuthState)
      window.removeEventListener(AUTH_CHANGE_EVENT, syncAuthState)
    }
  }, [])

  useEffect(() => {
    if (!authSession) {
      setEvents([])
      setEventsError(null)
      setEventsLoading(false)
      return
    }

    loadEvents({ silent: false })
  }, [authSession, loadEvents])

  const isAuthenticated = authSession !== null

  function handleLogout() {
    clearAuthSession()
  }

  return (
    <BrowserRouter>
      <div className="app">
        {isAuthenticated ? <Header onLogout={handleLogout} /> : null}
        <main className="main">
          <Routes>
            <Route
              path="/"
              element={
                isAuthenticated
                  ? (
                      <HomePage
                        events={events}
                        eventsLoading={eventsLoading}
                        eventsError={eventsError}
                      />
                    )
                  : <Navigate to="/auth" replace />
              }
            />
            <Route
              path="/add-events"
              element={
                isAuthenticated
                  ? (
                      <AddEventsPage
                        events={events}
                        onEventsChanged={() => loadEvents({ silent: true })}
                      />
                    )
                  : <Navigate to="/auth" replace />
              }
            />
            <Route
              path="/edit-events"
              element={
                isAuthenticated
                  ? (
                      <EditEventsPage
                        events={events}
                        onEventsChanged={() => loadEvents({ silent: true })}
                      />
                    )
                  : <Navigate to="/auth" replace />
              }
            />
            <Route
              path="/auth"
              element={isAuthenticated ? <Navigate to="/" replace /> : <AuthPage onAuthSuccess={setAuthSession} />}
            />
            <Route
              path="/reservations"
              element={isAuthenticated ? <ReservationsPage /> : <Navigate to="/auth" replace />}
            />
            <Route path="*" element={<Navigate to={isAuthenticated ? '/' : '/auth'} replace />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}
