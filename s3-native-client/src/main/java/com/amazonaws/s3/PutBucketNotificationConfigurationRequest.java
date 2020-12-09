package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutBucketNotificationConfigurationRequest {
  private String bucket;

  private NotificationConfiguration notificationConfiguration;

  private String expectedBucketOwner;

  public PutBucketNotificationConfigurationRequest() {
    this.bucket = null;
    this.notificationConfiguration = null;
    this.expectedBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutBucketNotificationConfigurationRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutBucketNotificationConfigurationRequest);
  }

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
