package com.alpharip.staffmod.BuildTimelapse.capture;

import com.alpharip.staffmod.BuildTimelapse.integration.MinecraftScreenshotBridge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
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
            CaptureFrame frame = pending.getFrame();
            if (frame == null) throw new IOException("Minecraft render event produced no capture frame");
            return frame;
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
        private volatile CaptureFrame frame;
        private volatile Exception failure;
        private boolean oldHud;
        private boolean oldHand;
        private volatile boolean registered;

        private PendingCapture(long index, long timestamp, File output, CaptureSettings settings) {
            this.index = index;
            this.timestamp = timestamp;
            this.output = output;
            this.settings = settings;
        }

        private void prepare() throws Exception {
            oldHud = bridge.isHudHidden();
            oldHand = bridge.isHandHidden();

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
                MinecraftForge.EVENT_BUS.register(this);
                registered = true;
            } catch (Exception setupFailure) {
                try { bridge.setHandHidden(oldHand); }
                catch (Exception cleanup) { setupFailure.addSuppressed(cleanup); }
                try { bridge.setHudHidden(oldHud); }
                catch (Exception cleanup) { setupFailure.addSuppressed(cleanup); }
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

        private void captureNow() {
            if (!finished.compareAndSet(false, true)) return;
            try {
                int width = bridge.getDisplayWidth();
                int height = bridge.getDisplayHeight();
                bridge.captureFramebuffer(output, width, height);
                if (!output.isFile() || output.length() == 0L) {
                    throw new IOException("Framebuffer adapter did not write a PNG frame");
                }
                frame = new CaptureFrame(index, timestamp, output.getAbsolutePath(),
                        width, height, output.length());
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
            try {
                bridge.setHandHidden(oldHand);
            } catch (Exception restoreHandFailure) {
                recordCleanupFailure(restoreHandFailure);
            }
            try {
                bridge.setHudHidden(oldHud);
            } catch (Exception restoreHudFailure) {
                recordCleanupFailure(restoreHudFailure);
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

        private CaptureFrame getFrame() { return frame; }
        private Exception getFailure() { return failure; }
    }
}
