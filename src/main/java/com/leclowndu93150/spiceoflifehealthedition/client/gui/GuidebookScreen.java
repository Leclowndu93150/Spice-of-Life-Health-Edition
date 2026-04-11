package com.leclowndu93150.spiceoflifehealthedition.client.gui;

import com.leclowndu93150.spiceoflifehealthedition.api.NutritionalProfile;
import com.leclowndu93150.spiceoflifehealthedition.client.NutritionClientCache;
import com.leclowndu93150.spiceoflifehealthedition.client.gui.widget.UiComponents;
import com.leclowndu93150.spiceoflifehealthedition.client.gui.widget.UiTheme;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietAttachment;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietHistory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class GuidebookScreen extends Screen {

    private static final String P = "gui.spiceoflifehealthedition.";
    private static final String TT = "tooltip.spiceoflifehealthedition.";

    private static final int PANEL_W = 320;
    private static final int PANEL_H = 220;
    private static final int HEADER_H = 30;
    private static final int TAB_H = 22;
    private static final int PADDING = 12;

    private int px, py;
    private int contentX, contentY, contentW, contentH;

    private enum Tab {
        HOME("home", "\u2605"),
        DIET("diet", "\u2630"),
        HEALTH("health", "\u2764"),
        FOODS("foods", "\u25A6"),
        BODY("body", "\u25EF");

        final String key;
        final String icon;
        Tab(String key, String icon) { this.key = key; this.icon = icon; }
    }

    private Tab activeTab = Tab.HOME;

    private int foodScroll = 0;
    private int foodMaxScroll = 0;
    private String searchFilter = "";
    private boolean searchFocused = false;
    private List<FoodEntry> filteredFoods = new ArrayList<>();
    private ResourceLocation selectedFood;

    public GuidebookScreen() {
        super(Component.translatable(P + "title"));
    }

    @Override
    protected void init() {
        px = (width - PANEL_W) / 2;
        py = (height - PANEL_H) / 2;
        contentX = px + PADDING;
        contentY = py + HEADER_H + TAB_H + 4;
        contentW = PANEL_W - PADDING * 2;
        contentH = PANEL_H - HEADER_H - TAB_H - PADDING - 4;
        rebuildFoodList();
    }

    private void rebuildFoodList() {
        filteredFoods.clear();
        Map<ResourceLocation, NutritionalProfile> all = NutritionClientCache.getAll();
        String lower = searchFilter.toLowerCase();
        for (Map.Entry<ResourceLocation, NutritionalProfile> entry : all.entrySet()) {
            Item item = BuiltInRegistries.ITEM.get(entry.getKey());
            if (item == null) continue;
            if (entry.getValue().isEmpty()) continue;
            String name = item.getDescription().getString().toLowerCase();
            if (lower.isEmpty() || name.contains(lower) || entry.getKey().toString().contains(lower)) {
                filteredFoods.add(new FoodEntry(entry.getKey(), item, entry.getValue()));
            }
        }
        filteredFoods.sort(Comparator.comparing(e -> e.item.getDescription().getString()));
        foodMaxScroll = Math.max(0, filteredFoods.size() * 18 - contentH);
        if (foodScroll > foodMaxScroll) foodScroll = foodMaxScroll;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);

        UiComponents.panel(g, px, py, PANEL_W, PANEL_H);
        drawHeader(g);
        drawTabs(g, mx, my);
        UiComponents.innerPanel(g, contentX - 2, contentY - 2, contentW + 4, contentH + 4);

        g.enableScissor(contentX, contentY, contentX + contentW, contentY + contentH);
        switch (activeTab) {
            case HOME -> drawHome(g);
            case DIET -> drawDiet(g);
            case HEALTH -> drawHealth(g);
            case FOODS -> drawFoods(g, mx, my);
            case BODY -> drawBody(g);
        }
        g.disableScissor();
    }

    private void drawHeader(GuiGraphics g) {
        String title = Component.translatable(P + "title").getString();
        g.drawString(font, title, px + PADDING, py + 10, UiTheme.TEXT_PRIMARY, false);

        String subtitle = Component.translatable(P + "subtitle").getString();
        g.drawString(font, subtitle, px + PADDING, py + 20, UiTheme.TEXT_MUTED, false);

        UiComponents.separator(g, px + PADDING, py + HEADER_H - 2, PANEL_W - PADDING * 2);
    }

    private void drawTabs(GuiGraphics g, int mx, int my) {
        Tab[] tabs = Tab.values();
        int tabW = (PANEL_W - PADDING * 2) / tabs.length;
        int tabY = py + HEADER_H;

        for (int i = 0; i < tabs.length; i++) {
            int tx = px + PADDING + i * tabW;
            boolean active = tabs[i] == activeTab;
            boolean hovered = UiComponents.pointInRect(mx, my, tx, tabY, tabW, TAB_H);
            String label = tabs[i].icon + "  " + Component.translatable(P + "tab." + tabs[i].key).getString();
            UiComponents.tab(g, font, tx, tabY, tabW, TAB_H, label, active, hovered);
        }
    }

    private void drawHome(GuiGraphics g) {
        int x = contentX + 4;
        int y = contentY + 6;

        g.drawString(font, Component.translatable(P + "home.welcome"), x, y, UiTheme.TEXT_PRIMARY, false);
        y += 14;

        y = UiComponents.drawWrappedText(g, font, Component.translatable(P + "home.desc"),
                x, y, contentW - 8, UiTheme.TEXT_SECONDARY);
        y += 8;

        g.drawString(font, Component.translatable(P + "home.stats_title"), x, y, UiTheme.TEXT_PRIMARY, false);
        y += 12;

        UiComponents.Grid grid = new UiComponents.Grid(x, y, contentW - 8, 70, 2, 4, 4);
        String[] keys = UiTheme.STAT_KEYS;
        for (int i = 0; i < keys.length; i++) {
            int col = i % 2;
            int row = i / 2;
            int cx = grid.cellX(col);
            int cy = grid.cellY(row);
            g.fill(cx, cy, cx + 3, cy + grid.cellH, UiTheme.STAT_COLORS[i]);
            String name = Component.translatable(TT + keys[i]).getString();
            g.drawString(font, name, cx + 8, cy + (grid.cellH - 8) / 2, UiTheme.TEXT_SECONDARY, false);
        }
    }

    private void drawDiet(GuiGraphics g) {
        DietHistory history = getDietHistory();
        int x = contentX + 4;
        int y = contentY + 6;

        if (history == null || history.getEntryCount() == 0) {
            drawEmptyState(g, Component.translatable(P + "diet.empty").getString());
            return;
        }

        NutritionalProfile avg = history.getAverage();

        g.drawString(font, Component.translatable(P + "diet.avg_title"), x, y, UiTheme.TEXT_PRIMARY, false);
        String meta = Component.translatable(P + "diet.meta", history.getEntryCount(), history.getDiversity()).getString();
        g.drawString(font, meta, x + contentW - 8 - font.width(meta), y, UiTheme.TEXT_MUTED, false);
        y += 12;

        int barW = contentW - 70;
        float[] vals = {avg.sugar(), avg.fat(), avg.salt(), avg.protein(), avg.fiber(), avg.hydration(), avg.vitamins()};
        for (int i = 0; i < vals.length; i++) {
            String name = Component.translatable(TT + UiTheme.STAT_KEYS[i]).getString();
            g.drawString(font, name, x, y + 1, UiTheme.TEXT_SECONDARY, false);
            UiComponents.progressBar(g, x + 56, y + 1, barW - 40, 7, vals[i], 8f, UiTheme.STAT_COLORS[i]);
            String valStr = String.format("%.1f", vals[i]);
            g.drawString(font, valStr, x + contentW - 8 - font.width(valStr), y + 1, UiTheme.TEXT_MUTED, false);
            y += 11;
        }

        y += 4;
        UiComponents.separator(g, x, y, contentW - 8);
        y += 6;

        g.drawString(font, Component.translatable(P + "diet.recent_title"), x, y, UiTheme.TEXT_PRIMARY, false);
        y += 12;

        List<DietHistory.GroupedEntry> groups = history.getGroupedRecent(5);
        for (DietHistory.GroupedEntry group : groups) {
            if (y + 16 > contentY + contentH) break;
            Item item = BuiltInRegistries.ITEM.get(group.foodId);
            if (item == null) continue;
            g.renderItem(new ItemStack(item), x, y);
            String name = item.getDescription().getString();
            g.drawString(font, name, x + 20, y + 4, UiTheme.TEXT_SECONDARY, false);
            if (group.count > 1) {
                String countStr = "x" + group.count;
                g.drawString(font, countStr, x + contentW - 8 - font.width(countStr), y + 4, UiTheme.ACCENT, false);
            }
            y += 18;
        }
    }

    private void drawHealth(GuiGraphics g) {
        DietHistory history = getDietHistory();
        int x = contentX + 4;
        int y = contentY + 6;

        if (history == null || history.getEntryCount() < 5) {
            drawEmptyState(g, Component.translatable(P + "health.need_data").getString());
            return;
        }

        NutritionalProfile avg = history.getAverage();
        int diversity = history.getDiversity();

        g.drawString(font, Component.translatable(P + "health.title"), x, y, UiTheme.TEXT_PRIMARY, false);
        y += 12;

        String subtitle = Component.translatable(P + "health.subtitle").getString();
        g.drawString(font, subtitle, x, y, UiTheme.TEXT_MUTED, false);
        y += 12;

        HealthMetric[] metrics = {
                new HealthMetric("diabetes", avg.sugar(), 0, 6, 15, false),
                new HealthMetric("high_cholesterol", avg.fat(), 0, 5, 15, false),
                new HealthMetric("fatigue", Math.min(avg.protein(), avg.vitamins()), 1.5f, 5, 10, true),
                new HealthMetric("dehydration", avg.hydration(), 1, 5, 10, true),
                new HealthMetric("scurvy", avg.vitamins(), 0.5f, 4, 10, true),
                new HealthMetric("obesity", avg.total(), 0, 35, 60, false),
        };

        for (HealthMetric m : metrics) {
            if (y + 18 > contentY + contentH) break;
            drawHealthBar(g, x, y, contentW - 8, m);
            y += 22;
        }
    }

    private void drawHealthBar(GuiGraphics g, int x, int y, int w, HealthMetric m) {
        String name = Component.translatable("effect.spiceoflifehealthedition." + m.key).getString();
        g.drawString(font, name, x, y, UiTheme.TEXT_SECONDARY, false);

        String valStr = String.format("%.1f", m.value);
        g.drawString(font, valStr, x + w - font.width(valStr), y, UiTheme.TEXT_MUTED, false);

        int barY = y + 10;
        int barH = 6;
        g.fill(x, barY, x + w, barY + barH, UiTheme.BAR_BG);

        float normalized = Math.min(1f, m.value / m.absMax);

        float safeStart, safeEnd;
        if (m.higherIsBetter) {
            safeStart = m.safeMin / m.absMax;
            safeEnd = 1f;
        } else {
            safeStart = 0f;
            safeEnd = m.safeMax / m.absMax;
        }

        int safeStartX = x + (int) (safeStart * w);
        int safeEndX = x + (int) (safeEnd * w);
        g.fill(safeStartX, barY + 1, safeEndX, barY + barH - 1, 0xFF2A6B42);

        int markerX = x + (int) (normalized * w);
        markerX = Math.max(x, Math.min(x + w - 2, markerX));

        boolean inSafe = m.higherIsBetter ? (m.value >= m.safeMin) : (m.value <= m.safeMax);
        int markerColor = inSafe ? UiTheme.GOOD : UiTheme.BAD;
        g.fill(markerX - 1, barY - 1, markerX + 2, barY + barH + 1, markerColor);

        int refMark = m.higherIsBetter
                ? x + (int) ((m.safeMin / m.absMax) * w)
                : x + (int) ((m.safeMax / m.absMax) * w);
        g.fill(refMark, barY + barH, refMark + 1, barY + barH + 2, UiTheme.TEXT_DIM);
    }

    private void drawFoods(GuiGraphics g, int mx, int my) {
        int x = contentX + 4;
        int y = contentY + 4;

        if (selectedFood != null) {
            drawFoodDetail(g, x, y);
            return;
        }

        UiComponents.searchBox(g, font, x, y, contentW - 8, 14,
                searchFilter, Component.translatable(P + "foods.search").getString(), searchFocused);
        y += 18;

        int listY = y;
        int listH = contentY + contentH - listY;

        if (filteredFoods.isEmpty()) {
            String msg = Component.translatable(P + "foods.empty").getString();
            g.drawString(font, msg, x + (contentW - 8 - font.width(msg)) / 2, listY + listH / 2 - 4, UiTheme.TEXT_MUTED, false);
            return;
        }

        foodMaxScroll = Math.max(0, filteredFoods.size() * 18 - listH);

        int drawY = listY - foodScroll;
        for (FoodEntry entry : filteredFoods) {
            if (drawY + 18 > listY && drawY < listY + listH) {
                boolean hovered = UiComponents.pointInRect(mx, my, x, drawY, contentW - 16, 16);
                if (hovered) {
                    g.fill(x, drawY, x + contentW - 16, drawY + 16, UiTheme.TAB_HOVER);
                }
                g.renderItem(new ItemStack(entry.item), x + 2, drawY);
                g.drawString(font, entry.item.getDescription().getString(), x + 22, drawY + 4,
                        hovered ? UiTheme.TEXT_PRIMARY : UiTheme.TEXT_SECONDARY, false);

                NutritionalProfile p = entry.profile;
                int dotX = x + contentW - 20;
                int dotCount = 0;
                float[] pvals = {p.sugar(), p.fat(), p.salt(), p.protein(), p.fiber(), p.hydration(), p.vitamins()};
                for (int i = UiTheme.STAT_KEYS.length - 1; i >= 0 && dotCount < 4; i--) {
                    if (pvals[i] > 0) {
                        g.fill(dotX, drawY + 6, dotX + 4, drawY + 10, UiTheme.STAT_COLORS[i]);
                        dotX -= 6;
                        dotCount++;
                    }
                }
            }
            drawY += 18;
        }

        UiComponents.scrollbar(g, contentX + contentW - 5, listY, listH, foodScroll, foodMaxScroll, listH, filteredFoods.size() * 18);
    }

    private void drawFoodDetail(GuiGraphics g, int x, int y) {
        NutritionalProfile profile = NutritionClientCache.get(selectedFood);
        Item item = BuiltInRegistries.ITEM.get(selectedFood);
        if (item == null) return;

        g.drawString(font, Component.translatable(P + "foods.back"), x, y + 2, UiTheme.ACCENT, false);
        y += 16;

        g.renderItem(new ItemStack(item), x, y);
        g.drawString(font, item.getDescription().getString(), x + 20, y + 4, UiTheme.TEXT_PRIMARY, false);
        y += 22;

        int barW = contentW - 70;
        float[] vals = {profile.sugar(), profile.fat(), profile.salt(), profile.protein(), profile.fiber(), profile.hydration(), profile.vitamins()};
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] <= 0) continue;
            String name = Component.translatable(TT + UiTheme.STAT_KEYS[i]).getString();
            g.drawString(font, name, x, y + 1, UiTheme.TEXT_SECONDARY, false);
            UiComponents.progressBar(g, x + 56, y + 1, barW - 40, 7, vals[i], 10f, UiTheme.STAT_COLORS[i]);
            String valStr = String.format("%.1f", vals[i]);
            g.drawString(font, valStr, x + contentW - 12 - font.width(valStr), y + 1, UiTheme.TEXT_MUTED, false);
            y += 11;
        }

        y += 6;
        UiComponents.separator(g, x, y, contentW - 16);
        y += 6;

        float weightImpact = profile.total() * 0.1f + profile.fat() * 0.05f + profile.sugar() * 0.03f;
        String wi = Component.translatable(P + "foods.weight_impact", String.format("%.1f", weightImpact)).getString();
        g.drawString(font, wi, x, y, UiTheme.TEXT_MUTED, false);
        y += 10;
        String tot = Component.translatable(P + "foods.total", String.format("%.1f", profile.total())).getString();
        g.drawString(font, tot, x, y, UiTheme.TEXT_MUTED, false);
    }

    private void drawBody(GuiGraphics g) {
        DietHistory history = getDietHistory();
        float weight = history != null ? history.getWeight() : 70f;

        int x = contentX + 4;
        int y = contentY + 6;

        g.drawString(font, Component.translatable(P + "body.title"), x, y, UiTheme.TEXT_PRIMARY, false);
        y += 14;

        String catKey;
        int catColor;
        if (weight < 60) { catKey = P + "body.cat.underweight"; catColor = UiTheme.WARN; }
        else if (weight <= 85) { catKey = P + "body.cat.normal"; catColor = UiTheme.GOOD; }
        else if (weight <= 120) { catKey = P + "body.cat.overweight"; catColor = UiTheme.WARN; }
        else { catKey = P + "body.cat.obese"; catColor = UiTheme.BAD; }

        String weightStr = String.format("%.1f kg", weight);
        g.drawString(font, weightStr, x, y, UiTheme.TEXT_SECONDARY, false);
        Component category = Component.translatable(catKey);
        int catW = font.width(category);
        g.drawString(font, category, x + contentW - 8 - catW, y, catColor, false);
        y += 12;

        int barW = contentW - 8;
        int barH = 10;
        g.fill(x, y, x + barW, y + barH, UiTheme.BAR_BG);

        int zone1 = (int) ((60f - 40f) / 160f * barW);
        int zone2 = (int) ((85f - 40f) / 160f * barW);
        int zone3 = (int) ((120f - 40f) / 160f * barW);

        g.fill(x + 1, y + 1, x + zone1, y + barH - 1, 0x80E8B04A);
        g.fill(x + zone1, y + 1, x + zone2, y + barH - 1, 0x805ED67C);
        g.fill(x + zone2, y + 1, x + zone3, y + barH - 1, 0x80E8B04A);
        g.fill(x + zone3, y + 1, x + barW - 1, y + barH - 1, 0x80E85C5C);

        float norm = Math.max(0, Math.min(1, (weight - 40f) / 160f));
        int markerX = x + (int) (norm * barW);
        g.fill(markerX - 1, y - 2, markerX + 2, y + barH + 2, UiTheme.TEXT_PRIMARY);

        y += barH + 3;
        g.drawString(font, "40", x, y, UiTheme.TEXT_DIM, false);
        String mid = "85";
        g.drawString(font, mid, x + (barW - font.width(mid)) / 2, y, UiTheme.TEXT_DIM, false);
        g.drawString(font, "200", x + barW - font.width("200"), y, UiTheme.TEXT_DIM, false);
        y += 14;

        UiComponents.separator(g, x, y, contentW - 8);
        y += 6;

        g.drawString(font, Component.translatable(P + "body.effects"), x, y, UiTheme.TEXT_PRIMARY, false);
        y += 12;

        float delta = weight - 70f;
        int[] effectColors = { delta == 0 ? UiTheme.NEUTRAL : (delta > 0 ? UiTheme.BAD : UiTheme.GOOD) };

        String speed = Component.translatable(P + "body.speed", String.format("%+.0f", -delta * 0.2)).getString();
        g.drawString(font, "\u25B6 " + speed, x, y, effectColors[0], false); y += 11;
        String jump = Component.translatable(P + "body.jump", String.format("%+.0f", -delta * 0.1)).getString();
        g.drawString(font, "\u25B6 " + jump, x, y, effectColors[0], false); y += 11;
        String grav = Component.translatable(P + "body.gravity", String.format("%+.0f", delta * 0.05)).getString();
        g.drawString(font, "\u25B6 " + grav, x, y, effectColors[0], false); y += 14;

        g.drawString(font, Component.translatable(P + "body.tip").withStyle(net.minecraft.ChatFormatting.ITALIC),
                x, y, UiTheme.TEXT_MUTED, false);
    }

    private void drawEmptyState(GuiGraphics g, String message) {
        int w = font.width(message);
        int x = contentX + (contentW - w) / 2;
        int y = contentY + contentH / 2 - 4;
        g.drawString(font, message, x, y, UiTheme.TEXT_MUTED, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        Tab[] tabs = Tab.values();
        int tabW = (PANEL_W - PADDING * 2) / tabs.length;
        int tabY = py + HEADER_H;
        for (int i = 0; i < tabs.length; i++) {
            int tx = px + PADDING + i * tabW;
            if (UiComponents.pointInRect(mx, my, tx, tabY, tabW, TAB_H)) {
                activeTab = tabs[i];
                foodScroll = 0;
                selectedFood = null;
                searchFocused = false;
                return true;
            }
        }

        if (activeTab == Tab.FOODS) {
            int x = contentX + 4;
            int y = contentY + 4;
            if (selectedFood != null) {
                if (UiComponents.pointInRect(mx, my, x, y + 2, 40, 10)) {
                    selectedFood = null;
                    return true;
                }
            } else {
                searchFocused = UiComponents.pointInRect(mx, my, x, y, contentW - 8, 14);

                int listY = y + 18;
                int drawY = listY - foodScroll;
                for (FoodEntry entry : filteredFoods) {
                    if (UiComponents.pointInRect(mx, my, x, drawY, contentW - 16, 16)) {
                        selectedFood = entry.id;
                        return true;
                    }
                    drawY += 18;
                }
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (activeTab == Tab.FOODS && selectedFood == null && foodMaxScroll > 0) {
            foodScroll = (int) Math.max(0, Math.min(foodMaxScroll, foodScroll - sy * 18));
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        if (activeTab == Tab.FOODS && searchFocused && selectedFood == null) {
            if (Character.isLetterOrDigit(c) || c == ' ' || c == '_' || c == ':') {
                searchFilter += c;
                foodScroll = 0;
                rebuildFoodList();
                return true;
            }
        }
        return super.charTyped(c, mods);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int mods) {
        if (activeTab == Tab.FOODS && searchFocused && selectedFood == null) {
            if (keyCode == 259 && !searchFilter.isEmpty()) {
                searchFilter = searchFilter.substring(0, searchFilter.length() - 1);
                foodScroll = 0;
                rebuildFoodList();
                return true;
            }
            if (keyCode == 256) {
                searchFocused = false;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, mods);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private DietHistory getDietHistory() {
        if (Minecraft.getInstance().player == null) return null;
        return Minecraft.getInstance().player.getData(DietAttachment.DIET);
    }

    private record FoodEntry(ResourceLocation id, Item item, NutritionalProfile profile) {}

    private static class HealthMetric {
        final String key;
        final float value;
        final float safeMin;
        final float safeMax;
        final float absMax;
        final boolean higherIsBetter;

        HealthMetric(String key, float value, float safeMin, float safeMax, float absMax, boolean higherIsBetter) {
            this.key = key;
            this.value = value;
            this.safeMin = safeMin;
            this.safeMax = safeMax;
            this.absMax = absMax;
            this.higherIsBetter = higherIsBetter;
        }
    }
}
