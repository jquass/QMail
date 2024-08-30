package email.quass.qmail.core.email;

import email.quass.qmail.core.QMailStyle;
import org.immutables.value.Value;

import java.time.Instant;
import java.util.Optional;

@QMailStyle
@Value.Immutable
public interface EmailIF {

    String getTo();

    String getFrom();

    String getSubject();

    String getBody();

    Optional<String> getBodyHtml();

    Instant getDate();
}
