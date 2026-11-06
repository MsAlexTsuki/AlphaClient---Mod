# Alpha StaffMod Prototype Implementation Guide

This document is the implementation checklist for **every Java file currently located beside `staffops/`** under this package on branch `mod-ideas`.

- Scope inspected: `src/main/java/com/alpharip/staffmod/` outside `staffops/`.
- Inventory at creation: **195 Java files** across the folders listed below.
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
