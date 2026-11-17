package com.alpharip.staffmod.AlphaCrashReporter;
import net.minecraft.client.Minecraft;import net.minecraft.client.gui.GuiButton;import net.minecraft.client.gui.GuiScreen;
import java.util.*;
/** Read-only details view with paged stack-trace output and heuristic suggestions. */
public final class CrashReportDetailsScreen extends GuiScreen {
 private final GuiScreen parent;private final CrashReport report;private final CrashAnalysisResult analysis;private int page=0;
 private List<String> lines=Collections.emptyList();private static final int PAGE_SIZE=16;
 public CrashReportDetailsScreen(GuiScreen parent,CrashReport report,CrashAnalyzer analyzer){
  this.parent=parent;this.report=report;this.analysis=analyzer.analyze(report);this.lines=buildLines(report,analysis);
 }
 @Override public void initGui(){
  buttonList.clear();int y=height-28;int w=78;
  buttonList.add(new GuiButton(1,16,y,w,20,"Previous"));
  buttonList.add(new GuiButton(2,100,y,w,20,"Next"));
  buttonList.add(new GuiButton(3,width-94,y,w,20,"Back"));
 }
 @Override protected void actionPerformed(GuiButton b){
  if(b==null)return;
  if(b.id==1)page=Math.max(0,page-1);
  else if(b.id==2)page=Math.min(Math.max(0,(lines.size()-1)/PAGE_SIZE),page+1);
  else if(b.id==3)Minecraft.getMinecraft().displayGuiScreen(parent);
 }
 @Override public void drawScreen(int mx,int my,float partial){
  drawDefaultBackground();drawRect(0,0,width,31,0xE50B0D12);drawRect(0,31,width,33,0xFFFFC83D);
  drawCenteredString(fontRendererObj,"CRASH REPORT DETAILS",width/2,11,0xFFF2F3F5);
  drawString(fontRendererObj,trim(report.getTitle(),Math.max(20,(width-32)/6)),16,40,0xFFFFC83D);
  drawString(fontRendererObj,"Category: "+report.getCategory()+"  Severity: "+report.getSeverity()+"  Source: "+report.getSource(),16,53,0xFFBFC5D0);
  int start=72;
  for(int i=0;i<PAGE_SIZE;i++){int index=page*PAGE_SIZE+i;if(index>=lines.size())break;drawString(fontRendererObj,trim(lines.get(index),Math.max(20,(width-32)/6)),16,start+i*11,0xFFE4E6EA);}
  drawString(fontRendererObj,"Page "+(page+1)+" / "+Math.max(1,(lines.size()+PAGE_SIZE-1)/PAGE_SIZE),width-100,height-42,0xFFBFC5D0);
  super.drawScreen(mx,my,partial);
 }
 private static List<String> buildLines(CrashReport r,CrashAnalysisResult a){
  List<String> out=new ArrayList<String>();out.add("Exception: "+r.getExceptionType());out.add("Message: "+r.getMessage());
  out.add("Alpha Client: "+r.getClientVersion()+" | Minecraft: "+r.getMinecraftVersion()+" | Forge: "+r.getForgeVersion());
  out.add("Module: "+r.getModuleName());out.add("Fingerprint: "+r.getFingerprint());out.add("Analysis: "+a.getSummary());
  for(SuggestedFix fix:a.getSuggestedFixes()){out.add("Suggested: "+fix.getTitle()+" ("+fix.getConfidence()+"% heuristic confidence)");out.add(fix.getDescription());}
  out.add("Environment:");
  for(Map.Entry<String,String> e:r.getEnvironment().getValues().entrySet())out.add(e.getKey()+": "+e.getValue());
  out.add("Stack trace:");
  String stack=r.getStackTrace();if(stack.isEmpty())out.add("(Stack trace capture is disabled.)");
  else for(String line:stack.split("\\n")){if(line.length()<=110)out.add(line);else for(int i=0;i<line.length();i+=110)out.add(line.substring(i,Math.min(line.length(),i+110)));}
  return out;
 }
 private static String trim(String s,int n){if(s==null)return "";return s.length()<=n?s:s.substring(0,Math.max(0,n-3))+"...";}
 @Override public boolean doesGuiPauseGame(){return false;}
}
