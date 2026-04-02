package com.vladmarica.betterpingdisplay.display;

import com.vladmarica.betterpingdisplay.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class PingSmoothing {

    public static final PingSmoothing INSTANCE = new PingSmoothing();

    private float elytraBlend = 0f;
    private PlayerEntity lockedTarget = null;

    // Smoothed rotation accumulators — carries between frames for smooth feel
    private float smoothYaw   = 0f;
    private float smoothPitch = 0f;

    public void onUpdateMouse(MinecraftClient mc, Config cfg) {
        if (!cfg.isPingSmoothingEnabled()) return;
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) { smoothYaw=0; smoothPitch=0; return; }

        PlayerEntity target = getTarget(mc, cfg);
        if (target == null) {
            lockedTarget = null;
            // Smooth release — decay toward zero
            smoothYaw   *= 0.7f;
            smoothPitch *= 0.7f;
            if (Math.abs(smoothYaw) > 0.001f || Math.abs(smoothPitch) > 0.001f) {
                mc.player.setYaw(mc.player.getYaw()   + smoothYaw   * 0.3f);
                mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + smoothPitch * 0.3f, -90f, 90f));
            }
            return;
        }
        lockedTarget = target;

        // Elytra blend
        float tb = target.isFallFlying() ? 1f : 0f;
        elytraBlend = elytraBlend + (tb - elytraBlend) * 0.08f;

        // Get aim box — use static hitbox if available, else real box
        Box aimBox = getAimBox(target, cfg);
        Vec3d aimPos = getAimPos(aimBox, target, cfg.getPingSmoothingTargetZone());

        Vec3d eye = mc.player.getEyePos();
        double dx = aimPos.x-eye.x, dy = aimPos.y-eye.y, dz = aimPos.z-eye.z;
        double h  = Math.sqrt(dx*dx+dz*dz);

        float tYaw   = (float)(Math.toDegrees(Math.atan2(dz,dx))-90.0);
        float tPitch = (float)(-Math.toDegrees(Math.atan2(dy,h)));

        float curYaw   = mc.player.getYaw();
        float curPitch = mc.player.getPitch();

        float dYaw   = MathHelper.wrapDegrees(tYaw   - curYaw);
        float dPitch = MathHelper.wrapDegrees(tPitch - curPitch);

        float strength = cfg.getPingSmoothingStrength();

        // Target delta per frame
        float targetYawStep   = dYaw   * (0.04f + strength * 0.05f);
        float targetPitchStep = dPitch * (0.04f + strength * 0.05f);

        // Hard cap — never overshoot
        if (Math.abs(targetYawStep)   > Math.abs(dYaw))   targetYawStep   = dYaw;
        if (Math.abs(targetPitchStep) > Math.abs(dPitch)) targetPitchStep = dPitch;

        // Low alpha = high inertia = buttery smooth magnetic feel
        float alpha = 0.08f + strength * 0.07f;
        smoothYaw   = smoothYaw   * (1f - alpha) + targetYawStep   * alpha;
        smoothPitch = smoothPitch * (1f - alpha) + targetPitchStep * alpha;

        // Deadzone
        if (Math.abs(dYaw)   < 0.015f) smoothYaw   = 0f;
        if (Math.abs(dPitch) < 0.015f) smoothPitch = 0f;
        if (Math.abs(smoothYaw) < 0.001f && Math.abs(smoothPitch) < 0.001f) return;

        mc.player.setYaw(curYaw   + smoothYaw);
        mc.player.setPitch(MathHelper.clamp(curPitch + smoothPitch, -90f, 90f));
    }

    // Returns static hitbox if enabled, else real bounding box
    private Box getAimBox(PlayerEntity target, Config cfg) {
        if (cfg.isStaticHitboxEnabled()
                && target instanceof AbstractClientPlayerEntity acp
                && PingHelper.hiddenBoxes.containsKey(acp)) {
            return PingHelper.hiddenBoxes.get(acp);
        }
        return target.getBoundingBox();
    }

    private Vec3d getAimPos(Box box, PlayerEntity target, int zone) {
        double cx = (box.minX+box.maxX)/2.0;
        double cz = (box.minZ+box.maxZ)/2.0;
        double h  = box.maxY - box.minY;

        // For elytra: always aim at center of actual hitbox regardless of zone
        if (target.isFallFlying() && elytraBlend > 0.3f) {
            return new Vec3d(cx, box.minY + h*0.5, cz);
        }

        double aimY = switch (zone) {
            case 0 -> box.minY + h*0.90; // head
            case 2 -> box.minY + h*0.15; // legs
            default-> box.minY + h*0.60; // body
        };
        return new Vec3d(cx, aimY, cz);
    }

    private PlayerEntity getTarget(MinecraftClient mc, Config cfg) {
        float range = cfg.getPingSmoothingRange();
        float fov   = cfg.getPingSmoothingFov();
        boolean wallCheck = !cfg.isPingSmoothingWall(); // true = NO wall aim

        if (cfg.isPingSmoothingSticky() && lockedTarget != null
                && lockedTarget.isAlive()
                && mc.player.distanceTo(lockedTarget) <= range+1.5f) {
            // Re-check wall for sticky target
            if (wallCheck && hasWall(mc, lockedTarget)) return null;
            return lockedTarget;
        }

        PlayerEntity best = null;
        float bestAngle   = Float.MAX_VALUE;

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p==mc.player||!p.isAlive()) continue;
            // Skip whitelisted friends
            if (cfg.isDisplayFriend(p.getName().getString())) continue;
            float dist = mc.player.distanceTo(p);
            if (dist<0.5f||dist>range) continue;

            // Wall check
            if (wallCheck && hasWall(mc, p)) continue;

            Box aimBox = getAimBox(p, cfg);
            Vec3d aim  = getAimPos(aimBox, p, cfg.getPingSmoothingTargetZone());
            Vec3d dir  = aim.subtract(mc.player.getEyePos()).normalize();
            float aY   = (float)(Math.toDegrees(Math.atan2(dir.z,dir.x))-90);
            float aP   = (float)(-Math.toDegrees(Math.asin(MathHelper.clamp(dir.y,-1,1))));
            float adY  = Math.abs(MathHelper.wrapDegrees(aY-mc.player.getYaw()));
            float adP  = Math.abs(MathHelper.wrapDegrees(aP-mc.player.getPitch()));
            float ang  = (float)Math.sqrt(adY*adY+adP*adP);
            if (fov<360f&&ang>fov/2f) continue;
            if (ang<bestAngle){bestAngle=ang;best=p;}
        }
        return best;
    }

    // Returns true if there's a block between player and target
    private boolean hasWall(MinecraftClient mc, PlayerEntity target) {
        Vec3d eye    = mc.player.getEyePos();
        Vec3d tPos   = target.getEyePos();
        BlockHitResult hit = mc.world.raycast(new RaycastContext(
            eye, tPos,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            mc.player
        ));
        return hit.getType() == HitResult.Type.BLOCK;
    }

    public PlayerEntity getLockedTarget() { return lockedTarget; }
}
