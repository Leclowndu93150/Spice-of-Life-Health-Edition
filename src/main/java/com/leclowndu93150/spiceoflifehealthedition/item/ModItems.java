package com.leclowndu93150.spiceoflifehealthedition.item;

import com.leclowndu93150.spiceoflifehealthedition.SpiceOfLifeHealthEdition;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SpiceOfLifeHealthEdition.MODID);

    public static final DeferredItem<GuidebookItem> GUIDEBOOK = ITEMS.register("guidebook",
            () -> new GuidebookItem(new Item.Properties().stacksTo(1)));
}
