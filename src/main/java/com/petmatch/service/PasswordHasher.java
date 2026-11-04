package com.petmatch.service;

import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordHasher {
    private static final int ITERATIONS = 210000;

    public static String hash(char[] password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return ITERATIONS
                + ":"
                + Base64.getEncoder().encodeToString(salt)
                + ":"
                + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
    }

    public static boolean verify(char[] password, String encoded) {
        try {
            var p = encoded.split(":");
            int n = Integer.parseInt(p[0]);
            if (n < 100000 || n > 1000000)
                return false;
            return MessageDigest.isEqual(
                    Base64.getDecoder().decode(p[2]), derive(password, Base64.getDecoder().decode(p[1]), n));
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int n) {
        var spec = new PBEKeySpec(password, salt, n, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        } finally {
            spec.clearPassword();
        }
    }
}
