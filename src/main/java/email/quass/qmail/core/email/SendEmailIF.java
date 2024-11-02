package email.quass.qmail.core.email;

import email.quass.qmail.core.QMailStyle;
import org.immutables.value.Value;

@QMailStyle
@Value.Immutable
public interface SendEmailIF extends EmailFields {}
