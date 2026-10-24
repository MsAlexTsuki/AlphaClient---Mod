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
 * Rendering and persisted settings for the public Armor HUD and Potion HUD modules.
 * HUD backgrounds are intentionally never drawn.
 */
public final class InfoHudModules {
    public static final int MODE_ICONS = 0;
    public static final int MODE_ALL = 1;
    public static final int MODE_BUFFS = 2;
    public static final int MODE_DEBUFFS = 3;
    private static final int DEFAULT_COLOR = 0xFFFFFFFF;
    private static int armorX = 8, armorY = 80, armorColor = DEFAULT_COLOR, armorLayout;
    private static int potionX = 8, potionY = 120, potionColor = DEFAULT_COLOR, potionMode = MODE_ICONS;\n    private static boolean potionRainbow;
    private static boolean armorShowNames = true, armorShowDurability = true;
    private static boolean loaded;
    private static final Minecraft MC = Minecraft.getMinecraft();

    public InfoHudModules() { }

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
            armorShowNames = Boolean.parseBoolean(p.getProperty("armorShowNames", "true"));
            armorShowDurability = Boolean.parseBoolean(p.getProperty("armorShowDurability", "true"));
            potionX = integer(p, "potionX", potionX);
            potionY = integer(p, "potionY", potionY);
            potionColor = integer(p, "potionColor", potionColor);\n            potionRainbow = Boolean.parseBoolean(p.getProperty("potionRainbow", "false"));
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
        p.setProperty("armorShowNames", String.valueOf(armorShowNames));
        p.setProperty("armorShowDurability", String.valueOf(armorShowDurability));
        p.setProperty("potionX", String.valueOf(potionX));
        p.setProperty("potionY", String.valueOf(potionY));
        p.setProperty("potionColor", String.valueOf(potionColor));\n        p.setProperty("potionRainbow", String.valueOf(potionRainbow));
        p.setProperty("potionMode", String.valueOf(potionMode));
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(getFile());
            p.store(out, "AlphaClient Armor HUD and Potion HUD");
        } catch (Exception e) {
            System.err.println("[AlphaClient] Could not save Armor/Potion HUD settings: " + e.getMessage());
        } finally {
            if (out != null) try { out.close(); } catch (Exception ignored) { }
        }
    }

    public static int getArmorX() { load(); return armorX; }
    public static int getArmorY() { load(); return armorY; }
    public static int getArmorColor() { load(); return armorColor; }
    public static int getArmorEditorWidth() { load(); return armorLayout == 0 ? 210 : 120; }
    public static int getArmorEditorHeight() { load(); return armorLayout == 0 ? 18 : 70; }
    public static int getArmorLayout() { load(); return armorLayout; }
    public static boolean getArmorShowNames() { load(); return armorShowNames; }
    public static boolean getArmorShowDurability() { load(); return armorShowDurability; }
    public static void toggleArmorShowNames() { load(); armorShowNames = !armorShowNames; save(); }
    public static void toggleArmorShowDurability() { load(); armorShowDurability = !armorShowDurability; save(); }
    public static void setArmorPosition(int x, int y) { load(); armorX = Math.max(0, x); armorY = Math.max(0, y); save(); }
    public static void cycleArmorLayout() { load(); armorLayout = (armorLayout + 1) % 2; save(); }
    public static void cycleArmorColor() { load(); armorColor = nextColor(armorColor); save(); }
    public static String getArmorLayoutName() { return getArmorLayout() == 0 ? "Horizontal" : "Vertical"; }

    public static int getPotionX() { load(); return potionX; }
    public static int getPotionY() { load(); return potionY; }
    public static int getPotionColor() { load(); return potionRainbow ? rainbowColor() : potionColor; }\n    public static boolean isPotionRainbow() { load(); return potionRainbow; }
    public static int getPotionEditorWidth() { load(); return potionMode == MODE_ICONS ? 18 : 210; }
    public static int getPotionEditorHeight() {
        load();
        Minecraft mc = Minecraft.getMinecraft();
        int rows = mc == null || mc.thePlayer == null ? 3 : mc.thePlayer.getActivePotionEffects().size();
        return Math.max(66, Math.min(180, rows * 22));
    }
    public static int getPotionMode() { load(); return potionMode; }
    public static void setPotionPosition(int x, int y) { load(); potionX = Math.max(0, x); potionY = Math.max(0, y); save(); }
    public static void cyclePotionMode() { load(); potionMode = (potionMode + 1) % 4; save(); }
    public static void cyclePotionColor() {\n        load();\n        int[] colors = {0xFFFFFFFF, 0xFFB8BDC8, 0xFFFFD866, 0xFFFFAA00, 0xFFFF5555, 0xFF55FF55, 0xFF55FFFF, 0xFF5555FF, 0xFFAA55FF, 0xFFFF55AA};\n        if (potionRainbow) { potionRainbow = false; potionColor = colors[0]; }\n        else {\n            int next = -1;\n            for (int i = 0; i < colors.length; i++) if (colors[i] == potionColor) { next = i + 1; break; }\n            if (next >= colors.length) potionRainbow = true;\n            else if (next >= 0) potionColor = colors[next];\n            else potionColor = colors[0];\n        }\n        save();\n    }\n    private static int rainbowColor() {\n        float hue = (System.currentTimeMillis() % 6000L) / 6000.0F;\n        return 0xFF000000 | (java.awt.Color.HSBtoRGB(hue, 1.0F, 1.0F) & 0x00FFFFFF);\n    }
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

    private static int nextPotionColor(int color) {
        int[] colors = {0xFFFFFFFF, 0xFFB8BDC8, 0xFFFFD866, 0xFFFFAA00, 0xFFFF5555};
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
        if (preview || potionEnabled()) renderPotions(mc, preview);
    }

    private static void renderArmor(Minecraft mc) {
        // Compact layout: held item first, then equipped armor from head to feet.
        ItemStack[] armor = mc.thePlayer.inventory.armorInventory;
        ItemStack[] items = new ItemStack[] {
                mc.thePlayer.getHeldItem(), armor[3], armor[2], armor[1], armor[0]
        };
        int x = armorX, y = armorY;
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0.0F);
        GL11.glScalef(0.65F, 0.65F, 1.0F);
        for (int i = 0; i < items.length; i++) {
            int itemX = armorLayout == 0 ? i * 92 : 0;
            int itemY = armorLayout == 0 ? 0 : i * 20;
            ItemStack stack = items[i];
            drawItem(mc, stack, itemX, itemY);
            String label = stack == null ? (armorShowNames ? "Empty" : "") : ((armorShowNames ? stack.getDisplayName() : "") + (armorShowDurability ? " " + durability(stack) : "" )).trim();
            mc.fontRendererObj.drawStringWithShadow(fit(mc, label, 180), itemX + 20, itemY + 5, armorColor);
        }
        GL11.glPopMatrix();
    }

    private static void drawItem(Minecraft mc, ItemStack stack, int x, int y) {
        if (stack == null) return;
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, x, y);
        RenderHelper.disableStandardItemLighting();
    }

    private static String durability(ItemStack stack) {
        if (stack == null || !stack.isItemStackDamageable()) return "";
        return "[" + (stack.getMaxDamage() - stack.getItemDamage()) + "/" + stack.getMaxDamage() + "]";
    }

    private static String fit(Minecraft mc, String value, int maxWidth) {
        if (mc.fontRendererObj.getStringWidth(value) <= maxWidth) return value;
        return mc.fontRendererObj.trimStringToWidth(value, maxWidth - mc.fontRendererObj.getStringWidth("…")) + "…";
    }

    private static void renderPotions(Minecraft mc, boolean preview) {
        Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
        if (preview && effects.isEmpty()) {
            renderPotionPreview(mc);
            return;
        }
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
            String line = net.minecraft.util.StatCollector.translateToLocal(potion.getName()) + level + "  " + Potion.getDurationString(effect);
            mc.fontRendererObj.drawStringWithShadow(fit(mc, line, 190), x + 20, y + 5, potionColor);
            row++;
        }
    }

    private static void renderPotionPreview(Minecraft mc) {
        Potion[] samples = new Potion[] { Potion.moveSpeed, Potion.regeneration, Potion.poison };
        String[] names = new String[] { "Speed", "Regeneration", "Poison" };
        int row = 0;
        for (int i = 0; i < samples.length; i++) {
            Potion potion = samples[i];
            if (potion == null) continue;
            boolean bad = potion.isBadEffect();
            if (potionMode == MODE_BUFFS && bad) continue;
            if (potionMode == MODE_DEBUFFS && !bad) continue;
            int x = potionX, y = potionY + row * 22;
            drawPotionIcon(mc, potion, x, y);
            if (potionMode != MODE_ICONS) {
                String line = names[i] + "  1:23";
                mc.fontRendererObj.drawStringWithShadow(fit(mc, line, 190), x + 20, y + 5, potionColor);
            }
            row++;
        }
    }

    private static void drawPotionIcon(Minecraft mc, Potion potion, int x, int y) {
        if (!potion.hasStatusIcon()) return;
        mc.getTextureManager().bindTexture(new net.minecraft.util.ResourceLocation("textures/gui/container/inventory.png"));
        int icon = potion.getStatusIconIndex();
        Gui.drawModalRectWithCustomSizedTexture(x, y, (icon % 8) * 18, 198 + (icon / 8) * 18, 18, 18, 256.0F, 256.0F);
    }

    private static String roman(int value) {
        switch (value) { case 2: return "II"; case 3: return "III"; case 4: return "IV"; case 5: return "V"; default: return String.valueOf(value); }
    }
}
