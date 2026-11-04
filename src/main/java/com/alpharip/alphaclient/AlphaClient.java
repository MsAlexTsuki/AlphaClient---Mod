// --- Made By MsAlexTsuki ---
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
import com.alpharip.staffmod.fps.FpsMod;
import com.alpharip.staffmod.ping.PingMod;
import com.alpharip.staffmod.cps.CpsMod;
import com.alpharip.staffmod.combat.AimAssist;
import com.alpharip.staffmod.pvp.LegacyPvpMod;
import com.alpharip.staffmod.optimization.FpsOptimization;
import com.alpharip.staffmod.optimization.AlphaPerformanceCore;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.movement.WTapAssistance;
import com.alpharip.staffmod.profile.ModuleProfileManager;
import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.alpharip.staffmod.rankviewer.RankCache;
import com.alpharip.staffmod.rankviewer.RankViewerRenderer;
import com.alpharip.staffmod.quality.FullbrightMod;
import com.alpharip.staffmod.quality.AutoGGMod;
import com.alpharip.staffmod.quality.AutoReconnectMod;
import com.alpharip.staffmod.quality.enchantingname.EnchantingNameMod;
import com.alpharip.staffmod.quality.anvilcost.AnvilCostMod;
import com.alpharip.staffmod.quality.anvilcost.AnvilCostTooltipHandler;
import com.alpharip.staffmod.serverhistory.ServerHistoryManager;
import com.alpharip.staffmod.fighthistory.FightHistoryManager;
import com.alpharip.staffmod.founderfun.PixelGardenMessageNotifier;
import com.alpharip.staffmod.founderfun.PixelGardenModuleSettings;
import com.alpharip.staffmod.founderfun.eastereggs.EasterEggManager;
import com.alpharip.staffmod.inventorysearch.InventorySearchCommand;
import com.alpharip.staffmod.waypoints.DeathWaypointMod;
import com.alpharip.staffmod.waypoints.EventCountdownHub;
import com.alpharip.staffmod.waypoints.WaypointCommand;
import com.alpharip.staffmod.waypoints.CreateWaypointGui;
import com.alpharip.staffmod.waypoints.WaypointFromClickableCoordinates;
import com.alpharip.staffmod.waypoints.WaypointManager;
import com.alpharip.staffmod.waypoints.WaypointTracker;
import com.alpharip.staffmod.waypoints.WaypointPathfinder;
import com.alpharip.staffmod.Minimap.AlphaMinimap;
import com.alpharip.staffmod.Minimap.MinimapSettingsGui;
import com.alpharip.staffmod.Minimap.FullscreenMinimapGui;
import com.alpharip.staffmod.Minimap.MinimapCommand;
import com.alpharip.staffmod.Minimap.MinimapSettings;
import com.alpharip.staffmod.waypoints.WorldWaypointIcons;
import com.alpharip.staffmod.inventorysearch.InventorySearchMod;
import com.alpharip.staffmod.inventorysearch.InventorySearchSettings;
import com.alpharip.staffmod.singleplayer.worldedit.AlphaWorldEditCommand;

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
    private KeyBinding toggleMinimapKey;
    private KeyBinding openMinimapSettingsKey;
    private KeyBinding openFullscreenMinimapKey;
    private KeyBinding createQuickWaypointKey;
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
        InventorySearchSettings.load();

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(PixelGardenMessageNotifier.getInstance());
        EasterEggManager.initialize();
        DeveloperDiagnostics.initialize();
        AlphaPerformanceCore.getInstance().initialize();
        MinecraftForge.EVENT_BUS.register(new DeveloperDiagnostics());
        MinecraftForge.EVENT_BUS.register(new DeveloperDebugOverlay());
        registerClientCommands();
        WaypointManager.getInstance().load();
        ClientCommandHandler.instance.registerCommand(new WaypointCommand());
        ClientCommandHandler.instance.registerCommand(new MinimapCommand());
        AlphaWorldEditCommand worldEditCommand = new AlphaWorldEditCommand();
        ClientCommandHandler.instance.registerCommand(worldEditCommand);
        MinecraftForge.EVENT_BUS.register(worldEditCommand);
        ClientCommandHandler.instance.registerCommand(new InventorySearchCommand());
        MinecraftForge.EVENT_BUS.register(new AlphaClientGuiHandler());
        MinecraftForge.EVENT_BUS.register(new InventorySearchMod());
        MinecraftForge.EVENT_BUS.register(DeathWaypointMod.getInstance());
        MinecraftForge.EVENT_BUS.register(WaypointTracker.getInstance());
        MinecraftForge.EVENT_BUS.register(WaypointPathfinder.getInstance());
        MinecraftForge.EVENT_BUS.register(WaypointFromClickableCoordinates.getInstance());
        MinecraftForge.EVENT_BUS.register(EventCountdownHub.getInstance());
        // AlphaMinimap now owns terrain, entity and waypoint rendering.
        MinecraftForge.EVENT_BUS.register(AlphaMinimap.getInstance());
        MinecraftForge.EVENT_BUS.register(WorldWaypointIcons.getInstance());
        MinecraftForge.EVENT_BUS.register(new RankViewerRenderer());
        MinecraftForge.EVENT_BUS.register(new FpsMod());
        MinecraftForge.EVENT_BUS.register(new PingMod());
        MinecraftForge.EVENT_BUS.register(new CpsMod());
        MinecraftForge.EVENT_BUS.register(new InfoHudModules());
        MinecraftForge.EVENT_BUS.register(new LegacyPvpMod());
        MinecraftForge.EVENT_BUS.register(new WTapAssistance());
        MinecraftForge.EVENT_BUS.register(new AimAssist());
        MinecraftForge.EVENT_BUS.register(new FullbrightMod());
        MinecraftForge.EVENT_BUS.register(new AutoGGMod());
        MinecraftForge.EVENT_BUS.register(new AutoReconnectMod());
        MinecraftForge.EVENT_BUS.register(new AnvilCostTooltipHandler());
        ServerHistoryManager.loadSettings();
        MinecraftForge.EVENT_BUS.register(ServerHistoryManager.getInstance());
        FightHistoryManager.loadHistory();
        MinecraftForge.EVENT_BUS.register(FightHistoryManager.getInstance());
        RankCache.INSTANCE.start();
        SupabaseCommunityService.start();

        AlphaClientConfig.load();
        AutoGGMod.loadSettings();
        AutoReconnectMod.loadSettings();
        AnvilCostMod.loadSettings();
        EnchantingNameMod.loadSettings();
        BhopHud.loadHudPosition();
        CpsMod.load();
        PingMod.load();

        MinecraftForge.EVENT_BUS.register(new BhopHud());
        MinecraftForge.EVENT_BUS.register(new AfkTimerHud());
        MinecraftForge.EVENT_BUS.register(new SessionTimerHud());
        PlaytimeTrackerHud.initialize();
        MinecraftForge.EVENT_BUS.register(new PlaytimeTrackerHud());

        bhopMode = new BhopMode();
        ModuleProfileManager.load();
        ModuleProfileManager.applySelectedModuleStates();
        syncBhopModuleState();

        openModMenuKey = new KeyBinding("AlphaClient: Open Home Menu", Keyboard.KEY_RCONTROL, "AlphaClient");
        ClientRegistry.registerKeyBinding(openModMenuKey);
        toggleMinimapKey = new KeyBinding("AlphaClient: Toggle Minimap",
                MinimapSettings.getInt("keyToggleMinimap", Keyboard.KEY_NONE, -1, 65535), "AlphaClient");
        openMinimapSettingsKey = new KeyBinding("AlphaClient: Open Minimap Settings",
                MinimapSettings.getInt("keyOpenMinimapSettings", Keyboard.KEY_NONE, -1, 65535), "AlphaClient");
        openFullscreenMinimapKey = new KeyBinding("AlphaClient: Open Fullscreen Minimap",
                MinimapSettings.getInt("keyOpenFullscreenMinimap", Keyboard.KEY_NONE, -1, 65535), "AlphaClient");
        createQuickWaypointKey = new KeyBinding("AlphaClient: Create Waypoint",
                MinimapSettings.getInt("keyCreateQuickWaypoint", Keyboard.KEY_NONE, -1, 65535), "AlphaClient");
        ClientRegistry.registerKeyBinding(toggleMinimapKey);
        ClientRegistry.registerKeyBinding(openMinimapSettingsKey);
        ClientRegistry.registerKeyBinding(openFullscreenMinimapKey);
        ClientRegistry.registerKeyBinding(createQuickWaypointKey);
        syncBhopKeyBindingVisibility();
        System.out.println("[AlphaClient] Core initialized. Access: " + PermissionService.getStatusText());
    }

    public static void openGuiFromCommand(GuiScreen screen) { pendingCommandGui = screen; }

    private static boolean isPixelGardenScreen(GuiScreen screen) {
        return screen != null && screen.getClass().getName().startsWith("com.alpharip.staffmod.founderfun.PixelGarden");
    }

    private void registerClientCommands() {
        String[] commands = {
                "alphaclient", "alphahelp", "alphaclient:help", "alphaclient:modules",
                "alphaclient:settings", "alphaclient:module", "alphaclient:easteregg", "easteregg", "alphaclient:fps",
                "alphaclient:cps", "alphaclient:bhop", "alphaclient:wtap",
                "alphaclient:fullbright", "alphaclient:legacypvp", "alphaclient:playtime",
                "alphaclient:sessiontimer", "alphaclient:afktimer",
                "fpshud", "cpshud", "pothud", "playtime", "sessiontimer", "afktimer",
                "bhop", "wtap", "aimassist", "fullbright", "nameviewer", "legacypvp", "quality",
                "hudeditor", "profiles", "profile", "alphaperms", "staffoperations", "alphanote", "staffmods", "staffgeneral", "stafftmod", "staffmod",
                "staffsrmod", "staffadmin", "staffsradmin", "staffmanagement",
                "staffplatformadmin", "staffplatadmin", "staffmanager", "staffowner", "stafffounder",
                "general", "beta", "donator", "scout", "rogue", "elite", "alpha", "alphaplus", "autogg", "autoreconnect", "pixelgarden", "garden", "pgivecoins"
        };
        for (String command : commands) {
            if ("pixelgarden".equals(command) || "garden".equals(command)) {
                ClientCommandHandler.instance.registerCommand(new com.alpharip.staffmod.founderfun.PixelGardenCommand(command));
            } else if ("pgivecoins".equals(command)) {
                ClientCommandHandler.instance.registerCommand(new com.alpharip.staffmod.founderfun.FounderGivePixelGardenCoinsCommand());
            } else {
                ClientCommandHandler.instance.registerCommand(new AlphaClientCommand(command));
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft guiMinecraft = Minecraft.getMinecraft();
        if (PermissionService.isOnlineRankVerified() && !PermissionService.hasPixelGardenRankAccess()
                && PixelGardenModuleSettings.getInstance().isEnabled()) {
            PixelGardenModuleSettings.getInstance().setEnabled(false);
        }
        GuiScreen requestedGui = pendingCommandGui;
        if (requestedGui != null) {
            pendingCommandGui = null;
            boolean pixelGardenAllowed = PermissionService.hasPixelGardenRankAccess();
            if (guiMinecraft != null && (!isPixelGardenScreen(requestedGui)
                    || (pixelGardenAllowed && PixelGardenModuleSettings.getInstance().isEnabled()))) {
                guiMinecraft.displayGuiScreen(requestedGui);
            }
        }
        if (guiMinecraft != null && isPixelGardenScreen(guiMinecraft.currentScreen)
                && (!PermissionService.hasPixelGardenRankAccess() || !PixelGardenModuleSettings.getInstance().isEnabled())) {
            guiMinecraft.displayGuiScreen(null);
        }
        tickStaffPresence();
        syncBhopKeyBindingVisibility();
        persistMinimapKeyBindings();
        if (!fpsOptimizationInitialized) fpsOptimizationInitialized = FpsOptimization.applyOnce();
        AlphaPerformanceCore.getInstance().onClientTick();
        if (partnerAutoConnectConsumed) return;
        String partnerServer = System.getProperty("alphaclient.partnerServer");
        if (partnerServer == null || partnerServer.trim().isEmpty()) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) return;
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
        } else partnerAutoConnectTicks = -1;
    }

    private void tickStaffPresence() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || !SupabaseCommunityService.isSignedIn() || !PermissionService.hasStaffModsAccess()) {
            presenceHeartbeatTicks = 0;
            return;
        }
        if (minecraft.thePlayer != null) {
            double x = minecraft.thePlayer.posX, y = minecraft.thePlayer.posY, z = minecraft.thePlayer.posZ;
            float yaw = minecraft.thePlayer.rotationYaw, pitch = minecraft.thePlayer.rotationPitch;
            if (Double.isNaN(lastStaffX) || x != lastStaffX || y != lastStaffY || z != lastStaffZ
                    || yaw != lastStaffYaw || pitch != lastStaffPitch || Mouse.getDX() != 0 || Mouse.getDY() != 0
                    || Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_A)
                    || Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_D)
                    || Keyboard.isKeyDown(Keyboard.KEY_SPACE)) lastStaffInputAt = System.currentTimeMillis();
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
            if (minecraft.currentScreen == null) minecraft.displayGuiScreen(new AlphaClientHomeMenu());
            return;
        }
        if (toggleBhopKey != null && toggleBhopKey.isPressed()) {
            if (!SupabaseCommunityService.isSignedIn() || !PermissionService.hasAccess(PermissionService.Rank.T_MOD)) return;
            AlphaModule staffmods = AlphaModuleRegistry.find("staffmods");
            if (staffmods == null || !staffmods.isImplemented() || !PermissionService.hasModAccess("staff.bhop")) {
                System.out.println("[AlphaClient] BHOP requires Staff access or Founder offline test mode.");
                return;
            }
            staffmods.setEnabled(!staffmods.isEnabled());
            syncBhopModuleState();
        }
        if (toggleMinimapKey != null && toggleMinimapKey.isPressed()) {
            AlphaModule minimap = AlphaModuleRegistry.find("minimap");
            if (minimap != null && minimap.isImplemented()) minimap.setEnabled(!minimap.isConfiguredEnabled());
        }
        if (openMinimapSettingsKey != null && openMinimapSettingsKey.isPressed()
                && minecraft.currentScreen == null) {
            minecraft.displayGuiScreen(new MinimapSettingsGui(null));
        }
        if (openFullscreenMinimapKey != null && openFullscreenMinimapKey.isPressed()
                && minecraft.currentScreen == null) {
            AlphaModule minimap = AlphaModuleRegistry.find("minimap");
            if (minimap != null && minimap.isImplemented() && minimap.isEnabled()) {
                minecraft.displayGuiScreen(new FullscreenMinimapGui(null));
            }
        }
        if (createQuickWaypointKey != null && createQuickWaypointKey.isPressed()
                && minecraft.currentScreen == null && minecraft.theWorld != null && minecraft.thePlayer != null) {
            minecraft.displayGuiScreen(new CreateWaypointGui(null));
        }
    }

    private void persistMinimapKeyBindings() {
        saveKeyBinding("keyToggleMinimap", toggleMinimapKey);
        saveKeyBinding("keyOpenMinimapSettings", openMinimapSettingsKey);
        saveKeyBinding("keyOpenFullscreenMinimap", openFullscreenMinimapKey);
        saveKeyBinding("keyCreateQuickWaypoint", createQuickWaypointKey);
    }

    private void saveKeyBinding(String setting, KeyBinding binding) {
        if (binding == null) return;
        int currentKey = binding.getKeyCode();
        int savedKey = MinimapSettings.getInt(setting, Keyboard.KEY_NONE, -1, 65535);
        if (savedKey != currentKey) MinimapSettings.setInt(setting, currentKey);
    }

    private void syncBhopKeyBindingVisibility() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null || minecraft.gameSettings == null) return;
        boolean shouldBeVisible = SupabaseCommunityService.isSignedIn()
                && PermissionService.hasAccess(PermissionService.Rank.T_MOD);
        if (shouldBeVisible && toggleBhopKey == null) {
            toggleBhopKey = new KeyBinding("AlphaClient: Toggle BHOP", Keyboard.KEY_B, "AlphaClient");
            ClientRegistry.registerKeyBinding(toggleBhopKey);
        }
        if (toggleBhopKey == null) return;
        KeyBinding[] bindings = minecraft.gameSettings.keyBindings;
        boolean registered = false;
        for (KeyBinding binding : bindings) if (binding == toggleBhopKey) { registered = true; break; }
        if (shouldBeVisible && !registered) {
            KeyBinding[] expanded = new KeyBinding[bindings.length + 1];
            System.arraycopy(bindings, 0, expanded, 0, bindings.length);
            expanded[bindings.length] = toggleBhopKey;
            minecraft.gameSettings.keyBindings = expanded;
        } else if (!shouldBeVisible && registered) {
            KeyBinding[] filtered = new KeyBinding[bindings.length - 1];
            int index = 0;
            for (KeyBinding binding : bindings) if (binding != toggleBhopKey) filtered[index++] = binding;
            minecraft.gameSettings.keyBindings = filtered;
        }
    }

    public static void syncBhopModuleState() {
        // Intentionally preserve BhopMode's configured state.
    }
}
