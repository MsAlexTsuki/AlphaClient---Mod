# AlphaClient — Branch Changelog

**Branch:** mod-ideas  
**Repository:** MsAlexTsuki/AlphaClient---Mod  
**Coverage:** Work recorded in the complete mod-ideas branch history and represented in the current branch snapshot.

This changelog records concrete code, modules, prototypes, UI, commands, supporting systems, and repository changes. It is not a feature proposal catalogue. Items are grouped by the work that was created or expanded. Standalone prototypes and idea foundations are labelled as such; their presence does not mean they are integrated into the playable client.

## AlphaClient client, modules and interface

### Main menu and module navigation
- Created the compact AlphaClient main-menu interface and a separate AlphaClient home overlay while preserving the vanilla Minecraft title-screen path where required.
- Added the AlphaClient logo to the client home presentation and corrected its resource path and rendering.
- Added a dedicated home-menu keybind and direct navigation into the existing Mods interface.
- Added HUD preview and drag controls to the compact home overlay.
- Added module-list navigation, category routing, search/filter behavior, scrolling, and module detail/settings entry points.
- Restored the HUD Editor and Profiles entry points after menu layout changes.
- Added a grid layout for StaffMods module buttons and refined the General and Quality category layouts.
- Added shared GUI text/button presentation and normalized labels and capitalization across the client screens.

### HUD modules and editor work
- Added and registered General HUD foundations for Keystrokes, Coordinates, Compass, player/status information, held-item information, combat status, and warning displays.
- Added Armor HUD Info and Potion HUD Info with dedicated renderers, settings screens, editor previews, positioning, and display options.
- Added the Ping HUD with configurable appearance, saved position, editor dragging, and lifecycle registration.
- Added Session Timer and Playtime Tracker HUD modules, including settings, display formatting, editor support, and persistent state.
- Added AFK Timer display controls and background settings.
- Added per-module HUD scale configuration and applied saved scale values to the supported HUD renderers.
- Added editor resize handles and restricted resize controls to visible HUD modules.
- Added HUD color presets and rainbow modes for Armor and Potion HUDs, plus shared palette support for Session Timer.
- Added HUD editor preview handling for disabled modules and corrected positioning at screen edges.
- Added HUD preview and rendering adjustments for armor icons, potion icons, labels, colors, and text alignment.
- Kept public information HUD modules visible in General and separated their settings from restricted StaffMods functionality.

### Commands and in-game controls
- Added a shared AlphaClient command core and command shortcuts for module and HUD control.
- Added /alphahelp with command documentation, GUI access, argument guidance, aliases, and rank-aware command visibility.
- Added /alphaperms and its Founder-only permission-management GUI, including permission lookup, ID/note management, overrides, clearing overrides, rank aliases, and multi-word rank names.
- Added command routes for regular module categories, Beta/Donator categories, StaffMods folders, and individual module actions.
- Added tab completion and argument validation for profile, module, settings, and StaffMods commands.
- Added profile commands for listing, switching, creating, editing, renaming, and deleting named profiles.
- Added HUD Editor and Profiles command entry points.
- Added command permission checks and explicit module-grant handling for command execution.
- Added exception and stack-trace diagnostic command support.

### Profiles, accounts and rank access
- Added and refined profile persistence, profile recovery, Default-profile creation, and duplicate-profile handling.
- Restored profile records across profile stores without overwriting existing profile data.
- Connected Bhop settings to the active profile store and preserved saved preferences during permission refreshes.
- Added Alpha Account session persistence and community-rank/permission synchronization.
- Added display handling for the registered Alpha username and restored session state in the login interface.
- Added community-rank mappings and rank badge assets for Developer, Partner, Media, Famous, and other supported ranks.
- Corrected rank priority, rank labels, rank badge texture paths, texture sizing, alpha blending, and local third-person badge rendering.
- Added multi-rank permission merging while displaying the effective highest assigned rank.
- Added inherited rank access for staff and Donator module categories, plus explicit per-module and per-folder grants.
- Added separate StaffMods access checks and rank-based folder visibility.
- Kept Bhop permission-gated and located under StaffMods → General; removed its separate MOD-menu entry and restricted its HUD preview and dragging to users with Bhop access.
- Added Developer Mode and Developer Studio access controls, diagnostic tools, a live debug overlay, runtime metrics, and an in-client command console.
- Added Founder-only access paths for the private W-Tap assistance feature and protected Founder-only module access from ordinary folder grants.
- Added Platform Admin and Manager rank levels and their StaffMods hierarchy routing.
- Added staff-rank folder ordering and corrected the visibility of management-level folders.

