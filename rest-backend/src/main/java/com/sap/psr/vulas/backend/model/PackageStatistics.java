package com.sap.psr.vulas.backend.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>PackageStatistics class.</p>
 *
 */
public class PackageStatistics implements Serializable {

	/**
	 * Package to statistics
	 */
	private Map<ConstructId, ConstructIdFilter> constructIds = new HashMap<ConstructId, ConstructIdFilter>();

	/**
	 * <p>Constructor for PackageStatistics.</p>
	 *
	 * @param _constructs_ids a {@link java.util.Collection} object.
	 */
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

	/**
	 * <p>countConstructTypesPerPackage.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	@JsonProperty(value = "packageCounters")
	public Map<ConstructId, ConstructIdFilter> countConstructTypesPerPackage() {
		return this.constructIds;
	}
}
