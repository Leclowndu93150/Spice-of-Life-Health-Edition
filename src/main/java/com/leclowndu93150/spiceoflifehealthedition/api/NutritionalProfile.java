package com.leclowndu93150.spiceoflifehealthedition.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record NutritionalProfile(float sugar, float fat, float salt, float protein,
                                  float fiber, float hydration, float vitamins) {

    public static final NutritionalProfile EMPTY = new NutritionalProfile(0, 0, 0, 0, 0, 0, 0);

    public static final NutritionalProfile BASELINE = new NutritionalProfile(
            2.5f,
            2.5f,
            1.5f,
            3.0f,
            3.0f,
            3.0f,
            3.0f
    );

    public static final Codec<NutritionalProfile> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.optionalFieldOf("sugar", 0f).forGetter(NutritionalProfile::sugar),
            Codec.FLOAT.optionalFieldOf("fat", 0f).forGetter(NutritionalProfile::fat),
            Codec.FLOAT.optionalFieldOf("salt", 0f).forGetter(NutritionalProfile::salt),
            Codec.FLOAT.optionalFieldOf("protein", 0f).forGetter(NutritionalProfile::protein),
            Codec.FLOAT.optionalFieldOf("fiber", 0f).forGetter(NutritionalProfile::fiber),
            Codec.FLOAT.optionalFieldOf("hydration", 0f).forGetter(NutritionalProfile::hydration),
            Codec.FLOAT.optionalFieldOf("vitamins", 0f).forGetter(NutritionalProfile::vitamins)
    ).apply(i, NutritionalProfile::new));

    public static final StreamCodec<ByteBuf, NutritionalProfile> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public NutritionalProfile decode(ByteBuf buf) {
            return new NutritionalProfile(
                    buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
                    buf.readFloat(), buf.readFloat(), buf.readFloat()
            );
        }

        @Override
        public void encode(ByteBuf buf, NutritionalProfile profile) {
            buf.writeFloat(profile.sugar);
            buf.writeFloat(profile.fat);
            buf.writeFloat(profile.salt);
            buf.writeFloat(profile.protein);
            buf.writeFloat(profile.fiber);
            buf.writeFloat(profile.hydration);
            buf.writeFloat(profile.vitamins);
        }
    };

    public NutritionalProfile add(NutritionalProfile other) {
        return new NutritionalProfile(
                sugar + other.sugar, fat + other.fat, salt + other.salt,
                protein + other.protein, fiber + other.fiber,
                hydration + other.hydration, vitamins + other.vitamins
        );
    }

    public NutritionalProfile scale(float factor) {
        return new NutritionalProfile(
                sugar * factor, fat * factor, salt * factor,
                protein * factor, fiber * factor,
                hydration * factor, vitamins * factor
        );
    }

    public NutritionalProfile divide(int count) {
        if (count <= 0) return this;
        float f = 1f / count;
        return scale(f);
    }

    public float total() {
        return sugar + fat + salt + protein + fiber + hydration + vitamins;
    }

    public boolean isEmpty() {
        return sugar == 0 && fat == 0 && salt == 0 && protein == 0
                && fiber == 0 && hydration == 0 && vitamins == 0;
    }
}
