package com.alpharip.staffmod.InventorySearch;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import java.util.Collections;
import java.util.List;
/** Opens the InventorySearch settings with /inventorysearch or /invsearch. */
public final class InventorySearchCommand extends CommandBase {
 @Override public String getCommandName(){return "inventorysearch";}
 @Override public List<String> getCommandAliases(){return Collections.singletonList("invsearch");}
 @Override public String getCommandUsage(ICommandSender sender){return "/inventorysearch";}
 @Override public int getRequiredPermissionLevel(){return 0;}
 @Override public void processCommand(ICommandSender sender,String[] args)throws CommandException{Minecraft.getMinecraft().displayGuiScreen(new InventorySearchGui(Minecraft.getMinecraft().currentScreen));}
 @Override public List<String> addTabCompletionOptions(ICommandSender sender,String[] args,BlockPos pos){return Collections.emptyList();}
}