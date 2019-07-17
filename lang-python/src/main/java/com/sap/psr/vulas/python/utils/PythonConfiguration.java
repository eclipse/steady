package com.sap.psr.vulas.python.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.goals.GoalContext;
import com.sap.psr.vulas.shared.enums.ExportConfiguration;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import com.sap.psr.vulas.shared.json.model.Space;
import com.sap.psr.vulas.shared.json.model.Tenant;
import com.sap.psr.vulas.shared.util.Constants;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import com.sap.psr.vulas.sign.SignatureFactory;

/**
 * Wraps {@link VulasConfiguration} for accessing core-specific configuration settings.
 */
public class PythonConfiguration {

	private static final Log log = LogFactory.getLog(PythonConfiguration.class);

	/** Constant <code>PY_BOM_IGNORE_PACKS="vulas.core.bom.python.ignorePacks"</code> */
	public final static String PY_BOM_IGNORE_PACKS = "vulas.core.bom.python.ignorePacks";
	
	/** Constant <code>PY_PIP_PATH="vulas.core.bom.python.pip"</code> */
	public final static String PY_PIP_PATH = "vulas.core.bom.python.pip";
	
	/** Constant <code>PY_PY_PATH="vulas.core.bom.python.python"</code> */
	public final static String PY_PY_PATH = "vulas.core.bom.python.python";
}

