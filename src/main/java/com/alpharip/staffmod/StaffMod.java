package com.alpharip.staffmod;

import com.alpharip.staffmod.gui.AlphaClientGuiHandler;
import com.alpharip.staffmod.hud.AlphaHudRenderer;
import com.alpharip.staffmod.hud.BhopHud;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.modules.LongJumpMod;
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
 * AlphaClient entry point.
 *
 * The old general-purpose staff-mod bootstrap is replaced by the new
 * AlphaClient core. BHOP is deliberately retained as the sole old module.
 */
@Mod(
        modid = StaffMod.MODID,
        name = StaffMod.NAME,
        version = StaffMod.VERSION,
        clientSideOnly = true
)
public class StaffMod {
    public static final String MODID = "alphaclient";
    public static final String NAME = "Alpha Client";
    public static final String VERSION = BuildInfo.VERSION;

    public static StaffMod instance;
    public static BhopMode bhopMode;
    // Kept as a source-compatibility field for inactive legacy GUI classes.
    public static LongJumpMod longJumpMod;

    private KeyBinding bhopKey;
    private KeyBinding modMenuKey;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        instance = this;

        System.out.println("[AlphaClient] Initializing AlphaClient v" + VERSION);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new AlphaClientGuiHandler());
        MinecraftForge.EVENT_BUS.register(new AlphaHudRenderer());

        BhopHud.loadHudPosition();
        MinecraftForge.EVENT_BUS.register(new BhopHud());
        bhopMode = new BhopMode();

        bhopKey = new KeyBinding("AlphaClient: Toggle BHOP", Keyboard.KEY_B, "AlphaClient");
        modMenuKey = new KeyBinding("AlphaClient: Open Mod Menu", Keyboard.KEY_RCONTROL, "AlphaClient");
        ClientRegistry.registerKeyBinding(bhopKey);
        ClientRegistry.registerKeyBinding(modMenuKey);

        System.out.println("[AlphaClient] Core initialized. Mode: " + PermissionService.getStatusText());
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) {
            return;
        }

        if (modMenuKey != null && modMenuKey.isPressed()) {
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new com.alpharip.staffmod.gui.AlphaModMenu(
                        new com.alpharip.staffmod.gui.AlphaMainMenu()));
            }
            return;
        }

        if (bhopKey != null && bhopKey.isPressed()) {
            if (!PermissionService.hasAccess(PermissionService.Rank.STAFF)) {
                System.out.println("[AlphaClient] BHOP requires verified Staff access or Founder offline test mode.");
                return;
            }
            AlphaModule module = AlphaModuleRegistry.find("bhop");
            boolean enable = module == null || !module.isEnabled();
            if (module != null) {
                module.setEnabled(enable);
            }
            BhopMode.setEnabled(enable);
        }
    }
}