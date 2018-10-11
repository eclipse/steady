package com.sap.psr.vulas.backend.requests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Used to repeat HTTP requests for a certain number of times.
 *
 */
public class RequestRepeater {

	private static final Log log = LogFactory.getLog(RequestRepeater.class);

	private long failCount = 0;
	private long max = 50;
	private long waitMilli = 60000;

	public RequestRepeater() {
		this(VulasConfiguration.getGlobal().getConfiguration().getLong(CoreConfiguration.REPEAT_MAX, 50), VulasConfiguration.getGlobal().getConfiguration().getLong(CoreConfiguration.REPEAT_WAIT, 60000));
	}
	
	public RequestRepeater(long _max, long _milli) {
		this.max = _max;
		this.waitMilli = _milli;
	}

	/**
	 * Returns true (and waits {@link RequestRepeater#waitMilli) milliseconds) if the response code of the previous HTTP call was 503
	 * and the number of maximum retries has not been reached, false otherwise.
	 */
	public boolean repeat(boolean _503) {

		// Don't wait and repeat if the HTTP response code was NE 503
		if(!_503)
			return false;
		// Else check whether we reached the max. no. of retries
		else {
			this.failCount++;
			if(this.failCount<this.max) {
				log.info("Retry [" + this.failCount + "/" + this.max + "] in [" + this.waitMilli/1000 + "] seconds");
				try {
					Thread.sleep(this.waitMilli);
				} catch (InterruptedException e) {
					log.error("Interrupted: " + e.getMessage());
				}
				return true;
			}
			else {
				return false;
			}
		}
	}
}
