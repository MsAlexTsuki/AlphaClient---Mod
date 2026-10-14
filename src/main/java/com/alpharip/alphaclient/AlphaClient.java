package com.alpharip.alphaclient;

import com.alpharip.staffmod.BuildInfo;
import com.alpharip.staffmod.StaffMod;
import com.alpharip.staffmod.config.AlphaClientConfig;
import com.alpharip.staffmod.gui.AlphaClientGuiHandler;
import com.alpharip.staffmod.gui.AlphaModMenu;
import com.alpharip.staffmod.hud.AlphaHudRenderer;
import com.alpharip.staffmod.hud.BhopHud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.permissions.PermissionService;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

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

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) {
        instance = this;
        StaffMod.instance = this;
        System.out.println("[AlphaClient] Initializing " + NAME + " v" + VERSION);

        // Register the client systems through one central startup path.
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new AlphaClientGuiHandler());

        AlphaHudRenderer hudRenderer = new AlphaHudRenderer();
        MinecraftForge.EVENT_BUS.register(hudRenderer);

        // Load settings before enabling the retained BHOP implementation.
        AlphaClientConfig.load();
        BhopHud.loadHudPosition();
        MinecraftForge.EVENT_BUS.register(new BhopHud());

        bhopMode = new BhopMode();
        StaffMod.bhopMode = bhopMode;
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
            if (staffmods == null || !staffmods.isImplemented() || !staffmods.isUnlocked()) {
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
                && PermissionService.hasAccess(PermissionService.Rank.STAFF));
    }
}
