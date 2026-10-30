package com.alpharip.staffmod.staffops.ideas;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Captures the last known death position and announces it once per death. */
public final class DeathWaypointMod {
    private boolean wasAlive = true;
    private int lastX;
    private int lastY;
    private int lastZ;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) {
            wasAlive = true;
            return;
        }

        AlphaModule module = AlphaModuleRegistry.find("death-waypoint");
        if (module == null || !module.isEnabled() || !module.isUnlocked()) {
            wasAlive = true;
            return;
        }

        boolean alive = !mc.thePlayer.isDead && mc.thePlayer.getHealth() > 0.0F;
        if (wasAlive && !alive) {
            lastX = (int) Math.floor(mc.thePlayer.posX);
            lastY = (int) Math.floor(mc.thePlayer.posY);
            lastZ = (int) Math.floor(mc.thePlayer.posZ);
            mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                    "§cDeath location: §f" + lastX + ", " + lastY + ", " + lastZ));
        }
        wasAlive = alive;
    }

    public int getLastX() { return lastX; }
    public int getLastY() { return lastY; }
    public int getLastZ() { return lastZ; }
}
