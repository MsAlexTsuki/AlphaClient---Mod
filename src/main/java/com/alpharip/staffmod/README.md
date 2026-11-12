# Alpha StaffMod Prototype Implementation Guide

This document is the implementation checklist for **every Java file currently located beside `staffops/`** under this package on branch `mod-ideas`.

- Scope inspected: `src/main/java/com/alpharip/staffmod/` outside `staffops/`.
- Inventory at creation: **210 Java files** across the folders listed below.
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

**Integration boundary:** This is an independent prototype folder beside `staffops/`. It does not modify the existing `development` AimAssist, AlphaClient Core, rank permissions, or Alpha Launcher. No Supabase, remote persistence, attacks, or packet sending are included. Compile and in-game behavior have not been verified.


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

**Prototype boundary:** This is an independent Singleplayer prototype folder beside `staffops/`. It does not change AlphaClient Core, `development`, the normal player inventory GUI, Alpha Launcher, rank/permission rules, or Supabase. The book button is a hook component, not yet injected into the live Crafting Table screen. The adapter that safely transfers ingredients through the vanilla `ContainerWorkbench` and performs the requested craft is not implemented yet; the Craft button delegates to that adapter contract. The recipe catalog reads registered vanilla/Forge recipes, but custom recipe ingredient extraction may need additional adapters. Favorites are local-only. No compile, automated test, or in-game test is claimed because this branch contains no build configuration in the inspected tree and no runtime test was run.

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

