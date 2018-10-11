package com.sap.psr.vulas.goals;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GoalExecutor {

	private static final Log log = LogFactory.getLog(GoalExecutor.class);
	
	private static GoalExecutor instance = null;
	
	public static synchronized GoalExecutor getInstance() {
		if(instance==null)
			instance = new GoalExecutor();
		return instance;
	}
	
	private ExecutorService pool = null;
	
	private GoalExecutor() {
		this.pool = Executors.newFixedThreadPool(4); //newSingleThreadExecutor();
	}
	
	public void execute(AbstractGoal _goal) {
		this.pool.execute(_goal);
	}
}