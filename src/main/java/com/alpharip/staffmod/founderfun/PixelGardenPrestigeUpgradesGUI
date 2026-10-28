// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.founderfun;

import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

/** Server-backed Prestige Coins shop with atomic purchases and live balance refresh. */
public final class PixelGardenPrestigeUpgradesGUI extends GuiScreen {
    public static final String MENU_ID = "menu.pixelgarden.prestige";
    public static final String PIXEL_GARDEN_ID = "pixelgarden";
    private final GuiScreen parent;
    private JsonObject state;
    private String status = "";
    private boolean busy;
    public PixelGardenPrestigeUpgradesGUI(GuiScreen parent) { this.parent = parent; }

    @Override public void initGui() {
        buttonList.clear(); int left=width/2-150;
        buttonList.add(new GuiButton(1,left,height/2-5,300,20,"Prestige Yield — buy next level"));
        buttonList.add(new GuiButton(2,left,height/2+20,300,20,"Prestige Bonus — buy next level"));
        buttonList.add(new GuiButton(3,left,height/2+45,300,20,"Garden Coin Cache — buy next level"));
        buttonList.add(new GuiButton(4,left,height/2+75,300,20,"Refresh"));
        buttonList.add(new GuiButton(5,left,height/2+100,300,20,"Back"));
        refreshState(); updateButtons();
    }
    private void refreshState(){try{state=SupabaseCommunityService.getPixelGardenPrestigeState();status="";}catch(Exception e){status="Could not load Prestige data: "+safeMessage(e);}}
    private void updateButtons(){
        for(GuiButton b:buttonList)b.enabled=!busy;
        if(state==null)return;
        JsonObject u=state.has("upgrades")&&state.get("upgrades").isJsonObject()?state.getAsJsonObject("upgrades"):new JsonObject();
        int[] levels={level(u,"prestige_yield"),level(u,"prestige_bonus"),level(u,"coin_cache")};
        int[] costs={25,40,30}; String[] labels={"Prestige Yield (+1 coin/reward)","Prestige Bonus (+10% reward/level)","Garden Coin Cache (+250 coins after Prestige)"};
        for(int i=0;i<3;i++){GuiButton b=buttonList.get(i);b.displayString=levels[i]>=5?labels[i]+" — MAX":labels[i]+" — L"+(levels[i]+1)+" ("+(costs[i]*(levels[i]+1))+" PC)";b.enabled=!busy&&levels[i]<5&&num(state,"prestige_count")>0&&num(state,"upgrade_steps")>=18;}
    }
    @Override protected void actionPerformed(GuiButton b){
        if(b==null||busy)return;if(b.id==5){mc.displayGuiScreen(parent);return;}if(b.id==4){refreshState();updateButtons();return;}
        String key=b.id==1?"prestige_yield":b.id==2?"prestige_bonus":b.id==3?"coin_cache":null;if(key==null)return;
        busy=true;updateButtons();try{SupabaseCommunityService.purchasePixelGardenPrestigeUpgrade(key);refreshState();status="Prestige upgrade purchased.";}catch(Exception e){status="Purchase failed: "+safeMessage(e);}finally{busy=false;updateButtons();}
    }
    @Override public void drawScreen(int mx,int my,float partial){
        drawDefaultBackground();int x=width/2-180,y=height/2-105;drawRect(x,y,x+360,y+225,0xF0202020);drawRect(x,y,x+360,y+3,0xFFFFC83D);
        drawCenteredString(fontRendererObj,"PIXEL GARDEN PRESTIGE",width/2,y+12,0xFFFFC83D);
        if(state!=null){drawString(fontRendererObj,"Prestige Coins: "+state.get("prestige_coins").getAsString(),x+12,y+32,0xFFFFD45A);drawString(fontRendererObj,"Multiplier: "+state.get("effective_multiplier").getAsString()+"x ("+state.get("multiplier_source").getAsString()+")",x+12,y+45,0xFFE3E7EE);drawString(fontRendererObj,"Next reward in: "+(state.get("seconds_to_reward").getAsInt()/60)+"m",x+12,y+58,0xFFB7C4D5);drawString(fontRendererObj,"Upgrade steps: "+num(state,"upgrade_steps")+"/18",x+12,y+71,0xFFB7C4D5);drawString(fontRendererObj,"Prestiges completed: "+num(state,"prestige_count"),x+12,y+84,0xFFB7C4D5);}
        if(!status.isEmpty())drawCenteredString(fontRendererObj,status,width/2,y+204,0xFFFF7777);super.drawScreen(mx,my,partial);
    }
    private static int level(JsonObject o,String k){return Math.max(0,Math.min(5,num(o,k)));}
    private static int num(JsonObject o,String k){try{return o!=null&&o.has(k)?o.get(k).getAsInt():0;}catch(Exception ignored){return 0;}}
    private static String safeMessage(Exception e){return e.getMessage()==null?"unknown error":e.getMessage();}
    @Override public boolean doesGuiPauseGame(){return false;}
    @Override protected void keyTyped(char c,int key)throws IOException{if(key==1&&!busy)mc.displayGuiScreen(parent);else super.keyTyped(c,key);}
}