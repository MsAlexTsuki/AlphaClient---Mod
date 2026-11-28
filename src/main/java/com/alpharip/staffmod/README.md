# Alpha StaffMod Prototype Implementation Guide

## Prototype location — required folder

**All standalone prototypes belong in `src/main/java/com/alpharip/staffmod/Prototypes/`.**

This README is stored directly beside `staffops/` in `src/main/java/com/alpharip/staffmod/`. When creating, extending, or inspecting a prototype, use `Prototypes/` first. Do not create a new prototype beside `staffops/` and do not place a prototype under `staffops/ideas/` unless the user explicitly asks for an idea note there. Keep `staffops/` for its separate ideas/done workflow. The package-root files, including `StaffMod.java`, are not prototype folders.

The previous repository-root `Prototypes/` directory has been moved into `src/main/java/com/alpharip/staffmod/Prototypes/`. The existing prototype folders were retained during the move; their contents were not rewritten. The `CHANGELOG.md`, `staffops/`, `Prototypes/`, and this README are now grouped under `src/main/java/com/alpharip/staffmod/`.

This document is the implementation checklist for Java prototype source in `src/main/java/com/alpharip/staffmod/Prototypes/` on branch `mod-ideas`.

- Scope inspected: the complete `src/main/java/com/alpharip/staffmod/Prototypes/` tree on `mod-ideas`.
- Current verified inventory: **751 Java files across 69 top-level prototype folders** in `src/main/java/com/alpharip/staffmod/Prototypes/`. The package-root `StaffMod.java` is separate and is not included in this prototype count.
- Current branch source inventory: **2,705 Java files** in total — 751 in `Prototypes/`, 1,495 in `staffops/ideas/`, 458 in `staffops/done/`, and the separate root `StaffMod.java`.
- This is a prototype roadmap, not a claim that these modules are production-ready.
- The existing source idea files under `staffops/ideas/` are not included in this inventory.
- Do not copy these prototypes into AlphaClient Core or `development` unless that work is separately and explicitly authorized.
- Do not change Alpha Launcher as part of this work.
- Do not add Supabase calls, schemas, migrations, credentials, or remote persistence. If a future design may need it, leave a clearly scoped comment only.
- Keep existing features and the established Alpha rank/permission rules unchanged. Staff Dashboard screens must use the real staff authorization layer when that integration is separately approved; local prototype data must never grant permissions.
- Target environment: Minecraft 1.8.9, Forge 11.15.1.2318, Java 8 / current project ForgeGradle setup. Confirm the actual build configuration before selecting APIs.

## How to use this checklist

For each file, the note describes its current responsibility as indicated by the source and the work required before a production integration. A model/enum/DTO may need only validation, tests, and wiring; a GUI or event adapter usually needs more. Do not rewrite a file just because it appears in this list.

**Status meanings**
- **Present in prototype:** a Java source file exists and already contains the named model, calculation, GUI, or adapter boundary.
- **Integration required:** the file still needs to be connected to real Minecraft/Forge events, an approved module entry point, a menu, or another production source.
- **Verify:** compile and behavior have not been established by this inventory. Never mark a feature tested without running the relevant checks.

---

## Access Review Reminder

Folder: `AccessReviewReminder/`

- **AccessReviewEntry.java** — Represents one staff member's review record (identity/name, rank, last-reviewed date, review interval, next-review date, and status). Complete date/interval validation, stable identity handling, and status-boundary tests; ensure due/upcoming calculations use one consistent clock and timezone.
- **AccessReviewReminder.java** — Owns the reminder instance and entry operations (load, list, add, update, mark reviewed, remove, due count, upcoming count). Finish the lifecycle and error handling around loading; connect it only to an approved local storage path and authorized staff data source.
- **AccessReviewReminderGui.java** — Displays and manages review reminders. Complete selection, add/edit/remove/mark-reviewed interactions, empty/loading/error states, scrolling, and screen navigation; register it only through an approved Staff Dashboard entry and apply the real staff permission checks.
- **AccessReviewStorage.java** — Handles reminder persistence. Verify its serialization format, malformed-file handling, safe writes, directory creation, and compatibility/versioning; test save/load round trips and recovery from a corrupt or missing file.

## Accessibility Theme

Folder: `AccessibilityTheme/`

- **AccessibilityThemeGui.java** — Provides the prototype settings screen. Replace placeholder records with concrete accessibility controls and preview each setting; wire the controls to the client rendering/font/color systems only after the intended options and defaults are specified.
- **AccessibilityThemeSettings.java** — Holds local accessibility settings and records. Define the actual theme model (for example, text scale, contrast, color choices, and readable UI states) before adding fields; validate ranges and ensure settings can be applied consistently. The current file explicitly leaves the actual options undefined.

## Adjustable Third Person

Folder: `AdjustThirdPerson/`

- **AdjustableThirdPersonCameraDistance.java** — Stores rear/front camera distance settings and local configuration. Validate minimum/maximum values, load/save behavior, defaults, and malformed config recovery; expose settings to the actual camera calculation without changing vanilla behavior when disabled.
- **ThirdPersonOptionsGui.java** — Provides the standalone third-person options screen. Finish live slider/value feedback, reset/default behavior, bounds, and return navigation; make sure the layout works at supported resolutions.
- **ThirdPersonOptionsHooks.java** — Defines the hook methods for adding/opening the third-person options entry. Connect these methods to the actual Minecraft Options screen through the project's approved hook/transformer mechanism; prevent duplicate buttons and verify navigation. The source currently describes this hook as a prototype boundary, not an installed hook.

## Analytics — Combo Analysis

Folder: `Analystics/Combo Analysis/`

- **ComboAnalysisAfterFight.java** — Records hit timestamps and derives combo lengths from the configured hit-gap rule. Connect it to the correct combat hit events, define fight/session boundaries, handle duplicate/out-of-order timestamps, and test empty, single-hit, and long-gap cases.
- **ComboConfig.java** — Holds the maximum allowed gap between hits. Validate the configured range and define the exact boundary rule (equal to the threshold versus greater than it); expose approved settings if a GUI is added.
- **ComboResult.java** — Carries one completed combo analysis result. Verify that the result is immutable and contains the timestamps/statistics needed by the display or history; test duration and hit-count edge cases.
- **ComboSession.java** — Manages a live combo session (record hit, finish, cancel, finished state). Connect it to fight lifecycle events, prevent recording after completion, and ensure finish/cancel cannot create duplicate results.
- **ComboStats.java** — Calculates summary values such as hit count, combo count, longest combo, average interval, and largest gap. Verify denominator/empty-data handling and test the calculations against known timestamp sequences.

## Analytics — Target Switch

Folder: `Analystics/Target/`

- **TargetSwitchAnalyzer.java** — Stores target-switch events and exposes event/stat snapshots. Connect it to the actual target-selection source; define what counts as a target change, ignore null/self/duplicate transitions where appropriate, and enforce the configured event limit.
- **TargetSwitchConfig.java** — Holds the maximum retained event count. Validate the limit and define behavior when it is reduced below the current event count.
- **TargetSwitchResult.java** — Represents a completed analysis interval with start/end times, events, and stats. Verify immutable snapshots and safe handling of zero-duration or empty sessions.
- **TargetSwitchSession.java** — Records switches during one session and supports finish/cancel. Connect session start/end to the relevant combat lifecycle and prevent post-finish recording.
- **TargetSwitchStats.java** — Calculates switch count, unique targets, and interval statistics. Define how repeated targets and zero intervals are treated; test empty, one-event, and repeated-target cases.

## Badges — Anniversary

Folder: `Badges/Anniversary/`

- **AnniversaryBadgeCalculator.java** — Calculates earned anniversary milestones from the account creation date. Verify calendar-month/year handling, leap-day behavior, future/null dates, and consistency with the milestone definitions.
- **AnniversaryBadgeGui.java** — Displays anniversary milestone status and opens the Birthday Celebration screen. Complete badge presentation, responsive layout, settings retention, and navigation; connect account creation date only to an approved source. Do not replace the existing Profiles UI with this prototype.
- **AnniversaryBadgeSettings.java** — Holds the local enable state and manually supplied account creation date. Add robust date validation and approved local persistence if required; keep the account-data source disconnected until explicitly specified.
- **AnniversaryBadgeStatus.java** — Represents one tier's earned/locked state and earned date. Verify consistency between tier, earned flag, and date; ensure consumers receive safe immutable status data.
- **AnniversaryBadgeTier.java** — Defines supported account-age milestones and display metadata. Verify the exact milestone list and chronological ordering against the approved badge design; keep threshold semantics consistent with the calculator.

### Badges — Anniversary Birthday Celebration

Folder: `Badges/Anniversary/Birthday/`

- **BirthdayCelebrationGui.java** — Lets the user set/clear a birthday and toggle the celebration. Complete persistence if approved, validation and leap-day messaging, and navigation; clearly communicate that the current setting is session-only.
- **BirthdayCelebrationService.java** — Detects the matching calendar day and requests one confetti burst. Connect it to the client tick only when a player/world is present; ensure exactly one burst per calendar day/session, reset behavior on world changes, and no emission when disabled or no birthday is configured.
- **BirthdayCelebrationSettings.java** — Holds the local birthday and enabled state. Validate dates and define February 29 behavior for non-leap years; do not silently invent an alternate date.
- **BirthdayConfettiParticle.java** — Carries position, motion, color, and lifetime for one particle. Validate finite coordinates, sensible motion/lifetime bounds, and immutable values.
- **BirthdayConfettiParticleSink.java** — Defines the adapter boundary for spawning particles. Implement a Minecraft 1.8.9 particle adapter that maps every field to the actual particle renderer; this interface alone does not render anything.

## Badges — Playtime

Folder: `Badges/Playtime/`

- **PlaytimeBadgeCalculator.java** — Converts total playtime seconds into earned/locked badge statuses. Verify threshold boundaries and avoid rounding the source total before calculating progress.
- **PlaytimeBadgeGui.java** — Displays total playtime and milestone progress. Finish navigation, responsive rows/scrolling, and badge presentation; open it from an approved client UI entry.
- **PlaytimeBadgeService.java** — Reads the existing cumulative Playtime Tracker and calculates current badges. Verify the tracker API/package and ensure the badge module does not start a second timer or alter the tracker's persistence/tracking behavior.
- **PlaytimeBadgeStatus.java** — Represents current/required seconds, earned state, remaining time, and progress. Verify clamping (0–100%), zero/invalid thresholds, and consistency of derived values.
- **PlaytimeBadgeTier.java** — Defines the seven requested thresholds: 1 hour, 1 day, 1 month, 3 months, 6 months, 1 year, and 2 years. Confirm that month/year conversions match the existing Playtime Tracker's convention and keep the display order correct.

## Block Break Ring

Folder: `Block Break Ring/`

- **BlockBreakProgressRing.java** — Owns module state, configuration, progress, and settings access. Connect start/stop to real block-breaking state and respect the enabled setting; avoid a second competing block-progress source.
- **BlockBreakRingAnimation.java** — Smooths progress transitions. Verify interpolation, reset behavior, frame-rate independence, and progress clamping.
- **BlockBreakRingConfig.java** — Holds enable, background, smoothing, line width, radius, opacity, and color settings. Validate every range and preserve safe defaults for missing/invalid values.
- **BlockBreakRingEventHandler.java** — Receives client tick and world-render events. Register/unregister through the correct Forge lifecycle, derive progress from the current block-breaking state, and clear stale state on world/player changes.
- **BlockBreakRingGui.java** — Exposes ring settings. Complete interactive controls, preview, reset, and persistence; verify that settings update the renderer immediately.
- **BlockBreakRingRenderer.java** — Draws the ring in the world/HUD render context. Implement correct coordinate transforms, depth/blend state restoration, scaling, and cleanup so rendering does not affect other modules.
- **BlockBreakRingSnapshot.java** — Exposes an immutable view of ring state. Verify snapshot consistency and ensure the renderer does not mutate module state through it.

## Block Placement Preview

Folder: `BlockPlacementPreview/`

- **BlockPlacementPreview.java** — Owns preview state and world-render behavior. Connect to the actual placement/target block calculation, validate collision/replaceability rules, and render only when the feature is enabled and the player/world are valid.
- **BlockPlacementPreviewCommand.java** — Defines the command entry. Register it with the client command system, implement argument/help/error behavior, and ensure it changes only this module's settings.
- **BlockPlacementPreviewGui.java** — Provides preview settings UI. Complete actual control interactions, preview feedback, and navigation; verify all settings are reflected by the renderer.
- **BlockPlacementPreviewSettings.java** — Holds enable, collision display, and opacity settings. Validate opacity and define defaults; ensure collision display does not bypass Minecraft placement rules.

## Clutch Highlights

Folder: `Clutch/`

- **ClutchHighlight.java** — Represents one recorded clutch highlight with type, description, and timestamp. Verify immutable data, valid event labels, and stable ordering.
- **ClutchHighlightType.java** — Defines the event labels available to a highlight. Align the enum with the approved clutch event list and provide clear display labels.
- **ClutchHighlights.java** — Manages enable/config state and retained highlight records. Connect it to actual game events (fall saves, block placements, pearl saves, or other approved clutch events); define deduplication, retention, and clear behavior.
- **ClutchHighlightsConfig.java** — Defines the maximum number of retained highlights. Validate the limit and ensure older entries are evicted predictably.

## Combo Breaker Cue

Folder: `Combo/`

- **ComboBreakerConfig.java** — Holds cue duration and enabled reason categories. Validate duration and ensure each reason has an explicit default.
- **ComboBreakerCue.java** — Detects/represents a broken combo cue and exposes its active display state. Connect it to the actual combat combo/session source; define the exact break condition, avoid duplicate cues, expire cues reliably, and connect the result to a HUD renderer.

## Cooldowns

Folder: `Cooldowns/`

- **Cooldowns.java** — Provides shared deadline-based cooldown tracking (start, remaining, ready/active, cancel, expiry cleanup). Verify clock handling, identifier uniqueness, cleanup, and behavior after world/server changes; do not let it overwrite unrelated cooldown keys.
- **enderpearl/PearlCooldown.java** — Holds pearl cooldown settings, option/record helpers, and remaining-time calculations. Connect it to actual pearl throw events and the correct HUD/menu display; define whether the timer is client-estimated or server-confirmed and avoid presenting estimates as authoritative.

## Indicators — Bow

Folder: `Indicators/Bow/`

- **BowChargeCalculator.java** — Converts draw ticks into a charge percentage. Verify the Minecraft 1.8.9 bow draw formula and boundary behavior from zero to full charge.
- **BowChargeConfig.java** — Holds full-charge tick and ready-percentage thresholds. Validate values and document the chosen threshold semantics.
- **BowChargeIndicator.java** — Maintains current charge state and recorded observations. Connect it to actual held-item/use-item state and tick updates; clear on release, item switch, death, and world change; expose data to a HUD renderer.
- **BowChargeSnapshot.java** — Carries immutable draw ticks, charge percentage, readiness, and timestamp. Verify clamping and snapshot timing.

## Indicators — Knockback

Folder: `Indicators/Knockback/`

- **KnockbackDirectionIndicator.java** — Tracks the horizontal direction/magnitude of received knockback. Connect to the correct velocity/knockback event or packet source; distinguish server corrections from actual knockback and clear stale direction after the configured visibility window.
- **KnockbackDirectionSnapshot.java** — Carries direction components, normalized direction, magnitude, and timestamp. Verify zero-vector handling and immutable values.
- **KnockbackIndicatorConfig.java** — Holds visibility duration and minimum magnitude. Validate both settings and define units clearly.

## Opponent Profiles

Folder: `OpponentProfiles/`

- **OpponentProfile.java** — Stores/aggregates opponent encounter information and win counts. Define stable opponent identity, encounter lifecycle, reset behavior, and data ownership; connect it only to actual duel/fight events after the data model is approved.

## Predictions — Armor

Folder: `Predictions/Armor/`

- **ArmorBreakPrediction.java** — Aggregates armor damage observations and produces per-opponent/per-slot predictions. Connect it to actual combat damage and armor durability changes; identify equipment swaps, reset on opponent/session changes, and do not claim a prediction when samples are insufficient.
- **ArmorBreakPredictionConfig.java** — Defines minimum samples and maximum samples per slot. Validate limits and ensure bounded history.
- **ArmorBreakResult.java** — Represents remaining durability, average damage, estimated hits to break, and prediction availability. Verify calculations, unknown durability handling, and rounding; do not display impossible negative durability.
- **ArmorDurabilityTracker.java** — Tracks durability observations for armor pieces. Complete the tracking lifecycle, slot/opponent keying, item replacement detection, and safe cleanup.
- **ArmorSlot.java** — Defines supported armor equipment slots. Verify mapping to Minecraft 1.8.9 inventory slots and keep ordering/display names consistent.

## Predictions — Ender Pearl

Folder: `Predictions/Enderpearl/`

- **EnderPearlLandingPrediction.java** — Coordinates settings, prediction requests, and recorded results. Connect it to actual pearl throw velocity/origin and world collision data; display a prediction only while its input is valid and label uncertainty honestly.
- **EnderPearlPredictionConfig.java** — Holds gravity, air drag, and maximum simulation ticks. Calibrate against Minecraft 1.8.9 projectile behavior and validate physics ranges.
- **EnderPearlPredictionResult.java** — Represents trajectory, predicted landing, simulation ticks, collision status, and confidence. Define confidence meaning and distinguish confirmed collision from timeout/no collision.
- **EnderPearlTrajectoryPredictor.java** — Simulates projectile motion and queries collisions through its collision probe. Implement the real world collision probe, account for block/entity collision and relevant projectile physics, and compare predicted paths with observed pearl trajectories.
- **EnderPearlVector.java** — Provides immutable three-dimensional vector operations. Verify arithmetic, distance, and zero-vector behavior; add tests for the operations used by the predictor.

## Staff Dashboard Expansion — Attendance

Folder: `Staff Dashboard Expansion/Attendance/`

- **StaffAttendanceGui.java** — Displays attendance and check-in/check-out controls. Complete date selection, staff filtering, empty/error states, and navigation; register only under the approved Staff Dashboard permission layer.
- **StaffAttendanceManager.java** — Coordinates check-in/check-out sessions and attendance queries. Enforce one valid open session per staff identity, handle duplicate actions and day boundaries, and validate durations.
- **StaffAttendanceOverview.java** — Exposes clock-in/out and aggregate attendance queries. Verify totals and date grouping; the production host must supply authenticated staff identity and approved data.
- **StaffAttendanceSession.java** — Represents one check-in/check-out interval. Validate timestamps and prevent negative or overlapping sessions; test open and completed session behavior.

## Staff Dashboard Expansion — Availability

Folder: `Staff Dashboard Expansion/Availability/`

