package email.quass.qmail.core.http;

import email.quass.qmail.core.QMailStyle;
import org.immutables.value.Value;

import java.util.Optional;

@QMailStyle
@Value.Immutable
public interface QMailResponseIF<V> {

    Optional<V> getContent();

    ResponseType getType();

}
