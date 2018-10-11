sap.ui.controller("sap.psr.patcha.web.view.Second", {

	onInit : function() {
        
        /*var view = this.getView();

        // Data is fetched here
        jQuery.ajax("Data.json", { // load the data from a relative URL (the Data.json file in the same directory)
            dataType: "json",
            success: function(data){
                var oModel = new sap.ui.model.json.JSONModel(data);
                view.setModel(oModel);
            }
        });


        // remember the App Control
        this.app = view.byId("theApp");*/

        // Register for the event "RevisionsLoaded"
        var bus = sap.ui.getCore().getEventBus();
        bus.subscribe("app", "RevisionsLoaded", this.revisionsLoaded, this);
    },

	searchCommitLog: function(oEvent) {
        var app = sap.ui.getCore().byId('idViewApp');
        var ctrl = app.getController();
        var view1 = app.byId('idViewFirst');

        // Get input from the various UI fields
		var thisView = this.getView();
		var repoURL = encodeURIComponent(thisView.byId("repoURLTextField").getValue());
		var searchString = encodeURIComponent(thisView.byId("searchStringTextField").getValue());
        var bugId = encodeURIComponent(view1.byId('bugIdTextField').getValue());
        //var asOf = encodeURIComponent(thisView.byId('searchDateDatePicker').getYyyymmdd());

        // Build the 1st part of the query string
        var queryString = "/patchaWeb/pa?a=search" +
                                        "&u=" + repoURL +
                                        "&s=" + searchString +
                                        "&b=" + bugId;
                                        //"&d=" + asOf;

        // Append params for revision(s) (if any, when multiple separated by comma)
        var revisionsString = thisView.byId("revisionsTextField").getValue();
        var revisionsArray = revisionsString.split(",");
        var l = revisionsArray.length;
        var rev = null;
        for (var i = 0; i < l; i++) {
            rev = revisionsArray[i].trim();
            queryString += "&r=" + encodeURIComponent(rev);
        }

        // In debug mode, take data from local file
        if(ctrl.debugEnabled()) {
            queryString = "./model/revisions.json";
        }
        
        // Show busy dialog
        if (! this._dialog) {
            this._dialog = sap.ui.xmlfragment("sap.psr.patcha.web.view.BusyDialogPatchaBackend", this);
            this.getView().addDependent(this._dialog);
        }
        this._dialog.open();
        var local_dialog = this._dialog;

        // Search for revisions
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
				app.setModel(oModel, "revisions");
                
                // Publish event to indicate successful data load
				sap.ui.getCore().getEventBus().publish("app", "RevisionsLoaded");
			})
			.fail(function(jqXHR, textStatus, errorThrown) {
				local_dialog.close();
				alert(errorThrown + ": " + jqXHR.responseText);
			});
    },

    // Navigate to the next page (triggered after the revisions data is loaded)
    revisionsLoaded: function(oEvent) {
    	// Remove busy dialog
    	this._dialog.close();
    	// Go to next page
    	this.navigation.navTo("idViewApp--idViewThird");
    },

    // Show/hide some form elements
    selectMode: function(oEvent) {
        var thisView = this.getView();
        thisView.byId("searchStringTextField").setVisible(!thisView.byId("searchStringTextField").getVisible());
        thisView.byId("searchDateDatePicker").setVisible(!thisView.byId("searchDateDatePicker").getVisible());
        thisView.byId("revisionsTextField").setVisible(!thisView.byId("revisionsTextField").getVisible());
    },

    handleNavButtonPress: function(oEvent) {
        this.navigation.navBack();
    },
    
    enterSettings: function(oEvent) {
    	this.navigation.navTo("idViewApp--idViewSettings");
    }
});