package com.cubiclauncher.client.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class HudRenderer implements HudRenderCallback {

    private final Minecraft mc = Minecraft.getInstance();

    @Override
    public void onHudRender(PoseStack poseStack, float tickDelta) {
        HudConfig cfg = HudConfig.getInstance();
        if (mc.player == null || mc.level == null) return;

        int color = 0xFFFFFFFF;
        int shadowColor = 0x000000;

        if (cfg.isShowFps()) {
            int x = cfg.getFpsX();
            int y = cfg.getFpsY();
            String text = "FPS: " + mc.fpsString;
            mc.font.draw(poseStack, text, x + 1, y + 1, shadowColor);
            mc.font.draw(poseStack, text, x, y, color);
        }

        if (cfg.isShowCoords()) {
            int x = cfg.getCoordsX();
            int y = cfg.getCoordsY();
            LocalPlayer p = mc.player;
            String text = String.format("%.1f / %.1f / %.1f", p.getX(), p.getY(), p.getZ());
            mc.font.draw(poseStack, text, x + 1, y + 1, shadowColor);
            mc.font.draw(poseStack, text, x, y, color);
        }

        if (cfg.isShowCompass()) {
            int x = cfg.getCompassX();
            int y = cfg.getCompassY();
            String dir = getFacing();
            mc.font.draw(poseStack, dir, x + 1, y + 1, shadowColor);
            mc.font.draw(poseStack, dir, x, y, color);
        }

        if (cfg.isShowArmor()) {
            renderArmor(poseStack, cfg.getArmorX(), cfg.getArmorY());
        }

        if (cfg.isShowHeldItems()) {
            renderHeldItems(poseStack, cfg.getHeldItemsX(), cfg.getHeldItemsY());
        }
    }

    private String getFacing() {
        Direction d = mc.player.getDirection();
        return switch (d) {
            case NORTH -> "N  (-Z)";
            case SOUTH -> "S  (+Z)";
            case EAST  -> "E  (+X)";
            case WEST  -> "W  (-X)";
            default -> "?";
        };
    }

    private void renderArmor(PoseStack poseStack, int x, int y) {
        EquipmentSlot[] slots = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET
        };
        String[] labels = { "H", "C", "L", "B" };
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = mc.player.getItemBySlot(slots[i]);
            if (stack.isEmpty()) continue;
            mc.getItemRenderer().renderGuiItem(stack, x, y);
            mc.getItemRenderer().renderGuiItemDecorations(mc.font, stack, x, y);
            mc.font.draw(poseStack, labels[i], x + 20, y + 4, 0xFFFFFFFF);
            y += 18;
        }
    }

    private void renderHeldItems(PoseStack poseStack, int x, int y) {
        ItemStack main = mc.player.getMainHandItem();
        ItemStack off  = mc.player.getOffhandItem();

        if (!main.isEmpty()) {
            mc.getItemRenderer().renderGuiItem(main, x, y);
            mc.getItemRenderer().renderGuiItemDecorations(mc.font, main, x, y);
            mc.font.draw(poseStack, "M", x + 20, y + 4, 0xFFFFFFFF);
            y += 18;
        }
        if (!off.isEmpty()) {
            mc.getItemRenderer().renderGuiItem(off, x, y);
            mc.getItemRenderer().renderGuiItemDecorations(mc.font, off, x, y);
            mc.font.draw(poseStack, "O", x + 20, y + 4, 0xFFFFFFFF);
        }
    }
}
