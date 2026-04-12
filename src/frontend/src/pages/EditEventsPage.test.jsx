import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import EditEventsPage from './EditEventsPage'

vi.mock('../api/eventsApi', () => ({
  updateEvent: vi.fn(),
  cancelEvent: vi.fn(),
}))

import { updateEvent, cancelEvent } from '../api/eventsApi'

const activeEvents = [
  { id: 1, title: 'Rock Concert', category: 'Concerts', location: 'Montreal', date: '2025-08-01', price: 50, capacity: 200, isCancelled: false },
  { id: 2, title: 'Art Exhibition', category: 'Movies', location: 'Toronto', date: '2025-09-15', price: 0, capacity: 100, isCancelled: false },
]

const mixedEvents = [
  ...activeEvents,
  { id: 3, title: 'Old Show', category: 'Movies', location: 'Ottawa', date: '2025-07-01', price: 20, capacity: 50, isCancelled: true },
]

function renderPage(props = {}) {
  return render(
    <EditEventsPage
      events={props.events ?? activeEvents}
      onEventsChanged={props.onEventsChanged ?? vi.fn()}
    />
  )
}

describe('EditEventsPage — rendering', () => {
  it('renders the page title', () => {
    renderPage()
    expect(screen.getByText('Edit existing events.')).toBeInTheDocument()
  })

  it('shows "No editable events" option when all events are cancelled', () => {
    renderPage({ events: [{ id: 3, title: 'Old Show', category: 'Movies', location: 'Ottawa', date: '2025-07-01', price: 20, capacity: 50, isCancelled: true }] })
    expect(screen.getByText('No editable events')).toBeInTheDocument()
  })

  it('pre-fills form with the first non-cancelled event title', () => {
    renderPage()
    expect(screen.getByDisplayValue('Rock Concert')).toBeInTheDocument()
  })

  it('pre-fills location for the first selected event', () => {
    renderPage()
    expect(screen.getByDisplayValue('Montreal')).toBeInTheDocument()
  })

  it('shows Cancelled status pill for cancelled events in the sidebar', () => {
    renderPage({ events: mixedEvents })
    expect(screen.getByText('Cancelled')).toBeInTheDocument()
  })

  it('shows "Select to Edit" button for non-selected active events', () => {
    renderPage()
    expect(screen.getByRole('button', { name: 'Select to Edit' })).toBeInTheDocument()
  })

  it('shows "Currently Editing" for the currently selected event', () => {
    renderPage()
    expect(screen.getByRole('button', { name: 'Currently Editing' })).toBeInTheDocument()
  })

  it('shows "No events available." in the sidebar when events list is empty', () => {
    renderPage({ events: [] })
    expect(screen.getByText('No events available.')).toBeInTheDocument()
  })

  it('shows both event titles in the sidebar', () => {
    renderPage({ events: mixedEvents })
    expect(screen.getByText('Rock Concert')).toBeInTheDocument()
    expect(screen.getByText('Art Exhibition')).toBeInTheDocument()
    expect(screen.getByText('Old Show')).toBeInTheDocument()
  })
})

describe('EditEventsPage — event selection', () => {
  it('updates form when a different event is selected from the dropdown', () => {
    renderPage()
    const selects = screen.getAllByRole('combobox')
    fireEvent.change(selects[0], { target: { value: '2' } })
    expect(screen.getByDisplayValue('Art Exhibition')).toBeInTheDocument()
    expect(screen.getByDisplayValue('Toronto')).toBeInTheDocument()
  })

  it('updates form when "Select to Edit" is clicked in the sidebar', () => {
    renderPage()
    fireEvent.click(screen.getByRole('button', { name: 'Select to Edit' }))
    expect(screen.getByDisplayValue('Art Exhibition')).toBeInTheDocument()
  })

  it('updates location when a different event is selected', () => {
    renderPage()
    const selects = screen.getAllByRole('combobox')
    fireEvent.change(selects[0], { target: { value: '2' } })
    expect(screen.getByDisplayValue('Toronto')).toBeInTheDocument()
  })
})

describe('EditEventsPage — form submission', () => {
  beforeEach(() => {
    updateEvent.mockReset()
    cancelEvent.mockReset()
  })

  it('shows success notice after saving changes', async () => {
    updateEvent.mockResolvedValue({ title: 'Rock Concert' })
    const onEventsChanged = vi.fn().mockResolvedValue(undefined)
    renderPage({ onEventsChanged })

    fireEvent.submit(screen.getByRole('button', { name: 'Save Changes' }).closest('form'))

    await waitFor(() => {
      expect(screen.getByText('Updated Rock Concert.')).toBeInTheDocument()
    })
    expect(onEventsChanged).toHaveBeenCalled()
  })

  it('shows error notice when updateEvent fails', async () => {
    updateEvent.mockRejectedValue(new Error('Update failed'))
    renderPage()

    fireEvent.submit(screen.getByRole('button', { name: 'Save Changes' }).closest('form'))

    await waitFor(() => {
      expect(screen.getByText('Update failed')).toBeInTheDocument()
    })
  })

  it('disables Save Changes button while submitting', async () => {
    let resolve
    updateEvent.mockReturnValue(new Promise(r => { resolve = r }))
    renderPage()

    fireEvent.submit(screen.getByRole('button', { name: 'Save Changes' }).closest('form'))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Saving...' })).toBeDisabled()
    })
    resolve({ title: 'Rock Concert' })
  })

  it('disables Cancel Selected Event button while submitting', async () => {
    let resolve
    updateEvent.mockReturnValue(new Promise(r => { resolve = r }))
    renderPage()

    fireEvent.submit(screen.getByRole('button', { name: 'Save Changes' }).closest('form'))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Cancel Selected Event' })).toBeDisabled()
    })
    resolve({ title: 'Rock Concert' })
  })
})

describe('EditEventsPage — cancel event', () => {
  beforeEach(() => {
    cancelEvent.mockReset()
  })

  it('calls cancelEvent with the selected event id', async () => {
    cancelEvent.mockResolvedValue(undefined)
    const onEventsChanged = vi.fn().mockResolvedValue(undefined)
    renderPage({ onEventsChanged })

    fireEvent.click(screen.getByRole('button', { name: 'Cancel Selected Event' }))

    await waitFor(() => {
      expect(cancelEvent).toHaveBeenCalledWith(1)
    })
    expect(onEventsChanged).toHaveBeenCalled()
  })

  it('shows success notice after cancelling an event', async () => {
    cancelEvent.mockResolvedValue(undefined)
    const onEventsChanged = vi.fn().mockResolvedValue(undefined)
    renderPage({ onEventsChanged })

    fireEvent.click(screen.getByRole('button', { name: 'Cancel Selected Event' }))

    await waitFor(() => {
      expect(screen.getByText('Cancelled Rock Concert.')).toBeInTheDocument()
    })
  })

  it('shows error notice when cancelEvent fails', async () => {
    cancelEvent.mockRejectedValue(new Error('Cannot cancel'))
    renderPage()

    fireEvent.click(screen.getByRole('button', { name: 'Cancel Selected Event' }))

    await waitFor(() => {
      expect(screen.getByText('Cannot cancel')).toBeInTheDocument()
    })
  })

  it('shows "Cancelling..." on the button while the request is in flight', async () => {
    let resolve
    cancelEvent.mockReturnValue(new Promise(r => { resolve = r }))
    renderPage()

    fireEvent.click(screen.getByRole('button', { name: 'Cancel Selected Event' }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Cancelling...' })).toBeInTheDocument()
    })
    resolve()
  })
})
