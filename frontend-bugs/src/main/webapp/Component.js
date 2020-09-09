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
jQuery.sap.declare("vulasbugfrontend.Component");

sap.ui.core.UIComponent.extend("vulasbugfrontend.Component", {
    metadata: {
        routing: {
            config: {
                routerClass: vulasbugfrontend.MyRouter,
                viewType: "XML",
                viewPath: "view",
                targetControl: "splitApp",
                clearTarget: false,
                transition: "flip"
            },
            routes: [{
                    pattern: "",
                    name: "master",
                    viewPath: "view",
                    view: "Master",
                    viewLevel: 0,
                    targetAggregation: "masterPages",
                    subroutes: [{
                            pattern: "bugs/{bugId}",
                            name: "component",
                            view: "Component",
                            viewPath: "view",
                            viewLevel: 1,
                            targetAggregation: "detailPages",
                            subroutes : [ {
                                            pattern : "bugs/{bugId}/libs/{sha1}",
                                            name : "checkversionDetail",
                                            view : "CheckversionDetail",
                                            viewPath : "view",
                                            viewLevel : 2,
                                            targetAggregation : "detailPages"
                                        }, 
                                        {
                                            pattern : "bugs/{bugId}/libdetails/{sha1}",
                                            name : "libDetail",
                                            view : "LibDetail",
                                            viewPath : "view",
                                            viewLevel : 2,
                                            targetAggregation : "detailPages"
                                        }, 
                                        {
                                            pattern : "bugs/{bugId}/libs/{sha1}/{repo}/{qname}/overallChange/showTree",
                                            name : "ASTViewer",
                                            view : "ASTViewer",
                                            viewPath : "view",
                                            viewLevel : 2,
                                            targetAggregation : "detailPages"
                                        }, 
                                        {
                                            pattern : "bugs/{bugId}/{repo}/{qname}/overallChange/showTree",
                                            name : "ASTViewerNew",
                                            view : "ASTViewerNew",
                                            viewPath : "view",
                                            viewLevel : 2,
                                            targetAggregation : "detailPages"
                                        },
                                        {
                                            pattern : "bugs/{bugId}/{group}/{artifact}/{version}/{source}",
                                            name : "BugDetailPatchEval",
                                            view : "BugDetailPatchEval",
                                            viewPath : "view",
                                            viewLevel : 2,
                                            targetAggregation : "detailPages"
                                        }                                       ,
                                        
                                        ]
                        }]
                }, ]
        }
    },
    init: function () {
        // 1. some very generic requires
        jQuery.sap.require("sap.m.routing.RouteMatchedHandler");
        jQuery.sap.require("sap.ui.core.routing.Router");
        jQuery.sap.require("model.Config");
        jQuery.sap.require("model.Formatter");
        jQuery.sap.require("model.Utils");
        jQuery.sap.require("model.TreeUtils");
        jQuery.sap.includeStyleSheet("css/style.css");
        // 2. call overwritten init (calls createContent)
        sap.ui.core.UIComponent.prototype.init.apply(this, arguments);

        // 3a. monkey patch the router
        var router = this.getRouter();

        // 5. initialize the router
        this.routeHandler = new sap.m.routing.RouteMatchedHandler(router);
        router.initialize();
    },
    destroy: function () {
        // call overwritten destroy
        sap.ui.core.UIComponent.prototype.destroy.apply(this, arguments);
    },
    createContent: function () {

        // create root view
        var oView = sap.ui.view({
            id: "app",
            viewName: "vulasbugfrontend.view.App",
            type: "JS",
            viewData: {
                component: this
            }
        });

        var oModel;
        if (!model.Config.isMock) {
            var sUrl = model.Config.getBugsAsUrl();
            console.log(sUrl);
            //var sUrl = model.Config.getMyAppsServiceUrl();
            //	oModel = new sap.ui.model.json.JSONModel(sUrl, true);
            oModel = new sap.ui.model.json.JSONModel();
            model.Config.loadData(oModel, sUrl, 'GET');
            //oModel.loadData(sUrl, null,true,'GET',false,false,{'Authorization':'Basic U1VfVlVMQVM6VnVsYXMxMjM0NQ=='});
            //'Access-Control-Allow-Origin':'*','Access-Control-Allow-Headers':'Origin, X-Requested-With, Content-Type, Accept'
            //    'Content-Type':'application/json'
            //});

            setInterval(function () {
                var currentUrl = model.Config.getBugsAsUrl();
                console.log(currentUrl);
                //var sUrl = model.Config.getMyAppsServiceUrl();
                oModel = new sap.ui.model.json.JSONModel();
                model.Config.loadData(oModel, currentUrl, 'GET');
                //		oModel.loadData(sUrl, null,true,'GET',false,false,{'Authorization':'Basic U1VfVlVMQVM6VnVsYXMxMjM0NQ=='});
                //'Access-Control-Allow-Origin':'*','Access-Control-Allow-Headers':'Origin, X-Requested-With, Content-Type, Accept'});
                // oModel.refresh(true);
                oView.setModel(oModel);
                // oView.rerender();

            }, 300000); // Update every 5 min

        }

        // set data model on root view
        oView.setModel(oModel);
        oView.attachAfterInit(
                oModel.attachRequestCompleted(function () {
                    // that.router = sap.ui.core.UIComponent.getRouterFor(this);
                    sap.ui.getCore().getEventBus().publish("app", "DataLoaded");
                }));

        setInterval(function () {
            sap.ui.getCore().getEventBus().publish("app", "DataLoaded");

            if (sap.ui.getCore().byId("busyIndicator")) {
                sap.ui.getCore().byId("busyIndicator").hide();
            }

        }, 300000);

        // set device model
        oModel = new sap.ui.model.json.JSONModel(
                {
                    isTouch: sap.ui.Device.support.touch,
                    isNoTouch: !sap.ui.Device.support.touch,
                    isPhone: jQuery.device.is.phone,
                    isNoPhone: !jQuery.device.is.phone,
                    listMode: (jQuery.device.is.phone) ? "None"
                            : "SingleSelectMaster",
                    listItemType: (jQuery.device.is.phone) ? "Active"
                            : "Inactive"
                });
        oModel.setDefaultBindingMode("OneWay");
        oView.setModel(oModel, "device");

        var i18nModel = new sap.ui.model.resource.ResourceModel({
            bundleUrl: "i18n/messageBundle.properties"
        });
        oView.setModel(i18nModel, "i18n");
        // done
        return oView;
    },
    destroy : function () {

        if (this.routeHandler) {
            this.routeHandler.destroy();
        }

        // call overridden destroy
        sap.ui.core.UIComponent.prototype.destroy.apply(this, arguments);
    }

});
