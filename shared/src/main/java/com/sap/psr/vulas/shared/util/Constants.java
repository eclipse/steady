package com.sap.psr.vulas.shared.util;

public interface Constants {

	public static final String HTTP_TENANT_HEADER = "X-Vulas-Tenant";
	
	public static final String HTTP_SPACE_HEADER = "X-Vulas-Space";
	
	// Other headers
	public static final String HTTP_VERSION_HEADER   = "X-Vulas-Version";
	public static final String HTTP_COMPONENT_HEADER = "X-Vulas-Component";
	public enum VulasComponent { client, appfrontend, patcheval, bugfrontend };
	
	// Length restrictions
	public static final int MAX_LENGTH_GROUP    = 128;
	public static final int MAX_LENGTH_ARTIFACT = 128;
	public static final int MAX_LENGTH_VERSION  = 96;
}