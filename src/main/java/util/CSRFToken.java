package util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class CSRFToken {

    public static String get() throws NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();

        byte[] values = new byte[16];
        random.nextBytes(values);

        return Base64.getEncoder().encodeToString(values);
    }
}
