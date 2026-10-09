import { useMemo, useState } from 'react'
import { Activity, BarChart3, ClipboardCheck, FileText, LayoutDashboard, Map, PlayCircle, RadioTower, TriangleAlert } from 'lucide-react'
import { Login } from './features/auth/Login'
import { Dashboard } from './features/dashboard/Dashboard'
import { MissionPlanner } from './features/missions/MissionPlanner'
import { ValidationPage } from './features/validation/ValidationPage'
import { LiveMission } from './features/live/LiveMission'
import { Replay } from './features/replay/Replay'
import { LogsAlerts } from './features/logs/LogsAlerts'
import { Reports } from './features/reports/Reports'
import { setAuthToken } from './api/client'
import type { Mission, SessionUser } from './types/astra'

type Page = 'Dashboard' | 'Mission Planner' | 'Validation' | 'Live Mission' | 'Replay' | 'Logs / Alerts' | 'Reports'

const navMeta: Record<Page, { icon: typeof LayoutDashboard; kicker: string }> = {
  Dashboard: { icon: LayoutDashboard, kicker: 'System overview' },
  'Mission Planner': { icon: Map, kicker: 'Waypoint design' },
  Validation: { icon: ClipboardCheck, kicker: 'Rule pipeline' },
  'Live Mission': { icon: RadioTower, kicker: 'SITL telemetry' },
  Replay: { icon: PlayCircle, kicker: 'Telemetry playback' },
  'Logs / Alerts': { icon: TriangleAlert, kicker: 'Audit trail' },
  Reports: { icon: FileText, kicker: 'Export evidence' },
}

export default function App() {
  const [user, setUser] = useState<SessionUser | undefined>(() => {
    const raw = localStorage.getItem('astra-session')
    if (!raw) return undefined
    const parsed = JSON.parse(raw) as SessionUser
    setAuthToken(parsed.token)
    return parsed
  })
  const [page, setPage] = useState<Page>('Dashboard')
  const [selected, setSelected] = useState<Mission>()
  const pages = useMemo<Page[]>(() => ['Dashboard', 'Mission Planner', 'Validation', 'Live Mission', 'Replay', 'Logs / Alerts', 'Reports'], [])

  if (!user) return <Login onLogin={(u) => { localStorage.setItem('astra-session', JSON.stringify(u)); setUser(u) }} />

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand-lockup">
          <div className="brand-mark"><RadioTower size={22} /></div>
          <div>
            <div className="brand">Astra Command</div>
            <div className="brand-subtitle">Adapter-Based Ground Control</div>
          </div>
        </div>
        <span className="badge simulation-badge"><Activity size={14} /> SIMULATION / SITL MODE</span>
        <div className="phase-strip" aria-label="Mission workflow">
          {['PLAN', 'VALIDATE', 'EXECUTE', 'MONITOR', 'REPLAY'].map((phase) => <span key={phase}>{phase}</span>)}
        </div>
        <nav className="nav">
          {pages.map((p) => {
            const Icon = navMeta[p].icon
            return (
              <button key={p} className={page === p ? 'active' : ''} onClick={() => setPage(p)}>
                <Icon size={18} />
                <span><strong>{p}</strong><small>{navMeta[p].kicker}</small></span>
              </button>
            )
          })}
        </nav>
        <div className="sidebar-footer">
          <span className="signal-dot" />
          Backend REST · Adapter isolated · No physical drone control
        </div>
      </aside>
      <main className="main">
        <header className="topbar">
          <div>
            <div className="eyebrow">Astra-SITL-01 · Bengaluru Simulation Field</div>
            <h1>{page}</h1>
          </div>
          <div className="topbar-cluster">
            <span className="badge"><BarChart3 size={14} /> Backend online</span>
            <span className="badge">{user.username} · {user.role}</span>
            <button onClick={() => { localStorage.removeItem('astra-session'); setAuthToken(); setUser(undefined) }}>Logout</button>
          </div>
        </header>
        <section className="content">
          {page === 'Dashboard' && <Dashboard />}
          {page === 'Mission Planner' && <MissionPlanner onSelect={(m) => { setSelected(m); setPage('Validation') }} />}
          {page === 'Validation' && <ValidationPage selected={selected} onSelect={setSelected} />}
          {page === 'Live Mission' && <LiveMission selected={selected} onSelect={setSelected} />}
          {page === 'Replay' && <Replay />}
          {page === 'Logs / Alerts' && <LogsAlerts />}
          {page === 'Reports' && <Reports token={user.token} />}
        </section>
      </main>
    </div>
  )
}
