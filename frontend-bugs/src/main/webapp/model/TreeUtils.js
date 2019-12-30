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
jQuery.sap.declare("model.TreeUtils");

model.TreeUtils = {};
model.TreeUtils.counter = -1;
    
model.TreeUtils.parseConstructJson = function(construct){
    var jsonBB = JSON.parse(construct);
    //console.log(jsonBB['ast'][0]);
    var methodName = jsonBB['ast'][0]['Value'];
    var entityType = jsonBB['ast'][0]['EntityType'];
    //console.log(methodName + "," + entityType);
    //var oNodeFather = new sap.ui.commons.TreeNode("father"+this.counter.toString(), {text:methodName, expanded: true});
    //
    this.counter = -1;
    var oData = {
                root:{
                        name: methodName,
                        children : [],
                        rowIdx : this.counter
                }};
    var locs = jsonBB['ast'][0]['C'];
    for ( var x in locs ) {
        //this.recursiveVisit(locs[x]);
        //oNodeFather.addNode(this.recursiveVisitBuildTree(locs[x]));
        //oData["root"]['children'].push(this.recursiveVisitBuildTree(locs[x], {level:1, range:x}));
        oData["root"]['children'].push(this.recursiveVisitBuildTreeWithParent(locs[x], {level:1, range:x}));
    }
    //console.log(oData);
    return oData;
};

model.TreeUtils.recursiveVisitBuildTree = function(entity, index){
    var tLevel = index['level'];
    var tRange = index['range'];
    //console.log("rec visit build tree");
    //onsole.log(entity);
    //console.log("level :" + tLevel + ", index : " + tRange);
    //this.counter++;
    //console.log(this.counter);
    //console.log(entity);
    var entityType = this.statementNameNormalizer(entity['EntityType']);
    this.counter++;
    var oNode = {name:entityType+" "+entity['Value'], children:[], rowIdx:this.counter, modificationType:''};
    var locs = entity['C'];
    if( typeof(locs)==='undefined' || locs.length == 0 ) {
        //console.log(entity['Value']);
        //console.log(oNode);
        return oNode;
    } 
    else if ( locs.length != 0 ) {
        //console.log(entity);
        for ( var y in locs ) {
            var tIndex = {"level":tLevel+1, "range":y};
            //oNode.addNode(this.recursiveVisitBuildTree(locs[y]), tIndex);
            //var child = this.recursiveVisitBuildTree(locs[y], tIndex);
            var child = this.recursiveVisitBuildTree(locs[y], tIndex);
            //oNode.addNode(child);
            oNode['children'].push(child);
        }
        return oNode;
    }
};

model.TreeUtils.recursiveVisitBuildTreeWithParent = function(entity, index){
    var tLevel = index['level'];
    var tRange = index['range'];
    var entityType = this.statementNameNormalizer(entity['EntityType']);
    this.counter++;
    var oNode = {name:entityType+" "+entity['Value'], children:[], rowIdx:this.counter, modificationType:'', 
            rStart: entity['SourceCodeEntity']['SourceRange']['Start'], rEnd:entity['SourceCodeEntity']['SourceRange']['End']};
    var locs = entity['C'];
    if( typeof(locs)==='undefined' || locs.length == 0 ) {
        //console.log(entity['Value']);
        return oNode;
    } 
    else if ( locs.length != 0 ) {
        //console.log(entity);
        for ( var y in locs ) {
            var tIndex = {"level":tLevel+1, "range":y};
            var meParent = entity['Value'];
            //oNode.addNode(this.recursiveVisitBuildTree(locs[y]), tIndex);
            //var child = this.recursiveVisitBuildTree(locs[y], tIndex);
            var child = this.recursiveVisitBuildTreeWithParent(locs[y], tIndex);
            //oNode.addNode(child);
            oNode['children'].push(child);
        }
        return oNode;
    }
};


model.TreeUtils.statementNameNormalizer = function(string){
  var s = string.replace("_STATEMENT", "");
  s = s.replace("VARIABLE_DECLARATION", "");
  s = s.replace("METHOD_INVOCATION", "");
  s = s.replace("ASSIGNMENT", "");
  return s;
};

