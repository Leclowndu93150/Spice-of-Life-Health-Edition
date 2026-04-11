package com.leclowndu93150.spiceoflifehealthedition.diet;

import com.leclowndu93150.spiceoflifehealthedition.api.NutritionalProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record DietEntry(ResourceLocation foodId, NutritionalProfile nutrition, long gameTime) {

    public static final Codec<DietEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("food").forGetter(DietEntry::foodId),
            NutritionalProfile.CODEC.fieldOf("nutrition").forGetter(DietEntry::nutrition),
            Codec.LONG.fieldOf("time").forGetter(DietEntry::gameTime)
    ).apply(i, DietEntry::new));
}
