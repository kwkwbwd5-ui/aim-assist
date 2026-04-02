package com.vladmarica.betterpingdisplay.gui;

import org.lwjgl.glfw.GLFW;

/**
 * Handles a two-key sequence to open the GUI.
 * Default: press L (76), then O (79) within 1 second.
 * Keys can be changed at runtime via setKey1/setKey2.
 */
public class KeySequenceHandler {

    // Default keys: L=76, O=79
    private int key1 = GLFW.GLFW_KEY_L;
    private int key2 = GLFW.GLFW_KEY_O;

    private static final long SEQUENCE_TIMEOUT_MS = 1000;

    private boolean firstKeyPressed = false;
    private long firstKeyTime = 0;

    /**
     * Call when a key is pressed. Returns true if the full sequence was completed.
     */
    public boolean onKeyPressed(int keyCode) {
        long now = System.currentTimeMillis();

        if (keyCode == key1) {
            firstKeyPressed = true;
            firstKeyTime = now;
            return false;
        }

        if (keyCode == key2 && firstKeyPressed) {
            if (now - firstKeyTime <= SEQUENCE_TIMEOUT_MS) {
                firstKeyPressed = false;
                return true;
            }
        }

        if (keyCode != key1) {
            firstKeyPressed = false;
        }

        return false;
    }

    public void tick() {
        if (firstKeyPressed && System.currentTimeMillis() - firstKeyTime > SEQUENCE_TIMEOUT_MS) {
            firstKeyPressed = false;
        }
    }

    public boolean isWaitingForSecondKey() {
        return firstKeyPressed;
    }

    public int getKey1() { return key1; }
    public int getKey2() { return key2; }

    public void setKey1(int keyCode) {
        this.key1 = keyCode;
        firstKeyPressed = false;
    }

    public void setKey2(int keyCode) {
        this.key2 = keyCode;
    }
}
