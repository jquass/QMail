package email.quass.qmail.core.login;

import email.quass.qmail.core.QMailStyle;
import java.util.Optional;
import org.immutables.value.Value;

@QMailStyle
@Value.Immutable
public interface SessionIF {

  Optional<String> getUsername();

  Optional<String> getKey();
}
