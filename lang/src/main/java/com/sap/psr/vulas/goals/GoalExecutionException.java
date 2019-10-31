package com.sap.psr.vulas.goals;

/**
 * Thrown by {@link AbstractGoal#executeSync()}, indicates that the execution of the respective goal
 * failed.
 */
public class GoalExecutionException extends Exception {

  /**
   * Constructor for GoalExecutionException.
   *
   * @param _cause a {@link java.lang.Throwable} object.
   */
  public GoalExecutionException(Throwable _cause) {
    super(_cause);
  }

  /**
   * Constructor for GoalExecutionException.
   *
   * @param _msg a {@link java.lang.String} object.
   * @param _cause a {@link java.lang.Throwable} object.
   */
  public GoalExecutionException(String _msg, Throwable _cause) {
    super(_msg, _cause);
  }
}
