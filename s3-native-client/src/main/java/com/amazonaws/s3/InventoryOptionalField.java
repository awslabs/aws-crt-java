package com.amazonaws.s3;

import static java.util.stream.Collectors.toSet;

import java.lang.String;
import java.util.Set;
import java.util.stream.Stream;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.EnumGenerator")
public enum InventoryOptionalField {
  SIZE("Size"),

  LAST_MODIFIED_DATE("LastModifiedDate"),

  STORAGE_CLASS("StorageClass"),

  E_TAG("ETag"),

  IS_MULTIPART_UPLOADED("IsMultipartUploaded"),

  REPLICATION_STATUS("ReplicationStatus"),

  ENCRYPTION_STATUS("EncryptionStatus"),

  OBJECT_LOCK_RETAIN_UNTIL_DATE("ObjectLockRetainUntilDate"),

  OBJECT_LOCK_MODE("ObjectLockMode"),

  OBJECT_LOCK_LEGAL_HOLD_STATUS("ObjectLockLegalHoldStatus"),

  INTELLIGENT_TIERING_ACCESS_TIER("IntelligentTieringAccessTier"),

  UNKNOWN_TO_SDK_VERSION("UNKNOWN_TO_SDK_VERSION");

  private final String value;

  private InventoryOptionalField(String value) {
    this.value = value;
  }

  public static InventoryOptionalField fromValue(String value) {
    if (value == null) {
      return null;
    }
    return Stream.of(com.amazonaws.s3.InventoryOptionalField.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
  }

  public static Set<InventoryOptionalField> knownValues() {
    return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(toSet());
  }
}
