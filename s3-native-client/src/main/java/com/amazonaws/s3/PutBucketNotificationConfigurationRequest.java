package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PutBucketNotificationConfigurationRequest {
  private String bucket;

  private NotificationConfiguration notificationConfiguration;

  private String expectedBucketOwner;

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  /**
   * <p>A container for specifying the notification configuration of the bucket. If this element
   *          is empty, notifications are turned off for the bucket.</p>
   */
  public NotificationConfiguration getNotificationConfiguration() {
    return notificationConfiguration;
  }

  /**
   * <p>A container for specifying the notification configuration of the bucket. If this element
   *          is empty, notifications are turned off for the bucket.</p>
   */
  public void setNotificationConfiguration(
      final NotificationConfiguration notificationConfiguration) {
    this.notificationConfiguration = notificationConfiguration;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
