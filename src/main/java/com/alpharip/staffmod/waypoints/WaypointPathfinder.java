package com.alpharip.staffmod.waypoints;

import net.minecraft.client.Minecraft;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.pathfinder.WalkNodeProcessor;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Calculates a terrain-aware walking route to the selected waypoint.
 * The vanilla 1.8.9 walk node processor accounts for player-sized clearance,
 * walkable ground, one-block step/jump transitions and water navigation.
 */
public final class WaypointPathfinder {
    private static final WaypointPathfinder INSTANCE = new WaypointPathfinder();
    private static final float SEARCH_RANGE = 128.0F;
    private static final int REPATH_INTERVAL_TICKS = 20;
    private static final double REPATH_DISTANCE_SQUARED = 16.0D;

    private List<Vec3> pathPoints = Collections.emptyList();
    private String targetId = "";
    private World lastWorld;
    private int lastDimension = Integer.MIN_VALUE;
    private double lastX;
    private double lastY;
    private double lastZ;
    private int ticksSincePath = REPATH_INTERVAL_TICKS;

    private WaypointPathfinder() { }

    public static WaypointPathfinder getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) {
            clear();
            return;
        }

        StaffWaypoint target = WaypointManager.getInstance().getSelected();
        if (target == null || !ServerWaypointIntegration.belongsToCurrentWorld(target, mc)) {
            clear();
            return;
        }

        ticksSincePath++;
        double dx = mc.thePlayer.posX - lastX;
        double dy = mc.thePlayer.posY - lastY;
        double dz = mc.thePlayer.posZ - lastZ;
        boolean targetChanged = !target.getId().equals(targetId)
                || lastWorld != mc.theWorld
                || lastDimension != mc.thePlayer.dimension;
        boolean playerMoved = dx * dx + dy * dy + dz * dz >= REPATH_DISTANCE_SQUARED;

        if (targetChanged || playerMoved || ticksSincePath >= REPATH_INTERVAL_TICKS) {
            recalculate(mc, target);
        }
    }

    private void recalculate(Minecraft mc, StaffWaypoint target) {
        ticksSincePath = 0;
        targetId = target.getId();
        lastWorld = mc.theWorld;
        lastDimension = mc.thePlayer.dimension;
        lastX = mc.thePlayer.posX;
        lastY = mc.thePlayer.posY;
        lastZ = mc.thePlayer.posZ;

        try {
            WalkNodeProcessor processor = new WalkNodeProcessor();
            processor.setEnterDoors(true);
            processor.setCanSwim(true);

            PathFinder finder = new PathFinder(processor);
            BlockPos destination = new BlockPos(
                    Math.floor(target.getX()),
                    Math.floor(target.getY()),
                    Math.floor(target.getZ())
            );
            PathEntity calculated = finder.createEntityPathTo(
                    mc.theWorld, mc.thePlayer, destination, SEARCH_RANGE
            );

            if (calculated == null || calculated.getCurrentPathLength() < 2) {
                pathPoints = Collections.emptyList();
                return;
            }

            List<Vec3> calculatedPoints = new ArrayList<Vec3>();
            for (int i = 0; i < calculated.getCurrentPathLength(); i++) {
                PathPoint point = calculated.getPathPointFromIndex(i);
                calculatedPoints.add(new Vec3(
                        point.xCoord + 0.5D,
                        point.yCoord + 0.05D,
                        point.zCoord + 0.5D
                ));
            }

            PathPoint lastPoint = calculated.getPathPointFromIndex(calculated.getCurrentPathLength() - 1);
            double endX = lastPoint.xCoord + 0.5D;
            double endY = lastPoint.yCoord;
            double endZ = lastPoint.zCoord + 0.5D;
            double endDx = endX - target.getX();
            double endDy = endY - target.getY();
            double endDz = endZ - target.getZ();
            double endDistanceSquared = endDx * endDx + endDy * endDy + endDz * endDz;

            double startDx = mc.thePlayer.posX - target.getX();
            double startDy = mc.thePlayer.posY - target.getY();
            double startDz = mc.thePlayer.posZ - target.getZ();
            double startDistanceSquared = startDx * startDx + startDy * startDy + startDz * startDz;

            // PathFinder may return the closest reachable partial path when the
            // destination is outside its current search window. Keep it only
            // when it makes measurable progress toward the selected waypoint.
            if (endDistanceSquared >= startDistanceSquared - 1.0D) {
                pathPoints = Collections.emptyList();
                return;
            }

            pathPoints = Collections.unmodifiableList(calculatedPoints);
        } catch (RuntimeException exception) {
            pathPoints = Collections.emptyList();
            System.err.println("[AlphaClient] Waypoint route calculation failed: " + exception.getMessage());
        }
    }

    private void clear() {
        if (!pathPoints.isEmpty() || !targetId.isEmpty()) {
            pathPoints = Collections.emptyList();
            targetId = "";
            lastWorld = null;
            lastDimension = Integer.MIN_VALUE;
            ticksSincePath = REPATH_INTERVAL_TICKS;
        }
    }

    public List<Vec3> getPathPoints() {
        return pathPoints;
    }
}
