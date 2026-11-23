# Alur Aplikasi Go Out

## Alur Layar

```
Splash ──(sudah login)──────────────► Peta (Home)
   └──(belum login)──► Login ──► Register
                         │  └──(tamu)──► Peta (read-only)
                         └──(sukses)──► Peta

Bottom nav: [Peta] [Koridor] [Lapor]
Peta ──klik polyline/kartu──► Detail Koridor ──Lapor──► Lapor
Peta ──ikon profil──► Profil & Pengaturan (telemetri, logout, mock)
Lapor ──hasil──► Panel Sukses / Offline / Lokasi-Tidak-Valid / GPS-Error / Login
```

## Fungsi 1 — Klasifikasi Kepadatan
```
kecepatan valid (≥0) ─► classifyTrafficDensity:
  ≥35 → LANCAR · ≥15 → MERAYAP · <15 → MACET · invalid → UNKNOWN
─► agregasi EMA koridor ─► Firestore + Room ─► Flow ─► warna polyline peta
```

## Fungsi 2 — Validasi Geofence
```
posisi GPS ─► cek koordinat valid & akurasi ≤50 m
─► jarak terpendek point-to-segment ke SEMUA segmen polyline 3 koridor
─► jarak ≤100 m? → VALID (corridorId terdekat, jarak) : INVALID (+alasan)
```

## Fungsi 3 — Telemetri Pasif (opt-in)
```
login? → telemetri aktif? → izin lokasi? → foreground service + notifikasi
─► update lokasi tiap 10 dtk ─► speed null/negatif? tolak
─► km/h = m/s × 3.6 ─► geofence valid? ─► interval ≥10 dtk?
─► simpan Room (PENDING) ─► agregasi koridor ─► WorkManager sync → SYNCED
```

## Fungsi 4 — Laporan Aktif 1-Ketukan
```
buka Lapor ─► login? ─► pilih LANCAR/MERAYAP/MACET ─► ambil GPS
─► geofence valid? tidak → "Lokasi Tidak Valid"
─► kecepatan representatif (40/25/8) ─► UUID ─► simpan Room
─► online: kirim Firestore → "Laporan berhasil dikirim."
─► offline: antrean → "Laporan disimpan dan menunggu sinkronisasi."
─► agregasi + pembaruan UI
```

## Sinkronisasi Offline
```
offline: data tampil dari cache Room; tulisan masuk antrean PENDING;
         indikator offline di header; sukses TIDAK pernah diklaim.
online kembali: SyncWorker (network constraint, backoff eksponensial)
         kirim antrean → tandai SYNCED → ID stabil mencegah duplikat.
```
