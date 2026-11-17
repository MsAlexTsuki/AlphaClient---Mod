package com.alpharip.staffmod.AlphaCrashReporter;
import java.io.IOException;import java.io.File;import java.util.*;
/** UI-facing operations separated from Minecraft rendering. */
public final class CrashReporterController {
 private final CrashReportManager manager;private final CrashAnalyzer analyzer;private final CrashExportService exporter;
 private List<CrashReport> reports=Collections.emptyList();private int selected=-1;
 public CrashReporterController(CrashReportManager m,CrashAnalyzer a,CrashExportService e){manager=m;analyzer=a;exporter=e;}
 public void refresh()throws IOException{reports=manager.getAll();if(reports.isEmpty())selected=-1;else selected=Math.max(0,Math.min(selected,reports.size()-1));}
 public List<CrashReport> getReports(){return Collections.unmodifiableList(new ArrayList<CrashReport>(reports));}
 public int getSelectedIndex(){return selected;}public CrashReport getSelected(){return selected<0||selected>=reports.size()?null:reports.get(selected);}
 public void select(int i){selected=reports.isEmpty()?-1:Math.max(0,Math.min(i,reports.size()-1));}
 public CrashAnalysisResult analyzeSelected(){return getSelected()==null?null:analyzer.analyze(getSelected());}
 public void deleteSelected()throws IOException{CrashReport r=getSelected();if(r!=null){manager.delete(r.getId());refresh();}}
 public File exportSelected()throws IOException{return getSelected()==null?null:exporter.export(getSelected());}
}
