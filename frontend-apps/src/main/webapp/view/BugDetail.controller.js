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
var bugId;
var archiveId;
var app = {};
var bAllShown = false;

sap.ui.controller("view.BugDetail", {

	onInit : function() {
		this.router = sap.ui.core.UIComponent.getRouterFor(this);
		this.router.attachRoutePatternMatched(this._handleRouteMatched, this);

	},

	_handleRouteMatched : function(evt) {
		if (evt.getParameter("name") !== "bugDetail") {
			return;
		}
		if (app.groupid!=evt.getParameter("arguments").group ||
				app.artifactid!=evt.getParameter("arguments").artifact ||
				app.version!=evt.getParameter("arguments").version ||
				bugId!=evt.getParameter("arguments").bugid ||
				archiveId!=evt.getParameter("arguments").archiveid ||
				workspaceSlug!=evt.getParameter("arguments").workspaceSlug){


			bugId = evt.getParameter("arguments").bugid;
			archiveId = evt.getParameter("arguments").archiveid;

			app.groupid = evt.getParameter("arguments").group;
			app.artifactid = evt.getParameter("arguments").artifact;
			app.version = evt.getParameter("arguments").version;
			workspaceSlug = evt.getParameter("arguments").workspaceSlug;
			app.workspaceSlug = evt.getParameter("arguments").workspaceSlug;

			model.Config.cleanRequests();
			this.loadDataIntoView();

			var place = this.getView().byId('trendGraph');

			// create the node
			var node = document.createElement('div');
			node.className = 'pane';
			this.node = node;
			//document.getElementById("trend")

			trends.embed.renderExploreWidgetTo(this.node,"TIMESERIES", {"comparisonItem":[{"keyword":bugId,"geo":"","time":"today 1-m"}],"category":0,"property":""}, {"exploreQuery":"q="+bugId+"&date=today 1-m","guestPath":"https://trends.google.com:443/trends/embed/"});

			place.setDOMContent(this.node);  


		}
	},

	loadDataIntoView : function() {
		if(bugId && archiveId) {
			// Get page and set title
			var bugDetailPage = this.getView().byId('idBugDetailPage');
			bugDetailPage.setTitle(bugId);

			var data = [];
			var emptyModel = new sap.ui.model.json.JSONModel();
			emptyModel.setData(data);
			bugDetailPage.setModel(emptyModel);
			bugDetailPage.getModel(emptyModel,"osspatch");
			bugDetailPage.getModel(emptyModel,"cveModel");
			bugDetailPage.getModel(emptyModel,"app");

			//clear previous model
//			var data =[];
//			var oldBug=bugDetailPage.getModel();
//			if(oldBug!=undefined){
//			oldBug.setData(data,false);
//			oldBug.refresh();
//			}
//			var oldoss = bugDetailPage.getModel("osspatch");
//			if(oldoss!=undefined){
//			oldoss.setData(data,false);
//			oldoss.refresh();
//			}
//			var oldCve = bugDetailPage.getModel("cveModel");
//			if(oldCve!=undefined){
//			oldCve.setData(data,false);
//			oldCve.refresh();
//			}
//			var oldApp = bugDetailPage.getModel("app");
//			if(oldApp!=undefined){
//			oldApp.setData(data,false);
//			oldApp.refresh();
//			}

			// Bug details
			var bugDetailModel = new sap.ui.model.json.JSONModel();
			if (!model.Config.isMock) {
				sUrl = model.Config.getVulnerabilityDetailsServiceUrl(app.groupid,app.artifactid,app.version,archiveId,bugId,model.Config.getVulnDepOrigin(),model.Config.getBundledDigest(),model.Config.getBundledGroup(),model.Config.getBundledArtifact(),model.Config.getBundledVersion());
				/*	if (!bAllShown) {
    				sUrl = sUrl + "?bugid=" + bugId + "&archiveid=" + archiveId + "&groupid=" + app.groupid + "&artifactid=" + app.artifactid + "&version=" + app.version;
    			} else {
    				sUrl = sUrl + "?bugid=" + bugId + "&archiveid=" + archiveId + "&showAll=true" + "&groupid=" + app.groupid + "&artifactid=" + app.artifactid + "&version=" + app.version;
    			}*/
				model.Config.addToQueue(bugDetailModel);
				model.Config.loadData (bugDetailModel,sUrl, 'GET');
				bugDetailPage.setModel(bugDetailModel);
				/*$.get(sUrl, function(data) {
    				bugDetailModel.setData(data);
    				bugDetailPage.setModel(bugDetailModel);
    			}.bind(this));*/

				//extract repo & commit info

				var osspatch = {};
				bugDetailModel.attachRequestCompleted(function() {
					model.Config.remFromQueue(bugDetailModel);
					// Loop over construct changes to collect patch-related info (repo and commit ids)
					var cc = bugDetailModel.getObject("/constructList") ;
					if(cc===null || cc.length===0) {
						osspatch.repo = "";
						osspatch.revisions = [];
					} else {
						var revisions = [];
						for(var i = 0; i < cc.length; i++) {
							if(revisions.indexOf(cc[i].constructChange.commit)==-1)
								revisions.push(cc[i].constructChange.commit);
						}
						osspatch.repo=cc[0].constructChange.repo;
						osspatch.revisions=revisions;
					}

					// Populate model
					//console.log(JSON.stringify(osspatch));
					var patchModel = new sap.ui.model.json.JSONModel();
					patchModel.setData(osspatch);
					bugDetailPage.setModel(patchModel, "osspatch");

					var cveDetailModel = new sap.ui.model.json.JSONModel();
					var bug = bugDetailModel.getProperty("/bug"); 

					// Always get bug info from backend
					var cveUrl = model.Config.getCvesServiceUrl(bugId);	    		
					model.Config.loadDataSync(cveDetailModel, cveUrl, 'GET');	
					var cve = cveDetailModel.getData(cve);

					// References
					var refs=[];
					if(bug.reference!=[]){
						for(var i=0;i<bug.reference.length;i++){
							var ref = {};
							ref.url = bug.reference[i];
							refs.push(ref);
						}
					}
					cveDetailModel.setProperty("/reference", refs);

					// Vuln description
					if(bug.description===null) {
						if(bug.descriptionAlt===null) {
							cve.summary = "n/a"; 
						} else {
							cve.summary  = bug.descriptionAlt;
						}
					}
					else {
						if(bug.descriptionAlt===null) {
							cve.summary  = bug.description;
						} else {
							cve.summary  = bug.description + " Addendum: " + bug.descriptionAlt;
						}
					}

					cve.createdAt = bug.createdAt;
					cve.cvssDisplayString = bug.cvssDisplayString;
					
					cveDetailModel.setData(cve);

					//$.get(cveUrl, function(cve) {
					//console.log(JSON.stringify(cve));
					//cveDetailModel.setData(cve);
					bugDetailPage.setModel(cveDetailModel,"cveModel");
					//}.bind(this));
				});
			}

			// App under analysis
			var appModel = new sap.ui.model.json.JSONModel();
			appModel.setData(app);
			bugDetailPage.setModel(appModel, "app");

		}

	},

	showOrHide : function() {
		if (!bAllShown) {
			bAllShown = true;
			this.getView().byId("showAllButton")
			.setText("Hide");
			this.loadDataIntoView();
		} else {
			bAllShown = false;
			this.getView().byId("showAllButton").setText("Show All");
			this.loadDataIntoView();
		}
	},

	onChangeConstructItemTap : function(oEvent) {

		var path = oEvent.getSource().getBindingContext().getPath();
		var index = path.toString().split("/")[path.toString().split("/").length-1];
		//var graphid = this.getView().byId('idBugDetailPage').getModel().getData().constructList[index].reachabilityGraph.id;
		var change = encodeURIComponent(this.getView().byId('idBugDetailPage').getModel().getData().constructList[index].constructChange.constructId.qname);
		var group = this.getView().byId('idBugDetailPage').getModel("app").getData().groupid;
		console.log(path + " " + index + " " + change + " " + group + app.artifactid + " " + app.version + " "+ archiveId + " " + bugId);
		const workspaceSlug = model.Config.getSpace()
		this.router.navTo("graphDetail", {
			workspaceSlug: workspaceSlug,
			group : app.groupid,
			artifact : app.artifactid,
			version : app.version,
			archiveid : archiveId,
			bugid : bugId,
			//	graphid : graphid,
			change : change
		});

	},

//	writeEmail : function(oEvent) {
//	var bugDetailPage = this.getView().byId('idBugDetailPage');
//	var data = bugDetailPage.getModel().getData();

//	//data.archiveDetails.filename doesn't exist any longer!
//	var emailText  = "Dear Vulas team,\n\nPlease note that the archive '" + data.archiveDetails.filename + "' (" + data.archiveid + ") is not affected by vulnerability " + data.cve.cve_id + " [1].\n\n";
//	var emailText2 = "Please mark this release as non-vulnerable so that Vulas does not raise an alert when discovering it.\n\n";
//	var emailText3 = "[EXPLAIN HERE WHY THIS RELEASE IS NOT VULNERABLE, E.G., A LINK TO VENDOR INFORMATION ON AFFECTED RELEASES]\n\n";
//	var emailText4 = "Kind regards, [XXX]\n\n";
//	var emailUrl   = "[1] " + window.location.href + "\n";
//	var emailSubject = "[VULAS] Archive '" + data.archiveDetails.filename + "' not affected by vulnerability " + data.cve.cve_id;
//	sap.m.URLHelper.triggerEmail("DL VULAS", emailSubject, emailText + emailText2 + emailText3 + emailText4 + emailUrl);
//	},

	openNVD : function(oEvent) {
		var bugDetailPage = this.getView().byId('idBugDetailPage');
		var data = bugDetailPage.getModel().getData();
		//var url = data.cve.link;
//		var url =null ;
//		if(data.bug.url!=null)
//		url = data.bug.url;
//		else
//		url = "https://web.nvd.nist.gov/view/vuln/detail?vulnId="+data.bug.bugId;
		var url = "https://web.nvd.nist.gov/view/vuln/detail?vulnId=" + data.bug.bugId;
		this.openLink(url, 'nvd');
	},

	openExploitDb : function(oEvent) {
		var bugDetailPage = this.getView().byId('idBugDetailPage');
		var data = bugDetailPage.getModel().getData();
		var url = "https://www.exploit-db.com/search?cve=" + data.bug.bugId.substring(4);
		this.openLink(url, 'edb');
	},

	openLink : function(_url, _window) {
		window.open(_url, _window).focus();
	},

	onExit : function() {
		var bugDetailPage = this.getView().byId('idBugDetailPage');
		if (bugDetailPage.getModel()) {
			bugDetailPage.getModel().destroy();
		}
		if (bugDetailPage.getModel("osspatch")) {
			bugDetailPage.getModel("osspatch").destroy();
		}
		if (bugDetailPage.getModel("cveModel")) {
			bugDetailPage.getModel("cveModel").destroy();
		}
		if (bugDetailPage.getModel("app")) {
			bugDetailPage.getModel("app").destroy();
		}
	},
	
	openWiki : function(evt){
		model.Config.openWiki("user/manuals/frontend/#vulnerabilities-details");
	},

	handleNavBack : function() {
		window.history.go(-1);
	}
});

