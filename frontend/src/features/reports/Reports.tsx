import { useEffect, useState } from 'react'
import { getMetrics, listMissions, reportUrl } from '../../api/client'
import type { Mission } from '../../types/astra'

export function Reports({ token }: { token: string }) {
  const [missions, setMissions] = useState<Mission[]>([])
  const [mission, setMission] = useState<Mission>()
  const [metrics, setMetrics] = useState<Record<string, unknown>>({})
  useEffect(() => { listMissions().then((m) => { setMissions(m); setMission(m[0]) }); getMetrics().then(setMetrics) }, [])

  async function download() {
    if (!mission) return
    const response = await fetch(reportUrl(mission.id), { headers: { Authorization: `Bearer ${token}` } })
    const blob = await response.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `astra-mission-${mission.id}-report.pdf`
    a.click()
    URL.revokeObjectURL(url)
  }

  return (
    <div className="grid">
      <section className="panel span-6 stack">
        <h2>Reports</h2>
        <select value={mission?.id ?? ''} onChange={(e) => setMission(missions.find((m) => m.id === Number(e.target.value)))}>
          {missions.map((m) => <option key={m.id} value={m.id}>{m.name}</option>)}
        </select>
        <button className="primary" onClick={download}>Download Mission Report PDF</button>
      </section>
      <section className="panel span-6">
        <h2>System Metrics</h2>
        <table className="table"><tbody>{Object.entries(metrics).map(([k, v]) => <tr key={k}><td>{k}</td><td>{String(v)}</td></tr>)}</tbody></table>
        <p className="metric">Targets are displayed for evaluation context only; this page reports actual values measured by the current prototype run.</p>
      </section>
    </div>
  )
}
