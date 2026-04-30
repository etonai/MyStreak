# DevCycle 001: Initial App Implementation

**Status:** Work Complete
**Start Date:** 2026-04-29
**Target Completion:** 2026-04-29
**Focus:** Build the complete MyStreak Android app from scratch per the design spec in `doc/planning/ideas/MyStreak.md`

---

## Goal

MyStreak is a new Android activity tracking app. No source code existed before this cycle — it covers full implementation from project creation through a functional, shippable v1. The design is fully specified in `doc/planning/ideas/MyStreak.md`, with PlayStreak implementation patterns documented in `doc/planning/ideas/PlayStreakForMyStreak.md` as the reference for architecture and UX conventions.

## Desired Outcome

A working Android app that allows the user to create and manage Tasks, log Activities against them, view a Dashboard with streak/summary information, and browse a Calendar with color-coded daily history. All behavior defined in the design spec is implemented. The app builds and runs on a physical device or emulator with local data persistence.

---

## Tasks

### Phase 1: Project Setup & Data Layer

**Status:** Work Complete

- [x] Create new Android project (Kotlin, Min SDK 24, Target SDK 36, Material 3 theme, dark mode support)
- [x] Add dependencies: Room 2.6.1, AndroidX Navigation Component 2.7.7, Material Design Components, ViewBinding, Safe Args, KSP, core library desugaring
- [x] Define `Task` entity (id, name, colorKey, priority, minSuccessDesc, medSuccessDesc, highSuccessDesc, isActive, dateCreated)
- [x] Define `TaskActivity` entity (id, taskId FK, timestamp, successLevel)
- [x] Define `CalendarDayCache` entity (dateEpochDay, colorLevel)
- [x] Write DAOs: `TaskDao`, `TaskActivityDao`, `CalendarDayCacheDao`
- [x] Write `MyStreakRepository` as the single data access layer
- [x] Wire up Room database class (`MyStreakDatabase`) with all entities, DAOs, and type converters
- [x] Write `DateUtils` helper (midnight boundary logic, date formatting, `java.time` via desugaring)
- [x] Write `StreakCalculator` utility (consecutive-day streak from activity data)

**Technical Notes:**
Stack mirrors PlayStreak: Kotlin, MVVM (Fragments + ViewModels + ViewBinding), Room, AndroidX Navigation, Material 3. Repository pattern with a single `MyStreakRepository`. The `CalendarDayCache` table stores one row per past day (epochDay as Long, colorLevel as Int via type converter). Today's color is always computed live. `coreLibraryDesugaringEnabled = true` is required for `java.time` on API 24.

---

### Phase 2: Navigation Shell & Bottom Nav

**Status:** Work Complete

- [x] Set up bottom navigation bar with three tabs: Dashboard, Calendar, Tasks
- [x] Create Fragments for each tab
- [x] Set up `nav_graph.xml` with top-level destinations and nested destinations (TaskDetail, AddEditTask)
- [x] Safe Args configured for type-safe argument passing

**Technical Notes:**
Uses AndroidX Navigation Component with a `NavHostFragment` and `BottomNavigationView` in a vertical `LinearLayout` in `activity_main.xml`. Each tab is a top-level destination. Additional destinations (TaskDetail, AddEditTask) are nested. BottomSheets (LogActivity, ActivityEdit, DayActivities) are not in the nav graph — they are shown directly via `show()`.

---

### Phase 3: Tasks Tab

**Status:** Work Complete

- [x] Build Tasks list screen with RecyclerView (`TasksFragment`, `TasksViewModel`)
- [x] Task list item: color dot, name, priority label, today's activity count, inactive indicator, inline "+" log button
- [x] Chip-based sort controls: Alphabetical (default), Recent, Activity Count
- [x] Floating Action Button opens Add Task screen
- [x] Task detail/edit screen (`TaskDetailFragment`, `TaskDetailViewModel`)
- [x] Add/Edit Task screen (`AddEditTaskFragment`, `AddEditTaskViewModel`): name, 12-color swatch grid, High/Low priority toggle, three success threshold text fields
- [x] Task deletion with confirmation dialog (cascade-deletes all Activities via Room FK)
- [x] Inactive Tasks greyed out (alpha 0.45), log button disabled
- [x] 12-color predefined Task palette defined in `colors.xml` and `ColorUtils`

**Technical Notes:**
`TaskListItem` is a data class combining `Task` with computed `todayCount`, `lastActivityTimestamp`, and `totalCount` for sorting. Sorting is done in-memory in the ViewModel after fetching from Room. Color palette displayed as a 6-column `GridLayoutManager` RecyclerView with `ColorSwatchAdapter`. Selected color indicated by a white ring overlay (`bg_swatch_selected.xml`).

---

### Phase 4: Activity Logging Flow

**Status:** Work Complete

- [x] Logging flow as a `BottomSheetDialogFragment` (`LogActivityBottomSheet`, `LogActivityViewModel`)
- [x] Step 1 (from Dashboard): Select Task from active Tasks list
- [x] Step 1 (from Task detail or Tasks "+" button): Task pre-selected, jumps to Step 2
- [x] Step 2: Three tappable buttons showing user's own Minimum/Medium/High threshold descriptions
- [x] Step 3: Summary (task name, level, time) with Save button
- [x] Activity edit bottom sheet (`ActivityEditBottomSheet`, `ActivityEditViewModel`): date/time picker (max = today), success level toggle, delete with confirmation
- [x] Activity edit accessible from Today's Activities, Yesterday's Activities (Dashboard), Calendar daily listing, and Task history

