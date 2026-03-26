import { useEffect, useMemo, useState } from 'react'
import { cancelEvent, updateEvent } from '../api/eventsApi'
import {
  buildEventFormFromEvent,
  buildUpdateEventPayload,
  CATEGORY_OPTIONS,
  formatEventDate,
} from '../data/events'
import './EditEventsPage.css'

const EMPTY_FORM = {
  title: '',
  date: '',
  category: 'Movies',
  location: '',
  price: '0',
  capacity: '50',
}

export default function EditEventsPage({ events, onEventsChanged }) {
  const editableEvents = useMemo(() => events.filter(eventItem => !eventItem.isCancelled), [events])
  const [selectedId, setSelectedId] = useState('')
  const [form, setForm] = useState(EMPTY_FORM)
  const [notice, setNotice] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [pendingCancelId, setPendingCancelId] = useState(null)

  const selectedEvent = editableEvents.find(eventItem => String(eventItem.id) === selectedId) || null

  useEffect(() => {
    if (editableEvents.length === 0) {
      setSelectedId('')
      setForm(EMPTY_FORM)
      return
    }

    const hasSelected = editableEvents.some(eventItem => String(eventItem.id) === selectedId)
    const activeId = hasSelected ? selectedId : String(editableEvents[0].id)
    setSelectedId(activeId)

    const selectedEvent = editableEvents.find(eventItem => String(eventItem.id) === activeId)
    setForm(buildEventFormFromEvent(selectedEvent))
  }, [editableEvents, selectedId])

  function handleSelectChange(event) {
    const nextId = event.target.value
    setSelectedId(nextId)
    const selectedEvent = editableEvents.find(eventItem => String(eventItem.id) === nextId)
    setForm(buildEventFormFromEvent(selectedEvent))
    setNotice('')
  }

  function handleFieldChange(event) {
    const { name, value } = event.target
    setForm(current => ({ ...current, [name]: value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    if (!selectedId) {
      return
    }

    setSubmitting(true)
    setNotice('')
    const title = form.title.trim()
    try {
      const payload = buildUpdateEventPayload(form)
      const updated = await updateEvent(selectedId, payload)
      await onEventsChanged()
      const serverTitle = updated?.title?.trim() || title
      setNotice(`Updated ${serverTitle}.`)
    } catch (err) {
      setNotice(err.message || 'Could not update event.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleCancelSelected() {
    if (!selectedEvent || pendingCancelId !== null) {
      return
    }

    setPendingCancelId(selectedEvent.id)
    setNotice('')
    try {
      await cancelEvent(selectedEvent.id)
      await onEventsChanged()
      setNotice(`Cancelled ${selectedEvent.title}.`)
    } catch (err) {
      setNotice(err.message || 'Could not cancel event.')
    } finally {
      setPendingCancelId(null)
    }
  }

  return (
    <div className="edit-events-page">
      <section className="edit-events-hero">
        <div>
          <p className="eyebrow">Event Management</p>
          <h1 className="edit-events-title">Edit existing events.</h1>
          <p className="edit-events-subtitle">
            Select an event and update its details.
          </p>
        </div>
      </section>

      <section className="edit-events-content">
        <form className="event-form-card" onSubmit={handleSubmit}>
          <div className="section-heading">
            <h2>Update Event</h2>
            <p>Edit active events or cancel the selected one.</p>
          </div>

          <label className="field-group">
            <span>Choose Event</span>
            <select
              className="field-input"
              value={selectedId}
              onChange={handleSelectChange}
              disabled={editableEvents.length === 0 || submitting}
            >
              {editableEvents.length === 0 ? <option value="">No editable events</option> : null}
              {editableEvents.map(eventItem => (
                <option key={eventItem.id} value={eventItem.id}>
                  {eventItem.title} ({formatEventDate(eventItem.date)})
                </option>
              ))}
            </select>
          </label>

          <label className="field-group">
            <span>Title</span>
            <input
              className="field-input"
              type="text"
              name="title"
              value={form.title}
              onChange={handleFieldChange}
              required
              disabled={!selectedId || submitting}
            />
          </label>

          <label className="field-group">
            <span>Location</span>
            <input
              className="field-input"
              type="text"
              name="location"
              value={form.location}
              onChange={handleFieldChange}
              required
              disabled={!selectedId || submitting}
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
                onChange={handleFieldChange}
                required
                disabled={!selectedId || submitting}
              />
            </label>

            <label className="field-group">
              <span>Category</span>
              <select
                className="field-input"
                name="category"
                value={form.category}
                onChange={handleFieldChange}
                disabled={!selectedId || submitting}
              >
                {CATEGORY_OPTIONS.map(category => (
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
                onChange={handleFieldChange}
                required
                disabled={!selectedId || submitting}
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
                onChange={handleFieldChange}
                required
                disabled={!selectedId || submitting}
              />
            </label>
          </div>

          <div className="form-actions">
            <button
              className="primary-action"
              type="submit"
              disabled={!selectedId || submitting || pendingCancelId !== null}
            >
              {submitting ? 'Saving...' : 'Save Changes'}
            </button>
            <button
              className="secondary-danger-action"
              type="button"
              disabled={!selectedId || submitting || pendingCancelId !== null}
              onClick={handleCancelSelected}
            >
              {pendingCancelId === selectedEvent?.id ? 'Cancelling...' : 'Cancel Selected Event'}
            </button>
            {notice ? <p className="form-notice">{notice}</p> : null}
          </div>
        </form>

        <aside className="event-preview-panel">
          <div className="section-heading">
            <h2>Event Status</h2>
            <p>Cancelled events stay visible but cannot be edited.</p>
          </div>

          <div className="preview-list">
            {events.length === 0 ? (
              <p className="empty-state">No events available.</p>
            ) : (
              events.map(eventItem => (
                <article
                  className={selectedEvent?.id === eventItem.id ? 'preview-card preview-card-selected' : 'preview-card'}
                  key={eventItem.id}
                >
                  <div className="preview-top">
                    <span className="preview-category">{eventItem.category}</span>
                    <span className="preview-date">{formatEventDate(eventItem.date)}</span>
                  </div>
                  <h3>{eventItem.title}</h3>
                  <p className="preview-location">{eventItem.location}</p>
                  <div className="preview-meta">
                    <span>{eventItem.capacity} seats</span>
                    <span>{eventItem.price === 0 ? 'Free' : `$${eventItem.price}`}</span>
                  </div>
                  <div className="preview-actions">
                    {eventItem.isCancelled ? (
                      <span className="status-pill status-cancelled">Cancelled</span>
                    ) : (
                      <button
                        className="preview-select-action"
                        type="button"
                        onClick={() => handleSelectChange({ target: { value: String(eventItem.id) } })}
                        disabled={submitting || pendingCancelId !== null}
                      >
                        {selectedEvent?.id === eventItem.id ? 'Currently Editing' : 'Select to Edit'}
                      </button>
                    )}
                  </div>
                </article>
              ))
            )}
          </div>
        </aside>
      </section>
    </div>
  )
}
