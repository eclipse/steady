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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.core.util;

import java.util.ServiceLoader;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.goals.GoalContext;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.json.model.ConstructId;
import org.eclipse.steady.shared.json.model.Space;
import org.eclipse.steady.shared.json.model.Tenant;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.eclipse.steady.sign.SignatureFactory;

/**
 * Wraps {@link VulasConfiguration} for accessing core-specific configuration settings.
 */
public class CoreConfiguration {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  // Goal context
  /** Constant <code>TENANT_TOKEN="vulas.core.tenant.token"</code> */
  public static final String TENANT_TOKEN = "vulas.core.tenant.token";

  /** Constant <code>SPACE_TOKEN="vulas.core.space.token"</code> */
  public static final String SPACE_TOKEN = "vulas.core.space.token";
  /** Constant <code>SPACE_NAME="vulas.core.space.name"</code> */
  public static final String SPACE_NAME = "vulas.core.space.name";
  /** Constant <code>SPACE_DESCR="vulas.core.space.description"</code> */
  public static final String SPACE_DESCR = "vulas.core.space.description";
  /** Constant <code>SPACE_EXPCFG="vulas.core.space.exportConfiguration"</code> */
  public static final String SPACE_EXPCFG = "vulas.core.space.exportConfiguration";
  /** Constant <code>SPACE_PUBLIC="vulas.core.space.public"</code> */
  public static final String SPACE_PUBLIC = "vulas.core.space.public";
  /** Constant <code>SPACE_BUGFLT="vulas.core.space.bugFilter"</code> */
  public static final String SPACE_BUGFLT = "vulas.core.space.bugFilter";
  /** Constant <code>SPACE_OWNERS="vulas.core.space.bugFilter"</code> */
  public static final String SPACE_OWNERS = "vulas.core.space.bugFilter";

  /** Constant <code>APP_CTX_GROUP="vulas.core.appContext.group"</code> */
  public static final String APP_CTX_GROUP = "vulas.core.appContext.group";
  /** Constant <code>APP_CTX_ARTIF="vulas.core.appContext.artifact"</code> */
  public static final String APP_CTX_ARTIF = "vulas.core.appContext.artifact";
  /** Constant <code>APP_CTX_VERSI="vulas.core.appContext.version"</code> */
  public static final String APP_CTX_VERSI = "vulas.core.appContext.version";

  // General
  public enum ConnectType {
    OFFLINE,
    READ_ONLY,
    READ_WRITE
  };
  /** Constant <code>BACKEND_CONNECT="vulas.core.backendConnection"</code> */
  public static final String BACKEND_CONNECT = "vulas.core.backendConnection";

  /** Constant <code>UPLOAD_DIR="vulas.core.uploadDir"</code> */
  public static final String UPLOAD_DIR = "vulas.core.uploadDir";
  /** Constant <code>UPLOAD_DEL_AFTER="vulas.core.upload.deleteAfterSuccess"</code> */
  public static final String UPLOAD_DEL_AFTER = "vulas.core.upload.deleteAfterSuccess";
  /** Constant <code>UPLOAD_MAX_SIZE="vulas.core.upload.maxSize"</code> */
  public static final String UPLOAD_MAX_SIZE = "vulas.core.upload.maxSize";

  /** Constant <code>REPEAT_MAX="vulas.core.repeatHttp.max"</code> */
  public static final String REPEAT_MAX = "vulas.core.repeatHttp.max";
  /** Constant <code>REPEAT_WAIT="vulas.core.repeatHttp.waitMilli"</code> */
  public static final String REPEAT_WAIT = "vulas.core.repeatHttp.waitMilli";

  /** Constant <code>VERIFY_JARS="vulas.core.verifyJars"</code> */
  public static final String VERIFY_JARS = "vulas.core.verifyJars";

