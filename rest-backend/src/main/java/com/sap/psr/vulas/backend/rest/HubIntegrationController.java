package com.sap.psr.vulas.backend.rest;


import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityNotFoundException;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.apache.tomcat.util.security.Escape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.DispatcherServlet;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Excemption;
import com.sap.psr.vulas.backend.model.GoalExecution;
import com.sap.psr.vulas.backend.model.Space;
import com.sap.psr.vulas.backend.model.Tenant;
import com.sap.psr.vulas.backend.model.VulnerableDependency;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.backend.repo.ApplicationRepository;
import com.sap.psr.vulas.backend.repo.GoalExecutionRepository;
import com.sap.psr.vulas.backend.repo.SpaceRepository;
import com.sap.psr.vulas.backend.repo.TenantRepository;
import com.sap.psr.vulas.shared.enums.ExportConfiguration;
import com.sap.psr.vulas.shared.enums.Scope;
import com.sap.psr.vulas.shared.util.Constants;
import com.sap.psr.vulas.shared.util.StopWatch;


/**
 * RESTful interface for application information.
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path="/hubIntegration/apps")
public class HubIntegrationController {

	private static Logger log = LoggerFactory.getLogger(HubIntegrationController.class);

	private final ApplicationRepository appRepository;

	private final GoalExecutionRepository gexeRepository;

	private final SpaceRepository spaceRepository;

	private final TenantRepository tenantRepository;

	@Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
	public DispatcherServlet dispatcherServlet() {
		DispatcherServlet dispatcherServlet = new DispatcherServlet();
		dispatcherServlet.setDispatchOptionsRequest(true);
		return dispatcherServlet;
	}

	@Autowired
	HubIntegrationController(ApplicationRepository appRepository, GoalExecutionRepository gexeRepository, SpaceRepository spaceRepository, TenantRepository tenantRepository) {
		this.appRepository = appRepository;
		this.gexeRepository = gexeRepository;
		this.spaceRepository = spaceRepository;
		this.tenantRepository = tenantRepository;
	}

	/**
	 * Returns a sorted set of item identifiers. Items can correspond to either {@link Space}s or {@link Application}s, depending on the value of {@link Space#getExportConfiguration()}.
	 * The identifiers looks as follows:
	 * <space-name> (<space-token>)
	 * <space-name> (<space-token>) <separator><group><separator><artifact><separator><version>
	 * 
	 * @return sorted set of all items for which data can be exported 
	 */
	@RequestMapping(value = "", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	public ResponseEntity<Collection<String>> getExportItemIds(
			@RequestParam(value="skipEmpty", required=false, defaultValue="false") Boolean skipEmpty, 
			@RequestParam(value="separator", required=false, defaultValue=":") String separator, 
			@RequestParam(value="max", required=false, defaultValue="-1") Integer max,
			@RequestParam(value="asOf", required=false, defaultValue="0") String asOfTimestamp,
			@RequestParam(value="aggregate", required=false, defaultValue="true") Boolean aggregate,
			@RequestHeader(value=Constants.HTTP_TENANT_HEADER, required=false) String tenant) {

		// Get the tenant
		Tenant t = null;
		try {
			if(tenant!=null && !tenant.equals(""))
				t = TenantRepository.FILTER.findOne(this.tenantRepository.findBySecondaryKey(tenant));
			else
				t = tenantRepository.findDefault();
		}
		catch(EntityNotFoundException enfe) {
			log.error("Tenant [" + tenant + "] not found");
			throw new RuntimeException("Tenant [" + tenant + "] not found");
		}
		
		// To be returned
		final Collection<String> items = new TreeSet<String>();

		// Get all spaces and evaluate the export setting
		final List<Space> spaces = this.spaceRepository.findAllTenantSpaces(t.getTenantToken());
		
		outerloop:
		for(Space s: spaces) {
			// Export will be aggregated (one item only, corresponding to the space)
			if(aggregate && s.getExportConfiguration()==ExportConfiguration.AGGREGATED) {
				//if asOfTimestamp has been specified, we check if at least 1 application in the space has lastChange>asOfTimestamp
				Boolean toAdd=true;
				if(Long.parseLong(asOfTimestamp)>0){
					final Set<Application> apps = this.appRepository.getApplications(skipEmpty, s.getSpaceToken(), Long.parseLong(asOfTimestamp));
					if(apps.isEmpty())
						toAdd=false;
				}
				if(toAdd){
					final ExportItem item = new ExportItem(s, null);
					if(max==-1 || items.size()<max)
						items.add(item.toString(separator));
					else
						break outerloop;
				}
			}
			// Export will be individual (one item per space application)
			else if((!aggregate && s.getExportConfiguration()==ExportConfiguration.AGGREGATED) || s.getExportConfiguration()==ExportConfiguration.DETAILED) {
				final Set<Application> apps = this.appRepository.getApplications(skipEmpty, s.getSpaceToken(), Long.parseLong(asOfTimestamp));
				for(Application app: apps) {
					final ExportItem item = new ExportItem(s, app);
					if(max==-1 || items.size()<max)
						items.add(item.toString(separator));
					else
						break outerloop;
				}
			}
			// No export
			else if(s.getExportConfiguration()==ExportConfiguration.OFF) {
				continue;
			}
		}

		return new ResponseEntity<Collection<String>>(items, HttpStatus.OK);
	}

	/**
	 * Returns a collection of {@link VulnerableDependency}s relevant for the {@link Application} with the given GAV.
	 * @param group/artifact/version
	 * @return 404 {@link HttpStatus#NOT_FOUND} if application with given GAV does not exist, 200 {@link HttpStatus#OK} if the application is found
	 */
	@RequestMapping(value = "/{itemId:.+}/vulndeps", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	public ResponseEntity<TreeSet<VulnerableItemDependency>> getExportItem(
			@PathVariable String itemId,
			@RequestParam(value="separator", required=false, defaultValue=":") String separator,
			@RequestParam(value="excludedScopes", required=false, defaultValue="") Scope[] excludedScopes,
			@RequestHeader(value=Constants.HTTP_TENANT_HEADER, required=false) String tenant) {

		// Get the tenant
		Tenant t = null;
		try {
			if(tenant!=null && !tenant.equals(""))
				t = TenantRepository.FILTER.findOne(this.tenantRepository.findBySecondaryKey(tenant));
			else
				t = tenantRepository.findDefault();
		}
		catch(EntityNotFoundException enfe) {
			log.error("Tenant [" + tenant + "] not found");
			throw new RuntimeException("Tenant [" + tenant + "] not found");
		}

		try {
			// Build from the simple string argument
			final ExportItem item = ExportItem.fromString(itemId, separator, spaceRepository, appRepository);
			final StopWatch sw = new StopWatch("Query vulnerable dependencies for item [" + itemId + "] (total)").start();

			// The set to be returned
			TreeSet<VulnerableItemDependency> vd_list = new TreeSet<VulnerableItemDependency>();

			// Export of app statistics
			if(item.hasApplication()) {
				final Application app = item.getApplication();
				vd_list.addAll(this.getVulnerableItemDependencies(item.getSpace(), app, excludedScopes, false));
			}
			// Export of space statistics
			else {
				final Space space = item.getSpace();
				final List<Application> apps = this.appRepository.findAllApps(space.getSpaceToken(),0);
				for(Application app: apps) {
					vd_list.addAll(this.getVulnerableItemDependencies(space, app, excludedScopes, true));
				}
			}

			sw.stop();
			return new ResponseEntity<TreeSet<VulnerableItemDependency>>(vd_list, HttpStatus.OK);
		}
		// Thrown by ExportItem.fromString(...)
		catch(IllegalArgumentException iae) {
			return new ResponseEntity<TreeSet<VulnerableItemDependency>>(HttpStatus.BAD_REQUEST);
		}
		catch(EntityNotFoundException enfe) {
			return new ResponseEntity<TreeSet<VulnerableItemDependency>>(HttpStatus.NOT_FOUND);
		}
	}

	private TreeSet<VulnerableItemDependency> getVulnerableItemDependencies(Space _s, Application _app, Scope[] _excluded_scopes, boolean _include_app_gav) {
		final TreeSet<VulnerableItemDependency> vd_list = new TreeSet<VulnerableItemDependency>();

		// Get latest goal execution date
		final GoalExecution latest_gexe = gexeRepository.findLatestGoalExecution(_app, null); 
		final Calendar snapshot_date = (latest_gexe==null ? null : latest_gexe.getCreatedAt());

		// All of them (no matter the scope)
		final TreeSet<VulnerableDependency> vd_all = this.appRepository.findAppVulnerableDependencies(_app, true, false);

		// Update traced and reachable flags 
		// populate the set to be returned depending on the historical flag
		for (VulnerableDependency vd : vd_all) {
			if(vd.getAffectedVersion()==1) {

				// Create vulnerable hub dependency
				VulnerableItemDependency vhd = null;
				if(_include_app_gav)
					vhd = new VulnerableItemDependency(_s.getSpaceToken(), _app, vd, snapshot_date);
				else
					vhd = new VulnerableItemDependency(_s.getSpaceToken(), vd, snapshot_date);

				//add application id to ease integration
				vhd.setAppId(_app.getId());
				//add application id to ease integration
				vhd.setLastScan(_app.getLastScan());
				//add client version
				if(latest_gexe!=null)
					vhd.setClientVersion(latest_gexe.getClientVersion());
				
				// Set to null if among excluded scopes
				if(_excluded_scopes!=null && _excluded_scopes.length>0) {
					for(Scope scope: _excluded_scopes) {
						if(scope.equals(vd.getDep().getScope())) {
							vhd = null;
							break;
						}
					}
				}

				// Set priority and add to collection
				if(vhd!=null) {
					if(Scope.TEST.equals(vd.getDep().getScope()) || Scope.PROVIDED.equals(vd.getDep().getScope()))
						vhd.setPriority(VulnerableItemDependency.OPTIONAL);
					vd_list.add(vhd);
				}
			}
		}

		return vd_list;
	}

	/**
	 * Represents an item for which vulnerability statistics can be exported.
	 * Items are either identified by space token and space name or by space token, space name and application GAV.
	 */
	static class ExportItem {

		private Space space;		
		private Application app;

		private ExportItem(@NotNull Space _s, Application _app) {
			this.space = _s;
			this.app = _app;
		}

		static ExportItem fromString(String _string, String _separator, SpaceRepository _space_repo, ApplicationRepository _app_repo) throws IllegalArgumentException, EntityNotFoundException {
			// Space
			String token = null;
			final int idx_start = _string.indexOf("(");
			final int idx_end   = _string.indexOf(")");
			if(idx_start==-1 || idx_end==-1)
				throw new IllegalArgumentException("Cannot find space token in argument [" + _string + "]");
			else
				token = _string.substring(idx_start+1, idx_end);

			Space space = SpaceRepository.FILTER.findOne(_space_repo.findBySecondaryKey(token));
			Application app = null;

			// Application
			final String app_string = _string.substring(idx_end+1);
			if(app_string!=null && !app_string.equalsIgnoreCase("")) {
				final String[] app_gav = app_string.split(_separator);
				if(app_gav.length!=3)
					throw new IllegalArgumentException("Cannot find application identifier in argument [" + _string + "]");
				app = ApplicationRepository.FILTER.findOne(_app_repo.findByGAV(revertEscapedCharacters(app_gav[0].trim()),  revertEscapedCharacters(app_gav[1].trim()),  revertEscapedCharacters(app_gav[2].trim()), space));
			}

			return new ExportItem(space, app);
		}

		private Space getSpace() { return this.space; }

		private Application getApplication() { return this.app; }

		private boolean hasApplication() { return this.app!=null; }

		private String toString(String _separator) {
			final StringBuffer b = new StringBuffer();
			b.append(this.space.getSpaceName()).append(" (").append(this.space.getSpaceToken()).append(")");
			if(this.app!=null)
				b.append(" ").append(escapeCharacters(this.app.getMvnGroup())).append(_separator).append(escapeCharacters(this.app.getArtifact())).append(_separator).append(escapeCharacters(this.app.getVersion()));
			return b.toString();
		}	
		
		private String escapeCharacters(String _param){
			return _param.replace("/", "%2F");
		}
		
		private static String revertEscapedCharacters(String _param){
			return _param.replace("%2F", "/");
		}
	}

	@JsonInclude(JsonInclude.Include.ALWAYS)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class VulnerableItemDependency implements Serializable, Comparable<VulnerableItemDependency> {

		private int count = 1;

		private String projectId;
		
		// === Property PRIORITY
		
		// Priority (1 = high, 5 = low)
		public static final int AUDIT_ALL = 1;
		public static final int SPOT_CHECKS = 2;
		public static final int OPTIONAL = 3;
		public static final int NO_PRIO = 5;

		private int priority = AUDIT_ALL; // default to highest prio
		
		// === Property STATE
		
		public static final int NOT_AUDITED = -1;
		public static final int FP_TOOL_LIMIT = 0;
		public static final int FP_SECURE_DESIGN = 1;
		public static final int TP = 2;
		public static final int FP_MITIGATED = 4;
		
		/** Defaults to {@link #TP}, will be set to {@link #FP_SECURE_DESIGN} if the dependency scope has been exempted or to {@link #FP_MITIGATED} if the vulnerability has been exempted. */
		private int state = TP;

		private String exemptionReason = null;
		
		// === Property STATUS
		
		/** Not audited in Vulas. */
		public static final int STATUS_NOT_AUDITED = -1;
		
		/** Audited in Vulas. */
		public static final int STATUS_AUDITED = 1;
		
		/** Defaults to {@link #STATUS_NOT_AUDITED}, will be set to {@link #STATUS_AUDITED} if the state equals {@link #FP_SECURE_DESIGN} or {@link #FP_MITIGATED}. */
		private int status = STATUS_NOT_AUDITED;

		/** Vulnerability identifier. */
		private String type;
		
		private String scope = null;
		
		private String spaceToken = null;
		
		private Long appId = null;
		
		private Boolean reachable = null;
		
		private String clientVersion = null;
		
		private Calendar lastScan = null;
		

		@JsonIgnore
		private VulnerableDependency vulnerableDependency = null;

		@JsonIgnore
		private Application application = null;

		@Temporal(TemporalType.TIMESTAMP)
		@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm", timezone="GMT")
		private java.util.Calendar snapshotDate; 

		private VulnerableItemDependency(String _spaceToken, VulnerableDependency _vd, Calendar _date) {
			this(_spaceToken, null, _vd, _date);
		}

		private VulnerableItemDependency(String _spaceToken, Application _app, VulnerableDependency _vd, Calendar _date) {
			// Only filename
			if(_app==null)
				this.projectId = _vd.getDep().getFilename();
			// Include app GAV
			else
				this.projectId = _app.getMvnGroup() + ":" + _app.getArtifact() + ":" + _app.getVersion() + " > " + _vd.getDep().getFilename();

			this.application = _app;
			this.vulnerableDependency = _vd;
			this.type = _vd.getBug().getBugId();
			this.snapshotDate = _date;
			this.scope = _vd.getDep().getScope()==null ? null : _vd.getDep().getScope().toString();
			this.spaceToken = _spaceToken;
			
			this.reachable = (_vd.getReachable()==1 || _vd.getTraced()==1);

			// Scope excluded: State to 1 (False Positive: Secure by design)
			// Bug excluded: State to 4 (False Positive: Sufficient mitigation in place)
			// Else: State to 2 (True Positive)
			final Excemption exc = _vd.getExcemption();
			if(exc!=null && exc.isExcludedBug()) {
				this.status = STATUS_AUDITED;
				this.state = FP_MITIGATED;
				this.exemptionReason = exc.getExcludedBugReason();
			}
			else if(exc!=null && exc.isExcludedScope()) {
				this.status = STATUS_AUDITED;
				this.state = FP_SECURE_DESIGN;
				this.exemptionReason = "Scope " + this.scope + " excluded";
			}
			else
				this.state = TP;
		}

		public int getCount() { return count; }
		
		public String getExemptionReason() { return this.exemptionReason; }
		
		public String getScope() { return this.scope; }
		
		public String getSpaceToken() { return this.spaceToken; }

		public String getProjectId() { return projectId; }

		public int getPriority() { return priority; }
		public void setPriority(int _prio) { this.priority= _prio; }

		public int getStatus() { return status; }

		public int getState() { return state; }

		public String getType() { return type; }

		public java.util.Calendar getSnapshotDate() { return snapshotDate; }
		
		public Long getAppId() { return this.appId; }
		
		public void setAppId(Long _id){
			this.appId = _id;
		}
		
		public Boolean getReachable() { return this.reachable; }

		
		public String getClientVersion() { return this.clientVersion; }
		
		public void setClientVersion(String _clientVersion){
			this.clientVersion = _clientVersion;
		}
		
		public Calendar getLastScan() { return this.lastScan; }
		
		public void setLastScan(Calendar _lastScan){
			this.lastScan = _lastScan;
		}
		
		/**
		 * Delegates the comparison to {@link Application#compareTo(Application)} and {@link VulnerableDependency#compareTo(VulnerableDependency)}.
		 */
		@Override
		public int compareTo(VulnerableItemDependency _other) {
			int c = 0;
			if(this.application!=null && _other.application!=null)
				c = this.application.compareTo(_other.application);
			if(c==0)
				c = this.vulnerableDependency.compareTo(_other.vulnerableDependency);
			return c;
		}
	}
}
