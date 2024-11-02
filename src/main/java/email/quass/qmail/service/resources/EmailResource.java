package email.quass.qmail.service.resources;

import email.quass.qmail.core.email.SendEmail;
import email.quass.qmail.core.email.SentEmail;
import email.quass.qmail.core.http.QMailResponse;
import email.quass.qmail.core.http.ResponseType;
import email.quass.qmail.data.aws.EmailSesClient;
import email.quass.qmail.data.aws.SentEmailDynamoClient;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Path("/email")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EmailResource {

  @POST
  @Path("/send")
  public QMailResponse<String> sendEmail(
      @HeaderParam("X-USERNAME") String username, SendEmail sendEmail) {

    Optional<String> messageId = EmailSesClient.sendEmail(sendEmail);
    messageId.ifPresent(id -> SentEmailDynamoClient.insertSentMessage(username, id, sendEmail));

    // Schedule email refresh

    return QMailResponse.of(messageId, ResponseType.OK);
  }

  @GET
  @Path("/sent")
  public QMailResponse<List<SentEmail>> sentEmail(@HeaderParam("X-USERNAME") String username) {

    List<SentEmail> sentEmails =
        SentEmailDynamoClient.listSentEmails(username, Instant.now().minus(30, ChronoUnit.DAYS));

    return QMailResponse.of(Optional.of(sentEmails), ResponseType.OK);
  }
}