- **StaffAvailabilityBoard.java** — Manages availability entries, status changes, filtering, counts, and snapshots. Define identity uniqueness, stale-entry handling, and update conflict behavior; connect shared state only through a separately approved mechanism.
- **StaffAvailabilityEntry.java** — Represents one staff member's availability and note. Validate identity/status/note length and update timestamps; prevent stale updates from overwriting newer data.
- **StaffAvailabilityGui.java** — Displays and edits staff availability. Complete status selection, note editing, refresh, filtering, and navigation; register under real staff permissions.
- **StaffAvailabilityStatus.java** — Defines availability states and next-state behavior. Confirm the approved state list and transitions; do not allow a UI cycle to skip required states.

## Staff Dashboard Expansion — Competency

Folder: `Staff Dashboard Expansion/Competency/`

- **StaffCompetency.java** — Defines one competency (name, category, description, creation time). Validate required fields and stable IDs; define how competency categories are displayed.
- **StaffCompetencyAssessment.java** — Represents one staff member's assessment for a competency. Validate identity, competency reference, level, notes, and assessment time.
- **StaffCompetencyGui.java** — Displays the competency matrix and assessment controls. Complete create/edit/remove/assess interactions, filtering, and navigation; protect all operations with the actual staff permission layer.
- **StaffCompetencyLevel.java** — Defines assessment levels and their order. Confirm the exact approved scale and transitions; keep display labels and numeric values consistent.
- **StaffCompetencyManager.java** — Creates/removes competencies and assessments and builds the matrix. Enforce valid references and one defined assessment policy per staff/competency; add safe update/removal behavior and tests.
- **StaffCompetencyMatrix.java** — Represents the combined competency/assessment view. Verify lookup behavior for missing assessments and stable ordering; ensure it cannot mutate manager-owned data unexpectedly.

## Staff Dashboard Expansion — Coverage Forecast

Folder: `Staff Dashboard Expansion/Coverage/`

- **CoverageForecastManager.java** — Manages coverage requirements and calculated results. Define overlap rules and the source of scheduled staff; test boundary times and duplicate requirements.
- **CoverageRequirement.java** — Represents a time window and minimum staffing requirement. Validate start/end order, minimum staff count, and timezone assumptions.
- **CoverageResult.java** — Represents scheduled staff, required staff, shortage, and coverage state. Verify shortage never becomes negative and that boundary conditions are handled.
- **StaffCoverageForecast.java** — Coordinates requirements and coverage forecasts. Connect to the approved shift schedule source; define which staff count as available/qualified and ensure no local prototype data is treated as authoritative.
- **StaffCoverageForecastGui.java** — Displays coverage requirements and forecast results. Complete date/time controls, shortage highlighting, refresh, and navigation; register only after explicit Staff Dashboard integration approval.

## Staff Dashboard Expansion — Goals

Folder: `Staff Dashboard Expansion/Goals/`

- **StaffGoal.java** — Represents a team goal, description, deadline, status, progress, and timestamps. Validate progress range, required fields, and deadline behavior; ensure overdue status is derived consistently.
- **StaffGoalBoard.java** — Manages goal creation, updates, progress, removal, snapshots, and counts. Enforce stable IDs, valid status transitions, and safe team filtering; define ownership/edit permissions at integration time.
- **StaffGoalBoardGui.java** — Displays and edits the goal board. Complete create/edit/delete, status/progress controls, team filtering, and deadline presentation; wire to approved dashboard navigation.
- **StaffGoalStatus.java** — Defines goal lifecycle states and transitions. Confirm the approved lifecycle and prevent invalid transitions.

## Staff Dashboard Expansion — Improvement Plan

Folder: `Staff Dashboard Expansion/Improvement Plan/`

- **StaffImprovementAction.java** — Represents an action item and its completion state. Validate description and completion updates; preserve action order and associate it with the correct plan.
- **StaffImprovementEvaluation.java** — Represents a dated reviewer evaluation and progress percentage. Validate reviewer/notes/date and progress range; define how multiple evaluations affect displayed progress.
- **StaffImprovementManager.java** — Creates/removes plans, manages actions/evaluations, and counts plans. Add lookup/update safeguards and lifecycle tests; data is currently memory-only.
- **StaffImprovementPlan.java** — Holds staff member, objective, deadline, status, progress, actions, and evaluations. Validate fields and keep derived action counts/progress consistent after changes.
- **StaffImprovementPlanGui.java** — Displays and edits improvement plans. Complete plan/action/evaluation CRUD, progress/status controls, and navigation; apply real staff permissions when integrated.
- **StaffImprovementStatus.java** — Defines improvement-plan lifecycle states. Confirm approved transitions and ensure the GUI and manager use the same rules.

## Staff Dashboard Expansion — Overloaded Staff Alert

Folder: `Staff Dashboard Expansion/OverloadedStaffAlert/`

- **OverloadedStaffAlert.java** — Coordinates staff workload values and generated alerts. Connect workload inputs to the approved task source; define deduplication, alert refresh, dismissal, and stale staff handling.
- **StaffWorkload.java** — Represents one staff member's open task count and identity. Validate counts and identity updates; ensure completed tasks cannot reduce the count below zero.
- **WorkloadAlert.java** — Represents an alert with staff, count, threshold, level, and message. Verify severity/message consistency and immutable alert snapshots.
- **WorkloadConfig.java** — Holds moderate and overloaded thresholds. Validate ordering and minimum values; define behavior when thresholds are equal or changed at runtime.
- **WorkloadLevel.java** — Classifies workload into levels. Verify exact threshold inclusivity and test below/at/above each boundary.
- **WorkloadManager.java** — Registers staff and updates workload/task counts. Connect to the approved task source, prevent duplicate task counting, and reconcile counts when assignments change.

## Staff Dashboard Expansion — Presence

Folder: `Staff Dashboard Expansion/Presence/`

- **StaffPresenceEntry.java** — Represents one observed staff member's status and observation time. Define stale timeout behavior and validate timestamps/status.
- **StaffPresenceGui.java** — Displays filtered/sorted staff presence. Connect it to the approved client-visible presence source; complete refresh/empty states and Staff Dashboard navigation.
- **StaffPresenceList.java** — Stores and updates observed presence entries. Define identity keys, update ordering, removal, and stale cleanup; do not introduce remote storage in this prototype.
- **StaffPresenceManager.java** — Applies status filters, sort modes, stale rules, and visible-entry selection. Verify stable sorting and boundary behavior; keep data acquisition separate from presentation.

## Staff Dashboard Expansion — Rank Comparison

Folder: `Staff Dashboard Expansion/Rank Comp/`

- **StaffRankComparison.java** — Compares two permission-name sets and derives shared/unique permissions. Replace manually entered labels with the canonical approved permission source only after integration is authorized; do not infer or change actual rank permissions.
- **StaffRankComparisonGui.java** — Displays a local comparison result. Complete profile selection, comparison refresh, scrolling, and empty states; clearly label manual data and never use this screen to grant/revoke permissions.
- **StaffRankComparisonModule.java** — Provides the in-memory comparison facade. Connect it to the approved rank/permission data source and Staff Dashboard navigation only after approval; add validation for duplicate/blank permission names.
- **StaffRankComparisonResult.java** — Carries shared, first-only, and second-only permission sets. Verify immutable collections and deterministic display order.
- **StaffRankProfile.java** — Represents a manually supplied role name and permission set. Validate names and permission values; keep it explicitly separate from the real Alpha rank model until approved integration.

## Staff Dashboard Expansion — Responsibility Board

Folder: `Staff Dashboard Expansion/Responsibility Board/`

- **StaffResponsibility.java** — Represents a responsibility with title, description, assignee, priority, and status. Validate required fields and updates; define whether reassignment/status changes are audited.
- **StaffResponsibilityBoard.java** — Exposes enabled state and the responsibility manager. Connect it to the Staff Dashboard only through approved navigation and permissions.
- **StaffResponsibilityBoardGui.java** — Displays and edits responsibilities. Complete create/edit/assign/priority/status/filter interactions and navigation; handle empty and overflow states.
- **StaffResponsibilityManager.java** — Creates, retrieves, removes, filters, and counts responsibilities. Enforce stable IDs and valid filters; define safe update and duplicate behavior.
- **StaffResponsibilityPriority.java** — Defines priority levels and display names. Confirm the approved order and ensure sorting uses the same order as the GUI.
- **StaffResponsibilityStatus.java** — Defines assignment/status states and next-state behavior. Confirm valid transitions and prevent accidental cycling into invalid states.

## Staff Dashboard Expansion — Review Calendar

Folder: `Staff Dashboard Expansion/Review Calendar/`

- **StaffReview.java** — Represents a dated review, staff member, notes, status, and reminder state. Validate date/time and reminder values; ensure overdue/reminder calculations use one consistent clock.
- **StaffReviewBoardGui.java** — Displays reviews grouped by status. Complete board filtering, item selection, status updates, and navigation; reuse the existing calendar manager rather than creating duplicate review data.
- **StaffReviewCalendar.java** — Provides the year-calendar facade and manager access. Complete screen registration and approved permission checks; keep shared synchronization out of this prototype.
- **StaffReviewCalendarGui.java** — Displays the year/month calendar. Finish date navigation, review markers, selection, and opening the day/editor/board views; verify all views share the same manager.
- **StaffReviewCalendarManager.java** — Owns review CRUD, date queries, status changes, and reminders. Validate updates, date boundaries, reminder acknowledgement, and stable ordering; test all queries against sample dates.
- **StaffReviewDayGui.java** — Displays reviews for a selected day. Complete selection, create/edit/delete/status actions, and return navigation using the shared manager.
- **StaffReviewEditorGui.java** — Creates/edits a review. Validate title, staff name, date/time, notes, status, and reminder interval; return changes to the same calendar manager.
- **StaffReviewReminder.java** — Represents a calculated reminder for a review. Verify reminder date and overdue behavior, including acknowledged and cancelled/completed reviews.
- **StaffReviewStatus.java** — Defines review lifecycle states. Confirm valid transitions and ensure calendar, editor, and board use identical labels and rules.

## Staff Dashboard Expansion — Shifts and Checklist

Folder: `Staff Dashboard Expansion/Shifts/`

- **ChecklistCategory.java** — Defines checklist categories and labels. Confirm the category set and stable display order.
- **ChecklistItem.java** — Represents one checklist task and completion state. Validate title/category and ensure updates preserve identity.
- **ShiftStartChecklist.java** — Manages checklist items, completion, reset, and progress. Define default checklist content and reset timing; connect the checklist to a real staff shift only after the shift workflow is approved.
- **ShiftStartChecklistGui.java** — Displays and updates the shift-start checklist. Complete category filtering, checkbox interaction, reset confirmation, and navigation.
- **StaffShift.java** — Represents a staff shift with start/end, staff, title, and status. Validate time ranges and overlap behavior; handle shifts crossing midnight.
- **StaffShiftBoard.java** — Coordinates shift records, status updates, queries, and conflicts. Define conflict rules and identity matching; connect to the approved staff schedule source only after approval.
- **StaffShiftBoardGui.java** — Displays the shift schedule. Complete create/edit/delete/status, date navigation, conflict indicators, and Staff Dashboard registration/permissions.
- **StaffShiftManager.java** — Provides shift scheduling and status-cycle operations. Validate scheduling constraints and prevent invalid transitions or duplicate assignments.
- **StaffShiftStatus.java** — Defines shift lifecycle states. Confirm approved state transitions and ensure the board/manager agree.

## Staff Dashboard Expansion — Staff Duty Mode

Folder: `Staff Dashboard Expansion/Staff Duty Mode/`

- **StaffDutyManager.java** — Manages local duty mode, on/off duty, busy state, and notes. Connect to a server-authoritative workflow only in a separately approved integration; local state must not determine actual staff permissions or assignment authority.
- **StaffDutyMode.java** — Represents one staff member's duty state and duration. Validate identity/state/time and define behavior when state changes repeatedly.
- **StaffDutyModeGui.java** — Displays and changes local duty state. Complete controls and status feedback; clearly identify the prototype as local and unsynchronized.
- **StaffDutyState.java** — Defines duty states and assignment availability metadata. Confirm the approved state model; do not use this enum to bypass real authorization.

## Staff Dashboard Expansion — Staff Handoff

Folder: `Staff Dashboard Expansion/Staff Handoff/`

- **StaffHandoff.java** — Represents a handoff from one staff member to another, including summary, timestamp, and status. Validate sender/recipient/summary and define immutable identity and update rules.
- **StaffHandoffGui.java** — Displays and responds to handoffs. Complete recipient filtering, accept/reject/withdraw controls, detail view, and navigation; ensure users can act only on handoffs they are authorized to access.
- **StaffHandoffManager.java** — Creates, accepts, rejects, withdraws, retrieves, and filters handoffs. Enforce valid state transitions and recipient ownership; add duplicate/conflict handling. Shared handoffs/notifications are not implemented in this local prototype.
- **StaffHandoffStatus.java** — Defines handoff states. Verify the full lifecycle and prevent invalid transitions.

## Staff Dashboard Expansion — Staff Queue

Folder: `Staff Dashboard Expansion/Staff Queue/`

- **StaffQueue.java** — Manages task creation, assignment, claiming, status, completion, cancellation, filtering, and capacity. Define ownership and transition rules; connect task input to an approved source only after permission checks are designed.
- **StaffQueueEntry.java** — Represents one queue task and its metadata. Validate title/description/creator/assignment/timestamps and ensure updates preserve identity.
- **StaffQueueGui.java** — Displays and manages queue tasks. Complete create/claim/assign/status/complete/cancel interactions, filters, and navigation; enforce actual staff permissions.
- **StaffQueuePriority.java** — Defines priority values and ordering weight. Confirm ordering and use it consistently for sorting and display.
- **StaffQueueStatus.java** — Defines task lifecycle states. Confirm valid transitions and prevent completion/cancellation from being reversed without an explicit rule.

## Statistics — Duels

Folder: `Statistics/Duels/`

- **DuelRecord.java** — Represents one duel outcome and timestamp. Validate opponent/result/time and define duplicate-result handling.
- **DuelStatisticsConfig.java** — Holds history enablement and maximum history size. Validate limits and define eviction behavior.
- **DuelStatisticsSnapshot.java** — Exposes opponent totals, win rate, streaks, and history. Verify immutable snapshots, division-by-zero handling, and deterministic history ordering.
- **OpponentSpecificDuelStatistics.java** — Aggregates duel outcomes per opponent, including wins/losses/streaks/history. Connect to the actual duel completion source; define opponent identity, duplicate match prevention, reset behavior, and bounded history.

## Timing — Block Placement

Folder: `Timing/Block placement timing/`

- **BlockPlacementSample.java** — Represents one interval and its recording timestamp. Validate non-negative interval and monotonic timestamps.
- **BlockPlacementTiming.java** — Records consecutive placement intervals and exposes samples/averages/stats. Connect to the actual successful block-placement event (not attempted clicks); reset on world/player changes and avoid counting cancelled/failed placements.
- **BlockPlacementTimingConfig.java** — Holds slow-placement threshold and sample limit. Validate ranges and define retention behavior.
- **BlockPlacementTimingStats.java** — Calculates sample count, slow count, average, fastest/slowest, and standard deviation. Verify empty/single-sample behavior and statistical correctness.

## Timing — Combat Timer

Folder: `Timing/Combat Timer/`

- **CombatTimer.java** — Contains timer state, event/chat handling, settings, and settings screen. Verify chat-pattern matching against actual supported server messages, avoid false positives, register event listeners correctly, and connect HUD rendering only when enabled. Keep settings local; do not add remote storage.

## Timing — Block Break

Folder: `Timing/block break timing/`

- **BlockBreakSample.java** — Represents one block-break interval and timestamp. Validate non-negative intervals and monotonic timestamps.
- **BlockBreakTiming.java** — Records intervals between successful block breaks and calculates statistics. Connect to the correct successful break event; distinguish different blocks/tools/contexts if required by the final feature design.
- **BlockBreakTimingConfig.java** — Holds slow threshold and maximum sample count. Validate ranges and bounded history.
- **BlockBreakTimingStats.java** — Calculates sample/slow counts, average, fastest/slowest, and standard deviation. Verify empty/single-sample and threshold-boundary behavior.

## Trackers — Arrow Tracker

Folder: `Trackers/Arrow Tracker/`

- **ArrowTrack.java** — Stores one arrow's identity, origin, current position, and timestamps. Connect updates to the actual projectile entity; handle entity removal, world changes, and invalid positions.
- **ArrowTracker.java** — Manages tracked arrows, settings, and history. Add Forge entity-spawn/tick/removal hooks and connect the approved display; enforce the maximum tracked-arrow count and clear stale entities.
- **ArrowTrackerConfig.java** — Defines the maximum number of tracked arrows. Validate the limit and define which arrow is evicted when capacity is reached.
- **ArrowTrackerSnapshot.java** — Exposes immutable arrow position, flight time, and distance data. Verify snapshot accuracy and avoid exposing mutable tracking objects to render code.

## Trainers — Aim Trainer

Folder: `Trainers/Aim Trainer/`

- **AimDriftHeatmap.java** — Aggregates aim-offset samples into heatmap cells. Define coordinate normalization and screen/world reference frame; cap samples and test edge-cell behavior.
- **AimTrainerConfig.java** — Holds heatmap grid settings. Validate grid dimensions and define reset/retention defaults.
- **AimTrainerResult.java** — Represents hits, misses, accuracy, elapsed time, and heatmap data. Verify accuracy and snapshot consistency for zero attempts.
- **AimTrainerSession.java** — Records aim offsets, hits, misses, and session completion. Connect to the actual trainer target/input events; define session start/end and prevent post-finish samples.
- **AimTrainerStats.java** — Aggregates hit/miss/attempt counts and accuracy. Verify zero-attempt handling and consistency with the session result.

## Trainers — Bridge

Folder: `Trainers/Bridge/`

- **BridgePersonalBest.java** — Stores and clears the best completed run. Define the comparison rule (time, accuracy, or combined metric) and test ties.
- **BridgeRun.java** — Tracks block placements, misplacements, and completion state for one run. Connect successful/incorrect placement events from Forge; define start/finish conditions and ignore unrelated placements.
- **BridgeRunResult.java** — Represents completed-run timing, placement counts, speed, and accuracy. Verify formulas and zero-placement behavior.
- **BridgeRunStats.java** — Aggregates placement and accuracy statistics. Verify count consistency and safe calculations.
- **BridgeSpeedTrainer.java** — Coordinates runs, history, personal best, and configuration. Connect to a trainer start/finish UI and real placement events; bound history and handle cancellation/world changes.
- **BridgeTrainerConfig.java** — Holds maximum history size. Validate the limit and define eviction behavior.

## Trainers — Ender Pearl Accuracy Practice

Folder: `Trainers/enderpearl/`

- **EnderPearlAccuracyPractice.java** — Manages practice target, pending throws, landing results, accuracy, streaks, error, and history. Connect to actual pearl throw and landing/entity events; define target placement, timeout/cancel behavior, and what counts as an accurate throw. Complete a dedicated practice GUI/visual target flow if required by the final design.

