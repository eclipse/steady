/*
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
jQuery.sap.declare("model.Config");
jQuery.sap.require("jquery.sap.storage");

model.Config = {
		cookie : {  host : "/backend",
			ciaHost : "/cia",
			token : ""
		},
	lang : "",
	user: "",
	pwd: "",
	user: "",
	pwd: "",
	skipEmpty : true,
    wikiUrl : "https://eclipse.github.io/steady/"
};

var oStore = jQuery.sap.storage(jQuery.sap.storage.Type.local);

//populates the settings with the data read from the cookie
//called in the init of Master.controller
model.Config.setModel = function(_m){
	if(_m) {
		if (typeof _m === 'string') {
			model.Config.cookie = JSON.parse(_m)
		} else {
			model.Config.cookie = _m
		}
	}
}

model.Config.setHost = function(_host) {
	model.Config.cookie.host=_host;
	oStore.put("vulas-frontend-settings", model.Config.cookie)
}

model.Config.getHost = function() {
	return model.Config.cookie.host;
}

model.Config.setCiaHost = function(_host){
	model.Config.cookie.ciaHost=_host;
	oStore.put("vulas-frontend-settings", model.Config.cookie)
}

model.Config.getCiaHost = function() {
	return model.Config.cookie.ciaHost;
}

model.Config.setToken = function(_token){
	model.Config.cookie.token =_token;
	oStore.put("vulas-frontend-settings", model.Config.cookie)
}

model.Config.getToken = function() {
	return model.Config.cookie.token;
}

model.Config.setLang = function(_lang){
	model.Config.lang=_lang;
}

model.Config.getLang = function(){
	return model.Config.lang;
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
	return model.Config.getHost()+"/cves/"+ cve;
}

/**
 * get the bugs list
 */
model.Config.getBugsAsUrl = function(){
    return model.Config.getHost()+"/bugs?lang="+ model.Config.lang;
}

model.Config.getBugsAsBaseUrl = function(){
    return model.Config.getHost()+"/bugs";
}

/**
 * Get details for the selected bugid
 */
model.Config.getBugDetailsAsUrl = function(bugId, source){
    return model.Config.getBugsAsBaseUrl()+"/"+bugId;
}

/**
 * Get affectedLibrary for selected bugid and GAV
 */
model.Config.getBugDetailsAffVersion = function(bugId, source,g,a,v){
    return model.Config.getBugsAsBaseUrl()+"/"+bugId+"/affectedLibIds/"+g+"/"+a+"/"+v+"/?source="+source;
}

/**
 * Get all affectedLibraries for selected bugid
 */
model.Config.getBugAllAffVersion = function(bugId){
    return model.Config.getBugsAsBaseUrl()+"/"+bugId+"/affectedLibIds";
}

/**
 * Get all libraries for selected bugid
 */
model.Config.getBugDetailsLibrariesAsUrl = function(bugId){
    return model.Config.getBugsAsBaseUrl()+"/"+bugId+"/libraries";
}

model.Config.getDiff = function() {
	return model.Config.getCiaHost()+"/constructs/diff";
};

/**
 * Get affected construct changes for selected bugid and library digest
 */
model.Config.getAffectedConstructsSha1Bugid = function(sha1,bugid){
    return model.Config.getHost()+"/libs/"+sha1+"/bugs/"+bugid+"/affConstructChanges";
};

/**
 * Get all artifacts for a given GA
 */
model.Config.getAffectedMavenArtifacts = function(ga) {
  return model.Config.getCiaHost() + "/artifacts/" + ga;
};

/**
 * Get library for selected digest
 */
model.Config.getLibraryDetailsUrl = function(sha1){
  return model.Config.getHost()+"/libs/"+sha1;  
};

/**
 * Get all applications with dependency on selected digest
 */
model.Config.getLibraryApplicationsUrl = function(sha1){
  return model.Config.getHost()+"/libs/"+sha1+"/apps";  
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
    var baseurl = model.Config.getHost() + "/bugs/";
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
    
    
    $.ajax({
        type: "PUT",
        url : fullUrl,
        headers : {'content-type': "application/json",'cache-control': "no-cache", 'X-Vulas-Client-Token' : model.Config.getToken() },
        data : JSON.stringify(elementArray),
        success : function(msg){
            sap.ui.commons.MessageBox.alert("Data correctly updated via PUT");
            sap.ui.core.BusyIndicator.hide();
        },
        error : function(msg){
        	  if ( msg['status']==404 ){
                  console.log("404");
                  console.log(data);
                  $.ajax({
                      type: "POST",
                      url : fullUrl,
                      headers : {'content-type': "application/json",'cache-control': "no-cache",'X-Vulas-Client-Token' : model.Config.getToken() },
                      data : JSON.stringify(elementArray),
                      success : function(msg){
                          sap.ui.commons.MessageBox.alert("Data correctly updated via POST");
                          sap.ui.core.BusyIndicator.hide();
                      },
                      error : function(msg){
                          sap.ui.commons.MessageBox.alert("error POST");
                          sap.ui.core.BusyIndicator.hide();
                      }
                  });
              } else {
                  sap.ui.commons.MessageBox.alert("error PUT");
                  sap.ui.core.BusyIndicator.hide();
              }
       }
    });
    
   //old mechanisma via DELETE-POST
//    $.ajax({
//        type: "DELETE",
//        url: fullUrl,
//        headers : {'content-type': "application/json",'cache-control': "no-cache" },
//        success: function(msg){
//            $.ajax({
//                type: "POST",
//                url : fullUrl,
//                headers : {'content-type': "application/json",'cache-control': "no-cache" },
//                data : JSON.stringify(elementArray),
//                success : function(msg){
//                    sap.ui.commons.MessageBox.alert("Data correctly updated");
//                    sap.ui.core.BusyIndicator.hide();
//                },
//                error : function(msg){
//                    sap.ui.commons.MessageBox.alert("error POST");
//                    sap.ui.core.BusyIndicator.hide();
//                }
//            });
//        }, 
//        error : function(msg){
//            if ( msg['status']==404 ){
//                console.log("404");
//                console.log(data);
//                $.ajax({
//                    type: "POST",
//                    url : fullUrl,
//                    headers : {'content-type': "application/json",'cache-control': "no-cache" },
//                    data : JSON.stringify(elementArray),
//                    success : function(msg){
//                        sap.ui.commons.MessageBox.alert("Data correctly updated");
//                        sap.ui.core.BusyIndicator.hide();
//                    },
//                    error : function(msg){
//                        sap.ui.commons.MessageBox.alert("error PUT");
//                        sap.ui.core.BusyIndicator.hide();
//                    }
//                });
//            } else {
//                sap.ui.commons.MessageBox.alert("error DELETE");
//                sap.ui.core.BusyIndicator.hide();
//            }
//        }
//    });
    
    //console.log(JSON.stringify(elementArray));
};

model.Config.uploadCVEDescription = function(newjson, url){
    $.ajax({
        type: "PUT",
        url : url,
        headers : {'content-type': "application/json",'cache-control': "no-cache",'X-Vulas-Client-Token' : model.Config.getToken() },
        data : JSON.stringify(newjson),
        success : function(msg){
            sap.ui.commons.MessageBox.alert("Data correctly updated");
        },
        error : function(msg){
            sap.ui.commons.MessageBox.alert("error PUT");
        }
    });
    
};

model.Config.openWiki = function(href){
	if(model.Config.getWikiUrl()==""){
		sap.m.MessageBox.warning(
				"Wiki url not configured in the backend."
			);
	}
	window.open(model.Config.getWikiUrl() + href, '_blank').focus();
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


