package com.sap.psr.vulas.shared.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.connectivity.Service;
import com.sap.psr.vulas.shared.connectivity.ServiceConnectionException;

/**
 * Provides central read/write access to Vulas configuration.
 *
 * Vulas configuration is composed of multiple, individual {@link Configuration} items.
 * When reading a given setting, the list of configuration items is searched until a configuration
 * containing the respective setting is found.
 *
 * The list contains items in the following order:
 *
 * Writable map properties (added empty by default): Settings provided through {@link VulasConfiguration#setProperty(String, Object, String, boolean)}.
 *
 * System properties (added by default): Settings specified with the JVM option -D.
 *
 * Map properties (no default, added through {@link VulasConfiguration#addLayerAfterSysProps(String, Map, String, boolean)}. Results in
 * rebuilding the entire composite configuration.
 *
 * Properties files (added by default): Found in the current work directory (.) and its sub-directories.
 *
 * Properties files contained in JAR files (by default, the JAR from which {@link VulasConfiguration}
 * is loaded is considered): Searched at the very end, these configuration
 * items are useful for providing defaults.
 *
 * @see org.apache.commons.configuration.CompositeConfiguration
 */
public class VulasConfiguration {

	private static Log log = null;
	private static final synchronized Log getLog() {
		if(VulasConfiguration.log==null)
			VulasConfiguration.log = LogFactory.getLog(VulasConfiguration.class);
		return VulasConfiguration.log;
	}

	private static final String[] LOG_PREFIXES = new String[] {"http", "https", "vulas"};

	private static VulasConfiguration global = null;
	/**
	 * <p>Getter for the field <code>global</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.util.VulasConfiguration} object.
	 */
	public static final synchronized VulasConfiguration getGlobal() {
		if(global==null)
			global = new VulasConfiguration();
		return global;
	}

	/**
	 * Used for retrieving actual configuration settings.
	 */
	private org.apache.commons.configuration.CompositeConfiguration cfg = new CompositeConfiguration();
	
	private Path m2 = null;

	//=============== Used for building and updating the composite configuration

	/**
	 * All the configurations used to build the composite configuration.
	 * The single configurations are added by {@link VulasConfiguration#appendInitialConfigurations()}.
	 */
	private Map<Configuration, String> individualConfigurations = new LinkedHashMap<Configuration, String>();

	private Configuration writableConfiguration = new MapConfiguration(new HashMap<String,Object>());
	
	// Add the initial ones right away
	/**
	 * <p>Constructor for VulasConfiguration.</p>
	 */
	public VulasConfiguration() {
		this.appendInitialConfigurations();
	}

	/**
	 * Regex used to discover configurations in the file system and (the root folder of) JAR files.
	 */
	private static final String propertiesRegex = "vulas-.*\\.properties";

	/**
	 * Regex used to discover configurations in the folder BOOT-INF/classes/ of executable Spring JARs.
	 */
	private static final String propertiesRegexSpring = "BOOT-INF/classes/vulas-.*\\.properties";
	
	/** Constant <code>SYS_PROP_CFG_LAYER="System-Properties"</code> */
	public static final String SYS_PROP_CFG_LAYER = "System-Properties";
	
	/** Constant <code>TRANSIENT_CFG_LAYER="Transient-Config-Layer"</code> */
	public static final String TRANSIENT_CFG_LAYER = "Transient-Config-Layer";
	
	/** Constant <code>ENV_CFG_LAYER="Environment-Variables"</code> */
	public static final String ENV_CFG_LAYER = "Environment-Variables";

	/**
	 * Returns the mutable configuration object for read/write access.
	 *
	 * @return a {@link org.apache.commons.configuration.Configuration} object.
	 */
	public org.apache.commons.configuration.Configuration getConfiguration() {
		return cfg;
	}

