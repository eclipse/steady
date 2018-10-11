package com.sap.psr.vulas.backend.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PackageStatistics {

	/**
	 * Package to statistics
	 */
	private Map<ConstructId, ConstructIdFilter> constructIds = new HashMap<ConstructId, ConstructIdFilter>();

	public PackageStatistics(Collection<ConstructId> _constructs_ids) {
		ConstructIdFilter stats = null;
		ConstructId pid = null;
		if(_constructs_ids!=null) {
			for(ConstructId cid: _constructs_ids) {
				pid = ConstructId.getPackageOf(cid);
				stats = this.constructIds.get(pid);
				if(stats==null) {
					stats = new ConstructIdFilter(null);
					this.constructIds.put(pid, stats);
				}
				stats.addConstructId(cid);
			}
		}
	}
	
	@JsonProperty(value = "packageCounters")
	public Map<ConstructId, ConstructIdFilter> countConstructTypesPerPackage() {
		return this.constructIds;
	}
}
