package com.sap.psr.vulas.cia.util;

import java.nio.file.Path;
import java.util.Set;

import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.Artifact;


/**
 * <p>RepositoryWrapper interface.</p>
 *
 */
public interface RepositoryWrapper {
	
	/**
	 * Returns all programming languages supported by the respective package repo.
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<ProgrammingLanguage> getSupportedLanguages();
	
	/**
	 * Returns true if the urls are configured (not null) in the class constructor
	 *
	 * @return a boolean.
	 */
	public boolean isConfigured();
	
	/**
	 * Returns all versions for the given group and artifact. Optionally only versions with either a certain
	 * classifier and/or a certain packaging can be obtained.
	 *
	 * @param group a {@link java.lang.String} object.
	 * @param artifact a {@link java.lang.String} object.
	 * @param classifier (optional) to filter on all versions having a certain classifier
	 * @param packaging (optional) to filter on all versions having a certain packaging
	 * @return all versions existing in the target repository.
	 * @throws java.lang.Exception if any.
	 */
	public Set<Artifact> getAllArtifactVersions(String group, String artifact, String classifier,  String packaging)  throws Exception ;
	
	/**
	 * Returns all versions greater than the one provided for the given group and artifact. If available the timestamp is used for comparing versions, otherwise an alphanumerical
	 * comparison is used. Optionally only versions with either a certain
	 * classifier and/or a certain packaging can be obtained.
	 *
	 * @param group a {@link java.lang.String} object.
	 * @param artifact a {@link java.lang.String} object.
	 * @param greaterThanVersion a {@link java.lang.String} object.
	 * @param classifier (optional) to filter on all versions having a certain classifier
	 * @param packaging (optional) to filter on all versions having a certain packaging
	 * @return all versions greater than the one provided in argument greaterThanVersion
	 * @throws java.lang.Exception
	 */
	public Set<Artifact> getGreaterArtifactVersions(String group, String artifact, String greaterThanVersion, String classifier,  String packaging) throws Exception;
	
	/**
	 * Returns the latest version for the given group and artifact. Optionally only versions with either a certain
	 * classifier and/or a certain packaging can be obtained.
	 *
	 * @param group a {@link java.lang.String} object.
	 * @param artifact a {@link java.lang.String} object.
	 * @param classifier (optional) to filter on all versions having a certain classifier
	 * @param packaging (optional) to filter on all versions having a certain packaging
	 * @return the latest version
	 * @throws java.lang.Exception if any.
	 */
	public Artifact getLatestArtifactVersion(String group, String artifact, String classifier,  String packaging)  throws Exception ;
	
	/**
	 * Returns the artifact for the given group, artifact and version. Optionally only versions with either a certain
	 * classifier and/or a certain packaging can be obtained.
	 *
	 * @param group a {@link java.lang.String} object.
	 * @param artifact a {@link java.lang.String} object.
	 * @param version a {@link java.lang.String} object.
	 * @param classifier a {@link java.lang.String} object.
	 * @param packaging a {@link java.lang.String} object.
	 * @param lang a {@link com.sap.psr.vulas.shared.enums.ProgrammingLanguage} object.
	 * @return a {@link com.sap.psr.vulas.shared.json.model.Artifact} object.
	 * @throws java.lang.Exception if any.
	 */
	public Artifact getArtifactVersion(String group, String artifact, String version, String classifier,  String packaging, ProgrammingLanguage lang)  throws Exception ;
	
	
	/**
	 * Downloads the artifact a and returns the path where it stores it (null otherwise).
	 *
	 * @param a a {@link com.sap.psr.vulas.shared.json.model.Artifact} object.
	 * @throws java.lang.Exception
	 * @return a {@link java.nio.file.Path} object.
	 */
	public Path downloadArtifact(Artifact a) throws Exception;
	
	/**
	 * Returns the artifact having the digest d
	 *
	 * @param digest a {@link java.lang.String} object.
	 * @throws com.sap.psr.vulas.cia.util.RepoException
	 * @throws java.lang.InterruptedException
	 * @return a {@link com.sap.psr.vulas.shared.json.model.Artifact} object.
	 */
	public Artifact getArtifactForDigest(String digest) throws RepoException, InterruptedException ;

}
