package com.alpharip.staffmod.AppealReviewWorkspace;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import java.io.IOException;
import java.util.List;

/** Initial local appeal queue UI; the caller must supply real StaffMods authorization. */
public final class AppealReviewWorkspaceGui extends GuiScreen {
 private static final int ADD=1,BACK=2,STATUS=1000,REMOVE=2000;
 private final GuiScreen parent;private final AppealReviewController controller;
 private GuiTextField player,punishment,reason;private int left,top,right,bottom,scroll;private String message="LOCAL PROTOTYPE";
 public AppealReviewWorkspaceGui(GuiScreen parent,AppealReviewController c){if(c==null)throw new IllegalArgumentException("Controller required");this.parent=parent;controller=c;}
 @Override public void initGui(){
  controller.load();buttonList.clear();int w=Math.min(560,width-20),h=Math.min(390,height-20);left=(width-w)/2;right=left+w;top=(height-h)/2;bottom=top+h;
  player=new GuiTextField(10,fontRendererObj,left+14,top+54,w-28,18);punishment=new GuiTextField(11,fontRendererObj,left+14,top+92,w-28,18);reason=new GuiTextField(12,fontRendererObj,left+14,top+130,w-140,18);
  player.setMaxStringLength(32);punishment.setMaxStringLength(100);reason.setMaxStringLength(240);
  if(controller.getAccess().canReviewAppeals())buttonList.add(new GuiButton(ADD,right-116,top+128,102,22,"SUBMIT"));
  buttonList.add(new GuiButton(BACK,left+14,bottom-28,70,20,"BACK"));rows();
 }
 private void rows(){
  for(int i=buttonList.size()-1;i>=0;i--)if(buttonList.get(i).id>=STATUS)buttonList.remove(i);
  List<AppealCase> all=controller.getBoard().getAll();int visible=Math.max(1,(bottom-top-205)/31);scroll=Math.max(0,Math.min(scroll,all.size()-visible));
  for(int i=scroll;i<Math.min(all.size(),scroll+visible);i++){int row=i-scroll,y=top+177+row*31;
   if(controller.getAccess().canReviewAppeals()){buttonList.add(new GuiButton(STATUS+row,right-128,y,58,18,"STATUS"));buttonList.add(new GuiButton(REMOVE+row,right-66,y,54,18,"REMOVE"));}
  }
 }
 @Override public void drawScreen(int mx,int my,float partial){
  drawDefaultBackground();drawRect(left,top,right,bottom,0xF00D1017);drawRect(left,top,right,top+2,0xFFFFD866);
  drawCenteredString(fontRendererObj,"APPEAL REVIEW WORKSPACE",width/2,top+13,0xFFF3F4F6);
  drawCenteredString(fontRendererObj,"STAFF OPERATIONS · LOCAL PROTOTYPE",width/2,top+29,0xFFFFD866);
  drawString(fontRendererObj,"PLAYER",left+14,top+43,0xFFB8BDC8);player.drawTextBox();
  drawString(fontRendererObj,"PUNISHMENT",left+14,top+81,0xFFB8BDC8);punishment.drawTextBox();
  drawString(fontRendererObj,"APPEAL REASON",left+14,top+119,0xFFB8BDC8);reason.drawTextBox();
  AppealReviewSummary s=controller.getService().summarize();
  drawString(fontRendererObj,"TOTAL "+s.getTotal()+"  SUBMITTED "+s.getSubmitted()+"  REVIEW "+s.getInReview()+"  ESCALATED "+s.getEscalated()+"  DECIDED "+s.getDecided(),left+14,top+158,0xFFB8BDC8);
  List<AppealCase> all=controller.getBoard().getAll();int visible=Math.max(1,(bottom-top-205)/31);
  if(all.isEmpty())drawCenteredString(fontRendererObj,"No appeals recorded",width/2,top+205,0xFFB8BDC8);
  for(int i=scroll;i<Math.min(all.size(),scroll+visible);i++){AppealCase a=all.get(i);int y=top+177+(i-scroll)*31;drawRect(left+12,y,right-12,y+27,0xAA191D25);
   String line=a.getPlayerName()+"  ·  "+a.getStatus().name()+"  ·  "+(a.getAssignedReviewer().isEmpty()?"UNASSIGNED":a.getAssignedReviewer());
   drawString(fontRendererObj,fontRendererObj.trimStringToWidth(line,right-left-170),left+20,y+9,0xFFF3F4F6);}
  drawString(fontRendererObj,message,left+100,bottom-24,0xFFB8BDC8);super.drawScreen(mx,my,partial);
 }
 @Override protected void actionPerformed(GuiButton b)throws IOException{
  if(b.id==BACK){mc.displayGuiScreen(parent);return;}
  if(b.id==ADD){try{controller.getService().submit(player.getText(),punishment.getText(),reason.getText(),System.currentTimeMillis());controller.save();player.setText("");punishment.setText("");reason.setText("");message="Appeal submitted";rows();}catch(RuntimeException e){message=e.getMessage();}return;}
  if(b.id>=STATUS&&b.id<REMOVE){if(!controller.getAccess().canReviewAppeals())return;int i=scroll+b.id-STATUS;List<AppealCase> all=controller.getBoard().getAll();if(i<all.size()){AppealCase a=all.get(i);AppealStatus[] states=AppealStatus.values();controller.getService().setStatus(a.getId(),states[(a.getStatus().ordinal()+1)%states.length],System.currentTimeMillis());controller.save();message="Status updated";}rows();return;}
  if(b.id>=REMOVE){if(!controller.getAccess().canReviewAppeals())return;int i=scroll+b.id-REMOVE;List<AppealCase> all=controller.getBoard().getAll();if(i<all.size())controller.getService().remove(all.get(i).getId());controller.save();message="Appeal removed";rows();}
 }
 @Override protected void mouseClicked(int x,int y,int button)throws IOException{player.mouseClicked(x,y,button);punishment.mouseClicked(x,y,button);reason.mouseClicked(x,y,button);super.mouseClicked(x,y,button);}
 @Override protected void keyTyped(char c,int key)throws IOException{if(key==1){mc.displayGuiScreen(parent);return;}player.textboxKeyTyped(c,key);punishment.textboxKeyTyped(c,key);reason.textboxKeyTyped(c,key);super.keyTyped(c,key);}
 @Override public boolean doesGuiPauseGame(){return false;}
}