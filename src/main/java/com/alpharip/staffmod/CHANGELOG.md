# AlphaClient — Complete Branch Changelog

**Branch:** `mod-ideas`  
**Repository:** [MsAlexTsuki/AlphaClient---Mod](https://github.com/MsAlexTsuki/AlphaClient---Mod/tree/mod-ideas)  
**Branch snapshot:** 28 November 2026  
**Scope:** The entire branch. This changelog covers AlphaClient code, client UI and infrastructure, StaffMods, StaffOps, standalone prototypes, the idea-source catalogue, and repository documentation. It is not limited to one folder.

This is a user-facing feature changelog. Related commits and repeated implementation steps are consolidated into feature descriptions instead of being repeated as Git commit subjects. Source presence is not presented as proof of production integration or successful testing.

## Status guide

- **Client implementation:** functionality is present in the AlphaClient client source.
- **Standalone prototype:** source and supporting components exist in `Prototypes/`; this does not mean the feature is connected to the playable client.
- **StaffOps done collection:** source is stored in `staffops/done/`; the folder name does not by itself certify production readiness.
- **Idea source:** an idea entry or supporting source exists in `staffops/ideas/`; it is not described as an implemented client feature.
- **Verification:** no build or in-game test is claimed unless explicitly recorded.

---

# 1. AlphaClient core and in-game experience

## Client foundation and module architecture

- Established the AlphaClient Forge mod entry point and client bootstrap, replacing the former StaffMod bootstrap while retaining compatibility bridges where required.
- Added a central module model, module registration, module categories, module state handling and persistent module configuration.
- Added per-feature permission checks and separate public, Beta and Donator entitlement tiers.
- Added module detail views and rank-gated feature information.
- Added a central HUD manager and modular HUD widget contract so HUD elements can be managed independently.
- Connected HUD indicators to the HUD editor and added saved HUD position and scale settings.
- Added startup loading for saved module settings and restoration of saved module toggle states.
- Added the public Keystrokes overlay and connected it to the module system.
- Connected Bhop to StaffMods rather than retaining it as a separate legacy menu entry. Bhop visibility is controlled by its individual entitlement.
- Retained Minecraft's hide-HUD behavior for Alpha HUD rendering.

## Main menu and visual identity

- Replaced the vanilla main-menu presentation with an AlphaClient-branded main menu.
- Added the Alpha logo and custom button styling, including restrained Alpha-yellow accents.
- Reworked the menu layout around a compact, centered presentation.
- Added an animated panorama-based background and corrected logo texture sizing, scaling and aspect ratio.
- Removed the vanilla branding elements where the AlphaClient menu replaces them.
- Added a compact account dropdown and account guidance screen.
- Added a compact in-game module menu with module icons, search, category navigation and rank status.
- Added a dedicated menu keybind to open the AlphaClient menu.
- Refined the menu layout for supported screen resolutions and preserved the Minecraft world backdrop where applicable.

## Settings and HUD controls

- Added the AlphaClient settings hub and routed main-menu settings into it.
- Added HUD scale controls for the retained Bhop HUD.
- Moved HUD controls out of the general settings destination into their dedicated entry point.
- Added modular HUD rendering and widget registration.
- Added persistence for module toggles and module settings across restarts.
- Added support for resetting individual module settings in the wider source catalogue.

## Community login, ranks and access

- Added in-client Alpha Community login backed by the existing community authentication flow.
- Added community rank sign-in and exposed the verified community rank label to the login interface.
- Updated the module menu to show the signed-in user's rank and a No Rank state when no community account is signed in.
- Added rank-filtered StaffMods navigation and category access.
- Added per-feature entitlement checks that fail closed when access cannot be established.
- Kept Beta and Donator entitlements separate.
- Added safeguards for stale rank state during sign-in and cleared password input after sign-in submission.
- Added permission labels and rank-gated feature information in module details.
- The branch contains additional rank, permission and account-related source ideas; those are listed separately under the idea-source catalogue and are not represented as live account features here.

## Client presentation and quality improvements

- Added AlphaClient-specific branding and metadata.
- Added compact, resolution-aware controls and shared button styling.
- Improved module search behavior so the search query remains available while module tiles are filtered.
- Added a clearer separation between public General modules and restricted StaffMods collections.
- Added supporting client diagnostics, HUD, rendering and compatibility source throughout the branch.

---

# 2. StaffOps — source collected under `staffops/done/`

The following modules have source folders in the done collection. Their presence is recorded here as repository work; this list does not claim that every module is wired into the production client or has passed in-game testing.

## Gameplay, PvP and tracking

- **AimAssist Expansion** — expanded aim-assist source, controller and configuration structure.
- **AimAssist** — separate aim-assist source and settings.
- **AnvilCost** — anvil-cost warning and calculation source.
- **AutoTool** — automatic tool-selection source, settings and GUI.
- **BlockPlacementPreview** — block-placement preview, command, settings and GUI source.
- **CPSMod** — clicks-per-second tracking.
- **ComboCounter** — combo tracking and its settings interface.
- **Fight History** — opponent/fight history source.
- **FPS** — FPS display source.
- **Ping** — ping display source.
- **WayPoints** — waypoint management, death waypoints and event countdown-related source.
- **WinLossTracker** — win/loss tracking source.

## Building, inventory and recording

- **BuildTimelapse** — building activity capture and timelapse-related source.
- **InventorySearch** — inventory, chest and Ender Chest search interfaces.
- **RecordingStudio** — recording and camera components, export workflow and export settings.
- **CommandPalette** — searchable command/action palette and supporting local data.
- **EnchantingName** — enchanting-name display source.
- **PixelGarden** — Pixel Garden source, commands, animations and related coin components.

## Store, cosmetics and client support

- **Store** — Store interface and supporting catalogue components, including the AlphaLauncher Store documentation area.
- **Developer** — developer diagnostics and debug-overlay source.
- **EasterEggs** — Easter egg configuration and management source.
- **GUI** — shared GUI and account-related interface components.
- **HUD** — HUD-related components.
- **Config** — client configuration and HUD position/scale support.
- **Modules** — module-system supporting source.
- **Movement** — movement-related supporting source.
- **Network** — network-related supporting source.
- **Optimization** — optimization-related supporting source.
- **Permissions** — permission-related supporting source.
- **Profile** — profile-related supporting source.
- **PvP** — PvP-related supporting source.
- **Quality** — quality-related supporting source.
- **Ranks** — rank-related supporting source.
- **RankViewer** — rank-viewing source.

---

# 3. Standalone Java prototypes — `Prototypes/`

The branch contains a separate prototype collection with **717 Java files across 67 top-level prototype folders**. These are standalone source projects and are not automatically part of the playable AlphaClient module system.

## Building, world interaction and singleplayer

- **BuildSymmetryGuides** — X-plane, Z-plane and combined X/Z mirror previews with configurable guides, colors, opacity and origin controls. It previews placements; it does not place or modify blocks.
- **BuilderGridAndRulerOverlay** — configurable world-space building grid, X/Z measurement guides, distance ticks, X/Y/Z ruler HUD, adjustable spacing and guide radius, origin capture and coordinate snapping.
- **ChunkBorders** — chunk boundary lines, vertical corner pillars, current-chunk highlighting, visual styles, render distance and opacity settings.
- **FishingBiteAlarm** — fishing-hook movement detection, configurable sound alert, alert interval, volume and test sound.
- **AdjustThirdPerson** — third-person camera distance settings and options-screen prototype hooks.
- **AdjustableRainAndSnowSpeed** — separate rain and snow speed controls.
- **AdjustableStarDensity** — configurable star density and visual settings.
- **BiomeHud** — biome information display.
- **AirHud** — remaining-air display and underwater-time estimate.
- **Adventure Diary** — adventure/session diary source.
- **Bestiary** — mob kill tracking and bestiary records.
- **Pet Memorial** — pet-death records and memorial-book source.
- **Remember Villagers** — villager trade capture and trade records.
- **ChestItemTotal** — chest item counting and entry tracking.
- **BaseStorageLedger** — storage indexing and searchable storage records.
- **RememberChestInventory** — chest inventory memory source.
- **ResourcePackOrganizer** — resource-pack catalogue and organization.
- **Singleplayer** — singleplayer utility collection, including Easy Craft recipe catalogue, categories, search and favorites.
- **AnimalBreedingCooldownTimer** — animal breeding records and cooldown tracking.
- **Kitmap & HCF** — faction ally-note source.
- **AutoLoadingServerProfiles** — server-address normalization, profile matching, registry, activation controller and storage.
- **World/building support** — additional source for block guides, placement, coordinates, world state and construction utilities.

## PvP, combat, training and statistics

- **Aim Pattern Library** — aim observations, pattern grouping and analysis.
- **ArrowCounter** — arrow tracking and HUD source.
- **Clutch** — clutch highlight records and event categories.
- **Combo** — combo-break cue configuration and state.
- **Cooldowns** — shared cooldown tracking and Ender Pearl cooldown source.
- **Indicators/Bow** — bow draw calculation, charge percentage and readiness state.
- **Indicators/Knockback** — knockback direction and magnitude snapshots with visibility settings.
- **Predictions/Armor** — armor durability observations and break-prediction calculations.
- **Predictions/Enderpearl** — Ender Pearl trajectory and landing-prediction source.
- **OpponentProfiles** — opponent encounter and win-record source.
- **Trainers/Aim Pattern Library and Aim Trainer** — training and aim-analysis source, including heatmap-related components.
- **Trainers/Bridge** — bridge-training source.
- **Analystics/Combo Analysis** — combo sessions, hit-gap analysis and summary statistics.
- **Analystics/Target** — target-switch sessions and statistics.
- **Statistics/Duels** — duel records and opponent-specific statistics.
- **Timing/Block placement timing** — block-placement samples and timing statistics.
- **Timing/Block break timing** — block-breaking timing source.
- **Arrow and projectile tracking** — tracking and prediction support across the relevant prototype folders.
- **Lifetime Stats Book** — lifetime statistics, snapshots and summary source.
- **LootFeed** — kill and dropped-item tracking, grouped entries, HUD rendering and independent fade behavior.
- **Hitbox** — isolated hitbox prototype with visibility restrictions.

## HUD, visuals, customization and interface

- **Weather-Reactive HUD** — weather-aware HUD palette resolution.
- **ArmorBreakWarning** — armor durability warning source.
- **Badges** — anniversary, birthday celebration and playtime badge foundations.
- **Customization** — theme management, color filters, block overlays and palette generation.
- **Sounds** — alert sound settings and GUI.
- **TimeTill** — time/event tracking, formatting and HUD rendering.
- **MinecraftWindowAlphaClient** — Minecraft window title formatting using AlphaClient and Minecraft version information.
- **AfkCamera** — AFK activity and camera-state source.
- **ScreenshotComparison** — screenshot comparison and composition source.
- **Viewer/ItemNbtViewer** — item NBT tree and viewer GUI.
- **Viewer/PlayerDirectionViewer** — player-direction viewer source.
- **Viewer/PlayerConnectionViewer** — player connection viewer source.
- **Achievements** — achievement definitions, progress, search and achievement-wall display.
- **GUI** — prototype GUI components.
- **Wrapped** — annual Alpha Wrapped statistics and summary-card source.
- **Sounds and visual effects** — configurable alert, rendering and presentation support.

## Staff dashboard, investigations and moderation

- **Staff Dashboard Expansion** — attendance, availability, competency, coverage forecasting, goals, improvement plans, presence, rank comparison, responsibility boards, review calendars, shifts, duty mode, handoffs and staff queues.
- **AccessReviewReminder** — staff access-review records, review intervals, due/upcoming status and GUI.
- **ActingRankAssignment** — temporary staff-rank assignment, expiry and revocation records.
- **ActiveDeviceListGlobalSignOut** — device-session listing and global sign-out workflow foundation.
- **Cases** — case management, case linking, import/export, redaction, blind review and detection/appeal conflict support.
- **PlayerHistory** — player observation and session history.
- **PunishmentCommandPreflight** — preflight checks before punishment commands.
- **PunishmentConflictDetector** — punishment-conflict detection source.
- **Staff dashboard support** — access review, assignment and operational dashboard components.
- **AlphaCrashReporter** — crash capture, analysis and reporting components.
- **AlphaClans** — clan creation, membership, roles, permissions, invitations, ownership transfer and clan chat.
- **AlphaCoins** — coin-related foundations, including playtime rewards and daily challenges.
- **Donator Modules** — separate Donator module collection.

---

# 4. StaffOps — idea-source catalogue under `staffops/ideas/`

The branch also contains **1,496 Java source files** in the StaffOps idea collection, plus its supporting directory structure. These entries are not counted as completed or integrated features merely because their source files exist. The catalogue spans the following areas.

## Combat, PvP and player analysis

- Aim, acceleration, deceleration, smoothness and movement-pattern analysis.
- Click consistency, click rhythm, click interval, click distribution and CPS history.
- Attack timing, first-hit reaction time, hit accuracy, hit delay and target acquisition.
- Combo highlights, combo-reactive feedback, fight summaries, duel statistics and post-fight reports.
- Bow accuracy, projectile tracking, Ender Pearl tracking, pearl cooldowns and landing alerts.
- Knockback, reach, velocity, strafing, sprint resets and movement analysis.
- Target distance, target health, target armor, target equipment, target hotbar and target weapon displays.
- Training drills, practice goals, reaction tests, aim training, bridge training and scenario builders.
- Kill feeds, kill highlights, kill streaks, assist tracking, damage numbers and damage statistics.

## HUD, rendering and visual customization

- Keystrokes, CPS, FPS, frametime, memory, CPU/GPU, ping, TPS and network-throughput displays.
- Health, hunger, saturation, XP, armor, durability, potion, effect, coordinates, biome, dimension, direction, time and date HUDs.
- Crosshair customization, dynamic crosshair, hit markers, hit colors, hit effects and target indicators.
- Item information, inventory counts, held-item durability, dropped-item labels and pickup feedback.
- Weather, world border, chunk, light level, Y-level, compass, bed status and entity-count indicators.
- Custom skies, sun/moon textures, boss bars, player lists, title styling, chat icons and notification effects.
- Camera, perspective, zoom, freelook, free camera, photo mode, motion blur and visual-quality controls.
- GUI inspection, movable/resizable HUD elements, rounded corners, scaling and accessibility-related controls.

## Minecraft gameplay and quality-of-life utilities

- Auto sprint, auto walk, toggle sneak/sprint, hotbar shortcuts and mouse/key remapping.
- Inventory sorting, slot locking, quick stacking, item comparison, crafting helpers and recipe overlays.
- Chest search, storage counters, resource counters, furnace timers and farm-rate calculations.
- Block guides, block-state inspection, block placement/break analysis and distance measurement.
- Singleplayer world management, world backups, world sharing, world seeds, world notes and world thumbnails.
- Resource-pack tweaks, texture overrides, screenshot tools, screenshot albums and screenshot privacy controls.
- Server favorites, quick connect, server history, reconnect, server switching and server status displays.
- Chat history, chat search, chat tabs, timestamps, filters, translation, formatting, autocomplete and message alerts.
- Sound controls, per-sound volume, notification sounds, music controls and audio behavior.
- AFK reminders, eye-rest reminders, real-world clock, alarms, stopwatches and Pomodoro timer.
- Achievements, daily/weekly challenges, playtime milestones, statistics, leaderboards and personal records.

## Community, friends and social features

- Friend lists, friend groups, requests, notes, status, presence, privacy and notifications.
- Friend synchronization between client and launcher, rich presence and server recommendations.
- Clan tags, clan leaderboards, clan wars, faction calendars and raid recaps.
- Community presets, configuration sharing, polls, reputation and feature-request boards.
- In-game launcher chat, Discord sharing, Discord voice overlays, TeamSpeak/Mumble integration and Twitch chat.
- Player profiles, personal notes, nicknames, player cards, player lookup and name history.
- Community announcements, news feeds, wiki shortcuts and onboarding guidance.

## Staff operations, moderation and governance

- Staff activity, assignments, availability, shifts, workload balancing and coverage forecasts.
- Staff dashboards, command centers, emergency broadcasts, panic actions and incident coordination.
- Staff permissions, role labels, permission history, permission audits and permission-drift alerts.
- Staff chat search, filters, keyword alerts, notifications, quiet hours and handoffs.
- Reports, report queues, duplicate detection, assignment, triage, notes, priorities and status tracking.
- Punishment history, templates, approval queues, confirmations, conflict checks and consistency reviews.
- Appeals, appeal assignments, evidence review, escalation and decision logs.
- Rank changes, promotion readiness, trial progress, coaching, training and quarterly reviews.
- Governance, policy approvals, decision records, delegation, succession planning and risk registers.
- Audit trails, action history, undo controls, approval chains and privileged-action receipts.

## Anti-cheat, detection and investigation

- Client-side movement, aim, click, combat, packet and interaction analysis.
- Behavioral baselines, anomaly detection, behavior clustering and similarity engines.
- Detection alerts, alert routing, escalation, history, suppression and confidence levels.
- Detection rule editing, testing, simulation, versioning, profiles, rollback and calibration.
- Packet analysis, packet bursts, packet frequency, network anomalies and performance profiling.
- Evidence capture, evidence linking, event markers, screenshots, replay markers and timelines.
- Investigation workspaces, case assignment, notes, checklists, timelines, graphs and exports.
- False-positive tracking, detection benchmarking, sensitivity tuning and review workflows.
- Privacy filters, permission guards, integrity checks and evidence access logs.

## Recording, replay and content creation

- Replay recording, browsing, playback, speed controls, seek controls and timeline navigation.
- Replay cameras, free cameras, follow cameras, camera paths and perspective switching.
- Event markers, best-moment markers, clip management and evidence replay.
- GIF recording, screenshots, screenshot annotation, editing, watermarks and automatic capture.
- Streamer mode, stream chat overlays, OBS statistics and notification controls.
- Video-loop backgrounds, cinematic camera paths and photo-mode controls.

## Server, network and operational tooling

- Server connection diagnostics, ping graphs, latency, packet loss and TPS estimation.
- Server health boards, incident consoles, maintenance mode and recovery checklists.
- Server favorites, address/player-count HUDs, auto reconnect and quick-connect menus.
- Network emergency controls, isolation workflows and restart/shutdown approvals.
- Server-aware profiles, server-specific settings and session statistics.
- Download progress, bandwidth displays and connection timelines.

## Inventory, world, survival and economy

- Item counters, item value estimates, trade history and shop price tracking.
- Resource collection, mining statistics, mining heatmaps and mob-grinder yield logs.
- Farming, crop growth, animal, mob, villager and bestiary utilities.
- World maps, explored-chunk maps, claim boundaries, claim reminders and coordinate bookmarks.
- Nether coordinate conversion, stronghold triangulation, redstone guides and timers.
- Survival day counters, session profit/loss, money-per-hour and auction price history.
- Faction readiness, raid routes, supply checklists and faction event calendars.

## Platform, account and client maintenance

- AlphaClient module/core, feature flags, module manager and update-checking concepts.
- Alpha friends, social network, communication, media and replay platform concepts.
- Crash reporting, system monitoring, performance telemetry and integrity checks.
- Account recovery, two-factor authentication, personal-data export and account deletion concepts.
- Configuration migration, validation, backup/restore, tamper checks and change history.
- Safe mode, update staging, signed-update verification and compatibility checks.
- Launcher/client synchronization and live launcher presence concepts.

The categories above describe the scope represented in the idea-source catalogue. An idea entry is not a statement that its feature is implemented, enabled, connected to a service, or tested.

---

# 5. Repository organization and documentation

- Consolidated standalone prototype source under `src/main/java/com/alpharip/staffmod/Prototypes/`.
- Kept the `staffops/` workflow separate from the sibling prototype folders.
- Kept idea-source entries under `staffops/ideas/` and moved selected source modules into `staffops/done/`.
- Added and expanded prototype READMEs describing source responsibilities, current boundaries and remaining integration or verification work.
- Documented the canonical prototype location and corrected prototype inventory records.
- Expanded the Auto Loading Server Profiles documentation and audited its related source structure.
- Updated documentation for Build Symmetry Guides, Builder Grid & Ruler Overlay, Chunk Borders, Fishing Bite Alarm and other standalone prototypes.
- Documented the target Minecraft 1.8.9 / Forge 11.15.1.2318 / Java 8 environment for prototype planning.
- Documented the separation between prototype source, StaffOps ideas and the playable client.
- Kept prototype planning separate from AlphaClient Core integration and Alpha Launcher changes.

---

# 6. Scope and verification notes

- This changelog covers the complete `mod-ideas` branch, not only `staffops/ideas/`, `staffops/done/` or `Prototypes/`.
- The prototype inventory and idea-source catalogue are source inventories. They are not a production feature checklist.
- Features described as prototypes remain separate from AlphaClient Core unless their integration is explicitly present in the client source.
- Idea-source entries remain ideas/source records unless implementation is separately present.
- No build, automated test, runtime test or in-game test is claimed by this document.
- Alpha Launcher is outside the scope of this branch changelog.
