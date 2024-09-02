package email.quass.qmail.core.http;

import email.quass.qmail.core.QMailStyle;
import java.util.Optional;
import org.immutables.value.Value;

@QMailStyle
@Value.Immutable
public interface QMailResponseIF<V> {

  Optional<V> getContent();

  ResponseType getType();
}