model.TreeUtils.recursiveVisit = function(entity){
    //console.log(entity);
    var locs = entity['C'];
    if( typeof(locs)==='undefined' || locs.length == 0 ) {
        //console.log(entity);
        return;
    } 
    else if ( locs.length != 0 ) {
        //console.log(entity);
        for ( var x in locs ) {
            this.recursiveVisit(locs[x]);
        }
    }
        
};

model.TreeUtils.getModConstructPerCommit = function(elements, qn, cid){
    for ( var x in elements){
        if ( elements[x]["overall_chg"] == "MOD" && qn==elements[x]['cc']['constructId']['qname']
                && cid == elements[x]['cc']['commit']){
            //console.log(elements[x]);
            return {bb:elements[x]['cc']['buggyBody'], fb:elements[x]['cc']['fixedBody'], bc:elements[x]['cc']['bodyChange']};
        }
    }
};

model.TreeUtils.getOverallChangeModifications = function(elements, qname, repo){
    var count = 0;
    var commitIds = [];
    var gitCommitIds = [];
    var isGit = false;
    for ( var x in elements ){
        if ( isNaN(elements[x]['cc']['commit']) ){
            isGit=true;
        }
        if (elements[x]["overall_chg"] == "MOD" && elements[x]['cc']['constructId']['qname']===qname && 
                elements[x]['cc']['repoPath']==repo){
             count++;
            if ( !isGit ) {
                commitIds.push(parseInt(elements[x]['cc']['commit']));
            } else {/*
                console.log("is git");
                console.log(elements[x]['cc']['committedAt']);
                var date = new Date(elements[x]['cc']['committedAt']);
                console.log(date);*/
                gitCommitIds.push({commit:elements[x]['cc']['commit'], date:elements[x]['cc']['committedAt']});
            }
        }        
    }
    //console.log(commitIds);
    // if only one commit is existing, return the overall change associated to that commit
    if ( count == 1 ){
        for ( var x in elements){
            if ( elements[x]["overall_chg"] == "MOD" && qname==elements[x]['cc']['constructId']['qname']){
                console.log(elements[x]);
                return {bb:elements[x]['cc']['buggyBody'], fb:elements[x]['cc']['fixedBody'], bc:elements[x]['cc']['bodyChange']};
            }
        }
    } // if more, call the new service by passing the buggybody of the first commit and the fixed body of the last commit
    else if ( count > 1 ){
        if ( isGit ) {
            gitCommitIds = gitCommitIds.sort(function(x,y){return new Date(x.date)- new Date(y.date)});
            for ( var x in gitCommitIds ){
                commitIds.push(gitCommitIds[x]['commit']);
            }
        } else {
            commitIds = commitIds.sort(function(x,y){return x-y;});
        }
        var first = commitIds[0];
        var last = commitIds[commitIds.length -1];
        //console.log(first,last);
        var bb, fb;
        for ( var x in elements){
            if ( elements[x]["overall_chg"] == "MOD" && qname==elements[x]['cc']['constructId']['qname'] ) {
                if (first == elements[x]['cc']['commit']){
                    bb = elements[x]['cc']['buggyBody'];
                } else if ( last == elements[x]['cc']['commit'] ) {
                    fb = elements[x]['cc']['fixedBody'];
                }
            }
        }
        // perform the request
        var url = model.Config.getDiff();

        var jsonBody = [];
        jsonBody.push(JSON.parse(bb));
        jsonBody.push(JSON.parse(fb));
        
        var settings = {
            "async": false,
            "crossDomain": true,
            "url": url,
            "method": "POST",
            "headers": {
              "content-type": "application/json",
              "cache-control": "no-cache",
            },
            "processData": false,
            "data": JSON.stringify(jsonBody)
          }
        
        var bc;
        $.ajax(settings).done(function (response) {
            bc = JSON.stringify(response);
        });
        
        return {bb:bb, fb:fb, bc:bc};
    }
}

