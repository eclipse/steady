/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.monitor.trace;

import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.GoalContext;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.monitor.ClassVisitor;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

import javassist.CannotCompileException;
import javassist.CtBehavior;

/**
 * Tracks how many times a construct has been invoked. Downloads the list of relevant bugs from the backend and
 * collects the stack trace for all their change list elements. Stack traces collected will be transformed into
 * call paths that are again uploaded to the backend.
 */
public class SingleStackTraceInstrumentor extends AbstractTraceInstrumentor {

	// ====================================== STATIC MEMBERS

	private static final Log log = LogFactory.getLog(SingleStackTraceInstrumentor.class);

	// ====================================== INSTANCE MEMBERS

	/**
	 * Constructs for which the stacktrace will be collected and transformed into a path.
	 */
	private Set<ConstructId> constructsCollectStacktrace = null;

	private int maxStacktraces = -1;

	/**
	 * <p>Constructor for SingleStackTraceInstrumentor.</p>
	 */
	public SingleStackTraceInstrumentor() {
		final GoalContext gc = CoreConfiguration.buildGoalContextFromConfiguration(this.vulasConfiguration);
		this.maxStacktraces = this.vulasConfiguration.getConfiguration().getInt(CoreConfiguration.INSTR_MAX_STACKTRACES, 10);
		try {
			final Map<String, Set<com.sap.psr.vulas.shared.json.model.ConstructId>> bug_change_lists = BackendConnector.getInstance().getAppBugs(gc, CoreConfiguration.getAppContext(this.vulasConfiguration));
			this.constructsCollectStacktrace = AbstractTraceInstrumentor.merge(bug_change_lists);
		} catch (ConfigurationException e) {
			SingleStackTraceInstrumentor.log.error("Error during instantiation: " + e.getMessage());
			throw new IllegalStateException("Error during instantiation: " + e.getMessage(), e);
		} catch (IllegalStateException e) {
			SingleStackTraceInstrumentor.log.error("Error during instantiation: " + e.getMessage());
			throw new IllegalStateException("Error during instantiation: " + e.getMessage(), e);
		} catch (BackendConnectionException e) {
			SingleStackTraceInstrumentor.log.error("Error during instantiation: " + e.getMessage());
			throw new IllegalStateException("Error during instantiation: " + e.getMessage(), e);
		}
	}

	/**
	 * <p>isStacktraceRequestedFor.</p>
	 *
	 * @param _construct a {@link com.sap.psr.vulas.ConstructId} object.
	 * @return a boolean.
	 */
	public boolean isStacktraceRequestedFor(ConstructId _construct) {
		return this.constructsCollectStacktrace!=null && this.constructsCollectStacktrace.contains(_construct);
	}

	/** {@inheritDoc} */
	public void instrument(StringBuffer _code, JavaId _jid, CtBehavior _behavior, ClassVisitor _cv) throws CannotCompileException {

		// Add stack trace only if the construct is in a bug change list
		final String st_count = _cv.getUniqueMemberName("VUL_ST_COUNT", _behavior.getName(), true);

		// Add counter for 
		final boolean is_stacktrace_requested = this.isStacktraceRequestedFor(_jid);
		if(is_stacktrace_requested)
			_cv.addIntMember(st_count, false);

		// Add boolean and check it to ensure that the instr code is only executed once
		final String member_name = _cv.getUniqueMemberName("VUL_TRC", _behavior.getName(), true);
		_cv.addBooleanMember(member_name, false, false);
		_code.append("if(!").append(member_name);

		// Collect stacktrace info?
		if(is_stacktrace_requested) {
			// Check stacktrace counter before getting stacktrace for vuln method
			if(this.maxStacktraces!=-1)
				_code.append(" || ").append(st_count).append("<").append(this.maxStacktraces).append(") {");
			// Always get stacktrace for vuln method
			else
				_code.append(" || true) {");
		}
		else
			_code.append(") {");

		this.injectUrlAndLoader(_code, _jid, _behavior);

		//Map containing all instrumentor specific parameters and flags to be used by the Execution monitor to properly treat the trace
		//generic not supported by javassist: _code.append("java.util.Map<String,java.io.Serializable> params = new java.util.HashMap<String,java.io.Serializable>();");
		//http://stackoverflow.com/questions/33279914/javassist-cannotcompileexception-when-trying-to-add-a-line-to-create-a-map
		_code.append("java.util.Map params = new java.util.HashMap();");

		if(is_stacktrace_requested) {
			this.injectStacktrace(_code, _jid, _behavior);
			_code.append("params.put(\"stacktrace\", vul_st);");
			_code.append(st_count).append("++;");
		} else {
			_code.append("params.put(\"stacktrace\", null);");
		}

		_code.append("params.put(\"path\", \""+ Boolean.valueOf(is_stacktrace_requested)+"\");");
		_code.append("params.put(\"junit\", \"true\");"); //corresponds to collectDetails= true -> SIngle StacktraceInstrumentor. junit is the name of the argument in the Execution Monitor
		_code.append("params.put(\"counter\", new Integer(1));");

		_code.append(member_name).append("=");

		// Callback method
		if(_jid.getType()==JavaId.Type.CONSTRUCTOR)
			_code.append("com.sap.psr.vulas.monitor.trace.TraceCollector.callbackConstructor");
		else if(_jid.getType()==JavaId.Type.METHOD)
			_code.append("com.sap.psr.vulas.monitor.trace.TraceCollector.callbackMethod");
		else if(_jid.getType()==JavaId.Type.CLASSINIT)
			_code.append("com.sap.psr.vulas.monitor.trace.TraceCollector.callbackClinit");

		// Callback args
		_code.append("(\"").append(ClassVisitor.removeParameterQualification(_behavior.getLongName())).append("\",vul_cls_ldr,vul_cls_res,");

		if(_cv.getOriginalArchiveDigest()!=null)
			_code.append("\"").append(_cv.getOriginalArchiveDigest()).append("\",");
		else
			_code.append("null,");

		// If specified, include the application context, otherwise null, null, null
		if(_cv.getAppContext()!=null) {
			_code.append("\"").append(_cv.getAppContext().getMvnGroup()).append("\",");
			_code.append("\"").append(_cv.getAppContext().getArtifact()).append("\",");
			_code.append("\"").append(_cv.getAppContext().getVersion()).append("\",");
		}
		else
			_code.append("null,null,null,");

		// Map containing instrumentor specific parameter
		_code.append("params); }");
	} 
}
