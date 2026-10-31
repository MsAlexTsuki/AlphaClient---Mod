package com.alpharip.staffmod.WayPoints;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/** UTF-8 TSV import/export for moving local waypoints between friends or servers. */
public final class WaypointImportExport {
    private static final String HEADER = "name\tserver\tdimension\tx\ty\tz\tcategory\tcolor\ticon\tdeath\n";

    public String exportWaypoints(List<StaffWaypoint> list) {
        StringBuilder out = new StringBuilder(HEADER);
        if (list == null) return out.toString();
        for (StaffWaypoint p : list) {
            if (p == null) continue;
            out.append(enc(p.getLabel())).append('\t').append(enc(p.getServerKey())).append('\t')
                .append(p.getDimensionId()).append('\t').append(p.getX()).append('\t')
                .append(p.getY()).append('\t').append(p.getZ()).append('\t')
                .append(enc(p.getCategory())).append('\t').append(Integer.toHexString(p.getColor()))
                .append('\t').append(enc(p.getIconId())).append('\t').append(p.isDeathWaypoint()).append('\n');
        }
        return out.toString();
    }

    public List<StaffWaypoint> importWaypoints(String text) {
        List<StaffWaypoint> out = new ArrayList<StaffWaypoint>();
        if (text == null || text.trim().isEmpty()) return Collections.unmodifiableList(out);
        String[] lines = text.split("\\r?\\n");
        int first = lines.length > 0 && lines[0].startsWith("name") ? 1 : 0;
        for (int i=first; i<lines.length; i++) {
            if (lines[i].trim().isEmpty()) continue;
            String[] p=lines[i].split("\\t",-1);
            if (p.length != 9 && p.length != 10) throw new IllegalArgumentException("Invalid waypoint line " + (i+1));
            try {
                double x = Double.parseDouble(p[3]);
                double y = Double.parseDouble(p[4]);
                double z = Double.parseDouble(p[5]);
                if (Double.isNaN(x) || Double.isInfinite(x)
                        || Double.isNaN(y) || Double.isInfinite(y)
                        || Double.isNaN(z) || Double.isInfinite(z)) {
                    throw new IllegalArgumentException("Coordinates must be finite");
                }
                StaffWaypoint point = new StaffWaypoint(dec(p[0]), dec(p[1]), Integer.parseInt(p[2]), x, y, z);
                point.setCategory(dec(p[6]));
                point.setColor((int)Long.parseLong(p[7],16));
                point.setIconId(dec(p[8]));
                if (p.length == 10) {
                    if (!"true".equalsIgnoreCase(p[9]) && !"false".equalsIgnoreCase(p[9]))
                        throw new IllegalArgumentException("Invalid death waypoint flag");
                    point.setDeathWaypoint(Boolean.parseBoolean(p[9]));
                }
                out.add(point);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Invalid waypoint data on line " + (i+1), e);
            }
        }
        return Collections.unmodifiableList(out);
    }

    public File defaultFile() {
        File base = new File(net.minecraft.client.Minecraft.getMinecraft().mcDataDir, "config/alphaclient");
        if (!base.exists()) base.mkdirs();
        return new File(base, "waypoints-share.tsv");
    }
    public void write(File file, List<StaffWaypoint> points) throws IOException {
        Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        try { writer.write(exportWaypoints(points)); } finally { writer.close(); }
    }
    public List<StaffWaypoint> read(File file) throws IOException {
        if (file == null || !file.isFile()) return Collections.emptyList();
        Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        try {
            StringBuilder text = new StringBuilder(); char[] buf = new char[2048]; int n;
            while ((n=reader.read(buf)) >= 0) text.append(buf,0,n);
            return importWaypoints(text.toString());
        } finally { reader.close(); }
    }
    private static String enc(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                (value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    }
    private static String dec(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
