package com.alpharip.staffmod.AlphaCrashReporter;
import net.minecraft.client.Minecraft;import net.minecraft.client.gui.GuiButton;import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;import java.text.SimpleDateFormat;import java.util.*;
/** Standalone local crash-history screen. Not wired into AlphaClient navigation. */
public final class CrashReporterScreen extends GuiScreen {
 private final GuiScreen parent;private final CrashReporterController controller;private String status="Loading crash history...";private int page=0;
 private static final int PAGE_SIZE=7;
 public CrashReporterScreen(GuiScreen p,CrashReportManager m,CrashAnalyzer a,CrashExportService e){parent=p;controller=new CrashReporterController(m,a,e);}
 @Override public void initGui(){
  buttonList.clear();int y=height-34,w=72,g=5,total=w*5+g*4,x=(width-total)/2;
  buttonList.add(new GuiButton(1,x,y,w,20,"Previous"));buttonList.add(new GuiButton(2,x+w+g,y,w,20,"Next"));
  buttonList.add(new GuiButton(3,x+2*(w+g),y,w,20,"Refresh"));buttonList.add(new GuiButton(4,x+3*(w+g),y,w,20,"Export"));
  buttonList.add(new GuiButton(5,x+4*(w+g),y,w,20,"Delete"));buttonList.add(new GuiButton(6,width-72,8,64,18,"Done"));refresh();
 }
 private void refresh(){try{controller.refresh();status=controller.getReports().size()+" report(s) stored locally.";}catch(IOException e){status="History read failed: "+e.getMessage();}}
 @Override protected void actionPerformed(GuiButton b){
  if(b==null)return;try{
   if(b.id==1)page=Math.max(0,page-1);
   else if(b.id==2)page=Math.min(Math.max(0,(controller.getReports().size()-1)/PAGE_SIZE),page+1);
   else if(b.id==3){refresh();page=0;}
   else if(b.id==4){java.io.File f=controller.exportSelected();status=f==null?"Select a report first.":"Exported "+f.getName();}
   else if(b.id==5){controller.deleteSelected();refresh();page=0;}
   else if(b.id==6){Minecraft.getMinecraft().displayGuiScreen(parent);return;}
  }catch(IOException e){status="Operation failed: "+e.getMessage();}
 }
 @Override protected void mouseClicked(int mx,int my,int button)throws IOException{
  super.mouseClicked(mx,my,button);if(button!=0)return;
  for(int i=0;i<PAGE_SIZE;i++){int index=page*PAGE_SIZE+i,y=48+i*28;if(index<controller.getReports().size()&&my>=y&&my<y+24){controller.select(index);break;}}
 }
 @Override public void drawScreen(int mx,int my,float partial){
  drawDefaultBackground();drawRect(0,0,width,31,0xE50B0D12);drawRect(0,31,width,33,0xFFFFC83D);
  drawCenteredString(fontRendererObj,"ALPHA CRASH REPORTER",width/2,11,0xFFF2F3F5);drawString(fontRendererObj,status,16,38,0xFFBFC5D0);
  List<CrashReport> list=controller.getReports();
  for(int i=0;i<PAGE_SIZE;i++){int index=page*PAGE_SIZE+i;if(index>=list.size())break;CrashReport r=list.get(index);int y=48+i*28;
   drawRect(12,y,width-12,y+24,index==controller.getSelectedIndex()?0xCC3A3320:0x99303038);
   drawString(fontRendererObj,trim(r.getTitle(),Math.max(10,(width-190)/6)),20,y+4,index==controller.getSelectedIndex()?0xFFFFC83D:0xFFF0F0F0);
   String meta=r.getCategory()+" | "+new SimpleDateFormat("dd-MM HH:mm",Locale.ROOT).format(new Date(r.getTimestamp()));
   drawString(fontRendererObj,trim(meta,28),20,y+14,0xFFADB4C0);drawString(fontRendererObj,r.getSeverity().name(),width-94,y+8,0xFFE0A35A);
  }
  CrashReport selected=controller.getSelected();if(selected!=null){drawString(fontRendererObj,"Selected: "+trim(selected.getExceptionType(),45),16,height-55,0xFFE4E6EA);
   CrashAnalysisResult a=controller.analyzeSelected();if(a!=null)drawString(fontRendererObj,trim(a.getSummary(),Math.max(20,(width-32)/6)),16,height-45,0xFFBFC5D0);}
  super.drawScreen(mx,my,partial);
 }
 private static String trim(String s,int n){if(s==null)return "";return s.length()<=n?s:s.substring(0,Math.max(0,n-3))+"...";}
 @Override public boolean doesGuiPauseGame(){return false;}
}
