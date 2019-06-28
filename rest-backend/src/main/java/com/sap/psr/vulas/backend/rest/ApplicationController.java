package com.sap.psr.vulas.backend.rest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.DispatcherServlet;

import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.component.ApplicationExporter;
import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.ConstructIdFilter;
import com.sap.psr.vulas.backend.model.ConstructSearchResult;
import com.sap.psr.vulas.backend.model.Dependency;
import com.sap.psr.vulas.backend.model.DependencyIntersection;
import com.sap.psr.vulas.backend.model.DependencyUpdate;
import com.sap.psr.vulas.backend.model.GoalExecution;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.backend.model.Path;
import com.sap.psr.vulas.backend.model.PathNode;
import com.sap.psr.vulas.backend.model.Space;
import com.sap.psr.vulas.backend.model.Tenant;
import com.sap.psr.vulas.backend.model.TouchPoint;
import com.sap.psr.vulas.backend.model.Trace;
import com.sap.psr.vulas.backend.model.VulnerableDependency;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.backend.repo.AffectedLibraryRepository;
import com.sap.psr.vulas.backend.repo.ApplicationRepository;
import com.sap.psr.vulas.backend.repo.BugRepository;
import com.sap.psr.vulas.backend.repo.ConstructIdRepository;
import com.sap.psr.vulas.backend.repo.DependencyRepository;
import com.sap.psr.vulas.backend.repo.GoalExecutionRepository;
import com.sap.psr.vulas.backend.repo.LibraryRepository;
import com.sap.psr.vulas.backend.repo.PathRepository;
import com.sap.psr.vulas.backend.repo.SpaceRepository;
import com.sap.psr.vulas.backend.repo.TenantRepository;
import com.sap.psr.vulas.backend.repo.TracesRepository;
import com.sap.psr.vulas.backend.repo.V_AppVulndepRepository;
import com.sap.psr.vulas.backend.util.DependencyUtil;
import com.sap.psr.vulas.backend.util.Message;
import com.sap.psr.vulas.backend.util.ServiceWrapper;
import com.sap.psr.vulas.shared.connectivity.ServiceConnectionException;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.ExportFormat;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.enums.Scope;
import com.sap.psr.vulas.shared.enums.VulnDepOrigin;
import com.sap.psr.vulas.shared.json.model.diff.JarDiffResult;
import com.sap.psr.vulas.shared.json.model.metrics.Counter;
import com.sap.psr.vulas.shared.json.model.metrics.Metrics;
import com.sap.psr.vulas.shared.json.model.metrics.Ratio;
import com.sap.psr.vulas.shared.util.Constants;
import com.sap.psr.vulas.shared.util.StopWatch;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

import springfox.documentation.annotations.ApiIgnore;


