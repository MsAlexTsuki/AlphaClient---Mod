package com.alpharip.staffmod.hud;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Properties;

/**
 * Rendering and persisted settings for the new public Armor HUD Info and Potion HUD Info modules.
 * HUD backgrounds are intentionally never drawn.
 */
public final class InfoHudModules {
    public static final int MODE_ICONS = 0;
    public static final int MODE_ALL = 1;
    public static final int MODE_BUFFS = 2;
    public static final int MODE_DEBUFFS = 3;
    private static final int DEFAULT_COLOR = 0xFFFFFFFF;
    private static int armorX = 8, armorY = 80, armorColor = DEFAULT_COLOR, armorLayout;
    private static int potionX = 8, potionY = 120, potionColor = DEFAULT_COLOR, potionMode = MODE_ICONS;
    private static boolean loaded;
    private static final Minecraft MC = Minecraft.getMinecraft();

    private InfoHudModules() { }

    public static void load() {
        if (loaded) return;
        loaded = true;
        Properties p = new Properties();
        FileInputStream in = null;
        try {
            File file = getFile();
            if (file.isFile()) {
                in = new FileInputStream(file);
                p.load(in);
            }
            armorX = integer(p, "armorX", armorX);
            armorY = integer(p, "armorY", armorY);
            armorColor = integer(p, "armorColor", armorColor);
            armorLayout = clamp(integer(p, "armorLayout", 0), 0, 1);
            potionX = integer(p, "potionX", potionX);
            potionY = integer(p, "potionY", potionY);
            potionColor = integer(p, "potionColor", potionColor);
            potionMode = clamp(integer(p, "potionMode", MODE_ICONS), MODE_ICONS, MODE_DEBUFFS);
        } catch (Exception e) {
            System.err.println("[AlphaClient] Could not load Armor/Potion HUD settings: " + e.getMessage());
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) { }
        }
    }

    private static File getFile() {
        File dir = new File(Minecraft.getMinecraft().mcDataDir, "config");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "alphaclient_info_huds.properties");
    }

    private static int integer(Properties p, String key, int fallback) {
        try { return Integer.parseInt(p.getProperty(key, String.valueOf(fallback))); }
        catch (Exception ignored) { return fallback; }
    }

    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }

    private static void save() {
        load();
        Properties p = new Properties();
        p.setProperty("armorX", String.valueOf(armorX));
        p.setProperty("armorY", String.valueOf(armorY));
        p.setProperty("armorColor", String.valueOf(armorColor));
        p.setProperty("armorLayout", String.valueOf(armorLayout));
        p.setProperty("potionX", String.valueOf(potionX));
        p.setProperty("potionY", String.valueOf(potionY));
        p.setProperty("potionColor", String.valueOf(potionColor));
        p.setProperty("potionMode", String.valueOf(potionMode));
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(getFile());
            p.store(out, "AlphaClient Armor HUD Info and Potion HUD Info");
        } catch (Exception e) {
            System.err.println("[AlphaClient] Could not save Armor/Potion HUD settings: " + e.getMessage());
        } finally {
            if (out != null) try { out.close(); } catch (Exception ignored) { }
        }
    }

    public static int getArmorX() { load(); return armorX; }
    public static int getArmorY() { load(); return armorY; }
    public static int getArmorColor() { load(); return armorColor; }
    public static int getArmorLayout() { load(); return armorLayout; }
    public static void setArmorPosition(int x, int y) { load(); armorX = Math.max(0, x); armorY = Math.max(0, y); save(); }
    public static void cycleArmorLayout() { load(); armorLayout = (armorLayout + 1) % 2; save(); }
    public static void cycleArmorColor() { load(); armorColor = nextColor(armorColor); save(); }
    public static String getArmorLayoutName() { return getArmorLayout() == 0 ? "Horizontal" : "Vertical"; }

    public static int getPotionX() { load(); return potionX; }
    public static int getPotionY() { load(); return potionY; }
    public static int getPotionColor() { load(); return potionColor; }
    public static int getPotionMode() { load(); return potionMode; }
    public static void setPotionPosition(int x, int y) { load(); potionX = Math.max(0, x); potionY = Math.max(0, y); save(); }
    public static void cyclePotionMode() { load(); potionMode = (potionMode + 1) % 4; save(); }
    public static void cyclePotionColor() { load(); potionColor = nextColor(potionColor); save(); }
    public static String getPotionModeName() {
        switch (getPotionMode()) {
            case MODE_ALL: return "All effects";
            case MODE_BUFFS: return "Buffs only";
            case MODE_DEBUFFS: return "Debuffs only";
            default: return "Icons only";
        }
    }
    private static int nextColor(int color) {
        int[] colors = {0xFFFFFFFF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55, 0xFF55FFFF, 0xFF5555FF, 0xFFAA55FF, 0xFFFF55AA};
        for (int i = 0; i < colors.length; i++) if (colors[i] == color) return colors[(i + 1) % colors.length];
        return colors[0];
    }

    public static boolean armorEnabled() { AlphaModule m = AlphaModuleRegistry.find("armor-hud-info"); return m != null && m.isEnabled() && m.isUnlocked(); }
    public static boolean potionEnabled() { AlphaModule m = AlphaModuleRegistry.find("potion-hud-info"); return m != null && m.isEnabled() && m.isUnlocked(); }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        renderEnabled(false);
    }

    public static void renderEditorPreviews() { renderEnabled(true); }

    private static void renderEnabled(boolean preview) {
        load();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.fontRendererObj == null || mc.gameSettings.showDebugInfo) return;
        if (preview || armorEnabled()) renderArmor(mc);
        if (preview || potionEnabled()) renderPotions(mc);
    }

    private static void renderArmor(Minecraft mc) {
        ItemStack held = mc.thePlayer.getHeldItem();
        ItemStack[] armor = mc.thePlayer.inventory.armorInventory;
        ItemStack[] items = new ItemStack[] { held, armor[3], armor[2], armor[1], armor[0] };
        String[] labels = new String[] { "Hand", "Helmet", "Chest", "Legs", "Boots" };
        int x = armorX, y = armorY;
        for (int i = 0; i < items.length; i++) {
            ItemStack stack = items[i];
            String label = labels[i] + ": " + (stack == null ? "Empty" : stack.getDisplayName() + " " + durability(stack));
            if (armorLayout == 0) {
                int itemX = x + i * 88;
                drawItem(mc, stack, itemX, y);
                mc.fontRendererObj.drawStringWithShadow(fit(mc, label, 82), itemX, y + 18, armorColor);
            } else {
                drawItem(mc, stack, x, y + i * 22);
                mc.fontRendererObj.drawStringWithShadow(fit(mc, label, 180), x + 20, y + i * 22 + 5, armorColor);
            }
        }
    }

    private static String durability(ItemStack stack) {
        if (stack == null || !stack.isItemStackDamageable()) return "";
        int max = stack.getMaxDamage();
        int remaining = Math.max(0, max - stack.getItemDamage());
        return "[" + remaining + "/" + max + "]";
    }

    private static void drawItem(Minecraft mc, ItemStack stack, int x, int y) {
        if (stack == null) return;
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, x, y);
        RenderHelper.disableStandardItemLighting();
    }

    private static String fit(Minecraft mc, String value, int maxWidth) {
        if (mc.fontRendererObj.getStringWidth(value) <= maxWidth) return value;
        return mc.fontRendererObj.trimStringToWidth(value, maxWidth - mc.fontRendererObj.getStringWidth("…")) + "…";
    }

    private static void renderPotions(Minecraft mc) {
        Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
        int row = 0;
        for (PotionEffect effect : effects) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            if (potion == null) continue;
            boolean bad = potion.isBadEffect();
            if (potionMode == MODE_BUFFS && bad) continue;
            if (potionMode == MODE_DEBUFFS && !bad) continue;
            int x = potionX, y = potionY + row * 22;
            if (potionMode == MODE_ICONS) {
                drawPotionIcon(mc, potion, x, y);
                row++;
                continue;
            }
            drawPotionIcon(mc, potion, x, y);
            String level = effect.getAmplifier() > 0 ? " " + roman(effect.getAmplifier() + 1) : "";
            String line = potion.getName() + level + "  " + Potion.getDurationString(effect);
            mc.fontRendererObj.drawStringWithShadow(fit(mc, line, 190), x + 20, y + 5, potionColor);
            row++;
        }
    }

    private static void drawPotionIcon(Minecraft mc, Potion potion, int x, int y) {
        if (!potion.hasStatusIcon()) return;
        mc.getTextureManager().bindTexture(new net.minecraft.util.ResourceLocation("textures/gui/container/inventory.png"));
        int icon = potion.getStatusIconIndex();
        Gui.drawTexturedModalRect(x, y, 0 + (icon % 8) * 18, 198 + (icon / 8) * 18, 18, 18);
    }

    private static String roman(int value) {
        switch (value) { case 2: return "II"; case 3: return "III"; case 4: return "IV"; case 5: return "V"; default: return String.valueOf(value); }
    }
}
