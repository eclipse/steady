package com.sap.psr.vulas.cia.model.mavenCentral;

import java.util.Collection;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Corresponds to the JSON object structure returned by the RESTful search of the Maven Central.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MavenSearchResponse {

	private long numFound;
	
	private Collection<ResponseDoc> docs;
	
	/**
	 * <p>Constructor for MavenSearchResponse.</p>
	 */
	public MavenSearchResponse() {}

	/**
	 * <p>Getter for the field <code>numFound</code>.</p>
	 *
	 * @return a long.
	 */
	public long getNumFound() { return numFound; }
	/**
	 * <p>Setter for the field <code>numFound</code>.</p>
	 *
	 * @param numFound a long.
	 */
	public void setNumFound(long numFound) { this.numFound = numFound; }

	/**
	 * <p>Getter for the field <code>docs</code>.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<ResponseDoc> getDocs() { return docs; }
	/**
	 * <p>Setter for the field <code>docs</code>.</p>
	 *
	 * @param docs a {@link java.util.Collection} object.
	 */
	public void setDocs(Collection<ResponseDoc> docs) { this.docs = docs; }
	
	/**
	 * <p>getSortedDocs.</p>
	 *
	 * @return a {@link java.util.TreeSet} object.
	 */
	@JsonIgnore
	public TreeSet<ResponseDoc> getSortedDocs() {
		final TreeSet<ResponseDoc> set = new TreeSet<ResponseDoc>();
		set.addAll(this.getDocs());
		return set;
	}
}
