import { MapContainer, Marker, Polyline, Popup, TileLayer, useMapEvents } from 'react-leaflet'
import L from 'leaflet'
import type { Telemetry, Waypoint } from '../types/astra'

const waypointIcon = (sequence: number) => new L.DivIcon({ className: 'waypoint-marker', html: String(sequence), iconSize: [34, 34] })
const vehicleIcon = new L.DivIcon({ className: 'vehicle-marker', html: '<span></span>', iconSize: [42, 42] })

function ClickHandler({ onAdd }: { onAdd?: (lat: number, lng: number) => void }) {
  useMapEvents({
    click: (event) => onAdd?.(event.latlng.lat, event.latlng.lng),
  })
  return null
}

export function MissionMap({ waypoints, telemetry, onAdd, small = false }: { waypoints: Waypoint[]; telemetry?: Telemetry; onAdd?: (lat: number, lng: number) => void; small?: boolean }) {
  const center: [number, number] = waypoints[0] ? [waypoints[0].latitude, waypoints[0].longitude] : [12.9716, 77.5946]
  const route = waypoints.map((w) => [w.latitude, w.longitude] as [number, number])
  return (
    <MapContainer className={`map ${small ? 'small-map' : ''}`} center={center} zoom={14} scrollWheelZoom>
      <TileLayer attribution="&copy; OpenStreetMap contributors" url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
      <ClickHandler onAdd={onAdd} />
      {route.length > 1 && <Polyline positions={route} pathOptions={{ color: '#6ee7d8', weight: 4 }} />}
      {waypoints.map((w) => (
        <Marker key={`${w.sequence}-${w.latitude}`} position={[w.latitude, w.longitude]} icon={waypointIcon(w.sequence)}>
          <Popup>Waypoint {w.sequence}<br />Alt {w.altitude}m</Popup>
        </Marker>
      ))}
      {telemetry && (
        <Marker position={[telemetry.latitude, telemetry.longitude]} icon={vehicleIcon}>
          <Popup>Battery {telemetry.battery.toFixed(1)}%<br />Progress {telemetry.progress.toFixed(0)}%</Popup>
        </Marker>
      )}
    </MapContainer>
  )
}
