package com.alpharip.staffmod.account;

/** Input validation for local account references; it does not verify account ownership. */
public final class AccountLinkValidator {
    private AccountLinkValidator() { }

    public static String validate(String provider, String accountIdentifier, String displayName) {
        if (!valid(provider, 2, 32)) return "Provider must contain 2-32 characters.";
        if (!valid(accountIdentifier, 2, 128)) return "Account identifier must contain 2-128 characters.";
        if (!valid(displayName, 2, 32)) return "Display name must contain 2-32 characters.";
        return null;
    }

    private static boolean valid(String value, int min, int max) {
        if (value == null) return false;
        String trimmed = value.trim();
        return trimmed.length() >= min && trimmed.length() <= max;
    }
}
