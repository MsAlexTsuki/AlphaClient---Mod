package com.alpharip.staffmod.modules;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoFallMod {

    private static NoFallMod instance;

    public static final String NAME = "NOFALL";

    public static final String DESCRIPTION =
            "Prevent fall damage in Survival.";

    private boolean enabled = false;

    private NoFallMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static NoFallMod getInstance() {

        if (instance == null) {
            instance = new NoFallMod();
        }

        return instance;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }

    public String getName() {
        return NAME;
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * Prevents fall damage while NoFall is enabled.
     *
     * Forge 1.8.9 fires LivingFallEvent before normal fall
     * damage is processed. Cancelling this event prevents the
     * normal fall from being processed.
     */
    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {

        if (!enabled) {
            return;
        }

        if (!(event.entityLiving instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.entityLiving;

        /*
         * NoFall is only intended for Survival.
         * Creative remains untouched.
         */
        if (player.capabilities.isCreativeMode) {
            return;
        }

        /*
         * Do not interfere with players that are already
         * completely invulnerable.
         */
        if (player.capabilities.disableDamage) {
            return;
        }

        /*
         * Cancel the fall event.
         *
         * In Forge 1.8.9 a cancelled LivingFallEvent prevents
         * the normal fall from being processed.
         */
        event.setCanceled(true);

        /*
         * Reset fall distance as an additional safeguard.
         */
        player.fallDistance = 0.0F;
    }
}