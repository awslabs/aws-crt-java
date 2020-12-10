package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class OwnershipControlsRule {
  private ObjectOwnership objectOwnership;

  /**
   * <p>The container element for object ownership for a bucket's ownership controls.</p>
   *          <p>BucketOwnerPreferred - Objects uploaded to the bucket change ownership to the bucket
   *          owner if the objects are uploaded with the <code>bucket-owner-full-control</code> canned
   *          ACL.</p>
   *          <p>ObjectWriter - The uploading account will own the object if the object is uploaded with
   *          the <code>bucket-owner-full-control</code> canned ACL.</p>
   */
  public ObjectOwnership getObjectOwnership() {
    return objectOwnership;
  }

  /**
   * <p>The container element for object ownership for a bucket's ownership controls.</p>
   *          <p>BucketOwnerPreferred - Objects uploaded to the bucket change ownership to the bucket
   *          owner if the objects are uploaded with the <code>bucket-owner-full-control</code> canned
   *          ACL.</p>
   *          <p>ObjectWriter - The uploading account will own the object if the object is uploaded with
   *          the <code>bucket-owner-full-control</code> canned ACL.</p>
   */
  public void setObjectOwnership(final ObjectOwnership objectOwnership) {
    this.objectOwnership = objectOwnership;
  }
}
