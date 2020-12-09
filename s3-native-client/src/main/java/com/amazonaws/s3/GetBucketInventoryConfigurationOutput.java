package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketInventoryConfigurationOutput {
  private InventoryConfiguration inventoryConfiguration;

  public GetBucketInventoryConfigurationOutput() {
    this.inventoryConfiguration = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketInventoryConfigurationOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketInventoryConfigurationOutput);
  }

  /**
   * <p>Specifies the inventory configuration for an Amazon S3 bucket. For more information, see
   *             <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketGETInventoryConfig.html">GET Bucket inventory</a> in the <i>Amazon Simple Storage Service API Reference</i>.
   *       </p>
   */
  public InventoryConfiguration getInventoryConfiguration() {
    return inventoryConfiguration;
  }

  /**
   * <p>Specifies the inventory configuration for an Amazon S3 bucket. For more information, see
   *             <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketGETInventoryConfig.html">GET Bucket inventory</a> in the <i>Amazon Simple Storage Service API Reference</i>.
   *       </p>
   */
  public void setInventoryConfiguration(final InventoryConfiguration inventoryConfiguration) {
    this.inventoryConfiguration = inventoryConfiguration;
  }
}
