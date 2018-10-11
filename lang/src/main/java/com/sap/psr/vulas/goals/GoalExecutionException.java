package com.sap.psr.vulas.goals;

/**
 * Thrown by {@link AbstractGoal#executeSync()}, indicates that the execution of the respective goal failed.
 */
public class GoalExecutionException extends Exception {
	
	public GoalExecutionException(Throwable _cause) {
		super(_cause);
	}
	
	public GoalExecutionException(String _msg, Throwable _cause) {
		super(_msg, _cause);
	}
}
