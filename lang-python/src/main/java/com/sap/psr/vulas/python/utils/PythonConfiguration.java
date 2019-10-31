package com.sap.psr.vulas.python.utils;

import com.sap.psr.vulas.shared.util.VulasConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Wraps {@link VulasConfiguration} for accessing core-specific configuration settings. */
public class PythonConfiguration {

  private static final Log log = LogFactory.getLog(PythonConfiguration.class);

  /** Constant <code>PY_BOM_IGNORE_PACKS="vulas.core.bom.python.ignorePacks"</code> */
  public static final String PY_BOM_IGNORE_PACKS = "vulas.core.bom.python.ignorePacks";

  /** Constant <code>PY_PIP_PATH="vulas.core.bom.python.pip"</code> */
  public static final String PY_PIP_PATH = "vulas.core.bom.python.pip";

  /** Constant <code>PY_PY_PATH="vulas.core.bom.python.python"</code> */
  public static final String PY_PY_PATH = "vulas.core.bom.python.python";
}
