jQuery.sap.require("view.GraphDetailObject");

var bugId;
var archiveId;
var app = {};
var graphData={};
var graphid;
var cve = {};
var change;
var allnodes = [], alledges = [];
var risks =[];
	
sap.ui.controller("view.GraphDetail", {
	onInit : function() {
		this.router = sap.ui.core.UIComponent.getRouterFor(this);
		this.router.attachRoutePatternMatched(this._handleRouteMatched, this);
	},
	
	_handleRouteMatched : function(evt) {
		if (evt.getParameter("name") !== "graphDetail") {
            return;
        }
		bugId = evt.getParameter("arguments").bugid;
		archiveId = evt.getParameter("arguments").archiveid;
		
		app.groupid = evt.getParameter("arguments").group;
		app.artifactid = evt.getParameter("arguments").artifact;
		app.version = evt.getParameter("arguments").version;
		
//		graphid = evt.getParameter("arguments").graphid;
		change = evt.getParameter("arguments").change;
		console.log( "route matched " + change + " " + app.groupid + app.artifactid + " " + app.version + " "+ archiveId + " " + bugId);
		this.loadDataIntoView();
	},
	
	loadDataIntoView : function() {
	    if(bugId && change) {
    		var graphDetailModel = new sap.ui.model.json.JSONModel();
    //		var graphDetailModelTemp = new sap.ui.model.json.JSONModel();
    		var graphDetailPage = this.getView().byId('idGraphDetailPage');
    		graphDetailPage.setTitle(bugId);
    		var oldGraph = graphDetailPage.getModel();
    		var data = [];
			if(oldGraph!=undefined){
				oldGraph.setData(data,false);
				oldGraph.refresh();
			}
			var oGraphHolder = this.getView().byId("GraphHolder");
			if(oGraphHolder!=undefined)
				oGraphHolder.removeAllItems();
    		
    		sUrl = model.Config.getReachabilityGraphServiceUrl(app.groupid,app.artifactid,app.version,archiveId,bugId,change);
    		model.Config.loadData (graphDetailModel,sUrl, 'GET');
			
			
			
			graphDetailPage.setModel(graphDetailModel);

    		/*$.get(sUrl, function(data) {
    			data.change = change;
    			graphDetailModel.setData(data);
    			graphDetailPage.setModel(graphDetailModel);
    			
    			//process all nodes and edges data
    			console.log("[GraphDetail.controller] Processing all data read from database...");
    			allnodes = data.constructs, alledges = data.paths;
    
    	        //process information for all edges
    	  		for( count in alledges ) {
    	  			if(allnodes[alledges[count].to].qname == change) allnodes[alledges[count].to].nodetype = "target";
    	  			alledges[count].source = allnodes[alledges[count].from];
    	  			alledges[count].target = allnodes[alledges[count].to];
    	  		}
    	  		
    		    this.onclickRemoveButton();
    		}.bind(this));*/
			//process all nodes and edges data
			//graphDetailModel.attachRequestCompleted(function() {
			graphDetailModel.attachRequestCompleted(function() {
				 this.processModel(graphDetailModel)

			
//				console.log("[GraphDetail.controller] Processing all data read from database...");
//				console.log((new Date).toLocaleTimeString());
//				var paths = graphDetailModel.getProperty("/");
//				var from=null;
//				var to=null;
//				var edge={};
//				allnodes = [], alledges = [];
//				var existing_nodes=[];
//				for(var i=0; i<paths.length;i++){	
//					from=null;
//					to=null;
//					for(var j=0;j<paths[i].path.length;j++){
//						to = paths[i].path[j].constructId;
//						to.lib = paths[i].path[j].lib;
//						var tmp_namelist = [];
//						//analyze qname to build method name, class name and package name
//				    	tmp_namelist = to.qname.substring(0, to.qname.indexOf("(")).split(".");
//						if(to.type == "CONS") {
//				    		to.method = tmp_namelist[tmp_namelist.length-1];
//				    		to.method_param = tmp_namelist[tmp_namelist.length-1] + to.qname.substring(to.qname.indexOf("("));
//				    		to.cls = tmp_namelist[tmp_namelist.length-1];
//				    		to.pkg = tmp_namelist[tmp_namelist.length-2]
//				    	} //method
//				    	else if (to.type == "METH") {
//				    		to.method = tmp_namelist[tmp_namelist.length-1]; //method name
//				    		to.method_param = tmp_namelist[tmp_namelist.length-1] + to.qname.substring(to.qname.indexOf("("));
//				    		to.cls = tmp_namelist[tmp_namelist.length-2]; //class name
//				    		to.pkg = tmp_namelist[tmp_namelist.length-3]; //package name
//				    	}
//						
//					//	console.log(JSON.stringify(existing_nodes));
//						if(existing_nodes.indexOf(to.qname)==-1){
//							existing_nodes.push(to.qname);
//							if(paths[i].source=='X2C'){
//								to.traced= 'timestamp';
//							}else{
//								var traced=false;
//								//check whether the construct is traced in some other path
//								for(var w=i;w<paths.length;w++){
//									for(var z=0;z<paths[w].path.length;z++){
//										if(paths[i].path[j].constructId==paths[w].path[z].constructId && paths[w].source=='X2C')
//											to.traced= 'timestamp';
//										if(paths[i].path[j].constructId==paths[w].path[z].constructId && z==0)
//											to.nodetype="entrypoint";
//									}
//								}
//								if(!traced)
//									to.traced = 'NA';
//							}
//							to.visible=true;
//							if (from == null){
//								//if from is null we are processing an entrypoint
//								to.nodetype="entrypoint";
//							}
//							//if we are processsing the last element of the pathnode list, set the nodetype to target
//							//if(j==paths[i].path.length-1){
//							if(to.qname==change){
//								to.nodetype="target";
//							}
//							allnodes.push(to);
//						}
//						else{
//							to=allnodes[existing_nodes.indexOf(to.qname)];
//						}
//					
//						
//						
//						if(from!=null){
//							edge={}
//							edge.source=from;
//							edge.target=to;
//							edge.exploitability='NN';
//							edge.type=(paths[i].source=='X2C')?'TST':'RA';
//							edge.visible=true;
//							edge.pathid=i;
//						}
//						
//						if(edge.hasOwnProperty("source"))
//							alledges.push(edge);
//						from=to;
//						
//						//create additional links for explotability
//						if(from!=null && paths[i].hasOwnProperty("exploitability")){
//							edge={}
//							edge.source=from;
//							edge.target=to;
//							edge.type = 'NN';
//							edge.exploitability=paths[i].exploitability;
//							edge.visible=true;
//							edge.pathid=i;
//						}
//						if(edge.hasOwnProperty("source"))
//							alledges.push(edge);
//						from=to;
//						
//					}
//				}
//			//	console.log(JSON.stringify(allnodes));
//			//	console.log(JSON.stringify(alledges));
//			
//				/*
//				allnodes = graphDetailModel.getProperty("/constructs"), alledges = graphDetailModel.getProperty("/paths");
//		        //process information for all edges
//		  		for( count in alledges ) {
//		  			if(allnodes[alledges[count].to].qname == change) allnodes[alledges[count].to].nodetype = "target";
//		  			alledges[count].source = allnodes[alledges[count].from];
//		  			alledges[count].target = allnodes[alledges[count].to];
//		  		}*/
//		  	//	console.log("end for");
//		  		console.log((new Date).toLocaleTimeString());
//				graphData.change = change;
//				graphData.totalPathsCount = graphDetailModel.getProperty("/").length;
//				var changeModel = new sap.ui.model.json.JSONModel();
//				changeModel.setData(graphData);
//				this.getView().byId('idGraphDetailPage').setModel(changeModel, "data");
//			    this.onclickRemoveButton();
			}.bind(this));
			
	    }

	},

	//user-interactive collapse
	onclickCollapsePKGButton : function(oEvent) {
		this.onclickRemoveButton();
		var pkg = this.byId("pkg").mProperties.value;
		if( !pkg == "" ) {
			tmplist = pkg.split(";");
			slist = [];
			for( n in tmplist ) {
				if(tmplist[n]!="") slist.push(tmplist[n]);
			}
			this.collapse(slist,false);
		}
	},
	
	//Collapse the graph on class-level
	onclickClassGraphButton : function() {
		this.onclickRemoveButton();
		var qname = "", list = [];
		for( count in allnodes ) {
			qname = allnodes[count].qname;
			if( allnodes[count].type == "CONS" ) qname = qname.substring(0, qname.indexOf("("));
			else qname = qname.substring(0, qname.lastIndexOf("."));
			if(list.indexOf(qname) == -1) {
				list.push(qname);
			}
		}
		this.collapse(list,false);
	},
	
	//Collapse the graph on package-level
	onclickPackageGraphButton : function() {
		this.onclickRemoveButton();
		var qname = "", list = [];
		for( count in allnodes ) {
			qname = allnodes[count].qname;
			if( allnodes[count].type == "CONS" ) qname = qname.substring( 0, (qname.indexOf("(")-allnodes[count].cls.length-1) );
			else qname = qname.substring( 0, (qname.lastIndexOf(".")-allnodes[count].cls.length-1) );
			if(list.indexOf(qname) == -1) {
				list.push(qname,false);
			}
		}
		this.collapse(list);
	},
	
	onclickLibGraphButton : function() {
		this.onclickRemoveButton();
		var qname = "", lib="", 	list = [];
		for( count in allnodes ) {
			qname = allnodes[count].qname;
			if( allnodes[count].lib != null ) 
				if(allnodes[count].lib.libraryId!=null){
					  qname = (allnodes[count].lib.libraryId.group + ":" + allnodes[count].lib.libraryId.artifact + ":" + allnodes[count].lib.libraryId.version);
				  }
				else if (allnodes[count].dep!=null){
					 qname = (allnodes[count].dep.filename);
				}
				  else
					  qname = allnodes[count].digest ;
			else if( allnodes[count].type == "CONS" ) qname = qname.substring( 0, (qname.indexOf("(")-allnodes[count].cls.length-1) );
			else qname = qname.substring( 0, (qname.lastIndexOf(".")-allnodes[count].cls.length-1) );
			if(list.indexOf(qname) == -1) {
				list.push(qname);
			}
		}
		this.collapse(list,true);
	},
	
	//clean the svg/graph
	onclickRemoveButton : function() {
		for( count in allnodes ){
			allnodes[count].visible = true;
		}
		var oGraphHolder = this.getView().byId("GraphHolder");
		var oGraph = new view.GraphDetailObject();
		//console.log(alledges);
		oGraph.setData(allnodes, alledges);
		if(risks.length>0)
			oGraph.setRisks(risks);
		oGraph.setMetadata(bugId, archiveId, app,graphid);
		oGraphHolder.removeAllItems();
		oGraphHolder.addItem(oGraph);
	},
	
	collapse : function(slist,_lib) {
		slist.sort();
		console.log("[GraphDetail.controller].collapse for [ " + slist.toString() + " ]");
		var currentnodes = [], currentedges = [], updatednodes = [], newedge, src, tgt;
		for( n in slist ){
			for( count in allnodes ){
				if( allnodes[count].qname.substring(0, slist[n].length) == slist[n] ) allnodes[count].visible = false;
				if(_lib && allnodes[count].lib!=null){
					var libid = undefined;
					var dep = undefined;
					if(allnodes[count].lib.libraryId!=null)
						libid = allnodes[count].lib.libraryId.group + ":" + allnodes[count].lib.libraryId.artifact + ":" + allnodes[count].lib.libraryId.version;
					if(allnodes[count].dep!=null)
						dep =  allnodes[count].dep;
					if((libid!=undefined && libid.substring(0, slist[n].length)) ||
							(dep!= undefined && dep.filename.substring(0, slist[n].length)) || 
							allnodes[count].lib.digest.substring(0, slist[n].length)) 
						allnodes[count].visible = false;
				}
			}
			currentnodes.push({"lang":"JAVA", "type":"PACK", "qname":slist[n], "visible":false});
		}
		
		//compute all edges
		for ( count in alledges ) {
			src = alledges[count].source.visible;
			tgt = alledges[count].target.visible;
			newedge =  Object.create(alledges[count]);
			if( (!src) && (!tgt) ) {
				for( n in slist ){
					if( newedge.source.qname.substring(0, slist[n].length) === slist[n] ) {
						currentnodes[n].nodetype = newedge.source.nodetype;
						newedge.source = currentnodes[n];
						currentnodes[n].visible = true; 
					}else if(_lib && newedge.source.lib!=null){
						var libid = undefined;
						var dep = undefined;
						if(newedge.source.lib.libraryId!=null)
							libid = newedge.source.lib.libraryId.group + ":" + newedge.source.lib.libraryId.artifact + ":" + newedge.source.lib.libraryId.version;
						if(newedge.source.dep!=null)
							dep =  newedge.source.dep;

						if((libid!=undefined && libid.substring(0, slist[n].length) === slist[n] ) ||
								(dep!=undefined && dep.filename.substring(0, slist[n].length)=== slist[n] ) ||
										newedge.source.lib.digest.substring(0, slist[n].length)=== slist[n]) {
							currentnodes[n].nodetype = newedge.source.nodetype;
							newedge.source = currentnodes[n];
							currentnodes[n].visible = true; 
						}
					}
					if( newedge.target.qname.substring(0, slist[n].length) === slist[n] ) {
						currentnodes[n].nodetype = newedge.target.nodetype;
						newedge.target = currentnodes[n];
						currentnodes[n].visible = true; 
					}else if(_lib && newedge.target.lib!=null){
						var libid = undefined;
						var dep = undefined;
						if(newedge.target.lib.libraryId!=null)
							libid = newedge.target.lib.libraryId.group + ":" + newedge.target.lib.libraryId.artifact + ":" + newedge.target.lib.libraryId.version;
						if(newedge.target.dep!=null)
							dep =  newedge.target.dep;

						if((libid!=undefined && libid.substring(0, slist[n].length) === slist[n] ) ||
								(dep!=undefined && dep.filename.substring(0, slist[n].length)=== slist[n] ) ||
										newedge.target.lib.digest.substring(0, slist[n].length)=== slist[n]) {
							currentnodes[n].nodetype = newedge.target.nodetype;
							newedge.target = currentnodes[n];
							currentnodes[n].visible = true; 
						}
					}
				}
			}else if( (!src) && (tgt) ) {
				for( n in slist ){
					if( newedge.source.qname.substring(0, slist[n].length) === slist[n] ) {
						currentnodes[n].nodetype = newedge.source.nodetype;
						newedge.source = currentnodes[n];
						currentnodes[n].visible = true; 
					}else if(_lib && newedge.source.lib!=null){
						var libid = undefined;
						var dep = undefined;
						if(newedge.source.lib.libraryId!=null)
							libid = newedge.source.lib.libraryId.group + ":" + newedge.source.lib.libraryId.artifact + ":" + newedge.source.lib.libraryId.version;
						if(newedge.source.dep!=null)
							dep =  newedge.source.dep;

						if((libid!=undefined && libid.substring(0, slist[n].length) === slist[n] ) ||
								(dep!=undefined && dep.filename.substring(0, slist[n].length)=== slist[n] ) ||
										newedge.source.lib.digest.substring(0, slist[n].length)=== slist[n]) {
							currentnodes[n].nodetype = newedge.source.nodetype;
							newedge.source = currentnodes[n];
							currentnodes[n].visible = true; 
						}
					}
				}
			}else if( (src) && (!tgt) ) {
				for( n in slist ){
					if( newedge.target.qname.substring(0, slist[n].length) === slist[n] ) {
						currentnodes[n].nodetype = newedge.target.nodetype;
						newedge.target = currentnodes[n];
						currentnodes[n].visible = true; 
					}else if(_lib && newedge.source.lib!=null){
						var libid = undefined;
						var dep = undefined;
						if(newedge.target.lib.libraryId!=null)
							libid = newedge.target.lib.libraryId.group + ":" + newedge.target.lib.libraryId.artifact + ":" + newedge.target.lib.libraryId.version;
						if(newedge.target.dep!=null)
							dep =  newedge.target.dep;

						if((libid!=undefined && libid.substring(0, slist[n].length) === slist[n] ) ||
								(dep!=undefined && dep.filename.substring(0, slist[n].length)=== slist[n] ) ||
										newedge.target.lib.digest.substring(0, slist[n].length)=== slist[n]) {
							currentnodes[n].nodetype = newedge.target.nodetype;
							newedge.target = currentnodes[n];
							currentnodes[n].visible = true; 
						}
					}
				}
			}
			if(newedge.source != newedge.target) {
				currentedges.push(newedge);
			}
		}
		
		for(count in allnodes){
			if(allnodes[count].visible) currentnodes.push(allnodes[count]);
		}
		
		var oGraphHolder = this.getView().byId("GraphHolder");
		var oGraph = new view.GraphDetailObject();
		oGraph.setData(currentnodes, currentedges);
		if(risks.length>0)
			oGraph.setRisks(risks);
		oGraph.setMetadata(bugId, archiveId, app, graphid);
		oGraphHolder.removeAllItems();
		oGraphHolder.addItem(oGraph);
	},	
	
	showNodeDetail : function() {
		alert("node detail");
	},
	
	onExit : function() {

	},

	handleNavBack : function() {
		window.history.go(-1);
	},
	
	openNVD : function(oEvent) {
	    var graphDetailPage = this.getView().byId('idGraphDetailPage');
	    var data = graphDetailPage.getModel().getData();
	    var url = data.cve.link;
	    this.openLink(url, 'nvd');
	},
	
	openLink : function(_url, _window) {
	    window.open(_url, _window).focus();
	},
	

	processModel : function(graphDetailModel) {
		console.log("[GraphDetail.controller] Processing all data read from database...");
		console.log((new Date).toLocaleTimeString());
		var paths = graphDetailModel.getProperty("/");
		var from=null;
		var to=null;
		var edge={};
		allnodes = [], alledges = []; risks=[];
		var existing_nodes=[];
		for(var i=0; i<paths.length;i++){	
			from=null;
			to=null;
			for(var j=0;j<paths[i].path.length;j++){
				to = paths[i].path[j].constructId;
				to.lib = paths[i].path[j].lib;
				to.dep = paths[i].path[j].dep;
				var tmp_namelist = [];
				//analyze qname to build method name, class name and package name
		    	tmp_namelist = to.qname.substring(0, to.qname.indexOf("(")).split(".");
				if(to.type == "CONS") {
		    		to.method = tmp_namelist[tmp_namelist.length-1];
		    		to.method_param = tmp_namelist[tmp_namelist.length-1] + to.qname.substring(to.qname.indexOf("("));
		    		to.cls = tmp_namelist[tmp_namelist.length-1];
		    		to.pkg = tmp_namelist[tmp_namelist.length-2]
		    	} //method
		    	else if (to.type == "METH") {
		    		to.method = tmp_namelist[tmp_namelist.length-1]; //method name
		    		to.method_param = tmp_namelist[tmp_namelist.length-1] + to.qname.substring(to.qname.indexOf("("));
		    		to.cls = tmp_namelist[tmp_namelist.length-2]; //class name
		    		to.pkg = tmp_namelist[tmp_namelist.length-3]; //package name
		    	}
				
			//	console.log(JSON.stringify(existing_nodes));
				if(existing_nodes.indexOf(to.qname)==-1){
					existing_nodes.push(to.qname);
					if(paths[i].source=='X2C'){
						to.traced= 'timestamp';
					}else{
						var traced=false;
						//check whether the construct is traced in some other path
						for(var w=i;w<paths.length;w++){
							for(var z=0;z<paths[w].path.length;z++){
								if(paths[i].path[j].constructId==paths[w].path[z].constructId && paths[w].source=='X2C')
									to.traced= 'timestamp';
								if(paths[i].path[j].constructId==paths[w].path[z].constructId && z==0)
									to.nodetype="entrypoint";
							}
						}
						if(!traced)
							to.traced = 'NA';
					}
					to.visible=true;
					if (from == null){
						//if from is null we are processing an entrypoint
						to.nodetype="entrypoint";
					}
					//if we are processsing the last element of the pathnode list, set the nodetype to target
					//if(j==paths[i].path.length-1){
					if(to.qname==change){
						to.nodetype="target";
					}
					allnodes.push(to);
				}
				else{
					to=allnodes[existing_nodes.indexOf(to.qname)];
				}
			
				
				
				if(from!=null && !paths[i].hasOwnProperty("exploitability")){
					edge={}
					edge.source=from;
					edge.target=to;
					edge.exploitability='NN';
					edge.type=(paths[i].source=='X2C')?'TST':'RA';
					edge.visible=true;
					edge.pathid=i;
				}
				
				if(edge.hasOwnProperty("source"))
					alledges.push(edge);
				//from=to;
				
				//create additional links for explotability
				if(from!=null && paths[i].hasOwnProperty("exploitability")){
					edge={}
					edge.source=from;
					edge.target=to;
					edge.type = 'NN';
					edge.exploitability=paths[i].exploitability;
					edge.visible=true;
					edge.pathid=i;
					if(risks.indexOf(paths[i].exploitability)==-1)
						risks.push(paths[i].exploitability);
				}
				if(edge.hasOwnProperty("source"))
					alledges.push(edge);
				from=to;
				
			}
		}
	//	console.log(JSON.stringify(allnodes));
	//	console.log(JSON.stringify(alledges));
	
		/*
		allnodes = graphDetailModel.getProperty("/constructs"), alledges = graphDetailModel.getProperty("/paths");
        //process information for all edges
  		for( count in alledges ) {
  			if(allnodes[alledges[count].to].qname == change) allnodes[alledges[count].to].nodetype = "target";
  			alledges[count].source = allnodes[alledges[count].from];
  			alledges[count].target = allnodes[alledges[count].to];
  		}*/
  	//	console.log("end for");
  		console.log((new Date).toLocaleTimeString());
		graphData.change = change;
		graphData.totalPathsCount = graphDetailModel.getProperty("/").length;
		var changeModel = new sap.ui.model.json.JSONModel();
		changeModel.setData(graphData);
		this.getView().byId('idGraphDetailPage').setModel(changeModel, "data");
	    this.onclickRemoveButton();
	},
	
	onclickExploitabilityButton: function(){

		
		var graphModel = this.getView().byId('idGraphDetailPage').getModel();
		var oGraphHolder = this.getView().byId("GraphHolder");
		oGraphHolder.removeAllItems();

		
		var graphController = this;
		//call UniTn service
		 $.ajax({
		        type: "POST",
		        url: model.Config.getExploitabilityServiceUrl()+"?"+ model.Config.getAppPack(),
		        data : JSON.stringify(graphModel.getData()),
		    //    headers : {'content-type': "application/json",'cache-control': "no-cache" },
		        headers : {'content-type': "application/json" ,'X-Vulas-Version':model.Version.version,'X-Vulas-Component':'appfrontend'},
		        success: function(data){
		        	var newGraphModel = new sap.ui.model.json.JSONModel();

		        	//newGraphModel.setData([],false);
		        	newGraphModel.setData(data,false);
		        	graphController.processModel(newGraphModel);
		        	//graphModel.refresh();
		        	//graphDetailPage.setModel(graphModel);
		        	

		        }, 
		        
		 });

	},
	
	openWiki : function(evt){
		
		model.Config.openWiki("Vulnerable-Code-Call-Graph");
		
	},
	

	
});
