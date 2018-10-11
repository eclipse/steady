package com.sap.psr.vulas.java;

import com.sap.psr.vulas.ConstructId;

/**
 * Identifies a Java package by its fully qualified name.
 *
 */
public class JavaPackageId extends JavaId {
	
	/**
	 * The default package has an empty string as qualified name. It is used
	 * when working with classes, interfaces or enums that do not belong to
	 * a package.
	 */
	public final static JavaPackageId DEFAULT_PACKAGE = new JavaPackageId(null);

	/** Fully qualified package identifier. */
	protected String packageId = null;
	public JavaPackageId(String _qualified_name) {
		super(JavaId.Type.PACKAGE);
		this.packageId = (_qualified_name==null ? "" : _qualified_name);
	}
	
	/**
	 * Returns the complete package name, including parent packages.
	 */
	public String getQualifiedName() { return this.packageId; }
	
	/**
	 * For packages, the method returns the qualified name.
	 */
	public String getName() { return this.getQualifiedName(); }
	
	/**
	 * For packages, the method returns the qualified name.
	 */
	public String getSimpleName() { return this.getQualifiedName(); }
	
	/**
	 * For packages, this method always returns null.
	 */
	public ConstructId getDefinitionContext() { return null; }
	
	/**
	 * For packages, this method always returns null.
	 */
	public JavaPackageId getJavaPackageId() { return null; }
	
	/**
	 * Returns true if the given {@link JavaPackageId} corresponds to the {@link JavaPackageId#DEFAULT_PACKAGE}, false otherwise.
	 * @param _pid
	 * @return
	 */
	public static final boolean isDefaultPackage(JavaPackageId _pid) { return DEFAULT_PACKAGE.equals(_pid); }
}
