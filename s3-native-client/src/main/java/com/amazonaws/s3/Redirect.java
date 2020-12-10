package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class Redirect {
  private String hostName;

  private String httpRedirectCode;

  private Protocol protocol;

  private String replaceKeyPrefixWith;

  private String replaceKeyWith;

  public String getHostName() {
    return hostName;
  }

  public void setHostName(final String hostName) {
    this.hostName = hostName;
  }

  public String getHttpRedirectCode() {
    return httpRedirectCode;
  }

  public void setHttpRedirectCode(final String httpRedirectCode) {
    this.httpRedirectCode = httpRedirectCode;
  }

  public Protocol getProtocol() {
    return protocol;
  }

  public void setProtocol(final Protocol protocol) {
    this.protocol = protocol;
  }

  public String getReplaceKeyPrefixWith() {
    return replaceKeyPrefixWith;
  }

  public void setReplaceKeyPrefixWith(final String replaceKeyPrefixWith) {
    this.replaceKeyPrefixWith = replaceKeyPrefixWith;
  }

  public String getReplaceKeyWith() {
    return replaceKeyWith;
  }

  public void setReplaceKeyWith(final String replaceKeyWith) {
    this.replaceKeyWith = replaceKeyWith;
  }
}
