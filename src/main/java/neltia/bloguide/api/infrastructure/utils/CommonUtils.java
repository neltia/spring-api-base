package neltia.bloguide.api.infrastructure.utils;

public class CommonUtils {
    public static String convertGbn2Priority(Integer gbn) {
        String priority = switch (gbn) {
            case 1 -> "high";
            case 2 -> "medium";
            case 3 -> "low";
            default -> null;
        };
        return priority;
    }
}
