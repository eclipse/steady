package com.sap.psr.vulas.backend.util;

import com.sap.psr.vulas.backend.rest.CoverageController;
import com.sap.psr.vulas.backend.rest.CoverageController.CveClassifierResponse;
import com.sap.psr.vulas.backend.rest.CoverageController.JiraSearchResponse;
import com.sap.psr.vulas.shared.connectivity.PathBuilder;
import com.sap.psr.vulas.shared.connectivity.Service;
import com.sap.psr.vulas.shared.connectivity.ServiceConnectionException;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.Artifact;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import com.sap.psr.vulas.shared.json.model.LibraryId;
import com.sap.psr.vulas.shared.json.model.diff.JarDiffResult;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Makes RESTful services available to local clients. Depends on */
public class ServiceWrapper {

  private static Logger log = LoggerFactory.getLogger(ServiceWrapper.class);

  private static ServiceWrapper instance = null;

  private ServiceWrapper() {}

  /**
   * Getter for the field <code>instance</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.util.ServiceWrapper} object.
   */
  public static synchronized ServiceWrapper getInstance() {
    if (instance == null) instance = new ServiceWrapper();
    return instance;
  }

  /**
   * Returns all versions of the Maven artifact with the given group and artifact identifiers and
   * packaging "JAR".
   *
   * @param _group a {@link java.lang.String} object.
   * @param _artifact a {@link java.lang.String} object.
   * @param _latest if true, only the latest version will be returned
   * @param _gt if specified, only versions greater than the provided value are returned
   * @throws com.sap.psr.vulas.shared.connectivity.ServiceConnectionException
   * @return a {@link java.util.Collection} object.
   */
  public Collection<LibraryId> getAllArtifactVersions(
      String _group, String _artifact, boolean _latest, String _gt)
      throws ServiceConnectionException {
    // Build URL

    final String service_url = VulasConfiguration.getGlobal().getServiceUrl(Service.CIA, true);
    final String service_path;
    String param_template;
    if (_latest) {
      service_path = PathBuilder.artifactsLatestGroupVersion(_group, _artifact);
      param_template = "?packagingFilter={p}";
    } else if (_gt != null) {
      service_path = PathBuilder.artifactsGreaterGroupVersion(_group, _artifact, _gt);
      param_template = "?packagingFilter={p}";
    } else {
      service_path = PathBuilder.artifactsGroupVersion(_group, _artifact);
      param_template = "?packaging={p}";
    }

    final String url = service_url.concat(service_path);

    // Parameters
    final Map<String, String> params = new HashMap<String, String>();
    params.put("p", "jar");

    // Make the query
    final List<LibraryId> result = new ArrayList<LibraryId>();
    URI uri = null;
    try {
      uri = new URI(url);
      this.logCallInfo(uri, params);
      final RestTemplate rest_template = new RestTemplate();
      //	LibraryId[] all_libids_array;
      if (_latest) {
        Artifact l = rest_template.getForObject(url + param_template, Artifact.class, params);
        // all_libids_array = new LibraryId[]{l.getLibId()};
        result.add(l.getLibId());
      } else {
        Artifact[] artifacts =
            rest_template.getForObject(url + param_template, Artifact[].class, params);
        for (Artifact a : artifacts) result.add(a.getLibId());
      }
      // result.addAll(Arrays.asList(all_libids_array));
    } catch (RestClientException e) {
      if (e.getMessage().contains("404")) log.info("Call [" + uri + "] returned 404 Not Found");
      else if (e.getMessage().contains("400"))
        log.error("Call [" + uri + "] returned 400 Bad request");
      else throw new ServiceConnectionException(uri, e);
    } catch (URISyntaxException use) {
      throw new ServiceConnectionException("Cannot create service URI from [" + url + "]", use);
    }

    return result;
  }

