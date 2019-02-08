package com.sap.psr.vulas.backend.requests;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.HttpResponse;
import com.sap.psr.vulas.goals.GoalContext;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class HttpRequestList extends AbstractHttpRequest {
	
	private static final Log log = LogFactory.getLog(HttpRequestList.class);

	/**
	 * When set to true, the sending of requests will be stopped upon success, i.e., once a Http response code 2xx will be received.
	 */
	private boolean stopOnSuccess = true;
	
	private List<HttpRequest> list = new LinkedList<HttpRequest>();
	
	public HttpRequestList() { this(true); }
	
	public HttpRequestList(boolean _stop_on_success) {
		this.stopOnSuccess = _stop_on_success;
	}
	
	public void addRequest(HttpRequest _r) { this.list.add(_r); }
	
	@Override
	public HttpRequest setGoalContext(GoalContext _ctx) {
		this.context = _ctx;
		for(HttpRequest r: this.list)
			r.setGoalContext(_ctx);
		return this;
	}

	/**
	 * Loops over the list of requests and calls {@link HttpRequest#send()}. Depending on
	 * the boolean {@link HttpRequestList#stopOnSuccess}, the sending stops or does not
	 * stop in case of a successful call.
	 */
	@Override
	public HttpResponse send() throws BackendConnectionException {
		HttpResponse response = null;
		for(HttpRequest r: this.list) {
			response = r.send();
			if(this.stopOnSuccess && response!=null && (response.isOk() || response.isCreated()))
				break;
		}
		return response;
	}
	
	@Override
	public String getFilename() {
		String prefix = this.ms + "-hrl";
		return prefix;
	}

	@Override
	public void savePayloadToDisk() throws IOException {
		for(HttpRequest r: this.list) {
			r.savePayloadToDisk();
		}
	}

	@Override
	public void loadPayloadFromDisk() throws IOException {
		for(HttpRequest r: this.list) {
			r.loadPayloadFromDisk();
		}
	}
	
	@Override
	public void deletePayloadFromDisk() throws IOException {
		for(HttpRequest r: this.list) {
			r.deletePayloadFromDisk();
		}
	}
}
