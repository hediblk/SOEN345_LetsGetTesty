import { useState } from 'react'
import './HomePage.css'

const ALL_EVENTS = [
  { id: 1, cat: 'Movies',   title: 'Dune: Messiah — World Premiere', date: '2026-04-15', location: 'Cinema Imperial, Montreal',    spots: 280, price: 22  },
  { id: 2, cat: 'Concerts', title: 'Montreal Jazz Night',             date: '2026-04-18', location: 'Place des Arts, Montreal',     spots: 118, price: 45  },
  { id: 3, cat: 'Sports',   title: 'Canadiens vs Maple Leafs',        date: '2026-04-22', location: 'Bell Centre, Montreal',        spots: 312, price: 89  },
  { id: 4, cat: 'Travel',   title: 'Paris Weekend Getaway',           date: '2026-04-25', location: 'Departure: Montreal-Trudeau',  spots: 60,  price: 699 },
  { id: 5, cat: 'Concerts', title: 'Arcade Fire — Live',              date: '2026-05-10', location: 'Bell Centre, Montreal',        spots: 54,  price: 120 },
  { id: 6, cat: 'Movies',   title: 'Blade Runner 2099 Screening',     date: '2026-05-12', location: 'Cineplex Forum, Montreal',     spots: 150, price: 18  },
  { id: 7, cat: 'Travel',   title: 'NYC Long Weekend Package',        date: '2026-05-16', location: 'Departure: Montreal-Trudeau',  spots: 45,  price: 399 },
  { id: 8, cat: 'Sports',   title: 'Grand Prix de Montreal',          date: '2026-06-07', location: 'Circuit Gilles Villeneuve',    spots: 890, price: 200 },
]

const CATS = ['All', 'Movies', 'Concerts', 'Travel', 'Sports']

function formatDate(iso) {
  return new Date(iso).toLocaleDateString('en-CA', { month: 'short', day: 'numeric', year: 'numeric' })
}

export default function HomePage() {
  const [search, setSearch]   = useState('')
  const [cat, setCat]         = useState('All')
  const [dateFrom, setDateFrom] = useState('')

  const events = ALL_EVENTS.filter(ev => {
    const matchCat    = cat === 'All' || ev.cat === cat
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
        {events.length === 0 ? (
          <p className="no-results">No events match your filters.</p>
        ) : (
          <div className="events-grid">
            {events.map((ev) => (
              <article
                className="event-card"
                key={ev.id}

              >
                <div className="card-top">
                  <span className="ev-cat">{ev.cat}</span>
                  <span className="ev-date">{formatDate(ev.date)}</span>
                </div>
                <h3 className="ev-title">{ev.title}</h3>
                <p className="ev-location">{ev.location}</p>
                <div className="card-footer">
                  <div className="ev-meta">
                    <span className="ev-spots">{ev.spots} spots</span>
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
