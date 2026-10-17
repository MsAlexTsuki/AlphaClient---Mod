package com.alpharip.alphaclient;

import com.alpharip.staffmod.BuildInfo;
import com.alpharip.staffmod.config.AlphaClientConfig;
import com.alpharip.staffmod.gui.AlphaClientGuiHandler;
import com.alpharip.staffmod.gui.AlphaModMenu;
import com.alpharip.staffmod.gui.AlphaMainMenu;
import com.alpharip.staffmod.hud.BhopHud;
import com.alpharip.staffmod.hud.AfkTimerHud;
import com.alpharip.staffmod.fps.FpsMod;
import com.alpharip.staffmod.cps.CpsMod;
import com.alpharip.staffmod.pvp.LegacyPvpMod;
import com.alpharip.staffmod.optimization.FpsOptimization;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.profile.ModuleProfileManager;
import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
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
        MinecraftForge.EVENT_BUS.register(new LegacyPvpMod());
        RankCache.INSTANCE.start();

        // Load every implemented HUD's persisted position before registering its renderer.
        AlphaClientConfig.load();
        BhopHud.loadHudPosition();
        CpsMod.load();

        MinecraftForge.EVENT_BUS.register(new BhopHud());
        MinecraftForge.EVENT_BUS.register(new AfkTimerHud());

        bhopMode = new BhopMode();
        // Restore per-profile module toggles after the registry and BHOP mode exist.
        ModuleProfileManager.load();
        ModuleProfileManager.applySelectedModuleStates();
        syncBhopModuleState();

        openModMenuKey = new KeyBinding(
                "AlphaClient: Open Module Menu",
                Keyboard.KEY_RCONTROL,
                "AlphaClient"
        );
        ClientRegistry.registerKeyBinding(openModMenuKey);
        syncBhopKeyBindingVisibility();

        System.out.println("[AlphaClient] Core initialized. Access: " + PermissionService.getStatusText());
    }

    /** Connects to the partner server only after the AlphaClient main menu has loaded. */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

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

    /** Keeps the temporary BHOP implementation connected to its module toggle. */
    public static void syncBhopModuleState() {
        AlphaModule staffmods = AlphaModuleRegistry.find("staffmods");
        BhopMode.setEnabled(staffmods != null && staffmods.isEnabled()
                && PermissionService.hasModAccess("staff.bhop"));
    }
}
