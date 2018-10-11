jQuery.sap.declare("model.Config");

model.Config = {
	mock : true,
	host : "/backend",
    ciaHost : "/cia",
    lang : "",
	user: "",
	pwd: "",
	user: "",
	pwd: "",
	skipEmpty : true
};

model.Config.setHost = function(_host){
	model.Config.host=_host;
}

model.Config.setCiaHost = function(_host){
	model.Config.ciaHost=_host;
}

model.Config.setLang = function(_lang){
	model.Config.lang=_lang;
}

model.Config.getLang = function(){
	return model.Config.lang;
}

model.Config.getHost = function(){
	return model.Config.host;
}
model.Config.setUser = function(_usr){
	model.Config.user=_usr;
}
model.Config.getUser = function(){
	return model.Config.user;
}

model.Config.setPwd = function(_pwd){
	model.Config.pwd=_pwd;
}

model.Config.setSkipEmpty = function(_skipEmpty){
	model.Config.skipEmpty=_skipEmpty;
}
model.Config.getSkipEmpty = function(){
	return model.Config.skipEmpty;
}

model.Config.loadData = function(oModel,sUrl, method) {
	var authz = 'Basic '+ btoa(model.Config.user + ":" + model.Config.pwd);
	oModel.loadData(sUrl, null,true,method,false,false,{'Authorization': authz,'X-Vulas-Version':model.Version.version,'X-Vulas-Component':'bugfrontend'});       
}

model.Config.loadDataSync = function(oModel,sUrl, method) {
	var authz = 'Basic '+ btoa(model.Config.user + ":" + model.Config.pwd);
	oModel.loadData(sUrl, null,false,method,false,false,{'Authorization': authz,'X-Vulas-Version':model.Version.version,'X-Vulas-Component':'bugfrontend'});       
}

model.Config.getCvesServiceUrl = function(cve) {
	//return model.Config.host+"/vulasfrontend/xs/assessment/getArchives.xsjs";
	return model.Config.host+"/cves/"+ cve;
}
/**
 * get the bugs list
 */
model.Config.getBugsAsUrl = function(){
    return model.Config.host+"/bugs?lang="+ model.Config.lang;
}

model.Config.getBugsAsBaseUrl = function(){
    return model.Config.host+"/bugs";
}


/**
 * Get detail for the selected bugid
 */
model.Config.getBugDetailsAsUrl = function(bugId, source){
    return model.Config.getBugsAsBaseUrl()+"/"+bugId;
}

model.Config.getBugDetailsAffVersion = function(bugId, source,g,a,v){
    return model.Config.getBugsAsBaseUrl()+"/"+bugId+"/affectedLibIds/"+g+"/"+a+"/"+v+"/?source="+source;
}

model.Config.getBugAllAffVersion = function(bugId){
    return model.Config.getBugsAsBaseUrl()+"/"+bugId+"/affectedLibIds";
}


model.Config.getBugDetailsLibrariesAsUrl = function(bugId){
    return model.Config.getBugsAsBaseUrl()+"/"+bugId+"/libraries";
}

/**
 * the service url for applications
 */
model.Config.getMyAppsServiceUrl = function() {
	//return model.Config.host+"/vulasfrontend/xs/assessment/getMyApplications.xsjs";
	if(model.Config.skipEmpty)
		return model.Config.host+"/apps?skipEmpty=true";
	else
		return model.Config.host+"/apps";
};

/**
 * the service url for archives
 */
model.Config.getArchivesServiceUrl = function(g,a,v) {
	//return model.Config.host+"/vulasfrontend/xs/assessment/getArchives.xsjs";
	return model.Config.host+"/apps/"+ g + "/" + a + "/" + v +"/deps";
};

/**
 * the service url for goal executions
 */
model.Config.getGoalExecutionsServiceUrl = function(g,a,v) {
	//return model.Config.host+"/vulasfrontend/xs/assessment/getGoalExecutions.xsjs";
	return model.Config.host+"/apps/"+ g + "/" + a + "/" + v +"/goals";
};

/**
 * the service url for goal execution details
 */
model.Config.getGoalExecutionDetailsServiceUrl = function() {
	return model.Config.host+"/vulasfrontend/xs/assessment/getGoalExecutionDetails.xsjs";
};

/**
 * the service url for user info
 */
