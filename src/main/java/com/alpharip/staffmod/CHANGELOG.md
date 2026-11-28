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

## Standalone Java prototype changelog

These entries describe source work added to the standalone prototype collection. They are prototypes, not claims that the features are integrated into the playable AlphaClient.

### 2026-11-28

#### Builder Grid & Ruler Overlay
- Added a world-space building grid with configurable spacing and guide radius.
- Added X/Z measurement guides, block-distance tick marks, and an X/Y/Z ruler HUD.
- Added origin capture, coordinate snapping, a dedicated settings interface, and local settings persistence.
- Bounded ruler rendering to the configured guide area and updated the prototype inventory.
- Source: [prototype implementation](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/dda1668ea59fd85cef41e68f675d5b3b462353c8) · [rendering and inventory refinement](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/614e6354c77e0fc45438cdb7f95d0ab468e075c2)

#### Chunk Borders
- Added vertical chunk-corner pillars and horizontal chunk-boundary lines.
- Added current-chunk highlighting, three visual styles, configurable render distance, visibility controls, and opacity settings.
- Added a dedicated settings interface and local settings storage.
- Preserved the prototype's disabled default and default visual style.
- Source: [Chunk Borders prototype](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/4eb0c96c01bfab68bb14db6e77a542a0b065fda0)

#### Fishing Bite Alarm
- Added fishing-hook movement detection as the trigger for a local sound alert.
- Added adjustable alert interval and volume, a test-sound option, a settings interface, and local settings storage.
- Source: [Fishing Bite Alarm prototype](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/8cb0f99e39dcb440210450831e8c89e163a18aad)

#### Build Symmetry Guides
- Added X-plane, Z-plane, and combined X/Z mirror previews.
- Added configurable guide visibility, guide lines, colors, opacity, and origin controls.
- The prototype renders visual guides only; it does not place or modify blocks.
- Moved the source into the canonical Java prototype directory and updated its documentation.
- Source: [initial prototype](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/40b064f7c684f4f3ad06c2c4104d63ea0ff526e0) · [source move and expansion](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/983fcf0cb0ad295bdad86a473ab39bb56f189abd) · [location documentation](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/85e016e42f16b23ee8093028ee973dafbc1e4f6)

### 2026-11-27

#### Auto Loading Server Profiles
- Added a modular prototype for loading server-specific profiles.
- Expanded its README with a detailed source audit, implementation boundaries, and a sibling-directory inventory.
- Source: [prototype](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/d7e91fd14185beea2673b1320e0c20e4d621eebd) · [documentation](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/ba25bc56f7196c3cb9f3b9d7cc74932469d4ac56)

#### StaffOps prototype handoff
- Moved RecordingStudio and Store source collections into `staffops/done`.
- Moved BuildTimelapse into `staffops/done`.
- Removed a duplicate Resource Pack Search Bar idea entry.
- Source: [RecordingStudio and Store move](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/278d9c8bd83e7d7e2a8d77fc419d84bc20c1d392) · [BuildTimelapse move](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/e52d2be7648df2624a497dd39f65af0cae139f1e) · [duplicate idea removal](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/1bbf61c296382585c0643a74ef3748395f73a383)

### 2026-11-24

#### Bestiary
- Added a kill-based Bestiary foundation with mob kill records.
- Added client-side tracking for kills made by the local player.
- Source: [Bestiary tracking](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/49f5e2fc112dce9dad0f82f575f449ab259ae21) · [local-player kill tracking](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/e89a467182e951abfcfca9131d8fbb4096cdabbe)

#### Lifetime Stats Book
- Added lifetime-stat records, snapshots, tracking, and summary source.
- Source: [Lifetime Stats Book](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/5ba0e565f283e248f435275b9d26de8116bfa42e)

#### Weather-Reactive HUD
- Added a HUD prototype that represents current weather conditions.
- Source: [Weather-Reactive HUD](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/3e266a1b37b33e611b8bf5cc400a2447dfe6c8f0)

