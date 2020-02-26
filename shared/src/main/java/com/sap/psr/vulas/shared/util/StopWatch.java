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
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.shared.util;

import java.util.LinkedList;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Notes:
 * System.nanoTime is well suited to measure elapsed time, cf. http://stackoverflow.com/questions/510462/is-system-nanotime-completely-useless#4588605
 * It is not well suited to get the current actual time. For that, we use System.runtimeMillis.
 */
public class StopWatch {

	private static Log log = LogFactory.getLog(StopWatch.class);

	private String id;

	private long startMillis = -1;
	private long start = -1;
	private long stop = -1;
	
	private LinkedList<Long> lapTimes = new LinkedList<Long>();
	private LinkedList<Long> lapsTakenAt = new LinkedList<Long>();
	
	private ProgressTracker progressTracker = null;

	private String activityDescription = null;

	private long LAP_TIME_LOG_THRESHOLD = 5L * 1000L * 1000L *1000L; // 5 sec
	
	private double COMPLETION_RATE_LOG_THRESHOLD = 5; // logs every 5%
	
	private static final String FAILED = "Failed";

	/**
	 * <p>Constructor for StopWatch.</p>
	 *
	 * @param _descr a {@link java.lang.String} object.
	 */
	public StopWatch(String _descr) {
		this(_descr, Double.MAX_VALUE, false);
	}

	/**
	 * Stop watch with a description and a number indicating the total of something to be achieved.
	 * The achievements or progress can be reflected with methods {@link StopWatch#progress()} and {@link StopWatch#progress(double, boolean)}.
	 * 
	 * @param _descr
	 * @param _total
	 * @param _start
	 */
	private StopWatch(String _descr, double _total, boolean _start) {
		if(_total<=0)
			throw new IllegalArgumentException("Total must be greater than 0");
		this.id = StringUtil.getRandonString(6);
		this.activityDescription = _descr;
		this.progressTracker = new ProgressTracker(_total);
		if(_start) this.start();
	}
	
	/**
	 * <p>setTotal.</p>
	 *
	 * @param _total a double.
	 * @return a {@link com.sap.psr.vulas.shared.util.StopWatch} object.
	 */
	public StopWatch setTotal(double _total) {
		this.progressTracker = new ProgressTracker(_total);
		return this;
	}

	/**
	 * Returns this stop watch in order to behave similar to a builder:
	 * StopWatch sw = new StopWatch("foo").start();
	 *
	 * @return a {@link com.sap.psr.vulas.shared.util.StopWatch} object.
	 */
	public StopWatch start() {
		if(!this.isStarted()) {
			this.startMillis = System.currentTimeMillis();
			this.start = System.nanoTime();
			this.log("Started", null, this.activityDescription);
		}
		return this;
	}
	
	/**
	 * <p>progress.</p>
	 *
	 * @return a long.
	 */
	public long progress() {
		return this.progress(1, false);
	}

	/**
	 * <p>progress.</p>
	 *
	 * @param _by a double.
	 * @param _force_log a boolean.
	 * @return a long.
	 */
	public long progress(double _by, boolean _force_log) {
		// Completion
		final long prev_compl = this.progressTracker.getCompletionAsLong();
		this.progressTracker.increase(_by);
		final long new_compl = this.progressTracker.getCompletionAsLong();
		
		// Compute remaining time
		final long r = this.getRuntime();
		final double remaining = (( r * this.progressTracker.getTotal()) / this.progressTracker.getCurrent()) -r; // remaining time in nano
		final long remaining_long = (long)remaining;

		// Only log if forced or another COMPLETION_RATE_LOG_THRESHOLD % have been completed (5 %)
		final boolean force_log = _force_log || ( (prev_compl % COMPLETION_RATE_LOG_THRESHOLD) > (new_compl % COMPLETION_RATE_LOG_THRESHOLD) );
		
		return this.lap(this.progressTracker.toString() + ", " + StringUtil.nanoToFlexDurationString(remaining_long) + " until completion", force_log);
	}

