package com.alpharip.staffmod.AlphaCrashReporter;
import java.io.File;
import net.minecraft.client.gui.GuiScreen;
/** Standalone entry point; no automatic client registration. */
public final class AlphaCrashReporter {
 public static final String FEATURE_NAME="Alpha Crash Reporter";
 private final CrashReporterConfig config;private final CrashReportManager manager;
 private final CrashAnalyzer analyzer=new CrashAnalyzer(new CrashFixCatalog());
 private final CrashDetector detector;private final CrashExportService exporter;
 private AlphaCrashReporter(File dir,CrashReporterConfig cfg){
  config=cfg==null?new CrashReporterConfig():cfg;
  manager=new CrashReportManager(new FileCrashReportRepository(dir,new CrashReportCodec()),config);
  detector=new CrashDetector(manager,config);exporter=new CrashExportService(config.getExportDirectory());
 }
 public static AlphaCrashReporter open(File dir,CrashReporterConfig cfg){
  if(dir==null)throw new IllegalArgumentException("Report directory is required.");
  return new AlphaCrashReporter(dir,cfg);
 }
 public void setVersions(String client,String minecraft,String forge){detector.setVersions(client,minecraft,forge);}
 public CrashReport capture(Throwable t,CrashSource source,String module){return detector.capture(t,source,module,CrashEnvironment.capture());}
 public CrashReporterConfig getConfig(){return config;}public CrashReportManager getReportManager(){return manager;}
 public CrashAnalyzer getAnalyzer(){return analyzer;}public CrashDetector getDetector(){return detector;}
 public CrashExportService getExportService(){return exporter;}
 public void openScreen(GuiScreen parent){net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(new CrashReporterScreen(parent,manager,analyzer,exporter));}
}
