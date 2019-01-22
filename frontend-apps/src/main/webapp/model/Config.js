jQuery.sap.declare("model.Config");
jQuery.sap.require("jquery.sap.storage");

model.Config = {};

model.Config.settings = {
		cookie : {  host : "/backend",
					space : "",
					ciaHost : "/cia",
					listSize : "50"
					//skipEmpty : false
		},
		defaultSpace :  "",
		tenant : "",
		//dev flag to be used in developement only
		// true : tenant field available in configuration pane
		// false : tenant field hidden (populated with the default of the configured backend)
		dev : false,
		dlRegexList : "",
		dlExample : "",
		swIdLabel : "",
		swIdLink : "", 
		swIdRegex : "",
		swIdDb : "",
		swIdMandatory : "",
		wikiUrl : "",
		swIdValidationUrls : "",
		swIdSeachUrl : ""
	};

//********* SECTION : GETTERS AND SETTERS FUNCTIONS TO POPULATE JSONMODELS WITH JSON RETURNED BY THE BACKEND REQUESTS *********\\


//loads the properties that are not store in cookies 
// but always loaded from the configured backend
model.Config.loadPropertiesFromBackend = function(){
	//retrieve default from backend
	var url = model.Config.getHost()+"/configuration?subset=vulas.backend.frontend.apps";
	var oModel = new sap.ui.model.json.JSONModel();
	//3rd param is 'asynch' 
	oModel.loadData(url, null,true,"GET",false,true,{'X-Vulas-Component':'appfrontend'});
	oModel.attachRequestCompleted(function() {
		var configs = oModel.getObject('/');
		if(configs.length==0){
			model.Config.settings.dlRegexList="";
			model.Config.settings.dlExample="";
			model.Config.settings.swIdLabel="";
			model.Config.settings.swIdLink="";
			model.Config.settings.swIdRegex="";
			model.Config.settings.wikiUrl="";
			model.Config.settings.swIdDb="";
			model.Config.settings.swIdMandatory="";
			model.Config.settings.swIdValidationUrlList="";
			model.Config.settings.swIdSeachUrl="";
		}
		for (var i in configs){
			if(configs[i].key=="dl.regex")
				model.Config.settings.dlRegexList=configs[i].value;
			if(configs[i].key=="dl.example")
				model.Config.settings.dlExample=configs[i].value[0];
			if(configs[i].key=="sw.id.label")
				model.Config.settings.swIdLabel=configs[i].value[0];
			if(configs[i].key=="sw.id.link")
				model.Config.settings.swIdLink=configs[i].value[0];
			if(configs[i].key=="sw.id.regex")
				model.Config.settings.swIdRegex=configs[i].value[0];
			if(configs[i].key=="wiki.url")
				model.Config.settings.wikiUrl=configs[i].value[0];
			if(configs[i].key=="sw.id.db.key")
				model.Config.settings.swIdDb=configs[i].value[0];		
			if(configs[i].key=="sw.id.mandatory")
				model.Config.settings.swIdMandatory=configs[i].value[0];		
			if(configs[i].key=="sw.id.validation.urls")
				model.Config.settings.swIdValidationUrlList=configs[i].value;	
			if(configs[i].key=="sw.id.search.url")
				model.Config.settings.swIdSeachUrl=configs[i].value[0];	
		}
	});
}


model.Config.upgradeCookieStructure = function (_cookie){
	if (!_cookie.hasOwnProperty('listSize')) {
		model.Config.setListSize(50)
	}
	//this save should be redundant as the setXXX functions for each upgraded value are already saving the entire cookie to the local storage
	oStore.put("vulas-frontend-settings", _cookie)
}

// populates the settings with the data read from the cookie
// called in the init of Master.controller
model.Config.setModel = function(_m){
	if(_m) {
		if (typeof _m === 'string') {
			model.Config.settings.cookie = JSON.parse(_m)
		} else {
			model.Config.settings.cookie = _m
		}
	}
	model.Config.upgradeCookieStructure(model.Config.settings.cookie)
}

