// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.AlphaClans;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Clan command entry point. */
public final class AlphaClanCommand extends CommandBase {
    @Override public String getCommandName(){return "clan";}
    @Override public String getCommandUsage(ICommandSender s){return "/clan help";}
    @Override public int getRequiredPermissionLevel(){return 0;}
    @Override public void processCommand(ICommandSender sender,String[] args)throws CommandException{
        if(args==null||args.length==0||"help".equalsIgnoreCase(args[0])||"open".equalsIgnoreCase(args[0])){
            tell(sender,"/clan create <name> <tag> [public|private] | browse | invite <Alpha username>");
            tell(sender,"/clan join <clan UUID> | request <clan UUID> | accept <invite UUID> | decline <invite UUID> | leave | disband");
            Minecraft mc=Minecraft.getMinecraft();if(mc!=null){AlphaClanService.INSTANCE.refresh();mc.displayGuiScreen(new AlphaClansGui(mc.currentScreen instanceof net.minecraft.client.gui.GuiChat?null:mc.currentScreen));}return;
        }
        String route=args[0].toLowerCase(java.util.Locale.ROOT);
        if("create".equals(route)&&args.length>=3){String vis=args.length>3?args[3]:"public";AlphaClanService.INSTANCE.create(args[1],args[2],"private".equalsIgnoreCase(vis));}
        else if("browse".equals(route)){Minecraft mc=Minecraft.getMinecraft();if(mc!=null){AlphaClanService.INSTANCE.refresh();mc.displayGuiScreen(new AlphaClansGui(mc.currentScreen));}}
        else if("invite".equals(route)&&args.length==2)AlphaClanService.INSTANCE.invite(args[1]);
        else if("join".equals(route)&&args.length==2)AlphaClanService.INSTANCE.join(args[1]);
        else if("request".equals(route)&&args.length==2)AlphaClanService.INSTANCE.requestJoin(args[1]);
        else if("accept".equals(route)&&args.length==2)AlphaClanService.INSTANCE.acceptInvite(args[1],true);
        else if("decline".equals(route)&&args.length==2)AlphaClanService.INSTANCE.acceptInvite(args[1],false);
        else if("leave".equals(route))AlphaClanService.INSTANCE.leave();
        else if("disband".equals(route))AlphaClanService.INSTANCE.disband();
        else tell(sender,"Usage: /clan help");
    }
    private void tell(ICommandSender s,String m){s.addChatMessage(new ChatComponentText("§6[AlphaClans] §f"+m));}
    @Override public List<String> addTabCompletionOptions(ICommandSender s,String[] args,net.minecraft.util.BlockPos p){
        if(args.length==1)return Arrays.asList("help","open","browse","create","invite","join","request","accept","decline","leave","disband");
        return new ArrayList<String>();
    }
}
