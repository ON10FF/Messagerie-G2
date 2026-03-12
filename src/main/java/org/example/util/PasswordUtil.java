package org.example.util;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Arrays;

public class PasswordUtil {

    public static String getHash(char[] password) {
            String hashPassword = BCrypt.hashpw(new String(password), BCrypt.gensalt());
            Arrays.fill(password, '0');
            return hashPassword;
    }

    public static boolean verifyPassword(char[] password, String storedPassword) {

        boolean verifyP = BCrypt.checkpw(new String(password), storedPassword);
        Arrays.fill(password, '0');
        return verifyP;
    }

    public static boolean verifyPassword(String hashPassword, String storedpassword) {
        return BCrypt.checkpw(hashPassword, storedpassword);
    }
}
