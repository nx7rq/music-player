# 🎵 Music Player App — Android (Kotlin)

A fully-featured Android music player app with dark mode, playlist management, and media notifications.

---

## Features
- 📋 **Song Library** — Auto-loads all music from your device storage
- ▶️ **Playback Controls** — Play, pause, skip next/previous with seek bar
- 🔔 **Media Notifications** — Lockscreen + notification panel controls (foreground service)
- 🌙 **Dark Mode** — Toggle via the overflow menu (⋮)
- 🎶 **Playlists** — Create playlists and add songs via long press
- 🔁 **Auto-advance** — Automatically plays the next song when one finishes
- 🎛️ **Mini Player** — Persistent bottom bar on the library screen

---

## Project Structure

```
MusicPlayerApp/
├── app/src/main/
│   ├── java/com/musicplayer/
│   │   ├── MainActivity.kt          ← Song library + mini player
│   │   ├── PlayerActivity.kt        ← Now Playing screen
│   │   ├── PlaylistActivity.kt      ← Playlist manager
│   │   ├── adapters/
│   │   │   └── SongAdapter.kt       ← RecyclerView adapter
│   │   ├── models/
│   │   │   └── Song.kt              ← Song + Playlist data classes
│   │   └── services/
│   │       └── MusicService.kt      ← Foreground playback + notifications
│   └── res/
│       ├── layout/                  ← All XML layouts
│       ├── drawable/                ← Vector icons
│       └── values[-night]/          ← Light + Dark themes
├── app/build.gradle
├── build.gradle
└── settings.gradle
```

---

## How to Build & Run

### Prerequisites
- **Android Studio** (Hedgehog 2023.1.1 or newer)
- **JDK 8+**
- Android device or emulator running **API 24+** (Android 7.0+)

### Steps
1. **Open** Android Studio → `File > Open` → select the `MusicPlayerApp` folder
2. Wait for **Gradle sync** to complete
3. Click **▶ Run** (or press `Shift+F10`)
4. On first launch, **grant storage permission** when prompted
5. Your device's music library will populate the list automatically

### Testing Without Real Music Files
Use the Android Emulator and push `.mp3` files via:
```bash
adb push your_song.mp3 /sdcard/Music/
```
Then restart the app.

---

## Permissions Used
| Permission | Purpose |
|---|---|
| `READ_MEDIA_AUDIO` | Read music files (Android 13+) |
| `READ_EXTERNAL_STORAGE` | Read music files (Android ≤ 12) |
| `FOREGROUND_SERVICE` | Background music playback |
| `POST_NOTIFICATIONS` | Media notification controls |

---

## Dark Mode
Toggle from the **⋮ menu** → **Dark Mode** at any time. The app remembers your preference for the session.

---

## Dependencies
All dependencies are fetched automatically via Gradle:
- `androidx.media` — MediaStyle notifications
- `Material3` — UI components & theming
- `RecyclerView` — Song list
- `Glide` — Album art image loading (ready for use)
