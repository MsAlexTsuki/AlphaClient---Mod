// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.keystrokes;

import com.alpharip.staffmod.config.HudScaleConfig;
import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** In-game controls for Keystrokes appearance and visible components. */
public final class KeystrokesSettingsGui extends AlphaGuiScreen {
    private static final int BACK=1, BG=2, ROUND=3, MOVEMENT=4, SPACE=5, MOUSE=6, CPS=7, ACTIVE=8, INACTIVE=9, TEXT=10, ALPHA_DOWN=11, ALPHA_UP=12, SCALE_DOWN=13, SCALE_UP=14;
    private final GuiScreen parent;
    public KeystrokesSettingsGui(GuiScreen parent) { this.parent=parent; }

    @Override public void initGui() {
        KeystrokesSettings.load(); buttonList.clear();
        int w=190, x=(width-w)/2, y=Math.max(34,(height-220)/2);
        buttonList.add(new AlphaButton(BG,x,y,w,20,"BACKGROUND: "+(KeystrokesSettings.hasBackground()?"ON":"OFF")));
        buttonList.add(new AlphaButton(ROUND,x,y+24,w,20,"ROUNDED: "+(KeystrokesSettings.isRounded()?"ON":"OFF")));
        buttonList.add(new AlphaButton(MOVEMENT,x,y+48,w,20,"WASD: "+(KeystrokesSettings.showMovement()?"ON":"OFF")));
        buttonList.add(new AlphaButton(SPACE,x,y+72,w,20,"SPACE: "+(KeystrokesSettings.showSpace()?"ON":"OFF")));
        buttonList.add(new AlphaButton(MOUSE,x,y+96,w,20,"MOUSE: "+(KeystrokesSettings.showMouse()?"ON":"OFF")));
        buttonList.add(new AlphaButton(CPS,x,y+120,w,20,"CPS: "+(KeystrokesSettings.showCps()?"ON":"OFF")));
        buttonList.add(new AlphaButton(ACTIVE,x,y+144,w,20,"CYCLE ACTIVE COLOR"));
        buttonList.add(new AlphaButton(INACTIVE,x,y+168,w,20,"CYCLE INACTIVE COLOR"));
        buttonList.add(new AlphaButton(TEXT,x,y+192,w,20,"CYCLE TEXT COLOR"));
        buttonList.add(new AlphaButton(ALPHA_DOWN,x-36,y+24,30,20,"-"));
        buttonList.add(new AlphaButton(ALPHA_UP,x+w+6,y+24,30,20,"+"));
        buttonList.add(new AlphaButton(SCALE_DOWN,x-36,y+72,30,20,"-"));
        buttonList.add(new AlphaButton(SCALE_UP,x+w+6,y+72,30,20,"+"));
        buttonList.add(new AlphaButton(BACK,x,height-30,w,20,"BACK"));
    }

    @Override protected void actionPerformed(GuiButton button) throws IOException {
        if(button==null)return;
        switch(button.id) {
            case BACK: mc.displayGuiScreen(parent); return;
            case BG: KeystrokesSettings.toggleBackground(); break;
            case ROUND: KeystrokesSettings.toggleRounded(); break;
            case MOVEMENT: KeystrokesSettings.toggleMovement(); break;
            case SPACE: KeystrokesSettings.toggleSpace(); break;
            case MOUSE: KeystrokesSettings.toggleMouse(); break;
            case CPS: KeystrokesSettings.toggleCps(); break;
            case ACTIVE: KeystrokesSettings.cycleActiveColor(); break;
            case INACTIVE: KeystrokesSettings.cycleInactiveColor(); break;
            case TEXT: KeystrokesSettings.cycleTextColor(); break;
            case ALPHA_DOWN: KeystrokesSettings.adjustBackgroundAlpha(-15); break;
            case ALPHA_UP: KeystrokesSettings.adjustBackgroundAlpha(15); break;
            case SCALE_DOWN: HudScaleConfig.set("keystrokes",HudScaleConfig.get("keystrokes")-0.1F); break;
            case SCALE_UP: HudScaleConfig.set("keystrokes",HudScaleConfig.get("keystrokes")+0.1F); break;
        }
        initGui();
    }

    @Override public void drawScreen(int mouseX,int mouseY,float partialTicks) {
        drawRect(0,0,width,height,0xB8000000);
        drawCenteredString(fontRendererObj,"KEYSTROKES SETTINGS",width/2,Math.max(12,(height-220)/2-18),0xFFFFD866);
        drawCenteredString(fontRendererObj,"Background opacity: "+KeystrokesSettings.getBackgroundAlpha()+"  |  HUD scale: "+String.format("%.1f",HudScaleConfig.get("keystrokes")),width/2,Math.max(20,(height-220)/2+218),0xFFE7E9EE);
        super.drawScreen(mouseX,mouseY,partialTicks);
    }
    @Override public boolean doesGuiPauseGame(){return false;}
}
