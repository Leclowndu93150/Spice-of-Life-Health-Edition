package com.leclowndu93150.spiceoflifehealthedition.nutrition;

import com.leclowndu93150.spiceoflifehealthedition.api.NutritionalProfile;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import java.util.Optional;

public class SpecialNutrition {

    public static final NutritionalProfile WATER_BOTTLE = new NutritionalProfile(0, 0, 0, 0, 0, 6, 0);
    public static final ResourceLocation WATER_BOTTLE_ID = ResourceLocation.fromNamespaceAndPath("minecraft", "water_bottle");

    public static NutritionalProfile getOverride(ItemStack stack) {
        if (stack.is(Items.POTION)) {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents != null) {
                Optional<Holder<Potion>> potion = contents.potion();
                if (potion.isPresent() && potion.get().is(Potions.WATER.getKey())) {
                    return WATER_BOTTLE;
                }
            }
        }
        return null;
    }

    public static ResourceLocation getOverrideId(ItemStack stack) {
        if (stack.is(Items.POTION)) {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents != null) {
                Optional<Holder<Potion>> potion = contents.potion();
                if (potion.isPresent() && potion.get().is(Potions.WATER.getKey())) {
                    return WATER_BOTTLE_ID;
                }
            }
        }
        return null;
    }
}
