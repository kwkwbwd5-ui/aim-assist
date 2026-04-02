package com.vladmarica.betterpingdisplay.display;

import com.vladmarica.betterpingdisplay.BetterPingDisplayMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import com.vladmarica.betterpingdisplay.display.SnapAim;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public class PingHelper {

    private long lastSwordHit = 0;
    private long lastAxeHit   = 0;
    private int  currentSwordDelay;
    private int  currentAxeDelay;
    private final Random random = new Random();

    private Entity lastSeenEntity = null;
    private long   lastSeenTime   = 0L;
    private static final long ELYTRA_WINDOW_MS = 200L;


    // Pending attack — set by tick(), consumed by GameRendererMixin in same frame
    public static volatile Entity pendingAttack = null;

    // Hidden hitbox mode: expanded boxes stored here, NOT in entity.getBoundingBox()
    public static final Map<AbstractClientPlayerEntity, Box> hiddenBoxes = new HashMap<>();

    public static void updateHiddenHitboxes(net.minecraft.client.MinecraftClient client,
            com.vladmarica.betterpingdisplay.Config cfg) {
        hiddenBoxes.clear();
        if (!cfg.isStaticHitboxEnabled() || !cfg.isStaticHitboxHide()) return;
        if (client.world == null) return;
        float tw = cfg.getStaticHitboxWidth();
        float th = cfg.getStaticHitboxHeight();
        for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
            if (player == client.player) continue;
            if (!player.isFallFlying() && !player.isSwimming() && !player.isSleeping()) continue;
            Box box = player.getBoundingBox();
            double cx = (box.minX + box.maxX) / 2.0;
            double cy = (box.minY + box.maxY) / 2.0;
            double cz = (box.minZ + box.maxZ) / 2.0;
            hiddenBoxes.put(player, new Box(cx-tw/2.0, cy-th/2.0, cz-tw/2.0, cx+tw/2.0, cy+th/2.0, cz+tw/2.0));
        }
    }

        public PingHelper() {
        currentSwordDelay = randomDelay(540, 550);
        currentAxeDelay   = randomDelay(780, 800);
    }

    public void tick(MinecraftClient client) {
        try {
            if (client.player == null || client.world == null) return;

            var cfg = BetterPingDisplayMod.instance().getConfig();
            if (!cfg.isDisplayEnabled()) return;
            if (client.currentScreen != null) return;

            Item item = client.player.getMainHandStack().getItem();

            // ── Hotbar slot filter ────────────────────────────────────
            int slot = cfg.getDisplayHotbarSlot();
            if (slot != 0 && client.player.getInventory().selectedSlot != (slot - 1)) return;

            // ── Skip while using item (right click held) ──────────────
            if (GLFW.glfwGetMouseButton(client.getWindow().getHandle(),
                    GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS) return;

            // ── Find target entity ────────────────────────────────────
            Entity entity = null;

            if (client.crosshairTarget instanceof EntityHitResult hit
                    && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                Entity e = hit.getEntity();
                if (e != null && e != client.player) {
                    entity = e;
                    lastSeenEntity = e;
                    lastSeenTime   = System.currentTimeMillis();
                }
            }

            // ── Expand Hitbox + Snap Aim ──────────────────────────────
            if (entity == null) {
                entity = SnapAim.INSTANCE.findAndSnap(client, cfg);
            }

            // Elytra fallback
            if (entity == null) {
                double speed = client.player.getVelocity().length();
                if (speed > 1.0) {
                    float range = cfg.getDisplayRange();
                    Vec3d pos   = client.player.getEyePos();
                    Vec3d look  = client.player.getRotationVec(1.0f);
                    Vec3d end   = pos.add(look.multiply(range));
                    Box   area  = client.player.getBoundingBox().expand(range);
                    for (Entity candidate : client.world.getEntitiesByClass(
                            LivingEntity.class, area, e -> e != client.player && e.isAlive())) {
                        // Use hidden expanded box if available, else normal box
                        // Get base box — use static hitbox if available
                        Box checkBox = (candidate instanceof AbstractClientPlayerEntity acp && hiddenBoxes.containsKey(acp))
                            ? hiddenBoxes.get(acp) : candidate.getBoundingBox();
                        // Apply expand hitbox (client-side only, no packet sent)
                        if (cfg.isExpandHitboxEnabled() && cfg.getExpandHitboxSize() > 0) {
                            double exp = cfg.getExpandHitboxSize();
                            checkBox = checkBox.expand(exp, exp, exp);
                        }
                        if (checkBox.raycast(pos, end).isPresent()) {
                            entity = candidate;
                            lastSeenEntity = entity;
                            lastSeenTime   = System.currentTimeMillis();
                            break;
                        }
                    }
                }
            }

            if (entity == null && lastSeenEntity != null
                    && System.currentTimeMillis() - lastSeenTime <= ELYTRA_WINDOW_MS) {
                entity = lastSeenEntity;
            }

            if (entity == null) return;

            // ── Range check ───────────────────────────────────────────
            float range = Math.min(cfg.getDisplayRange(), 2.85f); // Safely hardcap below vanilla 3.0 server limit
            if (client.player.distanceTo(entity) > range) return;

            // ── Line of sight ─────────────────────────────────────────
            Vec3d eyePos    = client.player.getEyePos();
            Vec3d targetPos = entity.getEyePos();
            BlockHitResult blockHit = client.world.raycast(new RaycastContext(
                eyePos, targetPos,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                client.player
            ));
            if (blockHit.getType() == HitResult.Type.BLOCK) return;

            // ── Target filter ─────────────────────────────────────────
            boolean valid = (entity instanceof PlayerEntity)
                    || (cfg.isDisplayStrayBypass() && entity instanceof ZombieEntity)
                    || (cfg.isDisplayAllEntities() && entity instanceof LivingEntity);
            if (!valid) return;

            if (entity instanceof PlayerEntity p) {
                if (cfg.isDisplayFriend(p.getGameProfile().getName())) return;
            }

            boolean allItems = cfg.isDisplayAllItems();
            long    now      = System.currentTimeMillis();
            final Entity finalEntity = entity;

            if (!allItems) {
                if (item instanceof SwordItem) {
                    if (now - lastSwordHit >= currentSwordDelay) {
                        scheduleAttack(client, finalEntity, cfg.isDisplaySwing());
                        lastSwordHit = now;
                        currentSwordDelay = randomDelay(
                            cfg.getDisplaySwordDelayMin(), cfg.getDisplaySwordDelayMax());
                    }
                } else if (item instanceof AxeItem) {
                    if (now - lastAxeHit >= currentAxeDelay) {
                        scheduleAttack(client, finalEntity, cfg.isDisplaySwing());
                        lastAxeHit = now;
                        currentAxeDelay = randomDelay(
                            cfg.getDisplaySwordDelayMin(), cfg.getDisplaySwordDelayMax());
                    }
                }
            } else {
                if (now - lastSwordHit >= currentSwordDelay) {
                    scheduleAttack(client, finalEntity, cfg.isDisplaySwing());
                                lastSwordHit = now;
                    currentSwordDelay = randomDelay(
                        cfg.getDisplaySwordDelayMin(), cfg.getDisplaySwordDelayMax());
                }
            }

        } catch (Exception ignored) {}
    }

    /**
     * Sets crosshairTarget to the entity and calls the game's own doAttack()
     * via GameRendererMixin — identical packet sequence to a real left click.
     */
    private void scheduleAttack(MinecraftClient mc, Entity target, boolean swing) {
        pendingAttack = target;
    }

    /**
     * Prioritize Crits logic:
     * - prioritize=false: attack normally regardless of crit state
     * - prioritize=true:
     *     • Already falling → attack now (guaranteed crit)
     *     • On ground → jump then wait for falling window → attack (guaranteed crit)
     *     • Sprinting/can't crit → attack normally (don't block the hit)
     */
    private int randomDelay(int lo, int hi) {
        if (lo >= hi) return lo;
        return lo + random.nextInt(hi - lo + 1);
    }
}
