package com.leclowndu93150.spiceoflifehealthedition.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;

import java.util.HashMap;
import java.util.Map;

public class FarmersDelightCompat {

    private static final Map<ResourceLocation, FeastMapping> FEAST_MAPPINGS = new HashMap<>();

    static {
        feast("farmersdelight:roast_chicken_block", "farmersdelight:roast_chicken", 4);
        feast("farmersdelight:stuffed_pumpkin_block", "farmersdelight:stuffed_pumpkin", 4);
        feast("farmersdelight:honey_glazed_ham_block", "farmersdelight:honey_glazed_ham", 4);
        feast("farmersdelight:shepherds_pie_block", "farmersdelight:shepherds_pie", 4);
        feast("farmersdelight:rice_roll_medley_block", "farmersdelight:kelp_roll_slice", 8);
    }

    private static void feast(String blockItem, String servingItem, int servings) {
        FEAST_MAPPINGS.put(ResourceLocation.parse(blockItem), new FeastMapping(ResourceLocation.parse(servingItem), servings));
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded("farmersdelight");
    }

    public static FeastMapping getFeastMapping(Item blockItem) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(blockItem);
        return FEAST_MAPPINGS.get(key);
    }

    public static Map<ResourceLocation, FeastMapping> getAllFeastMappings() {
        return FEAST_MAPPINGS;
    }

    public record FeastMapping(ResourceLocation servingItem, int servings) {}
}
