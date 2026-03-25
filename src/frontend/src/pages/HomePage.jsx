import { useState } from 'react'
import { formatEventDate } from '../data/events'
import './HomePage.css'

const CATS = ['All', 'Movies', 'Concerts', 'Travel', 'Sports']

export default function HomePage({ events, eventsLoading, eventsError }) {
  const [search, setSearch]   = useState('')
  const [cat, setCat]         = useState('All')
  const [dateFrom, setDateFrom] = useState('')

  const filteredEvents = events.filter(ev => {
    const matchCat    = cat === 'All' || ev.category === cat
    const matchSearch = ev.title.toLowerCase().includes(search.toLowerCase()) ||
                        ev.location.toLowerCase().includes(search.toLowerCase())
    const matchDate   = !dateFrom || ev.date >= dateFrom
    return matchCat && matchSearch && matchDate
  })

  return (
    <div className="home">
      <section className="hero">
        <h1 className="hero-title">
          Find your next<br />
          unforgettable event.
        </h1>
      </section>

      {eventsError ? (
        <p className="no-results" role="alert">{eventsError}</p>
      ) : null}

      <section className="filters">
        <input
          className="search-input"
          type="text"
          placeholder="Search events or locations…"
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <div className="filter-row">
          <div className="cat-pills">
            {CATS.map(c => (
              <button
                key={c}
                className={`cat-pill ${cat === c ? 'active' : ''}`}
                onClick={() => setCat(c)}
              >
                {c}
              </button>
            ))}
          </div>
          <input
            className="date-input"
            type="date"
            value={dateFrom}
            onChange={e => setDateFrom(e.target.value)}
            title="Filter from date"
          />
        </div>
      </section>

      <section className="events-grid-wrap">
        {eventsLoading ? (
          <p className="no-results">Loading events…</p>
        ) : eventsError ? null : filteredEvents.length === 0 ? (
          <p className="no-results">No events match your filters.</p>
        ) : (
          <div className="events-grid">
            {filteredEvents.map((ev) => (
              <article
                className="event-card"
                key={ev.id}

              >
                <div className="card-top">
                  <span className="ev-cat">{ev.category}</span>
                  <span className="ev-date">{formatEventDate(ev.date)}</span>
                </div>
                <h3 className="ev-title">{ev.title}</h3>
                <p className="ev-location">{ev.location}</p>
                <div className="card-footer">
                  <div className="ev-meta">
                    <span className="ev-spots">{ev.capacity} spots</span>
                    <span className="ev-price">{ev.price === 0 ? 'Free' : `$${ev.price}`}</span>
                  </div>
                  <button className="btn-reserve">Reserve</button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
