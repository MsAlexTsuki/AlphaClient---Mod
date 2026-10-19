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
        CLIENT_INPUT
    }

    public enum Status {
        CONFIGURABLE,
        RENDER_HOOK_REQUIRED,
        INPUT_HOOK_REQUIRED
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
        new Feature("blockhit", "True 1.7.10 blockhit animation", Scope.CLIENT_VISUAL, Status.CONFIGURABLE,
                "Adds swing progress to the sword-blocking first-person render branch."),
        new Feature("potion-punch", "Potion punch animation", Scope.CLIENT_VISUAL, Status.CONFIGURABLE,
                "Allows a left-click swing while a splash potion is held and right-click is down. It does not cancel the potion throw or create a server-side attack."),
        new Feature("missed-swing-delay", "Remove missed-swing delay in air", Scope.CLIENT_INPUT, Status.CONFIGURABLE,
                "Optionally clears the local miss-in-air click delay while targeting air; block and entity timing are preserved."),
        new Feature("sword-swing", "Legacy sword swing", Scope.CLIENT_VISUAL, Status.CONFIGURABLE,
                "Adds a 1.7.10-style first-person sword swing transform; independently configurable from blockhit."),
        new Feature("rod-animation", "Legacy fishing-rod animation", Scope.CLIENT_VISUAL, Status.CONFIGURABLE,
                "Adds a restrained 1.7.10-style first-person swing transform while holding a fishing rod."),
        new Feature("bow-animation", "Legacy 1.7.10 bow animation", Scope.CLIENT_VISUAL, Status.CONFIGURABLE,
                "Restores the 1.7.10 bow swing and midpoint-anchored draw stretch; can be switched off for vanilla 1.8.9 bow transforms."),
        new Feature("food-drink-animation", "Legacy food and potion-drinking swing", Scope.CLIENT_VISUAL, Status.CONFIGURABLE,
                "Independently restores the legacy first-person swing while eating food or drinking a potion."),
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
            default: return "UNKNOWN";
        }
    }
}
