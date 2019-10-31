package com.sap.psr.vulas.monitor;

/** LoaderFilter interface. */
public interface LoaderFilter {

  /**
   * accept.
   *
   * @param _loader a {@link com.sap.psr.vulas.monitor.Loader} object.
   * @return a boolean.
   */
  public boolean accept(Loader _loader);
}
