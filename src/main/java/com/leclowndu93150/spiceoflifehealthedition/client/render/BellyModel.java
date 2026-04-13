package com.leclowndu93150.spiceoflifehealthedition.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BellyModel {

    private final ModelPart belly;

    public BellyModel(ModelPart root) {
        this.belly = root.getChild("belly");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("belly",
                CubeListBuilder.create()
                        .texOffs(20, 23)
                        .addBox(-3.5F, 0.0F, -2.5F, 7.0F, 5.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 64, 64);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                       PlayerModel<AbstractClientPlayer> parentModel, float bellyScale,
                       float jiggleX, float jiggleY) {
        if (bellyScale <= 0.0F) return;

        poseStack.pushPose();
        parentModel.body.translateAndRotate(poseStack);

        float yOffset = 0.35F + jiggleY * bellyScale * 0.04F;
        float zOffset = -0.16F + jiggleX * bellyScale * 0.02F;
        poseStack.translate(0.0F, yOffset, zOffset);

        float scaleX = 1.0F + bellyScale * 0.15F + Math.abs(jiggleX) * bellyScale * 0.02F;
        float scaleY = 1.0F + bellyScale * 0.1F + jiggleY * bellyScale * 0.03F;
        float scaleZ = 1.0F + bellyScale * 0.5F + Math.abs(jiggleX) * bellyScale * 0.03F;
        poseStack.scale(scaleX, scaleY, scaleZ);

        belly.setPos(0, 0, 0);
        belly.xRot = jiggleY * bellyScale * 0.05F;
        belly.yRot = 0;
        belly.zRot = jiggleX * bellyScale * 0.03F;
        belly.render(poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }
}
