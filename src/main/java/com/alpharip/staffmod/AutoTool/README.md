# AutoTool

AutoTool is a client-side Minecraft 1.8.9 helper. When enabled, it selects a suitable item from the hotbar when the local player starts mining a block.

## Features

- Checks all nine hotbar slots.
- Prefers items that can harvest the block, then compares their block-breaking strength.
- Treats an empty hand as the normal baseline and ignores invalid or non-finite strength values.
- Synchronizes an automatically selected slot before vanilla sends its block-breaking action.
- Optional switch-back to the original slot after mining stops. If the player manually changes the selected slot, AutoTool leaves that choice alone.
- Does not move items or alter inventory stacks.
- Disabled by default. Settings are stored in `config/alphaclient/autotool.properties`.

Use `/autotool settings` to open the settings screen, or:

- `/autotool on|off|toggle`
- `/autotool revert on|off`

Selection uses Minecraft 1.8.9's `ItemStack.canHarvestBlock` and `ItemStack.getStrVsBlock`. Server-specific rules and custom item behavior may still differ.

## Ideas for future improvements

- **Per-block tool preferences:** let players prefer a specific tool type for selected blocks.
- **Tool durability protection:** avoid tools near breaking, with a configurable remaining-durability threshold.
- **Efficiency-aware selection:** consider Efficiency enchantments and other known speed bonuses where the client can calculate them reliably.
- **Ignore and force lists:** exclude selected items or blocks, or pin a preferred tool for a block.
- **Temporary pause key:** pause automatic switching while the key is held or toggled.
- **Switch-back modes:** choose between returning on attack release, after a short delay, or only after the block breaks.
- **Context rules:** disable switching while using an item, sneaking, or in selected game modes.
- **Feedback option:** briefly show the chosen tool and reason in the action bar.
- **Durability tie-breaker:** when tools perform equally, prefer the one with more durability remaining.
