jQuery.sap.declare("sap.psr.patcha.web.Component");

sap.ui.core.UIComponent.extend("sap.psr.patcha.web.Component", {

    createContent : function() {

        // create root view
        var oView = sap.ui.view({
            id: "idViewApp",
            viewName: "sap.psr.patcha.web.view.App",
            type: "XML",
            viewData: { component : this }
        });

        //oView.setModel(new sap.ui.model.json.JSONModel("model/revisions.json"));
        //oView.setModel(new sap.ui.model.json.JSONModel("model/search.json"), "search");
        
        // Vulnerability model (to be filled after entering the bug id in the 1st screen)
        var bugModel = new sap.ui.model.json.JSONModel ({
            bugid  : "CVE-2014-0050",
            desc   : "",
            pub    : "",
            cvss   : "",
            cwe    : "",
            cweUrl : "",
            nvdUrl : "",
            edbUrl : ""
        });
        oView.setModel(bugModel, "bug");
        
        // Search form model
        var searchModel = new sap.ui.model.json.JSONModel ({
            svnHost : "",
            svnUrl : "",
            searchString : "",
            asOf : "",
            revisions : ""
        });
        oView.setModel(searchModel, "search");
        
        // Settings
        var settingsModel = new sap.ui.model.json.JSONModel ({
            uploadURL : "",
            uploadUser : "",
            uploadUserPwd : "",
            debMode   : false,
            delTemp   : true
        });
        oView.setModel(settingsModel, "settings");

        // Set i18n model
        var i18nModel = new sap.ui.model.resource.ResourceModel({
            bundleUrl : "i18n/messageBundle.properties"
        });
        oView.setModel(i18nModel, "i18n");

        // Done
        return oView;
    }
});
