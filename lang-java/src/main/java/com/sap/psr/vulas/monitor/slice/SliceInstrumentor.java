package com.sap.psr.vulas.monitor.slice;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.AbstractGoal;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.monitor.AbstractInstrumentor;
import com.sap.psr.vulas.monitor.ClassVisitor;
import com.sap.psr.vulas.shared.json.model.Dependency;

import javassist.CannotCompileException;
import javassist.CtBehavior;

/**
 * Adds a configurable guard to all methods that have not been traced or found reachable.
 */
public class SliceInstrumentor extends AbstractInstrumentor {

	// ====================================== STATIC MEMBERS

	private static final Log log = LogFactory.getLog(SliceInstrumentor.class);

	// ====================================== INSTANCE MEMBERS

	/**
	 * Constructs that have been traced.
	 */
	private Set<ConstructId> tracedConstructs = null;

	/**
	 * Constructs that have been found reachable.
	 */
	private Set<Dependency> dependencies = null;
	
	public SliceInstrumentor() {
		try {
			this.tracedConstructs = JavaId.toCoreType(BackendConnector.getInstance().getAppTraces(CoreConfiguration.buildGoalContextFromConfiguration(this.vulasConfiguration), CoreConfiguration.getAppContext(this.vulasConfiguration)));
			this.dependencies = BackendConnector.getInstance().getAppDependencies(CoreConfiguration.buildGoalContextFromConfiguration(this.vulasConfiguration), CoreConfiguration.getAppContext(this.vulasConfiguration));
		} catch (ConfigurationException e) {
			SliceInstrumentor.log.error("Error during instantiation: " + e.getMessage());
			throw new IllegalStateException("Error during instantiation: " + e.getMessage(), e);
		} catch (IllegalStateException e) {
			SliceInstrumentor.log.error("Error during instantiation: " + e.getMessage());
			throw new IllegalStateException("Error during instantiation: " + e.getMessage(), e);
		} catch (BackendConnectionException e) {
			SliceInstrumentor.log.error("Error during instantiation: " + e.getMessage());
			throw new IllegalStateException("Error during instantiation: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Accepts every construct that has been traced or found reachable.
	 */
	@Override
	public boolean acceptToInstrument(JavaId _jid, CtBehavior _behavior, ClassVisitor _cv) {
		return !this.tracedConstructs.contains(_jid) && !this.isReachable(_jid);
	}
	
	/**
	 * Returns true if the given {@link JavaId} is among the reachable constructs of any of the application {@link Dependency}s, false otherwise.
	 */
	private boolean isReachable(JavaId _jid) {
		boolean is_reachable = false;
		for(Dependency d: this.dependencies) {
			if(d.getReachableConstructIds().contains(_jid)) {
				is_reachable = true;
				break;
			}
		}
		return is_reachable;
	}

	public void instrument(StringBuffer _code, JavaId _jid, CtBehavior _behavior, ClassVisitor _cv) throws CannotCompileException {
		_code.append("final boolean is_open = Boolean.parseBoolean(System.getProperty(\"").append(CoreConfiguration.INSTR_GUARD_OPEN).append("\"));");
		_code.append("System.err.println(\"Execution of " + _jid.toString() + "\" + (is_open ? \"allowed\" : \"prevented\") + \" by Vulas guarding condition\");");
		_code.append("if(!is_open) throw new IllegalStateException(\"Execution of " + _jid.toString() + "\" + prevented by Vulas guarding condition\");");
	}
	
	/**
	 * Implementation does not do anything.
	 */
	@Override
	public void upladInformation(AbstractGoal _exe, int _batch_size) { ; }

	/**
	 * Implementation does not do anything.
	 */
	@Override
	public void awaitUpload() { ; }

	@Override
	public Map<String,Long> getStatistics() {
		final Map<String, Long> stats = new HashMap<String, Long>();
		//TODO: Add number of instrumented methods
		return stats;
	}
}
