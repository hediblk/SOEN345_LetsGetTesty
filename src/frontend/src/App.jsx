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

export default function App() {
  const [authSession, setAuthSession] = useState(() => getStoredAuthSession())

  const [events, setEvents] = useState([])
  const [eventsLoading, setEventsLoading] = useState(true)
  const [eventsError, setEventsError] = useState(null)

  const [reservations, setReservations] = useState([])
  const [reservationsLoading, setReservationsLoading] = useState(false)
  const [reservationsError, setReservationsError] = useState(null)
  
  const isAuthenticated = authSession !== null

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
    if (!authSession?.id) {
      setReservations([])
      setReservationsError(null)
      setReservationsLoading(false)
      return
    }

    loadReservations(authSession.id, { silent: false })
  }, [authSession, loadReservations])

  function handleLogout() {
    clearAuthSession()
  }

  async function handleReserve(eventId) {
    if (!authSession?.id) {
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
        <Header isSignedIn={isAuthenticated} onLogout={handleLogout} />
        <main className="main">
          <Routes>
            <Route
              path="/"
              element={
                isAuthenticated ? (
                  <HomePage
                    events={events}
                    eventsLoading={eventsLoading}
                    eventsError={eventsError}
                    reservations={reservations}
                    onReserve={handleReserve}
                  />
                ) : (
                  <Navigate to="/auth" replace />
                )
              }
            />
            <Route
              path="/add-events"
              element={
                isAuthenticated ? (
                  <AddEventsPage
                    events={events}
                    onEventsChanged={() => loadEvents({ silent: true })}
                  />
                ) : (
                  <Navigate to="/auth" replace />
                )
              }
            />
            <Route
              path="/edit-events"
              element={
                isAuthenticated ? (
                  <EditEventsPage
                    events={events}
                    onEventsChanged={() => loadEvents({ silent: true })}
                  />
                ) : (
                  <Navigate to="/auth" replace />
                )
              }
            />
            <Route
              path="/auth"
              element={isAuthenticated ? <Navigate to="/" replace /> : <AuthPage onAuthSuccess={setAuthSession} />}
            />
            <Route
              path="/reservations"
              element={
                isAuthenticated ? (
                  <ReservationsPage
                    events={events}
                    reservations={reservations}
                    reservationsLoading={reservationsLoading}
                    reservationsError={reservationsError}
                  />
                ) : (
                  <Navigate to="/auth" replace />
                )
              }
            />
            <Route path="*" element={<Navigate to={isAuthenticated ? '/' : '/auth'} replace />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}
