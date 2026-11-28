package com.alpharip.staffmod.RiskMitigationBoard;
import net.minecraft.client.gui.GuiButton;import net.minecraft.client.gui.GuiScreen;import net.minecraft.client.gui.GuiTextField;
import java.io.IOException;import java.util.List;
/** Alpha-styled local risk register screen. Controller must be created with real authorization checks. */
public final class RiskMitigationBoardGui extends GuiScreen {
    private final GuiScreen parent;private final RiskBoardController controller;
    private GuiTextField title,description,owner;private int left,top,right,bottom,scroll;private String message="Local prototype";
    private static final int ADD=1,BACK=2,ROW=100;
    public RiskMitigationBoardGui(GuiScreen parent,RiskBoardController controller){if(controller==null)throw new IllegalArgumentException("Controller is required");this.parent=parent;this.controller=controller;}
    @Override public void initGui(){
        controller.load();buttonList.clear();int w=Math.min(560,width-20),h=Math.min(390,height-20);
        left=(width-w)/2;right=left+w;top=(height-h)/2;bottom=top+h;
        title=new GuiTextField(10,fontRendererObj,left+14,top+54,w-28,18);
        description=new GuiTextField(11,fontRendererObj,left+14,top+92,w-28,18);
        owner=new GuiTextField(12,fontRendererObj,left+14,top+130,Math.max(80,w-150),18);
        title.setMaxStringLength(80);description.setMaxStringLength(240);owner.setMaxStringLength(32);
        buttonList.add(new GuiButton(ADD,right-116,top+128,102,22,"ADD RISK"));
        buttonList.add(new GuiButton(BACK,left+14,bottom-28,70,20,"BACK"));rows();
    }
    private void rows(){
        for(int i=buttonList.size()-1;i>=0;i--)if(buttonList.get(i).id>=ROW)buttonList.remove(i);
        List<RiskRecord> all=controller.getBoard().getAll();int visible=Math.max(1,(bottom-top-205)/31);
        scroll=Math.max(0,Math.min(scroll,all.size()-visible));
        for(int i=scroll;i<Math.min(all.size(),scroll+visible);i++){int y=top+177+(i-scroll)*31;
            buttonList.add(new GuiButton(ROW+(i-scroll)*2,right-126,y,58,18,"STATUS"));
            buttonList.add(new GuiButton(ROW+(i-scroll)*2+1,right-64,y,54,18,"REMOVE"));}
    }
    @Override public void drawScreen(int mx,int my,float partial){
        drawDefaultBackground();drawRect(left,top,right,bottom,0xF00D1017);drawRect(left,top,right,top+2,0xFFFFD866);
        drawCenteredString(fontRendererObj,"RISK MITIGATION BOARD",width/2,top+13,0xFFF3F4F6);
        drawCenteredString(fontRendererObj,"STAFF OPERATIONS · LOCAL PROTOTYPE",width/2,top+29,0xFFFFD866);
        drawString(fontRendererObj,"TITLE",left+14,top+43,0xFFB8BDC8);title.drawTextBox();
        drawString(fontRendererObj,"DESCRIPTION",left+14,top+81,0xFFB8BDC8);description.drawTextBox();
        drawString(fontRendererObj,"OWNER (OPTIONAL)",left+14,top+119,0xFFB8BDC8);owner.drawTextBox();
        RiskBoardSummary s=new RiskBoardService(controller.getBoard()).summarize(System.currentTimeMillis());
        drawString(fontRendererObj,"TOTAL "+s.getTotal()+"   OPEN "+s.getOpen()+"   CRITICAL "+s.getCritical()+"   OVERDUE "+s.getOverdue(),left+14,top+158,0xFFB8BDC8);
        List<RiskRecord> all=controller.getBoard().getAll();int visible=Math.max(1,(bottom-top-205)/31);
        if(all.isEmpty())drawCenteredString(fontRendererObj,"No risks recorded",width/2,top+205,0xFFB8BDC8);
        for(int i=scroll;i<Math.min(all.size(),scroll+visible);i++){RiskRecord r=all.get(i);int y=top+177+(i-scroll)*31;
            drawRect(left+12,y,right-12,y+27,0xAA191D25);drawString(fontRendererObj,fontRendererObj.trimStringToWidth(r.getTitle(),right-left-230),left+20,y+4,0xFFF3F4F6);
            drawString(fontRendererObj,r.getSeverity().name()+" · "+r.getStatus().name(),left+20,y+16,severityColor(r.getSeverity()));}
        drawString(fontRendererObj,message,left+100,bottom-24,0xFFB8BDC8);super.drawScreen(mx,my,partial);
    }
    private int severityColor(RiskSeverity s){switch(s){case CRITICAL:return 0xFFFF7777;case HIGH:return 0xFFFFB45C;case MEDIUM:return 0xFFFFD866;default:return 0xFF9CE2AC;}}
    @Override protected void actionPerformed(GuiButton b)throws IOException{
        if(b.id==BACK){mc.displayGuiScreen(parent);return;}
        if(b.id==ADD){try{controller.getService().create(title.getText(),description.getText(),RiskCategory.OTHER,RiskSeverity.MEDIUM,owner.getText(),0L,System.currentTimeMillis());controller.save();title.setText("");description.setText("");owner.setText("");message="Risk added";rows();}catch(RuntimeException e){message=e.getMessage();}return;}
        if(b.id>=ROW){int relative=b.id-ROW,index=scroll+relative/2;List<RiskRecord> all=controller.getBoard().getAll();if(index<0||index>=all.size())return;RiskRecord r=all.get(index);
            if(relative%2==0){RiskStatus next=r.getStatus()==RiskStatus.OPEN?RiskStatus.IN_PROGRESS:r.getStatus()==RiskStatus.IN_PROGRESS?RiskStatus.MITIGATED:r.getStatus()==RiskStatus.MITIGATED?RiskStatus.CLOSED:RiskStatus.OPEN;controller.getService().setStatus(r.getId(),next,System.currentTimeMillis());message="Status: "+next.name();}
            else{controller.getService().remove(r.getId());message="Risk removed";}controller.save();rows();}
    }
    @Override protected void mouseClicked(int x,int y,int button)throws IOException{title.mouseClicked(x,y,button);description.mouseClicked(x,y,button);owner.mouseClicked(x,y,button);super.mouseClicked(x,y,button);}
    @Override protected void keyTyped(char c,int key)throws IOException{if(key==1){mc.displayGuiScreen(parent);return;}title.textboxKeyTyped(c,key);description.textboxKeyTyped(c,key);owner.textboxKeyTyped(c,key);super.keyTyped(c,key);}
    @Override public boolean doesGuiPauseGame(){return false;}
}
