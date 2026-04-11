package com.leclowndu93150.spiceoflifehealthedition.weight;

import com.leclowndu93150.spiceoflifehealthedition.Config;
import com.leclowndu93150.spiceoflifehealthedition.SpiceOfLifeHealthEdition;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietAttachment;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietHistory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class ExerciseTracker {

    private static final ResourceLocation WEIGHT_SPEED = ResourceLocation.fromNamespaceAndPath(SpiceOfLifeHealthEdition.MODID, "weight.speed");
    private static final ResourceLocation WEIGHT_JUMP = ResourceLocation.fromNamespaceAndPath(SpiceOfLifeHealthEdition.MODID, "weight.jump");
    private static final ResourceLocation WEIGHT_GRAVITY = ResourceLocation.fromNamespaceAndPath(SpiceOfLifeHealthEdition.MODID, "weight.gravity");
    private static final ResourceLocation UNDERWEIGHT_DAMAGE = ResourceLocation.fromNamespaceAndPath(SpiceOfLifeHealthEdition.MODID, "weight.underweight_damage");
    private static final ResourceLocation UNDERWEIGHT_HEALTH = ResourceLocation.fromNamespaceAndPath(SpiceOfLifeHealthEdition.MODID, "weight.underweight_health");

    private static final float BASE_WEIGHT = 70f;

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!Config.enableWeight) return;

        DietHistory history = player.getData(DietAttachment.DIET);

        if (player.isSprinting()) {
            history.addExercise(0.01f);
        }
        if (player.isSwimming()) {
            history.addExercise(0.008f);
        }

        if (player.tickCount % 200 == 0) {
            float exercised = history.consumeExerciseBuffer();
            if (exercised > 0) {
                history.setWeight(history.getWeight() - exercised * 0.1f);
            }

            float weight = history.getWeight();
            float drift = (BASE_WEIGHT - weight) * 0.001f;
            history.setWeight(weight + drift);

            player.setData(DietAttachment.DIET, history);

            updateWeightModifiers(player, history.getWeight());
        }
    }

    public static void updateWeightModifiers(ServerPlayer player, float weight) {
        float delta = weight - BASE_WEIGHT;

        setModifier(player, Attributes.MOVEMENT_SPEED, WEIGHT_SPEED,
                -delta * 0.002, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        setModifier(player, Attributes.JUMP_STRENGTH, WEIGHT_JUMP,
                -delta * 0.001, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        setModifier(player, Attributes.GRAVITY, WEIGHT_GRAVITY,
                delta * 0.0005, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        if (weight < 60f) {
            setModifier(player, Attributes.ATTACK_DAMAGE, UNDERWEIGHT_DAMAGE,
                    -1, AttributeModifier.Operation.ADD_VALUE);
            setModifier(player, Attributes.MAX_HEALTH, UNDERWEIGHT_HEALTH,
                    -2, AttributeModifier.Operation.ADD_VALUE);
        } else {
            removeModifier(player, Attributes.ATTACK_DAMAGE, UNDERWEIGHT_DAMAGE);
            removeModifier(player, Attributes.MAX_HEALTH, UNDERWEIGHT_HEALTH);
        }
    }

    private static void setModifier(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                     ResourceLocation id, double value, AttributeModifier.Operation op) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        if (value != 0) {
            instance.addTransientModifier(new AttributeModifier(id, value, op));
        }
    }

    private static void removeModifier(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                        ResourceLocation id) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }
}
