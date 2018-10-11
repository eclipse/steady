jQuery.sap.declare("vulasbugfrontend.Component");

sap.ui.core.UIComponent.extend("vulasbugfrontend.Component", {
    metadata: {
        routing: {
            config: {
                viewType: "XML",
                viewPath: "view",
                targetControl: "splitApp",
                clearTarget: false,
                transition: "slide"
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
                                        }  
                                    ]
                        }]
                }]
        }
    },
    init: function () {
        // 1. some very generic requires
        jQuery.sap.require("sap.m.routing.RouteMatchedHandler");
        jQuery.sap.require("sap.ui.core.routing.Router");
        jQuery.sap.require("model.Config");
        jQuery.sap.require("model.Formatter");
        jQuery.sap.require("model.Version");
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
//        var bugs_url = model.Config.getBugsAsUrl();
//        
//        //var apps_url = model.Config.getMyAppsServiceUrl();
//        if (!model.Config.isMock) {
//            console.log(bugs_url);
//            //oModel = new sap.ui.model.json.JSONModel(apps_url, true);
//            oModel = new sap.ui.model.json.JSONModel();
//            model.Config.loadData(oModel, bugs_url, 'GET');
//            //oModel.loadData(apps_url, null,true,'GET',false,false,{'Authorization':'Basic U1VfVlVMQVM6VnVsYXMxMjM0NQ=='});
//            //	'Access-Control-Allow-Origin':'*','Access-Control-Allow-Headers':'Origin, X-Requested-With, Content-Type, Accept'});
//
//            setInterval(function () {
//                //var currentUrl = model.Config.getMyAppsServiceUrl();
//                var currentUrl = model.Config.getBugsAsUrl();
//                console.log(currentUrl);
//                //oModel = new sap.ui.model.json.JSONModel(sUrl, true);
//                oModel = new sap.ui.model.json.JSONModel();
//                model.Config.loadData(oModel, currentUrl, 'GET');
//                //oModel.loadData(apps_url, null,true,'GET',false,false,{'Authorization':'Basic U1VfVlVMQVM6VnVsYXMxMjM0NQ=='});
//                //         'Access-Control-Allow-Origin':'*','Access-Control-Allow-Headers':'Origin, X-Requested-With, Content-Type, Accept'});
//                // oModel.refresh(true);
//                oView.setModel(oModel);
//                // oView.rerender();
//
//            }, 300000); // Update every 5 min
//
//        }
//
//        // set data model on root view
//        oView.setModel(oModel);
//        oView.attachAfterInit(
//                oModel.attachRequestCompleted(function () {
//                    // that.router = sap.ui.core.UIComponent.getRouterFor(this);
//                    sap.ui.getCore().getEventBus().publish("app", "DataLoaded");
//                    if (sap.ui.getCore().byId("busyIndicator")) {
//                        sap.ui.getCore().byId("busyIndicator").hide();
//                    }
//                }));
//
//        setInterval(function () {
//            sap.ui.getCore().getEventBus().publish("app", "DataLoaded");
//
//            if (sap.ui.getCore().byId("busyIndicator")) {
//                sap.ui.getCore().byId("busyIndicator").hide();
//            }
//
//        }, 300000);

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
     //   oModel.setDefaultBindingMode("OneWay");
        oView.setModel(oModel, "device");

        // Internationalization
        var i18nModel = new sap.ui.model.resource.ResourceModel({
            bundleUrl: "i18n/messageBundle.properties"
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
    destroy : function () {

        if (this.routeHandler) {
            this.routeHandler.destroy();
        }

        // call overridden destroy
        sap.ui.core.UIComponent.prototype.destroy.apply(this, arguments);
    }

});
