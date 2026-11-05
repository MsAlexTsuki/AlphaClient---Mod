# AutoTool

AutoTool is a client-side Minecraft 1.8.9 helper. It chooses a suitable item from the hotbar when the local player starts mining a block.

## Implemented behavior

- Checks all nine hotbar slots and does not move items or change stack contents.
- Prefers an item that can harvest the block. When suitable options exist, it avoids tools below the configured remaining-durability reserve.
- Supports an item preference for each block and an ignore list for tools.
- Compares base block-breaking strength and, when enabled, the vanilla Efficiency enchantment bonus.
- When strength and harvestability are tied, it can prefer the tool with more durability remaining.
- Synchronizes a changed slot before vanilla sends the block-breaking action.
- Offers three switch-back modes: never, after attack is released, or after the block breaks. Release mode has a configurable tick delay.
- Can be paused with the **AutoTool: Pause** keybind (default **P**). Choose hold-to-pause or toggle-to-pause. While paused, a HUD notice explains the state.
- Optional context pauses: while using an item, while sneaking, or in Survival, Creative, or Adventure.
- Optional chat feedback names the selected tool and why it was chosen. A cooldown prevents message spam.
- Disabled by default.

## GUI and commands

Open the three-page settings GUI with `/autotool settings`. It includes the main toggles, switch-back mode and delay, durability reserve, Efficiency ranking, tie-breaker, pause mode, and context rules. The GUI also displays whether settings use global defaults or the local profile. Press **Esc** or **Done** to return.

Commands:

- `/autotool on|off|toggle`
- `/autotool profile` — shows whether global settings or a local profile is active.
- `/autotool swapback <never|release|broken> [delayTicks]` — delay is 0–100 ticks and applies to release mode.
- `/autotool durability <0-100>` — minimum remaining durability percentage.
- `/autotool pausemode <hold|toggle>`
- `/autotool prefer <block-id> <item-id|clear>` — example: `/autotool prefer minecraft:stone minecraft:diamond_pickaxe`.
- `/autotool ignore <add|remove> <item-id>` — example: `/autotool ignore add minecraft:diamond_pickaxe`.

Use registry IDs (including the namespace, such as `minecraft:`). Preferences are used only when the chosen stack can harvest the target block. If all suitable tools are below the durability reserve, AutoTool still chooses the best available tool.

## Settings and profiles

Settings are stored locally in `config/alphaclient/autotool.properties`. These act as global defaults. Per-server or per-singleplayer-world overrides are saved under `config/alphaclient/autotool-profiles/`; filenames use a SHA-256 hash of the server address or singleplayer save folder name, so the raw identity is not written into the profile filename. A local profile overrides only the values it sets and inherits the rest from the global settings.

Older `swapBack=true` settings are read as the `release` mode. New settings are written as profile overrides when a world/server is active.

## Selection notes

AutoTool uses Minecraft 1.8.9's `ItemStack.canHarvestBlock` and `ItemStack.getStrVsBlock`, then adds the known vanilla Efficiency bonus when requested. Server-side plugins, custom items, or modded block rules can still disagree with client-side estimates. Item preferences, ignored item IDs, and profile values are local to the client.
