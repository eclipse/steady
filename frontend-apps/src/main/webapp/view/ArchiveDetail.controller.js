var libId = "";
var ajaxQueue1 = [];

sap.ui.controller("view.ArchiveDetail", {

	onInit : function() {
		this.router = sap.ui.core.UIComponent.getRouterFor(this);
		this.router.attachRoutePatternMatched(this._handleRouteMatched, this);
	},

	_handleRouteMatched : function(evt) {
		if (evt.getParameter("name") !== "archiveDetail") {
            return;
        }

		
		archiveId = evt.getParameter("arguments").archiveid;
		groupId = evt.getParameter("arguments").group;
		artifactId = evt.getParameter("arguments").artifact;
		version = evt.getParameter("arguments").version;
		
		model.Config.cleanRequests();
		var req;
		while(req = ajaxQueue1.pop()){
			req.abort();
		}
		
		var archiveDetailPage = this.getView().byId('archiveDetailPage');
		var archiveDetailPageController = this.getView("view.ArchiveDetail").getController();
		var archiveMetricTable = this.getView().byId("idArchiveMetrics");
		
		var data =[];
		var emptyModel = new sap.ui.model.json.JSONModel();
		emptyModel.setData(data);
		archiveMetricTable.setModel(emptyModel,"metricModel");
		archiveDetailPage.setModel(emptyModel,"archiveTouchPoints");
		archiveDetailPage.setModel(emptyModel,"archiveConstructCounts");
		archiveDetailPage.setModel(emptyModel);
		
		var callersCount = this.getView().byId("callersCount");
		var calleesCount = this.getView().byId("calleesCount");
		var callsCount = this.getView().byId("callsCount");

//		//clear previous models
//        var data =[];
//		var oldmetrics=archiveMetricTable.getModel('metricModel');
//		if(oldmetrics!=undefined){
//			oldmetrics.setData(data,false);
//			oldmetrics.refresh();
//		}
//		var oldTP = archiveDetailPage.getModel("archiveTouchPoints");
//		if(oldTP!=undefined){
//			oldTP.setData(data,false);
//			oldTP.refresh();
//		}
//		var oldCounts = archiveDetailPage.getModel("archiveConstructCounts");
//		if(oldCounts!=undefined){
//			oldCounts.setData(data,false);
//			oldCounts.refresh();
//		}
		
		var oArchiveDetailsModel = new sap.ui.model.json.JSONModel();
		var oArchiveVuln = new sap.ui.model.json.JSONModel();
		
		
		if (!model.Config.isMock) {
			
			
			
			
			sUrl = model.Config.getArchivePropertiesServiceUrl(groupId,artifactId,version,archiveId);
			model.Config.addToQueue(oArchiveDetailsModel);
			model.Config.loadData (oArchiveDetailsModel,sUrl, 'GET');
			archiveDetailPage.setModel(oArchiveDetailsModel);
			/*$.get(sUrl, function(data) {
				oArchiveDetailsModel.setData(data);
				archiveDetailPage.setModel(oArchiveDetailsModel);
			}.bind(this));*/
			
			//extract information on contructsCountType in a format suitable for the view
			var archiveConstructCounts = [];
			oArchiveDetailsModel.attachRequestCompleted(function() {
				model.Config.remFromQueue(oArchiveDetailsModel);
				var constructTypeCounters = oArchiveDetailsModel.getObject("/lib/constructTypeCounters") ;
				var constructTypeCountersReachable = oArchiveDetailsModel.getObject("/reachableConstructTypeCounters") ;
				var constructTypeCountersTraced = oArchiveDetailsModel.getObject("/tracedConstructTypeCounters") ;
				var archiveConstructCount = {};
			//	console.log(JSON.stringify(constructTypeCounters));
				for(var k in Object.keys(constructTypeCounters)){
					var type = Object.keys(constructTypeCounters)[k];
					archiveConstructCount={};
					if(type!="countTotal"){
						archiveConstructCount.type=type;
						archiveConstructCount.value=constructTypeCounters[type];
						archiveConstructCount.valueReach=constructTypeCountersReachable[type];
						archiveConstructCount.valueTraced=constructTypeCountersTraced[type];
						archiveConstructCounts.push(archiveConstructCount);
					}
				}
				var count = {};
				count.constructTypeCounters = archiveConstructCounts;
			//	console.log(JSON.stringify(count));
				var countModel = new sap.ui.model.json.JSONModel();
	    		countModel.setData(count);
	    		archiveDetailPage.setModel(countModel, "archiveConstructCounts");
	    		
	    		var touchPointsReachTrc = [];
				var touchPointRT={};
				var processedTP = [];
	    		var touchPoints = oArchiveDetailsModel.getObject("/touchPoints") ;
	    		var callers = [];
	    		var callees = [];
	    		//console.log(JSON.stringify(touchPoints));
		    	if(touchPoints){
		    		for(var t=0;t<touchPoints.length;t++){
		    			var call=touchPoints[t].from.qname.concat(touchPoints[t].to.qname);
		    			var reach=false;
		    			var traced=false;
		    			if(processedTP.indexOf(call)==-1){
		    				processedTP.push(call);
		    				touchPointRT={};
		    				for (var tt=t;tt<touchPoints.length;tt++){
			    				if(touchPoints[tt].from.qname.concat(touchPoints[tt].to.qname)==call){
			    				//	if(touchPoints[tt].source!=null){
				    					if(touchPoints[tt].source=='X2C')
				    						traced=true;
				    					else
				    						reach=true;
			    					}
			    				//}
		    				}
		    				if(touchPoints[t].direction=='L2A'){
		    					touchPointRT.from=touchPoints[t].to;
		    					touchPointRT.to=touchPoints[t].from;

		    				}
		    				else{
		    					touchPointRT.from=touchPoints[t].from;
		    					touchPointRT.to=touchPoints[t].to;
		    				}
	    					if(callers.indexOf(touchPointRT.from.type.concat(touchPointRT.from.qname))==-1)
	    						callers.push(touchPointRT.from.type.concat(touchPointRT.from.qname));
	    					if(callees.indexOf(touchPointRT.to.type.concat(touchPointRT.to.qname))==-1)
	    						callees.push(touchPointRT.to.type.concat(touchPointRT.to.qname));
	    					
		    				touchPointRT.traced=traced;
		    				touchPointRT.potential=reach;
		    				touchPointsReachTrc.push(touchPointRT);
		    			}
		    		}
		    		
		    		var callsModel = new sap.ui.model.json.JSONModel();
		    		callsModel.setData(touchPointsReachTrc);
		    		archiveDetailPage.setModel(callsModel, "archiveTouchPoints");
		    		
		    		callersCount.setText("Distinct callers: " + callers.length);
		    		calleesCount.setText("Distinct callees: " +  callees.length);
		    		callsCount.setText("Calls: " + touchPointsReachTrc.length);
		    	}
		    	
		    	
		    	
		    	//get vuln metric for all versions of the same group artifact
		    	libId = oArchiveDetailsModel.getObject("/lib/libraryId");
		    	if(libId!=null){
		    		
					var vUrl = model.Config.getArchiveVulnServiceUrl(libId.group,libId.artifact,libId.version,true,false);
					model.Config.addToQueue(oArchiveVuln);
					model.Config.loadData(oArchiveVuln,vUrl,'GET');
						
					
					oArchiveVuln.attachRequestCompleted(function() {
						model.Config.remFromQueue(oArchiveVuln);
						archiveDetailPageController.populateMetricTable(oArchiveVuln);
			    		//archiveDetailPageController.toggleNonVuln();
			    						
					});
		    	}
			});
			
		}
	},
	
//	toggleNonVuln : function(){
//		var oConstructView = this.getView().byId("idArchiveMetrics");
//		var column = this.getView().byId("id-vuln-count");
//		if(column.getFiltered()==false){
//			column.setFilterType("sap.ui.model.type.Integer");
//		//	column.setFilterProperty("metricModel>countVuln");
//			oConstructView.filter(column,"0");
//			column.setFiltered(true);
//			
//		}else{
//			oConstructView.filter(column,"");
//			column.setFiltered(false);
//		}
//		
//	},
	
	openMaven : function(oEvent) {
	    var archiveDetailPage = this.getView().byId('archiveDetailPage');
	    var data = archiveDetailPage.getModel().getData();
	    var url = "http://search.maven.org/#search|gav|1|g%3A%22" + data.lib.libraryId.group + "%22%20AND%20a%3A%22" + data.lib.libraryId.artifact + "%22";
	    this.openLink(url, 'maven');
	},
	
	
	openMavenElement:function(oEvent) {
		var archiveDetailPage = this.getView().byId('archiveDetailPage');
	    var data = archiveDetailPage.getModel().getData();
		var url = "http://search.maven.org/#artifactdetails%7C" + data.lib.libraryId.group + "%7C" + data.lib.libraryId.artifact + "%7C" + data.lib.libraryId.version + "%7Cjar";
		this.openLink(url, 'maven');
	},
	
	
	openLink : function(_url, _window) {
	    window.open(_url, _window).focus()
	},


	loadMetricsAsynch : function() {

		var metricModel = this.getView().byId("idArchiveMetrics").getModel('metricModel');
		var archiveMetricTableNew = this.getView().byId("idArchiveMetrics");
		var archiveDetailPageController = this.getView("view.ArchiveDetail").getController();
	
			
		for(var o=0; o<archiveMetricTableNew.getRows().length;o++){
				 
			//var r = archiveMetricTableNew.getRows();
			var rowIndex=o;
// code snippet : how to manually fire cellClick event
//			var newC=new sap.ui.model.Context(metricModel, "/"+rowIndex);
//			archiveMetricTableNew.fireCellClick({cellControl : archiveMetricTableNew.getRows()[o].getCells()[0],
//			cellDomRef : archiveMetricTableNew.getRows()[o].getCells()[0].getDomRef(),
//			rowIndex :0,
//			columnIndex : 0,
//			columnId : "id-lib",
//			rowBindingContext : newC
//			});
//		}
			
			
			var check = metricModel.getProperty("/"+rowIndex+"/calleeDelCount");
			if(check!="-"){
		
		
			var archive = metricModel.getProperty("/"+rowIndex+"/archive");
			
			var toCompare={};
			toCompare.group=archive.split(":")[0];
			toCompare.artifact=archive.split(":")[1];
			toCompare.version=archive.split(":")[2];
			
		
			var r = $.ajax({
		        type: "POST",
		        url: model.Config.getUpdateMetricsUrl(groupId,artifactId,version,archiveId),
		        data : JSON.stringify(toCompare),
		        headers : {'content-type': "application/json",'cache-control': "no-cache", 'X-Vulas-Space': model.Config.getSpace(), 'X-Vulas-Echo': o,'X-Vulas-Version':model.Version.version,'X-Vulas-Component':'appfrontend' },
		        success: function(data, status, jqXHR){
		        	ajaxQueue1.pop(r);
		        	var ratios = data.metrics.ratios;
		        	var echo = jqXHR.getResponseHeader("X-Vulas-Echo");
		      //  	var archive = data.toLibraryId.group.concat(":").concat(data.toLibraryId.artifact).concat(":").concat(data.toLibraryId.version);
		        	var metricModel = this.getView().byId("idArchiveMetrics").getModel("metricModel");
		        //	var oJson = JSON.parse(metricModel.getJSON());
//		        	var rowIndex;
//		        	for (var j=0;j<oJson.length;j++){
//		        		if(oJson[j].archive==archive){
//		        			rowIndex = j;
//		        			break;
//		        		}
//		        	}
//		        	
//		        	if (rowIndex!=undefined){
		        	rowIndex=echo;
		        	for(var r=0;r<ratios.length;r++){
		        		if(ratios[r].name == 'callee_stability'){
		        			metricModel.setProperty("/"+rowIndex+"/calleeDelCount", ratios[r].count);
		        			metricModel.setProperty("/"+rowIndex+"/calleeDelTotal", ratios[r].total);
		        			metricModel.setProperty("/"+rowIndex+"/calleeDelRatio", (ratios[r].ratio==-1.0)?"-":Math.round(ratios[r].ratio * 100) );
		        		}
		        		else if(ratios[r].name == 'calls_to_modify'){
		        			metricModel.setProperty("/"+rowIndex+"/callsToModifyCount",ratios[r].count);
		        			metricModel.setProperty("/"+rowIndex+"/callsToModifyTotal",ratios[r].total);
		        			metricModel.setProperty("/"+rowIndex+"/callsToModifyRatio",(ratios[r].ratio==-1.0)?"-":Math.round(ratios[r].ratio * 100) );
		        		}
		        		else if(ratios[r].name == 'reachable_body_stability'){
		        			metricModel.setProperty("/"+rowIndex+"/reachableBodyChangedCount",ratios[r].count);
		        			metricModel.setProperty("/"+rowIndex+"/reachableBodyChangedTotal", ratios[r].total);
		        			metricModel.setProperty("/"+rowIndex+"/reachableBodyChangedRatio",(ratios[r].ratio==-1.0)?"-":Math.round(ratios[r].ratio * 100) );
		        		}
		        		else if(ratios[r].name == 'jar_constructs_body_stability'){
		        			metricModel.setProperty("/"+rowIndex+"/bodyStabilityCount", ratios[r].count);
		        			metricModel.setProperty("/"+rowIndex+"/bodyStabilityTotal", ratios[r].total);
		        			metricModel.setProperty("/"+rowIndex+"/bodyStabilityRatio", (ratios[r].ratio==-1.0)?"-":Math.round(ratios[r].ratio * 100) );
		        		}
		        	}
		        	metricModel.refresh();
		        	//}
		        	
		        }.bind(this), 
		        error: function(jqXHR){
		        	ajaxQueue1.pop(r);
		        	var echo = jqXHR.getResponseHeader("X-Vulas-Echo");
		      //  	var archive = data.toLibraryId.group.concat(":").concat(data.toLibraryId.artifact).concat(":").concat(data.toLibraryId.version);
		        	var metricModel = this.getView().byId("idArchiveMetrics").getModel("metricModel");
		        //	var oJson = JSON.parse(metricModel.getJSON());
//		        	var rowIndex;
//		        	for (var j=0;j<oJson.length;j++){
//		        		if(oJson[j].archive==archive){
//		        			rowIndex = j;
//		        			break;
//		        		}
//		        	}
//		        	
//		        	if (rowIndex!=undefined){
		        	rowIndex=echo;
		        	
        			metricModel.setProperty("/"+rowIndex+"/calleeDelCount", "n/a");
        			metricModel.setProperty("/"+rowIndex+"/calleeDelTotal", "n/a");
        			metricModel.setProperty("/"+rowIndex+"/calleeDelRatio", "n/a");
    				metricModel.setProperty("/"+rowIndex+"/callsToModifyCount","n/a");
        			metricModel.setProperty("/"+rowIndex+"/callsToModifyTotal","n/a");
        			metricModel.setProperty("/"+rowIndex+"/callsToModifyRatio","n/a" );
        			metricModel.setProperty("/"+rowIndex+"/reachableBodyChangedCount","n/a");
        			metricModel.setProperty("/"+rowIndex+"/reachableBodyChangedTotal", "n/a");
        			metricModel.setProperty("/"+rowIndex+"/reachableBodyChangedRatio","n/a");
        			metricModel.setProperty("/"+rowIndex+"/bodyStabilityCount", "n/a");
        			metricModel.setProperty("/"+rowIndex+"/bodyStabilityTotal", "n/a");
        			metricModel.setProperty("/"+rowIndex+"/bodyStabilityRatio", "n/a");
	
		        	
		        	metricModel.refresh();
		        	//}
		        	
		        }.bind(this), 
		        
			});	
			ajaxQueue1.push(r);
		}
		}
	},

	openWiki : function(evt){
		
		model.Config.openWiki("user/manuals/frontend/#dependencies-details");
		
	},
	
	onLibItemTap : function(oEvent) {
		var check = oEvent.getParameters().rowBindingContext.getObject("calleeDelCount");
			if(check!="-"){
		//	var archiveid = oEvent.getParameters().rowBindingContext.getObject("dep/lib/sha1");
		//	console.log(oEvent.getParameters().rowBindingContext.getObject());
		//	console.log("model:" + oEvent.getParameters().rowBindingContext);
		
		//	var oModel = oEvent.getSource().getBindingContext("metricModel");
		//	var table = oEvent.getSource();
	//		var oJson = JSON.parse(oEvent.getParameters().rowBindingContext.getModel().getJSON());
			var oModel = oEvent.getParameters().rowBindingContext.getModel();
		//	console.log(oEvent.getParameters().rowBindingContext.getModel().getJSON());
			//console.log(oEvent);
			var libId = oEvent.getParameters().rowBindingContext.getObject("archive");
	//		var toCompare={};
	//		toCompare.group=archive.split(":")[0];
	//		toCompare.artifact=archive.split(":")[1];
	//		toCompare.version=archive.split(":")[2];
			
						
		//	archiveMetrics.archive=archive;
		//	archiveMetrics.countVuln = oEvent.getParameters().rowBindingContext.getObject("countVuln");
	
			this.router.navTo("updateDetail", {
				group : groupId,
				artifact : artifactId,
				version : version,
				archiveid : archiveId,
				libId : libId
			});
		}
		
		// Remove selection so that you can click on the same item again
		//oEvent.getSource().removeSelections();
	},
	
	toggleAllVersion : function(){
		var archiveMetricTableVulnCountColumn = this.getView().byId("id-countVulnXX");
		var archiveDetailPageController = this.getView("view.ArchiveDetail").getController();
		var oArchiveVuln = new sap.ui.model.json.JSONModel();
		if(libId!=null){
			var vUrl = null;
			if(archiveMetricTableVulnCountColumn.getFiltered()==true){
				vUrl = model.Config.getArchiveVulnServiceUrl(libId.group,libId.artifact,null,false,false);
				archiveMetricTableVulnCountColumn.setFiltered(false);
			}
			else if(archiveMetricTableVulnCountColumn.getFiltered()==false){
				vUrl = model.Config.getArchiveVulnServiceUrl(libId.group,libId.artifact,libId.version,true,false);
				archiveMetricTableVulnCountColumn.setFiltered(true);
			}
			model.Config.loadData(oArchiveVuln,vUrl,'GET');
				
			oArchiveVuln.attachRequestCompleted(function() {
				archiveDetailPageController.populateMetricTable(oArchiveVuln);
	    		//archiveDetailPageController.toggleNonVuln();
	    						
			});
		}
	},
	
	onExit: function(){
		var archiveDetailPage = this.getView().byId('archiveDetailPage');
		archiveDetailPage.getModel("archiveTouchPoints").destroy();
		archiveDetailPage.getModel("archiveConstructCounts").destroy();
		this.getView().byId("idArchiveMetrics").getModel('metricModel').destroy();
		
	},
	
	populateMetricTable : function(oArchiveVuln){
		var a = oArchiveVuln.getObject("/");
		var archiveDetailPageController = this.getView("view.ArchiveDetail").getController();
		var archiveMetricTable = this.getView().byId("idArchiveMetrics");
		var archiveMetrics = {};
		
		var archiveDetailsMetrics = [];
		for (var n= 0 ; n<a.length; n++){
			archiveMetrics = {};
			archiveMetrics.archive=a[n].group.concat(":").concat(a[n].artifact).concat(":").concat(a[n].version); 	
			//archiveMetrics.countVuln = "NN";
			
			archiveMetrics.calleeDelCount = (a[n].version==libId.version)?"-":"Loading";
			archiveMetrics.calleeDelTotal = (a[n].version==libId.version)?"-":"Loading";
			archiveMetrics.calleeDelRatio = (a[n].version==libId.version)?"-":"Loading";
			archiveMetrics.callsToModifyCount = (a[n].version==libId.version)?"-":"Loading";
			archiveMetrics.callsToModifyTotal = (a[n].version==libId.version)?"-":"Loading";
			archiveMetrics.callsToModifyRatio = (a[n].version==libId.version)?"-":"Loading";
			archiveMetrics.reachableBodyChangedCount = (a[n].version==libId.version)?"-":"Loading";
			archiveMetrics.reachableBodyChangedTotal = (a[n].version==libId.version)?"-":"Loading";
			archiveMetrics.reachableBodyChangedRatio = (a[n].version==libId.version)?"-":"Loading";
			archiveMetrics.bodyStabilityCount = (a[n].version==libId.version)?"-":"Loading";
    		archiveMetrics.bodyStabilityTotal = (a[n].version==libId.version)?"-":"Loading";
    		archiveMetrics.bodyStabilityRatio = (a[n].version==libId.version)?"-":"Loading";
    		
		//	archiveDetailsMetrics.push(archiveMetrics);
			
			//compute libIds vulns
			
			var count = 0;
			if(a[n].affLibraries!=null){
				for(var v=0; v<a[n].affLibraries.length;v++){
					if(a[n].affLibraries[v].affected){
						count++;
					}
				}
					
			}
			archiveMetrics.countVuln = count;
			archiveDetailsMetrics.push(archiveMetrics);
		}
		var metricModel = new sap.ui.model.json.JSONModel();
		archiveMetricTable.setVisibleRowCount(a.length);
		
		metricModel.setData(archiveDetailsMetrics);
		archiveMetricTable.setModel(metricModel, "metricModel");
		
		archiveMetricTable.rerender();
		archiveDetailPageController.loadMetricsAsynch();
	},
	
	handleNavBack : function() {
		window.history.go(-1);
	}
	
});
;