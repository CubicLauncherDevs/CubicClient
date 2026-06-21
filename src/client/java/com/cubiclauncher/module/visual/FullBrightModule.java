package com.cubiclauncher.module.visual;

import com.cubiclauncher.module.Module;
import com.cubiclauncher.module.ModuleCategory;
import net.minecraft.client.Minecraft;

/**
 * Fullbright — Makes everything fully bright (like a fullbright texture pack)
 * while enabled, and restores the original gamma when disabled.
 *
 * Uses a mixin on LightTexture to force the lightmap to full white, and also
 * sets the gamma option to 16.0 as a fallback.
 */
public class FullBrightModule extends Module {

    private static final double BRIGHT_GAMMA   = 16.0;
    private static final double DEFAULT_GAMMA  = 1.0;

    private static boolean mixinActive = false;

    private double savedGamma = DEFAULT_GAMMA;

    public static boolean isMixinActive() {
        return mixinActive;
    }

    public FullBrightModule() {
        super("Fullbright", "Elimina la oscuridad — visión total sin importar el nivel de luz", ModuleCategory.VISUAL);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mixinActive = enabled;
        Minecraft mc = Minecraft.getInstance();
        if (enabled) {
            savedGamma = mc.options.gamma().get();
            mc.options.gamma().set(BRIGHT_GAMMA);
        } else {
            mc.options.gamma().set(savedGamma);
        }
        // Persist options so the change survives opening the pause menu
        mc.options.save();
    }
}
