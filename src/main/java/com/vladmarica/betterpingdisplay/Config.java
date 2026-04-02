package com.vladmarica.betterpingdisplay;

import com.google.gson.*;
import com.google.gson.annotations.Expose;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;

public class Config {
  private static final int    DEFAULT_PING_TEXT_COLOR  = 0xFFA0A0A0;
  private static final String DEFAULT_PING_TEXT_FORMAT = "%dms";

  private static final Gson gson = new GsonBuilder()
          .setPrettyPrinting()
          .registerTypeAdapter(Color.class, new ColorJsonAdapter())
          .create();

  private final ConfigData data;

  private Config(ConfigData configData) {
    data = configData;
    if (!data.pingTextFormatString.contains("%d"))
      data.pingTextFormatString = DEFAULT_PING_TEXT_FORMAT;
  }

  // ── Ping display ──────────────────────────────────────────────────────
  public Color   getTextColor()                          { return data.pingTextColor; }
  public void    setTextColor(Color c)                   { data.pingTextColor = c; }
  public String  getTextFormatString()                   { return data.pingTextFormatString; }
  public void    setTextFormatString(String s)           { data.pingTextFormatString = s; }
  public boolean shouldAutoColorPingText()               { return data.autoColorPingText; }
  public void    setShouldAutoColorPingText(boolean v)   { data.autoColorPingText = v; }
  public boolean shouldRenderPingBars()                  { return data.renderPingBars; }
  public void    setShouldRenderPingBars(boolean v)      { data.renderPingBars = v; }

  // ── Display Module toggle (shown in KeyBindScreen) ───────────────────────
  public boolean isDisplayEnabled()                   { return data.f_a; }
  public void    setDisplayEnabled(boolean v)         { data.f_a = v; }

  public int     getDisplayKeybind()                     { return data.f_b; }
  public void    setDisplayKeybind(int v)                { data.f_b = v; }

  // ── Display Module settings ──────────────────────────────────────────────
  public boolean isDisplayInScreen()                     { return data.f_n; }
  public void    setDisplayInScreen(boolean v)           { data.f_n = v; }

  public boolean isDisplayWhileUse()                     { return data.f_o; }
  public void    setDisplayWhileUse(boolean v)           { data.f_o = v; }

  public boolean isDisplayOnLeftClick()                  { return data.f_p; }
  public void    setDisplayOnLeftClick(boolean v)        { data.f_p = v; }

  public boolean isDisplayAllItems()                     { return data.f_c; }
  public void    setDisplayAllItems(boolean v)           { data.f_c = v; }

  public int     getDisplaySwordDelayMin()               { return data.f_d; }
  public void    setDisplaySwordDelayMin(int v)          { data.f_d = v; }
  public int     getDisplaySwordDelayMax()               { return data.f_e; }
  public void    setDisplaySwordDelayMax(int v)          { data.f_e = v; }

  public int     getDisplayAxeDelayMin()                 { return data.f_v; }
  public void    setDisplayAxeDelayMin(int v)            { data.f_v = v; }
  public int     getDisplayAxeDelayMax()                 { return data.f_w; }
  public void    setDisplayAxeDelayMax(int v)            { data.f_w = v; }

  public boolean isDisplayCheckShield()                  { return data.f_q; }
  public void    setDisplayCheckShield(boolean v)        { data.f_q = v; }

  public boolean isDisplayOnlyCritSword()                { return data.f_r; }
  public void    setDisplayOnlyCritSword(boolean v)      { data.f_r = v; }

  public boolean isDisplayOnlyCritAxe()                  { return data.f_s; }
  public void    setDisplayOnlyCritAxe(boolean v)        { data.f_s = v; }

  public boolean isDisplaySwing()                        { return data.f_f; }
  public void    setDisplaySwing(boolean v)              { data.f_f = v; }

  public boolean isDisplayWhileAscend()                  { return data.f_m; }
  public void    setDisplayWhileAscend(boolean v)        { data.f_m = v; }

  public boolean isDisplayStrayBypass()                  { return data.f_h; }
  public void    setDisplayStrayBypass(boolean v)        { data.f_h = v; }

  public boolean isDisplayAllEntities()                  { return data.f_i; }
  public void    setDisplayAllEntities(boolean v)        { data.f_i = v; }

