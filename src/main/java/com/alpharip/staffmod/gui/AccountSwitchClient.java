// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Local bridge between AlphaClient and the AlphaLauncher process.
 * Authentication tokens are exchanged only over loopback and are never written to disk.
 */
public final class AccountSwitchClient {
    private static final Gson GSON = new Gson();

    private AccountSwitchClient() {
    }

    public static List<SavedAccount> getSavedAccounts() {
        String encoded = System.getProperty("alphaclient.savedAccounts");
        if (encoded == null || encoded.trim().isEmpty()) return Collections.emptyList();

        try {
            String json = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
            SavedAccount[] accounts = GSON.fromJson(json, SavedAccount[].class);
            if (accounts == null) return Collections.emptyList();

            List<SavedAccount> valid = new ArrayList<SavedAccount>();
            for (SavedAccount account : accounts) {
                if (account != null && account.name != null && !account.name.trim().isEmpty()
                        && account.email != null && !account.email.trim().isEmpty()) {
                    valid.add(account);
                }
            }
            return Collections.unmodifiableList(valid);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    public static SwitchResult switchAccount(String email) throws Exception {
        String portValue = System.getProperty("alphaclient.accountSwitchPort");
        String secret = System.getProperty("alphaclient.accountSwitchSecret");
        if (portValue == null || secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("Account switching is available only when AlphaClient is started by Alpha Launcher.");
        }

        int port;
        try {
            port = Integer.parseInt(portValue);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("The Alpha Launcher account bridge is not configured correctly.");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalStateException("The Alpha Launcher account bridge is not configured correctly.");
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL("http://127.0.0.1:" + port + "/switch-account").openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(180000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Connection", "close");

            String request = GSON.toJson(new SwitchRequest(secret, email));
            byte[] body = request.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(body.length);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(body);
            }

            int status = connection.getResponseCode();
            InputStream responseStream = status >= 200 && status < 300
                    ? connection.getInputStream() : connection.getErrorStream();
            String responseBody = readFully(responseStream);
            if (status < 200 || status >= 300) {
                ErrorResponse error = GSON.fromJson(responseBody, ErrorResponse.class);
                throw new IllegalStateException(error != null && error.error != null
                        ? error.error : "Alpha Launcher could not switch the Microsoft account.");
            }

            SwitchResult result = GSON.fromJson(responseBody, SwitchResult.class);
            if (result == null || result.name == null || result.uuid == null
                    || result.accessToken == null || result.accessToken.trim().isEmpty()) {
                throw new IllegalStateException("Alpha Launcher returned incomplete Minecraft session data.");
            }
            return result;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String readFully(InputStream input) throws Exception {
        if (input == null) return "";
        try (InputStream stream = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[2048];
            int count;
            while ((count = stream.read(buffer)) != -1) output.write(buffer, 0, count);
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    public static final class SavedAccount {
        String name;
        String email;
        String uuid;

        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    private static final class SwitchRequest {
        final String secret;
        final String email;

        SwitchRequest(String secret, String email) {
            this.secret = secret;
            this.email = email;
        }
    }

    public static final class SwitchResult {
        String name;
        String email;
        String uuid;
        String accessToken;

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getUuid() { return uuid; }
        public String getAccessToken() { return accessToken; }
    }

    private static final class ErrorResponse {
        String error;
    }
}
