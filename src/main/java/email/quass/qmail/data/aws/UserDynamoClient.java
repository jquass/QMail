package email.quass.qmail.data.aws;

import email.quass.qmail.core.QMailEnv;
import email.quass.qmail.core.login.Login;
import email.quass.qmail.core.login.User;
import email.quass.qmail.data.login.PasswordHasher;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

public class UserDynamoClient {

  private static final Logger LOG = LoggerFactory.getLogger(UserDynamoClient.class);

  private static final Region REGION = Region.of(QMailEnv.REGION.asString());
  private static final DynamoDbClient DYNAMO_DB_CLIENT =
      DynamoDbClient.builder().region(REGION).build();

  public static void createUser(Login login) {
    Optional<User> userMaybe = getUser(login.getUsername());
    if (userMaybe.isPresent()) {
      LOG.info("User with username {} already exists", login.getUsername());
      return;
    }

    PutItemRequest request =
        PutItemRequest.builder()
            .tableName(QMailEnv.USER_TABLE_NAME.asString())
            .item(
                Map.of(
                    "username", AttributeValue.fromS(login.getUsername()),
                    "password_hash",
                        AttributeValue.fromS(PasswordHasher.hashPassword(login.getPassword()))))
            .build();

    DYNAMO_DB_CLIENT.putItem(request);
  }

  public static Optional<User> getUser(String username) {
    GetItemResponse response =
        DYNAMO_DB_CLIENT.getItem(
            GetItemRequest.builder()
                .key(Map.of("username", AttributeValue.fromS(username)))
                .tableName(QMailEnv.USER_TABLE_NAME.asString())
                .build());
    if (!response.hasItem()) {
      return Optional.empty();
    }

    User user =
        User.builder()
            .setUsername(response.item().get("username").s())
            .setPasswordHash(response.item().get("password_hash").s())
            .build();
    return Optional.of(user);
  }
}
