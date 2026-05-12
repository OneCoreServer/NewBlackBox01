package com.onecore.loader.utils;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;

public final class GmsStub {
    private GmsStub() {
    }

    public static int isGooglePlayServicesAvailable(Context context) {
        return ConnectionResult.SUCCESS;
    }

    public static String getOpenSourceSoftwareLicenseInfo(Context context) {
        return "";
    }
}
