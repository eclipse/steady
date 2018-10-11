package com.sap.psr.vulas.monitor.trace;

import com.sap.psr.vulas.ConstructId;

public class PathNode {

	private ConstructId constructId = null;
	
	private String sha1 = null;
	
	public PathNode(ConstructId _cid) {
		this(_cid, null);
	}
	
	public PathNode(ConstructId _cid, String _sha1) {
		this.constructId = _cid;
		this.sha1 = _sha1;
	}

	public ConstructId getConstructId() {
		return constructId;
	}

	public String getSha1() {
		return sha1;
	}
	
	public void setSha1(String _sha1) {
		this.sha1= _sha1;
	}
	
	public boolean hasSha1() { return this.sha1!=null; }
	
	@Override
	public String toString() {
		final StringBuffer b = new StringBuffer();
		b.append("[qname=");
		b.append(this.constructId.getQualifiedName());
		if(this.sha1!=null) {
			b.append(", libsha1=").append(this.sha1);
		}
		b.append("]");
		return b.toString();
	}
}
