package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PutBucketTaggingRequest {
  private String bucket;

  private String contentMD5;

  private Tagging tagging;

  private String expectedBucketOwner;

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getContentMD5() {
    return contentMD5;
  }

  public void setContentMD5(final String contentMD5) {
    this.contentMD5 = contentMD5;
  }

  /**
   * <p>Container for <code>TagSet</code> elements.</p>
   */
  public Tagging getTagging() {
    return tagging;
  }

  /**
   * <p>Container for <code>TagSet</code> elements.</p>
   */
  public void setTagging(final Tagging tagging) {
    this.tagging = tagging;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