	/**
	 * Builds the initial list of configurations that can be extended afterwards
	 * using {@link VulasConfiguration#appendConfigurationsFromJar(Class)}.
	 * The initial list only covers system properties and properties files found in
	 * the file system.
	 */
	private void appendInitialConfigurations() {
		// Search for properties in FS
		final Pattern pattern = Pattern.compile(VulasConfiguration.propertiesRegex);
		final FilenamePatternSearch fs = new FilenamePatternSearch(pattern);
		final Set<Path> paths = fs.search(Paths.get("."));

		// Add: Writable map (takes all settings coming through setProperty)
		addConfiguration(writableConfiguration, TRANSIENT_CFG_LAYER);

		// Add: System properties
		addConfiguration(new SystemConfiguration(), SYS_PROP_CFG_LAYER);

		// Add: Properties in file system
		String pathToFileAsString = null;
		for(Path path: paths) {
			try {
				pathToFileAsString = URLDecoder.decode(path.toString(), "UTF-8");
				Configuration config = new PropertiesConfiguration(new File(pathToFileAsString));
				addConfiguration(config, pathToFileAsString);
			} catch (ConfigurationException e) {
				getLog().error("Could not create configuration from file [" + path + "]");
			} catch (UnsupportedEncodingException e) {
				getLog().error("Encoding not supported");
				e.printStackTrace();
			}
		}

		// Add: Environment variables
		final Map<String, String> env = System.getenv();
		Configuration env_config = new MapConfiguration(env);
		addConfiguration(env_config, ENV_CFG_LAYER);

		// Add: Properties in JAR files contained in classpath
		final ClassLoader cl = VulasConfiguration.class.getClassLoader();
		final Set<String> jar_paths = new HashSet<String>();
		
		// Consider JARs known to URLClassLoader
		if(cl instanceof URLClassLoader) {
			jar_paths.addAll(FileUtil.getJarFilePaths((URLClassLoader)cl));
		}
		// Search for JARs containing specific configuration files, e.g., vulas-core.properties
		else {
			jar_paths.addAll(FileUtil.getJarFilePathsForResources(cl, new String[] {"vulas-core.properties", "vulas-java.properties"}));
		}
		
		// Search in all JARs
		final Set<String> jar_paths_analyzed = new HashSet<String>();
		for(String jar_path: jar_paths) {
			if(!jar_paths_analyzed.contains(jar_path)) {
				//getLog().info("Search configuration info in URL [" + urls[i] + "], JAR [" + jar_path + "]");
				appendConfigurationsFromJarPath(jar_path);
				jar_paths_analyzed.add(jar_path);
			}
			else {
				//getLog().info("URL [" + urls[i] + "], JAR [" + jar_path + "] already analyzed for configuration info");
			}
		}

		// Log configuration composition and actual settings
		this.log(LOG_PREFIXES, "    ");
	}
	
	private void addConfiguration(Configuration _cfg, String _source) {
		if(!individualConfigurations.containsValue(_source)) {
			individualConfigurations.put(_cfg, _source);
			cfg.addConfiguration(_cfg);
			VulasConfiguration.getLog().info("Added configuration [" + _cfg.getClass().getSimpleName() + "] from source [" + _source + "]");
		} else {
			VulasConfiguration.getLog().warn("Configuration [" + _cfg.getClass().getSimpleName() + "] from source [" + _source + "] already existed and will not be added another time");
		}
	}

	/**
	 * Puts the given Configuration as a new layer at the given position and with the given name. If a layer with the same name
	 * already exists at the given position, it will be either deleted or shifted by one position according to the boolean argument.
	 * In combination with providing null as new configuration, this boolean flag can be used to remove existing layers.
	 * 
	 * @param _cfg
	 * @param _source
	 * @param _position
	 * @param _replace_if_existing
	 */
	private boolean putConfiguration(Configuration _cfg, String _source, int _position, boolean _replace_if_existing) {
		Map<Configuration, String> tmp = new LinkedHashMap<Configuration, String>();
		boolean removed_existing = false;
		int i=0;
		for(Configuration c: individualConfigurations.keySet()) {

			// Wrong position, just append the current layer
			if(i!=_position) {
				tmp.put(c,  individualConfigurations.get(c));
			}
			// Correct position
			else  {
				// Put new layer (if provided)
				if(_cfg!=null)
					tmp.put(_cfg, _source);

				// Check if current layer at this position is to be removed (replaced)
				final String name = individualConfigurations.get(c);
				if(_replace_if_existing && name.equals(_source)) {
					removed_existing = true;
				}
				else {
					tmp.put(c, name);
				}
			}
			i++;
		}
		individualConfigurations = tmp;
		return removed_existing;
	}