/*model.Config.getUserServiceUrl = function() {
	return model.Config.host+"/vulasfrontend/xs/assessment/getUserInfo.xsjs";
};*/

/**
 * the service url for archive properties
 */
model.Config.getArchivePropertiesServiceUrl = function(g,a,v,sha1) {
	//return model.Config.host+"/vulasfrontend/xs/assessment/getArchiveDetails.xsjs";
	//console.log(model.Config.host+"/apps/"+ g + "/" + a + "/" + v +"/deps/" + sha1);
	return model.Config.host+"/apps/"+ g + "/" + a + "/" + v +"/deps/" + sha1;
};

/**
 * the service url for used vulnerabilities
 */
model.Config.getUsedVulnerabilitiesServiceUrl = function(g,a,v) {
	//return model.Config.host+"/vulasfrontend/xs/assessment/getUsedVulnerabilities.xsjs";
	return model.Config.host+"/apps/"+ g + "/" + a + "/" + v +"/vulndeps";
};

/**
 * the service url for vulnerability details
 */
model.Config.getVulnerabilityDetailsServiceUrl = function(g,a,v,sha1,bug) {
	//return model.Config.host+"/vulasfrontend/xs/assessment/getVulnerabilityDetails.xsjs";
//	console.log(model.Config.host+"/apps/"+ g + "/" + a + "/" + v +"/vulndeps/"+sha1+"/bugs/"+bug);
	return model.Config.host+"/apps/"+ g + "/" + a + "/" + v +"/vulndeps/"+sha1+"/bugs/"+bug;
};

model.Config.getReachabilityGraphServiceUrl = function(g,a,v,sha1,bug,cid) {
	//return model.Config.host+"/vulasfrontend/xs/assessment/getReachabilityGraph.xsjs";
	return model.Config.host+"/apps/"+ g + "/" + a + "/" + v +"/deps/"+sha1+"/paths/"+bug+"/"+cid;
};

/**
 * the service url for packages including test coverage
 */
model.Config.getPackagesWithTestCoverageServiceUrl = function(g,a,v) {
	//return model.Config.host+"/vulasfrontend/xs/assessment/getPackagesWithTestCoverage.xsjs";
	return model.Config.host+"/apps/"+ g + "/" + a + "/" + v;
};

/**
 * the service url for CVE details
 */
model.Config.getCveDetailsUrl = function() {
	return model.Config.host+"/vulasfrontend/xs/assessment/getCveDetails.xsjs";
};

model.Config.getDiff = function() {
	return model.Config.ciaHost+"/constructs/diff";
};

model.Config.getAffectedConstructsSha1Bugid = function(sha1,bugid){
    //return model.Config.host+"/libs/"+sha1+"/bugs/"+bugid+"/constructIds";
    return model.Config.host+"/libs/"+sha1+"/bugs/"+bugid+"/affConstructChanges";
};


model.Config.getCiaHostUrl = function() {
    return model.Config.ciaHost;
};

model.Config.getAffectedMavenArtifacts = function(ga) {
  return model.Config.getCiaHostUrl() + "/artifacts/" + ga;
};

model.Config.getLibraryDetailsUrl = function(sha1){
  return model.Config.host+"/libs/"+sha1;  
};

model.Config.getLibraryApplicationsUrl = function(sha1){
  return model.Config.host+"/libs/"+sha1+"/apps";  
};

model.Config.getMavenId = function (affectedVersion){
    var mid = {};
    if ( affectedVersion.lib != null ){ 
        if (affectedVersion.lib.libraryId != null ){
            mid.group = affectedVersion.lib.libraryId.group;
            mid.artifact = affectedVersion.lib.libraryId.artifact;
            mid.version = affectedVersion.lib.libraryId.version;
        }
        mid.sha1 = affectedVersion.lib.sha1;
    } else if ( affectedVersion.libraryId != null ){
        mid.group = affectedVersion.libraryId.group;
        mid.artifact = affectedVersion.libraryId.artifact;
        mid.version = affectedVersion.libraryId.version;
        mid.sha1 = "";
    } 
    return mid;
};



