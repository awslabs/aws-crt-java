package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketWebsiteOutput {
  private RedirectAllRequestsTo redirectAllRequestsTo;

  private IndexDocument indexDocument;

  private ErrorDocument errorDocument;

  private List<RoutingRule> routingRules;

  public GetBucketWebsiteOutput() {
    this.redirectAllRequestsTo = null;
    this.indexDocument = null;
    this.errorDocument = null;
    this.routingRules = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketWebsiteOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketWebsiteOutput);
  }

  /**
   * <p>Specifies the redirect behavior of all requests to a website endpoint of an Amazon S3
   *          bucket.</p>
   */
  public RedirectAllRequestsTo getRedirectAllRequestsTo() {
    return redirectAllRequestsTo;
  }

  /**
   * <p>Specifies the redirect behavior of all requests to a website endpoint of an Amazon S3
   *          bucket.</p>
   */
  public void setRedirectAllRequestsTo(final RedirectAllRequestsTo redirectAllRequestsTo) {
    this.redirectAllRequestsTo = redirectAllRequestsTo;
  }

  /**
   * <p>Container for the <code>Suffix</code> element.</p>
   */
  public IndexDocument getIndexDocument() {
    return indexDocument;
  }

  /**
   * <p>Container for the <code>Suffix</code> element.</p>
   */
  public void setIndexDocument(final IndexDocument indexDocument) {
    this.indexDocument = indexDocument;
  }

  /**
   * <p>The error information.</p>
   */
  public ErrorDocument getErrorDocument() {
    return errorDocument;
  }

  /**
   * <p>The error information.</p>
   */
  public void setErrorDocument(final ErrorDocument errorDocument) {
    this.errorDocument = errorDocument;
  }

  public List<RoutingRule> getRoutingRules() {
    return routingRules;
  }

  public void setRoutingRules(final List<RoutingRule> routingRules) {
    this.routingRules = routingRules;
  }
}
