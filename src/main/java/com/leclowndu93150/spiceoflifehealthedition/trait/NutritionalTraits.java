package com.leclowndu93150.spiceoflifehealthedition.trait;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class NutritionalTraits {

    public static final TraitDefinition MUSCULAR = new TraitDefinition(
            "muscular", Stat.PROTEIN, true, true, 0xE56B6B,
            List.of(
                    new TraitTier(1, 3.5f, List.of(
                            AttributeEffect.add(Attributes.ATTACK_DAMAGE, 1.0, "damage")
                    )),
                    new TraitTier(2, 5.0f, List.of(
                            AttributeEffect.add(Attributes.ATTACK_DAMAGE, 2.0, "damage"),
                            AttributeEffect.add(Attributes.MAX_HEALTH, 2.0, "health")
                    )),
                    new TraitTier(3, 7.0f, List.of(
                            AttributeEffect.add(Attributes.ATTACK_DAMAGE, 3.0, "damage"),
                            AttributeEffect.add(Attributes.MAX_HEALTH, 4.0, "health"),
                            AttributeEffect.add(Attributes.KNOCKBACK_RESISTANCE, 0.05, "kb")
                    ))
            )
    );

    public static final TraitDefinition MUSCLE_WASTING = new TraitDefinition(
            "muscle_wasting", Stat.PROTEIN, false, false, 0x7A5555,
            List.of(
                    new TraitTier(1, 1.0f, List.of(
                            AttributeEffect.add(Attributes.ATTACK_DAMAGE, -2.0, "damage"),
                            AttributeEffect.mulTotal(Attributes.ATTACK_SPEED, -0.1, "atkspeed")
                    ))
            )
    );

    public static final TraitDefinition ENERGY_RESERVES = new TraitDefinition(
            "energy_reserves", Stat.FAT, true, true, 0xF5E45C,
            List.of(
                    new TraitTier(1, 2.0f, List.of(
                            AttributeEffect.add(Attributes.MAX_HEALTH, 1.0, "health")
                    )),
                    new TraitTier(2, 3.5f, List.of(
                            AttributeEffect.add(Attributes.MAX_HEALTH, 1.0, "health"),
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, 0.03, "speed")
                    )),
                    new TraitTier(3, 5.0f, List.of(
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, 0.05, "speed"),
                            AttributeEffect.mulTotal(Attributes.SAFE_FALL_DISTANCE, 0.5, "fall")
                    ))
            )
    );

    public static final TraitDefinition HIGH_CHOLESTEROL = new TraitDefinition(
            "high_cholesterol", Stat.FAT, false, true, 0xCC3333,
            List.of(
                    new TraitTier(1, 5.0f, List.of(
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, -0.05, "speed")
                    )),
                    new TraitTier(2, 7.0f, List.of(
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, -0.1, "speed"),
                            AttributeEffect.add(Attributes.MAX_HEALTH, -2.0, "health")
                    )),
                    new TraitTier(3, 9.0f, List.of(
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, -0.15, "speed"),
                            AttributeEffect.add(Attributes.MAX_HEALTH, -4.0, "health")
                    ))
            )
    );

    public static final TraitDefinition EMACIATED = new TraitDefinition(
            "emaciated", Stat.FAT, false, false, 0x443322,
            List.of(
                    new TraitTier(1, 0.5f, List.of(
                            AttributeEffect.add(Attributes.MAX_HEALTH, -2.0, "health")
                    ))
            )
    );

    public static final TraitDefinition QUICK_ENERGY = new TraitDefinition(
            "quick_energy", Stat.SUGAR, true, true, 0xFFB84D,
            List.of(
                    new TraitTier(1, 2.0f, List.of(
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, 0.05, "speed")
                    )),
                    new TraitTier(2, 3.5f, List.of(
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, 0.1, "speed"),
                            AttributeEffect.mulTotal(Attributes.BLOCK_BREAK_SPEED, 0.1, "mining")
                    )),
                    new TraitTier(3, 5.0f, List.of(
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, 0.15, "speed"),
                            AttributeEffect.mulTotal(Attributes.BLOCK_BREAK_SPEED, 0.2, "mining")
                    ))
            )
    );

    public static final TraitDefinition DIABETES = new TraitDefinition(
            "diabetes", Stat.SUGAR, false, true, 0xCC6600,
            List.of(
                    new TraitTier(1, 6.0f, List.of(
                            AttributeEffect.add(Attributes.ATTACK_DAMAGE, -1.0, "damage")
                    )),
                    new TraitTier(2, 8.0f, List.of(
                            AttributeEffect.add(Attributes.ATTACK_DAMAGE, -2.0, "damage"),
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, -0.1, "speed")
                    )),
                    new TraitTier(3, 10.0f, List.of(
                            AttributeEffect.add(Attributes.ATTACK_DAMAGE, -3.0, "damage"),
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, -0.1, "speed")
                    ), TickBehavior.every(600, (player, level) -> {
                        player.hurt(player.damageSources().magic(), 1.0f);
                    }))
            )
    );

    public static final TraitDefinition BALANCED_ELECTROLYTES = new TraitDefinition(
            "balanced_electrolytes", Stat.SALT, true, true, 0xE4E4E4,
            List.of(
                    new TraitTier(1, 1.0f, List.of()),
                    new TraitTier(2, 2.0f, List.of(
                            AttributeEffect.add(Attributes.MAX_HEALTH, 1.0, "health")
                    )),
                    new TraitTier(3, 3.0f, List.of(
                            AttributeEffect.add(Attributes.MAX_HEALTH, 2.0, "health"),
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, 0.03, "speed")
                    ))
            )
    );

    public static final TraitDefinition HYPERTENSION = new TraitDefinition(
            "hypertension", Stat.SALT, false, true, 0xAA3333,
            List.of(
                    new TraitTier(1, 4.0f, List.of(
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, -0.05, "speed")
                    )),
                    new TraitTier(2, 6.0f, List.of(
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, -0.1, "speed"),
                            AttributeEffect.add(Attributes.MAX_HEALTH, -2.0, "health")
                    ))
            )
    );

    public static final TraitDefinition LOW_SODIUM = new TraitDefinition(
            "low_sodium", Stat.SALT, false, false, 0x667788,
            List.of(
                    new TraitTier(1, 0.3f, List.of(
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, -0.05, "speed"),
                            AttributeEffect.mulTotal(Attributes.BLOCK_BREAK_SPEED, -0.1, "mining")
                    ))
            )
    );

    public static final TraitDefinition HEALTHY_GUT = new TraitDefinition(
            "healthy_gut", Stat.FIBER, true, true, 0x7BD86E,
            List.of(
                    new TraitTier(1, 2.5f, List.of()),
                    new TraitTier(2, 4.0f, List.of(
                            AttributeEffect.add(Attributes.MAX_HEALTH, 1.0, "health")
                    )),
                    new TraitTier(3, 6.0f, List.of(
                            AttributeEffect.add(Attributes.MAX_HEALTH, 2.0, "health")
                    ))
            )
    );

    public static final TraitDefinition INDIGESTION = new TraitDefinition(
            "indigestion", Stat.FIBER, false, false, 0x6B5B45,
            List.of(
                    new TraitTier(1, 1.0f, List.of(),
                            TickBehavior.every(60, (player, level) -> player.causeFoodExhaustion(0.5f)))
            )
    );

    public static final TraitDefinition WELL_HYDRATED = new TraitDefinition(
            "well_hydrated", Stat.HYDRATION, true, true, 0x66CFE4,
            List.of(
                    new TraitTier(1, 3.0f, List.of(), TickBehavior.every(100, (player, level) -> {
                        if (player.getHealth() < player.getMaxHealth()) {
                            player.heal(0.5f);
                        }
                    })),
                    new TraitTier(2, 5.0f, List.of(
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, 0.03, "speed")
                    ), TickBehavior.every(60, (player, level) -> {
                        if (player.getHealth() < player.getMaxHealth()) {
                            player.heal(0.5f);
                        }
                    })),
                    new TraitTier(3, 7.0f, List.of(
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, 0.05, "speed")
                    ), TickBehavior.every(40, (player, level) -> {
                        if (player.getHealth() < player.getMaxHealth()) {
                            player.heal(0.5f);
                        }
                        if (player.isUnderWater()) {
                            player.setAirSupply(Math.min(player.getMaxAirSupply(), player.getAirSupply() + 4));
                        }
                    }))
            )
    );

    public static final TraitDefinition DEHYDRATION = new TraitDefinition(
            "dehydration", Stat.HYDRATION, false, false, 0xCC9900,
            List.of(
                    new TraitTier(1, 1.0f, List.of(),
                            TickBehavior.every(40, (player, level) -> player.causeFoodExhaustion(0.5f))),
                    new TraitTier(2, 0.5f, List.of(
                            AttributeEffect.mulTotal(Attributes.MOVEMENT_SPEED, -0.1, "speed"),
                            AttributeEffect.mulTotal(Attributes.ATTACK_SPEED, -0.1, "atkspeed")
                    ), TickBehavior.every(30, (player, level) -> player.causeFoodExhaustion(1.0f)))
            )
    );

    public static final TraitDefinition VITALITY = new TraitDefinition(
            "vitality", Stat.VITAMINS, true, true, 0xD072E0,
            List.of(
                    new TraitTier(1, 3.0f, List.of(), TickBehavior.every(100, (player, level) -> {
                        if (player.getHealth() < player.getMaxHealth()) {
                            player.heal(0.5f);
                        }
                    })),
                    new TraitTier(2, 5.0f, List.of(), TickBehavior.every(60, (player, level) -> {
                        if (player.getHealth() < player.getMaxHealth()) {
                            player.heal(1.0f);
                        }
                    })),
                    new TraitTier(3, 7.0f, List.of(
                            AttributeEffect.add(Attributes.MAX_HEALTH, 2.0, "health")
                    ), TickBehavior.every(40, (player, level) -> {
                        if (player.getHealth() < player.getMaxHealth()) {
                            player.heal(1.0f);
                        }
                    }))
            )
    );

    public static final TraitDefinition SCURVY = new TraitDefinition(
            "scurvy", Stat.VITAMINS, false, false, 0x557733,
            List.of(
                    new TraitTier(1, 0.5f, List.of(
                            AttributeEffect.add(Attributes.MAX_HEALTH, -2.0, "health")
                    )),
                    new TraitTier(2, 0.3f, List.of(
                            AttributeEffect.add(Attributes.MAX_HEALTH, -2.0, "health"),
                            AttributeEffect.add(Attributes.ATTACK_DAMAGE, -2.0, "damage")
                    ))
            )
    );

    public static final List<TraitDefinition> ALL = List.of(
            MUSCULAR, MUSCLE_WASTING,
            ENERGY_RESERVES, HIGH_CHOLESTEROL, EMACIATED,
            QUICK_ENERGY, DIABETES,
            BALANCED_ELECTROLYTES, HYPERTENSION, LOW_SODIUM,
            HEALTHY_GUT, INDIGESTION,
            WELL_HYDRATED, DEHYDRATION,
            VITALITY, SCURVY
    );

    public static TraitDefinition byId(String id) {
        for (TraitDefinition def : ALL) {
            if (def.id().equals(id)) return def;
        }
        return null;
    }
}
