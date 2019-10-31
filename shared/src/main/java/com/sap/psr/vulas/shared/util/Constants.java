package com.sap.psr.vulas.shared.util;

/** Constants interface. */
public interface Constants {

  /** Constant <code>HTTP_TENANT_HEADER="X-Vulas-Tenant"</code> */
  public static final String HTTP_TENANT_HEADER = "X-Vulas-Tenant";

  /** Constant <code>HTTP_SPACE_HEADER="X-Vulas-Space"</code> */
  public static final String HTTP_SPACE_HEADER = "X-Vulas-Space";

  // Other headers
  /** Constant <code>HTTP_VERSION_HEADER="X-Vulas-Version"</code> */
  public static final String HTTP_VERSION_HEADER = "X-Vulas-Version";
  /** Constant <code>HTTP_COMPONENT_HEADER="X-Vulas-Component"</code> */
  public static final String HTTP_COMPONENT_HEADER = "X-Vulas-Component";

  public enum VulasComponent {
    client,
    appfrontend,
    patcheval,
    bugfrontend
  };

  // Length restrictions
  /** Constant <code>MAX_LENGTH_GROUP=128</code> */
  public static final int MAX_LENGTH_GROUP = 128;
  /** Constant <code>MAX_LENGTH_ARTIFACT=128</code> */
  public static final int MAX_LENGTH_ARTIFACT = 128;
  /** Constant <code>MAX_LENGTH_VERSION=96</code> */
  public static final int MAX_LENGTH_VERSION = 96;
}
