package com.amazonaws.s3;

import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetBucketWebsiteOutput {
  private RedirectAllRequestsTo redirectAllRequestsTo;

  private IndexDocument indexDocument;

  private ErrorDocument errorDocument;

  private List<RoutingRule> routingRules;

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
