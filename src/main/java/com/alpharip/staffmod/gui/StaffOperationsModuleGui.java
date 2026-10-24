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
    private static final int BACK = 1, REFRESH = 2, CREATE = 3, NEXT_STATUS = 4, SAVE = 5, DELETE = 6;
    private static final int ACCENT = 0xFFFFD866;
    private final net.minecraft.client.gui.GuiScreen parent;
    private final String type;
    private final String title;
    private GuiTextField titleField;
    private GuiTextField detailsField;
    private JsonArray records = new JsonArray();
    private JsonArray auditLogs = new JsonArray();
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
        buttonList.add(new AlphaButton(REFRESH, contentLeft, y - 29, 76, 20, "Refresh"));
        if (!"audit".equals(type)) { buttonList.add(new AlphaButton(CREATE, contentLeft + 82, y - 29, 94, 20, "Create", true)); buttonList.add(new AlphaButton(SAVE, contentLeft + 180, y - 29, 76, 20, "Save edit")); if (canManageRecords()) buttonList.add(new AlphaButton(DELETE, contentLeft + 260, y - 29, 76, 20, "Delete")); }
        if (!"audit".equals(type)) buttonList.add(new AlphaButton(NEXT_STATUS, contentRight - 140, y - 29, 140, 20, "Next status"));
        buttonList.add(new AlphaButton(BACK, contentRight - 80, y, 80, 20, "Back"));
        refreshRecords();
        if (SupabaseCommunityService.isSignedIn()) { try { SupabaseCommunityService.recordStaffModuleActivity("staff_operations", "module_opened", "open_" + type, title, null); } catch (Exception ignored) { } }
    }

    private boolean canManageRecords() {
        PermissionService.Rank rank = PermissionService.getEffectiveRank();
        return rank == PermissionService.Rank.DEVELOPER
                || rank == PermissionService.Rank.MANAGER
                || rank == PermissionService.Rank.OWNER
                || rank == PermissionService.Rank.FOUNDER;
    }

    private void refreshRecords() {
        if (!SupabaseCommunityService.isSignedIn()) { records = new JsonArray(); status = "Sign in to your Alpha Account to use shared staff records."; return; }
        try {
            if ("audit".equals(type)) { auditLogs = SupabaseCommunityService.getStaffAuditLogs(); records = new JsonArray(); status = "Loaded " + auditLogs.size() + " audit event(s)."; }
            else { records = SupabaseCommunityService.getStaffOperationsRecords(); status = "Synced. " + countType() + " " + type + " record(s)."; }
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
        if(b.id==SAVE) {
            JsonObject selected=findSelected();
            if(selected==null) { status="Select a record first."; return; }
            String nextTitle=titleField.getText().trim();
            if(nextTitle.isEmpty()) { status="Record title cannot be empty."; return; }
            JsonObject payload=selected.has("payload")&&selected.get("payload").isJsonObject()?selected.getAsJsonObject("payload"):new JsonObject();
            payload.addProperty("details",detailsField.getText().trim());
            try { SupabaseCommunityService.updateStaffOperationsRecord(selectedId,nextTitle,selectedStatus,payload); SupabaseCommunityService.recordStaffModuleActivity("staff_operations", "action_performed", "record_updated", nextTitle, payload); refreshRecords(); status="Record updated."; }
            catch(Exception e) { status="Update denied or failed: "+safe(e.getMessage()); }
            return;
        }
        if(b.id==DELETE) {
            if(selectedId==null) { status="Select a record first."; return; }
            try { SupabaseCommunityService.deleteStaffOperationsRecord(selectedId); SupabaseCommunityService.recordStaffModuleActivity("staff_operations", "action_performed", "record_deleted", selectedId, null); selectedId=null; refreshRecords(); status="Delete accepted by database."; }
            catch(Exception e) { status="Delete denied or failed: "+safe(e.getMessage()); }
            return;
        }
        if(b.id==CREATE) {
            String name=titleField==null?"":titleField.getText().trim();
            if(name.isEmpty()) { status="Enter a title before creating a record."; return; }
            JsonObject payload=new JsonObject();
            payload.addProperty("details", detailsField==null?"":detailsField.getText().trim());
            payload.addProperty("created_by_label", SupabaseCommunityService.getSignedInAlphaName());
            try {
                JsonObject created=SupabaseCommunityService.createStaffOperationsRecord(type,name,"open",payload);
                selectedId=value(created,"id",null); selectedStatus=value(created,"status","open"); SupabaseCommunityService.recordStaffModuleActivity("staff_operations", "action_performed", "record_created", name, payload);
                titleField.setText(""); detailsField.setText("");
                refreshRecords(); status="Record created and shared with staff.";
            } catch(Exception e) { status="Create failed: "+safe(e.getMessage()); }
            return;
        }
        if(b.id==NEXT_STATUS) {
            JsonObject selected=findSelected();
            if(selected==null) { status="Select a record by clicking it first."; return; }
            selectedStatus=nextStatus(selectedStatus);
            JsonObject payload=selected.has("payload")&&selected.get("payload").isJsonObject()?selected.getAsJsonObject("payload"):new JsonObject();
            try { SupabaseCommunityService.updateStaffOperationsRecord(selectedId,value(selected,"title","Untitled"),selectedStatus,payload); SupabaseCommunityService.recordStaffModuleActivity("staff_operations", "action_performed", "status_changed_to_" + selectedStatus, value(selected,"title","Untitled"), payload); refreshRecords(); status="Status changed to "+selectedStatus+"."; }
            catch(Exception e) { status="Status change denied or failed: "+safe(e.getMessage()); }
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
                if(index++==row) { selectedId=value(r,"id",null); selectedStatus=value(r,"status","open"); status="Selected: "+value(r,"title","Untitled")+" ["+selectedStatus+"]"; if(titleField!=null) titleField.setText(value(r,"title","")); if(detailsField!=null) detailsField.setText(value(r.get("payload")!=null&&r.get("payload").isJsonObject()?r.getAsJsonObject("payload"):null,"details","")); break; }
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
        if ("audit".equals(type)) {
            for(int i=0;i<auditLogs.size() && shown<7;i++) {
                if(!auditLogs.get(i).isJsonObject()) continue;
                JsonObject event=auditLogs.get(i).getAsJsonObject();
                String line=value(event,"occurred_at","")+"  "+value(event,"actor_label",value(event,"actor_id","Unknown staff"))+"  /  "+value(event,"category","client")+"  /  "+value(event,"action","event")+"  /  "+value(event,"outcome","unknown");
                if(line.length()>100) line=line.substring(0,97)+"...";
                drawString(fontRendererObj,line,contentLeft,y+shown*19,0xFFE6E8ED); shown++;
            }
            if(shown==0) drawString(fontRendererObj,"No audit events are available to this account.",contentLeft,y,0xFFB8BDC8);
        } else for(int i=0;i<records.size() && shown<5;i++) {
            if(!records.get(i).isJsonObject()) continue;
            JsonObject r=records.get(i).getAsJsonObject();
            if(!type.equals(value(r,"record_type",""))) continue;
            String line=value(r,"title","Untitled")+"  ["+value(r,"status","unknown")+"]  by "+value(r,"created_by","unknown");
            if(line.length()>92) line=line.substring(0,89)+"...";
            drawString(fontRendererObj,(value(r,"id","").equals(selectedId)?"> ":"  ")+line,contentLeft,y+shown*19,0xFFE6E8ED);
            String details=value(r.has("payload")&&r.get("payload").isJsonObject()?r.getAsJsonObject("payload"):null,"details","");
            if(!details.isEmpty() && shown<2) drawString(fontRendererObj,details.length()>90?details.substring(0,87)+"...":details,contentLeft+10,y+shown*19+9,0xFF9299A6);
            shown++;
        }
        if(shown==0) drawString(fontRendererObj,"No "+type+" records have been created yet.",contentLeft,y,0xFFB8BDC8);
        drawRect(panelLeft+14,bottom-36,right-14,bottom-35,0x334B515D);
        drawString(fontRendererObj,"Status: "+status,panelLeft+14,bottom-25,0xFFB8BDC8);
        super.drawScreen(mx,my,pt);
    }
    private JsonObject findSelected() {
        if(selectedId==null) return null;
        for(int i=0;i<records.size();i++) if(records.get(i).isJsonObject() && selectedId.equals(value(records.get(i).getAsJsonObject(),"id",null))) return records.get(i).getAsJsonObject();
        return null;
    }
    private String nextStatus(String current) {
        if("presence".equals(type)) return "active".equals(current)?"away":"away".equals(current)?"busy":"active";
        if("open".equals(current)) return "in_progress";
        if("in_progress".equals(current)) return "resolved";
        if("resolved".equals(current)) return "closed";
        return "open";
    }
    private String value(JsonObject o,String key,String fallback) {
        try { return o!=null&&o.has(key)&&!o.get(key).isJsonNull()?o.get(key).getAsString():fallback; }
        catch(Exception e){return fallback;}
    }
    private String safe(String s) { if(s==null||s.trim().isEmpty())return "request failed"; s=s.replace('\n',' ').replace('\r',' '); return s.length()>90?s.substring(0,90):s; }
    @Override public boolean doesGuiPauseGame(){return false;}
}