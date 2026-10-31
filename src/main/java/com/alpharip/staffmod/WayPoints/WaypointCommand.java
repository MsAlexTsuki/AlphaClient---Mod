package com.alpharip.staffmod.WayPoints;

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
    @Override public String getCommandUsage(ICommandSender sender) { return "/waypoint [open|add|list|select|remove|route|import|export]"; }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Minecraft mc=Minecraft.getMinecraft();
        if(mc==null) return;
        if(args==null||args.length==0||"open".equalsIgnoreCase(args[0])) {
            mc.displayGuiScreen(new WaypointsGui(mc.currentScreen)); return;
        }
        String action=args[0].toLowerCase(Locale.ROOT);
        WaypointManager manager=WaypointManager.getInstance();
        try {
            if("add".equals(action)) {
                if(mc.thePlayer==null){reply(sender,"Join a world before creating a waypoint.");return;}
                String name=args.length>1?join(args,1):"Waypoint";
                StaffWaypoint p=manager.add(name,mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ);
                reply(sender,"Waypoint saved: "+p.getLabel()+" ("+coords(p)+").");
            } else if("addat".equals(action)) {
                if(args.length<4){reply(sender,"Usage: /waypoint addat <x> <y> <z>");return;}
                String name=args.length>4?join(args,4):"Chat waypoint";
                StaffWaypoint p=manager.add(name,Double.parseDouble(args[1]),Double.parseDouble(args[2]),Double.parseDouble(args[3]));
                manager.select(p.getId()); reply(sender,"Waypoint saved: "+p.getLabel()+" ("+coords(p)+").");
            } else if("list".equals(action)) {
                List<StaffWaypoint> list=manager.getSortedCurrentWorld();
                if(list.isEmpty()){reply(sender,"No waypoints in this world yet.");return;}
                reply(sender,"Waypoints (nearest first):");
                if (mc.thePlayer != null) {
                    for (StaffWaypoint p : list) {
                        double dx = p.getX() - mc.thePlayer.posX;
                        double dy = p.getY() - mc.thePlayer.posY;
                        double dz = p.getZ() - mc.thePlayer.posZ;
                        long distance = Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz));
                        reply(sender, p.getLabel() + " — " + coords(p) + " (" + distance + "m)"
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
                if(args.length<3){reply(sender,"Usage: /waypoint route <start name> <end name> (quote names with spaces)");return;}
                StaffWaypoint start=manager.find(args[1]), end=manager.find(join(args,2));
                if(start==null||end==null){reply(sender,"Both waypoints must exist.");return;}
                if(!start.getServerKey().equals(end.getServerKey())||start.getDimensionId()!=end.getDimensionId()) {
                    reply(sender,"A route can only connect waypoints in the same server and dimension.");return;
                }
                manager.setRoute(start.getId(),end.getId());
                reply(sender,"Route set: "+start.getLabel()+" → "+end.getLabel()+" ("+Math.round(manager.getRoute().getDistance(start,end))+"m).");
            } else if("clearroute".equals(action)) {
                manager.clearRoute(); reply(sender,"Route cleared.");
            } else if("clear".equals(action)) {
                if(args.length<2||!"confirm".equalsIgnoreCase(args[1])) {
                    reply(sender,"This deletes every waypoint on this server. Confirm with /waypoint clear confirm."); return;
                }
                manager.clearCurrentServer(); reply(sender,"Waypoints for this server were cleared.");
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
                StaffWaypoint point=manager.find(args[1]);
                if(point==null){reply(sender,"Waypoint not found.");return;}
                String color=args[3].replace("#","");
                int rgb=Integer.parseInt(color,16);
                point.setCategory(args[2]); point.setColor(rgb); manager.saveChanges();
                reply(sender,"Updated "+point.getLabel()+" to category "+point.getCategory()+".");
            } else if("minimap".equals(action)||"death".equals(action)||"clickcoords".equals(action)||"markers".equals(action)) {
                if(args.length<2){reply(sender,"Usage: /waypoint "+action+" <on|off>");return;}
                boolean value="on".equalsIgnoreCase(args[1]);
                if(!value&&!"off".equalsIgnoreCase(args[1])){reply(sender,"Usage: /waypoint "+action+" <on|off>");return;}
                if("minimap".equals(action)) MinimapWithWaypoints.getInstance().setEnabled(value);
                else if("death".equals(action)) DeathWaypointMod.getInstance().setEnabled(value);
                else if("clickcoords".equals(action)) WaypointFromClickableCoordinates.getInstance().setEnabled(value);
                else WorldWaypointIcons.getInstance().setEnabled(value);
                reply(sender,"Waypoint "+action+" "+(value?"enabled.":"disabled."));
            } else if("pattern".equals(action)) {
                handlePattern(sender,args);
            } else if("help".equals(action)) {
                reply(sender,"/waypoint open | add <name> | addat <x> <y> <z> | list | select <name> | remove <name> | route <start> <end> | clearroute | import | export | clear confirm | category | minimap | pattern <add|remove> ...");
            } else {
                reply(sender,"Unknown action. Use /waypoint help.");
            }
        } catch(NumberFormatException e) {
            reply(sender,"Coordinates must be numbers.");
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
    private static String join(String[] values,int start) {
        StringBuilder out=new StringBuilder();
        for(int i=start;i<values.length;i++){if(i>start)out.append(' ');out.append(values[i]);}
        return out.toString().trim();
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
            String[] actions={"open","add","addat","list","select","remove","route","clearroute","import","export","share","clear","category","minimap","death","clickcoords","markers","pattern","help"};
            String prefix=args[0].toLowerCase(Locale.ROOT);
            for(String a:actions) if(a.startsWith(prefix)) result.add(a);
        } else if(args.length>=2&&("select".equalsIgnoreCase(args[0])||"remove".equalsIgnoreCase(args[0])||"delete".equalsIgnoreCase(args[0])||"goto".equalsIgnoreCase(args[0])||"route".equalsIgnoreCase(args[0]))) {
            return autocomplete.complete(args[args.length-1],WaypointManager.getInstance().getForCurrentServer());
        } else if(args.length==2&&"pattern".equalsIgnoreCase(args[0])) {
            if("add".startsWith(args[1].toLowerCase(Locale.ROOT)))result.add("add");
            if("remove".startsWith(args[1].toLowerCase(Locale.ROOT)))result.add("remove");
        }
        return result;
    }
}
