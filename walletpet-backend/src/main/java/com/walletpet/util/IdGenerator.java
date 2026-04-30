package com.walletpet.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class IdGenerator {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private IdGenerator() {
    }

    public static String generate(String prefix) {
        String time = LocalDateTime.now().format(FORMATTER);
        String random = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8);

        return prefix + time + random;
    }
}