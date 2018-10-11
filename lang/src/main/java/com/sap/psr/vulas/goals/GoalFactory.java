package com.sap.psr.vulas.goals;

import javax.validation.constraints.NotNull;

import com.sap.psr.vulas.shared.enums.GoalClient;
import com.sap.psr.vulas.shared.enums.GoalType;

public class GoalFactory {

	/**
	 * Creates a {@link AbstractGoal} for the given {@link GoalType}.
	 * @param _type
	 */
	public static AbstractGoal create(@NotNull GoalType _type) throws IllegalStateException, IllegalArgumentException {
		AbstractGoal goal = null;
		if(_type.equals(GoalType.CLEAN)) {
			goal = new CleanGoal();
		}
		else if(_type.equals(GoalType.APP)) {
			goal = new BomGoal();
		}
		else if(_type.equals(GoalType.A2C)) {
			final String clazzname = "com.sap.psr.vulas.cg.A2CGoal";
			try {
				final Class clazz = Class.forName(clazzname);
				goal = (AbstractGoal)clazz.newInstance();
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
			} catch (InstantiationException e) {
				throw new IllegalStateException("Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
			}
		}
		else if(_type.equals(GoalType.T2C)) {
			final String clazzname = "com.sap.psr.vulas.cg.T2CGoal";
			try {
				final Class clazz = Class.forName(clazzname);
				goal = (AbstractGoal)clazz.newInstance();
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
			} catch (InstantiationException e) {
				throw new IllegalStateException("Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
			}
		}
		else if(_type.equals(GoalType.INSTR)) {
			goal = new InstrGoal();
		}
		else if(_type.equals(GoalType.REPORT)) {
			goal = new ReportGoal();
		}
		else if(_type.equals(GoalType.UPLOAD)) {
			goal = new UploadGoal();
		}
		else if(_type.equals(GoalType.SEQUENCE)) {
			goal = new SequenceGoal();
		}
		else if(_type.equals(GoalType.SPACENEW)) {
			goal = new SpaceNewGoal();
		}
		else if(_type.equals(GoalType.SPACEMOD)) {
			goal = new SpaceModGoal();
		}
		else if(_type.equals(GoalType.SPACECLEAN)) {
			goal = new SpaceCleanGoal();
		}
		else if(_type.equals(GoalType.SPACEDEL)) {
			goal = new SpaceDelGoal();
		}else {
			throw new IllegalArgumentException ("Goal [" + _type + "] is not supported");
		}
		return goal;
	}
	
	/**
	 * Creates a {@link AbstractGoal} for the given {@link GoalType} and (@link GoalClient}.
	 * @param _type
	 */
	public static AbstractGoal create(@NotNull GoalType _type, @NotNull GoalClient _client) throws IllegalStateException, IllegalArgumentException {
		final AbstractGoal goal = GoalFactory.create(_type);
		goal.setGoalClient(_client);
		return goal;
	}	
}
