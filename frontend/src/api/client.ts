import axios from 'axios'
import type { Alert, Mission, MissionLog, SessionUser, Telemetry, Waypoint } from '../types/astra'

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080/api'

export const api = axios.create({ baseURL: API_BASE })

export function setAuthToken(token?: string) {
  if (token) api.defaults.headers.common.Authorization = `Bearer ${token}`
  else delete api.defaults.headers.common.Authorization
}

export async function login(username: string, password: string) {
  const { data } = await api.post<SessionUser>('/auth/login', { username, password })
  setAuthToken(data.token)
  return data
}

export async function listMissions() {
  return (await api.get<Mission[]>('/missions')).data
}

export async function createMission(payload: { name: string; description: string; vehicleId: string; waypoints: Waypoint[] }) {
  return (await api.post<Mission>('/missions', payload)).data
}

export async function commandMission(id: number, command: string) {
  return (await api.post<Mission>(`/missions/${id}/${command}`)).data
}

export async function getMission(id: number) {
  return (await api.get<Mission>(`/missions/${id}`)).data
}

export async function getTelemetry(id: number) {
  return (await api.get<Telemetry[]>(`/missions/${id}/telemetry`)).data
}

export async function getAlerts(id: number) {
  return (await api.get<Alert[]>(`/missions/${id}/alerts`)).data
}

export async function getLogs(id: number) {
  return (await api.get<MissionLog[]>(`/missions/${id}/logs`)).data
}

export async function getDashboard() {
  return (await api.get('/dashboard')).data
}

export async function getMetrics() {
  return (await api.get('/metrics')).data
}

export function reportUrl(id: number) {
  return `${API_BASE}/missions/${id}/report`
}
