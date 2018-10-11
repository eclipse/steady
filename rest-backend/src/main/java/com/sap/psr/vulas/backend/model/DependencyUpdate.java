package com.sap.psr.vulas.backend.model;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.shared.json.model.metrics.Metrics;

/**
 * Describes the update of a {@link Dependency} of an {@link Application} from one version of
 * a {@link Library} to another one. To that end, it contains a list of calls that require to be
 * modified because certain constructs are not available in the target {@link Library}.
 * Moreover, diverse {@link Metrics} quantify the update effort.
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DependencyUpdate {
	
	LibraryId fromLibraryId;
	
	LibraryId toLibraryId;
	
	Metrics metrics;
	
	Set<TouchPoint> callsToModify ;

	public DependencyUpdate(LibraryId f, LibraryId t){
		this.fromLibraryId=f;
		this.toLibraryId=t;
	}
	
	public Set<TouchPoint> getCallsToModify(){ return callsToModify;}
	public void setCallsToModify(Set<TouchPoint> c){this.callsToModify=c;}
	
	public LibraryId getFromLibraryId() { return fromLibraryId; }
	public void setFromLibraryId(LibraryId f) { this.fromLibraryId = f; }

	public LibraryId getToLibraryId() { return toLibraryId; }
	public void setToLibraryId(LibraryId t) { this.toLibraryId = t; }

	public Metrics getMetrics() { return metrics; }
	public void setMetrics(Metrics m) { this.metrics = m; }
}
