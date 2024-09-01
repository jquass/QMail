package email.quass.qmail.core;

import software.amazon.awssdk.regions.Region;

public enum QMailEnv {
    REGION,
    EMAIL_TABLE_NAME,
    EMAIL_DOMAIN,
    USER_TABLE_NAME,
    S3_BUCKET,
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
}
