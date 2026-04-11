package com.leclowndu93150.spiceoflifehealthedition.trait;

import com.leclowndu93150.spiceoflifehealthedition.diet.DietAttachment;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietHistory;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;

public class TraitTickHandler {

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        DietHistory history = player.getData(DietAttachment.DIET);
        Map<String, Integer> active = history.getActiveTraits();
        if (active.isEmpty()) return;

        for (Map.Entry<String, Integer> entry : active.entrySet()) {
            TraitDefinition def = NutritionalTraits.byId(entry.getKey());
            if (def == null) continue;
            TraitTier tier = tierByLevel(def, entry.getValue());
            if (tier == null || tier.tickBehavior() == null) continue;

            TickBehavior behavior = tier.tickBehavior();
            if (player.tickCount % behavior.intervalTicks() == 0) {
                behavior.tick(player, entry.getValue());
            }
        }
    }

    private static TraitTier tierByLevel(TraitDefinition def, int level) {
        for (TraitTier tier : def.tiers()) {
            if (tier.level() == level) return tier;
        }
        return null;
    }
}
