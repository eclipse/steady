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
sap.ui.controller("view.UpdateDetail", {

	onInit : function() {
		this.router = sap.ui.core.UIComponent.getRouterFor(this);
		this.router.attachRoutePatternMatched(this._handleRouteMatched, this);
	},

	_handleRouteMatched : function(evt) {
		if (evt.getParameter("name") !== "updateDetail") {
            return;
        }
		archiveId = evt.getParameter("arguments").archiveid;
		groupId = evt.getParameter("arguments").group;
		artifactId = evt.getParameter("arguments").artifact;
		version = evt.getParameter("arguments").version;
		libId = evt.getParameter("arguments").libId;
		
		var toCompare={};
		toCompare.group=libId.split(":")[0];
		toCompare.artifact=libId.split(":")[1];
		toCompare.version=libId.split(":")[2];
		
		var updateDetailPage = this.getView().byId('updateDetailPage');
		var updateDetailPagePageController = this.getView("view.updateDetail").getController();
	
		var callersCount = this.getView().byId("callersCount");
		var calleesCount = this.getView().byId("calleesCount");
		var callsCount = this.getView().byId("callsCount");

		//clear previous models
        var data =[];

		var oldTP = updateDetailPage.getModel();
		if(oldTP!=undefined){
			oldTP.setData(data,false);
			oldTP.refresh();
		}
	
		
		var oUpdateDetailsModel = new sap.ui.model.json.JSONModel();
		
		
		
		if (!model.Config.isMock) {
			this.getView().byId("callsToModifyCount").setText("Calls to modify: ");
			this.getView().byId("callersToModifyCount").setText("Distinct callers to modify: ");
			this.getView().byId("calleesDeletedCount").setText("Callees deleted: ");
//			this.getView().byId("callsCount").setText("Total calls: ");
//			this.getView().byId("callersCount").setText("Distinct callers: ");
//			this.getView().byId("calleesCount").setText("Distinct callees: ");
    		
			this.getView().byId('changeTable').setBusy(true);
			
			$.ajax({
		        type: "POST",
		        url: model.Config.getUpdateChangesUrl(groupId,artifactId,version,archiveId),
		        data : JSON.stringify(toCompare),
		        headers : {'content-type': "application/json",'cache-control': "no-cache" ,'X-Vulas-Version':model.Version.version,'X-Vulas-Component':'appfrontend','X-Vulas-Space': model.Config.getSpace(),'X-Vulas-Tenant': model.Config.getTenant()},
		        success: function(data){
		        	
        	       	var touchPointsReachTrc = [];
					var touchPointRT={};
					var processedTP = [];
		    		var touchPoints = data.callsToModify ;
		    		var callersToModify = [];
		    		var calleesDeleted = [];
		    		
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
		    					if(callersToModify.indexOf(touchPointRT.from.type.concat(touchPointRT.from.qname))==-1)
		    						callersToModify.push(touchPointRT.from.type.concat(touchPointRT.from.qname));
		    					if(calleesDeleted.indexOf(touchPointRT.to.type.concat(touchPointRT.to.qname))==-1)
		    						calleesDeleted.push(touchPointRT.to.type.concat(touchPointRT.to.qname));
		    					
			    				touchPointRT.traced=traced;
			    				touchPointRT.potential=reach;
			    				touchPointsReachTrc.push(touchPointRT);
			    			}
			    		
			    		}
			    		
			    		
			    		
			    		var callsModel = new sap.ui.model.json.JSONModel();
			    		callsModel.setData(touchPointsReachTrc);
			    		updateDetailPage.setModel(callsModel);
		        	
		        	
		        	
		        	
						this.getView().byId("callsToModifyCount").setText("Calls to modify: " + touchPointsReachTrc.length);
						this.getView().byId("callersToModifyCount").setText("Distinct callers to modify: " + callersToModify.length);
						this.getView().byId("calleesDeletedCount").setText("Callees deleted: " + calleesDeleted.length);


			    	}
			    	this.getView().byId('changeTable').setBusy(false);
		        	
		        }.bind(this),
		        error: function(){
		        	this.getView().byId('changeTable').setBusy(false);
		        }.bind(this)
			});
			}
	},
	
	openWiki : function(evt){
		
		model.Config.openWiki("user/manuals/frontend/#callers-to-be-modified");
		
	},
	
	handleNavBack : function() {
		window.history.go(-1);
	}
})
