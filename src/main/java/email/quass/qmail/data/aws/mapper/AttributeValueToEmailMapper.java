package email.quass.qmail.data.aws.mapper;

import email.quass.qmail.core.email.Email;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributeValueToEmailMapper {

    private AttributeValueToEmailMapper() {}

    public static List<Email> map(List<Map<String, AttributeValue>> attributeValues) {
        List<Email> emails = new ArrayList<>();
        for (Map<String, AttributeValue> attributeValue : attributeValues) {
            Email email = Email.builder()
                    .setTo(attributeValue.get("recipient").s())
                    .setFrom(attributeValue.get("sender").s())
                    .setSubject(attributeValue.get("subject").s())
                    .setBody(attributeValue.get("content").s())
                    .setBodyHtml(attributeValue.get("content_html").s())
                    .setDate(Instant.ofEpochMilli(Long.parseLong(attributeValue.get("date_ms").n())))
                    .build();
            emails.add(email);
        }
        return emails;
    }

}
