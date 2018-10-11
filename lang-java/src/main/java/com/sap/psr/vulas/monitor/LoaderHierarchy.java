package com.sap.psr.vulas.monitor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Offers methods to create and interact with a hierarchy of {@link ClassLoader}s and {@link Loader}s.
 *  
 *
 */
public class LoaderHierarchy {
	
	private static final Log log = LogFactory.getLog(LoaderHierarchy.class);

	private Map<ClassLoader, Loader> loaderMap = new HashMap<ClassLoader, Loader>();
	
	public Loader add(ClassLoader _cl) {
		Loader l = this.loaderMap.get(_cl); 
		if(l==null) {
			l = new Loader(_cl);
			this.loaderMap.put(_cl, l);
			
			final ClassLoader pcl = _cl.getParent();
			if(pcl!=null) {
				Loader pl = this.add(pcl);
				l.setParent(pl);
				l.createClassPool(pl, _cl);
			}
			else {
				l.createClassPool(null, _cl);
			}
		}
		return l;
	}
	
	public Loader getLoader(ClassLoader _cl) {
		return this.loaderMap.get(_cl);
	}
	
	public boolean isLeaf(ClassLoader _cl) {
		final Loader l = this.loaderMap.get(_cl);
		if(l==null)
			throw new IllegalArgumentException();
		else
			return l.isLeaf();
	}
	
	public boolean isRoot(ClassLoader _cl) {
		final Loader l = this.loaderMap.get(_cl);
		if(l==null)
			throw new IllegalArgumentException();
		else
			return l.isRoot();
	}
	
	public Loader getRoot() {
		Loader r = null;
		for(Loader l : this.loaderMap.values()) {
			if(l.isRoot()) {
				r = l;
				break;
			}
		}
		return r;
	}
	
	public void logHierarchy(Loader _l, int _lvl) {
		LoaderHierarchy.log.info(_lvl + "    " + this.getIndent(_lvl) + _l.toString());
		for(Loader c : _l.getChilds()) this.logHierarchy(c, _lvl+1);
	}
	
	private String getIndent(int _i) {
		final StringBuilder b = new StringBuilder();
		for(int i=0; i<_i; i++) b.append("    ");
		return b.toString();
	}
}
