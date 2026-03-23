import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Header from './components/Header'
import HomePage from './pages/HomePage'
import AuthPage from './pages/AuthPage'
import ReservationsPage from './pages/ReservationsPage'
import './App.css'

export default function App() {
  return (
    <BrowserRouter>
      <div className="app">
        <Header />
        <main className="main">
          <Routes>
            <Route path="/"             element={<HomePage />} />
            <Route path="/auth"         element={<AuthPage />} />
            <Route path="/reservations" element={<ReservationsPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}
