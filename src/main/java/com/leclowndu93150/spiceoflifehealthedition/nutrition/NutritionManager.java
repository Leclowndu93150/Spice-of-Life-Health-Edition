package com.leclowndu93150.spiceoflifehealthedition.nutrition;

import com.leclowndu93150.spiceoflifehealthedition.api.NutritionalProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.core.HolderLookup;
import org.slf4j.Logger;

import java.util.*;

public class NutritionManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final NutritionManager INSTANCE = new NutritionManager();

    private final Map<Item, NutritionalProfile> cache = new HashMap<>();
    private final Map<ResourceLocation, NutritionalProfile> cacheByKey = new HashMap<>();

    public static NutritionManager get() {
        return INSTANCE;
    }

    public NutritionalProfile getNutrition(Item item) {
        return cache.getOrDefault(item, NutritionalProfile.EMPTY);
    }

    public NutritionalProfile getNutrition(ItemStack stack) {
        return getNutrition(stack.getItem());
    }

    public Map<ResourceLocation, NutritionalProfile> getCacheByKey() {
        return Collections.unmodifiableMap(cacheByKey);
    }

    public void recompute(RecipeManager recipeManager, HolderLookup.Provider registryAccess) {
        cache.clear();
        cacheByKey.clear();

        Registry<Item> itemRegistry = BuiltInRegistries.ITEM;
        int dataMapCount = 0;
        for (Holder<Item> holder : itemRegistry.holders().toList()) {
            NutritionalProfile base = holder.getData(ModDataMaps.NUTRITION);
            if (base != null) {
                cache.put(holder.value(), base);
                dataMapCount++;
            }
        }
        LOGGER.info("[SpiceOfLife] Loaded {} base nutrition values from data maps", dataMapCount);

        Set<String> recipeTypesFound = new HashSet<>();
        List<RecipeEntry> entries = new ArrayList<>();

        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            Recipe<?> recipe = holder.value();
            if (recipe.isSpecial()) continue;

            ItemStack result = recipe.getResultItem(registryAccess);
            if (result.isEmpty()) continue;

            Item resultItem = result.getItem();
            List<Ingredient> ingredients = recipe.getIngredients();
            if (ingredients.isEmpty()) continue;

            List<Ingredient> nonEmpty = new ArrayList<>();
            for (Ingredient ing : ingredients) {
                if (ing.getItems().length > 0) {
                    nonEmpty.add(ing);
                }
            }
            if (nonEmpty.isEmpty()) continue;

            ResourceLocation typeId = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
            recipeTypesFound.add(typeId != null ? typeId.toString() : "unknown");

            boolean isCooking = recipe instanceof AbstractCookingRecipe || isCookingRecipeType(recipe);
            entries.add(new RecipeEntry(resultItem, nonEmpty, result.getCount(), isCooking, holder.id()));
        }

        LOGGER.info("[SpiceOfLife] Found {} non-special recipes across types: {}", entries.size(), recipeTypesFound);

        int resolved = 0;
        for (int pass = 0; pass < 10; pass++) {
            boolean progress = false;
            for (RecipeEntry entry : entries) {
                if (cache.containsKey(entry.output)) continue;

                NutritionalProfile sum = NutritionalProfile.EMPTY;
                boolean hasUnresolved = false;
                int nutritionalIngredientCount = 0;

                for (Ingredient ing : entry.ingredients) {
                    ResolveResult r = resolveIngredient(ing);
                    if (r == ResolveResult.UNRESOLVED) {
                        hasUnresolved = true;
                        break;
                    }
                    if (r.profile != null && !r.profile.isEmpty()) {
                        sum = sum.add(r.profile);
                        nutritionalIngredientCount++;
                    }
                }

                if (hasUnresolved) continue;
                if (nutritionalIngredientCount == 0) continue;

                NutritionalProfile result = sum.divide(entry.outputCount);

                if (entry.cooking) {
                    result = new NutritionalProfile(
                            result.sugar(), result.fat(), result.salt(),
                            result.protein() * 1.1f, result.fiber(),
                            result.hydration() * 0.8f, result.vitamins() * 0.9f
                    );
                }

                cache.put(entry.output, result);
                progress = true;
                resolved++;
            }
            if (!progress) break;
        }

        for (Map.Entry<Item, NutritionalProfile> e : cache.entrySet()) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(e.getKey());
            if (key != null) {
                cacheByKey.put(key, e.getValue());
            }
        }

        LOGGER.info("[SpiceOfLife] Nutrition computation done: {} total ({} from data maps, {} from recipes)",
                cache.size(), dataMapCount, resolved);
    }

    private ResolveResult resolveIngredient(Ingredient ingredient) {
        ItemStack[] items = ingredient.getItems();
        if (items.length == 0) return ResolveResult.UNRESOLVED;

        boolean anyIsNutritional = false;
        NutritionalProfile sum = NutritionalProfile.EMPTY;
        int count = 0;

        for (ItemStack stack : items) {
            NutritionalProfile p = cache.get(stack.getItem());
            if (p != null) {
                sum = sum.add(p);
                count++;
            }
            if (stack.get(DataComponents.FOOD) != null) {
                anyIsNutritional = true;
            }
        }

        if (count > 0) {
            anyIsNutritional = true;
            return new ResolveResult(sum.divide(count));
        }

        if (!anyIsNutritional) {
            return ResolveResult.TOOL;
        }

        return ResolveResult.UNRESOLVED;
    }

    private static final Set<String> COOKING_RECIPE_TYPES = Set.of(
            "farmersdelight:cooking",
            "minecraft:smelting",
            "minecraft:smoking",
            "minecraft:campfire_cooking",
            "minecraft:blasting"
    );

    private boolean isCookingRecipeType(Recipe<?> recipe) {
        ResourceLocation typeId = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
        return typeId != null && COOKING_RECIPE_TYPES.contains(typeId.toString());
    }

    private record RecipeEntry(Item output, List<Ingredient> ingredients, int outputCount, boolean cooking,
                                ResourceLocation id) {}

    private record ResolveResult(NutritionalProfile profile) {
        static final ResolveResult UNRESOLVED = new ResolveResult(null);
        static final ResolveResult TOOL = new ResolveResult(NutritionalProfile.EMPTY);
    }
}
