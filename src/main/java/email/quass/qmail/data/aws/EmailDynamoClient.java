package email.quass.qmail.data.aws;

import email.quass.qmail.core.QMailEnv;
import email.quass.qmail.core.email.Email;
import email.quass.qmail.core.email.S3MimeMessage;
import email.quass.qmail.data.aws.mapper.AttributeValueToEmailMapper;
import email.quass.qmail.data.aws.mapper.MimeMessageToAttributeValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.s3.model.S3Object;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class EmailDynamoClient {

    private static final Logger LOG = LoggerFactory.getLogger(EmailDynamoClient.class);

    private static final DynamoDbClient DYNAMO_DB_CLIENT = DynamoDbClient
            .builder()
            .region(QMailEnv.REGION.asRegion())
            .build();
    private static final String TABLE = QMailEnv.EMAIL_TABLE_NAME.asString();
    private static final String DOMAIN = QMailEnv.EMAIL_DOMAIN.asString();

    public static void insertS3MimeMessage(S3MimeMessage s3MimeMessage) throws MessagingException, IOException {
        MimeMessage mimeMessage = s3MimeMessage.getMimeMessage();
        S3Object s3Object = s3MimeMessage.getS3Object();
        String id = s3Object.key();

        GetItemResponse response = DYNAMO_DB_CLIENT.getItem(
                GetItemRequest.builder()
                        .key(Map.of("id", AttributeValue.fromS(id)))
                        .tableName(TABLE)
                        .build()
        );

        if (response.hasItem()) {
            LOG.warn("Message with id {} already exists", id);
            return;
        }

        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE)
                .item(MimeMessageToAttributeValueMapper.map(id, mimeMessage, s3Object.lastModified()))
                .build();

        LOG.debug("Dynamo Request: {}", request);

        DYNAMO_DB_CLIENT.putItem(request);
        // TODO Move to S3 Sub Folder to avoid re processing
    }

    public static List<Email> listEmails(String username, Instant from) {
        AttributeValue recipient = AttributeValue.fromS(username + "@" + DOMAIN);
        AttributeValue dateMs = AttributeValue.fromN(String.valueOf(from.toEpochMilli()));
        QueryRequest request = QueryRequest.builder()
                .tableName(TABLE)
                .indexName("recipient-date_ms-index")
                .keyConditionExpression("recipient = :recipient AND date_ms > :date_ms")
                .expressionAttributeValues(Map.of(
                        ":recipient", recipient,
                        ":date_ms", dateMs
                ))
                .build();
        QueryResponse response = DYNAMO_DB_CLIENT.query(request);
        if (!response.hasItems()) {
            return List.of();
        }

        return AttributeValueToEmailMapper.map(response.items());
    }
}
