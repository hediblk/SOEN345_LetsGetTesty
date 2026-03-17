import { useEffect, useState } from 'react'
import './App.css'

function App() {
  const [status, setStatus] = useState({
    loading: true,
    message: '',
    error: '',
  })

  useEffect(() => {
    const checkBackend = async () => {
      try {
        const response = await fetch('/api/test')

        if (!response.ok) {
          throw new Error(`Request failed with status ${response.status}`)
        }

        const data = await response.text()
        setStatus({
          loading: false,
          message: data,
          error: '',
        })
      } catch (error) {
        setStatus({
          loading: false,
          message: '',
          error: error.message,
        })
      }
    }

    checkBackend()
  }, [])

  return (
    <>
      {status.loading ? <p>checking backend...</p> : null}
      {status.message ? <p>{status.message}</p> : null}
      {status.error ? <p>{status.error}</p> : null}
    </>
  )
}

export default App
