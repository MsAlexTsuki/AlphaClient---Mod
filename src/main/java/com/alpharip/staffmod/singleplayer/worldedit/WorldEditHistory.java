// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.worldedit;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.ArrayDeque;
import java.util.Deque;

/** Bounded in-memory undo/redo history for Alpha World Edit region changes. */
public final class WorldEditHistory {
    private static final int MAX_ENTRIES=20;
    private static final Deque<Snapshot> UNDO=new ArrayDeque<Snapshot>();
    private static final Deque<Snapshot> REDO=new ArrayDeque<Snapshot>();

    private WorldEditHistory(){}

    public static void record(WorldServer world,WorldEditSelection.Bounds bounds) {
        UNDO.push(new Snapshot(bounds,WorldEditOperations.copy(world,bounds)));
        while(UNDO.size()>MAX_ENTRIES)UNDO.removeLast();
        REDO.clear();
    }

    public static boolean undo(WorldServer world) {
        if(UNDO.isEmpty())return false;
        Snapshot previous=UNDO.pop();
        REDO.push(new Snapshot(previous.bounds,WorldEditOperations.copy(world,previous.bounds)));
        apply(world,previous);
        return true;
    }

    public static boolean redo(WorldServer world) {
        if(REDO.isEmpty())return false;
        Snapshot next=REDO.pop();
        UNDO.push(new Snapshot(next.bounds,WorldEditOperations.copy(world,next.bounds)));
        apply(world,next);
        return true;
    }

    public static void clear(){UNDO.clear();REDO.clear();}
    public static int undoCount(){return UNDO.size();}
    public static int redoCount(){return REDO.size();}

    private static void apply(WorldServer world,Snapshot snapshot) {
        IBlockState[][][] blocks=snapshot.blocks;
        WorldEditSelection.Bounds b=snapshot.bounds;
        for(int x=0;x<blocks.length;x++)for(int y=0;y<blocks[x].length;y++)for(int z=0;z<blocks[x][y].length;z++)
            world.setBlockState(new BlockPos(b.minX+x,b.minY+y,b.minZ+z),blocks[x][y][z],2);
    }

    private static final class Snapshot {
        private final WorldEditSelection.Bounds bounds;
        private final IBlockState[][][] blocks;
        private Snapshot(WorldEditSelection.Bounds bounds,IBlockState[][][] blocks){this.bounds=bounds;this.blocks=blocks;}
    }
}
