package com.leclowndu93150.spiceoflifehealthedition.diet;

import com.leclowndu93150.spiceoflifehealthedition.api.NutritionalProfile;
import com.leclowndu93150.spiceoflifehealthedition.nutrition.ModDataMaps;
import com.leclowndu93150.spiceoflifehealthedition.nutrition.NutritionManager;
import com.leclowndu93150.spiceoflifehealthedition.nutrition.SpecialNutrition;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

public class DietTracker {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation WATER_ID = ResourceLocation.withDefaultNamespace("water");
    private static final NutritionalProfile WATER_PROFILE = new NutritionalProfile(0, 0, 0, 0, 0, 8, 0);

    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) {
            LOGGER.info("[SpiceOfLife] UseItemFinish: not a ServerPlayer ({})", entity.getClass().getSimpleName());
            return;
        }

        ItemStack stack = event.getItem();
        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        LOGGER.info("[SpiceOfLife] UseItemFinish: player={} item={}", player.getName().getString(), itemKey);

        NutritionalProfile override = SpecialNutrition.getOverride(stack);
        ResourceLocation overrideId = SpecialNutrition.getOverrideId(stack);

        NutritionalProfile profile;
        ResourceLocation foodId;

        if (override != null) {
            profile = override;
            foodId = overrideId;
            LOGGER.info("[SpiceOfLife]   Using override nutrition for {}", foodId);
        } else {
            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food == null) {
                LOGGER.info("[SpiceOfLife]   Skipped: no FoodProperties on {}", itemKey);
                return;
            }
            profile = NutritionManager.get().getNutrition(stack);
            foodId = itemKey;
            LOGGER.info("[SpiceOfLife]   NutritionManager returned profile total={} (isEmpty={})",
                    profile.total(), profile.isEmpty());

            if (profile.isEmpty()) {
                Holder<net.minecraft.world.item.Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem());
                NutritionalProfile fallback = holder.getData(ModDataMaps.NUTRITION);
                if (fallback != null) {
                    profile = fallback;
                    LOGGER.info("[SpiceOfLife]   Fallback to data map returned total={}", profile.total());
                }
            }
        }

        DietHistory history = player.getData(DietAttachment.DIET);
        history.addFood(foodId, profile, player.level().getGameTime());
        history.addWeightFromFood(profile);
        player.setData(DietAttachment.DIET, history);

        NutritionalProfile avg = history.getAverage();
        LOGGER.info("[SpiceOfLife]   History now: {} entries, avg total={}, weight={}",
                history.getEntryCount(), avg.total(), history.getWeight());
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.isShiftKeyDown()) return;
        if (!player.getMainHandItem().isEmpty()) return;

        BlockHitResult hit = event.getHitVec();
        BlockPos pos = hit.getBlockPos();
        Level level = event.getLevel();
        BlockState state = level.getBlockState(pos);

        boolean isWater = state.getFluidState().getType() == Fluids.WATER
                || state.getFluidState().getType() == Fluids.FLOWING_WATER
                || state.is(Blocks.WATER);

        if (!isWater) return;

        DietHistory history = player.getData(DietAttachment.DIET);
        history.addFood(WATER_ID, WATER_PROFILE, level.getGameTime());
        player.setData(DietAttachment.DIET, history);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5f, 1.0f);

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);

        LOGGER.debug("[SpiceOfLife] {} drank water from source at {}", player.getName().getString(), pos);
    }
}
