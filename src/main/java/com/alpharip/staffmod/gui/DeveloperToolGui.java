package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Founder-only account inspection tools inside the Minecraft Alpha Client. */
public final class DeveloperToolGui extends AlphaGuiScreen {
    public enum Page { RANK, VERIFICATION, PERMISSIONS, MODULES }

    private static final int BACK = 1;
    private static final int LOAD_ACCOUNTS = 2;
    private static final int REFRESH = 3;
    private static final int VERIFY_TOGGLE = 4;
    private static final int ACCENT = 0xFFFFD866;
    private static final int ACCOUNT_BUTTON_BASE = 1000;
    private static final int ACCOUNT_ROWS = 7;

    private final DeveloperGui parent;
    private final Page page;
    private int panelLeft, panelTop, panelWidth, panelHeight;
    private int accountScroll;
    private int detailScroll;
    private final List<String> accounts = new ArrayList<String>();
    private final List<String> lines = new ArrayList<String>();
    private String selectedUsername;
    private boolean selectedVerified;
    private boolean loading;
    private String status = "Load accounts to select an Alpha account.";
    private int accountListTop, accountListBottom, detailTop, detailBottom;

    public DeveloperToolGui(DeveloperGui parent, Page page) {
        this.parent = parent;
        this.page = page;
    }

    private boolean isFounder() {
        return SupabaseCommunityService.isSignedInFounder()
                && (PermissionService.hasAccess(PermissionService.Rank.FOUNDER)
                    || Boolean.TRUE.equals(PermissionService.getCommandPermissionOverride("staffmods.founder")));
    }

    private boolean hasAccess() {
        boolean developerRank = PermissionService.hasAccess(PermissionService.Rank.DEVELOPER)
                || Boolean.TRUE.equals(PermissionService.getCommandPermissionOverride("staffmods.developer"));
        return DeveloperModeState.isEnabled() && (isFounder() || developerRank);
    }

    @Override
    public void initGui() {
        buttonList.clear();
        if (!hasAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }
        panelWidth = Math.min(650, Math.max(400, width - 24));
        panelHeight = Math.min(390, Math.max(260, height - 24));
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;

        int contentTop = panelTop + 66;
        int contentBottom = panelTop + panelHeight - 42;
        accountListTop = contentTop + 23;
        accountListBottom = contentBottom;
        detailTop = contentTop;
        detailBottom = contentBottom;

        int leftWidth = Math.max(130, Math.min(190, panelWidth / 3));
        int accountButtonWidth = leftWidth - 12;
        int rowHeight = 19;
        for (int i = accountScroll; i < accounts.size() && i < accountScroll + ACCOUNT_ROWS; i++) {
            int row = i - accountScroll;
            buttonList.add(new AlphaButton(ACCOUNT_BUTTON_BASE + i,
                    panelLeft + 12, accountListTop + row * (rowHeight + 3),
                    accountButtonWidth, rowHeight, clip(accounts.get(i), 22)));
        }
        buttonList.add(new AlphaButton(LOAD_ACCOUNTS, panelLeft + 12, panelTop + panelHeight - 34,
                116, 22, "LOAD ACCOUNTS"));
        buttonList.add(new AlphaButton(REFRESH, panelLeft + 134, panelTop + panelHeight - 34,
                78, 22, "REFRESH"));
        if (page == Page.VERIFICATION && selectedUsername != null) {
            buttonList.add(new AlphaButton(VERIFY_TOGGLE, panelLeft + panelWidth - 232,
                    panelTop + panelHeight - 34, 136, 22,
                    selectedVerified ? "REMOVE VERIFICATION" : "VERIFY ACCOUNT"));
        }
        buttonList.add(new AlphaButton(BACK, panelLeft + panelWidth - 88,
                panelTop + panelHeight - 34, 76, 22, "BACK"));
    }

