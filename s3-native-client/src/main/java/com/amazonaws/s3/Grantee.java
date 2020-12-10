package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class Grantee {
  private String displayName;

  private String emailAddress;

  private String iD;

  private String uRI;

  private Type type;

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(final String displayName) {
    this.displayName = displayName;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(final String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public String getID() {
    return iD;
  }

  public void setID(final String iD) {
    this.iD = iD;
  }

  public String getURI() {
    return uRI;
  }

  public void setURI(final String uRI) {
    this.uRI = uRI;
  }

  public Type getType() {
    return type;
  }

  public void setType(final Type type) {
    this.type = type;
  }
}
