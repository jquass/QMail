package email.quass.qmail.core.login;

import email.quass.qmail.core.QMailStyle;
import org.immutables.value.Value;

import java.util.Optional;

@QMailStyle
@Value.Immutable
public interface SessionIF {

    Optional<String> getUsername();

    Optional<String> getKey();

}
