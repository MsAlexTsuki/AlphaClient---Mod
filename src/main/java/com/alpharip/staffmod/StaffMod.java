package com.alpharip.staffmod;

import com.alpharip.staffmod.config.HudPositionConfig;
import com.alpharip.staffmod.gui.MainMenuGui;
import com.alpharip.staffmod.hud.BhopHud;
import com.alpharip.staffmod.hud.KnockbackHud;
import com.alpharip.staffmod.hud.TargetHud;
import com.alpharip.staffmod.modules.LongJumpMod;
import com.alpharip.staffmod.movement.BhopMode;
import com.alpharip.staffmod.network.PacketSniffer;
import com.alpharip.staffmod.ranks.PlayerRankManager;
import com.alpharip.staffmod.util.CPSTracker;
import com.alpharip.staffmod.util.KnockbackTracker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiConnecting;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.settings.KeyBinding;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import org.lwjgl.input.Keyboard;

@Mod(
        modid = StaffMod.MODID,
        name = StaffMod.NAME,
        version = StaffMod.VERSION,
        clientSideOnly = true
)
public class StaffMod {

    public static final String MODID = "alphastaffmod";
    public static final String NAME = "Alpha Client";
    public static final String VERSION = BuildInfo.VERSION;

    public static StaffMod instance;

    public static BhopMode bhopMode;
    public static LongJumpMod longJumpMod;

    private KeyBinding settingsKey;
    private KeyBinding settingsKeyRControl;
    private KeyBinding bhopKey;

    // Partner Server launches should show the Minecraft menu before connecting.
    private boolean partnerServerAutoConnectHandled = false;
    private int partnerServerWaitTicks = 0;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

        instance = this;

        System.out.println(
                "[AlphaStaffMod] Initializing Alpha Client v"
                        + VERSION
                        + "..."
        );

        HudPositionConfig.load();

        TargetHud.loadHudPosition();
        KnockbackTracker.loadHudPosition();
        BhopHud.loadHudPosition();

        MinecraftForge.EVENT_BUS.register(this);

        TargetHud targetHud = new TargetHud();
        MinecraftForge.EVENT_BUS.register(targetHud);

        KnockbackHud knockbackHud = new KnockbackHud();
        MinecraftForge.EVENT_BUS.register(knockbackHud);

        BhopHud bhopHud = new BhopHud();
        MinecraftForge.EVENT_BUS.register(bhopHud);

        bhopMode = new BhopMode();

        longJumpMod = LongJumpMod.getInstance();
        MinecraftForge.EVENT_BUS.register(longJumpMod);

        PlayerRankManager playerRankManager =
                new PlayerRankManager();

        MinecraftForge.EVENT_BUS.register(playerRankManager);

        // M = Settings
        settingsKey = new KeyBinding(
                "Alpha Staff Mod Settings",
                Keyboard.KEY_M,
                "Alpha Staff Mod"
        );

        // RCTRL = Settings
        settingsKeyRControl = new KeyBinding(
                "Alpha Staff Mod Settings RCTRL",
                Keyboard.KEY_RCONTROL,
                "Alpha Staff Mod"
        );

        // B = BHOP
        bhopKey = new KeyBinding(
                "Alpha Staff Mod Bhop",
                Keyboard.KEY_B,
                "Alpha Staff Mod"
        );

        ClientRegistry.registerKeyBinding(settingsKey);
        ClientRegistry.registerKeyBinding(settingsKeyRControl);
        ClientRegistry.registerKeyBinding(bhopKey);

        System.out.println(
                "[AlphaStaffMod] Initialization complete!"
        );
    }

    @SubscribeEvent
    public void onKeyInput(
            InputEvent.KeyInputEvent event) {

        Minecraft mc = Minecraft.getMinecraft();

        if (mc == null) {
            return;
        }

        // M of RCTRL = Staff Mod Settings
        if (settingsKey.isPressed()
                || settingsKeyRControl.isPressed()) {

            if (mc.currentScreen == null) {

                mc.displayGuiScreen(
                        new MainMenuGui()
                );
            }

            return;
        }

        // B = BHOP
        if (bhopKey.isPressed()) {

            BhopMode.toggle();

            return;
        }
    }

    @SubscribeEvent
    public void onClientTick(
            TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        if (mc == null) {
            return;
        }

        // The launcher passes this property only for its Partner Server button.
        // Wait for the main menu to initialize and render before opening the
        // normal Minecraft connection screen instead of connecting during startup.
        if (!partnerServerAutoConnectHandled) {
            String partnerServer = System.getProperty("alphaclient.partnerServer");

            if (partnerServer != null && !partnerServer.trim().isEmpty()
                    && mc.currentScreen instanceof GuiMainMenu) {
                partnerServerWaitTicks++;

                if (partnerServerWaitTicks >= 20) {
                    partnerServerAutoConnectHandled = true;
                    System.clearProperty("alphaclient.partnerServer");

                    mc.displayGuiScreen(new GuiConnecting(
                            mc.currentScreen,
                            mc,
                            new ServerData("Alpha.Rip", partnerServer.trim(), false)
                    ));
                }
            }
        }

        if (mc.thePlayer == null) {
            return;
        }

        KnockbackTracker.tick();
    }

    @SubscribeEvent
    public void onConnect(
            FMLNetworkEvent.ClientConnectedToServerEvent event) {

        try {

            if (event == null) {

                System.out.println(
                        "[AlphaStaffMod] Connect event was null."
                );

                return;
            }

            if (event.manager == null) {

                System.out.println(
                        "[AlphaStaffMod] Network manager was null."
                );

                System.out.println(
                        "[AlphaStaffMod] Packet sniffer skipped."
                );

                return;
            }

            if (event.manager.channel() == null) {

                System.out.println(
                        "[AlphaStaffMod] Network channel was null."
                );

                System.out.println(
                        "[AlphaStaffMod] Packet sniffer skipped."
                );

                return;
            }

            if (event.manager.channel().pipeline() == null) {

                System.out.println(
                        "[AlphaStaffMod] Network pipeline was null."
                );

                System.out.println(
                        "[AlphaStaffMod] Packet sniffer skipped."
                );

                return;
            }

            if (!event.manager
                    .channel()
                    .pipeline()
                    .names()
                    .contains(
                            PacketSniffer.HANDLER_NAME
                    )) {

                event.manager
                        .channel()
                        .pipeline()
                        .addBefore(
                                "packet_handler",
                                PacketSniffer.HANDLER_NAME,
                                new PacketSniffer()
                        );

                System.out.println(
                        "[AlphaStaffMod] Packet sniffer enabled."
                );

            } else {

                System.out.println(
                        "[AlphaStaffMod] Packet sniffer already enabled."
                );
            }

        } catch (Throwable t) {

            System.out.println(
                    "[AlphaStaffMod] Packet sniffer could not be enabled."
            );

            t.printStackTrace();

            System.out.println(
                    "[AlphaStaffMod] Continuing without packet sniffer."
            );
        }
    }

    @SubscribeEvent
    public void onDisconnect(
            FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {

        CPSTracker.clear();

        KnockbackTracker.reset();

        System.out.println(
                "[AlphaStaffMod] Disconnected - CPS and Knockback trackers cleared."
        );
    }
}
