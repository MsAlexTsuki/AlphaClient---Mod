package com.alpharip.staffmod.RecordingStudio.command;

import com.alpharip.staffmod.RecordingStudio.capture.RecordingCaptureService;
import com.alpharip.staffmod.RecordingStudio.capture.RecordingCaptureSettings;
import com.alpharip.staffmod.RecordingStudio.core.RecordingProject;
import com.alpharip.staffmod.RecordingStudio.core.RecordingStudioController;
import com.alpharip.staffmod.RecordingStudio.core.RecordingStudioProjectStore;
import com.alpharip.staffmod.RecordingStudio.integration.MinecraftFramebufferFrameWriter;
import com.alpharip.staffmod.RecordingStudio.integration.RecordingStudioRuntime;
import com.alpharip.staffmod.RecordingStudio.timeline.Timeline;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/** In-game entry point for the Recording Studio feature bundled in the Alpha Recording Studio addon. */
public final class RecordingStudioCommand extends CommandBase {
    private static RecordingStudioRuntime activeRuntime;
    private static String activeProjectId;

    public String getCommandName() { return "recordingstudio"; }
    public String getCommandUsage(ICommandSender sender) {
        return "/recordingstudio [list|create <name>|open <project-id>]";
    }
    public int getRequiredPermissionLevel() { return 0; }

    /** Public entry point used by Alpha Client's optional-addon shortcut. */
    public static void openFromClient() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) return;
        try {
            new RecordingStudioCommand().processCommand(mc.thePlayer, new String[0]);
        } catch (CommandException failure) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    "§6[Recording Studio] §fCould not open: " + failure.getMessage()));
        }
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) {
            sender.addChatMessage(new ChatComponentText("§6[Recording Studio] §fJoin a world before using Recording Studio."));
            return;
        }
        File root = new File(mc.mcDataDir, "recordingstudio");
        File projects = new File(root, "projects");
        RecordingStudioProjectStore store = new RecordingStudioProjectStore(projects);
        String action = args == null || args.length == 0 ? "create" : args[0].toLowerCase(java.util.Locale.ROOT);

        try {
            if ("list".equals(action)) {
                List<RecordingProject> entries = store.list();
                sender.addChatMessage(new ChatComponentText("§6[Recording Studio] §fSaved projects: " + entries.size()));
                for (RecordingProject entry : entries) {
                    sender.addChatMessage(new ChatComponentText("§e" + entry.getId() + " §7- §f" + entry.getName()));
                }
                return;
            }

            RecordingProject project;
            Timeline timeline;
            if ("open".equals(action)) {
                if (args.length != 2) {
                    usage(sender);
                    return;
                }
                project = store.loadProject(args[1]);
                timeline = store.loadTimeline(project.getId());
            } else if ("create".equals(action)) {
                String name = args.length <= 1 ? "Manual Recording" : join(args, 1);
                project = new RecordingProject(name);
                timeline = new Timeline();
                timeline.addTrack(new com.alpharip.staffmod.RecordingStudio.timeline.TimelineTrack("video", "Video"));
                timeline.addTrack(new com.alpharip.staffmod.RecordingStudio.timeline.TimelineTrack("audio", "Audio"));
                store.save(project, timeline);
            } else {
                usage(sender);
                return;
            }

            RecordingStudioRuntime runtime;
            if (activeRuntime != null && !activeRuntime.isClosed()
                    && project.getId().equals(activeProjectId)) {
                runtime = activeRuntime;
            } else {
                if (activeRuntime != null && !activeRuntime.isClosed()) activeRuntime.shutdown();
                File output = new File(new File(root, "recordings"), project.getId() + "/frames");
                RecordingCaptureService capture = new RecordingCaptureService(
                        new MinecraftFramebufferFrameWriter(mc), new RecordingCaptureSettings(), output);
                RecordingStudioController controller =
                        new RecordingStudioController(project, capture, projects, timeline);
                runtime = new RecordingStudioRuntime(controller);
                activeRuntime = runtime;
                activeProjectId = project.getId();
            }
            try {
                runtime.open(mc);
            } catch (RuntimeException failure) {
                runtime.shutdown();
                if (activeRuntime == runtime) {
                    activeRuntime = null;
                    activeProjectId = null;
                }
                throw failure;
            }
        } catch (IOException | RuntimeException failure) {
            sender.addChatMessage(new ChatComponentText("§6[Recording Studio] §fCould not open: " + failure.getMessage()));
        }
    }

    private static String join(String[] values, int start) {
        StringBuilder result = new StringBuilder();
        for (int i = start; i < values.length; i++) {
            if (result.length() > 0) result.append(' ');
            result.append(values[i]);
        }
        return result.toString();
    }

    private static void usage(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText("§6[Recording Studio] §fUsage: /recordingstudio [list|create <name>|open <project-id>]"));
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, net.minecraft.util.BlockPos pos) {
        if (args != null && args.length == 1) {
            return java.util.Arrays.asList("list", "create", "open");
        }
        if (args != null && args.length == 2 && "open".equalsIgnoreCase(args[0])) {
            try {
                java.util.ArrayList<String> ids = new java.util.ArrayList<String>();
                File directory = new File(Minecraft.getMinecraft().mcDataDir, "recordingstudio/projects");
                for (RecordingProject project : new RecordingStudioProjectStore(directory).list()) ids.add(project.getId());
                return ids;
            } catch (IOException ignored) {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }
}
