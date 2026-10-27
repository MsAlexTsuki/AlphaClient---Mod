// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import java.io.IOException;

/** Private staff notes. Supabase RLS is authoritative for visibility and mutations. */
public final class PrivateStaffNotesGui extends AlphaGuiScreen {
    private static final int BACK=1,REFRESH=2,CREATE=3,DELETE=4,EDIT=5;
    private static final int ACCENT=0xFFFFD866;
    private final net.minecraft.client.gui.GuiScreen parent;
    private GuiTextField playerField,noteField;
    private JsonArray notes=new JsonArray();
    private String status="Loading private notes...";
    private int left,top,panelWidth,panelHeight,selectedIndex=-1,listTop;
    public PrivateStaffNotesGui(net.minecraft.client.gui.GuiScreen parent){this.parent=parent;}
    @Override public void initGui(){
        buttonList.clear();if(!SupabaseCommunityService.hasStaffOperationsAccess()){mc.displayGuiScreen(parent);return;}
        panelWidth=Math.min(690,Math.max(360,width-24));panelHeight=Math.min(430,Math.max(300,height-32));left=(width-panelWidth)/2;top=(height-panelHeight)/2;
        playerField=new GuiTextField(20,fontRendererObj,left+16,top+72,panelWidth-32,20);playerField.setMaxStringLength(16);
        noteField=new GuiTextField(21,fontRendererObj,left+16,top+112,panelWidth-32,20);noteField.setMaxStringLength(2000);
        buttonList.add(new AlphaButton(REFRESH,left+16,top+panelHeight-31,68,20,"Refresh"));
        buttonList.add(new AlphaButton(CREATE,left+90,top+panelHeight-31,90,20,"Add note",true));
        buttonList.add(new AlphaButton(EDIT,left+186,top+panelHeight-31,86,20,"Edit selected"));
        buttonList.add(new AlphaButton(DELETE,left+278,top+panelHeight-31,94,20,"Delete selected"));
        buttonList.add(new AlphaButton(BACK,left+panelWidth-96,top+panelHeight-31,80,20,"Back"));refreshNotes();
    }
    private void refreshNotes(){try{notes=SupabaseCommunityService.getPrivateStaffNotes(null);if(selectedIndex>=notes.size())selectedIndex=-1;status="Loaded "+notes.size()+" note(s) visible to this account.";}catch(Exception e){status="Could not load notes: "+safe(e.getMessage());}}
    @Override protected void actionPerformed(GuiButton button)throws IOException{
        if(button==null)return;if(button.id==BACK){mc.displayGuiScreen(parent);return;}
        if(!SupabaseCommunityService.hasStaffOperationsAccess()){mc.displayGuiScreen(parent);return;}
        if(button.id==REFRESH){refreshNotes();return;}
        if(button.id==CREATE){try{SupabaseCommunityService.createPrivateStaffNote(playerField.getText().trim(),noteField.getText().trim());noteField.setText("");refreshNotes();status="Private note saved.";}catch(Exception e){status="Save denied or failed: "+safe(e.getMessage());}return;}
        if(button.id==EDIT||button.id==DELETE){
            if(selectedIndex<0||selectedIndex>=notes.size()||!notes.get(selectedIndex).isJsonObject()){status="Select a note from the list first.";return;}
            JsonObject item=notes.get(selectedIndex).getAsJsonObject();String id=value(item,"id","");if(id.isEmpty()){status="Selected note has no ID.";return;}
            try{if(button.id==EDIT){String replacement=noteField.getText().trim();if(replacement.isEmpty()){status="Enter replacement text above first.";return;}
                    SupabaseCommunityService.updatePrivateStaffNote(id,replacement);status="Note updated. Only its author can edit it.";
                }else{SupabaseCommunityService.deletePrivateStaffNote(id);status="Note deleted.";selectedIndex=-1;}refreshNotes();
            }catch(Exception e){status="Action denied or failed: "+safe(e.getMessage());}
        }
    }
    @Override protected void keyTyped(char typedChar,int keyCode)throws IOException{
        if(playerField!=null)playerField.textboxKeyTyped(typedChar,keyCode);if(noteField!=null)noteField.textboxKeyTyped(typedChar,keyCode);
        if(keyCode==Keyboard.KEY_ESCAPE){mc.displayGuiScreen(parent);return;}super.keyTyped(typedChar,keyCode);
    }
    @Override protected void mouseClicked(int mouseX,int mouseY,int mouseButton)throws IOException{
        super.mouseClicked(mouseX,mouseY,mouseButton);if(playerField!=null)playerField.mouseClicked(mouseX,mouseY,mouseButton);if(noteField!=null)noteField.mouseClicked(mouseX,mouseY,mouseButton);
        if(mouseButton==0&&mouseX>=left+16&&mouseX<=left+panelWidth-16&&mouseY>=listTop){int row=(mouseY-listTop)/28;
            if(row>=0&&row<notes.size()&&row<visibleRows()&&notes.get(row).isJsonObject()){selectedIndex=row;JsonObject item=notes.get(row).getAsJsonObject();playerField.setText(value(item,"subject_name",""));noteField.setText(value(item,"note",""));}}
    }
    private int visibleRows(){return Math.max(3,Math.min(7,(panelHeight-235)/28));}
    @Override public void drawScreen(int mouseX,int mouseY,float partialTicks){
        drawRect(0,0,width,height,0x30000000);drawRect(left,top,left+panelWidth,top+panelHeight,0xE00B0D12);drawRect(left,top,left+panelWidth,top+1,ACCENT);
        drawString(fontRendererObj,"Alpha  /  Staff operations",left+16,top+12,0xFFF0F1F4);drawString(fontRendererObj,"Private staff notes",left+16,top+29,ACCENT);
        drawString(fontRendererObj,"Select a note to edit its text or delete it. Supabase enforces author/rank rules.",left+16,top+47,0xFFB8BDC8);
        drawString(fontRendererObj,"Minecraft player",left+16,top+61,0xFFE6E8ED);if(playerField!=null)playerField.drawTextBox();
        drawString(fontRendererObj,"Note text (1-2000 characters; select a note to edit)",left+16,top+101,0xFFE6E8ED);if(noteField!=null)noteField.drawTextBox();
        drawString(fontRendererObj,"Visible notes ("+notes.size()+")",left+16,top+139,ACCENT);listTop=top+155;
        for(int i=0;i<notes.size()&&i<visibleRows();i++){if(!notes.get(i).isJsonObject())continue;JsonObject item=notes.get(i).getAsJsonObject();int y=listTop+i*28;
            drawRect(left+12,y-3,left+panelWidth-12,y+22,i==selectedIndex?0xFF29251B:0xFF151820);
            String line="#"+(i+1)+"  "+value(item,"subject_name","Unknown")+"  /  "+value(item,"created_by_label","Staff")+"  /  "+value(item,"note","");
            if(line.length()>108)line=line.substring(0,105)+"...";drawString(fontRendererObj,line,left+18,y+4,i==selectedIndex?ACCENT:0xFFE6E8ED);}
        if(notes.size()==0)drawString(fontRendererObj,"No private notes are visible to this account.",left+16,listTop+4,0xFFB8BDC8);
        drawString(fontRendererObj,"Status: "+status,left+16,top+panelHeight-47,0xFFB8BDC8);super.drawScreen(mouseX,mouseY,partialTicks);
    }
    private String value(JsonObject o,String key,String fallback){try{return o!=null&&o.has(key)&&!o.get(key).isJsonNull()?o.get(key).getAsString():fallback;}catch(Exception ignored){return fallback;}}
    private String safe(String value){if(value==null||value.trim().isEmpty())return "request failed";value=value.replace('\n',' ').replace('\r',' ');return value.length()>110?value.substring(0,110):value;}
    @Override public boolean doesGuiPauseGame(){return false;}
}