## Package and integration checks

The inventory shows package declarations with different capitalization/naming conventions (for example, `Badges` folders declare lowercase packages, while some other folders declare uppercase path segments). Before any future integration:

1. Verify every package declaration against the project's Java source/package conventions. Do not rename packages or paths without an explicit scoped change.
2. Verify imports and cross-module API calls against the current branch source; do not assume a prototype compiles because its files exist.
3. Check Java 8 compatibility and Minecraft 1.8.9 / Forge 11.15.1.2318 APIs. Avoid newer Minecraft/Forge classes.
4. Register event handlers only once and unregister/clear state on world unload, disconnect, player change, and module disable where relevant.
5. Keep GUI entry points, event handlers, renderers, models, and storage responsibilities separated.
6. Validate user-controlled values, cap collections, handle missing/corrupt local config safely, and avoid blocking work on the client thread.
7. Add focused tests or deterministic test harnesses for calculations, thresholds, state transitions, date/time boundaries, and persistence.
8. Verify visual behavior at supported GUI scales/resolutions and restore OpenGL/render state after custom rendering.
9. Apply real authorization at the production host for Staff Dashboard features. Rank comparison data is informational only; it must not change Alpha rank or permission rules.
10. Keep prototypes isolated from AlphaClient Core and `development` until a separate implementation request explicitly authorizes a specific scope.

## Current verification boundary

This README was assembled from the `mod-ideas` branch tree and source-file declarations at the time of creation. It is an implementation checklist, not a compile report. The listed prototypes have **not** been collectively compiled or tested in-game as part of creating this document. A file being present does not prove that its imports, package path, API usage, UI, event registration, persistence, or runtime behavior are correct.


## Wrapped — Annual Alpha Wrapped Summary

Folder: `Wrapped/`

- **AnnualAlphaWrappedSession.java** — Immutable local session sample containing start time, end time, and server identifier. Validate timestamps and define how open sessions are finalized before production use.
- **AnnualAlphaWrappedPeriod.java** — Identifies the calendar-year reporting period. Extend only when additional reporting periods are explicitly required.
- **AnnualAlphaWrappedSummary.java** — Immutable annual result containing year, total playtime, session count, visited-server count, and availability. Keep result values non-negative and define whether an empty year should be hidden or shown as an empty recap.
- **AnnualAlphaWrappedSettings.java** — Holds local enablement/opt-in and selected-year state. Add local persistence and a settings UI only after defaults and opt-in behavior are confirmed; this prototype does not sync settings remotely.
- **AnnualAlphaWrappedCalculator.java** — Calculates session overlap with the selected calendar year and counts distinct non-empty server identifiers. Verify timezone boundaries, daylight-saving transitions, open sessions, duplicate/unknown server identifiers, and large-duration overflow.
- **AnnualAlphaWrappedService.java** — Coordinates calculation from caller-supplied session samples and respects the local enabled setting. Connect a real, approved yearly activity source only in a separately authorized implementation; do not infer annual history from a lifetime total.
- **AnnualAlphaWrappedCard.java** — Draws reusable statistic cards for the Wrapped GUI. Verify rendering at supported GUI scales and restore/avoid changing shared render state.
- **AnnualAlphaWrappedGui.java** — Displays the annual recap preview with playtime, sessions, servers visited, year, and a back action. Add year navigation, loading/error states, and a real menu entry only after the data source and UX are specified.

**Prototype boundary:** This folder accepts supplied local session samples only. It does not retrieve account history, persist annual session records, call Supabase, or integrate with AlphaClient Core. The settings object is currently in-memory. No compile or in-game test is claimed for this prototype.


## Achievements — Achievement Checklist with Explanations

Folder: `achievements/`

- **AchievementEntry.java** — Holds one achievement's ID, title, description, explanation, category, target, progress, and unlock timestamp. Validate timestamp semantics and define how progress is populated by approved gameplay events.
- **AchievementChecklist.java** — Provides an in-memory registry, progress updates, unlock operations, and completed-entry queries. Add persistence only after the local data format and lifecycle are specified.
- **AchievementChecklistSearch.java** — Performs case-insensitive matching against title, description, explanation, and category. Add tests for empty queries, null input, and Unicode/case behavior.
- **AchievementChecklistSettings.java** — Holds session-local enablement and search query. Persistence and a settings editor are not implemented.
- **AchievementChecklistGui.java** — Provides a Minecraft 1.8.9 preview with selectable entries, progress states, explanation text, and navigation. Connect it to a real menu only after the catalogue and entry source are defined; finish layout/overflow handling for long text and larger lists.

**Prototype boundary:** No predefined achievement list, gameplay event hooks, persistent storage, Supabase integration, account synchronization, or AlphaClient Core integration is included. The GUI requires caller-supplied entries. No compile or in-game test is claimed.


### Achievements — Suffixes

Folder: `achievements/suffix/`

- **AchievementSuffixEntry.java** — Defines a suffix ID, the achievement ID required to unlock it, display text, and description. Confirm the final suffix catalogue and display-format rules before adding production definitions.
- **AchievementSuffixRegistry.java** — Stores caller-supplied suffix definitions in insertion order and rejects duplicate IDs. Add validation/tests for catalogue consistency and define whether removal is permitted after a suffix has been selected.
- **AchievementSuffixSettings.java** — Holds the session-local enabled state and selected suffix ID. Local persistence and a user-facing settings entry are not implemented.
- **AchievementSuffixService.java** — Resolves whether a suffix is unlocked by checking its required entry in the existing local `AchievementChecklist`; allows selection only when that achievement is unlocked. It does not render suffixes in chat, tab list, nametags, or profiles.
- **AchievementSuffixGui.java** — Provides a Minecraft 1.8.9 preview for browsing suffix definitions, showing locked/unlocked state, selecting an unlocked suffix, disabling it, and returning to the parent screen. Verify scrolling/layout with large catalogues and supported GUI scales.

**Prototype boundary:** The suffix catalogue must be supplied by the caller; no default suffixes are invented. Unlock checks use the in-memory achievement checklist only. There is no chat/name rendering hook, persistent storage, Supabase integration, account synchronization, permission/rank change, or AlphaClient Core integration. No compile or in-game test is claimed.

## Account — Account Linking

Folder: `account/`

- **AccountLink.java** — Immutable local account reference containing a generated local ID, provider label, account identifier, display name, creation timestamp, and local state. It stores no passwords, tokens, or other credentials; ownership and identity are not verified.
- **AccountLinkStatus.java** — Distinguishes a locally registered entry from a locally disconnected entry. These states are UI/prototype states, not proof of an authenticated external connection.
- **AccountLinkValidator.java** — Validates required provider, account identifier, and display-name lengths. Add provider-specific format rules only after the supported providers and identity requirements are defined.
- **AccountLinkRegistry.java** — Maintains account references in insertion order, rejects duplicate provider/identifier pairs, updates local state, removes entries, and exposes immutable list snapshots. It is in-memory only.
- **AccountLinkingSettings.java** — Holds the prototype enable flag and selected local entry ID. Settings are session-local and are not persisted.
- **AccountLinking.java** — Main facade exposing the registry and settings and clearing local prototype data.
- **AccountLinkingService.java** — Coordinates local entry creation, selection, disconnection, and removal. Creation records user-supplied labels only; it does not perform an OAuth flow, verify account ownership, or establish a real account link.
- **AccountLinkingGui.java** — Provides a Minecraft 1.8.9 screen preview for adding local references, selecting entries, disconnecting/removing them, and toggling the prototype. The UI explicitly labels entries as local and unverified; complete scrolling, layout overflow handling, and parent-screen integration before production use.

**Prototype boundary:** This is a local UI/data-model prototype only. It does not authenticate accounts, connect Microsoft/Minecraft identities, store credentials, persist entries, synchronize account data, call Supabase, or integrate with AlphaClient Core. A production implementation requires an explicitly approved identity provider and secure authentication/verification design. No compile or in-game test is claimed.



## Achievement Wall — Profile Achievement Display

Folder: `achievements/`

- **AchievementWall.java** — Coordinates the profile-wall prototype and preserves the original idea class's unlock-record API. It can also read unlocked entries from the existing local `AchievementChecklist`; it does not change the checklist's definitions or progress rules.
- **AchievementWallEntry.java** — Creates an immutable display snapshot for one achievement, including title, category, progress, unlock state, and unlock timestamp.
- **AchievementWallSort.java** — Defines recent-first, oldest-first, title, and category ordering.
- **AchievementWallSettings.java** — Holds session-local enablement, locked-entry visibility, sorting, and search-query settings.
- **AchievementWallSnapshot.java** — Exposes an immutable filtered wall result and total/unlocked/locked counts with a completion percentage.
- **AchievementWallService.java** — Builds the wall from the existing in-memory `AchievementChecklist`, applies text filtering and locked-entry visibility, and sorts the result deterministically.
- **AchievementWallGui.java** — Provides a Minecraft 1.8.9 profile-wall preview with paged entries, completion summary, sorting, and a locked-entry visibility toggle. It is not registered in an existing menu.

**Prototype boundary:** The wall reuses caller-supplied entries from the existing local `AchievementChecklist`. It does not invent an achievement catalogue, add gameplay event hooks, persist data, synchronize accounts, call Supabase, alter rank/permission behavior, or integrate with AlphaClient Core. Settings remain in-memory. The GUI requires an approved entry point and further layout verification. No compile or in-game test is claimed.


## Trackers — Acknowledgement

Folder: `Trackers/Acknowledgement/`

- **AcknowledgementTracker.java** — Main facade for the local acknowledgement workflow. Retains the original `isEnabled`, `setEnabled`, `addRecord`, `getRecords`, and `clearRecords` methods while exposing structured acknowledgement operations. The legacy string-record view is local and in-memory.
- **AcknowledgementEntry.java** — Stores one acknowledgement with a stable ID, creator, subject, details, creation timestamp, status, and acknowledgement attribution/timestamp. Verify timestamp and identity rules against the final workflow requirements.
- **AcknowledgementStatus.java** — Defines the two prototype states: `PENDING` and `ACKNOWLEDGED`. Confirm whether additional states are required before production use.
- **AcknowledgementValidator.java** — Validates creator, subject, and details lengths. Confirm the final field limits and whether additional content rules are needed.
- **AcknowledgementRegistry.java** — Maintains bounded in-memory records, acknowledges pending entries, searches record text/attribution, and exposes list snapshots. Define retention/eviction and deletion policies before production use.
- **AcknowledgementSettings.java** — Holds session-local enablement, acknowledged-entry visibility, and maximum record count. Settings are not persisted.
- **AcknowledgementService.java** — Coordinates record creation, acknowledgement, filtering, and clearing. It uses the local system clock and caller-supplied staff identity; it does not verify identity or enforce staff permissions.
- **AcknowledgementGui.java** — Provides a Minecraft 1.8.9 preview for selecting records and acknowledging them. It is not connected to a menu and still needs layout/scrolling verification for large lists and supported GUI scales.

**Prototype boundary:** Records exist only in memory. There is no Supabase integration, remote synchronization, persistence, identity verification, staff permission enforcement, Forge event integration, or AlphaClient Core integration. The original plain-string methods remain available as a compatibility layer. No compile or in-game test is claimed.


## Acting Rank Assignment — Temporary Staff Rank Tracking

Folder: `ActingRankAssignment/`

- **ActingRankAssignment.java** — Main facade for the local prototype. Preserves the original `isEnabled`, `setEnabled`, `addRecord`, `getRecords`, and `clearRecords` methods while exposing the structured registry, settings, and service.
- **ActingRankEntry.java** — Immutable assignment record containing the staff member, current rank, acting rank, assigning staff member, reason, assignment/expiry timestamps, lifecycle status, and optional revocation attribution.
- **ActingRankStatus.java** — Defines the local lifecycle states `ACTIVE`, `EXPIRED`, and `REVOKED`.
- **ActingRankValidator.java** — Validates required names/ranks, field lengths, distinct current and acting ranks, and timestamp order. Rank names remain caller-supplied; no rank hierarchy is hardcoded.
- **ActingRankSettings.java** — Holds session-local enablement, expired-entry visibility, and the maximum number of retained records.
- **ActingRankRegistry.java** — Stores records in insertion order, supports lookup and staff-name filtering, exposes immutable snapshots, counts active assignments, and applies a bounded in-memory record limit.
- **ActingRankService.java** — Creates time-limited assignments, revokes active assignments, marks expired records, and searches assignment text. It uses caller-supplied staff identities and the local system clock.
- **ActingRankAssignmentGui.java** — Provides a Minecraft 1.8.9 preview for entering staff/current/acting rank details, duration, reason, reviewing records, and revoking a selected active assignment. It is not registered in an existing menu.

**Prototype boundary:** Acting ranks are informational workflow records only. This prototype does not change a staff member's actual rank, grant or remove permissions, modify rank hierarchy, authenticate the assigning staff member, persist records, call Supabase, synchronize accounts, integrate with Forge events, or connect to AlphaClient Core. Rank names are free-form input and no production policy for who may assign which acting rank has been defined. Settings and records are in-memory only. GUI layout and interaction still require in-game verification. No compile or in-game test is claimed.


## Active Device List and Global Sign-Out

Folder: `ActiveDeviceListGlobalSignOut/`

- **ActiveDeviceListGlobalSignOut.java** — Main facade exposing the local device registry, settings, and service.
- **ActiveDeviceSession.java** — Immutable device-session snapshot containing a local ID, device name, platform, account label, creation/last-seen timestamps, current-device flag, and local sign-out state.
- **ActiveDeviceStatus.java** — Defines the local states `ACTIVE`, `SIGN_OUT_REQUESTED`, and `SIGNED_OUT`. These are prototype states, not remote authentication claims.
- **ActiveDeviceValidator.java** — Validates required device/platform labels, field lengths, and timestamp order. It does not verify device identity.
- **ActiveDeviceSettings.java** — Holds session-local enablement, signed-out visibility, and the maximum retained device count.
- **ActiveDeviceRegistry.java** — Maintains a bounded in-memory registry and exposes immutable list snapshots and lookup/replacement operations.
- **ActiveDeviceSignOutResult.java** — Reports how many local sign-out requests were recorded or skipped, with an explicit non-confirmation message.
- **ActiveDeviceService.java** — Registers caller-supplied device snapshots, requests individual sign-outs, records a global sign-out request for all other active devices, and accepts a separate confirmation that a remote sign-out was completed.
- **ActiveDeviceListGlobalSignOutGui.java** — Provides a Minecraft 1.8.9 preview for reviewing device records, requesting an individual sign-out, and requesting sign-out for all other devices. It is not registered in an existing menu.

**Prototype boundary:** Device records are supplied locally by the caller and remain in memory. A sign-out action only records local intent; it does not terminate a Microsoft, Alpha, or other remote session. The separate `confirmRemoteSignOut` method is an integration boundary and must only be called after a trusted backend confirms revocation. There is no account authentication, device fingerprinting, IP collection, credential/token storage, remote API, persistence, Supabase integration, Forge event integration, or AlphaClient Core integration. The current device is excluded from sign-out requests. The GUI requires in-game layout/interaction verification. No compile or in-game test is claimed.

## Store — Central Store Core and Addons

Folder: `store/`

- **Store.java** — Central Store core. Maintains the enabled state, registers Store sections, tracks the selected section, and exposes the Addons section facade. It is the shared entry point for future Store sections.
- **Addons/AddonStore.java** — Addons section facade. Coordinates the local catalogue, search/filter, settings, and state manager.
- **Addons/AddonEntry.java** — Immutable add-on metadata record containing ID, name, description, version, author, and category.
- **Addons/AddonCategory.java** — Initial category definitions: All, HUD, Cosmetic, Utility, Gameplay, Staff, and Other. Confirm the final catalogue taxonomy before production use.
- **Addons/AddonRegistry.java** — Bounded in-memory catalogue with duplicate-ID rejection and local installation-state tracking.
- **Addons/AddonSearch.java** — Case-insensitive local search across ID, name, description, and author, with category filtering.
- **Addons/AddonInstallState.java** — Defines Available, Installed, Disabled, and Removed local catalogue states.
- **Addons/AddonManager.java** — Applies local state transitions. Marking an entry Installed does not download files, modify the client, or execute add-on code.
- **Addons/AddonStoreSettings.java** — Holds session-local enablement, selected category, search query, and display style. Settings are not persisted.
- **Addons/AddonStoreGui.java** — Minecraft 1.8.9 preview for browsing supplied entries, searching, and changing local Installed/Disabled state. It is not registered in an existing menu.

**Prototype boundary:** The central Store and Addons catalogue are local prototypes. Add-on entries must be supplied by a caller; no real catalogue service, downloads, external distribution, file installation, code loading/execution, persistence, account synchronization, Supabase integration, Forge event integration, or AlphaClient Core integration is included. The GUI is a preview only. No compile or in-game test is claimed.



## Adjustable Rain and Snow Speed

Folder: `AdjustableRainAndSnowSpeed/`

- **AdjustableRainAndSnowSpeed.java** — Main facade exposing the local settings, controller, and standalone settings-screen factory. The original idea's `enabled` and `style` fields are retained for source-level continuity.
- **WeatherType.java** — Identifies the two independently configurable weather channels: rain and snow.
- **WeatherSpeedSettings.java** — Holds overall enablement, separate rain/snow toggles, and independent speed multipliers. Multipliers are clamped to 0.25x–3.0x; values are in-memory only.
- **WeatherSpeedSnapshot.java** — Provides an immutable read snapshot of the current toggles and multipliers.
- **WeatherSpeedController.java** — Applies channel toggles and multiplier changes and exposes effective values. Disabled channels or the overall module return the vanilla multiplier (1.0x).
- **AdjustableRainAndSnowSpeedGui.java** — Provides a standalone Minecraft 1.8.9 settings preview with separate rain/snow sliders, channel toggles, overall enablement, reset, and Done controls. It is not registered in an existing menu.

**Prototype boundary:** This prototype models separate rain and snow speed settings only. It does not hook Minecraft's weather renderer, change actual precipitation particle timing, distinguish biome-specific weather, persist settings, call Supabase, integrate with Forge events, or connect to AlphaClient Core. The GUI is a preview and is not wired into the client. No compile or in-game test is claimed.


## Adjustable Star Density

Folder: `AdjustableStarDensity/`

- **AdjustableStarDensity.java** — Main facade exposing the local settings, controller, and standalone settings-screen factory. The original idea's `enabled` and `style` fields are retained for source-level continuity.
- **StarDensitySettings.java** — Holds the enabled state, density percentage (0–200%, default 100%), and selected visual style. Settings are in-memory only; disabled mode reports the neutral 1.0x multiplier.
- **StarDensityStyle.java** — Defines the prototype display styles: Default, Soft, and Bright. These are configuration labels only until a renderer is implemented.
- **StarDensitySnapshot.java** — Exposes an immutable snapshot of enablement, density, style, and effective multiplier.
- **StarDensityController.java** — Coordinates enablement, density changes, style cycling, reset, and snapshot creation.
- **AdjustableStarDensityGui.java** — Provides a standalone Minecraft 1.8.9 settings preview with a density slider, enable toggle, style cycling, reset, and Done/back navigation. It is not registered in an existing menu.

