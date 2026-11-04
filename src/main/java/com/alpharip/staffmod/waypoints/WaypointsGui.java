package com.alpharip.staffmod.waypoints;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import com.alpharip.staffmod.gui.AlphaButton;
import com.alpharip.staffmod.gui.AlphaGuiScreen;
import org.lwjgl.input.Mouse;
import java.io.File;
import java.io.IOException;
import java.util.List;

/** Compact waypoint manager with an integrated editor for the selected waypoint. */
public final class WaypointsGui extends AlphaGuiScreen {
    private static final int GOLD=0xFFFFD866;
    private static final int[] COLORS={
            0xFFFF5555,0xFFFFAA00,0xFFFFFF55,0xFF55FF55,0xFF55FFFF,
            0xFF5555FF,0xFFAA55FF,0xFFFF55FF,0xFFFFFFFF,0xFFFFD866
    };
    private final GuiScreen parent;
    private String selectedId="";
    private String status="Select a waypoint to edit it.";
    private int left,top,panelWidth,panelHeight,listWidth,listTop,listBottom;
    private int scroll,selectedColor=GOLD;
    private GuiTextField nameField;

    public WaypointsGui(GuiScreen parent){this.parent=parent;}

    @Override public void initGui() {
        buttonList.clear();
        panelWidth=Math.min(570,width-24);
        panelHeight=Math.min(340,height-24);
        left=(width-panelWidth)/2;
        top=(height-panelHeight)/2;
        listWidth=Math.min(220,Math.max(150,(panelWidth-26)*2/5));
        listTop=top+82;
        listBottom=top+panelHeight-54;

        int toggleY=top+41;
        buttonList.add(new AlphaButton(8,left+14,toggleY,104,18,
                "Hide: "+onOff(WaypointSettings.isTemporarilyHidden())));
        buttonList.add(new AlphaButton(10,left+123,toggleY,Math.max(104,listWidth-92),18,
                "Sort: "+sortName()));

        int closeWidth=52;
        buttonList.add(new AlphaButton(7,left+panelWidth-closeWidth-14,top+10,closeWidth,18,"Close"));

        int buttonY=top+panelHeight-31;
        int gap=5;
        int buttonWidth=(panelWidth-28-gap*5)/6;
        String activeTargetId=WaypointManager.getInstance().getSelectedId();
        boolean navigating=!activeTargetId.isEmpty()
                &&(selectedId.isEmpty()||activeTargetId.equals(selectedId));
        String[] labels={"Create",navigating?"Stop":"Navigate","Delete","Export","Import","Options"};
        for(int i=0;i<labels.length;i++)
            buttonList.add(new AlphaButton(i==5?6:i+1,left+14+i*(buttonWidth+gap),buttonY,buttonWidth,20,labels[i]));

        StaffWaypoint selected=findCurrent(selectedId);
        if(selected==null) {
            StaffWaypoint managerSelected=WaypointManager.getInstance().getSelected();
            if(managerSelected!=null) selected=findCurrent(managerSelected.getId());
        }
        if(selected!=null) {
            selectedId=selected.getId();
            selectedColor=selected.getColor();
        } else {
            selectedId="";
            selectedColor=GOLD;
        }

        int editorX=left+listWidth+25;
        int editorWidth=panelWidth-listWidth-39;
        nameField=new GuiTextField(30,fontRendererObj,editorX+12,top+105,editorWidth-24,20);
        nameField.setMaxStringLength(40);
        nameField.setText(selected==null?"":selected.getLabel());
        nameField.setEnabled(selected!=null);
        nameField.setFocused(false);

        if(selected!=null) {
            buttonList.add(new AlphaButton(11,editorX+12,top+panelHeight-99,editorWidth-24,19,"Save Changes",true));
            buttonList.add(new AlphaButton(12,editorX+12,top+panelHeight-75,(editorWidth-29)/2,18,
                    "Favorite: "+onOff(selected.isFavorite())));
            buttonList.add(new AlphaButton(13,editorX+17+(editorWidth-29)/2,top+panelHeight-75,
                    (editorWidth-29)/2,18,"Chroma: "+onOff(selected.isChromaEnabled())));
        }
    }

    private StaffWaypoint findCurrent(String id) {
        if(id==null||id.isEmpty())return null;
        for(StaffWaypoint point:WaypointManager.getInstance().getForCurrentWorld())
            if(point.getId().equals(id))return point;
        return null;
    }

