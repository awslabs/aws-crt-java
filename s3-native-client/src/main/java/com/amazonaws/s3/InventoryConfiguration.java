package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.String;
import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class InventoryConfiguration {
  private InventoryDestination destination;

  private Boolean isEnabled;

  private InventoryFilter filter;

  private String id;

  private InventoryIncludedObjectVersions includedObjectVersions;

  private List<InventoryOptionalField> optionalFields;

  private InventorySchedule schedule;

  /**
   * <p>Specifies the inventory configuration for an Amazon S3 bucket.</p>
   */
  public InventoryDestination getDestination() {
    return destination;
  }

  /**
   * <p>Specifies the inventory configuration for an Amazon S3 bucket.</p>
   */
  public void setDestination(final InventoryDestination destination) {
    this.destination = destination;
  }

  public Boolean isIsEnabled() {
    return isEnabled;
  }

  public void setIsEnabled(final Boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  /**
   * <p>Specifies an inventory filter. The inventory only includes objects that meet the
   *          filter's criteria.</p>
   */
  public InventoryFilter getFilter() {
    return filter;
  }

  /**
   * <p>Specifies an inventory filter. The inventory only includes objects that meet the
   *          filter's criteria.</p>
   */
  public void setFilter(final InventoryFilter filter) {
    this.filter = filter;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public InventoryIncludedObjectVersions getIncludedObjectVersions() {
    return includedObjectVersions;
  }

  public void setIncludedObjectVersions(
      final InventoryIncludedObjectVersions includedObjectVersions) {
    this.includedObjectVersions = includedObjectVersions;
  }

  public List<InventoryOptionalField> getOptionalFields() {
    return optionalFields;
  }

  public void setOptionalFields(final List<InventoryOptionalField> optionalFields) {
    this.optionalFields = optionalFields;
  }

  /**
   * <p>Specifies the schedule for generating inventory results.</p>
   */
  public InventorySchedule getSchedule() {
    return schedule;
  }

  /**
   * <p>Specifies the schedule for generating inventory results.</p>
   */
  public void setSchedule(final InventorySchedule schedule) {
    this.schedule = schedule;
  }
}
