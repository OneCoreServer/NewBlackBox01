package com.onecore.loader.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileHelper {
    private final Context context;

    public FileHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public File getSafeLibDir() {
        File libDir = new File(context.getFilesDir(), "libs");
        if (!libDir.exists() && !libDir.mkdirs()) {
            FLog.error("Failed to create safe lib dir: " + libDir.getAbsolutePath());
        }
        return libDir;
    }

    public boolean copyAssetToPrivate(String assetName, String outName) {
        File outFile = new File(getSafeLibDir(), outName);
        try (InputStream is = context.getAssets().open(assetName);
             FileOutputStream fos = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            if (!outFile.setExecutable(true, false)) {
                FLog.error("Unable to mark executable: " + outFile.getAbsolutePath());
            }
            return true;
        } catch (IOException e) {
            FLog.error("copyAssetToPrivate failed: " + e.getMessage());
            return false;
        }
    }
}
