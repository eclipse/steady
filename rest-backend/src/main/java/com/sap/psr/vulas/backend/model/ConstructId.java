package com.sap.psr.vulas.backend.model;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.backend.repo.ConstructIdRepository;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table( name="ConstructId",
		uniqueConstraints=@UniqueConstraint( columnNames = { "lang", "type", "qname" } ),
		indexes = {@Index(name="cid_lang_index",  columnList = "lang"),
				   @Index(name="cid_type_index",  columnList = "type"),
				   @Index(name="cid_qname_index", columnList = "qname"),
				   @Index(name="cid_index",       columnList = "lang, type, qname" )})
public class ConstructId implements Serializable, Comparable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long id;

	@Column(nullable = false, length = 4)
	@Enumerated(EnumType.STRING)
	private ProgrammingLanguage lang;

	@Column(nullable = false, length = 4)
	@Enumerated(EnumType.STRING)
	private ConstructType type;

	@Column(nullable = false, length = 3072)
	private String qname;
	
	public ConstructId() { super(); }

	public ConstructId(ProgrammingLanguage lang, ConstructType type, String qname) {
		super();
		this.lang = lang;
		this.type = type;
		this.qname = qname;
	}
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; } 

	public ProgrammingLanguage getLang() { return lang; }
	public void setLang(ProgrammingLanguage lang) { this.lang = lang; }

	public ConstructType getType() { return type; }
	public void setType(ConstructType type) { this.type = type; }

	public String getQname() { return qname; }
	public void setQname(String qname) { this.qname = qname; }

	public final String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("[").append(this.getLang()).append(":").append(this.getType()).append(":").append(this.getQname()).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lang == null) ? 0 : lang.hashCode());
		result = prime * result + ((qname == null) ? 0 : qname.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		ConstructId other = (ConstructId) obj;
		if (lang == null) {
			if (other.lang != null)
				return false;
		} else if (!lang.equals(other.lang))
			return false;
		if (qname == null) {
			if (other.qname != null)
				return false;
		} else if (!qname.equals(other.qname))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public int compareTo(Object _other) {
		if(_other==null || !(_other instanceof ConstructId))
			throw new IllegalArgumentException();
		return this.getQname().compareTo(((ConstructId)_other).getQname());
	}
	
	
	
	/**
	 * Returns a {@link ConstructId} of type {@link ConstructType#PACK} in which the given construct ID has been declared.
	 * @param _cid
	 * @return
	 */
	public static ConstructId getPackageOf(ConstructId _cid) {
		ConstructId pid = null;
		
		if(_cid.getType()==ConstructType.PACK)
			pid = _cid;
		
		else if(_cid.getType()==ConstructType.CLAS || _cid.getType()==ConstructType.ENUM || _cid.getType()==ConstructType.INTF || 
				_cid.getType()==ConstructType.MODU) {
			final int idx = _cid.getQname().lastIndexOf(".");
			if(idx==-1)
				pid = new ConstructId(_cid.getLang(), ConstructType.PACK, "");
			else
				pid = new ConstructId(_cid.getLang(), ConstructType.PACK, _cid.getQname().substring(0, idx));
		}
		else if(_cid.getType()==ConstructType.INIT || _cid.getType()==ConstructType.METH  
				|| (_cid.getType()==ConstructType.CONS && _cid.getLang()==ProgrammingLanguage.PY)) {
			int idx = _cid.getQname().lastIndexOf(".");
			final String ctx = _cid.getQname().substring(0, idx);
			idx = ctx.lastIndexOf(".");
			if(idx==-1)
				pid = new ConstructId(_cid.getLang(), ConstructType.PACK, "");
			else
				pid = new ConstructId(_cid.getLang(), ConstructType.PACK, ctx.substring(0, idx));
		}	
		else if (_cid.getType()==ConstructType.FUNC){
			int idx = _cid.getQname().lastIndexOf(".");
			String ctx = _cid.getQname().substring(0, idx);
			while(ctx.contains("(") && ctx.lastIndexOf(".")!=-1){
				ctx = ctx.substring(0, ctx.lastIndexOf("."));
			}
			if(idx==-1)
				pid = new ConstructId(_cid.getLang(), ConstructType.PACK, "");
			else
				pid = new ConstructId(_cid.getLang(), ConstructType.PACK, ctx);
		}
		else if(_cid.getType()==ConstructType.CONS && _cid.getLang()==ProgrammingLanguage.JAVA) {
			final int idx = _cid.getQname().lastIndexOf(".");
			if(idx==-1)
				pid = new ConstructId(_cid.getLang(), ConstructType.PACK, "");
			else
				pid = new ConstructId(_cid.getLang(), ConstructType.PACK, _cid.getQname().substring(0, idx));
		}
		return pid;
	}
	
	public com.sap.psr.vulas.shared.json.model.ConstructId toSharedType() {
		final com.sap.psr.vulas.shared.json.model.ConstructId shared_type = new com.sap.psr.vulas.shared.json.model.ConstructId();
		shared_type.setLang(this.getLang());
		shared_type.setType(this.getType());
		shared_type.setQname(this.getQname());
		return shared_type;
	}
}