	/**
	 * Rebuilds the composite configuration from the list of individual configurations.
	 * Called after {@link VulasConfiguration#addLayerAfterSysProps(String, Map, String, boolean)}, which adds
	 * a configuration in the middle of the list rather than appending it to the end.
	 * Rebuilding is necessary, since {@link CompositeConfiguration} only appends to the end.
	 */
	private void rebuild() {
		cfg.clear();
		for(Configuration config: individualConfigurations.keySet())
			cfg.addConfiguration(config);
		this.log(LOG_PREFIXES, "    ");
	}

	/**
	 * Adds a {@link MapConfiguration} right after the {@link SystemConfiguration}, which is the second element,
	 * and before all other configurations. As such, contained settings get precedence before file-based configurations
	 * in file system or JAR files.
	 *
	 * @param _map a {@link java.util.Map} object.
	 * @param _ignore_value if specified, elements will only be added if the value's string representation from this argument
	 * @param _ignore_null whether or not null values shall be ignored
	 * @throws java.lang.IllegalArgumentException
	 * @param _layer_name a {@link java.lang.String} object.
	 */
	public void addLayerAfterSysProps(@NotNull String _layer_name, @NotNull Map<?,?> _map, String _ignore_value, boolean _ignore_null) {
		final Map<String,Object> map = new HashMap<String,Object>();
		Configuration config = null;

		// Add value by value to the new layer
		if(_map!=null) {
			for(Object key: _map.keySet()) {
				final Object value = _map.get(key);
				if( (value!=null || !_ignore_null) && (_ignore_value==null || !value.toString().equals(_ignore_value)) ) {
					map.put(key.toString(), _map.get(key));	
				}
			}
			config = new MapConfiguration(map);
		}
		final int no_layers_before = individualConfigurations.size();
		final boolean removed_existing = putConfiguration(config, _layer_name, 2, true);
		final int no_layers_after = individualConfigurations.size();		

		// Log message
		final StringBuffer msg = new StringBuffer();
		if(_map==null) {
			if(removed_existing)
				msg.append("Removed configuration layer [").append(_layer_name).append("] from 3rd position");
			else
				msg.append("No change of configuration layers");
		}
		else {
			if(removed_existing)
				msg.append("Replaced existing configuration layer [").append(_layer_name).append("] by new one with [").append(map.size()).append("] settings on 3rd position");
			else
				msg.append("Added new configuration layer [").append(_layer_name).append("] with [").append(map.size()).append("] settings on 3rd position");
		}
		msg.append(", old/new number of layers is [").append(no_layers_before).append("/").append(no_layers_after).append("]");
		getLog().info(msg.toString());

		if(_map!=null || removed_existing)
			rebuild();
	}
	
	/**
	 * Returns the {@link Configuration} layer with the given name. If multiple layers with that name exist, the top-most layer will be returned.
	 *
	 * @param _layer_name a {@link java.lang.String} object.
	 * @return a {@link org.apache.commons.configuration.Configuration} object.
	 */
	public Configuration getConfigurationLayer(String _layer_name) {
		for(Configuration c: this.individualConfigurations.keySet()) {
			if(this.individualConfigurations.get(c).equals(_layer_name)) {
				return c;
			}
		}
		return null;
	}

	/**
	 * <p>appendConfigurationsFromJarPath.</p>
	 *
	 * @param _jar_path a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	protected boolean appendConfigurationsFromJarPath(String _jar_path) {
		final Map<String, Configuration> jar_entries = this.discoverConfigurationsInJarUri(_jar_path);
		for(Map.Entry<String, Configuration> entry: jar_entries.entrySet()) {
			this.addConfiguration(entry.getValue(), entry.getKey());
		}
		return !jar_entries.isEmpty();
	}

	/**
	 * Identifies configurations in the JAR from which the given class was loaded, and appends them to
	 * the configuration list from which the composite configuration will be built.
	 *
	 * Afterwards, if such configurations were found, the composite configuration is rebuilt
	 * from the updated configuration list.
	 *
	 * Returns true if configurations were found and added.
	 *
	 * @param _clazz a {@link java.lang.Class} object.
	 * @return a boolean.
	 */
	protected boolean appendConfigurationsFromJar(Class<?> _clazz) {
		final Map<String, Configuration> jar_entries = this.discoverConfigurationsInJar(_clazz);
		for(Map.Entry<String, Configuration> entry: jar_entries.entrySet()) {
			addConfiguration(entry.getValue(), entry.getKey());
		}
		return !jar_entries.isEmpty();
	}