  /** Constant <code>JAR_TIMEOUT="vulas.core.jarAnalysis.timeout"</code> */
  public static final String JAR_TIMEOUT = "vulas.core.jarAnalysis.timeout";

  // CLEAN
  /** Constant <code>CLEAN_HISTORY="vulas.core.clean.goalHistory"</code> */
  public static final String CLEAN_HISTORY = "vulas.core.clean.goalHistory";
  /** Constant <code>CLEAN_PURGE_VERSIONS="vulas.core.clean.purgeVersions"</code> */
  public static final String CLEAN_PURGE_VERSIONS = "vulas.core.clean.purgeVersions";
  /** Constant <code>CLEAN_PURGE_KEEP_LAST="vulas.core.clean.purgeVersions.keepLast"</code> */
  public static final String CLEAN_PURGE_KEEP_LAST = "vulas.core.clean.purgeVersions.keepLast";

  // BOM, fka APP
  /** Constant <code>APP_DIRS="vulas.core.app.sourceDir"</code> */
  public static final String APP_DIRS = "vulas.core.app.sourceDir";
  /** Constant <code>TEST_DIRS="vulas.core.app.testDir"</code> */
  public static final String TEST_DIRS = "vulas.core.app.testDir";
  /** Constant <code>APP_PREFIXES="vulas.core.app.appPrefixes"</code> */
  public static final String APP_PREFIXES = "vulas.core.app.appPrefixes";
  /** Constant <code>APP_JAR_NAMES="vulas.core.app.appJarNames"</code> */
  public static final String APP_JAR_NAMES = "vulas.core.app.appJarNames";
  /** Constant <code>APP_UPLOAD_EMPTY="vulas.core.app.uploadEmpty"</code> */
  public static final String APP_UPLOAD_EMPTY = "vulas.core.app.uploadEmpty";
  /** Constant <code>APP_LIB_UPLOAD="vulas.core.app.uploadLibrary"</code> */
  public static final String APP_LIB_UPLOAD = "vulas.core.app.uploadLibrary";

  /** Constant <code>REP_AGGR_MODULES="vulas.report.aggregateModules"</code> */
  public static final String REP_AGGR_MODULES = "vulas.report.aggregateModules";
  /** Constant <code>REP_EXC_THRESHOLD="vulas.report.exceptionThreshold"</code> */
  public static final String REP_EXC_THRESHOLD = "vulas.report.exceptionThreshold";
  /** Constant <code>REP_DIR="vulas.report.reportDir"</code> */
  public static final String REP_DIR = "vulas.report.reportDir";
  /** Constant <code>REP_OVERRIDE_VER="vulas.report.overridePomVersion"</code> */
  public static final String REP_OVERRIDE_VER = "vulas.report.overridePomVersion";
  /** Constant <code>REP_LIB_ASSESS="vulas.report.createLibraryAssessments"</code> */
  public static final String REP_CREATE_AFF_LIB = "vulas.report.createLibraryAssessments";

  /** Constant <code>SLICING_DIR="vulas.slicing.slicingDir"</code> */
  public static final String SLICING_DIR = "vulas.slicing.slicingDir";

  /** Constant <code>SEQ_DEFAULT="vulas.core.sequence.defaultGoals"</code> */
  public static final String SEQ_DEFAULT = "vulas.core.sequence.defaultGoals";

