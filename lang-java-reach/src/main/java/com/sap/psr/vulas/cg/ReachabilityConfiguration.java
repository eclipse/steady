package com.sap.psr.vulas.cg;

public class ReachabilityConfiguration {

    public final static String REACH_BUGS = "vulas.reach.bugs";
    public final static String REACH_FWK = "vulas.reach.fwk";
    public final static String REACH_EXIT_UNKOWN_EP = "vulas.reach.exitOnUnknownEntryPoints";
    public final static String REACH_CONSTR_FILTER = "vulas.reach.constructFilter";
    public final static String REACH_EXCL_JARS = "vulas.reach.excludeJars";
    public final static String REACH_EXCL_PACK = "vulas.reach.excludePackages";
    public final static String REACH_PREPROCESS = "vulas.reach.preprocessDependencies";
    public final static String REACH_TIMEOUT = "vulas.reach.timeout";
    public final static String REACH_MAX_PATH = "vulas.reach.maxPathPerChangeListElement";

    public final static String REACH_BL_CLASS_JRE = "vulas.reach.blacklist.classes.jre";
    public final static String REACH_BL_CLASS_CUST = "vulas.reach.blacklist.classes.custom";

    public final static String REACH_TOUCHPOINTS = "vulas.reach.identifyTouchpoints";

    public final static String REACH_SEARCH_SHORTEST = "vulas.reach.searchShortest";

    public final static String REACH_WRITE_TO = "vulas.reach.callgraph.writeTo";
    public final static String REACH_READ_FROM = "vulas.reach.callgraph.readFrom";

    public final static String CLI_PLUGIN_DIR = "vulas.reach.cli.plugins.dir";
}