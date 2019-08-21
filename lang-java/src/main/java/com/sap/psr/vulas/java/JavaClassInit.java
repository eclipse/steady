package com.sap.psr.vulas.java;

import com.sap.psr.vulas.ConstructId;

/**
 * <p>JavaClassInit class.</p>
 *
 */
public class JavaClassInit extends JavaId {

	/** Constant <code>NAME="&lt;clinit&gt;"</code> */
	public static final String NAME = "<clinit>";
	private JavaClassId classContext = null;
	
	/**
	 * <p>Constructor for JavaClassInit.</p>
	 *
	 * @param _c a {@link com.sap.psr.vulas.java.JavaClassId} object.
	 */
	protected JavaClassInit(JavaClassId _c) {
		super(JavaId.Type.CLASSINIT);
		this.classContext = _c;
	}

	/**
	 * <p>getJavaClassContext.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.java.JavaClassId} object.
	 */
	public JavaClassId getJavaClassContext() { return this.classContext; }
	
	/**
	 * <p>getQualifiedName.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getQualifiedName() { return classContext.getQualifiedName() + "." + JavaClassInit.NAME; }

	/**
	 * Returns &lt;clinit&gt;.
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() { return JavaClassInit.NAME; }
	
	/**
	 * Returns &lt;clinit&gt;.
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSimpleName() { return JavaClassInit.NAME; }
	
	/**
	 * <p>getDefinitionContext.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.ConstructId} object.
	 */
	public ConstructId getDefinitionContext() { return this.classContext; }
	
	/**
	 * <p>getJavaPackageId.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.java.JavaPackageId} object.
	 */
	public JavaPackageId getJavaPackageId() { return this.classContext.getJavaPackageId(); }
}