  /** Constant <code>SIGN_BUGS="vulas.core.sign.bugs"</code> */
  public static final String SIGN_BUGS = "vulas.core.sign.bugs";
  // TODO: Name the other properly
  /** Constant <code>SIGN_BUGS0="vulas.core.sign.saveEditScripts"</code> */
  public static final String SIGN_BUGS0 = "vulas.core.sign.saveEditScripts";
  /** Constant <code>SIGN_BUGS1="vulas.core.sign.saveEditScriptIntersect"{trunked}</code> */
  public static final String SIGN_BUGS1 = "vulas.core.sign.saveEditScriptIntersection";
  /** Constant <code>SIGN_BUGS2="vulas.core.sign.saveDecompiledArchive"</code> */
  public static final String SIGN_BUGS2 = "vulas.core.sign.saveDecompiledArchive";
  /** Constant <code>SIGN_BUGS3="vulas.core.sign.showDecompiledConstruct"</code> */
  public static final String SIGN_BUGS3 = "vulas.core.sign.showDecompiledConstruct";
  /** Constant <code>SIGN_BUGS4="vulas.core.sign.relaxDecompiler"</code> */
  public static final String SIGN_BUGS4 = "vulas.core.sign.relaxDecompiler";
  /** Constant <code>SIGN_BUGS5="vulas.core.sign.relaxEditScript"</code> */
  public static final String SIGN_BUGS5 = "vulas.core.sign.relaxEditScript";
  /** Constant <code>SIGN_BUGS6="vulas.core.sign.relaxEqualIgnoreParentR"{trunked}</code> */
  public static final String SIGN_BUGS6 = "vulas.core.sign.relaxEqualIgnoreParentRoot";
  /** Constant <code>SIGN_BUGS7="vulas.core.sign.relaxStripFinals"</code> */
  public static final String SIGN_BUGS7 = "vulas.core.sign.relaxStripFinals";
  /** Constant <code>SIGN_BUGS8="vulas.core.sign.relaxRelaxedByDefault"</code> */
  public static final String SIGN_BUGS8 = "vulas.core.sign.relaxRelaxedByDefault";

  /** Constant <code>INSTR_SRC_DIR="vulas.core.instr.sourceDir"</code> */
  public static final String INSTR_SRC_DIR = "vulas.core.instr.sourceDir";
  /** Constant <code>INSTR_SEARCH_RECURSIVE="vulas.core.instr.searchRecursive"</code> */
  public static final String INSTR_SEARCH_RECURSIVE = "vulas.core.instr.searchRecursive";
  /** Constant <code>INSTR_TARGET_DIR="vulas.core.instr.targetDir"</code> */
  public static final String INSTR_TARGET_DIR = "vulas.core.instr.targetDir";
  /** Constant <code>INSTR_INCLUDE_DIR="vulas.core.instr.includeDir"</code> */
  public static final String INSTR_INCLUDE_DIR = "vulas.core.instr.includeDir";
  /** Constant <code>INSTR_LIB_DIR="vulas.core.instr.libDir"</code> */
  public static final String INSTR_LIB_DIR = "vulas.core.instr.libDir";
  /** Constant <code>INSTR_CHOOSEN_INSTR="vulas.core.instr.instrumentorsChoosen"</code> */
  public static final String INSTR_CHOOSEN_INSTR = "vulas.core.instr.instrumentorsChoosen";
  /** Constant <code>INSTR_MAX_STACKTRACES="vulas.core.instr.maxStacktraces"</code> */
  public static final String INSTR_MAX_STACKTRACES = "vulas.core.instr.maxStacktraces";
  /** Constant <code>INSTR_WRITE_CODE="vulas.core.instr.writeCode"</code> */
  public static final String INSTR_WRITE_CODE = "vulas.core.instr.writeCode";
  /** Constant <code>INSTR_FLD_ANNOS="vulas.core.instr.fieldAnnotations"</code> */
  public static final String INSTR_FLD_ANNOS = "vulas.core.instr.fieldAnnotations";

  /** Constant <code>INSTR_BLACKLIST_DIRS="vulas.core.instr.blacklist.dirs"</code> */
  public static final String INSTR_BLACKLIST_DIRS = "vulas.core.instr.blacklist.dirs";

