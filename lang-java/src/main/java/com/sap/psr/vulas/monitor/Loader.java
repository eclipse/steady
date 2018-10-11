package com.sap.psr.vulas.monitor;

import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.LoaderClassPath;

/**
 * Wraps a {@link ClassLoader} to facilitate the search of parent and child class loaders.
 * Specifically, {@link ClassLoader} does not allow navigating to the child class loader(s).
 * 
 *
 */
public class Loader {
	private ClassLoader classLoader = null;
	private Loader parent = null;
	private Set<Loader> childs = new HashSet<Loader>();
	private ClassPool classPool = null;
	Loader(ClassLoader _loader) {
		this.classLoader = _loader;
	}
	public void setParent(Loader _l) {
		if(this.parent==null)
			this.parent = _l;
		if(!_l.getChilds().contains(this))
			_l.addChild(this);
	}
	public Loader getParent() { return this.parent; }
	public void addChild(Loader _l) {
		this.childs.add(_l);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((classLoader == null) ? 0 : classLoader.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Loader other = (Loader) obj;
		if (classLoader == null) {
			if (other.classLoader != null)
				return false;
		} else if (!classLoader.equals(other.classLoader))
			return false;
		return true;
	}
	public Set<Loader> getChilds() { return this.childs; }
	public boolean isLeaf() {
		return this.childs.size()==0;
	}
	public boolean isRoot() {
		return this.parent==null;
	}
	public int getDepth() {
		return (this.parent==null ? 0 : parent.getDepth()+1);
	}
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}
	public ClassPool getClassPool() {
		return this.classPool;
	}
	public void createClassPool(Loader _parent, ClassLoader _classloader) {
		if(_parent==null) {
			this.classPool = new ClassPool();
		}
		else {
			this.classPool = new ClassPool(_parent.getClassPool());
		}
		this.classPool.appendClassPath(new LoaderClassPath(_classloader));
	}
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append("Loader [classLoader=").append(this.classLoader.getClass().getName());
		b.append(",hashCode=").append(this.classLoader.hashCode());
		b.append(",#childs=").append(this.childs.size());
		b.append(",depth=").append(this.getDepth()).append("]");
		return b.toString();
	}
	public String toJSON() {
		StringBuilder b = new StringBuilder();
		b.append(" { \"classLoader\" : \"").append(this.classLoader.getClass().getName()).append("\"");
		b.append(" , \"hashCode\" : \"").append(this.classLoader.hashCode()).append("\"");
		b.append(" , \"depth\" : \"").append(this.getDepth()).append("\" }");
		return b.toString();
	}
}