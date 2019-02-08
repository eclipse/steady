package com.sap.psr.vulas.backend.requests;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.HttpMethod;
import com.sap.psr.vulas.backend.HttpResponse;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.GoalContext;

public class ConditionalHttpRequest extends BasicHttpRequest {

	private static final Log log = LogFactory.getLog(ConditionalHttpRequest.class);

	private List<ResponseCondition> conditions = new LinkedList<ResponseCondition>();

	private BasicHttpRequest conditionRequest = null;

	public ConditionalHttpRequest(HttpMethod _method, String _path, Map<String,String> _query_string_params) {
		super(_method, _path, _query_string_params);
	}

	public ConditionalHttpRequest setConditionRequest(BasicHttpRequest _cr) { this.conditionRequest = _cr; return this; }

	/**
	 * Adds a condition to the list of conditions that must be met before the actual request is sent.
	 * @param _request
	 * @param _condition
	 */
	public ConditionalHttpRequest addCondition(ResponseCondition _condition) {
		this.conditions.add(_condition);
		return this;
	}
	
	@Override
	public HttpRequest setGoalContext(GoalContext _ctx) {
		this.context = _ctx;
		if(this.conditionRequest!=null)
			this.conditionRequest.setGoalContext(_ctx);
		return this;
	}

	/**
	 * First performs the conditional requests. Only if all the responses meets the condition, the actual request will be performed.
	 */
	@Override
	public HttpResponse send() throws IllegalStateException, BackendConnectionException {
		if(this.conditionRequest==null || this.conditions.size()==0)
			throw new IllegalStateException("No condition request or no conditions set");

		// Conditional requests will be skipped in offline mode
		if(CoreConfiguration.isBackendOffline(this.context.getVulasConfiguration())) {
			ConditionalHttpRequest.log.info("Condition(s) not evaluated due to offline mode, do " + this.toString());
			return super.send();
		}
		// Perform conditional requests
		else {
			// Indicates whether all conditions are met
			boolean meets = true;
			final HttpResponse condition_response = this.conditionRequest.send();
			for(ResponseCondition rc: this.conditions) {
				meets = meets && rc.meetsCondition(condition_response);
				if(!meets) {
					ConditionalHttpRequest.log.info("Condition " + rc + " not met");
					break;
				} else {
					ConditionalHttpRequest.log.info("Condition " + rc + " met");
				}
			}

			// Only send if they are met
			if(meets) {
				ConditionalHttpRequest.log.info("Condition(s) met, do " + this.toString());
				return super.send();
			}
			else {
				ConditionalHttpRequest.log.info("Condition(s) not met, skip " + this.toString());
				return null;
			}	
		}	
	}
	
	@Override
	public void savePayloadToDisk() throws IOException {
		super.savePayloadToDisk();
		if(this.conditionRequest!=null)
			this.conditionRequest.savePayloadToDisk();
	}

	@Override
	public void loadPayloadFromDisk() throws IOException {
		super.loadPayloadFromDisk();
		if(this.conditionRequest!=null)
			this.conditionRequest.loadPayloadFromDisk();
	}
	
	@Override
	public void deletePayloadFromDisk() throws IOException {
		super.deletePayloadFromDisk();
		if(this.conditionRequest!=null)
			this.conditionRequest.deletePayloadFromDisk();
	}
	
	/**
	 * First calls the default method {@link ObjectInputStream#defaultReadObject()}, then calls {@link HttpRequest#loadFromDisk()}
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		super.loadPayloadFromDisk();
		this.loadPayloadFromDisk();
	}
}
