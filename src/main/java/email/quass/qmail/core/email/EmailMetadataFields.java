package email.quass.qmail.core.email;

import java.time.Instant;

public interface EmailMetadataFields {

  Instant getDate();

  String getUsername();
}
