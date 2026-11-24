# Go Out - Pemantauan Kepadatan Koridor Kota Malang

Go Out adalah aplikasi Android native (Kotlin, Jetpack Compose, Google Maps SDK,
Firebase Authentication, Cloud Firestore) untuk memantau kepadatan tiga koridor
jalan utama Kota Malang berbasis crowdsourcing: telemetri GPS pasif dan laporan
aktif 1-ketukan. Gagasan terinspirasi SISRI (Surabaya Integrated Smart Road
Infrastructure), diadaptasi menjadi pendekatan ringan berbasis smartphone.

Proyek akhir Praktikum Pengembangan Aplikasi Perangkat Bergerak, TIF-B, Kelompok 7.
Dosen pengampu: Fais Al Huda, S.Kom, M.Kom.

## Fitur utama
- **Peta kepadatan**: tiga koridor digambar sebagai garis berwarna (hijau, kuning, merah) yang berubah otomatis mengikuti data terbaru.
- **Laporan aktif 1-tap**: pengguna memilih LANCAR, MERAYAP, atau MACET. Laporan hanya diterima bila pelapor berada dalam jarak 100 meter dari koridor.
- **Telemetri pasif**: kecepatan dari GPS dikirim di latar belakang (foreground service) dan diagregasi per koridor.
- **Detail koridor**: status, kecepatan rata-rata, dan waktu pembaruan terakhir tiap koridor.
- **Offline-first**: data disimpan lebih dulu di perangkat dan dikirim ulang otomatis saat koneksi kembali.
- **Autentikasi**: registrasi dan login Email/Password lewat Firebase Authentication.
- **Data simulasi**: generator kecepatan acak untuk demo, hanya aktif pada build debug.

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

Status ditampilkan UNKNOWN bila koridor tidak menerima data selama 30 menit.

## Pembagian fungsi
| Anggota | NIM | Fungsi |
|---|---|---|
| Bastian Nevan Baruch | 245150207111011 | Klasifikasi status kepadatan, data koridor, layar Detail Koridor |
| Raditya Ananta Maheswara Suryanto | 245150200111007 | Validasi geofence koridor, izin dan indikator lokasi |
| Jeremiah Harmoni Keenan Pigawahi | 245150201111008 | Telemetri pasif (Fused Location Provider) |
| Fevoura Agnesia Tatontos | 245150207111058 | Pelaporan aktif 1-tap, layar Home (peta), mock data generator |

## Cara kerja singkat
1. Data masuk dari dua sumber: telemetri GPS pasif dan laporan 1-tap pengguna.
2. Setiap data disimpan dulu ke database lokal (Room) dengan status `PENDING`, lalu dikirim ke Cloud Firestore. Bila berhasil, statusnya menjadi `SYNCED`.
3. Bila koneksi putus, `SyncWorker` (WorkManager) mengirim ulang antrean saat jaringan kembali, dengan jeda percobaan yang bertambah.
4. Data diagregasi per koridor (rata-rata bergerak eksponensial, jendela 10 menit) lalu diklasifikasikan menjadi LANCAR, MERAYAP, atau MACET.
5. Layar Home mengamati Room, sehingga warna koridor di peta berubah otomatis setiap ada data baru.

## Teknologi
| Bagian | Teknologi |
|---|---|
| Bahasa dan UI | Kotlin 2.2, Jetpack Compose (Material 3), Navigation Compose |
| Arsitektur | MVVM + Clean Architecture, StateFlow, Hilt |
| Data lokal | Room, DataStore |
| Data jarak jauh | Firebase Authentication, Cloud Firestore |
| Lokasi dan peta | Fused Location Provider, Google Maps Compose |
| Latar belakang | WorkManager (sinkronisasi), foreground service (telemetri) |
| Pengujian | JUnit (unit test), Compose UI test |

minSdk 26 (Android 8.0), targetSdk 36.

## Struktur proyek
```
app/src/main/java/com/example/goout/
  feature/     layar dan ViewModel: auth, home, corridor, report, location, profile
  domain/      model, antarmuka repository, dan use case (klasifikasi, geofence, laporan, telemetri)
  data/        Room (local), Firebase (remote), implementasi repository
  service/     telemetry (foreground service) dan sync (WorkManager)
  core/        design system, navigasi, konfigurasi koridor, utilitas geo, mock generator
  di/          modul Hilt
docs/          dokumentasi arsitektur, skema database, alur aplikasi, setup Firebase
firestore.rules  aturan keamanan Firestore
```

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
2. Siapkan proyek Firebase (Authentication Email/Password dan Cloud Firestore) mengikuti [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md), lalu letakkan `google-services.json` di folder `app/` (tidak di-commit).
3. Buat API key Google Maps (Maps SDK for Android), lalu tambahkan `MAPS_API_KEY=...` di `local.properties` tanpa tanda kutip (tidak di-commit).
4. Sync Gradle, lalu Run konfigurasi `app`.

Tes unit: `./gradlew :app:testDebugUnitTest`
Build APK debug: `./gradlew :app:assembleDebug`

Catatan: tanpa `google-services.json` proyek tetap dapat dikompilasi dan diuji,
tetapi login dan sinkronisasi cloud tidak berjalan. Tanpa `MAPS_API_KEY`, peta
tampil kosong sedangkan fitur lain tetap berfungsi.

## Alur kerja tim
Setiap anggota mengerjakan fungsinya di branch sendiri (`feature/...`), membuka
Pull Request ke `main`, lalu di-merge dengan **Rebase and merge** agar riwayat
commit tetap linear. Branch `main` dilindungi, sehingga perubahan hanya masuk
lewat Pull Request.

<!--
## Tampilan aplikasi
Tambahkan screenshot di docs/screenshots lalu aktifkan bagian ini.

| Home | Lapor | Detail Koridor |
|---|---|---|
| ![Home](docs/screenshots/home.png) | ![Lapor](docs/screenshots/report.png) | ![Detail](docs/screenshots/detail.png) |
-->