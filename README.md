<div align="center">

# ● NothingCalendar

**A minimalist calendar for Android in the Nothing OS design language.**
Dot-matrix type · Deep black · Pure white · One red accent · Haptics everywhere.

[![Build APK](https://github.com/Real3rr0r303YT/NothingCalendar/actions/workflows/build.yml/badge.svg)](https://github.com/Real3rr0r303YT/NothingCalendar/actions/workflows/build.yml)
![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-000000?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-D71921?logo=kotlin&logoColor=white)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-FFFFFF)](LICENSE)

<br>

### ☕ Like the app? Support development!

[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-Support%20NothingCalendar-FFDD00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/3rr0r303YT)

**[buymeacoffee.com/3rr0r303YT](https://buymeacoffee.com/3rr0r303YT)**

</div>

---

## Why?

Nothing Phones ship without a calendar app of their own. NothingCalendar fills that gap
with an app that looks and feels like it belongs on Nothing OS: monochrome, dotted,
calm, and tactile.

## Features

- **Month view** – 6-week grid with dot indicators for events, swipe left/right to change months
- **Dot-matrix headers** – month name and day number rendered on a custom 5×7 LED-style matrix
- **Day view** – 24-hour timeline with a live red "now" line; overlapping events are laid out side by side
- **Agenda** – the selected day's events right below the month grid
- **Create & edit events** – title, date, start/end time or all-day, notes
  - Long-press a day (month view) or tap an hour (day view) to create an event there
  - Two-tap delete to prevent accidents
- **Haptic feedback** – distinct patterns for ticks (swiping), taps, long-presses, errors and saves;
  rich composed vibration on devices that support haptic primitives (e.g. Nothing Phone's X-axis motor).
  Can be turned off in settings.
- **Nothing OS look** – `#000000` black, `#FFFFFF` white, `#D71921` red accent, monospace labels, dot grid background
- **Settings** – toggle haptics, choose Monday or Sunday as first day of the week
- **Localized** – English and German, honours the system 12/24-hour setting
- **Private** – events are stored only on your device, no account, no internet permission

## Installation

### Option A – Download the APK

1. Open the [**Actions**](https://github.com/Real3rr0r303YT/NothingCalendar/actions/workflows/build.yml) tab
   (or the [**Releases**](https://github.com/Real3rr0r303YT/NothingCalendar/releases) page for tagged versions).
2. Pick the latest successful run and download the `NothingCalendar-release` (or `-debug`) artifact.
3. Unzip it, copy the `.apk` to your phone and open it.
   You may need to allow *Install unknown apps* for your file manager or browser.

### Option B – Build it yourself

Requirements: **JDK 17** and the **Android SDK** (Android Studio Ladybug or newer is easiest).

```bash
git clone https://github.com/Real3rr0r303YT/NothingCalendar.git
cd NothingCalendar
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk

# Install directly on a connected phone (USB debugging enabled):
./gradlew installDebug
```

Or open the folder in Android Studio and press **Run ▶**.

## Usage

| Action | Where | Result |
| --- | --- | --- |
| Tap a day | Month | Select it and show its agenda |
| Tap the selected day again | Month | Open the day view |
| Long-press a day | Month | Create an event on that day |
| Swipe left / right | Month / Day | Next / previous month or day |
| Tap an empty hour | Day | Create an event at that hour |
| Tap an event | Anywhere | Edit or delete it |
| `TODAY` pill | Month / Day | Jump back to today |

## Project structure

```
NothingCalendar/
├── .github/workflows/build.yml      # CI: builds debug + release APKs, uploads artifacts
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/real3rr0r303yt/nothingcalendar/
│       │   ├── MainActivity.kt
│       │   ├── data/                 # CalendarEvent, EventRepository, SettingsRepository
│       │   ├── ui/
│       │   │   ├── CalendarViewModel.kt
│       │   │   ├── NothingCalendarApp.kt
│       │   │   ├── components/       # DotMatrixText, pills, FAB, dotted dividers
│       │   │   ├── screens/          # Month, Day, event editor, settings
│       │   │   └── theme/            # Nothing colours, typography, shapes
│       │   └── util/                 # Haptics, formatting helpers
│       └── res/                      # strings (en/de), adaptive dot-matrix icon
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradlew / gradlew.bat
```

**Tech stack:** Kotlin · Jetpack Compose · Material 3 · `java.time` · minSdk 26 / targetSdk 35

## Signed release builds (optional)

The CI signs the release APK with the debug key unless you add these repository secrets
(*Settings → Secrets and variables → Actions*):

| Secret | Content |
| --- | --- |
| `SIGNING_KEYSTORE_BASE64` | your `.jks` file, base64-encoded (`base64 -w0 release.jks`) |
| `SIGNING_STORE_PASSWORD` | keystore password |
| `SIGNING_KEY_ALIAS` | key alias |
| `SIGNING_KEY_PASSWORD` | key password |

Push a tag like `v1.0.0` and the workflow also publishes a GitHub Release with the APK.

## Roadmap

- [ ] Sync with system calendars (Google, CalDAV) via `CalendarContract`
- [ ] Reminders / notifications
- [ ] Recurring and multi-day events
- [ ] Home-screen widget in Nothing style
- [ ] Week view

## Contributing

Issues and pull requests are welcome! Please keep the design minimal – black, white, one red accent.

## Support

If NothingCalendar is useful to you, consider buying me a coffee – it keeps the project going. ❤️

<a href="https://buymeacoffee.com/3rr0r303YT"><img src="https://img.shields.io/badge/Buy%20Me%20a%20Coffee-☕-FFDD00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black" alt="Buy Me A Coffee"></a>

## License

[GNU GPL v3](LICENSE) © Real3rr0r303YT

> NothingCalendar is an independent community project and is not affiliated with,
> endorsed by, or sponsored by Nothing Technology Limited. "Nothing" is a trademark of its owner.

---

<p align="center"><b>Made by <a href="https://github.com/Real3rr0r303YT">Real3rr0r303YT</a></b></p>
