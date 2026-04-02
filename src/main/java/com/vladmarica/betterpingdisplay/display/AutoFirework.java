package com.vladmarica.betterpingdisplay.display;

import com.vladmarica.betterpingdisplay.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class AutoFirework {

    public static final AutoFirework INSTANCE = new AutoFirework();
    private long lastFireTime = 0;
    private int state = 0;

    public void tick(MinecraftClient mc, Config cfg) {
        // Keybind toggle support (keyboard + mouse)
        if (!cfg.isAutoFireworkEnabled() && !cfg.isAutoFireworkActive()) return;
        if (!cfg.isAutoFireworkEnabled()) return;
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        
        // Tick 2: Revert server slot
        if (state == 1) {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            state = 0;
            return;
        }

        if (!mc.player.isFallFlying()) return;
        // Don't fire while sprinting flag would be sent — wait for stable elytra
        if (mc.player.isOnGround()) return;
        
        long now   = System.currentTimeMillis();
        long delay = Math.max(50, (long)(cfg.getAutoFireworkDelay() * 1000f));
        if (now - lastFireTime < delay) return;

        var inv = mc.player.getInventory();

        // Search hotbar ONLY for silent swaps
        int fwSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).getItem() instanceof FireworkRocketItem) { fwSlot = i; break; }
        }
        
        if (fwSlot == -1) return;

        int oldSlot = inv.selectedSlot;

        // 1. Packet to server
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(fwSlot));
        
        // 2. Temporarily set local for Interaction sequence resolving
        inv.selectedSlot = fwSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        
        // 3. Immediately revert local visually so no frame is drawn with the Firework
        inv.selectedSlot = oldSlot;
        
        // 4. Mark pending server revert
        state = 1;
        lastFireTime = now;
    }
}
