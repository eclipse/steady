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
jQuery.sap.declare("vulasfrontend.Component");

sap.ui.core.UIComponent.extend("vulasfrontend.Component", {

	metadata : {
		routing : {
			config : {
				viewType : "XML",
				viewPath : "view",
				targetControl : "splitApp",
				clearTarget : false,
				transition : "slide"
			},
			routes : [ {
				pattern : ":workspaceSlug:",
				name : "master",
				viewPath : "view",
				view : "Master",
				viewLevel : 0,
				targetAggregation : "masterPages",
				subroutes : [ {
					pattern : "{workspaceSlug}/{group}/{artifact}/{version}",
					name : "component",
					view : "Component",
					viewPath : "view",
					viewLevel : 1,
					targetAggregation : "detailPages",
					subroutes : [ {
						pattern : "{workspaceSlug}/{group}/{artifact}/{version}/usedBug/{bugid}/{archiveid}",
						name : "bugDetail",
						view : "BugDetail",
						viewPath : "view",
						viewLevel : 2,
						targetAggregation : "detailPages",
						subroutes : [ {
							pattern : "{workspaceSlug}/{group}/{artifact}/{version}/usedBug/{bugid}/{archiveid}/{change}",
							name : "graphDetail",
							view : "GraphDetail",
							viewPath : "view",
							viewLevel : 3,
							targetAggregation : "detailPages"
						}]
					},{
						pattern : "{workspaceSlug}/{group}/{artifact}/{version}/archive/{archiveid}",
						name : "archiveDetail",
						view : "ArchiveDetail",
						viewPath : "view",
						viewLevel : 2,
						targetAggregation : "detailPages",
						subroutes : [ {
							pattern : "{workspaceSlug}/{group}/{artifact}/{version}/archive/{archiveid}/{libId}",
							name : "updateDetail",
							view : "UpdateDetail",
							viewPath : "view",
							viewLevel : 3,
							targetAggregation : "detailPages"
						}]
					},{
						pattern : "{workspaceSlug}/{group}/{artifact}/{version}/executions/{exeid}",
						name : "exeDetail",
						view : "ExecutionDetail",
						viewPath : "view",
						viewLevel : 2,
						targetAggregation : "detailPages"
					}]
				} ]
			} ]
		}
	},

	init : function() {
		// 1. some very generic requires
		jQuery.sap.require("sap.m.routing.RouteMatchedHandler");
		jQuery.sap.require("sap.ui.core.routing.Router");
		jQuery.sap.require("model.Version");
		jQuery.sap.require("model.Config");
		jQuery.sap.require("model.Formatter");
		
		// 2. call overwritten init (calls createContent)
		sap.ui.core.UIComponent.prototype.init.apply(this, arguments);

		// 3a. monkey patch the router
		var router = this.getRouter();

		// 5. initialize the router
		this.routeHandler = new sap.m.routing.RouteMatchedHandler(router);
		router.initialize();
	},

	destroy : function() {
		// call overwritten destroy
		sap.ui.core.UIComponent.prototype.destroy.apply(this, arguments);
	},

	createContent : function() {

		// create root view
		var oView = sap.ui.view({
			id : "app",
			viewName : "vulasfrontend.view.App",
			type : "JS",
			viewData : {
				component : this
			}
		});
		
		var oModel ;
		
//		var sUrl = model.Config.getMyAppsServiceUrl();
//		//	oModel = new sap.ui.model.json.JSONModel(sUrl, true);
//			oModel = new sap.ui.model.json.JSONModel();
//			model.Config.loadData (oModel,sUrl, 'GET');
//			console.log(oModel.getJSON());
//	
////		if (!model.Config.isMock) {
////
////			setInterval(function() {
////				var currentUrl = model.Config.getMyAppsServiceUrl();
////				//oModel = new sap.ui.model.json.JSONModel(sUrl, true);
////				
////				var list = this.getView().byId("idListApplications");
////				list.setBusy(true);
////							
////				oModel = new sap.ui.model.json.JSONModel();
////				model.Config.loadData(oModel,currentUrl, 'GET');
////				
////				sap.ui.getCore().byId("idListApplications").setModel(oModel,false);
////				sap.ui.getCore().byId("idListApplications").getModel().refresh();
////				// oView.rerender();
////				list.setBusy(false);
////				
////			}, 300000); // Update every 5 min
////
////		}
//
//		oView.setModel(oModel);

		// set device model
		oModel = new sap.ui.model.json.JSONModel(
				{
					isTouch : sap.ui.Device.support.touch,
					isNoTouch : !sap.ui.Device.support.touch,
					isPhone : jQuery.device.is.phone,
					isNoPhone : !jQuery.device.is.phone,
					listMode : (jQuery.device.is.phone) ? "None"
							: "SingleSelectMaster",
					listItemType : (jQuery.device.is.phone) ? "Active"
							: "Inactive"
				});
	//	oModel.setDefaultBindingMode("OneWay");
		oView.setModel(oModel, "device");

        // Internationalization
		var i18nModel = new sap.ui.model.resource.ResourceModel({
			bundleUrl : "i18n/messageBundle.properties"
		});
		oView.setModel(i18nModel, "i18n");
		
		versionModel = new sap.ui.model.json.JSONModel(model.Version);
		oView.setModel(versionModel, "version");
		
		// User information
	/*	var userModel;
		var user_url = model.Config.getUserServiceUrl();
		userModel = new sap.ui.model.json.JSONModel(user_url, true);
		userModel.setDefaultBindingMode("OneWay");
        oView.setModel(userModel, "user");*/
        	
		// done
		return oView;
	},

	destroy : function() {

		if (this.routeHandler) {
			this.routeHandler.destroy();
		}

		// call overridden destroy
		sap.ui.core.UIComponent.prototype.destroy.apply(this, arguments);
	}

});
