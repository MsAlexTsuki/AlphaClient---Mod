# AutoTool

AutoTool is a client-side Minecraft 1.8.9 helper. When enabled, it selects the strongest matching item from the hotbar when the local player starts mining a block.

## Features

- Checks all nine hotbar slots and switches only when a stack has a higher block-breaking strength than the current item.
- Does not move items or alter inventory stacks. Minecraft synchronizes the selected slot normally.
- Optional switch-back to the original slot after mining stops. If the player manually changes the selected slot, AutoTool leaves it alone.
- Disabled by default. Settings are stored in `config/alphaclient/autotool.properties`.

Use `/autotool settings` to open the settings screen, or:

- `/autotool on|off|toggle`
- `/autotool revert on|off`

The strength choice uses Minecraft 1.8.9's `ItemStack.getStrVsBlock`. It does not account for every server-specific rule, enchantment behavior, or custom item mechanic.
