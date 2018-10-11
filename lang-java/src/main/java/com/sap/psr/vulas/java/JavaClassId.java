package com.sap.psr.vulas.java;

import com.sap.psr.vulas.ConstructId;


/**
 * Identifies a Java class (normal or nested).
 *
 */
public class JavaClassId extends JavaId {
	
	private JavaId declarationContext = null;
	private String className = null;
	
	/**
	 * Constructor for creating the identifier of a nested class.
	 * @param _p
	 * @param _declaration_ctx
	 */
	public JavaClassId(JavaId _declaration_ctx, String _simple_name) {
		//super( (_declaration_ctx.getType().equals(JavaId.Type.PACKAGE) ? JavaId.Type.CLASS : JavaId.Type.NESTED_CLASS) );
		super( JavaId.Type.CLASS );
		this.declarationContext = _declaration_ctx;
		this.className = _simple_name;
	}
	
	public boolean isNestedClass() {
		return this.declarationContext!=null && !this.declarationContext.getType().equals(JavaId.Type.PACKAGE);
	}
	
	/**
	 * Returns the fully qualified class name, i.e., including the name of the package in which the class is defined.
	 */
	@Override
	public String getQualifiedName() {
		final StringBuilder builder = new StringBuilder();
		if(this.declarationContext!=null) {
			final String prefix = this.declarationContext.getQualifiedName();
			builder.append(prefix); // Empty string in case of default package			
			// Outer class
			if(!this.isNestedClass()) {
				if(!prefix.equals("")) // Could also use JavaPackageId.isDefaultPackage
					builder.append(".");
			}
			// Inner class
			else {
				if(!prefix.equals("")) // Should probably never happen
					builder.append("$");
			}
		}
		builder.append(this.className);
		return builder.toString();
	}
	
	/**
	 * Returns a class name that is unique within the package in which the class is defined.
	 * In case of nested classes, the names of parent classes will be included (e.g., OuterClass$InnerClass).
	 * @returns the class name including the names of parent classes (if any)
	 */
	@Override
	public String getName() {
		if(this.declarationContext!=null) {
			if(!this.isNestedClass())
				return this.className;
			else
				return this.declarationContext.getName() + "$" + this.className;
		} else {
			return this.className;
		}
	}
	
	/**
	 * Returns the class name without considering any context.
	 * @returns the simple class name w/o context information
	 */
	@Override
	public String getSimpleName() { return this.className; }
	
	/**
	 * Returns the name of the Java package in which the class or nested class is defined. Returns null if a class is defined outside of a package.
	 * @returns a Java package name
	 */
	@Override
	public ConstructId getDefinitionContext() {
		return this.declarationContext;
		//return this.getJavaPackageId();
	}
	
	/**
	 * Returns the Java package in the context of which the construct is defined.
	 * @return
	 */
	@Override
	public JavaPackageId getJavaPackageId() {
		if(this.isNestedClass())
			return this.declarationContext.getJavaPackageId();
		else
			return (JavaPackageId)this.declarationContext;
	}
	
	public JavaClassInit getClassInit() {
		return new JavaClassInit(this);
	}
	
	public boolean isTestClass() {
		return this.className.toLowerCase().startsWith("test") || this.className.toLowerCase().endsWith("test");
	}
        
}
