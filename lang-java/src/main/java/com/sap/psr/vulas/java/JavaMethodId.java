package com.sap.psr.vulas.java;

import java.util.List;

import com.sap.psr.vulas.ConstructId;


/**
 * Identifies a method relative to its class (normal or nested).
 */
public class JavaMethodId extends JavaId {

	private JavaId context = null;
	private String methodId = null;
	private List<String> parameterTypes = null;

	/**
	 * <p>Constructor for JavaMethodId.</p>
	 *
	 * @param _c a {@link com.sap.psr.vulas.java.JavaId} object.
	 * @param _simple_name a {@link java.lang.String} object.
	 * @param _parameter_types a {@link java.util.List} object.
	 */
	public JavaMethodId(JavaId _c, String _simple_name, List<String> _parameter_types) {
		super(JavaId.Type.METHOD);
		this.context = _c;
		this.methodId = _simple_name;
		this.parameterTypes = _parameter_types;
	}
	
	/**
	 * Returns the fully qualified name, i.e., with parameter types and the surrounding class.
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getQualifiedName() { return context.getQualifiedName() + "." + this.methodId + JavaId.parameterTypesToString(this.parameterTypes, true); }

	/**
	 * Returns the method name, including parameter types in brackets.
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() { return this.methodId + JavaId.parameterTypesToString(this.parameterTypes, true); }

	/**
	 * Returns the method name, excluding parameter types and brackets.
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSimpleName() { return this.methodId; }
	
	/**
	 * <p>getDefinitionContext.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.ConstructId} object.
	 */
	public ConstructId getDefinitionContext() { return this.context; }
	
	/**
	 * <p>getJavaPackageId.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.java.JavaPackageId} object.
	 */
	public JavaPackageId getJavaPackageId() { return this.context.getJavaPackageId(); }
	
	/**
	 * Returns true if the method has the @Test annotation or both the method name and its class name have the postfix or suffix 'test'.
	 *
	 * @return a boolean.
	 */
	public boolean isTestMethod() {
		boolean is_test = this.hasAnnotation("Test");
		
		// @Test annotation not found, let's check the qualified names of the class context and the method itself
		if(!is_test && this.context.getType().equals(JavaId.Type.CLASS)) {
			is_test = ((JavaClassId)this.context).isTestClass() && (this.methodId.toLowerCase().startsWith("test") || this.methodId.toLowerCase().endsWith("test"));
		}
		
		return is_test;
	}
}
