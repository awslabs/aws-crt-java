package com.amazonaws.s3;

import java.lang.String;
import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class IntelligentTieringConfiguration {
  private String id;

  private IntelligentTieringFilter filter;

  private IntelligentTieringStatus status;

  private List<Tiering> tierings;

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  /**
   * <p>The <code>Filter</code> is used to identify objects that the S3 Intelligent-Tiering
   *          configuration applies to.</p>
   */
  public IntelligentTieringFilter getFilter() {
    return filter;
  }

  /**
   * <p>The <code>Filter</code> is used to identify objects that the S3 Intelligent-Tiering
   *          configuration applies to.</p>
   */
  public void setFilter(final IntelligentTieringFilter filter) {
    this.filter = filter;
  }

  public IntelligentTieringStatus getStatus() {
    return status;
  }

  public void setStatus(final IntelligentTieringStatus status) {
    this.status = status;
  }

  public List<Tiering> getTierings() {
    return tierings;
  }

  public void setTierings(final List<Tiering> tierings) {
    this.tierings = tierings;
  }
}
