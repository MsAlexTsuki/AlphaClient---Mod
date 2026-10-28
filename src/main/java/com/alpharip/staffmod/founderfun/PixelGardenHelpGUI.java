// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Scrollable command reference opened from Pixel Garden's Help button. */
public final class PixelGardenHelpGUI extends GuiScreen {
    private static final int BACK = 1;
    private final GuiScreen parent;
    private int scrollOffset;

    public PixelGardenHelpGUI(GuiScreen parent) {
        this.parent = parent;
    }

    private List<String> lines() {
        List<String> lines = new ArrayList<String>();
        lines.add("BASICS");
        lines.add("/pixelgarden open - Open the garden");
        lines.add("/pixelgarden quests open - Open the Quest Book");
        lines.add("PLAYER QUEST COMMANDS");
        lines.add("/pixelgarden Quest open - Open the Quest Book (same as quests open)");
        lines.add("/pixelgarden Quest active - List active quests");
        lines.add("/pixelgarden Quest list - List assigned quests and their status");
        lines.add("/pixelgarden Quest info <number> - Show quest objectives, progress and rewards");
        lines.add("/pixelgarden Quest accept [number] - Accept the first available quest or a numbered quest");
        lines.add("/pixelgarden Quest Remove <number> - Remove a quest after the delayed confirmation");
        lines.add("/pixelgarden Quest givequest <number> <crop> <amount> - Deliver harvested crops to an active quest");
        lines.add("/pixelgarden Quest complete <number> - Complete an eligible quest and claim its rewards");
        lines.add("/pixelgarden Quest history - Show completed and abandoned quest history");
        lines.add("/pixelgarden Quest progress - Show progress for active quests");
        lines.add("LEVELS AND XP");
        lines.add("/pixelgarden level - Show your current level");
        lines.add("/pixelgarden level check <Alpha account name> - Check an account's level");
        lines.add("/pixelgarden xp - Show XP remaining for the next level");
        lines.add("/pixelgarden xp check <Alpha account name> - Check another account's remaining XP");
        lines.add("/pixelgarden level list - Open the level and reward GUI");
        lines.add("/pixelgarden resetleveltoken - Check your level reset tokens");
        lines.add("/pixelgarden resetleveltoken use - Spend a token to reset level and XP");
        lines.add("/pixelgarden edit level message <message> - Set your personal level-up message");
        lines.add("/pixelgarden idlist - Open the dedicated Quest ID List GUI with quest IDs, NPC IDs and rewards");
        lines.add("/pixelgarden ids - Open the scrollable quest ID and NPC reference registry");
        lines.add("/pixelgarden help - Open this command guide GUI");
        lines.add("/pixelgarden guide - Open this scrollable guide");
        lines.add("/garden - Alias for /pixelgarden");
        lines.add("/pixelgarden inventory - Open inventory");
        lines.add("/pixelgarden inv - List seeds and harvested crops");
        lines.add("/pixelgarden shop - Open the seed shop");
        lines.add("/pixelgarden buy <crop> [amount] - Buy seeds");
        lines.add("/pixelgarden sell <crop|all> [amount] - Sell harvested crops");
        lines.add("/pixelgarden plant <plot> <crop> - Plant on an empty plot");
        lines.add("/pixelgarden harvest <plot|all> - Harvest ready crops");
        lines.add("/pixelgarden time - Show growth timers");
        lines.add("/pixelgarden crops - Show harvested crop counts");
        lines.add("/pixelgarden seeds - Show planting-seed counts");
        lines.add("/pixelgarden coins [Alpha account] - Check a coin balance");
        lines.add("/pixelgarden playtime - Show your AlphaClient playtime");
        lines.add("CHAT REMINDERS");
        lines.add("/pixelgarden msg on - Enable Pixel Garden chat reminders");
        lines.add("/pixelgarden msg off - Disable Pixel Garden chat reminders");
        lines.add("/garden msg <on|off> - Toggle reminders using the short alias");
        lines.add("Reminders include ready crops and one daily message at Netherlands midnight.");
        lines.add("UPGRADES AND PRESTIGE");
        lines.add("/pixelgarden upgrades - Open the upgrades screen");
        lines.add("/pixelgarden upgrade <upgrade> - Purchase an upgrade");
        lines.add("/pixelgarden upgrade prestige_yield|prestige_bonus|coin_cache - Buy exclusive Prestige upgrades");
        lines.add("/pixelgarden prestige - Open Prestige upgrades when unlocked");
        lines.add("/pixelgarden prestige use - Prestige for 20,000 coins after 18 upgrade steps");
        lines.add("/pixelgarden resettokens - Check your reset-token balance");
        lines.add("/pixelgarden resettoken use - Open reset confirmation");
        lines.add("/pixelgarden level list - View all levels and rewards");
        if (SupabaseCommunityService.isSignedInFounder()) {
            lines.add("FOUNDER QUEST COMMANDS");
            lines.add("/pixelgarden createquest admin <quest id> <description> <NPC> <amount> <reward type> <weight> - Create a shared crop-delivery quest");
            lines.add("/pixelgarden addnpc admin <npc_id> <quest_id> - Assign an existing NPC to an existing quest");
            lines.add("/pixelgarden removenpc admin <npc_id> <quest_id> - Remove an NPC assignment from a quest");
            lines.add("/pixelgarden editnpc admin <npc_id> <new_npc_id> - Rename an NPC ID and update its quest assignments");
            lines.add("/pixelgarden createnpc admin <new_npc_id> - Register a new NPC ID");
            lines.add("/pixelgarden Givequest admin <Alpha account name> <quest id> - Assign a quest to an account");
            lines.add("/pixelgarden takequest admin <Alpha account name> <quest id> - Remove an available or active quest");
            lines.add("/pixelgarden completequest admin <quest id> - Complete this quest on your Founder account when objectives are met");
            lines.add("/pixelgarden rewards add <quest id> <type> <amount> - Add to a quest reward");
            lines.add("/pixelgarden rewards remove <quest id> <type> <amount|all> - Remove some or all of a quest reward");
            lines.add("Reward types: coins, seeds_wheat/carrot/potato/pumpkin, crop_wheat/carrot/potato/pumpkin");
            lines.add("FOUNDER LEVEL COMMANDS");
            lines.add("/pixelgarden setlevel <Alpha account name> <level> - Set level and reset XP to 0");
            lines.add("/pixelgarden resetlevel <Alpha account name> - Reset level to 1 and XP to 0");
            lines.add("/pixelgarden resetleveltoken give <Alpha account name> [amount] - Give level reset tokens");
            lines.add("/pixelgarden resetleveltoken take <Alpha account name> [amount] - Take level reset tokens");
            lines.add("/pixelgarden editlevel rewards <level> <rewards> - Edit level rewards");
            lines.add("/pixelgarden editlevel xp <level> <xp> - Edit XP required for the next level");
            lines.add("/pixelgarden editlevel desc <level> <desc> - Edit the level description");
            lines.add("/pixelgarden editlevelmsg <message> - Change the default level-up message");
            lines.add("/pixelgarden admin level create <level> <rewards> <xp> - Create a level");
            lines.add("/pixelgarden admin level delete <level> - Delete a level");
            lines.add("FOUNDER COMMANDS");
            lines.add("/pixelgarden admin fullreset <Alpha account name> - Reset garden, inventory, upgrades, levels, quests, tutorial and Prestige");
            lines.add("/pixelgarden harvesttime <multiplier|finish|normal> - Speed up, finish, or restore normal crop timers");
            lines.add("/pixelgarden coinsgive <Alpha account> <amount> - Give regular coins");
            lines.add("/pixelgarden takecoins <Alpha account> <amount> - Remove regular coins");
            lines.add("/pixelgarden coinsreset <Alpha account> - Reset regular coins");
            lines.add("/pixelgarden give resettoken <Alpha account> - Give a reset token");
            lines.add("/pixelgarden resettoken admin <Alpha account> - Force-reset a garden");
            lines.add("/pixelgarden prestigecoingive <Alpha name> <amount> - Give Prestige Coins");
            lines.add("/pixelgarden prestigecointake <Alpha name> <amount> - Take Prestige Coins");
            lines.add("/pixelgarden prestigecoinreset <Alpha name> - Reset Prestige Coins only");
            lines.add("/pixelgarden prestigereset <Alpha name> - Reset Prestige progress");
            lines.add("/pixelgarden admin fullreset <Alpha name> - Reset all garden and Prestige data");
            lines.add("/pixelgarden multiplier prestige add <Alpha name> <multiplier> - Set personal multiplier (max 5x)");
            lines.add("/pixelgarden multiplier prestige reset <Alpha name> - Restore rank multiplier");
        }
        lines.add("Use the mouse wheel to scroll through this guide.");
        return lines;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new AlphaButton(BACK, width / 2 - 70, height - 32, 140, 22, "Back to Garden", true));
        int max = Math.max(0, lines().size() - visibleLineCount());
        scrollOffset = Math.max(0, Math.min(scrollOffset, max));
    }

    private int visibleLineCount() {
        return Math.max(5, (height - 105) / 13);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int max = Math.max(0, lines().size() - visibleLineCount());
            scrollOffset = Math.max(0, Math.min(max, scrollOffset + (wheel < 0 ? 3 : -3)));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button != null && button.id == BACK && mc != null) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(0, 0, width, 46, 0xF00B1018);
        drawRect(0, 44, width, 47, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "PIXEL GARDEN | COMMAND GUIDE", width / 2, 15, 0xFFFFC83D);
        drawCenteredString(fontRendererObj, "Commands and permissions available to your account", width / 2, 31, 0xFFB7C4D5);

        int left = Math.max(18, width / 2 - 220);
        int right = Math.min(width - 18, width / 2 + 220);
        int top = 54;
        int bottom = height - 43;
        drawRect(left, top, right, bottom, 0xE510141C);
        drawRect(left, top, right, top + 1, 0xFFFFC83D);
        List<String> all = lines();
        int end = Math.min(all.size(), scrollOffset + visibleLineCount());
        int y = top + 9;
        for (int i = scrollOffset; i < end; i++) {
            String line = all.get(i);
            boolean heading = line.equals("BASICS") || line.equals("UPGRADES AND PRESTIGE")
                    || line.equals("FOUNDER COMMANDS");
            int color = heading ? 0xFFFFC83D : (line.startsWith("/pixelgarden") || line.startsWith("/garden")
                    ? 0xFFE5EAF2 : 0xFFB7C4D5);
            drawString(fontRendererObj, line, left + 10, y, color);
            y += 13;
        }
        if (all.size() > visibleLineCount()) {
            String scroll = "Scroll " + (scrollOffset + 1) + "-" + end + " / " + all.size();
            drawCenteredString(fontRendererObj, scroll, width / 2, height - 44, 0xFF9DA8B8);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
