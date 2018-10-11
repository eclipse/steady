package com.sap.psr.vulas.java;

import com.sap.psr.vulas.ConstructId;

public class JavaClassInit extends JavaId {

	public static final String NAME = "<clinit>";
	private JavaClassId classContext = null;
	
	protected JavaClassInit(JavaClassId _c) {
		super(JavaId.Type.CLASSINIT);
		this.classContext = _c;
	}

	public JavaClassId getJavaClassContext() { return this.classContext; }
	
	public String getQualifiedName() { return classContext.getQualifiedName() + "." + JavaClassInit.NAME; }

	/**
	 * Returns <clinit>.
	 */
	public String getName() { return JavaClassInit.NAME; }
	
	/**
	 * Returns <clinit>.
	 */
	public String getSimpleName() { return JavaClassInit.NAME; }
	
	public ConstructId getDefinitionContext() { return this.classContext; }
	
	public JavaPackageId getJavaPackageId() { return this.classContext.getJavaPackageId(); }
}
