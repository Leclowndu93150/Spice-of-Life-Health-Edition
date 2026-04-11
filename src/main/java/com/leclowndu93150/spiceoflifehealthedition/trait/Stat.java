package com.leclowndu93150.spiceoflifehealthedition.trait;

import com.leclowndu93150.spiceoflifehealthedition.api.NutritionalProfile;

import java.util.function.Function;

public enum Stat {
    SUGAR("sugar", NutritionalProfile::sugar),
    FAT("fat", NutritionalProfile::fat),
    SALT("salt", NutritionalProfile::salt),
    PROTEIN("protein", NutritionalProfile::protein),
    FIBER("fiber", NutritionalProfile::fiber),
    HYDRATION("hydration", NutritionalProfile::hydration),
    VITAMINS("vitamins", NutritionalProfile::vitamins);

    public final String id;
    private final Function<NutritionalProfile, Float> getter;

    Stat(String id, Function<NutritionalProfile, Float> getter) {
        this.id = id;
        this.getter = getter;
    }

    public float get(NutritionalProfile profile) {
        return getter.apply(profile);
    }
}
