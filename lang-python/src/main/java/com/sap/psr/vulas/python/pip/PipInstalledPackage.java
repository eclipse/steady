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
package com.sap.psr.vulas.python.pip;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;


import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.DirAnalyzer;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.FileAnalyzerFactory;
import com.sap.psr.vulas.python.PythonArchiveAnalyzer;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.enums.PropertySource;
import com.sap.psr.vulas.shared.json.model.Library;
import com.sap.psr.vulas.shared.json.model.LibraryId;
import com.sap.psr.vulas.shared.json.model.Property;
import com.sap.psr.vulas.shared.util.DirUtil;
import com.sap.psr.vulas.shared.util.DirnamePatternSearch;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.FilenamePatternSearch;
import com.sap.psr.vulas.shared.util.StringList;

/**
 * <p>PipInstalledPackage class.</p>
 *
 */
public class PipInstalledPackage implements Comparable {

	private final static Logger log = org.apache.logging.log4j.LogManager.getLogger(PipInstalledPackage.class);

	private static final String LOCATION = "Location";
	private static final String REQUIRES = "Requires";
	
	private String name = null;
	private String version = null;
	private Map<String, String> properties = new HashMap<String,String>();
	private String digest = null;
	private Map<ConstructId, Construct> constructs = null;

	private String downloadUrl = null;
	private Path downloadPath = null;

	private Path eggFile = null;
	private Path pyFile  = null;
	private Path pyDir   = null;
	
	private FileAnalyzer fileAnalyzer = null;

	/**
	 * <p>Constructor for PipInstalledPackage.</p>
	 *
	 * @param _name a {@link java.lang.String} object.
	 * @param _version a {@link java.lang.String} object.
	 */
	public PipInstalledPackage(String _name, String _version) {
		this.name = _name;
		this.version = _version;
	}

	/**
	 * <p>Getter for the field <code>properties</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, String> getProperties() { return properties; }

	/**
	 * Returns the installation path of the Python package.
	 *
	 * @return a {@link java.nio.file.Path} object.
	 */
	public Path getInstallPath() {
		return Paths.get(this.properties.get(LOCATION));
	}

	/**
	 * <p>Getter for the field <code>downloadUrl</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDownloadUrl() {
		return downloadUrl;
	}

	/**
	 * <p>Setter for the field <code>downloadUrl</code>.</p>
	 *
	 * @param downloadUrl a {@link java.lang.String} object.
	 */
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	/**
	 * Returns the download path of the Python package. This path is extracted from the output of "pip install".
	 *
	 * @return a {@link java.nio.file.Path} object.
	 */
	public Path getDownloadPath() {
		return downloadPath;
	}

	/**
	 * <p>Setter for the field <code>downloadPath</code>.</p>
	 *
	 * @param downloadPath a {@link java.nio.file.Path} object.
	 */
	public void setDownloadPath(Path downloadPath) {
		this.downloadPath = downloadPath;
	}

	/**
	 * Returns a {@link Library} representing the analyzed archive.
	 *
	 * @throws com.sap.psr.vulas.FileAnalysisException
	 * @return a {@link com.sap.psr.vulas.shared.json.model.Library} object.
	 */
	public Library getLibrary() throws FileAnalysisException {
		Library lib = null;
		
		if(this.fileAnalyzer!=null && this.fileAnalyzer instanceof PythonArchiveAnalyzer) {
			lib = ((PythonArchiveAnalyzer)this.fileAnalyzer).getLibrary();
			lib.setLibraryId(new LibraryId(this.getName(), this.getName(), this.getVersion()));
			
		} else {
			lib = new Library();
			lib.setDigest(this.getDigest());
			lib.setDigestAlgorithm(DigestAlgorithm.MD5);
			lib.setLibraryId(new LibraryId(this.getName(), this.getName(), this.getVersion()));
			if(this.getConstructs()!=null)
				lib.setConstructs(ConstructId.getSharedType(this.getConstructs().keySet()));
		}	
		
		final Set<Property> p = new HashSet<Property>();
		for(String key: this.getProperties().keySet()) {
			p.add(new Property(PropertySource.PIP, key, this.getProperties().get(key)));
		}
		lib.setProperties(p);
		
		return lib;
	}

	/**
	 * Returns true if this package requieres the given package, as indicated by the 'Requires' property, false otherwise.
	 *
	 * @param _pack a {@link com.sap.psr.vulas.python.pip.PipInstalledPackage} object.
	 * @return a boolean.
	 * @throws java.lang.IllegalStateException if any.
	 */
	public boolean requires(PipInstalledPackage _pack) throws IllegalStateException {
		if(this.properties==null || !this.properties.containsKey(REQUIRES))
			throw new IllegalStateException("Property [" + REQUIRES + "] not known");

		final String[] packs = this.properties.get(REQUIRES).split(",");
		for(int i=0; i<packs.length; i++)
			if(_pack.getName().equalsIgnoreCase(packs[i].trim()))
				return true;
		return false;
	}

