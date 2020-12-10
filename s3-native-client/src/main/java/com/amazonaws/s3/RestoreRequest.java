package com.amazonaws.s3;

import java.lang.Integer;
import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class RestoreRequest {
  private Integer days;

  private GlacierJobParameters glacierJobParameters;

  private RestoreRequestType type;

  private Tier tier;

  private String description;

  private SelectParameters selectParameters;

  private OutputLocation outputLocation;

  public Integer getDays() {
    return days;
  }

  public void setDays(final Integer days) {
    this.days = days;
  }

  /**
   * <p>Container for S3 Glacier job parameters.</p>
   */
  public GlacierJobParameters getGlacierJobParameters() {
    return glacierJobParameters;
  }

  /**
   * <p>Container for S3 Glacier job parameters.</p>
   */
  public void setGlacierJobParameters(final GlacierJobParameters glacierJobParameters) {
    this.glacierJobParameters = glacierJobParameters;
  }

  public RestoreRequestType getType() {
    return type;
  }

  public void setType(final RestoreRequestType type) {
    this.type = type;
  }

  public Tier getTier() {
    return tier;
  }

  public void setTier(final Tier tier) {
    this.tier = tier;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  /**
   * <p>Describes the parameters for Select job types.</p>
   */
  public SelectParameters getSelectParameters() {
    return selectParameters;
  }

  /**
   * <p>Describes the parameters for Select job types.</p>
   */
  public void setSelectParameters(final SelectParameters selectParameters) {
    this.selectParameters = selectParameters;
  }

  /**
   * <p>Describes the location where the restore job's output is stored.</p>
   */
  public OutputLocation getOutputLocation() {
    return outputLocation;
  }

  /**
   * <p>Describes the location where the restore job's output is stored.</p>
   */
  public void setOutputLocation(final OutputLocation outputLocation) {
    this.outputLocation = outputLocation;
  }
}