model.TreeUtils.getOverallChangeModificationsAffectedCC = function(elements, qname, repo){
    var count = 0;
    var commitIds = [];
    var gitCommitIds = [];
    var isGit = false;
    for ( var x in elements ){
       /* console.log("########################");
        console.log(elements[x]["constructChangeType"]);
        console.log(elements[x]['constructId']["qname"]);
        console.log(qname);
        console.log(elements[x]["repoPath"]);
        console.log(repo);   */     
        if ( isNaN(elements[x]['commit']) ){
            isGit=true;
        }
        if (elements[x]["constructChangeType"] === "MOD" && elements[x]['constructId']['qname']===qname && 
                elements[x]['repoPath'].startsWith(repo)){
             count++;
            if ( !isGit ) {
                commitIds.push(parseInt(elements[x]['commit']));
            } else {/*
                console.log("is git");
                console.log(elements[x]['cc']['committedAt']);
                var date = new Date(elements[x]['cc']['committedAt']);
                console.log(date);*/
                gitCommitIds.push({commit:elements[x]['commit'], date:elements[x]['committedAt']});
            }
        }
    }
    //console.log(commitIds);
    // if only one commit is existing, return the overall change associated to that commit
    if ( count === 1 ){
        for ( var x in elements){
            if ( elements[x]["constructChangeType"] == "MOD" && qname==elements[x]['constructId']['qname']){
                //console.log(elements[x]);
                return {bb:elements[x]['buggyBody'], fb:elements[x]['fixedBody'], bc:elements[x]['bodyChange']};
            }
        }
    } // if more, call the new service by passing the buggybody of the first commit and the fixed body of the last commit
    else if ( count > 1 ){
        if ( isGit ) {
            gitCommitIds = gitCommitIds.sort(function(x,y){return new Date(x.date)- new Date(y.date)});
            for ( var x in gitCommitIds ){
                commitIds.push(gitCommitIds[x]['commit']);
            }
        } else {
            commitIds = commitIds.sort(function(x,y){return x-y;});
        }
        var first = commitIds[0];
        var last = commitIds[commitIds.length -1];
        //console.log(first,last);
        var bb, fb;
        for ( var x in elements){
            if ( elements[x]["constructChangeType"] == "MOD" && qname==elements[x]['constructId']['qname'] ) {
                if (first == elements[x]['commit']){
                    bb = elements[x]['buggyBody'];
                } else if ( last == elements[x]['commit'] ) {
                    fb = elements[x]['fixedBody'];
                }
            }
        }
        // perform the request
        var url = model.Config.getDiff();

        var jsonBody = [];
        jsonBody.push(JSON.parse(bb));
        jsonBody.push(JSON.parse(fb));
        
        var settings = {
            "async": false,
            "crossDomain": true,
            "url": url,
            "method": "POST",
            "headers": {
              "content-type": "application/json",
              "cache-control": "no-cache",
            },
            "processData": false,
            "data": JSON.stringify(jsonBody)
          }
        
        var bc;
        $.ajax(settings).done(function (response) {
            bc = JSON.stringify(response);
        });
        //console.log("count : "+count);
        return {bb:bb, fb:fb, bc:bc};
    }
}

