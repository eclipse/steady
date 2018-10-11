package com.sap.psr.vulas.backend.model;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true, value = { "createdAt", "lastModified" }, allowGetters=true) // On allowGetters: https://github.com/FasterXML/jackson-databind/issues/95
@Entity
@Table( name="Tenant", uniqueConstraints=@UniqueConstraint( columnNames = { "tenantToken" } ) )
public class Tenant {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonIgnore
	private Long id;

	@Column(nullable = false, length = 64)
	private String tenantToken = null;
	
	@Column(nullable = false, length = 1024)
	private String tenantName = null;
	
	@Column
	private boolean isDefault = false;
	
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar createdAt;
	
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar lastModified;

	@OneToMany(cascade = { CascadeType.REMOVE }, fetch = FetchType.EAGER, mappedBy = "tenant", orphanRemoval=true)
	@JsonManagedReference
	private Collection<Space> spaces;
	
	public String getTenantToken() { return this.tenantToken; }
	public void setTenantToken(String tenantToken) { this.tenantToken = tenantToken; }
	
	public String getTenantName() { return tenantName; }
	public void setTenantName(String tenantName) { this.tenantName = tenantName; }
	public boolean hasTenantName() { return this.tenantName!=null && !this.tenantName.equals(""); }
	
	public java.util.Calendar getCreatedAt() { return createdAt; }
	public void setCreatedAt(java.util.Calendar createdAt) { this.createdAt = createdAt; }

	public java.util.Calendar getLastModified() { return lastModified; }
	public void setLastModified(java.util.Calendar lastModified) { this.lastModified = lastModified; }

	public Collection<Space> getSpaces() { return spaces; }
	public void setSpaces(Collection<Space> spaces) { this.spaces = spaces; }
	public void addSpace(Space _space) {
		if(this.getSpaces()==null)
			this.spaces = new HashSet<Space>();
		this.spaces.add(_space);
	}
	
	@PrePersist
	public void prePersist() {
		if(this.getCreatedAt()==null)
			this.setCreatedAt(Calendar.getInstance());
		this.setLastModified(Calendar.getInstance());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tenantToken == null) ? 0 : tenantToken.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tenant other = (Tenant) obj;
		if (tenantToken == null) {
			if (other.tenantToken != null)
				return false;
		} else if (!tenantToken.equals(other.tenantToken))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "tenant [token=" + tenantToken + ", name=" + tenantName + "]";
	}
	
	@JsonIgnore
	public boolean isTransient() {
		return this.id==null;
	}
	
	public boolean isDefault() { return isDefault; }
	public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

}
