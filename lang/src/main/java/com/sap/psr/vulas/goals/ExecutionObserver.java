package com.sap.psr.vulas.goals;

/** ExecutionObserver interface. */
public interface ExecutionObserver {

  /**
   * callback.
   *
   * @param _g a {@link com.sap.psr.vulas.goals.AbstractGoal} object.
   */
  public void callback(AbstractGoal _g);
}
