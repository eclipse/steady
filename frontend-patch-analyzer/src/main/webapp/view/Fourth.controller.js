sap.ui.controller("sap.psr.patcha.web.view.Fourth", {

    onInit : function() {
        var bus = sap.ui.getCore().getEventBus();
        bus.subscribe("app", "ChangesUploaded", this.changesUploaded, this);
    },
    
    onSearch : function (oEvt) {
    	
        // add filter for search
        var aFilters = [];
        var sQuery = oEvt.getSource().getValue();
        if (sQuery && sQuery.length > 0) {
          var filter = new sap.ui.model.Filter("construct/qname", sap.ui.model.FilterOperator.Contains, sQuery);
          aFilters.push(filter);
        }

        var thisView = this.getView();
        var oTable = thisView.byId("changeTable");
        var binding = oTable.getBinding("items");
        binding.filter(aFilters, "Application");
    },
    
    handleNavButtonPress: function(oEvent) {
        this.navigation.navBack();
    },

    // Navigate to the next page (triggered after the revisions data is loaded)
    upload: function(oEvent) {
        // Upload will be done server-side
        var app = sap.ui.getCore().byId('idViewApp');
        var ctrl = app.getController();
        var view1 = app.byId('idViewFirst');
        var view2 = app.byId('idViewSecond');
        var view3 = app.byId('idViewThird');
        var viewSet = app.byId('idViewSettings');
        var ctrl3 = view3.getController();

        // Get input from the various UI fields
        var repoURL = encodeURIComponent(view2.byId('repoURLTextField').getValue());
        var bugId = encodeURIComponent(view1.byId('bugIdTextField').getValue());
        var usr = encodeURIComponent(viewSet.byId('uploadUserTextField').getValue());
        var pwd = encodeURIComponent(viewSet.byId('uploadUserPwdPasswordField').getValue());
        
        // Build the 1st part of the query string
        var queryString = "/patchaWeb/pa?a=upload" +
								        "&u=" + repoURL +
								        "&b=" + bugId +
                                        "&t=" + encodeURIComponent(ctrl.delTempEnabled()) +
                                        "&c=" + encodeURIComponent(ctrl.getUploadURL()) +
                                        "&usr=" + usr +
                                        "&pwd=" + pwd;
        
        var sel_revs = ctrl3.getSelectedRevisions();
        var l = sel_revs.length;
        if(l==0) { return; }
	    for (var i = 0; i < l; i++) {
	    	queryString += "&r=" + encodeURIComponent(sel_revs[i]);
	    }
	    
        // Show busy dialog
        if (! this._dialog) {
            this._dialog = sap.ui.xmlfragment("sap.psr.patcha.web.view.BusyDialogUpload", this);
            this.getView().addDependent(this._dialog);
        }
        this._dialog.open();
        var local_dialog = this._dialog;
        
        // In debug mode, just jump to first screen
        if(ctrl.debugEnabled()) {
            this.changesUploaded();
        }
        // Upload JSON data
        else {
	        var jqxhr = jQuery.ajax( {
                type : "GET",
                contentType : "multipart/form-data",
                url : queryString,
                //cache : false,
                //timeout : 300000,
                //dataType : "json"
            })
            .done(function(data, textStatus, jqXHR) {
                // Publish event to indicate successful data load
                sap.ui.getCore().getEventBus().publish("app", "ChangesUploaded");
            })
            .fail(function(jqXHR, textStatus, errorThrown) {
				local_dialog.close();
				alert(errorThrown + ": " + jqXHR.responseText);
            });
    	}
    },
    
    // Navigate to the next page (triggered after the revisions data is loaded)
    changesUploaded: function(oEvent) {
        // Close busy dialog
        this._dialog.close();
        alert("Upload successful");
        // Go to next page
        this.navigation.navTo("idViewApp--idViewFirst");
    },
    
    enterSettings: function(oEvent) {
    	this.navigation.navTo("idViewApp--idViewSettings");
    }
});