model.Config.setHost = function(_host) {
	model.Config.settings.cookie.host=_host;
	oStore.put("vulas-frontend-settings", model.Config.settings.cookie)
	//also update the tenant and space tokens to match the new backend host (calling get with flag force=true will trigger the request)
	// note that the order of the calls MUST NOT be modified (tenant is needed to query for space etc.)
	model.Config.getTenant(true);
	model.Config.getDefaultSpace(true);
	model.Config.getSpace(true);
	model.Config.loadPropertiesFromBackend();
}
model.Config.getHost = function() {
	return model.Config.settings.cookie.host;
}

model.Config.setSpace = function(_token) {
	model.Config.settings.cookie.space=_token;
	oStore.put("vulas-frontend-settings", model.Config.settings.cookie)
}

model.Config.setTemporaryWorkspace = function(workSpace) {
	model.Config.temporaryWorkspace = workSpace
}

model.Config.getTemporaryWorkspace = function() {
	return model.Config.temporaryWorkspace
}

model.Config.getSpace = function(_force) {
	if(_force || !model.Config.settings.cookie.space){		
		model.Config.setSpace(model.Config.getDefaultSpace());
	}
	const temporaryWorkspace = model.Config.getTemporaryWorkspace()
	if (temporaryWorkspace) {
		return temporaryWorkspace
	} else {
		return model.Config.settings.cookie.space;
	}
}

model.Config.getDefaultSavedSpace = function() {
	return model.Config.settings.cookie.space
}

model.Config.getDefaultSpace = function(_force) {
	if((_force!=undefined && _force)|| model.Config.settings.defaultSpace=="" || model.Config.settings.defaultSpace ==null || model.Config.settings.defaultSpace == undefined){
		//retrieve default from backend
		var url = model.Config.getHost()+"/spaces/default";
		var oModel = new sap.ui.model.json.JSONModel();
		//3rd param is 'asynch', set to false as the value is required to continue
		oModel.loadData(url, null,false,"GET",false,true,{'X-Vulas-Component':'appfrontend','X-Vulas-Tenant':model.Config.getTenant()});
		model.Config.settings.defaultSpace = oModel.getObject('/spaceToken');
	}
	
	return model.Config.settings.defaultSpace;
}

model.Config.setCiaHost = function(_host) {
	model.Config.settings.cookie.ciaHost=_host;
	oStore.put("vulas-frontend-settings", model.Config.settings.cookie)
}
model.Config.getCiaHost = function() {
	return model.Config.settings.cookie.ciaHost;
}

model.Config.setListSize = function(_size) {
	model.Config.settings.cookie.listSize =_size;
	oStore.put("vulas-frontend-settings", model.Config.settings.cookie)
}
model.Config.getListSize = function() {
	return model.Config.settings.cookie.listSize;
}

//model.Config.setSkipEmpty = function(_skipEmpty){
//	model.Config.settings.cookie.skipEmpty=_skipEmpty;
//	oStore.put("vulas-frontend-settings", model.Config.settings.cookie)
//}
model.Config.getSkipEmpty = function(){
	//return model.Config.settings.cookie.skipEmpty;
	return false;
}

model.Config.setTenant = function(_token) {
	model.Config.settings.tenant=_token;
	//we do not store the tenant in the cookies as it's always retrieved from the backend based on the configured host
//	oStore.put("vulas-frontend-settings", model.Config.settings.cookie)
}
model.Config.getTenant = function(_force) {
	if((_force!=undefined && _force) || model.Config.settings.tenant=="" || model.Config.settings.tenant ==null || model.Config.settings.tenant == undefined){
		//retrieve default from backend
		var url = model.Config.getHost()+"/tenants/default";
		var oModel = new sap.ui.model.json.JSONModel();
		//3rd param is 'asynch', set to false as the value is required to continue
		oModel.loadData(url, null,false,"GET",false,true, {
			'X-Vulas-Component':'appfrontend'
		});
		model.Config.setTenant(oModel.getObject('/tenantToken'));
	}
		
	return model.Config.settings.tenant;
	
}

