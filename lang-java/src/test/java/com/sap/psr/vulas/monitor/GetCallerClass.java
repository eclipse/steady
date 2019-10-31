package com.sap.psr.vulas.monitor;

/**
 * Test the speed of various methods for getting the caller class name Copy&paste from
 * http://stackoverflow.com/questions/421280/how-do-i-find-the-caller-of-a-method-using-stacktrace-or-reflection
 * Also related: https://github.com/nallar/WhoCalled
 */
public class GetCallerClass {

  /** Abstract class for testing different methods of getting the caller class name */
  private abstract static class GetCallerClassNameMethod {
    public abstract String getCallerClassName(int callStackDepth);

    public abstract String getMethodName();
  }

  /** Uses the internal Reflection class */
  private static class ReflectionMethod extends GetCallerClassNameMethod {
    @SuppressWarnings({"restriction", "deprecation"})
    public String getCallerClassName(int callStackDepth) {
      return sun.reflect.Reflection.getCallerClass(callStackDepth).getName();
    }

    public String getMethodName() {
      return "Reflection";
    }
  }

  /** Get a stack trace from the current thread */
  private static class ThreadStackTraceMethod extends GetCallerClassNameMethod {
    public String getCallerClassName(int callStackDepth) {
      return Thread.currentThread().getStackTrace()[callStackDepth].getClassName();
    }

    public String getMethodName() {
      return "Current Thread StackTrace";
    }
  }

  /** Get a stack trace from a new Throwable */
  private static class ThrowableStackTraceMethod extends GetCallerClassNameMethod {

    public String getCallerClassName(int callStackDepth) {
      return new Throwable().getStackTrace()[callStackDepth].getClassName();
    }

    public String getMethodName() {
      return "Throwable StackTrace";
    }
  }

  /** Use the SecurityManager.getClassContext() */
  private static class SecurityManagerMethod extends GetCallerClassNameMethod {
    public String getCallerClassName(int callStackDepth) {
      return mySecurityManager.getCallerClassName(callStackDepth);
    }

    public String getMethodName() {
      return "SecurityManager";
    }

    /** A custom security manager that exposes the getClassContext() information */
    static class MySecurityManager extends SecurityManager {
      public String getCallerClassName(int callStackDepth) {
        return getClassContext()[callStackDepth].getName();
      }
    }

    private static final MySecurityManager mySecurityManager = new MySecurityManager();
  }

  /** Test all four methods */
  public static void main(String[] args) {
    testMethod(new ReflectionMethod());
    testMethod(new ThreadStackTraceMethod());
    testMethod(new ThrowableStackTraceMethod());
    testMethod(new SecurityManagerMethod());
  }

  private static void testMethod(GetCallerClassNameMethod method) {
    long startTime = System.nanoTime();
    String className = null;
    for (int i = 0; i < 1000000; i++) {
      className = method.getCallerClassName(2);
    }
    printElapsedTime(method.getMethodName(), startTime);
  }

  private static void printElapsedTime(String title, long startTime) {
    System.out.println(
        title + ": " + ((double) (System.nanoTime() - startTime)) / 1000000 + " ms.");
  }
}
