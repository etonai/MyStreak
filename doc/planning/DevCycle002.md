# DevCycle 002: Visual Alignment with PlayStreak

**Status:** Work Complete
**Start Date:** 2026-04-29
**Target Completion:** 2026-04-30
**Focus:** Update MyStreak's visual design to match PlayStreak's look and feel, using the assembled reference files in `reference/`

---

## Goal

MyStreak was built in DC-1 using Material Design defaults and currently looks like a generic Material app. The goal of this cycle is to make MyStreak feel visually familiar to a PlayStreak user: same color palette, same card-based layout patterns, same component density and typography hierarchy, and matching drawable styles.

The reference source is the `reference/` directory, which contains the actual layout, drawable, and value files from PlayStreak.

## Desired Outcome

A user who is familiar with PlayStreak opens MyStreak and recognizes the visual language immediately — the purple/teal color scheme, the card-wrapped section layout on the Dashboard, the circular calendar day cells, the sort-control card on the Tasks tab, and the general typography scale. The apps look like they come from the same family even though they track different domains.

---

## Tasks

### Phase 1: Theme and Color Foundation

**Status:** Work Complete

- [x] Add missing named colors to `values/colors.xml`:
  - `purple_200 = #FFBB86FC` (used as dark-mode `colorPrimary`)
  - `black = #FF000000` and `white = #FFFFFFFF`
  - `teal_700 = #FF018786`
  - `star_yellow = #FFFFC107`
  - `calendar_selection_ring = #FFCC99FF` (lavender, used when a calendar day is selected)
  - `edit_text_background = #FFFFFFFF`, `edit_text_text = #FF000000`, `edit_text_hint = #FF666666`
- [x] Update `values/themes.xml` (light theme):
  - Add `android:windowLightStatusBar = true`
  - Add `RoundedButton` style (`cornerRadius = 24dp`) matching PlayStreak
- [x] Update `values-night/themes.xml` (dark theme) to mirror PlayStreak's dark pattern:
  - `colorPrimary = @color/purple_200`
  - `colorOnPrimary = @color/black`
  - `android:windowLightStatusBar = false`
