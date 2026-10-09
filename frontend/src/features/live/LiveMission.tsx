import { useEffect, useState } from 'react'
import { commandMission, getAlerts, getLogs, getMission, getTelemetry, listMissions } from '../../api/client'
import { MissionMap } from '../../components/MissionMap'
import type { Alert, Mission, MissionLog, Telemetry } from '../../types/astra'

export function LiveMission({ selected, onSelect }: { selected?: Mission; onSelect: (mission: Mission) => void }) {
  const [missions, setMissions] = useState<Mission[]>([])
  const [current, setCurrent] = useState<Mission | undefined>(selected)
  const [telemetry, setTelemetry] = useState<Telemetry[]>([])
  const [alerts, setAlerts] = useState<Alert[]>([])
  const [logs, setLogs] = useState<MissionLog[]>([])
  const [launching, setLaunching] = useState(false)
  useEffect(() => { listMissions().then(setMissions) }, [])
  useEffect(() => setCurrent(selected), [selected])
  useEffect(() => {
    if (!current) return
    const load = async () => {
      setCurrent(await getMission(current.id))
      setTelemetry(await getTelemetry(current.id))
      setAlerts(await getAlerts(current.id))
      setLogs(await getLogs(current.id))
    }
    load()
    const id = window.setInterval(load, 1000)
    return () => window.clearInterval(id)
  }, [current?.id])

  async function run(command: string) {
    if (!current) return
    try {
      if (command === 'execute') {
        setLaunching(true)
        await new Promise((resolve) => window.setTimeout(resolve, 1450))
      }
      const mission = await commandMission(current.id, command)
      setCurrent(mission)
      onSelect(mission)
    } finally {
      setLaunching(false)
    }
  }

  const latest = telemetry.at(-1)
  return (
    <div className="grid">
      <section className="panel span-8">
        <h2>Live Mission</h2>
        <MissionMap waypoints={current?.waypoints ?? []} telemetry={latest} />
        {launching && (
          <div className="launch-overlay" role="status" aria-live="polite">
            <div className="launch-core">
              <span>LAUNCH SEQUENCE</span>
              <strong>{current?.name}</strong>
              <ol>
                <li>Adapter handshake confirmed</li>
                <li>SITL telemetry stream armed</li>
                <li>Mission command queued</li>
              </ol>
            </div>
          </div>
        )}
      </section>
      <section className="panel span-4 stack">
        <select value={current?.id ?? ''} onChange={(e) => {
          const mission = missions.find((m) => m.id === Number(e.target.value))
          setCurrent(mission)
          if (mission) onSelect(mission)
        }}>
          <option value="">Select mission</option>
          {missions.map((m) => <option key={m.id} value={m.id}>{m.name} · {m.status}</option>)}
        </select>
        <div className="actions">
          <button className="primary" onClick={() => run('execute')} disabled={current?.status !== 'VALIDATED'}>Start Mission</button>
          <button onClick={() => run('pause')}>Pause</button>
          <button onClick={() => run('resume')}>Resume</button>
          <button className="danger" onClick={() => run('emergency-stop')}>Emergency Stop</button>
          <button onClick={() => run('simulate-low-battery')}>Simulate Low Battery</button>
          <button onClick={() => run('simulate-connection-loss')}>Simulate Connection Loss</button>
          <button onClick={() => run('reset')}>Reset Simulation</button>
        </div>
        <div className="telemetry-grid">
          <div className="telemetry-cell"><span>Status</span><strong>{current?.status ?? 'No mission'}</strong></div>
          <div className="telemetry-cell"><span>Battery</span><strong>{latest ? `${latest.battery.toFixed(1)}%` : 'No telemetry'}</strong></div>
          <div className="telemetry-cell"><span>Altitude</span><strong>{latest ? `${latest.altitude.toFixed(1)}m` : 'No telemetry'}</strong></div>
          <div className="telemetry-cell"><span>Progress</span><strong>{latest ? `${latest.progress.toFixed(0)}%` : '0%'}</strong></div>
        </div>
        <div className="progress-track"><div className="progress-fill" style={{ width: `${latest?.progress ?? 0}%` }} /></div>
        <p>GPS: {latest ? `${latest.latitude.toFixed(5)}, ${latest.longitude.toFixed(5)}` : 'No telemetry'}</p>
        <p>Latest telemetry: {latest?.timestamp ?? 'none'}</p>
      </section>
      <section className="panel span-6"><h3>Alerts</h3>{alerts.map((a) => <p key={a.id} className={`severity-${a.severity}`}>{a.severity} {a.type}: {a.message}</p>)}</section>
      <section className="panel span-6"><h3>Mission Log</h3>{logs.map((l) => <p key={l.id}>{l.eventType}: {l.message}</p>)}</section>
    </div>
  )
}
