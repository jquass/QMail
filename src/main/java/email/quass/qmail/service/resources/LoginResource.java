package email.quass.qmail.service.resources;

import email.quass.qmail.core.http.QMailResponse;
import email.quass.qmail.core.http.ResponseType;
import email.quass.qmail.core.login.Login;
import email.quass.qmail.core.login.Session;
import email.quass.qmail.core.login.User;
import email.quass.qmail.data.aws.UserDynamoClient;
import email.quass.qmail.data.login.PasswordHasher;
import email.quass.qmail.data.login.SessionCache;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Optional;
import java.util.UUID;

@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoginResource {

  public LoginResource() {}

  @POST
  public QMailResponse<Session> login(Login login) {

    String sessionKey = UUID.randomUUID().toString();
    Optional<User> user = UserDynamoClient.getUser(login.getUsername());
    String loginPasswordHash = PasswordHasher.hashPassword(login.getPassword());
    Session session = Session.builder().setUsername(login.getUsername()).setKey(sessionKey).build();

    if (user.isPresent() && user.get().getPasswordHash().equals(loginPasswordHash)) {
      SessionCache.saveUserSession(login.getUsername(), sessionKey);
      return QMailResponse.<Session>builder().setType(ResponseType.OK).setContent(session).build();
    }

    return QMailResponse.<Session>builder()
        .setType(ResponseType.UNAUTHORIZED)
        .setContent(Session.builder().build())
        .build();
  }
}
