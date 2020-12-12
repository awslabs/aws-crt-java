// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.EmptyStructureGenerator")
public class DeleteBucketWebsiteResponse {
    public DeleteBucketWebsiteResponse() {
    }

    @Override
    public int hashCode() {
        return Objects.hash(DeleteBucketWebsiteResponse.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof DeleteBucketWebsiteResponse);
    }
}