	/**
	 * <p>lap.</p>
	 *
	 * @param _message a {@link java.lang.String} object.
	 * @return a long.
	 */
	public long lap(String _message) {
		return this.lap(_message, false);
	}
	
	/**
	 * <p>getMaxLapTime.</p>
	 *
	 * @return a long.
	 */
	public long getMaxLapTime() {
		long max = 0;
		// Note that the call of stop does not close the final lap
		for(Long l: this.lapTimes)
			if(l > max)
				max = l;
		return max;
	}
	
	/**
	 * <p>getAvgLapTime.</p>
	 *
	 * @return a long.
	 */
	public long getAvgLapTime() {
		long total_laps = 0;
		// Note that the call of stop does not close the final lap
		for(Long l: this.lapTimes)
			total_laps += l;
		return Math.round( (double)total_laps / (double)this.lapTimes.size() );
	}

	/**
	 * <p>lap.</p>
	 *
	 * @param _message a {@link java.lang.String} object.
	 * @param _force_log a boolean.
	 * @return a long.
	 */
	public long lap(String _message, boolean _force_log) {
		final long lap_taken_at = System.nanoTime();
		final long lap_time = lap_taken_at - (this.lapsTakenAt.isEmpty() ? this.start : this.lapsTakenAt.getLast());
		this.lapsTakenAt.add(lap_taken_at);
		this.lapTimes.add(lap_time);
		
		// Only log if forced or more than LAP_TIME_LOG_THRESHOLD time passed (5 sec)
		if(_force_log || lap_time > LAP_TIME_LOG_THRESHOLD)
			this.log("Progress after", lap_time, _message);
		
		return lap_time;
	}

	/**
	 * <p>stop.</p>
	 */
	public void stop() {
		if(this.isRunning()) {
			this.stop = System.nanoTime();
			this.log("Completed", null, this.activityDescription);
		}
	}

	/**
	 * <p>stop.</p>
	 *
	 * @param _e a {@link java.lang.Exception} object.
	 */
	public void stop(Exception _e) {
		if(this.isRunning()) {
			this.stop = System.nanoTime();
			this.log(FAILED, null, _e.getMessage());
		}
	}

	private void log(String _msg1, Long _lap_time, @NotNull String _msg2) {
		final StringBuilder b = new StringBuilder();
		b.append("[").append(this.id).append("] ");

		b.append(StringUtil.nanoToFlexDurationString(this.getRuntime())).append(" "); // total time		
		
		// Message 1 and lap time
		if(_msg1!=null)
			b.append(_msg1);
		if(_lap_time!=null)
			b.append(" ").append(StringUtil.nanoToFlexDurationString(_lap_time)); // lap time
		b.append(": ");

		// Message 2
		b.append(_msg2);

		if(FAILED.equals(_msg1))
			log.error(b.toString());
		else
			log.info(b.toString());
	}

	/**
	 * Returns the start time (in milliseconds).
	 *
	 * @return a long.
	 */
	public long getStartMillis() {
		return this.startMillis;
	}

	/**
	 * Returns the runtime (in nanoseconds).
	 *
	 * @return a long.
	 */
	public long getRuntime() {
		if(!this.isRunning())
			return this.stop - this.start;
		else
			return System.nanoTime() - this.start;
	}

	/**
	 * Returns the runtime (in milliseconds).
	 *
	 * @return a long.
	 */
	public long getRuntimeMillis() {
		final long nano = this.getRuntime();
		return nano / 1000000L;		
	}

	/**
	 * Returns false if one of the stop methods have been called previously, true otherwise.
	 *
	 * @return a boolean.
	 */
	public boolean isStarted() {
		return this.start!=-1;
	}
	
	/**
	 * Returns false if one of the stop methods have been called previously, true otherwise.
	 *
	 * @return a boolean.
	 */
	public boolean isRunning() {
		return this.stop==-1;
	}
}
