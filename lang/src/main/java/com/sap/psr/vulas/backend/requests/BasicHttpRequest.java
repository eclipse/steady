package com.sap.psr.vulas.backend.requests;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.backend.HttpMethod;
import com.sap.psr.vulas.backend.HttpResponse;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.GoalContext;
import com.sap.psr.vulas.shared.connectivity.Service;
import com.sap.psr.vulas.shared.json.JsonBuilder;
import com.sap.psr.vulas.shared.json.JsonSyntaxException;
import com.sap.psr.vulas.shared.util.Constants;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * <p>BasicHttpRequest class.</p>
 *
 */
public class BasicHttpRequest extends AbstractHttpRequest {

	private static final Log log = LogFactory.getLog(BasicHttpRequest.class);
	
	private static final long serialVersionUID = 1L;

	private HttpMethod method = null;
	private String path = null;
	private Map<String,String> params = null;
	private Service service = null;

	/** Will not be serialized as part of the class, but is written to dedicated file. */
	private transient String payload = null;

	/** Will not be serialized as part of the class, but is written to dedicated file. */
	private transient FileInputStream binPayload = null;

	private String contentType = null;

	private String dir=null;
	
	/** Null in case the request does not exist on disk. */
	private String payloadPath = null;

	/** Cached in case {@link HttpRequest#send()} is called multiple times on the same request. */
	private transient HttpResponse response = null;

	private boolean checkJson = false;
	
	/**
	 * <p>Constructor for BasicHttpRequest.</p>
	 *
	 * @param _method a {@link com.sap.psr.vulas.backend.HttpMethod} object.
	 * @param _path a {@link java.lang.String} object.
	 */
	public BasicHttpRequest(HttpMethod _method, String _path) {
		this(Service.BACKEND, _method, _path, null);
	}

	/**
	 * Creates a request for the RESTful service {@link Service#BACKEND}.
	 *
	 * @param _method a {@link com.sap.psr.vulas.backend.HttpMethod} object.
	 * @param _path a {@link java.lang.String} object.
	 * @param _query_string_params a {@link java.util.Map} object.
	 */
	public BasicHttpRequest(HttpMethod _method, String _path, Map<String,String> _query_string_params) {
		this(Service.BACKEND, _method, _path, _query_string_params);
	}

	/**
	 * Creates a request for the given RESTful {@link Service}.
	 *
	 * @param _service a {@link com.sap.psr.vulas.shared.connectivity.Service} object.
	 * @param _method a {@link com.sap.psr.vulas.backend.HttpMethod} object.
	 * @param _path a {@link java.lang.String} object.
	 * @param _query_string_params a {@link java.util.Map} object.
	 */
	public BasicHttpRequest(Service _service, HttpMethod _method, String _path, Map<String,String> _query_string_params) {
		this.service = _service;
		this.method = _method;
		this.path = _path;
		this.params = _query_string_params;
	}

	/**
	 * <p>Setter for the field <code>payload</code>.</p>
	 *
	 * @param _payload a {@link java.lang.String} object.
	 * @param _type a {@link java.lang.String} object.
	 * @param _check a boolean.
	 * @return a {@link com.sap.psr.vulas.backend.requests.BasicHttpRequest} object.
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public BasicHttpRequest setPayload(String _payload, String _type, boolean _check) throws IllegalArgumentException {
		if(this.method.equals(HttpMethod.POST) || this.method.equals(HttpMethod.PUT) || this.method.equals(HttpMethod.DELETE) ) {
			this.payload = _payload;
			this.contentType = _type;
			this.checkJson = _check;
			return this;
		} else {
			throw new IllegalStateException("Payload only possible for POST, PUT and DELETE, not for [" + this.method + "]");
		}
	}

	/**
	 * <p>Setter for the field <code>binPayload</code>.</p>
	 *
	 * @param _payload a {@link java.io.FileInputStream} object.
	 * @param _type a {@link java.lang.String} object.
	 * @return a {@link com.sap.psr.vulas.backend.requests.BasicHttpRequest} object.
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public BasicHttpRequest setBinPayload(FileInputStream _payload, String _type) throws IllegalArgumentException {
		if(this.method.equals(HttpMethod.POST) ) {
			this.binPayload = _payload;
			this.contentType = _type;
			return this;
		} else {
			throw new IllegalStateException("Payload only possible for POST not for [" + this.method + "]");
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public HttpRequest setGoalContext(GoalContext _ctx) {
		this.context = _ctx;
		return this;
	}

	/**
	 * <p>Setter for the field <code>dir</code>.</p>
	 *
	 * @param dir a {@link java.lang.String} object.
	 */
	public void setDir(String dir) {
		this.dir = dir;
	}

