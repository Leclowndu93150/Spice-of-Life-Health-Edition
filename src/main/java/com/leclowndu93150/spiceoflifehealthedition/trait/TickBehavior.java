package com.leclowndu93150.spiceoflifehealthedition.trait;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface TickBehavior {
    void tick(ServerPlayer player, int level);

    default int intervalTicks() {
        return 20;
    }

    static TickBehavior every(int intervalTicks, TickBehavior action) {
        return new TickBehavior() {
            @Override
            public void tick(ServerPlayer player, int level) {
                action.tick(player, level);
            }

            @Override
            public int intervalTicks() {
                return intervalTicks;
            }
        };
    }
}
