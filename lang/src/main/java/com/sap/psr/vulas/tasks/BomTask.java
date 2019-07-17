package com.sap.psr.vulas.tasks;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.json.model.Library;

/**
 * Methods required to create a method-level BOM (Bill of Material) of an {@link Application} developed in a given
 * {@link ProgrammingLanguage}.
 */
public interface BomTask extends Task {
	
	/**
	 * Returns the {@link Application} including (a) all its {@link Construct}s of the respective {@link ProgrammingLanguage},
	 * and (b) the {@link Dependency}s of that application. The {@link Library} of each {@link Dependency} must contain
	 * all details such as its {@link Construct}s and properties.
	 *
	 * @return a {@link com.sap.psr.vulas.shared.json.model.Application} object.
	 */
	public Application getCompletedApplication();
}
