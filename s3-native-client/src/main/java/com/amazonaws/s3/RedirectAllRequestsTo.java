package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class RedirectAllRequestsTo {
  private String hostName;

  private Protocol protocol;

  public String getHostName() {
    return hostName;
  }

  public void setHostName(final String hostName) {
    this.hostName = hostName;
  }

  public Protocol getProtocol() {
    return protocol;
  }

  public void setProtocol(final Protocol protocol) {
    this.protocol = protocol;
  }
}
