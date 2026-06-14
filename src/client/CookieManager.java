package client;

import java.util.HashMap;
import java.util.Map;

public class CookieManager {
    private static final Map<String, String> cookies = new HashMap<>();

    public static void saveCookie(String cookieLine) {
        if (cookieLine == null) {
            return;
        }
        String firstPart = cookieLine.split(";")[0];
        String[] kv = firstPart.split("=", 2);
        if (kv.length != 2) {
            return;
        }
        String key = kv[0].trim();
        String value = kv[1].trim();
        if (value.isEmpty()) {
            cookies.remove(key);
        } else {
            cookies.put(key, value);
        }
    }

    public static String buildCookieHeader() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            if (!sb.isEmpty()) {
                sb.append("; ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    public static void printCookies() {
        System.out.println("Cookie: " + cookies);
    }

    public static String getCookie(String name) {
        if (name == null) {
            return "";
        }
        String cookie = cookies.get(name);
        if (cookie == null) {
            return "";
        } else {
            return cookie;
        }
    }
}