package com.amazonaws.s3;

import java.lang.Integer;
import java.lang.String;
import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class CORSRule {
  private List<String> allowedHeaders;

  private List<String> allowedMethods;

  private List<String> allowedOrigins;

  private List<String> exposeHeaders;

  private Integer maxAgeSeconds;

  public List<String> getAllowedHeaders() {
    return allowedHeaders;
  }

  public void setAllowedHeaders(final List<String> allowedHeaders) {
    this.allowedHeaders = allowedHeaders;
  }

  public List<String> getAllowedMethods() {
    return allowedMethods;
  }

  public void setAllowedMethods(final List<String> allowedMethods) {
    this.allowedMethods = allowedMethods;
  }

  public List<String> getAllowedOrigins() {
    return allowedOrigins;
  }

  public void setAllowedOrigins(final List<String> allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }

  public List<String> getExposeHeaders() {
    return exposeHeaders;
  }

  public void setExposeHeaders(final List<String> exposeHeaders) {
    this.exposeHeaders = exposeHeaders;
  }

  public Integer getMaxAgeSeconds() {
    return maxAgeSeconds;
  }

  public void setMaxAgeSeconds(final Integer maxAgeSeconds) {
    this.maxAgeSeconds = maxAgeSeconds;
  }
}
