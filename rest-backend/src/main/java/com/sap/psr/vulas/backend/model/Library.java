package com.sap.psr.vulas.backend.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.jayway.jsonpath.JsonPath;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.backend.util.ConnectionUtil;
import com.sap.psr.vulas.backend.util.DigestVerifierEnumerator;
import com.sap.psr.vulas.backend.util.VerificationException;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true)
@Entity
@Table( name="Lib", uniqueConstraints=@UniqueConstraint( columnNames = { "digest" } ), indexes = {@Index(name = "digest_index",  columnList="digest", unique = true)} )
public class Library implements Serializable {

	private static Logger log = LoggerFactory.getLogger(Library.class);

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long id;

	/** Was previously named 'sha1', but renamed to digest to be more general. */
	@Column(nullable = false, length = 64)
	private String digest;
	
	/** Exists to be backward-compatible with Vulas 2.x clients, see setSha1 for details. */
	@Transient
	private String sha1;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private DigestAlgorithm digestAlgorithm;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar createdAt;

	/**
	 * Used to store additional library properties, e.g., the Java manifest file entries.
	 */
	@ManyToMany(cascade = {}, fetch = FetchType.LAZY)
	@JsonView(Views.LibDetails.class)
	private Collection<Property> properties;

	@ManyToMany(cascade = {}, fetch = FetchType.LAZY)
	@JsonView(Views.Never.class)
	private Collection<ConstructId> constructs;

	//http://stackoverflow.com/questions/23260464/how-to-serialize-using-jsonview-with-nested-objects
	/**
	 * Human-readable library identifier, e.g., a Maven artifact ID consisting of group, artifact and version.
	 */
	@ManyToOne(optional = true, cascade = {}, fetch = FetchType.EAGER)
	private LibraryId libraryId;

	@Transient
	@JsonView(Views.Overview.class)
	private Integer directUsageCounter = null;

	/**
	 * True if the library provider or a trusted software repository confirms the mapping of digest to human-readable ID, false otherwise.
	 * TODO: field now set to false even in case the verification failed because the external service returns [500]; should be null instead
	 * Was 'wellknownSha1' but renamed to wellknownDigest to be more general.
	 * 
	 */
	@Column(nullable = true)
	@JsonIgnoreProperties(value = { "wellknownDigest" }, allowGetters=true)
	private Boolean wellknownDigest;	

	/**
	 * The URL used to verify the digest. Will be empty if none of the available
	 * package repositories was able to confirm the digest.
	 * Should be renamed to wellknownDigest, to be more general.
	 */
	@Column(nullable = true)
	@JsonIgnoreProperties(value = { "digestVerificationUrl" }, allowGetters=true)
	private String digestVerificationUrl;	

	/**
	 * Contains collections of library constructs per {@link ConstructType}.
	 */
	@Transient
	private ConstructIdFilter filter = null;


	public Library() { super(); }

	public Library(String digest) {
		super();
		this.digest = digest;
	}

	@Transient
	private String mvnResponse=null;

	@Transient
	private Set<ProgrammingLanguage> developedIn = null;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	// Only exists for backward-compatibility with Vulas 2.x clients
	public String getSha1() { return this.getDigest(); }

	/**
	 * Vulas 2.x clients will only provide the SHA1 digest of the library. It will be used to set the Vulas 3.x fields {@link Library#digest} and {@link Library#digestAlgorithm}.
	 * @param sha1
	 */
	public void setSha1(String sha1) {
		this.sha1 = sha1;
		this.setDigest(sha1);
		this.setDigestAlgorithm(DigestAlgorithm.SHA1);
	}

	public String getDigest() { return this.digest; }
	public void setDigest(String digest) { this.digest = digest; }

	public DigestAlgorithm getDigestAlgorithm() { return digestAlgorithm; }
	public void setDigestAlgorithm(DigestAlgorithm digestAlgorithm) { this.digestAlgorithm = digestAlgorithm; }

	public java.util.Calendar getCreatedAt() { return createdAt; }
	public void setCreatedAt(java.util.Calendar createdAt) { this.createdAt = createdAt; }

	public Collection<Property> getProperties() { return properties; }
	public void setProperties(Collection<Property> properties) { this.properties = properties; }

	public Collection<ConstructId> getConstructs() { return constructs; }
	public void setConstructs(Collection<ConstructId> constructs) { this.constructs = constructs; }

	public LibraryId getLibraryId() { return libraryId; }
	public void setLibraryId(LibraryId _library_id) { this.libraryId = _library_id; }

	public boolean isWellknownDigest() { return wellknownDigest!=null && wellknownDigest.equals(true); }
	public Boolean getWellknownDigest() { return wellknownDigest; }
	public void setWellknownDigest(Boolean wellknownDigest) { this.wellknownDigest = wellknownDigest; }

	public String getDigestVerificationUrl() { return digestVerificationUrl; }
	public void setDigestVerificationUrl(String digestVerificationUrl) { this.digestVerificationUrl = digestVerificationUrl; }

	//substituted with fields constructorCounter and methodCounter to avoid queries all constructs in order to have the total in the dependencies summary
	/*@JsonProperty(value = "constructCounter")
	public int countConstructs() { return ( this.getConstructs()==null ? 0 : this.getConstructs().size()); } */ 