#### Pet Memorial and Adventure Diary
- Added pet-death records and memorial-book presentation source.
- Added adventure-entry and event records with diary presentation source.
- Source: [Pet Memorial](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/3d057f5059472a61dcd88286b0e4b0c935a55444) · [Adventure Diary](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/13c1981f7ec52d4e32aa53b95e39acf52dcee35a)

#### ToggleSneak and ToggleSprint
- Added standalone ToggleSneak and ToggleSprint prototype source.
- Separated movement-toggle state from runtime/controller responsibilities and added a dedicated controller.
- Expanded the prototype architecture and documented the enabled-by-default ToggleSprint setting.
- Source: [initial prototype](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/7057dfe1733f1ad917cc7d26273dfe604624ca7f) · [controller](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/bd936601198b7ea725b9f62fe81365d63cecea06) · [architecture documentation](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/0598868f6a7a190644c30db069903619fa27afdf)

### 2026-11-21

#### Hitbox
- Added an isolated Hitbox prototype with its own geometry, module, settings, and renderer source.
- Added visibility restrictions to the prototype.
- Source: [Hitbox prototype](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/a0111369b161a6d7e6258d8ea45430fcf0431439) · [visibility restrictions](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/52a612de5ebd131a0ceb3e0a6d4b393742c4c5bf)

#### Resource Pack Organizer
- Added resource-pack entries, file-management source, category validation, and GUI source.
- Removed the original idea file after the prototype handoff.
- Source: [prototype](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/68d24fb0bec954478d0244948c0644aa9733f015) · [idea-file removal](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/92d2ee7134776ee0bf0ad8d74283bb50cec52749)

