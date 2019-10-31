package com.sap.psr.vulas.monitor;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates implementations of {@link IInstrumentor} for all classes specified via the configuration
 * option {@link CoreConfiguration#INSTR_CHOOSEN_INSTR}.
 */
public class InstrumentorFactory {

  private static final Log log = LogFactory.getLog(InstrumentorFactory.class);

  private static List<IInstrumentor> instrumentors = null;

  /**
   * Creates and returns implementations of {@link IInstrumentor} for all classes specified via the
   * configuration option {@link CoreConfiguration#INSTR_CHOOSEN_INSTR}. Those will be looped during
   * static and dynamic instrumentation, e.g., in the classes {@link ExecutionMonitor} and {@link
   * ClassVisitor}.
   *
   * @return a {@link java.util.List} object.
   */
  public static synchronized List<IInstrumentor> getInstrumentors() {
    if (instrumentors == null) {
      instrumentors = new ArrayList<IInstrumentor>();
      final Configuration cfg = VulasConfiguration.getGlobal().getConfiguration();
      final String[] instrumentors = cfg.getStringArray(CoreConfiguration.INSTR_CHOOSEN_INSTR);
      for (String name : instrumentors) {
        final AbstractInstrumentor i = InstrumentorFactory.getInstrumentor(name);
        if (i != null) InstrumentorFactory.instrumentors.add(i);
      }
    }
    return instrumentors;
  }

  private static AbstractInstrumentor getInstrumentor(String _name) {
    AbstractInstrumentor i = null;
    try {
      final Class cls = Class.forName(_name);
      i = (AbstractInstrumentor) cls.newInstance();
    } catch (Throwable e) {
      InstrumentorFactory.log.error(
          "Error while creating instrumentor of class [" + _name + "]: " + e.getMessage(), e);
    }
    return i;
  }
}
