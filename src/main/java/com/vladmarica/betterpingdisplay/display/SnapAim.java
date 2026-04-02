package com.vladmarica.betterpingdisplay.display;

import com.vladmarica.betterpingdisplay.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public class SnapAim {

    public static final SnapAim INSTANCE = new SnapAim();

    /**
     * Find target in expanded box and snap yaw/pitch
     * Returns the target entity if found, deferring attack to PingHelper.
     */
    public LivingEntity findAndSnap(MinecraftClient mc, Config cfg) {
        if (!cfg.isExpandHitboxEnabled() || cfg.getExpandHitboxSize() <= 0) return null;
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return null;
        if (mc.interactionManager == null) return null;

        PlayerEntity self = mc.player;
        float  range = cfg.getDisplayRange();
        double exp   = cfg.getExpandHitboxSize();

        Vec3d eye  = self.getEyePos();
        Vec3d look = self.getRotationVec(1.0f);
        Vec3d end  = eye.add(look.multiply(range + exp));

        // Find closest player in expanded hitbox along look direction
        LivingEntity target    = null;
        double       bestDist  = Double.MAX_VALUE;

        Box area = self.getBoundingBox().expand(range + exp);
        for (var candidate : mc.world.getEntitiesByClass(
                LivingEntity.class, area, e -> e != self && e.isAlive())) {
            Box expanded = candidate.getBoundingBox().expand(exp, exp, exp);
            var hit = expanded.raycast(eye, end);
            if (hit.isPresent()) {
                double d = eye.squaredDistanceTo(candidate.getPos());
                if (d < bestDist) { bestDist = d; target = candidate; }
            }
        }

        if (target == null) return null;

        // ── Spoof Yaw & GCD ───────────────────────────────────────────
        Vec3d tPos   = target.getBoundingBox().getCenter();
        Vec3d dir    = tPos.subtract(eye).normalize();
        float targetYaw   = (float)(Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90.0);
        float targetPitch = (float)(-Math.toDegrees(Math.asin(MathHelper.clamp(dir.y,-1.0,1.0))));

        // Apply Mouse GCD to make rotation physically possible for anticheats
        float f = mc.options.getMouseSensitivity().getValue().floatValue() * 0.6F + 0.2F;
        float gcd = f * f * f * 8.0F * 0.15F;

        float deltaYaw = targetYaw - self.getYaw();
        float deltaPitch = targetPitch - self.getPitch();

        deltaYaw -= deltaYaw % gcd;
        deltaPitch -= deltaPitch % gcd;

        float tYaw = self.getYaw() + deltaYaw;
        float tPitch = self.getPitch() + deltaPitch;

        self.setYaw(tYaw);
        self.setPitch(tPitch);
        self.prevYaw   = self.getYaw();
        self.prevPitch = self.getPitch();

        // We physically snap point to the target and let vanilla sync natively.
        // Return target so ping helper properly uses delay.
        
        return target;
    }
}
