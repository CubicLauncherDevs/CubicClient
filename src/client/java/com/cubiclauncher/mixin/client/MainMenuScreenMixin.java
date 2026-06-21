package com.cubiclauncher.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MainMenuScreenMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        TitleScreen screen = (TitleScreen) (Object) this;
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        poseStack.pushPose();

        int centerX = screen.width / 2;
        int centerY = screen.height / 2 - 50;

        Component titleText = Component.literal("CubicClient");
        Component byText = Component.literal("By: Cubiclauncher");

        int titleWidth = font.width(titleText);
        int byWidth = font.width(byText);

        MultiBufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        font.drawInBatch(titleText, centerX - titleWidth / 2f, centerY, 0x4CAF50, false, poseStack.last().pose(), bufferSource, true, 0, 15728880);
        font.drawInBatch(byText, centerX - byWidth / 2f, centerY + 20, 0x888888, false, poseStack.last().pose(), bufferSource, true, 0, 15728880);

        poseStack.popPose();
    }
}
