// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun.eastereggs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** First-pass Easter Eggs registry and local death-screen controller. */
public final class EasterEggManager {
    public static final int CHANCE_DENOMINATOR = 15000;
    private static final Random RANDOM = new Random();
    private static final List<Egg> EGGS = new ArrayList<Egg>();
    private static final boolean[] USER_ENABLED = new boolean[26];
    private static final boolean[] MASTER_ENABLED = new boolean[26];
    private static final String[] CUSTOM_TEXT = new String[26];
    private static boolean globallyEnabled;
    private static boolean loaded;
    private static GuiScreen lastDeathScreen;
    private static int activeEggId = -1;
    private static boolean closeButtonIsZero;
    private static int queuedActivation = -1;
    private static String queuedRemoteActivationId;
    private static String lastDisplayedRemoteActivationId;
    private static boolean remoteActivationInFlight;
    private static boolean deathRollConsumed;
    private static int lastDeathEggId = -1;
    private static boolean specialButtonAssignmentMade;

    static {
        add(1, "Please uninstall your game, it's a skill issue! But don't worry you are bad at every game!");
        add(2, "Your mom told you to stop! Now you died, go cry a river");
        add(3, "AlphaClient does not agree with your skills");
        add(4, "AlphaClient is not the problem, it's you! Of course! It's always you and you know it!");
        add(5, "Great, another death. Congratulations!");
        add(6, "Is it your birthday? Oh, it's not? Too bad you dead mothafacka!");
        add(7, "Go kiss your mom, she missed you");
        add(8, "Crazy, another day, another death in a game");
        add(9, "Just too bad you died today");
        add(10, "Need some help with Minecraft? Do you know how to play?");
        add(11, "Crazy that you cannot aim at a person");
        add(12, "It's MY MOUSE!");
        add(13, "POT ME, NOOOOO YOU DIDN'T POT MEEEE");
        add(14, "CRAZY THAT I DROPPED");
        add(15, "Nahhh you can't do that shii");
        add(16, "you died? Not my fault");
        add(17, "Help a hand at your local community");
        add(18, "Do not pray for skills, you just died because of skill issue");
        add(19, "Turn left to go right, YEET");
        add(20, "yee");
        add(21, "Too bad this isn't hardcore, you can respawn... but which one is it?");
        add(22, "Hey Vsauce, Michael here. And your home security is great! Or is it?");
        add(23, "MsAlexTsuki agrees with this death");
        add(24, "AlphaClient and MsAlexTsuki just agreed that you should die, so you did");
        add(25, "Feed your pets. You might've forgotten them");
        for (int id = 1; id <= 25; id++) MASTER_ENABLED[id] = true;
    }

    private EasterEggManager() { }
    private static void add(int id, String text) { EGGS.add(new Egg(id, text)); }

