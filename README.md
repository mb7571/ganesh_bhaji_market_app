# Ganesh Bhaji Market — Customer Android App

A lightweight, full-screen Android app that opens your existing web app
**https://ganeshmarket.in/app/login** inside a native WebView — customers get
a real app experience with no browser bar.

## Features

- 📱 Full-screen WebView — looks and feels like a native app
- ✨ Branded splash screen — logo + tagline shows while the site loads, fades out automatically (also on Android 12+ system splash)
- 🔄 Pull-to-refresh + loading progress bar
- 📴 Offline screen with retry button
- ⬅️ Back button navigates web history
- 📷 Camera permission support (QR scan, profile photo)
- 📎 File upload support
- 📍 Location permission support
- ⬇️ Download support (bills, reports) via Android Download Manager
- 🔗 Links stay in-app; WhatsApp / tel / mailto / other sites open outside
- ✍️ Signed APK (same signature for every build — updates install over old versions)

## How to get the APK (no Android Studio needed)

1. Create a new **empty repository** on GitHub (e.g. `ganesh-market-app`).
2. Push this folder to it:

   ```bash
   git init
   git add .
   git commit -m "Ganesh Bhaji Market customer app"
   git branch -M main
   git remote add origin https://github.com/YOUR-USERNAME/ganesh-market-app.git
   git push -u origin main
   ```

3. Wait ~5 minutes. Open the repo → **Actions** tab → latest run →
   **Artifacts** → download **GaneshBhajiMarket-APK**.
4. Send that APK to customers, or host it on your website
   (e.g. `https://ganeshmarket.in/app/ganesh-market.apk`) so people can
   download and install it directly.

> The GitHub workflow builds automatically on every push — just update the
> `versionCode` / `versionName` in `app/build.gradle` when you release a new version.

## Installing on a phone

1. Copy the APK to the phone (WhatsApp, download link, etc.).
2. Tap it → allow **Install from unknown sources** when asked.
3. Open **Ganesh Bhaji Market** — it opens the login page like the web app.

## App signing (important)

`keystore/ganesh-market.keystore` signs every release APK.
**Keep this file safe and private** — if it is lost, you cannot publish
updates that install over the old app (users would have to uninstall first).

- Keystore password: `ganesh-market-2026`
- Key alias: `ganesh-market`

(The keystore is in `.gitignore`; keep a backup somewhere safe like Google Drive.)

## Changing the URL

Edit `START_URL` in
`app/src/main/java/in/ganeshmarket/app/MainActivity.java`.

## Project structure

```
├── app/
│   ├── build.gradle              # app config (version, signing)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/in/ganeshmarket/app/MainActivity.java
│       └── res/                  # layouts, theme, icons
├── gradle/wrapper/               # Gradle wrapper (used by CI)
├── scripts/                      # icon + keystore generators (already run)
├── keystore/                     # signing key (private)
└── .github/workflows/build-apk.yml  # auto-builds APK on push
```
