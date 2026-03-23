import { useState } from 'react'
import './ReservationsPage.css'

const MOCK_RESERVATIONS = [
  { id: 1, event: 'Dune: Messiah — World Premiere', date: '2026-04-15', venue: 'Cinema Imperial, Montreal',   status: 'CONFIRMED', booked: '2026-03-10' },
  { id: 2, event: 'Montreal Jazz Night',             date: '2026-04-18', venue: 'Place des Arts, Montreal',   status: 'CONFIRMED', booked: '2026-03-15' },
  { id: 3, event: 'Paris Weekend Getaway',           date: '2026-04-25', venue: 'Departure: Montreal-Trudeau',status: 'CANCELLED', booked: '2026-02-20' },
]

function formatDate(iso) {
  return new Date(iso).toLocaleDateString('en-CA', { month: 'short', day: 'numeric', year: 'numeric' })
}

export default function ReservationsPage() {
  const [reservations, setReservations] = useState(MOCK_RESERVATIONS)
  const [cancelling, setCancelling] = useState(null)

  const handleCancel = (id) => {
    setReservations(rs =>
      rs.map(r => r.id === id ? { ...r, status: 'CANCELLED' } : r)
    )
    setCancelling(null)
  }

  return (
    <div className="res-page">
      <div className="res-header">
        <h1 className="res-title">My Tickets</h1>
        <span className="res-count">{reservations.filter(r => r.status === 'CONFIRMED').length} active</span>
      </div>

      {reservations.length === 0 ? (
        <p className="res-empty">No reservations yet. <a href="/">Browse events →</a></p>
      ) : (
        <div className="res-list">
          {reservations.map((r) => (
            <div
              key={r.id}
              className={`res-item ${r.status === 'CANCELLED' ? 'cancelled' : ''}`}
            >
              <div className="res-item-left">
                <span className={`res-status ${r.status.toLowerCase()}`}>{r.status}</span>
                <h3 className="res-event">{r.event}</h3>
                <div className="res-meta">
                  <span>{formatDate(r.date)}</span>
                  <span className="res-sep">·</span>
                  <span>{r.venue}</span>
                </div>
                <p className="res-booked">Booked {formatDate(r.booked)}</p>
              </div>
              <div className="res-item-right">
                {r.status === 'CONFIRMED' && (
                  cancelling === r.id ? (
                    <div className="cancel-confirm">
                      <span>Cancel this reservation?</span>
                      <div className="cancel-actions">
                        <button className="btn-yes" onClick={() => handleCancel(r.id)}>Yes, cancel</button>
                        <button className="btn-no" onClick={() => setCancelling(null)}>Keep it</button>
                      </div>
                    </div>
                  ) : (
                    <button className="btn-cancel" onClick={() => setCancelling(r.id)}>Cancel</button>
                  )
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
