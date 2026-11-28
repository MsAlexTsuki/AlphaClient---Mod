package com.alpharip.staffmod.staffops.ideas;
import java.text.SimpleDateFormat;import java.util.Date;
/** Creates safe server-prefixed screenshot filenames. */
public class ServerNameScreenshotFilename {public String create(String server,long timestamp){String safe=(server==null||server.trim().isEmpty()?"singleplayer":server.trim()).replaceAll("[^A-Za-z0-9._-]","_");return safe+"_"+new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date(timestamp))+".png";}}