model.Config.uploadManualAssessment = function(data, pbugid ){
    var baseurl = model.Config.host + "/bugs/";
    var trail = "/affectedLibIds?source=MANUAL";
    var fullUrl = baseurl + pbugid + trail;
    var elementArray = [];
    sap.ui.core.BusyIndicator.show();
    for ( x in data['affectedVersions'] ){
        var jo = null;
        if ( (data['affectedVersions'][x]['manual'] == false)||(data['affectedVersions'][x]['manual'] == true) ){ // only if manual is set
            // 2 cases : GAV or only sha1
            if ( typeof(data['affectedVersions'][x]['group']) != 'undefined' ) {
                jo = {
                    "libraryId" : {
                        "group" : data['affectedVersions'][x]['group'],
                        "artifact" : data['affectedVersions'][x]['artifact'],
                        "version" : data['affectedVersions'][x]['version']
                    },
                    "lib": null,
                    "affectedcc": [],
                    "source" : "MANUAL",
                    "affected" : data['affectedVersions'][x]['manual'],
                    "explanation": "Manual review(frontend)"
                };
            } else {
                jo = {
                    "libraryId": null,
                    "lib" : {
                        "sha1" : data['affectedVersions'][x]['sha1']
                    },
                    "affectedcc": [],
                    "source" : "MANUAL",
                    "affected" : data['affectedVersions'][x]['manual'],
                    "explanation": "Manual review(frontend)"
                };
            }
            elementArray.push(jo);
        }
    }
    // delete before post
    
    $.ajax({
        type: "DELETE",
        url: fullUrl,
        headers : {'content-type': "application/json",'cache-control': "no-cache" },
        success: function(msg){
            $.ajax({
                type: "POST",
                url : fullUrl,
                headers : {'content-type': "application/json",'cache-control': "no-cache" },
                data : JSON.stringify(elementArray),
                success : function(msg){
                    sap.ui.commons.MessageBox.alert("Data correctly updated");
                    sap.ui.core.BusyIndicator.hide();
                },
                error : function(msg){
                    sap.ui.commons.MessageBox.alert("error POST");
                    sap.ui.core.BusyIndicator.hide();
                }
            });
        }, 
        error : function(msg){
            if ( msg['status']==404 ){
                console.log("404");
                console.log(data);
                $.ajax({
                    type: "POST",
                    url : fullUrl,
                    headers : {'content-type': "application/json",'cache-control': "no-cache" },
                    data : JSON.stringify(elementArray),
                    success : function(msg){
                        sap.ui.commons.MessageBox.alert("Data correctly updated");
                        sap.ui.core.BusyIndicator.hide();
                    },
                    error : function(msg){
                        sap.ui.commons.MessageBox.alert("error PUT");
                        sap.ui.core.BusyIndicator.hide();
                    }
                });
            } else {
                sap.ui.commons.MessageBox.alert("error DELETE");
                sap.ui.core.BusyIndicator.hide();
            }
        }
    });
    
    //console.log(JSON.stringify(elementArray));
};

model.Config.uploadCVEDescription = function(newjson, url){
    $.ajax({
        type: "PUT",
        url : url,
        headers : {'content-type': "application/json",'cache-control': "no-cache" },
        data : JSON.stringify(newjson),
        success : function(msg){
            sap.ui.commons.MessageBox.alert("Data correctly updated");
        },
        error : function(msg){
            sap.ui.commons.MessageBox.alert("error PUT");
        }
    });
    
};


/*model.Config.getToLookupList = function(affectedVersions){
    var toLookup = {};
    for ( var k in Object.keys(affectedVersions)){
        var mavenID = model.Config.getMavenId(affectedVersions[k]);
        if (typeof(mavenID.group)!=='undefined'){
            var key = mavenID.group+":"+mavenID.artifact;
            toLookup[key] = true;
        }
    }
    for ( var x in toLookup){
        console.log("tolookup:\n"+x);
    }
    return toLookup;
};*/

/**
 * 
 */
(function() {

	// The "reponder" URL parameter defines if the app shall run with mock data
	var responderOn = jQuery.sap.getUriParameters().get("responderOn");

	// set the flag for later usage
	model.Config.isMock = ("true" === responderOn)
			|| !model.Config.getMyAppsServiceUrl()
			|| !model.Config.getArchivesServiceUrl()
			|| !model.Config.getUsedVulnerabilitiesServiceUrl()
			|| !model.Config.getPackagesWithTestCoverageServiceUrl()
			|| !model.Config.getArchivePropertiesServiceUrl()
			|| !model.Config.getVulnerabilityDetailsServiceUrl()
})();
