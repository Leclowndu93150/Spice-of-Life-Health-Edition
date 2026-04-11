package com.leclowndu93150.spiceoflifehealthedition.trait;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public record TraitTier(
        int level,
        float threshold,
        List<AttributeEffect> attributes,
        @Nullable TickBehavior tickBehavior
) {
    public TraitTier(int level, float threshold, List<AttributeEffect> attributes) {
        this(level, threshold, attributes, null);
    }
}
