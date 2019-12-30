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
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
var bugId;
var sha1;

var archiveId;
var app = {};
var bAllShown = false;

var repo;

sap.ui.controller("view.CheckversionDetail", {

	onInit : function() {
		this.router = sap.ui.core.UIComponent.getRouterFor(this);
		this.router.attachRoutePatternMatched(this._handleRouteMatched, this);
	},

	_handleRouteMatched : function(evt) {
                sha1 = evt.getParameter("arguments").sha1;
                bugId = evt.getParameter("arguments").bugId;  
		this.loadDataIntoView();
	},

	loadDataIntoView : function() {
	    if(bugId && sha1) {
                // Get page and set title
                var bugDetailPage = this.getView().byId('idBugDetailPage');
                bugDetailPage.setBusy(true);
                var changeConstructListTree = this.getView().byId('idChangeConstructListTree');
                var changeConstructList = this.getView().byId('idChangeConstructList');
    		bugDetailPage.setTitle(bugId);
    		
    		// Bug details
    		var bugDetailModel = new sap.ui.model.json.JSONModel();
    		if (!model.Config.isMock) {
                    var url = model.Config.getAffectedConstructsSha1Bugid(sha1, bugId);
                    console.log(url);

                    //sUrl = model.Config.getVulnerabilityDetailsServiceUrl(app.groupid,app.artifactid,app.version,archiveId,bugId);

                    model.Config.loadData (bugDetailModel,url, 'GET');
                    bugDetailModel.attachRequestCompleted(function(){
                        var elements = bugDetailModel.getObject('/');
                        for ( var x in elements ) {
                            if ( typeof(elements[x]['cc']['repo']) !== 'undefined' ){
                                repo = elements[x]['cc']['repo'];
                                break;
                            }
                        }
                        var repoModel = new sap.ui.model.json.JSONModel();
                        var repoObj = {repo:repo};
                        repoModel.setData(repoObj);
                        bugDetailPage.setModel(repoModel,"repoModel");

                        oRootData = model.Utils.parseAffectedCCs(elements);
                        var oConstructsTreeModel = new sap.ui.model.json.JSONModel();
                        oConstructsTreeModel.setData(oRootData);
                        bugDetailPage.setModel(oConstructsTreeModel);
                        //console.log(oRootData);
                       /* var elements = bugDetailModel.getObject('/');
                        for ( var x in elements){
                            var bb = elements[x]['cc']['buggyBody'];
                            if ( typeof(bb) !== 'undefined'){
                                model.Utils.parseConstructJson(bb);/*
                                jsonBB = JSON.parse(bb);
                                //console.log(jsonBB['ast'][0]);
                                var methodName = jsonBB['ast'][0]['Value'];
                                var entityType = jsonBB['ast'][0]['EntityType'];
                                var locs = jsonBB['ast'][0]['C'];
                                console.log(methodName + "," + entityType);
                                console.log(locs);
                            }

                        }*/
                        //changeConstructList.setModel(bugDetailModel);
                    });
                    //bugDetailPage.setModel(bugDetailModel, "bugDetailModel");

                    //extract repo & commit info

                    /*var osspatch = {};
                    bugDetailModel.attachRequestCompleted(function() {

                            var cc = bugDetailModel.getObject("/constructList") ;
                            var revisions = [];
                            for(var i = 0; i < cc.length; i++) {
                                    if(revisions.indexOf(cc[i].constructChange.commit)==-1)
                                            revisions.push(cc[i].constructChange.commit);
                            }
                            osspatch.repo=cc[0].constructChange.repo;
                            osspatch.revisions=revisions;
                            //console.log(JSON.stringify(osspatch));
                            var patchModel = new sap.ui.model.json.JSONModel();
                    patchModel.setData(osspatch);
                    bugDetailPage.setModel(patchModel, "osspatch");
                    });*/
                    bugDetailPage.setBusy(false);
    		}
    		
    		//var cveUrl = "http://cve.circl.lu/api/cve/"+bugId;
    		var cveUrl = model.Config.getCvesServiceUrl(bugId);
    		var cveDetailModel = new sap.ui.model.json.JSONModel();
    		model.Config.loadData (cveDetailModel,cveUrl, 'GET');
                bugDetailPage.setModel(cveDetailModel,"cveModel");
	    }
	},
        
	onConstructNameClick : function(oEvent){
            var mtype = oEvent.getParameters().rowBindingContext.getObject("cc/constructChangeType");
            if ( mtype=="MOD" ){
                if ( oEvent.getParameters().columnIndex == 4 ){
                    var qname = oEvent.getParameters().rowBindingContext.getObject("cc/constructId/qname");
                    var commit = oEvent.getParameters().rowBindingContext.getObject("cc/commit");
                    // sha1, bugid, qname
                    console.log(qname);
                    this.router.navTo("ASTViewer", {
                                sha1 : sha1,
                                bugId : bugId,
                                qname : qname,
                                commit : commit
                        });
                } 
            } else {
                sap.ui.commons.MessageBox.alert("Only avaylable for MOD constructs", {title: "Error"});
            }
        },
        
        onConstructListTreeClick : function(oEvent){
            var qname = oEvent.getParameters().rowBindingContext.getObject("name");
            var mtype = oEvent.getParameters().rowBindingContext.getObject("overallChg");
            var repo =  oEvent.getParameters().rowBindingContext.getObject("repo");
            if ( mtype=="MOD" ){
                if ( oEvent.getParameters().columnIndex == 0 ){
                    // sha1, bugid, qname
                    this.router.navTo("ASTViewer", {
                                sha1 : sha1,
                                bugId : bugId,
                                qname : qname,
                                repo : repo.replace(/\//g, "+") // temporary
                        });
                } 
            } else {
               sap.ui.commons.MessageBox.alert("Only avaylable for MOD constructs", {title: "Error"});
            }
        },
        
        onCollapseAllConstructs : function(oEvent){
            var ChangeConstructListTree = this.getView().byId('idChangeConstructListTree');
            ChangeConstructListTree.collapseAll();
        },
        
        onExpandAllConstructs : function(oEvent){
            var ChangeConstructListTree = this.getView().byId('idChangeConstructListTree');
            ChangeConstructListTree.expandToLevel(2);
        },
        
	writeEmail : function(oEvent) {
	    var bugDetailPage = this.getView().byId('idBugDetailPage');
	    var data = bugDetailPage.getModel().getData();
	    
	    var emailText  = "Dear Vulas team,\n\nPlease note that the archive '" + data.archiveDetails.filename + "' (" + data.archiveid + ") is not affected by vulnerability " + data.cve.cve_id + " [1].\n\n";
            var emailText2 = "Please mark this release as non-vulnerable so that Vulas does not raise an alert when discovering it.\n\n";
            var emailText3 = "[EXPLAIN HERE WHY THIS RELEASE IS NOT VULNERABLE, E.G., A LINK TO VENDOR INFORMATION ON AFFECTED RELEASES]\n\n";
            var emailText4 = "Kind regards, [XXX]\n\n";
            var emailUrl   = "[1] " + window.location.href + "\n";
            var emailSubject = "[VULAS] Archive '" + data.archiveDetails.filename + "' not affected by vulnerability " + data.cve.cve_id;
            sap.m.URLHelper.triggerEmail("DL VULAS", emailSubject, emailText + emailText2 + emailText3 + emailText4 + emailUrl);
	},
	
	openNVD : function(oEvent) {
	    var bugDetailPage = this.getView().byId('idBugDetailPage');
	    var data = bugDetailPage.getModel().getData();
	    //var url = data.cve.link;
	    var url = "https://web.nvd.nist.gov/view/vuln/detail?vulnId="+data.bug.bugId;
	    this.openLink(url, 'nvd');
	},
	
	openExploitDb : function(oEvent) {
	    var bugDetailPage = this.getView().byId('idBugDetailPage');
	    var data = bugDetailPage.getModel().getData();
	    //var url = "https://www.exploit-db.com/search/?action=search&cve=" + data.cve.cve_id.substring(4);
	    var url = "https://www.exploit-db.com/search/?action=search&cve=" + data.bug.bugId.substring(4);
	    this.openLink(url, 'edb');
	},
		
	openLink : function(_url, _window) {
	    window.open(_url, _window).focus();
	},

	onExit : function() {

	},

	handleNavBack : function() {
		window.history.go(-1);
	}
});

