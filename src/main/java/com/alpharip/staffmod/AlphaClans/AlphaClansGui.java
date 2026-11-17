// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.AlphaClans;

import com.alpharip.staffmod.gui.AlphaButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import java.io.IOException;

/** AlphaClans browser, member management, join-request and realtime chat screen. */
public final class AlphaClansGui extends GuiScreen {
    private static final int BACK=1,BROWSER=2,CLAN=3,INVITES=4,REQUESTS=5,CHAT=6,LOGIN=7;
    private static final int SELECT=100,CREATE=10,JOIN=11,TOGGLE=12,INVITE=13,SEND=14,LEAVE=15,DISBAND=16;
    private static final int ACCEPT=17,DECLINE=18,KICK=19,ROLE=20,TRANSFER=21,SAVE=22;
    private final GuiScreen parent;
    private int tab=BROWSER,selected=-1,selectedBrowser=-1,left,top,widthPanel,heightPanel;
    private boolean privateClan;
    private String settingsClanId;
    private GuiTextField name,tag,input;
    private Object renderedSnapshot;
    public AlphaClansGui(GuiScreen parent){this.parent=parent;}

    @Override public void initGui(){
        buttonList.clear(); name=null; tag=null; input=null;
        widthPanel=Math.min(560,Math.max(310,width-20)); heightPanel=Math.min(350,Math.max(250,height-20));
        left=(width-widthPanel)/2; top=(height-heightPanel)/2;
        int x=left+10,y=top+10;
        buttonList.add(new AlphaButton(BACK,x,y,42,18,"BACK"));
        buttonList.add(new AlphaButton(BROWSER,x+48,y,72,18,"BROWSER",tab==BROWSER));
        buttonList.add(new AlphaButton(CLAN,x+124,y,66,18,"MY CLAN",tab==CLAN));
        buttonList.add(new AlphaButton(INVITES,x+194,y,62,18,"INVITES",tab==INVITES));
        buttonList.add(new AlphaButton(REQUESTS,x+260,y,76,18,"REQUESTS",tab==REQUESTS));
        buttonList.add(new AlphaButton(CHAT,x+340,y,58,18,"CHAT",tab==CHAT));
        if(!com.alpharip.staffmod.permissions.SupabaseCommunityService.isSignedIn())
            buttonList.add(new AlphaButton(LOGIN,left+widthPanel-92,y,80,18,"SIGN IN",true));
        AlphaClanService.Snapshot s=AlphaClanService.INSTANCE.getSnapshot();
        String activeClanId=AlphaClanService.str(s.clan,"id");
        if(tab==CLAN && activeClanId!=null && !activeClanId.equals(settingsClanId)){
            privateClan="private".equalsIgnoreCase(AlphaClanService.str(s.clan,"visibility"));
            settingsClanId=activeClanId;
        }
        int listY=top+52;
        if(tab==BROWSER){
            for(int i=0;i<Math.min(8,s.browser.size());i++){
                JsonObject c=s.browser.get(i).getAsJsonObject();
                String label=val(c,"name")+" ["+val(c,"tag")+"]  "+val(c,"member_count")+"/50  "+val(c,"visibility").toUpperCase();
                buttonList.add(new AlphaButton(SELECT+i,left+14,listY+i*24,widthPanel-28,21,clip(label,widthPanel-48),i==selectedBrowser));
            }
            name=field(40,left+14,top+heightPanel-62,150); tag=field(41,left+170,top+heightPanel-62,72);
            buttonList.add(new AlphaButton(TOGGLE,left+248,top+heightPanel-62,76,18,privateClan?"PRIVATE":"PUBLIC"));
            buttonList.add(new AlphaButton(CREATE,left+330,top+heightPanel-62,100,18,"CREATE CLAN",true));
            if(selectedBrowser>=0&&selectedBrowser<s.browser.size()) buttonList.add(new AlphaButton(JOIN,left+14,top+heightPanel-34,145,20,"JOIN / REQUEST",true));
        } else if(tab==CLAN){
            for(int i=0;i<Math.min(8,s.members.size());i++){
                JsonObject m=s.members.get(i).getAsJsonObject();
                buttonList.add(new AlphaButton(SELECT+i,left+14,listY+i*24,widthPanel-28,21,
                    clip(AlphaClanService.playerName(m)+"  "+val(m,"role").toUpperCase(),widthPanel-48),i==selected));
            }
            if(s.clan!=null){
                input=field(42,left+14,top+heightPanel-86,widthPanel-28);
                if(s.owner||s.officer) buttonList.add(new AlphaButton(INVITE,left+14,top+heightPanel-60,70,18,"INVITE"));
                buttonList.add(new AlphaButton(LEAVE,left+90,top+heightPanel-60,70,18,"LEAVE"));
                if(s.owner){
                    buttonList.add(new AlphaButton(DISBAND,left+166,top+heightPanel-60,76,18,"DISBAND"));
                    buttonList.add(new AlphaButton(ROLE,left+248,top+heightPanel-60,68,18,"ROLE"));
                    buttonList.add(new AlphaButton(TRANSFER,left+322,top+heightPanel-60,82,18,"TRANSFER"));
                    buttonList.add(new AlphaButton(KICK,left+408,top+heightPanel-60,68,18,"KICK"));
                    name=field(43,left+14,top+heightPanel-34,130); tag=field(44,left+150,top+heightPanel-34,64);
                    name.setText(val(s.clan,"name")); tag.setText(val(s.clan,"tag"));
                    buttonList.add(new AlphaButton(TOGGLE,left+220,top+heightPanel-34,76,18,privateClan?"PRIVATE":"PUBLIC"));
                    buttonList.add(new AlphaButton(SAVE,left+302,top+heightPanel-34,88,18,"SAVE SETTINGS"));
                } else if(s.officer) buttonList.add(new AlphaButton(KICK,left+166,top+heightPanel-60,68,18,"KICK"));
            }
        } else if(tab==INVITES||tab==REQUESTS){
            JsonArray rows=tab==INVITES?s.invites:s.requests;
            for(int i=0;i<Math.min(8,rows.size());i++){
                JsonObject row=rows.get(i).getAsJsonObject();
                String label=tab==INVITES?val(AlphaClanService.nested(row,"clans"),"name")+" ["+val(AlphaClanService.nested(row,"clans"),"tag")+"]":AlphaClanService.playerName(row);
                buttonList.add(new AlphaButton(SELECT+i,left+14,listY+i*24,widthPanel-28,21,clip(label,widthPanel-48),i==selected));
            }
            if(selected>=0&&selected<rows.size()){
                buttonList.add(new AlphaButton(ACCEPT,left+14,top+heightPanel-34,100,20,tab==INVITES?"ACCEPT":"APPROVE"));
                buttonList.add(new AlphaButton(DECLINE,left+122,top+heightPanel-34,100,20,"DECLINE"));
            }
        } else if(tab==CHAT){
            input=field(45,left+14,top+heightPanel-38,widthPanel-100);
            buttonList.add(new AlphaButton(SEND,left+widthPanel-78,top+heightPanel-38,64,20,"SEND",true));
        }
        renderedSnapshot=s;
        AlphaClanService.INSTANCE.refreshIfStale();
    }
    private GuiTextField field(int id,int x,int y,int w){GuiTextField f=new GuiTextField(id,fontRendererObj,x,y,w,18);f.setMaxStringLength(id==45?256:64);f.setEnableBackgroundDrawing(true);f.setTextColor(0xFFE8EAF0);return f;}
    @Override public void updateScreen(){
        super.updateScreen(); AlphaClanService.INSTANCE.refreshIfStale();
        Object current=AlphaClanService.INSTANCE.getSnapshot();
        if(current!=renderedSnapshot){
            String n=name==null?"":name.getText(),t=tag==null?"":tag.getText(),v=input==null?"":input.getText();
            initGui(); if(name!=null)name.setText(n);if(tag!=null)tag.setText(t);if(input!=null)input.setText(v);
        }
    }
    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(b==null)return;
        if(b.id==BACK){mc.displayGuiScreen(parent);return;}
        if(b.id==LOGIN){mc.displayGuiScreen(new com.alpharip.staffmod.gui.CommunityLoginGui(this));return;}
        if(b.id>=BROWSER&&b.id<=CHAT){tab=b.id;selected=-1;initGui();return;}
        if(b.id==TOGGLE){privateClan=!privateClan;initGui();return;}
        if(b.id>=SELECT&&b.id<SELECT+10){if(tab==BROWSER)selectedBrowser=b.id-SELECT;else selected=b.id-SELECT;initGui();return;}
        AlphaClanService.Snapshot s=AlphaClanService.INSTANCE.getSnapshot();
        if(b.id==CREATE&&name!=null&&tag!=null){AlphaClanService.INSTANCE.create(name.getText(),tag.getText(),privateClan);return;}
        if(b.id==JOIN&&selectedBrowser>=0&&selectedBrowser<s.browser.size()){
            JsonObject c=s.browser.get(selectedBrowser).getAsJsonObject();String id=val(c,"id");
            if("public".equalsIgnoreCase(val(c,"visibility")))AlphaClanService.INSTANCE.join(id);else AlphaClanService.INSTANCE.requestJoin(id);return;
        }
        if(b.id==INVITE&&input!=null){AlphaClanService.INSTANCE.invite(input.getText());return;}
        if(b.id==SEND&&input!=null){AlphaClanService.INSTANCE.sendMessage(input.getText());input.setText("");return;}
        if(b.id==LEAVE){AlphaClanService.INSTANCE.leave();return;} if(b.id==DISBAND){AlphaClanService.INSTANCE.disband();return;}
        if((b.id==ACCEPT||b.id==DECLINE)&&selected>=0){
            JsonArray rows=tab==INVITES?s.invites:s.requests;if(selected>=rows.size())return;JsonObject row=rows.get(selected).getAsJsonObject();
            if(tab==INVITES)AlphaClanService.INSTANCE.acceptInvite(val(row,"id"),b.id==ACCEPT);
            else AlphaClanService.INSTANCE.respondRequest(val(row,"id"),b.id==ACCEPT);return;
        }
        if(tab==CLAN&&selected>=0&&selected<s.members.size()){
            JsonObject m=s.members.get(selected).getAsJsonObject();String id=val(m,"profile_id");
            if(s.clan!=null && id.equals(val(s.clan,"owner_id")))return;
            if(b.id==KICK)AlphaClanService.INSTANCE.kick(id);
            if(b.id==ROLE)AlphaClanService.INSTANCE.setRole(id,!"officer".equalsIgnoreCase(val(m,"role")));
            if(b.id==TRANSFER)AlphaClanService.INSTANCE.transfer(id);
        }
        if(b.id==SAVE&&name!=null&&tag!=null&&s.clan!=null)
            AlphaClanService.INSTANCE.updateSettings(name.getText(),tag.getText(),val(s.clan,"description"),privateClan);
    }
    @Override protected void keyTyped(char ch,int key)throws IOException{
        if(name!=null&&name.isFocused()){name.textboxKeyTyped(ch,key);return;}
        if(tag!=null&&tag.isFocused()){tag.textboxKeyTyped(ch,key);return;}
        if(input!=null&&input.isFocused()){
            if(key==Keyboard.KEY_RETURN&&tab==CHAT){AlphaClanService.INSTANCE.sendMessage(input.getText());input.setText("");return;}
            input.textboxKeyTyped(ch,key);return;
        }
        if(key==Keyboard.KEY_ESCAPE){mc.displayGuiScreen(parent);return;}super.keyTyped(ch,key);
    }
    @Override protected void mouseClicked(int x,int y,int b)throws IOException{super.mouseClicked(x,y,b);if(name!=null)name.mouseClicked(x,y,b);if(tag!=null)tag.mouseClicked(x,y,b);if(input!=null)input.mouseClicked(x,y,b);}
    @Override public void drawScreen(int mx,int my,float pt){
        drawDefaultBackground();drawRect(left,top,left+widthPanel,top+heightPanel,0xE80C0E13);drawRect(left,top,left+widthPanel,top+2,0xFFFFD866);
        drawCenteredString(fontRendererObj,"ALPHACLANS",width/2,top+35,0xFFFFD866);
        AlphaClanService.Snapshot s=AlphaClanService.INSTANCE.getSnapshot();
        if(tab==BROWSER){drawRows(s.browser,top+54,0);if(name!=null)name.drawTextBox();if(tag!=null)tag.drawTextBox();}
        else if(tab==CLAN){
            if(s.clan==null)drawCenteredString(fontRendererObj,"You are not in a clan. Find one in BROWSER.",width/2,top+100,0xFFB8BDC8);
            else{drawCenteredString(fontRendererObj,val(s.clan,"name")+" ["+val(s.clan,"tag")+"]",width/2,top+52,0xFFFFD866);drawRows(s.members,top+74,1);
                if(input!=null)input.drawTextBox();if(name!=null)name.drawTextBox();if(tag!=null)tag.drawTextBox();}
        }else if(tab==INVITES)drawRows(s.invites,top+54,2);
        else if(tab==REQUESTS)drawRows(s.requests,top+54,3);
        else if(tab==CHAT){
            int count=Math.min(10,s.messages.size());
            for(int i=0;i<count;i++){JsonObject m=s.messages.get(count-i-1).getAsJsonObject();String line=val(AlphaClanService.nested(m,"profiles"),"username")+": "+val(m,"body");
                fontRendererObj.drawStringWithShadow(clip(line,widthPanel-32),left+16,top+58+i*18,0xFFE6E8ED);}
            if(input!=null)input.drawTextBox();
        }
        fontRendererObj.drawStringWithShadow(clip(s.status,widthPanel-24),left+12,top+heightPanel-12,0xFFB8BDC8);
        super.drawScreen(mx,my,pt);
    }
    private void drawRows(JsonArray rows,int y,int mode){
        int n=Math.min(8,rows.size());if(n==0){drawCenteredString(fontRendererObj,"Nothing to display yet.",width/2,y+36,0xFFB8BDC8);return;}
        for(int i=0;i<n;i++){JsonObject r=rows.get(i).getAsJsonObject();String label;
            if(mode==0)label=val(r,"name")+" ["+val(r,"tag")+"]  "+val(r,"member_count")+"/50  "+val(r,"visibility");
            else if(mode==1)label=AlphaClanService.playerName(r)+"  "+val(r,"role");
            else if(mode==2)label=val(AlphaClanService.nested(r,"clans"),"name")+" ["+val(AlphaClanService.nested(r,"clans"),"tag")+"]";
            else label=AlphaClanService.playerName(r);
            fontRendererObj.drawStringWithShadow(clip(label,widthPanel-32),left+16,y+i*23,i==(mode==0?selectedBrowser:selected)?0xFFFFD866:0xFFE6E8ED);
        }
    }
    private String val(JsonObject o,String k){String v=AlphaClanService.str(o,k);return v==null?"":v;}
    private String clip(String s,int max){if(s==null)return "";while(!s.isEmpty()&&fontRendererObj.getStringWidth(s)>max)s=s.substring(0,s.length()-1);return s;}
    @Override public boolean doesGuiPauseGame(){return false;}
}
