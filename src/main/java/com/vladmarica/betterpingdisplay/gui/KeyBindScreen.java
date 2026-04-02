package com.vladmarica.betterpingdisplay.gui;

import com.vladmarica.betterpingdisplay.BetterPingDisplayMod;
import com.vladmarica.betterpingdisplay.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.io.IOException;

public class KeyBindScreen extends Screen {

    private final Screen parent;
    private final KeySequenceHandler keyHandler;
    private final Config config;

    private static final int W = 280, H = 235;
    private int wx, wy;
    private int tab = 0;
    private int dragging = 0;
    private int listening = 0;
    private String friendInput = "";
    private boolean typingFriend = false;

    private static final int IH=13, CB=7, SW=120, SH=3, HR=3;
    private static final int DELAY_MIN=0, DELAY_MAX=1000;
    private static final float RANGE_MIN=0.1f, RANGE_MAX=6.5f;

    private static final int BG=0xF0111115, PANEL=0xFF1A1A20, BORDER=0xFF303040;
    private static final int TAB_SEL=0xFF222230, TAB_OFF=0xFF151518;
    private static final int ACCENT=0xFFD04858, ON=0xFF4CC870, OFF_CB=0xFF252530;
    private static final int TEXT=0xFFCCCCDD, TEXT_DIM=0xFF666680, TEXT_HDR=0xFF9090B0;
    private static final int SL_TRACK=0xFF252530, SL_FILL=0xFF9040D0, SL_KNOB=0xFFDDDDEE;
    private static final int BTN_BG=0xFF202028, BTN_HV=0xFF2A2A38, BTN_BD=0xFF383848;

    public KeyBindScreen(Screen parent, KeySequenceHandler keyHandler) {
        super(Text.literal("BPD Client"));
        this.parent=parent; this.keyHandler=keyHandler;
        this.config=BetterPingDisplayMod.instance().getConfig();
    }

    @Override protected void init() { wx=(width-W)/2; wy=(height-H)/2; }

    private void saveAndClose() {
        try{config.writeToFile(BetterPingDisplayMod.instance().getConfigFilePath().toFile());}catch(IOException e){}
        if(client!=null)client.setScreen(parent);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0,0,width,height,0x80000000);
        ctx.fill(wx,wy,wx+W,wy+H,BG);
        for(int i=0;i<W;i++){float t=(float)i/W;int r=(int)(0xD0*(1-t)+0x78*t),g=(int)(0x48*(1-t)+0x40*t),b=(int)(0x58*(1-t)+0xC8*t);ctx.fill(wx+i,wy,wx+i+1,wy+2,0xFF000000|(r<<16)|(g<<8)|b);}
        ctx.fill(wx,wy+2,wx+W,wy+3,BORDER);ctx.fill(wx,wy+H-1,wx+W,wy+H,BORDER);
        ctx.fill(wx,wy+2,wx+1,wy+H,BORDER);ctx.fill(wx+W-1,wy+2,wx+W,wy+H,BORDER);
        ctx.fill(wx+1,wy+2,wx+W-1,wy+20,PANEL);
        ctx.drawTextWithShadow(textRenderer,Text.literal("BPD ").append(Text.literal("Client").withColor(ACCENT)),wx+7,wy+6,TEXT);
        boolean ch=in(mx,my,wx+W-15,wy+4,11,12);
        ctx.fill(wx+W-15,wy+4,wx+W-4,wy+16,ch?0xFFAA2222:BTN_BG);
        ctx.drawCenteredTextWithShadow(textRenderer,Text.literal("×"),wx+W-9,wy+6,ch?0xFFFFFFFF:TEXT_DIM);

        // Tabs
        String[] tabs={"Trigger","Ping Smooth","Hitbox"};
        int tabW=(W-2)/3, tabY=wy+20;
        for(int i=0;i<3;i++){
            int tx=wx+1+i*tabW; boolean sel=tab==i, hov=!sel&&in(mx,my,tx,tabY,tabW,16);
            ctx.fill(tx,tabY,tx+tabW,tabY+16,sel?TAB_SEL:hov?BTN_HV:TAB_OFF);
            if(sel)ctx.fill(tx,tabY+15,tx+tabW,tabY+16,ACCENT);
            ctx.drawCenteredTextWithShadow(textRenderer,Text.literal(tabs[i]),tx+tabW/2,tabY+4,sel?TEXT:TEXT_DIM);
        }
        ctx.fill(wx+1,tabY+16,wx+W-1,tabY+17,BORDER);

