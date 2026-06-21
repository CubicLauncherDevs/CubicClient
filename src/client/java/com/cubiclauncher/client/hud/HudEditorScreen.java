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

        // Very subtle dark overlay
        fill(poseStack, 0, 0, width, height, 0x40000000);

        // Minimal header
        drawCenteredString(poseStack, font, "Editor de HUD", width / 2, 6, 0xFFFFFFFF);
        drawString(poseStack, font, "ESC para salir", 6, 6, 0xFFAAAAAA);

        // Draw each widget with actual HUD content
        for (HudWidget w : widgets) {
            boolean hovered = dragging == w || (mouseX >= w.x && mouseX < w.x + w.w && mouseY >= w.y && mouseY < w.y + w.h);

            // Subtle selection indicator - thin cyan border
            if (hovered || dragging == w) {
                int borderAlpha = (dragging == w) ? 0xFF : 0xAA;
                fill(poseStack, w.x - 1, w.y - 1, w.x + w.w + 1, w.y, 0x00FFCC | (borderAlpha << 24));
                fill(poseStack, w.x - 1, w.y + w.h, w.x + w.w + 1, w.y + w.h + 1, 0x00FFCC | (borderAlpha << 24));
                fill(poseStack, w.x - 1, w.y, w.x, w.y + w.h, 0x00FFCC | (borderAlpha << 24));
                fill(poseStack, w.x + w.w, w.y, w.x + w.w + 1, w.y + w.h, 0x00FFCC | (borderAlpha << 24));
            }

            // Render actual HUD content preview
            renderHudPreview(poseStack, w, hovered);
        }

        // Hint when no active widgets
        if (widgets.isEmpty()) {
            drawCenteredString(poseStack, font, "Activa al menos un elemento del HUD en el menú primero.", width / 2, height / 2, 0xFFAAAAAA);
        }
    }

    private void renderHudPreview(PoseStack poseStack, HudWidget w, boolean hovered) {
        HudConfig cfg = HudConfig.getInstance();
        int color = 0xFFFFFFFF;
        int shadowColor = 0x80000000;

        String text = "";
        switch (w.label) {
            case "FPS":
                text = "FPS: " + mc.fpsString;
                break;
            case "Coordenadas":
                if (mc.player != null) {
                    text = String.format("%.1f / %.1f / %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
                }
                break;
            case "Brújula":
                text = getFacingDirection();
                break;
            case "Armadura":
                renderArmorPreview(poseStack, w.x, w.y);
                return;
            case "Objetos":
                renderHeldItemsPreview(poseStack, w.x, w.y);
                return;
        }

        if (!text.isEmpty()) {
            mc.font.draw(poseStack, text, w.x + 1, w.y + 1, shadowColor);
            mc.font.draw(poseStack, text, w.x, w.y, color);
        }

        // Show position hint when hovered
        if (hovered) {
            String pos = w.x + ", " + w.y;
            mc.font.draw(poseStack, pos, w.x, w.y + w.h + 2, 0xFF00FFCC);
        }
    }

    private String getFacingDirection() {
        if (mc.player == null) return "?";
        return switch (mc.player.getDirection()) {
            case NORTH -> "N  (-Z)";
            case SOUTH -> "S  (+Z)";
            case EAST  -> "E  (+X)";
            case WEST  -> "W  (-X)";
            default -> "?";
        };
    }

    private void renderArmorPreview(PoseStack poseStack, int x, int y) {
        String[] labels = { "H", "C", "L", "B" };
        for (int i = 0; i < labels.length; i++) {
            mc.font.draw(poseStack, labels[i], x, y + i * 18, 0xFFFFFFFF);
        }
    }

    private void renderHeldItemsPreview(PoseStack poseStack, int x, int y) {
        mc.font.draw(poseStack, "M", x, y, 0xFFFFFFFF);
        mc.font.draw(poseStack, "O", x, y + 18, 0xFFFFFFFF);
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
