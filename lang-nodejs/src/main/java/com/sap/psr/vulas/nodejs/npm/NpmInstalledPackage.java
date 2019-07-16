package com.sap.psr.vulas.nodejs.npm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.sap.psr.vulas.*;
import com.sap.psr.vulas.nodejs.NodejsFileAnalyzer;
import com.sap.psr.vulas.nodejs.NodejsPackageAnalyzer;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.enums.PropertySource;
import com.sap.psr.vulas.shared.json.model.Library;
import com.sap.psr.vulas.shared.json.model.LibraryId;
import com.sap.psr.vulas.shared.json.model.Property;
import com.sap.psr.vulas.shared.util.DirUtil;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NpmInstalledPackage implements Comparable {

    private final static Log log = LogFactory.getLog(NpmInstalledPackage.class);

    private static final String LOCATION = "location";
    private static final String REQUIRES = "dependencies";
    private static final String REQUIRED_BY = "required_by";

    private String name = null;
    private String version = null;
    private Map<String, String> properties = new HashMap<String, String>();
    private String digest = null;
    private Map<ConstructId, Construct> constructs = null;

    private String downloadUrl = null;
    private Path downloadPath = null;

    private Path jsFile = null;

    private FileAnalyzer fileAnalyzer = null;

    public NpmInstalledPackage(String _name, String _version) {
        this.name = _name;
        this.version = _version;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Returns the installation path of the npm package.
     * @return
     */
    public Path getInstallPath() {
        return Paths.get(this.properties.get(LOCATION));
    }

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    /**
     * Returns the download path of the npm package. This path is extracted from the output of "npm install".
     * @return
     */
    public Path getDownloadPath() {
        return this.downloadPath;
    }

    public void setDownloadPath(Path downloadPath) {
        this.downloadPath = downloadPath;
    }

    /**
     * Returns a {@link Library} representing the analyzed archive.
     * @return
     * @throws FileAnalysisException
     */
    public Library getLibrary() throws FileAnalysisException {
        Library lib;

        if(this.fileAnalyzer != null && this.fileAnalyzer instanceof NodejsPackageAnalyzer) {
            lib = ((NodejsPackageAnalyzer) this.fileAnalyzer).getLibrary();
            lib.setLibraryId(new LibraryId(this.getName(), this.getName(), this.getVersion()));
        }
        else {
            lib = new Library();
            lib.setDigest(this.getDigest());
            lib.setDigestAlgorithm(DigestAlgorithm.MD5);
            lib.setLibraryId(new LibraryId(this.getName(), this.getName(), this.getVersion()));
            if(this.getConstructs()!=null) {
                lib.setConstructs(ConstructId.getSharedType(this.getConstructs().keySet()));
            }
        }

        final Set<Property> p = new HashSet<Property>();
        for(String key: this.getProperties().keySet()) {
            p.add(new Property(PropertySource.NPM, key, this.getProperties().get(key)));
        }
        lib.setProperties(p);

        return lib;
    }

    /**
     * Returns true if this package requieres the given package, as indicated by the 'Requires' property, false otherwise.
     * @param _pack
     * @return
     */
    public boolean requires(NpmInstalledPackage _pack) throws IllegalStateException {
        if(this.properties == null || !this.properties.containsKey(REQUIRED_BY))
            throw new IllegalStateException("Property [" + REQUIRED_BY + "] not known");

        final String[] packs = this.properties.get(REQUIRED_BY).split(",");
        for(int i=0; i<packs.length; i++) {
            if (this.properties.get("dep_location").equalsIgnoreCase(packs[i].trim()))
                return true;
        }
        return false;
    }

    /**
     * Sets the properties as provided by npm.
     * @param properties
     */
    public void addProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
    }

    public String getName() { return name; }
    public String getVersion() { return version; }

    @Override
    public String toString() {
        final StringBuffer b= new StringBuffer();
        b.append("[").append(this.getName()).append(":").append(this.getVersion()).append("]");
        return b.toString();
    }

    public String getDigest() {
        if(this.downloadPath==null && (this.properties==null || !this.properties.containsKey(LOCATION)))
            throw new IllegalStateException(this + " does not have local download path nor property [" + LOCATION + "]");

        if(this.digest == null) {
            if(this.properties.get("integrity").equalsIgnoreCase(""))
                this.digest = "";
            else
                this.digest = this.properties.get("integrity").split("-")[1];
//            // Take the downloaded file
//            if(this.downloadPath != null) {
//                this.digest = FileUtil.getDigest(this.downloadPath.toFile(), DigestAlgorithm.MD5);
//                log.info("Computed MD5 [" + this.digest + "] from downloaded file [" + this.downloadPath + "]");
//            }
//            // Search in site-packages (location property)
//            else {
//                //this.jsFile = this.searchJsFile();
//                if(this.jsFile != null) {
//                    this.digest = FileUtil.getDigest(this.jsFile.toFile(), DigestAlgorithm.MD5);
//                    log.info("Computed MD5 [" + this.digest + "] from file [" + this.jsFile + "]");
//                }
//                else {
//                    log.error("Cannot compute MD5 of " + this);
//                }
//            }
        }
        return this.digest;
    }

    /**
     *
     * @return
     * @throws FileAnalysisException
     */
    private Map<ConstructId, Construct> getConstructs() throws FileAnalysisException {
        if(this.constructs == null){
            // Get from directory
            if(this.downloadPath != null) {
                this.fileAnalyzer = FileAnalyzerFactory.buildFileAnalyzer(this.downloadPath.toFile());
                this.constructs = this.fileAnalyzer.getConstructs();
                log.info("Got [" + this.constructs.size() + "] constructs from package [" + this.getName() + "]");
            }
            // Error
            else {
                log.error("Cannot get constructs of " + this);
            }
        }
        return this.constructs;
    }



    /**
     * Filter the given packages according to whether the artifact name is (or is not, depending on the boolean flag) contained in the given filter.
     * @param _packages
     * @param _filter
     * @param _include
     * @return
     */
    public static Set<NpmInstalledPackage> filterUsingArtifact(Set<NpmInstalledPackage> _packages, StringList _filter, boolean _include) {
        final Set<NpmInstalledPackage> r = new HashSet<NpmInstalledPackage>();
        for(NpmInstalledPackage p: _packages) {
            try {
                if(_include) {
                    if(_filter.contains(p.getLibrary().getLibraryId().getArtifact())) {
                        r.add(p);
                    }
                }
                else {
                    if(!_filter.contains(p.getLibrary().getLibraryId().getArtifact())) {
                        r.add(p);
                    }
                }
            } catch (FileAnalysisException e) {
                log.error("Error getting library ID of package [" + p + "]: " + e.getMessage(), e);
            }
        }
        return r;
    }

    @Override
    public int compareTo(Object _other) {
        if(_other instanceof NpmInstalledPackage) {
            final NpmInstalledPackage other = (NpmInstalledPackage) _other;
            int i = this.name.compareTo(other.getName());
            if(i==0)
                i = this.version.compareTo(other.getVersion());
            return i;
        }
        else {
            throw new IllegalArgumentException("Cannot compare with object of type [" + _other.getClass() + "]");
        }
    }

//    public String getStandardDistributionName() {
//        return NpmInstalledPackage.getStandardDistributionName(this.getName());
//    }

//    public static String getStandardDistributionName(String _name) {
//        return _name.toLowerCase().replaceAll("\\p{Punct}", "_");
//    }
}
