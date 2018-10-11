package com.sap.psr.vulas.monitor;

import java.util.Map;

import com.sap.psr.vulas.goals.AbstractGoal;
import com.sap.psr.vulas.java.JavaId;

import javassist.CannotCompileException;
import javassist.CtBehavior;

/**
 * To be implemented by every instrumentor.
 *
 */
public interface IInstrumentor {
	
	/**
	 * Returns true of a given instrumentor accepts to instrument the given Java class, false otherwise.
	 * @param _jid
	 * @param _behavior
	 * @param _cv
	 * @return
	 */
	public boolean acceptToInstrument(JavaId _jid, CtBehavior _behavior, ClassVisitor _cv);

	/**
	 * Appends Java source code to the given {@link StringBuffer}. After all {@link IInstrumentor}s appended
	 * their respective code, the ensemble will be added to a {@link Construct}, which will then be compiled.
	 */
	public void instrument(StringBuffer _code, JavaId _jid, CtBehavior _behavior, ClassVisitor _cv) throws CannotCompileException;

	/**
	 * Saves the information that has been collected by the instrumentor
	 * in the context of the given {@link AbstractGoal}.
	 */
	public void upladInformation(AbstractGoal _exe, int batchSize);

	/**
	 * Make the current thread wait until the upload of the information finished.
	 */
	public void awaitUpload();

	/**
	 * Return instrumentor-specific statistics.
	 * @return
	 */
	public Map<String,Long> getStatistics();
}
