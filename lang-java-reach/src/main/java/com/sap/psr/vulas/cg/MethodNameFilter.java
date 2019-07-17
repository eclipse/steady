/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.cg;

import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.shared.util.StringList;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Checks whether a given {@link ConstructId} is part of the application, a library and
 * whether it is blacklisted. The method is used when searching for touch points.
 */
public class MethodNameFilter {

	private static Log log = LogFactory.getLog(MethodNameFilter.class);
	
	private static MethodNameFilter instance = null;
	
	private StringList classnameBlacklist  = null;

	/**
	 * Singleton.
	 */
	private MethodNameFilter() {
		final Configuration cfg = VulasConfiguration.getGlobal().getConfiguration();
		this.classnameBlacklist = new StringList();
		this.classnameBlacklist.addAll(cfg.getStringArray(ReachabilityConfiguration.REACH_BL_CLASS_JRE));      
		this.classnameBlacklist.addAll(cfg.getStringArray(ReachabilityConfiguration.REACH_BL_CLASS_CUST));
	}

	/**
	 * If there is a MethodNameFilter instance available it will return it,
	 * otherwise instantiate it and then return
	 *
	 * @return an instantiation of the class MethodNameFilter
	 */
	public synchronized static MethodNameFilter getInstance() {
		if(MethodNameFilter.instance==null)
			MethodNameFilter.instance = new MethodNameFilter();
		return MethodNameFilter.instance;
	}

	/**
	 * Check if the argument is a string contained in the list of the blacklisted
	 * methods
	 *
	 * @param value a string representing the complete name of a method
	 * @return a boolean value stating if value is blacklisted or not
	 */
	public boolean isBlackListed(String value){
		return this.classnameBlacklist.contains(value, StringList.ComparisonMode.STARTSWITH, StringList.CaseSensitivity.CASE_SENSITIVE);
	}

	/**
	 * Check if the argument is a string representing a method of an external library
	 * (meaning not of my application). To define the domain of the application we use
	 * the entries on which we built the graph.
	 *
	 * @param app_entries Entry methods that we used to define the app domain. Use {@link BackendConnector#getApplicationConstructIds(MavenId)} to get them.
	 * @param value a string representing the complete name of a method
	 * @return a boolean value stating if value is part of a library or not
	 */
	public boolean isLibraryMethod(Set<com.sap.psr.vulas.shared.json.model.ConstructId> app_entries, String value){
		return !this.isAnAppMethod(app_entries, value);
	}

	/**
	 * Check if the argument is a string representing a method of my application
	 * (meaning not of an external library). To define the domain of the application we use
	 * the entries on which we built the graph.
	 *
	 * @param app_entries Entry methods that we used to define the app domain. Use {@link BackendConnector#getApplicationConstructIds(MavenId)} to get them.
	 * @param value  a string representing the complete name of a method
	 * @return a boolean value stating if value is part of the application or not
	 */
	public boolean isAnAppMethod(Set<com.sap.psr.vulas.shared.json.model.ConstructId> app_entries, String value){
		// this method take as input the constructs of the app but if the inner class
		// constructor is modified (e.g.  a$1(a)==>a$1() ) we have problems so we
		// can just add a workaround here. 
		// NB: even is the name of the class contains a $ and is not an innerclass
		// the method result is right anyway
		if(value.contains("$")) value = value.substring(0, value.indexOf("$")); // this solve problems with inner classes
		for(com.sap.psr.vulas.shared.json.model.ConstructId entry : app_entries)
			if(entry.getQname().startsWith(value)) return true;
		return false;
	}
}
