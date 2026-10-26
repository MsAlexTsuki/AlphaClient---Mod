package com.alpharip.staffmod.combat;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Counts obvious local-player kill messages in visible chat; server formats vary. */
public final class KillCounterMod {
    private int kills;
    private String lastMessage = "";
    private long lastCountAt;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || event.message == null) return;
        AlphaModule module = AlphaModuleRegistry.find("kill-counter");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) return;
        String plain = net.minecraft.util.StringUtils.stripControlCodes(event.message.getUnformattedText());
        String player = mc.thePlayer.getName();
        boolean credited = plain.contains("was slain by " + player)
                || plain.contains("was killed by " + player)
                || plain.contains(player + " killed ");
        long now = System.currentTimeMillis();
        if (credited && (!plain.equals(lastMessage) || now - lastCountAt > 1500L)) {
            kills++;
            lastMessage = plain;
            lastCountAt = now;
        }
    }

    public int getKills() { return kills; }
    public void reset() { kills = 0; lastMessage = ""; lastCountAt = 0L; }
}
