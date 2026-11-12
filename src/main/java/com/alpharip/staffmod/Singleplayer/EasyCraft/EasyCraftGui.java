package com.alpharip.staffmod.Singleplayer.EasyCraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Compact modern-recipe-book-inspired screen, intended to open only from GuiCrafting. */
public final class EasyCraftGui extends GuiScreen {
    private static final int TAB_ALL = 1;
    private static final int TAB_BUILDING = 2;
    private static final int TAB_TOOLS = 3;
    private static final int TAB_COMBAT = 4;
    private static final int TAB_FOOD = 5;
    private static final int TAB_REDSTONE = 6;
    private static final int TAB_FAVORITES = 7;
    private static final int CRAFT = 8;
    private static final int CLOSE = 9;
    private static final int FAVORITE = 10;

    private final GuiScreen parent;
    private final EasyCraftCraftingAdapter crafting;
    private final EasyCraftRecipeCatalog catalog;
    private final EasyCraftFavorites favorites;
    private final List<EasyCraftRecipe> visibleRecipes = new ArrayList<EasyCraftRecipe>();

    private EasyCraftCategory category = EasyCraftCategory.ALL;
    private boolean favoritesOnly;
    private EasyCraftRecipe selected;
    private GuiTextField search;
    private int scrollOffset;

    public EasyCraftGui(GuiScreen parent, EasyCraftCraftingAdapter crafting,
                        EasyCraftRecipeCatalog catalog, EasyCraftFavorites favorites) {
        if (parent == null || crafting == null || catalog == null || favorites == null)
            throw new IllegalArgumentException("Easy Craft requires its crafting-table context");
        this.parent = parent;
        this.crafting = crafting;
        this.catalog = catalog;
        this.favorites = favorites;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int left = panelLeft();
        int top = panelTop();
        buttonList.add(new GuiButton(TAB_ALL, left + 10, top + 43, 42, 18, "All"));
        buttonList.add(new GuiButton(TAB_BUILDING, left + 54, top + 43, 55, 18, "Build"));
        buttonList.add(new GuiButton(TAB_TOOLS, left + 111, top + 43, 43, 18, "Tools"));
        buttonList.add(new GuiButton(TAB_COMBAT, left + 156, top + 43, 48, 18, "Combat"));
        buttonList.add(new GuiButton(TAB_FOOD, left + 206, top + 43, 39, 18, "Food"));
        buttonList.add(new GuiButton(TAB_REDSTONE, left + 247, top + 43, 53, 18, "Redstone"));
        buttonList.add(new GuiButton(TAB_FAVORITES, left + 302, top + 43, 62, 18, "Favorites"));
        buttonList.add(new GuiButton(CRAFT, left + 17, top + 207, 72, 20, "Craft"));
        buttonList.add(new GuiButton(CLOSE, left + 95, top + 207, 50, 20, "Back"));
        buttonList.add(new GuiButton(FAVORITE, left + 350, top + 68, 22, 18, "*"));
        search = new GuiTextField(0, fontRendererObj, left + 12, top + 17, 180, 18);
        search.setMaxStringLength(64);
        refreshVisibleRecipes();
    }

    private int panelLeft() { return width / 2 - 195; }
    private int panelTop() { return height / 2 - 125; }

    private void refreshVisibleRecipes() {
        visibleRecipes.clear();
        visibleRecipes.addAll(catalog.find(search == null ? "" : search.getText(),
                category, favoritesOnly, favorites));
        if (scrollOffset > Math.max(0, visibleRecipes.size() - 18))
            scrollOffset = Math.max(0, visibleRecipes.size() - 18);
        if (selected != null && !containsRecipe(selected)) selected = null;
        if (selected == null && !visibleRecipes.isEmpty()) selected = visibleRecipes.get(0);
    }

    private boolean containsRecipe(EasyCraftRecipe recipe) {
        for (EasyCraftRecipe candidate : visibleRecipes)
            if (candidate.getKey().equals(recipe.getKey())) return true;
        return false;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == null) return;
        switch (button.id) {
            case TAB_ALL: category = EasyCraftCategory.ALL; favoritesOnly = false; break;
            case TAB_BUILDING: category = EasyCraftCategory.BUILDING; favoritesOnly = false; break;
            case TAB_TOOLS: category = EasyCraftCategory.TOOLS; favoritesOnly = false; break;
            case TAB_COMBAT: category = EasyCraftCategory.COMBAT; favoritesOnly = false; break;
            case TAB_FOOD: category = EasyCraftCategory.FOOD; favoritesOnly = false; break;
            case TAB_REDSTONE: category = EasyCraftCategory.REDSTONE; favoritesOnly = false; break;
            case TAB_FAVORITES: favoritesOnly = true; category = EasyCraftCategory.ALL; break;
            case CRAFT:
                if (selected != null && crafting.isCraftingTableOpen()
                        && crafting.getAvailableCraftCount(selected) > 0) {
                    crafting.craft(selected, 1);
                }
                break;
            case FAVORITE:
                if (selected != null) favorites.toggle(selected.getKey());
                break;
            case CLOSE:
                Minecraft.getMinecraft().displayGuiScreen(parent);
                return;
            default: break;
        }
        refreshVisibleRecipes();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int left = panelLeft();
        int top = panelTop();
        drawRect(left, top, left + 390, top + 252, 0xF012151B);
        drawRect(left, top, left + 390, top + 2, 0xFFFFD45A);
        drawString(fontRendererObj, "Easy Craft", left + 12, top + 7, 0xFFFFD45A);
        search.drawTextBox();

