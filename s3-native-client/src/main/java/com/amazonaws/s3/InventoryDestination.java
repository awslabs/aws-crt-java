package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class InventoryDestination {
  private InventoryS3BucketDestination s3BucketDestination;

  /**
   * <p>Contains the bucket name, file format, bucket owner (optional), and prefix (optional)
   *          where inventory results are published.</p>
   */
  public InventoryS3BucketDestination getS3BucketDestination() {
    return s3BucketDestination;
  }

  /**
   * <p>Contains the bucket name, file format, bucket owner (optional), and prefix (optional)
   *          where inventory results are published.</p>
   */
  public void setS3BucketDestination(final InventoryS3BucketDestination s3BucketDestination) {
    this.s3BucketDestination = s3BucketDestination;
  }
}
