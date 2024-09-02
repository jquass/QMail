package email.quass.qmail.data.login;

import email.quass.qmail.core.QMailEnv;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {

  private static final byte[] SALT =
      QMailEnv.PASSWORD_SALT.asString().getBytes(Charset.defaultCharset());
  private static final int ITERATIONS = QMailEnv.PASSWORD_ITERATION_COUNT.asInt();
  private static final int KEY_LENGTH = QMailEnv.PASSWORD_KEY_LENGTH.asInt();

  public static String hashPassword(String password) {
    try {
      KeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, ITERATIONS, KEY_LENGTH);
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      byte[] hash = factory.generateSecret(spec).getEncoded();
      return new String(hash);
    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