  /** Constant <code>INSTR_BLACKLIST_JAR_SCOPES="vulas.core.instr.blacklist.jars.ignoreS"{trunked}</code> */
  public static final String INSTR_BLACKLIST_JAR_SCOPES =
      "vulas.core.instr.blacklist.jars.ignoreScopes";
  /** Constant <code>INSTR_BLACKLIST_JARS="vulas.core.instr.blacklist.jars"</code> */
  public static final String INSTR_BLACKLIST_JARS = "vulas.core.instr.blacklist.jars";
  /** Constant <code>INSTR_BLACKLIST_CUSTOM_JARS="vulas.core.instr.blacklist.jars.custom"</code> */
  public static final String INSTR_BLACKLIST_CUSTOM_JARS = "vulas.core.instr.blacklist.jars.custom";

  /** Constant <code>INSTR_BLACKLIST_CLASSES="vulas.core.instr.blacklist.classes"</code> */
  public static final String INSTR_BLACKLIST_CLASSES = "vulas.core.instr.blacklist.classes";
  /** Constant <code>INSTR_BLACKLIST_JRE_CLASSES="vulas.core.instr.blacklist.classes.jre"</code> */
  public static final String INSTR_BLACKLIST_JRE_CLASSES = "vulas.core.instr.blacklist.classes.jre";
  /** Constant <code>INSTR_BLACKLIST_CUSTOM_CLASSES="vulas.core.instr.blacklist.classes.cust"{trunked}</code> */
  public static final String INSTR_BLACKLIST_CUSTOM_CLASSES =
      "vulas.core.instr.blacklist.classes.custom";

  /** Constant <code>INSTR_BLACKLIST_CLASSLOADER="vulas.core.instr.blacklist.classloader"</code> */
  public static final String INSTR_BLACKLIST_CLASSLOADER = "vulas.core.instr.blacklist.classloader";
  /** Constant <code>INSTR_BLACKLIST_CLASSLOADER_ACC_CHILD="vulas.core.instr.whitelist.classloader."{trunked}</code> */
  public static final String INSTR_BLACKLIST_CLASSLOADER_ACC_CHILD =
      "vulas.core.instr.whitelist.classloader.acceptChilds";

  /** Constant <code>INSTR_SLICE_WHITELIST="vulas.core.instr.slice.whitelist"</code> */
  public static final String INSTR_SLICE_WHITELIST = "vulas.core.instr.slice.whitelist";
  /** Constant <code>INSTR_SLICE_BLACKLIST="vulas.core.instr.slice.blacklist"</code> */
  public static final String INSTR_SLICE_BLACKLIST = "vulas.core.instr.slice.blacklist";
  /** Constant <code>INSTR_SLICE_GUARD_OPEN="vulas.core.instr.slice.guardOpen"</code> */
  public static final String INSTR_SLICE_GUARD_OPEN = "vulas.core.instr.slice.guardOpen";

  /** Constant <code>MONI_PERIODIC_UPL_ENABLED="vulas.core.monitor.periodicUpload.enabl"{trunked}</code> */
  public static final String MONI_PERIODIC_UPL_ENABLED =
      "vulas.core.monitor.periodicUpload.enabled";
  /** Constant <code>MONI_PERIODIC_UPL_INTERVAL="vulas.core.monitor.periodicUpload.inter"{trunked}</code> */
  public static final String MONI_PERIODIC_UPL_INTERVAL =
      "vulas.core.monitor.periodicUpload.interval";
  /** Constant <code>MONI_PERIODIC_UPL_BATCH_SIZE="vulas.core.monitor.periodicUpload.batch"{trunked}</code> */
  public static final String MONI_PERIODIC_UPL_BATCH_SIZE =
      "vulas.core.monitor.periodicUpload.batchSize";
  /** Constant <code>MONI_PERIODIC_MAX_ITEMS="vulas.core.monitor.maxItems"</code> */
  public static final String MONI_PERIODIC_MAX_ITEMS = "vulas.core.monitor.maxItems";
  /** Constant <code>MONI_BLACKLIST_JARS="vulas.core.monitor.blacklist.jars"</code> */
  public static final String MONI_BLACKLIST_JARS = "vulas.core.monitor.blacklist.jars";

