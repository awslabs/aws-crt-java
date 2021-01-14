// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class InventoryConfiguration {
    /**
     * <p>Contains information about where to publish the inventory results.</p>
     */
    InventoryDestination destination;

    /**
     * <p>Specifies whether the inventory is enabled or disabled. If set to <code>True</code>, an
     *          inventory list is generated. If set to <code>False</code>, no inventory list is
     *          generated.</p>
     */
    Boolean isEnabled;

    /**
     * <p>Specifies an inventory filter. The inventory only includes objects that meet the
     *          filter's criteria.</p>
     */
    InventoryFilter filter;

    /**
     * <p>The ID used to identify the inventory configuration.</p>
     */
    String id;

    /**
     * <p>Object versions to include in the inventory list. If set to <code>All</code>, the list
     *          includes all the object versions, which adds the version-related fields
     *             <code>VersionId</code>, <code>IsLatest</code>, and <code>DeleteMarker</code> to the
     *          list. If set to <code>Current</code>, the list does not contain these version-related
     *          fields.</p>
     */
    InventoryIncludedObjectVersions includedObjectVersions;

    /**
     * <p>Contains the optional fields that are included in the inventory results.</p>
     */
    List<InventoryOptionalField> optionalFields;

    /**
     * <p>Specifies the schedule for generating inventory results.</p>
     */
    InventorySchedule schedule;

    InventoryConfiguration() {
        this.destination = null;
        this.isEnabled = null;
        this.filter = null;
        this.id = "";
        this.includedObjectVersions = null;
        this.optionalFields = null;
        this.schedule = null;
    }

    protected InventoryConfiguration(BuilderImpl builder) {
        this.destination = builder.destination;
        this.isEnabled = builder.isEnabled;
        this.filter = builder.filter;
        this.id = builder.id;
        this.includedObjectVersions = builder.includedObjectVersions;
        this.optionalFields = builder.optionalFields;
        this.schedule = builder.schedule;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(InventoryConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof InventoryConfiguration);
    }

    public InventoryDestination destination() {
        return destination;
    }

    public Boolean isEnabled() {
        return isEnabled;
    }

    public InventoryFilter filter() {
        return filter;
    }

    public String id() {
        return id;
    }

    public InventoryIncludedObjectVersions includedObjectVersions() {
        return includedObjectVersions;
    }

    public List<InventoryOptionalField> optionalFields() {
        return optionalFields;
    }

    public InventorySchedule schedule() {
        return schedule;
    }

    public interface Builder {
        Builder destination(InventoryDestination destination);

        Builder isEnabled(Boolean isEnabled);

        Builder filter(InventoryFilter filter);

        Builder id(String id);

        Builder includedObjectVersions(InventoryIncludedObjectVersions includedObjectVersions);

        Builder optionalFields(List<InventoryOptionalField> optionalFields);

        Builder schedule(InventorySchedule schedule);

        InventoryConfiguration build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Contains information about where to publish the inventory results.</p>
         */
        InventoryDestination destination;

        /**
         * <p>Specifies whether the inventory is enabled or disabled. If set to <code>True</code>, an
         *          inventory list is generated. If set to <code>False</code>, no inventory list is
         *          generated.</p>
         */
        Boolean isEnabled;

        /**
         * <p>Specifies an inventory filter. The inventory only includes objects that meet the
         *          filter's criteria.</p>
         */
        InventoryFilter filter;

        /**
         * <p>The ID used to identify the inventory configuration.</p>
         */
        String id;

        /**
         * <p>Object versions to include in the inventory list. If set to <code>All</code>, the list
         *          includes all the object versions, which adds the version-related fields
         *             <code>VersionId</code>, <code>IsLatest</code>, and <code>DeleteMarker</code> to the
         *          list. If set to <code>Current</code>, the list does not contain these version-related
         *          fields.</p>
         */
        InventoryIncludedObjectVersions includedObjectVersions;

        /**
         * <p>Contains the optional fields that are included in the inventory results.</p>
         */
        List<InventoryOptionalField> optionalFields;

        /**
         * <p>Specifies the schedule for generating inventory results.</p>
         */
        InventorySchedule schedule;

        protected BuilderImpl() {
        }

        private BuilderImpl(InventoryConfiguration model) {
            destination(model.destination);
            isEnabled(model.isEnabled);
            filter(model.filter);
            id(model.id);
            includedObjectVersions(model.includedObjectVersions);
            optionalFields(model.optionalFields);
            schedule(model.schedule);
        }

        public InventoryConfiguration build() {
            return new InventoryConfiguration(this);
        }

        public final Builder destination(InventoryDestination destination) {
            this.destination = destination;
            return this;
        }

        public final Builder isEnabled(Boolean isEnabled) {
            this.isEnabled = isEnabled;
            return this;
        }

        public final Builder filter(InventoryFilter filter) {
            this.filter = filter;
            return this;
        }

        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        public final Builder includedObjectVersions(
                InventoryIncludedObjectVersions includedObjectVersions) {
            this.includedObjectVersions = includedObjectVersions;
            return this;
        }

        public final Builder optionalFields(List<InventoryOptionalField> optionalFields) {
            this.optionalFields = optionalFields;
            return this;
        }

        public final Builder schedule(InventorySchedule schedule) {
            this.schedule = schedule;
            return this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(BuilderImpl.class);
        }

        @Override
        public boolean equals(Object rhs) {
            if (rhs == null) return false;
            return (rhs instanceof BuilderImpl);
        }

        public InventoryDestination destination() {
            return destination;
        }

        public Boolean isEnabled() {
            return isEnabled;
        }

        public InventoryFilter filter() {
            return filter;
        }

        public String id() {
            return id;
        }

        public InventoryIncludedObjectVersions includedObjectVersions() {
            return includedObjectVersions;
        }

        public List<InventoryOptionalField> optionalFields() {
            return optionalFields;
        }

        public InventorySchedule schedule() {
            return schedule;
        }
    }
}
