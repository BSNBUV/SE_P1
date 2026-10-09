# API

All protected endpoints require `Authorization: Bearer <token>`.

- `POST /api/auth/login`
- `POST /api/missions`
- `GET /api/missions`
- `GET /api/missions/{id}`
- `POST /api/missions/{id}/validate`
- `GET /api/missions/{id}/validation`
- `POST /api/missions/{id}/execute`
- `GET /api/missions/{id}/telemetry`
- `GET /api/missions/{id}/logs`
- `GET /api/missions/{id}/alerts`
- `GET /api/missions/{id}/report`
- `POST /api/missions/{id}/pause`
- `POST /api/missions/{id}/resume`
- `POST /api/missions/{id}/emergency-stop`
- `POST /api/missions/{id}/simulate-low-battery`
- `POST /api/missions/{id}/simulate-connection-loss`
- `POST /api/missions/{id}/reset`
- `GET /api/dashboard`
- `GET /api/metrics`
- `GET /api/admin/audit` admin only
