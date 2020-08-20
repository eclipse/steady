package com.sap.psr.vulas.kb.model;

import java.util.List;

public class Note {
  private List<String> links;
  private String text;

  public List<String> getLinks() {
    return links;
  }

  public void setLinks(List<String> links) {
    this.links = links;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}
