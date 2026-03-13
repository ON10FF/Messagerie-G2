package org.example.util;

import org.mindrot.jbcrypt.BCrypt;

import java.security.MessageDigest;
import java.util.Arrays;

public class PasswordUtil {

    // Hash SHA-256 — utilisé côté CLIENT avant envoi au serveur
    public static String getSHA256(char[] password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(new String(password).getBytes("UTF-8"));
            Arrays.fill(password, '0');
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erreur SHA-256", e);
        }
    }

    // Hash BCrypt — utilisé côté SERVEUR pour stocker en BDD
    public static String getHash(char[] password) {
        String hashed = BCrypt.hashpw(new String(password), BCrypt.gensalt());
        Arrays.fill(password, '0');
        return hashed;
    }

    // Vérifie char[] vs BCrypt stocké — NON UTILISÉ mais gardé
    public static boolean verifyPassword(char[] password, String storedPassword) {
        boolean result = BCrypt.checkpw(new String(password), storedPassword);
        Arrays.fill(password, '0');
        return result;
    }

    // Vérifie SHA256 reçu vs BCrypt stocké en BDD — utilisé dans handleLogin
    public static boolean verifyPassword(String sha256Password, String storedBcrypt) {
        return BCrypt.checkpw(sha256Password, storedBcrypt);
    }
}