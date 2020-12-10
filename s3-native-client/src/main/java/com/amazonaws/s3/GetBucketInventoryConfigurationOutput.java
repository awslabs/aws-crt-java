package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetBucketInventoryConfigurationOutput {
  private InventoryConfiguration inventoryConfiguration;

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
