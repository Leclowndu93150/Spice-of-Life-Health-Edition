package com.leclowndu93150.spiceoflifehealthedition.trait;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record AttributeEffect(
        Holder<Attribute> attribute,
        AttributeModifier.Operation operation,
        double value,
        String suffix
) {
    public static AttributeEffect add(Holder<Attribute> attribute, double value, String suffix) {
        return new AttributeEffect(attribute, AttributeModifier.Operation.ADD_VALUE, value, suffix);
    }

    public static AttributeEffect mulTotal(Holder<Attribute> attribute, double value, String suffix) {
        return new AttributeEffect(attribute, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, value, suffix);
    }

    public static AttributeEffect mulBase(Holder<Attribute> attribute, double value, String suffix) {
        return new AttributeEffect(attribute, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, value, suffix);
    }
}
