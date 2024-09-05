package email.quass.qmail.core.email;

import email.quass.qmail.core.QMailStyle;
import java.time.Instant;
import java.util.Optional;
import org.immutables.value.Value;

@QMailStyle
@Value.Immutable
public interface EmailIF {

  String getTo();

  String getFrom();

  String getSubject();

  String getBody();

  Optional<String> getBodyHtml();

  Instant getDate();

  String getUsername();
}
