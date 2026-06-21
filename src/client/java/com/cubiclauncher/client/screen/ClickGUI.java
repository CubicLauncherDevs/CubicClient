package com.cubiclauncher.client.screen;

import com.cubiclauncher.client.hud.HudConfig;
import com.cubiclauncher.client.hud.HudEditorScreen;
import com.cubiclauncher.module.Module;
import com.cubiclauncher.module.ModuleCategory;
import com.cubiclauncher.module.ModuleManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends Screen {

    // ----- Layout constants -----
    private static final int WIN_W    = 300;
    private static final int WIN_H    = 230;
    private static final int SIDE_W   = 72;   // sidebar width
    private static final int ITEM_H   = 22;
    private static final int HEADER_H = 30;

    // ----- Colour palette (dark neon) -----
    private static final int
        C_WIN_BG     = 0xEB121216,
        C_SIDE_BG    = 0xF40D0D11,
        C_DIVIDER    = 0xFF282830,
        C_HOVER_ROW  = 0xFF1C1C24,
        C_ACCENT     = 0xFF00FFCC,
        C_TEXT_DIM   = 0xFF8A8A93,
        C_TEXT_WHITE = 0xFFFFFFFF,
        C_TOGGLE_OFF = 0xFF404040,
        C_BTN_BG     = 0xFF1A1A26,
        C_BTN_HOVER  = 0xFF252535;

    // ----- Tabs -----
    private static final String TAB_HUD    = "HUD";
    private static final String TAB_VISUAL = "Visual";
    private static final String[] TAB_NAMES = { TAB_HUD, TAB_VISUAL };

    private static int selectedTab = 0;   // persists across open/close

    // ----- HUD tab state -----
    private static final List<HudToggle> hudToggles = new ArrayList<>();
    private static boolean hudBuilt = false;

    // ----- Visual tab state -----
    private static final List<ModuleRow> visualModules = new ArrayList<>();
    private static boolean visualBuilt = false;

    // ----- Animation -----
    private float openAnim = 0f;
    private boolean isClosing = false;
    private long lastFrameTime = 0;

    // ----- Tooltip -----
    private String hoveredDesc = null;
    private int hoverX, hoverY;

    public ClickGUI() {
        super(Component.literal("Cubic Client"));
    }

    // ------------------------------------------------------------------ init

    @Override
    protected void init() {
        super.init();
        if (!hudBuilt)    { buildHudToggles();   hudBuilt    = true; }
        if (!visualBuilt) { buildVisualModules(); visualBuilt = true; }
    }

    private static void buildHudToggles() {
        hudToggles.clear();
        HudConfig cfg = HudConfig.getInstance();
        hudToggles.add(new HudToggle("FPS",         "Muestra los FPS en pantalla",    cfg::isShowFps,       cfg::setShowFps));
        hudToggles.add(new HudToggle("Coordenadas", "Muestra tus coordenadas XYZ",    cfg::isShowCoords,    cfg::setShowCoords));
        hudToggles.add(new HudToggle("Brújula",     "Muestra la dirección cardinal",  cfg::isShowCompass,   cfg::setShowCompass));
        hudToggles.add(new HudToggle("Armadura",    "Muestra tu armadura equipada",   cfg::isShowArmor,     cfg::setShowArmor));
        hudToggles.add(new HudToggle("Objetos",     "Muestra objetos en mano",        cfg::isShowHeldItems, cfg::setShowHeldItems));
    }

    private static void buildVisualModules() {
        visualModules.clear();
        List<Module> mods = ModuleManager.getInstance().getModulesByCategory(ModuleCategory.VISUAL);
        for (Module m : mods) {
            visualModules.add(new ModuleRow(m));
        }
    }

    // ----------------------------------------------------------------- render

    @Override
    public void render(PoseStack ps, int mx, int my, float pt) {
        updateAnim();
        if (isClosing && openAnim <= 0) { minecraft.setScreen(null); return; }

        float a = openAnim;
        int wx = (width  - WIN_W) / 2;
        int wy = (height - WIN_H) / 2;

        // Backdrop overlay
        fill(ps, 0, 0, width, height, (int)(a * 90) << 24);

        // Window BG
        fill(ps, wx, wy, wx + WIN_W, wy + WIN_H, withAlpha(C_WIN_BG, a));

        // Sidebar BG
        fill(ps, wx, wy, wx + SIDE_W, wy + WIN_H, withAlpha(C_SIDE_BG, a));

        // Sidebar / content divider
        fill(ps, wx + SIDE_W, wy, wx + SIDE_W + 1, wy + WIN_H, withAlpha(C_DIVIDER, a));

        // Outer border
        border(ps, wx - 1, wy - 1, wx + WIN_W + 1, wy + WIN_H + 1, withAlpha(C_DIVIDER, a));

        // ----- Sidebar header -----
        fill(ps, wx, wy, wx + SIDE_W, wy + HEADER_H, withAlpha(0xF4080810, a));
        drawCenteredString(ps, font, "Cubic", wx + SIDE_W / 2, wy + 6,  withAlpha(C_TEXT_WHITE, a));
        drawCenteredString(ps, font, "Client", wx + SIDE_W / 2, wy + 16, withAlpha(C_ACCENT,     a));
        fill(ps, wx + 6, wy + HEADER_H - 1, wx + SIDE_W - 6, wy + HEADER_H, withAlpha(C_DIVIDER, a));

        // ----- Tab buttons in sidebar -----
        int tabY = wy + HEADER_H + 4;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            boolean sel = (i == selectedTab);
            boolean hov = mx >= wx && mx < wx + SIDE_W && my >= tabY && my < tabY + 20;

            if (sel) {
                // Accent left bar + subtle bg
                fill(ps, wx, tabY, wx + SIDE_W, tabY + 20, withAlpha(0xFF111118, a));
                fill(ps, wx, tabY + 2, wx + 2, tabY + 18, withAlpha(C_ACCENT, a));
            } else if (hov) {
                fill(ps, wx, tabY, wx + SIDE_W, tabY + 20, withAlpha(0xFF0E0E14, a));
            }

            int tc = sel ? C_ACCENT : (hov ? C_TEXT_WHITE : C_TEXT_DIM);
            drawCenteredString(ps, font, TAB_NAMES[i], wx + SIDE_W / 2, tabY + 6, withAlpha(tc, a));
            tabY += 20;
        }

        // ----- Content area header -----
        fill(ps, wx + SIDE_W + 1, wy, wx + WIN_W, wy + HEADER_H, withAlpha(0xF4080810, a));
        drawString(ps, font, TAB_NAMES[selectedTab], wx + SIDE_W + 10, wy + 11, withAlpha(C_TEXT_WHITE, a));
        fill(ps, wx + SIDE_W + 6, wy + HEADER_H - 1, wx + WIN_W - 6, wy + HEADER_H, withAlpha(C_DIVIDER, a));

        // ----- Tab content -----
        hoveredDesc = null;
        if (selectedTab == 0) renderHudTab(ps, wx, wy, mx, my, a);
        else                  renderVisualTab(ps, wx, wy, mx, my, a);

        // ----- Tooltip -----
        if (hoveredDesc != null && !hoveredDesc.isEmpty()) drawTooltip(ps, hoveredDesc, mx, my);
    }

    // -------------------------------------------------------- HUD tab content

    private void renderHudTab(PoseStack ps, int wx, int wy, int mx, int my, float a) {
        int cx = wx + SIDE_W + 1;
        int cw = WIN_W - SIDE_W - 1;
        int iy = wy + HEADER_H;

        for (HudToggle t : hudToggles) {
            t.anim += ((t.isActive() ? 1f : 0f) - t.anim) * 0.18f;

            boolean hov = mx >= cx && mx < cx + cw && my >= iy && my < iy + ITEM_H;
            if (hov) {
                fill(ps, cx, iy, cx + cw, iy + ITEM_H, withAlpha(C_HOVER_ROW, a));
                hoveredDesc = t.description;
                hoverX = mx; hoverY = my;
            }
            fill(ps, cx + 8, iy + ITEM_H - 1, cx + cw - 8, iy + ITEM_H, withAlpha(C_DIVIDER, a));

            drawString(ps, font, t.name, cx + 10, iy + 7, withAlpha(lerpColor(C_TEXT_DIM, C_ACCENT, t.anim), a));
            drawToggle(ps, cx + cw - 38, iy + (ITEM_H - 14) / 2, t.anim, a);
            iy += ITEM_H;
        }

        // "Edit HUD" button at the bottom of the content area
        int btnY = wy + WIN_H - 26;
        boolean btnHov = mx >= cx + 8 && mx < cx + cw - 8 && my >= btnY && my < btnY + 18;
        fill(ps, cx + 8, btnY, cx + cw - 8, btnY + 18, withAlpha(btnHov ? C_BTN_HOVER : C_BTN_BG, a));
        border(ps, cx + 8, btnY, cx + cw - 8, btnY + 18, withAlpha(C_ACCENT, a * (btnHov ? 0.9f : 0.5f)));
        drawCenteredString(ps, font, "Editar posiciones del HUD", cx + cw / 2, btnY + 5, withAlpha(C_ACCENT, a));
    }

    // ----------------------------------------------------- Visual tab content

    private void renderVisualTab(PoseStack ps, int wx, int wy, int mx, int my, float a) {
        int cx = wx + SIDE_W + 1;
        int cw = WIN_W - SIDE_W - 1;
        int iy = wy + HEADER_H;

        if (visualModules.isEmpty()) {
            drawCenteredString(ps, font, "Sin módulos visuales", cx + cw / 2, wy + WIN_H / 2, withAlpha(C_TEXT_DIM, a));
            return;
        }

        for (ModuleRow row : visualModules) {
            row.anim += ((row.module.isEnabled() ? 1f : 0f) - row.anim) * 0.18f;

            boolean hov = mx >= cx && mx < cx + cw && my >= iy && my < iy + ITEM_H;
            if (hov) {
                fill(ps, cx, iy, cx + cw, iy + ITEM_H, withAlpha(C_HOVER_ROW, a));
                hoveredDesc = row.module.getDescription();
                hoverX = mx; hoverY = my;
            }
            fill(ps, cx + 8, iy + ITEM_H - 1, cx + cw - 8, iy + ITEM_H, withAlpha(C_DIVIDER, a));

            drawString(ps, font, row.module.getName(),
                cx + 10, iy + 7,
                withAlpha(lerpColor(C_TEXT_DIM, C_ACCENT, row.anim), a));
            drawToggle(ps, cx + cw - 38, iy + (ITEM_H - 14) / 2, row.anim, a);
            iy += ITEM_H;
        }
    }

    // --------------------------------------------------------- draw helpers

    private void drawToggle(PoseStack ps, int x, int y, float pos, float a) {
        fillRoundedRect(ps, x, y, x + 28, y + 14, 7,
            withAlpha(lerpColor(C_TOGGLE_OFF, C_ACCENT, pos), a));
        int kx = (int)(x + 2 + 14 * pos);
        fillCircle(ps, kx + 5, y + 7, 5, withAlpha(C_TEXT_WHITE, a));
    }

    private void drawTooltip(PoseStack ps, String text, int mx, int my) {
        int tw = font.width(text);
        int tx = mx + 10, ty = my + 10;
        if (tx + tw + 10 > width)  tx = mx - tw - 10;
        if (ty + 18      > height) ty = height - 18;
        fill(ps, tx - 4, ty - 3, tx + tw + 4, ty + 11, 0xF20D0D11);
        border(ps, tx - 5, ty - 4, tx + tw + 5, ty + 12, 0xFF282830);
        drawString(ps, font, text, tx, ty, 0xFFE1E1E6);
    }

    private void border(PoseStack ps, int x1, int y1, int x2, int y2, int col) {
        fill(ps, x1, y1, x2, y1 + 1, col);
        fill(ps, x1, y2 - 1, x2, y2, col);
        fill(ps, x1, y1, x1 + 1, y2, col);
        fill(ps, x2 - 1, y1, x2, y2, col);
    }

    private void fillCircle(PoseStack ps, float cx, float cy, int r, int col) {
        for (int dy = -r; dy <= r; dy++) {
            int dx = (int) Math.sqrt(r * r - dy * dy);
            fill(ps, (int)cx - dx, (int)cy + dy, (int)cx + dx + 1, (int)cy + dy + 1, col);
        }
    }

    private void fillRoundedRect(PoseStack ps, int x, int y, int x2, int y2, int r, int col) {
        r = Math.min(r, Math.min((x2 - x) / 2, (y2 - y) / 2));
        if (r <= 0) { fill(ps, x, y, x2, y2, col); return; }
        fill(ps, x + r, y, x2 - r, y2, col);
        fill(ps, x, y + r, x + r, y2 - r, col);
        fill(ps, x2 - r, y + r, x2, y2 - r, col);
        for (int i = 0; i < r; i++) {
            int h = (int) Math.sqrt(r * r - (r - i) * (r - i));
            if (h <= 0) continue;
            fill(ps, x + i, y + r - h, x + i + 1, y + r, col);
            fill(ps, x2 - i - 1, y + r - h, x2 - i, y + r, col);
            fill(ps, x + i, y2 - r, x + i + 1, y2 - r + h, col);
            fill(ps, x2 - i - 1, y2 - r, x2 - i, y2 - r + h, col);
        }
    }

    // -------------------------------------------------------------- animation

    private void updateAnim() {
        long now = System.currentTimeMillis();
        if (lastFrameTime == 0) { lastFrameTime = now; return; }
        float dt = (now - lastFrameTime) / 1000f;
        lastFrameTime = now;
        float spd = 7f;
        openAnim = isClosing
            ? Math.max(0, openAnim - dt * spd)
            : Math.min(1, openAnim + dt * spd);
    }

    // ------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);

        int wx = (width  - WIN_W) / 2;
        int wy = (height - WIN_H) / 2;

        // Outside window → close
        if (mx < wx || mx > wx + WIN_W || my < wy || my > wy + WIN_H) {
            startClose(); return true;
        }

        // Sidebar tab clicks
        if (mx >= wx && mx < wx + SIDE_W) {
            int tabY = wy + HEADER_H + 4;
            for (int i = 0; i < TAB_NAMES.length; i++) {
                if (my >= tabY && my < tabY + 20) {
                    selectedTab = i;
                    return true;
                }
                tabY += 20;
            }
            return true;
        }

        // Content area
        int cx = wx + SIDE_W + 1;
        int cw = WIN_W - SIDE_W - 1;

        if (selectedTab == 0) {
            // HUD toggles
            int iy = wy + HEADER_H;
            for (HudToggle t : hudToggles) {
                if (my >= iy && my < iy + ITEM_H && mx >= cx && mx < cx + cw) {
                    t.toggle(); return true;
                }
                iy += ITEM_H;
            }
            // Edit HUD button
            int btnY = wy + WIN_H - 26;
            if (mx >= cx + 8 && mx < cx + cw - 8 && my >= btnY && my < btnY + 18) {
                minecraft.setScreen(new HudEditorScreen());
                return true;
            }
        } else {
            // Visual module toggles
            int iy = wy + HEADER_H;
            for (ModuleRow row : visualModules) {
                if (my >= iy && my < iy + ITEM_H && mx >= cx && mx < cx + cw) {
                    row.module.toggle(); return true;
                }
                iy += ITEM_H;
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int key, int sc, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE)      { startClose(); return true; }
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) { if (isClosing) isClosing = false; else startClose(); return true; }
        return super.keyPressed(key, sc, mods);
    }

    @Override public boolean shouldCloseOnEsc() { return false; }
    @Override public boolean isPauseScreen()    { return false; }

    private void startClose() { if (!isClosing) isClosing = true; }

    // ----------------------------------------------------------------- utils

    private static int withAlpha(int color, float alpha) {
        int a = Math.min(255, Math.max(0, (int)(((color >> 24) & 0xFF) * alpha)));
        return (a << 24) | (color & 0xFFFFFF);
    }

    private static int lerpColor(int from, int to, float t) {
        t = Math.max(0, Math.min(1, t));
        return ((int)(((from >> 24) & 0xFF) * (1-t) + ((to >> 24) & 0xFF) * t) << 24)
             | ((int)(((from >> 16) & 0xFF) * (1-t) + ((to >> 16) & 0xFF) * t) << 16)
             | ((int)(((from >>  8) & 0xFF) * (1-t) + ((to >>  8) & 0xFF) * t) <<  8)
             |  (int)(( from        & 0xFF) * (1-t) + ( to        & 0xFF) * t);
    }

    // ----------------------------------------------------------------- model

    private static class HudToggle {
        final String name, description;
        final java.util.function.BooleanSupplier getter;
        final java.util.function.Consumer<Boolean> setter;
        float anim;

        HudToggle(String name, String desc,
                  java.util.function.BooleanSupplier getter,
                  java.util.function.Consumer<Boolean> setter) {
            this.name = name; this.description = desc;
            this.getter = getter; this.setter = setter;
            this.anim = getter.getAsBoolean() ? 1f : 0f;
        }

        boolean isActive() { return getter.getAsBoolean(); }
        void toggle()      { setter.accept(!getter.getAsBoolean()); }
    }

    private static class ModuleRow {
        final Module module;
        float anim;

        ModuleRow(Module module) {
            this.module = module;
            this.anim = module.isEnabled() ? 1f : 0f;
        }
    }
}
