package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetObjectTorrentOutput {
  private byte[] body;

  private RequestCharged requestCharged;

  public GetObjectTorrentOutput() {
    this.body = null;
    this.requestCharged = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetObjectTorrentOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetObjectTorrentOutput);
  }

  public byte[] getBody() {
    return body;
  }

  public void setBody(final byte[] body) {
    this.body = body;
  }

  /**
   * <p>If present, indicates that the requester was successfully charged for the
   *          request.</p>
   */
  public String getRequestCharged() {
    return requestCharged;
  }

  /**
   * <p>If present, indicates that the requester was successfully charged for the
   *          request.</p>
   */
  public void setRequestCharged(final String requestCharged) {
    this.requestCharged = requestCharged;
  }
}
