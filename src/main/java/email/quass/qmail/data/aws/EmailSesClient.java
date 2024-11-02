package email.quass.qmail.data.aws;

import email.quass.qmail.core.QMailEnv;
import email.quass.qmail.core.email.SendEmail;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

public class EmailSesClient {

  private static final Logger LOG = LoggerFactory.getLogger(EmailSesClient.class);

  private static final SesV2Client SES_V2_CLIENT =
      SesV2Client.builder().region(QMailEnv.REGION.asRegion()).build();

  public static Optional<String> sendEmail(SendEmail sendEmail) {
    LOG.debug("Sending email: {}", sendEmail);

    try {
      Destination destination = Destination.builder().toAddresses(sendEmail.getTo()).build();
      Content bodyContent = Content.builder().data(sendEmail.getBody()).build();
      Content subjectContent = Content.builder().data(sendEmail.getSubject()).build();
      Body body = Body.builder().html(bodyContent).build();
      Message message = Message.builder().subject(subjectContent).body(body).build();
      EmailContent emailContent = EmailContent.builder().simple(message).build();
      SendEmailRequest emailRequest =
          SendEmailRequest.builder()
              .destination(destination)
              .content(emailContent)
              .fromEmailAddress(sendEmail.getFrom())
              .build();
      SendEmailResponse response = SES_V2_CLIENT.sendEmail(emailRequest);
      LOG.info("Response {}", response);
      return response.getValueForField("MessageId", String.class);
    } catch (Exception e) {
      LOG.error("Exception sending SES", e);
      return Optional.empty();
    }
  }
}
