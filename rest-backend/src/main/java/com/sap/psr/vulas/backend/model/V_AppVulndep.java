package com.sap.psr.vulas.backend.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true)
@Entity
@IdClass(V_AppVulndep.class)
@Table( name="v_app_vulndep", uniqueConstraints=@UniqueConstraint( columnNames = { "app_id", "dep_id", "lib_id", "digest","bug" } ) ) 
public class V_AppVulndep implements Serializable {
	

	@Id
	private Long app_id;
	
	@Column
	private String app_group;
	
	@Column
	private String app_artifact;
	
	@Column
	private String app_version;

	@Id
	private Long dep_id;
	
	@Id
	private String bug;
	
	@Id
	private Long lib_id;
	
	@Id
	private String digest;
	
	@Column
	private String filename;
	
	@Column
	private String scope;
	
	@Column
	private Boolean transitive;
	
	@Column
	private String mvn_group;
	
	@Column
	private String artifact;
	
	@Column
	private String version;
	
	@Column
	private Boolean affected;

	public V_AppVulndep() { super(); }

	public Long getApp_id() {
		return app_id;
	}

	public void setApp_id(Long app_id) {
		this.app_id = app_id;
	}

	public Long getDep_id() {
		return dep_id;
	}

	public void setDep_id(Long dep_id) {
		this.dep_id = dep_id;
	}
	
	
	public Long getLib_id() {
		return lib_id;
	}

	public void setLib_id(Long lib_id) {
		this.lib_id = lib_id;
	}

	public void setBug(String bug) {
		this.bug = bug;
	}

	public String getApp_group() {
		return app_group;
	}

	public void setApp_group(String app_group) {
		this.app_group = app_group;
	}

	public String getApp_artifact() {
		return app_artifact;
	}

	public void setApp_artifact(String app_artifact) {
		this.app_artifact = app_artifact;
	}

	public String getApp_version() {
		return app_version;
	}

	public void setApp_version(String app_version) {
		this.app_version = app_version;
	}

	public String getBug() {
		return bug;
	}

	public void setBugId(String bug) {
		this.bug = bug;
	}

	public String getDigest() {
		return digest;
	}

	public void setDigest(String digest) {
		this.digest = digest;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getMvn_group() {
		return mvn_group;
	}

	public void setMvn_group(String mvn_group) {
		this.mvn_group = mvn_group;
	}

	public String getArtifact() {
		return artifact;
	}

	public void setArtifact(String artifact) {
		this.artifact = artifact;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Boolean getAffected() {
		return affected;
	}

	public void setAffected(Boolean affected) {
		this.affected = affected;
	}
}
