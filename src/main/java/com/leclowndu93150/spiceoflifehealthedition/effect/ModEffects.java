package com.leclowndu93150.spiceoflifehealthedition.effect;

import com.leclowndu93150.spiceoflifehealthedition.SpiceOfLifeHealthEdition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, SpiceOfLifeHealthEdition.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> DIABETES = MOB_EFFECTS.register("diabetes",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0xCC6600) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            id("diabetes.speed"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                            id("diabetes.damage"), -2, AttributeModifier.Operation.ADD_VALUE));

    public static final DeferredHolder<MobEffect, MobEffect> HIGH_CHOLESTEROL = MOB_EFFECTS.register("high_cholesterol",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0xCC3333) {}
                    .addAttributeModifier(Attributes.MAX_HEALTH,
                            id("cholesterol.health"), -4, AttributeModifier.Operation.ADD_VALUE)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            id("cholesterol.speed"), -0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final DeferredHolder<MobEffect, MobEffect> FATIGUE = MOB_EFFECTS.register("fatigue",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x666666) {}
                    .addAttributeModifier(Attributes.ATTACK_SPEED,
                            id("fatigue.atkspeed"), -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                            id("fatigue.damage"), -1, AttributeModifier.Operation.ADD_VALUE));

    public static final DeferredHolder<MobEffect, MobEffect> DEHYDRATION = MOB_EFFECTS.register("dehydration",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0xCC9900) {
                @Override
                public boolean applyEffectTick(LivingEntity entity, int amplifier) {
                    if (entity instanceof Player player) {
                        player.causeFoodExhaustion(0.5f * (1 + amplifier));
                    }
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return duration % 40 == 0;
                }
            });

    public static final DeferredHolder<MobEffect, MobEffect> SCURVY = MOB_EFFECTS.register("scurvy",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x339933) {}
                    .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                            id("scurvy.damage"), -3, AttributeModifier.Operation.ADD_VALUE)
                    .addAttributeModifier(Attributes.MAX_HEALTH,
                            id("scurvy.health"), -2, AttributeModifier.Operation.ADD_VALUE));

    public static final DeferredHolder<MobEffect, MobEffect> OBESITY_EFFECT = MOB_EFFECTS.register("obesity",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x996633) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            id("obesity.speed"), -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.ATTACK_SPEED,
                            id("obesity.atkspeed"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final DeferredHolder<MobEffect, MobEffect> WELL_FED = MOB_EFFECTS.register("well_fed",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x00CC00) {}
                    .addAttributeModifier(Attributes.MAX_HEALTH,
                            id("wellfed.health"), 4, AttributeModifier.Operation.ADD_VALUE));

    public static final DeferredHolder<MobEffect, MobEffect> ENERGIZED = MOB_EFFECTS.register("energized",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFFCC00) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            id("energized.speed"), 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.ATTACK_SPEED,
                            id("energized.atkspeed"), 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final DeferredHolder<MobEffect, MobEffect> IRON_STOMACH = MOB_EFFECTS.register("iron_stomach",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x009999) {});

    public static final DeferredHolder<MobEffect, MobEffect> VITALITY = MOB_EFFECTS.register("vitality",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF6699) {
                @Override
                public boolean applyEffectTick(LivingEntity entity, int amplifier) {
                    if (entity.getHealth() < entity.getMaxHealth()) {
                        entity.heal(1.0f);
                    }
                    return true;
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return duration % 50 == 0;
                }
            });

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(SpiceOfLifeHealthEdition.MODID, path);
    }
}
