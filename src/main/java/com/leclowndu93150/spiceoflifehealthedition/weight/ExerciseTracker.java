package com.leclowndu93150.spiceoflifehealthedition.weight;

import com.leclowndu93150.spiceoflifehealthedition.Config;
import com.leclowndu93150.spiceoflifehealthedition.SpiceOfLifeHealthEdition;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietAttachment;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietHistory;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExerciseTracker {

    private static final ResourceLocation WEIGHT_SPEED = ResourceLocation.fromNamespaceAndPath(SpiceOfLifeHealthEdition.MODID, "weight.speed");
    private static final ResourceLocation WEIGHT_JUMP = ResourceLocation.fromNamespaceAndPath(SpiceOfLifeHealthEdition.MODID, "weight.jump");
    private static final ResourceLocation WEIGHT_GRAVITY = ResourceLocation.fromNamespaceAndPath(SpiceOfLifeHealthEdition.MODID, "weight.gravity");
    private static final ResourceLocation UNDERWEIGHT_DAMAGE = ResourceLocation.fromNamespaceAndPath(SpiceOfLifeHealthEdition.MODID, "weight.underweight_damage");
    private static final ResourceLocation UNDERWEIGHT_HEALTH = ResourceLocation.fromNamespaceAndPath(SpiceOfLifeHealthEdition.MODID, "weight.underweight_health");

    private static final float BASE_WEIGHT = 70f;
    private static final float EXERTION_SPRINT_RATE = 0.2f;
    private static final float EXERTION_JUMP_SPIKE = 3.0f;
    private static final float EXERTION_DECAY = 0.15f;
    private static final float EXERTION_BREATH_THRESHOLD = 30.0f;
    private static final float MAX_EXERTION = 100.0f;
    private static final ResourceLocation EXHAUSTED_JUMP = ResourceLocation.fromNamespaceAndPath(SpiceOfLifeHealthEdition.MODID, "weight.exhausted_jump");

    private static final Map<UUID, ExertionState> exertionStates = new HashMap<>();

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!Config.enableWeight) return;

        DietHistory history = player.getData(DietAttachment.DIET);

        if (player.isSprinting()) {
            history.addExercise(0.01f);
        }
        if (player.isSwimming()) {
            history.addExercise(0.012f);
        }
        if (player.swinging) {
            history.addExercise(0.003f);
        }

        float weight = history.getWeight();

        if (weight > BASE_WEIGHT && player.isUnderWater()) {
            float overweightFactor = (weight - BASE_WEIGHT) / 130.0f;
            int airLoss = (int) (overweightFactor * 3);
            if (airLoss > 0 && player.tickCount % 4 == 0) {
                player.setAirSupply(Math.max(-20, player.getAirSupply() - airLoss));
            }
        }
        if (weight > BASE_WEIGHT) {
            float overweightFactor = (weight - BASE_WEIGHT) / 130.0f;
            ExertionState state = exertionStates.computeIfAbsent(player.getUUID(), k -> new ExertionState());

            if (player.isSprinting()) {
                state.exertion += EXERTION_SPRINT_RATE * (1.0f + overweightFactor * 2.0f);
            }

            boolean wasOnGround = state.wasOnGround;
            if (!player.onGround() && wasOnGround) {
                state.exertion += EXERTION_JUMP_SPIKE * (1.0f + overweightFactor * 3.0f);
            }
            state.wasOnGround = player.onGround();

            if (!player.isSprinting() && player.onGround()) {
                state.exertion -= EXERTION_DECAY;
            }

            state.exertion = Math.max(0, Math.min(MAX_EXERTION, state.exertion));

            if (state.exertion >= MAX_EXERTION) {
                player.setSprinting(false);
                state.sprintCooldown = 60;
            }

            if (state.sprintCooldown > 0) {
                state.sprintCooldown--;
                player.setSprinting(false);
                setModifier(player, Attributes.JUMP_STRENGTH, EXHAUSTED_JUMP,
                        -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            } else {
                removeModifier(player, Attributes.JUMP_STRENGTH, EXHAUSTED_JUMP);
            }

            if (state.exertion > EXERTION_BREATH_THRESHOLD && player.tickCount % 4 == 0) {
                float intensity = (state.exertion - EXERTION_BREATH_THRESHOLD) / (MAX_EXERTION - EXERTION_BREATH_THRESHOLD);
                spawnBreathParticles(player, intensity);
            }
        }

        if (player.tickCount % 200 == 0) {
            float exercised = history.consumeExerciseBuffer();
            if (exercised > 0) {
                history.setWeight(history.getWeight() - exercised * 0.1f);
            }

            float currentWeight = history.getWeight();
            float drift = (BASE_WEIGHT - currentWeight) * 0.001f;
            history.setWeight(currentWeight + drift);

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

    private static void spawnBreathParticles(ServerPlayer player, float intensity) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle();
        double mouthX = player.getX() + look.x * 0.3;
        double mouthY = player.getEyeY() - 0.1;
        double mouthZ = player.getZ() + look.z * 0.3;

        int count = intensity > 0.6f ? 2 : 1;
        float spread = 0.02f + intensity * 0.03f;
        float speed = 0.01f + intensity * 0.03f;

        level.sendParticles(ParticleTypes.WHITE_SMOKE,
                mouthX, mouthY, mouthZ,
                count,
                look.x * 0.05 + spread, spread, look.z * 0.05 + spread,
                speed);
    }

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!Config.enableWeight) return;
        DietHistory history = player.getData(DietAttachment.DIET);
        history.addExercise(0.015f);
        player.setData(DietAttachment.DIET, history);
    }

    public static void onItemFished(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!Config.enableWeight) return;
        DietHistory history = player.getData(DietAttachment.DIET);
        history.addExercise(0.02f);
        player.setData(DietAttachment.DIET, history);
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            if (!Config.enableWeight) return;
            DietHistory history = player.getData(DietAttachment.DIET);
            history.addExercise(0.03f);
            player.setData(DietAttachment.DIET, history);
        }
    }

    private static class ExertionState {
        float exertion;
        int sprintCooldown;
        boolean wasOnGround = true;
    }
}
