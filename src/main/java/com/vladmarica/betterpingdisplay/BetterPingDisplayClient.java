package com.vladmarica.betterpingdisplay;

import com.vladmarica.betterpingdisplay.gui.KeySequenceHandler;
import com.vladmarica.betterpingdisplay.display.AutoFirework;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.Box;

public class BetterPingDisplayClient implements ClientModInitializer {

    private static final KeySequenceHandler keySequenceHandler = new KeySequenceHandler();

    private static int lastMouseX = 0;
    private static boolean aaMouseWasPressed  = false;
    private static boolean trigMouseWasPressed = false;
    private static boolean fwMouseWasPressed   = false;
    private static int lastMouseY = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            keySequenceHandler.tick();

            // Mouse button keybind support
            var cfg2 = com.vladmarica.betterpingdisplay.BetterPingDisplayMod.instance().getConfig();
            if (client.currentScreen == null) {
                long win = client.getWindow().getHandle();

                // Ping Smooth mouse keybind — toggle on press
                int aaKey = cfg2.getPingSmoothingKeybind();
                if (aaKey >= 1000) {
                    int mbtn = aaKey - 1000;
                    int state = org.lwjgl.glfw.GLFW.glfwGetMouseButton(win, mbtn);
                    if (state == org.lwjgl.glfw.GLFW.GLFW_PRESS && !aaMouseWasPressed) {
                        cfg2.setPingSmoothingEnabled(!cfg2.isPingSmoothingEnabled());
                        aaMouseWasPressed = true;
                    } else if (state == org.lwjgl.glfw.GLFW.GLFW_RELEASE) {
                        aaMouseWasPressed = false;
                    }
                }

                // Trigger mouse keybind — toggle on press
                int trigKey = cfg2.getDisplayKeybind();
                if (trigKey >= 1000) {
                    int mbtn = trigKey - 1000;
                    int state = org.lwjgl.glfw.GLFW.glfwGetMouseButton(win, mbtn);
                    if (state == org.lwjgl.glfw.GLFW.GLFW_PRESS && !trigMouseWasPressed) {
                        cfg2.setDisplayEnabled(!cfg2.isDisplayEnabled());
                        trigMouseWasPressed = true;
                    } else if (state == org.lwjgl.glfw.GLFW.GLFW_RELEASE) {
                        trigMouseWasPressed = false;
                    }
                }

                // Auto Firework mouse keybind
                int fwKey = cfg2.getAutoFireworkKeybind();
                if (fwKey >= 1000) {
                    int mbtn = fwKey - 1000;
                    int state = org.lwjgl.glfw.GLFW.glfwGetMouseButton(win, mbtn);
                    if (state == org.lwjgl.glfw.GLFW.GLFW_PRESS && !fwMouseWasPressed) {
                        cfg2.setAutoFireworkEnabled(!cfg2.isAutoFireworkEnabled());
                        fwMouseWasPressed = true;
                    } else if (state == org.lwjgl.glfw.GLFW.GLFW_RELEASE) {
                        fwMouseWasPressed = false;
                    }
                }
            }

            if (client.currentScreen == null && client.getWindow() != null) {
                double mx = client.mouse.getX() * client.getWindow().getScaledWidth()  / client.getWindow().getWidth();
                double my = client.mouse.getY() * client.getWindow().getScaledHeight() / client.getWindow().getHeight();
                lastMouseX = (int) mx;
                lastMouseY = (int) my;
            }

            // Static Hitbox
            if (client.world != null && client.player != null) {
                var cfg = com.vladmarica.betterpingdisplay.BetterPingDisplayMod.instance().getConfig();
                if (cfg.isStaticHitboxEnabled() && !cfg.isStaticHitboxHide()) {
                    // Normal mode: expand visible bounding box
                    float tw = cfg.getStaticHitboxWidth();
                    float th = cfg.getStaticHitboxHeight();
                    for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
                        if (player == client.player) continue;
                        if (!player.isFallFlying() && !player.isSwimming() && !player.isSleeping()) continue;
                        Box box = player.getBoundingBox();
                        double w = box.maxX - box.minX, h = box.maxY - box.minY;
                        if (Math.abs(tw - w) > 0.001 || Math.abs(th - h) > 0.001) {
                            double cx = (box.minX + box.maxX) / 2.0;
                            double cy = (box.minY + box.maxY) / 2.0;
                            double cz = (box.minZ + box.maxZ) / 2.0;
                            player.setBoundingBox(new Box(cx-tw/2.0, cy-th/2.0, cz-tw/2.0, cx+tw/2.0, cy+th/2.0, cz+tw/2.0));
                        }
                    }
                }
                // Hide mode: store expanded boxes separately — used by PingHelper for detection only
                com.vladmarica.betterpingdisplay.display.PingHelper.updateHiddenHitboxes(client, cfg);
            }
        });
    }

    public static KeySequenceHandler getKeySequenceHandler() { return keySequenceHandler; }
    public static int getLastMouseX() { return lastMouseX; }
    public static int getLastMouseY() { return lastMouseY; }
}
