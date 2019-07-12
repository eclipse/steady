package com.sap.psr.vulas.core.util;

import java.util.ServiceLoader;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.goals.GoalContext;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import com.sap.psr.vulas.shared.json.model.Space;
import com.sap.psr.vulas.shared.json.model.Tenant;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import com.sap.psr.vulas.sign.SignatureFactory;

/**
 * Wraps {@link VulasConfiguration} for accessing core-specific configuration settings.
 * 
 */
public class CoreConfiguration {

	private static final Log log = LogFactory.getLog(CoreConfiguration.class);

	// Goal context
	public final static String TENANT_TOKEN = "vulas.core.tenant.token";

	public final static String SPACE_TOKEN  = "vulas.core.space.token";
	public final static String SPACE_NAME   = "vulas.core.space.name";
	public final static String SPACE_DESCR  = "vulas.core.space.description";
	public final static String SPACE_EXPCFG = "vulas.core.space.exportConfiguration";
	public final static String SPACE_PUBLIC = "vulas.core.space.public";
	public final static String SPACE_BUGFLT = "vulas.core.space.bugFilter";
	public final static String SPACE_OWNERS = "vulas.core.space.bugFilter";

	public final static String APP_CTX_GROUP = "vulas.core.appContext.group";
	public final static String APP_CTX_ARTIF = "vulas.core.appContext.artifact";
	public final static String APP_CTX_VERSI = "vulas.core.appContext.version";

	// General
	public enum ConnectType { OFFLINE, READ_ONLY, READ_WRITE };
	public final static String BACKEND_CONNECT  = "vulas.core.backendConnection";
	
	public final static String UPLOAD_DIR       = "vulas.core.uploadDir";
	public final static String UPLOAD_DEL_AFTER = "vulas.core.upload.deleteAfterSuccess";

	public final static String REPEAT_MAX     = "vulas.core.repeatHttp.max";
	public final static String REPEAT_WAIT    = "vulas.core.repeatHttp.waitMilli";
	
	public final static String VERIFY_JARS = "vulas.core.verifyJars";
	
	public final static String JAR_TIMEOUT = "vulas.core.jarAnalysis.timeout";
	
	// CLEAN
	public final static String CLEAN_HISTORY         = "vulas.core.clean.goalHistory";
	public final static String CLEAN_PURGE_VERSIONS  = "vulas.core.clean.purgeVersions";
	public final static String CLEAN_PURGE_KEEP_LAST = "vulas.core.clean.purgeVersions.keepLast";

	// BOM, fka APP	
	public final static String APP_DIRS         = "vulas.core.app.sourceDir";
	public final static String APP_PREFIXES     = "vulas.core.app.appPrefixes";
	public final static String APP_JAR_NAMES    = "vulas.core.app.appJarNames";
	public final static String APP_UPLOAD_EMPTY = "vulas.core.app.uploadEmpty";
	public final static String APP_LIB_UPLOAD   = "vulas.core.app.uploadLibrary";

	public final static String REP_AGGR_MODULES  = "vulas.report.aggregateModules";
	public final static String REP_EXC_THRESHOLD = "vulas.report.exceptionThreshold";
	public final static String REP_EXC_SCOPE_BL  = "vulas.report.exceptionScopeBlacklist";
	public final static String REP_EXCL_UNASS    = "vulas.report.exceptionExcludeUnassessed";
	public final static String REP_EXCL_BUGS     = "vulas.report.exceptionExcludeBugs";
	public final static String REP_DIR           = "vulas.report.reportDir";
	public final static String REP_OVERRIDE_VER  = "vulas.report.overridePomVersion";

	public final static String SEQ_DEFAULT       = "vulas.core.sequence.defaultGoals";

	public final static String SIGN_BUGS         = "vulas.core.sign.bugs";
	//TODO: Name the other properly
	public final static String SIGN_BUGS0         = "vulas.core.sign.saveEditScripts";
	public final static String SIGN_BUGS1         = "vulas.core.sign.saveEditScriptIntersection";
	public final static String SIGN_BUGS2         = "vulas.core.sign.saveDecompiledArchive";
	public final static String SIGN_BUGS3         = "vulas.core.sign.showDecompiledConstruct";
	public final static String SIGN_BUGS4         = "vulas.core.sign.relaxDecompiler";
	public final static String SIGN_BUGS5         = "vulas.core.sign.relaxEditScript";
	public final static String SIGN_BUGS6         = "vulas.core.sign.relaxEqualIgnoreParentRoot";
	public final static String SIGN_BUGS7         = "vulas.core.sign.relaxStripFinals";
	public final static String SIGN_BUGS8         = "vulas.core.sign.relaxRelaxedByDefault";

