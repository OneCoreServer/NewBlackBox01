package com.onecore.loader.utils;

import java.io.IOException;

public final class SafeExec {
    private SafeExec() {
    }

    public static int safeExec(String command) {
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
            pb.redirectErrorStream(true);
            process = pb.start();
            return process.waitFor();
        } catch (Exception e) {
            FLog.error("SafeExec failed: " + e.getMessage());
            return -1;
        } finally {
            if (process != null) {
                closeQuietly(process.getInputStream());
                closeQuietly(process.getOutputStream());
                closeQuietly(process.getErrorStream());
                process.destroy();
            }
        }
    }

    private static void closeQuietly(java.io.Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }
}