model.TreeUtils.processBodyChange = function(bc){
    var allChangesData = JSON.parse(bc)['StructureEntity']['changes'];
    var newData = [];
    for ( var x in allChangesData ){
        singleData = {};      
        singleData['OperationType']=allChangesData[x]['OperationType'];
        //console.log(allChangesData[x]);
        switch ( allChangesData[x]['OperationType'] ){
            case "Delete":
                singleData['EntityName'] = allChangesData[x]['DeletedEntity']['UniqueName'];
                singleData['EntityType'] = this.statementNameNormalizer(allChangesData[x]['DeletedEntity']['EntityType']);
                singleData['rangeStart'] = allChangesData[x]['DeletedEntity']['SourceCodeRange']['Start'];         
                singleData['rangeEnd'] = allChangesData[x]['DeletedEntity']['SourceCodeRange']['End'];   
                break;
            case "Update":
                singleData['EntityName'] = allChangesData[x]['UpdatedEntity']['UniqueName'];
                singleData['EntityType'] = this.statementNameNormalizer(allChangesData[x]['UpdatedEntity']['EntityType']);
                singleData['NewEntity'] = allChangesData[x]['NewEntity']['UniqueName'];
                singleData['rangeStart'] = allChangesData[x]['UpdatedEntity']['SourceCodeRange']['Start'];         
                singleData['rangeEnd'] = allChangesData[x]['UpdatedEntity']['SourceCodeRange']['End'];         
                singleData['newRangeStart'] = allChangesData[x]['NewEntity']['SourceCodeRange']['Start'];         
                singleData['newRangeEnd'] = allChangesData[x]['NewEntity']['SourceCodeRange']['End'];         
                break;
            case "Insert":
                singleData['NewEntity'] = allChangesData[x]['InsertedEntity']['UniqueName'];
                singleData['EntityType'] = this.statementNameNormalizer(allChangesData[x]['InsertedEntity']['EntityType']);
                singleData['rangeStart'] = allChangesData[x]['InsertedEntity']['SourceCodeRange']['Start'];         
                singleData['rangeEnd'] = allChangesData[x]['InsertedEntity']['SourceCodeRange']['End'];   
                break;
            case "Move":
                singleData['EntityName'] = allChangesData[x]['MovedEntity']['UniqueName'];
                singleData['EntityType'] = this.statementNameNormalizer(allChangesData[x]['MovedEntity']['EntityType']);
                singleData['NewEntity'] = allChangesData[x]['NewEntity']['UniqueName'];
                singleData['rangeStart'] = allChangesData[x]['MovedEntity']['SourceCodeRange']['Start'];         
                singleData['rangeEnd'] = allChangesData[x]['MovedEntity']['SourceCodeRange']['End'];         
                singleData['newRangeStart'] = allChangesData[x]['NewEntity']['SourceCodeRange']['Start'];         
                singleData['newRangeEnd'] = allChangesData[x]['NewEntity']['SourceCodeRange']['End'];     
                break;
        }
        newData.push(singleData);
    }
    return newData;
};

model.TreeUtils.recursiveSearch = function(node, toFind){
    if ( node.name === toFind ) {
        return node.rowIdx;
    }
    for ( var x in node.children ) {
        var retv = this.recursiveSearch(node.children[x], toFind);
        if (retv!=-1){
            return retv;
        }
    }
    return -1;
};

model.TreeUtils.recursiveSearchUpdate = function(node, toFind, nS, nE){
    /*console.log(toFind);
    console.log(node.name);
    console.log(nS);
    console.log(nE);*/
    if ( node.name === toFind && node.rStart == nS && node.rEnd == nE) {
        return node.rowIdx;
    }
    for ( var x in node.children ) {
        var retv = this.recursiveSearchUpdate(node.children[x], toFind, nS, nE);
        if (retv!=-1){
            return retv;
        }
    }
    return -1;
};

model.TreeUtils.recursiveSearchAddMT = function(node, toFind, mT){
    if ( node.name === toFind ) {
        node.modificationType = mT;
        return node.rowIdx;
    }
    for ( var x in node.children ) {
        var retv = this.recursiveSearchAddMT(node.children[x], toFind, mT);
        if (retv!=-1){
            return retv;
        }
    }
    return -1;
};

model.TreeUtils.filterMovUpd = function(nodes){
    var preFiltered = [];
    var toPush = [];
    for ( var i=0; i<preFiltered.length; i++ ){
        toPush[i] = 1;
    }
    for ( var x in nodes ) {
        if (nodes[x]['OperationType'] === 'Move') {
            var found = false;
            // look for the correspondend update (if any)
            for ( var y in nodes ){
                if ( nodes[y]['OperationType'] === 'Update' ){
                    if ( nodes[x]['EntityName'] === nodes[y]['EntityName'] &&
                         nodes[x]['EntityType'] === nodes[y]['EntityType'] &&
                         nodes[x]['NewEntity'] === nodes[y]['NewEntity'] ) {
                            found = true;
                            break;
                         }
                }
            }
            if ( found ) {
                var newNode = {
                                EntityName:nodes[x]['EntityName'],
                                EntityType:nodes[x]['EntityType'],
                                NewEntity:nodes[x]['NewEntity'],
                                OperationType:'Mov-Upd',
                                newRangeEnd:nodes[x]['newRangeEnd'],
                                newRangeStart:nodes[x]['newRangeStart'],
                                rangeStart:nodes[x]['rangeStart'],
                                rangeEnd:nodes[x]['rangeEnd']
                                };
                preFiltered.push(newNode);
            } else {
                preFiltered.push(nodes[x]);
            }
        } else {
            preFiltered.push(nodes[x]);
        }
    }
    var filtered = [];
    for ( var x in preFiltered ) {
        if ( preFiltered[x]['OperationType'] === 'Update' ){
            // look if a Mov-Upd already exists
            var found = false;
            for ( var y in preFiltered ){
                if ( preFiltered[y]['OperationType'] === 'Mov-Upd' ){ 
                    if ( preFiltered[x]['EntityName'] === preFiltered[y]['EntityName'] &&
                         preFiltered[x]['EntityType'] === preFiltered[y]['EntityType'] &&
                         preFiltered[x]['NewEntity'] === preFiltered[y]['NewEntity'] ) {
                            found = true;
                            break;
                         }
                }
            }
            if ( found ) {
                // do not push
            } else {
                filtered.push(preFiltered[x]);
            }
        } else {
            filtered.push(preFiltered[x]);
        }
    }
    return filtered;
};

