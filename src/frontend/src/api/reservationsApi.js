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
  const res = await fetch(RESERVATIONS_URL)
  if (!res.ok) {
    throw new Error(await readErrorMessage(res))
  }
  return res.json()
}

export async function fetchReservationsByUser(userId) {
  const res = await fetch(`${RESERVATIONS_URL}/user/${userId}`)
  if (!res.ok) {
    throw new Error(await readErrorMessage(res))
  }
  return res.json()
}

export async function fetchReservationsByEvent(eventId) {
  const res = await fetch(`${RESERVATIONS_URL}/event/${eventId}`)
  if (!res.ok) {
    throw new Error(await readErrorMessage(res))
  }
  return res.json()
}

export async function createReservation(payload) {
  const res = await fetch(RESERVATIONS_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error(await readErrorMessage(res))
  }
  return res.json()
}

export async function cancelReservation(id) {
  const res = await fetch(`${RESERVATIONS_URL}/${id}/cancel`, {
    method: 'PATCH',
  })
  if (!res.ok) {
    throw new Error(await readErrorMessage(res))
  }
  return res.json()
}