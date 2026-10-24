package security;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.security.MessageDigest;
import security.PasswordUtils;
public class PasswordUtils {
    private static final int ITERATIONS = 600_000;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return salt;
    }
    private static byte[] hashPassword(char[] password, byte[] salt)
            throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(
                password,
                salt,
                ITERATIONS,
                KEY_LENGTH
        );
        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        try {
            return factory.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
    public static String hash(String password) {
        byte[] salt = generateSalt();
        try {
            byte[] hashedPassword = hashPassword(password.toCharArray(), salt);
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hashedPassword);

            return saltBase64 + ":" + hashBase64;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing failed", e);
        }

    }
    public static boolean verify(String password, String storedHash) {

        if (password == null || storedHash == null) {
            return false;
        }

        String[] parts = storedHash.split(":", -1);

        if (parts.length != 2) {
            return false;
        }
        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            byte[] actualHash = hashPassword(password.toCharArray(), salt);
            return MessageDigest.isEqual(expectedHash, actualHash);

         } catch (IllegalArgumentException e) {
        return false;
    } catch (GeneralSecurityException e) {
        throw new IllegalStateException("Password verification failed", e);
    }

    }

    public static void main(String[] args) {
        System.out.println(hash("123456"));
    }
}