	/**
	 * <p>hasPayload.</p>
	 *
	 * @return a boolean.
	 */
	public boolean hasPayload() {
		return this.payload!=null && !this.payload.isEmpty();
	}

	private boolean isUploadRequest() {
		return this.method.equals(HttpMethod.POST) || this.method.equals(HttpMethod.PUT);
	}

	/**
	 * <p>send.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.backend.HttpResponse} object.
	 * @throws com.sap.psr.vulas.backend.BackendConnectionException if any.
	 */
	public HttpResponse send() throws BackendConnectionException {
		if(this.response==null) {
			// Check JSON
			Exception exception = ( this.hasPayload() && this.checkJson ? BasicHttpRequest.checkJson(this.payload) : null);

			// Make call if one of the following holds:
			// - call is read request and connect is not offline
			// - call is write request, exception is null and connect is read_write
			if( (!this.isUploadRequest() && !CoreConfiguration.isBackendOffline(this.getVulasConfiguration()) ) ||
				(this.isUploadRequest() && exception==null && CoreConfiguration.isBackendReadWrite(this.getVulasConfiguration())) ) {
				try {
					response = this.sendRequest();

					// Delete file (if any) on success
					if(response.isCreated() || response.isOk()) {
						try {
							this.deleteFromDisk();
						} catch (IOException e) {
							BasicHttpRequest.log.error("Error deleting files from update directory: " + e.getMessage());
						}
					}
				}
				catch (BackendConnectionException e) {
					exception = e;
					BasicHttpRequest.log.error(e.getMessage());
					try {
						FileUtil.writeToFile(new File(this.getVulasConfiguration().getTmpDir().toFile(), this.getFilename() + ".html"), e.getHttpResponseBody());
					} catch (IOException e1) {
						BasicHttpRequest.log.error("Error saving HTTP error message: " + e1.getMessage(), e1);
					}
				}
			}

			// Save to disk if
			// - call is write request and exception is not null or connect is not read_write
			if(this.isUploadRequest() && !this.isPayloadSavedOnDisk() && (exception!=null || !CoreConfiguration.isBackendReadWrite(this.getVulasConfiguration()))) {
				try {
					this.saveToDisk();
				} catch (IOException e) {
					BasicHttpRequest.log.info("HttpUploadRequest could not be saved: " + e.getMessage());
				}
			}

			// Throw exception (if any)
			if(exception!=null)
				throw new BackendConnectionException(exception.getMessage(), exception);
		}

		return response;
	}

	private boolean isPayloadSavedOnDisk() {
		return this.payloadPath!=null && Paths.get(this.payloadPath).toFile().exists();
	}

	/** {@inheritDoc} */
	@Override
	public String getFilename() {
		String prefix = this.path;
		if(prefix.startsWith("/")) prefix = prefix.substring(1);
		prefix = prefix.replace("/", "__");
		prefix = prefix.replace("?", "__");
		prefix = this.ms + "-" + prefix;
		return prefix;
	}
	
