package com.cubiclauncher.client;

import com.cubiclauncher.client.hud.HudRenderer;
import com.cubiclauncher.client.screen.ClickGUI;
import com.cubiclauncher.module.Module;
import com.cubiclauncher.module.ModuleCategory;
import com.cubiclauncher.module.ModuleManager;
import com.cubiclauncher.module.visual.FullBrightModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class cubicClientClient implements ClientModInitializer {

    private static KeyMapping guiKey;

    // Keep a reference so we can sync gamma every tick
    private static FullBrightModule fullBright;

    @Override
    public void onInitializeClient() {
        guiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.cubic.gui",
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.cubic"
        ));

        registerModules();

        HudRenderCallback.EVENT.register(new HudRenderer());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Open/close GUI
            while (guiKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ClickGUI());
                }
            }

            // Keep fullbright gamma applied every tick so Minecraft doesn't reset it
            // (e.g. after loading a world or changing video settings)
            if (fullBright != null && fullBright.isEnabled()) {
                if (client.options.gamma().get() < 15.9) {
                    client.options.gamma().set(16.0);
                }
            }
        });
    }

    private void registerModules() {
        ModuleManager man = ModuleManager.getInstance();

        // Visual modules (with real logic)
        fullBright = new FullBrightModule();
        man.addModule(fullBright);
    }
}
