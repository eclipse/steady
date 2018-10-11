package com.sap.psr.vulas;

import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.json.JsonBuilder;
import com.sap.psr.vulas.shared.util.DigestUtil;

/**
 * A programming construct represents a container for programming statements in a given programming language, e.g., a Java constructor or a C function.
 * Programming constructs are created by instances of FileAnalyzer, e.g., JavaFileAnalyzer or JavaClassAnalyzer.
 *
 */
public class Construct {
	
	private static final Log log = LogFactory.getLog(Construct.class);
	private ConstructId id = null;
	private String content, contentDigest = null;
	
	public Construct(ConstructId _id, String _content) {
		if(_id == null || _content==null) throw new IllegalArgumentException("Id and content must be provided");
		this.id = _id;
		this.setContent(_content);		
	}
	public String getDigest() {
		return contentDigest;
	}
	public ConstructId getId() {
		return id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String _content) {
		this.content = _content;
		this.contentDigest = DigestUtil.getDigestAsString(_content, StandardCharsets.UTF_8, DigestAlgorithm.MD5);
	}
	public String toJSON() {
		final JsonBuilder jb = new JsonBuilder();
		jb.startObject();
		jb.appendObjectProperty("id", this.id.toJSON(), false);
		jb.appendObjectProperty("cd", this.contentDigest);
		jb.endObject();
		return jb.getJson();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contentDigest == null) ? 0 : contentDigest.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Construct other = (Construct) obj;
		if (contentDigest == null) {
			if (other.contentDigest != null)
				return false;
		} else if (!contentDigest.equals(other.contentDigest))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	public String toString() {
    	return this.id.toString();
	}
}
