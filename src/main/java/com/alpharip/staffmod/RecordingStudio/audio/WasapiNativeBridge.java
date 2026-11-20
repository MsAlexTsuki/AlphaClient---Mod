package com.alpharip.staffmod.RecordingStudio.audio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Loads the packaged Windows x64 WASAPI bridge without installing a driver or changing system settings. */
final class WasapiNativeBridge {
    private static volatile boolean loaded;

    private WasapiNativeBridge() { }

    static synchronized void ensureLoaded() throws IOException {
        if (loaded) return;
        String os = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(java.util.Locale.ROOT);
        if (!os.contains("win")) throw new IOException("WASAPI game-audio capture is available only on Windows");
        if (!("amd64".equals(arch) || "x86_64".equals(arch))) {
            throw new IOException("WASAPI game-audio capture requires 64-bit Windows");
        }

        File directory = new File(new File(System.getProperty("java.io.tmpdir"), "AlphaRecordingStudio"), "native");
        if ((!directory.exists() && !directory.mkdirs()) || !directory.isDirectory()) {
            throw new IOException("Cannot create the temporary WASAPI native-library directory");
        }
        File library = File.createTempFile("alpha_wasapi-", ".dll", directory);
        boolean copied = false;
        try (InputStream input = WasapiNativeBridge.class.getResourceAsStream(
                "/native/windows-x86_64/alpha_wasapi.dll");
             FileOutputStream output = new FileOutputStream(library)) {
            if (input == null) throw new IOException("The packaged WASAPI native library is missing");
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) {
                if (count > 0) output.write(buffer, 0, count);
            }
            output.getFD().sync();
            copied = true;
        } finally {
            if (!copied && library.exists() && !library.delete()) library.deleteOnExit();
        }

        try {
            System.load(library.getAbsolutePath());
            library.deleteOnExit();
            loaded = true;
        } catch (UnsatisfiedLinkError failure) {
            if (library.exists() && !library.delete()) library.deleteOnExit();
            throw new IOException("Could not load the WASAPI native library: " + failure.getMessage(), failure);
        }
    }

    static native void nativeReset();
    static native long nativeCapture(String temporaryPath, String outputPath) throws IOException;
    static native void nativeStop();
}
