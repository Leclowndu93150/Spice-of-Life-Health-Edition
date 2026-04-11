package com.leclowndu93150.spiceoflifehealthedition.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class UiComponents {

    public static void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, UiTheme.PANEL_BORDER);
        g.fill(x, y, x + w, y + h, UiTheme.PANEL_BG);
    }

    public static void innerPanel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, UiTheme.PANEL_BORDER_DIM);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, UiTheme.PANEL_INNER);
    }

    public static void separator(GuiGraphics g, int x, int y, int w) {
        g.fill(x, y, x + w, y + 1, UiTheme.PANEL_BORDER_DIM);
    }

    public static void verticalSeparator(GuiGraphics g, int x, int y, int h) {
        g.fill(x, y, x + 1, y + h, UiTheme.PANEL_BORDER_DIM);
    }

    public static void progressBar(GuiGraphics g, int x, int y, int w, int h, float value, float max, int fillColor) {
        g.fill(x, y, x + w, y + h, UiTheme.BAR_BG);
        g.fill(x, y, x + w, y + 1, UiTheme.BAR_TRACK);
        g.fill(x, y + h - 1, x + w, y + h, UiTheme.BAR_TRACK);
        if (max <= 0) return;
        int filled = (int) Math.min(w - 2, ((value / max) * (w - 2)));
        if (filled > 0) {
            g.fill(x + 1, y + 1, x + 1 + filled, y + h - 1, fillColor);
        }
    }

    public static void segmentedBar(GuiGraphics g, int x, int y, int w, int h, int segments, int filledSegments, int fillColor) {
        g.fill(x, y, x + w, y + h, UiTheme.BAR_BG);
        int segW = (w - (segments - 1)) / segments;
        for (int i = 0; i < segments; i++) {
            int sx = x + i * (segW + 1);
            int color = i < filledSegments ? fillColor : UiTheme.BAR_TRACK;
            g.fill(sx, y + 1, sx + segW, y + h - 1, color);
        }
    }

    public static void rangeBar(GuiGraphics g, int x, int y, int w, int h,
                                 float value, float normalMin, float normalMax, float absMax) {
        g.fill(x, y, x + w, y + h, UiTheme.BAR_BG);

        int normalStartX = x + (int) ((normalMin / absMax) * w);
        int normalEndX = x + (int) ((normalMax / absMax) * w);
        g.fill(normalStartX, y + 1, normalEndX, y + h - 1, 0x40338855);

        int markerX = x + (int) Math.min(w - 2, Math.max(0, (value / absMax) * w));
        int markerColor = (value >= normalMin && value <= normalMax) ? UiTheme.GOOD :
                          (value < normalMin ? UiTheme.WARN : UiTheme.BAD);
        g.fill(markerX - 1, y - 1, markerX + 2, y + h + 1, markerColor);

        int normalMarkX = x + (int) (((normalMin + normalMax) / 2 / absMax) * w);
        g.fill(normalMarkX, y + h / 2, normalMarkX + 1, y + h / 2 + 1, UiTheme.TEXT_DIM);
    }

    public static void tab(GuiGraphics g, Font font, int x, int y, int w, int h,
                           String label, boolean active, boolean hovered) {
        int bg = active ? UiTheme.TAB_ACTIVE : (hovered ? UiTheme.TAB_HOVER : UiTheme.TAB_INACTIVE);
        g.fill(x, y, x + w, y + h, bg);

        if (active) {
            g.fill(x, y + h - 2, x + w, y + h, UiTheme.ACCENT);
        } else {
            g.fill(x, y + h - 1, x + w, y + h, UiTheme.PANEL_BORDER_DIM);
        }

        int textColor = active ? UiTheme.TEXT_PRIMARY : (hovered ? UiTheme.TEXT_SECONDARY : UiTheme.TEXT_MUTED);
        int textW = font.width(label);
        int textX = x + (w - textW) / 2;
        int textY = y + (h - 8) / 2;
        g.drawString(font, label, textX, textY, textColor, false);
    }

    public static void searchBox(GuiGraphics g, Font font, int x, int y, int w, int h,
                                  String text, String placeholder, boolean focused) {
        int border = focused ? UiTheme.ACCENT : UiTheme.PANEL_BORDER_DIM;
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, border);
        g.fill(x, y, x + w, y + h, UiTheme.BAR_BG);

        String display = text.isEmpty() ? placeholder : text;
        int color = text.isEmpty() ? UiTheme.TEXT_DIM : UiTheme.TEXT_PRIMARY;
        g.drawString(font, display, x + 4, y + (h - 8) / 2, color, false);
    }

    public static int drawWrappedText(GuiGraphics g, Font font, Component text, int x, int y, int maxWidth, int color) {
        List<FormattedCharSequence> lines = font.split(text, maxWidth);
        int lineH = font.lineHeight;
        for (FormattedCharSequence line : lines) {
            g.drawString(font, line, x, y, color, false);
            y += lineH;
        }
        return y;
    }

    public static int drawWrappedText(GuiGraphics g, Font font, String text, int x, int y, int maxWidth, int color) {
        return drawWrappedText(g, font, Component.literal(text), x, y, maxWidth, color);
    }

    public static void scrollbar(GuiGraphics g, int x, int y, int h, int scroll, int maxScroll, int viewH, int contentH) {
        if (maxScroll <= 0) return;
        g.fill(x, y, x + 3, y + h, UiTheme.BAR_BG);
        int thumbH = Math.max(8, h * viewH / contentH);
        int thumbY = y + (int) (((float) scroll / maxScroll) * (h - thumbH));
        g.fill(x, thumbY, x + 3, thumbY + thumbH, UiTheme.ACCENT);
    }

    public static void iconButton(GuiGraphics g, int x, int y, int size, boolean hovered) {
        int bg = hovered ? UiTheme.TAB_HOVER : UiTheme.TAB_INACTIVE;
        g.fill(x, y, x + size, y + size, bg);
        g.fill(x, y, x + size, y + 1, UiTheme.PANEL_BORDER_DIM);
        g.fill(x, y + size - 1, x + size, y + size, UiTheme.PANEL_BORDER_DIM);
        g.fill(x, y, x + 1, y + size, UiTheme.PANEL_BORDER_DIM);
        g.fill(x + size - 1, y, x + size, y + size, UiTheme.PANEL_BORDER_DIM);
    }

    public static boolean pointInRect(double px, double py, int x, int y, int w, int h) {
        return px >= x && px < x + w && py >= y && py < y + h;
    }

    public static class Grid {
        public final int x, y, w, h;
        public final int cols, rows;
        public final int gap;
        public final int cellW, cellH;

        public Grid(int x, int y, int w, int h, int cols, int rows, int gap) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.cols = cols;
            this.rows = rows;
            this.gap = gap;
            this.cellW = (w - gap * (cols - 1)) / cols;
            this.cellH = (h - gap * (rows - 1)) / rows;
        }

        public int cellX(int col) { return x + col * (cellW + gap); }
        public int cellY(int row) { return y + row * (cellH + gap); }
    }

    public static List<String> wrapToLines(Font font, String text, int maxWidth) {
        List<String> result = new ArrayList<>();
        List<FormattedCharSequence> split = font.split(Component.literal(text), maxWidth);
        for (FormattedCharSequence fcs : split) {
            StringBuilder sb = new StringBuilder();
            fcs.accept((index, style, codePoint) -> {
                sb.appendCodePoint(codePoint);
                return true;
            });
            result.add(sb.toString());
        }
        return result;
    }
}