	/**
	 * Sets the properties as provided by pip.
	 *
	 * @param properties a {@link java.util.Map} object.
	 */
	public void addProperties(Map<String, String> properties) {
		this.properties.putAll(properties);
	}

	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() { return name; }
	/**
	 * <p>Getter for the field <code>version</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getVersion() { return version; }

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuffer b = new StringBuffer();
		b.append("[").append(this.getName()).append(":").append(this.getVersion()).append("]");
		return b.toString();
	}

	/**
	 * <p>Getter for the field <code>digest</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDigest() {
		if(this.downloadPath==null && (this.properties==null || !this.properties.containsKey(LOCATION)))
			throw new IllegalStateException(this + " does not have local download path nor property [" + LOCATION + "]");

		if(this.digest==null) {

			// Take the downloaded file
			if(this.downloadPath!=null) {
				this.digest = FileUtil.getDigest(this.downloadPath.toFile(), DigestAlgorithm.MD5);
				log.info("Computed MD5 [" + this.digest + "] from downloaded file [" + this.downloadPath + "]");
			}
			// Search in site-packages (location property)
			else {
				this.eggFile = this.searchEgg();
				this.pyFile = this.searchPyFile();
				this.pyDir = this.searchPyDir();

				if(this.eggFile!=null) {
					this.digest = FileUtil.getDigest(this.eggFile.toFile(), DigestAlgorithm.MD5);
					log.info("Computed MD5 [" + this.digest + "] from egg file [" + this.eggFile + "]");
				} else if(this.pyFile!=null) {
					this.digest = FileUtil.getDigest(this.pyFile.toFile(), DigestAlgorithm.MD5);
					log.info("Computed MD5 [" + this.digest + "] from file [" + this.pyFile + "]");
				} else if(this.pyDir!=null) {
					this.digest = DirUtil.getDigest(this.pyDir.toFile(), new String[] {".pyc"}, DigestAlgorithm.MD5);
					log.info("Computed MD5 [" + this.digest + "] from directory [" + this.pyDir + "]");
				} else {
					log.error("Cannot compute MD5 of " + this);
				}
			}
		}
		return this.digest;
	}

	/**
	 * 
	 * @return
	 * @throws FileAnalysisException
	 */
	private Map<ConstructId, Construct> getConstructs() throws FileAnalysisException {
		if(this.fileAnalyzer==null) {
			// Get from archive
			if(this.downloadPath!=null) {
				this.fileAnalyzer = FileAnalyzerFactory.buildFileAnalyzer(this.downloadPath.toFile());
				if(this.fileAnalyzer!=null) {
					this.constructs = this.fileAnalyzer.getConstructs();
					log.info("Got [" + this.constructs.size() + "] constructs from downloaded file [" + this.downloadPath + "]");
				}
			}
			// Get from file
			else {
				if(this.eggFile!=null) {
					this.fileAnalyzer = FileAnalyzerFactory.buildFileAnalyzer(this.eggFile.toFile());
					this.constructs = this.fileAnalyzer.getConstructs();
					log.info("Got [" + this.constructs.size() + "] constructs from egg [" + this.eggFile + "]");
				}
				// Get from file
				else if(this.pyFile!=null) {
					this.fileAnalyzer = FileAnalyzerFactory.buildFileAnalyzer(this.pyFile.toFile());
					this.constructs = this.fileAnalyzer.getConstructs();
					log.info("Got [" + this.constructs.size() + "] constructs from file [" + this.pyFile + "]");
				}
				// Get from dir
				else if(this.pyDir!=null) {
					this.fileAnalyzer = FileAnalyzerFactory.buildFileAnalyzer(this.pyDir.toFile());
					this.constructs = this.fileAnalyzer.getConstructs();
					log.info("Got [" + this.constructs.size() + "] constructs from directory [" + this.pyDir + "]");
				}
				// Error
				else {
					log.error("Cannot get constructs of " + this);
				}
			}
		}
		return this.constructs;
	}
	
	/**
	 * <p>getNestedArchives.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<FileAnalyzer> getNestedArchives() {
		if(this.fileAnalyzer!=null && !(this.fileAnalyzer instanceof DirAnalyzer)) {
			return this.fileAnalyzer.getChilds(true);
		} else {
			return null;
		}
	}

	/** {@inheritDoc} */
	public int compareTo(Object _other) {
		if(_other instanceof PipInstalledPackage) {
			final PipInstalledPackage other = (PipInstalledPackage)_other;
			int i = this.name.compareTo(other.getName());
			if(i==0)
				i = this.version.compareTo(other.getVersion());
			return i;
		} else {
			throw new IllegalArgumentException("Cannot compare with object of type [" + _other.getClass() + "]");
		}
	}

