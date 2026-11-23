# Arsitektur Go Out

## Gambaran Umum

Go Out menggunakan **MVVM + Clean Architecture** dengan tiga lapisan dan
**hybrid storage** (Room lokal + Cloud Firestore).

```
┌─────────────────────────────────────────────────────────────┐
│ PRESENTATION  (feature/)                                     │
│  Compose Screens ←→ ViewModels (StateFlow, immutable state)  │
│  auth · home · corridor · report · profile                   │
└──────────────────────────┬──────────────────────────────────┘
                           │ use cases
┌──────────────────────────▼──────────────────────────────────┐
│ DOMAIN  (domain/)                                            │
│  model/ (Corridor, TrafficReport, TelemetryEvent, …)         │
│  repository/ (interfaces)                                    │
│  usecase/ (Classify, ValidateGeofence, SubmitReport,         │
│            ProcessTelemetryFix, Sync, Observe…)              │
└──────────────────────────┬──────────────────────────────────┘
                           │ implementasi repositori
┌──────────────────────────▼──────────────────────────────────┐
│ DATA  (data/)                                                │
│  local/   Room (CorridorDao, TelemetryDao, ReportDao)        │
│           DataStore (preferensi non-sensitif)                │
│  remote/  FirebaseProvider → Auth + Firestore                │
│  repository/ *Impl (local-first, sinkronisasi)               │
└─────────────────────────────────────────────────────────────┘
          │                                   │
   ┌──────▼──────┐                     ┌──────▼──────┐
   │ service/    │                     │ core/       │
   │ telemetry/  │ Foreground service  │ designsystem│ tema Figma
   │ sync/       │ WorkManager         │ utils/ mock/│ GeoUtils, mock
   └─────────────┘                     └─────────────┘
```

## Aliran Data (local-first)

1. UI meng-observe **Room** melalui Flow (`observeCorridors()`).
2. Snapshot listener Firestore memantulkan perubahan cloud → Room.
3. Penulisan (telemetri/laporan) disimpan dulu ke Room dengan status
   `PENDING`, lalu dikirim ke Firestore; sukses → `SYNCED`.
4. Saat offline, `SyncWorker` (WorkManager, constraint network, backoff
   eksponensial) mengirim antrean saat koneksi kembali.
5. ID dokumen Firestore = ID lokal yang stabil (UUID) → retry **idempoten**,
   tidak ada duplikasi.

## Keputusan Teknik (terdokumentasi)

| Keputusan | Alasan |
|---|---|
| Agregasi EMA (α=0.4) dengan jendela 10 menit | Deterministik; satu pembacaan basi tidak dapat menimpa status yang lebih baru (timestamp lama ditolak). Status menjadi UNKNOWN setelah 30 menit tanpa data. |
| Kecepatan representatif laporan: LANCAR 40, MERAYAP 25, MACET 8 km/h | Dokumen sumber tidak menetapkan nilai eksak; dipilih di tengah pita klasifikasi masing-masing. Terpusat di `CorridorConfig`. |
| Geometri koridor approximate di `CorridorConfig` | Koordinat presisi tidak tersedia di dokumen sumber; satu file konfigurasi, ditandai jelas, mudah diganti. |
| minSdk 26 | Menyederhanakan adaptive icon & foreground service; Android 8.0+ mencakup hampir semua perangkat aktif. |
| Plugin Google Services kondisional | Proyek tetap bisa dikompilasi/diuji tanpa `google-services.json`. |
| Telemetri = foreground service, bukan background location | Tidak meminta `ACCESS_BACKGROUND_LOCATION`; notifikasi selalu terlihat; patuh kebijakan privasi/Play. |
| Logout menghapus telemetri PENDING milik user & mematikan telemetri | Data pasif tak lagi dapat diatribusikan ke sesi terautentikasi. Laporan PENDING tetap disinkronkan (dibuat eksplisit oleh user saat login). |
| Font sistem Roboto | Figma memakai Roboto Flex; Roboto adalah padanan langsung di perangkat Android. |
| Layar login/profil tidak ada di Figma | Dibangun mengikuti design system yang sama (warna, radius, tipografi); ditandai sebagai tambahan engineering. |

## Keterbatasan MVP (dan jalur penguatan)

Validasi geofence dan agregasi berjalan **di sisi klien**. Security Rules
membatasi tipe/field dan identitas penulis, tetapi klien jahat yang
terautentikasi tetap bisa menulis status koridor palsu. Penguatan yang
disarankan: **Cloud Function** ber-trigger `onCreate` pada `telemetry/` dan
`reports/` yang melakukan validasi geofence server-side dan menjadi satu-satunya
penulis `corridors/*` (klien kehilangan izin update koridor). Memerlukan paket
Blaze untuk deploy functions; tidak termasuk MVP ini.

## Dependency Injection

Hilt. `AppModule` menyediakan database/DAO/WorkManager/CoroutineScope aplikasi;
`RepositoryModule` mem-bind interface domain ke implementasinya. ViewModels
memakai `@HiltViewModel`; `TelemetryService` memakai `@AndroidEntryPoint`;
`SyncWorker` memakai `@HiltWorker`.
