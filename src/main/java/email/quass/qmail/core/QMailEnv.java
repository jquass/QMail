package email.quass.qmail.core;

import software.amazon.awssdk.regions.Region;

public enum QMailEnv {
  REGION,
  TABLE_PREFIX,
  EMAIL_TABLE_NAME,
  USER_TABLE_NAME,
  SENT_EMAIL_TABLE_NAME,
  EMAIL_DOMAIN,
  S3_BUCKET_INBOX,
  S3_BUCKET_ARCHIVE,
  PASSWORD_SALT,
  PASSWORD_ITERATION_COUNT,
  PASSWORD_KEY_LENGTH,
  CORS_ORIGIN,
  ;

  public String asString() {
    return System.getenv(name());
  }

  public int asInt() {
    return Integer.parseInt(asString());
  }

  public Region asRegion() {
    return Region.of(asString());
  }

  public String withTablePrefix() {
    return TABLE_PREFIX.asString() + "_" + asString();
  }
}
