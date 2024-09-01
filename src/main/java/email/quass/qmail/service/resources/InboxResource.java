package email.quass.qmail.service.resources;

import email.quass.qmail.core.email.Email;
import email.quass.qmail.core.email.S3MimeMessage;
import email.quass.qmail.core.http.QMailResponse;
import email.quass.qmail.core.http.ResponseType;
import email.quass.qmail.data.aws.EmailDynamoClient;
import email.quass.qmail.data.aws.EmailS3Client;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Path("/inbox")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InboxResource {

    private static final Logger LOG = LoggerFactory.getLogger(InboxResource.class);

    @GET
    public QMailResponse<List<Email>> getInbox(
            @HeaderParam("X-USERNAME") String username) {
        // TODO Pass in from based on UI state
        Instant from = Instant.now().minus(30, ChronoUnit.DAYS);
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
                // TODO Move S3 Object to Sub Folder to avoid re processing old
            } catch (MessagingException | IOException e) {
                LOG.error("Error inserting s3 mime message", e);
            }
        }

        return getInbox(username);
    }

}
