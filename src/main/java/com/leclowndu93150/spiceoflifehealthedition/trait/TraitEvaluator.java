package com.leclowndu93150.spiceoflifehealthedition.trait;

import com.leclowndu93150.spiceoflifehealthedition.Config;
import com.leclowndu93150.spiceoflifehealthedition.SpiceOfLifeHealthEdition;
import com.leclowndu93150.spiceoflifehealthedition.api.NutritionalProfile;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietAttachment;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietHistory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;

public class TraitEvaluator {

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % Config.conditionCheckInterval != 0) return;

        DietHistory history = player.getData(DietAttachment.DIET);
        NutritionalProfile avg = history.getAverage();

        Map<String, Integer> newActive = computeActive(avg);
        Map<String, Integer> oldActive = new HashMap<>(history.getActiveTraits());

        if (!newActive.equals(oldActive)) {
            for (Map.Entry<String, Integer> old : oldActive.entrySet()) {
                Integer newLevel = newActive.get(old.getKey());
                if (newLevel == null || !newLevel.equals(old.getValue())) {
                    removeModifiers(player, old.getKey());
                }
            }
            for (Map.Entry<String, Integer> next : newActive.entrySet()) {
                Integer oldLevel = oldActive.get(next.getKey());
                if (oldLevel == null || !oldLevel.equals(next.getValue())) {
                    TraitDefinition def = NutritionalTraits.byId(next.getKey());
                    if (def != null) {
                        TraitTier tier = tierByLevel(def, next.getValue());
                        if (tier != null) {
                            applyModifiers(player, def, tier);
                        }
                    }
                }
            }

            history.setActiveTraits(newActive);
            player.setData(DietAttachment.DIET, history);
        }
    }

    public static Map<String, Integer> computeActive(NutritionalProfile avg) {
        Map<Stat, TraitDefinition> bestPositive = new HashMap<>();
        Map<Stat, TraitDefinition> bestNegative = new HashMap<>();
        Map<TraitDefinition, TraitTier> tiers = new HashMap<>();

        for (TraitDefinition def : NutritionalTraits.ALL) {
            float value = def.stat().get(avg);
            TraitTier tier = def.getTierForValue(value);
            if (tier == null) continue;

            tiers.put(def, tier);

            if (def.positive()) {
                bestPositive.put(def.stat(), def);
            } else {
                TraitDefinition existing = bestNegative.get(def.stat());
                if (existing == null || tier.level() > tiers.get(existing).level()) {
                    bestNegative.put(def.stat(), def);
                }
            }
        }

        Map<String, Integer> result = new HashMap<>();
        for (Stat stat : Stat.values()) {
            TraitDefinition positive = bestPositive.get(stat);
            TraitDefinition negative = bestNegative.get(stat);
            if (positive != null && negative == null) {
                result.put(positive.id(), tiers.get(positive).level());
            } else if (negative != null) {
                result.put(negative.id(), tiers.get(negative).level());
            }
        }
        return result;
    }

    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        DietHistory history = player.getData(DietAttachment.DIET);
        reapplyAll(player, history.getActiveTraits());
    }

    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        DietHistory history = player.getData(DietAttachment.DIET);
        reapplyAll(player, history.getActiveTraits());
    }

    public static void reapplyAll(ServerPlayer player, Map<String, Integer> active) {
        removeAllModifiers(player);
        for (Map.Entry<String, Integer> entry : active.entrySet()) {
            TraitDefinition def = NutritionalTraits.byId(entry.getKey());
            if (def == null) continue;
            TraitTier tier = tierByLevel(def, entry.getValue());
            if (tier != null) {
                applyModifiers(player, def, tier);
            }
        }
    }

    public static void applyModifiers(ServerPlayer player, TraitDefinition def, TraitTier tier) {
        for (AttributeEffect effect : tier.attributes()) {
            AttributeInstance instance = player.getAttribute(effect.attribute());
            if (instance == null) continue;
            ResourceLocation id = modifierId(def.id(), effect.suffix());
            instance.removeModifier(id);
            instance.addTransientModifier(new AttributeModifier(id, effect.value(), effect.operation()));
        }
    }

    public static void removeModifiers(ServerPlayer player, String traitId) {
        TraitDefinition def = NutritionalTraits.byId(traitId);
        if (def == null) return;
        for (TraitTier tier : def.tiers()) {
            for (AttributeEffect effect : tier.attributes()) {
                AttributeInstance instance = player.getAttribute(effect.attribute());
                if (instance == null) continue;
                instance.removeModifier(modifierId(def.id(), effect.suffix()));
            }
        }
    }

    public static void removeAllModifiers(ServerPlayer player) {
        for (TraitDefinition def : NutritionalTraits.ALL) {
            removeModifiers(player, def.id());
        }
    }

    private static TraitTier tierByLevel(TraitDefinition def, int level) {
        for (TraitTier tier : def.tiers()) {
            if (tier.level() == level) return tier;
        }
        return null;
    }

    private static ResourceLocation modifierId(String traitId, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(SpiceOfLifeHealthEdition.MODID, "trait/" + traitId + "/" + suffix);
    }
}
