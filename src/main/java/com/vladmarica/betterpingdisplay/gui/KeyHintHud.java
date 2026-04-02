package com.vladmarica.betterpingdisplay.gui;

import net.minecraft.client.MinecraftClient;

/**
 * Handles click detection for opening the KeyBindScreen.
 * No longer renders anything on the main HUD.
 * The KeyBindScreen is opened via the key sequence (L then O by default).
 */
public class KeyHintHud {

    // No rendering - boxes are only inside KeyBindScreen now.

    /**
     * No longer used - kept for compatibility with MouseMixin.
     * Opening the GUI is handled purely by the keyboard sequence in KeyboardMixin.
     */
    public static boolean onClick(int mouseX, int mouseY, KeySequenceHandler keyHandler) {
        return false;
    }
}
