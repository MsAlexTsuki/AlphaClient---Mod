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
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** AlphaClient-only WorldEdit namespace. */
public final class AlphaWorldEditCommand extends CommandBase {
    private static int MAX_BLOCKS = 65536;
    private static IBlockState GLOBAL_MASK;
    private static boolean editWandEnabled = true;
    private static boolean togglePlace;
    private static String brushMode = "none", toolMode = "none", pickaxeMode = "none";
    private static int brushRadius = 3;
    private static IBlockState brushBlock = net.minecraft.init.Blocks.stone.getDefaultState();
    private static IBlockState toolFrom, toolTo;
    private static String activeAuditCommand = "Alpha World Edit operation";
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
        activeAuditCommand = "//acwe " + joinArguments(args);

        if ("logs".equals(cmd)) {
            AlphaClient.openGuiFromCommand(new WorldEditAuditGui(mc.currentScreen));
            return;
        }
        if ("helpids".equals(cmd)) {
            AlphaClient.openGuiFromCommand(new WorldEditIdGuideGui(mc.currentScreen));
            return;
        }
        if ("limit".equals(cmd) && args.length==2) {
            try {
                int requested=Integer.parseInt(args[1]);
                if(requested<1||requested>1000000){reply(sender,"Limit must be between 1 and 1000000 blocks.");return;}
                MAX_BLOCKS=requested;
                reply(sender,"Maximum region size set to "+MAX_BLOCKS+" blocks.");
            } catch(NumberFormatException ex){reply(sender,"Usage: //acwe limit <blocks>");}
            return;
        }
        if ("mat".equals(cmd) || "material".equals(cmd)) {
            if (args.length != 2) { reply(sender, "Usage: //acwe mat <block>"); return; }
            IBlockState parsed = WorldEditBlockParser.parse(args[1]);
            if (parsed == null) { reply(sender, "Unknown brush material: " + args[1]); return; }
            brushBlock = parsed;
            reply(sender, "Brush material set to " + args[1] + ".");
            return;
        }
        if ("range".equals(cmd)) {
            if (args.length != 2) { reply(sender, "Usage: //acwe range <1-16>"); return; }
            int requested;
            try { requested = Integer.parseInt(args[1]); }
            catch (NumberFormatException ex) { reply(sender, "Usage: //acwe range <1-16>"); return; }
            if (requested < 1 || requested > 16) { reply(sender, "Brush range must be between 1 and 16."); return; }
            brushRadius = requested;
            reply(sender, "Brush range set to " + brushRadius + ".");
            return;
        }

        if ("toggleplace".equals(cmd)) {
            if (args.length != 1) { reply(sender, "Usage: //acwe toggleplace"); return; }
            togglePlace = !togglePlace;
            reply(sender, togglePlace
                    ? "Placement position set to selection position 1."
                    : "Placement position set to your player position.");
            return;
        }

