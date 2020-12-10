package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class RoutingRule {
  private Condition condition;

  private Redirect redirect;

  /**
   * <p>A container for describing a condition that must be met for the specified redirect to
   *          apply. For example, 1. If request is for pages in the <code>/docs</code> folder, redirect
   *          to the <code>/documents</code> folder. 2. If request results in HTTP error 4xx, redirect
   *          request to another host where you might process the error.</p>
   */
  public Condition getCondition() {
    return condition;
  }

  /**
   * <p>A container for describing a condition that must be met for the specified redirect to
   *          apply. For example, 1. If request is for pages in the <code>/docs</code> folder, redirect
   *          to the <code>/documents</code> folder. 2. If request results in HTTP error 4xx, redirect
   *          request to another host where you might process the error.</p>
   */
  public void setCondition(final Condition condition) {
    this.condition = condition;
  }

  /**
   * <p>Specifies how requests are redirected. In the event of an error, you can specify a
   *          different error code to return.</p>
   */
  public Redirect getRedirect() {
    return redirect;
  }

  /**
   * <p>Specifies how requests are redirected. In the event of an error, you can specify a
   *          different error code to return.</p>
   */
  public void setRedirect(final Redirect redirect) {
    this.redirect = redirect;
  }
}
