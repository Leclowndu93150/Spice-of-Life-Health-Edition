package com.leclowndu93150.spiceoflifehealthedition.client.tooltip;

import com.leclowndu93150.spiceoflifehealthedition.Config;
import com.leclowndu93150.spiceoflifehealthedition.SpiceOfLifeHealthEdition;
import com.leclowndu93150.spiceoflifehealthedition.api.NutritionalProfile;
import com.leclowndu93150.spiceoflifehealthedition.client.NutritionClientCache;
import com.leclowndu93150.spiceoflifehealthedition.nutrition.ModDataMaps;
import com.leclowndu93150.spiceoflifehealthedition.nutrition.SpecialNutrition;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.slf4j.Logger;

import java.util.function.Function;

@EventBusSubscriber(modid = SpiceOfLifeHealthEdition.MODID, value = Dist.CLIENT)
public class NutritionTooltipHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TT = "tooltip.spiceoflifehealthedition.";
    private static boolean loggedOnce = false;

    private record StatDef(String langKey, int color, Function<NutritionalProfile, Float> getter) {}

    private static final StatDef[] STATS = {
            new StatDef(TT + "sugar", 0xFFAA00, NutritionalProfile::sugar),
            new StatDef(TT + "fat", 0xDDCC33, NutritionalProfile::fat),
            new StatDef(TT + "salt", 0xBBBBBB, NutritionalProfile::salt),
            new StatDef(TT + "protein", 0xCC4444, NutritionalProfile::protein),
            new StatDef(TT + "fiber", 0x55CC55, NutritionalProfile::fiber),
            new StatDef(TT + "hydration", 0x55BBCC, NutritionalProfile::hydration),
            new StatDef(TT + "vitamins", 0xCC66CC, NutritionalProfile::vitamins),
    };

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (Config.tooltipDetail == 0) return;

        ItemStack stack = event.getItemStack();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        NutritionalProfile override = SpecialNutrition.getOverride(stack);
        NutritionalProfile profile;
        if (override != null) {
            profile = override;
        } else {
            profile = NutritionClientCache.get(itemId);
            if (profile.isEmpty()) {
                Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem());
                NutritionalProfile dataMapProfile = holder.getData(ModDataMaps.NUTRITION);
                if (dataMapProfile != null) {
                    profile = dataMapProfile;
                }
            }
        }

        if (!loggedOnce) {
            LOGGER.info("[SpiceOfLife] Tooltip check for {}: cache={}, profile={}",
                    itemId, NutritionClientCache.getAll().size(), profile.isEmpty() ? "EMPTY" : "found");
            loggedOnce = true;
        }

        if (profile.isEmpty()) return;

        boolean isFood = stack.get(DataComponents.FOOD) != null;

        if (Screen.hasShiftDown()) {
            addDetailedTooltip(event, profile, isFood);
        } else {
            addCompactTooltip(event, profile, isFood);
        }
    }

    private static void addCompactTooltip(ItemTooltipEvent event, NutritionalProfile profile, boolean isFood) {
        if (!isFood) {
            event.getToolTip().add(Component.translatable(TT + "ingredient_label")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        int present = 0;
        for (StatDef stat : STATS) {
            if (stat.getter.apply(profile) > 0) present++;
        }

        if (present <= 4) {
            for (StatDef stat : STATS) {
                float val = stat.getter.apply(profile);
                if (val <= 0) continue;
                String name = Component.translatable(stat.langKey).getString();
                event.getToolTip().add(compactStatLine(name, val, stat.color));
            }
        } else {
            MutableComponent line1 = Component.empty();
            MutableComponent line2 = Component.empty();
            int idx = 0;
            for (StatDef stat : STATS) {
                float val = stat.getter.apply(profile);
                if (val <= 0) continue;
                String name = Component.translatable(stat.langKey).getString();
                MutableComponent chip = compactChip(name, val, stat.color);
                if (idx < 4) {
                    if (idx > 0) line1.append(Component.literal("  "));
                    line1.append(chip);
                } else {
                    if (idx > 4) line2.append(Component.literal("  "));
                    line2.append(chip);
                }
                idx++;
            }
            event.getToolTip().add(line1);
            if (idx > 4) event.getToolTip().add(line2);
        }

        event.getToolTip().add(Component.translatable(TT + "hold_shift")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    private static MutableComponent compactStatLine(String name, float value, int color) {
        return Component.literal(" ")
                .append(Component.literal("\u25CF ").withStyle(Style.EMPTY.withColor(color)))
                .append(Component.literal(name).withStyle(Style.EMPTY.withColor(color)))
                .append(Component.literal("  " + formatValue(value)).withStyle(ChatFormatting.GRAY));
    }

    private static MutableComponent compactChip(String name, float value, int color) {
        return Component.literal("\u25CF").withStyle(Style.EMPTY.withColor(color))
                .append(Component.literal(name + " " + formatValue(value)).withStyle(ChatFormatting.GRAY));
    }

    private static void addDetailedTooltip(ItemTooltipEvent event, NutritionalProfile profile, boolean isFood) {
        if (isFood) {
            event.getToolTip().add(Component.translatable(TT + "nutrition")
                    .withStyle(ChatFormatting.WHITE));
        } else {
            event.getToolTip().add(Component.translatable(TT + "ingredient_nutrition")
                    .withStyle(ChatFormatting.WHITE));
        }

        for (StatDef stat : STATS) {
            float val = stat.getter.apply(profile);
            if (val <= 0) continue;
            String name = Component.translatable(stat.langKey).getString();
            event.getToolTip().add(detailedStatLine(name, val, stat.color));
        }

        if (isFood) {
            event.getToolTip().add(Component.empty());

            float weightImpact = profile.total() * 0.1f + profile.fat() * 0.05f + profile.sugar() * 0.03f;
            event.getToolTip().add(Component.literal(" ")
                    .append(Component.translatable(TT + "weight_label").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(" +" + formatValue(weightImpact)).withStyle(ChatFormatting.DARK_GRAY)));

            event.getToolTip().add(Component.literal(" ")
                    .append(Component.translatable(TT + "total_label").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(" " + formatValue(profile.total())).withStyle(ChatFormatting.DARK_GRAY)));
        }
    }

    private static MutableComponent detailedStatLine(String name, float value, int color) {
        int bars = Math.max(1, Math.min(10, Math.round(value)));
        int empty = 10 - bars;

        MutableComponent line = Component.literal(" ");
        line.append(Component.literal("\u25CF ").withStyle(Style.EMPTY.withColor(color)));

        String padded = name + " ".repeat(Math.max(0, 10 - name.length()));
        line.append(Component.literal(padded).withStyle(Style.EMPTY.withColor(color)));

        StringBuilder barStr = new StringBuilder();
        for (int i = 0; i < bars; i++) barStr.append('\u2503');
        line.append(Component.literal(barStr.toString()).withStyle(Style.EMPTY.withColor(color)));

        if (empty > 0) {
            StringBuilder emptyStr = new StringBuilder();
            for (int i = 0; i < empty; i++) emptyStr.append('\u2503');
            line.append(Component.literal(emptyStr.toString()).withStyle(Style.EMPTY.withColor(0x333333)));
        }

        line.append(Component.literal(" " + formatValue(value)).withStyle(ChatFormatting.GRAY));
        return line;
    }

    private static String formatValue(float value) {
        if (value == (int) value) return String.valueOf((int) value);
        return String.format("%.1f", value);
    }
}
