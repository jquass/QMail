package email.quass.qmail.data.aws;

import email.quass.qmail.core.QMailEnv;
import email.quass.qmail.core.email.S3MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.encryption.s3.S3EncryptionClient;
import software.amazon.encryption.s3.materials.KmsKeyring;

public class EmailS3Client {

  private static final Logger LOG = LoggerFactory.getLogger(EmailS3Client.class);

  private static final Region REGION = Region.of(QMailEnv.REGION.asString());
  private static final S3Client S3_CLIENT = S3Client.builder().region(REGION).build();
  private static final AwsCredentialsProvider DEFAULT_CREDENTIALS =
      DefaultCredentialsProvider.create();
  private static final KmsClient KMS_CLIENT =
      KmsClient.builder().credentialsProvider(DEFAULT_CREDENTIALS).region(REGION).build();
  private static final KmsKeyring KMS_KEYRING =
      KmsKeyring.builder().kmsClient(KMS_CLIENT).enableLegacyWrappingAlgorithms(true).build();
  private static final S3Client S3_CLIENT_ENCRYPTED =
      S3EncryptionClient.builder().wrappedClient(S3_CLIENT).keyring(KMS_KEYRING).build();
  private static final String BUCKET_INBOX = QMailEnv.S3_BUCKET_INBOX.asString();
  private static final String BUCKET_ARCHIVE = QMailEnv.S3_BUCKET_ARCHIVE.asString();

  public EmailS3Client() {}

  public static List<S3MimeMessage> listNew() {
    ListObjectsResponse listObjectsResponse =
        S3_CLIENT_ENCRYPTED.listObjects(ListObjectsRequest.builder().bucket(BUCKET_INBOX).build());
    List<S3MimeMessage> messages = new ArrayList<>();
    for (S3Object s3Object : listObjectsResponse.contents()) {
      LOG.info("Processing message {}", s3Object.key());
      GetObjectRequest request =
          GetObjectRequest.builder().bucket(BUCKET_INBOX).key(s3Object.key()).build();
      ResponseInputStream<GetObjectResponse> getObjectResponse =
          S3_CLIENT_ENCRYPTED.getObject(request);
      Session session = Session.getInstance(new Properties());
      MimeMessage mimeMessage;
      try {
        InputStream stream = new ByteArrayInputStream(getObjectResponse.readAllBytes());
        mimeMessage = new MimeMessage(session, stream);
      } catch (IOException | MessagingException e) {
        throw new RuntimeException(e);
      }
      messages.add(
          S3MimeMessage.builder().setS3Object(s3Object).setMimeMessage(mimeMessage).build());
    }
    return messages;
  }

  public static void archiveS3MimeMessage(S3MimeMessage s3MimeMessage, String recipient) {
    String user = recipient.substring(0, recipient.indexOf("@"));

    CopyObjectRequest copyObjectRequest =
        CopyObjectRequest.builder()
            .sourceBucket(BUCKET_INBOX)
            .sourceKey(s3MimeMessage.getS3Object().key())
            .destinationBucket(BUCKET_ARCHIVE)
            .destinationKey(user + "/" + s3MimeMessage.getS3Object().key())
            .build();
    S3_CLIENT.copyObject(copyObjectRequest);

    DeleteObjectRequest deleteObjectRequest =
        DeleteObjectRequest.builder()
            .bucket(BUCKET_INBOX)
            .key(s3MimeMessage.getS3Object().key())
            .build();
    S3_CLIENT.deleteObject(deleteObjectRequest);
  }
}
