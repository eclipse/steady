package com.sap.psr.vulas.cg.soot;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.*;
import soot.jimple.infoflow.data.SootMethodAndClass;
import soot.jimple.infoflow.entryPointCreators.DefaultEntryPointCreator;
import soot.jimple.infoflow.util.SootMethodRepresentationParser;
import soot.jimple.internal.JNopStmt;

/** CustomEntryPointCreator class. */
public class CustomEntryPointCreator extends DefaultEntryPointCreator {

  private static final Logger logger = LoggerFactory.getLogger(CustomEntryPointCreator.class);

  private final Collection<SootClass> dummyClasses = new HashSet<>();

  /**
   * Constructor for CustomEntryPointCreator.
   *
   * @param methodsToCall a {@link java.util.Collection} object.
   */
  public CustomEntryPointCreator(Collection<String> methodsToCall) {

    super(methodsToCall);
    generateAppropriateDummyClasses(methodsToCall);
  }

  /**
   * generateAppropriateDummyClasses.
   *
   * @param methodsToCall a {@link java.util.Collection} object.
   */
  public void generateAppropriateDummyClasses(Collection<String> methodsToCall) {

    Map<String, Set<String>> classMap =
        SootMethodRepresentationParser.v().parseClassNames(methodsToCall, false);
    for (String className : classMap.keySet()) {
      SootClass createdClass = Scene.v().getSootClass(className);
      if (createdClass.isConcrete()
          && !createdClass.isPhantom()
          && !createdClass.isPhantomClass()) {
        for (String method : classMap.get(className)) {
          SootMethodAndClass methodAndClass =
              SootMethodRepresentationParser.v().parseSootMethodString(method);
          SootMethod methodToInvoke =
              findMethod(
                  Scene.v().getSootClass(methodAndClass.getClassName()),
                  methodAndClass.getSubSignature());

          List<Type> parameterTypes = methodToInvoke.getParameterTypes();
          // check if we actually have concrete parameters for these classes, otherwise generate
          // dummyclasses
          for (Type parameterType : parameterTypes) {
            if (super.isSimpleType(parameterType.getEscapedName())) {
              continue;
            }
            if (!(parameterType instanceof RefType)) {
              continue;
            }
            SootClass class2Search = ((RefType) parameterType).getSootClass();
            // check if a concrete subclass exists
            boolean compatibleTypeExists = concreteSubClassExists(class2Search);

            if (!compatibleTypeExists) {

              if (Scene.v().isExcluded(class2Search)) {
                SootClass dummyClass = getDummyClass(class2Search);
                this.dummyClasses.add(dummyClass);
              }
            }
          }
        }
      }
    }
  }

  /**
   * concreteSubClassExists.
   *
   * @param classToType a {@link soot.SootClass} object.
   * @return a boolean.
   */
  public boolean concreteSubClassExists(SootClass classToType) {
    if (classToType.isAbstract() || classToType.isInterface()) {
      // check if a public exported class exists implementing this interface or extending the
      // abstract class
      if (classToType.isInterface()) {

        for (SootClass implementorOfSootClass :
            Scene.v().getActiveHierarchy().getImplementersOf(classToType)) {
          if (!implementorOfSootClass.isAbstract()) return true;
        }

      } else {
        for (SootClass sootSubClass : Scene.v().getActiveHierarchy().getSubclassesOf(classToType)) {
          if (isCompatible(sootSubClass, classToType) && !sootSubClass.isAbstract()) return true;
        }
      }

      return false;
    }
    return classToType.isConcrete();
  }

