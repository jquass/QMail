package email.quass.qmail.data.aws;

import email.quass.qmail.core.QMailEnv;
import email.quass.qmail.core.email.Email;
import email.quass.qmail.core.email.S3MimeMessage;
import email.quass.qmail.data.aws.mapper.AttributeValueToEmailMapper;
import email.quass.qmail.data.aws.mapper.MimeMessageToAttributeValueMapper;
import email.quass.qmail.data.aws.mapper.RecipientHeaderMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

public class EmailDynamoClient {

  private static final Logger LOG = LoggerFactory.getLogger(EmailDynamoClient.class);

  private static final DynamoDbClient DYNAMO_DB_CLIENT =
      DynamoDbClient.builder().region(QMailEnv.REGION.asRegion()).build();
  private static final String TABLE = QMailEnv.EMAIL_TABLE_NAME.asString();

  public static void insertS3MimeMessage(S3MimeMessage s3MimeMessage)
      throws MessagingException, IOException {
    MimeMessage mimeMessage = s3MimeMessage.getMimeMessage();
    S3Object s3Object = s3MimeMessage.getS3Object();
    String id = s3Object.key();
    Map<String, AttributeValue> attributeValueMap =
        MimeMessageToAttributeValueMapper.map(id, mimeMessage, s3Object.lastModified());
    if (!attributeValueMap.containsKey("recipients")) {
      LOG.warn("Could not determine recipients for message {}", id);
      EmailS3Client.archiveS3MimeMessage(s3MimeMessage);
      return;
    }

    String recipients = attributeValueMap.get("recipients").s();
    for (String username : RecipientHeaderMapper.getUsernames(recipients)) {
      LOG.info("Created record for user {}", username);
      Map<String, AttributeValue> userMap = new HashMap<>(attributeValueMap);
      userMap.put("username", AttributeValue.fromS(username));
      PutItemRequest request = PutItemRequest.builder().tableName(TABLE).item(userMap).build();
      DYNAMO_DB_CLIENT.putItem(request);
    }
    EmailS3Client.archiveS3MimeMessage(s3MimeMessage);
  }

  public static List<Email> listEmails(String username, Instant from) {
    AttributeValue dateMs = AttributeValue.fromN(String.valueOf(from.toEpochMilli()));
    LOG.info("Listing emails for {} from {}", username, dateMs);
    QueryRequest request =
        QueryRequest.builder()
            .tableName(TABLE)
            .keyConditionExpression("username = :username AND date_ms >= :date_ms")
            .expressionAttributeValues(
                Map.of(
                    ":username", AttributeValue.fromS(username),
                    ":date_ms", AttributeValue.fromN(String.valueOf(from.toEpochMilli()))))
            .build();
    QueryResponse response = DYNAMO_DB_CLIENT.query(request);
    if (!response.hasItems()) {
      return List.of();
    }

    return AttributeValueToEmailMapper.map(response.items());
  }
}