	private Map<String, Configuration> discoverConfigurationsInJar(Class<?> _clazz) {
		Map<String, Configuration> jar_configs = new HashMap<String, Configuration>();

		// Get FS path for JAR (if any)
		final String jar_path = FileUtil.getJarFilePath(_clazz);
		if(jar_path==null) {
			getLog().warn("Class [" + _clazz.getName() + "] not loaded from JAR");
		} else {
			jar_configs = discoverConfigurationsInJarUri(jar_path);
		}

		return jar_configs;
	}

	private Map<String, Configuration> discoverConfigurationsInJarUri(String _jar_path) {
		final Map<String, Configuration> jar_configs = new HashMap<String, Configuration>();
		JarFile jf = null;
		try {
			jf = new JarFile(Paths.get(_jar_path).toFile());
			final Enumeration<JarEntry> entries_enum = jf.entries();
			final Pattern pattern = Pattern.compile(VulasConfiguration.propertiesRegex);
			final Pattern pattern_spring = Pattern.compile(VulasConfiguration.propertiesRegexSpring);
			while(entries_enum.hasMoreElements()) {
				final JarEntry entry = entries_enum.nextElement();
				final String full_name = _jar_path + ">" + entry.getName();

				// Evaluates regex(es)
				final Matcher m = pattern.matcher(entry.getName());
				final Matcher m_spring = pattern_spring.matcher(entry.getName());
				if(m.matches() || m_spring.matches()) {
					try {
						final Properties prop = new Properties();
						prop.load(jf.getInputStream(entry));
						jar_configs.put(full_name, ConfigurationConverter.getConfiguration(prop));
					} catch (Exception e) {
						getLog().error("Error loading properties from JAR entry [" + full_name + "]: " + e.getMessage(), e);
					}
				} else if(entry.getName().endsWith(".jar") || entry.getName().endsWith(".war")) {
					final Map<String, Configuration> nested_configs = this.discoverConfigurationsInNestedJar(full_name, new JarInputStream(jf.getInputStream(entry)));
					if(nested_configs!=null && nested_configs.size()>0) {
						jar_configs.putAll(nested_configs);
					}
				}
			}
		} catch (IOException e) {
			getLog().error("Error searching for configurations in JAR [" + _jar_path + "]: " + e.getMessage(), e);
		} finally {
			if(jf!=null) {
				try {
					jf.close();
				} catch (IOException e) {
					getLog().error("Error closing JAR [" + _jar_path + "]: " + e.getMessage(), e);
				}
			}
		}
		return jar_configs;
	}

	private Map<String, Configuration> discoverConfigurationsInNestedJar(String _name, JarInputStream _jis) {
		final Map<String, Configuration> jar_configs = new HashMap<String, Configuration>();
		try {
			final Pattern pattern = Pattern.compile(VulasConfiguration.propertiesRegex);
			final Pattern pattern_spring = Pattern.compile(VulasConfiguration.propertiesRegexSpring);
			JarEntry entry = null;
			while( (entry=_jis.getNextJarEntry())!=null) {
				final String full_name = _name + ">" + entry.getName();

				// Evaluates regex(es)
				final Matcher m = pattern.matcher(entry.getName());
				final Matcher m_spring = pattern_spring.matcher(entry.getName());
				if(m.matches() || m_spring.matches()) {
					try {
						final Properties prop = new Properties();
						prop.load(new ByteArrayInputStream(this.readContent(_jis)));
						jar_configs.put(full_name, ConfigurationConverter.getConfiguration(prop));
					} catch (Exception e) {
						getLog().error("Error loading properties from JAR entry [" + full_name + "]: " + e.getMessage(), e);
					}
				}
				// Process nested JAR
				else if(entry.getName().endsWith(".jar") || entry.getName().endsWith(".war")) {
					final Map<String, Configuration> nested_configs = this.discoverConfigurationsInNestedJar(full_name, new JarInputStream(new ByteArrayInputStream(this.readContent(_jis))));
					if(nested_configs!=null && nested_configs.size()>0) {
						jar_configs.putAll(nested_configs);
					}
				}
			}
		} catch (IOException e) {
			getLog().error("Error searching for configurations in JAR [" + _name + "]: " + e.getMessage(), e);
		} finally {
			if(_jis!=null) {
				try {
					_jis.close();
				} catch (IOException e) {
					getLog().error("Error closing JAR [" + _name + "]: " + e.getMessage(), e);
				}
			}
		}
		return jar_configs;
	}

