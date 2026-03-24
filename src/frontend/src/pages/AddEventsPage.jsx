import { useState } from 'react'
import './AddEventsPage.css'

const CATEGORIES = ['Movies', 'Concerts', 'Travel', 'Sports']

const INITIAL_FORM = {
  title: '',
  date: '',
  category: 'Movies',
  location: '',
  price: '0',
  capacity: '50',
}

function formatDate(isoDate) {
  return new Date(isoDate).toLocaleDateString('en-CA', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  })
}

export default function AddEventsPage({ events, setEvents }) {
  const [form, setForm] = useState(INITIAL_FORM)
  const [notice, setNotice] = useState('')

  function handleChange(event) {
    const { name, value } = event.target
    setForm(current => ({ ...current, [name]: value }))
  }

  function handleSubmit(event) {
    event.preventDefault()

    const nextEvent = {
      id: Date.now(),
      title: form.title.trim(),
      date: form.date,
      category: form.category,
      location: form.location.trim(),
      price: Number(form.price),
      capacity: Number(form.capacity),
    }

    setEvents(current => [nextEvent, ...current])
    setForm(INITIAL_FORM)
    setNotice(`Added ${nextEvent.title} to the demo event list.`)
  }

  return (
    <div className="add-events-page">
      <section className="add-events-hero">
        <div>
          <p className="eyebrow">Event Management</p>
          <h1 className="add-events-title">Create a new event.</h1>
          <p className="add-events-subtitle">
            Fill out the form below to add an event.
          </p>
        </div>
      </section>

      <section className="add-events-content">
        <form className="event-form-card" onSubmit={handleSubmit}>
          <div className="section-heading">
            <h2>Event Details</h2>
          </div>

          <label className="field-group">
            <span>Title</span>
            <input
              className="field-input"
              type="text"
              name="title"
              placeholder="Example: Montreal Comedy Night"
              value={form.title}
              onChange={handleChange}
              required
            />
          </label>

          <label className="field-group">
            <span>Location</span>
            <input
              className="field-input"
              type="text"
              name="location"
              placeholder="Example: Bell Centre, Montreal"
              value={form.location}
              onChange={handleChange}
              required
            />
          </label>

          <div className="field-grid">
            <label className="field-group">
              <span>Date</span>
              <input
                className="field-input"
                type="date"
                name="date"
                value={form.date}
                onChange={handleChange}
                required
              />
            </label>

            <label className="field-group">
              <span>Category</span>
              <select
                className="field-input"
                name="category"
                value={form.category}
                onChange={handleChange}
              >
                {CATEGORIES.map(category => (
                  <option key={category} value={category}>
                    {category}
                  </option>
                ))}
              </select>
            </label>
          </div>

          <div className="field-grid">
            <label className="field-group">
              <span>Price</span>
              <input
                className="field-input"
                type="number"
                name="price"
                min="0"
                step="1"
                value={form.price}
                onChange={handleChange}
                required
              />
            </label>

            <label className="field-group">
              <span>Capacity</span>
              <input
                className="field-input"
                type="number"
                name="capacity"
                min="1"
                step="1"
                value={form.capacity}
                onChange={handleChange}
                required
              />
            </label>
          </div>

          <div className="form-actions">
            <button className="primary-action" type="submit">Add Event</button>
            {notice ? <p className="form-notice">{notice}</p> : null}
          </div>
        </form>

        <aside className="event-preview-panel">
          <div className="section-heading">
            <h2>{events.length} Recent Events</h2>
          </div>

          <div className="preview-list">
            {events.map(eventItem => (
              <article className="preview-card" key={eventItem.id}>
                <div className="preview-top">
                  <span className="preview-category">{eventItem.category}</span>
                  <span className="preview-date">{formatDate(eventItem.date)}</span>
                </div>
                <h3>{eventItem.title}</h3>
                <p className="preview-location">{eventItem.location}</p>
                <div className="preview-meta">
                  <span>{eventItem.capacity} seats</span>
                  <span>{eventItem.price === 0 ? 'Free' : `$${eventItem.price}`}</span>
                </div>
              </article>
            ))}
          </div>
        </aside>
      </section>
    </div>
  )
}