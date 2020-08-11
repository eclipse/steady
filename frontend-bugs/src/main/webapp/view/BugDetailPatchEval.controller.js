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
var group;
var artifact;
var version;
var source;
var bAllShown = false;

sap.ui.controller("view.BugDetailPatchEval", {

	onInit : function() {
		this.router = sap.ui.core.UIComponent.getRouterFor(this);
		this.router.attachRoutePatternMatched(this._handleRouteMatched, this);
	},

	_handleRouteMatched : function(evt) {
            bugId = evt.getParameter("arguments").bugId;
            group = evt.getParameter("arguments").group;
            artifact = evt.getParameter("arguments").artifact;
            version = evt.getParameter("arguments").version;
            source = evt.getParameter("arguments").source;
            //console.log(bugId+group+artifact+version+source);
            this.loadDataIntoView();
	},

	loadDataIntoView : function() {
	    if(bugId&&group&&artifact&&version&&source) {
    	    // Get page and set title
            var bugDetailPatchEvalPage = this.getView().byId('idBugDetailPatchEvalPage');
    		bugDetailPatchEvalPage.setTitle(bugId); 
                var oBugAnalysisModel = new sap.ui.model.json.JSONModel();
                var oAffectedCCsModel = new sap.ui.model.json.JSONModel();
    		//var bugDetailModel = new sap.ui.model.json.JSONModel();
    		
                
    		//clear previous model
			var data = [];			
			var oldModel = bugDetailPatchEvalPage.getModel();
			if (oldModel != undefined) {
				oldModel.setData(data, false);
				oldModel.refresh();
			}
			oldModel = this.getView().byId('idVuln').getModel();
			if (oldModel != undefined) {
				oldModel.setData(data, false);
				oldModel.refresh();
			}
			oldModel = this.getView().byId('idFixed').getModel();
			if (oldModel != undefined) {
				oldModel.setData(data, false);
				oldModel.refresh();
			}
			oldModel = this.getView().byId('idTested').getModel();
			if (oldModel != undefined) {
				oldModel.setData(data, false);
				oldModel.refresh();
			}
			oldModel =  this.getView().byId('idSameBytecode').getModel();
			if (oldModel != undefined) {
				oldModel.setData(data, false);
				oldModel.refresh();
			}
			
			
    		if (!model.Config.isMock) {
    				
                    sUrl = model.Config.getBugDetailsAffVersion(bugId, source,group,artifact,version);
                    model.Config.loadData (oBugAnalysisModel, sUrl, 'GET');
                    var affectedVersions = [];
                    var affectedCCs = [];
                    oBugAnalysisModel.attachRequestCompleted(function(){
                        affectedVersions = oBugAnalysisModel.getObject("/");
                        //affectedVersions = oBugAnalysisModel.getObject("/affectedVersions");
                        for ( var x in affectedVersions ){
                            if ( affectedVersions[x].libraryId.group == group &&
                                    affectedVersions[x].libraryId.version == version &&
                                    affectedVersions[x].libraryId.artifact == artifact && 
                                    affectedVersions[x].source == source ){
                                //console.log(affectedVersions[x]);
                                affectedCCs = affectedVersions[x].affectedcc;
                                
                            }
                        }
                        oAffectedCCsModel.setData(affectedCCs);
                        bugDetailPatchEvalPage.setModel(oAffectedCCsModel);
                        // cve
                        
                        var cveDetailModel = new sap.ui.model.json.JSONModel();
                		
                	//	to get existing descriptions we would need to GET the bug again, is it worth?
                	/*	if(bug.description!=null ){
                			var cve={};
                			cve.summary= bug.description;
                			cve.Pusblished = bug.createdAt;
                			cve.Modified = bug.modifiedAt;
                			//cve.cvss 
                			cveDetailModel.setData(cve);
                		}
                		else{*/
                			//var cveUrl = "http://cve.circl.lu/api/cve/"+bugId;
            	    		var cveUrl = model.Config.getCvesServiceUrl(bugId);	    		
            	    		model.Config.loadData (cveDetailModel,cveUrl, 'GET');		
                		//}

                		bugDetailPatchEvalPage.setModel(cveDetailModel,"cveModel");

                        
                       
                        // archive
                        var archiveModel = new sap.ui.model.json.JSONModel();
                        var archiveData = {};
                        archiveData['group']=group;
                        archiveData['artifact']=artifact;
                        archiveData['version']=version;
                        archiveModel.setData(archiveData);
                        bugDetailPatchEvalPage.setModel(archiveModel,"archiveModel");
                        //console.log(archiveData);
                    });
                    
    		}    		
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
		var qname = oEvent.getParameters().rowBindingContext.getObject("cc/constructId/qname");
		var path = oEvent.getParameters().rowBindingContext.getObject("cc/repoPath");
		var mtype = oEvent.getParameters().rowBindingContext.getObject("cc/constructChangeType");
//		var repo = oEvent.getParameters().rowBindingContext.getObject("cc/repo");
//		if (mtype == "MOD") {
//	//		if (oEvent.getParameters().columnIndex == 0) {
//				this.router.navTo("ASTPatchEval", {
//					bugId : bugId,
//					repo : repo.replace(/\//g, "+"), // temporary        
//					qname : qname
//				});
//	//		}
//		} else {
//			sap.ui.commons.MessageBox.alert(
//					"Only avaylable for MOD constructs", {
//						title : "Error"
//					});
//		}
		if (mtype != "MOD") {
			sap.ui.commons.MessageBox.alert(
					"Only avaylable for MOD constructs", {
						title : "Error"
					});
		}else{
		
			var affCC = this.getView().byId('idBugDetailPatchEvalPage').getModel().getData();
		
			for(var el=0;el<affCC.length;el++){
				if(affCC[el].cc.repoPath==path && affCC[el].cc.constructId.qname==qname){
					
					if(affCC[el].testedBody!=null){
						
						var bb = affCC[el].vulnBody;
						
				        var oNodeBuggyFather = {};
				        if ( typeof(bb) !== 'undefined'){
				            oNodeBuggyFather = model.TreeUtils.parseConstructJson(bb);
				        }
				        var oBuggyModel = new sap.ui.model.json.JSONModel();
				        
				        // fixed
				        var fb = affCC[el].fixedBody;
				        var oNodeFixedFather = {};
				        if ( typeof(fb) !== 'undefined'){
				            oNodeFixedFather = model.TreeUtils.parseConstructJson(fb);
				        }
				        var oFixedModel = new sap.ui.model.json.JSONModel();
				        
				        // tested
				        var tb = affCC[el].testedBody;
				        var oNodeTestedFather = {};
				        if ( typeof(fb) !== 'undefined'){
				        	oNodeTestedFather = model.TreeUtils.parseConstructJson(tb);
				        }
				        var oTestedModel = new sap.ui.model.json.JSONModel();
				    
				        
		                var oBuggyTree = this.getView().byId('idVuln');
		                var oFixedTree = this.getView().byId('idFixed');
		                var oTestedTree = this.getView().byId('idTested');
		                
				        oBuggyModel.setData(oNodeBuggyFather);
				        oBuggyTree.setModel(oBuggyModel);
				        oBuggyTree.expandToLevel(100);
				        oBuggyTree.setVisibleRowCount(model.TreeUtils.getVisibleRowCountTable(oBuggyTree));
	                    
	                    oFixedModel.setData(oNodeFixedFather);
	                    oFixedTree.setModel(oFixedModel);
	                    oFixedTree.expandToLevel(100);
	                    oFixedTree.setVisibleRowCount(model.TreeUtils.getVisibleRowCountTable(oFixedTree));
	
	                    oTestedModel.setData(oNodeTestedFather);
	                    oTestedTree.setModel(oTestedModel);
	                    oTestedTree.expandToLevel(100);
	                    oTestedTree.setVisibleRowCount(model.TreeUtils.getVisibleRowCountTable(oTestedTree));
                              
					}
					else if(affCC[el].sameBytecodeLids!=null){
						var sameBytecodeModel = new sap.ui.model.json.JSONModel();
						sameBytecodeModel.setData(affCC[el]);
						var sameBytecodeTable = this.getView().byId('idSameBytecode');
						sameBytecodeTable.setModel(sameBytecodeModel);
					}
				}
			}		
		}  
    },
	
	openExploitDb : function(oEvent) {
	    var bugDetailPatchEvalPage = this.getView().byId('idBugDetailPage');
	    var data = bugDetailPatchEvalPage.getModel().getData();
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

