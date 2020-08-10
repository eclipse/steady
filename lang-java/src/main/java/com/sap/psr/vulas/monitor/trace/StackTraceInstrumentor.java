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
import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.monitor.ClassVisitor;

import javassist.CannotCompileException;
import javassist.CtBehavior;

/**
 * Tracks how many times a construct has been invoked. Downloads the list of relevant bugs from the backend and
 * collects the stack trace for all their change list elements. Stack traces collected will be transformed into
 * call paths that are again uploaded to the backend.
 */
public class StackTraceInstrumentor extends AbstractTraceInstrumentor {

    // ====================================== STATIC MEMBERS

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

    // ====================================== INSTANCE MEMBERS

    /**
     * Constructs for which the stacktrace will be collected and transformed into a path.
     */
    private Set<ConstructId> constructsCollectStacktrace = null;

    /**
     * <p>Constructor for StackTraceInstrumentor.</p>
     */
    public StackTraceInstrumentor() {
        try {
            final Map<String, Set<com.sap.psr.vulas.shared.json.model.ConstructId>>
                    bug_change_lists =
                            BackendConnector.getInstance()
                                    .getAppBugs(
                                            CoreConfiguration.buildGoalContextFromConfiguration(
                                                    this.vulasConfiguration),
                                            CoreConfiguration.getAppContext(
                                                    this.vulasConfiguration));
            this.constructsCollectStacktrace = AbstractTraceInstrumentor.merge(bug_change_lists);
        } catch (ConfigurationException e) {
            StackTraceInstrumentor.log.error("Error during instantiation: " + e.getMessage());
            throw new IllegalStateException("Error during instantiation: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            StackTraceInstrumentor.log.error("Error during instantiation: " + e.getMessage());
            throw new IllegalStateException("Error during instantiation: " + e.getMessage(), e);
        } catch (BackendConnectionException e) {
            StackTraceInstrumentor.log.error("Error during instantiation: " + e.getMessage());
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
        return this.constructsCollectStacktrace != null
                && this.constructsCollectStacktrace.contains(_construct);
    }

    /** {@inheritDoc} */
    public void instrument(StringBuffer _code, JavaId _jid, CtBehavior _behavior, ClassVisitor _cv)
            throws CannotCompileException {

        // Inject some basic stuff common to several instrumentors
        this.injectUrlAndLoader(_code, _jid, _behavior);

        // Add counter and increment it at every call
        final String member_name = _cv.getUniqueMemberName("VUL_TRC", _behavior.getName(), true);
        _cv.addIntMember(member_name, false);
        _code.append(member_name).append("++;");

        // Map containing all instrumentor specific parameters and flags to be used by the Execution
        // monitor to properly treat the trace
        // generic not supported by javassist:
        // _code.append("java.util.Map<String,java.io.Serializable> params = new
        // java.util.HashMap<String,java.io.Serializable>();");
        // http://stackoverflow.com/questions/33279914/javassist-cannotcompileexception-when-trying-to-add-a-line-to-create-a-map
        _code.append("java.util.Map params = new java.util.HashMap();");

        // Add stack trace only if the construct is in a bug change list
        final boolean is_stacktrace_requested = this.isStacktraceRequestedFor(_jid);
        if (is_stacktrace_requested) {
            this.injectStacktrace(_code, _jid, _behavior);
            _code.append("params.put(\"stacktrace\", vul_st);");
        } else {
            _code.append("params.put(\"stacktrace\", null);");
        }

        _code.append("params.put(\"path\", \"" + Boolean.valueOf(is_stacktrace_requested) + "\");");
        _code.append(
                "params.put(\"junit\", \"true\");"); // corresponds to collectDetails= true ->
                                                     // SIngle StacktraceInstrumentor. junit is the
                                                     // name of the argument in the Execution
                                                     // Monitor
        _code.append("params.put(\"counter\", new Integer(" + member_name + "));");

        // Callback method
        if (_jid.getType() == JavaId.Type.CONSTRUCTOR)
            _code.append("com.sap.psr.vulas.monitor.trace.TraceCollector.callbackConstructor");
        else if (_jid.getType() == JavaId.Type.METHOD)
            _code.append("com.sap.psr.vulas.monitor.trace.TraceCollector.callbackMethod");
        else if (_jid.getType() == JavaId.Type.CLASSINIT)
            _code.append("com.sap.psr.vulas.monitor.trace.TraceCollector.callbackClinit");

        // Callback args
        _code.append("(\"")
                .append(ClassVisitor.removeParameterQualification(_behavior.getLongName()))
                .append("\",vul_cls_ldr,vul_cls_res,");

        if (_cv.getOriginalArchiveDigest() != null)
            _code.append("\"").append(_cv.getOriginalArchiveDigest()).append("\",");
        else _code.append("null,");

        // If specified, include the application context, otherwise null, null, null
        if (_cv.getAppContext() != null) {
            _code.append("\"").append(_cv.getAppContext().getMvnGroup()).append("\",");
            _code.append("\"").append(_cv.getAppContext().getArtifact()).append("\",");
            _code.append("\"").append(_cv.getAppContext().getVersion()).append("\",");
        } else _code.append("null,null,null,");

        // Map containing instrumentor specific parameter
        _code.append("params);");
    }
}
