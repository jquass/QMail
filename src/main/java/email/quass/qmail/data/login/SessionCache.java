package email.quass.qmail.data.login;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class SessionCache {

    private SessionCache() {}

    private static final Cache<String, String> USER_SESSIONS_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(24, TimeUnit.HOURS)
            .build();

    public static void saveUserSession(String username, String sessionId) {
        USER_SESSIONS_CACHE.put(username, sessionId);
    }

    public static Optional<String> getUserSession(String username) {
        return Optional.ofNullable(USER_SESSIONS_CACHE.getIfPresent(username));
    }

    public static void removeUserSession(String email) {
        USER_SESSIONS_CACHE.invalidate(email);
    }
}
