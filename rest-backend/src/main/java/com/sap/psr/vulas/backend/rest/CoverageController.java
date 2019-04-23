package com.sap.psr.vulas.backend.rest;


import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.DispatcherServlet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.backend.repo.BugRepository;
import com.sap.psr.vulas.backend.util.ServiceWrapper;
import com.sap.psr.vulas.shared.connectivity.ServiceConnectionException;
import com.sap.psr.vulas.shared.enums.CoverageStatus;
import com.sap.psr.vulas.shared.util.StopWatch;
import com.sap.psr.vulas.shared.util.VulasConfiguration;


/**
 * RESTful interface for application information.
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path="/coverage")
public class CoverageController {

	private static Logger log = LoggerFactory.getLogger(CoverageController.class);
	
	private static final String LANG_CONF_THRESHOLD = "vulas.backend.coverageService.langConfidenceThreshold";
	private static final String LICENSE_CONF_THRESHOLD = "vulas.backend.coverageService.licenseConfidenceThreshold";
	
	public static final String BROWSE_ISSUE_URL = "vulas.shared.jira.browseIssueUrl";
	public static final String CREATE_ISSUE_URL = "vulas.shared.jira.createIssueUrl";
	public static final String PROJECT_ID = "vulas.shared.jira.projectId";
	public static final String COMPONENT_ID = "vulas.shared.jira.componentId";

	private final BugRepository bugRepository;

//	@Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
//	public DispatcherServlet dispatcherServlet() {
//		DispatcherServlet dispatcherServlet = new DispatcherServlet();
//		dispatcherServlet.setDispatchOptionsRequest(true);
//		return dispatcherServlet;
//	}

	@Autowired
	CoverageController(BugRepository bugRepository) {
		this.bugRepository = bugRepository;
	}

	/**
	 * 
	 * @param id
	 * @return 404 {@link HttpStatus#NOT_FOUND} if bug with given bug ID does not exist, 200 {@link HttpStatus#OK} if the bug is found
	 */
	@RequestMapping(value = "/{bugid}", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	@JsonView(Views.BugDetails.class)
	public ResponseEntity<CoverageStatusResponse> isCovered(@PathVariable String bugid) {
		final String bugid_uc = bugid.toUpperCase();
		final StopWatch sw = new StopWatch("Check coverage of bug [" + bugid_uc + "]").start();

		// Always get basic vulnerability info and prediction
		CveClassifierResponse classifier_response = null;
		try {
			classifier_response = ServiceWrapper.getInstance().classify(bugid_uc);
		} catch (ServiceConnectionException e1) {
			log.error("Error while calling the CVE classifier: " + e1.getMessage());
		}
		
		final String browse_issue = VulasConfiguration.getGlobal().getConfiguration().getString(BROWSE_ISSUE_URL, null);
		final String create_issue = VulasConfiguration.getGlobal().getConfiguration().getString(CREATE_ISSUE_URL, null);

		// Create response and set basic info
		final CoverageStatusResponse response = new CoverageStatusResponse(bugid_uc);
		if(classifier_response!=null)
			response.setDescription(classifier_response.getDescription());

		// Check whether the bug is already in the database
		try {
			final List<Bug> bugs = this.bugRepository.findCoverageByBugId(bugid_uc);
			sw.lap("Completed database query", true);
			final Bug b = BugRepository.FILTER.findOne(bugs);

			// It is, let's fill the response fields
			response.status = CoverageStatus.COVERED;
			response.setActionUrl(VulasConfiguration.getGlobal().getConfiguration().getString(VulasConfiguration.HOMEPAGE, "n/a"));
			response.setActionText("Learn how to detect vulnerable open-source software in your application");
		}
		catch(EntityNotFoundException enfe) {
			try {
				// It is not in the database, let's ask Jira whether there's already a ticket
				final JiraSearchResponse jira_search_response = ServiceWrapper.getInstance().searchJira(bugid_uc);
				sw.lap("Completed Jira query", true);

				// Found in Jira
				if(jira_search_response!=null && jira_search_response.getTotal()>0) {
					final JiraIssue issue = jira_search_response.getIssues()[0];

					// Set link to Jira ticket (if possible)
					if(browse_issue!=null && !browse_issue.equals("")) {
						response.setActionUrl(browse_issue + issue.getKey());
						response.setActionText("See Jira ticket " + issue.getKey() + " (status: " + issue.getFields().getStatus().getName() + ").");
					}
					
					// Out of scope
					if(issue.getFields().getStatus().getName().equals("Closed")) {
						response.status = CoverageStatus.OUT_OF_SCOPE;
					}
					// In discussion
					else {
						response.status = CoverageStatus.OPEN;
					}
				}
				// Classifier
				else if(classifier_response!=null) {					
					final double license_threshold = VulasConfiguration.getGlobal().getConfiguration().getDouble(LICENSE_CONF_THRESHOLD, 0.2);
					final double language_threshold = VulasConfiguration.getGlobal().getConfiguration().getDouble(LANG_CONF_THRESHOLD, 0.2);
					// In scope but not covered yet
					if(classifier_response.isSupportedLanguage(language_threshold) && classifier_response.isSupportedLicense(license_threshold)) {
						response.status = CoverageStatus.UNKNOWN;
						if(create_issue!=null && !create_issue.equals("")) {
							response.setActionUrl(create_issue + bugid_uc);
							response.setActionText("Create a Jira ticket if this CVE affects an open-source software component developed in Java or Python.");
						}
					}
					// Out of scope
					else {
						response.status = CoverageStatus.OUT_OF_SCOPE;
						if(create_issue!=null && !create_issue.equals("")) {
							response.setActionUrl(create_issue + bugid_uc);
							response.setActionText("The ML classifier predicted that this CVE does not concern an open-source software component developed in Java or Python. Create a Jira ticket if the classifier is wrong.");
						}
					}
				}
				// All other cases
				else {
					response.status = CoverageStatus.UNKNOWN;
					if(create_issue!=null && !create_issue.equals("")) {
						response.setActionUrl(create_issue + bugid_uc);
						response.setActionText("Create a Jira ticket if this CVE affects an open-source software component developed in Java or Python.");
					}
				}

			} catch(ServiceConnectionException e) {
				response.status = CoverageStatus.ERROR;
				response.setErrorText(e.getLocalizedMessage());
				response.setActionUrl(null);
				response.setActionText(null);
			}
			catch(Exception e) {
				sw.stop(e);
				return new ResponseEntity<CoverageStatusResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		catch(Exception e) {
			sw.stop(e);
			return new ResponseEntity<CoverageStatusResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		sw.stop();
		return new ResponseEntity<CoverageStatusResponse>(response, HttpStatus.OK);
	}
	
	@JsonInclude(JsonInclude.Include.ALWAYS)
	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class CveClassifierResponse {
		private String cve_id;
		private String description;
		private String language;
		private float language_confidence;
		private String license;
		private  float license_confidence;
		public String getCve_id() { return cve_id; }
		public void setCve_id(String cve_id) { this.cve_id = cve_id; }
		public String getDescription() { return description; }
		public void setDescription(String description) { this.description = description; }
		public String getLanguage() { return language; }

		/**
		 * Returns true if (a) the confidence is above the given threshold and the language equals 'java', or
		 * (b) the confidence is below the given threshold.
		 * @param _confidence_threshold
		 * @return
		 */
		@JsonIgnore
		public boolean isSupportedLanguage(double _confidence_threshold) {
			return (this.language_confidence > _confidence_threshold && "java".equalsIgnoreCase(this.getLanguage())) || this.language_confidence < _confidence_threshold;
		}
		public void setLanguage(String language) { this.language = language; }
		public float getLanguage_confidence() { return language_confidence; }
		public void setLanguage_confidence(float language_confidence) { this.language_confidence = language_confidence; }
		public String getLicense() { return license; }
		public void setLicense(String license) { this.license = license; }
		public float getLicense_confidence() { return license_confidence; }
		@JsonIgnore
		public boolean isSupportedLicense(double _confidence_threshold) {
			return (this.license_confidence > _confidence_threshold && "oss".equalsIgnoreCase(this.getLicense())) || this.license_confidence < _confidence_threshold;
		}
		public void setLicense_confidence(float license_confidence) { this.license_confidence = license_confidence; }
	}

	@JsonInclude(JsonInclude.Include.ALWAYS)
	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class JiraSearchResponse {
		private int total;
		private JiraIssue[] issues;
		public int getTotal() {
			return total;
		}
		public void setTotal(int total) {
			this.total = total;
		}
		public JiraIssue[] getIssues() {
			return issues;
		}
		public void setIssues(JiraIssue[] issues) {
			this.issues = issues;
		}
	}

	@JsonInclude(JsonInclude.Include.ALWAYS)
	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class JiraIssue {
		private int id;
		private String key;
		private JiraIssueFields fields;
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public JiraIssueFields getFields() {
			return fields;
		}
		public void setFields(JiraIssueFields fields) {
			this.fields = fields;
		}
	}

	@JsonInclude(JsonInclude.Include.ALWAYS)
	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class JiraIssueFields {
		private String summary;
		private JiraIssueStatus status;
		public String getSummary() {
			return summary;
		}
		public void setSummary(String summary) {
			this.summary = summary;
		}
		public JiraIssueStatus getStatus() {
			return status;
		}
		public void setStatus(JiraIssueStatus status) {
			this.status = status;
		}
	}

	@JsonInclude(JsonInclude.Include.ALWAYS)
	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class JiraIssueStatus {
		private String name;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}

	@JsonInclude(JsonInclude.Include.ALWAYS)
	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class CoverageStatusResponse {
		private String bug;
		private String description;
		private CoverageStatus status;
		private String actionUrl;
		private String actionText;
		private String errorText;
		public CoverageStatusResponse(String bug) {
			super();
			this.bug = bug;
		}
		public String getBug() { return bug; }
		public void setBug(String bug) { this.bug = bug; }

		public String getDescription() { return description; }
		public void setDescription(String description) { this.description = description; }

		@JsonIgnore
		public CoverageStatus getStatus() {
			return status;
		}
		public void setStatus(CoverageStatus status) {
			this.status = status;
		}
		public byte getStatusCode() {
			return this.status.getStatusCode();
		}
		public String getStatusText() {
			return this.status.getText();
		}
		public String getStatusDescription() {
			return this.status.getDescription();
		}
		public String getActionUrl() {
			return actionUrl;
		}
		public void setActionUrl(String actionUrl) {
			this.actionUrl = actionUrl;
		}
		public String getActionText() {
			return actionText;
		}
		public void setActionText(String actionText) {
			this.actionText = actionText;
		}
		public String getErrorText() {
			return errorText;
		}
		public void setErrorText(String errorText) {
			this.errorText = errorText;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((bug == null) ? 0 : bug.hashCode());
			result = prime * result + ((status == null) ? 0 : status.hashCode());
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
			CoverageStatusResponse other = (CoverageStatusResponse) obj;
			if (bug == null) {
				if (other.bug != null)
					return false;
			} else if (!bug.equals(other.bug))
				return false;
			if (status != other.status)
				return false;
			return true;
		}
	}
}
