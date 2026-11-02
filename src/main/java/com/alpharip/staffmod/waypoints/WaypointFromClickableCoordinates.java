package com.alpharip.staffmod.waypoints;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.event.ClickEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Adds a clickable "save waypoint" chat action to messages containing three coordinates. */
public final class WaypointFromClickableCoordinates {
    private static final WaypointFromClickableCoordinates INSTANCE=new WaypointFromClickableCoordinates();
    public static WaypointFromClickableCoordinates getInstance(){return INSTANCE;}
    private static final Pattern COORDINATES = Pattern.compile("(-?\\d{1,7})\\s*[, ]\\s*(-?\\d{1,7})\\s*[, ]\\s*(-?\\d{1,7})");
    public boolean enabled = WaypointSettings.getBoolean("clickableCoordinatesEnabled",true);
    public boolean confirmBeforeSaving = true;
    public void setEnabled(boolean value) { enabled=value; WaypointSettings.setBoolean("clickableCoordinatesEnabled",value); }
    public String defaultWaypointName = "Chat waypoint";

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!enabled || event == null || event.message == null || event.type == 2) return;
        Matcher matcher=COORDINATES.matcher(event.message.getUnformattedText());
        if(!matcher.find()) return;
        final String x=matcher.group(1), y=matcher.group(2), z=matcher.group(3);
        ChatComponentText action=new ChatComponentText("  [save waypoint]");
        action.setChatStyle(new ChatStyle().setColor(net.minecraft.util.EnumChatFormatting.GOLD)
                .setChatClickEvent(new ClickEvent(confirmBeforeSaving ? ClickEvent.Action.SUGGEST_COMMAND : ClickEvent.Action.RUN_COMMAND,
                        "/waypoint addat "+x+" "+y+" "+z+" \""+defaultWaypointName.replace("\"","")+"\"")));
        event.message.appendSibling(action);
    }
}
