/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.patcheval.representation;

import com.sap.psr.vulas.shared.enums.ConstructChangeType;

/**
 * Contains the results for a certain construct,path and library Id (i.e., for a line of the CSV
 * file)
 */
public class ConstructPathLibResult2 {
  private String qname;
  private String path;
  private ConstructChangeType type;
  private LidResult2 lidResult;
  private String vulnAst;
  private String fixedAst;

  /**
   * Constructor for ConstructPathLibResult2.
   *
   * @param _qname a {@link java.lang.String} object.
   * @param _path a {@link java.lang.String} object.
   * @param constructChangeType a {@link com.sap.psr.vulas.shared.enums.ConstructChangeType} object.
   * @param _lidres a {@link com.sap.psr.vulas.patcheval.representation.LidResult2} object.
   * @param _v a {@link java.lang.String} object.
   * @param _f a {@link java.lang.String} object.
   */
  public ConstructPathLibResult2(
      String _qname,
      String _path,
      ConstructChangeType constructChangeType,
      LidResult2 _lidres,
      String _v,
      String _f) {
    this.qname = _qname;
    this.path = _path;
    this.type = constructChangeType;
    this.lidResult = _lidres;
    this.vulnAst = _v;
    this.fixedAst = _f;
  }

  /**
   * Getter for the field <code>qname</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getQname() {
    return qname;
  }

  /**
   * Setter for the field <code>qname</code>.
   *
   * @param qname a {@link java.lang.String} object.
   */
  public void setQname(String qname) {
    this.qname = qname;
  }

  /**
   * Getter for the field <code>vulnAst</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getVulnAst() {
    return vulnAst;
  }

  /**
   * Setter for the field <code>vulnAst</code>.
   *
   * @param vulnAst a {@link java.lang.String} object.
   */
  public void setVulnAst(String vulnAst) {
    this.vulnAst = vulnAst;
  }

  /**
   * Getter for the field <code>fixedAst</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFixedAst() {
    return fixedAst;
  }

  /**
   * Setter for the field <code>fixedAst</code>.
   *
   * @param fixedAst a {@link java.lang.String} object.
   */
  public void setFixedAst(String fixedAst) {
    this.fixedAst = fixedAst;
  }

  /**
   * Getter for the field <code>path</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getPath() {
    return path;
  }

  /**
   * Setter for the field <code>path</code>.
   *
   * @param path a {@link java.lang.String} object.
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Getter for the field <code>type</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.ConstructChangeType} object.
   */
  public ConstructChangeType getType() {
    return type;
  }

  /**
   * Setter for the field <code>type</code>.
   *
   * @param t a {@link com.sap.psr.vulas.shared.enums.ConstructChangeType} object.
   */
  public void setType(ConstructChangeType t) {
    this.type = t;
  }

  /**
   * Getter for the field <code>lidResult</code>.
   *
   * @return a {@link com.sap.psr.vulas.patcheval.representation.LidResult2} object.
   */
  public LidResult2 getLidResult() {
    return lidResult;
  }

  /**
   * Setter for the field <code>lidResult</code>.
   *
   * @param lidResult a {@link com.sap.psr.vulas.patcheval.representation.LidResult2} object.
   */
  public void setLidResult(LidResult2 lidResult) {
    this.lidResult = lidResult;
  }

  //    @Override
  //    public String toString(){
  //        StringBuilder sb = new StringBuilder();
  //        sb.append("Qname: ").append(qname).append("; path: ").append(path).append("; lidResults:
  // ");
  //        for ( LidResult lr : lidResults) {
  //            sb.append("\n");
  //            sb.append(lr.toString()).append(" ,");
  //        }
  //        return sb.toString();
  //    }
}
