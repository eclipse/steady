package com.sap.psr.vulas.patcheval.representation;

import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.json.model.LibraryId;
import java.util.ArrayList;
import java.util.List;

/** Class used for the results obtained for a construct,path. */
public class ConstructPathAssessment2 {

  String construct;
  String path;

  ConstructType constructType;

  String type;

  Boolean qnameInJAR;
  // Boolean sources;
  String ast;
  String vulnAst;
  String fixedAst;

  Integer dToV, dToF;

  Integer doneComparisons;

  List<LibraryId> libsSameBytecode;

  /**
   * Constructor for ConstructPathAssessment2.
   *
   * @param _construct a {@link java.lang.String} object.
   * @param _path a {@link java.lang.String} object.
   * @param _ctype a {@link com.sap.psr.vulas.shared.enums.ConstructType} object.
   * @param _q a {@link java.lang.Boolean} object.
   * @param _ast a {@link java.lang.String} object.
   * @param _dToV a int.
   * @param _dToF a int.
   * @param _t a {@link java.lang.String} object.
   * @param _vast a {@link java.lang.String} object.
   * @param _fast a {@link java.lang.String} object.
   */
  public ConstructPathAssessment2(
      String _construct,
      String _path,
      ConstructType _ctype,
      Boolean _q,
      String _ast,
      int _dToV,
      int _dToF,
      String _t,
      String _vast,
      String _fast) {

    this.ast = _ast;
    this.vulnAst = _vast;
    this.fixedAst = _fast;
    this.construct = _construct;
    this.qnameInJAR = _q;
    this.path = _path;
    this.constructType = _ctype;
    //   this.sources = _sources;
    this.dToF = _dToF;
    this.dToV = _dToV;
    this.type = _t;
  }

  /**
   * Constructor for ConstructPathAssessment2.
   *
   * @param _construct a {@link java.lang.String} object.
   * @param _path a {@link java.lang.String} object.
   * @param _q a {@link java.lang.Boolean} object.
   * @param _t a {@link java.lang.String} object.
   */
  public ConstructPathAssessment2(String _construct, String _path, Boolean _q, String _t) {

    this.ast = null;
    this.vulnAst = null;
    this.fixedAst = null;
    this.construct = _construct;
    this.qnameInJAR = _q;
    this.path = _path;
    //   this.sources = _sources;
    this.dToF = null;
    this.dToV = null;
    this.type = _t;
  }

  /**
   * Getter for the field <code>construct</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getConstruct() {
    return construct;
  }

  /**
   * Setter for the field <code>construct</code>.
   *
   * @param construct a {@link java.lang.String} object.
   */
  public void setConstruct(String construct) {
    this.construct = construct;
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
   * Getter for the field <code>constructType</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.ConstructType} object.
   */
  public ConstructType getConstructType() {
    return constructType;
  }

  /**
   * Setter for the field <code>constructType</code>.
   *
   * @param ct a {@link com.sap.psr.vulas.shared.enums.ConstructType} object.
   */
  public void setConstructType(ConstructType ct) {
    this.constructType = ct;
  }

  /**
   * getQnameInJar.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getQnameInJar() {
    return qnameInJAR;
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
   * setQnameInJar.
   *
   * @param q a {@link java.lang.Boolean} object.
   */
  public void setQnameInJar(Boolean q) {
    this.qnameInJAR = q;
  }

  /**
   * Getter for the field <code>dToV</code>.
   *
   * @return a {@link java.lang.Integer} object.
   */
  public Integer getdToV() {
    return dToV;
  }

  /**
   * Setter for the field <code>dToV</code>.
   *
   * @param dToV a int.
   */
  public void setdToV(int dToV) {
    this.dToV = dToV;
  }

  /**
   * Getter for the field <code>dToF</code>.
   *
   * @return a {@link java.lang.Integer} object.
   */
  public Integer getdToF() {
    return dToF;
  }

  /**
   * Setter for the field <code>dToF</code>.
   *
   * @param dToF a int.
   */
  public void setdToF(int dToF) {
    this.dToF = dToF;
  }

  /**
   * Getter for the field <code>ast</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getAst() {
    return ast;
  }

  /**
   * Setter for the field <code>ast</code>.
   *
   * @param ast a {@link java.lang.String} object.
   */
  public void setAst(String ast) {
    this.ast = ast;
  }

  /**
   * Getter for the field <code>type</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getType() {
    return type;
  }

  /**
   * Setter for the field <code>type</code>.
   *
   * @param type a {@link java.lang.String} object.
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Getter for the field <code>libsSameBytecode</code>.
   *
   * @return a {@link java.util.List} object.
   */
  public List<LibraryId> getLibsSameBytecode() {
    return libsSameBytecode;
  }

  /**
   * setLibsSameBytecodeAsString.
   *
   * @param libsSameBytecode an array of {@link java.lang.String} objects.
   */
  public void setLibsSameBytecodeAsString(String[] libsSameBytecode) {
    if (this.libsSameBytecode == null) this.libsSameBytecode = new ArrayList<LibraryId>();
    for (int i = 0; i < libsSameBytecode.length; i++) {
      String[] l = libsSameBytecode[i].split(":");
      this.libsSameBytecode.add(new LibraryId(l[0], l[1], l[2]));
    }
  }

  /**
   * Setter for the field <code>libsSameBytecode</code>.
   *
   * @param libsSameBytecode a {@link java.util.List} object.
   */
  public void setLibsSameBytecode(List<LibraryId> libsSameBytecode) {
    this.libsSameBytecode = libsSameBytecode;
  }

  /**
   * Getter for the field <code>doneComparisons</code>.
   *
   * @return a {@link java.lang.Integer} object.
   */
  public Integer getDoneComparisons() {
    return doneComparisons;
  }

  /**
   * Setter for the field <code>doneComparisons</code>.
   *
   * @param doneComparisons a {@link java.lang.Integer} object.
   */
  public void setDoneComparisons(Integer doneComparisons) {
    this.doneComparisons = doneComparisons;
  }
}
