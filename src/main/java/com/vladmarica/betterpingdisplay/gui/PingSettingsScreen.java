package com.vladmarica.betterpingdisplay.gui;

import com.vladmarica.betterpingdisplay.BetterPingDisplayMod;
import com.vladmarica.betterpingdisplay.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

import java.io.IOException;

public class PingSettingsScreen extends Screen {

    private final Screen parent;
    private final Config config;

    public PingSettingsScreen(Screen parent) {
        super(Text.literal("Better Ping Display"));
        this.parent = parent;
        this.config = BetterPingDisplayMod.instance().getConfig();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;
        int btnW = 200;
        int btnH = 20;
        int gap = 26;

        // Auto-color toggle
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.shouldAutoColorPingText())
                .build(centerX - btnW / 2, startY, btnW, btnH,
                        Text.literal("Auto Color Ping"),
                        (btn, val) -> config.setShouldAutoColorPingText(val)));

        // Show ping bars toggle
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.shouldRenderPingBars())
                .build(centerX - btnW / 2, startY + gap, btnW, btnH,
                        Text.literal("Show Ping Bars"),
                        (btn, val) -> config.setShouldRenderPingBars(val)));

        // Done button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> {
            saveAndClose();
        }).dimensions(centerX - 75, startY + gap * 3, 150, btnH).build());
    }

    private void saveAndClose() {
        try {
            config.writeToFile(BetterPingDisplayMod.instance().getConfigFilePath().toFile());
        } catch (IOException e) {
            BetterPingDisplayMod.LOGGER.warn("Failed to save config", e);
        }
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 90, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        saveAndClose();
    }
}