model.TreeUtils.getNewRange = function(changesData, entityName, oldStart, oldEnd){
    //console.log(entityName);
    //console.log(oldStart);
    //console.log(oldEnd);
    for (var x in changesData){
        //console.log(changesData[x]);
        var et = changesData[x]['EntityType'];
            if ( et=='' ){
                et = ' ';
            } else {
                et += ' ';
            }
        if ( entityName === (et+changesData[x]['EntityName']) && 
                oldStart == parseInt(changesData[x]['rangeStart']) && 
                oldEnd == parseInt(changesData[x]['rangeEnd'])   ) {
            return {newEntity:et+changesData[x]['NewEntity'],newStart:changesData[x]['newRangeStart'], newEnd:changesData[x]['newRangeEnd']};
        }
    }
    return -1;
};

model.TreeUtils.getOldRange = function(changesData, entityName, newStart, newEnd){
    for (var x in changesData){
        var et = changesData[x]['EntityType'];
            if ( et=='' ){
                et = ' ';
            }else {
                et += ' ';
            }
        if ( entityName === (et+changesData[x]['NewEntity']) && 
                newStart == parseInt(changesData[x]['newRangeStart']) && 
                newEnd == parseInt(changesData[x]['newRangeEnd'])   ) {
            return {newEntity:et+changesData[x]['EntityName'],oldStart:changesData[x]['rangeStart'], oldEnd:changesData[x]['rangeEnd']};
        }
    }
    return -1;
};

model.TreeUtils.normalizeIndexRange = function(tree, index){
    var l = tree.getBinding("rows").getLength();
    if ( index >= l ) {
        return l-1;
    }
    return index;
};

model.TreeUtils.getVisibleRowCountTable = function(table){
    var l = table.getBinding("rows").getLength();
    if ( l>=15 ){
        return 15;
    }
    return l;
};

model.TreeUtils.getTestedBody = function(elements, qname, repo){
    //console.log(qname);
    //console.log(repo);
    for ( var x in elements ){
        //console.log(elements[x]);
        if (elements[x]["overall_chg"] == "MOD" && elements[x]['cc']['constructId']['qname']===qname && 
                elements[x]['cc']['repoPath']==repo){
            //console.log(elements[x]['testedBody']);
             return elements[x]['testedBody'];
        }        
    }
};

model.TreeUtils.parseConstructChangesPath = function(elements){
    var treeRoot = {
                root:{
                        name: "root",
                        children : []
                }};
    var repos = this.findAllPaths(elements);
    //console.log(repos);
    for ( var x in repos ){
        var oNode = {name:repos[x], children:[], repo:repos[x]};
        this.findAllQnamesPerPath(elements, oNode);
        treeRoot["root"]['children'].push(oNode);
    }
    //console.log(treeRoot);
    return treeRoot;
};

model.TreeUtils.findAllPaths = function(elements) {
    var repoPaths = [];
    for  ( var x in elements ) {
        //console.log(elements[x]);
        var repoPathName = model.TreeUtils.repoPathNameNormalizer(elements[x]['repoPath']);
        if ( typeof(repoPaths[repoPathName]) === 'undefined' ){
            repoPaths[repoPathName] = repoPathName;
        }
    }
    return repoPaths;
};