#### Base Storage Ledger
- Added a storage index, item records, and search/GUI source.
- Avoided rebuilding the storage index during GUI reads.
- Removed the original idea file after the prototype handoff.
- Source: [prototype](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/1e6e06941630cc02de8697bb31f2323c58a7beaa) · [index-read optimization](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/7002f291a7b54d889089b2d92c72ae4dfb170a5f) · [idea-file removal](https://github.com/MsAlexTsuki/AlphaClient---Mod/commit/c0bdcc5430baba60863e73f766164be19368d480)

### Additional standalone prototype feature changelogs

The entries below describe the source currently present for each named prototype. They are standalone prototype work unless an entry explicitly says otherwise. A source component or GUI does not imply AlphaClient Core registration, production integration, successful compilation, or in-game verification.

#### PvP and combat

##### Aimassist Expansion
- Added a standalone aim-assistance foundation with target selection, target snapshots, rotation calculation, configurable smoothing, selectable modes, settings, and a dedicated GUI.
- Kept the implementation separate from AlphaClient Core; runtime integration and in-game behavior are not verified.

##### Aim Pattern Library
- Added typed aim-pattern records, pattern categories, a registry and service layer, validation, settings, usage statistics, and a dedicated library GUI.
- The prototype provides the data and presentation structure for organizing and reviewing aim patterns; live combat-event collection and client integration are not established by this source entry.

##### Combo Analysis
- Added a standalone combo-analysis source area for collecting and presenting combo-related information.
- The current branch contains the prototype collection, but this changelog does not claim a verified live event pipeline or completed in-game analysis screen.

##### Combo Breaker Cue
- Added a configurable combo-break cue model and settings foundation for identifying a break in an active combo.
- Live event wiring, cue presentation in a running client, and runtime verification remain unconfirmed.

##### Clutch Highlights
- Added clutch-highlight records and highlight-type definitions, with a configuration model for organizing clutch moments.
- Automatic match/event detection, replay capture, and in-game presentation are not claimed as integrated or verified.

##### Opponent Profiles
- Added an opponent-profile data model as the foundation for retaining player-specific combat observations.
- A complete collection pipeline, persistent profile store, and integrated profile viewer are not established by the current source inventory.

##### Duel Statistics
- Added a standalone statistics prototype area for duel-related records and summaries.
- The branch listing does not establish a verified duel-result detector, persistent statistics service, or integrated statistics screen.

##### Aim Trainer and Aim Drift Heatmap
- Added a trainer-oriented source collection for aim practice and visualizing aim movement over a training session.
- The source is kept as a prototype; a verified live capture loop, finished heatmap workflow, and AlphaClient Core integration are not claimed here.

##### Bridge Speed Trainer
- Added a standalone bridge-training source collection intended to support practice-session timing and performance feedback.
- Automatic course detection, scoring accuracy, and in-game behavior remain unverified.

##### Ender Pearl Accuracy Practice
- Added an Ender Pearl practice source collection for recording and reviewing throw accuracy.
- A verified projectile capture pipeline, target-course integration, and final in-game training flow are not claimed.

##### Bow Charge Indicator
- Added a bow-charge indicator source collection for representing bow draw/charge state.
- Live charge sampling, HUD rendering behavior, and module registration have not been verified in-game.

##### Knockback Direction Indicator
- Added a knockback-direction indicator source collection for representing the direction of received knockback.
- The branch contains the prototype area; packet/event accuracy and live HUD behavior remain unverified.

##### Armor Break Prediction
- Added an armor-break prediction source collection for estimating armor durability outcomes.
- The current source presence does not establish a verified prediction model or reliable opponent-armor data feed.

##### Ender Pearl Trajectory Prediction
- Added a trajectory-prediction source collection for estimating an Ender Pearl's path.
- Collision accuracy, server-specific behavior, and live rendering have not been verified.

##### Arrow Tracker
- Added an arrow-tracking source collection for tracking projectile locations and related state.
- Persistent tracking, multi-arrow behavior, and live-world rendering remain unverified.

##### Block Placement Timing
- Added a timing-focused source collection for measuring block-placement intervals.
- Automated event capture, timing accuracy, and a finished HUD/reporting view are not claimed as verified.

##### Block Break Timing
- Added a timing-focused source collection for measuring block-break intervals.
- Automated event capture, timing accuracy, and a finished HUD/reporting view are not claimed as verified.

##### Combat Timer
- Added a combat-timing source collection for tracking combat-related timing state.
- The exact trigger rules, event integration, and live display behavior require runtime verification.

##### LootFeed
- Added a loot-feed model with individual loot entries, a tracker, and a HUD presentation component.
- The source separates event tracking, entry data, and display responsibilities; live loot-event wiring and Core registration are not confirmed.

##### Lifetime Stats Book
- Added typed lifetime counters for player and mob kills, deaths, mining, block placement, crafting, movement, and play time.
- Added a thread-safe counter store, non-negative increments, overflow protection, immutable snapshots, and a tracker API intended for Minecraft/Forge event adapters.
- Automatic event collection, persistence, reset confirmation, a statistics GUI/book item, and AlphaClient Core integration are not included in this prototype stage.

#### HUD and visuals

##### Air HUD
- Added an independent Forge module facade with explicit registration and unregistration.
- Added an underwater air display showing remaining air ticks and a tick-derived seconds estimate, four corner positions, and settings for enablement, label visibility, position, and text color.
- Added local settings persistence at `config/alphaclient/air-hud.properties`; Core registration and in-game verification are not claimed.

##### Biome HUD
- Added a standalone biome-HUD source collection with a module facade, Forge event adapter, renderer, settings GUI, settings model, and local storage.
- The source is structured for biome information presentation; live rendering and client registration remain unverified.

##### Armor Break Warning
- Added a standalone warning module that checks the local player's equipped armor for pieces at or below a configurable remaining-durability threshold.
- Preserved the original 10% default and added selectable thresholds of 5%, 10%, 15%, 20%, and 25%, a HUD warning renderer, settings preview, explicit event registration, and local properties storage.
- This warning observes the local player's armor; it does not predict an opponent's armor break. Core registration and in-game behavior are not verified.

##### Adjustable Rain and Snow Speed
- Added a weather-speed controller, settings model, weather-type representation, immutable snapshots, and a dedicated settings GUI.
- The prototype separates weather-speed configuration from runtime control; actual weather particle behavior and in-game rendering require verification.

##### Adjustable Star Density
- Added star-density settings, style definitions, a controller, immutable snapshots, and a dedicated settings GUI.
- The prototype provides configurable star-density presentation controls; rendering integration and visual behavior remain unverified.

##### Adjust Third Person
- Added a configurable third-person camera-distance controller, options GUI, and camera hooks.
- The source provides a separate camera-options path; compatibility with other camera changes and runtime behavior have not been verified.

##### Block Break Progress Ring
- Added a block-break progress-ring source collection with animation state, configuration, snapshots, a renderer, event adapter, and settings GUI.
- The source separates progress animation and rendering; integration with the live block-breaking overlay and in-game behavior remain unverified.

##### Chunk Borders
- Added vertical chunk-corner pillars, horizontal chunk-boundary lines, and an outline for the player's current chunk.
- Added Default, Minimal, and High contrast styles, configurable render radius, line visibility, current-chunk highlighting, opacity, a settings GUI, and local properties storage.
- Preserved the original disabled default and default style. The module uses explicit start/stop registration and is not registered in AlphaClient Core.

##### Minecraft Window AlphaClient Title Formatting
- Added a window-title controller and formatter with a configurable settings model.
- Added a target abstraction for the LWJGL window title and a resolver for the AlphaClient JAR version.
- Actual window-title updates depend on runtime integration and have not been verified.

##### TimeTill
- Added a time-until display source collection with a formatter, Forge event adapter, renderer, settings GUI, and local settings storage.
- The source separates time formatting, HUD presentation, and event handling; live timing accuracy and Core registration remain unverified.

##### Alert Sounds
- Added a dedicated sound-related prototype source collection.
- The current branch inventory does not establish a complete, verified alert catalogue, trigger pipeline, or settings integration.

##### Customization
- Expanded the standalone customization source collection with theme management, color filters, color-blind accessibility mode, custom block outlines, GUI scale controls, inventory backgrounds, scoreboard styling, dynamic themes, enchantment-glint settings, hotbar presets, HUD alignment/snap guides, one-handed keyboard layout, per-player chat colors, per-screen scaling, PvP HUD editing, tooltip rarity colors, reduced screen shake, selectable heart styles, per-item sensitivity profiles, server-specific keybinds, shareable profiles, transparent hotbar, view-bobbing customization, and water-color/brightness controls.
- These are separate customization foundations; the presence of the source collection does not mean every option is registered in the live client or verified.

##### Achievements and Badges
- Added achievement-entry and checklist models, progress and unlock operations, completed-entry queries, case-insensitive search, session-local settings, and a Minecraft 1.8.9 checklist GUI.
- Added a separate achievement-wall model, entry/snapshot types, sorting, service, settings, and GUI.
- A production achievement catalogue, gameplay event hooks, persistent storage, account synchronization, and Core integration are not included. Badge assets/catalogue presence is not proof of an integrated badge-award system.

##### Alpha Wrapped
- Added annual-period, session, summary, and card models for an Alpha Wrapped recap.
- Added a calculator, service, settings, and GUI source for assembling a yearly activity summary.
- The source inventory does not establish a complete data-collection pipeline or verified recap output in-game.

##### Screenshot Comparison
- Added local PNG/JPEG loading with file, format, and image-size validation.
- Added before/after image pairing and side-by-side, alpha-overlay, and pixel-difference preview modes.
- Added changed-pixel percentage and mean RGB-difference metrics, plus a Minecraft 1.8.9 screen for file paths, loading, comparison, and preview.
- Image processing is local. Settings are in-memory, paths are entered manually, and the difference view is a visual aid rather than forensic image-authenticity analysis.

##### Item NBT Viewer
- Added an item-NBT viewer prototype source area.
- The current source inventory does not establish a verified live item inspection screen, safe NBT rendering, or integration with inventory interactions.

##### Player Direction Viewer
- Added a player-direction viewer prototype source area.
- A live player-direction data adapter, final rendering, and runtime behavior are not confirmed by the current source inventory.

##### Player Connection Viewer
- Added a player-connection viewer prototype source area.
- A verified connection-state source, completed viewer workflow, and in-game integration are not established here.

##### AFK Camera
- Added AFK activity tracking, camera settings and snapshots, a controller, a standalone GUI, and selectable camera styles.
- The source separates activity detection, camera control, and presentation; automatic camera operation and runtime safety have not been verified.

##### ToggleSneak and ToggleSprint
- Added separate ToggleSneak and ToggleSprint modules on a shared movement-toggle contract.
- Added physical input sampling, configurable activation keybindings, vanilla key-state handling, a controller, a Forge client-tick adapter, and an explicit runtime start/stop owner.
- Both modules are enabled by default but start toggled off; activation keybindings are initially unbound. Physical input continues to work, and controlled key states are released when a GUI is open or Minecraft loses focus.
- The runtime is not automatically started or registered in AlphaClient Core. Compilation and in-game input behavior have not been verified.

#### Additional HUD/visual source collections

##### Weather-Reactive HUD
- Added weather-condition definitions and a palette resolver for clear, rain, thunder, and snow conditions.
- Added separate primary, secondary, and accent ARGB palette values and a model exposing the active condition and palette.
- No world-weather adapter, HUD renderer, transition animation, or Core integration is connected yet.

#### Building, inventory, survival, and singleplayer

##### Easy Craft
- Added an Easy Craft prototype source collection for simplifying access to crafting operations.
- The current branch inventory does not establish a verified crafting-screen integration, recipe workflow, or automated crafting behavior.

##### AutoSave
- Added an autosave module facade, Forge event adapter, formatter, settings GUI, HUD, state model, and local settings storage.
- The source separates save-state tracking and presentation; the actual save trigger policy, world-safety behavior, and runtime operation require verification.

##### Animal Breeding Cooldown Timer
- Added animal-breeding records, a tracker, Forge event adapter, settings, HUD, GUI, and local storage.
- The source is structured to track breeding cooldown state and present it to the player; event accuracy and persistence behavior have not been verified in-game.

##### Base Storage Ledger
- Added a storage index that aggregates item totals from remembered chest snapshots, immutable item rows, and minimum-stock thresholds.
- Added a searchable stock overview GUI and local threshold settings.
- Reuses the existing Remember Chest Inventory snapshot/manager rather than adding another chest scanner. Totals are only as current as the latest saved chest snapshots.

##### Remember Chest Inventory
- Added chest-memory entries, item snapshots, a manager, scanner, search, local storage, and a dedicated GUI.
- The source separates chest capture, stored snapshots, searching, and presentation; live chest-event coverage and in-game behavior require verification.

##### Chest Item Total
- Added item-total entry and snapshot models, a scanner, Forge event adapter, renderer, GUI, settings, and local storage.
- The source is structured to calculate and present item totals from chest contents; scan accuracy and runtime integration remain unverified.

##### Remember Villagers
- Added villager-trade records and entries, a registry, capture adapter, Forge event handler, GUI, and local storage.
- The source provides a structure for retaining observed villager trades; complete trade capture and persistence behavior have not been verified in-game.

##### Resource Pack Organizer
- Added scanning for ZIP resource packs and unpacked directories containing `pack.mcmeta`.
- Added category-folder creation, name filtering, guarded moves between the resourcepacks root and one immediate category directory, and a standalone management GUI with search, paging, and move actions.
- Category names are validated and existing destination files are not overwritten. Vanilla Minecraft 1.8.9 does not recursively discover packs in arbitrary category folders, so nested packs will not appear in the vanilla selector without a separate repository integration.

##### Build Symmetry Guides
- Added X-plane, Z-plane, and combined X/Z mirror-coordinate calculations around a configurable origin.
- Added source-to-mirror guide lines, mirrored block outlines, axis lines, color/opacity controls, bounded guide distance, and an origin-adjustment GUI.
- The overlay is visual only and does not place or modify blocks. Settings are stored locally; Core registration and runtime verification are not included.

##### Builder Grid & Ruler Overlay
- Added a horizontal world-space grid around a saved origin and X/Z ruler axes toward the player's current block position.
- Added X/Y/Z offset readouts, configurable spacing and guide radius, origin capture, coordinate snapping, settings GUI, and local settings/origin persistence.
- Preserved the original enabled grid/ruler defaults and spacing-based snapping behavior. The overlay is visual only and does not modify blocks.

##### Fishing Bite Alarm
- Added local fishing-hook sampling and a heuristic detector for a sharp downward bobber-motion impulse while the hook is in water.
- Added a local sound notifier, configurable alert interval and volume, test-sound action, settings GUI, and local properties storage.
- Preserved the original enabled default and 60-second interval. The trigger is heuristic rather than server-confirmed; reliability has not been verified in-game.

##### Pet Memorial
- Added memorial records for pet name, species, owner, world, cause, and timestamp.
- Added an ordered memorial book, a short-inscription formatter, and a recorder API intended for a future tamed-pet death listener.
- Automatic death detection, pet identity/deduplication, GUI, book item, disk persistence, and Core integration are not included.

##### Adventure Diary
- Added typed gameplay events for world entry, kills, deaths, pickups, dimension changes, and boss defeats.
- Added a separate dramatic narrator and an in-memory diary book that stores timestamped entries in insertion order.
- Forge event listeners, date/chapter grouping, GUI, export, disk persistence, and Core integration are not included.

##### Bestiary
- Added a mob-kill logbook model keyed by entity registry ID, retaining first world, first kill time, latest kill time, and credited kill count.
- Added a Forge LivingDeathEvent listener that filters player deaths and deaths not caused by the local player, with explicit standalone registration.
- A visual catalogue, disk persistence, cross-world save policy, Core integration, and verified projectile/indirect-damage attribution are not included.

#### Staff, moderation, and investigation

##### Staff Dashboard Expansion
- The branch contains the named Staff Dashboard Expansion prototype directory, but its current folder contains only a placeholder file.
- No additional dashboard feature implementation is claimed from that directory. The separately documented Staff Operations dashboard work above is existing source and is not silently attributed to this placeholder.

##### Access Review Reminder
- Added access-review entries, reminder logic, local storage, and a dedicated GUI.
- The prototype separates review records, reminder handling, and presentation; scheduled delivery, external identity synchronization, and production staff integration are not established.

##### Acting Rank Assignment
- Added acting-rank entries, status and settings models, a registry, service, validator, and GUI.
- The source provides the structure for temporary/acting rank assignments and validation; live permission enforcement, expiry scheduling, and remote rank synchronization are not claimed.

##### Acknowledgement Tracker
- Added an acknowledgement-tracking prototype source area for recording acknowledgement state.
- A verified notification source, persistent acknowledgement workflow, and staff-dashboard integration are not established by the current source inventory.

##### Active Device List and Global Sign-Out
- Added active-device session records, status/result models, a registry, service, validator, settings, and GUI.
- The source models device/session review and sign-out outcomes; actual account-session revocation requires an external authoritative service and is not implemented or verified by this standalone source.

##### Alpha Crash Reporter
- Added explicit throwable capture and an optional JVM uncaught-exception adapter.
- Added structured reports containing timestamps, client/Minecraft/Forge versions, source, category, severity, module, and environment metadata.
- Added local versioned report storage, history, retention/deletion, fingerprint-based duplicate grouping, heuristic classification, suggested fixes, best-effort privacy redaction, and plain-text export.
- Added history and report-details screens with paged stack-trace output. The reporter is local-only; automatic startup/module instrumentation, remote upload, and Core registration are not included.

##### Case Escalation Timer
- Added a case-escalation timing prototype source area.
- The current source inventory does not establish a verified case lifecycle adapter, scheduled escalation delivery, or integrated staff workflow.

##### Blind Case Review
- Added a blind-case-review prototype source area for separating review from identifying context.
- A complete case-data adapter, reviewer workflow, and verified UI integration are not established by the current source inventory.

##### Appeal Conflict Detector
- Added an appeal-conflict detection prototype source area.
- A verified appeal-record ingestion path, conflict rules, and integrated review workflow are not established by the current source inventory.

##### Punishment Command Preflight
- Added a Forge outgoing-chat observer that intercepts only explicitly configured punishment-command rules.
- Added quoted-argument parsing, target/duration/reason extraction, a review screen, cancel/confirm actions, and an optional local evidence note.
- Confirmation sends the original command unchanged. No server-specific command formats are assumed; rules are configured in code. Rank gating, persistent configuration, evidence upload, and Core/StaffMods registration are not included.

##### Punishment Conflict Detector
- Added immutable punishment records and supported punishment types, plus a detector for overlapping active records.
- A conflict requires the same target (case-insensitive), the same punishment type, and both records to be active, not revoked, and not expired; permanent records use expiry timestamp zero.
- Added a read-only conflict overview GUI. The caller must supply records; automatic history retrieval, punishment mutation, rank enforcement, and Core registration are not included.

##### Player History
- Added local player identity/session summaries with UUID, latest name, first/last observation times, session count, and accumulated observed online duration.
- Added a bounded 500-event timeline, local Properties persistence, a searchable GUI, and a Forge client-tick tracker sampling the visible player list every 20 ticks.
- The first sample is labelled FIRST_OBSERVED; later visible-list transitions are recorded as JOINED/LEFT observations. This is client-observed history only and cannot recover earlier or server-private events.

##### Investigation Case-Management Source
- Added an investigation case-management source collection as a foundation for organizing case-related data and workflows.
- The current source inventory does not establish a complete integrated case lifecycle, remote case service, or verified production investigation workflow.

##### Ally Faction Notes
- Added an ally-faction-notes prototype source area for retaining faction-related notes.
- A verified faction data adapter, synchronized note storage, and integrated staff workflow are not established by the current source inventory.

#### Store, social, and platform

##### AlphaClans
- Added clan creation with validated names/tags and one-clan-per-player membership.
- Added owner, officer, and member roles with permission checks; seven-day invitations with accept/decline and pending-invite limits; membership, promotion/demotion, ownership-transfer, rename, tag-change, and disband operations.
- Added bounded clan-chat history, quoted command parsing/handling, a UI-ready screen snapshot, and versioned local binary persistence with validation and atomic file replacement.
- The prototype uses a local file repository only. Cross-player synchronization, server-authoritative storage, and Minecraft command/GUI/event registration are not included.

##### AlphaCoins
- Added a shared currency core with wallet, validated coin amounts, transaction records, bounded history, service API, versioned local persistence, and legacy wallet migration.
- Added daily challenge definitions/rotation, progress tracking, GUI source, and challenge categories for playtime, block breaking, crafting, and hostile mobs.
- Added command parsing for balance/challenges, account lookup and coin operations through an adapter interface, plus playtime-based recurring rewards and milestones.
- Wallets and challenge/playtime state are local prototype data. Remote account operations, payments, Store entitlements, Supabase, live command registration, and Core integration are not included. The README records that normal challenge progress currently does not automatically claim completed rewards.

##### Alpha Store
- Added the Store navigation/source collection with Addons, Cosmetics, Owned, and Partners sections.
- Added catalogue/detail/search source and migrated cosmetic component files into the catalogue, including optional texture previews, ownership/loadout state, and player-skin preview source.
- Purchase processing, remote entitlements, and live cosmetic attachment are not claimed as implemented.

##### Cosmetics Catalogue
- Added the cosmetics catalogue source as part of the Store prototype, including cosmetic component records and catalogue presentation/source organization.
- Ownership and loadout state are represented locally in the prototype; purchase validation, remote entitlement synchronization, and live attachment are not established.

##### Addons Catalogue
- Added the Addons catalogue section within the Store navigation.
- A complete addon installation/update pipeline, remote catalogue service, and verified addon activation are not claimed by the current source entry.

##### Partners Directory
- Added a Partners directory section within the Store source collection.
- The current source does not establish a verified remote partner feed, application/approval workflow, or synchronized partner data.

##### Account Linking
- Added account-link records, status values, registry, validation, service, settings, and a standalone GUI.
- The source provides a local account-linking model and presentation boundary; identity-provider verification, secure remote linking, and cross-device synchronization are not included or verified.

##### Alpha Policy Website and Policy Documents
- Added the Alpha policy documentation structure and website preview, including policy drafts and a reader layout.
- Publication hosting, a live policy API, and automatic synchronization with client/launcher policy acceptance are not claimed.

##### AlphaCrashReporter
- The AlphaCrashReporter name refers to the same standalone crash-reporting prototype documented above; it is not a second independent reporter implementation.
- Its source includes local report capture, classification, history, export, and report-viewing components; remote telemetry and Core startup integration are not included.


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
