package email.quass.qmail.data.aws.mapper;

import email.quass.qmail.core.email.SentEmail;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class AttributeValueToSentEmailMapper {

  private AttributeValueToSentEmailMapper() {}

  public static List<SentEmail> map(List<Map<String, AttributeValue>> attributeValues) {
    List<SentEmail> emails = new ArrayList<>();
    for (Map<String, AttributeValue> attributeValue : attributeValues) {
      SentEmail email =
          SentEmail.builder()
              .setUsername(attributeValue.get("username").s())
              .setTo(attributeValue.get("recipients").s())
              .setFrom(attributeValue.get("sender").s())
              .setSubject(attributeValue.get("subject").s())
              .setBody(attributeValue.get("content").s())
              .setMessageId(attributeValue.get("message_id").s())
              .setDate(Instant.ofEpochMilli(Long.parseLong(attributeValue.get("date_ms").n())))
              .build();
      emails.add(email);
    }
    return emails;
  }
}
