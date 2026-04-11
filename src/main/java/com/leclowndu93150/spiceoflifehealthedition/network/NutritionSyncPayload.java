package com.leclowndu93150.spiceoflifehealthedition.network;

import com.leclowndu93150.spiceoflifehealthedition.SpiceOfLifeHealthEdition;
import com.leclowndu93150.spiceoflifehealthedition.api.NutritionalProfile;
import com.leclowndu93150.spiceoflifehealthedition.client.NutritionClientCache;
import com.leclowndu93150.spiceoflifehealthedition.nutrition.NutritionManager;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public record NutritionSyncPayload(Map<ResourceLocation, NutritionalProfile> data) implements CustomPacketPayload {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Type<NutritionSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SpiceOfLifeHealthEdition.MODID, "nutrition_sync"));

    private static final StreamCodec<ByteBuf, Map<ResourceLocation, NutritionalProfile>> MAP_CODEC =
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, NutritionalProfile.STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, NutritionSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public NutritionSyncPayload decode(RegistryFriendlyByteBuf buf) {
                    return new NutritionSyncPayload(MAP_CODEC.decode(buf));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, NutritionSyncPayload payload) {
                    MAP_CODEC.encode(buf, payload.data);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(SpiceOfLifeHealthEdition.MODID).optional();
        registrar.playToClient(TYPE, STREAM_CODEC, (payload, context) -> {
            LOGGER.info("[SpiceOfLife] Client received nutrition sync: {} entries", payload.data.size());
            NutritionClientCache.update(payload.data());
        });
    }

    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LOGGER.info("[SpiceOfLife] Sending nutrition data to {} on login", player.getName().getString());
            sendToPlayer(player);
        }
    }

    public static void onDatapackReload(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            LOGGER.info("[SpiceOfLife] Sending nutrition data to {} on datapack sync", event.getPlayer().getName().getString());
            sendToPlayer(event.getPlayer());
        } else {
            LOGGER.info("[SpiceOfLife] Broadcasting nutrition data to all players on reload");
            PacketDistributor.sendToAllPlayers(new NutritionSyncPayload(NutritionManager.get().getCacheByKey()));
        }
    }

    private static void sendToPlayer(ServerPlayer player) {
        Map<ResourceLocation, NutritionalProfile> data = NutritionManager.get().getCacheByKey();
        LOGGER.info("[SpiceOfLife] Sending {} nutrition entries to {}", data.size(), player.getName().getString());
        PacketDistributor.sendToPlayer(player, new NutritionSyncPayload(data));
    }
}
