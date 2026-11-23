# Skema Database Go Out

## A. Room (lokal) — `goout.db`

### Tabel `corridors`
| Kolom | Tipe | Keterangan |
|---|---|---|
| corridorId (PK) | String | `corridor_veteran`, `corridor_soehat`, `corridor_haryono` |
| corridorName | String | Nama jalan |
| segmentLabel | String | "Bundaran UB → Simpang Matos" |
| polylineEncoded | String | `lat,lng;lat,lng;…` (geometri koridor) |
| densityStatus | String | LANCAR / MERAYAP / MACET / UNKNOWN |
| currentSpeed | Double? | km/h hasil agregasi, null bila belum ada |
| lastUpdated | Long | epoch millis pembaruan terakhir |
| isSynced | Boolean | hasil agregasi sudah tertulis ke cloud |

### Tabel `telemetry` (index: syncStatus, corridorId)
| Kolom | Tipe | Keterangan |
|---|---|---|
| telemetryId (PK) | String | UUID — juga menjadi ID dokumen Firestore (idempoten) |
| userId | String | UID Firebase |
| corridorId | String | koridor hasil geofence |
| latitude, longitude | Double | posisi pengukuran |
| speedKmh | Double | m/s × 3.6 |
| timestamp | Long | epoch millis |
| syncStatus | String | PENDING / SYNCED / FAILED |
| source | String | `gps` atau `mock` |

### Tabel `traffic_reports` (index: syncStatus, corridorId)
| Kolom | Tipe | Keterangan |
|---|---|---|
| reportId (PK) | String | UUID — juga ID dokumen Firestore |
| userId | String | UID pembuat laporan |
| corridorId | String | koridor tervalidasi |
| reportedStatus | String | LANCAR / MERAYAP / MACET |
| representativeSpeed | Double | 40 / 25 / 8 km/h (lihat CorridorConfig) |
| latitude, longitude | Double | posisi saat melapor |
| timestamp | Long | epoch millis |
| syncStatus | String | PENDING / SYNCED / FAILED |

### DataStore (`goout_prefs`)
`passive_telemetry_enabled`, `onboarding_done`, `mock_generator_enabled` —
hanya preferensi non-sensitif. Tidak pernah menyimpan password/token.

## B. Cloud Firestore

### `users/{uid}`
`uid, displayName, email, createdAt (server), lastLoginAt (server), accountStatus`

### `corridors/{corridorId}`
`corridorId, corridorName, segmentLabel, polyline (string),`
`densityStatus, currentSpeed, lastUpdated` — dokumen dibuat/merge saat seed;
field lalu lintas hanya di-update oleh agregasi (Rules membatasi field).

### `telemetry/{telemetryId}`
`telemetryId, userId, corridorId, latitude, longitude, speedKmh,`
`clientTimestamp, source, createdAt (serverTimestamp)` — write-once per user.

### `reports/{reportId}`
`reportId, userId, corridorId, reportedStatus, representativeSpeed,`
`latitude, longitude, clientTimestamp, createdAt (serverTimestamp)`

Waktu otoritatif cloud = `createdAt` (server timestamp); `clientTimestamp`
dipertahankan untuk analisis jendela agregasi.
