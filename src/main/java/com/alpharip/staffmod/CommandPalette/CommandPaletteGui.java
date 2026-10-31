package com.alpharip.staffmod.CommandPalette;

import com.alpharip.staffmod.gui.AlphaClientHomeMenu;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Searchable launcher for AlphaClient screens and accessible registered modules. */
public final class CommandPaletteGui extends GuiScreen {
    private static final int ACCENT = 0xFFFFC83D;
    private static final int ROW_HEIGHT = 32;

    private final GuiScreen parent;
    private GuiTextField searchField;
    private List<Action> availableActions = Collections.emptyList();
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private int listTop;
    private int listBottom;
    private int scroll;
    private int selectedIndex;
    private String status = "Search screens and modules, then press Enter or click.";

    public CommandPaletteGui(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        panelWidth = Math.min(560, Math.max(260, width - 24));
        panelHeight = Math.min(360, Math.max(190, height - 24));
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        listTop = top + 72;
        listBottom = top + panelHeight - 31;
        searchField = new GuiTextField(0, fontRendererObj, left + 14, top + 43, panelWidth - 28, 20);
        searchField.setMaxStringLength(80);
        searchField.setFocused(true);
        availableActions = createActions();
        Keyboard.enableRepeatEvents(true);
        clampSelection();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (keyCode == Keyboard.KEY_UP) {
            moveSelection(-1);
            return;
        }
        if (keyCode == Keyboard.KEY_DOWN) {
            moveSelection(1);
            return;
        }
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            executeSelected();
            return;
        }
        searchField.textboxKeyTyped(typedChar, keyCode);
        selectedIndex = 0;
        clampSelection();
        scroll = 0;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0 || mouseY < listTop || mouseY >= listBottom) return;

        int row = (mouseY - listTop) / ROW_HEIGHT;
        int index = scroll + row;
        List<Action> actions = getFilteredActions();
        if (index >= 0 && index < actions.size()) {
            selectedIndex = index;
            executeSelected();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getDWheel();
        if (wheel == 0) return;
        int visible = visibleRows();
        int maxScroll = Math.max(0, getFilteredActions().size() - visible);
        scroll = Math.max(0, Math.min(maxScroll, scroll + (wheel > 0 ? -1 : 1)));
        selectedIndex = Math.max(scroll, Math.min(selectedIndex, scroll + visible - 1));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x99000000);
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xF00B0D12);
        drawRect(left, top, left + panelWidth, top + 2, ACCENT);
        drawString(fontRendererObj, "COMMAND PALETTE", left + 14, top + 13, 0xFFF2F3F5);
        drawString(fontRendererObj, "Type to search AlphaClient actions", left + 14, top + 28, 0xFFB8BDC8);
        searchField.drawTextBox();

        drawRect(left + 10, listTop - 4, left + panelWidth - 10, listBottom, 0x55212630);
        List<Action> actions = getFilteredActions();
        clampSelection(actions);
        int visible = visibleRows();
        if (actions.isEmpty()) {
            drawString(fontRendererObj, "No matching actions.", left + 20, listTop + 6, 0xFFB8BDC8);
        }
        for (int row = 0; row < visible; row++) {
            int index = scroll + row;
            if (index >= actions.size()) break;
            Action action = actions.get(index);
            int y = listTop + row * ROW_HEIGHT;
            if (index == selectedIndex) {
                drawRect(left + 12, y, left + panelWidth - 12, y + ROW_HEIGHT - 1, 0xAA363B45);
            }
            String title = trim(action.getTitle(), panelWidth - 38);
            String detail = trim(action.description, panelWidth - 38);
            drawString(fontRendererObj, title, left + 20, y + 4,
                    action.module == null ? 0xFFF0F1F4 : (action.module.isEnabled() ? 0xFF77DD88 : 0xFFE6C66B));
            drawString(fontRendererObj, detail, left + 20, y + 17, 0xFFB8BDC8);
        }

        String footer = trim(status + "   ↑/↓  Enter  Esc", panelWidth - 28);
        drawString(fontRendererObj, footer, left + 14, top + panelHeight - 18, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int visibleRows() {
        return Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
    }

    private void moveSelection(int direction) {
        List<Action> actions = getFilteredActions();
        if (actions.isEmpty()) {
            selectedIndex = 0;
            scroll = 0;
            return;
        }
        selectedIndex = Math.max(0, Math.min(actions.size() - 1, selectedIndex + direction));
        int visible = visibleRows();
        if (selectedIndex < scroll) scroll = selectedIndex;
        else if (selectedIndex >= scroll + visible) scroll = selectedIndex - visible + 1;
    }

    private void clampSelection() {
        clampSelection(getFilteredActions());
    }

    private void clampSelection(List<Action> actions) {
        if (actions.isEmpty()) {
            selectedIndex = 0;
            scroll = 0;
            return;
        }
        selectedIndex = Math.max(0, Math.min(selectedIndex, actions.size() - 1));
        int maxScroll = Math.max(0, actions.size() - visibleRows());
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        if (selectedIndex < scroll) scroll = selectedIndex;
        if (selectedIndex >= scroll + visibleRows()) scroll = selectedIndex - visibleRows() + 1;
    }

    private void executeSelected() {
        List<Action> actions = getFilteredActions();
        if (selectedIndex < 0 || selectedIndex >= actions.size()) return;
        Action action = actions.get(selectedIndex);
        if (action.module != null) {
            boolean enable = !action.module.isEnabled();
            action.module.setEnabled(enable);
            status = (enable ? "Enabled " : "Disabled ") + action.module.getName() + ".";
            availableActions = createActions();
            clampSelection();
            return;
        }
        if ("home".equals(action.id)) {
            mc.displayGuiScreen(new AlphaClientHomeMenu());
            return;
        }
        if (action.command != null) {
            if (mc.thePlayer == null) {
                status = "Join a world before using this action.";
                return;
            }
            mc.thePlayer.sendChatMessage(action.command);
            status = "Opened " + action.name + ".";
        }
    }

    private List<Action> createActions() {
        List<Action> all = new ArrayList<Action>();
        all.add(new Action("home", "Open AlphaClient home menu",
                "Open the AlphaClient overlay and HUD controls.", null, null));
        all.add(new Action("general", "Browse General modules",
                "Open the public module browser.", "/general", null));
        all.add(new Action("quality", "Open Quality settings",
                "Open the Quality module settings.", "/quality", null));
        all.add(new Action("hud-editor", "Open HUD editor",
                "Move and configure supported HUD elements.", "/hudeditor", null));
        all.add(new Action("help", "Open AlphaClient help",
                "Browse the available commands and help entries.", "/alphahelp open", null));
        all.add(new Action("waypoints", "Open Waypoints",
                "Manage saved waypoint markers.", "/waypoint", null));
        all.add(new Action("waypoint-visuals", "Open Waypoint display settings",
                "Change waypoint markers, beams, labels, and sorting.", "/waypoint visuals", null));
        all.add(new Action("staff-operations", "Open Staff Operations",
                "Open the staff workspace if your account has access.", "/staffoperations", null));

        Set<String> seenModuleIds = new HashSet<String>();
        for (AlphaModule module : AlphaModuleRegistry.getAll()) {
            if (module == null || !module.isImplemented() || !module.isUnlocked()
                    || !seenModuleIds.add(module.getId())) continue;
            String state = module.isEnabled() ? "on" : "off";
            all.add(new Action("module:" + module.getId(),
                    "Toggle " + module.getName(),
                    module.getDescription() + "  ·  Currently " + state + ".", null, module));
        }

        Collections.sort(all, new Comparator<Action>() {
            @Override
            public int compare(Action a, Action b) {
                return a.name.compareToIgnoreCase(b.name);
            }
        });
        return Collections.unmodifiableList(all);
    }

    private List<Action> getFilteredActions() {
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) return availableActions;
        List<Action> filtered = new ArrayList<Action>();
        for (Action action : availableActions) {
            String searchable = action.name + " " + action.description + " "
                    + (action.command == null ? "" : action.command) + " "
                    + (action.module == null ? "" : action.module.getId());
            if (searchable.toLowerCase(Locale.ROOT).contains(query)) filtered.add(action);
        }
        return filtered;
    }

    private String trim(String value, int maximumWidth) {
        if (fontRendererObj.getStringWidth(value) <= maximumWidth) return value;
        return fontRendererObj.trimStringToWidth(value, Math.max(0, maximumWidth - 8)) + "...";
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static final class Action {
        final String id;
        final String name;
        final String description;
        final String command;
        final AlphaModule module;

        Action(String id, String name, String description, String command, AlphaModule module) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.command = command;
            this.module = module;
        }

        String getTitle() {
            return name;
        }
    }
}