	/**
	 * Reads the content of the current {@link JarEntry} from the given {@link JarInputStream} into a byte array.
	 * @param _jis
	 * @return
	 * @throws IOException
	 */
	private byte[] readContent(JarInputStream _jis) throws IOException {
		byte[] bytes = new byte[1024];
		while(_jis.read(bytes, 0, 1024)!=-1) {;} //read()
		return bytes;
	}

	//=============== Stuff for accessing single shared configuration settings

	/** Constant <code>MAND_SETTINGS="vulas.shared.settings.mandatory"</code> */
	public final static String MAND_SETTINGS = "vulas.shared.settings.mandatory";

	/** Constant <code>OPTI_SETTINGS="vulas.shared.settings.optional"</code> */
	public final static String OPTI_SETTINGS = "vulas.shared.settings.optional";

	/** Constant <code>HOMEPAGE="vulas.shared.homepage"</code> */
	public final static String HOMEPAGE = "vulas.shared.homepage";
	
	/** Constant <code>CHARSET="vulas.shared.charset"</code> */
	public final static String CHARSET = "vulas.shared.charset";

	/** Constant <code>TMP_DIR="vulas.shared.tmpDir"</code> */
	public final static String TMP_DIR = "vulas.shared.tmpDir";

	/** Constant <code>VULAS_JIRA_USER="vulas.jira.usr"</code> */
	public final static String VULAS_JIRA_USER = "vulas.jira.usr";

	/** Constant <code>VULAS_JIRA_PWD="vulas.jira.pwd"</code> */
	public final static String VULAS_JIRA_PWD = "vulas.jira.pwd";

	/** Constant <code>M2_DIR="vulas.shared.m2Dir"</code> */
	public final static String M2_DIR = "vulas.shared.m2Dir";
	
	/** Constant <code>SYS_PROPS="vulas.shared.sys"</code> */
	public final static String SYS_PROPS = "vulas.shared.sys";
	
	/** Constant <code>SYS_PROPS_CUSTOM="vulas.shared.sys.custom"</code> */
	public final static String SYS_PROPS_CUSTOM = "vulas.shared.sys.custom";

	/** Constant <code>ENV_VARS="vulas.shared.env"</code> */
	public final static String ENV_VARS = "vulas.shared.env";
	
	/** Constant <code>ENV_VARS_CUSTOM="vulas.shared.env.custom"</code> */
	public final static String ENV_VARS_CUSTOM = "vulas.shared.env.custom";
	