        if ("gmask".equals(cmd)||"mask".equals(cmd)) {
            if(args.length!=2){reply(sender,"Usage: //acwe gmask <block|off>");return;}
            if("off".equalsIgnoreCase(args[1])||"none".equalsIgnoreCase(args[1])) {
                GLOBAL_MASK=null;reply(sender,"Global block mask disabled.");return;
            }
            IBlockState parsed=WorldEditBlockParser.parse(args[1]);
            if(parsed==null){reply(sender,"Unknown mask block.");return;}
            GLOBAL_MASK=parsed;reply(sender,"Global mask set to "+args[1]+".");
            return;
        }
        if ("searchitem".equals(cmd) || "search".equals(cmd) || "l".equals(cmd)) {
            int index = 1, page = 1;
            boolean blocksOnly = false, itemsOnly = false;
            while (index < args.length && args[index].startsWith("-")) {
                String flag = args[index].toLowerCase(Locale.ROOT);
                if ("-p".equals(flag) && index + 1 < args.length) {
                    try { page = Integer.parseInt(args[index + 1]); }
                    catch (NumberFormatException ex) { reply(sender, "Usage: //acwe searchitem [-bi] [-p page] <query...>"); return; }
                    index += 2;
                } else if (flag.length() > 1 && flag.charAt(0) == '-') {
                    boolean validFlags = true;
                    for (int flagIndex = 1; flagIndex < flag.length(); flagIndex++) {
                        char option = flag.charAt(flagIndex);
                        if (option == 'b') blocksOnly = true;
                        else if (option == 'i') itemsOnly = true;
                        else validFlags = false;
                    }
                    if (!validFlags) { reply(sender, "Usage: //acwe searchitem [-bi] [-p page] <query...>"); return; }
                    index++;
                } else {
                    reply(sender, "Usage: //acwe searchitem [-bi] [-p page] <query...>");
                    return;
                }
            }
            if (index >= args.length || page < 1 || page > 1000) {
                reply(sender, "Usage: //acwe searchitem [-bi] [-p page] <query...>");
                return;
            }
            final String query = joinArguments(java.util.Arrays.copyOfRange(args, index, args.length)).toLowerCase(Locale.ROOT);
            java.util.Set<String> matches = new java.util.TreeSet<String>();
            if (!itemsOnly || blocksOnly) {
                for (Object key : net.minecraft.block.Block.blockRegistry.getKeys()) {
                    String id = String.valueOf(key);
                    if (id.toLowerCase(Locale.ROOT).contains(query)) matches.add(id);
                }
            }
            if (!blocksOnly || itemsOnly) {
                for (Object key : net.minecraft.item.Item.itemRegistry.getKeys()) {
                    String id = String.valueOf(key);
                    if (id.toLowerCase(Locale.ROOT).contains(query)) matches.add(id);
                }
            }
            java.util.List<String> results = new java.util.ArrayList<String>(matches);
            int pageSize = 12, pages = Math.max(1, (results.size() + pageSize - 1) / pageSize);
            if (results.isEmpty()) { reply(sender, "No items matched: " + query); return; }
            if (page > pages) { reply(sender, "Page " + page + " does not exist. Total pages: " + pages + "."); return; }
            reply(sender, "Search results for '" + query + "' — page " + page + "/" + pages + ":");
            int start = (page - 1) * pageSize, end = Math.min(results.size(), start + pageSize);
            for (int i = start; i < end; i++) reply(sender, results.get(i));
            return;
        }
        if ("calc".equals(cmd) && args.length==2) {
            java.util.regex.Matcher m=java.util.regex.Pattern.compile("^(-?[0-9]+)([+*/%-])(-?[0-9]+)$").matcher(args[1]);
            if(!m.matches()){reply(sender,"Usage: //acwe calc <number><+|-|*|/|%><number>");return;}
            try {
                long a=Long.parseLong(m.group(1)),b=Long.parseLong(m.group(3)),v;
                String op=m.group(2);
                if("/".equals(op)||"%".equals(op)){if(b==0){reply(sender,"Division by zero.");return;}}
                if("+".equals(op))v=a+b;else if("-".equals(op))v=a-b;else if("*".equals(op))v=a*b;else if("/".equals(op))v=a/b;else v=a%b;
                reply(sender,"Result: "+v);
            } catch(NumberFormatException ex){reply(sender,"Calculation is outside the supported integer range.");}
            return;
        }
        if ("ceil".equals(cmd)) {
            int clearance = 1;
            if (args.length > 2) { reply(sender, "Usage: //acwe ceil [clearance]"); return; }
            if (args.length == 2) {
                try { clearance = Integer.parseInt(args[1]); }
                catch (NumberFormatException ex) { reply(sender, "Usage: //acwe ceil [clearance]"); return; }
            }
            if (clearance < 1 || clearance > 64) { reply(sender, "Clearance must be between 1 and 64."); return; }
            final int requestedClearance = clearance;
            final java.util.UUID playerId = mc.thePlayer.getUniqueID();
            onServer(sender, new Task() { public void run(WorldServer world) {
                EntityPlayer player = findPlayer(world, playerId);
                if (player == null) { reply(sender, "Could not find your singleplayer player entity."); return; }
                BlockPos at = player.getPosition();
                BlockPos top = world.getTopSolidOrLiquidBlock(at);
                int targetY = top.getY() + requestedClearance + 1;
                if (targetY < 2 || targetY > 254) { reply(sender, "No safe ceiling position is available within the world height."); return; }
                if (!isClearPlayerPosition(player, world, targetY)) { reply(sender, "The requested ceiling position is obstructed."); return; }
                BlockPos support = new BlockPos(at.getX(), targetY - 1, at.getZ());
                if (!world.isSideSolid(support, net.minecraft.util.EnumFacing.UP)) {
                    world.setBlockState(support, net.minecraft.init.Blocks.glass.getDefaultState(), 2);
                }
                player.setPositionAndUpdate(player.posX, targetY, player.posZ);
                reply(sender, "Moved to the ceiling with " + requestedClearance + " block(s) of clearance.");
            }});
            return;
        }

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
        if ("size".equals(cmd) || ("count".equals(cmd) && args.length == 1)) {
            if (!SELECTION.hasSelection()) {
                reply(sender, "No selection. Set both positions first.");
                return;
            }
            WorldEditSelection.Bounds selected = SELECTION.getBounds();
            reply(sender, "Selection: " + selected.sizeX() + " x " + selected.sizeY()
                    + " x " + selected.sizeZ() + " (" + selected.volume() + " blocks).");
            return;
        }
        if ("count".equals(cmd) && args.length == 2) {
            final IBlockState target = WorldEditBlockParser.parse(args[1]);
            final boolean metadataSpecified = args[1].split(":").length == 3;
            final WorldEditSelection.Bounds b = selection(sender);
            if (target == null || b == null) { reply(sender, "Usage: //acwe count <block[:metadata]>"); return; }
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Selection contains unloaded chunks.");return;}
                int count=0;
                for(int x=b.minX;x<=b.maxX;x++)for(int y=b.minY;y<=b.maxY;y++)for(int z=b.minZ;z<=b.maxZ;z++) {
                    IBlockState current = w.getBlockState(new BlockPos(x,y,z));
                    if(current.getBlock()==target.getBlock() && (!metadataSpecified || current.equals(target))) count++;
                }
                reply(sender,"Found "+count+" matching block(s).");
            }});
            return;
        }
        if ("distr".equals(cmd)) {
            if (args.length > 2) { reply(sender, "Usage: //acwe distr [page]"); return; }
            int page = 1;
            if (args.length == 2) {
                try { page = Integer.parseInt(args[1]); }
                catch (NumberFormatException ex) { reply(sender, "Usage: //acwe distr [page]"); return; }
            }
            if (page < 1 || page > 1000) { reply(sender, "Page must be between 1 and 1000."); return; }
            final int requestedPage = page;
            final WorldEditSelection.Bounds b=selection(sender);
            if(b==null)return;
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Selection contains unloaded chunks.");return;}
                java.util.Map<String,Integer> counts=new java.util.HashMap<String,Integer>();
                for(int x=b.minX;x<=b.maxX;x++)for(int y=b.minY;y<=b.maxY;y++)for(int z=b.minZ;z<=b.maxZ;z++){
                    String name=String.valueOf(net.minecraft.block.Block.blockRegistry.getNameForObject(w.getBlockState(new BlockPos(x,y,z)).getBlock()));
                    Integer n=counts.get(name);counts.put(name,n==null?1:n+1);
                }
                java.util.List<java.util.Map.Entry<String,Integer>> entries =
                        new java.util.ArrayList<java.util.Map.Entry<String,Integer>>(counts.entrySet());
                java.util.Collections.sort(entries, new java.util.Comparator<java.util.Map.Entry<String,Integer>>() {
                    public int compare(java.util.Map.Entry<String,Integer> a, java.util.Map.Entry<String,Integer> b) {
                        int byCount = b.getValue().compareTo(a.getValue());
                        return byCount != 0 ? byCount : a.getKey().compareTo(b.getKey());
                    }
                });
                if(entries.isEmpty()){reply(sender,"Selection is empty.");return;}
                int pageSize = 10;
                int pages = (entries.size()+pageSize-1)/pageSize;
                if(requestedPage>pages){reply(sender,"Page "+requestedPage+" does not exist. Total pages: "+pages+".");return;}
                reply(sender,"Block distribution — page "+requestedPage+"/"+pages+" ("+entries.size()+" block types):");
                int start=(requestedPage-1)*pageSize,end=Math.min(entries.size(),start+pageSize);
                for(int i=start;i<end;i++){
                    java.util.Map.Entry<String,Integer> entry=entries.get(i);
                    reply(sender,entry.getKey()+": "+entry.getValue());
                }
            }});
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
        if ("unstuck".equals(cmd) || "!".equals(cmd)) {
            if (args.length != 1) { reply(sender, "Usage: //acwe unstuck"); return; }
            final java.util.UUID playerId = mc.thePlayer.getUniqueID();
            onServer(sender, new Task() { public void run(WorldServer world) {
                EntityPlayer player = findPlayer(world, playerId);
                if (player == null) { reply(sender, "Could not find your singleplayer player entity."); return; }
                int currentY = player.getPosition().getY();
                if (isClearPlayerPosition(player, world, currentY)) {
                    reply(sender, "You are not stuck in a block.");
                    return;
                }
                int targetY = findSafeStandingY(player, world, currentY + 1, 1, 1);
                if (targetY < 0) { reply(sender, "No safe position was found above you."); return; }
                player.setPositionAndUpdate(player.posX, targetY, player.posZ);
                reply(sender, "Moved to the nearest safe position above you (Y=" + targetY + ").");
            }});
            return;
        }
        if ("up".equals(cmd)) {
            int amount = 1;
            if (args.length > 2) { reply(sender, "Usage: //acwe up [distance]"); return; }
            if (args.length == 2) {
                try { amount = Integer.parseInt(args[1]); }
                catch (NumberFormatException ex) { reply(sender, "Usage: //acwe up [distance]"); return; }
            }
            if (amount < 1 || amount > 64) { reply(sender, "Distance must be between 1 and 64."); return; }
            final int distance = amount;
            final java.util.UUID playerId = mc.thePlayer.getUniqueID();
            onServer(sender, new Task() { public void run(WorldServer world) {
                EntityPlayer player = findPlayer(world, playerId);
                if (player == null) { reply(sender, "Could not find your singleplayer player entity."); return; }
                int targetY = player.getPosition().getY() + distance;
                if (targetY > 254 || !isClearPlayerPosition(player, world, targetY)) {
                    reply(sender, "There is not enough clear space above you.");
                    return;
                }
                BlockPos support = new BlockPos(player.getPosition().getX(), targetY - 1, player.getPosition().getZ());
                if (!world.isSideSolid(support, net.minecraft.util.EnumFacing.UP)) {
                    world.setBlockState(support, net.minecraft.init.Blocks.glass.getDefaultState(), 2);
                }
                player.setPositionAndUpdate(player.posX, targetY, player.posZ);
                reply(sender, "Moved up " + distance + " block(s).");
            }});
            return;
        }
        if ("ascend".equals(cmd) || "asc".equals(cmd) || "descend".equals(cmd) || "desc".equals(cmd)) {
            int levels = 1;
            if (args.length > 2) { reply(sender, "Usage: //acwe " + cmd + " [levels]"); return; }
            if (args.length == 2) {
                try { levels = Integer.parseInt(args[1]); }
                catch (NumberFormatException ex) { reply(sender, "Usage: //acwe " + cmd + " [levels]"); return; }
            }
            if (levels < 1 || levels > 32) { reply(sender, "Levels must be between 1 and 32."); return; }
            final int requestedLevels = levels;
            final boolean ascending = "ascend".equals(cmd) || "asc".equals(cmd);
            final java.util.UUID playerId = mc.thePlayer.getUniqueID();
            onServer(sender, new Task() { public void run(WorldServer world) {
                EntityPlayer player = findPlayer(world, playerId);
                if (player == null) { reply(sender, "Could not find your singleplayer player entity."); return; }
                int targetY = findSafeStandingY(player, world, player.getPosition().getY() + (ascending ? 1 : -1),
                        ascending ? 1 : -1, requestedLevels);
                if (targetY < 0) {
                    reply(sender, ascending ? "Could not find that many safe floors above you." : "Could not find that many safe floors below you.");
                    return;
                }
                player.setPositionAndUpdate(player.posX, targetY, player.posZ);
                reply(sender, "Moved " + (ascending ? "up" : "down") + " " + requestedLevels + " floor(s) to Y=" + targetY + ".");
            }});
            return;
        }
        if ("thru".equals(cmd)) {
            if (args.length != 1) { reply(sender, "Usage: //acwe thru"); return; }
            final java.util.UUID playerId = mc.thePlayer.getUniqueID();
            onServer(sender, new Task() { public void run(WorldServer world) {
                EntityPlayer player = findPlayer(world, playerId);
                if (player == null || !player.isEntityAlive()) {
                    reply(sender, "Could not find your singleplayer player entity.");
                    return;
                }
                net.minecraft.util.Vec3 destination = findThruDestination(player, world);
                if (destination == null) {
                    reply(sender, "No clear space was found beyond a wall within 12 blocks.");
                    return;
                }
                player.setPositionAndUpdate(destination.xCoord, destination.yCoord, destination.zCoord);
                reply(sender, "Moved through the wall.");
            }});
            return;
        }
        if ("jumpto".equals(cmd) || "j".equals(cmd)) {
            if (mc.objectMouseOver == null || mc.objectMouseOver.getBlockPos() == null) {
                reply(sender, "Look at a block first.");
                return;
            }
            BlockPos target = mc.objectMouseOver.getBlockPos().up();
            mc.thePlayer.setPositionAndUpdate(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D);
            reply(sender, "Teleported to the targeted block.");
            return;
        }

        if ("generatebiome".equals(cmd)||"genbiome".equals(cmd)||"gb".equals(cmd)) {
            if(args.length!=2){reply(sender,"Usage: //acwe generatebiome <biome>");return;}
            final String requested=args[1].replace('_',' ').toLowerCase(Locale.ROOT);
            final WorldEditSelection.Bounds b=selection(sender);if(b==null)return;
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Selection contains unloaded chunks.");return;}
                net.minecraft.world.biome.BiomeGenBase found=null;
                net.minecraft.world.biome.BiomeGenBase[] all=net.minecraft.world.biome.BiomeGenBase.getBiomeGenArray();
                if(all!=null)for(net.minecraft.world.biome.BiomeGenBase biome:all)
                    if(biome!=null&&biome.biomeName.toLowerCase(Locale.ROOT).equals(requested)){found=biome;break;}
                if(found==null){reply(sender,"Unknown biome: "+requested);return;}
                java.util.HashSet<net.minecraft.world.chunk.Chunk> changed=new java.util.HashSet<net.minecraft.world.chunk.Chunk>();
                for(int x=b.minX;x<=b.maxX;x++)for(int z=b.minZ;z<=b.maxZ;z++){
                    net.minecraft.world.chunk.Chunk chunk=w.getChunkFromBlockCoords(new BlockPos(x,b.minY,z));
                    byte[] data=chunk.getBiomeArray();data[(z&15)<<4|(x&15)]=(byte)found.biomeID;
                    chunk.setBiomeArray(data);changed.add(chunk);
                }
                for(net.minecraft.world.chunk.Chunk chunk:changed)chunk.setChunkModified();
                WorldEditAuditLog.recordCurrentSummary(w,"Biome changed to "+found.biomeName,b.sizeX()*b.sizeZ(),new BlockPos(b.minX,b.minY,b.minZ));
                reply(sender,"Biome set to "+found.biomeName+" across the selected columns.");
            }});
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
            if (args.length == 2 && "gone".equalsIgnoreCase(args[1])) {
                final java.util.UUID playerId = mc.thePlayer.getUniqueID();
                onServer(sender, new Task() {
                    public void run(WorldServer world) {
                        EntityPlayer player = null;
                        for (Object entry : world.playerEntities) {
                            EntityPlayer candidate = (EntityPlayer) entry;
                            if (playerId.equals(candidate.getUniqueID())) {
                                player = candidate;
                                break;
                            }
                        }
                        if (player == null) {
                            reply(sender, "Could not find your singleplayer inventory.");
                            return;
                        }
                        int removed = 0;
                        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                            if (isWorldEditWand(player.inventory.getStackInSlot(i))) {
                                player.inventory.setInventorySlotContents(i, null);
                                removed++;
                            }
                        }
                        player.inventory.markDirty();
                        player.inventoryContainer.detectAndSendChanges();
                        reply(player, "Removed " + removed + " WorldEdit wand(s) from your inventory.");
                    }
                });
                return;
            }
            if (args.length != 1) {
                reply(sender, "Usage: //acwe wand [gone]");
                return;
            }
            final java.util.UUID playerId = mc.thePlayer.getUniqueID();
            onServer(sender, new Task() {
                public void run(WorldServer world) {
                    EntityPlayer player = null;
                    for (Object entry : world.playerEntities) {
                        EntityPlayer candidate = (EntityPlayer) entry;
                        if (playerId.equals(candidate.getUniqueID())) {
                            player = candidate;
                            break;
                        }
                    }
                    if (player == null) {
                        reply(sender, "Could not find your singleplayer inventory.");
                        return;
                    }
                    for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                        if (isWorldEditWand(player.inventory.getStackInSlot(i))) {
                            reply(player, "You already have the WorldEdit wooden axe.");
                            return;
                        }
                    }
                    ItemStack wand = new ItemStack(Items.wooden_axe);
                    NBTTagCompound wandTag = new NBTTagCompound();
                    wandTag.setBoolean(WAND_TAG, true);
                    wand.setTagCompound(wandTag);
                    wand.setStackDisplayName("World Edit Wand");
                    if (player.inventory.addItemStackToInventory(wand)) {
                        player.inventory.markDirty();
                        player.inventoryContainer.detectAndSendChanges();
                        reply(player, "WorldEdit wand added to your inventory.");
                    } else {
                        reply(player, "Your inventory is full. Make room for the WorldEdit wand.");
                    }
                }
            });
            return;
        }
        if ("desel".equals(cmd) || "deselect".equals(cmd)) {
            SELECTION.setPos1(null); SELECTION.setPos2(null);
            reply(sender, "Selection cleared."); return;
        }
        if ("toggleeditwand".equals(cmd)) {
            editWandEnabled=!editWandEnabled;
            reply(sender,"World Edit wand selection is "+(editWandEnabled?"enabled.":"disabled."));
            return;
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
            final BlockPos dest = placementPosition(mc.thePlayer.getPosition());
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
            fill(sender, block, GLOBAL_MASK); return;
        }
        if ("replace".equals(cmd) && args.length == 3) {
            IBlockState from = WorldEditBlockParser.parse(args[1]);
            IBlockState to = WorldEditBlockParser.parse(args[2]);
            if (from == null || to == null) { reply(sender, "Usage: //acwe replace <from> <to>"); return; }
            fill(sender, to, from); return;
        }
        if (("walls".equals(cmd) || "outline".equals(cmd) || "faces".equals(cmd) || "hollow".equals(cmd)) && args.length == 2) {
            final IBlockState block = WorldEditBlockParser.parse(args[1]);
            if (block == null) { reply(sender, "Usage: //acwe " + cmd + " <block>"); return; }
            final int op = "walls".equals(cmd) ? WALLS : (("outline".equals(cmd) || "faces".equals(cmd)) ? OUTLINE : HOLLOW);
            final WorldEditSelection.Bounds b = selection(sender);
            if (b == null) return;
            onServer(sender, new Task() { public void run(WorldServer w) {
                if (!WorldEditOperations.regionLoaded(w, b)) { reply(sender, "Selection contains unloaded chunks."); return; }
                WorldEditOperations.shape(w, b, block, op, WALLS, OUTLINE, HOLLOW);
                reply(sender, op == WALLS ? "Side walls built." : op == OUTLINE ? "Region outline built." : "Hollow region built.");
            }});
            return;
        }
        // Biome commands.
        if ("biomelist".equals(cmd) || "biomels".equals(cmd)) {
            StringBuilder names = new StringBuilder("Biomes: ");
            net.minecraft.world.biome.BiomeGenBase[] biomes = net.minecraft.world.biome.BiomeGenBase.getBiomeGenArray();
            int shown = 0;
            if (biomes != null) for (net.minecraft.world.biome.BiomeGenBase biome : biomes) {
                if (biome != null && shown < 24) {
                    if (shown > 0) names.append(", ");
                    names.append(biome.biomeName);
                    shown++;
                }
            }
            reply(sender, names.toString());
            return;
        }
        if ("setbiome".equals(cmd) && args.length == 2) {
            final String requested = args[1].replace('_', ' ').toLowerCase(Locale.ROOT);
            final BlockPos target = placementPosition(mc.thePlayer.getPosition());
            onServer(sender, new Task() { public void run(WorldServer w) {
                net.minecraft.world.biome.BiomeGenBase found = null;
                net.minecraft.world.biome.BiomeGenBase[] all = net.minecraft.world.biome.BiomeGenBase.getBiomeGenArray();
                if (all != null) for (net.minecraft.world.biome.BiomeGenBase biome : all)
                    if (biome != null && biome.biomeName.toLowerCase(Locale.ROOT).equals(requested)) { found = biome; break; }
                if (found == null) { reply(sender, "Unknown biome: " + requested); return; }
                net.minecraft.world.chunk.Chunk chunk = w.getChunkFromBlockCoords(target);
                byte[] biomeData = chunk.getBiomeArray();
                int index = (target.getZ() & 15) << 4 | (target.getX() & 15);
                biomeData[index] = (byte) found.biomeID;
                chunk.setBiomeArray(biomeData);
                chunk.setChunkModified();
                WorldEditAuditLog.recordCurrentSummary(w,"Chunk biome changed to "+found.biomeName,1,target);
                reply(sender, "Biome changed to " + found.biomeName + " for the current chunk.");
            }});
            return;
        }

        // Region commands.
        // Selection geometry adjustments.
        if (("expand".equals(cmd)||"contract".equals(cmd)||"shift".equals(cmd)) && args.length>=2) {
            if(!SELECTION.hasSelection()){reply(sender,"Select two positions first.");return;}
            final int amount;
            try{amount=Integer.parseInt(args[1]);}catch(NumberFormatException ex){reply(sender,"Usage: //acwe "+cmd+" <amount> [direction]");return;}
            int dx=0,dy=0,dz=0;
            String direction=args.length>2?args[2].toLowerCase(Locale.ROOT):"forward";
            if("up".equals(direction))dy=1;else if("down".equals(direction))dy=-1;
            else if("north".equals(direction))dz=-1;else if("south".equals(direction))dz=1;
            else if("east".equals(direction))dx=1;else if("west".equals(direction))dx=-1;
            else if("forward".equals(direction)){net.minecraft.util.EnumFacing f=mc.thePlayer.getHorizontalFacing();dx=f.getFrontOffsetX();dz=f.getFrontOffsetZ();}
            else {reply(sender,"Direction must be up, down, north, south, east, west, or forward.");return;}
            BlockPos p1=SELECTION.getPos1(),p2=SELECTION.getPos2();
            if("shift".equals(cmd)){
                SELECTION.setPos1(p1.add(dx*amount,dy*amount,dz*amount));
                SELECTION.setPos2(p2.add(dx*amount,dy*amount,dz*amount));
            }else{
                boolean expand="expand".equals(cmd);
                int sign=expand?1:-1;
                int ax=dx*amount*sign,ay=dy*amount*sign,az=dz*amount*sign;
                int minX=Math.min(p1.getX(),p2.getX()),maxX=Math.max(p1.getX(),p2.getX());
                int minY=Math.min(p1.getY(),p2.getY()),maxY=Math.max(p1.getY(),p2.getY());
                int minZ=Math.min(p1.getZ(),p2.getZ()),maxZ=Math.max(p1.getZ(),p2.getZ());
                if(ax<0)minX+=ax;else maxX+=ax;
                if(ay<0)minY+=ay;else maxY+=ay;
                if(az<0)minZ+=az;else maxZ+=az;
                SELECTION.setPos1(new BlockPos(minX,minY,minZ));
                SELECTION.setPos2(new BlockPos(maxX,maxY,maxZ));
            }
            reply(sender,"Selection "+cmd+" completed: "+SELECTION.getBounds().volume()+" blocks.");
            return;
        }
        if (("outset".equals(cmd)||"inset".equals(cmd)) && args.length<=2) {
            if(!SELECTION.hasSelection()){reply(sender,"Select two positions first.");return;}
            int amount=1;
            try{if(args.length==2)amount=Integer.parseInt(args[1]);}catch(NumberFormatException ex){reply(sender,"Usage: //acwe "+cmd+" [amount]");return;}
            int sign="outset".equals(cmd)?1:-1;
            WorldEditSelection.Bounds b=SELECTION.getBounds();
            int minX=b.minX-sign*amount,maxX=b.maxX+sign*amount;
            int minY=b.minY-sign*amount,maxY=b.maxY+sign*amount;
            int minZ=b.minZ-sign*amount,maxZ=b.maxZ+sign*amount;
            if(minX>maxX||minY>maxY||minZ>maxZ){reply(sender,"Inset amount is larger than the selection.");return;}
            SELECTION.setPos1(new BlockPos(minX,minY,minZ));SELECTION.setPos2(new BlockPos(maxX,maxY,maxZ));
            reply(sender,"Selection "+cmd+" completed.");
            return;
        }

        if ("line".equals(cmd) && args.length == 2) {
            final IBlockState state = WorldEditBlockParser.parse(args[1]);
            if (state == null || !SELECTION.hasSelection()) { reply(sender, "Usage: //acwe line <block> (select two positions first)"); return; }
            final BlockPos a = SELECTION.getPos1(), b = SELECTION.getPos2();
            final WorldEditSelection.Bounds lineBounds=new WorldEditSelection.Bounds(Math.min(a.getX(),b.getX()),Math.min(a.getY(),b.getY()),Math.min(a.getZ(),b.getZ()),Math.max(a.getX(),b.getX()),Math.max(a.getY(),b.getY()),Math.max(a.getZ(),b.getZ()));
            if(!valid(sender,lineBounds))return;
            onServer(sender, new Task() { public void run(WorldServer w) {
                if(!WorldEditOperations.regionLoaded(w,lineBounds)){reply(sender,"Line area contains unloaded chunks.");return;}
                WorldEditAdvancedOperations.line(w,a,b,state); reply(sender,"Line drawn.");
            }});
            return;
        }
        if ("center".equals(cmd) && args.length == 2) {
            final IBlockState state = WorldEditBlockParser.parse(args[1]);
            final WorldEditSelection.Bounds b = selection(sender);
            if (state == null || b == null) return;
            onServer(sender, new Task() { public void run(WorldServer w) {
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Selection contains unloaded chunks.");return;}
                WorldEditAdvancedOperations.center(w,b,state); reply(sender,"Region center set.");
            }});
            return;
        }
        if ("overlay".equals(cmd) && args.length == 2) {
            final IBlockState state = WorldEditBlockParser.parse(args[1]);
            final WorldEditSelection.Bounds b = selection(sender);
            if (state == null || b == null) return;
            onServer(sender, new Task() { public void run(WorldServer w) {
                if (!WorldEditOperations.regionLoaded(w,b)) { reply(sender,"Selection contains unloaded chunks."); return; }
                WorldEditAdvancedOperations.overlay(w,b,state); reply(sender,"Surface overlay applied.");
            }});
            return;
        }
        if ("smooth".equals(cmd) && args.length<=2) {
            final int iterations;
            try{iterations=args.length==2?Integer.parseInt(args[1]):1;}catch(NumberFormatException ex){reply(sender,"Usage: //acwe smooth [iterations]");return;}
            if(iterations<1||iterations>5){reply(sender,"Iterations must be between 1 and 5.");return;}
            final WorldEditSelection.Bounds b=selection(sender);if(b==null)return;
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Selection contains unloaded chunks.");return;}
                WorldEditAdvancedOperations.smooth(w,b,iterations);reply(sender,"Terrain smoothed ("+iterations+" iteration(s)).");
            }});
            return;
        }
        if ("naturalize".equals(cmd)) {
            final WorldEditSelection.Bounds b = selection(sender);
            if (b == null) return;
            onServer(sender, new Task() { public void run(WorldServer w) {
                if (!WorldEditOperations.regionLoaded(w,b)) { reply(sender,"Selection contains unloaded chunks."); return; }
                WorldEditAdvancedOperations.naturalize(w,b); reply(sender,"Selected terrain naturalized.");
            }});
            return;
        }
        if ("move".equals(cmd) && args.length == 4) {
            final int dx,dy,dz;
            try { dx=Integer.parseInt(args[1]); dy=Integer.parseInt(args[2]); dz=Integer.parseInt(args[3]); }
            catch(NumberFormatException ex) { reply(sender,"Usage: //acwe move <x> <y> <z>"); return; }
            final WorldEditSelection.Bounds b=selection(sender);
            if(b==null) return;
            final WorldEditSelection.Bounds dest=new WorldEditSelection.Bounds(b.minX+dx,b.minY+dy,b.minZ+dz,b.maxX+dx,b.maxY+dy,b.maxZ+dz);
            if(!valid(sender,dest)) return;
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)||!WorldEditOperations.regionLoaded(w,dest)){reply(sender,"Move area contains unloaded chunks.");return;}
                WorldEditAdvancedOperations.move(w,b,dx,dy,dz); reply(sender,"Region moved.");
            }});
            return;
        }
        if ("stack".equals(cmd) && args.length >= 2 && args.length <= 5) {
            final int count,dx,dy,dz;
            try { count=Integer.parseInt(args[1]); dx=args.length>2?Integer.parseInt(args[2]):0; dy=args.length>3?Integer.parseInt(args[3]):0; dz=args.length>4?Integer.parseInt(args[4]):1; }
            catch(NumberFormatException ex) { reply(sender,"Usage: //acwe stack <count> [dx dy dz]"); return; }
            if(count<1||count>32){reply(sender,"Stack count must be between 1 and 32.");return;}
            final WorldEditSelection.Bounds b=selection(sender);
            if(b==null)return;
            final WorldEditSelection.Bounds dest=new WorldEditSelection.Bounds(b.minX+dx*count,b.minY+dy*count,b.minZ+dz*count,b.maxX+dx*count,b.maxY+dy*count,b.maxZ+dz*count);
            if(!valid(sender,dest))return;
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)||!WorldEditOperations.regionLoaded(w,dest)){reply(sender,"Stack area contains unloaded chunks.");return;}
                WorldEditAdvancedOperations.stack(w,b,count,dx,dy,dz);reply(sender,"Region stacked "+count+" time(s).");
            }});
            return;
        }

        // Primitive generation around the player.
        if (("sphere".equals(cmd)||"hsphere".equals(cmd)||"cyl".equals(cmd)||"hcyl".equals(cmd)
                ||"pyramid".equals(cmd)||"hpyramid".equals(cmd)) && args.length >= 3) {
            final IBlockState state=WorldEditBlockParser.parse(args[1]);
            final int radius,heightArg;
            try { radius=Integer.parseInt(args[2]); heightArg=args.length>3?Integer.parseInt(args[3]):radius*2+1; }
            catch(NumberFormatException ex){reply(sender,"Usage: //acwe "+cmd+" <block> <radius> [height]");return;}
            if(state==null||radius<1||radius>32||heightArg<1||heightArg>64){reply(sender,"Invalid block, radius, or height (radius 1-32, height 1-64).");return;}
            final BlockPos center=placementPosition(mc.thePlayer.getPosition());
            final boolean sphereCommand=cmd.endsWith("sphere");
            final int minY=sphereCommand?center.getY()-radius:center.getY();
            final int maxY=sphereCommand?center.getY()+radius:center.getY()+heightArg-1;
            final WorldEditSelection.Bounds bounds=new WorldEditSelection.Bounds(center.getX()-radius,Math.max(0,minY),center.getZ()-radius,center.getX()+radius,Math.min(255,maxY),center.getZ()+radius);
            if(!valid(sender,bounds))return;
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,bounds)){reply(sender,"Generation area contains unloaded chunks.");return;}
                if(sphereCommand) WorldEditAdvancedOperations.sphere(w,center,radius,radius,radius,state,cmd.startsWith("h"));
                else if(cmd.endsWith("cyl")) WorldEditAdvancedOperations.cylinder(w,center,radius,heightArg,state,cmd.startsWith("h"));
                else WorldEditAdvancedOperations.pyramid(w,center,radius,state,cmd.startsWith("h"));
                reply(sender,cmd+" generated.");
            }});
            return;
        }

        // Utility commands with bounded local radius.
        if (("drain".equals(cmd)||"extinguish".equals(cmd)) && args.length <= 2) {
            final int radius;
            try { radius=args.length>1?Integer.parseInt(args[1]):5; }
            catch(NumberFormatException ex){reply(sender,"Usage: //acwe "+cmd+" [radius]");return;}
            if(radius<1||radius>16){reply(sender,"Radius must be between 1 and 16.");return;}
            final BlockPos center=mc.thePlayer.getPosition();
            final WorldEditSelection.Bounds area=new WorldEditSelection.Bounds(center.getX()-radius,Math.max(0,center.getY()-radius),center.getZ()-radius,center.getX()+radius,Math.min(255,center.getY()+radius),center.getZ()+radius);
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,area)){reply(sender,"Area contains unloaded chunks.");return;}
                int changed="drain".equals(cmd)?WorldEditAdvancedOperations.drain(w,center,radius):WorldEditAdvancedOperations.extinguish(w,center,radius);
                reply(sender,cmd+" removed "+changed+" block(s).");
            }});
            return;
        }
        if (("removeabove".equals(cmd)||"removebelow".equals(cmd)) && args.length<=2) {
            final int distance;
            try { distance=args.length>1?Integer.parseInt(args[1]):20; }
            catch(NumberFormatException ex){reply(sender,"Usage: //acwe "+cmd+" [distance]");return;}
            if(distance<1||distance>64){reply(sender,"Distance must be between 1 and 64.");return;}
            final BlockPos at=placementPosition(mc.thePlayer.getPosition());
            final WorldEditSelection.Bounds area=new WorldEditSelection.Bounds(at.getX()-8,Math.max(0,at.getY()-distance),at.getZ()-8,at.getX()+8,Math.min(255,at.getY()+distance),at.getZ()+8);
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,area)){reply(sender,"Area contains unloaded chunks.");return;}
                if("removeabove".equals(cmd))WorldEditAdvancedOperations.removeAbove(w,at,distance);
                else WorldEditAdvancedOperations.removeBelow(w,at,distance);
                reply(sender,cmd+" completed.");
            }});
            return;
        }
        if ("replacenear".equals(cmd) && args.length>=3 && args.length<=4) {
            final IBlockState from=WorldEditBlockParser.parse(args[1]), to=WorldEditBlockParser.parse(args[2]);
            final int radius;
            try{radius=args.length==4?Integer.parseInt(args[3]):5;}
            catch(NumberFormatException ex){reply(sender,"Usage: //acwe replacenear <from> <to> [radius]");return;}
            if(from==null||to==null||radius<1||radius>16){reply(sender,"Invalid block or radius (1-16).");return;}
            final BlockPos center=mc.thePlayer.getPosition();
            final WorldEditSelection.Bounds area=new WorldEditSelection.Bounds(center.getX()-radius,Math.max(0,center.getY()-radius),center.getZ()-radius,center.getX()+radius,Math.min(255,center.getY()+radius),center.getZ()+radius);
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,area)){reply(sender,"Area contains unloaded chunks.");return;}
                int changed=WorldEditAdvancedOperations.replaceNear(w,center,radius,from,to);
                reply(sender,"Replaced "+changed+" nearby block(s).");
            }});
            return;
        }

        if ("clearhistory".equals(cmd)) {
            WorldEditHistory.clear();
            reply(sender,"World Edit undo and redo history cleared.");
            return;
        }
        if ("undo".equals(cmd)||"redo".equals(cmd)) {
            final boolean undo="undo".equals(cmd);
            onServer(sender,new Task(){public void run(WorldServer w){
                boolean changed=undo?WorldEditHistory.undo(w):WorldEditHistory.redo(w);
                reply(sender,changed?(undo?"Undo completed.":"Redo completed."):"Nothing to "+cmd+".");
            }});
            return;
        }

        // Local AlphaClient schematic files (independent .acwe NBT format).
        if (("schematic".equals(cmd)||"schem".equals(cmd)) && args.length>=2) {
            String action=args[1].toLowerCase(Locale.ROOT);
            if ("formats".equals(action)) { reply(sender,"Supported formats: Minecraft 1.8.9 .schematic (MCEdit/WorldEdit) and AlphaClient .acwe."); return; }
            if ("list".equals(action)) {
                java.util.List<String> names=WorldEditSchematicStore.list();
                reply(sender,names.isEmpty()?"No schematics found.":"Schematics: "+names.toString());
                return;
            }
            if (args.length!=3) { reply(sender,"Usage: //acwe schematic <load|save|delete> <name>"); return; }
            String name=args[2];
            if ("save".equals(action)) {
                if(!CLIPBOARD.hasBlocks()){reply(sender,"Copy a region before saving a schematic.");return;}
                try{WorldEditSchematicStore.save(name,CLIPBOARD.getBlocks());reply(sender,"Schematic saved: "+name);}
                catch(java.io.IOException ex){reply(sender,"Could not save schematic: "+ex.getMessage());}
                return;
            }
            if ("load".equals(action)) {
                try{CLIPBOARD.setBlocks(WorldEditSchematicStore.load(name));reply(sender,"Schematic loaded to clipboard: "+name);}
                catch(java.io.IOException ex){reply(sender,"Could not load schematic: "+ex.getMessage());}
                return;
            }
            if ("delete".equals(action)) {
                reply(sender,WorldEditSchematicStore.delete(name)?"Schematic deleted: "+name:"Schematic not found: "+name);
                return;
            }
            reply(sender,"Unknown schematic action. Use load, save, delete, formats, or list.");
            return;
        }
        if (("load".equals(cmd)||"save".equals(cmd)) && args.length==2) {
            if ("save".equals(cmd)) {
                if(!CLIPBOARD.hasBlocks()){reply(sender,"Copy a region before saving a schematic.");return;}
                try{WorldEditSchematicStore.save(args[1],CLIPBOARD.getBlocks());reply(sender,"Schematic saved: "+args[1]);}
                catch(java.io.IOException ex){reply(sender,"Could not save schematic: "+ex.getMessage());}
            } else {
                try{CLIPBOARD.setBlocks(WorldEditSchematicStore.load(args[1]));reply(sender,"Schematic loaded to clipboard: "+args[1]);}
                catch(java.io.IOException ex){reply(sender,"Could not load schematic: "+ex.getMessage());}
            }
            return;
        }

        // Clipboard editing.
        if ("cut".equals(cmd)) {
            final WorldEditSelection.Bounds b=selection(sender);
            if(b==null)return;
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Selection contains unloaded chunks.");return;}
                CLIPBOARD.setBlocks(WorldEditOperations.copy(w,b));
                WorldEditOperations.fill(w,b,net.minecraft.init.Blocks.air.getDefaultState(),null);
                reply(sender,"Region cut to the Alpha World Edit clipboard.");
            }});
            return;
        }
        if ("rotate".equals(cmd) && args.length==2 && CLIPBOARD.hasBlocks()) {
            final int degrees;
            try{degrees=Integer.parseInt(args[1]);}
            catch(NumberFormatException ex){reply(sender,"Usage: //acwe rotate <90|180|270>");return;}
            if(degrees!=90&&degrees!=180&&degrees!=270){reply(sender,"Rotation must be 90, 180, or 270 degrees.");return;}
            CLIPBOARD.setBlocks(WorldEditAdvancedOperations.rotateY(CLIPBOARD.getBlocks(),degrees));
            reply(sender,"Clipboard rotated "+degrees+" degrees around the vertical axis.");
            return;
        }
        if ("flip".equals(cmd) && args.length==2 && CLIPBOARD.hasBlocks()) {
            String axis=args[1].toLowerCase(Locale.ROOT);
            if(!"x".equals(axis)&&!"y".equals(axis)&&!"z".equals(axis)){reply(sender,"Usage: //acwe flip <x|y|z>");return;}
            CLIPBOARD.setBlocks(WorldEditAdvancedOperations.flip(CLIPBOARD.getBlocks(),axis));
            reply(sender,"Clipboard flipped on the "+axis+" axis.");
            return;
        }

        if (("fill".equals(cmd)||"fillr".equals(cmd)) && args.length>=2 && args.length<=3) {
            final IBlockState state=WorldEditBlockParser.parse(args[1]);
            final int radius;
            try{radius=args.length==3?Integer.parseInt(args[2]):5;}
            catch(NumberFormatException ex){reply(sender,"Usage: //acwe "+cmd+" <block> [radius]");return;}
            if(state==null||radius<1||radius>16){reply(sender,"Invalid block or radius (1-16).");return;}
            final BlockPos target=mc.objectMouseOver!=null&&mc.objectMouseOver.getBlockPos()!=null?("fill".equals(cmd)?mc.objectMouseOver.getBlockPos().up():mc.objectMouseOver.getBlockPos().offset(mc.objectMouseOver.sideHit)):placementPosition(mc.thePlayer.getPosition());
            final WorldEditSelection.Bounds area=new WorldEditSelection.Bounds(target.getX()-radius,Math.max(0,target.getY()-radius),target.getZ()-radius,target.getX()+radius,Math.min(255,target.getY()+radius),target.getZ()+radius);
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,area)){reply(sender,"Fill area contains unloaded chunks.");return;}
                WorldEditHistory.record(w,area);
                int changed="fill".equals(cmd)?WorldEditAdvancedOperations.fillDown(w,target,radius,state):WorldEditAdvancedOperations.floodFillAir(w,target,radius,state);
                reply(sender,cmd+" placed "+changed+" block(s).");
            }});
            return;
        }

        // Surface and nearby block utilities.
        if ("snow".equals(cmd)||"thaw".equals(cmd)||"green".equals(cmd)) {
            final WorldEditSelection.Bounds b=selection(sender);
            if(b==null)return;
            final int mode="snow".equals(cmd)?0:("thaw".equals(cmd)?1:2);
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Selection contains unloaded chunks.");return;}
                int changed=WorldEditAdvancedOperations.changeSurface(w,b,mode);
                reply(sender,cmd+" changed "+changed+" surface block(s).");
            }});
            return;
        }
        if ("fixwater".equals(cmd)||"fixlava".equals(cmd)) {
            final WorldEditSelection.Bounds b=selection(sender);
            if(b==null)return;
            final boolean lava="fixlava".equals(cmd);
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Selection contains unloaded chunks.");return;}
                int changed=WorldEditAdvancedOperations.fixLiquid(w,b,lava);
                reply(sender,cmd+" updated "+changed+" liquid block(s).");
            }});
            return;
        }
        if ("remove".equals(cmd)) {
            final WorldEditSelection.Bounds b=selection(sender);
            if(b==null)return;
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Selection contains unloaded chunks.");return;}
                WorldEditOperations.fill(w,b,net.minecraft.init.Blocks.air.getDefaultState(),null);
                reply(sender,"Selected region cleared.");
            }});
            return;
        }
        if ("removenear".equals(cmd)&&args.length>=2&&args.length<=3) {
            final IBlockState target=WorldEditBlockParser.parse(args[1]);
            final int radius;
            try{radius=args.length==3?Integer.parseInt(args[2]):5;}
            catch(NumberFormatException ex){reply(sender,"Usage: //acwe removenear <block> [radius]");return;}
            if(target==null||radius<1||radius>16){reply(sender,"Invalid block or radius (1-16).");return;}
            final BlockPos center=mc.thePlayer.getPosition();
            final WorldEditSelection.Bounds area=new WorldEditSelection.Bounds(center.getX()-radius,Math.max(0,center.getY()-radius),center.getZ()-radius,center.getX()+radius,Math.min(255,center.getY()+radius),center.getZ()+radius);
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,area)){reply(sender,"Area contains unloaded chunks.");return;}
                int changed=WorldEditAdvancedOperations.removeNear(w,center,radius,target);
                reply(sender,"Removed "+changed+" nearby block(s).");
            }});
            return;
        }

        if ("cs".equals(cmd)) {
            if(args.length!=2){reply(sender,"Usage: //acwe cs <script-name>");return;}
            try{WorldEditScriptStore.create(args[1]);reply(sender,"Script template created in AlphaWorldEdit/scripts: "+args[1]+".acs");}
            catch(java.io.IOException ex){reply(sender,"Could not create script: "+ex.getMessage());}
            return;
        }
        if (".s".equals(cmd)) {
            if(args.length!=2){reply(sender,"Usage: //acwe .s <script-name>");return;}
            try{
                java.util.List<String> lines=WorldEditScriptStore.read(args[1]);
                int executed=0;
                for(String line:lines){
                    if(line.startsWith("//acwe "))line=line.substring(7).trim();
                    if(line.startsWith("/")){reply(sender,"Scripts may contain only Alpha World Edit commands.");return;}
                    String[] scriptArgs=line.split("\\s+");
                    if(scriptArgs.length==0)continue;
                    String first=scriptArgs[0].toLowerCase(Locale.ROOT);
                    if("cs".equals(first)||".s".equals(first)||"help".equals(first)){reply(sender,"Nested scripts and help are not allowed inside scripts.");return;}
                    processCommand(sender,scriptArgs);
                    executed++;
                }
                reply(sender,"Executed "+executed+" Alpha World Edit script command(s).");
            }catch(java.io.IOException ex){reply(sender,"Could not run script: "+ex.getMessage());}
            return;
        }
        if ("snapshot".equals(cmd) && args.length>=2) {
            String action=args[1].toLowerCase(Locale.ROOT);
            if("list".equals(action)){
                java.util.List<String> names=WorldEditSnapshotStore.list();
                reply(sender,names.isEmpty()?"No snapshots saved.":"Snapshots: "+names.toString());return;
            }
            if(args.length!=3){reply(sender,"Usage: //acwe snapshot <before|after|use|sel> <name>");return;}
            String name=args[2];
            if("before".equals(action)||"after".equals(action)){
                if(!name.matches("[A-Za-z0-9_-]{1,41}")){reply(sender,"Snapshot names must use 1-41 letters, numbers, underscores, or hyphens.");return;}
                final String snapshotName=name+"-"+action;
                final WorldEditSelection.Bounds b=selection(sender);if(b==null)return;
                onServer(sender,new Task(){public void run(WorldServer w){
                    if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Snapshot area contains unloaded chunks.");return;}
                    try{WorldEditSnapshotStore.save(snapshotName,w,b);reply(sender,"Snapshot "+action+" saved: "+name);}
                    catch(java.io.IOException ex){reply(sender,"Could not save snapshot: "+ex.getMessage());}
                }});
                return;
            }
            try{
                String snapshotName=WorldEditSnapshotStore.exists(name+"-after")?name+"-after":
                        (WorldEditSnapshotStore.exists(name+"-before")?name+"-before":name);
                WorldEditSnapshotStore.Snapshot snapshot=WorldEditSnapshotStore.load(snapshotName);
                if("use".equals(action)){CLIPBOARD.setBlocks(snapshot.blocks);reply(sender,"Snapshot loaded into clipboard: "+name);return;}
                if("sel".equals(action)){
                    WorldEditSelection.Bounds b=snapshot.bounds();
                    SELECTION.setPos1(new BlockPos(b.minX,b.minY,b.minZ));SELECTION.setPos2(new BlockPos(b.maxX,b.maxY,b.maxZ));
                    reply(sender,"Selection restored from snapshot: "+name);return;
                }
                reply(sender,"Unknown snapshot action. Use before, after, use, sel, or list.");
            }catch(java.io.IOException ex){reply(sender,"Could not load snapshot: "+ex.getMessage());}
            return;
        }
        if ("restore".equals(cmd) && args.length==2) {
            final String name=args[1];
            final WorldEditSnapshotStore.Snapshot snapshot;
            try{
                String snapshotName=WorldEditSnapshotStore.exists(name+"-after")?name+"-after":
                        (WorldEditSnapshotStore.exists(name+"-before")?name+"-before":name);
                snapshot=WorldEditSnapshotStore.load(snapshotName);
            }catch(java.io.IOException ex){reply(sender,"Could not load snapshot: "+ex.getMessage());return;}
            final WorldEditSelection.Bounds b=snapshot.bounds();
            if(!valid(sender,b))return;
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Restore area contains unloaded chunks.");return;}
                WorldEditHistory.record(w,b);WorldEditOperations.paste(w,snapshot.origin,snapshot.blocks);
                reply(sender,"Snapshot restored: "+name);
            }});
            return;
        }

        // Brush configuration; the selected brush is applied with a right-click.
        if ("brush".equals(cmd)||"br".equals(cmd)) {
            if(args.length<2){reply(sender,"Usage: //acwe brush <sphere|cylinder|clipboard|smooth|ex|gravity|butcher|deform|set|paint|apply|forest|raise|lower> [block] [radius]");return;}
            String mode=args[1].toLowerCase(Locale.ROOT);
            String[] allowed={"none","sphere","cylinder","clipboard","smooth","ex","gravity","butcher","deform","set","paint","apply","forest","raise","lower"};
            boolean found=false;for(String value:allowed)if(value.equals(mode)){found=true;break;}
            if(!found){reply(sender,"Unknown brush type: "+mode);return;}
            if("none".equals(mode)){brushMode="none";reply(sender,"Brush disabled.");return;}
            int blockIndex=2,radiusIndex=3;
            IBlockState configured=brushBlock;
            if(args.length>2) {
                IBlockState parsed=WorldEditBlockParser.parse(args[2]);
                if(parsed!=null){configured=parsed;blockIndex=3;radiusIndex=4;}
                else if(args.length>2){reply(sender,"Unknown brush block: "+args[2]);return;}
            }
            int radius=brushRadius;
            try{if(args.length>radiusIndex)radius=Integer.parseInt(args[radiusIndex]);}
            catch(NumberFormatException ex){reply(sender,"Brush radius must be an integer.");return;}
            if(radius<1||radius>16){reply(sender,"Brush radius must be between 1 and 16.");return;}
            brushMode=mode;brushBlock=configured;brushRadius=radius;
            reply(sender,"Brush "+mode+" selected (radius "+radius+"). Right-click a block to apply it.");
            return;
        }
        if ("superpickaxe".equals(cmd)||"pickaxe".equals(cmd)||"sp".equals(cmd)) {
            if(args.length!=2){reply(sender,"Usage: //acwe superpickaxe <single|area|recursive>");return;}
            String mode=args[1].toLowerCase(Locale.ROOT);
            if("recur".equals(mode))mode="recursive";
            if(!"none".equals(mode)&&!"single".equals(mode)&&!"area".equals(mode)&&!"recursive".equals(mode)){reply(sender,"Mode must be single, area, or recursive.");return;}
            pickaxeMode=mode;
            reply(sender,"none".equals(mode)?"Super Pickaxe disabled.":"Super Pickaxe "+mode+" mode enabled. Left-click a block to use it.");
            return;
        }
        if ("tool".equals(cmd)) {
            if(args.length<2){reply(sender,"Usage: //acwe tool <none|info|tree|repl|cycler|floodfill|deltree|farwand|lrbuild> [arguments]");return;}
            String mode=args[1].toLowerCase(Locale.ROOT);
            if("none".equals(mode)){toolMode="none";reply(sender,"World Edit tool disabled.");return;}
            if(!"info".equals(mode)&&!"tree".equals(mode)&&!"repl".equals(mode)&&!"cycler".equals(mode)&&!"floodfill".equals(mode)&&!"deltree".equals(mode)&&!"farwand".equals(mode)&&!"lrbuild".equals(mode)){
                reply(sender,"Unknown World Edit tool: "+mode);return;
            }
            if("floodfill".equals(mode)&&args.length==2)toolTo=net.minecraft.init.Blocks.stone.getDefaultState();
            if("floodfill".equals(mode)&&args.length==3){
                toolTo=WorldEditBlockParser.parse(args[2]);
                if(toolTo==null){reply(sender,"Unknown flood-fill block.");return;}
            }
            if("repl".equals(mode)){
                if(args.length!=4){reply(sender,"Usage: //acwe tool repl <from> <to>");return;}
                toolFrom=WorldEditBlockParser.parse(args[2]);toolTo=WorldEditBlockParser.parse(args[3]);
                if(toolFrom==null||toolTo==null){reply(sender,"Unknown replacement block.");return;}
            }
            if("lrbuild".equals(mode)){
                if(args.length!=4){reply(sender,"Usage: //acwe tool lrbuild <left-block> <right-block>");return;}
                toolFrom=WorldEditBlockParser.parse(args[2]);toolTo=WorldEditBlockParser.parse(args[3]);
                if(toolFrom==null||toolTo==null){reply(sender,"Unknown build block.");return;}
            }
            toolMode=mode;reply(sender,"World Edit tool "+mode+" selected. Use left/right-click on a block.");
            return;
        }

        if ("regen".equals(cmd)) {
            final WorldEditSelection.Bounds b=selection(sender);if(b==null)return;
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Regeneration area contains unloaded chunks.");return;}
                WorldEditFeatureOperations.regenerate(w,b,w.getSeed()^System.nanoTime());
                reply(sender,"Selected area regenerated with Alpha World Edit terrain generation.");
            }});
            return;
        }
        if (("generate".equals(cmd)||"gen".equals(cmd)||"g".equals(cmd)) && args.length>=3) {
            final IBlockState state=WorldEditBlockParser.parse(args[1]);
            if(state==null){reply(sender,"Usage: //acwe generate <block> <expression> (variables: x, y, z; place blocks where the result is <= 0)");return;}
            final StringBuilder expressionBuilder=new StringBuilder();
            for(int i=2;i<args.length;i++){if(i>2)expressionBuilder.append(' ');expressionBuilder.append(args[i]);}
            final String expression=expressionBuilder.toString();
            try{WorldEditExpression.evaluate(expression,0,0,0);}
            catch(IllegalArgumentException ex){reply(sender,"Invalid generation expression: "+ex.getMessage());return;}
            final WorldEditSelection.Bounds b=selection(sender);if(b==null)return;
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Generation area contains unloaded chunks.");return;}
                try{
                    int placed=WorldEditFeatureOperations.generate(w,b,state,expression);
                    reply(sender,"Expression generation placed "+placed+" block(s).");
                }catch(IllegalArgumentException ex){reply(sender,"Generation failed: "+ex.getMessage());}
            }});
            return;
        }

        // Chunk utilities.
        if ("listchunks".equals(cmd)) {
            final BlockPos at=mc.thePlayer.getPosition();
            final WorldEditSelection.Bounds b=SELECTION.hasSelection()?selection(sender):
                    new WorldEditSelection.Bounds(at.getX()-16,0,at.getZ()-16,at.getX()+16,255,at.getZ()+16);
            if(b==null)return;
            int minCx=b.minX>>4,maxCx=b.maxX>>4,minCz=b.minZ>>4,maxCz=b.maxZ>>4;
            StringBuilder out=new StringBuilder("Chunks: ");
            int shown=0,total=0;
            for(int cx=minCx;cx<=maxCx;cx++)for(int cz=minCz;cz<=maxCz;cz++){
                boolean loaded=mc.theWorld.isBlockLoaded(new BlockPos(cx<<4,64,cz<<4));
                total++;
                if(shown<12){if(shown>0)out.append(", ");out.append(cx).append('/').append(cz).append(loaded?" (loaded)":" (not loaded)");shown++;}
            }
            out.append(" | ").append(total).append(" chunk(s) checked");
            reply(sender,out.toString());
            return;
        }
        if ("delchunks".equals(cmd)) {
            final BlockPos at=mc.thePlayer.getPosition();
            final int cx=at.getX()>>4,cz=at.getZ()>>4;
            final WorldEditSelection.Bounds chunkBounds=new WorldEditSelection.Bounds(cx<<4,0,cz<<4,(cx<<4)+15,255,(cz<<4)+15);
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,chunkBounds)){reply(sender,"Current chunk is not fully loaded.");return;}
                WorldEditHistory.record(w,chunkBounds);
                int changed=0;
                for(int x=chunkBounds.minX;x<=chunkBounds.maxX;x++)for(int y=0;y<=255;y++)for(int z=chunkBounds.minZ;z<=chunkBounds.maxZ;z++){
                    BlockPos p=new BlockPos(x,y,z);
                    if(!w.isAirBlock(p)){w.setBlockState(p,net.minecraft.init.Blocks.air.getDefaultState(),2);changed++;}
                }
                reply(sender,"Cleared "+changed+" block(s) in the current chunk. The chunk itself remains loaded.");
            }});
            return;
        }

        // Advanced region and natural generation commands.
        if ("curve".equals(cmd) && args.length==2) {
            final IBlockState state=WorldEditBlockParser.parse(args[1]);
            final WorldEditSelection.Bounds b=selection(sender);
            if(state==null||b==null){reply(sender,"Usage: //acwe curve <block>");return;}
            final BlockPos start=SELECTION.getPos1(),end=SELECTION.getPos2(),control=mc.thePlayer.getPosition();
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Curve area contains unloaded chunks.");return;}
                WorldEditFeatureOperations.curve(w,start,control,end,state);reply(sender,"Curve drawn between the selected corners using your position as its control point.");
            }});
            return;
        }
        if ("deform".equals(cmd) && args.length>=2 && args.length<=3) {
            final double amplitude,frequency;
            try{amplitude=Double.parseDouble(args[1]);frequency=args.length==3?Double.parseDouble(args[2]):0.25D;}
            catch(NumberFormatException ex){reply(sender,"Usage: //acwe deform <amplitude> [frequency]");return;}
            if(amplitude<0.0D||amplitude>32.0D||frequency<=0.0D||frequency>2.0D){reply(sender,"Amplitude must be 0-32 and frequency must be greater than 0 and at most 2.");return;}
            final WorldEditSelection.Bounds b=selection(sender);if(b==null)return;
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Deform area contains unloaded chunks.");return;}
                WorldEditFeatureOperations.deform(w,b,amplitude,frequency);reply(sender,"Selected terrain deformed.");
            }});
            return;
        }
        if ("forest".equals(cmd)||"flora".equals(cmd)||"pumpkins".equals(cmd)) {
            final int density;
            try{density=args.length>1?Integer.parseInt(args[1]):("forest".equals(cmd)?8:64);}
            catch(NumberFormatException ex){reply(sender,"Usage: //acwe "+cmd+" [count]");return;}
            if(density<1||density>512){reply(sender,"Count must be between 1 and 512.");return;}
            final WorldEditSelection.Bounds b=selection(sender);if(b==null)return;
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,cmd+" area contains unloaded chunks.");return;}
                java.util.Random random=new java.util.Random(w.getSeed()^System.nanoTime());
                int placed="forest".equals(cmd)?WorldEditFeatureOperations.forest(w,b,density,random):
                        ("flora".equals(cmd)?WorldEditFeatureOperations.flora(w,b,density,random):WorldEditFeatureOperations.pumpkins(w,b,density,random));
                reply(sender,cmd+" placed "+placed+" feature(s).");
            }});
            return;
        }
        if ("forestgen".equals(cmd) && args.length>=2 && args.length<=3) {
            final int radius,count;
            try{radius=Integer.parseInt(args[1]);count=args.length==3?Integer.parseInt(args[2]):32;}
            catch(NumberFormatException ex){reply(sender,"Usage: //acwe forestgen <radius> [tree-count]");return;}
            if(radius<1||radius>32||count<1||count>256){reply(sender,"Radius must be 1-32 and tree count 1-256.");return;}
            final BlockPos at=mc.thePlayer.getPosition();
            final WorldEditSelection.Bounds b=new WorldEditSelection.Bounds(at.getX()-radius,Math.max(0,at.getY()-16),at.getZ()-radius,at.getX()+radius,Math.min(255,at.getY()+32),at.getZ()+radius);
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Forest generation area contains unloaded chunks.");return;}
                int placed=WorldEditFeatureOperations.forest(w,b,Math.max(1,(radius*radius*4)/count),new java.util.Random(w.getSeed()^System.nanoTime()));
                reply(sender,"Forest generated: "+placed+" tree(s) placed in the bounded area.");
            }});
            return;
        }
        if ("butcher".equals(cmd) && args.length<=3) {
            final boolean animals=args.length>1&&"-a".equalsIgnoreCase(args[1]);
            final int radius;
            try{radius=args.length>(animals?2:1)?Integer.parseInt(args[animals?2:1]):16;}
            catch(NumberFormatException ex){reply(sender,"Usage: //acwe butcher [-a] [radius]");return;}
            if(radius<1||radius>64){reply(sender,"Radius must be between 1 and 64.");return;}
            final BlockPos center=mc.thePlayer.getPosition();
            onServer(sender,new Task(){public void run(WorldServer w){
                int removed=WorldEditFeatureOperations.butcher(w,center,radius,animals);
                WorldEditAuditLog.recordCurrentSummary(w,"Mobs removed",removed,center);
                reply(sender,"Removed "+removed+(animals?" hostile and passive":" hostile")+" mob(s).");
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
        final String auditAction = activeAuditCommand;
        server.addScheduledTask(new Runnable() { public void run() {
            WorldEditAuditLog.setCurrentAction(auditAction);
            WorldServer world = server.worldServerForDimension(dimension);
            if (world == null) { reply(sender, "Singleplayer world is unavailable."); return; }
            try { task.run(world); }
            finally { WorldEditAuditLog.clearCurrentAction(); }
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
            String[] values = {"help","helpids","logs","cs",".s","snapshot","restore","brush","br","superpickaxe","pickaxe","sp","tool","listchunks","delchunks","curve","deform","forest","flora","pumpkins","forestgen","butcher","regen","generate","gen","g","limit","gmask","mask","searchitem","search","l","mat","material","range","toggleplace","calc","ceil","generatebiome","genbiome","gb","fill","fillr","wand","toggleeditwand","pos1","pos2","hpos1","hpos2","chunk","size","count","distr","sel","desel","deselect","unstuck","!","ascend","asc","descend","desc","up","thru","jumpto","j","biomelist","biomels","biomeinfo","setbiome","chunkinfo","expand","contract","shift","outset","inset","line","center","overlay","naturalize","smooth","move","stack","sphere","hsphere","cyl","hcyl","pyramid","hpyramid","set","replace","walls","outline","faces","hollow","undo","redo","clearhistory","schematic","schem","load","save","copy","cut","paste","rotate","flip","clearclipboard","drain","extinguish","removeabove","removebelow","replacenear","removenear","snow","thaw","green","fixwater","fixlava","remove"};
            for (String value : values) if (value.startsWith(args[0].toLowerCase(Locale.ROOT))) result.add(value);
        }
        else if(args!=null&&args.length==2) {
            String parent=args[0].toLowerCase(Locale.ROOT);
            String[] values;
            if("snapshot".equals(parent))values=new String[]{"before","after","list","use","sel"};
            else if("brush".equals(parent)||"br".equals(parent))values=new String[]{"none","sphere","cylinder","clipboard","smooth","ex","gravity","butcher","deform","set","paint","apply","forest","raise","lower"};
            else if("tool".equals(parent))values=new String[]{"none","info","tree","repl","cycler","floodfill","deltree","farwand","lrbuild"};
            else if("superpickaxe".equals(parent)||"pickaxe".equals(parent)||"sp".equals(parent))values=new String[]{"none","single","area","recursive","recur"};
            else if("schematic".equals(parent)||"schem".equals(parent))values=new String[]{"load","save","delete","formats","list"};
            else values=new String[0];
            for(String value:values)if(value.startsWith(args[1].toLowerCase(Locale.ROOT)))result.add(value);
        }
        return result;
    }

    private static boolean isWorldEditWand(ItemStack stack) {
        return stack != null && stack.getItem() == Items.wooden_axe
                && stack.hasTagCompound() && stack.getTagCompound().getBoolean(WAND_TAG);
    }

    /** The tagged //acwe wand selects positions and never performs vanilla block interaction. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldEditWandUse(PlayerInteractEvent event) {
        if (event == null || event.world == null || event.entityPlayer == null || event.pos == null) return;

        AlphaModule module = AlphaModuleRegistry.find("world-edit");
        Minecraft mc = Minecraft.getMinecraft();
        if (module == null || !module.isEnabled() || !editWandEnabled
                || mc == null || !mc.isSingleplayer() || mc.thePlayer == null
                || !event.entityPlayer.getUniqueID().equals(mc.thePlayer.getUniqueID())
                || !isWorldEditWand(event.entityPlayer.getHeldItem())) return;

        boolean firstPosition = event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK;
        boolean secondPosition = event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK;
        if (!firstPosition && !secondPosition) return;

        // Let the client send RIGHT_CLICK_BLOCK to the integrated server.
        // The server then cancels the normal interaction and records pos2.
        if (event.world.isRemote) {
            if (secondPosition) return;
            event.setCanceled(true);
            return;
        }
        event.setCanceled(true);

        if (firstPosition) {
            SELECTION.setPos1(event.pos);
            reply(event.entityPlayer, "pos1 is set (" + event.pos.getX() + ", "
                    + event.pos.getY() + ", " + event.pos.getZ() + ")");
        } else {
            SELECTION.setPos2(event.pos);
            reply(event.entityPlayer, "pos2 is set (" + event.pos.getX() + ", "
                    + event.pos.getY() + ", " + event.pos.getZ() + ")");
        }
    }

    /** Applies configured Alpha World Edit tools only to the local Singleplayer player. */
    @SubscribeEvent
    public void onWorldEditToolUse(PlayerInteractEvent event) {
        if(event==null||event.world==null||event.world.isRemote||event.entityPlayer==null||event.pos==null)return;
        AlphaModule module=AlphaModuleRegistry.find("world-edit");
        if(module==null||!module.isEnabled())return;
        Minecraft mc=Minecraft.getMinecraft();
        if(mc==null||!mc.isSingleplayer()||mc.thePlayer==null
                ||!event.entityPlayer.getUniqueID().equals(mc.thePlayer.getUniqueID()))return;
        if(!(event.world instanceof WorldServer))return;
        if(isWorldEditWand(event.entityPlayer.getHeldItem()))return;
        final WorldServer world=(WorldServer)event.world;
        final BlockPos clicked=event.pos;
        final BlockPos adjacent=event.face==null?clicked.up():clicked.offset(event.face);
        boolean left=event.action==PlayerInteractEvent.Action.LEFT_CLICK_BLOCK;
        boolean right=event.action==PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK;
        if (left || right) {
            String action = !"none".equals(pickaxeMode) && left ? "Super Pickaxe: " + pickaxeMode
                    : (!"none".equals(toolMode) ? "Tool: " + toolMode
                    : (!"none".equals(brushMode) && right ? "Brush: " + brushMode : "World Edit interaction"));
            WorldEditAuditLog.setCurrentAction(action);
        }
        if(!left&&!right)return;

        if(left&&!"none".equals(pickaxeMode)) {
            WorldEditSelection.Bounds pickBounds=new WorldEditSelection.Bounds(clicked.getX()-3,Math.max(0,clicked.getY()-3),clicked.getZ()-3,
                    clicked.getX()+3,Math.min(255,clicked.getY()+3),clicked.getZ()+3);
            if(!WorldEditOperations.regionLoaded(world,pickBounds)){reply(event.entityPlayer,"Super Pickaxe area contains unloaded chunks.");event.setCanceled(true);return;}
            int mode="single".equals(pickaxeMode)?0:("area".equals(pickaxeMode)?1:2);
            int removed=WorldEditFeatureOperations.superPick(world,clicked,mode,3);
            reply(event.entityPlayer,"Super Pickaxe removed "+removed+" block(s).");
            event.setCanceled(true);return;
        }

        if(!"none".equals(toolMode)) {
            if("farwand".equals(toolMode)) {
                if(left)SELECTION.setPos1(clicked);else SELECTION.setPos2(clicked);
                reply(event.entityPlayer,(left?"Position 1":"Position 2")+" set: "+clicked.getX()+", "+clicked.getY()+", "+clicked.getZ());
                event.setCanceled(true);return;
            }
            if("info".equals(toolMode)) {
                IBlockState state=world.getBlockState(clicked);
                reply(event.entityPlayer,"Block: "+state.getBlock().getRegistryName()+" (metadata "+state.getBlock().getMetaFromState(state)+")");
                event.setCanceled(true);return;
            }
            if("lrbuild".equals(toolMode)&&left) {
                if(toolFrom!=null){WorldEditHistory.record(world,new WorldEditSelection.Bounds(adjacent.getX(),adjacent.getY(),adjacent.getZ(),adjacent.getX(),adjacent.getY(),adjacent.getZ()));world.setBlockState(adjacent,toolFrom,2);}
                reply(event.entityPlayer,"Left-click build block placed.");
                event.setCanceled(true);return;
            }
            if(!right)return;
            WorldEditSelection.Bounds toolArea=new WorldEditSelection.Bounds(clicked.getX()-8,Math.max(0,clicked.getY()-8),clicked.getZ()-8,
                    clicked.getX()+8,Math.min(255,clicked.getY()+8),clicked.getZ()+8);
            if(!WorldEditOperations.regionLoaded(world,toolArea)){reply(event.entityPlayer,"Tool area contains unloaded chunks.");event.setCanceled(true);return;}
            if("tree".equals(toolMode)) {
                WorldEditHistory.record(world,new WorldEditSelection.Bounds(adjacent.getX()-3,Math.max(0,adjacent.getY()-1),adjacent.getZ()-3,adjacent.getX()+3,Math.min(255,adjacent.getY()+8),adjacent.getZ()+3));
                boolean generated=new net.minecraft.world.gen.feature.WorldGenTrees(true).generate(world,new java.util.Random(),adjacent);
                reply(event.entityPlayer,generated?"Tree generated.":"Could not generate a tree here.");
            } else if("repl".equals(toolMode)) {
                int changed=WorldEditFeatureOperations.replaceNear(world,clicked,4,toolFrom,toolTo);
                reply(event.entityPlayer,"Tool replaced "+changed+" block(s).");
            } else if("cycler".equals(toolMode)) {
                WorldEditHistory.record(world,new WorldEditSelection.Bounds(clicked.getX(),clicked.getY(),clicked.getZ(),clicked.getX(),clicked.getY(),clicked.getZ()));
                IBlockState current=world.getBlockState(clicked);
                int meta=(current.getBlock().getMetaFromState(current)+1)&15;
                world.setBlockState(clicked,current.getBlock().getStateFromMeta(meta),2);
                reply(event.entityPlayer,"Block state cycled.");
            } else if("floodfill".equals(toolMode)) {
                WorldEditHistory.record(world,new WorldEditSelection.Bounds(adjacent.getX()-6,Math.max(0,adjacent.getY()-6),adjacent.getZ()-6,adjacent.getX()+6,Math.min(255,adjacent.getY()+6),adjacent.getZ()+6));
                IBlockState fill=toolTo==null?net.minecraft.init.Blocks.stone.getDefaultState():toolTo;
                int changed=WorldEditAdvancedOperations.floodFillAir(world,adjacent,6,fill);
                reply(event.entityPlayer,"Flood fill placed "+changed+" block(s).");
            } else if("deltree".equals(toolMode)) {
                int changed=WorldEditFeatureOperations.deleteTree(world,clicked,6);
                reply(event.entityPlayer,"Tree tool removed "+changed+" log/leaf block(s).");
            } else if("lrbuild".equals(toolMode)) {
                if(toolTo!=null){WorldEditHistory.record(world,new WorldEditSelection.Bounds(adjacent.getX(),adjacent.getY(),adjacent.getZ(),adjacent.getX(),adjacent.getY(),adjacent.getZ()));world.setBlockState(adjacent,toolTo,2);}
                reply(event.entityPlayer,"Right-click build block placed.");
            }
            event.setCanceled(true);return;
        }

        if(!right||"none".equals(brushMode))return;
        final BlockPos center=adjacent;
        WorldEditSelection.Bounds brushArea;
        if("clipboard".equals(brushMode)&&CLIPBOARD.hasBlocks()) {
            brushArea=new WorldEditSelection.Bounds(center.getX(),center.getY(),center.getZ(),
                    center.getX()+CLIPBOARD.getSizeX()-1,center.getY()+CLIPBOARD.getSizeY()-1,center.getZ()+CLIPBOARD.getSizeZ()-1);
        } else {
            brushArea=new WorldEditSelection.Bounds(center.getX()-brushRadius,Math.max(0,center.getY()-brushRadius-8),center.getZ()-brushRadius,
                    center.getX()+brushRadius,Math.min(255,center.getY()+brushRadius+16),center.getZ()+brushRadius);
        }
        if(brushArea.minY<0||brushArea.maxY>255){reply(event.entityPlayer,"Brush area must stay between Y=0 and Y=255.");event.setCanceled(true);return;}
        if(!WorldEditOperations.regionLoaded(world,brushArea)){reply(event.entityPlayer,"Brush area contains unloaded chunks.");event.setCanceled(true);return;}
        if("sphere".equals(brushMode)||"set".equals(brushMode)||"paint".equals(brushMode)||"apply".equals(brushMode)) {
            WorldEditAdvancedOperations.sphere(world,center,brushRadius,brushRadius,brushRadius,brushBlock,false);
        } else if("cylinder".equals(brushMode)) {
            WorldEditAdvancedOperations.cylinder(world,center,brushRadius,brushRadius*2+1,brushBlock,false);
        } else if("clipboard".equals(brushMode)) {
            if(CLIPBOARD.hasBlocks())WorldEditOperations.paste(world,center,CLIPBOARD.getBlocks());
        } else if("smooth".equals(brushMode)) {
            WorldEditSelection.Bounds b=new WorldEditSelection.Bounds(center.getX()-brushRadius,Math.max(0,center.getY()-brushRadius),center.getZ()-brushRadius,
                    center.getX()+brushRadius,Math.min(255,center.getY()+brushRadius),center.getZ()+brushRadius);
            if(WorldEditOperations.regionLoaded(world,b))WorldEditAdvancedOperations.smooth(world,b,1);
        } else if("ex".equals(brushMode)) {
            WorldEditAdvancedOperations.extinguish(world,center,brushRadius);
        } else if("gravity".equals(brushMode)) {
            int moved=WorldEditFeatureOperations.gravity(world,center,brushRadius);
            reply(event.entityPlayer,"Gravity brush moved "+moved+" block(s).");
        } else if("butcher".equals(brushMode)) {
            int removed=WorldEditFeatureOperations.butcher(world,center,brushRadius,true);
            reply(event.entityPlayer,"Butcher brush removed "+removed+" mob(s).");
        } else if("deform".equals(brushMode)) {
            WorldEditSelection.Bounds b=new WorldEditSelection.Bounds(center.getX()-brushRadius,Math.max(0,center.getY()-brushRadius),center.getZ()-brushRadius,
                    center.getX()+brushRadius,Math.min(255,center.getY()+brushRadius),center.getZ()+brushRadius);
            WorldEditFeatureOperations.deform(world,b,Math.max(1,brushRadius/2.0D),0.35D);
        } else if("forest".equals(brushMode)) {
            WorldEditSelection.Bounds b=new WorldEditSelection.Bounds(center.getX()-brushRadius,Math.max(0,center.getY()-8),center.getZ()-brushRadius,
                    center.getX()+brushRadius,Math.min(255,center.getY()+16),center.getZ()+brushRadius);
            WorldEditFeatureOperations.forest(world,b,4,new java.util.Random());
        } else if("raise".equals(brushMode)) {
            WorldEditFeatureOperations.raiseLower(world,center,brushRadius,1);
        } else if("lower".equals(brushMode)) {
            WorldEditFeatureOperations.raiseLower(world,center,brushRadius,-1);
        }
        event.setCanceled(true);
    }

    private static BlockPos placementPosition(BlockPos playerPosition) {
        BlockPos first = SELECTION.getPos1();
        return togglePlace && first != null ? first : playerPosition;
    }

    private static EntityPlayer findPlayer(WorldServer world, java.util.UUID playerId) {
        for (Object entry : world.playerEntities) {
            EntityPlayer candidate = (EntityPlayer) entry;
            if (playerId.equals(candidate.getUniqueID())) return candidate;
        }
        return null;
    }

    private static boolean isClearPlayerPosition(EntityPlayer player, net.minecraft.world.World world, int y) {
        if (y < 1 || y > 254) return false;
        net.minecraft.util.AxisAlignedBB candidate = player.getEntityBoundingBox().offset(0.0D, y - player.posY, 0.0D);
        return world.getCollidingBoundingBoxes(player, candidate).isEmpty();
    }

    /**
     * Finds successive positions with a solid floor and enough collision-free room for the player.
     */
    private static int findSafeStandingY(EntityPlayer player, net.minecraft.world.World world,
                                         int startY, int direction, int levels) {
        int found = 0;
        int x = player.getPosition().getX();
        int z = player.getPosition().getZ();
        int y = startY;
        while (y >= 1 && y <= 254) {
            BlockPos floor = new BlockPos(x, y - 1, z);
            if (world.isSideSolid(floor, net.minecraft.util.EnumFacing.UP)
                    && isClearPlayerPosition(player, world, y)) {
                found++;
                if (found >= levels) return y;
            }
            y += direction;
        }
        return -1;
    }

    /**
     * Finds the first collision-free player position after a wall in the look direction.
     * The scan is deliberately bounded so the command cannot teleport across large distances.
     */
    /**
     * Finds a collision-free position beyond an obstruction in the player's horizontal look direction.
     * The player's height is preserved so looking up or down cannot create an invalid body trajectory.
     */
    private static net.minecraft.util.Vec3 findThruDestination(EntityPlayer player, net.minecraft.world.World world) {
        net.minecraft.util.Vec3 look = player.getLook(1.0F);
        double horizontalLength = Math.sqrt(look.xCoord * look.xCoord + look.zCoord * look.zCoord);
        if (horizontalLength < 0.05D) return null;

        double directionX = look.xCoord / horizontalLength;
        double directionZ = look.zCoord / horizontalLength;
        boolean enteredWall = false;

        for (double distance = 0.25D; distance <= 12.0D; distance += 0.125D) {
            double x = player.posX + directionX * distance;
            double z = player.posZ + directionZ * distance;
            net.minecraft.util.AxisAlignedBB candidate = player.getEntityBoundingBox().offset(
                    x - player.posX, 0.0D, z - player.posZ);
            boolean clear = world.getCollidingBoundingBoxes(player, candidate).isEmpty();

            if (!clear) {
                enteredWall = true;
            } else if (enteredWall) {
                return new net.minecraft.util.Vec3(x, player.posY, z);
            }
        }
        return null;
    }

    private static String joinArguments(String[] args) {
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) value.append(" ");
            value.append(args[i]);
        }
        return value.toString();
    }

    @SubscribeEvent
    public void onAuditWorldTick(TickEvent.WorldTickEvent event) {
        WorldEditAuditLog.onWorldTick(event);
    }

    private interface Task { void run(WorldServer world); }
}
