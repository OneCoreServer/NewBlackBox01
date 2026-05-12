package com.onecore.loader.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class GoogleLoginBrowser {

    private static final String OAUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String REDIRECT_URI = "com.onecore.loader://auth";

    private final String clientId;

    public GoogleLoginBrowser(String clientId) {
        this.clientId = clientId;
    }

    public void startLogin(Activity activity) {
        String authUrl = OAUTH_URL + "?"
                + "client_id=" + Uri.encode(clientId)
                + "&redirect_uri=" + Uri.encode(REDIRECT_URI)
                + "&response_type=token"
                + "&scope=openid%20email%20profile";

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
        activity.startActivity(intent);
    }

    public String handleCallback(Intent intent) {
        if (intent == null) {
            return null;
        }

        Uri data = intent.getData();
        if (data == null) {
            return null;
        }

        if (!"com.onecore.loader".equals(data.getScheme())) {
            return null;
        }

        String fragment = data.getFragment();
        if (TextUtils.isEmpty(fragment)) {
            return null;
        }

        return parseAccessToken(fragment);
    }

    private String parseAccessToken(String fragment) {
        String[] params = fragment.split("&");
        Map<String, String> values = new HashMap<>();
        for (String param : params) {
            String[] entry = param.split("=", 2);
            if (entry.length == 2) {
                values.put(entry[0], Uri.decode(entry[1]));
            }
        }
        return values.get("access_token");
    }
}
