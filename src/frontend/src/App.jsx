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
import { createReservation, fetchReservationsByUser } from './api/reservationsApi'
import { mapEventFromApi } from './data/events'
import { buildCreateReservationPayload, mapReservationFromApi } from './data/reservations'
import { AUTH_STORAGE_KEY } from './constants'
import './App.css'

export default function App() {
  const [authSession, setAuthSession] = useState(() => getStoredAuthSession())
  const [authUser, setAuthUser] = useState(() => {
    const stored = localStorage.getItem(AUTH_STORAGE_KEY)
    return stored ? JSON.parse(stored) : null
  })

  const [events, setEvents] = useState([])
  const [eventsLoading, setEventsLoading] = useState(true)
  const [eventsError, setEventsError] = useState(null)

  const [reservations, setReservations] = useState([])
  const [reservationsLoading, setReservationsLoading] = useState(false)
  const [reservationsError, setReservationsError] = useState(null)

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

  const loadReservations = useCallback(async (userId, options = {}) => {
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

  useEffect(() => {
    if (!authUser) return
    loadReservations(authUser.id, { silent: false })
  }, [authUser, loadReservations])

  function handleLogout() {
    setAuthUser(null);
    setReservations([]);
    localStorage.removeItem(AUTH_STORAGE_KEY)
  }

  async function handleReserve(eventId) {
    if (!authUser) return
    const userId = authUser.id
    try {
      await createReservation(buildCreateReservationPayload(userId, eventId))
      loadReservations(authUser.id, { silent: false })
    } catch (err) {
      alert(err.message || 'Failed to reserve. The event may be full.')
    }
  }

  return (
    <BrowserRouter>
      <div className="app">
        {isAuthenticated ? <Header onLogout={handleLogout} /> : null}
        <Header isSignedIn={!!authUser} onLogout={(handleLogout)} />
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
                <HomePage
                  events={events}
                  eventsLoading={eventsLoading}
                  eventsError={eventsError}
                  reservations={reservations}
                  onReserve={handleReserve}
                />
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
              element={
                <AuthPage
                  display={authUser?.fullName}
                  onAuthUserChange={setAuthUser}
                />
              }
            />
            <Route
              path="/reservations"
              element={
                <ReservationsPage
                  events={events}
                  reservations={reservations}
                  reservationsLoading={reservationsLoading}
                  reservationsError={reservationsError}
                />
              }
            />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}
