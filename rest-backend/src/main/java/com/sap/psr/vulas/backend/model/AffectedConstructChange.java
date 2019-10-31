package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/** AffectedConstructChange class. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(
    name = "BugAffectedConstructChange",
    indexes = {@Index(name = "affected_lib_cc_index", columnList = "affectedLib")})
public class AffectedConstructChange implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @JsonIgnore
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "BugConstructChange", referencedColumnName = "id")
  private ConstructChange cc;

  @Column(columnDefinition = "text")
  // @Lob
  private String testedBody;

  @Column(columnDefinition = "text")
  // @Lob
  private String vulnBody;

  @Column(columnDefinition = "text")
  // @Lob
  private String fixedBody;

  @ManyToOne(optional = false)
  @JsonBackReference
  @JoinColumn(name = "affectedLib", referencedColumnName = "id")
  private AffectedLibrary affectedLib;

  // this is used to store the result of CHECK_VERSION
  @Column private Boolean affected;

  @Column private String pathGroup;

  @Column private Boolean qnameInJar;

  // this is used to store the result of CHECK_VERSION
  private Boolean inArchive;

  // this is used to store the result of CHECK_VERSION
  private Boolean classInArchive;

  //	@Column
  //	private String astEqual;

  @Column private Integer dtv;

  @Column private Integer dtf;

  // this is used to store the result of CHECK_VERSION
  private Boolean equalChangeType;

  @ManyToMany(
      cascade = {},
      fetch = FetchType.EAGER)
  @JoinColumn(name = "bugAffectedConstructChangeSameBytecode", referencedColumnName = "id")
  private Collection<LibraryId> sameBytecodeLids;

  enum ChangeType {
    ADD,
    DEL,
    MOD,
    NUL
  };

  private ChangeType overall_chg;

  /** Constructor for AffectedConstructChange. */
  public AffectedConstructChange() {
    super();
  }

  /**
   * Constructor for AffectedConstructChange.
   *
   * @param _cc a {@link com.sap.psr.vulas.backend.model.ConstructChange} object.
   * @param _af a {@link com.sap.psr.vulas.backend.model.AffectedLibrary} object.
   * @param _affected a {@link java.lang.Boolean} object.
   * @param _inArch a {@link java.lang.Boolean} object.
   * @param _classinArch a {@link java.lang.Boolean} object.
   * @param _testedBody a {@link java.lang.String} object.
   */
  public AffectedConstructChange(
      ConstructChange _cc,
      AffectedLibrary _af,
      Boolean _affected,
      Boolean _inArch,
      Boolean _classinArch,
      String _testedBody) {
    super();
    this.cc = _cc;
    this.affectedLib = _af;
    this.affected = _affected;
    this.inArchive = _inArch;
    this.classInArchive = _classinArch;
    this.testedBody = _testedBody;
  }

  /**
   * Getter for the field <code>pathGroup</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getPathGroup() {
    return pathGroup;
  }

  /**
   * Setter for the field <code>pathGroup</code>.
   *
   * @param pathGroup a {@link java.lang.String} object.
   */
  public void setPathGroup(String pathGroup) {
    this.pathGroup = pathGroup;
  }

  /**
   * Getter for the field <code>qnameInJar</code>.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getQnameInJar() {
    return qnameInJar;
  }

  /**
   * Setter for the field <code>qnameInJar</code>.
   *
   * @param qnameInJar a {@link java.lang.Boolean} object.
   */
  public void setQnameInJar(Boolean qnameInJar) {
    this.qnameInJar = qnameInJar;
  }

  /**
   * Getter for the field <code>id</code>.
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getId() {
    return id;
  }
  /**
   * Setter for the field <code>id</code>.
   *
   * @param id a {@link java.lang.Long} object.
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Getter for the field <code>cc</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.ConstructChange} object.
   */
  public ConstructChange getCc() {
    return cc;
  }
  /**
   * Setter for the field <code>cc</code>.
   *
   * @param _cc a {@link com.sap.psr.vulas.backend.model.ConstructChange} object.
   */
  public void setCc(ConstructChange _cc) {
    this.cc = _cc;
  }

  /**
   * Getter for the field <code>affectedLib</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.AffectedLibrary} object.
   */
  public AffectedLibrary getAffectedLib() {
    return affectedLib;
  }
  /**
   * Setter for the field <code>affectedLib</code>.
   *
   * @param _al a {@link com.sap.psr.vulas.backend.model.AffectedLibrary} object.
   */
  public void setAffectedLib(AffectedLibrary _al) {
    this.affectedLib = _al;
  }

  /**
   * Getter for the field <code>affected</code>.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getAffected() {
    return affected;
  }
  /**
   * Setter for the field <code>affected</code>.
   *
   * @param affected a {@link java.lang.Boolean} object.
   */
  public void setAffected(Boolean affected) {
    this.affected = affected;
  }

  /**
   * Getter for the field <code>inArchive</code>.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getInArchive() {
    return inArchive;
  }
  /**
   * Setter for the field <code>inArchive</code>.
   *
   * @param _b a {@link java.lang.Boolean} object.
   */
  public void setInArchive(Boolean _b) {
    this.inArchive = _b;
  }

  //	public String getAstEqual() { return this.astEqual; }
  //	public void setAstEqual(String _b) { this.astEqual = _b; }

  /**
   * Getter for the field <code>dtv</code>.
   *
   * @return a {@link java.lang.Integer} object.
   */
  public Integer getDtv() {
    return this.dtv;
  }
  /**
   * Setter for the field <code>dtv</code>.
   *
   * @param _b a {@link java.lang.Integer} object.
   */
  public void setDtv(Integer _b) {
    this.dtv = _b;
  }

  /**
   * Getter for the field <code>dtf</code>.
   *
   * @return a {@link java.lang.Integer} object.
   */
  public Integer getDtf() {
    return this.dtf;
  }
  /**
   * Setter for the field <code>dtf</code>.
   *
   * @param _b a {@link java.lang.Integer} object.
   */
  public void setDtf(Integer _b) {
    this.dtf = _b;
  }

  /**
   * Getter for the field <code>classInArchive</code>.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getClassInArchive() {
    return classInArchive;
  }
  /**
   * Setter for the field <code>classInArchive</code>.
   *
   * @param _b a {@link java.lang.Boolean} object.
   */
  public void setClassInArchive(Boolean _b) {
    this.classInArchive = _b;
  }

  /**
   * Getter for the field <code>equalChangeType</code>.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getEqualChangeType() {
    return equalChangeType;
  }
  /**
   * Setter for the field <code>equalChangeType</code>.
   *
   * @param _b a {@link java.lang.Boolean} object.
   */
  public void setEqualChangeType(Boolean _b) {
    this.equalChangeType = _b;
  }

  /**
   * Getter for the field <code>overall_chg</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType} object.
   */
  public ChangeType getOverall_chg() {
    return overall_chg;
  }
  /**
   * Setter for the field <code>overall_chg</code>.
   *
   * @param _c a {@link com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType} object.
   */
  public void setOverall_chg(ChangeType _c) {
    this.overall_chg = _c;
  }

  /**
   * Getter for the field <code>testedBody</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getTestedBody() {
    return testedBody;
  }
  /**
   * Setter for the field <code>testedBody</code>.
   *
   * @param testedBody a {@link java.lang.String} object.
   */
  public void setTestedBody(String testedBody) {
    this.testedBody = testedBody;
  }

  /**
   * Getter for the field <code>vulnBody</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getVulnBody() {
    return vulnBody;
  }

  /**
   * Setter for the field <code>vulnBody</code>.
   *
   * @param vulnBody a {@link java.lang.String} object.
   */
  public void setVulnBody(String vulnBody) {
    this.vulnBody = vulnBody;
  }

  /**
   * Getter for the field <code>fixedBody</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getFixedBody() {
    return fixedBody;
  }

  /**
   * Setter for the field <code>fixedBody</code>.
   *
   * @param fixedBody a {@link java.lang.String} object.
   */
  public void setFixedBody(String fixedBody) {
    this.fixedBody = fixedBody;
  }

  /**
   * Getter for the field <code>sameBytecodeLids</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<LibraryId> getSameBytecodeLids() {
    return sameBytecodeLids;
  }

  /**
   * Setter for the field <code>sameBytecodeLids</code>.
   *
   * @param sameBytecodeLids a {@link java.util.Collection} object.
   */
  public void setSameBytecodeLids(Collection<LibraryId> sameBytecodeLids) {
    this.sameBytecodeLids = sameBytecodeLids;
  }
}