**Prototype boundary:** This prototype models star-density settings and a standalone GUI preview only. It does not modify Minecraft's night-sky renderer, generate/remove stars, apply the selected visual styles, persist settings, call Supabase, integrate with Forge events, or connect to AlphaClient Core. The 0–200% range and three style labels are prototype defaults, not requirements stated in the original idea. No compile or in-game test is claimed.

## AFK Camera

Folder: `AfkCamera/`

- **AfkCamera.java** — Main facade exposing local settings, the activity tracker, the state controller, and a standalone settings-screen factory.
- **AfkCameraSettings.java** — Holds enablement, the inactivity delay (prototype default: 60 seconds; configurable from 5 to 600 seconds), and the selected camera preset. Settings remain in memory only.
- **AfkCameraStyle.java** — Defines the proposed display presets Default, Slow Orbit, and Gentle Sweep. These are labels/state only; camera movement is not implemented.
- **AfkActivityTracker.java** — Accepts activity timestamps from a future input/event adapter and calculates elapsed inactivity. It deliberately does not decide which player inputs count as activity.
- **AfkCameraSnapshot.java** — Provides an immutable view of enablement, known activity, idle state, elapsed idle time, delay, and selected preset.
- **AfkCameraController.java** — Evaluates whether the configured idle threshold has been reached and reports when a future camera adapter may activate the selected behavior.
- **AfkCameraGui.java** — Provides a standalone Minecraft 1.8.9 settings preview with enable, delay, preset cycling, reset, and Done/back controls. It is not registered in an existing menu.

**Prototype boundary:** The 60-second default, 5–600-second range, and three preset labels are prototype choices because the source idea does not specify them. The prototype does not yet determine actual player activity from Minecraft input/events, move or rotate the camera, restore the camera on activity, persist settings, call Supabase, integrate with Forge events, or connect to AlphaClient Core. No compile or in-game test is claimed.

## Aimassist Expansion

Folder: `Aimassist Expansion/`

- **AimAssistMod.java** — Standalone Forge client-tick prototype facade. It owns local settings and delegates updates to the controller; it is not registered with the existing AlphaModule system.
- **AimAssistMode.java** — Defines Smooth, Snap, Dynamic, Predictive, Horizontal, Vertical, and Adaptive correction profiles. Verify each profile's feel and boundaries in controlled gameplay before production use.
- **AimAssistSettings.java** — Holds enablement, mode, target priority, range, angle, speed, step, and prediction settings in memory only. Add approved persistence and migration only in a separately authorized integration.
- **AimAssistController.java** — Coordinates target collection, priority selection, prediction offset, angle gating, and bounded rotation updates. Validate target switching and edge cases before integration.
- **AimTarget.java** — Immutable target snapshot with position, per-tick displacement, and distance. Confirm displacement sampling and entity lifecycle handling against live Minecraft updates.
- **AimTargetSelector.java** — Collects visible, alive player targets within range. The current priority enum is exposed; crosshair and lowest-angle currently share the angle score and require a dedicated crosshair ray comparison for exact distinction.
- **AimRotationCalculator.java** — Calculates yaw/pitch deltas without mutating player rotation. Verify angle conventions and boundary cases in Minecraft 1.8.9.
- **AimSmoothingController.java** — Applies bounded correction factors for the selected mode. Tune and validate mode behavior; Snap is immediate up to the configured maximum step, not an unrestricted instant rotation.
- **AimAssistGui.java** — Standalone preview for enablement, mode, and priority selection. It is not registered in the existing AlphaClient menu and does not expose every setting yet.

**Integration boundary:** This is an independent prototype folder inside `Prototypes/`. It does not modify the existing `development` AimAssist, AlphaClient Core, rank permissions, or Alpha Launcher. No Supabase, remote persistence, attacks, or packet sending are included. Compile and in-game behavior have not been verified.


## Aim Pattern Library

Folder: `Aim Pattern Library/`

- **AimPatternLibrary.java** — Main facade exposing the local registry, settings, and service. Retains the original numeric observation API (`record`, `size`, `snapshot`, `mean`, and `reset`) for source-level continuity.
- **AimPattern.java** — Immutable named pattern snapshot with category, creation timestamp, and numeric observations.
- **AimPatternCategory.java** — Defines the prototype organization labels General, Horizontal, Vertical, Smoothing, Targeting, and Other. These are labels, not aim-assist modes.
- **AimPatternStats.java** — Calculates count, mean, minimum, maximum, and population standard deviation for a pattern's observations.
- **AimPatternValidator.java** — Validates pattern names and rejects non-finite numeric observations.
- **AimPatternRegistry.java** — Maintains a bounded in-memory collection of patterns and supports creation, observation recording, renaming, categorization, removal, and immutable snapshots.
- **AimPatternSettings.java** — Holds session-local selected-pattern, category-filter, and statistics-display settings.
- **AimPatternService.java** — Coordinates pattern management, filtering, deletion, and statistics access.
- **AimPatternLibraryGui.java** — Provides a standalone Minecraft 1.8.9 preview for creating, filtering, reviewing, and deleting local patterns. The GUI is not registered in an existing menu; pattern selection and richer editing controls still require completion.

**Prototype boundary:** The source idea only supplied a numeric observation accumulator. Named patterns, categories, the 50-pattern default limit, statistics, and the GUI are prototype expansions. Observations are caller-supplied numeric values; no live aim data is collected, no aim behavior is changed, and no pattern recognition or automatic comparison is implemented. Data and settings remain in memory only. There is no persistence, Supabase integration, Forge event integration, or AlphaClient Core integration. No compile or in-game test is claimed.



## Singleplayer — Easy Craft

Folder: `Singleplayer/EasyCraft/`

- **EasyCraft.java** — Owns the recipe catalog and local favorites collection for the standalone prototype. It is not registered with AlphaClient Core.
- **EasyCraftCategory.java** — Defines the compact recipe-book categories: All, Building, Tools, Combat, Food, Redstone, and Misc.
- **EasyCraftRecipe.java** — Wraps a vanilla/Forge `IRecipe`, copies its output and supported shaped/shapeless ingredients, assigns a display category, and creates a local favorite key. Ingredient extraction for custom third-party recipe implementations still needs compatibility review.
- **EasyCraftRecipeCatalog.java** — Reads the recipes registered in Minecraft/Forge's `CraftingManager`, skips invalid outputs, and filters by search, category, and favorites.
- **EasyCraftFavorites.java** — Manages the user's favorite recipe keys and saves changes through local storage.
- **EasyCraftFavoriteStorage.java** — Reads/writes favorites in `config/alphaclient/easycraft-favorites.properties`; no account or remote storage is used.
- **EasyCraftCraftingAdapter.java** — Defines the integration contract for checking available crafts and requesting crafting through the active vanilla Crafting Table container. A real container adapter is still required.
- **EasyCraftTableButton.java** — Draws the small book-icon button intended for the vanilla Crafting Table GUI.
- **EasyCraftCraftingTableHook.java** — Provides the narrow button creation/opening boundary for `GuiCrafting` only. The actual call sites in the vanilla screen or an approved mixin have not been added.
- **EasyCraftGui.java** — Provides a compact modern-Recipe-Book-inspired GUI with category tabs, search, recipe icons, ingredient preview, favorite toggle, available-craft count, Craft, and Back controls. It is designed to be opened only from the Crafting Table context.

**Prototype boundary:** This is an independent Singleplayer prototype folder inside `Prototypes/`. It does not change AlphaClient Core, `development`, the normal player inventory GUI, Alpha Launcher, rank/permission rules, or Supabase. The book button is a hook component, not yet injected into the live Crafting Table screen. The adapter that safely transfers ingredients through the vanilla `ContainerWorkbench` and performs the requested craft is not implemented yet; the Craft button delegates to that adapter contract. The recipe catalog reads registered vanilla/Forge recipes, but custom recipe ingredient extraction may need additional adapters. Favorites are local-only. No compile, automated test, or in-game test is claimed because this branch contains no build configuration in the inspected tree and no runtime test was run.

## Player History

Folder: `playerhistory/`

- **PlayerHistory.java** — Standalone module facade. Retains the original lightweight record API and exposes explicit lifecycle and GUI-opening methods.
- **PlayerHistoryEntry.java** — Immutable per-player summary with UUID, latest name, first/last observation timestamps, session count, and accumulated observed online duration.
- **PlayerHistoryEvent.java** — Immutable first-observed/joined/left event with timestamp and connection context.
- **PlayerHistoryStore.java** — Maintains player summaries and a bounded 500-event timeline; returns sorted immutable snapshots.
- **PlayerHistoryStorage.java** — Loads/saves local Properties data at `config/alphaclient/player-history.properties`; skips malformed individual entries and replaces files through a temporary write.
- **PlayerHistoryTracker.java** — Samples the active client's visible network player list every 20 client ticks, ignores the local player, and records observed presence transitions.
- **PlayerHistoryEventHandler.java** — Forge client-tick adapter. Registration is explicit; no AlphaClient Core hook is added.
- **PlayerHistoryFormatter.java** — Formats timestamps and observed durations.
- **PlayerHistoryGui.java** — Standalone searchable and scrollable player list with online/offline state, session count, and total observed time.

**Prototype boundary:** The original idea only provided an in-memory text-record model. Live player-list sampling, per-UUID summaries, event history, the searchable GUI, local persistence, the 20-tick sampling interval, and the 500-event cap are prototype expansions. The tracker sees only entries exposed by the local client and cannot recover history from before registration or inspect server-side/private data. First-sample entries are labelled FIRST_OBSERVED rather than confirmed joins. No menu button, keybind, rank permission, StaffMods integration, Supabase, or AlphaClient Core registration is included. Minecraft/Forge compatibility, compilation, and in-game behavior remain unverified.

## Air HUD

Folder: `AirHud/`

- **AirHud.java** — Standalone module facade with explicit Forge event registration, settings access, local loading/saving, and GUI opening. It does not depend on AlphaModuleRegistry.
- **AirHudSettings.java** — Holds enablement, label visibility, one of four screen-corner positions, and a selectable text color.
- **AirHudRenderer.java** — Displays remaining air ticks and a tick-derived seconds estimate while the local player is underwater.
- **AirHudEventHandler.java** — Provides the Forge overlay-event listener boundary.
- **AirHudStorage.java** — Loads and saves settings locally at `config/alphaclient/air-hud.properties`.
- **AirHudGui.java** — Standalone settings screen for enablement, label visibility, position, and color.

**Prototype boundary:** The original idea only contained a minimal overlay event handler dependent on AlphaModuleRegistry. The standalone facade, settings, local persistence, corner positioning, color selection, and GUI are prototype expansions. No existing menu button, keybind, AlphaClient Core registration, Supabase, or Launcher integration is included. The displayed seconds are estimated from client air ticks. Compilation and in-game behavior remain unverified.


## Armor Break Warning

Folder: `armorbreakwarning/`

- **ArmorBreakWarning.java** — Standalone module facade with explicit Forge event registration, local settings, storage, and GUI opening.
- **ArmorBreakWarningSettings.java** — Holds enablement and the remaining-durability threshold. The default is 10%; the standalone GUI cycles through 5%, 10%, 15%, 20%, and 25%.
- **ArmorBreakWarningRenderer.java** — Counts equipped damageable armor pieces at or below the configured remaining-durability threshold and renders the warning in the HUD.
- **ArmorBreakWarningEventHandler.java** — Provides the Forge overlay-event adapter.
- **ArmorBreakWarningStorage.java** — Loads and saves local settings at `config/alphaclient/armor-break-warning.properties`.
- **ArmorBreakWarningGui.java** — Standalone settings screen for enablement and threshold selection.

**Prototype boundary:** The original idea provides a fixed 10% threshold and a HUD warning with the number of low-durability armor pieces. The configurable threshold and standalone settings screen are prototype additions. This checks only the local player's equipped armor; it is separate from opponent armor-break prediction. Registration is explicit. No existing menu button, keybind, AlphaModuleRegistry integration, AlphaClient Core registration, rank permission, Supabase, or Launcher integration is included. Compilation and in-game behavior remain unverified.


## Auto Loading Server Profiles — complete source and integration audit (2026-11-27)

