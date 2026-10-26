package com.alpharip.staffmod.staffops.ideas;
/** Builds a Discord share payload/link. Uploading a file to Discord requires an explicit external integration. */
public class DiscordShareButton {
 private boolean enabled=true;private String caption="";private String webhookUrl="";
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}
 public String getCaption(){return caption;}public void setCaption(String s){caption=s==null?"":s.trim();}
 public void setWebhookUrl(String url){if(url!=null&&!url.trim().isEmpty()&&!url.trim().startsWith("https://"))throw new IllegalArgumentException("HTTPS URL required");webhookUrl=url==null?"":url.trim();}
 public String getWebhookUrl(){return webhookUrl;}
 public String buildMessage(String screenshotName){String name=screenshotName==null?"screenshot":screenshotName.trim();return (caption.isEmpty()?"":caption+"\n")+name;}
}