package com.alpharip.staffmod.animalbreedingcooldowntimer;

import net.minecraft.entity.passive.EntityAnimal;

/**
 * Adapter boundary for successful animal-breeding notifications.
 *
 * TODO: Connect this method to a verified Minecraft 1.8.9 / Forge 11.15.1.2318
 * breeding-success hook that identifies each parent animal. Do not infer a
 * successful breeding event merely from an animal being nearby or a baby spawning.
 */
public final class AnimalBreedingEventHandler {
    private final AnimalBreedingCooldownTimer module;

    AnimalBreedingEventHandler(AnimalBreedingCooldownTimer module) {
        this.module = module;
    }

    /** Call only after the game has confirmed that this animal successfully bred. */
    public void onSuccessfulBreeding(EntityAnimal parent) {
        if (parent == null || parent.worldObj == null || parent.worldObj.isRemote) return;
        module.recordSuccessfulBreeding(parent);
    }
}