	/**
	 * Checks mandatory and optional settings and, where provided, the format.
	 *
	 * @throws org.apache.commons.configuration.ConfigurationException
	 */
	public void checkSettings() throws ConfigurationException {

		// Optional settings
		final String[] optional_settings = this.getStringArray(OPTI_SETTINGS, null);
		if(optional_settings!=null && optional_settings.length>0) {
			for(String s: optional_settings) {
				if(this.isEmpty(s)) {
					log.warn("Optional setting [" + s + "] not specified");
				}
			}
		}

		// Mandatory settings
		final String[] mandatory_settings = this.getStringArray(MAND_SETTINGS, null);
		final Set<String> not_specified = new HashSet<String>();
		if(mandatory_settings!=null && mandatory_settings.length>0) {
			for(String s: mandatory_settings) {
				// Check if empty
				if(this.isEmpty(s)) {
					log.error("Mandatory setting [" + s + "] not specified");
					not_specified.add(s);
				}
			}
		}
		
		// Check format (where provided)
		final Iterator<String> iter = this.cfg.getKeys();
		final Set<String> wrong_format = new HashSet<String>();
		while(iter.hasNext()) {
			final String key = iter.next();
			if(key.startsWith("vulas.") && key.endsWith(".format") && !this.isEmpty(key)) {
				final String key_to_check = key.substring(0, key.indexOf(".format"));
				if(!this.isEmpty(key_to_check)) {
					final String[] values_to_check = this.getStringArray(key_to_check, new String[] {});
					for(String value_to_check: values_to_check) {
						if(!value_to_check.matches(this.cfg.getString(key))) {
							log.error("Setting [" + key_to_check + "], value [" + value_to_check + "] does not comply with the required format [" + this.cfg.getString(key) + "]");
							wrong_format.add(key_to_check);
						}
					}
				}
			}
		}
		
		if(!not_specified.isEmpty() || !wrong_format.isEmpty())
			throw new ConfigurationException("The following mandatory settings are not specified: [" + StringUtil.join(not_specified, ", ") + "], the following settings do not comply with the required format: [" + StringUtil.join(wrong_format, ", ") + "]");
	}

	/**
	 * Deletes all transient (not persisted) configuration settings.
	 * Returns true if the transient configuration layer contained values that were deleted, false otherwise.
	 *
	 * @return a boolean.
	 */
	public boolean clearTransientProperties() {
		final boolean contains_etries = !writableConfiguration.isEmpty();
		if(contains_etries)
			writableConfiguration.clear();
		return contains_etries;
	}

	/**
	 * Returns true if the configuration does not contain the given setting or its value is an empty string.
	 *
	 * @param _key a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean isEmpty(String _key) {
		return !cfg.containsKey(_key) || cfg.getString(_key).equals("");
	}

	/**
	 * Reads the setting with the given key (recursively, if the key's value is the name of another setting).
	 *
	 * @param _key a {@link java.lang.String} object.
	 * @param _default a {@link java.lang.Object} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object getProperty(@NotNull String _key, Object _default) {
		Object obj = cfg.getProperty(_key);
		if(obj==null) {
			obj = _default;
		}
		else if(obj instanceof String) {
			String other_key = (String)obj;
			if(this.cfg.containsKey(other_key)) {
				obj = this.getProperty(other_key, _default);
			}
		}
		return obj;
	}

	/**
	 * Sets the given setting to the specified value in case it is empty. Note that this setting is transient (not persisted).
	 *
	 * @param _key a {@link java.lang.String} object.
	 * @param _value a {@link java.lang.Object} object.
	 */
	public void setPropertyIfEmpty(@NotNull String _key, Object _value) {
		if(isEmpty(_key))
			setProperty(_key, _value, null, false);
	}

	/**
	 * Sets the given setting to the specified value. Note that this setting is transient (not persisted).
	 *
	 * @param _key a {@link java.lang.String} object.
	 * @param _value a {@link java.lang.Object} object.
	 */
	public void setProperty(@NotNull String _key, Object _value) {
		setProperty(_key, _value, null, false);
	}

	/**
	 * <p>setProperty.</p>
	 *
	 * @param _key a {@link java.lang.String} object.
	 * @param _value a {@link java.lang.Object} object.
	 * @param _ignore_value a {@link java.lang.String} object.
	 * @param _ignore_null a boolean.
	 */
	public void setProperty(@NotNull String _key, Object _value, String _ignore_value, boolean _ignore_null) {
		if( (_value!=null || !_ignore_null) && (_ignore_value==null || !_value.toString().equals(_ignore_value)) ) {

			final Object before = cfg.getProperty(_key);
			if(_value==null)
				writableConfiguration.clearProperty(_key);
			else
				writableConfiguration.setProperty(_key, _value);

			final Object after = cfg.getProperty(_key);

			// Log everything (to be deleted)
			//getLog().info("Setting [" + _key + "] value [before=" + before + ", arg=" + _value + ", after=" + after + "]");

			// If the _value contains a comma, the new object will be a String array or ArrayList<String>
			final ArrayList<String> array_list = new ArrayList<String>();

			// Check that setting worked
			if(after!=null && (after.getClass().isArray() || after.getClass().equals(array_list.getClass()))) {}
			else if( (_value==null && after!=null) || (_value!=null && !_value.equals(after)) ) {
				getLog().error("New value [" + _value + "] for setting [" + _key + "] not set: Before [" + before + "], after [" + after + "]");
			}
		}
	}