        drawRect(left + 9, top + 66, left + 215, top + 232, 0xFF202630);
        drawRect(left + 222, top + 66, left + 381, top + 232, 0xFF202630);
        drawString(fontRendererObj, favoritesOnly ? "Favorite recipes" : "Recipes",
                left + 17, top + 70, 0xFFFFFFFF);
        drawRecipeGrid(left, top, mouseX, mouseY);
        drawSelectedRecipe(left, top);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawRecipeGrid(int left, int top, int mouseX, int mouseY) {
        int startX = left + 17;
        int startY = top + 87;
        for (int slot = 0; slot < 18; slot++) {
            int index = scrollOffset + slot;
            int x = startX + (slot % 6) * 32;
            int y = startY + (slot / 6) * 34;
            drawRect(x, y, x + 28, y + 28, 0xFF343C49);
            if (index >= visibleRecipes.size()) continue;
            EasyCraftRecipe recipe = visibleRecipes.get(index);
            if (selected != null && selected.getKey().equals(recipe.getKey()))
                drawRect(x, y, x + 28, y + 28, 0xFF78622C);
            if (favorites.contains(recipe.getKey()))
                drawString(fontRendererObj, "*", x + 19, y + 1, 0xFFFFD45A);
            renderStack(recipe.getOutput(), x + 6, y + 6);
        }
        drawString(fontRendererObj, (visibleRecipes.isEmpty() ? "0" : Integer.toString(scrollOffset + 1))
                + " / " + visibleRecipes.size(), left + 17, top + 222, 0xFFB7C0CC);
    }

    private void drawSelectedRecipe(int left, int top) {
        int x = left + 232;
        drawString(fontRendererObj, "Selected recipe", x, top + 72, 0xFFFFD45A);
        if (selected == null) {
            drawString(fontRendererObj, "No recipes found", x, top + 98, 0xFFB7C0CC);
            return;
        }

        renderStack(selected.getOutput(), x + 2, top + 91);
        drawString(fontRendererObj, selected.getDisplayName(), x + 28, top + 96, 0xFFFFFFFF);
        drawString(fontRendererObj, "Ingredients", x, top + 124, 0xFFFFD45A);

        List<ItemStack> ingredients = selected.getIngredients();
        int shown = Math.min(ingredients.size(), 9);
        for (int i = 0; i < shown; i++) {
            int slotX = x + (i % 3) * 28;
            int slotY = top + 140 + (i / 3) * 24;
            drawRect(slotX, slotY, slotX + 24, slotY + 22, 0xFF343C49);
            renderStack(ingredients.get(i), slotX + 4, slotY + 3);
        }

        int available = crafting.isCraftingTableOpen() ? Math.max(0, crafting.getAvailableCraftCount(selected)) : 0;
        String count = available > 0 ? "Can craft: " + available : "Missing ingredients";
        drawString(fontRendererObj, count, x, top + 214, available > 0 ? 0xFF8EDB8E : 0xFFFF8A8A);

        drawString(fontRendererObj, favorites.contains(selected.getKey()) ? "Saved to Favorites" : "Not favorited",
                x, top + 229, 0xFFFFD45A);
    }

    private void renderStack(ItemStack stack, int x, int y) {
        if (stack == null) return;
        RenderHelper.enableGUIStandardItemLighting();
        itemRender.renderItemAndEffectIntoGUI(stack, x, y);
        itemRender.renderItemOverlays(fontRendererObj, stack, x, y);
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (search != null) search.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) return;

        int left = panelLeft();
        int top = panelTop();
        if (mouseX >= left + 17 && mouseX < left + 209
                && mouseY >= top + 87 && mouseY < top + 223) {
            int column = (mouseX - (left + 17)) / 32;
            int row = (mouseY - (top + 87)) / 34;
            if (column >= 0 && column < 6 && row >= 0 && row < 3) {
                int index = scrollOffset + row * 6 + column;
                if (index >= 0 && index < visibleRecipes.size()) selected = visibleRecipes.get(index);
            }
        }

    }

    @Override
    protected void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset = Math.max(0, Math.min(Math.max(0, visibleRecipes.size() - 24),
                    scrollOffset + (wheel < 0 ? 6 : -6)));
            refreshVisibleRecipes();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            Minecraft.getMinecraft().displayGuiScreen(parent);
            return;
        }
        if (search != null && search.textboxKeyTyped(typedChar, keyCode)) {
            scrollOffset = 0;
            refreshVisibleRecipes();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void updateScreen() {
        if (search != null) search.updateCursorCounter();
        if (!Minecraft.getMinecraft().isSingleplayer() || !crafting.isCraftingTableOpen())
            Minecraft.getMinecraft().displayGuiScreen(parent);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
