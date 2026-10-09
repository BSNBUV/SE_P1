import { useEffect, useState } from 'react'
import { getTelemetry, listMissions } from '../../api/client'
import { MissionMap } from '../../components/MissionMap'
import type { Mission, Telemetry } from '../../types/astra'

export function Replay() {
  const [missions, setMissions] = useState<Mission[]>([])
  const [mission, setMission] = useState<Mission>()
  const [telemetry, setTelemetry] = useState<Telemetry[]>([])
  const [index, setIndex] = useState(0)
  const [playing, setPlaying] = useState(false)
  useEffect(() => { listMissions().then(setMissions) }, [])
  useEffect(() => {
    if (!mission) return
    getTelemetry(mission.id).then((rows) => { setTelemetry(rows); setIndex(0) })
  }, [mission])
  useEffect(() => {
    if (!playing || telemetry.length === 0) return
    const id = window.setInterval(() => setIndex((i) => Math.min(i + 1, telemetry.length - 1)), 600)
    return () => window.clearInterval(id)
  }, [playing, telemetry.length])
  const currentPoint = telemetry[index]
  return (
    <div className="grid">
      <section className="panel span-8">
        <h2>Replay</h2>
        <MissionMap waypoints={mission?.waypoints ?? []} telemetry={currentPoint} />
      </section>
      <section className="panel span-4 stack replay-console">
        <div>
          <div className="eyebrow">Telemetry Playback</div>
          <h2>{mission?.name ?? 'Select a Mission'}</h2>
        </div>
        <select value={mission?.id ?? ''} onChange={(e) => setMission(missions.find((m) => m.id === Number(e.target.value)))}>
          <option value="">Select mission</option>
          {missions.map((m) => <option key={m.id} value={m.id}>{m.name} · {m.status}</option>)}
        </select>
        <div className="actions"><button onClick={() => setPlaying(true)}>Play</button><button onClick={() => setPlaying(false)}>Pause</button></div>
        <input type="range" min={0} max={Math.max(0, telemetry.length - 1)} value={index} onChange={(e) => setIndex(Number(e.target.value))} />
        <div className="telemetry-grid">
          <div className="telemetry-cell"><span>Packets</span><strong>{telemetry.length}</strong></div>
          <div className="telemetry-cell"><span>Frame</span><strong>{telemetry.length ? index + 1 : 0}</strong></div>
          <div className="telemetry-cell"><span>Battery</span><strong>{currentPoint?.battery.toFixed(1) ?? 'n/a'}%</strong></div>
          <div className="telemetry-cell"><span>Progress</span><strong>{currentPoint?.progress.toFixed(0) ?? 0}%</strong></div>
        </div>
        <p>Timestamp: {currentPoint?.timestamp ?? 'none'}</p>
      </section>
    </div>
  )
}