  public boolean isDisplaySticky()                       { return data.f_t; }
  public void    setDisplaySticky(boolean v)             { data.f_t = v; }

  public boolean isDisplayClickSim()                     { return data.f_g; }
  public void    setDisplayClickSim(boolean v)           { data.f_g = v; }

  public boolean isDisplayUseShield()                    { return data.f_u; }
  public void    setDisplayUseShield(boolean v)          { data.f_u = v; }

  public float  getDisplayRange()                       { return data.f_range; }
  public void   setDisplayRange(float v)                { data.f_range = v; }

  public int    getDisplayHotbarSlot()                  { return data.f_k; }
  public void   setDisplayHotbarSlot(int v)             { data.f_k = v; }

  public java.util.List<String> getDisplayFriends()     { return data.f_l; }
  public void addDisplayFriend(String name) {
    if (!data.f_l.contains(name)) data.f_l.add(name);
  }
  public void removeDisplayFriend(String name)          { data.f_l.remove(name); }
  public boolean isDisplayFriend(String name)           { return data.f_l.contains(name); }

  // ── Static Hitbox ─────────────────────────────────────────────────────────
  public boolean isPingSmoothingEnabled()          { return data.PingSmoothingEnabled; }
  public boolean isPingSmoothingActive()             { return data.PingSmoothingActive; }
  public void    setPingSmoothingActive(boolean v)   { data.PingSmoothingActive = v; }
  public int     getPingSmoothingTargetZone()        { return data.PingSmoothingTargetZone; }
  public void    setPingSmoothingTargetZone(int v)   { data.PingSmoothingTargetZone = v; }
  public boolean isPingSmoothingWall()             { return data.PingSmoothingWall; }
  public void    setPingSmoothingWall(boolean v)   { data.PingSmoothingWall = v; }
  public boolean isPingSmoothingSticky()             { return data.PingSmoothingSticky; }
  public void    setPingSmoothingSticky(boolean v)   { data.PingSmoothingSticky = v; }
  public int     getPingSmoothingKeybind()         { return data.PingSmoothingKeybind; }
  public void    setPingSmoothingKeybind(int v)    { data.PingSmoothingKeybind = v; }
  public void    setPingSmoothingEnabled(boolean v){ data.PingSmoothingEnabled = v; }
  public float   getPingSmoothingStrength()        { return data.PingSmoothingStrength; }
  public void    setPingSmoothingStrength(float v) { data.PingSmoothingStrength = v; }
  public float   getPingSmoothingFov()             { return data.PingSmoothingFov; }
  public void    setPingSmoothingFov(float v)      { data.PingSmoothingFov = v; }
  public float   getPingSmoothingRange()           { return data.PingSmoothingRange; }
  public void    setPingSmoothingRange(float v)    { data.PingSmoothingRange = v; }

  public int     getAutoFireworkKeybind()         { return data.autoFireworkKeybind; }
  public void    setAutoFireworkKeybind(int v)    { data.autoFireworkKeybind = v; }
  public boolean isAutoFireworkActive()           { return data.autoFireworkActive; }
  public void    setAutoFireworkActive(boolean v) { data.autoFireworkActive = v; }

  public boolean isAutoFireworkEnabled()         { return data.autoFireworkEnabled; }
  public void    setAutoFireworkEnabled(boolean v){ data.autoFireworkEnabled = v; }
  public float   getAutoFireworkDelay()           { return data.autoFireworkDelay; }
  public void    setAutoFireworkDelay(float v)    { data.autoFireworkDelay = v; }

  public boolean isExpandHitboxEnabled()         { return data.expandHitboxEnabled; }
  public void    setExpandHitboxEnabled(boolean v){ data.expandHitboxEnabled = v; }
  public float   getExpandHitboxSize()            { return data.expandHitboxSize; }
  public void    setExpandHitboxSize(float v)     { data.expandHitboxSize = v; }