model.Config.getDlRegexList = function() {
	return model.Config.settings.dlRegexList;
}

model.Config.getDlExample = function() {
	return model.Config.settings.dlExample;
}

model.Config.getSwIdLabel = function() {
	return model.Config.settings.swIdLabel;
}

model.Config.getSwIdLink = function() {
	return model.Config.settings.swIdLink;
}

model.Config.getSwIdRegex = function() {
	return model.Config.settings.swIdRegex;
}

model.Config.getWikiUrl = function() {
	return model.Config.settings.wikiUrl;
}

model.Config.getSwIdDb = function() {
	return model.Config.settings.swIdDb;
}

model.Config.getSwIdMandatory = function() {
	return model.Config.settings.swIdMandatory;
}

model.Config.getSwIdValidationUrlList = function() {
	return model.Config.settings.swIdValidationUrlList;
}

model.Config.getSwIdSeachUrl = function() {
	return model.Config.settings.swIdSeachUrl;
}


//********* SECTION : FUNCTIONS TO POPULATE JSONMODELS WITH JSON RETURNED BY THE BACKEND REQUESTS *********\\

model.Config.authz = function() {
	return 'Basic ' + btoa(model.Config.settings.user + ':' + model.Config.settings.pwd)
}

model.Config.defaultHeaders = function () {
	return {
		'Authorization': model.Config.authz(),
		'X-Vulas-Version': model.Version.version,
		'X-Vulas-Component': 'appfrontend',
		'X-Vulas-Tenant': model.Config.getTenant(),
		'X-Vulas-Space': model.Config.getSpace()
	}
}

model.Config.loadSpaces = function(_t){
	var sUrl = model.Config.getSpacesServiceUrl();
	var oSpaceModel = new sap.ui.model.json.JSONModel();
	model.Config.loadDataSync (oSpaceModel,sUrl, 'GET',_t);
	oSpaceModel.setSizeLimit(oSpaceModel.getData().length);
	sap.ui.getCore().byId('idSpace').setModel(oSpaceModel);
}

model.Config.loadData = function(oModel,sUrl, method) {
	oModel.loadData(sUrl, null,true,method,false,true, model.Config.defaultHeaders());
	oModel.attachRequestFailed(function(oControlEvent){
		console.log(oControlEvent.getParameters().statusCode);
		if(oControlEvent.getParameters().statusCode=="503"){
			sap.m.MessageBox.warning(
				"The Vulas backend is in maintenance mode. Please come back later."
			);
		}
	});
}

model.Config.loadDataSync = function(oModel,sUrl, method, tenant) {
	var t = model.Config.getTenant();
	var headers = model.Config.defaultHeaders()
	if(tenant != null) {
		t = tenant
	}
	headers['X-Vulas-Tenant'] = t
	oModel.loadData(sUrl, null, false, method, false, false, headers);
	oModel.attachRequestFailed(function(oControlEvent){
		console.log(oControlEvent.getParameters().statusCode);
		if(oControlEvent.getParameters().statusCode=="503"){
			sap.m.MessageBox.warning(
				"The Vulas backend is in maintenance mode. Please come back later."
			);
		}
	});
}

//********* END SECTION : FUNCTIONS TO POPULATE JSONMODELS WITH JSON RETURNED BY THE BACKEND REQUESTS *********\\

//********* SECTION : UTIL FUNCTIONS USED TO HANDLE THE QUEUE OF BACKEND REQUESTS (USEFULL TO DROP THOSE NOT NECESSARY ANYLONGER) *********\\


var reqQueue = [];
var oStore = jQuery.sap.storage(jQuery.sap.storage.Type.local);


