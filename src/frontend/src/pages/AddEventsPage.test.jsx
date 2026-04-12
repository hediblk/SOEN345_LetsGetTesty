import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import AddEventsPage from './AddEventsPage'

vi.mock('../api/eventsApi', () => ({ createEvent: vi.fn() }))

import { createEvent } from '../api/eventsApi'

const sampleEvents = [
  { id: 1, title: 'Rock Concert', category: 'Concerts', location: 'Montreal', date: '2025-08-01', price: 50, capacity: 200 },
  { id: 2, title: 'Art Exhibition', category: 'Movies', location: 'Toronto', date: '2025-09-15', price: 0, capacity: 100 },
]

function renderPage(props = {}) {
  return render(
    <AddEventsPage
      events={props.events ?? sampleEvents}
      onEventsChanged={props.onEventsChanged ?? vi.fn()}
    />
  )
}

describe('AddEventsPage — rendering', () => {
  it('renders the page title', () => {
    renderPage()
    expect(screen.getByText('Create a new event.')).toBeInTheDocument()
  })

  it('renders title and location inputs', () => {
    renderPage()
    expect(screen.getByPlaceholderText('Example: Montreal Comedy Night')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Example: Bell Centre, Montreal')).toBeInTheDocument()
  })

  it('renders the Add Event submit button', () => {
    renderPage()
    expect(screen.getByRole('button', { name: 'Add Event' })).toBeInTheDocument()
  })

  it('shows event count in the sidebar heading', () => {
    renderPage()
    expect(screen.getByText('2 Recent Events')).toBeInTheDocument()
  })

  it('renders event titles in the sidebar', () => {
    renderPage()
    expect(screen.getByText('Rock Concert')).toBeInTheDocument()
    expect(screen.getByText('Art Exhibition')).toBeInTheDocument()
  })

  it('shows "Free" for events with price 0', () => {
    renderPage()
    expect(screen.getByText('Free')).toBeInTheDocument()
  })

  it('shows price with $ for paid events', () => {
    renderPage()
    expect(screen.getByText('$50')).toBeInTheDocument()
  })

  it('shows 0 Recent Events when events list is empty', () => {
    renderPage({ events: [] })
    expect(screen.getByText('0 Recent Events')).toBeInTheDocument()
  })
})

describe('AddEventsPage — form interaction', () => {
  it('updates title field on input', () => {
    renderPage()
    const titleInput = screen.getByPlaceholderText('Example: Montreal Comedy Night')
    fireEvent.change(titleInput, { target: { value: 'Jazz Night' } })
    expect(titleInput.value).toBe('Jazz Night')
  })

  it('updates location field on input', () => {
    renderPage()
    const locationInput = screen.getByPlaceholderText('Example: Bell Centre, Montreal')
    fireEvent.change(locationInput, { target: { value: 'Place des Arts' } })
    expect(locationInput.value).toBe('Place des Arts')
  })

  it('category select defaults to Movies', () => {
    renderPage()
    expect(screen.getByDisplayValue('Movies')).toBeInTheDocument()
  })

  it('updates category when changed', () => {
    renderPage()
    const categorySelect = screen.getByDisplayValue('Movies')
    fireEvent.change(categorySelect, { target: { value: 'Concerts' } })
    expect(screen.getByDisplayValue('Concerts')).toBeInTheDocument()
  })

  it('price field defaults to 0', () => {
    renderPage()
    expect(screen.getByDisplayValue('0')).toBeInTheDocument()
  })

  it('capacity field defaults to 50', () => {
    renderPage()
    expect(screen.getByDisplayValue('50')).toBeInTheDocument()
  })
})

describe('AddEventsPage — form submission', () => {
  beforeEach(() => {
    createEvent.mockReset()
  })

  it('shows success notice after adding an event', async () => {
    createEvent.mockResolvedValue({ title: 'Jazz Night' })
    const onEventsChanged = vi.fn().mockResolvedValue(undefined)
    renderPage({ onEventsChanged })

    fireEvent.change(screen.getByPlaceholderText('Example: Montreal Comedy Night'), { target: { value: 'Jazz Night' } })
    fireEvent.change(screen.getByPlaceholderText('Example: Bell Centre, Montreal'), { target: { value: 'Place des Arts' } })
    fireEvent.submit(screen.getByRole('button', { name: 'Add Event' }).closest('form'))

    await waitFor(() => {
      expect(screen.getByText('Added Jazz Night to the event list.')).toBeInTheDocument()
    })
    expect(onEventsChanged).toHaveBeenCalled()
  })

  it('shows error notice when createEvent fails', async () => {
    createEvent.mockRejectedValue(new Error('Server error'))
    renderPage()

    fireEvent.change(screen.getByPlaceholderText('Example: Montreal Comedy Night'), { target: { value: 'Test Event' } })
    fireEvent.change(screen.getByPlaceholderText('Example: Bell Centre, Montreal'), { target: { value: 'Venue' } })
    fireEvent.submit(screen.getByRole('button', { name: 'Add Event' }).closest('form'))

    await waitFor(() => {
      expect(screen.getByText('Server error')).toBeInTheDocument()
    })
  })

  it('disables the submit button while submitting', async () => {
    let resolve
    createEvent.mockReturnValue(new Promise(r => { resolve = r }))
    const onEventsChanged = vi.fn().mockResolvedValue(undefined)
    renderPage({ onEventsChanged })

    fireEvent.change(screen.getByPlaceholderText('Example: Montreal Comedy Night'), { target: { value: 'Test Event' } })
    fireEvent.change(screen.getByPlaceholderText('Example: Bell Centre, Montreal'), { target: { value: 'Venue' } })
    fireEvent.submit(screen.getByRole('button', { name: 'Add Event' }).closest('form'))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Adding…' })).toBeDisabled()
    })
    resolve({ title: 'Test Event' })
  })

  it('resets the form after successful submission', async () => {
    createEvent.mockResolvedValue({ title: 'Jazz Night' })
    const onEventsChanged = vi.fn().mockResolvedValue(undefined)
    renderPage({ onEventsChanged })

    const titleInput = screen.getByPlaceholderText('Example: Montreal Comedy Night')
    fireEvent.change(titleInput, { target: { value: 'Jazz Night' } })
    fireEvent.change(screen.getByPlaceholderText('Example: Bell Centre, Montreal'), { target: { value: 'Place des Arts' } })
    fireEvent.submit(screen.getByRole('button', { name: 'Add Event' }).closest('form'))

    await waitFor(() => {
      expect(titleInput.value).toBe('')
    })
  })
})