	/**
	 * Improves the method {@link Configuration#getStringArray(String)} by adding a default value, which is returned
	 * if the String array returned for the respective setting is either null or contains a single empty {@link String}.
	 *
	 * @param _key a {@link java.lang.String} object.
	 * @param _default an array of {@link java.lang.String} objects.
	 * @return an array of {@link java.lang.String} objects.
	 */
	public String[] getStringArray(@NotNull String _key, String[] _default) {
		String[] value = this.getConfiguration().getStringArray(_key);
		if(value!=null && value.length>0 && !(value.length==1 && value[0].equals("")))
			return value;
		else
			return _default;
	}

	/**
	 * Returns the configuration setting for the given key as {@link Path}. If no such setting exists, the tmp directory
	 * will be returned.
	 *
	 * @param _key a {@link java.lang.String} object.
	 * @return a {@link java.nio.file.Path} object.
	 */
	public Path getDir(String _key) {
		Path p = null;
		if(!this.isEmpty(_key))
			p = Paths.get(this.getConfiguration().getString(_key));
		else
			p = this.getTmpDir();

		// Create if necessary
		FileUtil.createDirectory(p);

		return p;
	}

	/**
	 * Creates if necessary and returns the temporary directory to be used by Vulas. This is either the directory
	 * indicated by the configuration setting TMP_DIR (if any) or the OS' temporary directory.
	 *
	 * @return a {@link java.nio.file.Path} object.
	 */
	public Path getTmpDir() {
		Path p = null;
		if(!this.isEmpty(TMP_DIR))
			p = Paths.get(cfg.getString(TMP_DIR));
		else
			p = Paths.get(System.getProperty("java.io.tmpdir"));

		// Create if necessary
		FileUtil.createDirectory(p);

		return p;
	}

	/**
	 * <p>getServiceUrl.</p>
	 *
	 * @param _service a {@link com.sap.psr.vulas.shared.connectivity.Service} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getServiceUrl(Service _service) {
		String value = null;
		try {
			value = getServiceUrl(_service, false);
		} catch (ServiceConnectionException e) {
			getLog().warn(e.getMessage(), e);
		}
		return value;
	}

	/**
	 * <p>getServiceUrl.</p>
	 *
	 * @param _service a {@link com.sap.psr.vulas.shared.connectivity.Service} object.
	 * @param _throw_exception a boolean.
	 * @return a {@link java.lang.String} object.
	 * @throws com.sap.psr.vulas.shared.connectivity.ServiceConnectionException if any.
	 */
	public String getServiceUrl(Service _service, boolean _throw_exception) throws ServiceConnectionException {
		final String key = VulasConfiguration.getServiceUrlKey(_service);
		final String value = cfg.getString(key, null);
		if(_throw_exception && value==null)
			throw new ServiceConnectionException("Service URL is not configured (parameter [" + key + "])", null);
		return value;
	}

	/**
	 * <p>hasServiceUrl.</p>
	 *
	 * @param _service a {@link com.sap.psr.vulas.shared.connectivity.Service} object.
	 * @return a boolean.
	 */
	public boolean hasServiceUrl(Service _service) {
		final String key = VulasConfiguration.getServiceUrlKey(_service);
		return !this.isEmpty(key);
	}

