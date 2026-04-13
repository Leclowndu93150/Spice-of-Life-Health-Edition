package com.leclowndu93150.spiceoflifehealthedition.client.gui;

import com.leclowndu93150.spiceoflifehealthedition.diet.DietAttachment;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietHistory;
import com.leclowndu93150.spiceoflifehealthedition.trait.NutritionalTraits;
import com.leclowndu93150.spiceoflifehealthedition.trait.TraitDefinition;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class NutritionHudOverlay implements LayeredDraw.Layer {

    private static final int BG = 0xA0101018;
    private static final int BG_INNER = 0xA0181824;
    private static final int BORDER = 0xFF2A3050;
    private static final int BORDER_LIGHT = 0xFF3A4568;

    private static final int BAR_BG = 0xFF0E1020;
    private static final int STAMINA_FULL = 0xFF4EC870;
    private static final int STAMINA_MID = 0xFFE8B840;
    private static final int STAMINA_LOW = 0xFFE84848;
    private static final int STAMINA_EMPTY = 0xFF8B2020;

    private static final int WEIGHT_NORMAL = 0xFFB8C0D8;
    private static final int WEIGHT_OVER = 0xFFE8B840;
    private static final int WEIGHT_OBESE = 0xFFE85858;
    private static final int WEIGHT_UNDER = 0xFF68A8E8;

    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFF707890;

    private static final float BASE_WEIGHT = 70.0f;
    private static final float EXERTION_SPRINT_RATE = 0.2f;
    private static final float EXERTION_JUMP_SPIKE = 3.0f;
    private static final float EXERTION_DECAY = 0.15f;
    private static final float MAX_EXERTION = 100.0f;

    private float clientExertion;
    private boolean wasOnGround = true;
    private boolean wasSprinting;

    private float displayExertion;

    @Override
    public void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui || player.isSpectator()) return;

        DietHistory history = player.getData(DietAttachment.DIET);
        float weight = history.getWeight();
        Map<String, Integer> activeTraits = history.getActiveTraits();

        updateClientExertion(player, weight);
        displayExertion = Mth.lerp(0.15f, displayExertion, clientExertion);

        Font font = mc.font;
        int screenW = g.guiWidth();
        int screenH = g.guiHeight();

        int panelW = 72;
        int panelX = screenW - panelW - 4;
        int panelY = screenH - 58;

        renderWeightSection(g, font, panelX, panelY, panelW, weight);
        renderStaminaBar(g, font, panelX, panelY + 22, panelW, weight);
        renderTraitIcons(g, font, panelX, panelY + 38, panelW, activeTraits);
    }

    private void renderWeightSection(GuiGraphics g, Font font, int x, int y, int w, float weight) {
        drawPanel(g, x, y, w, 18);

        int weightColor;
        if (weight < 60) weightColor = WEIGHT_UNDER;
        else if (weight <= 85) weightColor = WEIGHT_NORMAL;
        else if (weight <= 130) weightColor = WEIGHT_OVER;
        else weightColor = WEIGHT_OBESE;

        String weightStr = String.format("%.0fkg", weight);
        int textW = font.width(weightStr);

        String label = "\u2696";
        g.drawString(font, label, x + 4, y + 5, TEXT_DIM, false);
        g.drawString(font, weightStr, x + w - textW - 4, y + 5, weightColor, false);
    }

    private void renderStaminaBar(GuiGraphics g, Font font, int x, int y, int w, float weight) {
        if (weight <= BASE_WEIGHT) return;

        drawPanel(g, x, y, w, 12);

        int barX = x + 3;
        int barY = y + 3;
        int barW = w - 6;
        int barH = 6;

        g.fill(barX, barY, barX + barW, barY + barH, BAR_BG);

        float ratio = displayExertion / MAX_EXERTION;
        float invRatio = 1.0f - ratio;
        int fillW = (int) (barW * invRatio);

        int barColor;
        if (ratio < 0.3f) barColor = STAMINA_FULL;
        else if (ratio < 0.6f) barColor = STAMINA_MID;
        else if (ratio < 0.9f) barColor = STAMINA_LOW;
        else barColor = STAMINA_EMPTY;

        if (fillW > 0) {
            g.fill(barX, barY, barX + fillW, barY + barH, barColor);
            int highlight = (barColor & 0x00FFFFFF) | 0x30000000;
            g.fill(barX, barY, barX + fillW, barY + 1, highlight);
        }

        if (ratio >= 1.0f) {
            long pulse = System.currentTimeMillis() % 1000;
            if (pulse < 500) {
                g.fill(barX, barY, barX + barW, barY + barH, 0x40FF2020);
            }
        }
    }

    private void renderTraitIcons(GuiGraphics g, Font font, int x, int y, int w, Map<String, Integer> activeTraits) {
        if (activeTraits.isEmpty()) return;

        int count = Math.min(activeTraits.size(), 6);
        int iconSize = 9;
        int gap = 2;
        int totalW = count * iconSize + (count - 1) * gap;
        int startX = x + (w - totalW) / 2;

        int i = 0;
        for (Map.Entry<String, Integer> entry : activeTraits.entrySet()) {
            if (i >= 6) break;

            TraitDefinition trait = NutritionalTraits.byId(entry.getKey());
            if (trait == null) { i++; continue; }

            int tier = entry.getValue();
            int ix = startX + i * (iconSize + gap);
            int iy = y;

            int color = trait.color() | 0xFF000000;
            int darkColor = darken(color, 0.4f);

            g.fill(ix, iy, ix + iconSize, iy + iconSize, BORDER);
            g.fill(ix + 1, iy + 1, ix + iconSize - 1, iy + iconSize - 1, darkColor);

            String sym = trait.positive() ? "\u25B2" : "\u25BC";
            int symW = font.width(sym);
            g.drawString(font, sym, ix + (iconSize - symW) / 2, iy + 1, color, false);

            if (tier > 1) {
                String tierStr = String.valueOf(tier);
                g.drawString(font, tierStr, ix + iconSize - font.width(tierStr), iy + iconSize - 8, 0xCCFFFFFF, false);
            }

            i++;
        }
    }

    private void updateClientExertion(LocalPlayer player, float weight) {
        if (weight <= BASE_WEIGHT) {
            clientExertion = 0;
            return;
        }

        float overweightFactor = (weight - BASE_WEIGHT) / 130.0f;

        if (player.isSprinting()) {
            clientExertion += EXERTION_SPRINT_RATE * (1.0f + overweightFactor * 2.0f);
        }

        if (!player.onGround() && wasOnGround) {
            clientExertion += EXERTION_JUMP_SPIKE * (1.0f + overweightFactor * 3.0f);
        }
        wasOnGround = player.onGround();

        if (!player.isSprinting() && player.onGround()) {
            clientExertion -= EXERTION_DECAY;
        }

        clientExertion = Mth.clamp(clientExertion, 0, MAX_EXERTION);
    }

    private void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, BORDER);
        g.fill(x, y, x + w, y + h, BG);
    }

    private static int darken(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int gr = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (gr << 8) | b;
    }
}
