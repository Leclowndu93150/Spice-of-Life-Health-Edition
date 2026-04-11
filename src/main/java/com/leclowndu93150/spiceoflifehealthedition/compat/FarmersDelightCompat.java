package com.leclowndu93150.spiceoflifehealthedition.compat;

import net.neoforged.fml.ModList;

public class FarmersDelightCompat {

    public static boolean isLoaded() {
        return ModList.get().isLoaded("farmersdelight");
    }
}
