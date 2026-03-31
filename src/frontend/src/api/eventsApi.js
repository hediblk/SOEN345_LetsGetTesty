import { getStoredAuthSession } from '../auth/authStorage'

const EVENTS_URL = '/api/events'

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

export async function fetchEvents() {
  const res = await fetch(EVENTS_URL, {
    headers: withAuthHeaders(),
  })
  if (!res.ok) {
    throw await buildError(res)
  }
  return res.json()
}

export async function createEvent(payload) {
  const res = await fetch(EVENTS_URL, {
    method: 'POST',
    headers: withAuthHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw await buildError(res)
  }
  return res.json()
}

export async function updateEvent(id, payload) {
  const res = await fetch(`${EVENTS_URL}/${id}`, {
    method: 'PUT',
    headers: withAuthHeaders({ 'Content-Type': 'application/json' }),
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw await buildError(res)
  }
  return res.json()
}

export async function cancelEvent(id) {
  const res = await fetch(`${EVENTS_URL}/${id}/cancel`, {
    method: 'PATCH',
    headers: withAuthHeaders(),
  })
  if (!res.ok) {
    throw await buildError(res)
  }
  return res.json()
}
