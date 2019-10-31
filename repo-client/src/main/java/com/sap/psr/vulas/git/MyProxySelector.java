package com.sap.psr.vulas.git;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/** MyProxySelector class. */
public class MyProxySelector extends ProxySelector {

  private final Proxy newProxy;

  /**
   * Constructor for MyProxySelector.
   *
   * @param newProxy a {@link java.net.Proxy} object.
   */
  public MyProxySelector(Proxy newProxy) {
    this.newProxy = newProxy;
  }

  /** {@inheritDoc} */
  @Override
  public List<Proxy> select(URI uri) {
    return Arrays.asList(this.newProxy);
  }

  /** {@inheritDoc} */
  @Override
  public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
    if (uri == null || sa == null || ioe == null) {
      throw new IllegalArgumentException("Arguments can not be null.");
    }
  }
}
