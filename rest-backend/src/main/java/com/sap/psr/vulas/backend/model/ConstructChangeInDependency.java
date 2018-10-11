package com.sap.psr.vulas.backend.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConstructChangeInDependency implements Serializable {

	private ConstructChange cc;
	
	//"inarchive": false,
	private Trace trace;
	private Boolean traced;
	/*"reachabilityGraph": null or  {
"sourceDescription": "APP",
"shortestpathlength": "5",
"shortestpathEPcid": "1B06F030C13DA3348473B006824D973F336BB7152BC783C564E415FEE61C8459",
"id": "16832",
"shortestpathEP": {
"lang": "JAVA",
"type": "METH",
"qname": "com.acme.ArchivePrinter.compressArchive()"
}
}*/
	private Boolean reachable;
	/*"versionCheck": {
	"fixed_version": false,
	
	"class_in_archive": false,
	"overall_change_type": "ADD"
	}*/
	
	//TODO
	private Boolean inArchive;
	//fake
	private String reachabilityGraph;
	
	
	private Boolean affected;
	
	private Boolean classInArchive;
	
	private Boolean equalChangeType;
	

	private com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType  overall_change;
	
	public ConstructChangeInDependency(){super();}
	
	public ConstructChangeInDependency(ConstructChange cc){
		super();
		this.cc = cc;
		this.reachabilityGraph=null;
	}
	
	public ConstructChange getConstructChange() { return cc; }
	public void setConstructChange(ConstructChange _cc) { this.cc = _cc; }
	
	public Trace getTrace() { return trace; }
	public void setTrace(Trace trace) { this.trace = trace; }
	
	public Boolean getTraced() { return traced; }
	public void setTraced(Boolean traced) { this.traced = traced; }
	
	public Boolean getAffected() { return affected; }
	public void setAffected(Boolean a) { this.affected = a; }
	
	public Boolean getInArchive() { return inArchive; }
	public void setInArchive(Boolean inArchive) { this.inArchive = inArchive; }
	
	public Boolean getClassInArchive() { return classInArchive; }
	public void setClassInArchive(Boolean classinArchive) { this.classInArchive = classinArchive; }
	
	public Boolean getEqualChangeType() { return equalChangeType; }
	public void setEqualChangeType(Boolean e) { this.equalChangeType = e; }
	
	public com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType  getOverall_change() { return overall_change; }
	public void setOverall_change(com.sap.psr.vulas.backend.model.AffectedConstructChange.ChangeType changeType) { this.overall_change = changeType; }
	
	public Boolean getReachable() { return reachable; }
	public void setReachable(Boolean reachable) { this.reachable = reachable; }

}
