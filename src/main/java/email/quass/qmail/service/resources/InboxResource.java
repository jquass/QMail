package email.quass.qmail.service.resources;

import email.quass.qmail.core.email.Email;
import email.quass.qmail.core.email.S3MimeMessage;
import email.quass.qmail.core.http.QMailResponse;
import email.quass.qmail.core.http.ResponseType;
import email.quass.qmail.data.aws.EmailDynamoClient;
import email.quass.qmail.data.aws.EmailS3Client;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/inbox")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InboxResource {

  private static final Logger LOG = LoggerFactory.getLogger(InboxResource.class);

  @GET
  public QMailResponse<List<Email>> getInbox(@HeaderParam("X-USERNAME") String username) {
    // TODO Pass in from based on UI state
    Instant from = Instant.now().minus(30, ChronoUnit.DAYS);
    LOG.info("Getting inbox for {} from {}", username, from);
    return QMailResponse.<List<Email>>builder()
        .setType(ResponseType.OK)
        .setContent(EmailDynamoClient.listEmails(username, from))
        .build();
  }

  @POST()
  @Path("/refresh")
  public QMailResponse<List<Email>> refreshInbox(@HeaderParam("X-USERNAME") String username) {
    List<S3MimeMessage> s3MimeMessages = EmailS3Client.listNew();
    for (S3MimeMessage s3MimeMessage : s3MimeMessages) {
      try {
        EmailDynamoClient.insertS3MimeMessage(s3MimeMessage);
      } catch (MessagingException | IOException e) {
        LOG.error("Error inserting s3 mime message", e);
        return QMailResponse.<List<Email>>builder().setType(ResponseType.ERROR).build();
      }
    }

    return getInbox(username);
  }
}
