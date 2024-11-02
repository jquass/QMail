package email.quass.qmail.data.aws.query;

import java.time.Instant;
import java.util.Map;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

public class QueryRequestBuilder {

  private QueryRequestBuilder() {}

  public static QueryRequest build(String table, String username, Instant date) {
    return QueryRequest.builder()
        .tableName(table)
        .keyConditionExpression("username = :username AND date_ms >= :date_ms")
        .expressionAttributeValues(
            Map.of(
                ":username", AttributeValue.fromS(username),
                ":date_ms", AttributeValue.fromN(String.valueOf(date.toEpochMilli()))))
        .build();
  }
}
