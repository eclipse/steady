package com.sap.psr.vulas.goals;

/**
 * <p>ExecutionObserver interface.</p>
 *
 */
public interface ExecutionObserver {

	/**
	 * <p>callback.</p>
	 *
	 * @param _g a {@link com.sap.psr.vulas.goals.AbstractGoal} object.
	 */
	public void callback(AbstractGoal _g);
}
