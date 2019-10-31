package com.sap.psr.vulas.shared.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** MemoryMonitor class. */
public class MemoryMonitor implements Runnable {

  private static final Log log = LogFactory.getLog(MemoryMonitor.class);

  private long memoSleepTimeMs = 2000; // Every  2 seconds
  private long memoPrintTimeMs = 60000; // Every 60 seconds
  private boolean continueMonitoring = true;
  private Runtime runtime = null;

  private long jvmMax = -1;
  private long maxUsed = -1;
  private double avgUsed = -1;

  /** Constructor for MemoryMonitor. */
  public MemoryMonitor() {
    this.runtime = Runtime.getRuntime();
    this.jvmMax = this.runtime.maxMemory();
  }

  /** {@inheritDoc} */
  @Override
  public void run() {
    long mem_total = 0, mem_free = 0, mem_used = 0, sum_used = 0, no_samples = 0, waited = 0;
    try {
      while (this.continueMonitoring) {

        // Sleep defined time
        Thread.sleep(this.memoSleepTimeMs);
        waited += this.memoSleepTimeMs;

        // Monitor mem consumption
        mem_total = this.runtime.totalMemory();
        mem_free = this.runtime.freeMemory();
        mem_used = mem_total - mem_free;

        // Update stats
        no_samples++;
        sum_used += mem_used;
        this.avgUsed = (double) sum_used / (double) no_samples;
        if (mem_used > this.maxUsed) this.maxUsed = mem_used;

        // Print?
        if (waited >= this.memoPrintTimeMs) {
          waited = 0;
          MemoryMonitor.log.info(
              "Memory consumption (used/avg): ["
                  + StringUtil.byteToMBString(mem_used)
                  + "/"
                  + StringUtil.byteToMBString(this.avgUsed)
                  + "], JVM (free/total/max): ["
                  + StringUtil.byteToMBString(mem_free)
                  + "/"
                  + StringUtil.byteToMBString(mem_total)
                  + "/"
                  + StringUtil.byteToMBString(this.jvmMax)
                  + "]");
        }
      }
    } catch (InterruptedException e) {
      MemoryMonitor.log.error("Memory monitor interrupted:" + e.getMessage());
    }
  }

  /** stop. */
  public void stop() {
    this.continueMonitoring = false;
  }
  /**
   * Getter for the field <code>jvmMax</code>.
   *
   * @return a long.
   */
  public long getJvmMax() {
    return this.jvmMax;
  }
  /**
   * Getter for the field <code>maxUsed</code>.
   *
   * @return a long.
   */
  public long getMaxUsed() {
    return this.maxUsed;
  }
  /**
   * Getter for the field <code>avgUsed</code>.
   *
   * @return a double.
   */
  public double getAvgUsed() {
    return this.avgUsed;
  }
}
