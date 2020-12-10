package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PutBucketInventoryConfigurationRequest {
  private String bucket;

  private String id;

  private InventoryConfiguration inventoryConfiguration;

  private String expectedBucketOwner;

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
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

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
