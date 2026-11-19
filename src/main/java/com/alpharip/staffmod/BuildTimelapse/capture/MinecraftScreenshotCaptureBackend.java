package com.alpharip.staffmod.BuildTimelapse.capture;

import com.alpharip.staffmod.BuildTimelapse.integration.MinecraftScreenshotBridge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Captures the framebuffer at the correct render stage: after world rendering
 * when the HUD is hidden, or after the HUD when it is visible.
 */
public final class MinecraftScreenshotCaptureBackend implements CaptureBackend {
    private static final long CAPTURE_TIMEOUT_SECONDS = 30L;

    private final MinecraftScreenshotBridge bridge;
    private final RenderThreadExecutor executor;

    public MinecraftScreenshotCaptureBackend(MinecraftScreenshotBridge bridge, RenderThreadExecutor executor) {
        if (bridge == null || executor == null) throw new IllegalArgumentException("bridge/executor");
        this.bridge = bridge;
        this.executor = executor;
    }

    @Override
    public CaptureFrame capture(final long index, final long timestamp, final File output,
                                final CaptureSettings settings) throws Exception {
        if (output == null || settings == null) {
            throw new IllegalArgumentException("output/settings");
        }

        final PendingCapture pending = new PendingCapture(index, timestamp, output, settings);
        try {
            executor.call(new java.util.concurrent.Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    pending.prepare();
                    return null;
                }
            });

            if (!pending.await()) {
                pending.cancel();
                throw new IOException("Timed out waiting for the Minecraft render event");
            }
            if (pending.getFailure() != null) throw pending.getFailure();
            byte[] pixels = pending.getPixels();
            if (pixels == null) throw new IOException("Minecraft render event produced no framebuffer pixels");
            int width = pending.getWidth();
            int height = pending.getHeight();
            bridge.writePng(output, width, height, pixels);
            if (!output.isFile() || output.length() == 0L) {
                throw new IOException("Framebuffer adapter did not write a PNG frame");
            }
            return new CaptureFrame(index, timestamp, output.getAbsolutePath(),
                    width, height, output.length());
        } catch (InterruptedException interrupted) {
            try { pending.cancel(); }
            catch (Exception cleanup) { interrupted.addSuppressed(cleanup); }
            Thread.currentThread().interrupt();
            throw interrupted;
        } catch (Exception failure) {
            try { pending.cancel(); }
            catch (Exception cleanup) { failure.addSuppressed(cleanup); }
            throw failure;
        }
    }

    public final class PendingCapture {
        private final long index;
        private final long timestamp;
        private final File output;
        private final CaptureSettings settings;
        private final CountDownLatch completed = new CountDownLatch(1);
        private final AtomicBoolean finished = new AtomicBoolean();
        private volatile byte[] pixels;
        private volatile int width;
        private volatile int height;
        private volatile Exception failure;
        private boolean oldHud;
        private boolean oldHand;
        private boolean oldHudCaptured;
        private boolean oldHandCaptured;
        private volatile boolean registered;

        private PendingCapture(long index, long timestamp, File output, CaptureSettings settings) {
            this.index = index;
            this.timestamp = timestamp;
            this.output = output;
            this.settings = settings;
        }

        private void prepare() throws Exception {
            oldHud = bridge.isHudHidden();
            oldHudCaptured = true;
            oldHand = bridge.isHandHidden();
            oldHandCaptured = true;

            int nativeWidth = bridge.getDisplayWidth();
            int nativeHeight = bridge.getDisplayHeight();
            int width = settings.getWidth() == 0 ? nativeWidth : settings.getWidth();
            int height = settings.getHeight() == 0 ? nativeHeight : settings.getHeight();
            if (nativeWidth < 1 || nativeHeight < 1 || width != nativeWidth || height != nativeHeight) {
                throw new UnsupportedOperationException(
                        "Build Timelapse currently supports native framebuffer resolution only");
            }

            File parent = output.getParentFile();
            if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                throw new IOException("Cannot create frame directory: " + parent);
            }

            try {
                bridge.setHudHidden(settings.isHideHud());
                bridge.setHandHidden(settings.isHideHand());
                registered = true;
                MinecraftForge.EVENT_BUS.register(this);
            } catch (Exception setupFailure) {
                cleanupOnRenderThread();
                if (failure != null) setupFailure.addSuppressed(failure);
                throw setupFailure;
            }
        }

        @SubscribeEvent
        public void onWorldRender(RenderWorldLastEvent event) {
            if (settings.isHideHud()) captureNow();
        }

        @SubscribeEvent
        public void onOverlay(RenderGameOverlayEvent.Post event) {
            if (!settings.isHideHud() && event.type == RenderGameOverlayEvent.ElementType.ALL) {
                captureNow();
            }
        }

        @SubscribeEvent
        public void onWorldUnload(WorldEvent.Unload event) {
            if (event.world != null && event.world.isRemote) {
                failCapture(new IOException("World unloaded before the frame could be captured"));
            }
        }

        private void failCapture(Exception captureFailure) {
            if (!finished.compareAndSet(false, true)) return;
            failure = captureFailure;
            cleanupOnRenderThread();
            completed.countDown();
        }

        private void captureNow() {
            if (!finished.compareAndSet(false, true)) return;
            try {
                width = bridge.getDisplayWidth();
                height = bridge.getDisplayHeight();
                pixels = bridge.readFramebufferPixels(width, height);
            } catch (Exception captureFailure) {
                failure = captureFailure;
            } finally {
                cleanupOnRenderThread();
                completed.countDown();
            }
        }

        private void cleanupOnRenderThread() {
            if (registered) {
                MinecraftForge.EVENT_BUS.unregister(this);
                registered = false;
            }
            if (oldHandCaptured) {
                try {
                    bridge.setHandHidden(oldHand);
                } catch (Exception restoreHandFailure) {
                    recordCleanupFailure(restoreHandFailure);
                } finally {
                    oldHandCaptured = false;
                }
            }
            if (oldHudCaptured) {
                try {
                    bridge.setHudHidden(oldHud);
                } catch (Exception restoreHudFailure) {
                    recordCleanupFailure(restoreHudFailure);
                } finally {
                    oldHudCaptured = false;
                }
            }
        }

        private void recordCleanupFailure(Exception cleanupFailure) {
            if (failure == null) failure = cleanupFailure;
            else failure.addSuppressed(cleanupFailure);
        }

        private boolean await() throws InterruptedException {
            return completed.await(CAPTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }

        private void cancel() throws Exception {
            executor.call(new java.util.concurrent.Callable<Void>() {
                @Override
                public Void call() {
                    if (finished.compareAndSet(false, true)) {
                        cleanupOnRenderThread();
                        completed.countDown();
                    }
                    return null;
                }
            });
        }

        private byte[] getPixels() { return pixels; }
        private int getWidth() { return width; }
        private int getHeight() { return height; }
        private Exception getFailure() { return failure; }
    }
}
