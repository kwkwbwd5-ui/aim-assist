package com.vladmarica.betterpingdisplay.mixin;

import com.vladmarica.betterpingdisplay.BetterPingDisplayMod;
import com.vladmarica.betterpingdisplay.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class ExpandHitboxMixin {

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void expandBox(CallbackInfoReturnable<Box> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;

        LivingEntity self = (LivingEntity)(Object)this;

        // فقط للاعبين الآخرين
        if (!(self instanceof AbstractClientPlayerEntity)) return;
        if (self == mc.player) return;

        Config cfg = BetterPingDisplayMod.instance().getConfig();
        if (!cfg.isExpandHitboxEnabled()) return;

        float exp = cfg.getExpandHitboxSize();
        if (exp <= 0) return;

        Box box = cir.getReturnValue();
        cir.setReturnValue(box.expand(exp, exp, exp));
    }
}
