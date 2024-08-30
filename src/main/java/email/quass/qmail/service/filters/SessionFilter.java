package email.quass.qmail.service.filters;

import email.quass.qmail.core.http.QMailResponse;
import email.quass.qmail.core.http.ResponseType;
import email.quass.qmail.core.login.Session;
import email.quass.qmail.data.login.SessionCache;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class SessionFilter implements ContainerRequestFilter {

    private static final String SESSION_ID_HEADER = "X-SESSION-ID";
    private static final String USERNAME_HEADER = "X-USERNAME";

    private static final Logger LOG = LoggerFactory.getLogger(SessionFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (!requestContext.getUriInfo().getRequestUri().getPath().equals("/api/login")) {
            LOG.debug("Verifying user session");
            if(requestContext.getHeaders().containsKey(SESSION_ID_HEADER)
                    && requestContext.getHeaders().containsKey(USERNAME_HEADER)) {
                String sessionId = requestContext.getHeaders().getFirst(SESSION_ID_HEADER);
                String username = requestContext.getHeaders().getFirst(USERNAME_HEADER);
                if (SessionCache.getUserSession(username).isPresent()
                        && SessionCache.getUserSession(username).get().equals(sessionId)) {
                    LOG.debug("User session valid");
                    return;
                }
            }

            LOG.debug("User session invalid");
            QMailResponse<Session> response = QMailResponse.<Session>builder()
                    .setType(ResponseType.UNAUTHORIZED)
                    .build();
            requestContext.abortWith(
                    Response.status(Response.Status.OK)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(response)
                            .build()

            );
        }
    }
}