**Technical Notes:**
`LogActivityViewModel` takes a `preselectedTaskId` (-1 means show task selector). Step transitions managed by a `LogStep` enum and state-switching visibility in the layout. `ActivityEditBottomSheet` uses `DatePickerDialog` + `TimePickerDialog` chained together. Future dates are blocked by setting `datePicker.maxDate = System.currentTimeMillis()`.

---

### Phase 5: Dashboard

**Status:** Work Complete

- [x] `DashboardFragment` and `DashboardViewModel`
- [x] Current Streak section: computed via `StreakCalculator` from all activities
- [x] Today's Activities: Flow-backed LiveData, sorted ascending by timestamp, includes inactive Task activities
- [x] Yesterday's Activities: same format
- [x] Week Summary: total and high-priority activity counts for last 7 rolling days, active Tasks only
- [x] High Priority Outstanding: active High Priority Tasks with no activity today; cleared reactively when any activity is logged
- [x] Add Activity FAB opens `LogActivityBottomSheet` with task selector
- [x] Import/Export accessible via overflow menu (`menu_dashboard.xml`)
- [x] All sections update reactively via Room Flow → LiveData

**Technical Notes:**
`DashboardViewModel` launches a coroutine in `init` that collects the today-activities Flow and calls `refresh()` on each emission to recompute streak, week summary, and outstanding list. Import uses `ActivityResultContracts.OpenDocument`; export uses `ActivityResultContracts.CreateDocument`.

---

### Phase 6: Calendar

**Status:** Work Complete

- [x] `CalendarFragment` and `CalendarViewModel`
- [x] Monthly grid: 7-column RecyclerView, Sun–Sat header, Month/Year label, Previous/Next buttons
- [x] `SquareCellDecoration` makes each day cell square by overriding cell height in `getItemOffsets`
- [x] Today's color computed live; past days use `CalendarDayCache`
- [x] Color freeze runs on every `MainActivity.onResume` via `repo.freezePastDays()`
- [x] Calendar color rules (Light Blue / Medium Blue / Dark Blue / Bright Green) implemented in `MyStreakRepository.computeColorForDay()`
- [x] Today highlighted with a yellow ring (`bg_calendar_today_ring.xml`)
- [x] Tapping a day opens `DayActivitiesBottomSheet` showing all Activities for that day with task color, time, and success level
- [x] Activity entries in daily listing are tappable to open `ActivityEditBottomSheet`

**Technical Notes:**
`CalendarViewModel` builds a `List<CalendarCell>` that includes empty padding cells for the day-of-week offset before the 1st of the month. Live today color is refreshed in both `onViewCreated` and `onResume`. The `CalendarDayAdapter` holds a `todayColor` field updated separately from the list data.

---

### Phase 7: Import / Export

**Status:** Work Complete

- [x] Export: serializes all Tasks and Activities to JSON via `JsonExporter` (Gson); Android file picker for save location; success/error Snackbar
- [x] Import: `JsonImporter` reads JSON; warning dialog → confirmation → replace database; success Snackbar with counts
- [x] `ExportData` schema: `{ tasks: [...], activities: [...] }`
- [x] Import rebuilds old-ID-to-new-ID mapping so Activity→Task foreign keys remain correct after re-insertion
- [x] `CalendarDayCache` is cleared on import and recomputed on next app open

**Technical Notes:**
Import/export triggered from Dashboard overflow menu. Full-replace semantics: all existing tasks and activities are deleted before inserting imported data. `CalendarDayCache` is not part of the export — it is derived data and is regenerated by `freezePastDays()` on next app resume.

---

## Open Questions

1. **Logging from the Tasks tab — entry point** — Resolved: implemented as a dedicated "+" button on each Task card in the list. Inactive Tasks have the button disabled.

2. **Task detail screen — contents** — Resolved: shows Task name, color, priority, active status, all three success threshold descriptions, a "Log Activity" button, Edit Task button, Set Active/Inactive toggle, Delete button, and a scrollable recent activity history list.

---

## Notes and Risks

- `java.time` APIs on API 24 require `coreLibraryDesugaringEnabled = true` with the `desugar_jdk_libs` dependency.
- Safe Args plugin added for type-safe navigation arguments.
- Calendar cell height is enforced via `SquareCellDecoration` since RecyclerView cells cannot declare a square aspect ratio in XML alone.
- Launcher icons use `mipmap-anydpi` XML drawables as placeholders; production icon should be replaced via Android Studio's Image Asset tool.
- The calendar color freeze uses the current active High Priority Task count at freeze time. Because colors are frozen eagerly on each app open, past day colors correctly reflect the task priorities in effect at that time.

---

## Completion Summary

**Completion Date:** 2026-04-29
**Phases Completed:** All (1–7)
**Work Deferred:** None

**Accomplishments:**
- Full Android project created from scratch
- Data layer: Task, TaskActivity, CalendarDayCache entities; 3 DAOs; MyStreakRepository
- Utilities: DateUtils, StreakCalculator, ColorUtils (12-color palette), JsonExporter, JsonImporter
- All 7 phases of planned work implemented

**Metrics:**
- Kotlin source files: 37
- XML resource files: 38 (layouts, drawables, values, navigation, menu, mipmap)
- Build/config files: 5

**Lessons / Notes:**
- Separated domain Activity class from Android's `android.app.Activity` by naming it `TaskActivity` — avoids import conflicts in Fragment files.
- BottomSheetDialogFragments used for all transient flows (logging, activity edit, day activities) to keep the nav graph simple.
- `combine` on multiple Flows in `TasksViewModel` and `CalendarViewModel` cleanly merges data + sort-mode state without extra LiveData machinery.