	/**
	 * Searches for an egg file in the location of this installed package.
	 * @return
	 */
	private Path searchEgg() {
		Path file = null;
		final String name = this.getName().replaceAll("-", "_");
		final Path location = Paths.get(this.properties.get(LOCATION));
		FilenamePatternSearch fns = new FilenamePatternSearch("^" + name + "-" + this.version + "-.*egg$");
		Set<Path> files = fns.search(location, 0);
		if(files.size()==0) {
			log.info("No egg file found for " + this + " in path [" + location + "]");
		}
		else if(files.size()==1) {
			file = files.iterator().next();
			log.info("Egg file [" + file + "] found for " + this + " in path [" + location + "]");
		}
		else {
			log.info(files.size() + " egg files found for " + this + " in path [" + location + "]");
		}
		return file;
	}

	/**
	 * Searches for an egg file in the location of this installed package.
	 * @return
	 */
	private Path searchPyFile() {
		Path file = null;
		final String name = this.getName().replaceAll("-", "_");
		final Path location = Paths.get(this.properties.get(LOCATION));
		FilenamePatternSearch fns = new FilenamePatternSearch("^" + name + ".py$");
		Set<Path> files = fns.search(location, 0);
		if(files.size()==0) {
			log.info("No python file found for " + this + " in path [" + location + "]");
		}
		else if(files.size()==1) {
			file = files.iterator().next();
			log.info("Python file [" + file + "] found for " + this + " in path [" + location + "]");
		}
		else {
			log.info(files.size() + " python files found for " + this + " in path [" + location + "]");
		}
		return file;
	}

	/**
	 * Searches for an egg directory in the location of this installed package.
	 * @return
	 */
	private Path searchPyDir() {
		// Search for <name>.py
		Path file = Paths.get(Paths.get(this.properties.get(LOCATION)).toString());
		if(FileUtil.isAccessibleDirectory(file) && !file.toString().endsWith("site-packages"))
			return file;

		// Search for <name>-<version>-py3.6.egg
		final String name = this.getName().replaceAll("-", "_");
		final Path location = Paths.get(this.properties.get(LOCATION));
		DirnamePatternSearch dns = new DirnamePatternSearch("^" + name + "-" + this.version + "-.*egg$");
		Set<Path> files = dns.search(location, 0);
		if(files.size()==0) {
			log.info("No python dir found for " + this + " in path [" + location + "]");
			file = null;
		}
		else if(files.size()==1) {
			log.info("Python dir [" + file + "] found for " + this + " in path [" + location + "]");
			file = files.iterator().next();
		}
		else {
			log.info(files.size() + " python dirs found for " + this + " in path [" + location + "]");
			file = null;
		}
		return file;
	}

	/**
	 * Filter the given packages according to whether the artifact name is (or is not, depending on the boolean flag) contained in the given filter.
	 *
	 * @param _packages a {@link java.util.Set} object.
	 * @param _filter a {@link com.sap.psr.vulas.shared.util.StringList} object.
	 * @param _include a boolean.
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<PipInstalledPackage> filterUsingArtifact(Set<PipInstalledPackage> _packages, StringList _filter, boolean _include) {
		final Set<PipInstalledPackage> r = new HashSet<PipInstalledPackage>();
		for(PipInstalledPackage p: _packages) {
			try {
				if(_include) {
					if(_filter.contains(p.getLibrary().getLibraryId().getArtifact()))
						r.add(p);
				} else {
					if(!_filter.contains(p.getLibrary().getLibraryId().getArtifact()))
						r.add(p);
				}
			} catch (FileAnalysisException e) {
				log.error("Error getting library ID of package [" + p + "]: " + e.getMessage(), e);
			}
		}
		return r;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PipInstalledPackage other = (PipInstalledPackage) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!this.getStandardDistributionName().equals(other.getStandardDistributionName()))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
	
	/**
	 * https://stackoverflow.com/questions/19097057/pip-e-no-magic-underscore-to-dash-replacement
	 * https://www.python.org/dev/peps/pep-0008/#package-and-module-names
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getStandardDistributionName() {
		return PipInstalledPackage.getStandardDistributionName(this.getName());
	}
	
	/**
	 * <p>getStandardDistributionName.</p>
	 *
	 * @param _name a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getStandardDistributionName(String _name) {
		return _name.toLowerCase().replaceAll("\\p{Punct}", "_");
	}
	
	/**
	 * Returns true if the standard distribution name of this package is equal to the one of the given package.
	 *
	 * @param _other a {@link com.sap.psr.vulas.python.pip.PipInstalledPackage} object.
	 * @see PipInstalledPackage#getStandardDistributionName()
	 * @return a boolean.
	 */
	public boolean equalsStandardDistributionName(PipInstalledPackage _other) {
		return this.getStandardDistributionName().equals(_other.getStandardDistributionName());
	}
}
