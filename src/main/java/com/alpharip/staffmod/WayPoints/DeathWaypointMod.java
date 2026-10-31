package com.alpharip.staffmod.WayPoints;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Saves one local waypoint at every player death. */
public final class DeathWaypointMod {
    private static final DeathWaypointMod INSTANCE=new DeathWaypointMod();
    public static DeathWaypointMod getInstance(){return INSTANCE;}
    private boolean wasAlive=true;
    private boolean enabled=WaypointSettings.getBoolean("deathEnabled",true);

    public void setEnabled(boolean value){enabled=value;WaypointSettings.setBoolean("deathEnabled",value);}
    public boolean isEnabled(){return enabled;}

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END) return;
        Minecraft mc=Minecraft.getMinecraft();
        if(mc==null||mc.thePlayer==null||mc.theWorld==null){wasAlive=true;return;}
        boolean alive=!mc.thePlayer.isDead&&mc.thePlayer.getHealth()>0.0F;
        if(enabled&&wasAlive&&!alive) {
            StaffWaypoint point=WaypointManager.getInstance().add("Death",mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ);
            point.setCategory("Death"); point.setColor(WaypointCategoriesColors.colorFor("Death")); point.setDeathWaypoint(true); WaypointManager.getInstance().saveChanges();
            mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                    "§cDeath waypoint saved: §f"+Math.floor(mc.thePlayer.posX)+", "+Math.floor(mc.thePlayer.posY)+", "+Math.floor(mc.thePlayer.posZ)));
        }
        wasAlive=alive;
    }
}
