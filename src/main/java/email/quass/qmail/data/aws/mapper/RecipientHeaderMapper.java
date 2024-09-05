package email.quass.qmail.data.aws.mapper;

import email.quass.qmail.core.QMailEnv;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecipientHeaderMapper {

  private static final Logger LOG = LoggerFactory.getLogger(RecipientHeaderMapper.class);

  private static final String DOMAIN = QMailEnv.EMAIL_DOMAIN.asString();
  private static final Pattern USERNAME_PATTERN =
      Pattern.compile("((?<name>.*) <)?(?<username>.*)@" + DOMAIN + ">?");

  private RecipientHeaderMapper() {}

  public static List<String> getUsernames(String recipientHeader) {
    List<String> usernames = new ArrayList<>();
    List<String> recipients = Arrays.stream(recipientHeader.split(",")).map(String::trim).toList();
    for (String recipient : recipients) {
      Matcher matcher = USERNAME_PATTERN.matcher(recipient);
      if (matcher.find()) {
        String username = matcher.group("username");
        Optional<String> name = Optional.ofNullable(matcher.group("name"));
        LOG.info("Recipient {} Found Username {} Name {}", recipient, username, name);
        usernames.add(username);
      }
    }
    return usernames;
  }
}
