package com.sap.psr.vulas.java;

import com.sap.psr.vulas.ConstructId;

/** JavaClassInit class. */
public class JavaClassInit extends JavaId {

  /** Constant <code>NAME="&lt;clinit&gt;"</code> */
  public static final String NAME = "<clinit>";

  private JavaClassId classContext = null;

  /**
   * Constructor for JavaClassInit.
   *
   * @param _c a {@link com.sap.psr.vulas.java.JavaClassId} object.
   */
  protected JavaClassInit(JavaClassId _c) {
    super(JavaId.Type.CLASSINIT);
    this.classContext = _c;
  }

  /**
   * getJavaClassContext.
   *
   * @return a {@link com.sap.psr.vulas.java.JavaClassId} object.
   */
  public JavaClassId getJavaClassContext() {
    return this.classContext;
  }

  /**
   * getQualifiedName.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getQualifiedName() {
    return classContext.getQualifiedName() + "." + JavaClassInit.NAME;
  }

  /**
   * Returns &lt;clinit&gt;.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getName() {
    return JavaClassInit.NAME;
  }

  /**
   * Returns &lt;clinit&gt;.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSimpleName() {
    return JavaClassInit.NAME;
  }

  /**
   * getDefinitionContext.
   *
   * @return a {@link com.sap.psr.vulas.ConstructId} object.
   */
  public ConstructId getDefinitionContext() {
    return this.classContext;
  }

  /**
   * getJavaPackageId.
   *
   * @return a {@link com.sap.psr.vulas.java.JavaPackageId} object.
   */
  public JavaPackageId getJavaPackageId() {
    return this.classContext.getJavaPackageId();
  }
}
