// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.AlphaClans;

import com.alpharip.staffmod.permissions.SupabaseCommunityService;
import com.google.gson.JsonObject;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

/** Java 8 WebSocket transport for authenticated Supabase Realtime Broadcast channels. */
public final class ClanRealtimeClient {
    public static final ClanRealtimeClient INSTANCE = new ClanRealtimeClient();
    private static final String HOST = "lescvpixttmmbnaluidd.supabase.co";
    private static final String KEY = "sb_publishable_DEeIl6IL4mPXiEx6SaJ3EQ_OvNhEPP8";
    private final AtomicBoolean active = new AtomicBoolean(false);
    private volatile SSLSocket socket;
    private volatile String clanId;
    private ClanRealtimeClient() {}

    public synchronized void subscribe(String id) {
        if (id == null) { disconnect(); return; }
        if (id.equals(clanId) && active.get()) return;
        disconnect(); clanId = id; active.set(true);
        Thread t = new Thread(() -> run(id), "AlphaClient-ClanRealtime"); t.setDaemon(true); t.start();
    }
    public synchronized void disconnect() {
        active.set(false); clanId = null;
        SSLSocket old = socket; socket = null; if (old != null) try { old.close(); } catch (Exception ignored) {}
    }
    private void run(String id) {
        long delay = 1000L;
        while (active.get() && id.equals(clanId)) {
            try { connect(id); delay = 1000L; }
            catch (Exception e) { if (active.get()) System.err.println("[AlphaClient] Clan Realtime retry: " + e.getMessage()); }
            if (active.get() && id.equals(clanId)) {
                try { Thread.sleep(delay); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                delay = Math.min(30000L, delay * 2L);
            }
        }
    }
    private void connect(String id) throws Exception {
        String token = SupabaseCommunityService.getClanAccessToken(); if (token == null) return;
        SSLSocket s = (SSLSocket) SSLSocketFactory.getDefault().createSocket(HOST, 443); socket = s;
        s.setKeepAlive(true); s.setSoTimeout(1000); s.startHandshake();
        String key = Base64.getEncoder().encodeToString(randomBytes(16));
        OutputStream out = s.getOutputStream();
        String request = "GET /realtime/v1/websocket?apikey=" + KEY + "&vsn=1.0.0 HTTP/1.1\r\nHost: " + HOST
                + "\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Key: " + key
                + "\r\nSec-WebSocket-Version: 13\r\nOrigin: https://" + HOST + "\r\n\r\n";
        out.write(request.getBytes(StandardCharsets.UTF_8)); out.flush();
        BufferedInputStream in = new BufferedInputStream(s.getInputStream());
        StringBuilder headers = new StringBuilder();
        while (!headers.toString().endsWith("\r\n\r\n")) {
            int b = in.read(); if (b < 0 || headers.length() > 8192) throw new java.io.IOException("Realtime handshake failed.");
            headers.append((char)b);
        }
        String accept = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1")
                .digest((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8)));
        if (!headers.toString().startsWith("HTTP/1.1 101") || !headers.toString().contains(accept))
            throw new java.io.IOException("Realtime server rejected the WebSocket upgrade.");

        JsonObject broadcast = new JsonObject(); broadcast.addProperty("ack", false); broadcast.addProperty("self", false);
        JsonObject presence = new JsonObject(); presence.addProperty("key", "");
        JsonObject config = new JsonObject(); config.add("broadcast", broadcast); config.add("presence", presence); config.addProperty("private", true);
        JsonObject payload = new JsonObject(); payload.add("config", config); payload.addProperty("access_token", token);
        JsonObject join = envelope("realtime:clan:" + id, "phx_join", payload, "1"); join.addProperty("join_ref", "1");
        send(out, join.toString());
        long heartbeat = System.currentTimeMillis();
        while (active.get() && id.equals(clanId) && !s.isClosed()) {
            if (System.currentTimeMillis() - heartbeat > 20000L) {
                send(out, envelope("phoenix", "heartbeat", new JsonObject(), String.valueOf(System.nanoTime())).toString());
                heartbeat = System.currentTimeMillis();
                String next = SupabaseCommunityService.getClanAccessToken();
                if (next != null && !next.equals(token)) {
                    JsonObject auth = new JsonObject(); auth.addProperty("access_token", next);
                    send(out, envelope("realtime:clan:" + id, "access_token", auth, String.valueOf(System.nanoTime())).toString());
                    token = next;
                }
            }
            try {
                String frame = readFrame(in, out);
                if (frame != null) {
                    if (frame.contains("\"event\":\"phx_error\"")
                            || frame.contains("\"event\":\"phx_close\"")
                            || (frame.contains("\"event\":\"phx_reply\"")
                            && frame.contains("\"status\":\"error\""))) {
                        throw new java.io.IOException("Supabase rejected the private clan channel.");
                    }
                    if (frame.contains("\"type\":\"broadcast\"")) AlphaClanService.INSTANCE.refresh();
                }
            } catch (java.net.SocketTimeoutException ignored) {}
        }
    }
    private static JsonObject envelope(String topic, String event, JsonObject payload, String ref) {
        JsonObject o = new JsonObject(); o.addProperty("topic", topic); o.addProperty("event", event);
        o.add("payload", payload); o.addProperty("ref", ref); return o;
    }
    private static byte[] randomBytes(int n) { byte[] b = new byte[n]; new SecureRandom().nextBytes(b); return b; }
    private static void send(OutputStream out, String text) throws Exception {
        byte[] data = text.getBytes(StandardCharsets.UTF_8), mask = randomBytes(4);
        out.write(0x81);
        if (data.length < 126) out.write(0x80 | data.length);
        else { out.write(0x80 | 126); out.write((data.length >>> 8) & 255); out.write(data.length & 255); }
        out.write(mask); for (int i=0;i<data.length;i++) out.write(data[i] ^ mask[i & 3]); out.flush();
    }
    private static String readFrame(InputStream in, OutputStream out) throws Exception {
        int first=in.read(); if(first<0) throw new java.io.IOException("Realtime socket closed.");
        int second=in.read(); if(second<0) throw new java.io.IOException("Incomplete Realtime frame.");
        int opcode=first & 15; long length=second & 127;
        if(length==126) length=((long)in.read()<<8)|(in.read()&255);
        else if(length==127) { length=0; for(int i=0;i<8;i++) length=(length<<8)|(in.read()&255); }
        if(length>1048576) throw new java.io.IOException("Realtime frame too large.");
        boolean masked=(second&128)!=0; byte[] mask=masked?new byte[4]:null;
        if(masked) readFully(in,mask);
        byte[] data=new byte[(int)length]; readFully(in,data);
        if(masked) for(int i=0;i<data.length;i++) data[i]=(byte)(data[i]^mask[i&3]);
        if(opcode==8) throw new java.io.IOException("Realtime channel closed.");
        if(opcode==9) { sendControl(out,10,data); return null; }
        return opcode==1 ? new String(data,StandardCharsets.UTF_8) : null;
    }
    private static void sendControl(OutputStream out,int opcode,byte[] data) throws Exception {
        byte[] mask=randomBytes(4); out.write(0x80|opcode); out.write(0x80|data.length); out.write(mask);
        for(int i=0;i<data.length;i++) out.write(data[i]^mask[i&3]); out.flush();
    }
    private static void readFully(InputStream in,byte[] data) throws Exception {
        int p=0; while(p<data.length) { int n=in.read(data,p,data.length-p); if(n<0) throw new java.io.IOException("Incomplete Realtime frame."); p+=n; }
    }
}
