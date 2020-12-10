package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetObjectTorrentOutput {
  private byte[] body;

  private RequestCharged requestCharged;

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
  public RequestCharged getRequestCharged() {
    return requestCharged;
  }

  /**
   * <p>If present, indicates that the requester was successfully charged for the
   *          request.</p>
   */
  public void setRequestCharged(final RequestCharged requestCharged) {
    this.requestCharged = requestCharged;
  }
}
