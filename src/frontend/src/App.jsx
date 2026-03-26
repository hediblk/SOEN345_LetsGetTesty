import { useCallback, useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Header from './components/Header'
import HomePage from './pages/HomePage'
import AddEventsPage from './pages/AddEventsPage'
import EditEventsPage from './pages/EditEventsPage'
import AuthPage from './pages/AuthPage'
import ReservationsPage from './pages/ReservationsPage'
import { fetchEvents } from './api/eventsApi'
import { fetchReservationsByUser } from './api/reservationsApi'
import { mapEventFromApi } from './data/events'
import { mapReservationFromApi } from './data/reservations'
import { AUTH_STORAGE_KEY } from './constants'
import './App.css'

export default function App() {
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
    const silent = options.silent === true
    if (!silent) {
      setEventsLoading(true)
    }
    setEventsError(null)
    try {
      const raw = await fetchEvents()
      setEvents(raw.map(mapEventFromApi))
    } catch (err) {
      setEventsError(err.message || 'Failed to load events.')
    } finally {
      if (!silent) {
        setEventsLoading(false)
      }
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
    loadEvents({ silent: false })
  }, [loadEvents])

  useEffect(() => {
    if (!authUser) return
    loadReservations(authUser.id, { silent: false })
  }, [authUser, loadReservations])

  function handleLogout() {
    setAuthUser(null);
    setReservations([]);
    localStorage.removeItem(AUTH_STORAGE_KEY)
  }

  return (
    <BrowserRouter>
      <div className="app">
        <Header isSignedIn={!!authUser} onLogout={(handleLogout)} />
        <main className="main">
          <Routes>
            <Route
              path="/"
              element={
                <HomePage
                  events={events}
                  eventsLoading={eventsLoading}
                  eventsError={eventsError}
                />
              }
            />
            <Route
              path="/add-events"
              element={
                <AddEventsPage
                  events={events}
                  onEventsChanged={() => loadEvents({ silent: true })}
                />
              }
            />
            <Route
              path="/edit-events"
              element={
                <EditEventsPage
                  events={events}
                  onEventsChanged={() => loadEvents({ silent: true })}
                />
              }
            />
            <Route
              path="/auth"
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
