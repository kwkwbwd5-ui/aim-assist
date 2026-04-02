package com.vladmarica.betterpingdisplay.mixin;

import com.vladmarica.betterpingdisplay.display.PingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onInput(CallbackInfo ci) {
        MinecraftClient mc = (MinecraftClient)(Object)this;
        com.vladmarica.betterpingdisplay.display.AutoFirework.INSTANCE.tick(
                mc, com.vladmarica.betterpingdisplay.BetterPingDisplayMod.instance().getConfig());
        
        Entity target = PingHelper.pendingAttack;
        if (target == null) return;
        PingHelper.pendingAttack = null;

        if (mc.player == null || mc.interactionManager == null) return;

        mc.crosshairTarget = new EntityHitResult(target);
        
        // Stop sprinting and apply vanilla knockback penalty to prevent tickMovement from instantly re-sprinting
        if (mc.player.isSprinting()) {
            mc.player.setSprinting(false);
            mc.player.setVelocity(mc.player.getVelocity().multiply(0.6, 1.0, 0.6));
        }

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.resetLastAttackedTicks();
    }
}