### PvP and legacy visual mechanics
- Added and refined the Legacy PvP module foundation, settings screen, feature status display, and independent feature toggles.
- Added the 1.8.9 blockhit render hook and its persistent setting, with guarded injection and compatibility handling.
- Added legacy bow swing/draw animation controls and render transforms.
- Added legacy food, drink, and potion swing behavior with independent settings and input handling.
- Added legacy fishing-rod animation controls and render-hook work.
- Added missed-swing delay controls and status reporting.
- Added Founder-only W-Tap Assistance settings and input handling, including recording/playback work for the Custom mode.
- Added S-Tap assistance and its Founder-only settings entry.
- Refined swing timing, blockhit targeting, camera-bobbing restoration, and compatibility with obfuscated Minecraft 1.8.9 methods.
- Added the initial combat target-selection foundation and first-pass Aim Assist, Targeting, Attack Timing, and hitbox visualization source.

### Quality modules
- Added Fullbright as a Quality module with a keybind, saved enabled state, and profile-linked settings.
- Added Auto GG with settings, persistence, command controls, event handling, and match-end/kill detection refinements.
- Added Auto Reconnect with settings, commands, registration, and disconnect-screen reconnect controls.
- Added the Quality category module navigation and corrected its module ordering and presentation.

## Staff Operations and StaffMods

### Staff Operations workspace
- Created the Staff Operations entry and dashboard UI.
- Added a dedicated Staff Dashboard screen and separated dashboard navigation from the individual Staff Operations module folders.
- Added interactive Staff Operations module screens and folder routing.
- Added the dashboard command workspace and integrated the CMD view into dashboard navigation.
- Added command parsing, keyboard input, command history, command aliases, and staff-data commands.
- Added private staff notes with create, edit, delete, and permission-checked command operations.
- Added staff presence tracking, heartbeat updates, automatic AFK status, and shared presence display.
- Added staff login/logout activity records, module-open/action records, activity history, and audit-event display.
- Added staff record editing, deletion restrictions, status transitions, record ownership, and management-rank checks.
- Added read-only Investigating views for senior staff and suspect-record command support.
- Added staff display names to operational records and improved dashboard activity filtering.
- Added the Staff Operations data-access and record structure used by the workspace. The branch also contains related migration and policy work; this changelog does not claim that a remote service is available or verified.

### StaffOps source moved into the done collection
The following 35 folders are present under staffops/done in the current branch:

- AimAssist Expansion and AimAssist
- AnvilCost and AnvilCostWarning
- AutoTool
- BlockPlacementPreview
- BuildTimelapse
- CPSMod and ComboCounter
- CommandPalette
- InventorySearch
- PixelGarden
- RecordingStudio
- Store
- WayPoints
- Fight History
- FPS and Ping
- GUI and HUD
- Config and Modules
- Movement, Network, and Optimization
- Permissions
- Profile
- PvP and Quality
- Ranks and RankViewer
- Developer and EasterEggs
- EnchantingName
- WinLossTracker

These folders are recorded as completed/moved StaffOps source collections. Their presence does not by itself certify production integration or successful in-game testing.

## Standalone Java prototypes

The current branch contains **717 Java files across 66 prototype folders** under Prototypes. The prototypes are separate source projects, not automatically registered AlphaClient modules.

