package com.vladmarica.betterpingdisplay.mixin;

import com.vladmarica.betterpingdisplay.BetterPingDisplayClient;
import com.vladmarica.betterpingdisplay.BetterPingDisplayMod;
import com.vladmarica.betterpingdisplay.display.PingSmoothing;
import com.vladmarica.betterpingdisplay.gui.KeyBindScreen;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKeyPress(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action != 1) return;
        if (client.currentScreen != null) return;

        // Open KeyBindScreen via key sequence
        boolean shouldOpen = BetterPingDisplayClient.getKeySequenceHandler().onKeyPressed(key);
        if (shouldOpen) {
            client.execute(() -> client.setScreen(
                new KeyBindScreen(null, BetterPingDisplayClient.getKeySequenceHandler())
            ));
            return;
        }

        // Toggle TriggerBot via keybind
        int displayKey = BetterPingDisplayMod.instance().getConfig().getDisplayKeybind();
        if (displayKey != -1 && displayKey < 1000 && key == displayKey) {
            boolean current = BetterPingDisplayMod.instance().getConfig().isDisplayEnabled();
            BetterPingDisplayMod.instance().getConfig().setDisplayEnabled(!current);
        }

        // Toggle Ping Smooth via keybind
        int aaKey = BetterPingDisplayMod.instance().getConfig().getPingSmoothingKeybind();
        if (aaKey != -1 && aaKey < 1000 && key == aaKey) {
            var cfg = BetterPingDisplayMod.instance().getConfig();
            cfg.setPingSmoothingEnabled(!cfg.isPingSmoothingEnabled());
        }

        // Toggle Auto Firework via keybind
        int fwKey = BetterPingDisplayMod.instance().getConfig().getAutoFireworkKeybind();
        if (fwKey != -1 && fwKey < 1000 && key == fwKey) {
            var cfg = BetterPingDisplayMod.instance().getConfig();
            cfg.setAutoFireworkEnabled(!cfg.isAutoFireworkEnabled());
        }
    }
}
