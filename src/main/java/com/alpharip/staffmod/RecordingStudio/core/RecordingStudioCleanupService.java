package com.alpharip.staffmod.RecordingStudio.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Preview-first cleanup for old Recording Studio media; project metadata is never deleted. */
public final class RecordingStudioCleanupService {
    public static final class CleanupPlan {
        private final File root;
        private final long cutoffMillis;
        private final int retentionDays;
        private final List<File> files;
        private final long bytes;

        private CleanupPlan(File root, long cutoffMillis, int retentionDays, List<File> files, long bytes) {
            this.root = root;
            this.cutoffMillis = cutoffMillis;
            this.retentionDays = retentionDays;
            this.files = Collections.unmodifiableList(new ArrayList<File>(files));
            this.bytes = bytes;
        }

        public int getRetentionDays() { return retentionDays; }
        public int getFileCount() { return files.size(); }
        public long getBytes() { return bytes; }
    }

    public static final class CleanupResult {
        private final int deletedFiles;
        private final long deletedBytes;
        private CleanupResult(int deletedFiles, long deletedBytes) {
            this.deletedFiles = deletedFiles;
            this.deletedBytes = deletedBytes;
        }
        public int getDeletedFiles() { return deletedFiles; }
        public long getDeletedBytes() { return deletedBytes; }
    }

    private RecordingStudioCleanupService() { }

    /** Scans only old media inside recordings/ and exports/, preserving active or unreadable projects. */
    public static CleanupPlan preview(File root, int retentionDays, long now) throws IOException {
        if (root == null || retentionDays < 1) throw new IllegalArgumentException("A root and positive retention period are required");
        if (!root.exists() || !root.isDirectory()) throw new IOException("Recording Studio storage directory is unavailable");
        String rootPath = root.getCanonicalPath() + File.separator;
        long ageMillis = retentionDays * 24L * 60L * 60L * 1000L;
        long cutoff = now < ageMillis ? 0L : now - ageMillis;
        List<File> candidates = new ArrayList<File>();
        long[] bytes = new long[] { 0L };
        File projects = new File(root, "projects");
        scanCategory(new File(root, "recordings"), projects, rootPath, cutoff, candidates, bytes);
        scanCategory(new File(root, "exports"), projects, rootPath, cutoff, candidates, bytes);
        return new CleanupPlan(root, cutoff, retentionDays, candidates, bytes[0]);
    }

    /** Revalidates every candidate immediately before deleting it. */
    public static CleanupResult clean(CleanupPlan plan) throws IOException {
        if (plan == null) throw new IllegalArgumentException("Cleanup preview is required");
        String rootPath = plan.root.getCanonicalPath() + File.separator;
        int deleted = 0;
        long bytes = 0L;
        for (File file : plan.files) {
            String canonical = file.getCanonicalPath();
            if (!canonical.startsWith(rootPath)) continue;
            String relative = canonical.substring(rootPath.length());
            int firstSeparator = relative.indexOf(File.separatorChar);
            int secondSeparator = firstSeparator < 0 ? -1 : relative.indexOf(File.separatorChar, firstSeparator + 1);
            if (firstSeparator < 0) continue;
            String category = relative.substring(0, firstSeparator);
            String projectId = relative.substring(firstSeparator + 1,
                    secondSeparator < 0 ? relative.length() : secondSeparator);
            if (!"recordings".equals(category) && !"exports".equals(category)) continue;
            if (!isSafeCandidate(file, new File(plan.root, "projects"), projectId, rootPath, plan.cutoffMillis)) continue;
            long length = Math.max(0L, file.length());
            if (file.delete()) {
                deleted++;
                bytes = safeAdd(bytes, length);
            }
        }
        return new CleanupResult(deleted, bytes);
    }

    private static void scanCategory(File category, File projects, String rootPath, long cutoff,
                                     List<File> candidates, long[] bytes) throws IOException {
        if (!category.exists()) return;
        if (Files.isSymbolicLink(category.toPath())) return;
        File[] projectDirs = category.listFiles();
        if (projectDirs == null) throw new IOException("Cannot inspect " + category.getName() + " storage");
        for (File projectDir : projectDirs) {
            if (!projectDir.isDirectory() || Files.isSymbolicLink(projectDir.toPath())) continue;
            String projectId = projectDir.getName();
            if (!projectId.matches("[A-Za-z0-9_-]{1,80}") || isProtectedProject(projects, projectId)) continue;
            scanFiles(projectDir, projects, projectId, rootPath, cutoff, candidates, bytes);
        }
    }

    private static void scanFiles(File directory, File projects, String projectId, String rootPath, long cutoff,
                                  List<File> candidates, long[] bytes) throws IOException {
        if (isProtectedProject(projects, projectId) || Files.isSymbolicLink(directory.toPath())) return;
        File[] children = directory.listFiles();
        if (children == null) throw new IOException("Cannot inspect project media directory");
        for (File child : children) {
            if (Files.isSymbolicLink(child.toPath())) continue;
            if (child.isDirectory()) {
                scanFiles(child, projects, projectId, rootPath, cutoff, candidates, bytes);
            } else if (child.isFile() && isSafeCandidate(child, projects, projectId, rootPath, cutoff)) {
                candidates.add(child);
                bytes[0] = safeAdd(bytes[0], Math.max(0L, child.length()));
            }
        }
    }

    private static boolean isSafeCandidate(File file, File projects, String projectId,
                                            String rootPath, long cutoff) throws IOException {
        if (file == null || !projectId.matches("[A-Za-z0-9_-]{1,80}")
                || isProtectedProject(projects, projectId)
                || !file.isFile() || Files.isSymbolicLink(file.toPath())) return false;
        String canonical = file.getCanonicalPath();
        if (!canonical.startsWith(rootPath)) return false;
        String name = file.getName().toLowerCase(java.util.Locale.ROOT);
        if (!(name.endsWith(".png") || name.endsWith(".wav") || name.endsWith(".mp4")
                || name.endsWith(".gif") || name.endsWith(".time") || name.endsWith(".tmp"))) return false;
        long modified = file.lastModified();
        return modified > 0L && modified <= cutoff;
    }

    private static boolean isProtectedProject(File projects, String projectId) {
        if (new File(projects, projectId + ".active").exists()) return true;
        File metadata = new File(projects, projectId + ".recording");
        if (!metadata.isFile()) return true;
        try {
            return RecordingProject.load(projects, projectId).isRecording();
        } catch (IOException invalidOrUnreadable) {
            return true;
        }
    }

    private static long safeAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }
}