### PvP, combat, training and statistics
- Aimassist Expansion — target selection, rotation calculation, smoothing, modes, settings, and GUI source.
- Aim Pattern Library — pattern records, categories, validation, statistics, registry, service, and GUI.
- Combo Analysis — hit-session tracking, combo results, and summary statistics.
- Combo Breaker Cue — configurable cue state and reason categories.
- Clutch Highlights — highlight records, event types, and retention configuration.
- Opponent Profiles — opponent encounter and win-record model.
- Duel Statistics — duel records, snapshots, and opponent-specific statistics.
- Aim Trainer and Aim Drift Heatmap — training sessions, results, configuration, and heatmap source.
- Bridge Speed Trainer — run records, personal bests, statistics, configuration, and session controller.
- Ender Pearl Accuracy Practice — practice-session source.
- Bow Charge Indicator — draw-tick calculation, charge snapshots, readiness, and configuration.
- Knockback Direction Indicator — knockback snapshots, direction/magnitude data, and settings.
- Armor Break Prediction — per-slot durability tracking, samples, results, and prediction configuration.
- Ender Pearl Trajectory Prediction — vector model, trajectory simulation, prediction results, and configuration.
- Hitbox — isolated hitbox geometry, module, settings, and renderer source.
- Arrow Tracker and ArrowCounter — arrow tracking and HUD source.
- Block Placement Timing, Block Break Timing, and Combat Timer — timing samples, statistics, and configuration.
- LootFeed — kill/drop tracking, grouped entries, HUD rendering, ordering, eviction, and independent fade behavior.
- Lifetime Stats Book — lifetime-stat records, snapshots, tracking, and summary source.

### HUD, visuals and client presentation
- Air HUD and Biome HUD.
- Armor Break Warning.
- Weather-Reactive HUD.
- Adjustable Rain and Snow Speed.
- Adjustable Star Density.
- Adjustable Third Person camera distance settings and options-screen hook boundary.
- Block Break Progress Ring.
- Chunk Borders.
- Build Symmetry Guides.
- Builder Grid and Ruler Overlay.
- Fishing Bite Alarm.
- Minecraft Window AlphaClient title formatting and JAR-version resolution.
- TimeTill event countdown and HUD rendering.
- Alert Sounds settings and GUI.
- Customization source for themes, colors, block overlays, and palettes.
- Achievement Checklist, Achievement Suffixes, Achievement Wall, Anniversary Badges, Birthday Celebration, and Playtime Badges.
- Annual Alpha Wrapped summary and cards.
- Screenshot Comparison and composition.
- Item NBT Viewer, Player Direction Viewer, and Player Connection Viewer.
- AFK Camera activity tracking, settings, controller, and GUI.
- ToggleSneak and ToggleSprint with separate state, controller, and keybinding source.

### Building, inventory, survival and singleplayer
- Easy Craft — recipe catalogue, categories, search, favorites, crafting-table screen/button boundary, and crafting adapter contract.
- AutoSave — save-state tracking, local settings/storage, HUD, and GUI.
- Animal Breeding Cooldown Timer — per-animal records, cooldown calculations, event boundary, storage, and GUI.
- Base Storage Ledger — storage index, item records, search/GUI source, and index-read optimizations.
- Remember Chest Inventory — chest snapshots, scanning, memory entries, search, storage, and GUI.
- Chest Item Total — chest item counting and entry tracking.
- Remember Villagers — villager trade capture, records, persistence, and GUI.
- Resource Pack Organizer — resource-pack entries, file management, validation, and GUI.
- Build Symmetry Guides — X/Z mirror previews and guide settings.
- Builder Grid and Ruler Overlay — world-space grid, measurement guides, ruler HUD, origin controls, and snapping.
- Chunk Borders — chunk boundaries, corner pillars, current-chunk highlighting, style, distance, and opacity controls.
- Fishing Bite Alarm — hook movement detection and configurable alert settings.
- Pet Memorial — pet-death records, formatting, and memorial-book source.
- Adventure Diary — adventure entries, events, and diary presentation.
- Bestiary — mob kill-event tracking and bestiary records.

### Staff dashboard, moderation and investigation prototypes
- Staff Dashboard Expansion: Attendance, Availability, Competency, Coverage Forecast, Goals, Improvement Plans, Overloaded Staff Alert, Presence, Rank Comparison, Responsibility Board, Review Calendar, Shifts and Checklist, Staff Duty Mode, Staff Handoff, and Staff Queue.
- Access Review Reminder.
- Acting Rank Assignment.
- Acknowledgement Tracker.
- Active Device List and Global Sign-Out.
- Alpha Crash Reporter.
- Case Escalation Timer.
- Blind Case Review.
- Appeal Conflict Detector.
- Punishment Command Preflight.
- Punishment Conflict Detector.
- Player History.
- Investigation case source for case management, case linking, import/export, redaction, and evidence workflows.
- Ally Faction Notes under Kitmap & HCF.
- Staff-oriented viewer and inspection source, including player connection/direction viewers and item NBT inspection.

