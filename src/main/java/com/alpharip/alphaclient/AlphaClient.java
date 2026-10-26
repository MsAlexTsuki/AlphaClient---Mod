package com.alpharip.alphaclient;

import com.alpharip.staffmod.BuildInfo;
import com.alpharip.staffmod.config.AlphaClientConfig;
import com.alpharip.staffmod.developer.DeveloperDiagnostics;
import com.alpharip.staffmod.developer.DeveloperDebugOverlay;
import com.alpharip.staffmod.gui.AlphaClientGuiHandler;
import com.alpharip.staffmod.gui.AlphaModMenu;
import com.alpharip.staffmod.gui.AlphaMainMenu;
import com.alpharip.staffmod.gui.AlphaClientHomeMenu;
import com.alpharip.staffmod.hud.BhopHud;
import com.alpharip.staffmod.hud.AfkTimerHud;
import com.alpharip.staffmod.hud.SessionTimerHud;
import com.alpharip.staffmod.hud.PlaytimeTrackerHud;
import com.alpharip.staffmod.hud.InfoHudModules;
import com.alpharip.staffmod.hud.CoordinatesHud;
import com.alpharip.staffmod.hud.CompassHud;
import com.alpharip.staffmod.hud.KeystrokesHud;
import com.alpharip.staffmod.hud.ItemHud;
import com.alpharip.staffmod.hud.StatusDashboardHud;
import com.alpharip.staffmod.hud.CombatWarningsHud;
import com.alpharip.staffmod.fps.FpsMod;
import com.alpharip.staffmod.ping.PingMod;
import com.alpharip.staffmod.cps.CpsMod;
import com.alpharip.staffmod.distance.DistanceTrackerMod;
import com.alpharip.staffmod.healthbar.HealthbarMod;
import com.alpharip.staffmod.combat.AimAssistMod;
import com.alpharip.staffmod.combat.TargetingMod;
import com.alpharip.staffmod.combat.AttackTimingMod;
import com.alpharip.staffmod.combat.HitboxMod;
import com.alpharip.staffmod.pvp.LegacyPvpMod;
import com.alpharip.staffmod.optimization.FpsOptimization;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.movement.WTapAssistance;
import com.alpharip.staffmod.movement.STapAssistance;
import com.alpharip.staffmod.profile.ModuleProfileManager;
import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.alpharip.staffmod.rankviewer.RankCache;
import com.alpharip.staffmod.rankviewer.RankViewerRenderer;
import com.alpharip.staffmod.quality.FullbrightMod;
import com.alpharip.staffmod.quality.AutoGGMod;
import com.alpharip.staffmod.quality.AutoReconnectMod;
import com.alpharip.staffmod.quality.AutoRespawnMod;
import com.alpharip.staffmod.combat.LowHealthWarningMod;
import com.alpharip.staffmod.hud.PotionExpirationWarningMod;
import com.alpharip.staffmod.hud.BlockCounterHud;
import com.alpharip.staffmod.world.DeathWaypointMod;
import com.alpharip.staffmod.hud.ArmorDurabilityWarningMod;
import com.alpharip.staffmod.hud.TargetDistanceHud;
import com.alpharip.staffmod.hud.NearestEnemyHud;
import com.alpharip.staffmod.hud.PotionHud;
import com.alpharip.staffmod.hud.ArmorHud;
import com.alpharip.staffmod.quality.SessionTimerMod;
import com.alpharip.staffmod.quality.PlaytimeTrackerMod;
import com.alpharip.staffmod.hud.TargetHealthHud;
import com.alpharip.staffmod.hud.CrosshairCustomizerMod;
import com.alpharip.staffmod.chat.ChatTimestampMod;
import com.alpharip.staffmod.combat.KillCounterMod;
import com.alpharip.staffmod.hud.AttackIndicatorMod;
import com.alpharip.staffmod.hud.BiomeHud;
import com.alpharip.staffmod.hud.LightLevelHud;
import com.alpharip.staffmod.hud.WorldTimeHud;
import com.alpharip.staffmod.hud.FallDistanceHud;
import com.alpharip.staffmod.hud.HungerWarning;
import com.alpharip.staffmod.hud.DurabilityWarning;
import com.alpharip.staffmod.hud.EntityCountHUD;
import com.alpharip.staffmod.hud.DayCounter;
import com.alpharip.staffmod.hud.DimensionHUD;
import com.alpharip.staffmod.hud.SpeedHUD;
import com.alpharip.staffmod.hud.AirHUD;
import com.alpharip.staffmod.hud.ExperienceHUD;
import com.alpharip.staffmod.hud.HealthHUD;
import com.alpharip.staffmod.hud.FoodHUD;
import com.alpharip.staffmod.hud.VelocityHud;
import com.alpharip.staffmod.hud.HeldItemDurabilityHud;
import com.alpharip.staffmod.movement.AutoSprintMod;
import com.alpharip.staffmod.inventory.AutoToolMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

