package com.alpharip.staffmod.BuildTimelapse.export;
import java.io.*;import java.nio.file.*;import java.util.*;
/** External FFmpeg adapter; FFmpeg must be installed separately. */
public final class FfmpegVideoEncoder implements VideoEncoder {
 private final String executable;
 public FfmpegVideoEncoder(String value){executable=value==null||value.trim().isEmpty()?"ffmpeg":value.trim();}
 public void encode(ExportJob job)throws Exception{
  if(job==null)throw new IllegalArgumentException("job");
  File frames=job.getFrames();if(!frames.isDirectory())throw new FileNotFoundException(frames.toString());
  ExportSettings settings=job.getSettings();File output=job.getOutput();
  if(settings.getFormat()==ExportSettings.Format.IMAGE_SEQUENCE){copySequence(frames,output);return;}
  if(settings.getFormat()==ExportSettings.Format.MP4&&((settings.getWidth()&1)!=0||(settings.getHeight()&1)!=0))throw new IllegalArgumentException("MP4 dimensions must be even for yuv420p output");
  File parent=output.getAbsoluteFile().getParentFile();if(parent!=null&&!parent.exists()&&!parent.mkdirs())throw new IOException("Cannot create export directory: "+parent);
  List<String> command=new ArrayList<String>();Collections.addAll(command,executable,"-y","-framerate",Integer.toString(settings.getFps()),"-i",new File(frames,"frame-%08d.png").getAbsolutePath());
  if(settings.getFormat()==ExportSettings.Format.MP4)Collections.addAll(command,"-vf","scale="+settings.getWidth()+":"+settings.getHeight()+":force_original_aspect_ratio=decrease,pad="+settings.getWidth()+":"+settings.getHeight()+":(ow-iw)/2:(oh-ih)/2","-an","-c:v","libx264","-pix_fmt","yuv420p",output.getAbsolutePath());
  else if(settings.getFormat()==ExportSettings.Format.GIF)Collections.addAll(command,"-vf","fps="+settings.getFps()+",scale="+settings.getWidth()+":"+settings.getHeight()+":flags=lanczos",output.getAbsolutePath());
  else throw new UnsupportedOperationException("Unsupported export format");
  File logFile=File.createTempFile("alpha-timelapse-ffmpeg-",".log");
  Process process=null;
  try{
   process=new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(logFile).start();
   int exit;
   try{exit=process.waitFor();}catch(InterruptedException interrupted){process.destroy();if(process.isAlive())process.destroyForcibly();Thread.currentThread().interrupt();throw new InterruptedException("Export cancelled");}
   if(exit!=0)throw new IOException("FFmpeg failed with exit code "+exit+": "+readTail(logFile,12000));
   if(!output.isFile()||output.length()==0)throw new IOException("FFmpeg did not create a valid output file");
  }finally{if(process!=null&&process.isAlive()){process.destroy();if(process.isAlive())process.destroyForcibly();}if(!logFile.delete())logFile.deleteOnExit();}
 }
 private static String readTail(File f,int max)throws IOException{RandomAccessFile r=new RandomAccessFile(f,"r");try{long start=Math.max(0,r.length()-max);r.seek(start);byte[] b=new byte[(int)(r.length()-start)];r.readFully(b);return new String(b,"UTF-8");}finally{r.close();}}
 private void copySequence(File source,File destination)throws IOException{if(!destination.exists()&&!destination.mkdirs())throw new IOException("Cannot create image sequence output directory");if(!destination.isDirectory())throw new IOException("Image sequence output is not a directory");File[] png=source.listFiles(new FilenameFilter(){public boolean accept(File d,String n){return n.matches("frame-[0-9]{8}\\.png");}});if(png==null||png.length==0)throw new IOException("No captured PNG frames found");Arrays.sort(png,new Comparator<File>(){public int compare(File a,File b){return a.getName().compareTo(b.getName());}});for(File f:png){if(Thread.currentThread().isInterrupted())throw new InterruptedException("Export cancelled");File target=new File(destination,f.getName());if(!f.getCanonicalFile().equals(target.getCanonicalFile()))Files.copy(f.toPath(),target.toPath(),StandardCopyOption.REPLACE_EXISTING);}}
}
