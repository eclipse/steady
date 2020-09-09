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
jQuery.sap.declare("model.Utils");

model.Utils = {};

model.Utils.counter = 0;


model.Utils.addSHA1ToLib = function (lookupMavenId,affectedVersions){
    for ( var k in Object.keys(affectedVersions)){
    	if(affectedVersions[k].lib!=null && affectedVersions[k].lib.libraryId!=null){
	        var mavenID = model.Config.getMavenId(affectedVersions[k].lib.libraryId);
	        var key = mavenID.group+":"+mavenID.artifact+":"+mavenID.version;
	        if ( this.compareMavenIds(lookupMavenId, mavenID) && mavenID.sha1 !== ""  ) {
	            return mavenID.sha1;
	        }
    	}
    }
};


model.Utils.compareMavenIds = function ( mid1,mid2 ){
    if ( mid1.group==mid2.group && mid1.artifact==mid2.artifact && mid1.version==mid2.version ){
        //console.log(mid1.group,mid2.group,mid1.artifact,mid2.artifact,mid1.version,mid2.version);
        return true;
    }
    return false;
};

model.Utils.parseConstructJson = function(construct){
    var jsonBB = JSON.parse(construct);
    //console.log(jsonBB['ast'][0]);
    var methodName = jsonBB['ast'][0]['Value'];
    var entityType = jsonBB['ast'][0]['EntityType'];
    console.log(methodName + "," + entityType);
    var oNodeFather = new sap.ui.commons.TreeNode("father"+this.counter.toString(), {text:methodName, expanded: true});
    //
    var locs = jsonBB['ast'][0]['C'];
    for ( var x in locs ) {
        //this.recursiveVisit(locs[x]);
        oNodeFather.addNode(this.recursiveVisitBuildTree(locs[x]));
    }
    console.log(oNodeFather);
    return oNodeFather;
};


model.Utils.recursiveVisitBuildTree = function(entity){
    //var tLevel = index['level'];
    //var tRange = index['range'];
    //console.log("level :" + tLevel + ", index : " + tRange);
    this.counter++;
    console.log(this.counter);
    console.log(entity);
    var entityType = this.statementNameNormalizer(entity['EntityType']);
    var oNode = new sap.ui.commons.TreeNode("node"+this.counter.toString(), {text:entityType+" "+entity['Value']});
    var locs = entity['C'];
    if( typeof(locs)==='undefined' || locs.length == 0 ) {
        //console.log(entity['Value']);
        //console.log(oNode);
        return oNode;
    } 
    else if ( locs.length != 0 ) {
        //console.log(entity);
        for ( var y in locs ) {
            //var tIndex = {"level":tLevel+1, "range":y};
            //oNode.addNode(this.recursiveVisitBuildTree(locs[y]), tIndex);
            //var child = this.recursiveVisitBuildTree(locs[y], tIndex);
            var child = this.recursiveVisitBuildTree(locs[y]);
            oNode.addNode(child);
        }
        return oNode;
    }
};

model.Utils.statementNameNormalizer = function(string){
  return string.replace("_STATEMENT", "");
};

model.Utils.recursiveVisit = function(entity){
    //console.log(entity);
    var locs = entity['C'];
    if( typeof(locs)==='undefined' || locs.length == 0 ) {
        console.log(entity);
        return;
    } 
    else if ( locs.length != 0 ) {
        console.log(entity);
        for ( var x in locs ) {
            this.recursiveVisit(locs[x]);
        }
    }
        
};

model.Utils.parseAffectedCCs = function(affectedCCs){
    var treeRoot = {
                    root:{
                            name: "root",
                            children : []
                    }};
    var repoPaths = this.findAllRepoPaths(affectedCCs);
    for ( var x in repoPaths ) {
        console.log(repoPaths[x]);
        var oNode = {name:repoPaths[x], children:[]};
        this.findAllQnamesPerRepoPath(affectedCCs, oNode);
        treeRoot["root"]['children'].push(oNode);
    }
    return treeRoot;
};

model.Utils.findAllRepoPaths = function(affectedCCs) {
    var repoPaths = [];
    for  ( var x in affectedCCs ) {
        if ( typeof(repoPaths[affectedCCs[x]['cc']['repoPath']]) === 'undefined' ){
            repoPaths[affectedCCs[x]['cc']['repoPath']] = affectedCCs[x]['cc']['repoPath'];
        }
    }
    return repoPaths;
};

model.Utils.findAllQnamesPerRepoPath = function(affectedCCs, repoPath) {
    for ( var x in affectedCCs ) {
        if (affectedCCs[x]['cc']['repoPath'] === repoPath['name']){
            var alreadyThere = false;
            for ( var y in repoPath['children']){
                alreadyThere = affectedCCs[x]['cc']['constructId']['qname'] == repoPath['children'][y]['name'];
            }
            if ( !alreadyThere ) {
                var oNode = {name:affectedCCs[x]['cc']['constructId']['qname'], children:[], type:affectedCCs[x]['cc']['constructId']['type'],
                    overallChg:affectedCCs[x]['overall_chg'], constructAffected:affectedCCs[x]['affected'], 
                    inArchive:affectedCCs[x]['inArchive'] , classInArchive:affectedCCs[x]['classInArchive'], 
                    repo:affectedCCs[x]['cc']['repoPath']};
                this.findAllCommitsPerQname(affectedCCs, repoPath['name'], oNode);
                repoPath['children'].push(oNode);
            }
        }
    }
};

model.Utils.findAllCommitsPerQname = function(affectedCCs, repoPath, qname){
    for ( var x in affectedCCs ) {
        if (affectedCCs[x]['cc']['repoPath'] === repoPath && affectedCCs[x]['cc']['constructId']['qname'] === qname['name'] ){
            var oNode = {name:affectedCCs[x]['cc']['commit'], children:[], overallChg:affectedCCs[x]['overall_chg']};
            qname['children'].push(oNode); 
        }
    }
};
