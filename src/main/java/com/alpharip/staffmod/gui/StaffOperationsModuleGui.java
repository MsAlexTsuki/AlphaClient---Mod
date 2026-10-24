package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.permissions.PermissionService;
import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import java.io.IOException;

/** Shared, interactive module screens for Staff Operations records. */
public final class StaffOperationsModuleGui extends AlphaGuiScreen {
    private static final int BACK = 1, REFRESH = 2, CREATE = 3, NEXT_STATUS = 4;
    private static final int ACCENT = 0xFFFFD866;
    private final net.minecraft.client.gui.GuiScreen parent;
    private final String type;
    private final String title;
    private GuiTextField titleField;
    private GuiTextField detailsField;
    private JsonArray records = new JsonArray();
    private String status = "Loading shared records...";
    private String selectedId = null;
    private String selectedStatus = "open";
    private int panelLeft, panelTop, panelWidth, panelHeight, contentLeft, contentRight;

    public StaffOperationsModuleGui(net.minecraft.client.gui.GuiScreen parent, String type) {
        this.parent = parent;
        this.type = type;
        this.title = "case".equals(type) ? "Cases" : "shift".equals(type) ? "Shifts" : "presence".equals(type) ? "Presence" : "Activity and audit";
    }

    @Override public void initGui() {
        buttonList.clear();
        if (!PermissionService.hasStaffModsAccess()) { mc.displayGuiScreen(parent); return; }
        panelWidth = Math.min(650, Math.max(360, width - 28));
        panelHeight = Math.min(390, Math.max(280, height - 24));
        panelLeft = (width - panelWidth) / 2; panelTop = (height - panelHeight) / 2;
        contentLeft = panelLeft + 16; contentRight = panelLeft + panelWidth - 16;
        if (!"audit".equals(type)) {
            titleField = new GuiTextField(20, fontRendererObj, contentLeft, panelTop + 88, contentRight - contentLeft, 20);
            titleField.setMaxStringLength(80);
            detailsField = new GuiTextField(21, fontRendererObj, contentLeft, panelTop + 133, contentRight - contentLeft, 20);
            detailsField.setMaxStringLength(240);
        }
        int y = panelTop + panelHeight - 32;
        buttonList.add(new AlphaButton(REFRESH, contentLeft, y - 29, 90, 20, "Refresh"));
        if (!"audit".equals(type)) buttonList.add(new AlphaButton(CREATE, contentLeft + 98, y - 29, 120, 20, "Create record", true));
        if ("case".equals(type) || "shift".equals(type)) buttonList.add(new AlphaButton(NEXT_STATUS, contentRight - 140, y - 29, 140, 20, "Change selected status"));
        buttonList.add(new AlphaButton(BACK, contentRight - 80, y, 80, 20, "Back"));
        refreshRecords();
    }

