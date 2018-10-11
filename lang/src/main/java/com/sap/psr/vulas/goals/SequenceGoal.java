package com.sap.psr.vulas.goals;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class SequenceGoal extends AbstractAppGoal {

	private static final Log log = LogFactory.getLog(SequenceGoal.class);

	private List<AbstractGoal> sequence = new ArrayList<AbstractGoal>();
	
	private double progress = 0;
	
	public SequenceGoal() { super(GoalType.SEQUENCE); }
	
	public void addGoal(AbstractGoal _goal) {
		this.sequence.add(_goal);
	}
	
	/**
	 * Checks whether one or more {@link AbstractGoal}s have been added.
	 */
	@Override
	protected void prepareExecution() throws GoalConfigurationException {
		super.prepareExecution();
		
		// Add goals from configuration parameter if there are none yet
		if(this.sequence.isEmpty()) {
			
			final String goals[] = this.getConfiguration().getStringArray(CoreConfiguration.SEQ_DEFAULT, null);
			if(goals==null || goals.length==0)
				throw new GoalConfigurationException("No goals have been added to the sequence");
			
			// Add one goal after the other
			for(String g: goals) {
				try {
					final GoalType gt = GoalType.parseGoal(g);
					this.addGoal(GoalFactory.create(gt, this.getGoalClient()));
				}
				// Thrown by parseGoal
				catch (IllegalArgumentException e) {
					throw new GoalConfigurationException("Cannot add goal [" + g + "] to sequence: " + e.getMessage());
				}
				// Thrown by create
				catch (IllegalStateException e) {
					throw new GoalConfigurationException("Cannot add goal [" + g + "] to sequence: " + e.getMessage());
				}				
			}
		}
		
		// Loop over all goals and set configuration
		for(AbstractGoal g: this.sequence) {
			g.getGoalContext().setApplication(this.getApplicationContext());
			((AbstractAppGoal)g).addAppPaths(new HashSet<Path>(this.getAppPaths()));
		}
	}
	
	/**
	 * Calls {@link AbstractGoal#executeSync()} for all goals that have been added to the sequence. 
	 */
	@Override
	protected void executeTasks() throws Exception {
		int i = 0;
		for(AbstractGoal g: this.sequence) {
			g.executeSync();
			this.progress = (double)++i / (double)this.sequence.size();
		}
	}
	
	/**
	 * Returns the progress, i.e., number of completed goals divided by total number of goals.
	 */
	public double getProgress() { return this.progress; }
}