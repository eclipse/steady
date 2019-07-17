package com.sap.psr.vulas.monitor.trace;



import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.monitor.ClassVisitor;

//import java.util.*;
//import java.io.Serializable;

import javassist.CannotCompileException;
import javassist.CtBehavior;

/**
 * Tracks whether a method has been invoked or not. The instrumentation code is only executed once.
 */
public class SingleTraceInstrumentor extends AbstractTraceInstrumentor {

	/** {@inheritDoc} */
	public void instrument(StringBuffer _code, JavaId _jid, CtBehavior _behavior,ClassVisitor _cv) throws CannotCompileException {
				
		// Add boolean and check it to ensure that the instr code is only executed once
		final String member_name = _cv.getUniqueMemberName("VUL_TRC", _behavior.getName(), true);
		_cv.addBooleanMember(member_name, false, false);
		_code.append("if(!").append(member_name).append(") {");

		this.injectUrlAndLoader(_code, _jid, _behavior);

		//Map containing all instrumentor specific parameters and flags to be used by the Execution monitor to properly treat the trace
		//generic not supported by javassist: _code.append("java.util.Map<String,java.io.Serializable> params = new java.util.HashMap<String,java.io.Serializable>();");
		//http://stackoverflow.com/questions/33279914/javassist-cannotcompileexception-when-trying-to-add-a-line-to-create-a-map
		_code.append("java.util.Map params = new java.util.HashMap();");
		_code.append("params.put(\"junit\", \"false\");"); //corresponds to collectDetails= false -> SIngle Instrumentor. junit is the name of the argument in the Execution Monitor
		_code.append("params.put(\"counter\", new Integer(1));");

		// Change boolean flag to prevent subsequent executions of the callback (only here, in the SingleInstrumentor)
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
