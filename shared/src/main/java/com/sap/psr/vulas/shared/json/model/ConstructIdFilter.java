package com.sap.psr.vulas.shared.json.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.psr.vulas.shared.enums.ConstructType;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConstructIdFilter {

	private Map<ConstructType, SortedSet<ConstructId>> constructIds = new HashMap<ConstructType, SortedSet<ConstructId>>();

	public ConstructIdFilter(Collection<ConstructId> _constructs_ids) {
		SortedSet<ConstructId> cids = null;
		if(_constructs_ids!=null) {
			for(ConstructId cid: _constructs_ids) {
				cids = this.constructIds.get(cid.getType());
				if(cids==null) {
					cids = new TreeSet<ConstructId>();
					this.constructIds.put(cid.getType(), cids);
				}
				cids.add(cid);
			}
		}
	}

	@JsonProperty(value = "PACK")
	public int countPack() { return ( this.constructIds.containsKey(ConstructType.PACK) ? this.constructIds.get(ConstructType.PACK).size()  : 0); }

	@JsonProperty(value = "CLASS")
	public int countClass() { return ( this.constructIds.containsKey(ConstructType.CLAS) ? this.constructIds.get(ConstructType.CLAS).size()  : 0); }

	@JsonProperty(value = "ENUM")
	public int countEnum() { return ( this.constructIds.containsKey(ConstructType.ENUM) ? this.constructIds.get(ConstructType.ENUM).size()  : 0); }

	@JsonProperty(value = "INIT")
	public int countInit() { return ( this.constructIds.containsKey(ConstructType.INIT) ? this.constructIds.get(ConstructType.INIT).size()  : 0); }

	@JsonProperty(value = "CONS")
	public int countCons() { return ( this.constructIds.containsKey(ConstructType.CONS) ? this.constructIds.get(ConstructType.CONS).size()  : 0); }

	@JsonProperty(value = "METH")
	public int countMeth() { return ( this.constructIds.containsKey(ConstructType.METH) ? this.constructIds.get(ConstructType.METH).size()  : 0); }

	@JsonProperty(value = "MODU")
	public int countModule() { return ( this.constructIds.containsKey(ConstructType.MODU) ? this.constructIds.get(ConstructType.MODU).size()  : 0); }

	@JsonProperty(value = "FUNC")
	public int countFunction() { return ( this.constructIds.containsKey(ConstructType.FUNC) ? this.constructIds.get(ConstructType.FUNC).size()  : 0); }

}