### Store, social and platform prototypes
- AlphaClans — clan identity, membership, roles, permissions, invitations, ownership transfer, and bounded clan chat records.
- AlphaCoins — shared wallet/core service, commands, daily challenges, and playtime rewards.
- Alpha Store — central section registry, Addons, Cosmetics, Owned, and Partners catalogues.
- Cosmetics catalogue — 128 migrated cosmetic component files, category metadata, search, details, optional texture previews, ownership registry, and local loadout/equip state.
- Addons catalogue — local metadata, search, category filtering, and local state transitions.
- Partners directory — relationship categories, search, and profile details.
- Account Linking — local account-link model, validation, registry, service, settings, and GUI.
- Alpha Policy website and policy document collection, including draft policy pages and the policy reader.
- AlphaCrashReporter — crash capture, analysis, report metadata, privacy redaction, and report-detail screen.


### Detailed StaffOps module work
- **AimAssist Expansion:** added target selection, target snapshots, rotation calculation, smoothing controls, modes, settings, and a standalone GUI.
- **AnvilCost:** added vanilla anvil-cost calculation and compact inventory tooltips; refined enchantment-book previews, XP-cost sorting, and missing-XP information.
- **AutoTool:** added automatic tool selection, settings/navigation, selection refinements, and singleplayer-save-scoped profile handling.
- **BlockPlacementPreview:** added placement preview rendering, settings and command source; refined placement metadata, collision checks, and vanilla block-model handling for non-solid shapes.
- **BuildTimelapse:** added a modular studio with build-area tracking, block/activity analytics, local project persistence, interval-based frame capture, camera paths, timeline models, effects profiles, and export-job metadata.
- **CPSMod and ComboCounter:** added click/combo tracking; ComboCounter also received HUD rendering, a public General module registration, settings, preview dragging, reset support, and saved appearance/position.
- **CommandPalette:** added searchable action navigation and corrected row interaction and mouse-wheel scrolling.
- **InventorySearch:** added search interfaces for player inventory, chests, and Ender Chests, plus settings and persistence.
- **PixelGarden:** added the Pixel Garden source collection, including its feature, commands, animation, and coin-related components.
- **RecordingStudio:** added an OBS-style recording workspace with scenes, sources, a live gameplay preview, video/audio timeline, camera editor, microphone selection and capture, local project storage, crash-recovery checkpoints, asynchronous frame encoding, and MP4/GIF export controls. Export support includes timeline clip timing, resolution/quality presets, progress reporting, cancellation, and external video sources. FFmpeg/FFprobe are external requirements; this prototype is not described as integrated or tested.
- **Store:** added the central Store navigation and Addons, Cosmetics, Owned, and Partners sections; migrated 128 cosmetic component files into the catalogue and added local search, details, optional texture previews, ownership/loadout state, and player-skin preview source. Purchase processing, remote entitlements, and live cosmetic attachment are not claimed.
- **WayPoints:** added local waypoint management with commands, profiles, favorites, history, import/export, route tools, visual customization, and safer persistence.
- **WinLossTracker:** added Practice win/loss tracking, server-address profiles, per-address statistics, profile aliases, explicit result detection, and safer local persistence.
- **EnchantingName:** added vanilla-enchantment clue handling and a text-only clue renderer.
- **FPS, Ping, GUI, HUD, Config, Modules, Movement, Network, Optimization, Permissions, Profile, PvP, Quality, Ranks, RankViewer, Developer, and EasterEggs:** organized their supporting source into the StaffOps done collection.