model.Config.addToQueue = function(oModel){
	reqQueue.push(oModel);
//	console.log("in queue " +reqQueue.length);
}

model.Config.remFromQueue = function(oModel){
	reqQueue.pop(oModel);
	//console.log("REM " +reqQueue.length);
}

model.Config.cleanRequests = function () {
	var model;
	//console.log("to be cleaned " +reqQueue.length);
	while (model = reqQueue.pop()) {
        // kill any queued requests
	//	console.log(model);
        model.destroy();
	}
	//console.log("cleaned " +reqQueue.length);
}

//********* END SECTION : UTIL FUNCTIONS USED TO HANDLE THE QUEUE OF BACKEND REQUESTS (USEFULL TO DROP THOSE NOT NECESSARY ANYLONGER) *********\\


//********* SECTION : FUNCTIONS USED TO CONFIGURE THE URLS *********\\

model.Config.getCvesServiceUrl = function(cve) {
	return model.Config.getHost() + "/cves/" + cve;
};



model.Config.getSpacesServiceUrl = function(){
	return model.Config.getHost() +"/spaces";
}

/**
 * the service url for applications
 */
model.Config.getMyAppsServiceUrl = function(_loadVulnerabityIcons) {
	var url = null;
	if(model.Config.getSkipEmpty())
		url =  model.Config.getHost()+"/apps?skipEmpty=true";
	else
		url =  model.Config.getHost()+"/apps?skipEmpty=false";
	return url;
};

model.Config.getArchiveVulnServiceUrl = function(g,a,gt,sec,l) {
	if(gt!=null)
		return model.Config.getHost()+"/libids/"+ g + "/" + a + "?latest="+l+"&greaterThanVersion=" + gt + "&secureOnly=" + sec ;
	else 
		return model.Config.getHost()+"/libids/"+ g + "/" + a + "?latest="+l+"&secureOnly=" + sec ;
};


//used to check whether the g a are known using the response status code 
model.Config.isArchiveInMaven = function(g,a) {
	return model.Config.getCiaHost()+"/artifacts/"+ g + "/" + a + "?packaging=jar&skipResponseBody=true";
};

model.Config.getUpdateMetricsUrl = function(g,a,v,sha1) {
		return model.Config.getHost()+"/apps/"+ g + "/" + a + "/" + v + "/deps/" + sha1 + "/updateMetrics";
};

model.Config.getUpdateChangesUrl = function(g,a,v,sha1) {
		return model.Config.getHost()+"/apps/"+ g + "/" + a + "/" + v + "/deps/" + sha1 + "/updateChanges";
};

/**
 * the service url for archives
 */
model.Config.getArchivesServiceUrl = function(g, a, v, lastChange) {
	let archiveServiceUrl = model.Config.getHost() + "/apps/" + g + "/" + a + "/" + v + "/deps"
	if (lastChange) {
		archiveServiceUrl += "?lastChange=" + lastChange
	}
	return archiveServiceUrl
}

/**
 * the service url for goal executions
 */
model.Config.getGoalExecutionsServiceUrl = function(g, a, v, lastChange) {
	let goalExecutionServiceUrl = model.Config.getHost() + "/apps/" + g + "/" + a + "/" + v + "/goals"
	if (lastChange) {
		goalExecutionServiceUrl += "?lastChange=" + lastChange
	}
	return goalExecutionServiceUrl
}

model.Config.getLatestGoalExecutionServiceUrl = function(g, a, v, lastChange) {
	let latestGoalExecutionServiceUrl = model.Config.getHost() + "/apps/"+ g + "/" + a + "/" + v + "/goals/latest?type=APP"
	if (lastChange) {
		latestGoalExecutionServiceUrl += "&lastChange=" + lastChange
	}
	return latestGoalExecutionServiceUrl
}

/**
 * the service url for goal execution details
 */