	public final static String INSTR_SRC_DIR           = "vulas.core.instr.sourceDir";
	public final static String INSTR_SEARCH_RECURSIVE  = "vulas.core.instr.searchRecursive";
	public final static String INSTR_TARGET_DIR        = "vulas.core.instr.targetDir";
	public final static String INSTR_INCLUDE_DIR       = "vulas.core.instr.includeDir";
	public final static String INSTR_LIB_DIR           = "vulas.core.instr.libDir";
	public final static String INSTR_CHOOSEN_INSTR     = "vulas.core.instr.instrumentorsChoosen";
	public final static String INSTR_MAX_STACKTRACES   = "vulas.core.instr.maxStacktraces";
	public final static String INSTR_WRITE_CODE        = "vulas.core.instr.writeCode";
	public final static String INSTR_FLD_ANNOS         = "vulas.core.instr.fieldAnnotations";

	public final static String INSTR_BLACKLIST_DIRS           = "vulas.core.instr.blacklist.dirs";

	public final static String INSTR_BLACKLIST_JAR_SCOPES     = "vulas.core.instr.blacklist.jars.ignoreScopes";
	public final static String INSTR_BLACKLIST_JARS           = "vulas.core.instr.blacklist.jars";
	public final static String INSTR_BLACKLIST_CUSTOM_JARS    = "vulas.core.instr.blacklist.jars.custom";

	public final static String INSTR_BLACKLIST_CLASSES        = "vulas.core.instr.blacklist.classes";
	public final static String INSTR_BLACKLIST_JRE_CLASSES    = "vulas.core.instr.blacklist.classes.jre";
	public final static String INSTR_BLACKLIST_CUSTOM_CLASSES = "vulas.core.instr.blacklist.classes.custom";

	public final static String INSTR_BLACKLIST_CLASSLOADER    = "vulas.core.instr.blacklist.classloader";
	public final static String INSTR_BLACKLIST_CLASSLOADER_ACC_CHILD = "vulas.core.instr.whitelist.classloader.acceptChilds";
	
	public final static String INSTR_SLICE_WHITELIST       = "vulas.core.instr.slice.whitelist";
	public final static String INSTR_SLICE_BLACKLIST       = "vulas.core.instr.slice.blacklist";
	public final static String INSTR_SLICE_GUARD_OPEN      = "vulas.core.instr.slice.guardOpen";

	public final static String MONI_PERIODIC_UPL_ENABLED    = "vulas.core.monitor.periodicUpload.enabled";
	public final static String MONI_PERIODIC_UPL_INTERVAL   = "vulas.core.monitor.periodicUpload.interval";
	public final static String MONI_PERIODIC_UPL_BATCH_SIZE = "vulas.core.monitor.periodicUpload.batchSize";
	public final static String MONI_PERIODIC_MAX_ITEMS      = "vulas.core.monitor.maxItems";
	public final static String MONI_BLACKLIST_JARS          = "vulas.core.monitor.blacklist.jars";

	private static Integer cachedMaxItems = null;

	private static String vulasRelease = null;

	public static boolean isBackendOffline(VulasConfiguration _c) { return ConnectType.OFFLINE.equals(getBackendConnectType(_c)); }
	public static boolean isBackendReadOnly(VulasConfiguration _c) { return ConnectType.READ_ONLY.equals(getBackendConnectType(_c)); }
	public static boolean isBackendReadWrite(VulasConfiguration _c) { return ConnectType.READ_WRITE.equals(getBackendConnectType(_c)); }

	private static ConnectType getBackendConnectType(VulasConfiguration _c) {
		final String value = _c.getConfiguration().getString(BACKEND_CONNECT, null);
		if("READ_WRITE".equalsIgnoreCase(value))
			return ConnectType.READ_WRITE;
		else if("READ_ONLY".equalsIgnoreCase(value))
			return ConnectType.READ_ONLY;
		else if("OFFLINE".equalsIgnoreCase(value))
			return ConnectType.OFFLINE;
		else
			throw new IllegalStateException("Illegal value of configuration setting [" + BACKEND_CONNECT + "]: [" + value + "]");
	} 

	public static boolean isJarUploadEnabled(VulasConfiguration _vc) {
		return _vc.getConfiguration().getBoolean(APP_LIB_UPLOAD, false);
	}

	/**
	 * Reads configuration settings in order to instantiate an {@link Application}.
	 * @return an {@link Application)
	 * @throws ConfigurationException if the instantiation fails
	 */
	public static Application getAppContext() throws ConfigurationException {
		return getAppContext(VulasConfiguration.getGlobal());
	}
	
	public static Application getAppContext(VulasConfiguration _c) throws ConfigurationException {
		final Configuration c = _c.getConfiguration();
		Application a = null;
		try {
			a = new Application(c.getString(APP_CTX_GROUP), c.getString(APP_CTX_ARTIF), c.getString(APP_CTX_VERSI));
		} catch (IllegalArgumentException e) {
			throw new ConfigurationException("Application incomplete: " + e.getMessage(), e);
		}
		if(!a.isComplete())
			throw new ConfigurationException("Application incomplete: " + a.toString());
		return a;
	}
	
	/**
	 * Reads the global configuration in order to instantiate a {@link GoalContext}.
	 * @return a {@link GoalContext) built from the global configuration
	 */
	public static final GoalContext buildGoalContextFromGlobalConfiguration() {
		return buildGoalContextFromConfiguration(VulasConfiguration.getGlobal());
	}