### Exact standalone prototype folder inventory
AccessReviewReminder; AccessibilityTheme; ActingRankAssignment; ActiveDeviceListGlobalSignOut; AdjustThirdPerson; AdjustableRainAndSnowSpeed; AdjustableStarDensity; Adventure Diary; AfkCamera; Aim Pattern Library; AirHud; AlphaClans; AlphaCoins; AlphaCrashReporter; Analystics; ArrowCounter; AutoLoadingServerProfiles; Badges; Bestiary; BiomeHud; Block Break Ring; BuildSymmetryGuides; BuilderGridAndRulerOverlay; ChestItemTotal; ChunkBorders; Clutch; Combo; Cooldowns; Customization; FishingBiteAlarm; Indicators; Kitmap & HCF; Lifetime Stats Book; LootFeed; MinecraftWindowAlphaClient; OpponentProfiles; Pet Memorial; Predictions; Remember Villagers; Singleplayer; Sounds; Staff Dashboard Expansion; Statistics; TimeTill; Timing; Trackers; Trainers; Viewer; Weather-Reactive HUD; Wrapped; account; achievements; animalbreedingcooldowntimer; armorbreakwarning; autosave; basestorageledger; cases; gui; hitbox; playerhistory; punishmentcommandpreflight; punishmentconflictdetector; rememberchestinventory; resourcepackorganizer; screenshotcomparison; togglesneak.

## StaffOps idea-source foundations

The current branch contains **1,496 Java source files** under staffops/ideas. These are idea/source foundations and are not represented as finished client features.

The idea-source work covers:
- Combat and PvP: aim, rotation, movement, click-pattern, attack timing, reach, knockback, target switching, combat statistics, training, and opponent analysis.
- HUD and visuals: player/world status, coordinates, health, armor, potion effects, inventory, crosshair, hit feedback, rendering options, and customization.
- Minecraft gameplay and quality of life: inventory and crafting helpers, world/building tools, survival counters, resource tracking, server utilities, chat tools, and singleplayer helpers.
- Staff operations: dashboards, shifts, assignments, staff workload, availability, permission review, activity history, notes, reports, moderation, appeals, and governance.
- Anti-cheat and investigation: movement/aim/click/packet analysis, behavioral baselines, anomaly detection, detection rules, alert routing, evidence capture, replay markers, investigation workspaces, and false-positive review.
- Recording and replay: replay capture, timelines, camera controls, event markers, screenshots, GIF/video tools, and streaming overlays.
- Community and social: friends, presence, clans, chat, profiles, sharing, community presets, polls, reputation, and recommendations.
- Platform and account maintenance: account security, recovery, privacy, configuration migration, backups, integrity checks, update safety, diagnostics, and launcher/client synchronization.
- Cosmetics and customization: capes, trails, emotes, names, chat badges, effects, pets, themes, profile presentation, and Store-related systems.
- Founder, management, and governance concepts: privileged-action safeguards, audit history, approval workflows, succession, policy review, incident coordination, and operational dashboards.

The idea-source directory also contains concrete Java foundations for many named concepts, including AlphaClientCoreExperience, AlphaCommunicationCore, AlphaControlCenter, AlphaFriends, AlphaInvestigationStudio, AlphaMediaCore, AlphaModuleManager, AlphaReplayCore, AlphaStaffOperationsCenter, AlphaSystemMonitor, AlphaUpdateChecker, Detection and Evidence components, Replay components, Staff utilities, and the individual gameplay, HUD, social, cosmetic, and moderation features represented by the source files.

## Repository organization and documentation
- Created the mod-ideas workspace and organized the source around the StaffMod package.
- Consolidated standalone prototypes under Prototypes and kept them separate from staffops.
- Kept StaffOps idea-source files under staffops/ideas and moved selected source collections under staffops/done.
- Added and expanded prototype READMEs and implementation checklists.
- Added source inventories and corrected prototype-location documentation.
- Added detailed implementation and integration notes for Auto Loading Server Profiles, Build Symmetry Guides, Builder Grid and Ruler Overlay, Chunk Borders, Fishing Bite Alarm, Recording Studio, Build Timelapse, Store, AlphaClans, AlphaCoins, and other prototypes.
- Added the isolated ForgeGradle build setup for the mod-ideas workspace.
- Added the Alpha policy documentation structure and policy website preview, including policy drafts and the reader layout.

## Verification and integration status
- This changelog describes source and branch history; it does not claim every prototype is connected to AlphaClient Core.
- Prototype READMEs identify the remaining integration boundaries for their respective modules.
- Some prototypes have local models, controllers, GUIs, persistence, or event-adapter source; those components still require the integration and verification described in their documentation.
- The branch snapshot does not establish a successful build or in-game test. No build or runtime test is claimed here.
- Alpha Launcher was not changed as part of this changelog.