  /**
   * getArtifactConstructs.
   *
   * @param _group a {@link java.lang.String} object.
   * @param _artifact a {@link java.lang.String} object.
   * @param _version a {@link java.lang.String} object.
   * @param _t a {@link com.sap.psr.vulas.shared.enums.ConstructType} object.
   * @return a {@link java.util.List} object.
   * @throws com.sap.psr.vulas.shared.connectivity.ServiceConnectionException if any.
   */
  public List<ConstructId> getArtifactConstructs(
      String _group, String _artifact, String _version, ConstructType _t)
      throws ServiceConnectionException {
    // Build URL
    final String service_url = VulasConfiguration.getGlobal().getServiceUrl(Service.CIA, true);
    final String service_path = PathBuilder.artifactsConstruct(_group, _artifact, _version);
    final String url = service_url.concat(service_path);
    String param_template = (_t != null) ? "?type={type}" : "";

    // Parameters
    final Map<String, String> params = new HashMap<String, String>();
    if (_t != null) params.put("type", _t.toString());
    // Make the query
    final List<ConstructId> result = new ArrayList<ConstructId>();
    URI uri = null;
    try {
      uri = new URI(url);
      this.logCallInfo(uri, params);
      final RestTemplate rest_template = new RestTemplate();
      ConstructId[] all_libids_array =
          rest_template.getForObject(url + param_template, ConstructId[].class, params);
      result.addAll(Arrays.asList(all_libids_array));
    } catch (RestClientException e) {
      throw new ServiceConnectionException(uri, e);
    } catch (URISyntaxException use) {
      throw new ServiceConnectionException("Cannot create service URI from [" + url + "]", use);
    }

    return result;
  }

  /**
   * diffJars.
   *
   * @param _old a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
   * @param _new a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
   * @return a {@link com.sap.psr.vulas.shared.json.model.diff.JarDiffResult} object.
   * @throws com.sap.psr.vulas.shared.connectivity.ServiceConnectionException if any.
   */
  public JarDiffResult diffJars(LibraryId _old, LibraryId _new) throws ServiceConnectionException {
    final String service_url = VulasConfiguration.getGlobal().getServiceUrl(Service.CIA, true);
    final String service_path = PathBuilder.diffArtifacts();
    final String url = service_url.concat(service_path);

    // The artifacts to compare
    final Artifact[] artifacts = new Artifact[2];
    artifacts[0] = ServiceWrapper.fromLibid(_old);
    artifacts[0].setPackaging("jar");
    artifacts[0].setProgrammingLanguage(ProgrammingLanguage.JAVA);
    artifacts[1] = ServiceWrapper.fromLibid(_new);
    artifacts[1].setPackaging("jar");
    artifacts[1].setProgrammingLanguage(ProgrammingLanguage.JAVA);

    // Make the query
    JarDiffResult result;
    URI uri = null;
    try {
      uri = new URI(url);
      this.logCallInfo(uri, null);
      final RestTemplate rest_template = new RestTemplate();
      result = rest_template.postForObject(url, artifacts, JarDiffResult.class, (Object) null);
    } catch (HttpClientErrorException he) {
      throw new EntityNotFoundException();
    } catch (RestClientException e) {
      throw new ServiceConnectionException(uri, e);
    } catch (URISyntaxException use) {
      throw new ServiceConnectionException("Cannot create service URI from [" + url + "]", use);
    }

    return result;
  }

  /**
   * Returns a {@link ResponseDoc} corresponding to the given {@link LibraryId}.
   *
   * @param _libid
   * @return
   */
  private static Artifact fromLibid(LibraryId _libid) {
    final Artifact d = new Artifact();
    d.setLibId(_libid);
    return d;
  }

  /**
   * Logs call information.
   *
   * @param _uri
   * @param _params
   */
  private void logCallInfo(URI _uri, Map<String, String> _params) {
    final StringBuilder b = new StringBuilder();
    b.append("Calling [").append(_uri.toString()).append("]");
    if (_params != null && _params.size() > 0) {
      b.append(" with params [");
      int i = 0;
      for (String key : _params.keySet()) {
        if (i++ > 0) b.append(", ");
        b.append(key).append("=").append(_params.get(key));
      }
    }
    log.info(b.toString() + "]");
  }

