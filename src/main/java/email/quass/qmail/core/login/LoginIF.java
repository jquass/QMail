package email.quass.qmail.core.login;

import email.quass.qmail.core.QMailStyle;
import org.immutables.value.Value;

@QMailStyle
@Value.Immutable
public interface LoginIF {

  String getUsername();

  String getPassword();
}
