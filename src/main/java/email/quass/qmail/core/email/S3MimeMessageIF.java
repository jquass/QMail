package email.quass.qmail.core.email;

import email.quass.qmail.core.QMailStyle;
import javax.mail.internet.MimeMessage;
import org.immutables.value.Value;
import software.amazon.awssdk.services.s3.model.S3Object;

@QMailStyle
@Value.Immutable
public interface S3MimeMessageIF {
  S3Object getS3Object();

  MimeMessage getMimeMessage();
}