  /**
   * classify.
   *
   * @param _cve a {@link java.lang.String} object.
   * @return a {@link com.sap.psr.vulas.backend.rest.CoverageController.CveClassifierResponse}
   *     object.
   * @throws com.sap.psr.vulas.shared.connectivity.ServiceConnectionException if any.
   */
  public CveClassifierResponse classify(String _cve) throws ServiceConnectionException {
    CveClassifierResponse response = null;

    final String service_url = VulasConfiguration.getGlobal().getServiceUrl(Service.CVE, true);
    if (service_url == null || service_url.equals("")) {
      log.error(
          "Configuration setting ["
              + VulasConfiguration.getGlobal().getServiceUrlKey(Service.CVE)
              + "] not specified");
    } else {
      // Parameters
      final String param_template = "{cve}";
      final Map<String, String> params = new HashMap<String, String>();
      params.put("cve", _cve);

      // Make the query
      URI uri = null;
      try {
        uri = new URI(service_url);
        this.logCallInfo(uri, params);
        final RestTemplate rest_template = new RestTemplate();
        response =
            rest_template.getForObject(
                service_url + param_template, CveClassifierResponse.class, params);
      } catch (RestClientException e) {
        throw new ServiceConnectionException(uri, e);
      } catch (URISyntaxException use) {
        throw new ServiceConnectionException(
            "Cannot create service URI from [" + service_url + "]", use);
      }
    }

    return response;
  }

  /**
   * searchJira.
   *
   * @param _bugid a {@link java.lang.String} object.
   * @return a {@link com.sap.psr.vulas.backend.rest.CoverageController.JiraSearchResponse} object.
   * @throws com.sap.psr.vulas.shared.connectivity.ServiceConnectionException if any.
   */
  public JiraSearchResponse searchJira(String _bugid) throws ServiceConnectionException {
    JiraSearchResponse response = null;

    final String service_url = VulasConfiguration.getGlobal().getServiceUrl(Service.JIRA, true);
    final String project_id =
        VulasConfiguration.getGlobal()
            .getConfiguration()
            .getString(CoverageController.PROJECT_ID, null);
    final String component_id =
        VulasConfiguration.getGlobal()
            .getConfiguration()
            .getString(CoverageController.COMPONENT_ID, null);

    if (service_url == null
        || service_url.equals("")
        || project_id == null
        || project_id.equals("")
        || component_id == null
        || component_id.equals("")) {
      log.error(
          "Jira configuration settings ["
              + VulasConfiguration.getGlobal().getServiceUrlKey(Service.JIRA)
              + ", "
              + CoverageController.PROJECT_ID
              + ", "
              + CoverageController.COMPONENT_ID
              + "] not fully specified");
    } else {
      // Parameters
      final String param_template = "?jql={jql}";
      final Map<String, String> params = new HashMap<String, String>();
      params.put(
          "jql",
          "project = "
              + project_id
              + " AND component = "
              + component_id
              + " AND summary ~ \""
              + _bugid
              + "\"");

      // Make the query
      URI uri = null;
      try {
        uri = new URI(service_url);
        this.logCallInfo(uri, params);
        final RestTemplate rest_template = new RestTemplate();
        if ((VulasConfiguration.getGlobal()
                    .getConfiguration()
                    .getString(VulasConfiguration.VULAS_JIRA_USER)
                == null)
            || (VulasConfiguration.getGlobal()
                    .getConfiguration()
                    .getString(VulasConfiguration.VULAS_JIRA_PWD)
                == null)) {
          log.error(
              "Missing Jira credentials, please provide the arguments -Dvulas.jira.usr and -Dvulas.jira.pwd at startup");

          throw new ServiceConnectionException(
              "Missing Jira credentials, please provide the arguments -Dvulas.jira.usr and -Dvulas.jira.pwd at startup",
              new Throwable(
                  "Missing Jira credentials, please provide the arguments -Dvulas.jira.usr and -Dvulas.jira.pwd at startup"));
        }

        final ResponseEntity<JiraSearchResponse> re =
            rest_template.exchange(
                service_url + param_template,
                HttpMethod.GET,
                new HttpEntity<String>(
                    this.createHeaders(
                        VulasConfiguration.getGlobal()
                            .getConfiguration()
                            .getString(VulasConfiguration.VULAS_JIRA_USER),
                        VulasConfiguration.getGlobal()
                            .getConfiguration()
                            .getString(VulasConfiguration.VULAS_JIRA_PWD))),
                JiraSearchResponse.class,
                params);

        response = re.getBody();
      } catch (RestClientException e) {
        throw new ServiceConnectionException(uri, e);
      } catch (URISyntaxException use) {
        throw new ServiceConnectionException(
            "Cannot create service URI from [" + service_url + "]", use);
      }
    }

    return response;
  }

  private HttpHeaders createHeaders(final String username, final String password) {
    return new HttpHeaders() {
      {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        set("Authorization", authHeader);
      }
    };
  }
}