        // Content — all Y values relative to wy
        int lx=wx+10, c2=wx+10+140;
        switch(tab){
            case 0 -> renderTrigger(ctx,lx,c2,mx,my);
            case 1 -> renderPingSmoothing(ctx,lx,c2,mx,my);
            case 2 -> renderHitbox(ctx,lx,mx,my);
        }
        super.render(ctx,mx,my,delta);
    }

    // ── TRIGGER ───────────────────────────────────────────────────────────
    private void renderTrigger(DrawContext ctx, int lx, int c2, int mx, int my) {
        // Left column — exact Y values from calculation
        hdr(ctx,lx,wy+57,"SETTINGS");
        cb(ctx,lx,wy+68,"All Items",   config.isDisplayAllItems(),   mx,my);
        cb(ctx,lx,wy+81,"Swing Hand",  config.isDisplaySwing(),      mx,my);
        cb(ctx,lx,wy+94,"Stray Bypass",config.isDisplayStrayBypass(),mx,my);
        cb(ctx,lx,wy+107,"All Entities",config.isDisplayAllEntities(),mx,my);
        hdr(ctx,lx,wy+124,"HOTBAR");
        // Hotbar buttons
        int hbX=lx, slot=config.getDisplayHotbarSlot();
        String[] hl={"All","1","2","3","4","5","6","7","8","9"};
        for(int i=0;i<hl.length;i++){
            int bw=textRenderer.getWidth(hl[i])+5;
            if(hbX+bw>wx+W-10)break;
            boolean sel=slot==i, hov=!sel&&mx>=hbX&&mx<=hbX+bw&&my>=wy+134&&my<=wy+144;
            ctx.fill(hbX,wy+135,hbX+bw,wy+144,sel?ACCENT:hov?BTN_HV:BTN_BG);
            ctx.drawTextWithShadow(textRenderer,Text.literal(hl[i]),hbX+3,wy+136,sel?0xFFFFFF:TEXT_DIM);
            hbX+=bw+2;
        }

        // Right column
        hdr(ctx,c2,wy+57,"SWORD DELAY");
        int swMin=config.getDisplaySwordDelayMin(), swMax=config.getDisplaySwordDelayMax();
        int hxMin=c2+(int)((float)(swMin-DELAY_MIN)/(DELAY_MAX-DELAY_MIN)*SW);
        int hxMax=c2+(int)((float)(swMax-DELAY_MIN)/(DELAY_MAX-DELAY_MIN)*SW);
        ctx.fill(c2,wy+69,c2+SW,wy+69+SH,SL_TRACK);
        ctx.fill(hxMin,wy+69,hxMax,wy+69+SH,SL_FILL);
        ctx.fill(hxMin-HR,wy+68,hxMin+HR,wy+68+SH+2,SL_KNOB);
        ctx.fill(hxMax-HR,wy+68,hxMax+HR,wy+68+SH+2,SL_KNOB);
        ctx.drawTextWithShadow(textRenderer,Text.literal(swMin+"-"+swMax+"ms"),c2,wy+75,TEXT_DIM);

        hdr(ctx,c2,wy+85,"RANGE  "+String.format("%.1f",config.getDisplayRange()));
        int rhx=c2+(int)((config.getDisplayRange()-RANGE_MIN)/(RANGE_MAX-RANGE_MIN)*SW);
        ctx.fill(c2,wy+97,c2+SW,wy+97+SH,SL_TRACK);
        ctx.fill(c2,wy+97,rhx,wy+97+SH,SL_FILL);
        ctx.fill(rhx-HR,wy+96,rhx+HR,wy+96+SH+2,SL_KNOB);

        // Reset button — below range slider, inside window
        boolean rHov=in(mx,my,c2,wy+102,54,11);
        ctx.fill(c2,wy+102,c2+54,wy+113,rHov?BTN_HV:BTN_BG);
        ctx.fill(c2,wy+102,c2+54,wy+103,BTN_BD);
        ctx.fill(c2,wy+112,c2+54,wy+113,BTN_BD);
        ctx.fill(c2,wy+102,c2+1,wy+113,BTN_BD);
        ctx.fill(c2+53,wy+102,c2+54,wy+113,BTN_BD);
        ctx.drawCenteredTextWithShadow(textRenderer,Text.literal("Reset"),c2+27,wy+104,rHov?TEXT:TEXT_DIM);

        hdr(ctx,c2,wy+111,"KEY SEQUENCE");
        boolean l1=listening==1, l2=listening==2;
        keyBtn(ctx,c2,wy+122,48,getKeyName(keyHandler.getKey1()),l1,mx,my);
        ctx.drawCenteredTextWithShadow(textRenderer,Text.literal("→"),c2+55,wy+125,TEXT_DIM);
        keyBtn(ctx,c2+62,wy+122,48,getKeyName(keyHandler.getKey2()),l2,mx,my);

        hdr(ctx,c2,wy+138,"KEYBIND");
        keyBtn(ctx,c2,wy+149,SW,listening==3?"...":getKeyName(config.getDisplayKeybind()),listening==3,mx,my);
    }

    // ── Ping Smooth ────────────────────────────────────────────────────────
    private void renderPingSmoothing(DrawContext ctx, int lx, int c2, int mx, int my) {
        hdr(ctx,lx,wy+57,"OPTIONS");
        cb(ctx,lx,wy+68,"Enabled",      config.isPingSmoothingEnabled(), mx,my);
        cb(ctx,lx,wy+81,"Sticky Lock",  config.isPingSmoothingSticky(),  mx,my);
        cb(ctx,lx,wy+94,"Through Walls",config.isPingSmoothingWall(),    mx,my);

        hdr(ctx,lx,wy+113,"TARGET ZONE");
        String[] zones={"Head","Body","Legs"}; int zbx=lx, cur=config.getPingSmoothingTargetZone();
        for(int i=0;i<3;i++){
            int bw=textRenderer.getWidth(zones[i])+8;
            boolean sel=cur==i,hov=!sel&&in(mx,my,zbx,wy+124,bw,11);
            ctx.fill(zbx,wy+124,zbx+bw,wy+135,sel?ACCENT:hov?BTN_HV:BTN_BG);
            ctx.fill(zbx,wy+124,zbx+bw,wy+125,sel?ACCENT:BTN_BD);ctx.fill(zbx,wy+134,zbx+bw,wy+135,sel?ACCENT:BTN_BD);
            ctx.fill(zbx,wy+124,zbx+1,wy+135,sel?ACCENT:BTN_BD);ctx.fill(zbx+bw-1,wy+124,zbx+bw,wy+135,sel?ACCENT:BTN_BD);
            ctx.drawCenteredTextWithShadow(textRenderer,Text.literal(zones[i]),zbx+bw/2,wy+126,sel?0xFFFFFF:TEXT_DIM);
            zbx+=bw+3;
        }

        hdr(ctx,lx,wy+139,"WHITELIST");
        boolean hovAdd=in(mx,my,lx,wy+149,14,10);
        ctx.fill(lx,wy+149,lx+14,wy+159,hovAdd?BTN_HV:BTN_BG);
        ctx.drawCenteredTextWithShadow(textRenderer,Text.literal("+"),lx+7,wy+150,ON);
        if(typingFriend){
            ctx.fill(lx+16,wy+149,lx+110,wy+159,0xFF1A1A25);
            ctx.fill(lx+16,wy+149,lx+110,wy+150,BTN_BD);
            ctx.drawTextWithShadow(textRenderer,Text.literal(friendInput+(System.currentTimeMillis()%800<400?"|":"")),lx+18,wy+150,TEXT);
        }
        int fy=wy+161;
        for(String name:config.getDisplayFriends()){
            ctx.drawTextWithShadow(textRenderer,Text.literal("▸ "+name),lx+4,fy,TEXT_DIM);
            int xbx=lx+4+textRenderer.getWidth("▸ "+name)+4;
            ctx.drawTextWithShadow(textRenderer,Text.literal("×"),xbx,fy,in(mx,my,xbx,fy,7,8)?ACCENT:0xFF553333);
            fy+=10;
        }

        // Right sliders — exact positions
        hdr(ctx,c2,wy+57,String.format("STRENGTH  %.0f%%",config.getPingSmoothingStrength()*100));
        sl1(ctx,c2,wy+68,config.getPingSmoothingStrength(),0f,1f);
        hdr(ctx,c2,wy+83,String.format("FOV  %.0f°",config.getPingSmoothingFov()));
        sl1(ctx,c2,wy+94,config.getPingSmoothingFov(),1f,360f);
        hdr(ctx,c2,wy+109,String.format("RANGE  %.0f blk",config.getPingSmoothingRange()));
        sl1(ctx,c2,wy+120,config.getPingSmoothingRange(),1f,30f);
        hdr(ctx,c2,wy+135,"KEYBIND");
        keyBtn(ctx,c2,wy+146,SW,listening==20?"...":getKeyName(config.getPingSmoothingKeybind()),listening==20,mx,my);
    }

    // ── HITBOX ────────────────────────────────────────────────────────────
    private void renderHitbox(DrawContext ctx, int lx, int mx, int my) {
        hdr(ctx,lx,wy+57,"STATIC HITBOX");
        cb(ctx,lx,wy+68,"Enabled",  config.isStaticHitboxEnabled(),mx,my);
        cb(ctx,lx,wy+81,"Hide Mode",config.isStaticHitboxHide(),   mx,my);
        hdr(ctx,lx,wy+102,String.format("WIDTH  %.2f",config.getStaticHitboxWidth()));
        sl1(ctx,lx,wy+113,config.getStaticHitboxWidth(),0.1f,2f);
        hdr(ctx,lx,wy+128,String.format("HEIGHT  %.2f",config.getStaticHitboxHeight()));
        sl1(ctx,lx,wy+139,config.getStaticHitboxHeight(),0.1f,2f);
        boolean rHov=in(mx,my,lx,wy+154,54,13);
        ctx.fill(lx,wy+154,lx+54,wy+167,rHov?BTN_HV:BTN_BG);
        ctx.fill(lx,wy+154,lx+54,wy+155,BTN_BD);ctx.fill(lx,wy+166,lx+54,wy+167,BTN_BD);
        ctx.fill(lx,wy+154,lx+1,wy+167,BTN_BD);ctx.fill(lx+53,wy+154,lx+54,wy+167,BTN_BD);
        ctx.drawCenteredTextWithShadow(textRenderer,Text.literal("Reset"),lx+27,wy+157,rHov?TEXT:TEXT_DIM);

        // Auto Firework section (right column)
        int c2fw=wx+10+140;
        hdr(ctx,c2fw,wy+57,"AUTO FIREWORK");
        cb(ctx,c2fw,wy+68,"Enabled",config.isAutoFireworkEnabled(),mx,my);
        hdr(ctx,c2fw,wy+84,String.format("DELAY  %.1fs",config.getAutoFireworkDelay()));
        sl1(ctx,c2fw,wy+95,config.getAutoFireworkDelay(),0.05f,3f);
        hdr(ctx,c2fw,wy+110,"KEYBIND");
        keyBtn(ctx,c2fw,wy+121,SW,listening==30?"...":getKeyName(config.getAutoFireworkKeybind()),listening==30,mx,my);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────
    private void hdr(DrawContext ctx,int x,int y,String t){ctx.drawTextWithShadow(textRenderer,Text.literal(t),x,y,TEXT_HDR);}
    private void cb(DrawContext ctx,int x,int y,String l,boolean on,int mx,int my){
        ctx.fill(x,y,x+CB,y+CB,on?ACCENT:OFF_CB);
        ctx.fill(x+1,y+1,x+CB-1,y+CB-1,on?ACCENT:0xFF1C1C24);
        if(on){ctx.fill(x+2,y+3,x+3,y+5,0xFFFFFFFF);ctx.fill(x+3,y+4,x+4,y+6,0xFFFFFFFF);ctx.fill(x+4,y+2,x+5,y+5,0xFFFFFFFF);}
        ctx.drawTextWithShadow(textRenderer,Text.literal(l),x+CB+4,y,TEXT);
    }
    private void sl1(DrawContext ctx,int x,int y,float v,float min,float max){
        int hx=x+(int)((v-min)/(max-min)*SW);
        ctx.fill(x,y+1,x+SW,y+1+SH,SL_TRACK);ctx.fill(x,y+1,hx,y+1+SH,SL_FILL);
        ctx.fill(hx-HR,y,hx+HR,y+SH+2,SL_KNOB);
    }
    private void keyBtn(DrawContext ctx,int x,int y,int w,String l,boolean act,int mx,int my){
        boolean hov=!act&&in(mx,my,x,y,w,12);
        ctx.fill(x,y,x+w,y+12,act?0xFF1A3020:hov?BTN_HV:BTN_BG);
        ctx.fill(x,y,x+w,y+1,act?ON:BTN_BD);ctx.fill(x,y+11,x+w,y+12,act?ON:BTN_BD);
        ctx.fill(x,y,x+1,y+12,act?ON:BTN_BD);ctx.fill(x+w-1,y,x+w,y+12,act?ON:BTN_BD);
        ctx.drawCenteredTextWithShadow(textRenderer,Text.literal(l),x+w/2,y+2,act?ON:TEXT);
    }
    private boolean in(double mx,double my,int x,int y,int w,int h){return mx>=x&&mx<=x+w&&my>=y&&my<=y+h;}
    private int toDelay(double mx,int tx){return(int)Math.max(DELAY_MIN,Math.min(DELAY_MAX,DELAY_MIN+(mx-tx)/SW*(DELAY_MAX-DELAY_MIN)));}
    private float toRange(double mx,int tx){return(float)Math.max(RANGE_MIN,Math.min(RANGE_MAX,RANGE_MIN+(mx-tx)/SW*(RANGE_MAX-RANGE_MIN)));}
    private float toSH(double mx,int tx){return(float)Math.max(0.1,Math.min(2.0,0.1+(mx-tx)/SW*1.9));}

    // ── CLICK ─────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx,double my,int btn){
        // Capture mouse buttons for keybind
        if(btn>=1&&listening!=0){setListeningKey(1000+btn);return true;}
        if(btn!=0)return super.mouseClicked(mx,my,btn);
        if(in(mx,my,wx+W-15,wy+4,11,12)){saveAndClose();return true;}
        int tabW=(W-2)/3;
        if(my>=wy+20&&my<=wy+36){int i=(int)((mx-wx-1)/tabW);if(i>=0&&i<3){tab=i;listening=0;dragging=0;return true;}}
        if(tab==0)return click0(mx,my);
        if(tab==1)return click1(mx,my);
        if(tab==2)return click2(mx,my);
        return super.mouseClicked(mx,my,btn);
    }

    private boolean click0(double mx,double my){
        int lx=wx+10, c2=wx+10+140;
        // Checkboxes — exact Y
        if(in(mx,my,lx,wy+68,CB,CB)){config.setDisplayAllItems(!config.isDisplayAllItems());return true;}
        if(in(mx,my,lx,wy+81,CB,CB)){config.setDisplaySwing(!config.isDisplaySwing());return true;}
        if(in(mx,my,lx,wy+94,CB,CB)){config.setDisplayStrayBypass(!config.isDisplayStrayBypass());return true;}
        if(in(mx,my,lx,wy+107,CB,CB)){config.setDisplayAllEntities(!config.isDisplayAllEntities());return true;}
        // Hotbar
        int hbX=lx; String[] hl={"All","1","2","3","4","5","6","7","8","9"};
        for(int i=0;i<hl.length;i++){
            int bw=textRenderer.getWidth(hl[i])+5;
            if(hbX+bw>wx+W-10)break;
            if(in(mx,my,hbX,wy+135,bw,9)){config.setDisplayHotbarSlot(i);return true;}
            hbX+=bw+2;
        }
        // Sword delay slider
        if(in(mx,my,c2,wy+68,SW,SH+4)){
            int v=toDelay(mx,c2);
            int hm=c2+(int)((float)(config.getDisplaySwordDelayMin()-DELAY_MIN)/(DELAY_MAX-DELAY_MIN)*SW);
            int hx=c2+(int)((float)(config.getDisplaySwordDelayMax()-DELAY_MIN)/(DELAY_MAX-DELAY_MIN)*SW);
            if(Math.abs(mx-hm)<=Math.abs(mx-hx)){dragging=1;config.setDisplaySwordDelayMin(Math.min(v,config.getDisplaySwordDelayMax()));}
            else{dragging=2;config.setDisplaySwordDelayMax(Math.max(v,config.getDisplaySwordDelayMin()));}
            return true;
        }
        // Range slider
        if(in(mx,my,c2,wy+96,SW,SH+4)){dragging=5;config.setDisplayRange(toRange(mx,c2));return true;}
        // Reset delay + range
        if(in(mx,my,c2,wy+102,54,11)){
            config.setDisplaySwordDelayMin(540);config.setDisplaySwordDelayMax(550);
            config.setDisplayRange(3.3f);return true;
        }
        // Key seq
        if(in(mx,my,c2,wy+122,48,12)){listening=(listening==1)?0:1;return true;}
        if(in(mx,my,c2+62,wy+122,48,12)){listening=(listening==2)?0:2;return true;}
        // Keybind
        if(in(mx,my,c2,wy+149,SW,12)){listening=(listening==3)?0:3;return true;}
        return false;
    }

    private boolean click1(double mx,double my){
        int lx=wx+10, c2=wx+10+140;
        if(in(mx,my,lx,wy+68,CB,CB)){config.setPingSmoothingEnabled(!config.isPingSmoothingEnabled());return true;}
        if(in(mx,my,lx,wy+81,CB,CB)){config.setPingSmoothingSticky(!config.isPingSmoothingSticky());return true;}
        if(in(mx,my,lx,wy+94,CB,CB)){config.setPingSmoothingWall(!config.isPingSmoothingWall());return true;}
        // Target zone
        String[] zones={"Head","Body","Legs"}; int zbx=lx;
        for(int i=0;i<3;i++){
            int bw=textRenderer.getWidth(zones[i])+8;
            if(in(mx,my,zbx,wy+124,bw,11)){config.setPingSmoothingTargetZone(i);return true;}
            zbx+=bw+3;
        }
        // Whitelist +
        if(in(mx,my,lx,wy+149,14,10)){typingFriend=!typingFriend;friendInput="";return true;}
        // X buttons
        int fy=wy+161;
        for(String name:new java.util.ArrayList<>(config.getDisplayFriends())){
            int xbx=lx+4+textRenderer.getWidth("▸ "+name)+4;
            if(in(mx,my,xbx,fy,7,8)){config.removeDisplayFriend(name);return true;}
            fy+=10;
        }
        // Right sliders
        if(in(mx,my,c2,wy+68,SW,SH+4)){dragging=10;config.setPingSmoothingStrength((float)Math.max(0.05,Math.min(1.0,(mx-c2)/SW)));return true;}
        if(in(mx,my,c2,wy+94,SW,SH+4)){dragging=11;config.setPingSmoothingFov((float)Math.max(1,Math.min(360,(mx-c2)/SW*359+1)));return true;}
        if(in(mx,my,c2,wy+120,SW,SH+4)){dragging=12;config.setPingSmoothingRange((float)Math.max(1,Math.min(30,1+(mx-c2)/SW*29)));return true;}
        if(in(mx,my,c2,wy+146,SW,12)){listening=(listening==20)?0:20;return true;}
        return false;
    }

    private boolean click2(double mx,double my){
        int lx=wx+10;
        if(in(mx,my,lx,wy+68,CB,CB)){config.setStaticHitboxEnabled(!config.isStaticHitboxEnabled());return true;}
        if(in(mx,my,lx,wy+81,CB,CB)){config.setStaticHitboxHide(!config.isStaticHitboxHide());return true;}
        if(in(mx,my,lx,wy+113,SW,SH+4)){dragging=6;config.setStaticHitboxWidth(toSH(mx,lx));return true;}
        if(in(mx,my,lx,wy+139,SW,SH+4)){dragging=7;config.setStaticHitboxHeight(toSH(mx,lx));return true;}
        if(in(mx,my,lx,wy+154,54,13)){config.setStaticHitboxWidth(0.6f);config.setStaticHitboxHeight(1.8f);return true;}
        // Auto Firework (right column)
        int c2fw=wx+10+140;
        if(in(mx,my,c2fw,wy+68,CB,CB)){config.setAutoFireworkEnabled(!config.isAutoFireworkEnabled());return true;}
        if(in(mx,my,c2fw,wy+95,SW,SH+4)){dragging=9;config.setAutoFireworkDelay((float)Math.max(0.05,Math.min(3.0,0.05+(mx-c2fw)/SW*2.95)));return true;}
        if(in(mx,my,c2fw,wy+121,SW,12)){listening=(listening==30)?0:30;return true;}
        return false;
    }

    @Override
    public boolean mouseDragged(double mx,double my,int btn,double dx,double dy){
        if(dragging==0)return super.mouseDragged(mx,my,btn,dx,dy);
        int c2=wx+10+140, lx=wx+10;
        switch(dragging){
            case 1->config.setDisplaySwordDelayMin(Math.min(toDelay(mx,c2),config.getDisplaySwordDelayMax()));
            case 2->config.setDisplaySwordDelayMax(Math.max(toDelay(mx,c2),config.getDisplaySwordDelayMin()));
            case 5->config.setDisplayRange(toRange(mx,c2));
            case 6->config.setStaticHitboxWidth(toSH(mx,lx));
            case 7->config.setStaticHitboxHeight(toSH(mx,lx));
            case 8->config.setExpandHitboxSize((float)Math.max(0,Math.min(15,(mx-lx)/SW*15)));
            case 9->config.setAutoFireworkDelay((float)Math.max(0.05,Math.min(3.0,0.05+(mx-(wx+10+140))/SW*2.95))); 
            case 10->config.setPingSmoothingStrength((float)Math.max(0.05,Math.min(1.0,(mx-c2)/SW)));
            case 11->config.setPingSmoothingFov((float)Math.max(1,Math.min(360,(mx-c2)/SW*359+1)));
            case 12->config.setPingSmoothingRange((float)Math.max(1,Math.min(30,1+(mx-c2)/SW*29)));
        }
        return true;
    }

    @Override public boolean mouseReleased(double mx,double my,int btn){dragging=0;return super.mouseReleased(mx,my,btn);}

    private void setListeningKey(int kc){
        switch(listening){
            case 1->keyHandler.setKey1(kc);
            case 2->keyHandler.setKey2(kc);
            case 3->config.setDisplayKeybind(kc);
            case 20->config.setPingSmoothingKeybind(kc);
            case 30->config.setAutoFireworkKeybind(kc);
        }
        listening=0;
    }

    @Override
    public boolean keyPressed(int kc,int sc,int mod){
        if(kc==GLFW.GLFW_KEY_ESCAPE){
            if(typingFriend){typingFriend=false;friendInput="";return true;}
            if(listening!=0){listening=0;return true;}
            saveAndClose();return true;
        }
        if(typingFriend){
            if(kc==GLFW.GLFW_KEY_ENTER&&!friendInput.isEmpty()){config.addDisplayFriend(friendInput.trim());friendInput="";typingFriend=false;}
            else if(kc==GLFW.GLFW_KEY_BACKSPACE&&!friendInput.isEmpty())friendInput=friendInput.substring(0,friendInput.length()-1);
            else if(kc==GLFW.GLFW_KEY_V&&(mod&2)!=0){
                // Ctrl+V paste
                String clip=(client!=null&&client.keyboard!=null)?client.keyboard.getClipboard():"";
                if(clip!=null&&!clip.isEmpty()){
                    friendInput+=(friendInput.length()+clip.length()<=16)?clip:clip.substring(0,16-friendInput.length());
                }
            }
            return true;
        }
        if(listening!=0){setListeningKey(kc);return true;}
        return super.keyPressed(kc,sc,mod);
    }

    @Override
    public boolean charTyped(char chr,int mod){
        if(typingFriend&&friendInput.length()<16){friendInput+=chr;return true;}
        return super.charTyped(chr,mod);
    }

    public static String getKeyName(int k){
        if(k==-1||k==GLFW.GLFW_KEY_UNKNOWN)return "None";
        // Mouse buttons (encoded as negative)
        if(k>=1000){return "M"+(k-1000+1);}  // mouse button
        String n=GLFW.glfwGetKeyName(k,0);
        if(n!=null&&!n.isEmpty())return n.toUpperCase();
        return switch(k){
            case GLFW.GLFW_KEY_SPACE->"SPACE";case GLFW.GLFW_KEY_ENTER->"ENTER";
            case GLFW.GLFW_KEY_LEFT_SHIFT->"LSHIFT";case GLFW.GLFW_KEY_LEFT_CONTROL->"LCTRL";
            case GLFW.GLFW_KEY_RIGHT_SHIFT->"RSHIFT";case GLFW.GLFW_KEY_RIGHT_CONTROL->"RCTRL";
            case GLFW.GLFW_KEY_F1->"F1";case GLFW.GLFW_KEY_F2->"F2";case GLFW.GLFW_KEY_F3->"F3";
            case GLFW.GLFW_KEY_F4->"F4";case GLFW.GLFW_KEY_F5->"F5";case GLFW.GLFW_KEY_F6->"F6";
            case GLFW.GLFW_KEY_F7->"F7";case GLFW.GLFW_KEY_F8->"F8";case GLFW.GLFW_KEY_F9->"F9";
            case GLFW.GLFW_KEY_F10->"F10";case GLFW.GLFW_KEY_F11->"F11";case GLFW.GLFW_KEY_F12->"F12";
            default->"K"+k;
        };
    }
}