    private void refreshRecords() {
        if (!SupabaseCommunityService.isSignedIn()) { records = new JsonArray(); status = "Sign in to your Alpha Account to use shared staff records."; return; }
        try {
            records = SupabaseCommunityService.getStaffOperationsRecords();
            status = "Synced. " + countType() + " " + type + " record(s).";
        } catch (Exception e) { status = "Could not load records: " + safe(e.getMessage()); }
    }
    private int countType() {
        int count=0;
        for(int i=0;i<records.size();i++) if(records.get(i).isJsonObject() && type.equals(value(records.get(i).getAsJsonObject(),"record_type",""))) count++;
        return count;
    }
    @Override protected void actionPerformed(GuiButton b) throws IOException {
        if(b==null) return;
        if(!PermissionService.hasStaffModsAccess()) { mc.displayGuiScreen(parent); return; }
        if(b.id==BACK) { mc.displayGuiScreen(parent); return; }
        if(b.id==REFRESH) { refreshRecords(); return; }
        if(b.id==CREATE) {
            String name=titleField==null?"":titleField.getText().trim();
            if(name.isEmpty()) { status="Enter a title before creating a record."; return; }
            JsonObject payload=new JsonObject();
            payload.addProperty("details", detailsField==null?"":detailsField.getText().trim());
            payload.addProperty("created_by_label", SupabaseCommunityService.getSignedInAlphaName());
            try {
                JsonObject created=SupabaseCommunityService.createStaffOperationsRecord(type,name,"open",payload);
                selectedId=value(created,"id",null); selectedStatus=value(created,"status","open");
                titleField.setText(""); detailsField.setText("");
                refreshRecords(); status="Record created and shared with staff.";
            } catch(Exception e) { status="Create failed: "+safe(e.getMessage()); }
            return;
        }
        if(b.id==NEXT_STATUS) {
            if(selectedId==null) { status="Select a record by clicking it first."; return; }
            status="Status editing requires the protected management endpoint; no change was made.";
        }
    }
    @Override protected void keyTyped(char c, int key) throws IOException {
        if(titleField!=null) titleField.textboxKeyTyped(c,key);
        if(detailsField!=null) detailsField.textboxKeyTyped(c,key);
        if(key==Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
        super.keyTyped(c,key);
    }
    @Override protected void mouseClicked(int x,int y,int button) throws IOException {
        super.mouseClicked(x,y,button);
        if(titleField!=null) titleField.mouseClicked(x,y,button);
        if(detailsField!=null) detailsField.mouseClicked(x,y,button);
        int listTop=panelTop+178;
        int row= (y-listTop)/19;
        if(y>=listTop && row>=0) {
            int index=0;
            for(int i=0;i<records.size();i++) {
                if(!records.get(i).isJsonObject()) continue;
                JsonObject r=records.get(i).getAsJsonObject();
                if(!type.equals(value(r,"record_type",""))) continue;
                if(index++==row) { selectedId=value(r,"id",null); selectedStatus=value(r,"status","open"); status="Selected: "+value(r,"title","Untitled")+" ["+selectedStatus+"]"; break; }
            }
        }
    }
    @Override public void drawScreen(int mx,int my,float pt) {
        drawRect(0,0,width,height,0x30000000);
        int right=panelLeft+panelWidth,bottom=panelTop+panelHeight;
        drawRect(panelLeft,panelTop,right,bottom,0xE00B0D12);
        drawRect(panelLeft,panelTop,right,panelTop+1,ACCENT);
        drawRect(panelLeft+14,panelTop+66,right-14,panelTop+67,0x554B515D);
        drawString(fontRendererObj,"Alpha  /  Staff operations",panelLeft+16,panelTop+13,0xFFF0F1F4);
        drawString(fontRendererObj,"Shared staff information",panelLeft+16,panelTop+30,ACCENT);
        drawString(fontRendererObj,title,panelLeft+16,panelTop+49,0xFFB8BDC8);
        if(!"audit".equals(type)) {
            drawString(fontRendererObj,"Record title",contentLeft,panelTop+75,0xFFE6E8ED);
            if(titleField!=null) titleField.drawTextBox();
            drawString(fontRendererObj,"Details / note",contentLeft,panelTop+119,0xFFE6E8ED);
            if(detailsField!=null) detailsField.drawTextBox();
        }
        int y=panelTop+178;
        drawString(fontRendererObj,"Shared records (click a row to select)",contentLeft,y-13,ACCENT);
        int shown=0;
        for(int i=0;i<records.size() && shown<5;i++) {
            if(!records.get(i).isJsonObject()) continue;
            JsonObject r=records.get(i).getAsJsonObject();
            if(!type.equals(value(r,"record_type",""))) continue;
            String line=value(r,"title","Untitled")+"  ["+value(r,"status","unknown")+"]  by "+value(r,"created_by","unknown");
            if(line.length()>92) line=line.substring(0,89)+"...";
            drawString(fontRendererObj,(value(r,"id","").equals(selectedId)?"> ":"  ")+line,contentLeft,y+shown*19,0xFFE6E8ED);
            String details=value(r.getAsJsonObject("payload"),"details","");
            if(!details.isEmpty() && shown<2) drawString(fontRendererObj,details.length()>90?details.substring(0,87)+"...":details,contentLeft+10,y+shown*19+9,0xFF9299A6);
            shown++;
        }
        if(shown==0) drawString(fontRendererObj,"No "+type+" records have been created yet.",contentLeft,y,0xFFB8BDC8);
        drawRect(panelLeft+14,bottom-36,right-14,bottom-35,0x334B515D);
        drawString(fontRendererObj,"Status: "+status,panelLeft+14,bottom-25,0xFFB8BDC8);
        super.drawScreen(mx,my,pt);
    }
    private String value(JsonObject o,String key,String fallback) {
        try { return o!=null&&o.has(key)&&!o.get(key).isJsonNull()?o.get(key).getAsString():fallback; }
        catch(Exception e){return fallback;}
    }
    private String safe(String s) { if(s==null||s.trim().isEmpty())return "request failed"; s=s.replace('\n',' ').replace('\r',' '); return s.length()>90?s.substring(0,90):s; }
    @Override public boolean doesGuiPauseGame(){return false;}
}