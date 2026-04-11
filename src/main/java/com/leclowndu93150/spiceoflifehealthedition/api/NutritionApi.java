package com.leclowndu93150.spiceoflifehealthedition.api;

import com.leclowndu93150.spiceoflifehealthedition.diet.DietAttachment;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietHistory;
import com.leclowndu93150.spiceoflifehealthedition.nutrition.NutritionManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NutritionApi {

    public static NutritionalProfile getNutrition(Item item) {
        return NutritionManager.get().getNutrition(item);
    }

    public static NutritionalProfile getNutrition(ItemStack stack) {
        return NutritionManager.get().getNutrition(stack);
    }

    public static DietHistory getDietHistory(Player player) {
        return player.getData(DietAttachment.DIET);
    }

    public static float getWeight(Player player) {
        return player.getData(DietAttachment.DIET).getWeight();
    }
}