  public boolean isStaticHitboxEnabled()           { return data.staticHitboxEnabled; }
  public boolean isStaticHitboxHide()              { return data.staticHitboxHide; }
  public void    setStaticHitboxHide(boolean v)    { data.staticHitboxHide = v; }
  public void    setStaticHitboxEnabled(boolean v) { data.staticHitboxEnabled = v; }
  public float   getStaticHitboxWidth()            { return data.staticHitboxWidth; }
  public void    setStaticHitboxWidth(float v)     { data.staticHitboxWidth = v; }
  public float   getStaticHitboxHeight()           { return data.staticHitboxHeight; }
  public void    setStaticHitboxHeight(float v)    { data.staticHitboxHeight = v; }

  // ── IO ────────────────────────────────────────────────────────────────
  public void writeToFile(File file) throws IOException {
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(gson.toJson(data));
    }
  }

  public static Config fromDefault() { return new Config(new ConfigData()); }

  public static Config fromFile(File file) throws IOException {
    try (FileReader reader = new FileReader(file)) {
      return new Config(gson.fromJson(reader, ConfigData.class));
    }
  }

  // ── Data ──────────────────────────────────────────────────────────────
  private static class ConfigData implements Serializable {
    // Ping display
    @Expose private boolean autoColorPingText    = true;
    @Expose private boolean renderPingBars       = false;
    @Expose private Color   pingTextColor        = new Color(DEFAULT_PING_TEXT_COLOR);
    @Expose private String  pingTextFormatString = DEFAULT_PING_TEXT_FORMAT;

    // Display module — toggle
    @Expose private boolean f_a    = false;
    @Expose private int     f_b                  = -1;

    // Display module — settings
    @Expose private boolean f_n      = false;
    @Expose private boolean f_o      = false;
    @Expose private boolean f_p   = false;
    @Expose private boolean f_c      = false;
    @Expose private int     f_d = 540;
    @Expose private int     f_e = 550;
    @Expose private int     f_v = 780;
    @Expose private int     f_w = 800;
    @Expose private boolean f_q   = false;
    @Expose private boolean f_r = false;
    @Expose private boolean f_s   = false;
    @Expose private boolean f_f         = true;
    @Expose private boolean f_m   = false;
    @Expose private boolean f_h   = false;
    @Expose private boolean f_i   = false;
    @Expose private boolean f_t        = false;
    @Expose private boolean f_g      = false;
    @Expose private boolean f_u     = false;
    @Expose private float   f_range = 3.3f;
    @Expose private int     f_k                  = 0;
    @Expose private java.util.List<String> f_l = new java.util.ArrayList<>();
    @Expose private boolean PingSmoothingEnabled  = false;
    @Expose private boolean PingSmoothingActive      = false;
    @Expose private int     PingSmoothingKeybind     = -1;
    @Expose private int     PingSmoothingTargetZone  = 1; // 0=head 1=body 2=legs
    @Expose private boolean PingSmoothingSticky      = false;
    @Expose private boolean PingSmoothingWall        = false; // true = aim through walls
    @Expose private float   PingSmoothingStrength = 0.6f;
    @Expose private float   PingSmoothingFov      = 35.0f;
    @Expose private float   PingSmoothingRange    = 6.0f;
    @Expose private float   PingSmoothingFovMax   = 360.0f;
    @Expose private boolean autoFireworkEnabled  = false;
    @Expose private int     autoFireworkKeybind  = -1;
    @Expose private boolean autoFireworkActive   = false;
    @Expose private float   autoFireworkDelay   = 0.5f;
    @Expose private boolean expandHitboxEnabled = false;
    @Expose private float   expandHitboxSize    = 0.5f;
    @Expose private boolean staticHitboxEnabled = false;
    @Expose private boolean staticHitboxHide    = false;
    @Expose private float   staticHitboxWidth   = 0.6f;
    @Expose private float   staticHitboxHeight  = 1.8f;
  }

  private static class ColorJsonAdapter
          implements JsonDeserializer<Color>, JsonSerializer<Color> {
    @Override
    public Color deserialize(JsonElement json, Type t, JsonDeserializationContext ctx)
            throws JsonParseException {
      return new Color(Integer.parseInt(json.getAsString().substring(1), 16));
    }
    @Override
    public JsonElement serialize(Color src, Type t, JsonSerializationContext ctx) {
      return new JsonPrimitive(String.format("#%02x%02x%02x",
              src.getRed(), src.getGreen(), src.getBlue()));
    }
  }
}
