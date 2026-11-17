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
    private static final int ACCEPT=17,DECLINE=18,KICK=19,ROLE=20,TRANSFER=21,SAVE=22,OPEN_CREATE=23,CANCEL_CREATE=24;
    private final GuiScreen parent;
    private int tab=BROWSER,selected=-1,selectedBrowser=-1,left,top,widthPanel,heightPanel;
    private boolean privateClan,creatingClan;
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
            if(creatingClan){
                int formWidth=286;
                int formLeft=left+(widthPanel-formWidth)/2;
                int fieldY=top+151;
                name=field(40,formLeft,fieldY,190);
                tag=field(41,formLeft+202,fieldY,72);
                buttonList.add(new AlphaButton(TOGGLE,left+(widthPanel-220)/2,top+218,220,20,
                        privateClan?"PRIVATE  -  Request approval":"PUBLIC  -  Anyone can join",false,true));
                int actionsWidth=218;
                int actionsLeft=left+(widthPanel-actionsWidth)/2;
                buttonList.add(new AlphaButton(CANCEL_CREATE,actionsLeft,top+heightPanel-42,84,22,"CANCEL"));
                buttonList.add(new AlphaButton(CREATE,actionsLeft+96,top+heightPanel-42,122,22,"CREATE CLAN",true));
            } else {
                for(int i=0;i<Math.min(8,s.browser.size());i++){
                    JsonObject c=s.browser.get(i).getAsJsonObject();
                    String label=val(c,"name")+" ["+val(c,"tag")+"]  "+val(c,"member_count")+"/50  "+val(c,"visibility").toUpperCase();
                    buttonList.add(new AlphaButton(SELECT+i,left+14,listY+i*24,widthPanel-28,21,clip(label,widthPanel-48),i==selectedBrowser));
                }
                buttonList.add(new AlphaButton(OPEN_CREATE,left+widthPanel-142,top+heightPanel-34,128,20,"CREATE A CLAN",true));
                if(selectedBrowser>=0&&selectedBrowser<s.browser.size()) buttonList.add(new AlphaButton(JOIN,left+14,top+heightPanel-34,145,20,"JOIN / REQUEST",true));
            }
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
    private GuiTextField field(int id,int x,int y,int w){GuiTextField f=new GuiTextField(id,fontRendererObj,x,y,w,18);f.setMaxStringLength(id==45?256:(id==41||id==44?3:(id==40||id==43?24:64)));f.setEnableBackgroundDrawing(true);f.setTextColor(0xFFE8EAF0);return f;}
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
        if(b.id==TOGGLE){
            String clanName=name==null?"":name.getText(),clanTag=tag==null?"":tag.getText();
            privateClan=!privateClan;initGui();
            if(name!=null)name.setText(clanName);if(tag!=null)tag.setText(clanTag);
            return;
        }
        if(b.id==OPEN_CREATE){creatingClan=true;selectedBrowser=-1;initGui();return;}
        if(b.id==CANCEL_CREATE){creatingClan=false;name=null;tag=null;initGui();return;}
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
        drawDefaultBackground();
        // Main surface and restrained Alpha accent.
        drawRect(left,top,left+widthPanel,top+heightPanel,0xF20B0D12);
        drawRect(left,top,left+widthPanel,top+2,0xFFFFD866);
        drawRect(left+1,top+1,left+widthPanel-1,top+heightPanel-1,0x221F2530);
        drawRect(left+12,top+39,left+widthPanel-12,top+40,0x553C424D);

        drawString(fontRendererObj,"ALPHACLANS",left+16,top+43,0xFFFFD866);
        drawString(fontRendererObj,tabTitle(),left+16,top+59,0xFF9299A6);
        AlphaClanService.Snapshot s=AlphaClanService.INSTANCE.getSnapshot();

        if(tab==BROWSER){
            if(creatingClan){
                int cardLeft=left+18,cardRight=left+widthPanel-18;
                int cardTop=top+82,cardBottom=top+heightPanel-27;
                drawRect(cardLeft,cardTop,cardRight,cardBottom,0xAA12151C);
                drawRect(cardLeft,cardTop,cardRight,cardTop+1,0x66545B68);
                drawString(fontRendererObj,"CREATE YOUR CLAN",left+24,top+91,0xFFFFD866);
                drawString(fontRendererObj,"Set up your clan identity and choose who can join.",left+24,top+109,0xFF9299A6);
                int formWidth=286;
                int formLeft=left+(widthPanel-formWidth)/2;
                drawString(fontRendererObj,"CLAN NAME",formLeft,top+132,0xFFE6E8ED);
                drawString(fontRendererObj,"3-24 CHARACTERS",formLeft,top+141,0xFF777F8D);
                drawString(fontRendererObj,"CLAN TAG",formLeft+202,top+132,0xFFE6E8ED);
                drawString(fontRendererObj,"2-3 LETTERS",formLeft+202,top+141,0xFF777F8D);
                drawCenteredString(fontRendererObj,"WHO CAN JOIN?",width/2,top+197,0xFFE6E8ED);
                drawCenteredString(fontRendererObj,privateClan?"PRIVATE CLAN":"PUBLIC CLAN",width/2,top+246,0xFFFFD866);
                drawCenteredString(fontRendererObj,privateClan
                        ?"Visible in the browser. Players must request to join."
                        :"Visible in the browser. Players can join immediately.",
                        width/2,top+259,0xFF9299A6);
                if(name!=null)name.drawTextBox();
                if(tag!=null)tag.drawTextBox();
            } else {
                drawSectionLabel("CLAN DIRECTORY",left+14,top+77);
                drawRows(s.browser,top+94,0);
            }
        } else if(tab==CLAN){
            if(s.clan==null)drawEmptyState("NO CLAN YET","Browse the directory to find a clan or create your own.",top+112);
            else{
                drawSectionLabel("MEMBERS",left+14,top+78);
                drawRows(s.members,top+96,1);
                if(input!=null)input.drawTextBox();if(name!=null)name.drawTextBox();if(tag!=null)tag.drawTextBox();
            }
        } else if(tab==INVITES){
            drawSectionLabel("PENDING INVITATIONS",left+14,top+78);drawRows(s.invites,top+96,2);
        } else if(tab==REQUESTS){
            drawSectionLabel("JOIN REQUESTS",left+14,top+78);drawRows(s.requests,top+96,3);
        } else if(tab==CHAT){
            drawSectionLabel("CLAN CONVERSATION",left+14,top+78);
            int count=Math.min(10,s.messages.size());
            if(count==0)drawEmptyState("NO MESSAGES YET","Messages from your clan will appear here.",top+132);
            for(int i=0;i<count;i++){
                JsonObject m=s.messages.get(count-i-1).getAsJsonObject();
                String user=val(AlphaClanService.nested(m,"profiles"),"username");
                String line=user+": "+val(m,"body");
                int rowY=top+101+i*19;
                drawRect(left+12,rowY-3,left+widthPanel-12,rowY+13,0x55212630);
                fontRendererObj.drawString(clip(user,widthPanel/3),left+20,rowY,0xFFFFD866);
                fontRendererObj.drawStringWithShadow(clip(line,widthPanel-48),left+20+fontRendererObj.getStringWidth(clip(user,widthPanel/3))+5,rowY,0xFFE6E8ED);
            }
            if(input!=null)input.drawTextBox();
        }
        drawRect(left+12,top+heightPanel-22,left+widthPanel-12,top+heightPanel-21,0x443C424D);
        fontRendererObj.drawString(clip(s.status,widthPanel-32),left+16,top+heightPanel-15,0xFF9299A6);
        super.drawScreen(mx,my,pt);
    }
    private String tabTitle(){
        switch(tab){case BROWSER:return "Discover public and private communities";case CLAN:return "Your clan and its members";
            case INVITES:return "Invitations sent to your account";case REQUESTS:return "Review requests to join your clan";
            case CHAT:return "Talk with your clan members";default:return "";}
    }
    private void drawSectionLabel(String text,int x,int y){fontRendererObj.drawString(text,x,y,0xFFFFD866);}
    private void drawEmptyState(String title,String detail,int y){
        drawRect(width/2-13,y-23,width/2+13,y+3,0x66212630);
        drawCenteredString(fontRendererObj,"◆",width/2,y-17,0xFFFFD866);
        drawCenteredString(fontRendererObj,title,width/2,y+12,0xFFE6E8ED);
        drawCenteredString(fontRendererObj,detail,width/2,y+26,0xFF9299A6);
    }
    private void drawRows(JsonArray rows,int y,int mode){
        int n=Math.min(8,rows.size());
        if(n==0){drawEmptyState(mode==0?"NO CLANS FOUND":"NOTHING HERE YET",
                mode==0?"Create a clan to get started.":"New items will appear here when available.",y+32);return;}
        for(int i=0;i<n;i++){
            JsonObject r=rows.get(i).getAsJsonObject();String label,meta="";
            if(mode==0){label=val(r,"name");meta="["+val(r,"tag")+"]   "+val(r,"member_count")+"/50   "+val(r,"visibility").toUpperCase();}
            else if(mode==1){label=AlphaClanService.playerName(r);meta=val(r,"role").toUpperCase();}
            else if(mode==2){label=val(AlphaClanService.nested(r,"clans"),"name");meta="["+val(AlphaClanService.nested(r,"clans"),"tag")+"]";}
            else{label=AlphaClanService.playerName(r);meta="PENDING";}
            int rowY=y+i*25;
            boolean active=i==(mode==0?selectedBrowser:selected);
            drawRect(left+12,rowY-4,left+widthPanel-12,rowY+17,active?0xAA302A1B:0x77212630);
            drawRect(left+12,rowY-4,left+13,rowY+17,active?0xFFFFD866:0x664B515D);
            fontRendererObj.drawStringWithShadow(clip(label,widthPanel/2),left+22,rowY+2,0xFFE6E8ED);
            String right=clip(meta,widthPanel/2-28);
            fontRendererObj.drawString(right,left+widthPanel-22-fontRendererObj.getStringWidth(right),rowY+2,
                    active?0xFFFFD866:0xFF9299A6);
        }
    }
    private String val(JsonObject o,String k){String v=AlphaClanService.str(o,k);return v==null?"":v;}
    private String clip(String s,int max){if(s==null)return "";while(!s.isEmpty()&&fontRendererObj.getStringWidth(s)>max)s=s.substring(0,s.length()-1);return s;}
    @Override public boolean doesGuiPauseGame(){return false;}
}