model.TreeUtils.repoPathNameNormalizer = function(repoPath){
    var trail = repoPath.split("/");
    var normalized = repoPath.replace(trail[trail.length-1], "");
    return normalized;
};

model.TreeUtils.findAllQnamesPerPath = function(elements, repoPath){
	var processed=[];
    for (var x in elements){
        var repoPathName = model.TreeUtils.repoPathNameNormalizer(elements[x]['repoPath']);
        if (elements[x]['constructId']['type']==='METH' || elements[x]['constructId']['type']==='CONS'
        	  || elements[x]['constructId']['type']==='FUNC' || elements[x]['constructId']['type']==='MODU' || elements[x]['constructId']['type']==='STMT' ){
            if ( repoPathName == repoPath['name'] && processed.indexOf(elements[x]['constructId']['qname'])==-1){
            	processed.push(elements[x]['constructId']['qname']);
                var oNode = {name:elements[x]['constructId']['qname'], type:elements[x]['constructId']['type'],
                            overallChg:elements[x]['constructChangeType'] ,children:[], repo:repoPath['name']};
                this.findAllCommitsPerQname(elements, repoPath['name'], oNode);
                repoPath['children'].push(oNode);
            }
        }
    }
};

model.TreeUtils.findAllCommitsPerQname = function(elements, repoPath, qname){
    for ( var x in elements ) {
        var elemRepoPathName = model.TreeUtils.repoPathNameNormalizer(elements[x]['repoPath']);
        if ( elemRepoPathName == repoPath && elements[x]['constructId']['qname'] == qname['name']){
            var oNode = {name:elements[x]['commit'], children:[]};
            qname['children'].push(oNode);
        }
    }
};

model.TreeUtils.parseConstructChangesCommit = function(elements){
    var treeRoot = {
                root:{
                        name: "root",
                        children : []
                }};
    var commits = this.findAllCommits(elements);
    for ( var x in commits ){
        var oNode = {name:commits[x], children:[]};
        this.findAllPathsPerCommit(elements, oNode);
        treeRoot["root"]['children'].push(oNode);
    }
    return treeRoot;
};

model.TreeUtils.findAllCommits = function(elements){
    var commits = [];
    for  ( var x in elements ) {
        var commitValue = elements[x]['commit'];
        if ( typeof(commits[commitValue]) === 'undefined' ){
            commits[commitValue] = commitValue;
        }
    }
    return commits;
};

model.TreeUtils.findAllPathsPerCommit = function(elements, commit){
    var pathCommits = [];
    for ( var x in elements){
        if ( elements[x]['commit'] == commit['name'] ){
            var pathName = this.repoPathNameNormalizer(elements[x]['repoPath']);
            if (typeof(pathCommits[pathName])==='undefined'){
                pathCommits[pathName] = pathName;
                var oNode = {name:pathName, children:[]};
                this.findAllQnamesPerCommitPerPath(elements, commit['name'], oNode);
                commit['children'].push(oNode);
            }
        }
    }
};

model.TreeUtils.findAllQnamesPerCommitPerPath = function(elements, commit, path){
    var processed=[];
	for ( var x in elements ) {
        var repoName = this.repoPathNameNormalizer(elements[x]['repoPath']);
        if ( repoName == path['name'] && elements[x]['commit'] == commit && 
             ( elements[x]['constructId']['type']==='METH' || elements[x]['constructId']['type']==='CONS'
                || elements[x]['constructId']['type']==='FUNC' || elements[x]['constructId']['type']==='MODU' || elements[x]['constructId']['type']==='STMT')
             && processed.indexOf(elements[x]['constructId']['qname'])==-1) {
        	processed.push(elements[x]['constructId']['qname']);
            var oNode = {name:elements[x]['constructId']['qname'], children:[], 
                type:elements[x]['constructId']['type'], overallChg:elements[x]['constructChangeType']};
            path['children'].push(oNode);
        }
    }
};

/*
model.TreeUtils.recursiveVisitBuildTree = function(entity){
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
*/