  private static Integer cachedMaxItems = null;

  private static String vulasRelease = null;

  /**
   * <p>isBackendOffline.</p>
   *
   * @param _c a {@link org.eclipse.steady.shared.util.VulasConfiguration} object.
   * @return a boolean.
   */
  public static boolean isBackendOffline(VulasConfiguration _c) {
    return ConnectType.OFFLINE.equals(getBackendConnectType(_c));
  }
  /**
   * <p>isBackendReadOnly.</p>
   *
   * @param _c a {@link org.eclipse.steady.shared.util.VulasConfiguration} object.
   * @return a boolean.
   */
  public static boolean isBackendReadOnly(VulasConfiguration _c) {
    return ConnectType.READ_ONLY.equals(getBackendConnectType(_c));
  }
  /**
   * <p>isBackendReadWrite.</p>
   *
   * @param _c a {@link org.eclipse.steady.shared.util.VulasConfiguration} object.
   * @return a boolean.
   */
  public static boolean isBackendReadWrite(VulasConfiguration _c) {
    return ConnectType.READ_WRITE.equals(getBackendConnectType(_c));
  }

  private static ConnectType getBackendConnectType(VulasConfiguration _c) {
    final String value = _c.getConfiguration().getString(BACKEND_CONNECT, null);
    if ("READ_WRITE".equalsIgnoreCase(value)) return ConnectType.READ_WRITE;
    else if ("READ_ONLY".equalsIgnoreCase(value)) return ConnectType.READ_ONLY;
    else if ("OFFLINE".equalsIgnoreCase(value)) return ConnectType.OFFLINE;
    else
      throw new IllegalStateException(
          "Illegal value of configuration setting [" + BACKEND_CONNECT + "]: [" + value + "]");
  }

  /**
   * <p>isJarUploadEnabled.</p>
   *
   * @param _vc a {@link org.eclipse.steady.shared.util.VulasConfiguration} object.
   * @return a boolean.
   */
  public static boolean isJarUploadEnabled(VulasConfiguration _vc) {
    return _vc.getConfiguration().getBoolean(APP_LIB_UPLOAD, false);
  }

  /**
   * Reads configuration settings in order to instantiate an {@link Application}.
   *
   * @return an {@link Application}
   * @throws org.apache.commons.configuration.ConfigurationException if the instantiation fails.
   */
  public static Application getAppContext() throws ConfigurationException {
    return getAppContext(VulasConfiguration.getGlobal());
  }

  /**
   * <p>getAppContext.</p>
   *
   * @param _c a {@link org.eclipse.steady.shared.util.VulasConfiguration} object.
   * @return a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @throws org.apache.commons.configuration.ConfigurationException if any.
   */
  public static Application getAppContext(VulasConfiguration _c) throws ConfigurationException {
    final Configuration c = _c.getConfiguration();
    Application a = null;
    try {
      a =
          new Application(
              c.getString(APP_CTX_GROUP), c.getString(APP_CTX_ARTIF), c.getString(APP_CTX_VERSI));
    } catch (IllegalArgumentException e) {
      throw new ConfigurationException("Application incomplete: " + e.getMessage(), e);
    }
    if (!a.isComplete())
      throw new ConfigurationException("Application incomplete: " + a.toString());
    return a;
  }

  /**
   * Reads the global configuration in order to instantiate a {@link GoalContext}.
   *
   * @return a {@link GoalContext} built from the global configuration
   */
  public static final GoalContext buildGoalContextFromGlobalConfiguration() {
    return buildGoalContextFromConfiguration(VulasConfiguration.getGlobal());
  }

