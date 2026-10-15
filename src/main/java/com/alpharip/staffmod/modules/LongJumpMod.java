package com.alpharip.staffmod.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class LongJumpMod {

    /*
     * =========================================================
     * INSTANCE
     * =========================================================
     */

    private static LongJumpMod instance;


    /*
     * =========================================================
     * MODULE IDENTITY
     * =========================================================
     */

    public static final String NAME =
            "DoubleJump";

    public static final String DESCRIPTION =
            "Allows you to jump up to 5 times in one airtime.";


    /*
     * =========================================================
     * STATE
     * =========================================================
     */

    private boolean enabled =
            false;

    /*
     * The normal Minecraft jump counts as jump 1.
     *
     * 1 = normal ground jump
     * 2 = first air jump
     * 3 = second air jump
     * 4 = third air jump
     * 5 = fourth air jump
     *
     * Total = 5 jumps.
     */
    private int jumpsUsed =
            0;

    /*
     * Prevent holding SPACE from triggering multiple jumps.
     *
     * A new jump is only accepted after SPACE has been
     * released.
     */
    private boolean jumpKeyWasDown =
            false;


    /*
     * =========================================================
     * SETTINGS
     * =========================================================
     *
     * These fields are kept for backwards compatibility with
     * existing GUI/config code.
     *
     * Double Jump itself uses Minecraft's normal jump()
     * strength.
     */

    private float horizontalPower =
            2.6f;

    private float verticalPower =
            0.42f;

    private int cooldown =
            10;

    private boolean requireSneakToArm =
            false;


    /*
     * =========================================================
     * CONSTRUCTOR
     * =========================================================
     */

    private LongJumpMod() {
    }


    /*
     * =========================================================
     * INSTANCE
     * =========================================================
     */

    public static LongJumpMod getInstance() {

        if (instance == null) {

            instance =
                    new LongJumpMod();
        }

        return instance;
    }


    /*
     * =========================================================
     * MODULE STATE
     * =========================================================
     */

    public boolean isEnabled() {

        return enabled;
    }


    public void setEnabled(
            boolean enabled) {

        this.enabled =
                enabled;

        if (!enabled) {

            jumpsUsed = 0;
            jumpKeyWasDown = false;
        }
    }


    public void toggle() {

        this.enabled =
                !this.enabled;

        if (!this.enabled) {

            jumpsUsed = 0;
            jumpKeyWasDown = false;
        }

        System.out.println(
                "[AlphaStaffMod] DOUBLEJUMP: "
                        + (
                        this.enabled
                                ? "ON"
                                : "OFF"
                )
        );
    }


    /*
     * =========================================================
     * MODULE INFORMATION
     * =========================================================
     */

    public String getName() {

        return NAME;
    }


    public String getDescription() {

        return DESCRIPTION;
    }


    /*
     * =========================================================
     * SETTINGS GETTERS / SETTERS
     * =========================================================
     *
     * Kept so existing code that still references the old
     * LongJump settings does not break.
     */

    public float getHorizontalPower() {

        return horizontalPower;
    }


    public void setHorizontalPower(
            float value) {

        this.horizontalPower =
                value;
    }


    public float getVerticalPower() {

        return verticalPower;
    }


    public void setVerticalPower(
            float value) {

        this.verticalPower =
                value;
    }


    public int getCooldown() {

        return cooldown;
    }


    public void setCooldown(
            int value) {

        this.cooldown =
                value;
    }


    public boolean isRequireSneakToArm() {

        return requireSneakToArm;
    }


    public void setRequireSneakToArm(
            boolean value) {

        this.requireSneakToArm =
                value;
    }


    /*
     * =========================================================
     * CLIENT TICK
     * =========================================================
     *
     * Reset the jump counter when the player lands.
     */

    @SubscribeEvent
    public void onClientTick(
            TickEvent.ClientTickEvent event) {

        if (event.phase
                != TickEvent.Phase.END) {

            return;
        }

        Minecraft mc =
                Minecraft.getMinecraft();

        if (mc == null
                || mc.thePlayer == null) {

            jumpsUsed = 0;
            jumpKeyWasDown = false;

            return;
        }

        EntityPlayerSP player =
                mc.thePlayer;

        /*
         * Every time the player touches the ground,
         * a completely new set of 5 jumps becomes available.
         */
        if (player.onGround) {

            jumpsUsed = 0;
        }
    }


    /*
     * =========================================================
     * JUMP KEY INPUT
     * =========================================================
     *
     * Only a real SPACE press can trigger an extra jump.
     *
     * Holding SPACE does NOT repeatedly jump.
     *
     * The key must first be released before another jump
     * can be registered.
     */

    @SubscribeEvent
    public void onKeyInput(
            InputEvent.KeyInputEvent event) {

        Minecraft mc =
                Minecraft.getMinecraft();

        if (mc == null
                || mc.thePlayer == null
                || mc.gameSettings == null
                || mc.gameSettings.keyBindJump == null) {

            return;
        }

        boolean jumpKeyDown =
                mc.gameSettings.keyBindJump.isKeyDown();

        /*
         * SPACE released.
         *
         * This arms the next possible jump.
         */
        if (!jumpKeyDown) {

            jumpKeyWasDown = false;

            return;
        }

        /*
         * SPACE is still being held.
         *
         * Do not trigger another jump.
         */
        if (jumpKeyWasDown) {

            return;
        }

        /*
         * This is a brand-new SPACE press.
         */
        jumpKeyWasDown = true;

        /*
         * If Double Jump is disabled, do nothing.
         */
        if (!enabled) {

            return;
        }

        EntityPlayerSP player =
                mc.thePlayer;


        /*
         * =====================================================
         * GROUND JUMP
         * =====================================================
         *
         * The normal Minecraft ground jump remains completely
         * normal.
         *
         * We only count it as jump 1.
         */
        if (player.onGround) {

            jumpsUsed = 1;

            return;
        }


        /*
         * =====================================================
         * CREATIVE FLIGHT
         * =====================================================
         *
         * Do not interfere with Creative flight.
         */
        if (player.capabilities.isFlying) {

            return;
        }


        /*
         * =====================================================
         * MAXIMUM JUMPS
         * =====================================================
         *
         * 1 = ground jump
         * 2 = air jump
         * 3 = air jump
         * 4 = air jump
         * 5 = air jump
         */
        if (jumpsUsed >= 5) {

            return;
        }


        /*
         * Perform exactly one additional jump.
         */
        performAirJump(player);
    }


    /*
     * =========================================================
     * FALL DAMAGE PROTECTION
     * =========================================================
     *
     * While Double Jump is enabled, falling after the jumps
     * will not cause normal Minecraft fall damage.
     *
     * This is handled through Forge's LivingFallEvent.
     *
     * We intentionally only affect the local player.
     */

    @SubscribeEvent
    public void onLivingFall(
            LivingFallEvent event) {

        /*
         * Double Jump must be enabled.
         */
        if (!enabled) {

            return;
        }

        /*
         * Only affect the local client player.
         */
        if (!(event.entityLiving instanceof EntityPlayerSP)) {

            return;
        }

        EntityPlayerSP player =
                (EntityPlayerSP) event.entityLiving;


        /*
         * Creative flight remains untouched.
         */
        if (player.capabilities.isCreativeMode) {

            return;
        }

        if (player.capabilities.isFlying) {

            return;
        }


        /*
         * Cancel the normal fall event.
         *
         * Forge 1.8.9 treats LivingFallEvent as cancelable.
         */
        event.setCanceled(true);


        /*
         * Reset fall distance as an additional safeguard.
         */
        player.fallDistance =
                0.0F;
    }


    /*
     * =========================================================
     * EXTERNAL TRIGGER
     * =========================================================
     *
     * Kept public for compatibility with the existing central
     * keybind system.
     *
     * This performs one additional jump only when the player
     * is already airborne.
     */

    public void trigger() {

        if (!enabled) {

            return;
        }

        Minecraft mc =
                Minecraft.getMinecraft();

        if (mc == null
                || mc.thePlayer == null) {

            return;
        }

        EntityPlayerSP player =
                mc.thePlayer;


        /*
         * Must already be airborne.
         */
        if (player.onGround) {

            return;
        }


        /*
         * Do not interfere with Creative flight.
         */
        if (player.capabilities.isFlying) {

            return;
        }


        /*
         * Maximum of five total jumps.
         */
        if (jumpsUsed >= 5) {

            return;
        }


        /*
         * Perform one air jump.
         */
        performAirJump(player);
    }


    /*
     * =========================================================
     * CORE DOUBLE JUMP LOGIC
     * =========================================================
     *
     * Uses EntityPlayer's normal Minecraft jump() method.
     *
     * Every air jump therefore has the same jump strength as
     * a normal Minecraft jump.
     */

    private void performAirJump(
            EntityPlayerSP player) {

        if (player == null) {

            return;
        }

        /*
         * Do not perform an air jump while standing on ground.
         */
        if (player.onGround) {

            return;
        }

        /*
         * Do not interfere with Creative flight.
         */
        if (player.capabilities.isFlying) {

            return;
        }

        /*
         * Five total jumps maximum.
         */
        if (jumpsUsed >= 5) {

            return;
        }


        /*
         * Use the exact normal Minecraft jump behaviour.
         */
        player.jump();


        /*
         * Count the jump.
         */
        jumpsUsed++;


        /*
         * Prevent accumulated fall distance from being carried
         * into the next jump.
         */
        player.fallDistance =
                0.0F;
    }


    /*
     * =========================================================
     * DEBUG / STATUS
     * =========================================================
     */

    public int getJumpsUsed() {

        return jumpsUsed;
    }


    public int getRemainingJumps() {

        return Math.max(
                0,
                5 - jumpsUsed
        );
    }
}