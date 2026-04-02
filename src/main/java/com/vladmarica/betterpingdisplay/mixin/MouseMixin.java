package com.vladmarica.betterpingdisplay.mixin;

import com.vladmarica.betterpingdisplay.BetterPingDisplayClient;
import com.vladmarica.betterpingdisplay.BetterPingDisplayMod;
import com.vladmarica.betterpingdisplay.display.PingSmoothing;
import com.vladmarica.betterpingdisplay.gui.KeyHintHud;
import net.minecraft.client.Mouse;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow @Final private MinecraftClient client;

    // Inject at HEAD — before Minecraft processes mouse delta
    // Ping Smooth sets target rotation, then Minecraft adds mouse delta on top
    // This is natural: player mouse movement adds to Ping Smooth rotation
    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void onUpdateMouse(CallbackInfo ci) {
        PingSmoothing.INSTANCE.onUpdateMouse(client,
            BetterPingDisplayMod.instance().getConfig());
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (button != 0 || action != 1) return;
        if (client.currentScreen != null) return;
        KeyHintHud.onClick(
            BetterPingDisplayClient.getLastMouseX(),
            BetterPingDisplayClient.getLastMouseY(),
            BetterPingDisplayClient.getKeySequenceHandler()
        );
    }
}
