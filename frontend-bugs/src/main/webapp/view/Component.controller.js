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
var groupId = "";
var artifactId = "";
var version = "";
var bugId = "";

var cve={};
sap.ui.controller(
				"view.Component",
				{

					onInit : function() {
						this.router = sap.ui.core.UIComponent.getRouterFor(this);
						this.router.attachRoutePatternMatched(this._handleRouteMatched, this);
					},

					_handleRouteMatched : function(evt) {
						if (evt.getParameter("name") !== "component") {
							return;
						}
						if (bugId != evt.getParameter("arguments").bugId) {
							bugId = evt.getParameter("arguments").bugId;
							this.getView().byId('componentPage').setTitle(bugId);
							this.loadData();
						}
					},
					
					loadData : function() {

						var sUrl = "";
						var oBugAnalysisModel = new sap.ui.model.json.JSONModel();
						var oBugAVModel = new sap.ui.model.json.JSONModel();
						var bugDetailModel = new sap.ui.model.json.JSONModel();
						var oAffectedMavenVersionsModel = new sap.ui.model.json.JSONModel();

						var oBugAnalysisView = this.getView().byId("idBugAnalysisList");
						var treeTablePath = this.getView().byId('idChangeListPath');
						var treeTableCommit = this.getView().byId('idChangeListCommit');
						var idCveModelDetailsView = this.getView().byId("idCveModelDetails");
						
						//Update tab controls
						var idCveModelDetailsUpdateView = this.getView().byId("idCveModelDetailsUpdate");
						var idCveUpdateDescription = this.getView().byId("tfDescription");
						var idCveUpdateDescriptionAlt = this.getView().byId("tfDescriptionAlt");
						var idCveUpdateBugIdAlt = this.getView().byId("tfBugIdAlt");
						var idCveUpdateCvssScore = this.getView().byId("tfCVSSScore");
						var idCveUpdateCvssVersion = this.getView().byId("tfCVSSVersion");
						var idCveUpdateCvssVector = this.getView().byId("tfCVSSVector");
						var idCveUpdateOrigin = this.getView().byId("tfOrigin");
						var idCveUpdateOrigin2 = this.getView().byId("sOrigin");
						var idCveUpdateMaturity = this.getView().byId("tfMaturity");
						var idCveUpdateMaturity2 = this.getView().byId("sMaturity");
						var idCveUpdateReferences = this.getView().byId("lReferences");
						var idCveUpdateURLNew = this.getView().byId("tfUrl");
						
						//clear previous model
						var data = [];
						var oldModel = oBugAnalysisView.getModel();
						
						
						
						if (oldModel != undefined) {
							oldModel.setData(data, false);
							oldModel.refresh();
						}
						oldModel = treeTablePath.getModel();
						if (oldModel != undefined) {
							oldModel.setData(data, false);
							oldModel.refresh();
						}
						oldModel = treeTableCommit.getModel();
						if (oldModel != undefined) {
							oldModel.setData(data, false);
							oldModel.refresh();
						}
						oldModel = idCveModelDetailsView.getModel();
						if (oldModel != undefined) {
							oldModel.setData(data, false);
							oldModel.refresh();
						}
						//oldModel = idCveUpdateDescription.getModel();
						//if (oldModel != undefined) {
						//	oldModel.setData(data, false);
						//	oldModel.refresh();
						//}
						//oldModel = idCveUpdateDescriptionAlt.getModel();
						//if (oldModel != undefined) {
						//	oldModel.setData(data, false);
						//	oldModel.refresh();
						//}											
						
						// bug Analysis Tab
						if (!model.Config.isMock) {

							sUrl = model.Config.getBugDetailsAsUrl(bugId);
							var bug;
							oBugAnalysisView.setBusy(true);
							var affectedVersions = [];
							//console.log(sUrl);
							model.Config.loadData(oBugAnalysisModel, sUrl,'GET');
							oBugAnalysisModel.attachRequestCompleted(function() {
								
								var avUrl = model.Config.getBugAllAffVersion(bugId);
								model.Config.loadData(oBugAVModel, avUrl,'GET');
								oBugAVModel.attachRequestCompleted(function() {
									bug = oBugAnalysisModel.getObject("/");
									
									//second tab
									treeTablePath.setBusy(true);
									treeTableCommit.setBusy(true);
									
									// sort per path
									var oRootDataPath = model.TreeUtils.parseConstructChangesPath(bug['constructChanges']);
									var oRootPathModel = new sap.ui.model.json.JSONModel();
									oRootPathModel.setData(oRootDataPath);
									treeTablePath.setModel(oRootPathModel);
									treeTablePath.setBusy(false);
									// sort per commit
									var oRootDataCommit = model.TreeUtils.parseConstructChangesCommit(bug['constructChanges']);
									var oRootCommitModel = new sap.ui.model.json.JSONModel();
									oRootCommitModel.setData(oRootDataCommit);
									treeTableCommit.setModel(oRootCommitModel);
									treeTableCommit.setBusy(false);
	
									
									var newlibraries = [];
									//newModelData.bugId = bugId;
									affectedVersions = oBugAVModel.getObject("/");
									
									var knownLibId = [];
									var knownSha1 = [];
									
									for ( var x in Object.keys(affectedVersions)) {
										var mavenID = model.Config.getMavenId(affectedVersions[x]);
										var key = undefined;
										if(affectedVersions[x].libraryId!=null || affectedVersions[x].lib.libraryId!= null)
											key = mavenID.group + ":"+ mavenID.artifact + ":"+ mavenID.version;
										if((key!=undefined && knownLibId.indexOf(key)==-1) && knownSha1.indexOf(mavenID.sha1)==-1){
											var newlibrary = {};
											
											newlibrary.sha1 = mavenID.sha1;
											if (newlibrary.sha1 === "") {
												newlibrary.sha1 = model.Utils.addSHA1ToLib(mavenID,affectedVersions);
											}
											newlibrary.group = mavenID.group;
											newlibrary.artifact = mavenID.artifact;
											newlibrary.version = mavenID.version;
											
											newlibraries.push(newlibrary);
											if(newlibrary.sha1!=undefined)
												knownSha1.push(newlibrary.sha1);
											if(key!=undefined){
												knownLibId.push(key);
											}
										}
										else if(key==undefined && knownSha1.indexOf(mavenID.sha1)==-1){
											var newlibrary = {};
											newlibrary.sha1 = mavenID.sha1;
											newlibraries.push(newlibrary);
											knownSha1.push(mavenID.sha1);
										}
										
										for(var u=0;u<newlibraries.length;u++){
											if((newlibraries[u].group!=null && newlibraries[u].group==mavenID.group && newlibraries[u].artifact==mavenID.artifact && newlibraries[u].version==mavenID.version)
													|| (newlibraries[u].sha1==mavenID.sha1 && mavenID.sha1!=undefined)){
												var lib={};
												lib=newlibraries[u];
												switch (affectedVersions[x].source) {
												case "MANUAL":
													lib.manual = affectedVersions[x].affected;
													break;
												case "CHECK_VERSION":
													lib.checkvers = affectedVersions[x].affected;
													break;
												case "PRE_COMMIT_POM":
													lib.precommitpom = affectedVersions[x].affected;
													break;
												case "AST_EQUALITY":
												case "MINOR_EQUALITY":
												case "MAJOR_EQUALITY":
												case "INTERSECTION":
												case "PROPAGATE_MANUAL":
	                                            case "GREATER_RELEASE":
	                                            	lib.patchevalAff = affectedVersions[x].affected;
	                                            	lib.patcheval = affectedVersions[x].source;
	                                            	lib.firstFixed = affectedVersions[x].firstFixed;
	                                            	lib.lastVulnerable = affectedVersions[x].lastVulnerable;
	                                            	lib.fromIntersection = affectedVersions[x].fromIntersection;
	                                            	lib.toIntersection = affectedVersions[x].toIntersection;
	                                            	lib.sourcesAvailable=affectedVersions[x].sourcesAvailable;
	                                            	lib.overallConfidence=affectedVersions[x].overallConfidence;
	                                            	if(lib.overallConfidence!=null && lib.overallConfidence.length>4)
	                                            		lib.overallConfidence=lib.overallConfidence.substring(0,4);
	                                            	lib.pathConfidence=affectedVersions[x].pathConfidence;
	                                            	if(lib.pathConfidence!=null && lib.pathConfidence.length>4)
	                                            		lib.pathConfidence=lib.pathConfidence.substring(0,4);
													break;
												case "TO_REVIEW":
													lib.sourcesAvailable=affectedVersions[x].sourcesAvailable;
													lib.patcheval = affectedVersions[x].source;
													break;
												}	
												newlibraries.splice(u, 1, lib);
												break;
											}
										}
									}
												
									// add the ones returned by the /bugs/{bugid}/libraries api
									var oAffectedVersionNeverAnalyzedModel = new sap.ui.model.json.JSONModel();
									var aNaUrl = model.Config.getBugDetailsLibrariesAsUrl(bugId);
									model.Config.loadData(oAffectedVersionNeverAnalyzedModel,aNaUrl, 'GET');
									oAffectedVersionNeverAnalyzedModel.attachRequestCompleted(function() {
										var allAffectedNeverA = [];
										allAffectedNeverA = oAffectedVersionNeverAnalyzedModel.getObject('/');
										for ( var na in allAffectedNeverA) {
											var libId= allAffectedNeverA[na].libraryId;
											//nor libid nor sha1 known
											if((libId!=null && knownLibId.indexOf(libId.group + ":"+ libId.artifact + ":"+ libId.version)==-1)&&knownSha1.indexOf(allAffectedNeverA[na].sha1)==-1){
												var newlibrary = {};
												newlibrary.group = allAffectedNeverA[na]['libraryId']['group'];
												newlibrary.artifact = allAffectedNeverA[na]['libraryId']['artifact'];
												newlibrary.version = allAffectedNeverA[na]['libraryId']['version'];
												newlibrary.sha1 = allAffectedNeverA[na]['sha1'];
												
												newlibraries.push(newlibrary);
												knownLibId.push(libId.group + ":"+ libId.artifact + ":"+ libId.version);
										
											}
											//the libId is known but not its sha1 (the other way around can't happen as the libid is already returned by the previous api)
											else if((libId!=null && knownLibId.indexOf(libId.group + ":"+ libId.artifact + ":"+ libId.version)!=-1)&&knownSha1.indexOf(allAffectedNeverA[na].sha1)==-1){
												for(var u=0;u<newlibraries.length;u++){
													if((newlibraries[u].group!=null && newlibraries[u].group==libId.group && newlibraries[u].artifact==libId.artifact && newlibraries[u].version==libId.version)){
														var lib={};
														lib=newlibraries[u];
														lib.sha1=allAffectedNeverA[na].sha1;
														
														newlibraries.splice(u, 1, lib);
														break;
													}
												}
											}
											else if(libId==null && knownSha1.indexOf(allAffectedNeverA[na].sha1)==-1){
												var lib={};
												lib.sha1=allAffectedNeverA[na].sha1;
												newlibraries.push(lib);
											}
										}
										
										// look for the affected artifacts and add them to newlibraries
										// at this point of time all the libraries are in newlibraries, once per gav
										var gas=[];
										for(var el in knownLibId){
											if(gas.indexOf(knownLibId[el].split(":")[0]+"/"+knownLibId[el].split(":")[1])==-1){
												gas.push(knownLibId[el].split(":")[0]+"/"+knownLibId[el].split(":")[1]);
											}
										}
										for(var ga in gas){
											var allversions = new sap.ui.model.json.JSONModel();
											var aNaUrl = model.Config.getAffectedMavenArtifacts(gas[ga] );
											$.ajax({  type: "GET",
											        url: aNaUrl,
											        headers : {'content-type': "application/json",'cache-control': "no-cache" ,'X-Vulas-Version':model.Version.version,'X-Vulas-Component':'bugfrontend'},
											        success: function(response) {
												allversions.setData(response);
												var oAffectedMavenVersions = allversions.getObject("/");
												for ( var gav in oAffectedMavenVersions) {
													var newlibrary = {};
													newlibrary.group = oAffectedMavenVersions[gav].libId.group;
													newlibrary.artifact = oAffectedMavenVersions[gav].libId.artifact;
													newlibrary.version = oAffectedMavenVersions[gav].libId.version;
													var key = newlibrary.group+ ":"+ newlibrary.artifact+ ":"+ newlibrary.version;
													if (knownLibId.indexOf(key) ==-1) { 
														newlibraries.push(newlibrary);
													}
												}
												},
												async : false
											});
	
										}
	
										
										/**/
										// update the table
										var newModelData = {};
										newModelData.affectedVersions = newlibraries;
										//oBugAnalysisView.setModel(oBugAnalysisModel);
										var newModel = new sap.ui.model.json.JSONModel();
										newModel.setData(newModelData);
										var listlength = newModel.getObject("/affectedVersions").length;
										if (listlength == 0) {
											listlength = 1;
										}
										//oBugAnalysisView.setVisibleRowCount(listlength);
										oBugAnalysisView.setModel(newModel);
										oBugAnalysisView.sort(oBugAnalysisView.getColumns()[2]);
										oBugAnalysisView.setBusy(false);


										// CVE info

										var cveDetailModel = new sap.ui.model.json.JSONModel();

										// defining a special one-way binded Model for the top presentation information of the CVE
										// we do not want the data in the top part of the screen to be updated until the "Update Bug!" button has been pressed
										var cveDetailpresentationModel = new sap.ui.model.json.JSONModel();
										cveDetailpresentationModel.setDefaultBindingMode(sap.ui.model.BindingMode.OneWay);

										var refs = [];
										if (bug.reference != []) {
											for (var i = 0; i < bug.reference.length; i++) {
												var ref = {};
												ref.url = bug.reference[i];
												refs.push(ref);
											}
										}
										if (bug.description == null || bug.cvssScore == null) {
											//var cveUrl = "http://cve.circl.lu/api/cve/"+bugId;
											var externalCveModel = new sap.ui.model.json.JSONModel();
											var cveUrl = model.Config.getCvesServiceUrl(bugId);
											model.Config.loadDataSync(externalCveModel, cveUrl, 'GET');
											//cveDetailModel.setProperty("/reference",refs);
										}

										//var
										idCveUpdateURLNew.setValue("");
										cve = {};
										cve.reference = refs;
										if (bug.description != null) {
											cve.summary = bug.description;
										} else {
											cve.summary = externalCveModel.getObject("/summary");
										}
										cve.summaryAlt = bug.descriptionAlt;
										cve.bugIdAlt = bug.bugIdAlt;
										cve.Published = bug.createdAt;
										cve.Modified = bug.modifiedAt;
										if (bug.cvssScore != null) {
											cve.cvssScore = "" + bug.cvssScore;
										} else {
											cve.cvssScore = externalCveModel.getObject("/cvss");
										}
										cve.cvssVector = bug.cvssVector;
										cve.cvssVersion = bug.cvssVersion;
										cve.maturity = bug.maturity;
										cve.origin = bug.origin;
										cveDetailModel.setData(cve);
										cveDetailpresentationModel.setData(cve);


										// assigning the one-way binded model to the information presentation part of the screen (top).
										idCveModelDetailsView.setModel(cveDetailpresentationModel, "cveModel");

										// assigning the dynamic two-way binded model to the update-related input fields.
										idCveModelDetailsUpdateView.setModel(cveDetailModel, "cveUpdateModel");

										// setting all URL items to selected=true
										var items = idCveUpdateReferences.getItems();
										if (items != []) {
											var item;
											for (var i = 0; i < items.length; i++) {
												item = items[i];
												item.setSelected(true);
											}
										}


										var data1 = "{\"ContentMaturityLevel\":  [	{\"Maturity\": \"DRAFT\"},	{\"Maturity\": \"READY\"}  ]}";
										var maturityModel = new sap.ui.model.json.JSONModel();
										maturityModel.setDefaultBindingMode(sap.ui.model.BindingMode.OneWay);
										maturityModel.setJSON(data1);
										//var oJsonFile = new sap.ui.model.json.JSONModel("model/maturity.json");
										//var oldModel = new sap.ui.model.json.JSONModel(jQuery.sap.getModulePath("model", "/maturity.json"));
										idCveUpdateMaturity2.setModel(maturityModel, "maturityModel");
										var tmp = idCveUpdateMaturity.getValue();
										idCveUpdateMaturity2.setSelectedKey(tmp);

										var data2 = "{\"ContentOriginLevel\":[{\"Origin\":\"PUBLIC\"},{\"Origin\":\"MCHLRN\"},{\"Origin\":\"SRCSCN\"}]}";
										var originModel = new sap.ui.model.json.JSONModel();
										originModel.setDefaultBindingMode(sap.ui.model.BindingMode.OneWay);
										originModel.setJSON(data2);
										idCveUpdateOrigin2.setModel(originModel, "originModel");
										tmp = idCveUpdateOrigin.getValue();
										idCveUpdateOrigin2.setSelectedKey(tmp);
	
									});
									});
							});
							

						}

						
						// third tab
						if (!model.Config.isMock) {
							var textFieldDescription = this.getView().byId(
									"tfDescription");
							var textFieldUrl = this.getView().byId("tfUrl");

						}
					},

					onBugListItemTap : function(oEvent) {
						if (oEvent.getParameters().columnIndex == 4) {
							// do nothing, it is the checkbox cell
						} else if (oEvent.getParameters().columnIndex == 9) {
							var lgroup = oEvent.getParameters().rowBindingContext
									.getObject("group");
							var lartifact = oEvent.getParameters().rowBindingContext
									.getObject("artifact");
							var lversion = oEvent.getParameters().rowBindingContext
									.getObject("version");
							var source = oEvent.getParameters().rowBindingContext
									.getObject("source");
							var sha1 = oEvent.getParameters().rowBindingContext
									.getObject("sha1");
							//console.log(sha1);
							if (typeof (sha1) !== 'undefined') {
								this.router.navTo("checkversionDetail", {
									sha1 : sha1,
									bugId : bugId
								});
							} else {
								sap.ui.commons.MessageBox
										.alert("No sha1 defined");
							}
						} else if (oEvent.getParameters().columnIndex == 3) {
							// archive detail
							var sha1 = oEvent.getParameters().rowBindingContext
									.getObject("sha1");
							if (typeof (sha1) !== 'undefined') {
								this.router.navTo("libDetail", {
									sha1 : sha1,
									bugId : bugId
								});
							} else {
								sap.ui.commons.MessageBox
										.alert("No sha1 defined");
							}
						} else if (oEvent.getParameters().columnIndex == 6) {
							var lgroup = oEvent.getParameters().rowBindingContext
									.getObject("group");
							var lartifact = oEvent.getParameters().rowBindingContext
									.getObject("artifact");
							var lversion = oEvent.getParameters().rowBindingContext
									.getObject("version");
							var source = oEvent.getParameters().rowBindingContext
									.getObject("patcheval");
							if (typeof (source) !== 'undefined') {
								this.router.navTo("BugDetailPatchEval", {
									bugId : bugId,
									group : lgroup,
									artifact : lartifact,
									version : lversion,
									source : source
								});
							}
						}
					},
					onUpdateDescriptionButtonPress : function(oEvent) {
						var textFieldAlternativeBugId       = this.getView().byId("tfBugIdAlt");
						var textFieldDescription            = this.getView().byId("tfDescription");
						var textFieldAlternativeDescription = this.getView().byId("tfDescriptionAlt");
						var textFieldUrl                    = this.getView().byId("tfUrl");
						var textFieldCVSSScore              = this.getView().byId("tfCVSSScore");
						var textFieldCVSSVersion            = this.getView().byId("tfCVSSVersion");
						var textFieldCVSSVector             = this.getView().byId("tfCVSSVector");
						var inputOrigin                     = this.getView().byId("sOrigin");
						var inputMaturity                   = this.getView().byId("sMaturity");
						var inputURLs						= this.getView().byId("lReferences");
						
						var newAltBugId    = textFieldAlternativeBugId.getValue();
						var newDesc        = textFieldDescription.getValue();
						var newAltDesc     = textFieldAlternativeDescription.getValue();
						var newMaturity    = inputMaturity.getSelectedItem().getText();
						var newOrigin      = inputOrigin.getSelectedItem().getText();
						//var newUrl         = textFieldUrl.getValue();
						var newCVSSScore   = textFieldCVSSScore.getValue();
						var newCVSSVersion = textFieldCVSSVersion.getValue();
						var newCVSSVector  = textFieldCVSSVector.getValue();
						var newURLs        = inputURLs.getSelectedItems();
						

						var changeListModel = new sap.ui.model.json.JSONModel();
						var url = model.Config.getBugDetailsAsUrl(bugId);
						model.Config.loadData(changeListModel, url, 'GET');
						changeListModel.attachRequestCompleted(function() {
							var elements = changeListModel.getObject('/');
							elements['bugIdAlt']       = newAltBugId;
							elements['description']    = newDesc;
							elements['descriptionAlt'] = newAltDesc;
							elements['origin']         = newOrigin;
							//elements['url']            = newUrl;
							elements['maturity']       = newMaturity;
							elements['cvssScore']      = newCVSSScore;
							elements['cvssVersion']    = newCVSSVersion;
							elements['cvssVector']     = newCVSSVector;
							
							// copying the selected URLs back to the backend. If an item is not selected, it will be deleted
							var item;
							var refs=[];
							if (newURLs != []){
								for(var i=0;i<newURLs.length;i++){
									item = newURLs[i];
									refs.push(item.getTitle());
							}
							}
							elements['reference'] = refs;
							
							var result = model.Config.uploadCVEDescription(elements, url);
						});

					},
					onAddURLButtonPress : function(oEvent) {
						var newURL  = this.getView().byId("tfUrl").getValue();
						var idCveUpdateReferences = this.getView().byId("lReferences")
						var currentURLs	= idCveUpdateReferences.getItems();
						var myModel = new sap.ui.model.json.JSONModel();
						
						var newURLs=[];
						var item;
						for(var i=0;i<currentURLs.length;i++){
							var urlText = {};
							item = currentURLs[i];
							urlText.url = item.getTitle();
							newURLs.push(urlText);
						}
						var urlText = {};
						urlText.url = newURL;
						newURLs.push(urlText); 
						cve.reference = newURLs;
						var cveModel = new sap.ui.model.json.JSONModel();
						//cveModel.setDefaultBindingMode(sap.ui.model.BindingMode.TwoWay);
						cveModel.setData(cve);
						
						var idCveModelDetailsUpdateView = this.getView().byId("idCveModelDetailsUpdate");
						idCveModelDetailsUpdateView.setModel(cveModel,"cveUpdateModel");
						
						// setting all URL items to selected=true
						;
						var items = idCveUpdateReferences.getItems();
						if(items!=[]){
							var item;
							for(var i=0;i<items.length;i++){
								item = items[i];
								item.setSelected (true);
							}
						}
						this.getView().byId("tfUrl").setValue("");
					},

					onCheckBoxStateChanged : function(oControlEvent) {
						// not used for now
					},

					onRadioButtonClick : function(e) {
						//console.log(e.getParameter('selectedIndex')); // access to the index of the selected button
						// no direct access to rowindex, need to parse the path and get the row index
						//console.log(e.getParameters()['key']);
						var sPath = e.getSource().getBindingContext().getPath();
						var idx = sPath.split("/");
						var oBugAnalysisView = this.getView().byId(
								"idBugAnalysisList");
						var previousData = oBugAnalysisView.getModel()
								.getData();
						//model.Config.uploadManualAssessment(previousData, bugId);
						data = previousData['affectedVersions'][idx[2]];
						switch (e.getParameters()['key']) {
						case "mkt":
							data.manual = true;
							break;
						case "mkf":
							data.manual = false;
							break;
						case "mkn":
							data.manual = null;
							break;
						}
					},
					onModConstructClick : function(oEvent) {
						var qname = oEvent.getParameters().rowBindingContext
								.getObject("name");
						var mtype = oEvent.getParameters().rowBindingContext
								.getObject("overallChg");
						var repo = oEvent.getParameters().rowBindingContext
								.getObject("repo");
						if (mtype == "MOD") {
							if (oEvent.getParameters().columnIndex == 0) {
								this.router.navTo("ASTViewerNew", {
									bugId : bugId,
									repo : repo.replace(/\//g, "+"), // temporary        
									qname : qname
								});
							}
						} else {
							sap.ui.commons.MessageBox.alert(
									"Only avaylable for MOD constructs", {
										title : "Error"
									});
						}
					},

					onSaveButtonPress : function(oPressEvent) {
						var oBugAnalysisView = this.getView().byId(
								"idBugAnalysisList");
						var previousData = oBugAnalysisView.getModel()
								.getData();
						model.Config
								.uploadManualAssessment(previousData, bugId);

						oBugAnalysisView.getModel().refresh();
					},

					onCollapseAllConstructsP : function(oEvent) {
						var ChangeConstructListTree = this.getView().byId(
								'idChangeListPath');
						ChangeConstructListTree.collapseAll();
					},

					onExpandAllConstructsP : function(oEvent) {
						var ChangeConstructListTree = this.getView().byId(
								'idChangeListPath');
						ChangeConstructListTree.expandToLevel(2);
					},
					onCollapseAllConstructsC : function(oEvent) {
						var ChangeConstructListTree = this.getView().byId(
								'idChangeListCommit');
						ChangeConstructListTree.collapseAll();
					},

					onExpandAllConstructsC : function(oEvent) {
						var ChangeConstructListTree = this.getView().byId(
								'idChangeListCommit');
						ChangeConstructListTree.expandToLevel(2);
					},
                   openNVD : function(oEvent) {
                		    var bugDetailPage = this.getView().byId('idCveModelDetails');
                		    var data = bugDetailPage.getModel("cveModel").getData();
                		    //var url = data.cve.link;
//                		    var url =null ;
//                		    if(data.reference!=null)
//                		    	url = data.url;
//                		    else
//                		    	url = "https://web.nvd.nist.gov/view/vuln/detail?vulnId="+bugId;
                		    var url = "https://web.nvd.nist.gov/view/vuln/detail?vulnId="+bugId;
                		    this.openLink(url, 'nvd');
                		},
                                                
                    openLink : function(_url, _window) {
                        window.open(_url, _window).focus();
                    },
					onExit : function() {
					},

					handleNavBack : function() {
						this.router.myNavBack("master", {});
					}
				});
