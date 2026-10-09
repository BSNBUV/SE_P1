import { useState } from 'react'
import type { FormEvent } from 'react'
import { login } from '../../api/client'
import type { SessionUser } from '../../types/astra'

export function Login({ onLogin }: { onLogin: (user: SessionUser) => void }) {
  const [username, setUsername] = useState('operator')
  const [password, setPassword] = useState('AstraOperator!2026')
  const [error, setError] = useState('')

  async function submit(event: FormEvent) {
    event.preventDefault()
    setError('')
    try {
      onLogin(await login(username, password))
    } catch {
      setError('Login failed. Try operator/AstraOperator!2026 or admin/AstraAdmin!2026.')
    }
  }

  return (
    <main className="login">
      <form className="login-card stack" onSubmit={submit}>
        <div className="login-hero">
          <span className="badge simulation-badge">SIMULATION / SITL MODE</span>
          <h1>Astra Command</h1>
          <p>Adapter-based ground-control prototype for planning, validating, executing, monitoring and replaying deterministic SITL missions.</p>
        </div>
        <label>Username<input value={username} onChange={(e) => setUsername(e.target.value)} /></label>
        <label>Password<input type="password" value={password} onChange={(e) => setPassword(e.target.value)} /></label>
        {error && <p className="severity-CRITICAL">{error}</p>}
        <button className="primary">Login</button>
        <p className="metric">Demo users: operator/AstraOperator!2026, admin/AstraAdmin!2026</p>
      </form>
    </main>
  )
}
