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
import java.util.ArrayList;
import java.util.List;

/** AlphaClans interface with membership-aware navigation and grouped actions. */
public final class AlphaClansGui extends GuiScreen {
    private static final int TAB_BROWSER=1, TAB_CLAN=2, TAB_INVITES=3, TAB_REQUESTS=4, TAB_CHAT=5, TAB_SETTINGS=6;
    private static final int BACK=10, SELECT_BASE=100, CREATE=200, OPEN_CREATE=201, CANCEL_CREATE=202;
    private static final int JOIN=203, INVITE=204, SEND=205, LEAVE=206, DISBAND=207;
    private static final int ACCEPT=208, DECLINE=209, KICK=210, ROLE=211, TRANSFER=212, SAVE=213, VIS_PUBLIC=214, VIS_PRIVATE=215;
    private final GuiScreen parent;
    private int tab=TAB_BROWSER, selected=-1, selectedBrowser=-1;
    private int left, top, panelWidth, panelHeight, bodyTop, footerTop;
    private boolean privateClan, creatingClan;
    private GuiTextField name, tag, input;
    private Object renderedSnapshot;
    private String observedClanId;

    public AlphaClansGui(GuiScreen parent) { this.parent=parent; }

    @Override public void initGui() {
        buttonList.clear();
        name=null; tag=null; input=null;
        panelWidth=Math.min(620,Math.max(320,width-24));
        panelHeight=Math.min(410,Math.max(270,height-24));
        left=(width-panelWidth)/2; top=(height-panelHeight)/2;
        bodyTop=top+91; footerTop=top+panelHeight-58;

        AlphaClanService.Snapshot s=AlphaClanService.INSTANCE.getSnapshot();
        String clanId=AlphaClanService.str(s.clan,"id");
        if (clanId!=null && tab==TAB_BROWSER) tab=TAB_CLAN;
        if (clanId==null && (tab==TAB_CLAN || tab==TAB_CHAT || tab==TAB_SETTINGS || tab==TAB_REQUESTS)) tab=TAB_BROWSER;
        if (s.clan!=null && (observedClanId==null || !clanId.equals(observedClanId))) {
            String visibility=AlphaClanService.str(s.clan,"visibility");
            privateClan="private".equalsIgnoreCase(visibility);
        }

        buttonList.add(new AlphaButton(BACK,left+12,top+12,54,20,"BACK"));
        List<Integer> tabs=availableTabs(s);
        if (!tabs.contains(tab)) tab=tabs.get(0);
        int gap=4, usable=panelWidth-28;
        int tabWidth=Math.max(46,Math.min(88,(usable-(tabs.size()-1)*gap)/Math.max(1,tabs.size())));
        int total=tabs.size()*tabWidth+Math.max(0,tabs.size()-1)*gap;
        int tabX=left+(panelWidth-total)/2;
        for (Integer tabId:tabs) {
            String label=tabLabel(tabId);
            buttonList.add(new AlphaButton(tabId,tabX,top+54,tabWidth,21,label,tabId==tab));
            tabX+=tabWidth+gap;
        }

        if (tab==TAB_BROWSER && s.clan==null) {
            if (creatingClan) {
                int formLeft=left+(panelWidth-292)/2;
                name=field(40,formLeft,bodyTop+58,204,24);
                tag=field(41,formLeft+216,bodyTop+58,76,24);
                int choiceWidth=112, choiceGap=10, choiceLeft=(width-(choiceWidth*2+choiceGap))/2;
                buttonList.add(new AlphaButton(VIS_PUBLIC,choiceLeft,bodyTop+96,choiceWidth,24,"PUBLIC",!privateClan,!privateClan));
                buttonList.add(new AlphaButton(VIS_PRIVATE,choiceLeft+choiceWidth+choiceGap,bodyTop+96,choiceWidth,24,"PRIVATE",privateClan,privateClan));
                buttonList.add(new AlphaButton(CANCEL_CREATE,left+panelWidth-204,footerTop+10,86,24,"CANCEL"));
                buttonList.add(new AlphaButton(CREATE,left+panelWidth-108,footerTop+10,96,24,"CREATE",true));
            } else {
                addRows(s.browser,bodyTop+18,Math.max(1,(footerTop-bodyTop-26)/25),true);
                if (selectedBrowser>=0 && selectedBrowser<s.browser.size())
                    buttonList.add(new AlphaButton(JOIN,left+14,footerTop+10,136,24,
                            "JOIN / REQUEST",true));
                buttonList.add(new AlphaButton(OPEN_CREATE,left+panelWidth-154,footerTop+10,140,24,"CREATE A CLAN",true));
            }
        } else if (tab==TAB_CLAN && s.clan!=null) {
            int listY=bodyTop+74;
            addRows(s.members,listY,Math.max(1,(footerTop-listY-8)/25),false);
            if (selected>=0 && selected<s.members.size()) {
                JsonObject member=s.members.get(selected).getAsJsonObject();
                String target=AlphaClanService.str(member,"profile_id");
                String ownerId=AlphaClanService.str(s.clan,"owner_id");
                if (target!=null && !target.equals(ownerId)) {
                    if (s.owner) {
                        buttonList.add(new AlphaButton(KICK,left+14,footerTop+10,62,24,"REMOVE"));
                        buttonList.add(new AlphaButton(ROLE,left+84,footerTop+10,92,24,
                                "officer".equalsIgnoreCase(AlphaClanService.str(member,"role"))?"SET MEMBER":"SET OFFICER"));
                        buttonList.add(new AlphaButton(TRANSFER,left+184,footerTop+10,112,24,"TRANSFER OWNER"));
                    } else if (s.officer) {
                        buttonList.add(new AlphaButton(KICK,left+14,footerTop+10,76,24,"REMOVE"));
                    }
                }
            } else {
                int actionRight=left+panelWidth-14;
                if (s.owner) {
                    int disbandX=actionRight-68;
                    int leaveX=disbandX-64;
                    int inviteX=leaveX-62;
                    int inputWidth=Math.max(78,inviteX-(left+14)-8);
                    input=field(42,left+14,footerTop+10,inputWidth,24);
                    buttonList.add(new AlphaButton(INVITE,inviteX,footerTop+10,56,24,"INVITE",true));
                    buttonList.add(new AlphaButton(LEAVE,leaveX,footerTop+10,58,24,"LEAVE"));
                    buttonList.add(new AlphaButton(DISBAND,disbandX,footerTop+10,68,24,"DISBAND"));
                } else if (s.officer) {
                    int leaveX=actionRight-58;
                    int inviteX=leaveX-62;
                    int inputWidth=Math.max(90,inviteX-(left+14)-8);
                    input=field(42,left+14,footerTop+10,inputWidth,24);
                    buttonList.add(new AlphaButton(INVITE,inviteX,footerTop+10,56,24,"INVITE",true));
                    buttonList.add(new AlphaButton(LEAVE,leaveX,footerTop+10,58,24,"LEAVE"));
                } else {
                    buttonList.add(new AlphaButton(LEAVE,actionRight-58,footerTop+10,58,24,"LEAVE"));
                }
            }
        } else if (tab==TAB_SETTINGS && s.owner && s.clan!=null) {
            int formLeft=left+panelWidth/2-146;
            name=field(43,formLeft,bodyTop+64,202,24); name.setText(AlphaClanService.str(s.clan,"name"));
            tag=field(44,formLeft+214,bodyTop+64,78,24); tag.setText(AlphaClanService.str(s.clan,"tag"));
            buttonList.add(new AlphaButton(VIS_PUBLIC,formLeft,bodyTop+112,136,24,"PUBLIC",!privateClan,!privateClan));
            buttonList.add(new AlphaButton(VIS_PRIVATE,formLeft+146,bodyTop+112,136,24,"PRIVATE",privateClan,privateClan));
            buttonList.add(new AlphaButton(SAVE,left+panelWidth-116,footerTop+10,102,24,"SAVE",true));
        } else if (tab==TAB_INVITES && s.clan==null) {
            addRows(s.invites,bodyTop+18,Math.max(1,(footerTop-bodyTop-26)/25),false);
            if (selected>=0 && selected<s.invites.size()) {
                buttonList.add(new AlphaButton(ACCEPT,left+14,footerTop+10,90,24,"ACCEPT",true));
                buttonList.add(new AlphaButton(DECLINE,left+112,footerTop+10,90,24,"DECLINE"));
            }
        } else if (tab==TAB_REQUESTS && (s.owner||s.officer)) {
            addRows(s.requests,bodyTop+18,Math.max(1,(footerTop-bodyTop-26)/26),false);
            if (selected>=0 && selected<s.requests.size()) {
                buttonList.add(new AlphaButton(ACCEPT,left+14,footerTop+10,90,24,"APPROVE",true));
                buttonList.add(new AlphaButton(DECLINE,left+112,footerTop+10,90,24,"DECLINE"));
            }
        } else if (tab==TAB_CHAT && s.clan!=null) {
            input=field(45,left+14,footerTop+10,panelWidth-112,24);
            buttonList.add(new AlphaButton(SEND,left+panelWidth-88,footerTop+10,74,24,"SEND",true));
        }
        renderedSnapshot=s;
        observedClanId=clanId;
        AlphaClanService.INSTANCE.refreshIfStale();
    }