model.Config.getGoalExecutionDetailsServiceUrl = function(g,a,v,gid) {
	return model.Config.getHost()+"/apps/"+ g + "/" + a + "/" + v +"/goals/" + gid;
};


/**
 * the service url for archive properties
 */
model.Config.getArchivePropertiesServiceUrl = function(g,a,v,sha1) {
	return model.Config.getHost()+"/apps/"+ g + "/" + a + "/" + v +"/deps/" + sha1;
};

/**
 * the service url for used vulnerabilities
 */
model.Config.getUsedVulnerabilitiesServiceUrl = function(g, a, v, _incl_historical, _incl_unconfirmed, _add_excemption_info, lastChange) {
	let usedVulnerabilitiesServiceUrl = model.Config.getHost() + "/apps/" + g + "/" + a + "/" + v + "/vulndeps?includeHistorical=" + _incl_historical + "&includeAffected=true&includeAffectedUnconfirmed=" + _incl_unconfirmed + "&addExcemptionInfo=" + _add_excemption_info
	if (lastChange) {
		usedVulnerabilitiesServiceUrl += "&lastChange=" + lastChange
	}
	return usedVulnerabilitiesServiceUrl
}


model.Config.getSpaceServiceUrl = function(_token) {
	return model.Config.getHost()+"/spaces/"+ _token;
};

/**
 * the service url for vulnerability details
 */
model.Config.getVulnerabilityDetailsServiceUrl = function(g,a,v,sha1,bug) {
	return model.Config.getHost()+"/apps/"+ g + "/" + a + "/" + v +"/vulndeps/"+sha1+"/bugs/"+bug;
};

model.Config.getReachabilityGraphServiceUrl = function(g,a,v,sha1,bug,cid) {
	return model.Config.getHost()+"/apps/"+ g + "/" + a + "/" + v +"/deps/"+sha1+"/paths/"+bug+"/"+cid;
	
};

/**
 * the service url for packages including test coverage
 */
model.Config.getPackagesWithTestCoverageServiceUrl = function(g,a,v, lastChange) {
	let packagesWithTestCoverageServiceUrl = model.Config.getHost() + "/apps/"+ g + "/" + a + "/" + v
	if (lastChange) {
		packagesWithTestCoverageServiceUrl += "?lastChange=" + lastChange
	}
	return packagesWithTestCoverageServiceUrl
}

/**
 * the service url for packages including test coverage
 */
model.Config.getAppDepRatios = function(g, a, v, lastChange) {
	let appDepRatios = model.Config.getHost() + "/apps/" + g + "/" + a + "/" + v + "/metrics?excludedScopes=PROVIDED&excludedScopes=TEST"
	if (lastChange) {
		appDepRatios += "&lastChange=" + lastChange
	}
	return appDepRatios
}

model.Config.openWiki = function(href){
	if(model.Config.getWikiUrl()==""){
		sap.m.MessageBox.warning(
				"Wiki url not configured in the backend."
			);
	}
	window.open(model.Config.getWikiUrl() +href, '_blank').focus();
};


//********* END SECTION : FUNCTIONS USED TO CONFIGURE THE URLS *********\\


// TODO: CHECK WHETHER THE FOLLOWING FUNCTION IS STILL USED/USEFULL

/**
 * 
 */
//(function() {
//
//	// The "reponder" URL parameter defines if the app shall run with mock data
//	var responderOn = jQuery.sap.getUriParameters().get("responderOn");
//
//	// set the flag for later usage
//	model.Config.isMock = ("true" === responderOn)
//			|| !model.Config.getMyAppsServiceUrl()
//			|| !model.Config.getArchivesServiceUrl()
//			|| !model.Config.getUsedVulnerabilitiesServiceUrl()
//			|| !model.Config.getPackagesWithTestCoverageServiceUrl()
//			|| !model.Config.getArchivePropertiesServiceUrl()
//			|| !model.Config.getVulnerabilityDetailsServiceUrl()
//})();


