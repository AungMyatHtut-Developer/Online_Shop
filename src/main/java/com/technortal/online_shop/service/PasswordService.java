package com.technortal.online_shop.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

@Service
public class PasswordService {
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private final SecureRandom random = new SecureRandom();

    public String newSalt() {
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    // Format: lowercase hex SHA-256 of UTF-8(salt text + password).
    public String hash(String password, String salt) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest((salt + password).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    public boolean matches(String password, String salt, String expectedHash) {
        if (password == null || password.length() > 128 || salt == null || expectedHash == null) return false;
        return MessageDigest.isEqual(hash(password, salt).getBytes(StandardCharsets.US_ASCII),
                expectedHash.getBytes(StandardCharsets.US_ASCII));
    }

    public boolean meetsPolicy(String password) {
        return password != null && password.matches("(?s)(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{6,128}");
    }

    public String generateTemporaryPassword() {
        String alphabet = LOWER + UPPER + DIGITS;
        char[] password = new char[16];
        password[0] = LOWER.charAt(random.nextInt(LOWER.length()));
        password[1] = UPPER.charAt(random.nextInt(UPPER.length()));
        password[2] = DIGITS.charAt(random.nextInt(DIGITS.length()));
        for (int i = 3; i < password.length; i++) password[i] = alphabet.charAt(random.nextInt(alphabet.length()));
        for (int i = password.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char character = password[i];
            password[i] = password[j];
            password[j] = character;
        }
        return new String(password);
    }
}