- [x] Update `bg_calendar_day.xml` from rounded-rectangle to oval shape (matching PlayStreak's `calendar_day_background.xml`: oval with transparent fill and 1dp transparent stroke)
- [x] Add `circle_indicator.xml` drawable: solid oval with `calendar_no_activity` default fill (color overridden programmatically) — used as the color dot next to a selected date label
- [x] Add `rounded_background.xml` drawable: rectangle with 12dp corner radius, transparent fill

**Technical Notes:**
PlayStreak's `values/colors.xml` uses `purple_500/#FF6200EE` as light-mode primary and `purple_200/#FFBB86FC` as dark-mode primary. MyStreak's current `colors.xml` already has `purple_500` and `purple_700` but is missing `purple_200`, `black`, `white`, and the auxiliary colors above. The calendar color names (`calendar_light_blue`, `calendar_medium_blue`, etc.) are intentionally different from PlayStreak's (`calendar_practice_*`, `calendar_performance_*`) because MyStreak uses a different semantic system — do not change those names. `bg_calendar_today_ring.xml` already exists and should be kept as-is (yellow ring for today).

---

### Phase 2: Dashboard Card Layout

**Status:** Work Complete

- [x] Wrap each Dashboard section in a `MaterialCardView` (`cardElevation="4dp"`, inner `padding="16dp"`):
  - "Current MyStreak" card: streak number + label inside a card
  - "Today's Activities" card (contains section header + RecyclerView)
  - "Yesterday's Activities" card (contains section header + RecyclerView)
  - "Week Summary" card
  - "High Priority Outstanding" card (conditional, collapses when empty)
- [x] Change the streak number display from raw `48sp` to `textAppearanceHeadline4` with `textColor="?attr/colorPrimary"` (matching PlayStreak's streak card)
- [x] Change section headers (`Today`, `Yesterday`, `Week Summary`, etc.) from `textSize="16sp" textStyle="bold"` to `textAppearance="?attr/textAppearanceSubtitle1"`
- [x] Replace `FloatingActionButton` with `ExtendedFloatingActionButton` (text `"Add Activity"`, icon `@drawable/ic_add`, `textColor` + `iconTint` = `?attr/colorOnPrimary`, `backgroundTint` = `?attr/colorPrimary`) — matching PlayStreak
- [x] Update `DashboardFragment.kt` for any binding reference changes caused by layout restructuring

**Technical Notes:**
PlayStreak's `fragment_dashboard.xml` uses `ScrollView → LinearLayout` with `MaterialCardView` wrappers per section. MyStreak currently uses `NestedScrollView → LinearLayout` with flat TextViews and bare RecyclerViews. The `NestedScrollView` should be kept (it works better with the nested RecyclerViews than a plain `ScrollView` does). Cards should use 16dp bottom margin between them to match PlayStreak's spacing. The outstanding section cards should toggle visibility at the card level, not just the header TextView.

---

### Phase 3: Calendar Layout Rework

**Status:** Work Complete

- [x] Move month navigation header to the **top** of `fragment_calendar.xml`, above the day-of-week labels and grid (currently it is below the grid)
- [x] Give the month navigation row a `colorSurface` background to match PlayStreak's calendar header
- [x] Update the month/year `TextView` to use `textAppearanceHeadline6` (currently raw `textSize="18sp" textStyle="bold"`)
- [x] Update prev/next month buttons to use `textSize="24sp"` single-character text (`‹` and `›`) matching PlayStreak
- [x] Add a "Monthly Activity" `MaterialCardView` section below the calendar grid (above the legend):
  - Shows "Active Days: X" and "Total Activities: X" for the displayed month
  - Hint text: "Tap a date to view its activities"
  - Wire up `CalendarViewModel` to compute these stats from the displayed month's data
- [x] Add `CalendarFragment` support for a color-indicator dot (`circle_indicator` drawable, 24dp) next to the selected-date label in the day activities bottom sheet (`DayActivitiesBottomSheet`) — color matches the day's calendar color
- [x] Update `item_calendar_day.xml` to center the day-number `TextView` in a fixed 40dp circle (matching PlayStreak's 40×40dp oval cell inside a 52dp height row)

**Technical Notes:**
MyStreak uses a custom `RecyclerView`-based calendar grid (`SquareCellDecoration`); PlayStreak uses the `kizitonwose/calendar-view` library. Switching libraries is out of scope for DC-2 — the focus is on matching visual output, not switching implementations. The month nav reorder is a layout-only change with no code impact. The monthly stats require querying `TaskActivityDao` for a date range matching the displayed month; add a `getActivitiesForMonth(startEpochDay: Long, endEpochDay: Long): Flow<List<TaskActivity>>` (or suspend) method in the DAO and surface it through `MyStreakRepository` and `CalendarViewModel`. The color-indicator dot in `DayActivitiesBottomSheet` requires passing the day's color level to the sheet (add it as an argument alongside the existing date argument).

---

### Phase 4: Tasks Tab Sort Control Card

**Status:** Work Complete

- [x] Wrap the sort `ChipGroup` in a `MaterialCardView` (`cardElevation="2dp"`, `cardCornerRadius="8dp"`, `margin="8dp"`) with a horizontal `LinearLayout` (padding 12dp) — matching PlayStreak's `fragment_pieces.xml` sort control card
- [x] Add a sort-direction toggle `MaterialButton` (`TextButton` style, text `"↑"`, `textSize="18sp"`, `minWidth="48dp"`) to the right of the `ChipGroup`
- [x] Change chip style from `Widget.MaterialComponents.Chip.Filter` to `Widget.MaterialComponents.Chip.Choice` (matching PlayStreak)
- [x] Update `TasksFragment.kt` to toggle sort direction (ascending / descending) on button tap and update button text (`↑` / `↓`)
- [x] Update `TasksViewModel` to expose a sort-direction state (`Boolean isAscending`) and incorporate it into the in-memory sort logic for all three sort modes

**Technical Notes:**
PlayStreak's `fragment_pieces.xml` sort card layout: outer `MaterialCardView` → `LinearLayout` (horizontal, `center_vertical`, 12dp padding) → `"Sort:"` label + `ChipGroup` (weight 1) + direction button. The existing three chips (`chip_alpha`, `chip_recent`, `chip_count`) keep their IDs. The `TasksViewModel` already sorts in-memory in `combine`; wrapping the comparator with `.let { if (isAscending) it else it.reversed() }` is sufficient.

---

### Phase 5: Activity Item Visual Refinement

**Status:** Work Complete

- [x] Update `item_activity.xml` to use a `MaterialCardView` wrapper (`cardElevation="2dp"`, horizontal margin 8dp, vertical margin 4dp) — matching PlayStreak's `item_activity.xml`
- [x] Update the inner layout to show:
  - Time string in `textAppearanceBody2` + `textColorSecondary` (top-left)
  - Edit and delete icon buttons (`32dp`, borderless background) on the top-right
  - Task name in `textAppearanceSubtitle1` (second row)
  - Success level description in `textAppearanceBody2` (third row)
- [x] Verify `DashboardFragment`, `DayActivitiesBottomSheet`, and `TaskDetailFragment` adapters bind correctly to updated view IDs

**Technical Notes:**
PlayStreak's `item_timeline_activity.xml` uses `MaterialCardView` with `cardElevation="2dp"` and inner `LinearLayout` with `padding="16dp"`. MyStreak's current `item_activity.xml` is a bare `LinearLayout` with a color bar, task name, level, and time — no card wrapper. The color bar (`view_activity_color`) should be replaced by the task name color being applied to the task-name `TextView` using `textColor` set programmatically, matching PlayStreak's approach of showing the piece name prominently without a colored bar. The edit/delete icon buttons should use `@drawable/ic_edit` and `@drawable/ic_delete` (already in the drawable directory). The delete button triggers the existing delete-with-confirmation flow; the edit button opens `ActivityEditBottomSheet` — same as the current long-press behavior, but now via an explicit button.

---

## Open Questions

1. **Calendar library migration (kizitonwose)** — Resolved: deferred to a future DevCycle. DC-2 achieves visual alignment without an architecture change.

2. **Piece list item simplicity vs. Task item richness** — Resolved: keep the existing Task card design. Phase 4 aligns the Tasks tab *frame* (sort control card) without changing the item itself.

---

## Notes and Risks

- Dashboard layout changes (Phase 2) will require updating view-binding references in `DashboardFragment.kt`. If RecyclerView IDs change, the adapter setup code must be updated.
- The sort-direction toggle (Phase 4) adds a new ViewModel state field. Take care that the new `isAscending` LiveData/StateFlow does not reset when the fragment is recreated (use `SavedStateHandle` if needed).
- The `item_activity.xml` change (Phase 5) touches adapters used in three places (`DashboardFragment`, `DayActivitiesBottomSheet`, `TaskDetailFragment`). Verify all three after the update.
- Activity item edit/delete buttons (Phase 5) expose actions that were previously only accessible via long-press or the bottom sheet. Confirm the UX matches the existing `ActivityEditBottomSheet` trigger points and that no duplicate delete flows are introduced.

---

## Completion Summary

*Fill in when the cycle closes. Move this document to `doc/planning/completed/` afterward.*

**Completion Date:** 2026-04-30
**Phases Completed:** All (1–5)
**Work Deferred:** None

**Accomplishments:**
- Aligned theme and color palette with PlayStreak: added `purple_200`, `teal_700`, `star_yellow`, `calendar_selection_ring`, `edit_text_*` colors; added `RoundedButton` style; updated dark mode theme to use `purple_200` as `colorPrimary`
- Updated calendar day drawables (`bg_calendar_day.xml`, `bg_calendar_today_ring.xml`) from rectangles to ovals; added `circle_indicator.xml` and `rounded_background.xml`
- Dashboard reworked: each section wrapped in `MaterialCardView` (4dp elevation); streak display uses `textAppearanceHeadline4` + `colorPrimary`; FAB replaced with `ExtendedFloatingActionButton` showing "Add Activity"
- Calendar reworked: month navigation moved to the top with `colorSurface` background; calendar day cells now show a centered 40dp circle; "Monthly Activity" stats card added (active days + total activities); color-indicator dot added to `DayActivitiesBottomSheet`
- Tasks tab: sort controls wrapped in a card; `↑`/`↓` sort-direction toggle button added; chip style changed to Choice; `TasksViewModel` updated with `isAscending` state
- Activity list items now use `MaterialCardView` (2dp elevation) with explicit Edit and Delete icon buttons; delete triggers a confirmation dialog inline; `ic_edit.xml` added to drawables

**Metrics:**
- Files modified: 26
- XML resources changed: 13 (2 values, 5 drawables, 4 layouts + 2 new layouts)
- Kotlin files changed: 13 (3 ViewModels, 1 DAO, 1 Repository, 2 Adapters, 2 Fragments, 1 BottomSheet, 1 new drawable added)

**Lessons / Notes:**
- `setBackgroundColor()` replaces a drawable's shape entirely; must use `ViewCompat.setBackgroundTintList()` to tint an oval drawable while preserving its shape.
- Kotlin self-referential lambdas (`val x = Foo(onAction = { x.bar() })`) require `lateinit var` to avoid a compile-time forward-reference error.
- `CalendarColorLevel.values()[ordinal]` used instead of `.entries` for Kotlin < 1.9 compatibility.
