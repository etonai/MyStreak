# MyStreak

An Android app for tracking recurring personal activities and maintaining streaks.

## What it does

MyStreak helps you build habits by tracking **Tasks** (things you do repeatedly) and **Activities** (individual instances of performing a Task). The app is built around daily accountability and streak motivation.

- **Dashboard** — At-a-glance view of your current streak, today's and yesterday's logged activities, a 7-day rolling week summary, and a list of high-priority tasks you haven't done yet today.
- **Calendar** — Visual history of daily activity intensity, color-coded from light blue (any activity) to bright green (all high-priority tasks completed).
- **Tasks** — Manage your task list. Each task has a name, color, priority (High/Low), and three success threshold descriptions (Minimum / Medium / High) that you define yourself.

## Key behaviors

- Each task can be logged multiple times per day. Each log is a separate Activity with its own timestamp and success level.
- Activities can be edited after logging (date/time and success level). Date editing is constrained to today or earlier.
- Calendar colors for past days are frozen at midnight and not recalculated by subsequent edits.
- Tasks can be made inactive without deleting their history. Inactive tasks are greyed out in the task list.
- Data is stored locally on the device. JSON import/export is supported.
- Dark mode is supported.

## Tech stack

- **Language:** Kotlin
- **Architecture:** MVVM with LiveData
- **Database:** Room
- **Navigation:** AndroidX Navigation Component with Safe Args
- **UI:** Material 3, ViewBinding
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 36

## Building

Open the project in Android Studio and press Run, or from the command line:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

## License

MIT — see [LICENSE](LICENSE).
