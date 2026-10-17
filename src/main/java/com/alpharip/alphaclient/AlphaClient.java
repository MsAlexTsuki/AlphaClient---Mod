package com.alpharip.alphaclient;

import com.alpharip.staffmod.BuildInfo;
import com.alpharip.staffmod.config.AlphaClientConfig;
import com.alpharip.staffmod.gui.AlphaClientGuiHandler;
import com.alpharip.staffmod.gui.AlphaModMenu;
import com.alpharip.staffmod.gui.AlphaMainMenu;
import com.alpharip.staffmod.hud.BhopHud;
import com.alpharip.staffmod.fps.FpsMod;
import com.alpharip.staffmod.cps.CpsMod;
import com.alpharip.staffmod.optimization.FpsOptimization;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.movement.BhopProfileManager;
import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.rankviewer.RankCache;
import com.alpharip.staffmod.rankviewer.RankViewerRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.lwjgl.input.Keyboard;

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
    private boolean partnerAutoConnectConsumed;
    private boolean fpsOptimizationInitialized;

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) {
        instance = this;
        System.out.println("[AlphaClient] Initializing " + NAME + " v" + VERSION);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new AlphaClientGuiHandler());
        MinecraftForge.EVENT_BUS.register(new RankViewerRenderer());
        MinecraftForge.EVENT_BUS.register(new FpsMod());
        MinecraftForge.EVENT_BUS.register(new CpsMod());
        RankCache.INSTANCE.start();

        // Load every implemented HUD's persisted position before registering its renderer.
        AlphaClientConfig.load();
        BhopHud.loadHudPosition();
        CpsMod.load();

        MinecraftForge.EVENT_BUS.register(new BhopHud());

        bhopMode = new BhopMode();
        // Restore per-profile module toggles after the registry and BHOP mode exist.
        BhopProfileManager.load();
        BhopProfileManager.applySelectedModuleStates();
        syncBhopModuleState();

        openModMenuKey = new KeyBinding(
                "AlphaClient: Open Module Menu",
                Keyboard.KEY_RCONTROL,
                "AlphaClient"
        );
        toggleBhopKey = new KeyBinding(
                "AlphaClient: Toggle BHOP",
                Keyboard.KEY_B,
                "AlphaClient"
        );
        ClientRegistry.registerKeyBinding(openModMenuKey);
        ClientRegistry.registerKeyBinding(toggleBhopKey);

        System.out.println("[AlphaClient] Core initialized. Access: " + PermissionService.getStatusText());
    }

    /** Connects to the partner server only after the AlphaClient main menu has loaded. */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

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

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) return;

        if (openModMenuKey != null && openModMenuKey.isPressed()) {
            if (minecraft.currentScreen == null) {
                minecraft.displayGuiScreen(new AlphaModMenu(null));
            }
            return;
        }

        if (toggleBhopKey != null && toggleBhopKey.isPressed()) {
            AlphaModule staffmods = AlphaModuleRegistry.find("staffmods");
            if (staffmods == null || !staffmods.isImplemented() || !PermissionService.hasModAccess("staff.bhop")) {
                System.out.println("[AlphaClient] BHOP requires Staff access or Founder offline test mode.");
                return;
            }
            staffmods.setEnabled(!staffmods.isEnabled());
            syncBhopModuleState();
        }
    }

    /** Keeps the temporary BHOP implementation connected to its module toggle. */
    public static void syncBhopModuleState() {
        AlphaModule staffmods = AlphaModuleRegistry.find("staffmods");
        BhopMode.setEnabled(staffmods != null && staffmods.isEnabled()
                && PermissionService.hasModAccess("staff.bhop"));
    }
}
