package com.leclowndu93150.spiceoflifehealthedition.trait;

import java.util.List;

public record TraitDefinition(
        String id,
        Stat stat,
        boolean positive,
        boolean triggerOnHigh,
        int color,
        List<TraitTier> tiers
) {
    public TraitTier getTierForValue(float value) {
        TraitTier match = null;
        for (TraitTier tier : tiers) {
            if (triggerOnHigh) {
                if (value >= tier.threshold() && (match == null || tier.threshold() > match.threshold())) {
                    match = tier;
                }
            } else {
                if (value <= tier.threshold() && (match == null || tier.threshold() < match.threshold())) {
                    match = tier;
                }
            }
        }
        return match;
    }
}
