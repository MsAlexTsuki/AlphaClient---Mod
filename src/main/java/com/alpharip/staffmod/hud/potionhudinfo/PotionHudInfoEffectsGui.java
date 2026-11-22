// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.hud.potionhudinfo;

import com.alpharip.staffmod.gui.AlphaGuiScreen;
import com.alpharip.staffmod.gui.AlphaButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.potion.Potion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Browse, enable, and configure individual potion effects. */
public final class PotionHudInfoEffectsGui extends AlphaGuiScreen {
    private static final int FIRST_EFFECT=100, PREVIOUS=10, NEXT=11, TOGGLE=12, EDIT=13, IMPORT=14, BACK=15;
    private static final int PAGE_SIZE=6;
    private final GuiScreen parent;
    private final List<Integer> ids = new ArrayList<Integer>();
    private final PotionHudInfoModule settings = PotionHudInfoModule.getInstance();
    private int index;
    private int page;

    public PotionHudInfoEffectsGui(GuiScreen parent) {
        this.parent = parent;
        Potion[] potions = Potion.potionTypes;
        for (int i=0; i<potions.length; i++) if (potions[i] != null) ids.add(Integer.valueOf(i));
    }

    private int currentId() {
        return ids.isEmpty() ? -1 : ids.get(Math.max(0, Math.min(index, ids.size()-1))).intValue();
    }
    private PotionHudInfoEffectSetting current() { return currentId() < 0 ? null : settings.getEffect(currentId()); }
    private int pageCount() { return Math.max(1, (ids.size() + PAGE_SIZE - 1) / PAGE_SIZE); }

    @Override public void initGui() {
        buttonList.clear();
        page = Math.max(0, Math.min(page, pageCount()-1));
        int start = page * PAGE_SIZE;
        int left = width / 2 - 211;
        int top = height / 2 - 105;
        for (int slot=0; slot<PAGE_SIZE && start+slot<ids.size(); slot++) {
            final int itemIndex = start + slot;
            int column = slot % 2;
            int row = slot / 2;
            int id = ids.get(itemIndex).intValue();
            PotionHudInfoEffectSetting value = settings.getEffect(id);
            String label = PotionHudInfoModule.getPotionName(id);
            if (label.length() > 15) label = label.substring(0, 14) + "…";
            label += value != null && value.isEnabled() ? "  •" : "  −";
            buttonList.add(new AlphaButton(FIRST_EFFECT + slot, left + column*112, top + 43 + row*31,
                    106, 25, label, false, itemIndex == index));
        }
        buttonList.add(new AlphaButton(PREVIOUS, left, top + 145, 48, 20, "PREV", false, page > 0));
        buttonList.add(new AlphaButton(NEXT, left + 164, top + 145, 48, 20, "NEXT", false, page < pageCount()-1));
        PotionHudInfoEffectSetting selected = current();
        buttonList.add(new AlphaButton(TOGGLE, left + 235, top + 55, 174, 23,
                selected == null ? "Unavailable" : (selected.isEnabled() ? "Disable effect" : "Enable effect"),
                false, selected != null && selected.isEnabled()));
        buttonList.add(new AlphaButton(EDIT, left + 235, top + 85, 174, 23, "EDIT APPEARANCE", true));
        buttonList.add(new AlphaButton(IMPORT, left + 235, top + 115, 174, 23, "COPY FROM ANOTHER EFFECT"));
        buttonList.add(new AlphaButton(BACK, width/2 - 42, top + 181, 84, 20, "BACK"));
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        if (button.id == BACK) { mc.displayGuiScreen(parent); return; }
        if (button.id == PREVIOUS) { page = Math.max(0, page-1); initGui(); return; }
        if (button.id == NEXT) { page = Math.min(pageCount()-1, page+1); initGui(); return; }
        if (button.id >= FIRST_EFFECT && button.id < FIRST_EFFECT + PAGE_SIZE) {
            int selected = page*PAGE_SIZE + button.id-FIRST_EFFECT;
            if (selected >= 0 && selected < ids.size()) index = selected;
            initGui();
            return;
        }
        if (currentId() < 0) return;
        if (button.id == TOGGLE && current() != null) {
            current().setEnabled(!current().isEnabled());
            initGui();
        } else if (button.id == EDIT) {
            mc.displayGuiScreen(new PotionHudInfoEffectGui(this, currentId()));
        } else if (button.id == IMPORT) {
            mc.displayGuiScreen(new PotionHudInfoImportGui(this, currentId()));
        }
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x50000000);
        int left = width/2 - 219, top = height/2 - 117, right = width/2 + 219, bottom = height/2 + 111;
        drawRect(left, top, right, bottom, 0xE60A0C12);
        drawRect(left, top, right, top+1, 0xFFFFD866);
        drawRect(left+224, top+35, left+225, bottom-12, 0x554B515D);
        drawString(fontRendererObj, "POTION EFFECTS", left+12, top+12, 0xFFF3F4F6);
        drawString(fontRendererObj, "Choose an effect to configure", left+12, top+25, 0xFFB8BDC8);
        drawString(fontRendererObj, "SELECT EFFECT", left+12, top+37, 0xFFFFD866);
        drawString(fontRendererObj, "SELECTED", left+237, top+37, 0xFFFFD866);
        if (currentId() >= 0) {
            PotionHudInfoEffectSetting value = current();
            drawCenteredString(fontRendererObj, PotionHudInfoModule.getPotionName(currentId()),
                    left+322, top+51, 0xFFF3F4F6);
            drawCenteredString(fontRendererObj, value != null && value.isEnabled() ? "Visible in HUD" : "Hidden from HUD",
                    left+322, top+143, value != null && value.isEnabled() ? 0xFF9CE2AC : 0xFFB8BDC8);
        } else {
            drawCenteredString(fontRendererObj, "No effects available", width/2, height/2, 0xFFB8BDC8);
        }
        drawString(fontRendererObj, "Page " + (page+1) + " / " + pageCount(), left+65, top+151, 0xFFB8BDC8);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public boolean doesGuiPauseGame() { return false; }
}
