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
var sha1;
var bugId;
var qname;

sap.ui.controller("view.ASTViewerNew", {

	onInit : function() {
		this.router = sap.ui.core.UIComponent.getRouterFor(this);
		this.router.attachRoutePatternMatched(this._handleRouteMatched, this);
                jQuery.sap.includeStyleSheet("css/style.css");
	},

	_handleRouteMatched : function(evt) {   
		if (evt.getParameter("name") !== "ASTViewerNew") {
			return;
		}
                bugId = evt.getParameter("arguments").bugId;   
                qname = evt.getParameter("arguments").qname;
                repo =  evt.getParameter("arguments").repo.replace(/\+/g, "/");
                console.log(repo);
                this.loadDataIntoView();
	},

	loadDataIntoView : function() {
	    if(bugId && qname && repo) {
                // Get page and set title
                var ASTViewerPage = this.getView().byId('idASTViewerNewPage');
                ASTViewerPage.setTitle(qname);
                
                var oBuggyTree = this.getView().byId('idBuggyTree');
                var oFixedTree = this.getView().byId('idFixedTree');
                var oChangesTable = this.getView().byId('idChangesTable');
                var oASTViewerModel = new sap.ui.model.json.JSONModel();
                
                sap.ui.core.BusyIndicator.show();
                if (!model.Config.isMock) {
                    //var url = model.Config.getAffectedConstructsSha1Bugid(sha1, bugId);
                    var url = model.Config.getBugDetailsAsUrl(bugId);
                    model.Config.loadData(oASTViewerModel, url, 'GET');
                    oASTViewerModel.attachRequestCompleted(function(){
                        var elements = oASTViewerModel.getObject('/constructChanges');
                        /*for (var y in elements){
                            console.log(elements[y]);
                        }*/
                        var x = model.TreeUtils.getOverallChangeModificationsAffectedCC(elements,qname,repo);
                        
                        //
                        console.log("overall change:");
                        console.log(x);
                        // old
                        //var x = model.TreeUtils.getModConstructPerCommit(elements, qname, commitid);
                                                    
                        // vulnerable
                        var bb = x['bb'];
                        var oNodeBuggyFather = {};
                        if ( typeof(bb) !== 'undefined'){
                            oNodeBuggyFather = model.TreeUtils.parseConstructJson(bb);
                        }
                        var oBuggyModel = new sap.ui.model.json.JSONModel();
                        
                        // fixed
                        var fb = x['fb'];
                        var oNodeFixedFather = {};
                        if ( typeof(fb) !== 'undefined'){
                            oNodeFixedFather = model.TreeUtils.parseConstructJson(fb);
                        }
                        var oFixedModel = new sap.ui.model.json.JSONModel();
                        
                        
                        // bodychanges
                        var bc = x['bc'];
                        var allChangesData = JSON.parse(bc)['StructureEntity']['changes'];
                        var newChangeData = model.TreeUtils.processBodyChange(bc);
                        
                        // filter MOV-UPD happening on same line
                        var filteredChangeData = model.TreeUtils.filterMovUpd(newChangeData);
                        //console.log(filteredChangeData);
                        
                        var oChangesModel = new sap.ui.model.json.JSONModel();
                        oChangesModel.setData(filteredChangeData);
                        oChangesTable.setModel(oChangesModel);   
                        // default sort by source code range start
                        oChangesTable.sort(oChangesTable.getColumns()[4]);
                        
                        // add change type for every LOC in both vulnerable and fixed
                        for (var i in filteredChangeData ){
                            var entityType = filteredChangeData[i]['EntityType'];
                            var operationType = filteredChangeData[i]['OperationType'];
                            if ( operationType === 'Insert' ){
                                var entityName = filteredChangeData[i]['NewEntity'];
                            } else {
                                var entityName = filteredChangeData[i]['EntityName'];
                            }
                            var strSearch = entityType + " " + entityName;
                            var idx;
                            
                            switch (operationType){
                                case 'Insert':
                                    model.TreeUtils.recursiveSearchAddMT(oNodeFixedFather.root, strSearch, operationType );
                                    break;
                                case 'Delete':
                                    model.TreeUtils.recursiveSearchAddMT(oNodeBuggyFather.root, strSearch, operationType );
                                    break;
                                case 'Update':
                                case 'Move':
                                case 'Mov-Upd':
                                    model.TreeUtils.recursiveSearchAddMT(oNodeBuggyFather.root, strSearch, operationType );
                                    var strSearchUpdF = entityType + " " + filteredChangeData[i]['NewEntity'];
                                    model.TreeUtils.recursiveSearchAddMT(oNodeFixedFather.root, strSearchUpdF, operationType );
                                    break; 
                            }
                            //model.TreeUtils.recursiveSearchAddMT(oNodeBuggyFather.root, strSearch, operationType );
                        }
                                                
                        
                        oBuggyModel.setData(oNodeBuggyFather);
                        //oBuggyModel.attachRequestCompleted(function(){
                        oBuggyTree.setModel(oBuggyModel);
                        oBuggyTree.expandToLevel(100);
                        oBuggyTree.setVisibleRowCount(model.TreeUtils.getVisibleRowCountTable(oBuggyTree));
                        //});
                        
                        oFixedModel.setData(oNodeFixedFather);
                        //oFixedModel.attachRequestCompleted(function(){
                        oFixedTree.setModel(oFixedModel);
                        oFixedTree.expandToLevel(100);
                        oFixedTree.setVisibleRowCount(model.TreeUtils.getVisibleRowCountTable(oFixedTree));
                        //});            
                                               
                        ASTViewerPage.setModel(oASTViewerModel);
                        
                        sap.ui.core.BusyIndicator.hide();
                    });
                    
                }
	    }
	},
        
        onChangedEntityClick : function(oEvent){
            var entityName = oEvent.getParameters().rowBindingContext.getObject("EntityName");
            var entityType = oEvent.getParameters().rowBindingContext.getObject("EntityType");
            var changeType = oEvent.getParameters().rowBindingContext.getObject("OperationType");
            if ( typeof(entityName)==='undefined' ){
                entityName = oEvent.getParameters().rowBindingContext.getObject("NewEntity");
            }
            var strSearch = entityType + " " + entityName;
            var oBuggyTree = this.getView().byId('idBuggyTree');
            var oFixedTree = this.getView().byId('idFixedTree');
            // remove focus if something had already been selected and expand all in order to make the search work
            oBuggyTree.expandToLevel(100);
            oFixedTree.expandToLevel(100);
            
            oBuggyTree.setSelectedIndex(-1);
            oFixedTree.setSelectedIndex(-1);
            if ( changeType=="Delete") {
                var bindedData = oBuggyTree.getModel().getData();
                var idx = model.TreeUtils.recursiveSearch(bindedData.root, strSearch);
                oBuggyTree.setFirstVisibleRow(idx);
                oBuggyTree.setSelectedIndex(idx);
            } else if ( changeType=="Update" || changeType=="Move" || changeType=="Mov-Upd" ){
                var bindedData = oBuggyTree.getModel().getData();
                var brS = oEvent.getParameters().rowBindingContext.getObject("rangeStart");
                var brE = oEvent.getParameters().rowBindingContext.getObject("rangeEnd");
                var idx = model.TreeUtils.recursiveSearchUpdate(bindedData.root, strSearch, brS, brE);
                oBuggyTree.setFirstVisibleRow(idx);
                oBuggyTree.setSelectedIndex(idx);
                // look for the new value in the fixed tree
                var newValue = oEvent.getParameters().rowBindingContext.getObject("NewEntity");
                var strSearchUpdate = entityType + " " + newValue;
                var fbindedData = oFixedTree.getModel().getData();
                var frS = oEvent.getParameters().rowBindingContext.getObject("newRangeStart");
                var frE = oEvent.getParameters().rowBindingContext.getObject("newRangeEnd");
                var fidx = model.TreeUtils.recursiveSearchUpdate(fbindedData.root, strSearchUpdate, frS, frE);
                console.log(fidx);
                oFixedTree.setFirstVisibleRow(fidx);
                oFixedTree.setSelectedIndex(fidx);
            } else if ( changeType=="Insert" ){
                var bindedData = oFixedTree.getModel().getData();
                var idx = model.TreeUtils.recursiveSearch(bindedData.root, strSearch);
                //var idx = model.TreeUtils.recursiveSearchAddMT(bindedData.root, strSearch, 'Insert');
                oFixedTree.setFirstVisibleRow(idx);
                oFixedTree.setSelectedIndex(idx);
            }
        },
        
        onVulnerableEntityClick : function(oEvent){
            var oBuggyTree = this.getView().byId('idBuggyTree');
            var oFixedTree = this.getView().byId('idFixedTree');
            var oChangesTable = this.getView().byId('idChangesTable');
            
            var bindedData = oBuggyTree.getModel().getData();
            var fbindedData = oFixedTree.getModel().getData();
            var changesData = oChangesTable.getModel().getData();
            
            var entityName = oEvent.getParameters().rowBindingContext.getObject("name");
            var modificationType = oEvent.getParameters().rowBindingContext.getObject("modificationType");
            var rStart = oEvent.getParameters().rowBindingContext.getObject("rStart");
            var rEnd = oEvent.getParameters().rowBindingContext.getObject("rEnd");
            
            oBuggyTree.setSelectedIndex(oEvent.getParameter("rowIndex"));
            // remove focus if something had already been selected and expand all in order to make the search work
            oFixedTree.expandToLevel(100);
            oFixedTree.setSelectedIndex(-1);
            switch(modificationType){
                case '':
                case 'Delete':
                    // look for the same name
                    var strSearch = entityName;
                    var idx = model.TreeUtils.recursiveSearch(fbindedData.root, strSearch);
                    if ( idx === -1 ) {
                        // just select the same row #
                        idx = oEvent.getParameter("rowIndex");
                    }
                    oFixedTree.setSelectedIndex(model.TreeUtils.normalizeIndexRange(oFixedTree, idx));
                    oFixedTree.setFirstVisibleRow(model.TreeUtils.normalizeIndexRange(oFixedTree, idx));
                    console.log(idx);
                    break;
                case 'Update':
                case 'Move':
                case 'Mov-Upd':
                    // need to look into changes
                    // need to retrieve the new range start and end from changesData
                    var newRange = model.TreeUtils.getNewRange(changesData, entityName, rStart, rEnd);
                    console.log(newRange);
                    if ( newRange === -1 ) {
                        // just select the same row #
                        idx = oEvent.getParameter("rowIndex");
                    } else {
                        idx = model.TreeUtils.recursiveSearchUpdate(fbindedData.root, newRange['newEntity'], newRange['newStart'], newRange['newEnd']);
                    }
                    oFixedTree.setFirstVisibleRow(model.TreeUtils.normalizeIndexRange(oFixedTree, idx));
                    oFixedTree.setSelectedIndex(model.TreeUtils.normalizeIndexRange(oFixedTree, idx));
                    break;
            }
            
        },
        
        onFixedEntityClick : function(oEvent){
            var oBuggyTree = this.getView().byId('idBuggyTree');
            var oFixedTree = this.getView().byId('idFixedTree');
            var oChangesTable = this.getView().byId('idChangesTable');
            
            var bindedData = oBuggyTree.getModel().getData();
            var fbindedData = oFixedTree.getModel().getData();
            var changesData = oChangesTable.getModel().getData();
            
            var entityName = oEvent.getParameters().rowBindingContext.getObject("name");
            var modificationType = oEvent.getParameters().rowBindingContext.getObject("modificationType");
            var rStart = oEvent.getParameters().rowBindingContext.getObject("rStart");
            var rEnd = oEvent.getParameters().rowBindingContext.getObject("rEnd");
            
            oFixedTree.setSelectedIndex(oEvent.getParameter("rowIndex"));
            // remove focus if something had already been selected and expand all in order to make the search work
            oBuggyTree.expandToLevel(100);
            oBuggyTree.setSelectedIndex(-1);
            //console.log(oEvent.getParameter("rowIndex"));
            switch(modificationType){
                case '':
                case 'Insert':
                    // look for the same name
                    var strSearch = entityName;
                    var idx = model.TreeUtils.recursiveSearch(bindedData.root, strSearch);
                    if ( idx === -1 ) {
                        // just select the same row #
                        idx = oEvent.getParameter("rowIndex");
                    }
                    oBuggyTree.setSelectedIndex(model.TreeUtils.normalizeIndexRange(oBuggyTree, idx));
                    oBuggyTree.setFirstVisibleRow(model.TreeUtils.normalizeIndexRange(oBuggyTree, idx));
                    break;
                case 'Update':
                case 'Move':
                case 'Mov-Upd':
                    // need to look into changes
                    // need to retrieve the old range start and end from changesData
                    var oldRange = model.TreeUtils.getOldRange(changesData, entityName, rStart, rEnd);
                    console.log(oldRange);
                    if ( oldRange === -1 ) {
                        // just select the same row #
                        idx = oEvent.getParameter("rowIndex");
                    } else {
                        idx = model.TreeUtils.recursiveSearchUpdate(bindedData.root, oldRange['newEntity'], oldRange['oldStart'], oldRange['oldEnd']);
                    }
                    oBuggyTree.setSelectedIndex(model.TreeUtils.normalizeIndexRange(oBuggyTree, idx));
                    oBuggyTree.setFirstVisibleRow(model.TreeUtils.normalizeIndexRange(oBuggyTree, idx));
                    break;
            }
        },
        
        onCollapseAllVulnerable : function(oEvent){
            var oBuggyTree = this.getView().byId('idBuggyTree');
            oBuggyTree.collapseAll();
        },
        onExpandAllVulnerable : function(oEvent){
            var oBuggyTree = this.getView().byId('idBuggyTree');
            oBuggyTree.expandToLevel(100);
        },
        onCollapseAllFixed : function(oEvent){
            var oFixedTree = this.getView().byId('idFixedTree');
            oFixedTree.collapseAll();
        },
        onExpandAllFixed : function(oEvent){
            var oFixedTree = this.getView().byId('idFixedTree');
            oFixedTree.expandToLevel(100);
        },
        onCollapseAllTestedF : function(oEvent){
            var oTestedTreeF = this.getView().byId('idTestedTreeF');
            oTestedTreeF.collapseAll();
        },
	onExpandAllTestedF: function(oEvent){
            var oTestedTreeF = this.getView().byId('idTestedTreeF');
            oTestedTreeF.expandToLevel(100);
        },
        onCollapseAllTestedD : function(oEvent){
            var oTestedTreeD = this.getView().byId('idTestedTreeD');
            oTestedTreeD.collapseAll();
        },
	onExpandAllTestedD: function(oEvent){
            var oTestedTreeD = this.getView().byId('idTestedTreeD');
            oTestedTreeD.expandToLevel(100);
        },
	writeEmail : function(oEvent) {
	    var bugDetailPage = this.getView().byId('idBugDetailPage');
	    var data = bugDetailPage.getModel().getData();
	    
	    var emailText  = "Dear team,\n\nPlease note that the archive '" + data.archiveDetails.filename + "' (" + data.archiveid + ") is not affected by vulnerability " + data.cve.cve_id + " [1].\n\n";
            var emailText2 = "Please mark this release as non-vulnerable so that Steady does not raise an alert when discovering it.\n\n";
            var emailText3 = "[EXPLAIN HERE WHY THIS RELEASE IS NOT VULNERABLE, E.G., A LINK TO VENDOR INFORMATION ON AFFECTED RELEASES]\n\n";
            var emailText4 = "Kind regards, [XXX]\n\n";
            var emailUrl   = "[1] " + window.location.href + "\n";
            var emailSubject = "[Steady] Archive '" + data.archiveDetails.filename + "' not affected by vulnerability " + data.cve.cve_id;
            sap.m.URLHelper.triggerEmail("DL STEADY", emailSubject, emailText + emailText2 + emailText3 + emailText4 + emailUrl);
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

