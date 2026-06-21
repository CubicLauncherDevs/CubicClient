package com.cubiclauncher.client.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Full-screen HUD editor. Each enabled HUD element is shown as a labelled
 * dark widget that the player can drag anywhere on screen. Positions are
 * saved back to HudConfig in real time.
 */
public class HudEditorScreen extends Screen {

    private final Minecraft mc = Minecraft.getInstance();
    private final List<HudWidget> widgets = new ArrayList<>();
    private HudWidget dragging = null;
    private int dragOffX, dragOffY;

    public HudEditorScreen() {
        super(Component.literal("Editor de HUD"));
    }

    @Override
    protected void init() {
        super.init();
        buildHudWidgets();
    }

    private void buildHudWidgets() {
        widgets.clear();
        HudConfig cfg = HudConfig.getInstance();

        if (cfg.isShowFps())
            widgets.add(new HudWidget("FPS", cfg.getFpsX(), cfg.getFpsY(), 60, 12) {
                @Override void savePos(int x, int y) { cfg.setFpsPos(x, y); }
            });

        if (cfg.isShowCoords())
            widgets.add(new HudWidget("Coordenadas", cfg.getCoordsX(), cfg.getCoordsY(), 110, 12) {
                @Override void savePos(int x, int y) { cfg.setCoordsPos(x, y); }
            });

        if (cfg.isShowCompass())
            widgets.add(new HudWidget("Brújula", cfg.getCompassX(), cfg.getCompassY(), 60, 12) {
                @Override void savePos(int x, int y) { cfg.setCompassPos(x, y); }
            });

        if (cfg.isShowArmor())
            widgets.add(new HudWidget("Armadura", cfg.getArmorX(), cfg.getArmorY(), 60, 76) {
                @Override void savePos(int x, int y) { cfg.setArmorPos(x, y); }
            });

        if (cfg.isShowHeldItems())
            widgets.add(new HudWidget("Objetos", cfg.getHeldItemsX(), cfg.getHeldItemsY(), 60, 40) {
                @Override void savePos(int x, int y) { cfg.setHeldItemsPos(x, y); }
            });
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        // Update dragged widget position
        if (dragging != null) {
            dragging.x = clamp(mouseX - dragOffX, 0, width - dragging.w);
            dragging.y = clamp(mouseY - dragOffY, 0, height - dragging.h);
            dragging.savePos(dragging.x, dragging.y);
        }

        // Dark transparent overlay
        fill(poseStack, 0, 0, width, height, 0xB0050508);

        // Grid hint lines (subtle)
        for (int gx = 0; gx < width; gx += 20)
            fill(poseStack, gx, 0, gx + 1, height, 0x14FFFFFF);
        for (int gy = 0; gy < height; gy += 20)
            fill(poseStack, 0, gy, width, gy + 1, 0x14FFFFFF);

        // Header bar
        fill(poseStack, 0, 0, width, 18, 0xE0080810);
        drawCenteredString(poseStack, font, "Editor de HUD  —  Arrastra los elementos", width / 2, 5, 0xFF00FFCC);
        drawString(poseStack, font, "ESC para guardar y salir", 6, 5, 0xFF8A8A93);

        // Draw each widget
        for (HudWidget w : widgets) {
            boolean hovered = dragging == w || (mouseX >= w.x && mouseX < w.x + w.w && mouseY >= w.y && mouseY < w.y + w.h);

            // Shadow
            fill(poseStack, w.x + 2, w.y + 2, w.x + w.w + 2, w.y + w.h + 2, 0x55000000);

            // Background
            int bg = hovered ? 0xEA1C1C28 : 0xE0121218;
            fill(poseStack, w.x, w.y, w.x + w.w, w.y + w.h, bg);

            // Neon cyan border
            int borderCol = (dragging == w) ? 0xFF00FFCC : (hovered ? 0x9900FFCC : 0x5500FFCC);
            fill(poseStack, w.x, w.y, w.x + w.w, w.y + 1, borderCol);
            fill(poseStack, w.x, w.y + w.h - 1, w.x + w.w, w.y + w.h, borderCol);
            fill(poseStack, w.x, w.y, w.x + 1, w.y + w.h, borderCol);
            fill(poseStack, w.x + w.w - 1, w.y, w.x + w.w, w.y + w.h, borderCol);

            // Label centred inside widget
            int labelColor = (dragging == w) ? 0xFF00FFCC : 0xFFE1E1E6;
            int lx = w.x + (w.w - font.width(w.label)) / 2;
            int ly = w.y + (w.h - 8) / 2;
            drawString(poseStack, font, w.label, lx, ly, labelColor);

            // Coords hint below label
            if (hovered) {
                String pos = w.x + ", " + w.y;
                int px = w.x + (w.w - font.width(pos)) / 2;
                drawString(poseStack, font, pos, px, ly + 10, 0xFF8A8A93);
            }
        }

        // Hint when no active widgets
        if (widgets.isEmpty()) {
            drawCenteredString(poseStack, font, "Activa al menos un elemento del HUD en el menú primero.", width / 2, height / 2, 0xFF8A8A93);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            // Iterate in reverse so top-drawn widget is picked first
            for (int i = widgets.size() - 1; i >= 0; i--) {
                HudWidget w = widgets.get(i);
                if (mx >= w.x && mx < w.x + w.w && my >= w.y && my < w.y + w.h) {
                    dragging = w;
                    dragOffX = (int) mx - w.x;
                    dragOffY = (int) my - w.y;
                    // Bring to front
                    widgets.remove(i);
                    widgets.add(w);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) {
            dragging = null;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            // Return to ClickGUI
            mc.setScreen(new com.cubiclauncher.client.screen.ClickGUI());
            return true;
        }
        return super.keyPressed(key, scancode, mods);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    /** Represents one draggable HUD element widget in the editor. */
    private static abstract class HudWidget {
        final String label;
        int x, y;
        final int w, h;

        HudWidget(String label, int x, int y, int w, int h) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        abstract void savePos(int x, int y);
    }
}
