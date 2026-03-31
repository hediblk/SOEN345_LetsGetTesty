import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import './ReservationsPage.css'
import { cancelReservation } from '../api/reservationsApi'

function formatDate(iso) {
  return new Date(iso).toLocaleDateString('en-CA', { month: 'short', day: 'numeric', year: 'numeric' })
}

export default function ReservationsPage({ events, reservations, reservationsLoading, reservationsError }) {
  const [localReservations, setLocalReservations] = useState([])
  const [cancelling, setCancelling] = useState(null)

  const handleCancel = async (id) => {
    try {
      await cancelReservation(id)
      setLocalReservations(rs =>
        rs.map(r => r.id === id ? { ...r, status: 'CANCELLED' } : r)
      )
    } catch {
      alert('Failed to cancel reservation. Please try again.')
    } finally {
      setCancelling(null)
    }
  }

  useEffect(() => {
    if (!reservations || !events) return

    const eventMap = new Map(events.map(e => [e.id, e]))

    const mapped = reservations.map(r => {
      const event = eventMap.get(r.eventId)
      return {
        id: r.id,
        event: event?.title ?? 'Unknown Event',
        date: event?.date ?? '',
        venue: event?.location ?? '',
        status: r.status,
        booked: r.createdAt,
      }
    })

    setLocalReservations(mapped)
    console.log(mapped)
  }, [events, reservations])

  return (
    <div className="res-page">
      <div className="res-header">
        <h1 className="res-title">My Tickets</h1>
        <span className="res-count">{localReservations.filter(r => r.status === 'CONFIRMED').length} active</span>
      </div>

      {reservationsError ? (
        <p className="res-empty" role="alert">{reservationsError}</p>
      ) : reservationsLoading ? (
        <p className="res-empty">Loading reservations…</p>
      ) : localReservations.length === 0 ? (
        <p className="res-empty">No reservations yet. <Link to="/">Browse events →</Link></p>
      ) : (
        <div className="res-list">
          {localReservations.map((r) => (
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
