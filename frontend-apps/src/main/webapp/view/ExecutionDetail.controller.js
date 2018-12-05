sap.ui.controller("view.ExecutionDetail", {

	onInit : function() {
		this.router = sap.ui.core.UIComponent.getRouterFor(this);
		this.router.attachRoutePatternMatched(this._handleRouteMatched, this);
	},

	_handleRouteMatched : function(evt) {
		
		if (evt.getParameter("name") !== "exeDetail") {
            return;
        }
		exeId = evt.getParameter("arguments").exeid;
		groupId = evt.getParameter("arguments").group;
		artifactId = evt.getParameter("arguments").artifact;
		version = evt.getParameter("arguments").version;
		var executionDetailsPage = this.getView().byId('executionDetailsPage');
		var oExecutionDetailsModel = new sap.ui.model.json.JSONModel();
		
		if (!model.Config.isMock) {
			sUrl = model.Config.getGoalExecutionDetailsServiceUrl(groupId,artifactId,version,exeId);
		//	sUrl = sUrl + "?exeid=" + exeId;
			model.Config.loadData (oExecutionDetailsModel,sUrl, 'GET');
		
			/*$.get(sUrl, function(data) {
				oExecutionDetailsModel.setData(data);
				executionDetailsPage.setModel(oExecutionDetailsModel);
			}.bind(this));*/
			
			oExecutionDetailsModel.attachRequestCompleted(function() {
				
				var execDetailsAsArrays = {};
				
				execDetailsAsArrays.goal=oExecutionDetailsModel.getObject("/goal")
				execDetailsAsArrays.startedAtClient=oExecutionDetailsModel.getObject("/startedAtClient")
				execDetailsAsArrays.executionId=oExecutionDetailsModel.getObject("/executionId")
				execDetailsAsArrays.exception=oExecutionDetailsModel.getObject("/exception")
				execDetailsAsArrays.configuration=oExecutionDetailsModel.getObject("/configuration");
				execDetailsAsArrays.systemInfo=oExecutionDetailsModel.getObject("/systemInfo");
				
				var jsonStats = oExecutionDetailsModel.getObject("/statistics");
				
				var stats = [];
				var singleStats={};
				
				for(var k in Object.keys(jsonStats)){
					var statName = Object.keys(jsonStats)[k];
					singleStats={};
					singleStats.name=statName;
					singleStats.value=jsonStats[statName];
					
					stats.push(singleStats);
				}
				execDetailsAsArrays.statistics = stats
				
				var execDetailsAsArraysModel = new sap.ui.model.json.JSONModel();
				execDetailsAsArraysModel.setData(execDetailsAsArrays);
				executionDetailsPage.setModel(execDetailsAsArraysModel);
				
			});
			
				
			
			
		}
	},

	onExit : function() {
	},
	
	openWiki : function(evt){
		
		model.Config.openWiki("user/manuals/frontend/#history-details");
		
	},

	handleNavBack : function() {
		window.history.go(-1);
	}
});
