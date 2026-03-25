import { useState } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Header from './components/Header'
import HomePage from './pages/HomePage'
import AddEventsPage from './pages/AddEventsPage'
import AuthPage from './pages/AuthPage'
import ReservationsPage from './pages/ReservationsPage'
import { INITIAL_EVENTS } from './data/events'
import './App.css'

export default function App() {
  const [events, setEvents] = useState(INITIAL_EVENTS)

  return (
    <BrowserRouter>
      <div className="app">
        <Header />
        <main className="main">
          <Routes>
            <Route path="/"             element={<HomePage events={events} />} />
            <Route path="/add-events"   element={<AddEventsPage events={events} setEvents={setEvents} />} />
            <Route path="/auth"         element={<AuthPage />} />
            <Route path="/reservations" element={<ReservationsPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}
