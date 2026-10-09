import { useEffect, useState } from 'react'
import { commandMission, getMission, listMissions } from '../../api/client'
import type { Mission } from '../../types/astra'

export function ValidationPage({ selected, onSelect }: { selected?: Mission; onSelect: (mission: Mission) => void }) {
  const [missions, setMissions] = useState<Mission[]>([])
  const [current, setCurrent] = useState<Mission | undefined>(selected)
  useEffect(() => { listMissions().then(setMissions) }, [])
  useEffect(() => setCurrent(selected), [selected])
  useEffect(() => {
    if (!current || current.status !== 'VALIDATING') return
    const id = window.setInterval(() => getMission(current.id).then((m) => { setCurrent(m); onSelect(m) }), 700)
    return () => window.clearInterval(id)
  }, [current, onSelect])

  async function validate() {
    if (!current) return
    const mission = await commandMission(current.id, 'validate')
    setCurrent(mission)
    onSelect(mission)
  }

  const rules = current?.validationSummary?.split('\n') ?? []
  return (
    <div className="grid">
      <section className="panel span-4 stack">
        <h2>Validation</h2>
        <select value={current?.id ?? ''} onChange={(e) => {
          const mission = missions.find((m) => m.id === Number(e.target.value))
          setCurrent(mission)
          if (mission) onSelect(mission)
        }}>
          <option value="">Select mission</option>
          {missions.map((m) => <option key={m.id} value={m.id}>{m.name}</option>)}
        </select>
        <button className="primary" disabled={!current || current.status === 'VALIDATING'} onClick={validate}>Validate Mission</button>
      </section>
      <section className="panel span-8">
        <h2>{current?.name ?? 'No mission selected'}</h2>
        <div className="validation-readout">
          <div><span>Overall</span><strong className={`status-${current?.validationResult}`}>{current?.validationResult ?? 'Not run'}</strong></div>
          <div><span>MVL</span><strong>{current?.validationLatencyMs ? `${current.validationLatencyMs} ms` : 'not measured'}</strong></div>
          <div><span>Distance</span><strong>{current?.estimatedDistance?.toFixed(2) ?? 0} km</strong></div>
          <div><span>Battery Reserve</span><strong>{current?.estimatedBatteryReserve?.toFixed(1) ?? 0}%</strong></div>
        </div>
        <p>Battery use: {current?.estimatedBatteryUsage?.toFixed(1) ?? 0}%</p>
        <table className="table"><tbody>{rules.map((r) => {
          const outcome = r.includes('REJECT') ? 'REJECT' : r.includes('WARNING') ? 'WARNING' : 'PASS'
          return <tr key={r}><td className={`status-${outcome}`}>{outcome}</td><td>{r}</td></tr>
        })}</tbody></table>
      </section>
    </div>
  )
}
