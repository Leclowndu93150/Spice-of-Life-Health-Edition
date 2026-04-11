package com.leclowndu93150.spiceoflifehealthedition.nutrition;

import com.leclowndu93150.spiceoflifehealthedition.SpiceOfLifeHealthEdition;
import com.leclowndu93150.spiceoflifehealthedition.api.NutritionalProfile;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

public class ModDataMaps {

    public static final DataMapType<Item, NutritionalProfile> NUTRITION = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(SpiceOfLifeHealthEdition.MODID, "nutrition"),
            Registries.ITEM,
            NutritionalProfile.CODEC
    ).synced(NutritionalProfile.CODEC, false).build();

    public static void register(RegisterDataMapTypesEvent event) {
        event.register(NUTRITION);
    }
}
