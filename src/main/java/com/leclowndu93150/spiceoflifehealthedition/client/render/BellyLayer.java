package com.leclowndu93150.spiceoflifehealthedition.client.render;

import com.leclowndu93150.spiceoflifehealthedition.diet.DietAttachment;
import com.leclowndu93150.spiceoflifehealthedition.diet.DietHistory;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class BellyLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final float BASELINE_WEIGHT = 70.0F;
    private static final float MAX_EXTRA_WEIGHT = 130.0F;

    private static final float SPRING = 0.3F;
    private static final float DAMPING = 0.65F;

    private final BellyModel bellyModel;
    private final Map<UUID, JiggleState> jiggleStates = new HashMap<>();

    public BellyLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet modelSet) {
        super(parent);
        this.bellyModel = new BellyModel(modelSet.bakeLayer(ClientEvents.BELLY_LAYER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player.isInvisible()) return;

        DietHistory history = player.getData(DietAttachment.DIET);
        float weight = history.getWeight();
        if (weight <= BASELINE_WEIGHT) return;

        float bellyScale = Math.min((weight - BASELINE_WEIGHT) / MAX_EXTRA_WEIGHT, 1.0F);

        JiggleState state = jiggleStates.computeIfAbsent(player.getUUID(), k -> new JiggleState());
        state.update(player, partialTick);

        float jiggleX = Mth.lerp(partialTick, state.prevJiggleX, state.jiggleX);
        float jiggleY = Mth.lerp(partialTick, state.prevJiggleY, state.jiggleY);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityTranslucent(player.getSkin().texture()));
        bellyModel.render(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, getParentModel(), bellyScale, jiggleX, jiggleY);
    }

    private static class JiggleState {
        float jiggleX, jiggleY;
        float prevJiggleX, prevJiggleY;
        float velocityX, velocityY;
        double lastMotionX, lastMotionY, lastMotionZ;
        float lastYRot;
        boolean initialized;

        void update(AbstractClientPlayer player, float partialTick) {
            double motionX = player.getX() - player.xOld;
            double motionY = player.getY() - player.yOld;
            double motionZ = player.getZ() - player.zOld;
            float yRot = player.getYRot();

            if (!initialized) {
                lastMotionX = motionX;
                lastMotionY = motionY;
                lastMotionZ = motionZ;
                lastYRot = yRot;
                initialized = true;
                return;
            }

            float deltaForward = (float) (motionX - lastMotionX) + (float) (motionZ - lastMotionZ);
            float deltaY = (float) (motionY - lastMotionY);
            float deltaYaw = Mth.wrapDegrees(yRot - lastYRot) * 0.05F;

            float forceX = -deltaForward * 4.0F - deltaYaw * 2.0F;
            float forceY = -deltaY * 6.0F;

            if (player.onGround() && Math.abs(motionY) < 0.01) {
                float walkBounce = Mth.sin(player.walkAnimation.position() * 0.6662F) * player.walkAnimation.speed() * 1.5F;
                forceY += walkBounce;
            }

            if (player.isSprinting()) {
                forceX *= 1.5F;
                forceY *= 1.3F;
            }

            prevJiggleX = jiggleX;
            prevJiggleY = jiggleY;

            velocityX += forceX;
            velocityY += forceY;

            velocityX += -jiggleX * SPRING;
            velocityY += -jiggleY * SPRING;

            velocityX *= DAMPING;
            velocityY *= DAMPING;

            jiggleX += velocityX;
            jiggleY += velocityY;

            jiggleX = Mth.clamp(jiggleX, -3.0F, 3.0F);
            jiggleY = Mth.clamp(jiggleY, -3.0F, 3.0F);

            lastMotionX = motionX;
            lastMotionY = motionY;
            lastMotionZ = motionZ;
            lastYRot = yRot;
        }
    }
}
