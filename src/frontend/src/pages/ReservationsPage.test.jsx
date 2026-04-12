import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import ReservationsPage from './ReservationsPage'

vi.mock('../api/reservationsApi', () => ({ cancelReservation: vi.fn() }))

import { cancelReservation } from '../api/reservationsApi'

const sampleEvents = [
  { id: 1, title: 'Rock Concert', location: 'Montreal', date: '2025-08-01' },
  { id: 2, title: 'Art Exhibition', location: 'Toronto', date: '2025-09-15' },
]

const sampleReservations = [
  { id: 101, eventId: 1, status: 'CONFIRMED', createdAt: '2025-07-01T12:00:00Z' },
  { id: 102, eventId: 2, status: 'CANCELLED', createdAt: '2025-07-02T12:00:00Z' },
]

function renderPage(props = {}) {
  return render(
    <MemoryRouter>
      <ReservationsPage
        events={props.events ?? sampleEvents}
        reservations={props.reservations ?? sampleReservations}
        reservationsLoading={props.reservationsLoading ?? false}
        reservationsError={props.reservationsError ?? null}
      />
    </MemoryRouter>
  )
}

describe('ReservationsPage — rendering', () => {
  it('renders the page title', () => {
    renderPage()
    expect(screen.getByText('My Tickets')).toBeInTheDocument()
  })

  it('shows active reservation count', () => {
    renderPage()
    expect(screen.getByText('1 active')).toBeInTheDocument()
  })

  it('shows loading state', () => {
    renderPage({ reservations: [], reservationsLoading: true })
    expect(screen.getByText('Loading reservations…')).toBeInTheDocument()
  })

  it('shows error message with role="alert" when reservationsError is set', () => {
    renderPage({ reservations: [], reservationsError: 'Network error' })
    expect(screen.getByRole('alert')).toHaveTextContent('Network error')
  })

  it('shows empty state with browse link when no reservations', () => {
    renderPage({ reservations: [] })
    expect(screen.getByText(/No reservations yet/)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /Browse events/i })).toBeInTheDocument()
  })

  it('renders event titles for each reservation', () => {
    renderPage()
    expect(screen.getByText('Rock Concert')).toBeInTheDocument()
    expect(screen.getByText('Art Exhibition')).toBeInTheDocument()
  })

  it('renders the venue for each reservation', () => {
    renderPage()
    expect(screen.getByText('Montreal')).toBeInTheDocument()
    expect(screen.getByText('Toronto')).toBeInTheDocument()
  })

  it('shows CONFIRMED status badge', () => {
    renderPage()
    expect(screen.getByText('CONFIRMED')).toBeInTheDocument()
  })

  it('shows CANCELLED status badge', () => {
    renderPage()
    expect(screen.getByText('CANCELLED')).toBeInTheDocument()
  })

  it('shows Cancel button only for CONFIRMED reservations', () => {
    renderPage()
    expect(screen.getAllByRole('button', { name: 'Cancel' }).length).toBe(1)
  })

  it('does not show Cancel button for CANCELLED reservations', () => {
    renderPage({ reservations: [{ id: 102, eventId: 2, status: 'CANCELLED', createdAt: '2025-07-02T12:00:00Z' }] })
    expect(screen.queryByRole('button', { name: 'Cancel' })).not.toBeInTheDocument()
  })

  it('shows "Unknown Event" when event id does not match any event', () => {
    renderPage({
      events: [],
      reservations: [{ id: 101, eventId: 999, status: 'CONFIRMED', createdAt: '2025-07-01T12:00:00Z' }],
    })
    expect(screen.getByText('Unknown Event')).toBeInTheDocument()
  })
})

describe('ReservationsPage — cancel flow', () => {
  beforeEach(() => {
    cancelReservation.mockReset()
  })

  it('shows confirmation prompt when Cancel is clicked', () => {
    renderPage()
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }))
    expect(screen.getByText('Cancel this reservation?')).toBeInTheDocument()
  })

  it('hides the Cancel button and shows Yes/Keep options after clicking Cancel', () => {
    renderPage()
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }))
    expect(screen.getByRole('button', { name: 'Yes, cancel' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Keep it' })).toBeInTheDocument()
  })

  it('calls cancelReservation with the correct id when confirmed', async () => {
    cancelReservation.mockResolvedValue(undefined)
    renderPage()
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }))
    fireEvent.click(screen.getByRole('button', { name: 'Yes, cancel' }))
    await waitFor(() => {
      expect(cancelReservation).toHaveBeenCalledWith(101)
    })
  })

  it('updates the reservation status to CANCELLED locally after confirmation', async () => {
    cancelReservation.mockResolvedValue(undefined)
    renderPage()
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }))
    fireEvent.click(screen.getByRole('button', { name: 'Yes, cancel' }))
    await waitFor(() => {
      expect(screen.getAllByText('CANCELLED').length).toBe(2)
    })
  })

  it('dismisses the confirmation when Keep it is clicked', () => {
    renderPage()
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }))
    fireEvent.click(screen.getByRole('button', { name: 'Keep it' }))
    expect(screen.queryByText('Cancel this reservation?')).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeInTheDocument()
  })

  it('shows alert when cancellation fails', async () => {
    cancelReservation.mockRejectedValue(new Error('Cancellation failed'))
    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {})
    renderPage()
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }))
    fireEvent.click(screen.getByRole('button', { name: 'Yes, cancel' }))
    await waitFor(() => {
      expect(alertSpy).toHaveBeenCalledWith('Failed to cancel reservation. Please try again.')
    })
    alertSpy.mockRestore()
  })
})
