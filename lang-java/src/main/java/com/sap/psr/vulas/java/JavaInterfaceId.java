package com.sap.psr.vulas.java;

import com.sap.psr.vulas.ConstructId;

/** Identifies a Java interface. */
public class JavaInterfaceId extends JavaId {

  private JavaId declarationContext = null;
  private String interfaceName = null;

  /**
   * Constructor for creating the identifier of an enum.
   *
   * @param _declaration_ctx a {@link com.sap.psr.vulas.java.JavaId} object.
   * @param _simple_name a {@link java.lang.String} object.
   */
  public JavaInterfaceId(JavaId _declaration_ctx, String _simple_name) {
    super(JavaId.Type.INTERFACE);
    this.declarationContext = _declaration_ctx;
    this.interfaceName = _simple_name;
  }

  /**
   * Returns true if the interface is not directly declared within a package, but within another
   * construct like a class or interface.
   *
   * @return a boolean.
   */
  public boolean isNested() {
    return this.declarationContext != null
        && !this.declarationContext.getType().equals(JavaId.Type.PACKAGE);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the fully qualified interface name, i.e., including the name of the package in which
   * it is defined.
   */
  @Override
  public String getQualifiedName() {
    if (this.declarationContext != null) {
      if (!this.isNested())
        return this.declarationContext.getQualifiedName() + "." + this.interfaceName;
      else return this.declarationContext.getQualifiedName() + "$" + this.interfaceName;
    } else {
      return this.interfaceName;
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns a class name that is unique within the package in which the class is defined. In
   * case of nested classes, the names of parent classes will be included (e.g.,
   * OuterClass$InnerClass).
   *
   * @return the class name including the names of parent classes (if any)
   */
  @Override
  public String getName() {
    if (this.declarationContext != null) {
      if (!this.isNested()) return this.interfaceName;
      else return this.declarationContext.getName() + "$" + this.interfaceName;
    } else {
      return this.interfaceName;
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the class name without considering any context.
   *
   * @return the simple class name w/o context information
   */
  @Override
  public String getSimpleName() {
    return this.interfaceName;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the name of the Java package in which the class or nested class is defined. Returns
   * null if a class is defined outside of a package.
   *
   * @return a Java package name
   */
  @Override
  public ConstructId getDefinitionContext() {
    return this.getJavaPackageId();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the Java package in the context of which the construct is defined.
   */
  @Override
  public JavaPackageId getJavaPackageId() {
    if (this.isNested()) return this.declarationContext.getJavaPackageId();
    else return (JavaPackageId) this.declarationContext;
  }
}
