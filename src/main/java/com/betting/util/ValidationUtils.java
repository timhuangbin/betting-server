package com.betting.util;

public class ValidationUtils {
    public static boolean isValidInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
