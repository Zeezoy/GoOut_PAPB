# Go Out - Pemantauan Kepadatan Koridor Kota Malang

Go Out adalah aplikasi Android native (Kotlin, Jetpack Compose, Google Maps SDK,
Firebase Authentication, Cloud Firestore) untuk memantau kepadatan tiga koridor
jalan utama Kota Malang berbasis crowdsourcing: telemetri GPS pasif dan laporan
aktif 1-ketukan. Gagasan terinspirasi SISRI (Surabaya Integrated Smart Road
Infrastructure), diadaptasi menjadi pendekatan ringan berbasis smartphone.

Proyek akhir Praktikum Pengembangan Aplikasi Perangkat Bergerak, TIF-B, Kelompok 7.
Dosen pengampu: Fais Al Huda, S.Kom, M.Kom.

## Koridor terpantau
1. Jl. Veteran (Bundaran UB - Simpang Matos)
2. Jl. Soekarno-Hatta (Simpang Suhat - Jembatan Brantas)
3. Jl. MT Haryono (Dinoyo - Simpang Betek)

## Klasifikasi
| Status | Kecepatan | Warna |
|---|---|---|
| LANCAR | >= 35 km/jam | Hijau |
| MERAYAP | 15 - 34,9 km/jam | Kuning |
| MACET | < 15 km/jam | Merah |

## Pembagian fungsi
| Anggota | NIM | Fungsi |
|---|---|---|
| Bastian Nevan Baruch | 245150207111011 | Klasifikasi status kepadatan, data koridor, layar Detail Koridor |
| Raditya Ananta Maheswara Suryanto | 245150200111007 | Validasi geofence koridor, izin dan indikator lokasi |
| Jeremiah Harmoni Keenan Pigawahi | 245150201111008 | Telemetri pasif (Fused Location Provider) |
| Fevoura Agnesia Tatontos | 245150207111058 | Pelaporan aktif 1-tap, layar Home (peta), mock data generator |

## Arsitektur
MVVM + Clean Architecture (presentation, domain, data), data local-first
(Room lalu Cloud Firestore), Hilt, Navigation Compose, WorkManager.
Dokumentasi: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md),
[docs/DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md),
[docs/APP_FLOW.md](docs/APP_FLOW.md),
[docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md).

## Menjalankan proyek
Prasyarat: Android Studio terbaru (JDK 17+ bawaan), Android SDK 36, emulator atau
perangkat Android 8.0+ dengan Google Play Services.

1. Clone repo ini dan buka di Android Studio.
2. Letakkan `google-services.json` di folder `app/` (tidak di-commit).
3. Tambahkan `MAPS_API_KEY=...` di `local.properties` (tidak di-commit).
4. Run konfigurasi `app`.

Tes unit: `./gradlew :app:testDebugUnitTest`
Build APK debug: `./gradlew :app:assembleDebug`

## Asal kode dan penggunaan AI
Kerangka proyek (konfigurasi build, DI, design system, navigasi, autentikasi,
database lokal, aturan Firestore) berasal dari prototipe awal
[Zeezoy/GoOut](https://github.com/Zeezoy/GoOut). Fungsi inti ditulis oleh
masing-masing anggota sesuai pembagian di atas. Pengembangan dibantu AI coding
assistant (Claude Code). [Sesuaikan kalimat ini dengan ketentuan praktikum.]
