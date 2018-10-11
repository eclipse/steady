package com.sap.psr.vulas.backend.model;

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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table( name="BugAffectedConstructChange",
		indexes = {@Index(name="affected_lib_cc_index",  columnList = "affectedLib")})
public class AffectedConstructChange implements Serializable{
	
	private static final long serialVersionUID = 1L;
	

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long id;


	@ManyToOne(optional = false)
	@JoinColumn(name = "BugConstructChange", referencedColumnName = "id") 
	private ConstructChange cc;
	
	@Column(columnDefinition = "text")
	//@Lob
	private String testedBody;
	
	@Column(columnDefinition = "text")
	//@Lob
	private String vulnBody;
	
	@Column(columnDefinition = "text")
	//@Lob
	private String fixedBody;
	
	@ManyToOne(optional = false)
	@JsonBackReference
	@JoinColumn(name = "affectedLib", referencedColumnName = "id") 
	private AffectedLibrary affectedLib;
	
	//this is used to store the result of CHECK_VERSION
	@Column
	private Boolean affected;
	
	@Column
	private String pathGroup;
	
	@Column
	private Boolean qnameInJar;
	
	//this is used to store the result of CHECK_VERSION
	private Boolean inArchive;
	
	//this is used to store the result of CHECK_VERSION
	private Boolean classInArchive;
	
//	@Column
//	private String astEqual;
	
	@Column
	private Integer dtv;
	
	@Column
	private Integer dtf;

	//this is used to store the result of CHECK_VERSION
	private Boolean equalChangeType; 
	
	@ManyToMany(cascade = {}, fetch = FetchType.EAGER)
	@JoinColumn(name = "bugAffectedConstructChangeSameBytecode", referencedColumnName = "id") 
	private Collection<LibraryId> sameBytecodeLids;
	
	enum ChangeType {ADD, DEL, MOD, NUL};
	
	private ChangeType overall_chg;
	
	public AffectedConstructChange () { super();}
	
	public AffectedConstructChange(ConstructChange _cc, AffectedLibrary _af, Boolean _affected, Boolean _inArch, Boolean _classinArch, String _testedBody) {
		super();
		this.cc = _cc;
		this.affectedLib = _af;
		this.affected = _affected;
		this.inArchive = _inArch;
		this.classInArchive = _classinArch;
		this.testedBody = _testedBody;
	}
	
	
	public String getPathGroup() {
		return pathGroup;
	}

	public void setPathGroup(String pathGroup) {
		this.pathGroup = pathGroup;
	}

	public Boolean getQnameInJar() {
		return qnameInJar;
	}

	public void setQnameInJar(Boolean qnameInJar) {
		this.qnameInJar = qnameInJar;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public ConstructChange getCc() { return cc; } 
	public void setCc(ConstructChange _cc) { this.cc = _cc; }

	
	public AffectedLibrary getAffectedLib() { return affectedLib; } 
	public void setAffectedLib(AffectedLibrary _al) { this.affectedLib = _al; }
	
	public Boolean getAffected() { return affected; }
	public void setAffected(Boolean affected) { this.affected = affected; }
	
	public Boolean getInArchive() { return inArchive; }
	public void setInArchive(Boolean _b) { this.inArchive = _b; }
	
//	public String getAstEqual() { return this.astEqual; }
//	public void setAstEqual(String _b) { this.astEqual = _b; }
	
	
	public Integer getDtv() { return this.dtv; }
	public void setDtv(Integer _b) { this.dtv = _b; }
	
	
	public Integer getDtf() { return this.dtf; }
	public void setDtf(Integer _b) { this.dtf = _b; }
	
	public Boolean getClassInArchive() { return classInArchive; }
	public void setClassInArchive(Boolean _b) { this.classInArchive = _b; }
	
	public Boolean getEqualChangeType() { return equalChangeType; }
	public void setEqualChangeType(Boolean _b) { this.equalChangeType = _b; }
	
	public ChangeType getOverall_chg() { return overall_chg; }
	public void setOverall_chg(ChangeType _c) { this.overall_chg = _c; }
	
	public String getTestedBody() { return testedBody; }
	public void setTestedBody(String testedBody) { this.testedBody = testedBody; }

	public String getVulnBody() {
		return vulnBody;
	}

	public void setVulnBody(String vulnBody) {
		this.vulnBody = vulnBody;
	}

	public String getFixedBody() {
		return fixedBody;
	}

	public void setFixedBody(String fixedBody) {
		this.fixedBody = fixedBody;
	}

	public Collection<LibraryId> getSameBytecodeLids() {
		return sameBytecodeLids;
	}

	public void setSameBytecodeLids(Collection<LibraryId> sameBytecodeLids) {
		this.sameBytecodeLids = sameBytecodeLids;
	}	
}
