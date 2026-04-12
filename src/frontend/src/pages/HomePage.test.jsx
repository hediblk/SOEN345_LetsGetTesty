import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import HomePage from './HomePage'

const sampleEvents = [
  { id: 1, title: 'Rock Concert', category: 'Concerts', location: 'Montreal', date: '2025-08-01', price: 50, capacity: 200, isCancelled: false },
  { id: 2, title: 'Art Exhibition', category: 'Movies', location: 'Toronto', date: '2025-09-15', price: 0, capacity: 100, isCancelled: false },
  { id: 3, title: 'Football Game', category: 'Sports', location: 'Ottawa', date: '2025-07-01', price: 20, capacity: 50, isCancelled: true },
]

function renderHome(props = {}) {
  return render(
    <HomePage
      events={props.events ?? sampleEvents}
      eventsLoading={props.eventsLoading ?? false}
      eventsError={props.eventsError ?? null}
      reservations={props.reservations ?? []}
      onReserve={props.onReserve ?? vi.fn()}
    />
  )
}

describe('HomePage — rendering', () => {
  it('renders all events by default', () => {
    renderHome()
    expect(screen.getByText('Rock Concert')).toBeInTheDocument()
    expect(screen.getByText('Art Exhibition')).toBeInTheDocument()
    expect(screen.getByText('Football Game')).toBeInTheDocument()
  })

  it('shows loading message when eventsLoading is true', () => {
    renderHome({ eventsLoading: true })
    expect(screen.getByText('Loading events…')).toBeInTheDocument()
  })

  it('hides events grid while loading', () => {
    renderHome({ eventsLoading: true })
    expect(screen.queryByText('Rock Concert')).not.toBeInTheDocument()
  })

  it('shows error message with role="alert" when eventsError is set', () => {
    renderHome({ eventsError: 'Failed to load events' })
    expect(screen.getByRole('alert')).toHaveTextContent('Failed to load events')
  })

  it('shows no-results message when events list is empty', () => {
    renderHome({ events: [] })
    expect(screen.getByText('No events match your filters.')).toBeInTheDocument()
  })
})

describe('HomePage — filtering by category', () => {
  it('shows only matching events when a category is selected', () => {
    renderHome()
    fireEvent.click(screen.getByRole('button', { name: 'Concerts' }))
    expect(screen.getByText('Rock Concert')).toBeInTheDocument()
    expect(screen.queryByText('Art Exhibition')).not.toBeInTheDocument()
  })

  it('shows all events when All is selected', () => {
    renderHome()
    fireEvent.click(screen.getByRole('button', { name: 'Concerts' }))
    fireEvent.click(screen.getByRole('button', { name: 'All' }))
    expect(screen.getByText('Rock Concert')).toBeInTheDocument()
    expect(screen.getByText('Art Exhibition')).toBeInTheDocument()
  })

  it('shows no-results when no events match the selected category', () => {
    renderHome()
    fireEvent.click(screen.getByRole('button', { name: 'Travel' }))
    expect(screen.getByText('No events match your filters.')).toBeInTheDocument()
  })
})

describe('HomePage — filtering by search', () => {
  it('filters by title (case-insensitive)', () => {
    renderHome()
    fireEvent.change(screen.getByPlaceholderText('Search events or locations…'), { target: { value: 'rock' } })
    expect(screen.getByText('Rock Concert')).toBeInTheDocument()
    expect(screen.queryByText('Art Exhibition')).not.toBeInTheDocument()
  })

  it('filters by location (case-insensitive)', () => {
    renderHome()
    fireEvent.change(screen.getByPlaceholderText('Search events or locations…'), { target: { value: 'toronto' } })
    expect(screen.getByText('Art Exhibition')).toBeInTheDocument()
    expect(screen.queryByText('Rock Concert')).not.toBeInTheDocument()
  })

  it('shows no-results when nothing matches the search', () => {
    renderHome()
    fireEvent.change(screen.getByPlaceholderText('Search events or locations…'), { target: { value: 'zzznomatch' } })
    expect(screen.getByText('No events match your filters.')).toBeInTheDocument()
  })

  it('clears filter when search is cleared', () => {
    renderHome()
    fireEvent.change(screen.getByPlaceholderText('Search events or locations…'), { target: { value: 'rock' } })
    fireEvent.change(screen.getByPlaceholderText('Search events or locations…'), { target: { value: '' } })
    expect(screen.getByText('Art Exhibition')).toBeInTheDocument()
  })
})

describe('HomePage — filtering by date', () => {
  it('hides events before the selected date', () => {
    renderHome()
    fireEvent.change(screen.getByTitle('Filter from date'), { target: { value: '2025-09-01' } })
    expect(screen.queryByText('Rock Concert')).not.toBeInTheDocument()
    expect(screen.getByText('Art Exhibition')).toBeInTheDocument()
  })

  it('shows all events when date filter is cleared', () => {
    renderHome()
    fireEvent.change(screen.getByTitle('Filter from date'), { target: { value: '2025-09-01' } })
    fireEvent.change(screen.getByTitle('Filter from date'), { target: { value: '' } })
    expect(screen.getByText('Rock Concert')).toBeInTheDocument()
  })
})

describe('HomePage — event status badges', () => {
  it('shows Cancelled badge for cancelled events', () => {
    renderHome()
    expect(screen.getByText('Cancelled')).toBeInTheDocument()
  })

  it('shows Booked badge for events the user has confirmed reservations for', () => {
    renderHome({ reservations: [{ eventId: 1, status: 'CONFIRMED' }] })
    expect(screen.getByText('Booked')).toBeInTheDocument()
  })

  it('does not show Booked badge for CANCELLED reservations', () => {
    renderHome({ reservations: [{ eventId: 1, status: 'CANCELLED' }] })
    expect(screen.queryByText('Booked')).not.toBeInTheDocument()
  })

  it('shows Reserve button for available, unbooked events', () => {
    renderHome()
    const reserveButtons = screen.getAllByText('Reserve')
    expect(reserveButtons.length).toBeGreaterThan(0)
  })

  it('calls onReserve with the event id when Reserve is clicked', () => {
    const onReserve = vi.fn()
    renderHome({ onReserve })
    fireEvent.click(screen.getAllByText('Reserve')[0])
    expect(onReserve).toHaveBeenCalledWith(expect.any(Number))
  })
})

describe('HomePage — price display', () => {
  it('shows "Free" for events with price 0', () => {
    renderHome()
    expect(screen.getByText('Free')).toBeInTheDocument()
  })

  it('shows the price with $ prefix for paid events', () => {
    renderHome()
    expect(screen.getByText('$50')).toBeInTheDocument()
  })
})
