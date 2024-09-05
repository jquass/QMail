package email.quass.qmail.data.aws.mapper;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class MimeMessageToAttributeValueMapper {

  private static final Logger LOG =
      LoggerFactory.getLogger(MimeMessageToAttributeValueMapper.class);

  private MimeMessageToAttributeValueMapper() {}

  public static Map<String, AttributeValue> map(String id, MimeMessage mimeMessage, Instant date)
      throws MessagingException, IOException {
    String content = "";
    String contentHtml = "";
    Object contentObject = mimeMessage.getContent();
    if (contentObject instanceof MimeMultipart mimeMultipart) {
      for (int i = 0; i < mimeMultipart.getCount(); i++) {
        String body = (String) mimeMultipart.getBodyPart(i).getContent();
        if (i == 0) {
          content = body;
        } else if (i == 1) {
          contentHtml = body;
        } else {
          LOG.error("Unexpected Body For ID {} at {} : {}", id, i, body);
        }
      }
    } else {
      content = (String) contentObject;
    }

    Map<String, AttributeValue> values = new HashMap<>();

    values.put("id", AttributeValue.fromS(id));
    values.put("content_type", AttributeValue.fromS(mimeMessage.getContentType()));
    values.put("content", AttributeValue.fromS(content));
    values.put("content_html", AttributeValue.fromS(contentHtml));
    values.put("date_ms", AttributeValue.fromN(String.valueOf(date.toEpochMilli())));

    List<AttributeValue> headers = new ArrayList<>();
    for (Iterator<Header> it = mimeMessage.getAllHeaders().asIterator(); it.hasNext(); ) {
      Header header = it.next();
      if (header.getName().equalsIgnoreCase("To")) {
        values.put("recipients", AttributeValue.fromS(header.getValue()));
      } else if (header.getName().equalsIgnoreCase("From")) {
        values.put("sender", AttributeValue.fromS(header.getValue()));
      } else if (header.getName().equalsIgnoreCase("Subject")) {
        values.put("subject", AttributeValue.fromS(header.getValue()));
      }
      headers.add(AttributeValue.fromS(header.getName() + ": " + header.getValue()));
    }
    values.put("headers", AttributeValue.fromL(headers));

    return values;
  }
}