    private List<Integer> availableTabs(AlphaClanService.Snapshot s) {
        List<Integer> result=new ArrayList<Integer>();
        if (s.clan==null) {
            result.add(TAB_BROWSER);
            if (s.invites.size()>0) result.add(TAB_INVITES);
        } else {
            result.add(TAB_CLAN);
            if (s.owner||s.officer) result.add(TAB_REQUESTS);
            if (s.owner) result.add(TAB_SETTINGS);
            result.add(TAB_CHAT);
        }
        return result;
    }
    private String tabLabel(int id) {
        switch(id) {
            case TAB_BROWSER:return "BROWSER";
            case TAB_CLAN:return "MY CLAN";
            case TAB_INVITES:return "INVITES";
            case TAB_REQUESTS:return "REQUESTS";
            case TAB_CHAT:return "CHAT";
            case TAB_SETTINGS:return "SETTINGS";
            default:return "";
        }
    }
    private GuiTextField field(int id,int x,int y,int w,int h) {
        GuiTextField f=new GuiTextField(id,fontRendererObj,x,y,w,h);
        f.setMaxStringLength(id==45?256:(id==41||id==44?3:(id==40||id==43?24:64)));
        f.setEnableBackgroundDrawing(true); f.setTextColor(0xFFE8EAF0); return f;
    }
    private void addRows(JsonArray rows,int y,int max,boolean browser) {
        int count=Math.min(max,rows.size());
        for(int i=0;i<count;i++) {
            JsonObject row=rows.get(i).getAsJsonObject();
            String label,meta;
            if(browser) {
                label=value(row,"name");
                meta="["+value(row,"tag")+"]   "+value(row,"member_count")+"/50   "+value(row,"visibility").toUpperCase();
            } else if(tab==TAB_CLAN) {
                label=AlphaClanService.playerName(row); meta=value(row,"role").toUpperCase();
            } else if(tab==TAB_INVITES) {
                JsonObject clan=AlphaClanService.nested(row,"clans");
                label=value(clan,"name"); meta="["+value(clan,"tag")+"]";
            } else {
                label=AlphaClanService.playerName(row); meta="PENDING";
            }
            int id=SELECT_BASE+i;
            buttonList.add(new AlphaButton(id,left+14,y+i*25,panelWidth-28,22,"",
                    false,i==(browser?selectedBrowser:selected),true));
        }
    }
    @Override public void updateScreen() {
        super.updateScreen();
        AlphaClanService.INSTANCE.refreshIfStale();
        Object current=AlphaClanService.INSTANCE.getSnapshot();
        if(current!=renderedSnapshot) {
            AlphaClanService.Snapshot s=(AlphaClanService.Snapshot)current;
            String newClanId=AlphaClanService.str(s.clan,"id");
            if(observedClanId==null && newClanId!=null) {
                tab=TAB_CLAN; creatingClan=false; selected=-1; selectedBrowser=-1;
            } else if(observedClanId!=null && newClanId==null) {
                tab=TAB_BROWSER; creatingClan=false; selected=-1; selectedBrowser=-1;
            }
            String n=name==null?"":name.getText(), t=tag==null?"":tag.getText(), v=input==null?"":input.getText();
            initGui();
            if(name!=null && !n.isEmpty()) name.setText(n);
            if(tag!=null && !t.isEmpty()) tag.setText(t);
            if(input!=null) input.setText(v);
        }
    }
    @Override protected void actionPerformed(GuiButton b)throws IOException {
        if(b==null)return;
        if(b.id==BACK){mc.displayGuiScreen(parent);return;}
        if(b.id==TAB_BROWSER||b.id==TAB_CLAN||b.id==TAB_INVITES||b.id==TAB_REQUESTS||b.id==TAB_CHAT||b.id==TAB_SETTINGS) {
            tab=b.id; selected=-1; selectedBrowser=-1; creatingClan=false; initGui(); return;
        }
        if(b.id==VIS_PUBLIC||b.id==VIS_PRIVATE) {
            privateClan=b.id==VIS_PRIVATE;
            String n=name==null?"":name.getText(), t=tag==null?"":tag.getText();
            initGui(); if(name!=null)name.setText(n); if(tag!=null)tag.setText(t); return;
        }
        if(b.id==OPEN_CREATE){creatingClan=true;selectedBrowser=-1;initGui();return;}
        if(b.id==CANCEL_CREATE){creatingClan=false;name=null;tag=null;initGui();return;}
        if(b.id>=SELECT_BASE&&b.id<SELECT_BASE+100) {
            int index=b.id-SELECT_BASE;
            if(tab==TAB_BROWSER) selectedBrowser=index; else selected=index;
            String v=input==null?"":input.getText(); initGui(); if(input!=null)input.setText(v); return;
        }
        AlphaClanService.Snapshot s=AlphaClanService.INSTANCE.getSnapshot();
        if(b.id==CREATE&&name!=null&&tag!=null) {
            AlphaClanService.INSTANCE.create(name.getText(),tag.getText(),privateClan); return;
        }
        if(b.id==JOIN&&s.clan==null&&selectedBrowser>=0&&selectedBrowser<s.browser.size()) {
            JsonObject c=s.browser.get(selectedBrowser).getAsJsonObject(); String id=value(c,"id");
            if("public".equalsIgnoreCase(value(c,"visibility"))) AlphaClanService.INSTANCE.join(id);
            else AlphaClanService.INSTANCE.requestJoin(id);
            return;
        }
        if(b.id==INVITE&&input!=null){AlphaClanService.INSTANCE.invite(input.getText());input.setText("");return;}
        if(b.id==SEND&&input!=null){AlphaClanService.INSTANCE.sendMessage(input.getText());input.setText("");return;}
        if(b.id==LEAVE){AlphaClanService.INSTANCE.leave();return;}
        if(b.id==DISBAND){AlphaClanService.INSTANCE.disband();return;}
        if((b.id==ACCEPT||b.id==DECLINE)&&selected>=0) {
            JsonArray rows=tab==TAB_INVITES?s.invites:s.requests;
            if(selected>=rows.size())return;
            JsonObject row=rows.get(selected).getAsJsonObject();
            if(tab==TAB_INVITES)AlphaClanService.INSTANCE.acceptInvite(value(row,"id"),b.id==ACCEPT);
            else AlphaClanService.INSTANCE.respondRequest(value(row,"id"),b.id==ACCEPT);
            return;
        }
        if(tab==TAB_CLAN&&selected>=0&&selected<s.members.size()) {
            JsonObject m=s.members.get(selected).getAsJsonObject();
            String id=value(m,"profile_id");
            if(id.equals(value(s.clan,"owner_id")))return;
            if(b.id==KICK)AlphaClanService.INSTANCE.kick(id);
            if(b.id==ROLE)AlphaClanService.INSTANCE.setRole(id,!"officer".equalsIgnoreCase(value(m,"role")));
            if(b.id==TRANSFER)AlphaClanService.INSTANCE.transfer(id);
        }
        if(b.id==SAVE&&name!=null&&tag!=null&&s.clan!=null)
            AlphaClanService.INSTANCE.updateSettings(name.getText(),tag.getText(),value(s.clan,"description"),privateClan);
    }
    @Override protected void keyTyped(char ch,int key)throws IOException {
        if(name!=null&&name.isFocused()){name.textboxKeyTyped(ch,key);return;}
        if(tag!=null&&tag.isFocused()){tag.textboxKeyTyped(ch,key);return;}
        if(input!=null&&input.isFocused()) {
            if(key==Keyboard.KEY_RETURN&&tab==TAB_CHAT){AlphaClanService.INSTANCE.sendMessage(input.getText());input.setText("");return;}
            input.textboxKeyTyped(ch,key);return;
        }
        if(key==Keyboard.KEY_ESCAPE){mc.displayGuiScreen(parent);return;}
        super.keyTyped(ch,key);
    }
    @Override protected void mouseClicked(int x,int y,int b)throws IOException {
        super.mouseClicked(x,y,b);
        if(name!=null)name.mouseClicked(x,y,b);
        if(tag!=null)tag.mouseClicked(x,y,b);
        if(input!=null)input.mouseClicked(x,y,b);
    }
    @Override public void drawScreen(int mx,int my,float pt) {
        drawDefaultBackground();
        drawRect(left,top,left+panelWidth,top+panelHeight,0xF20B0D12);
        drawRect(left,top,left+panelWidth,top+2,0xFFFFD866);
        drawRect(left+1,top+1,left+panelWidth-1,top+panelHeight-1,0x221F2530);
        drawString(fontRendererObj,"ALPHACLANS",left+16,top+17,0xFFFFD866);
        drawString(fontRendererObj,screenSubtitle(),left+16,top+31,0xFF9299A6);
        drawRect(left+12,top+45,left+panelWidth-12,top+46,0x553C424D);
        drawRect(left+12,top+81,left+panelWidth-12,top+82,0x553C424D);
        AlphaClanService.Snapshot s=AlphaClanService.INSTANCE.getSnapshot();

        if(tab==TAB_BROWSER&&s.clan==null) {
            if(creatingClan) {
                drawSection("CREATE YOUR CLAN",bodyTop+18);
                drawCenteredString(fontRendererObj,"Choose a unique name and a 2-3 letter tag.",width/2,bodyTop+34,0xFF9299A6);
                int formLeft=left+(panelWidth-292)/2;
                drawString(fontRendererObj,"CLAN NAME",formLeft,bodyTop+47,0xFFE6E8ED);
                drawString(fontRendererObj,"CLAN TAG",formLeft+216,bodyTop+47,0xFFE6E8ED);
                drawCenteredString(fontRendererObj,"WHO CAN JOIN?",width/2,bodyTop+82,0xFFE6E8ED);
                drawCenteredString(fontRendererObj,privateClan
                        ?"Private clans appear in the directory; requests need approval."
                        :"Public clans can be joined immediately.",width/2,bodyTop+116,0xFF9299A6);
                if(name!=null)name.drawTextBox(); if(tag!=null)tag.drawTextBox();
            } else {
                drawSection("CLAN DIRECTORY",left+16,bodyTop);
                drawBrowserRows(s.browser,bodyTop+18);
                if(s.browser.size()==0)drawEmpty("NO OTHER CLANS","Create a clan to start your own community.",bodyTop+78);
            }
        } else if(tab==TAB_CLAN&&s.clan!=null) {
            drawRect(left+14,bodyTop,left+panelWidth-14,bodyTop+58,0x77212630);
            drawString(fontRendererObj,value(s.clan,"name"),left+24,bodyTop+12,0xFFE6E8ED);
            drawString(fontRendererObj,"["+value(s.clan,"tag")+"]",left+24,bodyTop+29,0xFFFFD866);
            String meta=s.members.size()+" / 50 members   •   "+value(s.clan,"visibility").toUpperCase();
            drawString(fontRendererObj,meta,left+panelWidth-24-fontRendererObj.getStringWidth(meta),bodyTop+20,0xFF9299A6);
            drawSection("MEMBERS",left+16,bodyTop+67);
            drawMemberRows(s.members,bodyTop+84);
            if(input!=null)input.drawTextBox();
        } else if(tab==TAB_SETTINGS&&s.owner&&s.clan!=null) {
            drawSection("CLAN IDENTITY",left+16,bodyTop+18);
            drawString(fontRendererObj,"CLAN NAME",left+panelWidth/2-146,bodyTop+49,0xFFE6E8ED);
            drawString(fontRendererObj,"TAG",left+panelWidth/2+68,bodyTop+49,0xFFE6E8ED);
            drawCenteredString(fontRendererObj,"Only the owner can change clan identity and visibility.",width/2,bodyTop+124,0xFF9299A6);
            if(name!=null)name.drawTextBox(); if(tag!=null)tag.drawTextBox();
        } else if(tab==TAB_INVITES&&s.clan==null) {
            drawSection("PENDING INVITATIONS",left+16,bodyTop);
            drawGenericRows(s.invites,bodyTop+18,2);
            if(s.invites.size()==0)drawEmpty("NO INVITATIONS","Clan invitations sent to you will appear here.",bodyTop+80);
        } else if(tab==TAB_REQUESTS&&(s.owner||s.officer)) {
            drawSection("JOIN REQUESTS",left+16,bodyTop);
            drawGenericRows(s.requests,bodyTop+18,3);
            if(s.requests.size()==0)drawEmpty("NO PENDING REQUESTS","New clan join requests will appear here.",bodyTop+80);
        } else if(tab==TAB_CHAT&&s.clan!=null) {
            drawSection("CLAN CHAT",left+16,bodyTop);
            int count=Math.min(12,s.messages.size());
            if(count==0)drawEmpty("NO MESSAGES YET","Start the conversation with your clan.",bodyTop+82);
            for(int i=0;i<count;i++) {
                JsonObject m=s.messages.get(count-i-1).getAsJsonObject();
                String user=value(AlphaClanService.nested(m,"profiles"),"username");
                String body=value(m,"body");
                int rowY=bodyTop+22+i*19;
                drawRect(left+14,rowY-3,left+panelWidth-14,rowY+14,0x55212630);
                fontRendererObj.drawString(clip(user,100),left+22,rowY,0xFFFFD866);
                int offset=fontRendererObj.getStringWidth(clip(user,100))+32;
                fontRendererObj.drawStringWithShadow(clip(body,panelWidth-offset-36),left+offset,rowY,0xFFE6E8ED);
            }
            if(input!=null)input.drawTextBox();
        }
        drawRect(left+12,top+panelHeight-27,left+panelWidth-12,top+panelHeight-26,0x443C424D);
        fontRendererObj.drawString(clip(s.status,panelWidth-32),left+16,top+panelHeight-18,0xFF9299A6);
        super.drawScreen(mx,my,pt);
    }
    private String screenSubtitle() {
        switch(tab) {
            case TAB_BROWSER:return "Find a clan or create your own";
            case TAB_CLAN:return "Your clan and its members";
            case TAB_INVITES:return "Invitations sent to your account";
            case TAB_REQUESTS:return "Review requests to join your clan";
            case TAB_CHAT:return "Private conversation with your clan";
            case TAB_SETTINGS:return "Manage your clan identity";
            default:return "";
        }
    }
    private void drawSection(String text,int y){drawSection(text,left+16,y);}
    private void drawSection(String text,int x,int y){fontRendererObj.drawString(text,x,y,0xFFFFD866);}
    private void drawBrowserRows(JsonArray rows,int y) {
        int max=Math.max(1,(footerTop-y-8)/25);
        for(int i=0;i<Math.min(max,rows.size());i++) {
            JsonObject r=rows.get(i).getAsJsonObject();
            String title=value(r,"name"),meta="["+value(r,"tag")+"]   "+value(r,"member_count")+"/50   "+value(r,"visibility").toUpperCase();
            drawRow(title,meta,y+i*25,i==selectedBrowser);
        }
    }
    private void drawMemberRows(JsonArray rows,int y) {
        int max=Math.max(1,(footerTop-y-8)/25);
        for(int i=0;i<Math.min(max,rows.size());i++) {
            JsonObject r=rows.get(i).getAsJsonObject();
            drawRow(AlphaClanService.playerName(r),value(r,"role").toUpperCase(),y+i*25,i==selected);
        }
    }
    private void drawGenericRows(JsonArray rows,int y,int mode) {
        int max=Math.max(1,(footerTop-y-8)/25);
        for(int i=0;i<Math.min(max,rows.size());i++) {
            JsonObject r=rows.get(i).getAsJsonObject();
            String title,meta;
            if(mode==2) {JsonObject clan=AlphaClanService.nested(r,"clans");title=value(clan,"name");meta="["+value(clan,"tag")+"]";}
            else {title=AlphaClanService.playerName(r);meta="PENDING";}
            drawRow(title,meta,y+i*25,i==selected);
        }
    }
    private void drawRow(String title,String meta,int y,boolean active) {
        drawRect(left+14,y,left+panelWidth-14,y+22,active?0xAA302A1B:0x77212630);
        drawRect(left+14,y,left+15,y+22,active?0xFFFFD866:0x664B515D);
        fontRendererObj.drawStringWithShadow(clip(title,panelWidth/2),left+24,y+7,0xFFE6E8ED);
        String right=clip(meta,panelWidth/2-30);
        fontRendererObj.drawString(right,left+panelWidth-24-fontRendererObj.getStringWidth(right),y+7,active?0xFFFFD866:0xFF9299A6);
    }
    private void drawEmpty(String title,String detail,int y) {
        drawCenteredString(fontRendererObj,title,width/2,y,0xFFE6E8ED);
        drawCenteredString(fontRendererObj,detail,width/2,y+15,0xFF9299A6);
    }
    private String value(JsonObject o,String key) {
        String v=AlphaClanService.str(o,key); return v==null?"":v;
    }
    private String clip(String s,int max) {
        if(s==null)return "";
        while(!s.isEmpty()&&fontRendererObj.getStringWidth(s)>max)s=s.substring(0,s.length()-1);
        return s;
    }
    @Override public boolean doesGuiPauseGame(){return false;}
}
