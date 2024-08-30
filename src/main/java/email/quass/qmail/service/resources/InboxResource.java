package email.quass.qmail.service.resources;

import email.quass.qmail.core.email.Email;
import email.quass.qmail.core.http.QMailResponse;
import email.quass.qmail.core.http.ResponseType;
import email.quass.qmail.data.aws.EmailDynamoClient;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Path("/inbox")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InboxResource {

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

}
