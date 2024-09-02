package email.quass.qmail.data.aws;

import email.quass.qmail.core.QMailEnv;
import email.quass.qmail.core.email.Email;
import email.quass.qmail.core.email.S3MimeMessage;
import email.quass.qmail.data.aws.mapper.AttributeValueToEmailMapper;
import email.quass.qmail.data.aws.mapper.MimeMessageToAttributeValueMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

public class EmailDynamoClient {

  private static final Logger LOG = LoggerFactory.getLogger(EmailDynamoClient.class);

  private static final DynamoDbClient DYNAMO_DB_CLIENT =
      DynamoDbClient.builder().region(QMailEnv.REGION.asRegion()).build();
  private static final String TABLE = QMailEnv.EMAIL_TABLE_NAME.asString();
  private static final String DOMAIN = QMailEnv.EMAIL_DOMAIN.asString();

  public static void insertS3MimeMessage(S3MimeMessage s3MimeMessage)
      throws MessagingException, IOException {
    MimeMessage mimeMessage = s3MimeMessage.getMimeMessage();
    S3Object s3Object = s3MimeMessage.getS3Object();
    String id = s3Object.key();

    GetItemResponse response =
        DYNAMO_DB_CLIENT.getItem(
            GetItemRequest.builder()
                .key(Map.of("id", AttributeValue.fromS(id)))
                .tableName(TABLE)
                .build());

    if (response.hasItem()) {
      LOG.warn("Message with id {} already exists", id);
      EmailS3Client.archiveS3MimeMessage(s3MimeMessage, response.item().get("recipient").s());
      return;
    }

    Map<String, AttributeValue> attributeValueMap =
        MimeMessageToAttributeValueMapper.map(id, mimeMessage, s3Object.lastModified());

    if (!attributeValueMap.containsKey("recipient")) {
      LOG.warn("Could not determine recipient for message {}", id);
      return;
    }

    String recipient = attributeValueMap.get("recipient").s();

    PutItemRequest request =
        PutItemRequest.builder()
            .tableName(TABLE)
            .item(MimeMessageToAttributeValueMapper.map(id, mimeMessage, s3Object.lastModified()))
            .build();

    DYNAMO_DB_CLIENT.putItem(request);
    EmailS3Client.archiveS3MimeMessage(s3MimeMessage, recipient);
  }

  public static List<Email> listEmails(String username, Instant from) {
    AttributeValue recipient = AttributeValue.fromS(username + "@" + DOMAIN);
    AttributeValue dateMs = AttributeValue.fromN(String.valueOf(from.toEpochMilli()));
    QueryRequest request =
        QueryRequest.builder()
            .tableName(TABLE)
            .indexName("recipient-date_ms-index")
            .keyConditionExpression("recipient = :recipient AND date_ms > :date_ms")
            .expressionAttributeValues(
                Map.of(
                    ":recipient", recipient,
                    ":date_ms", dateMs))
            .build();
    QueryResponse response = DYNAMO_DB_CLIENT.query(request);
    if (!response.hasItems()) {
      return List.of();
    }

    return AttributeValueToEmailMapper.map(response.items());
  }
}
