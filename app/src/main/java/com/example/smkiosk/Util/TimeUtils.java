package com.example.smkiosk.Util;

public class TimeUtils {

    private TimeUtils() {
        // 유틸 클래스라서 생성 막기
    }

    public static String formatTime(String raw) {
        if (raw == null) return "";
        // 예: 2025-11-26T15:41:24.788421 -> 2025-11-26 15:41:24
        try {
            String noMs = raw.split("\\.")[0];      // . 앞까지
            return noMs.replace('T', ' ');          // T -> 공백
        } catch (Exception e) {
            return raw.replace('T', ' ');
        }
    }
}