    public static synchronized void initialize() {
        load();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new EasterEggManager());
    }

    public static synchronized List<Egg> getEggs() {
        return Collections.unmodifiableList(new ArrayList<Egg>(EGGS));
    }
    public static synchronized Egg getEgg(int id) {
        for (Egg egg : EGGS) if (egg.id == id) return egg;
        return null;
    }
    public static synchronized boolean isGloballyEnabled() { ensureLoaded(); return globallyEnabled; }
    public static synchronized void setGloballyEnabled(boolean value) { ensureLoaded(); globallyEnabled = value; save(); }
    public static synchronized boolean isUserEnabled(int id) { ensureLoaded(); return valid(id) && USER_ENABLED[id]; }
    public static synchronized void setUserEnabled(int id, boolean value) { ensureLoaded(); if (valid(id)) { USER_ENABLED[id] = value; save(); } }
    public static synchronized boolean isMasterEnabled(int id) { ensureLoaded(); return valid(id) && MASTER_ENABLED[id]; }
    public static synchronized void setMasterEnabled(int id, boolean value) { ensureLoaded(); if (valid(id)) { MASTER_ENABLED[id] = value; save(); } }
    public static synchronized String getText(int id) {
        ensureLoaded();
        Egg egg = getEgg(id);
        return egg == null ? null : (CUSTOM_TEXT[id] == null ? egg.defaultText : CUSTOM_TEXT[id]);
    }
    public static synchronized boolean setText(int id, String value) {
        ensureLoaded();
        if (getEgg(id) == null || value == null || value.trim().isEmpty()) return false;
        CUSTOM_TEXT[id] = value.trim(); save(); return true;
    }
    public static synchronized boolean queueActivation(int id) {
        ensureLoaded();
        if (getEgg(id) == null || queuedRemoteActivationId != null) return false;
        queuedActivation = id; return true;
    }

    /** Stores a server-delivered activation until the next death screen. */
    public static synchronized boolean queueRemoteActivation(String activationId, int id) {
        ensureLoaded();
        if (activationId == null || activationId.trim().isEmpty() || getEgg(id) == null) return false;
        if (activationId.equals(lastDisplayedRemoteActivationId)) return false;
        if (queuedRemoteActivationId != null) {
            if (!queuedRemoteActivationId.equals(activationId)) return false;
            if (remoteActivationInFlight) return true;
            if (queuedActivation <= 0) queuedActivation = id;
            return queuedActivation == id;
        }
        if (queuedActivation > 0) return false;
        queuedRemoteActivationId = activationId;
        queuedActivation = id;
        return true;
    }

    /** Returns whether an activation has already been displayed, to avoid replay after an acknowledgement failure. */
    public static synchronized boolean wasRemoteActivationDisplayed(String activationId) {
        return activationId != null && activationId.equals(lastDisplayedRemoteActivationId);
    }

    /** Clears the pending remote marker only after the server confirms acknowledgement. */
    public static synchronized void clearRemoteActivation(String activationId) {
        if (activationId != null && activationId.equals(queuedRemoteActivationId)) {
            queuedRemoteActivationId = null;
            remoteActivationInFlight = false;
        }
    }

    /** Allows a pending server activation to be retried when acknowledgement fails. */
    public static synchronized void markRemoteActivationAckFailed(String activationId) {
        if (activationId != null && activationId.equals(queuedRemoteActivationId)) {
            remoteActivationInFlight = false;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;
        if (mc.thePlayer != null && mc.thePlayer.getHealth() > 0.0F) {
            deathRollConsumed = false;
            lastDeathEggId = -1;
            specialButtonAssignmentMade = false;
        }
        if (!(mc.currentScreen instanceof GuiGameOver)) {
            lastDeathScreen = null;
            activeEggId = -1;
            return;
        }
        GuiScreen screen = mc.currentScreen;
        if (screen == lastDeathScreen) return;
        lastDeathScreen = screen;
        activeEggId = -1;
        int selected = -1;
        String remoteActivationToAcknowledge = null;
        synchronized (EasterEggManager.class) {
            ensureLoaded();
            if (deathRollConsumed) {
                selected = lastDeathEggId;
            } else {
                deathRollConsumed = true;
                if (queuedActivation > 0) {
                    int queuedId = queuedActivation;
                    queuedActivation = -1;
                    if (globallyEnabled && MASTER_ENABLED[queuedId] && USER_ENABLED[queuedId]) {
                        selected = queuedId;
                    }
                    if (queuedRemoteActivationId != null) {
                        remoteActivationToAcknowledge = queuedRemoteActivationId;
                        remoteActivationInFlight = true;
                    }
                } else if (globallyEnabled && RANDOM.nextInt(CHANCE_DENOMINATOR) == 0) {
                    List<Integer> eligible = new ArrayList<Integer>();
                    for (int id = 1; id <= 25; id++) {
                        if (MASTER_ENABLED[id] && USER_ENABLED[id]) eligible.add(Integer.valueOf(id));
                    }
                    if (!eligible.isEmpty()) selected = eligible.get(RANDOM.nextInt(eligible.size())).intValue();
                }
                lastDeathEggId = selected;
            }
        }
        if (selected > 0) {
            applyToDeathScreen(mc, screen, selected);
            if (remoteActivationToAcknowledge != null) {
                synchronized (EasterEggManager.class) {
                    lastDisplayedRemoteActivationId = remoteActivationToAcknowledge;
                }
            }
        }
        if (remoteActivationToAcknowledge != null) {
                final String activationId = remoteActivationToAcknowledge;
                Thread acknowledge = new Thread(new Runnable() {
                    @Override public void run() {
                        try {
                            com.alpharip.staffmod.permissions.SupabaseCommunityService
                                    .acknowledgeEasterEggActivation(activationId);
                        } catch (Exception error) {
                            EasterEggManager.markRemoteActivationAckFailed(activationId);
                            System.err.println("[AlphaClient] Could not acknowledge remote Easter Egg activation: " + error.getMessage());
                        }
                    }
                }, "AlphaClient-EasterEggAck");
                acknowledge.setDaemon(true);
                acknowledge.start();
            }
        }

    private void applyToDeathScreen(Minecraft mc, GuiScreen screen, int id) {
        String text = getText(id);
        if (text == null) return;
        activeEggId = id;
        if (id == 21) {
            List<GuiButton> buttons = ReflectionHelper.getPrivateValue(
                    GuiScreen.class, screen, "buttonList", "field_146292_n");
            if (buttons == null) return;
            if (!specialButtonAssignmentMade) {
                closeButtonIsZero = RANDOM.nextBoolean();
                specialButtonAssignmentMade = true;
            }
            for (GuiButton button : buttons) {
                if (button.id == 0 || button.id == 1) button.displayString = "YES";
            }
        }
    }

    @SubscribeEvent
    public void onDeathScreenDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (activeEggId <= 0 || !(event.gui instanceof GuiGameOver)) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.fontRendererObj == null) return;
        String text = getText(activeEggId);
        if (text == null) return;
        int centerX = event.gui.width / 2;
        int top = event.gui.height / 4 - 42;
        int maxWidth = Math.max(120, event.gui.width - 24);
        List<String> lines = mc.fontRendererObj.listFormattedStringToWidth(text, maxWidth);
        int lineHeight = mc.fontRendererObj.FONT_HEIGHT;
        int widest = 0;
        for (String line : lines) widest = Math.max(widest, mc.fontRendererObj.getStringWidth(line));
        Gui.drawRect(centerX - widest / 2 - 8, top - 2, centerX + widest / 2 + 8,
                top + lines.size() * lineHeight + 2, 0xAA000000);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            mc.fontRendererObj.drawString(line, centerX - mc.fontRendererObj.getStringWidth(line) / 2,
                    top + i * lineHeight, 0xFFFFFFFF);
        }
    }

    @SubscribeEvent
    public void onDeathScreenButton(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (activeEggId != 21 || !(event.gui instanceof GuiGameOver) || event.button == null) return;
        int id = event.button.id;
        if (id != 0 && id != 1) return;
        event.setCanceled(true);
        if (id == (closeButtonIsZero ? 0 : 1)) {
            Minecraft.getMinecraft().shutdown();
        } else {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null) mc.thePlayer.respawnPlayer();
            mc.displayGuiScreen(null);
        }
    }

    private static boolean valid(int id) { return id >= 1 && id <= 25; }
    private static synchronized void ensureLoaded() { if (!loaded) load(); }
    private static synchronized File getFile() {
        Minecraft mc = Minecraft.getMinecraft();
        File base = mc == null ? new File(".") : mc.mcDataDir;
        return new File(new File(base, "config"), "alphaclient-eastereggs.json");
    }
    private static synchronized void load() {
        File file = getFile();
        if (file.isFile()) {
            FileReader reader = null;
            try {
                reader = new FileReader(file);
                JsonElement parsed = new JsonParser().parse(reader);
                if (parsed != null && parsed.isJsonObject()) {
                    JsonObject root = parsed.getAsJsonObject();
                    if (root.has("globallyEnabled")) globallyEnabled = root.get("globallyEnabled").getAsBoolean();
                    JsonObject user = root.getAsJsonObject("userEnabled");
                    JsonObject master = root.getAsJsonObject("masterEnabled");
                    JsonObject custom = root.getAsJsonObject("customText");
                    for (int id = 1; id <= 25; id++) {
                        String key = String.valueOf(id);
                        if (user != null && user.has(key)) USER_ENABLED[id] = user.get(key).getAsBoolean();
                        if (master != null && master.has(key)) MASTER_ENABLED[id] = master.get(key).getAsBoolean();
                        if (custom != null && custom.has(key)) CUSTOM_TEXT[id] = custom.get(key).getAsString();
                    }
                }
            } catch (Exception error) {
                System.err.println("[AlphaClient] Could not load Easter Eggs settings: " + error.getMessage());
            } finally {
                if (reader != null) try { reader.close(); } catch (Exception ignored) { }
            }
        }
        loaded = true;
    }
    private static synchronized void save() {
        File file = getFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return;
        JsonObject root = new JsonObject();
        root.addProperty("globallyEnabled", globallyEnabled);
        JsonObject user = new JsonObject(), master = new JsonObject(), custom = new JsonObject();
        for (int id = 1; id <= 25; id++) {
            String key = String.valueOf(id);
            user.addProperty(key, USER_ENABLED[id]);
            master.addProperty(key, MASTER_ENABLED[id]);
            if (CUSTOM_TEXT[id] != null) custom.addProperty(key, CUSTOM_TEXT[id]);
        }
        root.add("userEnabled", user); root.add("masterEnabled", master); root.add("customText", custom);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file); writer.write(root.toString()); writer.flush();
        } catch (Exception error) {
            System.err.println("[AlphaClient] Could not save Easter Eggs settings: " + error.getMessage());
        } finally {
            if (writer != null) try { writer.close(); } catch (Exception ignored) { }
        }
    }

    public static final class Egg {
        private final int id;
        private final String defaultText;
        private Egg(int id, String text) { this.id = id; this.defaultText = text; }
        public int getId() { return id; }
        public String getDefaultText() { return defaultText; }
        public String getText() { return EasterEggManager.getText(id); }
        public boolean isEnabled() { return EasterEggManager.isUserEnabled(id); }
    }
}
