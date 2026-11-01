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
    private static int MAX_BLOCKS = 32768;
    private static IBlockState GLOBAL_MASK;
    private static boolean editWandEnabled = true;
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

        if ("limit".equals(cmd) && args.length==2) {
            try {
                int requested=Integer.parseInt(args[1]);
                if(requested<1||requested>1000000){reply(sender,"Limit must be between 1 and 1000000 blocks.");return;}
                MAX_BLOCKS=requested;
                reply(sender,"Maximum region size set to "+MAX_BLOCKS+" blocks.");
            } catch(NumberFormatException ex){reply(sender,"Usage: //acwe limit <blocks>");}
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
        if ("searchitem".equals(cmd) && args.length>=2) {
            String query=args[1].toLowerCase(Locale.ROOT);
            java.util.List<String> matches=new java.util.ArrayList<String>();
            for(Object key:net.minecraft.item.Item.itemRegistry.getKeys()) {
                String id=String.valueOf(key);
                if(id.toLowerCase(Locale.ROOT).contains(query)&&matches.size()<12)matches.add(id);
            }
            reply(sender,matches.isEmpty()?"No items matched: "+query:"Items: "+matches.toString());
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
            BlockPos at=mc.thePlayer.getPosition();
            BlockPos top=mc.theWorld.getTopSolidOrLiquidBlock(at);
            int y=Math.min(254,Math.max(at.getY()+2,top.getY()+2));
            mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX,y,mc.thePlayer.posZ);
            reply(sender,"Moved above the highest block at your position.");
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
            final WorldEditSelection.Bounds b = selection(sender);
            if (target == null || b == null) { reply(sender, "Usage: //acwe count <block>"); return; }
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Selection contains unloaded chunks.");return;}
                int count=0;
                for(int x=b.minX;x<=b.maxX;x++)for(int y=b.minY;y<=b.maxY;y++)for(int z=b.minZ;z<=b.maxZ;z++)
                    if(w.getBlockState(new BlockPos(x,y,z)).getBlock()==target.getBlock())count++;
                reply(sender,"Found "+count+" matching block(s).");
            }});
            return;
        }
        if ("distr".equals(cmd)) {
            final WorldEditSelection.Bounds b=selection(sender);
            if(b==null)return;
            onServer(sender,new Task(){public void run(WorldServer w){
                if(!WorldEditOperations.regionLoaded(w,b)){reply(sender,"Selection contains unloaded chunks.");return;}
                java.util.Map<String,Integer> counts=new java.util.TreeMap<String,Integer>();
                for(int x=b.minX;x<=b.maxX;x++)for(int y=b.minY;y<=b.maxY;y++)for(int z=b.minZ;z<=b.maxZ;z++){
                    String name=String.valueOf(net.minecraft.block.Block.blockRegistry.getNameForObject(w.getBlockState(new BlockPos(x,y,z)).getBlock()));
                    Integer n=counts.get(name);counts.put(name,n==null?1:n+1);
                }
                int shown=0;
                for(java.util.Map.Entry<String,Integer> e:counts.entrySet()){
                    if(shown++==10)break;
                    reply(sender,e.getKey()+": "+e.getValue());
                }
                if(counts.isEmpty())reply(sender,"Selection is empty.");
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
            final BlockPos target = mc.thePlayer.getPosition();
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
                WorldEditAdvancedOperations.center(w,b,state); reply(sender,"Region center set.");
            }});
            return;
        }
        if ("overlay".equals(cmd) && args.length == 2) {
            final IBlockState state = WorldEditBlockParser.parse(args[1]);
            final WorldEditSelection.Bounds b = selection(sender);
            if (state == null || b == null) return;
            onServer(sender, new Task() { public void run(WorldServer w) {\n                if (!WorldEditOperations.regionLoaded(w,b)) { reply(sender,"Selection contains unloaded chunks."); return; }\n                WorldEditAdvancedOperations.overlay(w,b,state); reply(sender,"Surface overlay applied.");\n            }});
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
            final BlockPos center=mc.thePlayer.getPosition();
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
            final BlockPos at=mc.thePlayer.getPosition();
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
            if ("formats".equals(action)) { reply(sender,"Supported schematic format: AlphaClient .acwe (compressed NBT)."); return; }
            if ("list".equals(action)) {
                java.util.List<String> names=WorldEditSchematicStore.list();
                reply(sender,names.isEmpty()?"No AlphaClient schematics saved.":"Schematics: "+names.toString());
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
            final BlockPos target=mc.objectMouseOver!=null&&mc.objectMouseOver.getBlockPos()!=null?("fill".equals(cmd)?mc.objectMouseOver.getBlockPos().up():mc.objectMouseOver.getBlockPos().offset(mc.objectMouseOver.sideHit)):mc.thePlayer.getPosition();
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

        // Chunk utilities.
        if ("listchunks".equals(cmd)) {
            final BlockPos at=mc.thePlayer.getPosition();
            final WorldEditSelection.Bounds b=SELECTION.hasSelection()?SELECTION.getBounds():
                    new WorldEditSelection.Bounds(at.getX()-16,0,at.getZ()-16,at.getX()+16,255,at.getZ()+16);
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
            String[] values = {"help","listchunks","delchunks","curve","deform","forest","flora","pumpkins","forestgen","butcher","limit","gmask","mask","searchitem","calc","ceil","generatebiome","genbiome","gb","fill","fillr","wand","toggleeditwand","pos1","pos2","hpos1","hpos2","chunk","size","count","distr","sel","desel","deselect","unstuck","!","ascend","asc","descend","desc","up","thru","jumpto","j","biomelist","biomels","biomeinfo","setbiome","chunkinfo","expand","contract","shift","outset","inset","line","center","overlay","naturalize","smooth","move","stack","sphere","hsphere","cyl","hcyl","pyramid","hpyramid","set","replace","walls","outline","faces","hollow","undo","redo","clearhistory","schematic","schem","load","save","copy","cut","paste","rotate","flip","clearclipboard","drain","extinguish","removeabove","removebelow","replacenear","removenear","snow","thaw","green","fixwater","fixlava","remove"};
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
        if (module == null || !module.isEnabled() || !editWandEnabled) return;
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
