package com.alpharip.staffmod.developer;

import com.alpharip.staffmod.modules.AlphaModule;
import com.alpharip.staffmod.modules.AlphaModuleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Bounded in-memory diagnostic data for Alpha Developer Studio.
 * This is client-local diagnostic information and does not send data anywhere.
 */
public final class DeveloperDiagnostics {
    private static final int MAX_ENTRIES = 500;
    private static final Deque<String> LOGS = new ArrayDeque<String>();
    private static final Deque<String> EVENTS = new ArrayDeque<String>();
    private static final Deque<String> ERRORS = new ArrayDeque<String>();
    private static final Deque<String> EXCEPTIONS = new ArrayDeque<String>();
    private static final Deque<String> STACK_TRACES = new ArrayDeque<String>();
    private static final long STARTED_AT = System.currentTimeMillis();
    private static long lastTickNanos;
    private static long latestTickNanos;
    private static long maxTickNanos;
    private static long sampledTicks;
    private static boolean testMode;
    private static boolean sandboxMode;
    private static boolean debugOverlayEnabled;
    private static String lastTestReport = "No tests have been run.";
    private static String lastError = "No errors captured by Alpha Developer Studio.";
    private static String lastException = "No exceptions captured by Alpha Developer Studio.";

    private DeveloperDiagnostics() { }

    public static synchronized void initialize() {
        log("INFO", "Developer diagnostics initialized.");
        event("Alpha Client diagnostic service initialized.");
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        long now = System.nanoTime();
        if (lastTickNanos != 0L) {
            latestTickNanos = now - lastTickNanos;
            if (latestTickNanos > maxTickNanos) maxTickNanos = latestTickNanos;
            sampledTicks++;
        }
        lastTickNanos = now;
        if (sampledTicks % 100L == 0L) {
            Minecraft mc = Minecraft.getMinecraft();
            event("Client tick sample; world=" + (mc != null && mc.theWorld != null)
                    + "; player=" + (mc != null && mc.thePlayer != null));
        }
    }

    public static synchronized void log(String level, String message) {
        String line = timestamp() + " [" + safe(level) + "] " + safe(message);
        add(LOGS, line);
        if ("ERROR".equals(level) || "FATAL".equals(level)) {
            lastError = line;
            add(ERRORS, line);
        }
    }

    public static synchronized void captureException(String context, Throwable error) {
        if (error == null) return;
        String heading = timestamp() + " [" + safe(context) + "] "
                + error.getClass().getName() + ": " + safe(error.getMessage());
        lastException = heading;
        add(EXCEPTIONS, heading);
        add(ERRORS, heading);
        StringBuilder trace = new StringBuilder(heading);
        for (StackTraceElement element : error.getStackTrace()) {
            trace.append("\n    at ").append(element.toString());
        }
        add(STACK_TRACES, trace.toString());
        log("ERROR", heading);
    }

    public static synchronized void event(String message) {
        add(EVENTS, timestamp() + " " + safe(message));
    }

    private static void add(Deque<String> target, String value) {
        while (target.size() >= MAX_ENTRIES) target.removeFirst();
        target.addLast(value);
    }

    private static String timestamp() {
        return new SimpleDateFormat("HH:mm:ss.SSS", Locale.ROOT).format(new Date());
    }

    private static String safe(String value) {
        return value == null ? "(none)" : value.replace("\r", " ").replace("\n", " ");
    }

    public static synchronized List<String> logs() { return new ArrayList<String>(LOGS); }
    public static synchronized List<String> events() { return new ArrayList<String>(EVENTS); }
    public static synchronized List<String> errors() { return new ArrayList<String>(ERRORS); }
    public static synchronized List<String> exceptions() { return new ArrayList<String>(EXCEPTIONS); }
    public static synchronized List<String> stackTraces() { return new ArrayList<String>(STACK_TRACES); }

