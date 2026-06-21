package com.cubiclauncher.client.hud;

public class HudConfig {
    private static final HudConfig INSTANCE = new HudConfig();

    // Toggles
    private boolean showFps;
    private boolean showCoords;
    private boolean showCompass;
    private boolean showArmor;
    private boolean showHeldItems;

    // Per-element positions (defaults spread vertically on the left)
    private int fpsX = 4, fpsY = 4;
    private int coordsX = 4, coordsY = 16;
    private int compassX = 4, compassY = 28;
    private int armorX = 4, armorY = 42;
    private int heldItemsX = 4, heldItemsY = 110;

    public static HudConfig getInstance() {
        return INSTANCE;
    }

    // --- Toggles ---
    public boolean isShowFps() { return showFps; }
    public void setShowFps(boolean v) { showFps = v; }

    public boolean isShowCoords() { return showCoords; }
    public void setShowCoords(boolean v) { showCoords = v; }

    public boolean isShowCompass() { return showCompass; }
    public void setShowCompass(boolean v) { showCompass = v; }

    public boolean isShowArmor() { return showArmor; }
    public void setShowArmor(boolean v) { showArmor = v; }

    public boolean isShowHeldItems() { return showHeldItems; }
    public void setShowHeldItems(boolean v) { showHeldItems = v; }

    // --- Positions ---
    public int getFpsX() { return fpsX; }
    public int getFpsY() { return fpsY; }
    public void setFpsPos(int x, int y) { fpsX = x; fpsY = y; }

    public int getCoordsX() { return coordsX; }
    public int getCoordsY() { return coordsY; }
    public void setCoordsPos(int x, int y) { coordsX = x; coordsY = y; }

    public int getCompassX() { return compassX; }
    public int getCompassY() { return compassY; }
    public void setCompassPos(int x, int y) { compassX = x; compassY = y; }

    public int getArmorX() { return armorX; }
    public int getArmorY() { return armorY; }
    public void setArmorPos(int x, int y) { armorX = x; armorY = y; }

    public int getHeldItemsX() { return heldItemsX; }
    public int getHeldItemsY() { return heldItemsY; }
    public void setHeldItemsPos(int x, int y) { heldItemsX = x; heldItemsY = y; }
}
