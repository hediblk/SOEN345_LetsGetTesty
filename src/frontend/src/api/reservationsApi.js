import { getStoredAuthSession } from '../auth/authStorage'

const RESERVATIONS_URL = '/api/reservations'

async function readErrorMessage(response) {
  try {
    const body = await response.json()
    if (body && typeof body.message === 'string') {
      return body.message
    }
  } catch {
    // ignore
  }
  return `Request failed (${response.status})`
}

export async function fetchReservations() {
  const res = await fetch(RESERVATIONS_URL, {
    headers: withAuthHeaders(),
  })
  if (!res.ok) {
    throw await buildError(res)
  }
  return res.json()
}

export async function fetchReservationsByUser(userId) {
  const res = await fetch(`${RESERVATIONS_URL}/user/${userId}`, {
    headers: withAuthHeaders(),
  })
  if (!res.ok) {
    throw await buildError(res)
  }
  return res.json()
}

export async function fetchReservationsByEvent(eventId) {
  const res = await fetch(`${RESERVATIONS_URL}/event/${eventId}`, {
    headers: withAuthHeaders(),
  })
  if (!res.ok) {
    throw await buildError(res)
  }
  return res.json()
}

export async function createReservation(payload) {
  const res = await fetch(RESERVATIONS_URL, {
    method: 'POST',
    headers: withAuthHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw await buildError(res)
  }
  return res.json()
}

export async function cancelReservation(id) {
  const res = await fetch(`${RESERVATIONS_URL}/${id}/cancel`, {
    method: 'PATCH',
    headers: withAuthHeaders(),
  })
  if (!res.ok) {
    throw await buildError(res)
  }
  return res.json()
}

async function buildError(response) {
  const error = new Error(await readErrorMessage(response))
  error.status = response.status
  return error
}

function withAuthHeaders(headers = {}) {
  const session = getStoredAuthSession()
  if (!session?.token) {
    return headers
  }

  return {
    ...headers,
    Authorization: `Bearer ${session.token}`,
  }
}
