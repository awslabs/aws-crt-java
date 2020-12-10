package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class OutputLocation {
  private S3Location s3;

  /**
   * <p>Describes an Amazon S3 location that will receive the results of the restore request.</p>
   */
  public S3Location getS3() {
    return s3;
  }

  /**
   * <p>Describes an Amazon S3 location that will receive the results of the restore request.</p>
   */
  public void setS3(final S3Location s3) {
    this.s3 = s3;
  }
}