**Location:** \`src/main/java/com/alpharip/staffmod/AutoLoadingServerProfiles/\`  
**Branch:** \`mod-ideas\`  
**Purpose:** Select an existing AlphaClient profile from the Minecraft server address after a connection is reported to the prototype controller.

This section is an inspection-based record of the code currently present. It does not claim that the prototype is compiled, registered, connected to Forge events, or verified in-game.

### 1. Overall project status

**Status: substantial standalone logic prototype; not a complete in-game feature.**

The folder contains ten Java files split into five responsibility packages:

- \`matching/\`: address normalization and match selection.
- \`model/\`: immutable server-to-profile binding and match data.
- \`registry/\`: in-memory binding collection.
- \`runtime/\`: controller, activation boundary, and result.
- \`storage/\`: local Properties persistence contract and implementation.

The prototype already contains the core algorithm and supporting data/storage pieces. It is more than an empty idea stub, but it is not an end-to-end Minecraft feature. There is no implemented Minecraft connection listener, no real adapter to the active AlphaClient Profiles manager, no lifecycle/bootstrap owner, and no GUI for configuring bindings.

**What the current source can do when called by an external integration:**

1. Normalize a supplied server address.
2. Compare it with enabled server-address bindings.
3. Prefer an exact match over a domain/subdomain match.
4. Prefer the most specific matching binding when multiple domain bindings match.
5. Return the selected profile identifier to an activation adapter.
6. Report no match, activation accepted, or activation rejected.
7. Load and save binding records through a caller-supplied local Properties file path.

**What it does not do by itself:** detect a Minecraft connection, discover the active profile list, validate that a referenced profile exists, switch the real AlphaClient profile, decide when to load/save, expose controls to the player, or register itself with AlphaClient.

### 2. Complete Java-file inventory — every file in this prototype

There are exactly **10 Java source files** in the module at this branch snapshot.

#### 2.1 \`matching/ServerAddressNormalizer.java\`

**Role:** Static address normalization and address-compatibility rules.

**Public API:**

- \`normalize(String address)\`
  - Rejects null or blank input with \`IllegalArgumentException\`.
  - Trims surrounding whitespace and lowercases using \`Locale.ROOT\`.
  - Removes a leading URI scheme through \`://\`.
  - Removes the path and query portions.
  - Removes trailing dots from a non-bracketed hostname.
  - Preserves an explicitly supplied port rather than inserting a default port.
  - For a non-bracketed single-colon address, validates that the port contains digits and is in the range 1–65535.
  - Handles bracketed address text separately, intended for IPv6-style literals.
- \`matches(String configuredAddress, String connectedAddress)\`
  - Normalizes both inputs.
  - Requires an explicitly configured port to equal the connected port.
  - Accepts an exact host match.
  - Otherwise accepts a DNS-style subdomain only when the connected host ends in a dot plus the configured host. This avoids treating an arbitrary string suffix as a subdomain.
  - A binding without a configured port can match the same host when the connected address uses a port.
- \`specificity(String address)\`
  - Scores the host length and adds one point when a port is explicitly present. The matcher uses this score to choose between matching domain bindings.

**Private implementation pieces:** \`split(...)\` separates host and optional port; \`isDnsName(...)\` prevents colon-containing literals from being treated as DNS names; the private \`Address\` value holder carries host and port.

**Current limitations / validation gaps visible in source:**

- The bracketed-address branch checks the port's digit format but does not apply the 1–65535 numeric range check used by the non-bracketed branch.
- The bracketed branch does not reject an empty host between the brackets.
- The code does not perform full DNS syntax validation or IDN conversion.
- URI fragments are not explicitly stripped.
- These are code-inspection findings, not claims that a particular in-game connection currently fails.

#### 2.2 \`matching/ServerProfileMatcher.java\`

**Role:** Chooses the best enabled binding for one connected server address.

**Public API:**

- \`findBest(String connectedAddress, Collection<ServerProfileBinding> bindings)\`
  - Returns no match for null/blank connection input or a null collection.
  - Ignores null binding entries and disabled bindings.
  - Normalizes the connected address.
  - Uses \`ServerAddressNormalizer.matches(...)\` for compatibility.
  - Gives an exact normalized address match priority over a domain match.
  - When match types are equal, prefers the greater specificity score.
  - Returns a \`ServerProfileMatch\` carrying the selected binding and match type, or \`null\` when nothing matches.

**Tie behavior:** When two candidates have the same match type and specificity score, the first encountered candidate remains selected. The registry currently preserves insertion order through \`LinkedHashMap\`.

**Not included:** profile existence validation, user-facing explanation, server event detection, or activation.

#### 2.3 \`model/ServerProfileBinding.java\`

**Role:** Immutable mapping from a server address/domain to an existing profile identifier.

**Stored fields:**

- \`serverAddress\`: normalized address/domain.
- \`profileId\`: nonblank identifier supplied by the caller.
- \`enabled\`: whether the matcher may use this binding.

**Public API:** Constructor plus \`getServerAddress()\`, \`getProfileId()\`, and \`isEnabled()\`.

**Validation:** Address and profile identifier must not be null or blank. The server address is normalized during construction.

**Not included:** checking the profile identifier against AlphaClient's actual profile registry, renaming a profile when its binding changes, or automatically deleting bindings when a profile is deleted.

#### 2.4 \`model/ServerProfileMatch.java\`

**Role:** Immutable result describing which binding was selected.

**Data:**

- Selected \`ServerProfileBinding\`.
- \`Type.EXACT\` or \`Type.DOMAIN\`.

**Validation:** Constructor rejects a null binding or null type.

**Public API:** \`getBinding()\` and \`getType()\`.

This class describes a decision; it does not activate anything.

#### 2.5 \`registry/ServerProfileRegistry.java\`

**Role:** Thread-synchronized in-memory registry of server-to-profile bindings.

**Public API:**

- \`put(ServerProfileBinding)\`: adds or replaces the binding under its normalized address key.
- \`remove(String serverAddress)\`: normalizes the supplied address and removes that key.
- \`get(String serverAddress)\`: normalizes the supplied address and returns the matching binding.
- \`getAll()\`: returns an unmodifiable snapshot list, not the mutable internal map.
- \`replaceAll(Collection<ServerProfileBinding>)\`: clears the current registry and inserts the supplied values; null means an empty registry.
- \`size()\`: returns the number of registered bindings.

**Implementation details:** A \`LinkedHashMap\` preserves insertion order. Public registry methods are synchronized. Adding another binding for the same normalized address replaces the previous entry.

**Not included:** automatic persistence, automatic profile-deletion cleanup, a GUI, or validation that the target profile exists.

#### 2.6 \`runtime/ServerProfileActivationPort.java\`

**Role:** Integration boundary for the real AlphaClient Profiles implementation.

**Contract:**

- \`boolean activateProfile(String profileId)\`

A future Minecraft/AlphaClient adapter must implement this interface and call the approved real profile-selection API. Returning \`true\` means the adapter accepted the activation request; returning \`false\` means it rejected it.

**Current state:** This is only an interface. No implementation that calls \`ModuleProfileManager\` is present in this module.

#### 2.7 \`runtime/ServerProfileAutoLoadController.java\`

**Role:** Coordinates matching and delegates activation.

**Dependencies supplied through its constructor:**

- \`ServerProfileRegistry\`
- \`ServerProfileMatcher\`
- \`ServerProfileActivationPort\`

The constructor rejects null dependencies.

**Public API:**

- \`onServerConnected(String serverAddress)\`
  1. Reads a snapshot of registry bindings.
  2. Asks the matcher for the best match.
  3. Returns \`NO_MATCH\` when there is no match.
  4. Calls \`activateProfile(profileId)\` when a match exists.
  5. Returns either \`ACTIVATED\` or \`ACTIVATION_REJECTED\` with the selected match.

**Important boundary:** The method name describes the event the future integration should report. This class does not subscribe to a Minecraft or Forge connection event. Another component must call it after the appropriate connection-success point.

**Not included:** client-thread scheduling, connection lifecycle registration, duplicate-event suppression, retry policy, or exception-to-result conversion around the activation adapter.

#### 2.8 \`runtime/ServerProfileAutoLoadResult.java\`

**Role:** Immutable outcome of one auto-load attempt.

**Statuses:**

- \`NO_MATCH\`: no enabled binding matched.
- \`ACTIVATED\`: a match existed and the activation adapter returned true.
- \`ACTIVATION_REJECTED\`: a match existed but the adapter returned false.

**Public API:** Static factories \`noMatch()\`, \`activated(match)\`, and \`rejected(match)\`; getters \`getStatus()\` and \`getMatch()\`.

For \`NO_MATCH\`, the match is null. For the other states, the controller supplies the selected match.

**Not included:** user-facing messages, logging, notification display, or persistence of the last result.

#### 2.9 \`storage/ServerProfileStore.java\`

**Role:** Persistence abstraction.

**Contract:**

- \`List<ServerProfileBinding> load() throws IOException\`
- \`void save(List<ServerProfileBinding> bindings) throws IOException\`

The registry and matching logic depend on this interface rather than a concrete file format.

**Not included:** a default path, automatic startup loading, automatic save-on-change, schema migration, or a lifecycle owner.

#### 2.10 \`storage/PropertiesServerProfileStore.java\`

**Role:** Local Java Properties implementation of \`ServerProfileStore\`.

**Constructor:** Receives a caller-supplied \`Path\`; null is rejected. The class does not decide the AlphaClient configuration directory itself.

**Load behavior:**

- Returns an empty list when the file does not exist.
- Loads the Properties file when present.
- Reads \`binding.count\`.
- Reads each indexed record's \`server\`, \`profile\`, and \`enabled\` values.
- Defaults a missing \`enabled\` property to true.
- Rejects missing server/profile values and wraps invalid binding data in \`IOException\`.
- Rejects a negative or non-numeric binding count.

**Save behavior:**

- Rejects a null list.
- Creates the parent directory when necessary.
- Writes the binding count and each address/profile/enabled value.
- Uses \`Properties.store(...)\` with the comment \`Alpha Client - Auto Loading Server Profiles\`.

**Current limitations visible in source:**

- The write is direct to the target file; there is no temporary-file plus atomic-replacement strategy.
- There is no explicit upper bound on the number of loaded bindings.
- \`Boolean.parseBoolean\` maps any value other than case-insensitive \`true\` to false; malformed boolean text is not separately rejected.
- The caller must handle load/save timing and I/O errors.
- No compile, round-trip test, corruption test, or runtime test is claimed here.

### 3. Data flow and ownership

The intended call sequence is:

1. A future client lifecycle component chooses the local Properties path.
2. It calls \`ServerProfileStore.load()\`.
3. It supplies the loaded records to \`ServerProfileRegistry.replaceAll(...)\`.
4. A future Forge/Minecraft connection adapter detects a successful server connection and obtains the actual connected address.
5. That adapter calls \`ServerProfileAutoLoadController.onServerConnected(address)\` on the correct client thread.
6. The controller asks \`ServerProfileMatcher\` to select the best enabled binding.
7. The controller delegates the selected profile ID to \`ServerProfileActivationPort\`.
8. The production adapter validates and activates the existing AlphaClient profile.
9. When a binding is created, edited, enabled, disabled, or removed, the owning settings/lifecycle component saves \`registry.getAll()\` through \`ServerProfileStore.save(...)\`.

Steps 1–3, 4–5, 8, and 9 are not wired into the live client by this prototype. The core classes for matching, registry, persistence, controller, and the adapter contract are present.

### 4. Matching rules currently encoded in source

| Configured binding | Connected address | Expected matcher outcome |
|---|---|---|
| \`play.example.net\` | \`play.example.net\` | Exact |
| \`example.net\` | \`play.example.net\` | Domain |
| \`example.net\` | \`eu.play.example.net\` | Domain |
| \`play.example.net:25566\` | \`play.example.net:25566\` | Exact |
| \`play.example.net:25566\` | \`play.example.net:25565\` | No match |
| \`other.net\` | \`play.example.net\` | No match |
| disabled \`play.example.net\` | \`play.example.net\` | Ignored |

A configured host without a port is intentionally port-agnostic. A configured host with a port requires the same connected port. An exact match wins over a broader domain match. Among domain matches, the more specific configured host wins.

### 5. Existing AlphaClient profile-related source — do not confuse these systems

#### 5.1 Active profile manager in \`development\`

The inspected \`development\` branch contains:

- \`src/main/java/com/alpharip/staffmod/profile/ModuleProfileManager.java\`
- \`src/main/java/com/alpharip/staffmod/profile/Profile.java\`
- \`src/main/java/com/alpharip/staffmod/profile/ProfileManager.java\`

The source comment in \`ProfileManager.java\` explicitly marks that class as deprecated and says the active AlphaClient Profiles system is \`ModuleProfileManager\`, which owns \`profiles.json\`.

The inspected \`ModuleProfileManager\` contains profile loading/saving, default-profile handling, profile listing, selection, creation, rename and deletion operations, profile/module settings, module-state application, and mod-colour handling. It also retains legacy Bhop profile data for backwards compatibility. Its nested \`BhopProfile\` model contains profile data; the old name in that model must not be interpreted as proof that the entire Profiles feature is still a standalone Bhop-only feature.

The deprecated \`ProfileManager\` retains legacy profile-file compatibility and references \`profiles_legacy.json\` and the old \`bhop_profiles.json\` migration path. \`Profile.java\` is the associated legacy profile data class in that older manager.

**Important:** This is the existing general AlphaClient Profiles system. It is the intended future activation target only after its actual API and lifecycle are integrated deliberately. The new prototype currently does not call it.

#### 5.2 Other profile-related files beside \`staffops/\` on \`mod-ideas\`

These files are present in the inspected \`mod-ideas\` tree, but are not the new auto-loader:

- \`Customization/ServerProfiles.java\`
  - Its physical path is under \`Customization/\`, but its declared package is \`com.alpharip.staffmod.staffops.ideas\`.
  - It stores an enabled flag, generic string options, and a list of string records.
  - It supports recording, listing, searching, and clearing those records.
  - Its own class comment describes it as a client-side foundation; Minecraft event hooks, rendering, and network integration are separate.
  - It does not implement the server-address-to-AlphaClient-profile matching algorithm in the new module.
- \`Customization/ServerProfileSpecificKeybinds.java\`
  - Its declared package is also \`com.alpharip.staffmod.staffops.ideas\`.
  - It currently contains only an enabled flag, an active-server-profile string, and a \`separateBindingsByServer\` flag.
  - The class comment explicitly says runtime integration is not implemented.
  - It does not yet switch keybind sets on connection.

Neither of these two files is a substitute for the active \`development/profile/ModuleProfileManager\`, and neither is the new auto-loader.

### 6. Branch-by-branch presence matrix

| Component | \`mod-ideas\` | AlphaClient \`development\` | Alpha Launcher |
|---|---|---|---|
| New \`AutoLoadingServerProfiles/\` folder | **Present** | **Absent** in inspected branch tree | Not applicable; no matching module folder |
| Address normalizer and matcher | **Present** | **Absent** | **Absent** |
| Server-address → profile binding registry | **Present** | **Absent** | **Absent** |
| Properties persistence for those bindings | **Present** | **Absent** | **Absent** |
| Auto-load controller and activation interface | **Present** | **Absent** | **Absent** |
| Real Minecraft/Forge connection-event adapter for this feature | **Absent** | **Absent** | **Absent** |
| Adapter that calls the real active AlphaClient profile manager | **Absent** | Existing profile manager exists, but no auto-loader adapter | **Absent** |
| Existing general AlphaClient Profiles manager | Other prototype/legacy foundations exist; not the production manager | **Present** | Community account profiles are separate |
| Server-profile binding management GUI | **Absent** | **Absent** for this feature | **Absent** |

The branch checks above are source-tree checks at the snapshot recorded in this README. “Absent” means no matching implementation was found in the inspected branch tree; it does not claim that no future work or untracked local file can exist.

### 7. Alpha Launcher overlap — what exists and what does not

The inspected Alpha Launcher branch is \`feature/changelog-audit-logging\`.

**Existing Launcher functionality related by the word “profile” or by AlphaClient module management:**

- The Launcher has community-profile data such as display name, bio, and avatar URL. The inspected migration adds \`bio\` and \`avatar_url\` to \`public.profiles\`, limits the bio to 160 characters, and defines storage policies for profile avatars.
- The Launcher has a Minecraft-account linking data path. The inspected migration creates \`public.alpha_minecraft_accounts\` and an authenticated \`link_minecraft_uuid(...)\` flow to associate Minecraft UUIDs with an Alpha community profile.
- The Launcher has a Client Mod Permissions Manager. Its inspected source manages module access by community rank, player-specific overrides, category grants, and revocation. It mirrors the AlphaClient StaffMods folder structure and deliberately excludes Founder-only personal modules from the Developer → Mods catalog.
- These are community identity, linked-account, and module-permission features. They are not Minecraft server-specific gameplay profile switching.

**Not present in the inspected Launcher branch for Auto Loading Server Profiles:**

- No server-address-to-AlphaClient-profile binding list.
- No exact-host/subdomain matching for Minecraft multiplayer connections.
- No profile activation request sent from Launcher to the running Minecraft client when a server is joined.
- No Auto Loading Server Profiles settings screen or persistence.
- No Launcher dependency is required by the current local-only prototype.

**Scope decision:** This prototype remains a client-side Minecraft feature. The Launcher is not modified, and no new Supabase table, migration, RPC, or remote storage is required for the currently documented design.

### 8. Implementation work still required for a complete in-game feature

The following work is still outstanding. It is not represented as already implemented.

1. **Connection event integration**
   - Identify the correct Minecraft 1.8.9 / Forge 11.15.1.2318 successful-connection lifecycle point.
   - Add a dedicated adapter that obtains the actual connected server address.
   - Ensure it does not trigger on failed connection attempts, menu previews, or stale server data.
   - Invoke the controller on the Minecraft client thread.
2. **Profile activation adapter**
   - Inspect the exact current \`ModuleProfileManager\` API on \`development\` at integration time.
   - Resolve the binding's profile identifier against the real profile list.
   - Define behavior when the profile was renamed or deleted.
   - Activate through the existing profile system without creating a second profile manager or bypassing existing module/profile application logic.
   - Return a truthful accepted/rejected result.
3. **Runtime/bootstrap ownership**
   - Construct the registry, matcher, store, activation adapter, and controller once.
   - Choose a stable local config path under the approved AlphaClient configuration directory.
   - Load bindings at the correct client lifecycle point.
   - Save after changes and handle I/O errors.
   - Avoid duplicate Forge event registrations and duplicate runtime instances.
4. **Binding management UI**
   - List configured server addresses and their target profiles.
   - Add, edit, remove, enable, and disable bindings.
   - Populate profile choices from the real Profiles manager rather than accepting arbitrary IDs only.
   - Show validation errors for malformed addresses, duplicate bindings, missing profiles, and storage failures.
   - Make it clear that a domain binding can apply to its subdomains.
5. **Profile lifecycle consistency**
   - Decide what happens to a binding when its target profile is renamed or deleted.
   - Prevent silent activation of a different profile if an identifier becomes stale.
   - Define behavior for duplicate matching bindings and equal-specificity ties.
6. **Hardening**
   - Validate bracketed IPv6 host/port values consistently with normal host/port values.
   - Decide whether URI fragments and internationalized hostnames should be supported.
   - Consider atomic file replacement, malformed boolean rejection, and a sensible maximum binding count.
   - Add tests for normalization, exact/domain matching, port rules, priority, disabled bindings, persistence round trips, malformed files, and missing profiles.
7. **Verification**
   - Compile with the actual project ForgeGradle/Java 8 setup.
   - Run focused unit tests where the project test setup permits.
   - Verify successful join, failed join, server switch, subdomain match, exact match priority, profile deletion/rename, and local save/reload in Minecraft 1.8.9.
   - Confirm no existing profile behavior, Bhop compatibility data, rank rules, or module permissions are changed.

### 9. Current completeness assessment

| Area | Assessment |
|---|---|
| Address normalization | **Implemented in prototype source; edge-case validation still needed** |
| Exact host matching | **Implemented in prototype source; not runtime-tested** |
| Domain/subdomain matching | **Implemented in prototype source; not runtime-tested** |
| Port-aware matching | **Implemented in prototype source; bracketed IPv6 port validation gap remains** |
| Match priority | **Implemented in prototype source; equal-score tie policy is insertion order** |
| In-memory binding registry | **Implemented in prototype source** |
| Local Properties store | **Implemented in prototype source; hardening and round-trip verification remain** |
| Controller/result flow | **Implemented as a callable prototype flow** |
| Actual profile activation | **Missing; interface only** |
| Minecraft/Forge connection detection | **Missing** |
| Lifecycle/bootstrap | **Missing** |
| Binding GUI | **Missing** |
| AlphaClient \`development\` integration | **Not present** |
| Alpha Launcher integration | **Not present and not required by the current design** |
| Supabase/remote persistence | **Not present and not required by the current design** |
| Build / automated test / in-game verification | **Not verified** |

### 10. Scope and preservation rules

- The prototype belongs to \`mod-ideas\` only until a separate integration task is authorized.
- The original idea file under \`staffops/ideas/\` was removed when this prototype was created; the new module is the replacement location.
- The existing \`development\` Profiles system remains separate and unchanged.
- The existing \`Customization/ServerProfiles.java\` and \`ServerProfileSpecificKeybinds.java\` files remain separate and unchanged.
- Alpha Launcher remains unchanged.
- No Supabase or remote service is used.
- No AlphaClient Core, rank, permission, StaffMods, or Bhop behavior is changed by this README update.
- This document records source presence and missing integration work; it does not certify compilation or runtime behavior.

## Sibling module directory inventory

The following counts are taken from the current `mod-ideas` Git tree. There are **63 top-level sibling directories** inside `Prototypes/`; **62 contain Java source files** and `Donator Modules/` currently contains no Java source file. The counts do not imply that any folder is a complete or tested feature.

| Sibling folder | Java files |
|---|---:|
| `AccessibilityTheme/` | 2 |
| `AccessReviewReminder/` | 4 |
| `account/` | 8 |
| `achievements/` | 17 |
| `ActingRankAssignment/` | 8 |
| `ActiveDeviceListGlobalSignOut/` | 9 |
| `AdjustableRainAndSnowSpeed/` | 6 |
| `AdjustableStarDensity/` | 6 |
| `AdjustThirdPerson/` | 3 |
| `Adventure Diary/` | 4 |
| `AfkCamera/` | 7 |
| `Aim Pattern Library/` | 9 |
| `AirHud/` | 7 |
| `AlphaClans/` | 16 |
| `AlphaCoins/` | 23 |
| `AlphaCrashReporter/` | 33 |
| `Analystics/` | 10 |
| `animalbreedingcooldowntimer/` | 8 |
| `armorbreakwarning/` | 6 |
| `ArrowCounter/` | 7 |
| `AutoLoadingServerProfiles/` | 10 |
| `autosave/` | 8 |
| `Badges/` | 15 |
| `basestorageledger/` | 5 |
| `Bestiary/` | 5 |
| `BiomeHud/` | 6 |
| `Block Break Ring/` | 7 |
| `cases/` | 88 |
| `ChestItemTotal/` | 9 |
| `Clutch/` | 4 |
| `Combo/` | 2 |
| `Cooldowns/` | 2 |
| `Customization/` | 33 |
| `Donator Modules/` | 0 |
| `gui/` | 1 |
| `hitbox/` | 4 |
| `Indicators/` | 7 |
| `Kitmap & HCF/` | 4 |
| `Lifetime Stats Book/` | 4 |
| `LootFeed/` | 4 |
| `MinecraftWindowAlphaClient/` | 7 |
| `OpponentProfiles/` | 1 |
| `Pet Memorial/` | 4 |
| `playerhistory/` | 9 |
| `Predictions/` | 16 |
| `punishmentcommandpreflight/` | 5 |
| `punishmentconflictdetector/` | 5 |
| `Remember Villagers/` | 8 |
| `rememberchestinventory/` | 8 |
| `resourcepackorganizer/` | 4 |
| `screenshotcomparison/` | 11 |
| `Singleplayer/` | 10 |
| `Sounds/` | 2 |
| `Staff Dashboard Expansion/` | 81 |
| `Statistics/` | 4 |
| `TimeTill/` | 7 |
| `Timing/` | 12 |
| `togglesneak/` | 11 |
| `Trackers/` | 12 |
| `Trainers/` | 23 |
| `Viewer/` | 16 |
| `Weather-Reactive HUD/` | 4 |
| `Wrapped/` | 8 |
| **Total in prototype folders inside `Prototypes/`** | **679** |

## Current Java Source Inventory (authoritative)

Snapshot: branch `mod-ideas`, commit/tree `ba25bc56f7196c3cb9f3b9d7cc74932469d4ac56`.
Count: **679 Java files across 62 prototype folders inside `Prototypes/`, plus 1 root-level Java file (680 total)**.
This manifest is generated from the current Git tree. It excludes everything under `staffops/` and excludes non-Java files. A listed file being present does not mean it compiles, is registered, or has been tested.

### `AccessibilityTheme/`

- `AccessibilityThemeGui.java`
- `AccessibilityThemeSettings.java`

### `AccessReviewReminder/`

- `AccessReviewEntry.java`
- `AccessReviewReminder.java`
- `AccessReviewReminderGui.java`
- `AccessReviewStorage.java`

### `account/`

- `AccountLink.java`
- `AccountLinking.java`
- `AccountLinkingGui.java`
- `AccountLinkingService.java`
- `AccountLinkingSettings.java`
- `AccountLinkRegistry.java`
- `AccountLinkStatus.java`
- `AccountLinkValidator.java`

### `achievements/`

- `AchievementChecklist.java`
- `AchievementChecklistGui.java`
- `AchievementChecklistSearch.java`
- `AchievementChecklistSettings.java`
- `AchievementEntry.java`
- `AchievementSuffixEntry.java`
- `AchievementSuffixGui.java`
- `AchievementSuffixRegistry.java`
- `AchievementSuffixService.java`
- `AchievementSuffixSettings.java`
- `AchievementWall.java`
- `AchievementWallEntry.java`
- `AchievementWallGui.java`
- `AchievementWallService.java`
- `AchievementWallSettings.java`
- `AchievementWallSnapshot.java`
- `AchievementWallSort.java`

### `ActingRankAssignment/`

- `ActingRankAssignment.java`
- `ActingRankAssignmentGui.java`
- `ActingRankEntry.java`
- `ActingRankRegistry.java`
- `ActingRankService.java`
- `ActingRankSettings.java`
- `ActingRankStatus.java`
- `ActingRankValidator.java`

### `ActiveDeviceListGlobalSignOut/`

- `ActiveDeviceListGlobalSignOut.java`
- `ActiveDeviceListGlobalSignOutGui.java`
- `ActiveDeviceRegistry.java`
- `ActiveDeviceService.java`
- `ActiveDeviceSession.java`
- `ActiveDeviceSettings.java`
- `ActiveDeviceSignOutResult.java`
- `ActiveDeviceStatus.java`
- `ActiveDeviceValidator.java`

### `AdjustableRainAndSnowSpeed/`

- `AdjustableRainAndSnowSpeed.java`
- `AdjustableRainAndSnowSpeedGui.java`
- `WeatherSpeedController.java`
- `WeatherSpeedSettings.java`
- `WeatherSpeedSnapshot.java`
- `WeatherType.java`

### `AdjustableStarDensity/`

- `AdjustableStarDensity.java`
- `AdjustableStarDensityGui.java`
- `StarDensityController.java`
- `StarDensitySettings.java`
- `StarDensitySnapshot.java`
- `StarDensityStyle.java`

### `AdjustThirdPerson/`

- `AdjustableThirdPersonCameraDistance.java`
- `ThirdPersonOptionsGui.java`
- `ThirdPersonOptionsHooks.java`

### `Adventure Diary/`

- `AdventureDiaryBook.java`
- `AdventureDiaryEntry.java`
- `AdventureDiaryEvent.java`
- `DramaticDiaryNarrator.java`

### `AfkCamera/`

- `AfkActivityTracker.java`
- `AfkCamera.java`
- `AfkCameraController.java`
- `AfkCameraGui.java`
- `AfkCameraSettings.java`
- `AfkCameraSnapshot.java`
- `AfkCameraStyle.java`

### `Aim Pattern Library/`

- `AimPattern.java`
- `AimPatternCategory.java`
- `AimPatternLibrary.java`
- `AimPatternLibraryGui.java`
- `AimPatternRegistry.java`
- `AimPatternService.java`
- `AimPatternSettings.java`
- `AimPatternStats.java`
- `AimPatternValidator.java`

### `AirHud/`

- `AirHud.java`
- `AirHudEventHandler.java`
- `AirHudGui.java`
- `AirHudRenderer.java`
- `AirHudSettings.java`
- `AirHudStorage.java`
- `MinecraftForgeBridge.java`

### `AlphaClans/`

- `AlphaClans.java`
- `Clan.java`
- `ClanChatMessage.java`
- `ClanCommand.java`
- `ClanCommandHandler.java`
- `ClanCommandParser.java`
- `ClanInvite.java`
- `ClanMember.java`
- `ClanPermission.java`
- `ClanPlayerDirectory.java`
- `ClanRepository.java`
- `ClanResult.java`
- `ClanRole.java`
- `ClanScreenModel.java`
- `ClanService.java`
- `FileClanRepository.java`

### `AlphaCoins/`

- `AlphaCoin.java`
- `AlphaCoinsCommands.java`
- `AlphaCoinsCore.java`
- `AlphaCoinService.java`
- `AlphaCoinsPlaytime.java`
- `AlphaCoinsPlaytimeEventHandler.java`
- `AlphaCoinsPlaytimeModule.java`
- `AlphaCoinStorage.java`
- `AlphaCoinTransaction.java`
- `AlphaCoinWallet.java`
- `AlphaDailyChallenges.java`
- `ChallengeDifficulty.java`
- `ChallengeMetric.java`
- `DailyChallengeCatalog.java`
- `DailyChallengeDefinition.java`
- `DailyChallengeEventHandler.java`
- `DailyChallengeManager.java`
- `DailyChallengeProgress.java`
- `DailyChallengesGui.java`
- `DailyChallengeStorage.java`
- `PlaytimeRewardDefinition.java`
- `PlaytimeRewardStatus.java`
- `PlaytimeRewardStorage.java`

### `AlphaCrashReporter/`

- `AlphaCrashReporter.java`
- `CrashAnalysisResult.java`
- `CrashAnalyzer.java`
- `CrashCaptureException.java`
- `CrashCategory.java`
- `CrashClassifier.java`
- `CrashDetector.java`
- `CrashEnvironment.java`
- `CrashEventListener.java`
- `CrashExportService.java`
- `CrashFingerprint.java`
- `CrashFixCatalog.java`
- `CrashHistoryFilter.java`
- `CrashModuleInfo.java`
- `CrashPattern.java`
- `CrashPatternGrouper.java`
- `CrashPrivacyFilter.java`
- `CrashReport.java`
- `CrashReportBuilder.java`
- `CrashReportCodec.java`
- `CrashReportDetailsScreen.java`
- `CrashReporterBootstrap.java`
- `CrashReporterConfig.java`
- `CrashReporterController.java`
- `CrashReporterScreen.java`
- `CrashReportManager.java`
- `CrashReportRepository.java`
- `CrashReportStatus.java`
- `CrashSeverity.java`
- `CrashSource.java`
- `FileCrashReportRepository.java`
- `SuggestedFix.java`
- `UncaughtExceptionHandlerAdapter.java`

### `Analystics/`

- `ComboAnalysisAfterFight.java`
- `ComboConfig.java`
- `ComboResult.java`
- `ComboSession.java`
- `ComboStats.java`
- `TargetSwitchAnalyzer.java`
- `TargetSwitchConfig.java`
- `TargetSwitchResult.java`
- `TargetSwitchSession.java`
- `TargetSwitchStats.java`

### `animalbreedingcooldowntimer/`

- `AnimalBreedingCooldownTimer.java`
- `AnimalBreedingEntry.java`
- `AnimalBreedingEventHandler.java`
- `AnimalBreedingGui.java`
- `AnimalBreedingHud.java`
- `AnimalBreedingSettings.java`
- `AnimalBreedingStorage.java`
- `AnimalBreedingTracker.java`

### `armorbreakwarning/`

- `ArmorBreakWarning.java`
- `ArmorBreakWarningEventHandler.java`
- `ArmorBreakWarningGui.java`
- `ArmorBreakWarningRenderer.java`
- `ArmorBreakWarningSettings.java`
- `ArmorBreakWarningStorage.java`

### `ArrowCounter/`

- `ArrowCounter.java`
- `ArrowCounterEventHandler.java`
- `ArrowCounterGui.java`
- `ArrowCounterRenderer.java`
- `ArrowCounterScanner.java`
- `ArrowCounterSettings.java`
- `ArrowCounterStorage.java`

### `AutoLoadingServerProfiles/`

- `PropertiesServerProfileStore.java`
- `ServerAddressNormalizer.java`
- `ServerProfileActivationPort.java`
- `ServerProfileAutoLoadController.java`
- `ServerProfileAutoLoadResult.java`
- `ServerProfileBinding.java`
- `ServerProfileMatch.java`
- `ServerProfileMatcher.java`
- `ServerProfileRegistry.java`
- `ServerProfileStore.java`

### `autosave/`

- `AutoSave.java`
- `AutoSaveEventHandler.java`
- `AutoSaveFormatter.java`
- `AutoSaveGui.java`
- `AutoSaveHud.java`
- `AutoSaveSettings.java`
- `AutoSaveState.java`
- `AutoSaveStorage.java`

### `Badges/`

- `AnniversaryBadgeCalculator.java`
- `AnniversaryBadgeGui.java`
- `AnniversaryBadgeSettings.java`
- `AnniversaryBadgeStatus.java`
- `AnniversaryBadgeTier.java`
- `BirthdayCelebrationGui.java`
- `BirthdayCelebrationService.java`
- `BirthdayCelebrationSettings.java`
- `BirthdayConfettiParticle.java`
- `BirthdayConfettiParticleSink.java`
- `PlaytimeBadgeCalculator.java`
- `PlaytimeBadgeGui.java`
- `PlaytimeBadgeService.java`
- `PlaytimeBadgeStatus.java`
- `PlaytimeBadgeTier.java`

### `basestorageledger/`

- `BaseStorageLedger.java`
- `BaseStorageLedgerGui.java`
- `StorageLedgerIndex.java`
- `StorageLedgerItem.java`
- `StorageLowAlert.java`

### `Bestiary/`

- `Bestiary.java`
- `BestiaryBook.java`
- `BestiaryEntry.java`
- `BestiaryKillEventHandler.java`
- `BestiaryTracker.java`

### `BiomeHud/`

- `BiomeHud.java`
- `BiomeHudEventHandler.java`
- `BiomeHudGui.java`
- `BiomeHudRenderer.java`
- `BiomeHudSettings.java`
- `BiomeHudStorage.java`

### `Block Break Ring/`

- `BlockBreakProgressRing.java`
- `BlockBreakRingAnimation.java`
- `BlockBreakRingConfig.java`
- `BlockBreakRingEventHandler.java`
- `BlockBreakRingGui.java`
- `BlockBreakRingRenderer.java`
- `BlockBreakRingSnapshot.java`

### BuildSymmetryGuides/

- BlockCoordinates.java
- BuildSymmetryGuideConfig.java
- BuildSymmetryGuideStorage.java
- BuildSymmetryGuides.java
- BuildSymmetryGuidesEventHandler.java
- BuildSymmetryGuidesGui.java
- BuildSymmetryGuidesRenderer.java
- SymmetryAxis.java
- SymmetryCalculator.java
- SymmetryGuideColor.java
- SymmetryGuidePoint.java
- SymmetryGuideSnapshot.java

### `cases/`

- `AppealCase.java`
- `AppealConflictDetector.java`
- `AppealConflictDetectorGui.java`
- `AppealConflictFinding.java`
- `AppealConflictFormatter.java`
- `AppealConflictPolicy.java`
- `AppealConflictRegistry.java`
- `AppealConflictReport.java`
- `AppealConflictRule.java`
- `AppealConflictService.java`
- `AppealInvolvement.java`
- `AppealInvolvementType.java`
- `BlindCase.java`
- `BlindCaseAnonymizer.java`
- `BlindCaseEvidence.java`
- `BlindCaseReviewFormatter.java`
- `BlindCaseReviewGui.java`
- `BlindCaseReviewService.java`
- `BlindCaseView.java`
- `BlindReviewDecision.java`
- `BlindReviewOutcome.java`
- `BlindReviewSession.java`
- `CaseAgeHighlighter.java`
- `CaseDocumentationScore.java`
- `CaseEscalationTimer.java`
- `CaseEscalationTimerGui.java`
- `CaseMergeApproval.java`
- `CaseMilestoneBoard.java`
- `CaseOwnershipTransfer.java`
- `CasePriority.java`
- `CasePriorityOverride.java`
- `CaseQualitySampler.java`
- `CaseRedactionTool.java`
- `CaseRedactionToolGui.java`
- `CaseSeverityRating.java`
- `CaseSlaMonitor.java`
- `CaseStatus.java`
- `CaseTags.java`
- `CaseTimeline.java`
- `DetectionCaseAutoLinker.java`
- `DetectionCaseAutoLinkerGui.java`
- `DetectionLink.java`
- `DetectionLinkService.java`
- `DetectionLinkStorage.java`
- `DetectionRecord.java`
- `EscalationCase.java`
- `EscalationCaseState.java`
- `EscalationDeadline.java`
- `EscalationFormatter.java`
- `EscalationNotification.java`
- `EscalationNotificationManager.java`
- `EscalationNotificationType.java`
- `EscalationPolicy.java`
- `EscalationStatus.java`
- `EscalationStorage.java`
- `EscalationTracker.java`
- `EvidenceCaseLinker.java`
- `EvidenceCaseLinkerGui.java`
- `EvidenceLink.java`
- `EvidenceLinkService.java`
- `EvidenceLinkStorage.java`
- `ExecutiveCaseFlag.java`
- `GuidedCaseWalkthrough.java`
- `InvestigationCaseExport.java`
- `InvestigationCaseExportGui.java`
- `InvestigationCaseImport.java`
- `InvestigationCaseImportGui.java`
- `InvestigationCaseManager.java`
- `InvestigationCaseManagerGui.java`
- `InvestigationCases.java`
- `InvestigationCaseStorage.java`
- `OverdueCaseAlert.java`
- `PracticeCaseSimulator.java`
- `RedactionDocument.java`
- `RedactionEntry.java`
- `RedactionService.java`
- `RedactionStorage.java`
- `RejectedCaseReview.java`
- `SeniorCaseChecklist.java`
- `SeniorCaseCommandCenter.java`
- `SeniorCaseLock.java`
- `SeniorCaseNotes.java`
- `SeniorCaseQueue.java`
- `SensitiveCaseFlag.java`
- `StaffCaseLock.java`
- `StaffCasePermissions.java`
- `StaffConductCase.java`
- `StaleCaseSweeper.java`

### `ChestItemTotal/`

- `ChestItemTotal.java`
- `ChestItemTotalEntry.java`
- `ChestItemTotalEventHandler.java`
- `ChestItemTotalGui.java`
- `ChestItemTotalRenderer.java`
- `ChestItemTotalScanner.java`
- `ChestItemTotalSettings.java`
- `ChestItemTotalSnapshot.java`
- `ChestItemTotalStorage.java`

### `Clutch/`

- `ClutchHighlight.java`
- `ClutchHighlights.java`
- `ClutchHighlightsConfig.java`
- `ClutchHighlightType.java`

### `Combo/`

- `ComboBreakerConfig.java`
- `ComboBreakerCue.java`

### `Cooldowns/`

- `Cooldowns.java`
- `PearlCooldown.java`

### `Customization/`

- `AlphaThemeManager.java`
- `BasicColorFilters.java`
- `BlockOverlayCustomizer.java`
- `BlockPaletteColorGenerator.java`
- `ColorBlindAccessibilityMode.java`
- `ColorCodePreview.java`
- `ConfigProfiles.java`
- `CustomBlockOutlineColor.java`
- `CustomGuiScale.java`
- `CustomInventoryBackground.java`
- `CustomScoreboard.java`
- `CustomScoreboardStyling.java`
- `DynamicTheme.java`
- `EnchantGlintCustomization.java`
- `EnchantmentGlintSettings.java`
- `GildedLedgerTheme.java`
- `HotbarLoadoutPresets.java`
- `HotbarManager.java`
- `HudAlignmentGuidesAndSnapToGrid.java`
- `OneHandedKeyboardLayout.java`
- `PerPlayerChatColors.java`
- `PerScreenGuiScaling.java`
- `PvpHudEditor.java`
- `RarityBasedTooltipColors.java`
- `ReduceScreenShake.java`
- `SelectableHeartStyles.java`
- `SensitivityProfilesPerItem.java`
- `ServerProfiles.java`
- `ServerProfileSpecificKeybinds.java`
- `ShareableProfiles.java`
- `TransparentHotbar.java`
- `ViewBobbingCustomizer.java`
- `WaterColorBrightnessControls.java`

### `gui/`

- `AlphaModMenu.java`

### `hitbox/`

- `HitboxGeometry.java`
- `HitboxModule.java`
- `HitboxRenderer.java`
- `HitboxSettings.java`

### `Indicators/`

- `BowChargeCalculator.java`
- `BowChargeConfig.java`
- `BowChargeIndicator.java`
- `BowChargeSnapshot.java`
- `KnockbackDirectionIndicator.java`
- `KnockbackDirectionSnapshot.java`
- `KnockbackIndicatorConfig.java`

### `Kitmap & HCF/`

- `AllyFactionNotes.java`
- `AllyFactionNotesModule.java`
- `AllyFactionNotesScreen.java`
- `AllyFactionNotesStore.java`

### `Lifetime Stats Book/`

- `LifetimeStat.java`
- `LifetimeStatsBook.java`
- `LifetimeStatsSnapshot.java`
- `LifetimeStatsTracker.java`

### `LootFeed/`

- `LootFeed.java`
- `LootFeedEntry.java`
- `LootFeedHud.java`
- `LootFeedTracker.java`

### `MinecraftWindowAlphaClient/`

- `AlphaClientJarVersionResolver.java`
- `LwjglWindowTitleTarget.java`
- `MinecraftWindowAlphaClient.java`
- `MinecraftWindowTitleController.java`
- `MinecraftWindowTitleFormatter.java`
- `MinecraftWindowTitleSettings.java`
- `MinecraftWindowTitleTarget.java`

### `OpponentProfiles/`

- `OpponentProfile.java`

### `Pet Memorial/`

- `PetDeathRecorder.java`
- `PetMemorialBook.java`
- `PetMemorialFormatter.java`
- `PetMemorialRecord.java`

### `playerhistory/`

- `PlayerHistory.java`
- `PlayerHistoryEntry.java`
- `PlayerHistoryEvent.java`
- `PlayerHistoryEventHandler.java`
- `PlayerHistoryFormatter.java`
- `PlayerHistoryGui.java`
- `PlayerHistoryStorage.java`
- `PlayerHistoryStore.java`
- `PlayerHistoryTracker.java`

### `Predictions/`

- `ArmorBreakPrediction.java`
- `ArmorBreakPredictionConfig.java`
- `ArmorBreakResult.java`
- `ArmorDurabilityTracker.java`
- `ArmorDurabilityWarningConfig.java`
- `ArmorDurabilityWarningMod.java`
- `ArmorDurabilityWarningPiece.java`
- `ArmorDurabilityWarningRenderer.java`
- `ArmorDurabilityWarningResult.java`
- `ArmorDurabilityWarningScanner.java`
- `ArmorSlot.java`
- `EnderPearlLandingPrediction.java`
- `EnderPearlPredictionConfig.java`
- `EnderPearlPredictionResult.java`
- `EnderPearlTrajectoryPredictor.java`
- `EnderPearlVector.java`

### `punishmentcommandpreflight/`

- `PunishmentCommandDraft.java`
- `PunishmentCommandParser.java`
- `PunishmentCommandPreflight.java`
- `PunishmentCommandPreflightGui.java`
- `PunishmentCommandRule.java`

### `punishmentconflictdetector/`

- `PunishmentConflict.java`
- `PunishmentConflictDetector.java`
- `PunishmentConflictDetectorGui.java`
- `PunishmentRecord.java`
- `PunishmentType.java`

### `Remember Villagers/`

- `remembervillagertrade.java`
- `RememberVillagerTradesGui.java`
- `VillagerTradeCapture.java`
- `VillagerTradeEntry.java`
- `VillagerTradeEventHandler.java`
- `VillagerTradeRecord.java`
- `VillagerTradeRegistry.java`
- `VillagerTradeStorage.java`

### `rememberchestinventory/`

- `ChestItemSnapshot.java`
- `ChestMemoryEntry.java`
- `ChestMemoryManager.java`
- `ChestMemoryScanner.java`
- `ChestMemorySearch.java`
- `ChestMemoryStorage.java`
- `RememberChestInventory.java`
- `RememberChestInventoryGui.java`

### `resourcepackorganizer/`

- `ResourcePackEntry.java`
- `ResourcePackFileManager.java`
- `ResourcePackOrganizer.java`
- `ResourcePackOrganizerGui.java`

### `screenshotcomparison/`

- `ScreenshotComparison.java`
- `ScreenshotComparisonComposer.java`
- `ScreenshotComparisonConfig.java`
- `ScreenshotComparisonImage.java`
- `ScreenshotComparisonImageLoader.java`
- `ScreenshotComparisonMode.java`
- `ScreenshotComparisonPair.java`
- `ScreenshotComparisonScreen.java`
- `ScreenshotComparisonTexture.java`
- `ScreenshotDifferenceAnalyzer.java`
- `ScreenshotDifferenceResult.java`

### `Singleplayer/`

- `EasyCraft.java`
- `EasyCraftCategory.java`
- `EasyCraftCraftingAdapter.java`
- `EasyCraftCraftingTableHook.java`
- `EasyCraftFavorites.java`
- `EasyCraftFavoriteStorage.java`
- `EasyCraftGui.java`
- `EasyCraftRecipe.java`
- `EasyCraftRecipeCatalog.java`
- `EasyCraftTableButton.java`

### `Sounds/`

- `AlertSounds.java`
- `AlertSoundsGui.java`

### `Staff Dashboard Expansion/`

- `ChecklistCategory.java`
- `ChecklistItem.java`
- `CoverageForecastManager.java`
- `CoverageRequirement.java`
- `CoverageResult.java`
- `OverloadedStaffAlert.java`
- `ShiftStartChecklist.java`
- `ShiftStartChecklistGui.java`
- `StaffAttendanceGui.java`
- `StaffAttendanceManager.java`
- `StaffAttendanceOverview.java`
- `StaffAttendanceSession.java`
- `StaffAvailabilityBoard.java`
- `StaffAvailabilityEntry.java`
- `StaffAvailabilityGui.java`
- `StaffAvailabilityStatus.java`
- `StaffCompetency.java`
- `StaffCompetencyAssessment.java`
- `StaffCompetencyGui.java`
- `StaffCompetencyLevel.java`
- `StaffCompetencyManager.java`
- `StaffCompetencyMatrix.java`
- `StaffCoverageForecast.java`
- `StaffCoverageForecastGui.java`
- `StaffDutyManager.java`
- `StaffDutyMode.java`
- `StaffDutyModeGui.java`
- `StaffDutyState.java`
- `StaffGoal.java`
- `StaffGoalBoard.java`
- `StaffGoalBoardGui.java`
- `StaffGoalStatus.java`
- `StaffHandoff.java`
- `StaffHandoffGui.java`
- `StaffHandoffManager.java`
- `StaffHandoffStatus.java`
- `StaffImprovementAction.java`
- `StaffImprovementEvaluation.java`
- `StaffImprovementManager.java`
- `StaffImprovementPlan.java`
- `StaffImprovementPlanGui.java`
- `StaffImprovementStatus.java`
- `StaffPresenceEntry.java`
- `StaffPresenceGui.java`
- `StaffPresenceList.java`
- `StaffPresenceManager.java`
- `StaffQueue.java`
- `StaffQueueEntry.java`
- `StaffQueueGui.java`
- `StaffQueuePriority.java`
- `StaffQueueStatus.java`
- `StaffRankComparison.java`
- `StaffRankComparisonGui.java`
- `StaffRankComparisonModule.java`
- `StaffRankComparisonResult.java`
- `StaffRankProfile.java`
- `StaffResponsibility.java`
- `StaffResponsibilityBoard.java`
- `StaffResponsibilityBoardGui.java`
- `StaffResponsibilityManager.java`
- `StaffResponsibilityPriority.java`
- `StaffResponsibilityStatus.java`
- `StaffReview.java`
- `StaffReviewBoardGui.java`
- `StaffReviewCalendar.java`
- `StaffReviewCalendarGui.java`
- `StaffReviewCalendarManager.java`
- `StaffReviewDayGui.java`
- `StaffReviewEditorGui.java`
- `StaffReviewReminder.java`
- `StaffReviewStatus.java`
- `StaffShift.java`
- `StaffShiftBoard.java`
- `StaffShiftBoardGui.java`
- `StaffShiftManager.java`
- `StaffShiftStatus.java`
- `StaffWorkload.java`
- `WorkloadAlert.java`
- `WorkloadConfig.java`
- `WorkloadLevel.java`
- `WorkloadManager.java`

### `Statistics/`

- `DuelRecord.java`
- `DuelStatisticsConfig.java`
- `DuelStatisticsSnapshot.java`
- `OpponentSpecificDuelStatistics.java`

### `TimeTill/`

- `TimeTillEventHandler.java`
- `TimeTillFormatter.java`
- `TimeTillGui.java`
- `TimeTillRenderer.java`
- `TimeTillSettings.java`
- `TimeTillStorage.java`
- `timetillsunsetornight.java`

### `Timing/`

- `BlockBreakSample.java`
- `BlockBreakTiming.java`
- `BlockBreakTimingConfig.java`
- `BlockBreakTimingStats.java`
- `BlockPlacementSample.java`
- `BlockPlacementTiming.java`
- `BlockPlacementTimingConfig.java`
- `BlockPlacementTimingStats.java`
- `BrewingStandReader.java`
- `BrewingTimer.java`
- `BrewingTimerSettingsScreen.java`
- `CombatTimer.java`

### `togglesneak/`

- `AbstractToggleMovementModule.java`
- `ToggleMovementController.java`
- `ToggleMovementInput.java`
- `ToggleMovementKeyBindings.java`
- `ToggleMovementKeyHandler.java`
- `ToggleMovementKeyState.java`
- `ToggleMovementModule.java`
- `ToggleMovementRuntime.java`
- `ToggleMovementType.java`
- `ToggleSneak.java`
- `ToggleSprint.java`

### `Trackers/`

- `AcknowledgementEntry.java`
- `AcknowledgementGui.java`
- `AcknowledgementRegistry.java`
- `AcknowledgementService.java`
- `AcknowledgementSettings.java`
- `AcknowledgementStatus.java`
- `AcknowledgementTracker.java`
- `AcknowledgementValidator.java`
- `ArrowTrack.java`
- `ArrowTracker.java`
- `ArrowTrackerConfig.java`
- `ArrowTrackerSnapshot.java`

### `Trainers/`

- `AimDriftHeatmap.java`
- `AimTrainerConfig.java`
- `AimTrainerResult.java`
- `AimTrainerSession.java`
- `AimTrainerStats.java`
- `BridgePersonalBest.java`
- `BridgeRun.java`
- `BridgeRunResult.java`
- `BridgeRunStats.java`
- `BridgeSpeedTrainer.java`
- `BridgeTrainerConfig.java`
- `EnderPearlAccuracyPractice.java`
- `PotionThrowCoach.java`
- `PotionThrowCoachConfig.java`
- `PotionThrowCoachPanel.java`
- `PotionThrowCoachStats.java`
- `PotionThrowResult.java`
- `WarmupRoutine.java`
- `WarmUpRoutineRunner.java`
- `WarmUpRoutineRunnerPanel.java`
- `WarmupSession.java`
- `WarmupStats.java`
- `WarmupStep.java`

### `Viewer/`

- `ConnectionFormatter.java`
- `ConnectionSnapshot.java`
- `ConnectionState.java`
- `ConnectionTracker.java`
- `ItemNbtViewer.java`
- `ItemNbtViewerGui.java`
- `NbtTreeBuilder.java`
- `NbtTreeNode.java`
- `PlayerConnectionViewer.java`
- `PlayerConnectionViewerEventHandler.java`
- `PlayerConnectionViewerGui.java`
- `PlayerDirectionFormatter.java`
- `PlayerDirectionSnapshot.java`
- `PlayerDirectionViewer.java`
- `PlayerDirectionViewerEventHandler.java`
- `PlayerDirectionViewerGui.java`

### `Weather-Reactive HUD/`

- `HudWeatherPalette.java`
- `WeatherCondition.java`
- `WeatherPaletteResolver.java`
- `WeatherReactiveHudModel.java`

### `Wrapped/`

- `AnnualAlphaWrappedCalculator.java`
- `AnnualAlphaWrappedCard.java`
- `AnnualAlphaWrappedGui.java`
- `AnnualAlphaWrappedPeriod.java`
- `AnnualAlphaWrappedService.java`
- `AnnualAlphaWrappedSession.java`
- `AnnualAlphaWrappedSettings.java`
- `AnnualAlphaWrappedSummary.java`

### Package root

- `StaffMod.java`

## Remember Chest Inventory

Folder: `rememberchestinventory/`

- **RememberChestInventory.java** — Standalone prototype entry point. Owns the manager, scanner, local storage lifecycle, and GUI opening. It is not registered with the live client module system yet.
- **ChestMemoryEntry.java** — Immutable snapshot of one chest, keyed by dimension and block coordinates, with chest name, observation timestamp, and item summaries.
- **ChestItemSnapshot.java** — Immutable item summary containing registry-style item ID, display name, metadata, and stack count. The Minecraft inventory-to-snapshot adapter still needs implementation.
- **ChestMemoryManager.java** — Maintains a bounded, insertion-ordered chest history, replaces a prior snapshot at the same location, supports clearing, and exposes search results.
- **ChestMemoryScanner.java** — Converts observed item summaries into a detached chest snapshot. It is an adapter boundary; it does not currently read a Minecraft container or chest tile entity.
- **ChestMemorySearch.java** — Searches remembered chest names, dimension/coordinates, item display names, and item IDs.
- **ChestMemoryStorage.java** — Saves and loads snapshots to a local properties file with bounded record counts and text encoding. No remote persistence or Supabase calls are used.
- **RememberChestInventoryGui.java** — Provides a searchable list of remembered chest locations, dimension, observation time, and item-type count, with clear-history and Done actions. It does not yet display each stored item stack or connect to an automatic chest-open event.

**Current prototype status:** The data model, bounded history, search, local persistence, and basic viewer GUI are present. Automatic capture from Minecraft 1.8.9 chest inventories, live module registration, item-level GUI details, and build/runtime verification remain outstanding. Do not describe automatic chest remembering as working until the Forge/container adapter is implemented and tested.

## AutoSave

Folder: `autosave/`

- **AutoSave.java** — Standalone prototype entry point. Owns settings, save-state tracking, local persistence, HUD rendering, GUI opening, and registration of its Forge event adapter. It is not connected to the live AlphaClient module/menu registry.
- **AutoSaveEventHandler.java** — Observes Forge `WorldEvent.Save` events and forwards server-side save observations to the module; also forwards HUD overlay events. The event path has not been runtime-verified against the project's exact Forge build.
- **AutoSaveFormatter.java** — Formats elapsed time since the latest observed world-save event for the HUD.
- **AutoSaveGui.java** — Provides enable/disable, HUD visibility, recent-save display window, scale, and X/Y position controls with a preview.
- **AutoSaveHud.java** — Displays a temporary "World saved" indicator after an observed save event, respecting HUD visibility and debug-screen state.
- **AutoSaveSettings.java** — Holds local display, duration, position, and scale settings with bounds.
- **AutoSaveState.java** — Stores a thread-safe timestamp and world name for the most recently observed save event.
- **AutoSaveStorage.java** — Persists HUD settings locally under `config/alphaclient/autosave.properties`; no remote or Supabase storage is used.

**Current prototype status:** The standalone classes, configurable HUD, settings GUI, and local settings persistence are present. The event adapter is written against Forge's world-save and overlay event APIs, but event delivery, actual singleplayer autosave timing, GUI navigation, and HUD rendering have not been compiled or runtime-tested. The prototype is not yet registered in the normal AlphaClient module interface. It reports an observed save event; it does not control Minecraft's save interval or force a save.

## Animal Breeding Cooldown Timer

Folder: `animalbreedingcooldowntimer/`

- **AnimalBreedingCooldownTimer.java** — Standalone prototype entry point. Owns settings, per-animal tracking, local persistence, HUD registration, and GUI opening. It is not registered in the live AlphaClient module/menu system.
- **AnimalBreedingEntry.java** — Immutable record for one animal, including UUID, display name, last successful breeding timestamp, configured cooldown, and remaining-time calculation.
- **AnimalBreedingEventHandler.java** — Defines the callback boundary for a confirmed successful breeding event. It deliberately does not infer breeding from nearby animals or a baby spawn.
- **AnimalBreedingGui.java** — Provides module/HUD toggles, configurable cooldown duration, a tracked-animal overview, and a clear-history action.
- **AnimalBreedingHud.java** — Displays the tracked animals and their remaining cooldown or Ready state.
- **AnimalBreedingSettings.java** — Holds local enable, HUD, interval, and position settings. The 60-second interval is a prototype default inherited from the original idea stub, not a verified vanilla Minecraft cooldown.
- **AnimalBreedingStorage.java** — Persists settings and tracked-animal snapshots locally in a properties file. No remote persistence or Supabase calls are used.
- **AnimalBreedingTracker.java** — Maintains a bounded registry of per-animal cooldown entries.

**Current prototype status:** The data model, per-animal timer calculations, local settings/history persistence, basic GUI, and HUD renderer are present in source. Successful breeding detection is not connected to a verified Minecraft 1.8.9 / Forge 11.15.1.2318 hook; the callback must be invoked only after the game confirms a successful breeding event. Module/menu registration, compile verification, and runtime testing remain outstanding. Do not describe automatic breeding detection or cooldown tracking in-game as working until that hook is implemented and tested.

### Inventory notes

- `ArrowCounter/`, `BiomeHud/`, `ChestItemTotal/`, `Remember Villagers/`, and `TimeTill/` are included because their Java prototype files are present in this snapshot.
- The current manifest is authoritative for file presence. Older explanatory sections above may describe earlier prototype states; if a folder or file is absent from this manifest, it is not present in this snapshot.
- `BlockPlacementPreview/` and `Aimassist Expansion/` are not present in this snapshot and are therefore not counted.


## Alpha Clans

Folder: `AlphaClans/` (a inside `Prototypes/`).

- **AlphaClans.java** — Standalone entry point that creates the clan service and UI-ready screen model using a caller-supplied local storage path.
- **Clan.java / ClanMember.java / ClanRole.java / ClanPermission.java** — Clan identity, membership, owner/officer/member roles, and permission model.
- **ClanInvite.java / ClanChatMessage.java** — Expiring invitations and bounded clan chat records.
- **ClanService.java** — Clan lifecycle and authorization operations: create, invite, accept/decline, leave, kick, promote/demote, transfer ownership, rename, change tag, disband, and chat.
- **ClanCommand.java / ClanCommandParser.java / ClanCommandHandler.java** — Tokenized command model, quoted argument parsing, and command dispatch. Multi-word clan names are accepted by `/clan create <name> <tag>`.
- **ClanPlayerDirectory.java** — Integration boundary for resolving Minecraft player names to UUIDs; a Minecraft-backed adapter is still required.
- **ClanRepository.java / FileClanRepository.java** — Local-only versioned binary persistence with bounded record counts and atomic replacement.
- **ClanScreenModel.java** — Immutable UI snapshot model for clan details, members, chat, and pending invitations.

**Current status:** The standalone clan domain, command parsing/dispatch, UI data model, and local persistence are present. Storage loading and saving now reject invalid owner/member/invitation relationships and invalid record bounds; chat messages are capped at 256 characters. The original idea stub was moved into this standalone module and is no longer kept under `staffops/ideas/`.

**Still missing / not verified:** A rendered Minecraft `GuiScreen`, command registration with Minecraft, a Minecraft player-directory adapter, server-authoritative persistence and cross-player synchronization, and AlphaClient module/menu wiring. No Supabase integration is used. Compile and runtime behavior have not been verified by this inventory.


## AlphaCoins — Playtime Rewards

Folder: `AlphaCoins/alphacoinsplaytime/`

- **AlphaCoinsPlaytime.java** — Tracks cumulative active playtime per Alpha profile, loads/saves the reward state, and credits due rewards through `AlphaCoinsCore.getInstance().getService().addCoins(...)`. It does not create a separate wallet.
- **PlaytimeRewardDefinition.java** — Defines the recurring 24-hour and 10-day rewards plus the one-time 1-, 3-, 6-, 9-, and 12-month milestones.
- **PlaytimeRewardStatus.java** — Immutable progress data for a reward row, including progress, remaining time, completed intervals, and earned state.
- **PlaytimeRewardStorage.java** — Versioned local properties storage for cumulative playtime and already-issued reward intervals/milestones.
- **AlphaCoinsPlaytimeEventHandler.java** — Forge client-tick adapter. It counts elapsed time only while a player and world are present; a one-second delta cap prevents long client stalls from being counted as playtime.
- **AlphaCoinsPlaytimeModule.java** — Explicit register/unregister lifecycle wrapper. It is not wired into AlphaClient Core or the live module menu.

**Reward rules:** 5 AlphaCoins for every completed 24 hours; 50 AlphaCoins for every completed 10-day interval; one-time milestone rewards of 100 (30 days), 300 (90 days), 800 (180 days), 1,200 (270 days), and 2,400 (365 days). Month and year durations are represented as fixed 30-day and 365-day playtime periods.

**Current status:** The standalone service, reward definitions, progress model, local storage, Forge tick adapter, and explicit lifecycle wrapper are present on `mod-ideas`. The original `staffops/ideas/AlphaCoinsPlaytime.java` source file is retained unchanged. No AlphaClient Core, `development`, Alpha Launcher, or Supabase files were changed.

**Still missing / not verified:** Registration from an approved AlphaClient integration point, a rendered GUI/menu entry, pause-menu behavior, compile verification, and runtime verification. Reward state and wallet state are stored in separate local files, so the prototype does not provide an atomic transaction across both files if the process crashes between wallet credit and reward-state persistence.


## Case Escalation Timer

Folder: `cases/caseescalationtimer/`.

- **CaseEscalationTimer.java** — Standalone module facade. Owns the tracker, caller-supplied warning policy, local notification manager, and storage adapter; it is not registered in the live AlphaClient module system.
- **EscalationCase.java** — Immutable case timer record with ID, title, optional reference, opening/deadline timestamps, state, and pause accounting. Pausing and resuming shifts the deadline by the paused duration.
- **EscalationCaseState.java** — Defines the OPEN, PAUSED, and RESOLVED lifecycle states.
- **EscalationDeadline.java** — Creates deadlines from an explicit positive duration and calculates remaining time.
- **EscalationStatus.java** — Represents no deadline, open, warning, due, overdue, paused, and resolved states.
- **EscalationPolicy.java** — Holds a caller-supplied warning window. It defaults to no warning window; no Alpha escalation SLA or warning threshold is assumed.
- **EscalationTracker.java** — Maintains the in-memory case registry and supports add, replace, remove, lookup, and snapshot operations.
- **EscalationNotificationType.java** — Defines local warning, deadline-reached, and overdue notification event types.
- **EscalationNotification.java** — Immutable local notification event data; it does not send chat messages or server commands.
- **EscalationNotificationManager.java** — Emits one local event when a case enters a warning, due, or overdue state.
- **EscalationFormatter.java** — Calculates deadline status and formats remaining/overdue time.
- **EscalationStorage.java** — Loads and saves case records in a local JSON file. No remote persistence or Supabase calls are used.
- **CaseEscalationTimerGui.java** — Standalone GUI to add cases with a deadline in hours, view their status/countdown, and pause or resume timers. Changes must be explicitly saved.

**Current status:** The prototype data model, configurable warning window, deadline calculations, pause/resume handling, local notification events, local JSON persistence, and basic GUI are present in source. The warning window is disabled by default because no approved Alpha threshold was supplied.

**Still missing / not verified:** The prototype does not fetch case data, assign staff, define Alpha escalation rules, deliver native toast/chat notifications, or connect to a server. Resolved state can be set through the API but is not yet exposed as a GUI action. No rank/permission checks, StaffMods registration, or live module integration are included. No Supabase integration is used. Compilation and Minecraft 1.8.9 / Forge 11.15.1.2318 runtime behavior have not been verified.


## Minecraft Window AlphaClient

Folder: `MinecraftWindowAlphaClient/` (a inside `Prototypes/`).

- **MinecraftWindowAlphaClient.java** — Standalone prototype entry point. Resolves the installed AlphaClient JAR version and asks the controller to apply the formatted title. It is not registered with AlphaClient Core or the live module menu.
- **AlphaClientJarVersionResolver.java** — Reads the AlphaClient version from package implementation metadata, then falls back to the running JAR manifest. If neither source provides a version, it returns `Unknown`; it does not invent or hardcode a release number.
- **MinecraftWindowTitleFormatter.java** — Formats the requested title as `AlphaClient <JAR version> + Forge 1.8.9`. The `1.8.9` suffix is the requested Minecraft/Forge target label; no separate Forge build number is shown.
- **MinecraftWindowTitleController.java** — Applies the title only when enabled and avoids sending the same title repeatedly.
- **MinecraftWindowTitleSettings.java** — Holds the local enabled setting. A settings GUI and persistence are not included.
- **MinecraftWindowTitleTarget.java** — Defines the boundary for setting the native window title.
- **LwjglWindowTitleTarget.java** — Uses LWJGL 2 `Display.setTitle` when the display has been created.

**Current status:** The standalone title formatter, JAR-version lookup, enable setting, controller, and LWJGL 2 target adapter are present in source. The intended example is `AlphaClient 0.5.XX + Forge 1.8.9`, with the AlphaClient version read from the actual installed JAR metadata.

**Still missing / not verified:** Automatic invocation at the correct Minecraft display lifecycle point, module/menu registration, settings persistence or GUI, and compile/runtime verification. The prototype has not been built or tested in Minecraft 1.8.9 / Forge 11.15.1.2318. The version resolver returns `Unknown` if the packaged JAR does not contain readable version metadata. No AlphaClient Core, `development`, Alpha Launcher, or Supabase integration is included.


## ToggleSneak & ToggleSprint

Folder: `togglesneak/` (a inside `Prototypes/`).

- **ToggleSneak.java** — Holds local enabled/toggled state for sneak. It starts enabled but not toggled; disabling the module clears its active state.
- **ToggleSprint.java** — Holds local enabled/toggled state for sprint. It starts enabled but not toggled; disabling the module clears its active state.
- **ToggleMovementKeyBindings.java** — Registers separate user-configurable activation keybindings. Both default to unbound so the prototype does not take over an existing control.
- **ToggleMovementKeyHandler.java** — Consumes the activation key presses on client ticks and applies the states to Minecraft's existing vanilla sneak/sprint key bindings. It preserves physical keyboard/mouse input when a toggle is off, and releases movement keys while a GUI is open or the game is unfocused.
- **ToggleMovementRuntime.java** — Owns the two state models and event handler with explicit start/stop methods. It is not started automatically by AlphaClient Core.
- **assets/alphaclient/lang/en_US.lang** — Supplies the movement category and keybinding labels.

**Current status:** The standalone state models, configurable activation bindings, client-tick adapter, explicit runtime lifecycle, and English keybinding labels are present in source. Both modules start enabled with their toggled movement state off; assign activation keys in Options → Controls.

**Still missing / not verified:** The runtime is not wired into a client lifecycle or module menu, and no settings GUI or persistence is included. Forge compilation and Minecraft 1.8.9 in-game behavior have not been verified. No AlphaClient Core, `development`, Alpha Launcher, or Supabase integration is included.


## Build Symmetry Guides

Folder: `src/main/java/com/alpharip/staffmod/Prototypes/BuildSymmetryGuides/`.

- **BuildSymmetryGuides.java** — Standalone module facade with explicit Forge event registration/unregistration, target snapshot ownership, local settings loading/saving, and a settings-screen entry point. It is not registered automatically by AlphaClient Core.
- **BuildSymmetryGuideConfig.java** — Holds the enable state, X/Z/BOTH mirror-plane selection, origin coordinates, preview toggles, guide distance, opacity, and color.
- **SymmetryAxis.java** — Defines reflection across the X plane, Z plane, or both planes.
- **BlockCoordinates.java** — Immutable integer block-coordinate value object.
- **SymmetryGuidePoint.java** — Represents one reflected destination and its reflection label.
- **SymmetryCalculator.java** — Calculates X, Z, and combined reflections around the center of the configured origin block. It preserves Y and filters source-position and duplicate destinations; it never changes the world.
- **SymmetryGuideSnapshot.java** — Provides immutable render-ready state for the current targeted block.
- **SymmetryGuideColor.java** — Defines the local overlay color palette.
- **BuildSymmetryGuidesEventHandler.java** — Samples the targeted block on client ticks and delegates world rendering through Forge events.
- **BuildSymmetryGuidesRenderer.java** — Draws horizontal mirror-plane guide lines, source-to-mirror connection lines, and wireframe outlines at reflected destination coordinates.
- **BuildSymmetryGuidesGui.java** — Provides controls for enablement, mirror plane, preview, axis and connection lines, guide distance, opacity, color, and precise one-block X/Z origin adjustment or setting the origin to the player's current position.
- **BuildSymmetryGuideStorage.java** — Loads and saves settings in a local properties file under the Minecraft config directory.

**Mirror behavior:** X reflects the X coordinate, Z reflects the Z coordinate, and BOTH calculates the X reflection, Z reflection, and combined reflection. The mirror planes pass through the center of the configured origin block. Y is unchanged. The renderer only draws guides; it does not place or modify blocks.

**Current status:** The standalone calculation model, immutable snapshot, event adapter, world overlay, settings GUI with one-block origin adjustment and connection-line controls, and local properties persistence are present in source. The original staffops/ideas/BuildSymmetryGuides.java idea stub is removed after moving its concept into this prototype folder.

**Still missing / not verified:** AlphaClient Core/module-menu integration, an approved way to open the settings GUI, compilation against the exact ForgeGradle setup, and Minecraft 1.8.9 runtime/render verification. No Supabase, Alpha Launcher, or development changes are included.


## Fishing Bite Alarm

Folder: `src/main/java/com/alpharip/staffmod/Prototypes/FishingBiteAlarm/`.

- **FishingBiteAlarm.java** — Standalone module facade with explicit Forge event registration/unregistration, settings ownership, local persistence, and a settings-screen entry point. It is not started or registered automatically by AlphaClient Core.
- **FishingBiteAlarmSettings.java** — Holds the enable state, minimum alert interval, and sound volume with validated bounds. The original 60-second interval default is retained.
- **FishingHookSample.java** — Captures an immutable snapshot of the local player's active fishing hook, including entity identity, Y position, vertical motion, and water state.
- **FishingBiteDetector.java** — Detects a sharp downward bobber-motion impulse while the hook is in water. This is a client-side heuristic, not a server-confirmed bite event.
- **FishingBiteAlarmEventHandler.java** — Samples the local player's fishing hook at the end of client ticks and resets detection when no valid hook is active.
- **FishingBiteNotifier.java** — Plays the vanilla `random.orb` sound locally and enforces the configured minimum interval between alerts.
- **FishingBiteAlarmGui.java** — Exposes enablement, alert interval, volume, test sound, save, and return controls.
- **FishingBiteAlarmStorage.java** — Loads and saves settings locally under `config/alphaclient/fishing_bite_alarm.properties`.

**Interval interpretation:** The original `staffops/ideas/FishingBiteAlarm.java` contained only `enabled = true` and `intervalSeconds = 60`; it did not define the interval's meaning. This prototype interprets it as the minimum time between alarm notifications and preserves the 60-second default.

**Current status:** The standalone source structure, client-tick adapter, motion-based detector, local sound notifier, settings GUI, and local properties persistence are present. The original idea file is removed after moving its concept into this prototype.

**Still missing / not verified:** AlphaClient Core/module-menu registration, an approved GUI access route, confirmation that the motion heuristic detects real fishing bites reliably, compilation against the exact ForgeGradle setup, and Minecraft 1.8.9 in-game verification. No Supabase, Alpha Launcher, or `development` changes are included.


## Chunk Borders

Folder: `src/main/java/com/alpharip/staffmod/Prototypes/ChunkBorders/`

- **ChunkBorders.java** — Standalone module facade with explicit Forge event registration/unregistration, settings access, GUI entry point, and local persistence. It is not automatically registered or enabled by AlphaClient Core.
- **ChunkBordersSettings.java** — Holds the disabled-by-default state, preserved default style, chunk render radius, vertical/horizontal line toggles, current-chunk highlight toggle, and opacity with bounded setters.
- **ChunkBorderStyle.java** — Defines the supported visual styles and their render colors/line widths.
- **ChunkBordersSnapshot.java** — Immutable render-time copy of the active settings and player chunk coordinates.
- **ChunkBordersRenderer.java** — Draws chunk grid boundaries and vertical corner pillars in the world render pass, using camera-relative coordinates and restoring the OpenGL state it changes.
- **ChunkBordersEventHandler.java** — Receives Forge world-render events and delegates rendering only while the standalone module is running and enabled.
- **ChunkBordersGui.java** — Exposes module enablement, style, radius, vertical/horizontal lines, current-chunk highlight, opacity, save, and return controls.
- **ChunkBordersStorage.java** — Loads and saves settings locally under `config/alphaclient/chunk_borders.properties`.

**Preserved defaults:** The original `staffops/ideas/ChunkBorders.java` had `enabled = false`, `category = "World/diagnostics"`, and `style = "default"`. The disabled state and default style are retained. The category remains descriptive documentation; this standalone prototype does not register itself into the production World category.

**Current status:** The standalone settings model, style definitions, render snapshot, Forge world-render adapter, OpenGL chunk-boundary renderer, settings GUI, and local properties persistence are present.

**Still missing / not verified:** AlphaClient Core/module-menu registration and an approved GUI access route. Compilation against the exact ForgeGradle setup, rendering behavior in Minecraft 1.8.9, OpenGL state interaction with other overlays, performance at larger radii, GUI behavior at different resolutions, and settings save/load round trips have not been verified. No Supabase, Alpha Launcher, or `development` changes are included.


## Builder Grid & Ruler Overlay

Folder: `src/main/java/com/alpharip/staffmod/Prototypes/BuilderGridAndRulerOverlay/`

- **BuilderGridAndRulerOverlay.java** — Standalone module facade with explicit Forge event lifecycle, settings access, anchor management, snap calculation, and GUI entry point. It is not automatically registered or enabled by AlphaClient Core.
- **BuilderGridSettings.java** — Holds the independent grid/ruler toggles, grid spacing, and visible build-area radius. The original defaults (both overlays enabled and spacing 1) are preserved.
- **BuilderGridAnchor.java** — Stores the world-coordinate origin used to position the guide area and measure offsets.
- **BuilderGridSnapshot.java** — Immutable render-time copy of the anchor, player block position, and active settings.
- **BuilderGridCalculator.java** — Calculates grid-aligned world coordinates using the original nearest-multiple snapping rule and generates bounded grid-line coordinates.
- **BuilderGridRenderer.java** — Renders the horizontal build grid and X/Z ruler axes with per-block tick marks, using camera-relative coordinates and restoring the OpenGL state it changes.
- **BuilderGridRulerHudRenderer.java** — Displays the current X/Z offset from the saved anchor and the active grid spacing while the ruler is enabled.
- **BuilderGridEventHandler.java** — Receives Forge world-render and HUD overlay events and delegates only while the standalone module is running.
- **BuilderGridGui.java** — Provides independent grid/ruler toggles, spacing and area-radius controls, anchor capture, save, and return navigation.
- **BuilderGridStorage.java** — Loads and saves local settings and anchor coordinates under `config/alphaclient/builder_grid_ruler.properties`.

**Preserved defaults:** The original idea used `gridEnabled = true`, `rulerEnabled = true`, and `gridSpacing = 1`. All three defaults are retained. The original spacing clamp of 1–64 and nearest-multiple snap behavior are retained.

**Current status:** The standalone settings model, saved world-coordinate anchor, grid snapping/calculation, world grid and ruler rendering, ruler offset HUD, settings GUI, and local properties persistence are present.

**Still missing / not verified:** AlphaClient Core/module-menu registration and an approved GUI access route. Compilation against the exact ForgeGradle setup, rendering behavior in Minecraft 1.8.9, OpenGL state interaction with other overlays, measurement appearance at different angles/heights, GUI behavior at different resolutions, and settings save/load round trips have not been verified. No Supabase, Alpha Launcher, or `development` changes are included.

## Risk Mitigation Board

Folder: `RiskMitigationBoard/`

- **RiskCategory.java**, **RiskSeverity.java**, and **RiskStatus.java** — Define the supported risk classifications, severity levels, and lifecycle states.
- **RiskRecord.java** — Holds the risk description, owner, due date, timestamps, status, mitigation actions, and notes.
- **RiskMitigationAction.java** and **RiskNote.java** — Represent actionable mitigation work and timestamped staff notes.
- **RiskBoard.java** — Maintains the in-memory risk register and exposes record snapshots.
- **RiskBoardService.java** — Provides validated create/update/status/action/note operations, filtering, and dashboard summary calculations.
- **RiskBoardFilter.java** and **RiskBoardSort.java** — Support filtering by status, severity, category, owner, and text, plus severity/date sorting.
- **RiskBoardSummary.java** — Exposes total, lifecycle, critical, overdue, and incomplete-action counts.
- **RiskBoardStorage.java** — Saves and loads a versioned JSON snapshot locally. No remote service or Supabase integration is included.
- **RiskBoardExport.java** — Exports a local CSV snapshot for manual review.
- **RiskBoardAccess.java** and **RiskBoardController.java** — Define the authorization boundary and access-checked controller. A real adapter to the established StaffMods authorization layer is still required; this prototype does not grant permissions.
- **RiskMitigationBoardGui.java** — Provides the first Alpha-styled in-game risk-register screen with record creation, summary counts, status cycling, and removal.

**Current status:** The standalone data model, in-memory register, service operations, filtering/sorting helpers, summary model, local JSON persistence, CSV export, access boundary, controller, and first GUI screen are present.

**Still missing / not verified:** Full GUI editing for category/severity/due dates, mitigation-action and note management controls, a concrete StaffMods authorization adapter and approved dashboard entry, production-safe persistence recovery, compilation against the exact ForgeGradle setup, and in-game behavior. No AlphaClient Core or `development` integration is included.


## Appeal Review Workspace

Folder: `Prototypes/AppealReviewWorkspace/`

- **AppealCase.java**, **AppealStatus.java**, and **AppealDecisionType.java** — Define an appeal record, its lifecycle, and decision actions.
- **AppealEvidence.java** — Stores evidence labels and references as metadata only; it does not copy, upload, or verify evidence files.
- **AppealReviewNote.java** — Stores reviewer notes with author and timestamp.
- **AppealReviewerAssignment.java** — Represents reviewer assignment history.
- **AppealDecisionRecord.java** — Records decision type, reviewer, rationale, and timestamp.
- **AppealEscalationRecord.java** — Records escalation reason, actor, target, and timestamp.
- **AppealReopenRequest.java** — Tracks reopen requests and their pending/accepted/rejected state.
- **AppealReviewFilter.java** and **AppealReviewSort.java** — Define queue filters and ordering.
- **AppealReviewSummary.java** — Exposes queue totals by workflow status.
- **AppealReviewBoard.java** — Manages appeal records and related evidence, notes, decisions, assignments, escalations, and reopen requests in memory.
- **AppealReviewService.java** — Provides submission, assignment, status changes, evidence/note entry, decisions, escalation, reopen-request handling, filtering, and summary operations.
- **AppealReviewStorage.java** — Saves and loads a versioned local JSON snapshot. Only base appeal records are persisted; related records remain memory-only.
- **AppealReviewAccess.java** — Defines an authorization adapter boundary. It does not grant ranks or permissions; connect it to the established StaffMods authorization layer before production use.
- **AppealReviewController.java** — Coordinates the board, service, local storage, and supplied access adapter.
- **AppealReviewWorkspaceGui.java** — Provides an initial in-game queue with submission, status cycling, removal, and summary counts. Dedicated evidence, notes, reviewer assignment, decision, escalation, and reopen-request screens are not exposed yet.

The prototype is local-only: no Supabase calls, remote persistence, server integration, or automatic appeal ingestion are included. Compilation, production integration, permission enforcement through the real StaffMods layer, persistence recovery, and in-game behavior remain unverified.
