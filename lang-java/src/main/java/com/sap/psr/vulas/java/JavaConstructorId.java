package com.sap.psr.vulas.java;

import java.util.List;

import com.sap.psr.vulas.ConstructId;

/**
 * Identifies a class constructor relative to its class (normal or nested).
 *
 */
public class JavaConstructorId extends JavaId {

	private JavaId context = null;
	private List<String> parameterTypes = null;
	
	public JavaConstructorId(JavaId _simple_name, List<String> _parameter_types) {
		super(JavaId.Type.CONSTRUCTOR);
		this.context = _simple_name;
		this.parameterTypes = _parameter_types;
	}
	
	/**
	 * Returns the definition context, which can be a class or enum.
	 * Due to the bad naming, rather use {@link JavaMethodId#getDefinitionContext()}.
	 * @return
	 */
	@Deprecated
	public JavaId getJavaClassContext() { return this.context; }
	
	/**
	 * Returns the fully qualified constructor name, including package and (unqualified) parameter types.
	 * Example: test.package.TestClass(int)
	 */
	public String getQualifiedName() { return context.getQualifiedName() + JavaId.parameterTypesToString(this.parameterTypes, true); }
	
	/**
	 * Returns the constructor name, which is equal to the class name, plus the (unqualified) parameter types in brackets.
	 * Example: TestClass(int)
	 */
	public String getName() { return this.context.getSimpleName() + JavaId.parameterTypesToString(this.parameterTypes, true); }
	
	/**
	 * Returns the simple constructor name, which is equal to the class name.
	 * Example: Test
	 */
	public String getSimpleName() { return this.context.getSimpleName(); }
	
	public ConstructId getDefinitionContext() { return this.context; }
	
	public JavaPackageId getJavaPackageId() { return this.context.getJavaPackageId(); }
	
	public String getConstructorIdParams() { return JavaId.parameterTypesToString(this.parameterTypes); }
	
	/**
	 * Returns true if the constructor has parameters, false otherwise.
	 */
	public boolean hasParams() { return this.parameterTypes!=null && this.parameterTypes.size()>0; }
}