    private String sortName() {
        String mode=WaypointSettings.getString("sortMode","distance");
        if("name".equalsIgnoreCase(mode))return "Name";
        if("category".equalsIgnoreCase(mode))return "Category";
        if("modified".equalsIgnoreCase(mode))return "Recently Modified";
        return "Distance";
    }

    private static String nextSort(String current) {
        String[] sorts={"distance","name","category","modified"};
        for(int i=0;i<sorts.length;i++)
            if(sorts[i].equalsIgnoreCase(current))return sorts[(i+1)%sorts.length];
        return sorts[0];
    }

    private static String onOff(boolean value){return value?"On":"Off";}

    @Override protected void actionPerformed(GuiButton button) {
        if(button==null)return;
        WaypointManager manager=WaypointManager.getInstance();
        try {
            if(button.id==1) { mc.displayGuiScreen(new CreateWaypointGui(this)); return; }
            if(button.id==2) {
                StaffWaypoint point=findCurrent(selectedId);
                if(point==null){status="Select a waypoint first.";return;}
                if(point.getId().equals(manager.getSelectedId())) {
                    manager.select("");
                    status="Navigation stopped.";
                } else {
                    manager.select(point.getId());
                    status="Navigation target: "+point.getLabel();
                }
            } else if(button.id==3) {
                if(manager.remove(selectedId)){selectedId="";status="Waypoint deleted.";}
                else status="Select a waypoint first.";
            } else if(button.id==4) {
                File file=new ShareWaypointsWithFriends().exportForSharing(manager.getForCurrentServer());
                status="Exported to "+file.getName();
            } else if(button.id==5) {
                List<StaffWaypoint> points=new WaypointImportExport().read(manager.getExportFile());
                int added=manager.addAllImported(points);
                status="Imported "+added+" new waypoint(s); skipped "+(points.size()-added)+" duplicate(s).";
            } else if(button.id==6) {
                mc.displayGuiScreen(new WaypointVisualSettingsGui(this));return;
            } else if(button.id==7) {
                mc.displayGuiScreen(parent);return;
            } else if(button.id==8) {
                WaypointSettings.setTemporarilyHidden(!WaypointSettings.isTemporarilyHidden());
            } else if(button.id==10) {
                WaypointSettings.setString("sortMode",nextSort(WaypointSettings.getString("sortMode","distance")));
                scroll=0;
            } else if(button.id==11) {
                saveEditedWaypoint();
            } else if(button.id==12) {
                StaffWaypoint point=findCurrent(selectedId);
                if(point!=null){point.setFavorite(!point.isFavorite());manager.saveChanges();}
            } else if(button.id==13) {
                StaffWaypoint point=findCurrent(selectedId);
                if(point!=null){point.setChromaEnabled(!point.isChromaEnabled());manager.saveChanges();}
            }
        } catch(Exception e){status="Waypoint action failed: "+e.getMessage();}
        initGui();
    }

    private void saveEditedWaypoint() {
        StaffWaypoint point=findCurrent(selectedId);
        if(point==null||nameField==null)return;
        String label=nameField.getText().trim();
        if(label.isEmpty()){status="Waypoint name cannot be empty.";return;}
        point.setLabel(label);
        point.setColor(selectedColor);
        WaypointManager.getInstance().saveChanges();
        status="Waypoint updated.";
    }

    @Override protected void keyTyped(char typedChar,int keyCode)throws IOException {
        if(keyCode==1){mc.displayGuiScreen(parent);return;}
        if(nameField!=null&&nameField.isFocused())nameField.textboxKeyTyped(typedChar,keyCode);
        else super.keyTyped(typedChar,keyCode);
    }

    @Override protected void mouseClicked(int mouseX,int mouseY,int mouseButton)throws IOException {
        super.mouseClicked(mouseX,mouseY,mouseButton);
        if(nameField!=null)nameField.mouseClicked(mouseX,mouseY,mouseButton);

        int editorX=left+listWidth+25;
        int paletteY=top+157;
        for(int i=0;i<COLORS.length;i++) {
            int x=editorX+14+(i%5)*25;
            int y=paletteY+(i/5)*25;
            if(mouseX>=x&&mouseX<x+17&&mouseY>=y&&mouseY<y+17) {
                selectedColor=COLORS[i];
                status="Color selected. Click Save Changes to apply it.";
                return;
            }
        }

        List<StaffWaypoint> points=WaypointManager.getInstance().getSortedCurrentWorld();
        int row=(mouseY-listTop)/17;
        int visible=Math.max(0,(listBottom-listTop)/17);
        if(mouseX>=left+12&&mouseX<=left+listWidth+5
                &&mouseY>=listTop&&mouseY<listBottom&&row>=0&&row<visible&&row+scroll<points.size()) {
            StaffWaypoint point=points.get(row+scroll);
            selectedId=point.getId();
            selectedColor=point.getColor();
            status="Selected "+point.getLabel()+".";
            initGui();
        }
    }

