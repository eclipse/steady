package com.sap.psr.vulas.java;

import java.util.List;

import com.sap.psr.vulas.ConstructId;


/**
 * Identifies a method relative to its class (normal or nested).
 *
 */
public class JavaMethodId extends JavaId {

	private JavaId context = null;
	private String methodId = null;
	private List<String> parameterTypes = null;

	public JavaMethodId(JavaId _c, String _simple_name, List<String> _parameter_types) {
		super(JavaId.Type.METHOD);
		this.context = _c;
		this.methodId = _simple_name;
		this.parameterTypes = _parameter_types;
	}
	
	/**
	 * Returns the fully qualified name, i.e., with parameter types and the surrounding class.
	 */
	public String getQualifiedName() { return context.getQualifiedName() + "." + this.methodId + JavaId.parameterTypesToString(this.parameterTypes, true); }

	/**
	 * Returns the method name, including parameter types in brackets.
	 */
	public String getName() { return this.methodId + JavaId.parameterTypesToString(this.parameterTypes, true); }

	/**
	 * Returns the method name, excluding parameter types and brackets.
	 */
	public String getSimpleName() { return this.methodId; }
	
	public ConstructId getDefinitionContext() { return this.context; }
	
	public JavaPackageId getJavaPackageId() { return this.context.getJavaPackageId(); }
	
	/**
	 * Returns true if the method has the @Test annotation or both the method name and its class name have the postfix or suffix 'test'.
	 * @return
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
