package com.amazonaws.s3;

import java.lang.String;
import java.time.Instant;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class Bucket {
  private String name;

  private Instant creationDate;

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public Instant getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(final Instant creationDate) {
    this.creationDate = creationDate;
  }
}
