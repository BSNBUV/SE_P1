import { useEffect, useState } from 'react'
import { createMission, listMissions } from '../../api/client'
import { MissionMap } from '../../components/MissionMap'
import type { Mission, Waypoint } from '../../types/astra'

const demoRoute: Waypoint[] = [
  { sequence: 1, latitude: 12.9716, longitude: 77.5946, altitude: 40 },
  { sequence: 2, latitude: 12.973, longitude: 77.5961, altitude: 45 },
  { sequence: 3, latitude: 12.9742, longitude: 77.5938, altitude: 42 },
  { sequence: 4, latitude: 12.9721, longitude: 77.5927, altitude: 38 },
]

export function MissionPlanner({ onSelect }: { onSelect: (mission: Mission) => void }) {
  const [missions, setMissions] = useState<Mission[]>([])
  const [name, setName] = useState(`LAB_ROUTE_${new Date().getMinutes()}${new Date().getSeconds()}`)
  const [description, setDescription] = useState('Interactive planned SITL route')
  const [waypoints, setWaypoints] = useState<Waypoint[]>(demoRoute)

  const load = () => listMissions().then(setMissions)
  useEffect(() => { load() }, [])

  function addWaypoint(latitude: number, longitude: number) {
    setWaypoints((items) => [...items, { sequence: items.length + 1, latitude, longitude, altitude: 45 }])
  }

  function updateAltitude(index: number, altitude: number) {
    setWaypoints((items) => items.map((w, i) => i === index ? { ...w, altitude } : w))
  }

  function remove(index: number) {
    setWaypoints((items) => items.filter((_, i) => i !== index).map((w, i) => ({ ...w, sequence: i + 1 })))
  }

  async function save() {
    const mission = await createMission({ name, description, vehicleId: 'Astra-SITL-01', waypoints })
    await load()
    onSelect(mission)
  }

  return (
    <div className="grid">
      <section className="panel span-8">
        <h2>Mission Planner</h2>
        <MissionMap waypoints={waypoints} onAdd={addWaypoint} />
      </section>
      <section className="panel span-4 stack">
        <h2>Route</h2>
        <label>Mission name<input value={name} onChange={(e) => setName(e.target.value)} /></label>
        <label>Description<textarea value={description} onChange={(e) => setDescription(e.target.value)} /></label>
        <div className="actions">
          <button onClick={() => setWaypoints(demoRoute)}>Generate Demo Route</button>
          <button onClick={() => addWaypoint(12.9716 + waypoints.length * 0.001, 77.5946 + waypoints.length * 0.001)}>Add Waypoint</button>
          <button onClick={() => setWaypoints([])}>Clear Route</button>
          <button className="primary" onClick={save} disabled={waypoints.length < 2}>Save Mission</button>
        </div>
        <table className="table"><thead><tr><th>#</th><th>Lat</th><th>Lng</th><th>Alt</th><th /></tr></thead><tbody>
          {waypoints.map((w, i) => <tr key={i}><td>{w.sequence}</td><td>{w.latitude.toFixed(5)}</td><td>{w.longitude.toFixed(5)}</td><td><input type="number" value={w.altitude} onChange={(e) => updateAltitude(i, Number(e.target.value))} /></td><td><button onClick={() => remove(i)}>Delete</button></td></tr>)}
        </tbody></table>
        <h3>Saved Missions</h3>
        {missions.map((m) => <button key={m.id} onClick={() => onSelect(m)}>{m.name} · {m.status}</button>)}
      </section>
    </div>
  )
}
