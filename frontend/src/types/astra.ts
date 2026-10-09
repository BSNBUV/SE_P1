export type Role = 'ADMIN' | 'OPERATOR'
export type MissionStatus = 'CREATED' | 'VALIDATING' | 'VALIDATED' | 'EXECUTING' | 'PAUSED' | 'COMPLETED' | 'FAILURE' | 'STOPPED'
export type ValidationOutcome = 'PASS' | 'WARNING' | 'REJECT'
export type AlertSeverity = 'INFO' | 'WARNING' | 'CRITICAL'

export interface Waypoint {
  id?: number
  sequence: number
  latitude: number
  longitude: number
  altitude: number
}

export interface Mission {
  id: number
  name: string
  description: string
  status: MissionStatus
  createdBy: string
  vehicleId: string
  createdAt: string
  updatedAt: string
  validationResult?: ValidationOutcome
  validationSummary?: string
  estimatedDistance: number
  estimatedBatteryUsage: number
  estimatedBatteryReserve: number
  validationLatencyMs?: number
  executionStartedAt?: string
  executionCompletedAt?: string
  waypoints: Waypoint[]
}

export interface Telemetry {
  id: number
  missionId: number
  timestamp: string
  latitude: number
  longitude: number
  altitude: number
  battery: number
  missionStatus: MissionStatus
  progress: number
}

export interface Alert {
  id: number
  missionId: number
  timestamp: string
  severity: AlertSeverity
  type: string
  message: string
  acknowledged: boolean
}

export interface MissionLog {
  id: number
  missionId: number
  timestamp: string
  eventType: string
  message: string
  metadata: string
}

export interface SessionUser {
  token: string
  username: string
  role: Role
}
