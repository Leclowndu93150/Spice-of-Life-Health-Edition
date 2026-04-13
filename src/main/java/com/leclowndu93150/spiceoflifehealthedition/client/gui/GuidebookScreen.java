package com.leclowndu93150.spiceoflifehealthedition.client.gui;

import com.leclowndu93150.spiceoflifehealthedition.api.NutritionalProfile;
import com.leclowndu93150.spiceoflifehealthedition.client.NutritionClientCache;
import com.leclowndu93150.spiceoflifehealthedition.client.gui.widget.UiComponents;
import com.leclowndu93150.spiceoflifehealthedition.client.gui.widget.UiTheme;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietAttachment;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietHistory;
import com.leclowndu93150.spiceoflifehealthedition.trait.NutritionalTraits;
import com.leclowndu93150.spiceoflifehealthedition.trait.Stat;
import com.leclowndu93150.spiceoflifehealthedition.trait.TraitDefinition;
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
        BODY("body", "\u25EF"),
        EXERCISE("exercise", "\u2191");

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
            case EXERCISE -> drawExercise(g);
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

    private static final float[] STAT_MAX = { 12f, 12f, 10f, 12f, 10f, 12f, 12f };

    private void drawHealth(GuiGraphics g) {
        DietHistory history = getDietHistory();
        int x = contentX + 4;
        int y = contentY + 6;

        if (history == null) {
            drawEmptyState(g, Component.translatable(P + "health.need_data").getString());
            return;
        }

        NutritionalProfile avg = history.getAverage();
        Map<String, Integer> active = history.getActiveTraits();

        g.drawString(font, Component.translatable(P + "health.title"), x, y, UiTheme.TEXT_PRIMARY, false);

        int good = 0, bad = 0;
        for (Map.Entry<String, Integer> e : active.entrySet()) {
            TraitDefinition def = NutritionalTraits.byId(e.getKey());
            if (def == null) continue;
            if (def.positive()) good++; else bad++;
        }
        String counter = Component.translatable(P + "health.counts", good, bad).getString();
        g.drawString(font, counter, x + contentW - 8 - font.width(counter), y, UiTheme.TEXT_MUTED, false);
        y += 14;

        Stat[] stats = Stat.values();
        for (int i = 0; i < stats.length; i++) {
            Stat stat = stats[i];
            if (y + 12 > contentY + contentH) break;
            float value = stat.get(avg);
            drawStatRow(g, x, y, contentW - 8, stat, value, STAT_MAX[i], active);
            y += 13;
        }
    }

    private void drawStatRow(GuiGraphics g, int x, int y, int w, Stat stat, float value, float max, Map<String, Integer> active) {
        String name = Component.translatable(TT + stat.id).getString();
        int statColor = UiTheme.STAT_COLORS[stat.ordinal()];

        TraitDefinition positive = null;
        TraitDefinition negative = null;
        int positiveLevel = 0;
        int negativeLevel = 0;
        for (TraitDefinition def : NutritionalTraits.ALL) {
            if (def.stat() != stat) continue;
            Integer level = active.get(def.id());
            if (level == null) continue;
            if (def.positive()) {
                positive = def;
                positiveLevel = level;
            } else {
                negative = def;
                negativeLevel = level;
            }
        }

        g.fill(x, y + 1, x + 2, y + 10, statColor);
        g.drawString(font, name, x + 6, y + 2, UiTheme.TEXT_SECONDARY, false);

        int barX = x + 58;
        int barW = w - 170;
        int barY = y + 4;
        int barH = 5;

        g.fill(barX, barY, barX + barW, barY + barH, UiTheme.BAR_BG);

        float norm = Math.min(1f, value / max);
        int filled = (int) (norm * (barW - 2));

        int barColor;
        if (negative != null) barColor = UiTheme.BAD;
        else if (positive != null) barColor = UiTheme.GOOD;
        else barColor = statColor;

        if (filled > 0) {
            g.fill(barX + 1, barY + 1, barX + 1 + filled, barY + barH - 1, barColor);
        }

        String valStr = String.format("%.1f", value);
        g.drawString(font, valStr, barX + barW + 4, y + 2, UiTheme.TEXT_MUTED, false);

        String effectLabel;
        int effectColor;
        if (negative != null) {
            effectLabel = Component.translatable("trait.spiceoflifehealthedition." + negative.id()).getString() + " " + roman(negativeLevel);
            effectColor = UiTheme.BAD;
        } else if (positive != null) {
            effectLabel = Component.translatable("trait.spiceoflifehealthedition." + positive.id()).getString() + " " + roman(positiveLevel);
            effectColor = UiTheme.GOOD;
        } else {
            effectLabel = "\u2014";
            effectColor = UiTheme.TEXT_DIM;
        }
        int labelX = x + w - font.width(effectLabel);
        g.drawString(font, effectLabel, labelX, y + 2, effectColor, false);
    }

    private static String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> String.valueOf(n);
        };
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

        float weightImpact = profile.fat() * 0.06f + profile.sugar() * 0.04f + profile.protein() * 0.02f
                + profile.salt() * 0.01f - profile.fiber() * 0.02f - profile.vitamins() * 0.01f - profile.hydration() * 0.01f;
        String wi = Component.translatable(P + "foods.weight_impact", String.format("%.1f", weightImpact)).getString();
        g.drawString(font, wi, x, y, UiTheme.TEXT_MUTED, false);
        y += 10;
        String tot = Component.translatable(P + "foods.total", String.format("%.1f", profile.total())).getString();
        g.drawString(font, tot, x, y, UiTheme.TEXT_MUTED, false);
    }

    private void drawBody(GuiGraphics g) {
        DietHistory history = getDietHistory();
        float weight = history != null ? history.getWeight() : 70f;
        Map<String, Integer> active = history != null ? history.getActiveTraits() : Map.of();

        int x = contentX + 4;
        int y = contentY + 6;

        g.drawString(font, Component.translatable(P + "body.title"), x, y, UiTheme.TEXT_PRIMARY, false);
        y += 12;

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
        y += 11;

        int barW = contentW - 8;
        int barH = 8;
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

        y += barH + 10;
        UiComponents.separator(g, x, y - 3, contentW - 8);

        g.drawString(font, Component.translatable(P + "body.adaptations"), x, y, UiTheme.TEXT_PRIMARY, false);
        y += 11;

        boolean anyShown = false;
        for (TraitDefinition def : NutritionalTraits.ALL) {
            Integer level = active.get(def.id());
            if (level == null) continue;
            if (y + 10 > contentY + contentH) break;
            drawTraitLine(g, x, y, def, level);
            y += 10;
            anyShown = true;
        }

        if (!anyShown) {
            g.drawString(font, Component.translatable(P + "body.no_effects"), x + 4, y, UiTheme.TEXT_DIM, false);
            y += 10;
        }
    }

    private void drawTraitLine(GuiGraphics g, int x, int y, TraitDefinition def, int level) {
        int color = def.positive() ? UiTheme.GOOD : UiTheme.BAD;
        String marker = def.positive() ? "\u25B2" : "\u25BC";
        g.drawString(font, marker, x, y, color, false);

        String name = Component.translatable("trait.spiceoflifehealthedition." + def.id()).getString() + " " + roman(level);
        g.drawString(font, name, x + 8, y, UiTheme.TEXT_SECONDARY, false);

        String desc = Component.translatable("trait.spiceoflifehealthedition." + def.id() + ".effect." + level).getString();
        g.drawString(font, desc, x + 8 + font.width(name) + 6, y, UiTheme.TEXT_DIM, false);
    }

    private static final int ACTIVITY_COLOR = 0xFF5EC870;
    private static final int ACTIVITY_DIM = 0xFF2A5A30;

    private void drawExercise(GuiGraphics g) {
        DietHistory history = getDietHistory();
        float weight = history != null ? history.getWeight() : 70f;
        int x = contentX + 4;
        int y = contentY + 6;

        g.drawString(font, Component.translatable(P + "exercise.title"), x, y, UiTheme.TEXT_PRIMARY, false);
        y += 14;

        y = UiComponents.drawWrappedText(g, font, Component.translatable(P + "exercise.desc"),
                x, y, contentW - 8, UiTheme.TEXT_SECONDARY);
        y += 8;

        UiComponents.separator(g, x, y, contentW - 8);
        y += 6;

        g.drawString(font, Component.translatable(P + "exercise.activities"), x, y, UiTheme.TEXT_PRIMARY, false);
        y += 12;

        String[][] activities = {
                {"\u2B06", Component.translatable(P + "exercise.sprint").getString(), "+++"},
                {"\u2B06", Component.translatable(P + "exercise.swim").getString(), "+++"},
                {"\u26CF", Component.translatable(P + "exercise.mine").getString(), "++"},
                {"\u2694", Component.translatable(P + "exercise.kill").getString(), "++"},
                {"\u2604", Component.translatable(P + "exercise.fish").getString(), "+"},
                {"\u270B", Component.translatable(P + "exercise.swing").getString(), "+"},
        };

        for (String[] act : activities) {
            g.drawString(font, act[0], x + 2, y, ACTIVITY_COLOR, false);
            g.drawString(font, act[1], x + 16, y, UiTheme.TEXT_SECONDARY, false);

            int intensity = act[2].length();
            int dotX = x + contentW - 30;
            for (int i = 0; i < 3; i++) {
                int color = i < intensity ? ACTIVITY_COLOR : ACTIVITY_DIM;
                g.fill(dotX + i * 8, y + 2, dotX + i * 8 + 5, y + 7, color);
            }
            y += 12;
        }

        y += 4;
        UiComponents.separator(g, x, y, contentW - 8);
        y += 6;

        g.drawString(font, Component.translatable(P + "exercise.weight_effects"), x, y, UiTheme.TEXT_PRIMARY, false);
        y += 12;

        if (weight > 70f) {
            float overweightFactor = (weight - 70f) / 130f;
            String staminaStr = Component.translatable(P + "exercise.stamina_penalty",
                    String.format("%.0f%%", overweightFactor * 200f)).getString();
            g.drawString(font, staminaStr, x + 4, y, UiTheme.BAD, false);
            y += 11;

            String airStr = Component.translatable(P + "exercise.air_penalty").getString();
            g.drawString(font, airStr, x + 4, y, UiTheme.BAD, false);
            y += 11;

            String speedStr = Component.translatable(P + "exercise.speed_penalty",
                    String.format("%.1f%%", (weight - 70f) * 0.2f)).getString();
            g.drawString(font, speedStr, x + 4, y, UiTheme.BAD, false);
        } else {
            g.drawString(font, Component.translatable(P + "exercise.no_penalties"), x + 4, y, UiTheme.GOOD, false);
        }
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
}
