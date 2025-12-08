import { useState, useEffect } from 'react'
import './App.css'

interface ApiResponse {
  message: string
  timestamp: string
  framework: string
}

function App() {
  const [data, setData] = useState<ApiResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [count, setCount] = useState(0)

  useEffect(() => {
    fetch('/api/hello')
      .then(res => res.json())
      .then((data: ApiResponse) => {
        setData(data)
        setLoading(false)
      })
      .catch(err => {
        console.error('Error fetching data:', err)
        setLoading(false)
      })
  }, [])

  return (
    <div className="App">
      <header className="App-header">
        <h1> IceBrew</h1>
        <p className="subtitle">Spring Boot + Vite + React</p>
      </header>

      <main className="App-main">
        <div className="card">
          <h2>Backend Connection</h2>
          {loading ? (
            <p>Loading...</p>
          ) : data ? (
            <div className="api-response">
              <p className="message">{data.message}</p>
              <p className="detail">Framework: {data.framework}</p>
              <p className="detail">Timestamp: {new Date(data.timestamp).toLocaleString()}</p>
            </div>
          ) : (
            <p className="error">Failed to connect to backend</p>
          )}
        </div>

        <div className="card">
          <h2>HMR Test</h2>
          <p>Click the button to test Hot Module Replacement</p>
          <button onClick={() => setCount(count + 1)}>
            Count: {count}
          </button>
          <p className="hint">
            Try editing this component and save - the state will be preserved!
          </p>
        </div>

        <div className="card">
          <h2>Features</h2>
          <ul className="features">
            <li>✅ Vite Dev Server with HMR</li>
            <li>✅ React 18 with TypeScript</li>
            <li>✅ Spring Boot 3.2 Backend</li>
            <li>✅ Auto-proxy API requests</li>
            <li>✅ Production build support</li>
          </ul>
        </div>
      </main>

      <footer className="App-footer">
        <p>Built with ❤️ using IceBrew</p>
      </footer>
    </div>
  )
}

export default App