  /**
   * Getter for the field <code>dummyClasses</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection getDummyClasses() {
    return this.dummyClasses;
  }

  /**
   * getDummyClass.
   *
   * @param toImplement a {@link soot.SootClass} object.
   * @return a {@link soot.SootClass} object.
   */
  public SootClass getDummyClass(SootClass toImplement) {
    String packageName = toImplement.getJavaPackageName();

    String clzName = toImplement.getJavaStyleName();

    String dummyClassName = packageName + ".Dummy" + clzName;
    if (Scene.v().containsClass(dummyClassName)) return Scene.v().getSootClass(dummyClassName);

    SootClass dummyClass = new SootClass(dummyClassName);
    // dummyClass.setModifiers(toImplement.getModifiers() ^ Modifier.ABSTRACT);
    dummyClass.setModifiers(Modifier.PUBLIC);

    // create the constructor
    SootMethod constructor = new SootMethod("<init>", Collections.<Type>emptyList(), VoidType.v());
    dummyClass.addMethod(constructor);
    JimpleBody body = Jimple.v().newBody(constructor);

    // Add this reference
    body.insertIdentityStmts();
    // special invoke Object Init
    //
    SootClass objectClazz = Scene.v().getSootClass("java.lang.Object");
    SootMethodRef methodRef;
    if (objectClazz.declaresMethod("void <init>()")) {
      SootMethod method = objectClazz.getMethod("void <init>()");
      methodRef = method.makeRef();
    } else {
      methodRef = Scene.v().makeConstructorRef(objectClazz, Collections.<Type>emptyList());
    }
    SpecialInvokeExpr expr = Jimple.v().newSpecialInvokeExpr(body.getThisLocal(), methodRef);
    Stmt invokeStmt = Jimple.v().newInvokeStmt(expr);
    body.getUnits().add(invokeStmt);
    Stmt ret = Jimple.v().newReturnStmt(body.getThisLocal());
    body.getUnits().add(ret);

    constructor.setActiveBody(body);

    /* handle multiple interfaces and cases in which  an interface extends another interface         */
    if (toImplement.isAbstract()) {

      // if class is an interface it might extend another interface, then we have to implement
      // the superclass methods as well

      // if class is abstract
      // a) it might implements several interfaces, that needs to be implemented

      // b) extends a superclass which is also abstract
      // b.2) extends a superclass whose superclass is also abstract (so on...)
      // c) these superclasses might implements several interfaces

      HashSet<SootMethod> methodsToImplement = new HashSet<>();

      HashSet<SootClass> classesWhoseMethodsMustBeImplemented = new HashSet<>();
      SootClass classToVisit = toImplement;

      while (classToVisit.isAbstract()) {
        classesWhoseMethodsMustBeImplemented.add(classToVisit);
        classToVisit = classToVisit.getSuperclass();
      }

      for (SootClass classWhichMethodsMustBeImplemented : classesWhoseMethodsMustBeImplemented) {
        methodsToImplement.addAll(classWhichMethodsMustBeImplemented.getMethods());
        for (SootClass interfaceToImplement : classWhichMethodsMustBeImplemented.getInterfaces()) {
          methodsToImplement.addAll(interfaceToImplement.getMethods());
        }
      }
      // above we might added methods which are already implemented but we catch this latter in the
      // for loop for actually generating the methods

      if (toImplement.isInterface()) {
        dummyClass.addInterface(toImplement);
        dummyClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));

      } else {
        // we have an abstract class
        dummyClass.setSuperclass(toImplement);
      }
      for (SootMethod parentMethod : methodsToImplement) {

        if (parentMethod
            .isAbstract()) { // if we have added to much methods above, we only generate methods for
          // the abstract ones here
          // the next if statement deals with name clashes of methods of several interfaces
          if (dummyClass.declaresMethod(
              parentMethod.getName(),
              parentMethod.getParameterTypes(),
              parentMethod.getReturnType())) {
            // a corresponding method  is already contained in the dummyClass; thus we don't need to
            // generate another one
            continue;
          }
          SootMethod generatedMethod = generateMethodImplementation(parentMethod, dummyClass);
          dummyClass.addMethod(generatedMethod);
        }
      }
    }

    // First add class to scene, then make it an application class
    // as addClass contains a call to "setLibraryClass"
    Scene.v().addClass(dummyClass);
    dummyClass.setApplicationClass();
    // add these classes to the dummyClass set to get the Parameter passed in at callbacks
    this.dummyClasses.add(dummyClass);
    return dummyClass;
  }

  private SootMethod generateMethodImplementation(
      SootMethod methodToImplement, final SootClass generatedDummyClass) {
    SootMethod generatedMethod =
        new SootMethod(
            methodToImplement.getName(),
            methodToImplement.getParameterTypes(),
            methodToImplement.getReturnType());
    Body body = Jimple.v().newBody();
    body.setMethod(generatedMethod);
    generatedMethod.setActiveBody(body);

    // add locals for Parameter
    // Add a parameter reference to the body
    LocalGenerator lg = new LocalGenerator(body);

    // create a local for the this reference
    if (!methodToImplement.isStatic()) {
      Local thisLocal = lg.generateLocal(generatedDummyClass.getType());
      body.getUnits()
          .addFirst(
              Jimple.v()
                  .newIdentityStmt(
                      thisLocal, Jimple.v().newThisRef(generatedDummyClass.getType())));
    }

    int i = 0;
    for (Type type : generatedMethod.getParameterTypes()) {
      Local paramLocal = lg.generateLocal(type);
      body.getUnits()
          .add(Jimple.v().newIdentityStmt(paramLocal, Jimple.v().newParameterRef(type, i)));
      i++;
    }

    JNopStmt startStmt = new JNopStmt();
    JNopStmt endStmt = new JNopStmt();

    body.getUnits().add(startStmt);

    // check if return type is void (check first, since next call includes void)
    if (methodToImplement.getReturnType() instanceof VoidType) {
      body.getUnits().add(Jimple.v().newReturnVoidStmt());
    }
    // if sootClass is simpleClass
    else if (isSimpleType(methodToImplement.getReturnType().toString())) {
      Local varLocal = lg.generateLocal(getSimpleTypeFromType(methodToImplement.getReturnType()));

      AssignStmt aStmt =
          Jimple.v()
              .newAssignStmt(varLocal, getSimpleDefaultValue(methodToImplement.getReturnType()));
      body.getUnits().add(aStmt);
      body.getUnits().add(Jimple.v().newReturnStmt(varLocal));
    } else {
      body.getUnits().add(Jimple.v().newReturnStmt(NullConstant.v()));
    }

    // remove the abstract Modifier from the new implemented method
    generatedMethod.setModifiers(methodToImplement.getModifiers() ^ Modifier.ABSTRACT);

    return generatedMethod;
  }

  private Type getSimpleTypeFromType(Type type) {
    if (type.toString().equals("java.lang.String")) {
      assert type instanceof RefType;

      return RefType.v(((RefType) type).getSootClass());
    } else if (type.toString().equals("void")) {
      return VoidType.v();
    } else if (type.toString().equals("char")) {
      return CharType.v();
    } else if (type.toString().equals("byte")) {
      return ByteType.v();
    } else if (type.toString().equals("short")) {
      return ShortType.v();
    } else if (type.toString().equals("int")) {
      return IntType.v();
    } else if (type.toString().equals("float")) {
      return FloatType.v();
    } else if (type.toString().equals("long")) {
      return LongType.v();
    } else if (type.toString().equals("double")) {
      return DoubleType.v();
    } else if (type.toString().equals("boolean")) {
      return BooleanType.v();
    } else {
      throw new RuntimeException("Unknown simple type: " + type);
    }
  }
}
