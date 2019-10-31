package com.sap.psr.vulas;

import com.google.gson.JsonObject;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import java.util.Collection;
import java.util.HashSet;

/**
 * The construct identifier shall allow the unique identification of programming constructs at
 * application compile time and runtime. It depends on the specific programming language used. Java
 * methods, for instance, are uniquely identified by package name, class name, internal class name
 * (if any), method name and method arguments.
 */
public abstract class ConstructId implements Comparable<ConstructId> {

  /** Programming language of the construct. */
  private ProgrammingLanguage lang = null;

  /**
   * Constructor for ConstructId.
   *
   * @param _l a {@link com.sap.psr.vulas.shared.enums.ProgrammingLanguage} object.
   */
  protected ConstructId(ProgrammingLanguage _l) {
    this.lang = _l;
  }

  /**
   * getLanguage.
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.ProgrammingLanguage} object.
   */
  public ProgrammingLanguage getLanguage() {
    return this.lang;
  }

  /**
   * Returns a construct identifier that is unique within the respective runtime environment.
   * Example: In the context of the Java Runtime Environment (more precisely: a given class loader),
   * a Java class is uniquely identified by its name and the package it belongs to.
   *
   * @return a {@link java.lang.String} object.
   */
  public abstract String getQualifiedName();

  /**
   * Returns a construct identifier that is unique within a given context (but not necessarily
   * within the runtime environment). Example: A Java method is uniquely identified in its class
   * context by its name and its parameter types. A Java class (nested or not) is uniquely
   * identified in its package context by its name.
   *
   * @return a {@link java.lang.String} object.
   */
  public abstract String getName();

  /**
   * Returns a simple, not necessarily unique name of the construct. Example: For Java methods, the
   * simple name corresponds to the method name (excluding parameter types).
   *
   * @return a {@link java.lang.String} object.
   */
  public abstract String getSimpleName();

  /**
   * Returns the context in which the the construct is defined. Example: Java classes are defined in
   * a package (the same holds true for nested classes). Java methods and constructors are defined
   * in a class.
   *
   * @see ConstructId#getName()
   * @see ConstructId#getQualifiedName()
   * @return a {@link com.sap.psr.vulas.ConstructId} object.
   */
  public abstract ConstructId getDefinitionContext();

  /**
   * Returns a JSON representation of the construct.
   *
   * @return a {@link java.lang.String} object.
   */
  public abstract String toJSON();

  /**
   * Returns a GSON representation of the construct.
   *
   * @return a {@link com.google.gson.JsonObject} object.
   */
  public abstract JsonObject toGSON();

  /**
   * Compares the construct with the given construct by comparing their qualified name.
   *
   * @param _c a {@link com.sap.psr.vulas.ConstructId} object.
   * @return a int.
   */
  public final int compareTo(ConstructId _c) {
    return this.getQualifiedName().compareTo(_c.getQualifiedName());
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.getQualifiedName().hashCode();
    return result;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns true if the qualified name of the two constructs are equal, false otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ConstructId other = (ConstructId) obj;
    return this.getQualifiedName().equals(other.getQualifiedName());
  }

  /**
   * getSharedType.
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.ConstructType} object.
   */
  public abstract ConstructType getSharedType();

  /**
   * Transforms an object with a given core type (defined in vulas-core) into an object having the
   * corresponding shared type (defined in vulas-share).
   *
   * @param _core_type a {@link com.sap.psr.vulas.ConstructId} object.
   * @return a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   */
  public static com.sap.psr.vulas.shared.json.model.ConstructId toSharedType(
      com.sap.psr.vulas.ConstructId _core_type) {
    return new com.sap.psr.vulas.shared.json.model.ConstructId(
        _core_type.getLanguage(), _core_type.getSharedType(), _core_type.getQualifiedName());
  }

  /**
   * getSharedType.
   *
   * @param _c a {@link java.util.Collection} object.
   * @return a {@link java.util.Collection} object.
   */
  public static Collection<com.sap.psr.vulas.shared.json.model.ConstructId> getSharedType(
      Collection<com.sap.psr.vulas.ConstructId> _c) {
    final Collection<com.sap.psr.vulas.shared.json.model.ConstructId> c =
        new HashSet<com.sap.psr.vulas.shared.json.model.ConstructId>();
    for (ConstructId cid : _c) c.add(ConstructId.toSharedType(cid));
    return c;
  }
}