    @Override public void handleMouseInput()throws IOException {
        super.handleMouseInput();
        int wheel=Mouse.getDWheel();
        if(wheel!=0) {
            int visible=Math.max(0,(listBottom-listTop)/17);
            int max=Math.max(0,WaypointManager.getInstance().getSortedCurrentWorld().size()-visible);
            scroll=Math.max(0,Math.min(max,scroll+(wheel>0?-1:1)));
        }
    }

    @Override public void drawScreen(int mouseX,int mouseY,float partialTicks) {
        drawRect(0,0,width,height,0x18000000);
        drawRect(left,top,left+panelWidth,top+panelHeight,0xD90A0C11);
        drawRect(left,top,left+panelWidth,top+2,GOLD);
        drawRect(left+1,top+1,left+2,top+panelHeight-1,0x664B515D);
        drawString(fontRendererObj,"ALPHA  /  WAYPOINTS",left+14,top+12,0xFFF0F1F4);
        drawString(fontRendererObj,"Current server/world waypoints",left+14,top+30,0xFFB8BDC8);

        int listRight=left+listWidth+10;
        drawRect(left+12,listTop-5,listRight,listBottom,0x55212630);
        drawString(fontRendererObj,"WAYPOINTS",left+17,listTop-17,0xFFE6E8ED);
        List<StaffWaypoint> points=WaypointManager.getInstance().getSortedCurrentWorld();
        int visible=Math.max(0,(listBottom-listTop)/17);
        for(int i=scroll;i<points.size()&&i<scroll+visible;i++) {
            StaffWaypoint point=points.get(i);
            int y=listTop+(i-scroll)*17;
            if(point.getId().equals(selectedId))
                drawRect(left+13,y-1,listRight-1,y+15,0xAA363B45);
            drawRect(left+18,y+4,left+24,y+10,WorldWaypointIcons.getDisplayColor(point));
            String line=(point.isFavorite()?"★ ":"")+point.getLabel();
            int maxText=listWidth-47;
            if(fontRendererObj.getStringWidth(line)>maxText)
                line=fontRendererObj.trimStringToWidth(line,Math.max(8,maxText-8))+"…";
            fontRendererObj.drawStringWithShadow(line,left+29,y+3,0xFFE6E8ED);
        }

        int editorX=left+listWidth+25;
        int editorWidth=panelWidth-listWidth-39;
        drawRect(editorX,top+78,left+panelWidth-13,listBottom,0x44212630);
        drawString(fontRendererObj,"EDIT WAYPOINT",editorX+12,top+86,0xFFF0F1F4);
        StaffWaypoint selected=findCurrent(selectedId);
        if(selected==null) {
            drawString(fontRendererObj,"Select a waypoint on the left.",editorX+12,top+112,0xFFB8BDC8);
        } else {
            drawString(fontRendererObj,"Name",editorX+12,top+94,0xFFB8BDC8);
            nameField.drawTextBox();
            drawString(fontRendererObj,"Color",editorX+12,top+137,0xFFB8BDC8);
            for(int i=0;i<COLORS.length;i++) {
                int x=editorX+14+(i%5)*25;
                int y=top+157+(i/5)*25;
                if(COLORS[i]==selectedColor)drawRect(x-2,y-2,x+19,y+19,0xFFFFFFFF);
                drawRect(x,y,x+17,y+17,COLORS[i]);
            }
            drawString(fontRendererObj,"Favorite and Chroma are saved immediately.",editorX+12,top+211,0xFF9CA3AF);
        }
        drawString(fontRendererObj,status,left+14,top+panelHeight-45,0xFFB8BDC8);
        super.drawScreen(mouseX,mouseY,partialTicks);
    }

    @Override public boolean doesGuiPauseGame(){return false;}
}
