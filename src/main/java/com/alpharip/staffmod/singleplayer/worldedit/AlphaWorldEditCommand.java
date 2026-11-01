// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import com.alpharip.alphaclient.AlphaClient;
import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** AlphaClient-only WorldEdit namespace. */
public final class AlphaWorldEditCommand extends CommandBase {
    private static final int MAX_BLOCKS = 32768;
    private static final String WAND_TAG = "AlphaWorldEditWand";
    private static final int WALLS = 1, OUTLINE = 2, HOLLOW = 3;
    private static final WorldEditSelection SELECTION = new WorldEditSelection();
    private static final WorldEditClipboard CLIPBOARD = new WorldEditClipboard();

    @Override public String getCommandName() { return "/acwe"; }
    @Override public String getCommandUsage(ICommandSender sender) { return "//acwe help"; }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        AlphaModule module = AlphaModuleRegistry.find("world-edit");
        if (module == null || !module.isEnabled()) {
            reply(sender, "Sorry, you have not enabled Singleplayer World Edit");
            return;
        }
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || !mc.isSingleplayer() || mc.thePlayer == null
                || mc.theWorld == null || mc.getIntegratedServer() == null) {
            reply(sender, "Alpha World Edit is available only in a loaded singleplayer world.");
            return;
        }
        if (args == null || args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            help(sender);
            return;
        }

        String cmd = args[0].toLowerCase(Locale.ROOT);

        // Selection commands and aliases.
        if ("hpos1".equals(cmd) || "hpos2".equals(cmd)) {
            if (mc.objectMouseOver == null || mc.objectMouseOver.getBlockPos() == null) {
                reply(sender, "Look at a block first.");
                return;
            }
            BlockPos target = mc.objectMouseOver.getBlockPos();
            if ("hpos1".equals(cmd)) SELECTION.setPos1(target); else SELECTION.setPos2(target);
            reply(sender, ("hpos1".equals(cmd) ? "Position 1: " : "Position 2: ")
                    + target.getX() + ", " + target.getY() + ", " + target.getZ());
            return;
        }
        if ("sel".equals(cmd) || "desel".equals(cmd) || "deselect".equals(cmd)) {
            SELECTION.setPos1(null);
            SELECTION.setPos2(null);
            reply(sender, "Selection cleared.");
            return;
        }
        if ("size".equals(cmd) || "count".equals(cmd)) {
            if (!SELECTION.hasSelection()) {
                reply(sender, "No selection. Set both positions first.");
                return;
            }
            WorldEditSelection.Bounds selected = SELECTION.getBounds();
            reply(sender, "Selection: " + selected.sizeX() + " x " + selected.sizeY()
                    + " x " + selected.sizeZ() + " (" + selected.volume() + " blocks).");
            return;
        }
        if ("chunk".equals(cmd)) {
            int cx = mc.thePlayer.getPosition().getX() >> 4;
            int cz = mc.thePlayer.getPosition().getZ() >> 4;
            int minX = cx << 4;
            int minZ = cz << 4;
            SELECTION.setPos1(new BlockPos(minX, 0, minZ));
            SELECTION.setPos2(new BlockPos(minX + 15, 255, minZ + 15));
            reply(sender, "Selection set to your current chunk. Note: operations remain subject to the region size limit.");
            return;
        }

        // Singleplayer navigation helpers.
        if ("up".equals(cmd) || "ascend".equals(cmd) || "asc".equals(cmd)
                || "descend".equals(cmd) || "desc".equals(cmd) || "unstuck".equals(cmd)
                || "!".equals(cmd)) {
            int amount = 1;
            if (args.length > 1) {
                try { amount = Math.max(1, Math.min(64, Integer.parseInt(args[1]))); }
                catch (NumberFormatException ignored) { reply(sender, "Usage: //acwe " + cmd + " [distance]"); return; }
            }
            int targetY = mc.thePlayer.getPosition().getY();
            if ("descend".equals(cmd) || "desc".equals(cmd)) targetY -= amount;
            else targetY += amount;
            targetY = Math.max(1, Math.min(254, targetY));
            mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, targetY, mc.thePlayer.posZ);
            reply(sender, "Moved to Y=" + targetY + ".");
            return;
        }
        if ("jumpto".equals(cmd) || "j".equals(cmd) || "thru".equals(cmd)) {
            if (mc.objectMouseOver == null || mc.objectMouseOver.getBlockPos() == null) {
                reply(sender, "Look at a block first.");
                return;
            }
            BlockPos target = mc.objectMouseOver.getBlockPos().up();
            mc.thePlayer.setPositionAndUpdate(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D);
            reply(sender, "Teleported to the targeted block.");
            return;
        }

        if ("biomeinfo".equals(cmd)) {
            BlockPos at = mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null
                    ? mc.objectMouseOver.getBlockPos() : mc.thePlayer.getPosition();
            net.minecraft.world.biome.BiomeGenBase biome = mc.theWorld.getBiomeGenForCoords(at);
            reply(sender, "Biome at " + at.getX() + ", " + at.getY() + ", " + at.getZ()
                    + ": " + (biome == null ? "unknown" : biome.biomeName));
            return;
        }
        if ("chunkinfo".equals(cmd)) {
            BlockPos at = mc.thePlayer.getPosition();
            reply(sender, "Chunk: " + (at.getX() >> 4) + ", " + (at.getZ() >> 4)
                    + " (block origin " + ((at.getX() >> 4) << 4) + ", " + ((at.getZ() >> 4) << 4) + ").");
            return;
        }
        if ("pos1".equals(cmd) || "pos2".equals(cmd)) {
            BlockPos pos = mc.thePlayer.getPosition();
            if ("pos1".equals(cmd)) SELECTION.setPos1(pos); else SELECTION.setPos2(pos);
            reply(sender, ("pos1".equals(cmd) ? "Position 1: " : "Position 2: ")
                    + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
            return;
        }
        if ("wand".equals(cmd)) {
            if (!(mc.thePlayer instanceof EntityPlayer)) {
                reply(sender, "Could not give the WorldEdit wand.");
                return;
            }
            EntityPlayer player = mc.thePlayer;
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (isWorldEditWand(stack)) {
                    reply(sender, "You already have the WorldEdit wooden axe.");
                    return;
                }
            }
            ItemStack wand = new ItemStack(Items.wooden_axe);
            NBTTagCompound wandTag = new NBTTagCompound();
            wandTag.setBoolean(WAND_TAG, true);
            wand.setTagCompound(wandTag);
            wand.setStackDisplayName("World Edit Wand");
            if (player.inventory.addItemStackToInventory(wand)) {
                player.inventoryContainer.detectAndSendChanges();
                reply(sender, "WorldEdit wand added to your inventory.");
            } else {
                reply(sender, "Your inventory is full. Make room for the WorldEdit wand.");
            }
            return;
        }
        if ("desel".equals(cmd) || "deselect".equals(cmd)) {
            SELECTION.setPos1(null); SELECTION.setPos2(null);
            reply(sender, "Selection cleared."); return;
        }
        if ("clearclipboard".equals(cmd)) {
            CLIPBOARD.clear(); reply(sender, "Clipboard cleared."); return;
        }
        if ("copy".equals(cmd)) {
            final WorldEditSelection.Bounds b = selection(sender);
            if (b == null) return;
            onServer(sender, new Task() { public void run(WorldServer w) {
                if (!WorldEditOperations.regionLoaded(w, b)) { reply(sender, "Selection contains unloaded chunks."); return; }
                CLIPBOARD.setBlocks(WorldEditOperations.copy(w, b));
                reply(sender, "Region copied to the WorldEdit clipboard.");
            }});
            return;
        }
        if ("paste".equals(cmd)) {
            if (!CLIPBOARD.hasBlocks()) { reply(sender, "Copy a region before pasting."); return; }
            final BlockPos dest = mc.thePlayer.getPosition();
            final IBlockState[][][] blocks = CLIPBOARD.getBlocks();
            final WorldEditSelection.Bounds b = new WorldEditSelection.Bounds(dest.getX(), dest.getY(), dest.getZ(),
                    dest.getX() + CLIPBOARD.getSizeX() - 1, dest.getY() + CLIPBOARD.getSizeY() - 1,
                    dest.getZ() + CLIPBOARD.getSizeZ() - 1);
            if (!valid(sender, b)) return;
            onServer(sender, new Task() { public void run(WorldServer w) {
                if (!WorldEditOperations.regionLoaded(w, b)) { reply(sender, "Paste area contains unloaded chunks."); return; }
                WorldEditOperations.paste(w, dest, blocks); reply(sender, "Clipboard pasted at your position.");
            }});
            return;
        }
        if ("set".equals(cmd) && args.length == 2) {
            IBlockState block = WorldEditBlockParser.parse(args[1]);
            if (block == null) { reply(sender, "Usage: //acwe set <block>"); return; }
            fill(sender, block, null); return;
        }
        if ("replace".equals(cmd) && args.length == 3) {
            IBlockState from = WorldEditBlockParser.parse(args[1]);
            IBlockState to = WorldEditBlockParser.parse(args[2]);
            if (from == null || to == null) { reply(sender, "Usage: //acwe replace <from> <to>"); return; }
            fill(sender, to, from); return;
        }
        if (("walls".equals(cmd) || "outline".equals(cmd) || "hollow".equals(cmd)) && args.length == 2) {
            final IBlockState block = WorldEditBlockParser.parse(args[1]);
            if (block == null) { reply(sender, "Usage: //acwe " + cmd + " <block>"); return; }
            final int op = "walls".equals(cmd) ? WALLS : ("outline".equals(cmd) ? OUTLINE : HOLLOW);
            final WorldEditSelection.Bounds b = selection(sender);
            if (b == null) return;
            onServer(sender, new Task() { public void run(WorldServer w) {
                if (!WorldEditOperations.regionLoaded(w, b)) { reply(sender, "Selection contains unloaded chunks."); return; }
                WorldEditOperations.shape(w, b, block, op, WALLS, OUTLINE, HOLLOW);
                reply(sender, op == WALLS ? "Side walls built." : op == OUTLINE ? "Region outline built." : "Hollow region built.");
            }});
            return;
        }
        reply(sender, "Unknown or unsupported Alpha World Edit command. Use //acwe help.");
    }

    private void fill(final ICommandSender sender, final IBlockState state, final IBlockState filter) {
        final WorldEditSelection.Bounds b = selection(sender);
        if (b == null) return;
        onServer(sender, new Task() { public void run(WorldServer w) {
            if (!WorldEditOperations.regionLoaded(w, b)) { reply(sender, "Selection contains unloaded chunks."); return; }
            WorldEditOperations.fill(w, b, state, filter);
            reply(sender, filter == null ? "Region filled." : "Matching blocks replaced.");
        }});
    }

    private WorldEditSelection.Bounds selection(ICommandSender sender) {
        if (!SELECTION.hasSelection()) { reply(sender, "Set both positions first: //acwe pos1 and //acwe pos2."); return null; }
        WorldEditSelection.Bounds b = SELECTION.getBounds();
        return valid(sender, b) ? b : null;
    }

    private boolean valid(ICommandSender sender, WorldEditSelection.Bounds b) {
        if (b.minY < 0 || b.maxY > 255) { reply(sender, "Selection must stay between Y=0 and Y=255."); return false; }
        if (b.volume() > MAX_BLOCKS) { reply(sender, "Selection is too large. Maximum: " + MAX_BLOCKS + " blocks."); return false; }
        return true;
    }

    private void onServer(final ICommandSender sender, final Task task) {
        final Minecraft mc = Minecraft.getMinecraft();
        final IntegratedServer server = mc == null ? null : mc.getIntegratedServer();
        if (server == null || mc.theWorld == null) { reply(sender, "Integrated server is unavailable."); return; }
        final int dimension = mc.theWorld.provider.getDimensionId();
        server.addScheduledTask(new Runnable() { public void run() {
            WorldServer world = server.worldServerForDimension(dimension);
            if (world == null) { reply(sender, "Singleplayer world is unavailable."); return; }
            task.run(world);
        }});
    }

    private void help(ICommandSender sender) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) {
            reply(sender, "World Edit command guide is unavailable.");
            return;
        }
        GuiScreen parent = mc.currentScreen;
        AlphaClient.openGuiFromCommand(new WorldEditCommandGuideGui(parent));
    }

    private void reply(ICommandSender sender, String message) {
        sender.addChatMessage(new ChatComponentText("§6[AlphaClient] §f" + message));
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        List<String> result = new ArrayList<String>();
        if (args != null && args.length == 1) {
            String[] values = {"help","wand","pos1","pos2","desel","set","replace","walls","outline","hollow","copy","paste","clearclipboard"};
            for (String value : values) if (value.startsWith(args[0].toLowerCase(Locale.ROOT))) result.add(value);
        }
        return result;
    }

    private static boolean isWorldEditWand(ItemStack stack) {
        return stack != null && stack.getItem() == Items.wooden_axe
                && stack.hasTagCompound() && stack.getTagCompound().getBoolean(WAND_TAG);
    }

    /** Only the tagged //acwe wand wooden axe may change WorldEdit selections. */
    @SubscribeEvent
    public void onWorldEditWandUse(PlayerInteractEvent event) {
        if (event == null || event.world == null || event.world.isRemote
                || event.entityPlayer == null) {
            return;
        }
        AlphaModule module = AlphaModuleRegistry.find("world-edit");
        if (module == null || !module.isEnabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || !mc.isSingleplayer() || mc.thePlayer == null
                || event.entityPlayer.getUniqueID() == null
                || !event.entityPlayer.getUniqueID().equals(mc.thePlayer.getUniqueID())) return;
        ItemStack held = event.entityPlayer.getHeldItem();
        if (!isWorldEditWand(held) || event.pos == null) return;

        if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            SELECTION.setPos1(event.pos);
            reply(event.entityPlayer, "Position 1 set to " + event.pos.getX() + ", "
                    + event.pos.getY() + ", " + event.pos.getZ());
            event.setCanceled(true);
        } else if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            SELECTION.setPos2(event.pos);
            reply(event.entityPlayer, "Position 2 set to " + event.pos.getX() + ", "
                    + event.pos.getY() + ", " + event.pos.getZ());
            event.setCanceled(true);
        }
    }

    private interface Task { void run(WorldServer world); }
}
