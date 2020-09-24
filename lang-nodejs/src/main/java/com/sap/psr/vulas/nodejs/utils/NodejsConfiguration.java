package com.sap.psr.vulas.nodejs.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Wraps {@link VulasConfiguration} for accessing core-specific configuration settings.
 *
 */
public class NodejsConfiguration {

    private static final Log log = LogFactory.getLog(NodejsConfiguration.class);

    public final static String JS_BOM_IGNORE_PACKS = "vulas.core.bom.nodejs.ignorePacks";

    public final static String JS_NPM_PATH = "vulas.core.bom.nodejs.npm";

    public final static String JS_NODE_PATH = "vulas.core.bom.nodejs.node";
}
