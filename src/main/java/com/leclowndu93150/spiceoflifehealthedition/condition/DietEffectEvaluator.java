package com.leclowndu93150.spiceoflifehealthedition.condition;

import com.leclowndu93150.spiceoflifehealthedition.Config;
import com.leclowndu93150.spiceoflifehealthedition.api.NutritionalProfile;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietAttachment;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietHistory;
import com.leclowndu93150.spiceoflifehealthedition.effect.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class DietEffectEvaluator {

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % Config.conditionCheckInterval != 0) return;

        DietHistory history = player.getData(DietAttachment.DIET);
        if (history.getEntryCount() < 5) return;

        NutritionalProfile avg = history.getAverage();
        double t = Config.conditionThresholdMultiplier;
        int duration = Config.conditionCheckInterval + 40;

        if (Config.enableDiabetes) {
            applyOrRemove(player, ModEffects.DIABETES, avg.sugar() > 6.0 * t, duration);
        }
        if (Config.enableCholesterol) {
            applyOrRemove(player, ModEffects.HIGH_CHOLESTEROL, avg.fat() > 5.0 * t, duration);
        }
        if (Config.enableFatigue) {
            applyOrRemove(player, ModEffects.FATIGUE,
                    avg.protein() < 1.5 / t && avg.vitamins() < 1.5 / t, duration);
        }
        if (Config.enableDehydration) {
            applyOrRemove(player, ModEffects.DEHYDRATION, avg.hydration() < 1.0 / t, duration);
        }
        if (Config.enableScurvy) {
            applyOrRemove(player, ModEffects.SCURVY, avg.vitamins() < 0.5 / t, duration);
        }
        if (Config.enableObesity) {
            applyOrRemove(player, ModEffects.OBESITY_EFFECT, avg.total() > 35.0 * t, duration);
        }

        if (Config.enableRewards) {
            int diversity = history.getDiversity();
            boolean allAbove = avg.sugar() > 1.5 && avg.fat() > 1.5 && avg.salt() > 1.5
                    && avg.protein() > 1.5 && avg.fiber() > 1.5
                    && avg.hydration() > 1.5 && avg.vitamins() > 1.5;

            applyOrRemove(player, ModEffects.WELL_FED, allAbove && diversity > 10, duration);
            applyOrRemove(player, ModEffects.ENERGIZED, avg.protein() > 3.0 && avg.vitamins() > 3.0, duration);
            applyOrRemove(player, ModEffects.IRON_STOMACH, diversity > 20, duration);
            applyOrRemove(player, ModEffects.VITALITY, avg.hydration() > 3.0 && avg.vitamins() > 3.0, duration);
        }
    }

    private static void applyOrRemove(ServerPlayer player, Holder<MobEffect> effect, boolean condition, int duration) {
        if (condition) {
            player.addEffect(new MobEffectInstance(effect, duration, 0, true, true, true));
        } else {
            player.removeEffect(effect);
        }
    }
}
