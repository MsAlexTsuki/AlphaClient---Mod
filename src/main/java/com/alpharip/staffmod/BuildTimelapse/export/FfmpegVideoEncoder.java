package com.alpharip.staffmod.BuildTimelapse.export;
import java.io.*;import java.util.*;
/** External FFmpeg adapter; FFmpeg must be installed separately. */
public final class FfmpegVideoEncoder implements VideoEncoder {
 private final String exe;public FfmpegVideoEncoder(String e){exe=e==null||e.trim().isEmpty()?"ffmpeg":e.trim();}
 public void encode(ExportJob j)throws Exception{
  if(j==null)throw new IllegalArgumentException("job");if(!j.getFrames().isDirectory())throw new FileNotFoundException(j.getFrames().toString());ExportSettings s=j.getSettings();if(s.getFormat()==ExportSettings.Format.MP4&&((s.getWidth()&1)!=0||(s.getHeight()&1)!=0))throw new IllegalArgumentException("MP4 dimensions must be even for yuv420p output");
  File parent=j.getOutput().getAbsoluteFile().getParentFile();if(parent!=null&&!parent.exists()&&!parent.mkdirs())throw new IOException("Cannot create export directory: "+parent);
  List<String> c=new ArrayList<String>();Collections.addAll(c,exe,"-y","-framerate",Integer.toString(s.getFps()),"-i",new File(j.getFrames(),"frame-%08d.png").getAbsolutePath());
  if(s.getFormat()==ExportSettings.Format.MP4)Collections.addAll(c,"-vf","scale="+s.getWidth()+":"+s.getHeight()+":force_original_aspect_ratio=decrease,pad="+s.getWidth()+":"+s.getHeight()+":(ow-iw)/2:(oh-ih)/2","-an","-c:v","libx264","-pix_fmt","yuv420p",j.getOutput().getAbsolutePath());
  else if(s.getFormat()==ExportSettings.Format.GIF)Collections.addAll(c,"-vf","fps="+s.getFps()+",scale="+s.getWidth()+":"+s.getHeight()+":flags=lanczos",j.getOutput().getAbsolutePath());
  else throw new UnsupportedOperationException("PNG sequence already exists");
  Process p=new ProcessBuilder(c).redirectErrorStream(true).start();try(BufferedReader r=new BufferedReader(new InputStreamReader(p.getInputStream(),"UTF-8"))){while(r.readLine()!=null){}}if(p.waitFor()!=0)throw new IOException("FFmpeg failed with exit code "+p.exitValue());
 }
}