/**
 * RESTful interface for application information.
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path="/apps")
public class ApplicationController {

	private static Logger log = LoggerFactory.getLogger(ApplicationController.class);

	private final ApplicationRepository appRepository;

	private final DependencyRepository depRepository;

	private final GoalExecutionRepository gexeRepository;

	private final TracesRepository traceRepository;

	private final LibraryRepository libRepository;

	private final AffectedLibraryRepository affLibRepository;

	private final PathRepository pathRepository;

	private final BugRepository bugRepository;

	private final SpaceRepository spaceRepository;

	private final TenantRepository tenantRepository;

	private final ConstructIdRepository cidRepository;
	
	private final V_AppVulndepRepository appVulDepRepository;
	
	private final ApplicationExporter appExporter;
	
	public final static String SENDER_EMAIL = "vulas.backend.smtp.sender";
	
	public static final String ALL_APPS_CSV_SUBJECT = "vulas.backend.allApps.mailSubject";

	@Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
	public DispatcherServlet dispatcherServlet() {
		DispatcherServlet dispatcherServlet = new DispatcherServlet();
		dispatcherServlet.setDispatchOptionsRequest(true);
		return dispatcherServlet;
	}

	@Autowired
	ApplicationController(ApplicationRepository appRepository, GoalExecutionRepository gexeRepository, DependencyRepository depRepository, TracesRepository traceRepository, LibraryRepository libRepository, PathRepository pathRepository, BugRepository bugRepository, SpaceRepository tokenRepository, ConstructIdRepository cidRepository, AffectedLibraryRepository affLibRepository, TenantRepository tenantRepository, V_AppVulndepRepository appVulDepRepository, ApplicationExporter appExporter) {
		this.appRepository = appRepository;
		this.gexeRepository = gexeRepository;
		this.depRepository = depRepository;
		this.traceRepository = traceRepository;
		this.libRepository = libRepository;
		this.pathRepository = pathRepository;
		this.bugRepository = bugRepository;
		this.spaceRepository = tokenRepository;
		this.cidRepository = cidRepository;
		this.affLibRepository = affLibRepository;
		this.tenantRepository = tenantRepository;
		this.appVulDepRepository = appVulDepRepository;
		this.appExporter = appExporter;
	}

	//TODO: The space headers must become mandatory once we get (most of) users to switch to vulas3
	@RequestMapping(value = "", method = RequestMethod.POST, consumes = {"application/json;charset=UTF-8"}, 
			produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	public ResponseEntity<Application> createApplication(@RequestBody Application application, 
			@RequestParam(value="skipResponseBody", required=false, defaultValue="false") Boolean skipResponseBody,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
			if(s.isReadOnly())
				return new ResponseEntity<Application>(HttpStatus.BAD_REQUEST);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<Application>(HttpStatus.NOT_FOUND);
		}
		
		if(!DependencyUtil.isValidDependencyCollection(application))
			return new ResponseEntity<Application>(HttpStatus.BAD_REQUEST);

		try{
			final String group = application.getMvnGroup();
			final String artifact = application.getArtifact();
			final String version = application.getVersion();

			ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(group, artifact, version, s));

			// Return CONFLICT to indicate that resource with this digest already exists
			return new ResponseEntity<Application>(HttpStatus.CONFLICT);
		} catch (EntityNotFoundException e) {
			try {
				application.setSpace(s);
				final Application app = this.appRepository.customSave(application);
				if(skipResponseBody){
					return new ResponseEntity<Application>(HttpStatus.CREATED);
				}
				else
					return new ResponseEntity<Application>(app, HttpStatus.CREATED);
			} catch (PersistenceException e1) {
				return new ResponseEntity<Application>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	/**
	 * Deletes multiple {@link Application} versions at once.
	 * @return 404 {@link HttpStatus#NOT_FOUND} if bug with given digest does not exist,
	 * 		   422 {@link HttpStatus.UNPROCESSABLE_ENTITY} if the value of path variable (digest) does not equal the corresponding field in the body
	 * 		   200 {@link HttpStatus#OK} if the library was successfully re-created
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}", method = RequestMethod.DELETE)
	@JsonView(Views.Default.class)
	public ResponseEntity<List<Application>> purgeApplicationVersions(@PathVariable String mvnGroup, 
			@PathVariable String artifact,
			@RequestParam(value="keep", required=false, defaultValue="3") Integer keep,
			@RequestParam(value="mode", required=false, defaultValue="versions") String mode,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER,  required=false)  String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
			if(s.isReadOnly())
				return new ResponseEntity<List<Application>>(HttpStatus.BAD_REQUEST);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<List<Application>>(HttpStatus.NOT_FOUND);
		}	

		try {
			// Check args
			if( keep<0 || keep>100 || mode==null || (!mode.equalsIgnoreCase("DAYS") && !mode.equalsIgnoreCase("VERSIONS")) ) {
				log.error("Invalid value for arg 'keep' (anything between 0 and 100, is [" + keep + "]) and/or arg 'mode' (should be DAYS or VERSIONS, is [" + mode + "])"); 
				return new ResponseEntity<List<Application>>(HttpStatus.BAD_REQUEST);
			}

			final List<Application> apps = this.appRepository.findByGA(mvnGroup, artifact,s);

			// None found, return 404
			if(apps==null || apps.isEmpty())
				return new ResponseEntity<List<Application>>(HttpStatus.NOT_FOUND);

			// Sort them after creation date
			final SortedSet<Application> sorted_apps = new TreeSet<Application>(new Comparator<Application>() {
				public int compare(Application a1, Application a2) {
					return a1.getCreatedAt().compareTo(a2.getCreatedAt());
				}
			});
			sorted_apps.addAll(apps);

			// The deleted apps
			final List<Application> deleted_apps = new ArrayList<Application>();

			// Delete all except those from the last KEEP days
			if(mode.equalsIgnoreCase("DAYS")) {
				final long milli_treshold = System.currentTimeMillis() - (keep * 24 * 60 * 60 * 1000);
				for(Application app: sorted_apps) {
					if(app.getCreatedAt().getTime().getTime() < milli_treshold) {
						deleted_apps.add(app);
						try {
							this.appRepository.deleteAnalysisResults(app, true);
							this.appRepository.delete(app);
						} catch (Exception e) {
							log.error("Error while deleting app " + app + ": " + e.getMessage(), e);
						}
					}
				}
			}
			// Delete all except the last KEEP versions
			else {
				for(Application app: sorted_apps) {
					if(sorted_apps.size() - deleted_apps.size() > keep) {
						deleted_apps.add(app);
						try {
							this.appRepository.deleteAnalysisResults(app, true);
							this.appRepository.delete(app);
						} catch (Exception e) {
							log.error("Error while deleting app " + app + ": " + e.getMessage(), e);
						}	
					}
				}
			}

			if(deleted_apps.size()>0) {
				log.info("Deleted the following [" + deleted_apps.size() + "] apps:");
				for(Application app: deleted_apps)
					log.info("    " + app);
			}

			return new ResponseEntity<List<Application>>(deleted_apps, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<Application>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
//	/**
//	 * Re-creates the {@link Application} with a given GAV.
//	 * @param digest
//	 * @return 404 {@link HttpStatus#NOT_FOUND} if bug with given digest does not exist,
//	 * 		   422 {@link HttpStatus.UNPROCESSABLE_ENTITY} if the value of path variable (digest) does not equal the corresponding field in the body
//	 *		   400 {@link HttpStatus.BAD_REQUEST} if the set of application dependencies is not valid (contains duplicated entries or the parent are not listed in the main set)
//	 * 		   200 {@link HttpStatus#OK} if the library was successfully re-created
//	 */
//	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/lastscan", method = RequestMethod.PUT, produces = {"application/json;charset=UTF-8"})
//	@JsonView(Views.Default.class)
//	public ResponseEntity<Application> updateApplicationLastScan(
//			@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version,
//			@RequestParam(value="skipResponseBody", required=false, defaultValue="false") Boolean skipResponseBody,
//			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {
//		Space s = null;
//		try {
//			s = this.spaceRepository.getSpace(space);
//		} catch (Exception e){
//			log.error("Error retrieving space: " + e);
//			return new ResponseEntity<Application>(HttpStatus.NOT_FOUND);
//		}
//		try{
//			final Application existing_app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup, artifact, version, s));
//			existing_app.setLastScan(Calendar.getInstance());
//			Application managed_app = appRepository.save(existing_app);
//			if(skipResponseBody){
//				return new ResponseEntity<Application>(HttpStatus.OK);
//			}
//			else
//				return new ResponseEntity<Application>(managed_app, HttpStatus.OK);
//		} catch (EntityNotFoundException e) {
//			return new ResponseEntity<Application>(HttpStatus.NOT_FOUND);
//		} catch (PersistenceException e) {
//			return new ResponseEntity<Application>(HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}

	/**
	 * Re-creates the {@link Application} with a given GAV.
	 * @param digest
	 * @return 404 {@link HttpStatus#NOT_FOUND} if bug with given digest does not exist,
	 * 		   422 {@link HttpStatus.UNPROCESSABLE_ENTITY} if the value of path variable (digest) does not equal the corresponding field in the body
	 *		   400 {@link HttpStatus.BAD_REQUEST} if the set of application dependencies is not valid (contains duplicated entries or the parent are not listed in the main set)
	 * 		   200 {@link HttpStatus#OK} if the library was successfully re-created
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}", method = RequestMethod.PUT, consumes = {"application/json;charset=UTF-8"}, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	public ResponseEntity<Application> updateApplication(
			@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version,
			@RequestBody Application application,
			@RequestParam(value="skipResponseBody", required=false, defaultValue="false") Boolean skipResponseBody,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
			if(s.isReadOnly())
				return new ResponseEntity<Application>(HttpStatus.BAD_REQUEST);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<Application>(HttpStatus.NOT_FOUND);
		}

		if(!DependencyUtil.isValidDependencyCollection(application))
			return new ResponseEntity<Application>(HttpStatus.BAD_REQUEST);
		
		try {
			//TODO: Ensure consistency of path variable and JSON content Check whether 
			/*final String group = _app.getMvnGroup();
			final String artifact = _app.getArtifact();
			final String version = _app.getVersion();*/

			// Check whether it exists
			//TODO: Checking whether the application exists is also done inside customSave
			// to avoid performing the query twice we could propagate the EntityNotFoundException from customSave )
			final Application existing_app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup, artifact, version, s));

			// Remove all deps and app constructs
			//existing_app.clean();
			//this.appRepository.customSave(existing_app);

			//TODO: Is this the best way to pass the space to customSave?
			application.setSpace(s);

			// Save
			final Application managed_app = this.appRepository.customSave(application);
			if(skipResponseBody){
				return new ResponseEntity<Application>(HttpStatus.OK);
			}
			else
				return new ResponseEntity<Application>(managed_app, HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<Application>(HttpStatus.NOT_FOUND);
		} catch (PersistenceException e) {
			return new ResponseEntity<Application>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * @return sorted set of all {@link Application}s of the respective tenant and space 
	 */
	@RequestMapping(value = "", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	public ResponseEntity<Collection<Application>> getApplications(
			@RequestParam(value="skipEmpty", required=false, defaultValue="false") Boolean skipEmpty,
//			@RequestParam(value="includeVulnerableFlag", required=false, defaultValue="false") Boolean includeVulnerableFlag,
			@RequestParam(value="group", required=false, defaultValue="*") String g,
			@RequestParam(value="artifact", required=false, defaultValue="*") String a,
			@RequestParam(value="version", required=false, defaultValue="*") String v,
			@RequestParam(value="asOf", required=false, defaultValue="0") String   asOfTimestamp,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER,  required=false)  String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<Collection<Application>>(HttpStatus.NOT_FOUND);
		}	

		try{
			if(g.equals("*") && a.equals("*") && v.equals("*")) {
				Collection<Application> results =  this.appRepository.getApplications(skipEmpty, s.getSpaceToken(), Long.parseLong(asOfTimestamp));
//				if(includeVulnerableFlag){
//					if(s.isDefault())
//						return new ResponseEntity<Collection<Application>>(HttpStatus.BAD_REQUEST);
//					results = new ArrayList<Application>();
//					for(Application app : all){
//						if (this.appVulDepRepository.isAppVulnerableCC(space,app.getMvnGroup(),app.getArtifact(),app.getVersion())){
//							app.setHasVulnerabilities(true);
//						}
//						else if (this.appVulDepRepository.isAppVulnerableConfig(space,app.getMvnGroup(),app.getArtifact(),app.getVersion())){
//							app.setHasVulnerabilities(true);
//						}
//						else
//							app.setHasVulnerabilities(false);
//						results.add(app);
//					}
//				}
//				else {
//					results = all;
//				}
				return new ResponseEntity<Collection<Application>>(results, HttpStatus.OK);
			}else {
				String search_string_g = g.replace('*', '%');
				String search_string_a = a.replace('*', '%');
				String search_string_v = v.replace('*', '%');

				// Perform the search
				Collection<Application> search = appRepository.searchByGAV(search_string_g, search_string_a, search_string_v);
				Collection<Application> result = null;
				if(skipEmpty){
					result = new ArrayList<Application>();
					//TODO 16-03-2018: this search was implemented in refactoring2 to enable the pull from SVM. To check whther and how it works with spaces
					Collection<Application> apps = this.appRepository.getApplications(skipEmpty, s.getSpaceToken(), Long.parseLong(asOfTimestamp));
					for(Application f:search)
						if(apps.contains(f))
							result.add(f);
				}
				else
					result = search;
				return new ResponseEntity<Collection<Application>>(result, HttpStatus.OK);

			}
		} catch(EntityNotFoundException e){
			return new ResponseEntity<Collection<Application>>(HttpStatus.NOT_FOUND);
		}

	}
	
	/**
	 * Compiles a list of all {@link Application}s of the respective {@link Tenant}, which is either sent by email (as attachment) or returned as part of the HTTP response.
	 * 
	 * @return
	 */
	@RequestMapping(value = "/export", method = RequestMethod.GET)
	public void exportApplications(
			@RequestParam(value="separator", required=false, defaultValue=";") final String separator, 
			@RequestParam(value="includeSpaceProperties", required=false, defaultValue="") final String[] includeSpaceProperties, 
			@RequestParam(value="includeGoalConfiguration", required=false, defaultValue="") final String[] includeGoalConfiguration,
			@RequestParam(value="includeGoalSystemInfo", required=false, defaultValue="") final String[] includeGoalSystemInfo,
			@RequestParam(value="vuln", required=false, defaultValue="") final String[] vuln,
			@RequestParam(value="to", required=false, defaultValue="") final String[] to,
			@RequestParam(value="format", required=false, defaultValue="csv") final String format,
			@RequestHeader(value=Constants.HTTP_TENANT_HEADER, required=false) final String tenant,
			HttpServletRequest request,
			HttpServletResponse response) {

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
		
		// Export format
		final ExportFormat exp_format = ExportFormat.parseFormat(format, ExportFormat.CSV);
		
		// Send export per email
		if(to!=null && to.length>0) {
			try {			
				final String req = request.getQueryString(); 
				
				// Build mesage
				final Message msg = new Message();
				msg.setSender(VulasConfiguration.getGlobal().getConfiguration().getString(SENDER_EMAIL));
				msg.setSubject(VulasConfiguration.getGlobal().getConfiguration().getString(ALL_APPS_CSV_SUBJECT));
				msg.setBody(req);
				for(String recipient: to)
					msg.addRecipient(recipient);
				
				// Write apps to CSV and send email (async)
				this.appExporter.produceExportAsync(t, null, separator, includeSpaceProperties, includeGoalConfiguration, includeGoalSystemInfo, vuln, false, false, exp_format, msg);
								
				// Short response				
				response.setContentType(ExportFormat.TXT_PLAIN);      
				final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
				writer.write("Result of request [" + req + "] will be sent to [" + StringUtil.join(to, ", ") + "]");
				writer.newLine();
				writer.flush();
				response.flushBuffer();
			} catch (IllegalStateException e) {
				log.error(e.getMessage());
				throw new RuntimeException(e.getMessage());
			} catch (FileNotFoundException e) {
				log.error("Error while reading all tenant apps (as [" + exp_format + "]): " + e.getMessage(), e);
				throw new RuntimeException("IOError writing file to output stream");
			} catch (IOException e) {
				log.error("Error while reading all tenant apps (as [" + exp_format + "]): " + e.getMessage(), e);
				throw new RuntimeException("IOError writing file to output stream");
			} catch (Exception e) {
				log.error("Error while reading all tenant apps (as [" + exp_format + "]): " + e.getMessage(), e);
				throw new RuntimeException("IOError writing file to output stream");
			}
		}
		// Write to response
		else {
			try {
				// Write apps to CSV
				final java.nio.file.Path csv = this.appExporter.produceExport(t, null, separator, includeSpaceProperties, includeGoalConfiguration, includeGoalSystemInfo, vuln, false, false, exp_format);
				
				// Headers
				response.setContentType(ExportFormat.getHttpContentType(exp_format));      
				response.setHeader("Content-Disposition", "attachment; filename=" + csv.getFileName().toString()); 
				final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csv.toFile())));
				String line = null;
				final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
				while( (line=reader.readLine())!=null ) {
					writer.write(line);
					writer.newLine();
					writer.flush();
				}

				// Finish up
				reader.close();
				writer.flush();
				response.flushBuffer();
			} catch (FileNotFoundException e) {
				log.error("Error while reading all tenant apps (as [" + exp_format + "]): " + e.getMessage(), e);
				throw new RuntimeException("IOError writing file to output stream");
			} catch (IOException e) {
				log.error("Error while reading all tenant apps (as [" + exp_format + "]): " + e.getMessage(), e);
				throw new RuntimeException("IOError writing file to output stream");
			} catch (Exception e) {
				log.error("Error while reading all tenant apps (as [" + exp_format + "]): " + e.getMessage(), e);
				throw new RuntimeException("IOError writing file to output stream");
			}
		}
	}

	/**
	 * Cleans an {@link Application} so that it does not have any of the following:
	 * {@link ConstructId}s, {@link Dependency}s, {@link Trace}s, to be continued
	 * 
	 * @param groupId ArtifactId VersionId 
	 * @return 404 {@link HttpStatus#NOT_FOUND} if application with given GAV does not exist, 200 {@link HttpStatus#OK} if the application is found
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
	public ResponseEntity<Application> cleanApplication(@PathVariable String mvnGroup, 
			@PathVariable String artifact, @PathVariable String version,
			@RequestParam(value="clean", required=true) Boolean clean,
			@RequestParam(value="cleanGoalHistory", required=false, defaultValue="false") Boolean cleanGoalHistory,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
			if(s.isReadOnly())
				return new ResponseEntity<Application>(HttpStatus.BAD_REQUEST);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<Application>(HttpStatus.NOT_FOUND);
		}
		
		// Ensure that app exists
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<Application>(HttpStatus.NOT_FOUND); }

		if(clean) {
			// Delete traces, paths and goal history
			this.appRepository.deleteAnalysisResults(app, cleanGoalHistory);

			// Remove all deps and app constructs
			app.clean();
			this.appRepository.customSave(app);
		}
		else {
			log.warn("Parameter [clean] is set to [false], application " + app + " will not be cleaned");
		}

		return new ResponseEntity<Application>(app, HttpStatus.OK);
	}

	/**
	 * Searches for {@link ConstructId}s in all of the {@link Application}'s dependencies.
	 * @param groupId ArtifactId VersionId 
	 * @return 404 {@link HttpStatus#NOT_FOUND} if application with given GAV does not exist, 200 {@link HttpStatus#OK} if the application is found
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/search", method = RequestMethod.GET)
	@JsonView(Views.Default.class)
	public ResponseEntity<Set<ConstructSearchResult>> searchConstructsInAppDependencies(@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version,
			@RequestParam(value="searchString", required=true, defaultValue="") String searchString,
			@RequestParam(value="constructTypes", required=false, defaultValue="") ConstructType[] constructTypes,
			@RequestParam(value="wildcardSearch", required=false, defaultValue="true") boolean wildcardSearch,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<Set<ConstructSearchResult>>(HttpStatus.NOT_FOUND);
		}

		Application app = null;
		try {
			// Ensure that app exists
			try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
			catch (EntityNotFoundException e) { return new ResponseEntity<Set<ConstructSearchResult>>(HttpStatus.NOT_FOUND); }

			// Search string must be at least 4 chars long
			if(searchString==null || searchString.length()<=3) {
				log.error("Search string must be at least 3 characters long"); 
				return new ResponseEntity<Set<ConstructSearchResult>>(HttpStatus.BAD_REQUEST);
			}

			// Types
			ConstructType[] types = null;
			if(constructTypes==null || constructTypes.length==0) {
				types = ConstructType.getAllAsArray();
			} else {
				types = constructTypes;
			}

			String search_string = searchString;
			// Wildcard search
			if(wildcardSearch)
				search_string = search_string.replace('*', '%');

			// Perform the search
			Set<ConstructSearchResult> result = appRepository.searchDepConstructs(app.getMvnGroup(), app.getArtifact(), app.getVersion(), app.getSpace(), types, search_string);

			return new ResponseEntity<Set<ConstructSearchResult>>(result, HttpStatus.OK);
		}
		catch(EntityNotFoundException enfe) {
			return new ResponseEntity<Set<ConstructSearchResult>>(HttpStatus.NOT_FOUND);
		}
		catch(Exception e) {
			log.info("Error while searching for constructs in dependencies of app " + app + ": " + e.getMessage());
			return new ResponseEntity<Set<ConstructSearchResult>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 
	 * @param groupId ArtifactId VersionId 
	 * @return 404 {@link HttpStatus#NOT_FOUND} if application with given GAV does not exist, 200 {@link HttpStatus#OK} if the application is found
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}", method = RequestMethod.OPTIONS)
	public ResponseEntity<Application> isApplicationExisting(
			@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<Application>(HttpStatus.NOT_FOUND);
		}

		try {
			ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup, artifact, version, s));
			return new ResponseEntity<Application>(HttpStatus.OK);
		}
		catch(EntityNotFoundException enfe) {
			return new ResponseEntity<Application>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Returns the {@link Application} with the given Group Artifact Version (GAV). 
	 * @param groupId ArtifactId VersionId 
	 * @return 404 {@link HttpStatus#NOT_FOUND} if application with given GAV does not exist, 200 {@link HttpStatus#OK} if the application is found
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.CountDetails.class)
	public ResponseEntity<Application> getApplication(@PathVariable String mvnGroup, @PathVariable String artifact,
			@PathVariable String version, 
			@RequestParam(value="inclTraces", required=false, defaultValue="true") Boolean inclTraces,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER,  required=false)  String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<Application>(HttpStatus.NOT_FOUND);
		}	
		try {
			final Application app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s));

			//TODO: Not including the traces does not significantly improve the perf, what else?
			if(inclTraces) {
				final List<Trace> traces = this.traceRepository.findByApp(app);
				if(!traces.isEmpty())
					app.setTraces(traces);
				for(Dependency dep: app.getDependencies()){
					dep.setTraces(this.traceRepository.findTracesOfLibrary(app, dep.getLib()));
				}
			}

			return new ResponseEntity<Application>(app, HttpStatus.OK);
		}
		catch(EntityNotFoundException enfe) {
			return new ResponseEntity<Application>(HttpStatus.NOT_FOUND);
		}

	}

	/**
	 * 
	 * @param 
	 * @return 
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/constructIds", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	public ResponseEntity<Collection<ConstructId>> getApplicationConstructIds(@PathVariable String mvnGroup, 
			@PathVariable String artifact, @PathVariable String version,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<Collection<ConstructId>>(HttpStatus.NOT_FOUND);
		}
		// Ensure that app exists
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<Collection<ConstructId>>(HttpStatus.NOT_FOUND); }

		// Return
		return new ResponseEntity<Collection<ConstructId>>(app.getConstructs(), HttpStatus.OK);
	}

	/**
	 * 
	 * @param 
	 * @return 404 {@link HttpStatus#NOT_FOUND} if application group artifact version of the goal execution is do not exist, 201 {@link HttpStatus#CREATED} if the bug was successfully created
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/goals", method = RequestMethod.POST, consumes = {"application/json;charset=UTF-8"}, produces = {"application/json;charset=UTF-8"})
	public ResponseEntity<GoalExecution> createGoalExecution(@PathVariable String mvnGroup, 
			@PathVariable String artifact, @PathVariable String version, 
			@RequestBody GoalExecution goalExecution,
			@RequestParam(value="skipResponseBody", required=false, defaultValue="false") Boolean skipResponseBody,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
			if(s.isReadOnly())
				return new ResponseEntity<GoalExecution>(HttpStatus.BAD_REQUEST);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<GoalExecution>(HttpStatus.NOT_FOUND);
		}
		// Ensure that app exists
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<GoalExecution>(HttpStatus.NOT_FOUND); }

		// Ensure consistency of path variable and JSON content
		//if(!app.equalsIgnoringSpace(goalExecution.getApp()))
		//	return new ResponseEntity<GoalExecution>(HttpStatus.UNPROCESSABLE_ENTITY);

		// Does it already exist (maybe upload is called multiple times on the same files)
		GoalExecution gexe = null;
		try {
			gexe = GoalExecutionRepository.FILTER.findOne(this.gexeRepository.findByAppGoalStartedAtClient(app, goalExecution.getGoal(), goalExecution.getStartedAtClient())); }
		catch (EntityNotFoundException e) {
			gexe = this.gexeRepository.customSave(app, goalExecution);
		}

		// Save and return
		if(skipResponseBody)
			return new ResponseEntity<GoalExecution>(HttpStatus.CREATED);
		else
			return new ResponseEntity<GoalExecution>(gexe, HttpStatus.CREATED);
	}
	
	/**
	 * 
	 * @param 
	 * @return 404 {@link HttpStatus#NOT_FOUND} if application group artifact version of the goal execution is do not exist, 200 {@link HttpStatus#OK} if the goal execution was successfully updates
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/goals/{executionId}", method = RequestMethod.PUT, consumes = {"application/json;charset=UTF-8"}, produces = {"application/json;charset=UTF-8"})
	public ResponseEntity<GoalExecution> updateGoalExecution(@PathVariable String mvnGroup, 
			@PathVariable String artifact, @PathVariable String version, 
			@PathVariable String executionId,
			@RequestBody GoalExecution goalExecution,
			@RequestParam(value="skipResponseBody", required=false, defaultValue="false") Boolean skipResponseBody,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
			if(s.isReadOnly())
				return new ResponseEntity<GoalExecution>(HttpStatus.BAD_REQUEST);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<GoalExecution>(HttpStatus.NOT_FOUND);
		}
		// Ensure that app exists
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<GoalExecution>(HttpStatus.NOT_FOUND); }

		try {
			GoalExecutionRepository.FILTER.findOne(this.gexeRepository.findByExecutionId(executionId));
			GoalExecution managed_gexe = this.gexeRepository.customSave(app, goalExecution);
			
			if(skipResponseBody)
				return new ResponseEntity<GoalExecution>(HttpStatus.OK);
			else
				return new ResponseEntity<GoalExecution>(managed_gexe, HttpStatus.OK);
		}
		catch (EntityNotFoundException e) {
			return new ResponseEntity<GoalExecution>(HttpStatus.NOT_FOUND);
			
		}
	}
	
	/**
	 * 
	 * @param application group, artifact,version and goal executionId
	 * @return  404 {@link HttpStatus#NOT_FOUND} if application with given GAV or the given executionId do not exist, 200 {@link HttpStatus#OK} if the executionId for the given Application is found
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/goals/{executionId}", method = RequestMethod.OPTIONS)
	public ResponseEntity<GoalExecution> isGoalExecutionExisting(@PathVariable String mvnGroup, 
			@PathVariable String artifact, @PathVariable String version, 
			@PathVariable String executionId,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<GoalExecution>(HttpStatus.NOT_FOUND);
		}
		// Ensure that app exists
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<GoalExecution>(HttpStatus.NOT_FOUND); }

		try {
			GoalExecutionRepository.FILTER.findOne(this.gexeRepository.findByExecutionId(executionId));
			return new ResponseEntity<GoalExecution>(HttpStatus.OK);
		}
		catch(EntityNotFoundException enfe) {
			return new ResponseEntity<GoalExecution>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Returns the {@link GoalExceution} for the given {@link Application} and having the given identifier.
	 * 
	 * @param 
	 * @return
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/goals/{id}", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.GoalDetails.class)
	public ResponseEntity<GoalExecution> getGoalExecution(@PathVariable String mvnGroup, 
			@PathVariable String artifact, @PathVariable String version, @PathVariable Long id,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<GoalExecution>(HttpStatus.NOT_FOUND);
		}
		// Ensure that app exists
		try { final Application app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<GoalExecution>(HttpStatus.NOT_FOUND); }

		// Ensure that goal execution exists
		final GoalExecution gexe = this.gexeRepository.findById(id).orElse(null);
		if(gexe!=null)
			return new ResponseEntity<GoalExecution>(HttpStatus.NOT_FOUND);

		return new ResponseEntity<GoalExecution>(gexe, HttpStatus.OK);
	}
	
	/**
	 * Returns the latest {@link GoalExceution} for the given {@link Application} and having the given {@link GoalType}.
	 * 
	 * @param 
	 * @return
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/goals/latest", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.GoalDetails.class)
	public ResponseEntity<GoalExecution> getLatestGoalExecution(@PathVariable String mvnGroup, 
			@PathVariable String artifact, @PathVariable String version,
			@RequestParam(value="type", required=false, defaultValue="") String type,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<GoalExecution>(HttpStatus.NOT_FOUND);
		}
		
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<GoalExecution>(HttpStatus.NOT_FOUND); }

		GoalType gt = null;
		try { gt = (type==null || type.equals("") ? null : GoalType.parseGoal(type)); }
		catch (IllegalArgumentException e) {
			log.error("Illegal goal type: " + type);
			return new ResponseEntity<GoalExecution>(HttpStatus.BAD_REQUEST);
		}
		
		final GoalExecution gexe = this.gexeRepository.findLatestGoalExecution(app, gt);
		if(gexe==null)
			return new ResponseEntity<GoalExecution>(HttpStatus.NOT_FOUND);

		return new ResponseEntity<GoalExecution>(gexe, HttpStatus.OK);
	}

	/**
	 * 
	 * @param 
	 * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/goals", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	public ResponseEntity<List<GoalExecution>> getGoalExecutions(@PathVariable String mvnGroup, 
			@PathVariable String artifact, @PathVariable String version,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<List<GoalExecution>>(HttpStatus.NOT_FOUND);
		}
		// Ensure that app exists
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<List<GoalExecution>>(HttpStatus.NOT_FOUND); }

		// Save and return
		return new ResponseEntity<List<GoalExecution>>(this.gexeRepository.findByApp(app), HttpStatus.OK);
	}

	/**
	 * 
	 * @param
	 * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/goals", method = RequestMethod.DELETE)
	public ResponseEntity<List<GoalExecution>> deleteGoalExecutions(@PathVariable String mvnGroup, 
			@PathVariable String artifact, @PathVariable String version,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
			if(s.isReadOnly())
				return new ResponseEntity<List<GoalExecution>>(HttpStatus.BAD_REQUEST);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<List<GoalExecution>>(HttpStatus.NOT_FOUND);
		}
		// Ensure that app exists
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<List<GoalExecution>>(HttpStatus.NOT_FOUND); }

		// Save and return
		List<GoalExecution> gexes = this.gexeRepository.findByApp(app);
		for(GoalExecution gexe: gexes) {
			try {
				this.gexeRepository.delete(gexe);
			} catch (Exception e) {
				log.info("Error while deleting goal execution " + gexe + ": " + e.getMessage());
			}
		}
		return new ResponseEntity<List<GoalExecution>>(HttpStatus.OK);
	}
	/**
	 * Returns a collection of {@link Bug}s relevant for the {@link Application} with the given GAV.
	 * @param group/artifact/version
	 * @return 404 {@link HttpStatus#NOT_FOUND} if application with given GAV does not exist, 200 {@link HttpStatus#OK} if the application is found
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/bugs", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.BugDetails.class)
	public ResponseEntity<List<Bug>> getApplicationBugs(@PathVariable String mvnGroup, @PathVariable String artifact, 
			@PathVariable String version,
			@RequestParam(value="historical", required=false, defaultValue="false") Boolean historical,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<List<Bug>>(HttpStatus.NOT_FOUND);
		}
		try {
			// To throw an exception if the entity is not found
			ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s));
			if (historical == true)
				return new ResponseEntity<List<Bug>>(this.appRepository.findBugsByGAV(mvnGroup,artifact,version,s), HttpStatus.OK);
			else {
				List<Bug> bugs = new ArrayList<Bug>() ;
				TreeSet<VulnerableDependency> vd_list = this.appRepository.findJPQLVulnerableDependenciesByGAV(mvnGroup,artifact,version,s);
				this.affLibRepository.computeAffectedLib(vd_list);
				for (VulnerableDependency vd : vd_list){
					if(vd.getAffectedVersion()==1)
						bugs.add(vd.getBug());
				}
				return new ResponseEntity<List<Bug>>(bugs, HttpStatus.OK);
			}

		}
		catch(EntityNotFoundException enfe) {
			return new ResponseEntity<List<Bug>>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Returns a collection of {@link DependencyIntersection}s for the {@link Application} with the given GAV.
	 * @param group/artifact/version
	 * @return 404 {@link HttpStatus#NOT_FOUND} if application with given GAV does not exist, 200 {@link HttpStatus#OK} if the application is found
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/deps/intersect", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	public ResponseEntity<List<DependencyIntersection>> findDependencyIntersections(@PathVariable String mvnGroup, 
			@PathVariable String artifact, @PathVariable String version,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<List<DependencyIntersection>>(HttpStatus.NOT_FOUND);
		}
		try {
			final StopWatch sw = new StopWatch("Find dependency intersections for application: " + mvnGroup + ":" + artifact + ":" + version).start();

			// Throws an exception if the entity is not found
			final Application a = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s));

			final List<DependencyIntersection> is = this.depRepository.findDepIntersections(a.getMvnGroup(), a.getArtifact(), a.getVersion(),s);

			sw.stop();
			return new ResponseEntity<List<DependencyIntersection>>(is, HttpStatus.OK);
		}
		catch(EntityNotFoundException enfe) {
			return new ResponseEntity<List<DependencyIntersection>>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Returns a collection of {@link Dependency} for the {@link Application} with the given GAV.
	 * @param group/artifact/version
	 * @return 404 {@link HttpStatus#NOT_FOUND} if application with given GAV does not exist, 200 {@link HttpStatus#OK} if the application is found
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/deps", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	public ResponseEntity<List<Dependency>> getDependencies(@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<List<Dependency>>(HttpStatus.NOT_FOUND);
		}
		try {
			final StopWatch sw = new StopWatch("Query dependencies for application: " + mvnGroup + ":" + artifact + ":" + version).start();

			final List<Dependency> deps = depRepository.findByGAV(mvnGroup,artifact,version,s);

			for(Dependency dep: deps){
				dep.setTotalTracedExecConstructCount(this.traceRepository.countTracesOfExecConstructLibrary(dep.getApp(), dep.getLib().getDigest()));
				dep.setTotalReachExecConstructCount(this.cidRepository.countReachableExecConstructsLibrary(dep));
			}
			sw.stop();
			return new ResponseEntity<List<Dependency>>(deps, HttpStatus.OK);
		}
		catch(EntityNotFoundException enfe) {
			return new ResponseEntity<List<Dependency>>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Returns a collection of {@link Bug}s relevant for the {@link Application} with the given GAV.
	 * @param group/artifact/version
	 * @return 404 {@link HttpStatus#NOT_FOUND} if application with given GAV does not exist, 200 {@link HttpStatus#OK} if the application is found
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/deps/{digest}", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.DepDetails.class) // extends View LibDetails that allows to see the properties
	public ResponseEntity<Dependency> getDependency(@PathVariable String mvnGroup, @PathVariable String artifact, 
			@PathVariable String version, @PathVariable String digest,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<Dependency>(HttpStatus.NOT_FOUND);
		}
		try {
			// Throws an exception if the entity is not found
			final Application a = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s));

			final Dependency dep = a.getDependency(digest);
			if(dep==null) {
				log.error("App " + a.toString() + " has no dependency with digest [" + digest + "]: No bugs will be returned");
				return new ResponseEntity<Dependency>(HttpStatus.NOT_FOUND);
			}
			dep.setTraces(this.traceRepository.findTracesOfLibrary(a, dep.getLib()));

			return new ResponseEntity<Dependency>(dep, HttpStatus.OK);
		}
		catch(EntityNotFoundException enfe) {
			return new ResponseEntity<Dependency>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Provides application-specific metrics regarding application size and the size of all application dependencies.
	 * Requires that the given dependency has a valid library Id.
	 * @param group/artifact/version
	 * @return 404 {@link HttpStatus#NOT_FOUND} if application with given GAV does not exist, 200 {@link HttpStatus#OK} if the application is found
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/metrics", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	public ResponseEntity<Metrics> getApplicationMetrics(
			@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version,
			@RequestParam(value="excludedScopes", required=false, defaultValue="") Scope[] excludedScopes,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<Metrics>(HttpStatus.NOT_FOUND);
		}
		try {
			// Throws an exception if the entity is not found
			final Application a = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s));
			final ConstructIdFilter af = a.countConstructTypes();

			final Metrics metrics = new Metrics();

			// Packages
			final Ratio packages = new Ratio("package_ratio", af.countPack(), af.countPack());
			int count_packages;
			if(excludedScopes==null || excludedScopes.length==0)
				count_packages = this.appRepository.countDepConstructTypes(a.getMvnGroup(), a.getArtifact(), a.getVersion(), a .getSpace(), new ConstructType[] { ConstructType.PACK });
			else
				count_packages = this.appRepository.countDepConstructTypes(a.getMvnGroup(), a.getArtifact(), a.getVersion(), a .getSpace(), new ConstructType[] { ConstructType.PACK }, excludedScopes);
			packages.incrementTotal(count_packages);
			metrics.addRatio(packages);

			// Classes
			final Ratio classes = new Ratio("class_ratio", af.countClass(), af.countClass());
			int count_classes;
			if(excludedScopes==null || excludedScopes.length==0)
				count_classes = this.appRepository.countDepConstructTypes(a.getMvnGroup(), a.getArtifact(), a.getVersion(),a .getSpace(), new ConstructType[] { ConstructType.CLAS });
			else
				count_classes = this.appRepository.countDepConstructTypes(a.getMvnGroup(), a.getArtifact(), a.getVersion(), a .getSpace(), new ConstructType[] { ConstructType.CLAS }, excludedScopes);
			classes.incrementTotal(count_classes);
			metrics.addRatio(classes);

			// Executable constructs (METH, CONS, INIT)
			final Ratio execs = new Ratio("executable_ratio", af.countExecutable(), af.countExecutable());
			int count_execs;
			if(excludedScopes==null || excludedScopes.length==0)
				count_execs = this.appRepository.countDepConstructTypes(a.getMvnGroup(), a.getArtifact(), a.getVersion(), a .getSpace(), new ConstructType[] { ConstructType.INIT, ConstructType.CONS, ConstructType.METH });
			else
				count_execs = this.appRepository.countDepConstructTypes(a.getMvnGroup(), a.getArtifact(), a.getVersion(), a .getSpace(), new ConstructType[] { ConstructType.INIT, ConstructType.CONS, ConstructType.METH }, excludedScopes);
			execs.incrementTotal(count_execs);
			metrics.addRatio(execs);

			// Number of app dependencies
			final Counter dep_counter = new Counter("dep_counter");
			int count_deps;
			if(excludedScopes==null || excludedScopes.length==0)
				count_deps = this.appRepository.countDependencies(a.getMvnGroup(), a.getArtifact(), a.getVersion(), a.getSpace());
			else
				count_deps = this.appRepository.countDependencies(a.getMvnGroup(), a.getArtifact(), a.getVersion(),  a.getSpace(), excludedScopes);
			dep_counter.increment(count_deps);
			metrics.addCounter(dep_counter);

			return new ResponseEntity<Metrics>(metrics, HttpStatus.OK);
		}
		catch(EntityNotFoundException enfe) {
			return new ResponseEntity<Metrics>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Provides application-specific metrics related to updating the current dependency to the provided one.
	 * Requires that the given dependency has a valid library Id.
	 * @param group/artifact/version
	 * @return 404 {@link HttpStatus#NOT_FOUND} if application with given GAV does not exist, 200 {@link HttpStatus#OK} if the application is found
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/deps/{digest}/updateMetrics", method = RequestMethod.POST, consumes = {"application/json;charset=UTF-8"}, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.DepDetails.class) // extends View LibDetails that allows to see the properties
	public ResponseEntity<com.sap.psr.vulas.backend.model.DependencyUpdate> getUpdateMetrics(@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version, @PathVariable String digest, @RequestBody LibraryId otherVersion,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<com.sap.psr.vulas.backend.model.DependencyUpdate>(HttpStatus.NOT_FOUND);
		}

		try {

			// To throw an exception if the entity is not found
			final Application a = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s));

			final Dependency dep = a.getDependency(digest);
			if(dep==null) {
				log.error("App " + a.toString() + " has no dependency with digest [" + digest + "]: No update metrics will be returned");
				return new ResponseEntity<com.sap.psr.vulas.backend.model.DependencyUpdate>(HttpStatus.NOT_FOUND);
			}

			// Pre-requisite: The dependency has to have a library id known to Maven
			if(dep.getLib().getLibraryId()==null) {
				log.error("App " + a.toString() + " dependency with digest [" + digest + "] has no library id");
				return new ResponseEntity<com.sap.psr.vulas.backend.model.DependencyUpdate>(HttpStatus.BAD_REQUEST);
			}

			dep.setTraces(this.traceRepository.findTracesOfLibrary(a, dep.getLib()));
			final JarDiffResult jdr = ServiceWrapper.getInstance().diffJars(dep.getLib().getLibraryId().toSharedType(), otherVersion.toSharedType());
			final com.sap.psr.vulas.shared.json.model.metrics.Metrics metrics = new com.sap.psr.vulas.shared.json.model.metrics.Metrics();
			final DependencyUpdate depUpdate = new DependencyUpdate(dep.getLib().getLibraryId(),otherVersion);
			//			final com.sap.psr.vulas.backend.model.DependencyUpdate depUpdate = new com.sap.psr.vulas.backend.model.DependencyUpdate(
			//																							new com.sap.psr.vulas.shared.json.model.LibraryId(dep.getLib().getLibraryId().getMvnGroup(),dep.getLib().getLibraryId().getArtifact(),dep.getLib().getLibraryId().getVersion()),
			//																									new com.sap.psr.vulas.shared.json.model.LibraryId(otherVersion.getMvnGroup(),otherVersion.getArtifact(),otherVersion.getVersion()));
			//			

			// Metrics related to touch points (from traces or static analysis)
			final Collection<TouchPoint> touch_points = dep.getTouchPoints();

			if(touch_points!=null) {

				final Ratio ratio_callees_deleted = new Ratio("callees_deleted");
				final Ratio api_stability = new Ratio("callee_stability"); // 1-callees_deleted
				final Ratio ratio_callers_to_modify = new Ratio("calls_to_modify");

				Set<ConstructId> callees = new HashSet<ConstructId>();
				Set<String> calls = new HashSet<String>();
				Set<ConstructId> deleted_callees = new HashSet<ConstructId>();


				for(TouchPoint tp: touch_points) {
					// Only A2L is relevant, as only this direction requires modification at app side
					if(tp.getDirection()==TouchPoint.Direction.A2L) {
						callees.add(tp.getTo());
						if(!calls.contains(tp.getFrom().toString().concat(tp.getTo().toString()))){
							calls.add(tp.getFrom().toString().concat(tp.getTo().toString()));
							ratio_callers_to_modify.incrementTotal();
							if(jdr.isDeleted(tp.getTo().toSharedType())) {
								deleted_callees.add(tp.getTo());
								ratio_callers_to_modify.incrementCount();
								log.info("Touch point callee " + tp.getTo().toString() + " deleted from " + dep.getLib().getLibraryId().toString() + " to " + otherVersion);
							}
						}
					}
				}

				ratio_callees_deleted.setCount(deleted_callees.size());
				ratio_callees_deleted.setTotal(callees.size());

				api_stability.setCount(callees.size()-deleted_callees.size());
				api_stability.setTotal(callees.size());

				metrics.addRatio(api_stability);
				metrics.addRatio(ratio_callees_deleted);
				metrics.addRatio(ratio_callers_to_modify);

			}

			// Metrics related to reachable and traced constructs
			final Set<ConstructId> reachable_cids = new HashSet<ConstructId>();
			reachable_cids.addAll(dep.getReachableConstructIds());
			reachable_cids.addAll(dep.getTracedConstructs());
			if(reachable_cids!=null) {

				final Ratio reachable_deleted = new Ratio("reachable_deleted", 0, reachable_cids.size());
				final Ratio reachable_body_changed = new Ratio("reachable_body_changed", 0, reachable_cids.size());
				final Ratio reachable_body_stability = new Ratio("reachable_body_stability", 0, reachable_cids.size());

				for(ConstructId cid: reachable_cids) {
					if(jdr.isDeleted(cid.toSharedType())) {
						reachable_deleted.incrementCount();
						log.debug("Reachable construct " + cid.toString() + " deleted from " + dep.getLib().getLibraryId().toString() + " to " + otherVersion);
					}
					if(jdr.isBodyChanged(cid.toSharedType())) {
						reachable_body_changed.incrementCount();
						log.debug("Reachable construct " + cid.toString() + " changed from " + dep.getLib().getLibraryId().toString() + " to " + otherVersion);
					}
				}
				reachable_body_stability.setCount(reachable_body_changed.getTotal()-reachable_body_changed.getCount());
				reachable_body_stability.setTotal(reachable_body_changed.getTotal());
				metrics.addRatio(reachable_body_stability);
				metrics.addRatio(reachable_deleted);
				metrics.addRatio(reachable_body_changed);
			}

			// App-independent metrics
			final List<ConstructId> old_constructs = this.libRepository.findConstructIds(digest);
			final Ratio ratio_constructs_body_stability = new Ratio("jar_constructs_body_stability");
			final Ratio ratio_constructs_deleted = new Ratio("jar_constructs_deleted");
			final Ratio ratio_constructs_body_changed = new Ratio("jar_constructs_body_changed");
			for(ConstructId c: old_constructs) {
				// Only consider METH and CONS
				if(c.getType().equals(ConstructType.CONS) || c.getType().equals(ConstructType.METH)) {
					ratio_constructs_deleted.incrementTotal();
					ratio_constructs_body_stability.incrementTotal();
					if(jdr.isDeleted(c.toSharedType())) {
						ratio_constructs_deleted.incrementCount();
						log.debug("Construct " + c.toString() + " deleted from " + dep.getLib().getLibraryId().toString() + " to " + otherVersion);
					}
					else {
						ratio_constructs_body_changed.incrementTotal();
						if(jdr.isBodyChanged(c.toSharedType())) {
							ratio_constructs_body_changed.incrementCount();
							log.debug("Construct " + c.toString() + " changed from " + dep.getLib().getLibraryId().toString() + " to " + otherVersion);
						} else {
							ratio_constructs_body_stability.incrementCount();
						}
					}
				}
			}
			metrics.addRatio(ratio_constructs_body_stability);
			metrics.addRatio(ratio_constructs_deleted);
			metrics.addRatio(ratio_constructs_body_changed);

			depUpdate.setMetrics(metrics);

			return new ResponseEntity<com.sap.psr.vulas.backend.model.DependencyUpdate>(depUpdate, HttpStatus.OK);
		}
		catch(ServiceConnectionException sce) {
			return new ResponseEntity<com.sap.psr.vulas.backend.model.DependencyUpdate>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		catch(EntityNotFoundException enfe) {
			return new ResponseEntity<com.sap.psr.vulas.backend.model.DependencyUpdate>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Provides application-specific changes required to update the current dependency to the provided one.
	 * Requires that the given dependency has a valid library Id.
	 * @param group/artifact/version
	 * @return 404 {@link HttpStatus#NOT_FOUND} if application with given GAV does not exist, 200 {@link HttpStatus#OK} if the application is found
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/deps/{digest}/updateChanges", method = RequestMethod.POST, consumes = {"application/json;charset=UTF-8"}, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.DepDetails.class) // extends View LibDetails that allows to see the properties
	public ResponseEntity<com.sap.psr.vulas.backend.model.DependencyUpdate> getUpdateChanges(@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version, @PathVariable String digest, @RequestBody LibraryId otherVersion,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<com.sap.psr.vulas.backend.model.DependencyUpdate>(HttpStatus.NOT_FOUND);
		}
		try {

			// To throw an exception if the entity is not found
			final Application a = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s));

			final Dependency dep = a.getDependency(digest);
			if(dep==null) {
				log.error("App " + a.toString() + " has no dependency with digest [" + digest + "]: No update changes will be returned");
				return new ResponseEntity<com.sap.psr.vulas.backend.model.DependencyUpdate>(HttpStatus.NOT_FOUND);
			}

			// Pre-requisite: The dependency has to have a library id known to Maven
			if(dep.getLib().getLibraryId()==null) {
				log.error("App " + a.toString() + " dependency with digest [" + digest + "] has no library id");
				return new ResponseEntity<com.sap.psr.vulas.backend.model.DependencyUpdate>(HttpStatus.BAD_REQUEST);
			}

			dep.setTraces(this.traceRepository.findTracesOfLibrary(a, dep.getLib()));
			final JarDiffResult jdr = ServiceWrapper.getInstance().diffJars(dep.getLib().getLibraryId().toSharedType(), otherVersion.toSharedType());

			final DependencyUpdate depUpdate = new DependencyUpdate(dep.getLib().getLibraryId(),otherVersion);


			// Metrics related to touch points (from traces or static analysis)
			final Collection<TouchPoint> touch_points = dep.getTouchPoints();

			if(touch_points!=null) {

				Set<TouchPoint> callsToModify = new HashSet<TouchPoint>();


				for(TouchPoint tp: touch_points) {
					// Only A2L is relevant, as only this direction requires modification at app side
					if(tp.getDirection()==TouchPoint.Direction.A2L) {
						if(jdr.isDeleted(tp.getTo().toSharedType())) {
							callsToModify.add(tp);
							log.info("Touch point callee " + tp.getTo().toString() + " deleted from " + dep.getLib().getLibraryId().toString() + " to " + otherVersion);
						}
					}
				}


				depUpdate.setCallsToModify(callsToModify);

			}

			return new ResponseEntity<com.sap.psr.vulas.backend.model.DependencyUpdate>(depUpdate, HttpStatus.OK);
		}
		catch(ServiceConnectionException sce) {
			return new ResponseEntity<com.sap.psr.vulas.backend.model.DependencyUpdate>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		catch(EntityNotFoundException enfe) {
			return new ResponseEntity<com.sap.psr.vulas.backend.model.DependencyUpdate>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Returns a collection of {@link VulnerableDependency}s relevant for the {@link Application} with the given GAV.
	 * @param group/artifact/version
	 * @return 404 {@link HttpStatus#NOT_FOUND} if application with given GAV does not exist, 200 {@link HttpStatus#OK} if the application is found
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/vulndeps", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	public ResponseEntity<TreeSet<VulnerableDependency>> getAppVulnerableDependencies(
			@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version,
			@RequestParam(value="includeHistorical", required=false, defaultValue="false") Boolean historical, // affected==0
			@RequestParam(value="includeAffected", required=false, defaultValue="true") Boolean affected, // affected==1
			@RequestParam(value="includeAffectedUnconfirmed", required=false, defaultValue="true") Boolean includeAffectedUnconfirmed, // affectedConfirmed==0
			@RequestParam(value="addExcemptionInfo", required=false, defaultValue="false") Boolean addExcemptionInfo, // consider configuration setting "vulas.report.exceptionScopeBlacklist" and "vulas.report.exceptionExcludeBugs"
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<TreeSet<VulnerableDependency>>(HttpStatus.NOT_FOUND);
		}

		try {			
			// Throw an exception if the entity is not found
			final Application app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup, artifact, version, s));
			
			// All of them (no matter the scope)
			final TreeSet<VulnerableDependency> vd_all = this.appRepository.findAppVulnerableDependencies(app, addExcemptionInfo, true);
			
			// The set to be returned
			final TreeSet<VulnerableDependency> vd_list = new TreeSet<VulnerableDependency>();
			
			// Update traced and reachable flags 
			// Populate the set to be returned depending on the historical flag
			for (VulnerableDependency vd : vd_all){
				if(   (includeAffectedUnconfirmed || vd.getAffectedVersionConfirmed()==1) &&
					 ((historical && vd.getAffectedVersion()==0) || (affected && vd.getAffectedVersion()==1)) ) {
					
					// Update CVE data (if needed)
					this.bugRepository.updateCachedCveData(vd.getBug(), false);
				
					vd_list.add(vd);
				}
			}
			
			return new ResponseEntity<TreeSet<VulnerableDependency>>(vd_list, HttpStatus.OK);
		}
		catch(EntityNotFoundException enfe) {
			return new ResponseEntity<TreeSet<VulnerableDependency>>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Returns a collection of all {@link VulnerableDependency}s of all applications.
	 * @param unconfirmedOnly if true, only non-assessed {@link VulnerableDependency}s are considered
	 */
	@RequestMapping(value = "/vulndeps", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	public ResponseEntity<TreeSet<VulnerableDependency>> getVulnerableDependencies(@RequestParam(value="unconfirmedOnly", required=false, defaultValue="true") Boolean unconfirmedOnly) {
		try {
			TreeSet<VulnerableDependency> vd_list = new TreeSet<VulnerableDependency>();
			List<Entry<BigInteger,String>> dep_bug = null;
			if(unconfirmedOnly)
				dep_bug = this.appVulDepRepository.findUnconfirmedVulnDeps();
			else
				dep_bug = this.appVulDepRepository.findConfirmedVulnDeps();
			//for (int i=0; i<dep_bug.size();i++) {
				for (Entry<BigInteger,String> entry : dep_bug) {
					try{
						VulnerableDependency vd = new VulnerableDependency(this.depRepository.findById(entry.getKey().longValue()).orElse(null), BugRepository.FILTER.findOne(this.bugRepository.findByBugId(entry.getValue())));
						vd_list.add(vd);
					}
					catch(EntityNotFoundException e){
						log.warn("Could not create vulnerable dependency, entity not found :" + e.getMessage());
					}
				}
		//	}
			this.affLibRepository.computeAffectedLib(vd_list);
			this.appRepository.updateFlags(vd_list,true);
			
				
		// Old code using JPQL query		
//			TreeSet<VulnerableDependency> all_vd = this.appRepository.findJPQLVulnerableDependencies();
//			this.affLibRepository.computeAffectedLib(all_vd);
//			TreeSet<VulnerableDependency> vd_list = new TreeSet<VulnerableDependency>();
//			if(unconfirmedOnly==true){
//				for (VulnerableDependency vd : all_vd){
//					if(vd.getAffectedVersionConfirmed()==0)
//						vd_list.add(vd);
//				}
//			}
//			else
//				vd_list=all_vd;
//			this.appRepository.updateFlags(vd_list,true);
			return new ResponseEntity<TreeSet<VulnerableDependency>>(vd_list, HttpStatus.OK);

		}
		catch(EntityNotFoundException enfe) {
			return new ResponseEntity<TreeSet<VulnerableDependency>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	/**
	 * Returns a {@link VulnerableDependency}s containing the details of the bugId affecting the dependency digest for the {@link Application} with the given GAV.
	 * @param group/artifact/version
	 * @return 404 {@link HttpStatus#NOT_FOUND} if application with given GAV does not exist, 200 {@link HttpStatus#OK} if the application is found
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/vulndeps/{digest}/bugs/{bugid}", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.VulnDepDetails.class)
	public ResponseEntity<VulnerableDependency> getVulnerableDependencyBugDetails(@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version, @PathVariable String digest, @PathVariable String bugid,
			@RequestParam(value="origin", required=true, defaultValue="CC") VulnDepOrigin vulnDepOrigin,
			@RequestParam(value="bundledGroup", required=false, defaultValue="") String bundledGroup,
			@RequestParam(value="bundledArtifact", required=false, defaultValue="") String bundledArtifact,
			@RequestParam(value="bundledVersion", required=false, defaultValue="") String bundledVersion,
			@RequestParam(value="bundledLibrary", required=false, defaultValue="") String bundledLibrary,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<VulnerableDependency>(HttpStatus.NOT_FOUND);
		}
		try {
			// To throw an exception if the entity is not found
			final Application a = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s));
			
			if( (vulnDepOrigin.equals(VulnDepOrigin.BUNDLEDCC) && bundledLibrary == null) || (vulnDepOrigin.equals(VulnDepOrigin.BUNDLEDAFFLIBID) && (bundledGroup == null || bundledGroup == null || bundledVersion == null )))
				return new ResponseEntity<VulnerableDependency>(HttpStatus.BAD_REQUEST);

			return new ResponseEntity<VulnerableDependency>(appRepository.getVulnerableDependencyBugDetails(a, digest, bugid, vulnDepOrigin, bundledLibrary, bundledGroup, bundledArtifact, bundledVersion), HttpStatus.OK);
		}
		catch(EntityNotFoundException enfe) {
			return new ResponseEntity<VulnerableDependency>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * 
	 * @param 
	 * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/traces", method = RequestMethod.POST, consumes = {"application/json;charset=UTF-8"}, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	public ResponseEntity<List<Trace>> createTraces(@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version, @RequestBody Trace[] traces,@RequestParam(value="skipResponseBody", required=false, defaultValue="false") Boolean skipResponseBody,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
			if(s.isReadOnly())
				return new ResponseEntity<List<Trace>>(HttpStatus.BAD_REQUEST);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<List<Trace>>(HttpStatus.NOT_FOUND);
		}
		// Ensure that app exists
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<List<Trace>>(HttpStatus.NOT_FOUND); }

		// Ensure consistency of path variable and JSON content
		for(Trace trace: traces)
			if(!app.equalsIgnoreSpace(trace.getApp()))
				return new ResponseEntity<List<Trace>>(HttpStatus.UNPROCESSABLE_ENTITY);

		// Save and return
		if (skipResponseBody){
			this.traceRepository.customSave(app, traces);
			return new ResponseEntity<List<Trace>>(HttpStatus.OK);
		}
		else
			return new ResponseEntity<List<Trace>>(this.traceRepository.customSave(app, traces), HttpStatus.OK);
	}

	/**
	 * 
	 * @param 
	 * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/traces", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	public ResponseEntity<List<Trace>> getTraces(@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<List<Trace>>(HttpStatus.NOT_FOUND);
		}
		// Ensure that app exists
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<List<Trace>>(HttpStatus.NOT_FOUND); }

		// Save and return
		return new ResponseEntity<List<Trace>>(this.traceRepository.findByApp(app), HttpStatus.OK);
	}
	
	/**
	 * Returns a {@link Collection} of all application {@link Dependency}s including their reachable {@link ConstructId}s. 
	 * @param 
	 * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/reachableConstructIds", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.DepDetails.class)
	public ResponseEntity<Collection<Dependency>> getReachableContructIds(@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<Collection<Dependency>>(HttpStatus.NOT_FOUND);
		}
		// Ensure that app exists
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<Collection<Dependency>>(HttpStatus.NOT_FOUND); }
		
		// Save and return
		return new ResponseEntity<Collection<Dependency>>(app.getDependencies(), HttpStatus.OK);
	}

	/**
	 * 
	 * @param 
	 * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/paths", method = RequestMethod.POST, consumes = {"application/json;charset=UTF-8"}, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	public ResponseEntity<List<Path>> createPaths(@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version, @RequestBody Path[] paths, @RequestParam(value="skipResponseBody", required=false, defaultValue="false") Boolean skipResponseBody,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
			if(s.isReadOnly())
				return new ResponseEntity<List<Path>>(HttpStatus.BAD_REQUEST);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<List<Path>>(HttpStatus.NOT_FOUND);
		}
		// Ensure that app exists
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<List<Path>>(HttpStatus.NOT_FOUND); }

		// Ensure consistency of path variable and JSON content
		for(Path path: paths)
			if(!app.equalsIgnoreSpace(path.getApp()))
				return new ResponseEntity<List<Path>>(HttpStatus.UNPROCESSABLE_ENTITY);

		// Save and return
		if(skipResponseBody){
			this.pathRepository.customSave(app, paths);
			return new ResponseEntity<List<Path>>(HttpStatus.OK);
		}
		else
			return new ResponseEntity<List<Path>>(this.pathRepository.customSave(app, paths), HttpStatus.OK);
	}

	/**
	 * 
	 * @param 
	 * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/paths", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	public ResponseEntity<List<Path>> getPaths(@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<List<Path>>(HttpStatus.NOT_FOUND);
		}
		// Ensure that app exists
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<List<Path>>(HttpStatus.NOT_FOUND); }

		// Select and return
		return new ResponseEntity<List<Path>>(this.pathRepository.findPathsForApp(app), HttpStatus.OK);
	}

	/**
	 * 
	 * @param 
	 * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/deps/{digest}/paths/{bugId}", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	public ResponseEntity<List<Path>> getVulndepPaths(@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version, @PathVariable String digest, @PathVariable String bugId,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<List<Path>>(HttpStatus.NOT_FOUND);
		}
		// Ensure that app exists
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<List<Path>>(HttpStatus.NOT_FOUND); }

		// Ensure that dependency exists
		final Dependency dep = app.getDependency(digest);
		if(dep==null) {
			log.error("App " + app.toString() + " has no dependency with digest [" + digest + "]: No paths will be returned for bug [" + bugId + "]");
			return new ResponseEntity<List<Path>>(HttpStatus.NOT_FOUND);
		}

		// Ensure that bug exists
		Bug bug = null;
		try { bug = BugRepository.FILTER.findOne(this.bugRepository.findByBugId(bugId)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<List<Path>>(HttpStatus.NOT_FOUND); }

		// Select and return
		return new ResponseEntity<List<Path>>(this.pathRepository.findPathsForLibraryBug(app, dep.getLib(), bug.getId()), HttpStatus.OK);
	}

	/**
	 * 
	 * @param 
	 * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/deps/{digest}/paths/{bugId}/{qname}", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.Default.class)
	public ResponseEntity<List<Path>> getVulndepConstructPaths(@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version, @PathVariable String digest, @PathVariable String bugId, @PathVariable String qname,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<List<Path>>(HttpStatus.NOT_FOUND);
		}
		// Ensure that app exists
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<List<Path>>(HttpStatus.NOT_FOUND); }

		// Ensure that dependency exists
		final Dependency dep = app.getDependency(digest);
		if(dep==null) {
			log.error("App " + app.toString() + " has no dependency with digest [" + digest + "]: No paths will be returned for bug [" + bugId + "] and construct [" + qname + "]");
			return new ResponseEntity<List<Path>>(HttpStatus.NOT_FOUND);
		}

		// Ensure that bug exists
		Bug bug = null;
		try { bug = BugRepository.FILTER.findOne(this.bugRepository.findByBugId(bugId)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<List<Path>>(HttpStatus.NOT_FOUND); }

		//select paths
		List<Path> paths = this.pathRepository.findPathsForLibraryBugConstructName(app, dep.getLib(), bug, qname);
		//select dependencies for each path node of each path
		for(Path p : paths){
			for(PathNode pn: p.getPath()) {
				if(pn.getLib()!=null) {
					try{
						final Dependency d = DependencyRepository.FILTER.findOne(this.depRepository.findByAppAndLib(app, pn.getLib().getDigest()));
						pn.setDep(d);
					} catch(EntityNotFoundException nf) {
						log.error("Error while retrieving dependency with sha1 [" + pn.getLib().getDigest() +"] in app [" +app+ "], "
								+ "found in pathNode ["+pn.getConstructId().toString()+"]");
					}
				}

			}
		}

		// Select and return
		return new ResponseEntity<List<Path>>(paths, HttpStatus.OK);
	}

	/**
	 * 
	 * @param 
	 * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/deps/{digest}/reachableConstructIds", method = RequestMethod.POST, consumes = {"application/json;charset=UTF-8"}, produces = {"application/json;charset=UTF-8"})
	public ResponseEntity<Set<ConstructId>> createReachableConstructIds(@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version, @PathVariable String digest, @RequestBody ConstructId[] constructIds,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
			if(s.isReadOnly())
				return new ResponseEntity<Set<ConstructId>>(HttpStatus.BAD_REQUEST);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<Set<ConstructId>>(HttpStatus.NOT_FOUND);
		}
		// Ensure that app exists
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<Set<ConstructId>>(HttpStatus.NOT_FOUND); }

		// Ensure that dependency exists
		Dependency dep = app.getDependency(digest);
		if(dep==null) {
			if(constructIds==null || constructIds.length==0) {
				log.warn("App " + app.toString() + " has no dependency with digest [" + digest + "]: [" + (constructIds==null?"-":constructIds.length) + "] reachable constructs cannot be saved");
			} else {
				log.warn("App " + app.toString() + " has no dependency with digest [" + digest + "]: [" + (constructIds==null?"-":constructIds.length) + "] reachable constructs such as " + constructIds[0].toString() + " cannot be saved");
			}
			return new ResponseEntity<Set<ConstructId>>(HttpStatus.NOT_FOUND);
		}
		// Save and return
		return new ResponseEntity<Set<ConstructId>>(this.depRepository.saveReachableConstructIds(dep, constructIds), HttpStatus.OK);
	}

	/**
	 * 
	 * @param 
	 * @return 409 {@link HttpStatus#CONFLICT} if bug with given bug ID already exists, 201 {@link HttpStatus#CREATED} if the bug was successfully created
	 */
	@RequestMapping(value = "/{mvnGroup:.+}/{artifact:.+}/{version:.+}/deps/{digest}/touchPoints", method = RequestMethod.POST, consumes = {"application/json;charset=UTF-8"}, produces = {"application/json;charset=UTF-8"})
	public ResponseEntity<Set<TouchPoint>> createTouchPoints(@PathVariable String mvnGroup, @PathVariable String artifact, @PathVariable String version, @PathVariable String digest, @RequestBody TouchPoint[] touchPoints, @RequestParam(value="skipResponseBody", required=false, defaultValue="false") Boolean skipResponseBody,
			@ApiIgnore @RequestHeader(value=Constants.HTTP_SPACE_HEADER, required=false) String space) {

		Space s = null;
		try {
			s = this.spaceRepository.getSpace(space);
			if(s.isReadOnly())
				return new ResponseEntity<Set<TouchPoint>>(HttpStatus.BAD_REQUEST);
		} catch (Exception e){
			log.error("Error retrieving space: " + e);
			return new ResponseEntity<Set<TouchPoint>>(HttpStatus.NOT_FOUND);
		}
		// Ensure that app exists
		Application app = null;
		try { app = ApplicationRepository.FILTER.findOne(this.appRepository.findByGAV(mvnGroup,artifact,version,s)); }
		catch (EntityNotFoundException e) { return new ResponseEntity<Set<TouchPoint>>(HttpStatus.NOT_FOUND); }

		// Ensure that dependency exists
		Dependency dep = app.getDependency(digest);
		if(dep==null) {
			if(touchPoints==null || touchPoints.length==0) {
				log.warn("App " + app.toString() + " has no dependency with digest [" + digest + "]: [" + (touchPoints==null?"-":touchPoints.length) + "] touch points cannot be saved");
			} else {
				log.warn("App " + app.toString() + " has no dependency with digest [" + digest + "]: [" + (touchPoints==null?"-":touchPoints.length) + "] touch points such as " + touchPoints[0].toString() + " cannot be saved");
			}
			return new ResponseEntity<Set<TouchPoint>>(HttpStatus.NOT_FOUND);
		}
		// Save and return
		if(skipResponseBody){
			this.depRepository.saveTouchPoints(dep, touchPoints);
			return new ResponseEntity<Set<TouchPoint>>(HttpStatus.OK);
		}
		else
			return new ResponseEntity<Set<TouchPoint>>(this.depRepository.saveTouchPoints(dep, touchPoints), HttpStatus.OK);
	}
}
