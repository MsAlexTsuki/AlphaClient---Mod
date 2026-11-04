package com.alpharip.staffmod.waypoints;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/** UTF-8 TSV import/export for moving local waypoints between friends or servers. */
public final class WaypointImportExport {
    private static final String HEADER = "name\tserver\tdimension\tx\ty\tz\tcategory\tcolor\ticon\tdeath\tfavorite\tvisited\n";

    public String exportWaypoints(List<StaffWaypoint> list) {
        StringBuilder out = new StringBuilder(HEADER);
        if (list == null) return out.toString();
        for (StaffWaypoint p : list) {
            if (p == null) continue;
            out.append(enc(p.getLabel())).append('\t').append(enc(p.getServerKey())).append('\t')
                .append(p.getDimensionId()).append('\t').append(p.getX()).append('\t')
                .append(p.getY()).append('\t').append(p.getZ()).append('\t')
                .append(enc(p.getCategory())).append('\t').append(Integer.toHexString(p.getColor()))
                .append('\t').append(enc(p.getIconId())).append('\t').append(p.isDeathWaypoint())
                .append('\t').append(p.isFavorite()).append('\t').append(p.isVisited()).append('\n');
        }
        return out.toString();
    }

    public List<StaffWaypoint> importWaypoints(String text) {
        List<StaffWaypoint> out = new ArrayList<StaffWaypoint>();
        if (text == null || text.trim().isEmpty()) return Collections.unmodifiableList(out);
        String[] lines = text.split("\\r?\\n");
        String firstLine=lines.length==0?"":lines[0].replace("\uFEFF","");
        int first=firstLine.startsWith("name\tserver\tdimension\t")?1:0;
        for (int i=first; i<lines.length; i++) {
            if (lines[i].trim().isEmpty()) continue;
            String[] p=lines[i].split("\\t",-1);
            if (p.length != 9 && p.length != 10 && p.length != 12 && p.length != 13) throw new IllegalArgumentException("Invalid waypoint line " + (i+1));
            try {
                int offset=newFormat?1:0;
                double x = Double.parseDouble(p[3+offset]);
                double y = Double.parseDouble(p[4+offset]);
                double z = Double.parseDouble(p[5+offset]);
                if (Double.isNaN(x) || Double.isInfinite(x)
                        || Double.isNaN(y) || Double.isInfinite(y)
                        || Double.isNaN(z) || Double.isInfinite(z)) {
                    throw new IllegalArgumentException("Coordinates must be finite");
                }
                StaffWaypoint point = new StaffWaypoint(dec(p[0]), dec(p[1]), Integer.parseInt(p[2]), x, y, z);
                if(newFormat) point.setWorldKey(dec(p[3]));
                point.setCategory(dec(p[6+offset]));
                point.setColor((int)Long.parseLong(p[7+offset],16));
                point.setIconId(dec(p[8+offset]));
                if (p.length >= 10+offset) {
                    if (!validBoolean(p[9+offset])) throw new IllegalArgumentException("Invalid death waypoint flag");
                    point.setDeathWaypoint(Boolean.parseBoolean(p[9+offset]));
                }
                if(p.length==12+offset) {
                    if(!validBoolean(p[10+offset])||!validBoolean(p[11+offset]))
                        throw new IllegalArgumentException("Invalid waypoint state flag");
                    point.setFavorite(Boolean.parseBoolean(p[10+offset]));
                    point.setVisited(Boolean.parseBoolean(p[11+offset]));
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
        if (file == null) throw new IOException("Export file is missing.");
        File absolute = file.getAbsoluteFile();
        File parent = absolute.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.isDirectory())
            throw new IOException("Could not create export directory.");
        File temporary = new File(parent, absolute.getName() + ".tmp");
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(temporary), StandardCharsets.UTF_8);
            writer.write(exportWaypoints(points));
            writer.close();
            writer = null;
            WaypointFileUtils.replaceAtomically(temporary,absolute);
        } finally {
            if (writer != null) writer.close();
        }
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
    private static boolean validBoolean(String value) {
        return "true".equalsIgnoreCase(value)||"false".equalsIgnoreCase(value);
    }
    private static String enc(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                (value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    }
    private static String dec(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
