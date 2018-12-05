package com.sap.psr.vulas.backend.repo;


import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.GoalExecution;
import com.sap.psr.vulas.backend.util.ResultSetFilter;
import com.sap.psr.vulas.shared.enums.GoalType;

/**
 * See here for JPQL: http://docs.oracle.com/javaee/6/tutorial/doc/bnbtg.html
 *
 */
@Repository
public interface GoalExecutionRepository extends CrudRepository<GoalExecution, Long>, GoalExecutionRepositoryCustom {
	
	public static final ResultSetFilter<GoalExecution> FILTER = new ResultSetFilter<GoalExecution>();
	
	List<GoalExecution> findByGoal(@Param("goal") String goal);

	/**
	 * Returns all {@link GoalExecution}s for the given {@link Application}.
	 * @param app
	 * @return
	 */
	@Query("SELECT goal FROM GoalExecution AS goal WHERE goal.app = :app ORDER BY goal.startedAtClient DESC")
	List<GoalExecution> findByApp(@Param("app") Application app);
	
	/**
	 * Returns the {@link GoalExecution}s for the given executionId.
	 * @param executionId
	 * @return
	 */
	@Query("SELECT goal FROM GoalExecution AS goal WHERE goal.executionId = :execId")
	List<GoalExecution> findByExecutionId(@Param("execId") String execId);
	
	/**
	 * Returns the latest {@link GoalExecution} for the given {@link Application}, or null if no executions exist.
	 * @param app
	 * @return
	 */
	@Query(value="SELECT id FROM app_goal_exe WHERE app = :app ORDER BY started_at_client DESC LIMIT 1", nativeQuery=true)
	Long findLatestForApp(@Param("app") Long app);
	
	/**
	 * Returns the latest {@link GoalExecution} of the given {@link GoalType} for the given {@link Application}, or null if no executions exist.
	 * @param app
	 * @return
	 */
	@Query(value="SELECT id FROM app_goal_exe WHERE app = :app AND goal = :type ORDER BY started_at_client DESC LIMIT 1", nativeQuery=true)
	Long findLatestForApp(@Param("app") Long app, @Param("type") String type);
	
	@Query("SELECT goal FROM GoalExecution AS goal WHERE goal.app = :app AND goal.goal = :goal AND goal.startedAtClient = :startedAtClient ORDER BY goal.startedAtClient DESC")
	List<GoalExecution> findByAppGoalStartedAtClient(@Param("app") Application app, @Param("goal") GoalType goal, @Param("startedAtClient") java.util.Calendar startedAtClient);
	
	/**
	 * Deletes all goal executions for the given {@link Application}.
	 * Called by goal {@link GoalType#CLEAN}.
	 * @param app
	 */
	@Modifying
	@Transactional
	@Query("DELETE FROM GoalExecution AS goal WHERE goal.app = :app")
	void deleteGoalHistory(@Param("app") Application app);
}