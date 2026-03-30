package com.medibhavan.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class UserIdGenerator {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    public String generate(String role) {
        String prefix = "doctor".equals(role) ? "Dr_" : "P_";
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
