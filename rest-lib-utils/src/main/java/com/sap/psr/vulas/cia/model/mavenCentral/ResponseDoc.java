package com.sap.psr.vulas.cia.model.mavenCentral;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.Artifact;
import com.sap.psr.vulas.shared.json.model.LibraryId;

/**
 * Corresponds to the JSON object structure returned by the RESTful search of the Maven Central.
 * This class is used to de-serialize requests from Maven central and to represent artifacts to be 
 * downloaded from Maven central.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true,value = { "c" })
public class ResponseDoc implements Comparable {
	
	final static Pattern VERSION_PATTERN = Pattern.compile("([\\d\\.]*)(.*)", Pattern.DOTALL);
	
	private static Log log = LogFactory.getLog(ResponseDoc.class);

	private String id;
	
	private String g;
	
	private String a;
	
	private String v;
	
	private String c;
	
	private String p;
	
	private long timestamp;
	
	private Collection<String> ec = null;
	
	public ResponseDoc() {}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	
	public String getG() { return g; }
	public void setG(String g) { this.g = g; }

	public String getA() { return a; }
	public void setA(String a) { this.a = a; }
	
	public String getV() { return v; }
	public void setV(String v) { this.v = v; }

	public String getC() { return c; }
	public void setC(String c) { 
		//TODO check that c is among ec
		this.c = c; 
		}
		
	public String getP() { return p; }
	public void setP(String p) { this.p = p; }

	public long getTimestamp() { return timestamp; }
	public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
	
	public Collection<String> getEc() { return ec; }
	public void setEc(Collection<String> ec) { this.ec = ec; }
	
	@JsonIgnore
	public boolean availableWith(String _classifier, String _packaging) {
		//final String filter_ec = (_classifier!=null && !_classifier.equals("") ? _classifier + "-" : "") + "." + _packaging;
		for(String ec: this.getEc()) {
			
			if(_classifier!=null && _packaging!=null) {
				if(ec.equals("-" + _classifier + "." + _packaging))
					return true;
			}
			else if(_classifier==null && _packaging!=null) {
				if(ec.endsWith("." + _packaging))
					return true;
			}
			else if(_classifier!=null && _packaging==null) {
				if(ec.startsWith("-" + _classifier))
					return true;
			}
			else
				return true;			
		}
		return false;
	}

	/**
	 * Compares the respective timestamps.
	 */
	@Override
	public int compareTo(Object other) {
		if(other instanceof ResponseDoc) {
			final ResponseDoc other_doc = (ResponseDoc)other;
			int c = new Long(this.timestamp).compareTo(Long.valueOf(other_doc.getTimestamp()));
			if(c==0) {
				//c = this.v.compareTo(other_doc.v);
				
				Matcher this_m  = VERSION_PATTERN.matcher(this.v);
				Matcher other_m = VERSION_PATTERN.matcher(other_doc.v);
				
				String this_v = (this_m.matches() ? this_m.group(1) : this.v);
				String other_v = (other_m.matches() ? other_m.group(1) : other_doc.v);
				
				ResponseDoc.log.debug("Compare artifact versions: Original [" + this.v + ", " + other_doc.v + "], modified for comparison [" + this_v + ", " + other_v + "]");
				c = this_v.compareTo(other_v);

				if(c==0) {
					String this_v_tag = (this_m.matches() ? this_m.group(2) : this.v);
					String other_v_tag = (other_m.matches() ? other_m.group(2) : other_doc.v);
				
					if(this_v_tag.equals("") && !other_v_tag.equals(""))
						return 1;
					else if(!this_v_tag.equals("") && other_v_tag.equals(""))
						return -1;
					else{
						ResponseDoc.log.debug("Compare artifact versions: Original [" + this.v + ", " + other_doc.v + "], modified for comparison based on tag (if any)[" + this_v_tag + ", " + other_v_tag + "]");
						//comparison in order to obtain X.X > X.X-RC > X.X-beta
						c = this_v_tag.compareToIgnoreCase(other_v_tag);
					}
				}
			}
			return c;
		}
		else {
			throw new IllegalArgumentException("Expected ResponseDoc, got [" + other.getClass().getName() + "]");
		}
	}
	

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("[").append(id);
		if(this.c!=null && !this.c.equals(""))
			b.append(":").append(this.getC());
		b.append(":").append(p).append("]");
		return b.toString();
	}
	
//	/**
//	 * Returns the path of the directory where the artifact is stored, relative to the local M2 repository.
//	 * E.g, com/jolira/guice/3.0.0.
//	 * @return
//	 */
//	@JsonIgnore
//	private Path getRelM2Dir() {
//		final StringBuilder b = new StringBuilder();
//		b.append(this.getG().replace('.',  '/')).append("/");
//		b.append(this.getA()).append("/");
//		b.append(this.getV());
//		return Paths.get(b.toString());
//	}
//	
//	/**
//     * Returns the artifact's filename root, e.g., guice-3.0.0
//     * To be completed with one of the available postfix in this.ec
//	 * @return
//	 */
//	@JsonIgnore
//	public String getM2Filename() {
//		final StringBuilder b = new StringBuilder();
//		b.append(this.getA()).append("-").append(this.getV());
//		if(this.c!=null && !this.c.equals(""))
//			b.append("-").append(this.getC());
//		b.append(".").append(this.getP());
//		return b.toString();
//	}
	
//	/**
//	 * http://search.maven.org/remotecontent?filepath=com/jolira/guice/3.0.0/guice-3.0.0.pom
//	 * @return
//	 */
//	@JsonIgnore
//	public Path getRelM2Path() {
//		return Paths.get(this.getRelM2Dir().toString(), this.getM2Filename());
//	}	
	
	/**
	 * Returns a {@link LibraryId} corresponding to this {@link ResponseDoc}.
	 * @param _libid
	 * @return
	 */
	public Artifact toArtifact() {
		Artifact r = new Artifact(g,a,v);
		r.setTimestamp(timestamp);
		r.setProgrammingLanguage(ProgrammingLanguage.JAVA);
		return r;
	}
	
}
 