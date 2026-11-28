package com.alpharip.staffmod.punishmentcommandpreflight;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Client-side command guard. Register this prototype explicitly from the client bootstrap.
 * It never grants server permissions and never changes the command being confirmed.
 */
public final class PunishmentCommandPreflight {
    public static final String NAME = "Punishment Command Preflight";
    public static final String CATEGORY = "StaffMods";
    public static final String REQUIRED_ACCESS = "Trial Moderator";
    private static final PunishmentCommandPreflight INSTANCE = new PunishmentCommandPreflight();

    private final List<PunishmentCommandRule> rules = new ArrayList<PunishmentCommandRule>();
    private final PunishmentCommandParser parser = new PunishmentCommandParser();
    private boolean registered;
    private boolean bypassNextCommand;

    private PunishmentCommandPreflight() { }
    public static PunishmentCommandPreflight getInstance() { return INSTANCE; }

    public synchronized void register() {
        if (registered) return;
        MinecraftForge.EVENT_BUS.register(this);
        registered = true;
    }

    public synchronized void unregister() {
        if (!registered) return;
        MinecraftForge.EVENT_BUS.unregister(this);
        registered = false;
    }

    public synchronized void addRule(PunishmentCommandRule rule) {
        if (rule == null) throw new IllegalArgumentException("Rule must not be null.");
        for (PunishmentCommandRule existing : rules) {
            if (existing.getCommandName().equals(rule.getCommandName())) {
                throw new IllegalArgumentException("A rule already exists for /" + rule.getCommandName());
            }
        }
        rules.add(rule);
    }

    public synchronized boolean removeRule(String commandName) {
        if (commandName == null) return false;
        String normalized = commandName.replaceFirst("^/", "").trim();
        for (int i = 0; i < rules.size(); i++) {
            if (rules.get(i).getCommandName().equalsIgnoreCase(normalized)) {
                rules.remove(i);
                return true;
            }
        }
        return false;
    }

    public synchronized List<PunishmentCommandRule> getRules() {
        return Collections.unmodifiableList(new ArrayList<PunishmentCommandRule>(rules));
    }

    @SubscribeEvent
    public void onOutgoingChat(ClientChatEvent event) {
        if (event == null || event.message == null) return;
        synchronized (this) {
            if (bypassNextCommand) {
                bypassNextCommand = false;
                return;
            }
        }
        PunishmentCommandDraft draft = parser.parse(event.message, getRules());
        if (draft == null) return;
        event.setCanceled(true);
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft != null && minecraft.thePlayer != null) {
            minecraft.displayGuiScreen(new PunishmentCommandPreflightGui(null, this, draft));
        }
    }

    /** Called only after the user presses the explicit confirmation button. */
    public void confirm(PunishmentCommandDraft draft) {
        if (draft == null || draft.getOriginalCommand().isEmpty()) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.thePlayer == null) return;
        synchronized (this) { bypassNextCommand = true; }
        minecraft.thePlayer.sendChatMessage(draft.getOriginalCommand());
    }

    public void cancel() {
        // The intercepted command remains cancelled. No packet is sent.
    }
}
