package com.cubiclauncher.mixin.client;

import com.cubiclauncher.module.visual.FullBrightModule;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public class LightTextureMixin {

    @Shadow @Final
    private NativeImage lightPixels;

    @Shadow @Final
    private DynamicTexture lightTexture;

    @Inject(method = "updateLightTexture", at = @At("HEAD"), cancellable = true)
    private void onUpdateLightTexture(float partialTicks, CallbackInfo ci) {
        if (FullBrightModule.isMixinActive()) {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    lightPixels.setPixelRGBA(x, y, 0xFFFFFFFF);
                }
            }
            lightTexture.upload();
            ci.cancel();
        }
    }
}