    private String clip(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, Math.max(1, max - 3)) + "...";
    }

    private JsonObject call(String action, String username, Boolean verified) throws Exception {
        JsonObject request = new JsonObject();
        request.addProperty("action", action);
        if (username != null) request.addProperty("username", username);
        if (verified != null) request.addProperty("verified", verified.booleanValue());
        JsonObject response = SupabaseCommunityService.manageCommandPermissions(request);
        if (response == null) throw new IllegalStateException("Alpha community returned an empty response.");
        if (response.has("error") && !response.get("error").isJsonNull()) {
            throw new IllegalStateException(response.get("error").getAsString());
        }
        if (response.has("ok") && !response.get("ok").getAsBoolean()) {
            throw new IllegalStateException("Alpha community rejected the request.");
        }
        return response;
    }

    private void loadAccounts() {
        loading = true;
        status = "Loading Alpha accounts...";
        try {
            JsonObject response = call("list_accounts", null, null);
            JsonArray result = response.getAsJsonArray("accounts");
            accounts.clear();
            if (result != null) {
                for (JsonElement item : result) {
                    if (item != null && !item.isJsonNull()) {
                        String name = item.getAsString().trim();
                        if (!name.isEmpty()) accounts.add(name);
                    }
                }
            }
            accountScroll = Math.max(0, Math.min(accountScroll, Math.max(0, accounts.size() - ACCOUNT_ROWS)));
            status = accounts.isEmpty() ? "No Alpha accounts were returned." : "Select an account from the list.";
            if (selectedUsername != null && !accounts.contains(selectedUsername)) {
                selectedUsername = null;
                selectedVerified = false;
                lines.clear();
            }
        } catch (Exception ex) {
            status = readableError(ex);
        } finally {
            loading = false;
            initGui();
        }
    }

    private String readableError(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.trim().isEmpty()) return "Request failed. Check the Alpha Account session.";
        if (message.toLowerCase(Locale.ROOT).contains("founder-only")
                || message.toLowerCase(Locale.ROOT).contains("founder")) {
            return "Founder access required. Sign in with the Founder Alpha Account.";
        }
        if (message.toLowerCase(Locale.ROOT).contains("expired")
                || message.toLowerCase(Locale.ROOT).contains("jwt")) {
            return "Alpha Account session expired. Sign in again.";
        }
        return message;
    }

    private void selectAccount(String username) {
        if (username == null || username.trim().isEmpty()) return;
        loading = true;
        selectedUsername = username;
        status = "Loading " + username + "...";
        lines.clear();
        try {
            JsonObject inspection = call("inspect_account", username, null);
            JsonObject account = inspection.getAsJsonObject("account");
            if (account == null) throw new IllegalStateException("Account details were missing from the response.");
            lines.add("Account: " + value(account, "username", username));
            String displayName = value(account, "display_name", "");
            if (!displayName.trim().isEmpty()) lines.add("Display name: " + displayName);
            String primaryRank = value(account, "primary_rank", "unknown");
            lines.add("Primary rank: " + rankLabel(primaryRank));
            lines.add("Founder account: " + bool(account, "is_founder"));
            lines.add("Assigned ranks:");
            JsonArray ranks = account.getAsJsonArray("ranks");
            if (ranks == null || ranks.size() == 0) {
                lines.add("  (none)");
            } else {
                for (JsonElement rank : ranks) lines.add("  " + rankLabel(rank.getAsString()));
            }

            JsonObject verification = account.getAsJsonObject("verification");
            selectedVerified = verification != null && verification.has("verified")
                    && verification.get("verified").getAsBoolean();
            lines.add("Verification: " + (selectedVerified ? "VERIFIED" : "NOT VERIFIED"));
            if (verification != null && verification.has("verified_at") && !verification.get("verified_at").isJsonNull()) {
                lines.add("Verified at: " + verification.get("verified_at").getAsString());
            }

            if (page == Page.PERMISSIONS || page == Page.MODULES) {
                JsonObject permissionResponse = call("list", username, null);
                JsonArray permissions = permissionResponse.getAsJsonArray("permissions");
                lines.add("--------------------------------");
                lines.add(page == Page.MODULES ? "MODULE ACCESS" : "EFFECTIVE PERMISSIONS");
                if (permissions == null || permissions.size() == 0) {
                    lines.add("No permission records returned.");
                } else {
                    for (JsonElement item : permissions) {
                        if (!item.isJsonObject()) continue;
                        JsonObject permission = item.getAsJsonObject();
                        String key = value(permission, "permission_key", "unknown");
                        if (page == Page.MODULES && !key.toLowerCase(Locale.ROOT).startsWith("mod.")) continue;
                        String label = value(permission, "label", value(permission, "display_name", key));
                        String category = value(permission, "category", "");
                        boolean allowed = bool(permission, "allowed");
                        boolean configured = bool(permission, "configured");
                        String source = value(permission, "source", configured ? "configured" : "default");
                        String description = value(permission, "description", "");
                        lines.add((allowed ? "ALLOW  " : "DENY   ") + label);
                        lines.add("  Key: " + key);
                        if (!category.isEmpty()) lines.add("  Category: " + category);
                        lines.add("  Source: " + sourceLabel(source, permission));
                        if (!description.isEmpty()) lines.add("  " + description);
                    }
                }
            }
            status = "Loaded account: " + username;
        } catch (Exception ex) {
            lines.clear();
            status = readableError(ex);
        } finally {
            loading = false;
            detailScroll = 0;
            initGui();
        }
    }

    private String sourceLabel(String source, JsonObject permission) {
        if ("founder".equalsIgnoreCase(source)) return "Founder default";
        if ("override".equalsIgnoreCase(source) || "account_override".equalsIgnoreCase(source)) return "Account override";
        if ("rank".equalsIgnoreCase(source)) return "Assigned rank permission";
        if ("default".equalsIgnoreCase(source)) return "Default / not explicitly configured";
        return source;
    }

    private String value(JsonObject object, String key, String fallback) {
        try {
            JsonElement value = object.get(key);
            return value == null || value.isJsonNull() ? fallback : value.getAsString();
        } catch (Exception ignored) { return fallback; }
    }

    private boolean bool(JsonObject object, String key) {
        try {
            JsonElement value = object.get(key);
            return value != null && !value.isJsonNull() && value.getAsBoolean();
        } catch (Exception ignored) { return false; }
    }

    private String rankLabel(String rank) {
        if (rank == null) return "Unknown";
        String key = rank.toLowerCase(Locale.ROOT);
        if ("plat_admin".equals(key)) return "Platform Admin";
        if ("senior_admin".equals(key) || "sr_admin".equals(key)) return "Sr Admin";
        if ("senior_moderator".equals(key) || "sr_mod".equals(key)) return "Sr Mod";
        if ("trial_moderator".equals(key) || "t_mod".equals(key)) return "Trial Moderator";
        if ("the_nibbler".equals(key)) return "The Nibbler";
        if ("alpha_plus".equals(key)) return "Alpha+";
        return rank.replace('_', ' ').toUpperCase(Locale.ROOT);
    }

    private void toggleVerification() {
        if (selectedUsername == null || loading) return;
        loading = true;
        status = "Saving verification...";
        try {
            boolean nextVerified = !selectedVerified;
            call("set_verification", selectedUsername, Boolean.valueOf(nextVerified));
            selectAccount(selectedUsername);
            status = nextVerified ? selectedUsername + " verified successfully." : "Verification removed for " + selectedUsername + ".";
        } catch (Exception ex) {
            status = readableError(ex);
            loading = false;
            initGui();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!hasAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (button == null) return;
        if (button.id == BACK) {
            mc.displayGuiScreen(parent);
        } else if (button.id == LOAD_ACCOUNTS || button.id == REFRESH) {
            loadAccounts();
        } else if (button.id == VERIFY_TOGGLE) {
            toggleVerification();
        } else if (button.id >= ACCOUNT_BUTTON_BASE && button.id < ACCOUNT_BUTTON_BASE + accounts.size()) {
            int index = button.id - ACCOUNT_BUTTON_BASE;
            if (index >= 0 && index < accounts.size()) selectAccount(accounts.get(index));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && mouseX >= panelLeft + 12 && mouseX <= panelLeft + 204
                && mouseY >= accountListBottom && mouseY < panelTop + panelHeight - 42) {
            if (mouseY < accountListTop) {
                if (accountScroll > 0) { accountScroll--; initGui(); }
            } else if (accounts.size() > accountScroll + ACCOUNT_ROWS) {
                accountScroll++;
                initGui();
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!hasAccess()) {
            mc.displayGuiScreen(parent);
            return;
        }
        drawRect(0, 0, width, height, 0x30000000);
        int right = panelLeft + panelWidth;
        int bottom = panelTop + panelHeight;
        drawRect(panelLeft, panelTop, right, bottom, 0xE00B0D12);
        drawRect(panelLeft, panelTop, right, panelTop + 1, ACCENT);
        drawRect(panelLeft, panelTop + 1, panelLeft + 1, bottom - 1, 0x554B515D);
        drawRect(right - 1, panelTop + 1, right, bottom - 1, 0x554B515D);
        drawString(fontRendererObj, "ALPHA CLIENT", panelLeft + 16, panelTop + 12, 0xFFF0F1F4);
        drawString(fontRendererObj, "STAFFMODS  /  DEVELOPER  /  " + pageTitle(), panelLeft + 16, panelTop + 30, ACCENT);

        int leftWidth = Math.max(130, Math.min(190, panelWidth / 3));
        int dividerX = panelLeft + leftWidth + 12;
        drawRect(dividerX, panelTop + 54, dividerX + 1, panelTop + panelHeight - 42, 0x554B515D);
        drawString(fontRendererObj, "ALPHA ACCOUNTS", panelLeft + 12, panelTop + 52, 0xFFE7E9EE);
        drawString(fontRendererObj, "Click LOAD ACCOUNTS to retrieve the list.", panelLeft + 12,
                panelTop + 64, 0xFFB8BDC8);

        int detailX = dividerX + 12;
        drawString(fontRendererObj, selectedUsername == null ? "ACCOUNT DETAILS" : selectedUsername,
                detailX, panelTop + 52, 0xFFE7E9EE);
        drawString(fontRendererObj, status, detailX, panelTop + 64, status.toLowerCase(Locale.ROOT).contains("failed")
                || status.toLowerCase(Locale.ROOT).contains("required")
                || status.toLowerCase(Locale.ROOT).contains("expired") ? 0xFFFFC56B : 0xFFB8BDC8);

        int y = detailTop + 26;
        int maxY = detailBottom - 4;
        for (int i = detailScroll; i < lines.size() && y < maxY; i++) {
            String line = lines.get(i);
            int color = line.startsWith("ALLOW") || line.endsWith("VERIFIED")
                    || line.startsWith("Founder account: true") ? 0xFF7DDBA3
                    : (line.startsWith("DENY") || line.endsWith("NOT VERIFIED")
                        || line.startsWith("Founder account: false") ? 0xFFFFC56B : 0xFFE7E9EE);
            drawString(fontRendererObj, clip(line, Math.max(28, (panelWidth - leftWidth - 52) / 6)), detailX, y, color);
            y += 13;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String pageTitle() {
        switch (page) {
            case RANK: return "RANK INSPECTOR";
            case VERIFICATION: return "VERIFICATION";
            case PERMISSIONS: return "PERMISSION INSPECTOR";
            case MODULES: return "MODULE INSPECTOR";
            default: return "TOOLS";
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            if (Mouse.getEventX() < (panelLeft + panelWidth / 3) * mc.displayWidth / Math.max(1, width)) {
                int max = Math.max(0, accounts.size() - ACCOUNT_ROWS);
                accountScroll = Math.max(0, Math.min(max, accountScroll + (wheel < 0 ? 1 : -1)));
                initGui();
            } else if (lines.size() > 10) {
                int max = Math.max(0, lines.size() - 10);
                detailScroll = Math.max(0, Math.min(max, detailScroll + (wheel < 0 ? 1 : -1)));
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
