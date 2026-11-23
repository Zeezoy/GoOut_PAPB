# Setup Firebase untuk Go Out

## 1. Buat Project Firebase
1. Buka https://console.firebase.google.com → **Add project**.
2. Nama bebas (mis. `goout-malang`). Google Analytics opsional (boleh off).

## 2. Daftarkan Aplikasi Android
1. Di project overview → ikon Android.
2. **Package name**: `com.example.goout` (harus persis).
3. Debug signing SHA-1 opsional (hanya perlu untuk Google Sign-In; MVP memakai
   Email/Password sehingga boleh dilewati). Jika ingin menambahkannya:
   `./gradlew signingReport`.
4. Unduh **google-services.json** → letakkan di folder `app/`
   (`GoOut/app/google-services.json`). File ini ada di `.gitignore` — jangan
   pernah di-commit.
5. Sync Gradle. Plugin Google Services diterapkan otomatis saat file ada.

## 3. Authentication
1. Build → **Authentication** → Get started.
2. Sign-in method → aktifkan **Email/Password**.

## 4. Cloud Firestore
1. Build → **Firestore Database** → Create database.
2. Pilih lokasi (mis. `asia-southeast2` / Jakarta).
3. Mulai dalam **production mode** (rules akan di-deploy di langkah 5).

## 5. Deploy Security Rules
Pilihan A (CLI):
```bash
npm install -g firebase-tools
firebase login
firebase init firestore   # pilih project; arahkan rules ke firestore.rules
firebase deploy --only firestore:rules
```
Pilihan B (Console): buka tab **Rules**, salin seluruh isi
[`firestore.rules`](../firestore.rules), Publish.

Untuk pengujian rules tanpa menyentuh data produksi, gunakan
**Firebase Emulator Suite**: `firebase emulators:start --only firestore,auth`.

## 6. Data Koridor
Tidak ada langkah manual: saat aplikasi pertama berjalan dengan pengguna
terautentikasi, dokumen `corridors/{corridorId}` dibuat otomatis (merge) dari
`CorridorConfig.kt` — hanya metadata & geometri, tanpa data lalu lintas.

Verifikasi di Console: Firestore → collection `corridors` → 3 dokumen.

## 7. Checklist Konfigurasi
- [ ] `app/google-services.json` ada
- [ ] Email/Password sign-in aktif
- [ ] Firestore database dibuat
- [ ] `firestore.rules` ter-deploy
- [ ] `MAPS_API_KEY` terisi di `local.properties`
- [ ] Jalankan aplikasi → register akun → 3 koridor muncul di Firestore

## Catatan Keamanan
- Jangan pernah menaruh service account key / private key apa pun di repo
  atau di dalam APK.
- App Check (Play Integrity) dapat ditambahkan sebagai penguatan opsional:
  Console → App Check → daftarkan app; SDK `firebase-appcheck-playintegrity`.
  Tidak diaktifkan pada MVP agar emulator/development tetap sederhana.