	/**
	 * Reads the global configuration in order to instantiate a {@link GoalContext}.
	 * @return a {@link GoalContext) built from the global configuration
	 */
	public static final GoalContext buildGoalContextFromConfiguration(VulasConfiguration _c) {
		final Configuration c = _c.getConfiguration();
		
		final GoalContext ctx = new GoalContext();
		
		ctx.setVulasConfiguration(_c);
		
		// Tenant
		Tenant tenant = null;
		if(!_c.isEmpty(CoreConfiguration.TENANT_TOKEN)) {
			tenant = new Tenant(c.getString(CoreConfiguration.TENANT_TOKEN)); 
			ctx.setTenant(tenant);
//			log.info("Using tenant " + tenant);
		}
		else {
			log.warn("No tenant configured, hence, the backend's default tenant is used");
		}
		
		// Space
		if(!_c.isEmpty(CoreConfiguration.SPACE_TOKEN)) {
			final Space space = new Space();
			space.setSpaceToken(c.getString(CoreConfiguration.SPACE_TOKEN));
			ctx.setSpace(space);
//			log.info("Using space " + space);
		}
		else {
			log.warn("No space configured, hence, the default space of " + (tenant==null ? "the default tenant" : "tenant [" + tenant + "]") + " is used");
		}
			
		// App
		final Application a = new Application(c.getString(CoreConfiguration.APP_CTX_GROUP), c.getString(CoreConfiguration.APP_CTX_ARTIF), c.getString(CoreConfiguration.APP_CTX_VERSI));
		if(a.isComplete())
			ctx.setApplication(a);
		else
			log.warn("Incomplete application context: " + a.toString());
		return ctx;
	}

	public static final boolean existsInBackend(Application _app) {
		boolean exists = false;
		try {
			exists = BackendConnector.getInstance().isAppExisting(CoreConfiguration.buildGoalContextFromGlobalConfiguration(), _app);
		} catch (BackendConnectionException e) {
			log.error("Error while checking whether " + _app + " exists in backend: " + e.getMessage());
		}
		return exists;
	}
	
	/**
	 * Returns true if a maximum number of items has been configured via {@link CoreConfiguration#MONI_PERIODIC_MAX_ITEMS} and
	 * the given number is greater or equal to this maximum.
	 * @param _count_collected_items
	 * @return
	 */
	public static synchronized boolean isMaxItemsCollected(int _count_collected_items) {
		if(cachedMaxItems==null)
			cachedMaxItems = VulasConfiguration.getGlobal().getConfiguration().getInt(CoreConfiguration.MONI_PERIODIC_MAX_ITEMS, -1);
		return cachedMaxItems!=-1 && _count_collected_items>=cachedMaxItems;
	}

	/**
	 * Reads the Manifest file entry "Implementation-Version" from the Java archive from which this class was loaded.
	 * @return the Vulas release
	 */
	public static synchronized String getVulasRelease() {
		if(vulasRelease==null) {		
			
			// Shortcut for determining the Vulas version
			vulasRelease = CoreConfiguration.class.getPackage().getImplementationVersion();
			
			if(vulasRelease==null || vulasRelease.equals("")) {
				CoreConfiguration.log.warn("Cannot determine Vulas version from manifest entry [Implementation-Version], check Vulas JAR");
				vulasRelease = "unknown";
			}
			
			// Too cumbersome, use above shortcut			
			/*final ClassLoader l = CoreConfiguration.class.getClassLoader();
			if(l!=null) {
				final URL r = l.getResource(CoreConfiguration.class.getName().replace('.', '/') + ".class");
				if(r!=null) {
					String path_string = r.toString();
					if(path_string.indexOf(".jar!")!=-1) {
						JarFile jar = null;
						try {
							path_string = path_string.substring(path_string.indexOf("file:/"), path_string.lastIndexOf('!'));
							final Path p = Paths.get(new URI(path_string));
							jar = new JarFile(p.toFile(), false, JarFile.OPEN_READ);
							if(jar!=null) {
								final Manifest m = jar.getManifest();
								if(m!=null) {
									vulasRelease = m.getMainAttributes().getValue("Implementation-Version");
								}
							}
						} catch (Exception e) {
							CoreConfiguration.log.error("Error while determining Vulas release from JAR [" + r + "]: " + e.getMessage());
						} finally {
							if(jar!=null) {
								try {
									jar.close();
								} catch (IOException e) {
									CoreConfiguration.log.error("Error closing JAR [" + r + "]: " + e.getMessage());
								}
							}
						}
					}
				}
			}*/
		}
		return vulasRelease;
	}

	/**
	 * Loops over available implementations of {@link SignatureFactory}, checks whether the given id is supported and returns an instance.
	 * @param _id
	 * @return
	 */
	public static SignatureFactory getSignatureFactory(ConstructId _id) {
		final ServiceLoader<SignatureFactory> loader = ServiceLoader.load(SignatureFactory.class);
		SignatureFactory factory = null;
		for(SignatureFactory la: loader) {
			if(la.isSupportedConstructId(_id)) {
				factory = la;
			}
		}
		return factory;
	}
}
