import { useEffect, useState } from 'react'
import { getDashboard } from '../../api/client'
import type { Alert, Mission, MissionLog, Telemetry } from '../../types/astra'

export function Dashboard() {
  const [data, setData] = useState<{ activeMission?: Mission; latestTelemetry?: Telemetry; simulatorConnection: string; recentAlerts: Alert[]; recentLogs: MissionLog[] }>()
  useEffect(() => {
    const load = () => getDashboard().then(setData)
    load()
    const id = window.setInterval(load, 1500)
    return () => window.clearInterval(id)
  }, [])
  const m = data?.activeMission
  const t = data?.latestTelemetry
  return (
    <div className="grid">
      <section className="hero-panel span-12">
        <div>
          <div className="eyebrow">Universal Ground-Control · Simulation/SITL</div>
          <h2>Astra Command</h2>
          <p>Plan, validate, execute, monitor and replay missions through a controlled adapter architecture. No physical drone control. Every demo action flows through the backend.</p>
        </div>
        <div className="hero-readout">
          <span>ACTIVE MISSION</span>
          <strong>{m?.name ?? 'Standby'}</strong>
          <small>{m?.vehicleId ?? 'Astra-SITL-01'} · {m?.status ?? 'No active run'}</small>
        </div>
      </section>
      <Story
        phase="PLAN"
        title="Real route design becomes a common mission model."
        copy="Create waypoints on the interactive map, edit altitude, save the mission and keep the frontend away from simulator internals."
        steps={['Map click', 'Waypoint order', 'CommonMission', 'Adapter ready']}
      />
      <Story
        phase="VALIDATE"
        title="Rules scan the route before execution."
        copy="Waypoint checks, route consistency, altitude limits and deterministic battery feasibility produce PASS, WARNING or REJECT with visible calculations."
        steps={['Waypoint scan', 'Route check', 'Altitude safety', 'Battery reserve']}
      />
      <Story
        phase="EXECUTE"
        title="The SITL adapter turns a validated plan into telemetry."
        copy="A simulated vehicle traverses the route, emits parsed telemetry, persists it, and drives the live dashboard instead of frontend-only animation."
        steps={['Adapter init', 'Telemetry parser', 'PostgreSQL', 'Live map']}
      />
      <Story
        phase="MONITOR"
        title="Operators see mission health as a live control surface."
        copy="Battery, altitude, GPS position, progress, alerts and mission logs update from backend state, so the dashboard demonstrates real control-loop observability."
        steps={['Live polling', 'Alerts', 'Logs', 'Emergency stop']}
      />
      <Story
        phase="REPLAY"
        title="Every simulated flight becomes evidence."
        copy="Persisted telemetry can be replayed after execution, giving the lab demo a clear audit trail and a way to compare planned route against actual movement."
        steps={['Telemetry history', 'Playback slider', 'Reports', 'Traceability']}
      />
      <Metric label="System Status" value="Operational" />
      <Metric label="Simulator" value={data?.simulatorConnection ?? 'Checking'} />
      <Metric label="Mission State" value={m?.status ?? 'No mission'} />
      <Metric label="Battery" value={t ? `${t.battery.toFixed(1)}%` : 'No telemetry'} />
      <Metric label="Altitude" value={t ? `${t.altitude.toFixed(1)} m` : 'No telemetry'} />
      <Metric label="GPS" value={t ? `${t.latitude.toFixed(5)}, ${t.longitude.toFixed(5)}` : 'No telemetry'} />
      <Metric label="Progress" value={t ? `${t.progress.toFixed(0)}%` : '0%'} />
      <section className="panel span-6"><h2>Recent Alerts</h2>{data?.recentAlerts?.map((a) => <p key={a.id} className={`severity-${a.severity}`}>{a.severity} {a.type}: {a.message}</p>)}</section>
      <section className="panel span-6"><h2>Recent Mission Logs</h2>{data?.recentLogs?.map((l) => <p key={l.id}>{l.eventType}: {l.message}</p>)}</section>
    </div>
  )
}

function Metric({ label, value }: { label: string; value: string }) {
  return <section className="panel metric-card span-3 metric">{label}<strong>{value}</strong></section>
}

function Story({ phase, title, copy, steps }: { phase: string; title: string; copy: string; steps: string[] }) {
  return (
    <section className="story-panel">
      <div className="story-copy">
        <div className="eyebrow">{phase}</div>
        <h2>{title}</h2>
        <p>{copy}</p>
        <div className="story-steps">{steps.map((step) => <span key={step}>{step}</span>)}</div>
      </div>
      <div className="story-visual" aria-hidden="true" />
    </section>
  )
}