	/**
	 * Loops over all constructs in order to find the distinct set of {@link ProgrammingLanguage}s used to develop the library.
	 * Note: If slow, it can maybe improved by using a JPQL query.
	 * @return
	 */
	@JsonProperty(value = "developedIn")
	@JsonView(Views.CountDetails.class)
	@JsonIgnoreProperties(value = { "developedIn" }, allowGetters=true)
	public Set<ProgrammingLanguage> getDevelopedIn() {
		if(this.developedIn==null) {
			this.developedIn = new HashSet<ProgrammingLanguage>();
			if(this.getConstructs()!=null) {
				for(ConstructId cid: this.getConstructs()) {
					if(!this.developedIn.contains(cid.getLang())) {
						this.developedIn.add(cid.getLang());
					}
				}
			}
		}
		return this.developedIn;
	}

	/**
	 * Note: Aggregates ignoring the programming language.
	 * @return
	 */
	@JsonProperty(value = "constructTypeCounters")
	@JsonView(Views.CountDetails.class)
	@JsonIgnoreProperties(value = { "constructTypeCounters" }, allowGetters=true)
	public ConstructIdFilter countConstructTypes() {
		if(this.filter==null)
			this.filter = new ConstructIdFilter(this.getConstructs());
		return this.filter;
	}

	@PrePersist
	public void prePersist() {
		if(this.getCreatedAt()==null) {
			this.setCreatedAt(Calendar.getInstance());
		}
		if(this.getWellknownDigest()==null) {
			this.verifyDigest();
		}
		// If uploaded by old client (<3.0.0), the digest also must be set
		if(this.sha1!=null && (this.digest==null || this.digestAlgorithm==null)) {
			this.digest = this.sha1;
			this.digestAlgorithm = DigestAlgorithm.SHA1;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((digest == null) ? 0 : digest.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Library other = (Library) obj;
		if (digest == null) {
			if (other.digest != null)
				return false;
		} else if (!digest.equals(other.digest))
			return false;
		return true;
	}

	@Override
	public final String toString() {
		return this.toString(false);
	}

	public final String toString(boolean _deep) {
		final StringBuilder builder = new StringBuilder();
		if(_deep) {
			builder.append("Library ").append(this.toString(false)).append(System.getProperty("line.separator"));
			for(ConstructId cid: this.getConstructs()) {
				builder.append("    ConstructId     ").append(cid).append(System.getProperty("line.separator"));
			}
		}
		else {
			builder.append("[digest=").append(this.getDigest());
			if(this.getLibraryId()!=null) {
				builder.append(", libid=").append(this.getLibraryId().getMvnGroup()).append(":").append(this.getLibraryId().getArtifact()).append(":").append(this.getLibraryId().getVersion());
			}
			if(this.getConstructs()!=null) {
				builder.append(", #constructs=").append(this.getConstructs().size());
			}
			builder.append("]");
		}
		return builder.toString();
	}

	//changed to public temporarily to recreate wellknownDigest flag for already persisted libs
	public void verifyDigest() {
		if(this.getWellknownDigest()==null || this.getDigestVerificationUrl()==null) {
			try {
				final DigestVerifierEnumerator dv = new DigestVerifierEnumerator();
				final Boolean verified = dv.verify(this);
				if(verified!=null) {
					this.setDigestVerificationUrl(dv.getVerificationUrl());
					this.setWellknownDigest(verified);
				}
			} catch (VerificationException e) {
				log.error(e.getMessage());
			}
		}
	}

	//changed to public temporarily to recreate libId flag for already persisted libs
	public final LibraryId getLibIdFromMaven(String _digest) {
		final String result = callMvn(_digest);
		LibraryId libid = null;
		try {
			libid = new LibraryId((String)JsonPath.read(result, "$.response.docs[0].g"),(String)JsonPath.read(result, "$.response.docs[0].a"),(String)JsonPath.read(result, "$.response.docs[0].v"));
		}
		//		catch(PathNotFoundException pnfe) {
		catch(Exception e) {
			System.err.println("[" + e.getClass().getSimpleName() + "], no artifact found for digest [" + _digest + "]: " + e.getMessage());
		}
		return libid;
	}

	//changed to public temporarily to recreate wellknownDigest flag for already persisted libs
	private String callMvn(String _digest) {
		if(mvnResponse==null){
			int sc = -1;
			String uri = null;
			try {
				final CloseableHttpClient httpclient = HttpClients.createDefault();
				uri = new String("http://search.maven.org/solrsearch/select?q=1:<SHA1>&rows=20&wt=json").replaceAll("<SHA1>", _digest);
				final HttpGet method = new HttpGet(uri);
				if(ConnectionUtil.getProxyConfig()!=null)
					method.setConfig(ConnectionUtil.getProxyConfig());
				final CloseableHttpResponse response = httpclient.execute(method);
				try {
					sc = response.getStatusLine().getStatusCode();
					HttpEntity entity = response.getEntity();
					if (sc==HttpStatus.SC_OK && entity != null) {
						mvnResponse = ConnectionUtil.readInputStream(entity.getContent());

					}
				} finally {
					response.close();
				}
			} catch (ClientProtocolException e) {
				log.error("HTTP GET [uri=" + uri + "] caused an exception: " + e.getMessage());
			} catch (Exception e) {
				log.error("HTTP GET [uri=" + uri + "] caused an exception: " + e.getMessage(), e);
			}
		}
		return mvnResponse;
	}

	public Integer getDirectUsageCounter() {
		return directUsageCounter;
	}

	public void setDirectUsageCounter(Integer directUsageCounter) {
		this.directUsageCounter = directUsageCounter;
	}
}
