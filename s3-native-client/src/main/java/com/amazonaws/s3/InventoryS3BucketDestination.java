package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class InventoryS3BucketDestination {
  private String accountId;

  private String bucket;

  private InventoryFormat format;

  private String prefix;

  private InventoryEncryption encryption;

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(final String accountId) {
    this.accountId = accountId;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public InventoryFormat getFormat() {
    return format;
  }

  public void setFormat(final InventoryFormat format) {
    this.format = format;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  /**
   * <p>Contains the type of server-side encryption used to encrypt the inventory
   *          results.</p>
   */
  public InventoryEncryption getEncryption() {
    return encryption;
  }

  /**
   * <p>Contains the type of server-side encryption used to encrypt the inventory
   *          results.</p>
   */
  public void setEncryption(final InventoryEncryption encryption) {
    this.encryption = encryption;
  }
}
