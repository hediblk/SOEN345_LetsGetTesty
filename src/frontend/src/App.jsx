import { useCallback, useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Header from './components/Header'
import HomePage from './pages/HomePage'
import AddEventsPage from './pages/AddEventsPage'
import EditEventsPage from './pages/EditEventsPage'
import AuthPage from './pages/AuthPage'
import ReservationsPage from './pages/ReservationsPage'
import { fetchEvents } from './api/eventsApi'
import { mapEventFromApi } from './data/events'
import './App.css'

export default function App() {
  const [events, setEvents] = useState([])
  const [eventsLoading, setEventsLoading] = useState(true)
  const [eventsError, setEventsError] = useState(null)

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

  useEffect(() => {
    loadEvents({ silent: false })
  }, [loadEvents])

  return (
    <BrowserRouter>
      <div className="app">
        <Header />
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
            <Route path="/auth" element={<AuthPage />} />
            <Route path="/reservations" element={<ReservationsPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}