    public static synchronized String profilerReport() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        return "Uptime: " + ((System.currentTimeMillis() - STARTED_AT) / 1000L) + " s\n"
                + "Tick samples: " + sampledTicks + "\n"
                + "Latest client tick: " + (latestTickNanos / 1000000.0) + " ms\n"
                + "Maximum client tick: " + (maxTickNanos / 1000000.0) + " ms\n"
                + "Heap used: " + (used / 1048576L) + " MiB / "
                + (runtime.maxMemory() / 1048576L) + " MiB";
    }

    public static synchronized String moduleReport() {
        StringBuilder out = new StringBuilder();
        for (AlphaModule module : AlphaModuleRegistry.getAll()) {
            out.append(module.getName()).append(" [").append(module.getId()).append("] ")
                    .append(module.getCategory().name()).append(" | implemented=")
                    .append(module.isImplemented()).append(" | unlocked=")
                    .append(module.isUnlocked()).append(" | enabled=")
                    .append(module.isEnabled()).append("\n")
                    .append("  ").append(module.getDescription()).append("\n");
        }
        return out.toString();
    }

    public static synchronized String modReport() {
        StringBuilder out = new StringBuilder();
        try {
            for (ModContainer mod : Loader.instance().getModList()) {
                out.append(mod.getModId()).append(" ").append(mod.getVersion())
                        .append(" — ").append(mod.getName()).append("\n");
            }
        } catch (Throwable error) {
            captureException("Mod Inspector", error);
            out.append("Could not enumerate Forge mods: ").append(error.getMessage());
        }
        return out.toString();
    }

    public static synchronized String dependencyReport() {
        StringBuilder out = new StringBuilder();
        out.append("Alpha module dependency graph\n");
        for (AlphaModule module : AlphaModuleRegistry.getAll()) {
            out.append(module.getId()).append(" -> ");
            // AlphaModule does not currently declare dependency metadata.
            out.append("(no declared dependencies)\n");
        }
        out.append("\nForge mod dependencies are reported by each mod's metadata; this view does not infer undeclared links.");
        return out.toString();
    }

    public static synchronized String configReport() {
        Minecraft mc = Minecraft.getMinecraft();
        File file = new File(new File(mc == null ? new File(".") : mc.mcDataDir, "config"), "alphaclient.json");
        if (!file.isFile()) return "AlphaClient config file not found: " + file.getAbsolutePath();
        StringBuilder out = new StringBuilder("File: ").append(file.getAbsolutePath()).append("\n");
        java.io.BufferedReader reader = null;
        try {
            reader = new java.io.BufferedReader(new java.io.FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) out.append(line).append("\n");
        } catch (Exception error) {
            captureException("Config Inspector", error);
            out.append("Could not read config: ").append(error.getMessage());
        } finally {
            if (reader != null) try { reader.close(); } catch (Exception ignored) { }
        }
        return out.toString();
    }

    public static synchronized String runTests() {
        int total = 0;
        int passed = 0;
        total++; if (Minecraft.getMinecraft() != null) passed++;
        total++; if (AlphaModuleRegistry.getAll() != null) passed++;
        total++; if (AlphaModuleRegistry.find("fps") != null) passed++;
        total++; if (AlphaModuleRegistry.find("cps") != null) passed++;
        total++; if (AlphaModuleRegistry.find("staffmods") != null) passed++;
        total++; if (AlphaModuleRegistry.find("missing-developer-test-module") == null) passed++;
        lastTestReport = "Alpha Client self-tests: " + passed + "/" + total + " passed.\n"
                + "Checks: Minecraft client available; module registry available; FPS/CPS/StaffMods registered; unknown module lookup safely returns null.\n"
                + "These are basic integrity checks, not a replacement for a Gradle build or in-game QA.";
        log(passed == total ? "INFO" : "ERROR", lastTestReport.replace("\n", " "));
        event("Developer Test Mode executed " + total + " checks; passed=" + passed);
        return lastTestReport;
    }

    public static synchronized void setTestMode(boolean enabled) {
        testMode = enabled;
        log("INFO", "Developer Test Mode " + (enabled ? "enabled" : "disabled") + ".");
        event("Test mode changed: " + enabled);
    }
    public static synchronized boolean isTestMode() { return testMode; }

    public static synchronized void setSandboxMode(boolean enabled) {
        sandboxMode = enabled;
        log("INFO", "Developer Sandbox Mode " + (enabled ? "enabled" : "disabled") + ".");
        event("Sandbox mode changed: " + enabled);
    }
    public static synchronized boolean isSandboxMode() { return sandboxMode; }

    public static synchronized void setDebugOverlayEnabled(boolean enabled) {
        debugOverlayEnabled = enabled;
        log("INFO", "Developer Debug Overlay " + (enabled ? "enabled" : "disabled") + ".");
        event("Debug overlay changed: " + enabled);
    }
    public static synchronized boolean isDebugOverlayEnabled() { return debugOverlayEnabled; }
    public static synchronized String profilerOverlayLine() {
        return "Tick: " + (latestTickNanos / 1000000L) + " ms | Heap: "
                + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L) + " MiB";
    }

    public static synchronized String lastTestReport() { return lastTestReport; }

    public static synchronized String errorReport() {
        return "Latest error:\n" + lastError + "\n\nLatest exception:\n" + lastException
                + "\n\nCaptured errors: " + ERRORS.size()
                + "\nCaptured exceptions: " + EXCEPTIONS.size()
                + "\nCaptured stack traces: " + STACK_TRACES.size();
    }

    public static synchronized File exportDiagnosticBundle() throws java.io.IOException {
        Minecraft mc = Minecraft.getMinecraft();
        File base = mc == null ? new File(".") : mc.mcDataDir;
        File dir = new File(base, "logs");
        if (!dir.exists() && !dir.mkdirs()) throw new java.io.IOException("Could not create logs directory.");
        File output = new File(dir, "alphaclient-diagnostic-" + System.currentTimeMillis() + ".txt");
        FileWriter writer = new FileWriter(output);
        try {
            writer.write("Alpha Client Diagnostic Bundle\nGenerated: " + new Date() + "\n\n");
            writer.write("PROFILER\n" + profilerReport() + "\n\n");
            writer.write("MODS\n" + modReport() + "\n\n");
            writer.write("MODULES\n" + moduleReport() + "\n\n");
            writer.write("DEPENDENCIES\n" + dependencyReport() + "\n\n");
            writer.write("CONFIG\n" + configReport() + "\n\n");
            writer.write("LOGS\n" + join(logs()) + "\n\nEVENTS\n" + join(events())
                    + "\n\nERRORS\n" + join(errors()) + "\n\nEXCEPTIONS\n" + join(exceptions())
                    + "\n\nSTACK TRACES\n" + join(stackTraces()));
        } finally {
            writer.close();
        }
        log("INFO", "Diagnostic bundle exported to " + output.getAbsolutePath());
        return output;
    }

    private static String join(List<String> values) {
        StringBuilder out = new StringBuilder();
        for (String value : values) out.append(value).append("\n");
        return out.toString();
    }
}