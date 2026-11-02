package com.alpharip.staffmod.waypoints;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Client command for opening, creating, navigating, importing and exporting waypoints. */
public final class WaypointCommand extends CommandBase {
    private final WaypointNameAutocompleteInCommands autocomplete = new WaypointNameAutocompleteInCommands();
    private final WaypointImportExport codec = new WaypointImportExport();
    private final ShareWaypointsWithFriends sharing = new ShareWaypointsWithFriends();

    @Override public String getCommandName() { return "waypoint"; }
    @Override public String getCommandUsage(ICommandSender sender) { return "/waypoint [open|visuals|profile|add|list|select|favorite|icon|route|history]"; }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Minecraft mc=Minecraft.getMinecraft();
        if(mc==null) return;
        if(args==null||args.length==0||"open".equalsIgnoreCase(args[0])) {
            mc.displayGuiScreen(new WaypointsGui(mc.currentScreen)); return;
        }
        if("visuals".equalsIgnoreCase(args[0])||"options".equalsIgnoreCase(args[0])) {
            mc.displayGuiScreen(new WaypointVisualSettingsGui(mc.currentScreen)); return;
        }
        String action=args[0].toLowerCase(Locale.ROOT);
        WaypointManager manager=WaypointManager.getInstance();
        try {
            if("profile".equals(action)) {
                if(args.length<2){reply(sender,"Active waypoint profile: "+WaypointSettings.getString("activeProfile","default"));return;}
                manager.setActiveProfile(join(args,1));
                reply(sender,"Switched to waypoint profile "+WaypointSettings.getString("activeProfile","default")+".");
            } else if("favorite".equals(action)) {
                StaffWaypoint point=find(manager,args,1);
                if(point==null){reply(sender,"Waypoint not found in this profile.");return;}
                point.setFavorite(!point.isFavorite());manager.saveChanges();
                reply(sender,(point.isFavorite()?"Pinned ":"Unpinned ")+point.getLabel()+".");
            } else if("icon".equals(action)) {
                if(args.length<3){reply(sender,"Usage: /waypoint icon <waypoint name> <symbol up to 4 characters>");return;}
                StaffWaypoint point=args.length>2?manager.find(join(args,1,args.length-1)):null;
                String symbol=args[args.length-1];
                if(point==null){reply(sender,"Waypoint not found in this profile.");return;}
                if(symbol.trim().isEmpty()||symbol.length()>4){reply(sender,"Custom icons need 1 to 4 characters.");return;}
                point.setIconId(symbol);manager.saveChanges();
                reply(sender,"Icon updated for "+point.getLabel()+".");
            } else if("history".equals(action)) {
                List<WaypointTracker.CoordinateEntry> history=WaypointTracker.getInstance().getHistoryForCurrentWorld();
                if(args.length>=3&&"add".equalsIgnoreCase(args[1])) {
                    int index=Integer.parseInt(args[2])-1;
                    if(index<0||index>=history.size()){reply(sender,"History index is out of range.");return;}
                    WaypointTracker.CoordinateEntry entry=history.get(index);
                    String name=args.length>3?join(args,3):"History "+(index+1);
                    StaffWaypoint point=manager.add(name,entry.getX(),entry.getY(),entry.getZ());
                    reply(sender,"Waypoint created from history: "+point.getLabel()+" ("+coords(point)+").");
                    return;
                }
                if(history.isEmpty()){reply(sender,"No coordinate history for this world yet.");return;}
                reply(sender,"Recent coordinates (newest first):");
                for(int i=0;i<history.size()&&i<15;i++) {
                    WaypointTracker.CoordinateEntry entry=history.get(i);
                    long ageSeconds=Math.max(0L,(System.currentTimeMillis()-entry.getTimestamp())/1000L);
                    String age=ageSeconds<60L?ageSeconds+"s ago":(ageSeconds/60L)+"m ago";
                    reply(sender,(i+1)+". "+(int)Math.floor(entry.getX())+", "+(int)Math.floor(entry.getY())+", "+(int)Math.floor(entry.getZ())+" ("+age+")");
                }
                reply(sender,"Create one with /waypoint history add <number> [name].");
            } else if("add".equals(action)) {
                if(mc.thePlayer==null){reply(sender,"Join a world before creating a waypoint.");return;}
                String name=args.length>1?join(args,1):"Waypoint";
                StaffWaypoint p=manager.add(name,mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ);
                reply(sender,"Waypoint saved: "+p.getLabel()+" ("+coords(p)+").");
            } else if("addat".equals(action)) {
                if(mc.thePlayer==null){reply(sender,"Join a world before creating a waypoint.");return;}
                if(args.length<4){reply(sender,"Usage: /waypoint addat <x> <y> <z>");return;}
                String name=args.length>4?join(args,4):"Chat waypoint";
                StaffWaypoint p=manager.add(name,Double.parseDouble(args[1]),Double.parseDouble(args[2]),Double.parseDouble(args[3]));
                manager.select(p.getId()); reply(sender,"Waypoint saved: "+p.getLabel()+" ("+coords(p)+").");
            } else if("list".equals(action)) {
                List<StaffWaypoint> list=manager.getSortedCurrentWorld();
                if(list.isEmpty()){reply(sender,"No waypoints in this world yet.");return;}
                reply(sender,"Waypoints (favorites first; sort: "+WaypointSettings.getString("sortMode","distance")+"):");
                if (mc.thePlayer != null) {
                    for (StaffWaypoint p : list) {
                        double dx = p.getX() - mc.thePlayer.posX;
                        double dy = p.getY() - mc.thePlayer.posY;
                        double dz = p.getZ() - mc.thePlayer.posZ;
                        long distance = Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz));
                        reply(sender, (p.isFavorite()?"[F] ":"") + p.getLabel() + " — " + coords(p) + " (" + distance + "m)"
                                + (p.isVisited()?" [visited]":"")
                                + (p.getId().equals(manager.getSelectedId()) ? " [selected]" : ""));
                    }
                } else {
                    for (StaffWaypoint p : list) reply(sender, p.getLabel() + " — " + coords(p));
                }
            } else if("select".equals(action)||"goto".equals(action)||"navigate".equals(action)) {
                StaffWaypoint p=find(manager,args,1);
                if(p==null){reply(sender,"Waypoint not found on this server.");return;}
                manager.select(p.getId()); reply(sender,"Navigation target: "+p.getLabel()+". Follow the waypoint HUD and marker.");
            } else if("remove".equals(action)||"delete".equals(action)) {
                if(args.length<2){reply(sender,"Usage: /waypoint remove <name>");return;}
                if(manager.remove(join(args,1))) reply(sender,"Waypoint removed.");
                else reply(sender,"Waypoint not found.");
            } else if("route".equals(action)) {
                List<String> routeNames=parseQuotedArguments(args,1);
                if(routeNames.size()<2){reply(sender,"Usage: /waypoint route <start> <end> [stop ...]; quote names with spaces.");return;}
                List<StaffWaypoint> routePoints=new ArrayList<StaffWaypoint>();
                for(String routeName:routeNames) {
                    StaffWaypoint point=manager.find(routeName);
                    if(point==null){reply(sender,"Waypoint not found: "+routeName);return;}
                    routePoints.add(point);
                }
                StaffWaypoint first=routePoints.get(0),last=routePoints.get(routePoints.size()-1);
                for(StaffWaypoint point:routePoints)
                    if(!first.getServerKey().equals(point.getServerKey())||first.getDimensionId()!=point.getDimensionId()) {
                        reply(sender,"Every route point must be in the same server and dimension.");return;
                    }
                if(!manager.setRoute(routePoints)){reply(sender,"Could not create this route.");return;}
                double direct=manager.getRoute().getDistance(first,last);
                double via=manager.getRoute().getTotalDistance(routePoints);
                reply(sender,"Route set with "+routePoints.size()+" points. Direct: "+Math.round(direct)+"m; via stops: "+Math.round(via)+"m.");
            } else if("clearroute".equals(action)) {
                manager.clearRoute(); reply(sender,"Route cleared.");
            } else if("clear".equals(action)) {
                if(args.length<2||!"confirm".equalsIgnoreCase(args[1])) {
                    reply(sender,"This deletes waypoints in the active profile on this server. Confirm with /waypoint clear confirm."); return;
                }
                manager.clearCurrentServer(); reply(sender,"Waypoints for this profile and server were cleared.");
            } else if("export".equals(action)||"share".equals(action)) {
                File file=sharing.exportForSharing(manager.getForCurrentServer());
                reply(sender,"Local waypoint export saved to "+file.getAbsolutePath()+". Share this file with a friend.");
            } else if("import".equals(action)) {
                File file=manager.getExportFile();
                List<StaffWaypoint> imported=codec.read(file);
                int added = manager.addAllImported(imported);
                reply(sender,"Imported "+added+" new waypoint(s); skipped "+(imported.size()-added)+" duplicate(s) from "+file.getName()+".");
            } else if("category".equals(action)) {
                if(args.length<4){reply(sender,"Usage: /waypoint category <name> <category> <hex-color>");return;}
                StaffWaypoint point=manager.find(join(args,1,args.length-2));
                if(point==null){reply(sender,"Waypoint not found.");return;}
                String category=args[args.length-2];
                String color=args[args.length-1].replace("#","");
                if(color.length()!=6){reply(sender,"Use a six-digit RGB color, for example FF8800.");return;}
                int rgb=Integer.parseInt(color,16);
                point.setCategory(category); point.setColor(rgb); manager.saveChanges();
                reply(sender,"Updated "+point.getLabel()+" to category "+point.getCategory()+".");
            } else if("minimap".equals(action)||"death".equals(action)||"clickcoords".equals(action)||"markers".equals(action)||"arrival".equals(action)) {
                if(args.length<2){reply(sender,"Usage: /waypoint "+action+" <on|off>");return;}
                boolean value="on".equalsIgnoreCase(args[1]);
                if(!value&&!"off".equalsIgnoreCase(args[1])){reply(sender,"Usage: /waypoint "+action+" <on|off>");return;}
                if("minimap".equals(action)) MinimapWithWaypoints.getInstance().setEnabled(value);
                else if("death".equals(action)) DeathWaypointMod.getInstance().setEnabled(value);
                else if("clickcoords".equals(action)) WaypointFromClickableCoordinates.getInstance().setEnabled(value);
                else if("arrival".equals(action)) WaypointSettings.setBoolean("arrivalDetectionEnabled",value);
                else WorldWaypointIcons.getInstance().setEnabled(value);
                reply(sender,"Waypoint "+action+" "+(value?"enabled.":"disabled."));
            } else if("pattern".equals(action)) {
                handlePattern(sender,args);
            } else if("help".equals(action)) {
                reply(sender,"/waypoint open | visuals | profile <name> | favorite <name> | icon <name> <symbol> | history [add <number> [name]] | add <name> | addat <x> <y> <z> | list | select <name> | remove <name> | route <start> <end> [stop ...] | clearroute | import | export | clear confirm | category | minimap | arrival <on|off> | pattern <add|remove> ...");
            } else {
                reply(sender,"Unknown action. Use /waypoint help.");
            }
        } catch(NumberFormatException e) {
            reply(sender,"Enter valid numbers for coordinates, history positions, colors, and timer durations.");
        } catch(Exception e) {
            reply(sender,"Waypoint action failed: "+safe(e.getMessage()));
        }
    }

    private void handlePattern(ICommandSender sender,String[] args) {
        if(args.length>=5&&"add".equalsIgnoreCase(args[1])) {
            String name=args[2]; int seconds=Integer.parseInt(args[3]); String regex=join(args,4);
            EventCountdownHub.getInstance().addPattern(name,regex,seconds);
            reply(sender,"Local event pattern added: "+name+".");
        } else if(args.length>=3&&"remove".equalsIgnoreCase(args[1])) {
            reply(sender,EventCountdownHub.getInstance().removePattern(join(args,2))?"Event pattern removed.":"Pattern not found.");
        } else {
            reply(sender,"Usage: /waypoint pattern add <name> <seconds> <regex> | pattern remove <name>");
        }
    }

    private static StaffWaypoint find(WaypointManager manager,String[] args,int start) {
        return args.length<=start?null:manager.find(join(args,start));
    }
    private static String join(String[] values,int start) { return join(values,start,values.length); }
    private static String join(String[] values,int start,int end) {
        StringBuilder out=new StringBuilder();
        for(int i=start;i<end;i++){if(i>start)out.append(' ');out.append(values[i]);}
        String result=out.toString().trim();
        if(result.length()>=2&&result.charAt(0)=='"'&&result.charAt(result.length()-1)=='"')
            result=result.substring(1,result.length()-1);
        return result;
    }
    private static List<String> parseQuotedArguments(String[] values,int start) {
        List<String> result=new ArrayList<String>();
        StringBuilder token=new StringBuilder();
        boolean quoted=false;
        for(int i=start;i<values.length;i++) {
            String value=values[i];
            if(!quoted) {
                if(value.startsWith("\"")) {
                    String remainder=value.substring(1);
                    int closing=remainder.indexOf('"');
                    if(closing>=0) {
                        token.append(remainder,0,closing);
                        result.add(token.toString());
                        token.setLength(0);
                        String trailing=remainder.substring(closing+1);
                        if(!trailing.isEmpty())result.add(trailing);
                    } else {
                        token.append(remainder);
                        quoted=true;
                    }
                } else result.add(value);
            } else {
                int closing=value.indexOf('"');
                token.append(' ');
                if(closing>=0) {
                    token.append(value,0,closing);
                    result.add(token.toString());
                    token.setLength(0);
                    quoted=false;
                    String trailing=value.substring(closing+1);
                    if(!trailing.isEmpty())result.add(trailing);
                } else token.append(value);
            }
        }
        if(quoted)result.add(token.toString());
        return result;
    }
    private static String coords(StaffWaypoint p) {
        return (int)Math.floor(p.getX())+", "+(int)Math.floor(p.getY())+", "+(int)Math.floor(p.getZ());
    }
    private static String safe(String s){return s==null||s.trim().isEmpty()?"unknown error":s;}
    private static void reply(ICommandSender sender,String s){sender.addChatMessage(new ChatComponentText("§6[Waypoints] §f"+s));}

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender,String[] args,BlockPos pos) {
        List<String> result=new ArrayList<String>();
        if(args.length==1) {
            String[] actions={"open","visuals","options","profile","favorite","icon","history","add","addat","list","select","remove","route","clearroute","import","export","share","clear","category","minimap","death","clickcoords","markers","arrival","pattern","help"};
            String prefix=args[0].toLowerCase(Locale.ROOT);
            for(String a:actions) if(a.startsWith(prefix)) result.add(a);
        } else if(args.length>=2&&("select".equalsIgnoreCase(args[0])||"remove".equalsIgnoreCase(args[0])||"delete".equalsIgnoreCase(args[0])||"goto".equalsIgnoreCase(args[0])||"route".equalsIgnoreCase(args[0])||"favorite".equalsIgnoreCase(args[0]))) {
            return autocomplete.complete(args[args.length-1],WaypointManager.getInstance().getForCurrentServer());
        } else if(args.length==2&&"icon".equalsIgnoreCase(args[0])) {
            return autocomplete.complete(args[1],WaypointManager.getInstance().getForCurrentServer());
        } else if(args.length==2&&"pattern".equalsIgnoreCase(args[0])) {
            if("add".startsWith(args[1].toLowerCase(Locale.ROOT)))result.add("add");
            if("remove".startsWith(args[1].toLowerCase(Locale.ROOT)))result.add("remove");
        }
        return result;
    }
}
