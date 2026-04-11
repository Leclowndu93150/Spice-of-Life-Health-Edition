package com.leclowndu93150.spiceoflifehealthedition;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = SpiceOfLifeHealthEdition.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue DIET_HISTORY_SIZE = BUILDER
            .defineInRange("dietHistorySize", 50, 10, 500);

    private static final ModConfigSpec.IntValue CONDITION_CHECK_INTERVAL = BUILDER
            .defineInRange("conditionCheckIntervalTicks", 200, 20, 2000);

    private static final ModConfigSpec.DoubleValue CONDITION_THRESHOLD_MULTIPLIER = BUILDER
            .defineInRange("conditionThresholdMultiplier", 1.0, 0.1, 10.0);

    private static final ModConfigSpec.BooleanValue ENABLE_DIABETES = BUILDER.define("conditions.diabetes", true);
    private static final ModConfigSpec.BooleanValue ENABLE_CHOLESTEROL = BUILDER.define("conditions.highCholesterol", true);
    private static final ModConfigSpec.BooleanValue ENABLE_FATIGUE = BUILDER.define("conditions.fatigue", true);
    private static final ModConfigSpec.BooleanValue ENABLE_DEHYDRATION = BUILDER.define("conditions.dehydration", true);
    private static final ModConfigSpec.BooleanValue ENABLE_SCURVY = BUILDER.define("conditions.scurvy", true);
    private static final ModConfigSpec.BooleanValue ENABLE_OBESITY = BUILDER.define("conditions.obesity", true);
    private static final ModConfigSpec.BooleanValue ENABLE_REWARDS = BUILDER.define("rewards.enabled", true);
    private static final ModConfigSpec.BooleanValue ENABLE_WEIGHT = BUILDER.define("weight.enabled", true);

    private static final ModConfigSpec.IntValue TOOLTIP_DETAIL = BUILDER
            .defineInRange("tooltipDetail", 1, 0, 2);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int dietHistorySize;
    public static int conditionCheckInterval;
    public static double conditionThresholdMultiplier;
    public static boolean enableDiabetes;
    public static boolean enableCholesterol;
    public static boolean enableFatigue;
    public static boolean enableDehydration;
    public static boolean enableScurvy;
    public static boolean enableObesity;
    public static boolean enableRewards;
    public static boolean enableWeight;
    public static int tooltipDetail;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        dietHistorySize = DIET_HISTORY_SIZE.get();
        conditionCheckInterval = CONDITION_CHECK_INTERVAL.get();
        conditionThresholdMultiplier = CONDITION_THRESHOLD_MULTIPLIER.get();
        enableDiabetes = ENABLE_DIABETES.get();
        enableCholesterol = ENABLE_CHOLESTEROL.get();
        enableFatigue = ENABLE_FATIGUE.get();
        enableDehydration = ENABLE_DEHYDRATION.get();
        enableScurvy = ENABLE_SCURVY.get();
        enableObesity = ENABLE_OBESITY.get();
        enableRewards = ENABLE_REWARDS.get();
        enableWeight = ENABLE_WEIGHT.get();
        tooltipDetail = TOOLTIP_DETAIL.get();
    }
}