  /**
   * Reads the global configuration in order to instantiate a {@link GoalContext}.
   *
   * @return a {@link GoalContext} built from the global configuration
   * @param _c a {@link org.eclipse.steady.shared.util.VulasConfiguration} object.
   */
  public static final GoalContext buildGoalContextFromConfiguration(VulasConfiguration _c) {
    final Configuration c = _c.getConfiguration();

    final GoalContext ctx = new GoalContext();

    ctx.setVulasConfiguration(_c);

    // Tenant
    Tenant tenant = null;
    if (!_c.isEmpty(CoreConfiguration.TENANT_TOKEN)) {
      tenant = new Tenant(c.getString(CoreConfiguration.TENANT_TOKEN));
      ctx.setTenant(tenant);
      //			log.info("Using tenant " + tenant);
    } else {
      log.warn("No tenant configured, hence, the backend's default tenant is used");
    }

    // Space
    if (!_c.isEmpty(CoreConfiguration.SPACE_TOKEN)) {
      final Space space = new Space();
      space.setSpaceToken(c.getString(CoreConfiguration.SPACE_TOKEN));
      ctx.setSpace(space);
      //			log.info("Using space " + space);
    } else {
      log.warn(
          "No space configured, hence, the default space of "
              + (tenant == null ? "the default tenant" : "tenant [" + tenant + "]")
              + " is used");
    }

    // App
    final Application a =
        new Application(
            c.getString(CoreConfiguration.APP_CTX_GROUP),
            c.getString(CoreConfiguration.APP_CTX_ARTIF),
            c.getString(CoreConfiguration.APP_CTX_VERSI));
    if (a.isComplete()) ctx.setApplication(a);
    else log.warn("Incomplete application context: " + a.toString());
    return ctx;
  }

  /**
   * <p>existsInBackend.</p>
   *
   * @param _app a {@link org.eclipse.steady.shared.json.model.Application} object.
   * @return a boolean.
   */
  public static final boolean existsInBackend(Application _app) {
    boolean exists = false;
    try {
      exists =
          BackendConnector.getInstance()
              .isAppExisting(CoreConfiguration.buildGoalContextFromGlobalConfiguration(), _app);
    } catch (BackendConnectionException e) {
      log.error("Error while checking whether " + _app + " exists in backend: " + e.getMessage());
    }
    return exists;
  }

  /**
   * Returns true if a maximum number of items has been configured via {@link CoreConfiguration#MONI_PERIODIC_MAX_ITEMS} and
   * the given number is greater or equal to this maximum.
   *
   * @param _count_collected_items a int.
   * @return a boolean.
   */
  public static synchronized boolean isMaxItemsCollected(int _count_collected_items) {
    if (cachedMaxItems == null)
      cachedMaxItems =
          VulasConfiguration.getGlobal()
              .getConfiguration()
              .getInt(CoreConfiguration.MONI_PERIODIC_MAX_ITEMS, -1);
    return cachedMaxItems != -1 && _count_collected_items >= cachedMaxItems;
  }

  /**
   * Reads the Manifest file entry "Implementation-Version" from the Java archive from which this class was loaded.
   *
   * @return the Vulas release
   */
  public static synchronized String getVulasRelease() {
    if (vulasRelease == null) {

      // Shortcut for determining the Vulas version
      vulasRelease = CoreConfiguration.class.getPackage().getImplementationVersion();

      if (vulasRelease == null || vulasRelease.equals("")) {
        CoreConfiguration.log.warn(
            "Cannot determine Vulas version from manifest entry [Implementation-Version], check"
                + " Vulas JAR");
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
   *
   * @param _id a {@link org.eclipse.steady.shared.json.model.ConstructId} object.
   * @return a {@link org.eclipse.steady.sign.SignatureFactory} object.
   */
  public static SignatureFactory getSignatureFactory(ConstructId _id) {
    final ServiceLoader<SignatureFactory> loader = ServiceLoader.load(SignatureFactory.class);
    SignatureFactory factory = null;
    for (SignatureFactory la : loader) {
      if (la.isSupportedConstructId(_id)) {
        factory = la;
      }
    }
    return factory;
  }
}
