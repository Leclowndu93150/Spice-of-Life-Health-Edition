package com.leclowndu93150.spiceoflifehealthedition.client;

import com.leclowndu93150.spiceoflifehealthedition.api.NutritionalProfile;
import com.leclowndu93150.spiceoflifehealthedition.nutrition.ModDataMaps;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NutritionClientCache {

    private static Map<ResourceLocation, NutritionalProfile> cache = Collections.emptyMap();
    private static Map<ResourceLocation, NutritionalProfile> mergedCache = null;

    public static void update(Map<ResourceLocation, NutritionalProfile> data) {
        cache = new HashMap<>(data);
        mergedCache = null;
    }

    public static NutritionalProfile get(ResourceLocation item) {
        NutritionalProfile profile = cache.get(item);
        if (profile != null) return profile;

        Item minecraftItem = BuiltInRegistries.ITEM.get(item);
        if (minecraftItem != null) {
            Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(minecraftItem);
            NutritionalProfile data = holder.getData(ModDataMaps.NUTRITION);
            if (data != null) return data;
        }

        return NutritionalProfile.EMPTY;
    }

    public static Map<ResourceLocation, NutritionalProfile> getAll() {
        if (mergedCache == null) {
            Map<ResourceLocation, NutritionalProfile> merged = new HashMap<>(cache);
            for (Holder.Reference<Item> holder : BuiltInRegistries.ITEM.holders().toList()) {
                NutritionalProfile data = holder.getData(ModDataMaps.NUTRITION);
                if (data != null) {
                    ResourceLocation key = holder.key().location();
                    merged.putIfAbsent(key, data);
                }
            }
            mergedCache = merged;
        }
        return Collections.unmodifiableMap(mergedCache);
    }

    public static void invalidate() {
        mergedCache = null;
    }
}
