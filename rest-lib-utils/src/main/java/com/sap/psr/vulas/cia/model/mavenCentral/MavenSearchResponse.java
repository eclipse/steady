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
	
	public MavenSearchResponse() {}

	public long getNumFound() { return numFound; }
	public void setNumFound(long numFound) { this.numFound = numFound; }

	public Collection<ResponseDoc> getDocs() { return docs; }
	public void setDocs(Collection<ResponseDoc> docs) { this.docs = docs; }
	
	@JsonIgnore
	public TreeSet<ResponseDoc> getSortedDocs() {
		final TreeSet<ResponseDoc> set = new TreeSet<ResponseDoc>();
		set.addAll(this.getDocs());
		return set;
	}
}
