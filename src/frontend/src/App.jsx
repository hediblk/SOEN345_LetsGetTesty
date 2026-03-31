import { useCallback, useEffect, useState } from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { fetchEvents } from './api/eventsApi'
import { createReservation, fetchReservationsByUser } from './api/reservationsApi'
import {
  AUTH_CHANGE_EVENT,
  clearAuthSession,
  getStoredAuthSession,
  isUnauthorizedError,
} from './auth/authStorage'
import Header from './components/Header'
import { mapEventFromApi } from './data/events'
import { buildCreateReservationPayload, mapReservationFromApi } from './data/reservations'
import AddEventsPage from './pages/AddEventsPage'
import AuthPage from './pages/AuthPage'
import EditEventsPage from './pages/EditEventsPage'
import HomePage from './pages/HomePage'
import ReservationsPage from './pages/ReservationsPage'
import './App.css'

function getDefaultPathForRole(role) {
  return role === 'ADMIN' ? '/admin/add-events' : '/'
}

export default function App() {
  const [authSession, setAuthSession] = useState(() => getStoredAuthSession())

  const [events, setEvents] = useState([])
  const [eventsLoading, setEventsLoading] = useState(true)
  const [eventsError, setEventsError] = useState(null)

  const [reservations, setReservations] = useState([])
  const [reservationsLoading, setReservationsLoading] = useState(false)
  const [reservationsError, setReservationsError] = useState(null)
  
  const isAuthenticated = authSession !== null
  const role = authSession?.role ?? null
  const isAdmin = role === 'ADMIN'
  const isUser = role === 'USER'
  const defaultAuthenticatedPath = getDefaultPathForRole(role)

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

  const loadReservations = useCallback(async (userId, options = {}) => {
    if (!userId) {
      setReservations([])
      setReservationsError(null)
      setReservationsLoading(false)
      return
    }

    const silent = options.silent === true
    if (!silent) {
      setReservationsLoading(true)
    }
    setReservationsError(null)

    try {
      const raw = await fetchReservationsByUser(userId)
      setReservations(raw.map(mapReservationFromApi))
    } catch (err) {
      if (isUnauthorizedError(err)) {
        clearAuthSession()
        return
      }
      setReservationsError(err.message || 'Failed to load reservations.')
    } finally {
      if (!silent) {
        setReservationsLoading(false)
      }
    }
  }, [])

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

  useEffect(() => {
    if (!isUser || !authSession?.id) {
      setReservations([])
      setReservationsError(null)
      setReservationsLoading(false)
      return
    }

    loadReservations(authSession.id, { silent: false })
  }, [authSession, isUser, loadReservations])

  function handleLogout() {
    clearAuthSession()
  }

  async function handleReserve(eventId) {
    if (!isUser || !authSession?.id) {
      return
    }

    try {
      await createReservation(buildCreateReservationPayload(authSession.id, eventId))
      await loadReservations(authSession.id, { silent: false })
    } catch (err) {
      alert(err.message || 'Failed to reserve. The event may be full.')
    }
  }

  return (
    <BrowserRouter>
      <div className="app">
        <Header authSession={authSession} onLogout={handleLogout} />
        <main className="main">
          <Routes>
            <Route
              path="/"
              element={
                !isAuthenticated ? (
                  <Navigate to="/auth" replace />
                ) : isUser ? (
                  <HomePage
                    events={events}
                    eventsLoading={eventsLoading}
                    eventsError={eventsError}
                    reservations={reservations}
                    onReserve={handleReserve}
                  />
                ) : (
                  <Navigate to={defaultAuthenticatedPath} replace />
                )
              }
            />
            <Route
              path="/admin/add-events"
              element={
                !isAuthenticated ? (
                  <Navigate to="/admin" replace />
                ) : isAdmin ? (
                  <AddEventsPage
                    events={events}
                    onEventsChanged={() => loadEvents({ silent: true })}
                  />
                ) : (
                  <Navigate to="/" replace />
                )
              }
            />
            <Route
              path="/admin/edit-events"
              element={
                !isAuthenticated ? (
                  <Navigate to="/admin" replace />
                ) : isAdmin ? (
                  <EditEventsPage
                    events={events}
                    onEventsChanged={() => loadEvents({ silent: true })}
                  />
                ) : (
                  <Navigate to="/" replace />
                )
              }
            />
            <Route
              path="/auth"
              element={
                isAuthenticated ? (
                  <Navigate to={defaultAuthenticatedPath} replace />
                ) : (
                  <AuthPage onAuthSuccess={setAuthSession} />
                )
              }
            />
            <Route
              path="/reservations"
              element={
                !isAuthenticated ? (
                  <Navigate to="/auth" replace />
                ) : isUser ? (
                  <ReservationsPage
                    events={events}
                    reservations={reservations}
                    reservationsLoading={reservationsLoading}
                    reservationsError={reservationsError}
                  />
                ) : (
                  <Navigate to={defaultAuthenticatedPath} replace />
                )
              }
            />
            <Route
              path="/admin"
              element={
                isAuthenticated ? (
                  <Navigate to={defaultAuthenticatedPath} replace />
                ) : (
                  <AuthPage
                    key="admin-auth"
                    onAuthSuccess={setAuthSession}
                    authRole="ADMIN"
                    allowRegister={false}
                  />
                )
              }
            />
            <Route path="/add-events" element={<Navigate to={isAdmin ? '/admin/add-events' : isAuthenticated ? '/' : '/admin'} replace />} />
            <Route path="/edit-events" element={<Navigate to={isAdmin ? '/admin/edit-events' : isAuthenticated ? '/' : '/admin'} replace />} />
            <Route path="*" element={<Navigate to={isAuthenticated ? defaultAuthenticatedPath : '/auth'} replace />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}