/** Central Forge entry point and lifecycle owner for AlphaClient. */
@Mod(
        modid = AlphaClient.MOD_ID,
        name = AlphaClient.NAME,
        version = AlphaClient.VERSION,
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.8.9]"
)
public final class AlphaClient {
    public static final String MOD_ID = "alphaclient";
    public static final String NAME = "AlphaClient";
    public static final String VERSION = BuildInfo.VERSION;

    public static AlphaClient instance;
    public static BhopMode bhopMode;

    private KeyBinding openModMenuKey;
    private KeyBinding toggleBhopKey;
    private int partnerAutoConnectTicks = -1;
    private int presenceHeartbeatTicks = 0;
    private long lastStaffInputAt = System.currentTimeMillis();
    private double lastStaffX = Double.NaN, lastStaffY = Double.NaN, lastStaffZ = Double.NaN;
    private float lastStaffYaw = Float.NaN, lastStaffPitch = Float.NaN;
    private boolean partnerAutoConnectConsumed;
    private boolean fpsOptimizationInitialized;
    private static volatile GuiScreen pendingCommandGui;

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) {
        instance = this;
        System.out.println("[AlphaClient] Initializing " + NAME + " v" + VERSION);

        MinecraftForge.EVENT_BUS.register(this);
        DeveloperDiagnostics.initialize();
        MinecraftForge.EVENT_BUS.register(new DeveloperDiagnostics());
        MinecraftForge.EVENT_BUS.register(new DeveloperDebugOverlay());
        registerClientCommands();
        MinecraftForge.EVENT_BUS.register(new AlphaClientGuiHandler());
        MinecraftForge.EVENT_BUS.register(new RankViewerRenderer());
        MinecraftForge.EVENT_BUS.register(new FpsMod());
        MinecraftForge.EVENT_BUS.register(new PingMod());
        MinecraftForge.EVENT_BUS.register(new CpsMod());
        MinecraftForge.EVENT_BUS.register(new DistanceTrackerMod());
        MinecraftForge.EVENT_BUS.register(new HealthbarMod());
        MinecraftForge.EVENT_BUS.register(new TargetingMod());
        MinecraftForge.EVENT_BUS.register(new AimAssistMod());
        MinecraftForge.EVENT_BUS.register(new AttackTimingMod());
        MinecraftForge.EVENT_BUS.register(new HitboxMod());
        MinecraftForge.EVENT_BUS.register(new InfoHudModules());
        MinecraftForge.EVENT_BUS.register(new CoordinatesHud());
        MinecraftForge.EVENT_BUS.register(new CompassHud());
        MinecraftForge.EVENT_BUS.register(new KeystrokesHud());
        MinecraftForge.EVENT_BUS.register(new ItemHud());
        MinecraftForge.EVENT_BUS.register(new StatusDashboardHud());
        MinecraftForge.EVENT_BUS.register(new CombatWarningsHud());
        MinecraftForge.EVENT_BUS.register(new LegacyPvpMod());
        MinecraftForge.EVENT_BUS.register(new WTapAssistance());
        MinecraftForge.EVENT_BUS.register(new STapAssistance());
        MinecraftForge.EVENT_BUS.register(new FullbrightMod());
        MinecraftForge.EVENT_BUS.register(new AutoGGMod());
        MinecraftForge.EVENT_BUS.register(new AutoReconnectMod());
        MinecraftForge.EVENT_BUS.register(new AutoRespawnMod());
        MinecraftForge.EVENT_BUS.register(new LowHealthWarningMod());
        MinecraftForge.EVENT_BUS.register(new PotionExpirationWarningMod());
        MinecraftForge.EVENT_BUS.register(new BlockCounterHud());
        MinecraftForge.EVENT_BUS.register(new DeathWaypointMod());
        MinecraftForge.EVENT_BUS.register(new ArmorDurabilityWarningMod());
        MinecraftForge.EVENT_BUS.register(new TargetDistanceHud());
        MinecraftForge.EVENT_BUS.register(new NearestEnemyHud());
        MinecraftForge.EVENT_BUS.register(new PotionHud());
        MinecraftForge.EVENT_BUS.register(new ArmorHud());
        MinecraftForge.EVENT_BUS.register(new SessionTimerMod());
        MinecraftForge.EVENT_BUS.register(new PlaytimeTrackerMod());
        MinecraftForge.EVENT_BUS.register(new TargetHealthHud());
        MinecraftForge.EVENT_BUS.register(new CrosshairCustomizerMod());
        MinecraftForge.EVENT_BUS.register(new ChatTimestampMod());
        MinecraftForge.EVENT_BUS.register(new KillCounterMod());
        MinecraftForge.EVENT_BUS.register(new AttackIndicatorMod());
        MinecraftForge.EVENT_BUS.register(new BiomeHud());
        MinecraftForge.EVENT_BUS.register(new LightLevelHud());
        MinecraftForge.EVENT_BUS.register(new WorldTimeHud());
        MinecraftForge.EVENT_BUS.register(new FallDistanceHud());
        MinecraftForge.EVENT_BUS.register(new HungerWarning());
        MinecraftForge.EVENT_BUS.register(new DurabilityWarning());
        MinecraftForge.EVENT_BUS.register(new EntityCountHUD());
        MinecraftForge.EVENT_BUS.register(new DayCounter());
        MinecraftForge.EVENT_BUS.register(new DimensionHUD());
        MinecraftForge.EVENT_BUS.register(new SpeedHUD());
        MinecraftForge.EVENT_BUS.register(new AirHUD());
        MinecraftForge.EVENT_BUS.register(new ExperienceHUD());
        MinecraftForge.EVENT_BUS.register(new HealthHUD());
        MinecraftForge.EVENT_BUS.register(new FoodHUD());
        MinecraftForge.EVENT_BUS.register(new VelocityHud());
        MinecraftForge.EVENT_BUS.register(new HeldItemDurabilityHud());
        MinecraftForge.EVENT_BUS.register(new com.alpharip.staffmod.hud.CombatTrackingHudModules());
        MinecraftForge.EVENT_BUS.register(new com.alpharip.staffmod.hud.UtilityTrackingHudModules());
        MinecraftForge.EVENT_BUS.register(new AutoSprintMod());
        MinecraftForge.EVENT_BUS.register(new AutoToolMod());
        RankCache.INSTANCE.start();
        // Restore the Alpha Account session and keep rank/module permissions in sync.
        SupabaseCommunityService.start();

        // Load every implemented HUD's persisted position before registering its renderer.
        AlphaClientConfig.load();
        AutoGGMod.loadSettings();
        AutoReconnectMod.loadSettings();
        BhopHud.loadHudPosition();
        CpsMod.load();
        PingMod.load();

        MinecraftForge.EVENT_BUS.register(new BhopHud());
        MinecraftForge.EVENT_BUS.register(new AfkTimerHud());
        MinecraftForge.EVENT_BUS.register(new SessionTimerHud());
        PlaytimeTrackerHud.initialize();
        MinecraftForge.EVENT_BUS.register(new PlaytimeTrackerHud());

        bhopMode = new BhopMode();
        // Restore per-profile module toggles after the registry and BHOP mode exist.
        ModuleProfileManager.load();
        ModuleProfileManager.applySelectedModuleStates();
        syncBhopModuleState();

        openModMenuKey = new KeyBinding(
                "AlphaClient: Open Home Menu",
                Keyboard.KEY_RCONTROL,
                "AlphaClient"
        );
        ClientRegistry.registerKeyBinding(openModMenuKey);
        syncBhopKeyBindingVisibility();

        System.out.println("[AlphaClient] Core initialized. Access: " + PermissionService.getStatusText());
    }

    /** Opens a command-requested GUI after Minecraft has finished closing the chat screen. */
    public static void openGuiFromCommand(GuiScreen screen) {
        pendingCommandGui = screen;
    }

    /** Registers the central command namespace and short aliases against shared handlers. */
    private void registerClientCommands() {
        String[] commands = {
                "alphaclient", "alphahelp", "alphaclient:help", "alphaclient:modules",
                "alphaclient:settings", "alphaclient:module", "alphaclient:fps",
                "alphaclient:cps", "alphaclient:bhop", "alphaclient:wtap",
                "alphaclient:fullbright", "alphaclient:legacypvp", "alphaclient:playtime",
                "alphaclient:sessiontimer", "alphaclient:afktimer",
                "fpshud", "cpshud", "pothud", "playtime", "sessiontimer", "afktimer",
                "bhop", "wtap", "fullbright", "nameviewer", "legacypvp", "quality",
                "hudeditor", "profiles", "profile", "alphaperms", "staffoperations", "alphanote", "staffmods", "staffgeneral", "stafftmod", "staffmod",
                "staffsrmod", "staffadmin", "staffsradmin", "staffmanagement",
                "staffplatformadmin", "staffplatadmin", "staffmanager", "staffowner", "stafffounder",
                "general", "beta", "donator", "scout", "rogue", "elite", "alpha", "alphaplus", "autogg", "autoreconnect"
        };
        for (String command : commands) {
            ClientCommandHandler.instance.registerCommand(new AlphaClientCommand(command));
        }
    }

    /** Connects to the partner server only after the AlphaClient main menu has loaded. */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Client commands run while GuiChat is closing; defer requested screens until the next tick.
        GuiScreen requestedGui = pendingCommandGui;
        if (requestedGui != null) {
            pendingCommandGui = null;
            Minecraft guiMinecraft = Minecraft.getMinecraft();
            if (guiMinecraft != null) guiMinecraft.displayGuiScreen(requestedGui);
        }

        // Staff Operations presence heartbeat and automatic AFK detection.
        tickStaffPresence();

        // Keep the BHOP key visible in Minecraft Controls only while community-authenticated.
        syncBhopKeyBindingVisibility();

        // Apply FPS defaults once when Minecraft's settings are ready, independent of HUD toggles.
        if (!fpsOptimizationInitialized) {
            fpsOptimizationInitialized = FpsOptimization.applyOnce();
        }

        if (partnerAutoConnectConsumed) return;
        String partnerServer = System.getProperty("alphaclient.partnerServer");
        if (partnerServer == null || partnerServer.trim().isEmpty()) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) return;

        // Wait for the actual menu screen to remain active for one second before connecting.
        if (minecraft.currentScreen instanceof AlphaMainMenu || minecraft.currentScreen instanceof GuiMainMenu) {
            if (partnerAutoConnectTicks < 0) partnerAutoConnectTicks = 0;
            partnerAutoConnectTicks++;
            if (partnerAutoConnectTicks >= 20) {
                partnerAutoConnectConsumed = true;
                GuiScreen parent = minecraft.currentScreen;
                ServerData serverData = new ServerData("Alpha.rip", partnerServer.trim(), false);
                minecraft.displayGuiScreen(new GuiConnecting(parent, minecraft, serverData));
                System.out.println("[AlphaClient] Connecting to partner server " + partnerServer.trim());
            }
        } else {
            partnerAutoConnectTicks = -1;
        }
    }

    private void tickStaffPresence() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || !SupabaseCommunityService.isSignedIn()
                || !PermissionService.hasStaffModsAccess()) {
            presenceHeartbeatTicks = 0;
            return;
        }
        if (minecraft.thePlayer != null) {
            double x = minecraft.thePlayer.posX, y = minecraft.thePlayer.posY, z = minecraft.thePlayer.posZ;
            float yaw = minecraft.thePlayer.rotationYaw, pitch = minecraft.thePlayer.rotationPitch;
            if (Double.isNaN(lastStaffX) || x != lastStaffX || y != lastStaffY || z != lastStaffZ
                    || yaw != lastStaffYaw || pitch != lastStaffPitch
                    || Mouse.getDX() != 0 || Mouse.getDY() != 0
                    || Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_A)
                    || Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_D)
                    || Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
                lastStaffInputAt = System.currentTimeMillis();
            }
            lastStaffX = x; lastStaffY = y; lastStaffZ = z; lastStaffYaw = yaw; lastStaffPitch = pitch;
        }
        if (++presenceHeartbeatTicks < 400) return;
        presenceHeartbeatTicks = 0;
        final boolean afk = System.currentTimeMillis() - lastStaffInputAt >= 300000L;
        Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                try { SupabaseCommunityService.sendStaffPresenceHeartbeat(afk); }
                catch (Exception e) { System.err.println("[AlphaClient] Staff presence heartbeat failed: " + e.getMessage()); }
            }
        }, "AlphaClient-StaffPresence");
        thread.setDaemon(true);
        thread.start();
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        if (!SupabaseCommunityService.isSignedIn()) return;
        Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                try { SupabaseCommunityService.markStaffPresenceOffline(); }
                catch (Exception e) { System.err.println("[AlphaClient] Could not mark staff presence offline: " + e.getMessage()); }
            }
        }, "AlphaClient-StaffPresenceOffline");
        thread.setDaemon(true);
        thread.start();
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) return;

        if (openModMenuKey != null && openModMenuKey.isPressed()) {
            if (minecraft.currentScreen == null) {
                minecraft.displayGuiScreen(new AlphaClientHomeMenu());
            }
            return;
        }

        if (toggleBhopKey != null && toggleBhopKey.isPressed()) {
            if (!SupabaseCommunityService.isSignedIn()
                    || !PermissionService.hasAccess(PermissionService.Rank.T_MOD)) return;
            AlphaModule staffmods = AlphaModuleRegistry.find("staffmods");
            if (staffmods == null || !staffmods.isImplemented() || !PermissionService.hasModAccess("staff.bhop")) {
                System.out.println("[AlphaClient] BHOP requires Staff access or Founder offline test mode.");
                return;
            }
            staffmods.setEnabled(!staffmods.isEnabled());
            syncBhopModuleState();
        }
    }

    /**
     * Shows the BHOP key in Minecraft Controls only for authenticated staff accounts.
     * Keep the registered KeyBinding instance so it can be hidden and restored without
     * accessing Minecraft's private keybind registry.
     */
    private void syncBhopKeyBindingVisibility() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.gameSettings == null) return;

        boolean shouldBeVisible = SupabaseCommunityService.isSignedIn()
                && PermissionService.hasAccess(PermissionService.Rank.T_MOD);

        if (shouldBeVisible && toggleBhopKey == null) {
            toggleBhopKey = new KeyBinding(
                    "AlphaClient: Toggle BHOP",
                    Keyboard.KEY_B,
                    "AlphaClient"
            );
            ClientRegistry.registerKeyBinding(toggleBhopKey);
        }

        if (toggleBhopKey == null) return;

        KeyBinding[] bindings = minecraft.gameSettings.keyBindings;
        boolean isRegisteredInControls = false;
        for (KeyBinding binding : bindings) {
            if (binding == toggleBhopKey) {
                isRegisteredInControls = true;
                break;
            }
        }

        if (shouldBeVisible && !isRegisteredInControls) {
            KeyBinding[] expanded = new KeyBinding[bindings.length + 1];
            System.arraycopy(bindings, 0, expanded, 0, bindings.length);
            expanded[bindings.length] = toggleBhopKey;
            minecraft.gameSettings.keyBindings = expanded;
        } else if (!shouldBeVisible && isRegisteredInControls) {
            KeyBinding[] filtered = new KeyBinding[bindings.length - 1];
            int index = 0;
            for (KeyBinding binding : bindings) {
                if (binding != toggleBhopKey) {
                    filtered[index++] = binding;
                }
            }
            minecraft.gameSettings.keyBindings = filtered;
        }
    }

    /**
     * Kept for compatibility with profile screens. Do not overwrite the saved BHOP
     * preference based on a module-registry entry: BHOP has its own profile state.
     * BhopMode checks the latest server permission on every client tick.
     */
    public static void syncBhopModuleState() {
        // Intentionally preserve BhopMode's configured state.
    }
}
