package com.sap.psr.vulas.monitor.touch;

import com.sap.psr.vulas.goals.AbstractGoal;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.monitor.AbstractInstrumentor;
import com.sap.psr.vulas.monitor.ClassVisitor;
import com.sap.psr.vulas.monitor.DynamicTransformer;
import com.sap.psr.vulas.monitor.IInstrumentor;
import java.util.HashMap;
import java.util.Map;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** TouchPointInstrumentor class. */
public class TouchPointInstrumentor extends AbstractInstrumentor {

  private static final Log log = LogFactory.getLog(TouchPointInstrumentor.class);

  /** {@inheritDoc} */
  public void instrument(StringBuffer _code, JavaId _jid, CtBehavior _behavior, ClassVisitor _cv)
      throws CannotCompileException {

    // Inject some basic stuff common to several instrumentors
    this.injectUrlAndLoader(_code, _jid, _behavior);
    this.injectStacktrace(_code, _jid, _behavior);

    // Is the construct in question part of the application?
    final boolean callee_in_app = ConstructIdUtil.getInstance().isAppConstruct(_jid);

    // Get type and value of all method arguments
    _code.append("final java.util.Map tp_params = new java.util.HashMap();");
    final String callee_qname = _behavior.getLongName();

    // The following code causes a StackOverflowException in certain cases --> see vulas-testapp,
    // packages POI and xmlbeans
    // Reason is maybe the call of this.toString() in constructors, which is injected due to the
    // loop starting at index 0
    // $0 means 'this' for Javassist
    /*final String callee_args = callee_qname.substring(callee_qname.indexOf('(')+1, callee_qname.indexOf(')'));

    String [] arg_types = null;
    if(callee_args.length() > 0)
    	arg_types = callee_args.split(",");
    else
    	arg_types = new String[0];

    for(int i=0; i<arg_types.length; i++){
    	// the key into a map cannot contain [] or <>
    	_code.append("tp_params.put(\"arg_type_\" + \"" + i +"\", \"" + arg_types[i] + "\");");
    	if(!arg_types[i].contains("<") && !arg_types[i].contains(">")) {
    		if(arg_types[i].contains(".")){ // assuming that a param type contains at least a point (e.g. java.lang.String)
    			_code.append("if($" + (i+1) + " != null){");
    			_code.append("tp_params.put(\"arg_value_\" + \"" + i +"\", $" + (i+1) + ".toString());");
    			_code.append("}");
    		}
    		else { // this case contains every case that is no an object (int, boolean, byte[])
    			_code.append("tp_params.put(\"arg_value_\" + \"" + i +"\", String.valueOf($" + (i+1) + "));");
    		}
    	}
    }*/

    _code.append("com.sap.psr.vulas.monitor.touch.TouchPointCollector.callback");
    _code
        .append("(\"")
        .append(_jid.getType() + "\",\"")
        .append(ClassVisitor.removeParameterQualification(_behavior.getLongName()))
        .append("\",vul_cls_ldr,vul_cls_res," + callee_in_app + ",vul_st,");

    // If specified, include the application context, otherwise null, null, null
    if (_cv.getAppContext() != null) {
      _code.append("\"").append(_cv.getAppContext().getMvnGroup()).append("\",");
      _code.append("\"").append(_cv.getAppContext().getArtifact()).append("\",");
      _code.append("\"").append(_cv.getAppContext().getVersion()).append("\",");
    } else _code.append("null,null,null,");

    // Map containing instrumentor specific parameter
    _code.append("tp_params);");
  }

  /** {@inheritDoc} */
  @Override
  public void upladInformation(AbstractGoal _exe, int _batch_size) {
    TouchPointCollector.getInstance().uploadInformation(_exe, _batch_size);
  }

  /** {@inheritDoc} */
  @Override
  public void awaitUpload() {
    ;
  }

  // TODO: Add statistics
  /** {@inheritDoc} */
  @Override
  public Map<String, Long> getStatistics() {
    return new HashMap<String, Long>();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Accepts every class.
   *
   * <p>Note that the instrumentation can involve two levels of filtering: First, {@link
   * DynamicTransformer#transform(ClassLoader, String, Class, java.security.ProtectionDomain,
   * byte[])} filters according to class names, JAR names and JAR directory locations. Second, every
   * {@link IInstrumentor} can apply an additional filter in the implementation of this method.
   */
  @Override
  public boolean acceptToInstrument(JavaId _jid, CtBehavior _behavior, ClassVisitor _cv) {
    return true;
  }
}
