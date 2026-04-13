package com.leclowndu93150.spiceoflifehealthedition;

import com.leclowndu93150.spiceoflifehealthedition.command.ModCommands;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietAttachment;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietTracker;
import com.leclowndu93150.spiceoflifehealthedition.item.ModItems;
import com.leclowndu93150.spiceoflifehealthedition.network.NutritionSyncPayload;
import com.leclowndu93150.spiceoflifehealthedition.nutrition.ModDataMaps;
import com.leclowndu93150.spiceoflifehealthedition.nutrition.NutritionManager;
import com.leclowndu93150.spiceoflifehealthedition.nutrition.NutritionReloadListener;
import com.leclowndu93150.spiceoflifehealthedition.trait.TraitEvaluator;
import com.leclowndu93150.spiceoflifehealthedition.trait.TraitTickHandler;
import com.leclowndu93150.spiceoflifehealthedition.weight.ExerciseTracker;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(SpiceOfLifeHealthEdition.MODID)
public class SpiceOfLifeHealthEdition {

    public static final String MODID = "spiceoflifehealthedition";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.spiceoflifehealthedition"))
                    .withTabsBefore(CreativeModeTabs.FOOD_AND_DRINKS)
                    .icon(() -> ModItems.GUIDEBOOK.get().getDefaultInstance())
                    .displayItems((parameters, output) -> output.accept(ModItems.GUIDEBOOK.get()))
                    .build());

    public SpiceOfLifeHealthEdition(IEventBus modEventBus, ModContainer modContainer) {
        CREATIVE_MODE_TABS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        DietAttachment.ATTACHMENT_TYPES.register(modEventBus);

        modEventBus.addListener(ModDataMaps::register);
        modEventBus.addListener(NutritionSyncPayload::registerPayloads);

        NeoForge.EVENT_BUS.addListener(DietTracker::onItemUseFinish);
        NeoForge.EVENT_BUS.addListener(DietTracker::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(TraitEvaluator::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(TraitEvaluator::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(TraitEvaluator::onPlayerRespawn);
        NeoForge.EVENT_BUS.addListener(TraitTickHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(ExerciseTracker::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(ExerciseTracker::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(ExerciseTracker::onItemFished);
        NeoForge.EVENT_BUS.addListener(ExerciseTracker::onLivingDeath);
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> {
            event.addListener(new NutritionReloadListener(event.getServerResources(), event.getRegistryAccess()));
        });
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> {
            LOGGER.info("[SpiceOfLife] ServerStarted - running nutrition recompute");
            NutritionManager.get().recompute(
                    event.getServer().getRecipeManager(),
                    event.getServer().registryAccess()
            );
        });
        NeoForge.EVENT_BUS.addListener(NutritionSyncPayload::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(NutritionSyncPayload::onDatapackReload);
        NeoForge.EVENT_BUS.addListener(ModCommands::register);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
