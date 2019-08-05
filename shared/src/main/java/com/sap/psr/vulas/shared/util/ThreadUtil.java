package com.sap.psr.vulas.shared.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>ThreadUtil class.</p>
 *
 */
public class ThreadUtil {
	
	private static final Log log = LogFactory.getLog(ThreadUtil.class);

	/** Constant <code>NO_OF_THREADS="vulas.core.noThreads"</code> */
	public final static String NO_OF_THREADS = "vulas.core.noThreads";

	/**
	 * Returns true if the value of configuration setting {@link NO_OF_THREADS} is equal to AUTO, false otherwise.
	 * @return
	 */
	private static boolean isAutoThreading(VulasConfiguration _cfg) {
		return "AUTO".equalsIgnoreCase(_cfg.getConfiguration().getString(NO_OF_THREADS, null));
	}
	
	/**
	 * Returns the number of threads to be used for parallelized processing steps, thereby taking the configuration setting
	 * {@link NO_OF_THREADS} and the number of cores into account.
	 *
	 * @param _multiply_if_auto a int.
	 * @return a int.
	 */
	public static final int getNoThreads(final int _multiply_if_auto) {
		return getNoThreads(VulasConfiguration.getGlobal(), _multiply_if_auto);
	}
	
	/**
	 * Returns the number of threads to be used for parallelized processing steps, thereby taking the configuration setting
	 * {@link NO_OF_THREADS} and the number of cores into account.
	 *
	 * @param _cfg a {@link com.sap.psr.vulas.shared.util.VulasConfiguration} object.
	 * @param _multiply_if_auto a int.
	 * @return a int.
	 */
	public static final int getNoThreads(final VulasConfiguration _cfg, final int _multiply_if_auto) {
		int number = 1;
		if(isAutoThreading(_cfg)) {
			number = Runtime.getRuntime().availableProcessors() * _multiply_if_auto;
			log.info("Auto-threading enabled: Number of threads is [" + _multiply_if_auto + " x " + Runtime.getRuntime().availableProcessors() + " cores]");
		}
		else {
			final String value = _cfg.getConfiguration().getString(NO_OF_THREADS, "1");
			try {
				number = Integer.parseInt(value);
				log.info("Auto-threading disabled: Number of threads is [" + number + "]");
			} catch(NumberFormatException nfe) {
				number = 1;
				log.error("Auto-threading disabled: Configuration setting [" + NO_OF_THREADS + "] must be AUTO or integer, but is [" + value + "], defaulting to [1]");
			}
		}
		return number;
	}

	/**
	 * <p>getNoThreads.</p>
	 *
	 * @return a int.
	 */
	public static int getNoThreads() {
		return getNoThreads(1);
	}
}
