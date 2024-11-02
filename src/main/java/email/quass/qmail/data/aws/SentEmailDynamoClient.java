package email.quass.qmail.data.aws;

import email.quass.qmail.core.QMailEnv;
import email.quass.qmail.core.email.SendEmail;
import email.quass.qmail.core.email.SentEmail;
import email.quass.qmail.data.aws.mapper.AttributeValueToSentEmailMapper;
import email.quass.qmail.data.aws.query.QueryRequestBuilder;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

public class SentEmailDynamoClient {

  private static final Logger LOG = LoggerFactory.getLogger(SentEmailDynamoClient.class);

  private static final DynamoDbClient DYNAMO_DB_CLIENT =
      DynamoDbClient.builder().region(QMailEnv.REGION.asRegion()).build();
  private static final String TABLE =
      QMailEnv.TABLE_PREFIX.asString() + "_" + QMailEnv.SENT_EMAIL_TABLE_NAME.asString();

  public static void insertSentMessage(String username, String messageId, SendEmail sendEmail) {
    Map<String, AttributeValue> attributeValueMap =
        Map.of(
            "username", AttributeValue.fromS(username),
            "date_ms", AttributeValue.fromN(String.valueOf(Instant.now().toEpochMilli())),
            "message_id", AttributeValue.fromS(messageId),
            "recipients", AttributeValue.fromS(sendEmail.getTo()),
            "sender", AttributeValue.fromS(sendEmail.getFrom()),
            "content", AttributeValue.fromS(sendEmail.getBody()),
            "subject", AttributeValue.fromS(sendEmail.getSubject()));

    PutItemRequest request =
        PutItemRequest.builder().tableName(TABLE).item(attributeValueMap).build();
    DYNAMO_DB_CLIENT.putItem(request);
  }

  public static List<SentEmail> listSentEmails(String username, Instant from) {
    LOG.info("Listing sent emails for {} from {}", username, from);
    QueryRequest request = QueryRequestBuilder.build(TABLE, username, from);
    QueryResponse response = DYNAMO_DB_CLIENT.query(request);
    if (!response.hasItems()) {
      return List.of();
    }

    return AttributeValueToSentEmailMapper.map(response.items());
  }
}
