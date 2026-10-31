# AutoTool

AutoTool is a client-side Minecraft 1.8.9 helper. When enabled, it selects a suitable hotbar item when the local player starts mining a block.

## Current behavior

- Checks all nine hotbar slots.
- Prefers items that can harvest the targeted block, then compares block-breaking strength.
- Treats an empty hand as the normal baseline and ignores invalid or non-finite strength values.
- Synchronizes a changed slot before vanilla sends its block-breaking action.
- Optional switch-back to the original slot after mining stops.
- Leaves a manual hotbar selection alone.
- Does not move items or alter inventory stacks.
- Disabled by default. Settings are stored in `config/alphaclient/autotool.properties`.

Open the settings screen with `/autotool settings`. The GUI has toggles for AutoTool and switch-back; press **Done** or **Esc** to return to the previous screen.

Commands:

- `/autotool on|off|toggle`
- `/autotool revert on|off`

Selection uses Minecraft 1.8.9's `ItemStack.canHarvestBlock` and `ItemStack.getStrVsBlock`. Server-specific rules and custom item behavior may still differ.

## Worked-out ideas

### 1. Per-block tool preferences — high value
Let the player choose a preferred hotbar slot or tool type for a block (for example, always use a silk-touch pickaxe for ore). Use the preference only when that item is present and can harvest the block; otherwise fall back to normal selection. Store preferences locally and provide a clear reset option.

### 2. Tool durability protection — high value
Add a configurable minimum durability reserve. AutoTool should avoid a nearly broken tool when another tool can still harvest the block. If every suitable tool is below the threshold, choose the best available one instead of refusing to help.

### 3. Switch-back modes — high value
Offer three predictable modes: never switch back, switch back shortly after attack is released, or switch back after the targeted block is destroyed. A short delay should be configurable and canceled if the player changes slots manually or starts mining another block.

### 4. Temporary pause key — medium value
Add a keybind to pause AutoTool while held, plus an optional toggle mode. Show a small status indicator so the player knows why automatic selection is paused. Manual slot changes should always take priority.

### 5. Ignore and preferred-item lists — medium value
Allow items to be excluded (such as a silk-touch tool) and optional preferred items to be named for certain blocks. Keep selection safe: ignore entries should never cause an unsuitable tool to be selected when a valid alternative exists.

### 6. Efficiency-aware selection — medium value
Use known enchantment bonuses when comparing tools, but only where the game client can calculate them correctly. Keep harvestability as a hard preference and fall back to the current vanilla strength comparison for unknown modded item behavior.

### 7. Context rules — medium value
Add opt-in rules to pause switching while using an item, sneaking, or playing in selected game modes. Default these rules off so they do not unexpectedly disable AutoTool.

### 8. Selection feedback — low effort
Optionally show the chosen tool name and a short reason such as “can harvest” or “faster” in the action bar. Add a cooldown so the message does not repeat every tick or for every block in a vein.

### 9. Durability tie-breaker — low effort
When two items have equal harvestability and nearly identical strength, prefer the one with more durability remaining. This complements the durability threshold without replacing it.

### 10. Per-world profiles — later
Save settings and preferences separately per server/world, with a global fallback profile. Profiles should be local-only and identify servers carefully so unrelated servers do not accidentally share settings.

## Suggested order

Start with durability protection, per-block preferences, and switch-back modes because they change the core tool choice in useful, understandable ways. Add pause and feedback afterward. Keep profiles and enchantment-aware ranking for later, since they need more settings and careful compatibility handling.
