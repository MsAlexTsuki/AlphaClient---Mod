package com.alpharip.staffmod.pvp;

/**
 * Honest capability inventory for the Legacy PvP settings screen.
 *
 * A feature is marked CONFIGURABLE only when the current client code actually
 * applies it. Informational entries are deliberately not exposed as switches.
 */
public final class LegacyPvpFeatureCatalog {
    public enum Scope {
        CLIENT_VISUAL,
        CLIENT_INPUT,
        SERVER_DEPENDENT
    }

    public enum Status {
        CONFIGURABLE,
        RENDER_HOOK_REQUIRED,
        INPUT_HOOK_REQUIRED,
        SERVER_REQUIRED
    }

    public static final class Feature {
        public final String id;
        public final String label;
        public final Scope scope;
        public final Status status;
        public final String detail;

        private Feature(String id, String label, Scope scope, Status status, String detail) {
            this.id = id;
            this.label = label;
            this.scope = scope;
            this.status = status;
            this.detail = detail;
        }
    }

    private static final Feature[] FEATURES = new Feature[] {
        new Feature("blockhit", "True 1.7.10 blockhit animation", Scope.CLIENT_VISUAL, Status.RENDER_HOOK_REQUIRED,
                "Needs a verified first-person render hook."),
        new Feature("sword-swing", "Legacy sword swing", Scope.CLIENT_VISUAL, Status.RENDER_HOOK_REQUIRED,
                "Needs a verified swing-animation hook."),
        new Feature("item-transform", "Legacy first-person item position / rotation", Scope.CLIENT_VISUAL, Status.RENDER_HOOK_REQUIRED,
                "Must preserve bow, food, drink and map rendering."),
        new Feature("rod-animation", "Legacy fishing-rod animation", Scope.CLIENT_VISUAL, Status.RENDER_HOOK_REQUIRED,
                "Needs item-use rendering changes."),
        new Feature("bow-animation", "Legacy bow animation", Scope.CLIENT_VISUAL, Status.RENDER_HOOK_REQUIRED,
                "Needs item-use rendering changes."),
        new Feature("hit-effects", "Legacy hit and damage effects", Scope.CLIENT_VISUAL, Status.RENDER_HOOK_REQUIRED,
                "Visual effects only; does not change damage."),
        new Feature("camera-bobbing", "Disable camera bobbing", Scope.CLIENT_VISUAL, Status.CONFIGURABLE,
                "Implemented: restores the previous game setting when disabled."),
        new Feature("sneak-walk", "Legacy sneak and walk visuals", Scope.CLIENT_VISUAL, Status.RENDER_HOOK_REQUIRED,
                "Visual animation only; not movement speed."),
        new Feature("attack-block", "Combine attacking and blocking", Scope.CLIENT_INPUT, Status.INPUT_HOOK_REQUIRED,
                "Must be validated against 1.8.9 input and server acceptance."),
        new Feature("attack-use", "Combine attacking and item use", Scope.CLIENT_INPUT, Status.INPUT_HOOK_REQUIRED,
                "Only valid for compatible item/action combinations."),
        new Feature("sprint-reset", "Sprint reset / W-tapping assistance", Scope.CLIENT_INPUT, Status.INPUT_HOOK_REQUIRED,
                "Input behavior cannot guarantee server-side knockback."),
        new Feature("attack-place", "Attack and block-placement interaction", Scope.CLIENT_INPUT, Status.INPUT_HOOK_REQUIRED,
                "Server still validates attacks and placement."),
        new Feature("rod-hotbar", "Rod and hotbar switching behavior", Scope.CLIENT_INPUT, Status.INPUT_HOOK_REQUIRED,
                "Requires input-state and inventory testing."),
        new Feature("hit-timing", "Hit delay and damage timing", Scope.SERVER_DEPENDENT, Status.SERVER_REQUIRED,
                "The client cannot override server combat timing."),
        new Feature("knockback", "Knockback profiles", Scope.SERVER_DEPENDENT, Status.SERVER_REQUIRED,
                "Client-only profiles cannot dictate server-applied velocity."),
        new Feature("hit-registration", "Hit registration behavior", Scope.SERVER_DEPENDENT, Status.SERVER_REQUIRED,
                "The server decides whether a hit is accepted."),
        new Feature("reach-hitbox", "Reach and hitbox behavior", Scope.SERVER_DEPENDENT, Status.SERVER_REQUIRED,
                "Local targeting cannot override server validation."),
        new Feature("weapon-values", "Legacy weapon damage / enchantment values", Scope.SERVER_DEPENDENT, Status.SERVER_REQUIRED,
                "Authoritative damage and enchantment outcomes are server-controlled."),
        new Feature("projectiles", "Legacy projectile and fishing-rod interactions", Scope.SERVER_DEPENDENT, Status.SERVER_REQUIRED,
                "Rendering is client-side; authoritative hits and effects are server-side.")
    };

    private LegacyPvpFeatureCatalog() { }

    public static Feature[] getAll() {
        return FEATURES.clone();
    }

    public static String getStatusLabel(Status status) {
        switch (status) {
            case CONFIGURABLE: return "AVAILABLE";
            case RENDER_HOOK_REQUIRED: return "RENDER HOOK NEEDED";
            case INPUT_HOOK_REQUIRED: return "INPUT HOOK NEEDED";
            case SERVER_REQUIRED: return "SERVER REQUIRED";
            default: return "UNKNOWN";
        }
    }
}
