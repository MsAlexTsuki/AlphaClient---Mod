package com.alpharip.staffmod.AutoLoadingServerProfiles.matching;

import java.util.Locale;

/** Normalizes user-entered Minecraft server addresses without assuming a default port. */
public final class ServerAddressNormalizer {
    private ServerAddressNormalizer() { }

    public static String normalize(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Server address must not be empty");
        }

        String value = address.trim().toLowerCase(Locale.ROOT);
        int scheme = value.indexOf("://");
        if (scheme >= 0) value = value.substring(scheme + 3);
        int path = value.indexOf('/');
        if (path >= 0) value = value.substring(0, path);
        int query = value.indexOf('?');
        if (query >= 0) value = value.substring(0, query);
        value = value.trim();

        if (value.startsWith("[") && value.contains("]")) {
            int close = value.indexOf(']');
            String host = value.substring(1, close);
            String suffix = value.substring(close + 1);
            if (!suffix.isEmpty() && !suffix.matches(":[0-9]{1,5}")) {
                throw new IllegalArgumentException("Invalid server port: " + address);
            }
            return "[" + host + "]" + suffix;
        }

        int firstColon = value.indexOf(':');
        int lastColon = value.lastIndexOf(':');
        String host = value;
        String port = "";
        if (firstColon >= 0 && firstColon == lastColon) {
            host = value.substring(0, firstColon);
            port = value.substring(firstColon);
            if (!port.matches(":[0-9]{1,5}")) {
                throw new IllegalArgumentException("Invalid server port: " + address);
            }
            int number = Integer.parseInt(port.substring(1));
            if (number < 1 || number > 65535) {
                throw new IllegalArgumentException("Server port is outside the valid range: " + address);
            }
        }

        while (host.endsWith(".")) host = host.substring(0, host.length() - 1);
        if (host.isEmpty()) throw new IllegalArgumentException("Server host must not be empty");
        return host + port;
    }

    public static boolean matches(String configuredAddress, String connectedAddress) {
        Address configured = split(normalize(configuredAddress));
        Address connected = split(normalize(connectedAddress));

        if (configured.port != null && !configured.port.equals(connected.port)) return false;
        if (configured.host.equals(connected.host)) return true;

        return isDnsName(configured.host)
                && isDnsName(connected.host)
                && connected.host.endsWith("." + configured.host);
    }

    public static int specificity(String address) {
        Address parts = split(normalize(address));
        return parts.host.length() * 2 + (parts.port == null ? 0 : 1);
    }

    private static Address split(String value) {
        if (value.startsWith("[")) {
            int close = value.indexOf(']');
            String host = value.substring(1, close);
            String suffix = value.substring(close + 1);
            return new Address(host, suffix.isEmpty() ? null : suffix.substring(1));
        }
        int colon = value.lastIndexOf(':');
        if (colon >= 0 && value.indexOf(':') == colon) {
            return new Address(value.substring(0, colon), value.substring(colon + 1));
        }
        return new Address(value, null);
    }

    private static boolean isDnsName(String host) {
        return host.indexOf(':') < 0 && host.indexOf(' ') < 0;
    }

    private static final class Address {
        private final String host;
        private final String port;
        private Address(String host, String port) { this.host = host; this.port = port; }
    }
}
