package com.sap.psr.vulas.goals;

/** GoalConfigurationException class. */
public class GoalConfigurationException extends Exception {

  /**
   * Constructor for GoalConfigurationException.
   *
   * @param _msg a {@link java.lang.String} object.
   */
  public GoalConfigurationException(String _msg) {
    super(_msg);
  }

  /**
   * Constructor for GoalConfigurationException.
   *
   * @param _t a {@link java.lang.Throwable} object.
   */
  public GoalConfigurationException(Throwable _t) {
    super(_t);
  }
}
