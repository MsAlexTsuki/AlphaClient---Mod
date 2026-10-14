package com.alpharip.alphaclient;

import com.alpharip.staffmod.BuildInfo;
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

/**
 * Main AlphaClient mod entry point.
 *
 * This replaces the old StaffMod bootstrap. The main class owns startup and
 * connects the UI, module registry, HUD, configuration, permissions and BHOP.
 */
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
        com.alpharip.staffmod.StaffMod.instance = this;
        System.out.println("[AlphaClient] Starting " + NAME + " v" + VERSION);

        // One central bootstrap wires the client systems together.
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new AlphaClientGuiHandler());
        MinecraftForge.EVENT_BUS.register(new AlphaHudRenderer());

        AlphaClientConfig.load();

        BhopHud.loadHudPosition();
        MinecraftForge.EVENT_BUS.register(new BhopHud());
        bhopMode = new BhopMode();
        com.alpharip.staffmod.StaffMod.bhopMode = bhopMode;

        AlphaModule bhop = AlphaModuleRegistry.find("bhop");
        if (bhop != null) {
            BhopMode.setEnabled(bhop.isEnabled());
        }

        openModMenuKey = new KeyBinding(
                "AlphaClient: Open Mod Menu",
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

        System.out.println("[AlphaClient] Core ready. Access: " + PermissionService.getStatusText());
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) {
            return;
        }

        if (openModMenuKey != null && openModMenuKey.isPressed()) {
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new AlphaModMenu(null));
            }
            return;
        }

        if (toggleBhopKey != null && toggleBhopKey.isPressed()) {
            AlphaModule bhop = AlphaModuleRegistry.find("bhop");
            if (bhop == null || !PermissionService.hasAccess(bhop.getCategory().getRequiredRank())) {
                System.out.println("[AlphaClient] BHOP requires Staff access or Founder offline test mode.");
                return;
            }

            boolean enabled = !bhop.isEnabled();
            bhop.setEnabled(enabled);
            BhopMode.setEnabled(enabled);
        }
    }
}