	/**
	 * <p>setServiceUrl.</p>
	 *
	 * @param _service a {@link com.sap.psr.vulas.shared.connectivity.Service} object.
	 * @param _value a {@link java.lang.String} object.
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public void setServiceUrl(Service _service, String _value) throws IllegalArgumentException {
		final String key = VulasConfiguration.getServiceUrlKey(_service);
		URI uri;
		try {
			uri = new URI(_value);
			getConfiguration().setProperty(key, uri.toString());
			getLog().info("Set [" + key + "] to URL [" + _value + "]");
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid URL [" + _value + "], cannot set [" + key + "]");
		}
	}

	/**
	 * <p>getServiceUrlKey.</p>
	 *
	 * @param _service a {@link com.sap.psr.vulas.shared.connectivity.Service} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getServiceUrlKey(Service _service) {
		return "vulas.shared." + _service.toString().toLowerCase() + ".serviceUrl";
	}

	/**
	 * Returns the path to the m2 repository.
	 *
	 * @throws java.lang.IllegalStateException
	 * @return a {@link java.nio.file.Path} object.
	 */
	public Path getLocalM2Repository() throws IllegalStateException {
		if(this.m2==null) {
			String m2_path = null;

			// Explicitly specified
			if(!this.isEmpty(M2_DIR)) {
				m2_path = cfg.getString(M2_DIR);
				this.m2 = Paths.get(m2_path);
			}
			// Use other dir
			else {
				if(System.getenv("HOME")!=null) {
					m2_path = System.getenv("HOME");
					this.m2 = Paths.get(m2_path, ".m2", "repository");
				}
				else {
					m2_path = getTmpDir().toString();
					this.m2 = Paths.get(m2_path, "vulas-m2", "repository");
				}
			}

			try {
				// Create if not existing
				if(!this.m2.toFile().exists())
					Files.createDirectories(m2);

				// Is writable?
				if(!this.m2.toFile().canWrite())
					throw new IllegalStateException("No write permission");
			} catch (Exception e) {
				getLog().info("Error configuring the m2 directory [" + m2_path + "], artifacts will not be cached: " + e.getMessage());
				this.m2 = null;
				throw new IllegalStateException("Error configuring the m2 directory [" + m2_path + "], artifacts will not be cached: " + e.getMessage(), e);
			}
		}
		return this.m2;
	}

	/**
	 * Prints settings having the given prefixes to the log.
	 *
	 * @param _prefix an array of {@link java.lang.String} objects.
	 * @param _indent a {@link java.lang.String} object.
	 */
	public void log(String[] _prefix, String _indent) {
		// Print all configurations considered
		int count = 0;
		for(Map.Entry<Configuration, String> entry: individualConfigurations.entrySet()) {
			int count_entries=0;
			final Iterator<String> iter = entry.getKey().getKeys();
			while(iter.hasNext()) { count_entries++; iter.next(); }
			VulasConfiguration.getLog().info("Configuration [" + ++count + "]: " + entry.getValue() + ", [" + count_entries + "] entries");
		}		

		// Print actual values that result from that composition
		final StringBuilder builder = new StringBuilder();
		builder.append("Configuration with prefix(es) [");
		for(int i=0; i<_prefix.length; i++) {
			if(i>0) builder.append(", ");
			builder.append(_prefix[i]);
		}
		builder.append("]");
		getLog().info(builder.toString());

		for(int i=0; i<_prefix.length; i++) {
			final Configuration config = cfg.subset(_prefix[i]);

			// Sort all the keys
			final SortedSet<String> keys = new TreeSet<String>();
			final Iterator<String> iter = config.getKeys();
			while(iter.hasNext()) keys.add(iter.next());

			// Print keys and values
			for(String key: keys)
				if(!isEmpty(_prefix[i] + "." + key))
					getLog().info((_indent==null?"":_indent) + _prefix[i] + "." + key + "=" + config.getProperty(key).toString());
		}
	}
	
	/**
	 * Returns a {@link StringList} containing items taken from the given configuration settings. Each configuration settings is
	 * expected to contain one or more values (comma-separated), which are trimmed and added to the {@link StringList}.
	 *
	 * @param _config_names a {@link java.lang.String} object.
	 * @return a {@link com.sap.psr.vulas.shared.util.StringList} object.
	 */
	public final StringList getStringList(String... _config_names) {
		final StringList l = new StringList();
		if(_config_names!=null && _config_names.length>0) {
			for(String config_name: _config_names) {
				l.addAll(this.getStringArray(config_name, new String[] {}), true);
			}
		}
		return l;
	}
}
