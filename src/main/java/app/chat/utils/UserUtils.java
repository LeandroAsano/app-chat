package app.chat.utils;

import app.chat.model.User;

import java.util.Map;

public final class UserUtils {

    public static boolean mapsEqualValues(Map<String, User> map1, Map<String, User> map2) {
        // Check for null and size mismatch
        if (map1 == null || map2 == null || map1.size() != map2.size()) {
            return false;
        }

        // Use streams to compare values
        return map1.entrySet().stream()
                .allMatch(entry -> map2.containsKey(entry.getKey()) &&
                        map2.get(entry.getKey()).equals(entry.getValue()));
    }

}
