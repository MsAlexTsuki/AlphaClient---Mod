package com.alpharip.staffmod.WayPoints;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Prepares a portable local file to share with friends. It does not upload,
 * message other users, or connect to a service.
 */
public final class ShareWaypointsWithFriends {
    private final WaypointImportExport codec = new WaypointImportExport();
    public File exportForSharing(List<StaffWaypoint> waypoints) throws IOException {
        File file = codec.defaultFile();
        codec.write(file, waypoints);
        return file;
    }
    public List<StaffWaypoint> importSharedFile(File file) throws IOException {
        return codec.read(file);
    }
}
