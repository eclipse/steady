package com.sap.psr.vulas.kb.model;

public class Artifact {
  private String id;
  private Boolean affected;
  private String reason;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Boolean getAffected() {
    return affected;
  }

  public void setAffected(Boolean affected) {
    this.affected = affected;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}