	/**
	 * <p>getPayloadFilename.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPayloadFilename() {
		return this.getFilename() + ".json";
	}
	
	/** {@inheritDoc} */
	@Override
	public void savePayloadToDisk()  throws IOException {
		if(this.hasPayload()) {
			final Path payload_path = Paths.get(this.getVulasConfiguration().getDir(CoreConfiguration.UPLOAD_DIR).toString(), this.getPayloadFilename());
			this.payloadPath = payload_path.toString();
			final File json_file = payload_path.toFile();
			FileUtil.writeToFile(json_file, this.payload);
			BasicHttpRequest.log.info("Request body (JSON) written to [" + json_file + "]");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void loadPayloadFromDisk() throws IOException {
		if(this.payloadPath!=null)
			this.payload = FileUtil.readFile(this.payloadPath);
	}

	/** {@inheritDoc} */
	@Override
	public void deletePayloadFromDisk() throws IOException {
		if(this.payloadPath!=null)
			Paths.get(this.payloadPath).toFile().deleteOnExit();
	}

	private final HttpResponse sendRequest() throws BackendConnectionException {
		HttpResponse response = null;
		HttpURLConnection connection = null;
		int response_code = -1;
		final URI uri = this.getUri();
		Map<String,List<String>> request_fields = null;
		final RequestRepeater repeater = new RequestRepeater(this.getVulasConfiguration().getConfiguration().getLong(CoreConfiguration.REPEAT_MAX, 50), this.getVulasConfiguration().getConfiguration().getLong(CoreConfiguration.REPEAT_WAIT, 60000));
		
		boolean is_503;
		try {
			do {
				is_503 = false;
				
				final long start_nano = System.nanoTime();
				
				connection = (HttpURLConnection)uri.toURL().openConnection();
				connection.setRequestMethod(this.method.toString().toUpperCase());

				// Include tenant and space Http headers
				String tenant_token = null, space_token = null;
				if(this.context!=null && this.context.hasTenant()) {
					tenant_token = this.context.getTenant().getTenantToken();
					connection.setRequestProperty(Constants.HTTP_TENANT_HEADER, tenant_token);
				}
				if(this.context!=null && this.context.hasSpace()) {
					space_token = this.context.getSpace().getSpaceToken();
					connection.setRequestProperty(Constants.HTTP_SPACE_HEADER, space_token);
				}
				
				// Include version and component as request header
				connection.setRequestProperty(Constants.HTTP_VERSION_HEADER, CoreConfiguration.getVulasRelease());
				connection.setRequestProperty(Constants.HTTP_COMPONENT_HEADER, Constants.VulasComponent.client.toString());
				
				// Only if put something in the body
				if(this.hasPayload()) {
					connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
					connection.setRequestProperty("Content-Length", Integer.toString(this.payload.getBytes().length));
					connection.setRequestProperty("Content-Language", "en-US");
				}
				else if(this.binPayload!=null){
					connection.setRequestProperty("Content-Type", this.contentType);
				}

				if(!this.hasPayload())
					BasicHttpRequest.log.info("HTTP " + this.method.toString().toUpperCase() + " [uri=" + uri + (tenant_token==null?"":", tenant=" + tenant_token) + (space_token==null?"":", space=" + space_token) + "]");
				else if(this.binPayload==null)	
					BasicHttpRequest.log.info("HTTP " + this.method.toString().toUpperCase() + " [uri=" + uri + ", size=" + StringUtil.byteToKBString(this.payload.getBytes().length) + (tenant_token==null?"":", tenant=" + tenant_token) + (space_token==null?"":", space=" + space_token) + "]");
				else
					BasicHttpRequest.log.info("HTTP " + this.method.toString().toUpperCase() + " [uri=" + uri + ", size=" + this.binPayload.available() + (tenant_token==null?"":", tenant=" + tenant_token) + (space_token==null?"":", space=" + space_token) + "]");

				connection.setUseCaches(false);
				connection.setDoInput(true);

				if(this.hasPayload()) {
					connection.setDoOutput(true);
					request_fields = connection.getRequestProperties();
					final DataOutputStream wr = new DataOutputStream (connection.getOutputStream ());
					wr.write(this.payload.getBytes("UTF-8"));
					wr.flush();
					wr.close();
				}
				else if(this.binPayload!=null){
					connection.setDoOutput(true);
					//required only for multipart/fileupload
					//	String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
					//	String CRLF = "\r\n"; // Line separator required by multipart/form-data.

					request_fields = connection.getRequestProperties();
					final OutputStream output = connection.getOutputStream();
					//	PrintWriter writer = new PrintWriter(new OutputStreamWriter(output), true); 

					byte[] buffer = new byte[4096];
					int length;
					while ((length = this.binPayload.read(buffer)) > 0) {
						output.write(buffer, 0, length);
					} 
					output.flush();
					output.close();
					//    writer.append(CRLF).flush();
					//   writer.append("--" + boundary + "--").append(CRLF).flush();

				}
				else {
					connection.setDoOutput(false);
					request_fields = connection.getRequestProperties();
					connection.connect();
				}

				// Read response
				response_code = connection.getResponseCode();
				response = new HttpResponse(response_code);

				// If the response body contains a JAR file, save it
				if(response.isOk() && connection.getContentType()!=null && connection.getContentType().contains("application/java-archive")){
					String fileName = "";
					String disposition = connection.getHeaderField("Content-Disposition");
					if (disposition != null) {
						// Extracts file name from header field
						int index = disposition.indexOf("filename=");
						if (index > 0) {
							fileName = disposition.substring(index + 9, disposition.length() );
						}
					} else {
						// Extracts file name from URL
						fileName = this.path.substring(this.path.lastIndexOf("/") + 1, this.path.length());
					}

					// Opens input stream from the HTTP connection
					InputStream inputStream = connection.getInputStream();
					String saveFilePath = null; 
					if (this.dir!=null){
						//create directories if not existing
						if(!Files.exists(Paths.get(dir))){
							Files.createDirectories(Paths.get(dir));
						}
						saveFilePath= dir + File.separator + fileName;
					}
					else
						saveFilePath= Paths.get(this.getVulasConfiguration().getTmpDir().toString()).toString()+ File.separator + fileName;

					// Opens an output stream to save into file
					FileOutputStream outputStream = new FileOutputStream(saveFilePath);

					int bytesRead = -1;
					byte[] buffer = new byte[inputStream.available()];
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
					}

					response.setBody(saveFilePath);
					outputStream.close();
					inputStream.close();
				}
				else if(response.isOk() || response.isCreated())
					response.setBody(FileUtil.readInputStream(connection.getInputStream(), FileUtil.getCharset()));

				// Stats
				final long end_nano = System.nanoTime();
				BasicHttpRequest.log.info("HTTP " + this.method.toString().toUpperCase() + " completed with response code [" + response_code + "] in " + StringUtil.nanoToFlexDurationString(end_nano-start_nano) + " (proxy=" + connection.usingProxy() + ")") ;

				// 503: Retry
				if(response.isServiceUnavailable()) {
					is_503 = true;
				}
				// 5xx: Throw exception
				else if(response.isServerError() || response.getStatus()==400) {
					final BackendConnectionException bce = new BackendConnectionException(this.method, uri, response_code, null);
					try {
						final String body = this.readErrorStream(connection);
						if(body!=null && !body.trim().equals(""))
							bce.setHttpResponseBody(body);
					} catch (IOException e1) {
						//BasicHttpRequest.log.error("Cannot read input stream: " + e1.getMessage());
					}
					throw bce;
				}
			}
			while(repeater.repeat(is_503));
			if(is_503)
				throw new BackendConnectionException(this.method, uri, 503, null);
		} catch(BackendConnectionException bce) {
			this.logHeaderFields("    Request-header", request_fields);	
			this.logHeaderFields("    Response-header", connection.getHeaderFields());
			if(bce.getHttpResponseBody()!=null)
				BasicHttpRequest.log.error("    Response-body: [" + bce.getHttpResponseBody().replaceAll("[\\t\\n\\x0B\\f\\r]*", "") + "]");
			BasicHttpRequest.log.error("    Exception message: [" + bce.getMessage() + "]");
			if(this.hasPayload())
				BasicHttpRequest.log.error("    HTTP Request body: [" + this.payload.toString() + "]");
			//throw bce;
		} catch(Exception e) {
			final BackendConnectionException bce = new BackendConnectionException(this.method, uri, response_code, e);
			try {
				bce.setHttpResponseBody(this.readErrorStream(connection));
			} catch (IOException e1) {
				//BasicHttpRequest.log.error("Cannot read error stream: " + e1.getMessage());
			}
			throw bce;
		}
		finally {
			if(connection != null) connection.disconnect();
		}
		return response;
	}
	
	private void logHeaderFields(String _prefix, Map<String,List<String>> _fields) {
		for(Map.Entry<String, List<String>> entry: _fields.entrySet())
			BasicHttpRequest.log.error(_prefix + " " + (entry.getKey()==null?"":"["+entry.getKey()+"]" + " = ") + entry.getValue());
	}

	/**
	 * @param _c
	 */
	private String readErrorStream(HttpURLConnection _c) throws IOException {
		String error = null;
		if(_c!=null) {
			InputStream is = _c.getErrorStream();
			if(is==null)
				is = _c.getInputStream();
			if(is!=null) {
				error = FileUtil.readInputStream(is, FileUtil.getCharset());
			}
		}
		return error;
	}

	private URI getUri() {
		return this.getUri(this.service, this.path, this.params);
	}

	/**
	 * <p>getUri.</p>
	 *
	 * @param _service a {@link com.sap.psr.vulas.shared.connectivity.Service} object.
	 * @param _path a {@link java.lang.String} object.
	 * @param _params a {@link java.util.Map} object.
	 * @return a {@link java.net.URI} object.
	 */
	public URI getUri(Service _service, String _path, Map<String,String> _params) {

		// Check whether URL is present
		if(!CoreConfiguration.isBackendOffline(this.getVulasConfiguration()) && !this.getVulasConfiguration().hasServiceUrl(_service))
			throw new IllegalStateException("URL for service [" + _service + "] is not configured");

		URI uri = null;

		final StringBuilder builder = new StringBuilder();
		builder.append(this.getVulasConfiguration().getServiceUrl(_service));
		builder.append(_path);
		int i = 0;
		if(_params!=null) {
			for(Map.Entry<String, String> entry: _params.entrySet()) {
				if(i==0)
					builder.append('?');
				else if(i<=_params.size())
					builder.append('&');
				builder.append(entry.getKey()).append('=').append(entry.getValue());
				i++;
			}
		}
		try {
			if ( builder.toString().contains("[") || builder.toString().contains("]") ){
				URL url = new URL(builder.toString());
				uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(),
						url.getPath(), url.getQuery(), url.getRef());
			} else {
				uri = new URI(builder.toString());
			}
		} catch (MalformedURLException e) {
			throw new IllegalStateException("Error when creating URI out of [" + builder.toString() + "]: " + e.getMessage());
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Error when creating URI out of [" + builder.toString() + "]: " + e.getMessage());
		}

		return uri;
	}

	/**
	 * Checks the syntax of the given JSON string.
	 * @param _json
	 * @return a JsonSyntaxException if there is a problem, null otherwise
	 */
	private static final Exception checkJson(String _json) {
		Exception exception = null;
		if(_json!=null) {
			try { JsonBuilder.checkJsonValidity(_json); }
			catch(JsonSyntaxException jse) {
				BasicHttpRequest.log.error("Invalid JSON syntax: " + jse.getMessage());
				exception = jse;
			}
		}
		return exception;
	}

	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return "HTTP " + this.method.toString().toUpperCase() + " [uri=" + this.getUri() + "]";
	}

	/**
	 * First calls the default method {@link ObjectInputStream#defaultReadObject()}, then calls {@link HttpRequest#loadFromDisk()}
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		this.loadFromDisk();
	}
}
