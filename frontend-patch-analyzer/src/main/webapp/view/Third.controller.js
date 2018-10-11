sap.ui.controller("sap.psr.patcha.web.view.Third", {

    onInit : function() {
        var bus = sap.ui.getCore().getEventBus();
        bus.subscribe("app", "ChangesLoaded", this.changesLoaded, this);
    },

    handleNavButtonPress : function(oEvent) {
        this.navigation.navBack();
    },
    
    getSelectedRevisions : function() {
    	var sel_revs = new Array();
    	var thisView = this.getView();
    	var selected_commits = thisView.byId("revisionsTable").getSelectedItems();
    	var l = selected_commits.length;
    	
        // Change the query string for every selected revision
        var bind_ctx  = null;
        var bind_path = null;
        var revid = null;
        var model = sap.ui.getCore().byId('idViewApp').getModel('revisions');
        
        for (var i = 0; i < l; i++) {
            bind_ctx = selected_commits[i].getBindingContext("revisions");
            revid = model.getProperty(bind_ctx.sPath).id;
            if(revid!=null) {
            	sel_revs.push(revid);
            }
        }
        
    	return sel_revs;
    },

    startAnalysis : function(oEvent) {
        var app = sap.ui.getCore().byId('idViewApp');
        var ctrl = app.getController();
        var view1 = app.byId('idViewFirst');
        var view2 = app.byId('idViewSecond');

        // Get input from the various UI fields
        var thisView = this.getView();
        var repoURL = encodeURIComponent(view2.byId('repoURLTextField').getValue());
        var bugId = encodeURIComponent(view1.byId('bugIdTextField').getValue());

        // Build the 1st part of the query string
        var queryString = "/patchaWeb/pa?a=identify" +
                                        "&u=" + repoURL +
                                        "&b=" + bugId +
                                        "&u=" + encodeURIComponent(ctrl.getUploadURL());
        
        var sel_revs = this.getSelectedRevisions();
        var l = sel_revs.length;
        if(l==0) { return; }
	    for (var i = 0; i < l; i++) {
	    	queryString += "&r=" + encodeURIComponent(sel_revs[i]);
	    }

//    	// Append the IDs of selected revisions (for which the analysis will be done)
//        var selected_commits = thisView.byId("revisionsTable").getSelectedItems();
//        var l = selected_commits.length;
//        
//        // Return if no revision was selected
//        if(l==0) {
//        	return;
//        }
//        
//        // Change the query string for every selected revision
//        var bind_ctx  = null;
//        var bind_path = null;
//        var revid = null;
//        var model = sap.ui.getCore().byId('idViewApp').getModel('revisions');
//        for (var i = 0; i < l; i++) {
//            bind_ctx = selected_commits[i].getBindingContext("revisions");
//            revid = model.getProperty(bind_ctx.sPath).id;
//            if(revid!=null) {
//            	queryString += "&r=" + encodeURIComponent(revid);
//            }
//        }

        // In debug mode, take data from local file
        if(ctrl.debugEnabled()) {
            queryString = "./model/changes.json";
        }
        
        // Show busy dialog
        if (! this._dialog) {
            this._dialog = sap.ui.xmlfragment("sap.psr.patcha.web.view.BusyDialogPatchaBackend", this);
            this.getView().addDependent(this._dialog);
        }
        this._dialog.open();
        var local_dialog = this._dialog;

        // Identify changes for selected revisions
        var jqxhr = jQuery.ajax( {
                type : "GET",
                contentType : "multipart/form-data",
                url : queryString,
                //cache : false,
                //timeout : 300000,
                //dataType : "json"
            })
            .done(function(data, textStatus, jqXHR) {

                // Update model with new data
                var oModel = new sap.ui.model.json.JSONModel();
                oModel.setData(data);
                app.setModel(oModel, "changes");

                // Publish event to indicate successful data load
                sap.ui.getCore().getEventBus().publish("app", "ChangesLoaded");
            })
            .fail(function(jqXHR, textStatus, errorThrown) {
				local_dialog.close();
				alert(errorThrown + ": " + jqXHR.responseText);
            });
    },

    // Navigate to the next page (triggered after the revisions data is loaded)
    changesLoaded: function(oEvent) {
    	
//        var app = sap.ui.getCore().byId('idViewApp');
//        var view4 = app.byId('idViewFourth');
//        var oTable = view4.byId("changeTable");
//        var thisView = this.getView();
//        var oList = thisView.byId("idListRevisions");
//        //Create a model and bind the table rows to this model
//        var oModel = app.getModel("changes");
//        //oTable.setModel(oModel);
//        //oTable.bindRows("/constructChanges");
        
        // Close busy dialog
        this._dialog.close();
        // Go to next page
        this.navigation.navTo("idViewApp--idViewFourth");
    },
    
    enterSettings: function(oEvent) {
    	this.navigation.navTo("idViewApp--idViewSettings");
    }
});