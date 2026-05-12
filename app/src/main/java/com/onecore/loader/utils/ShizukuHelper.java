package com.onecore.loader.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import rikka.shizuku.Shizuku;

public final class ShizukuHelper {

    private static final int SHIZUKU_PERMISSION_REQUEST_CODE = 3010;

    private ShizukuHelper() {
    }

    public static boolean isShizukuAvailable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isPermissionGranted() {
        if (!isShizukuAvailable()) return false;
        return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean requestPermissionIfNeeded(Activity activity) {
        if (!isShizukuAvailable()) return false;
        if (isPermissionGranted()) return true;
        if (Shizuku.shouldShowRequestPermissionRationale()) {
            return false;
        }
        Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE);
        return false;
    }

    public static int getRequestCode() {
        return SHIZUKU_PERMISSION_REQUEST_CODE;
    }
}
