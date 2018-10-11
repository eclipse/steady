sap.ui.controller("sap.psr.patcha.web.view.First", {

	onInit : function() {
        // Register for the event "cveLoaded"
        var bus = sap.ui.getCore().getEventBus();
        bus.subscribe("app", "cveLoaded", this.cveLoaded, this);
    },
    
    enterBugId: function(oEvent) {

    	// The bug ID will become the search string of the second view (as long as this text field is empty)
        var app = sap.ui.getCore().byId('idViewApp');
        var view2 = app.byId('idViewSecond');
        var thisView = this.getView();
        
        // Get the bug Id and check whether it follows the CVE format CVE-yyyy-xxx
        var bugid = thisView.byId('bugIdTextField').getValue();
        
        // Check whether this bug id is already present in the DB
        var is_existing = false; // isExisting(bugid);
        
        // Show model dialog if existing
        if(is_existing==true) {
        	
        }
        
        // Show busy dialog
        if (! this._dialog) {
            this._dialog = sap.ui.xmlfragment("sap.psr.patcha.web.view.BusyDialogPatchaBackend", this);
            this.getView().addDependent(this._dialog);
        }
        this._dialog.open();
        var local_dialog = this._dialog;
        
        var queryString = "http://10.97.26.145:8000/NVD_SEARCH/xs/REFDB.xsjs?cve=" + bugid;
    	
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
				app.setModel(oModel, "cve");
				
				// Publish event to indicate successful data load
				sap.ui.getCore().getEventBus().publish("app", "cveLoaded");
			})
			.fail(function(jqXHR, textStatus, errorThrown) {
				local_dialog.close();
				alert(errorThrown + ": " + jqXHR.responseText);
			});
        
        
        // Exit here if the user does not want to continue
        
        
        // Check format
        var is_cve = true; // isCVE(bugid);
        if(is_cve==true) {
        	// Read additional information from the NVD copy
        }
    },
    
    // Navigate to the next page (triggered after the revisions data is loaded)
    cveLoaded: function(oEvent) {
    	var app   = sap.ui.getCore().byId('idViewApp');
    	var ctrl  = app.getController();
    	var view2 = app.byId('idViewSecond');
    	
    	// Get the CVE data that was just set before
    	var cve = app.getModel("cve").getData();
    	var date_helper = null;
    	
		// Update bug model with CVE details
        var bugData = app.getModel("bug").getData();
        bugData.bugid  = cve.cve_id;
        bugData.desc   = cve.summary;
        bugData.cwe    = cve.cwe_id;
        bugData.cvss   = cve.cvss_score;
        date_helper    = ctrl.extractPubDate(cve);
        bugData.pub    = date_helper;//$.datepicker.formatDate('yymmdd', date_helper);
        bugData.cweUrl = ctrl.buildCweUrl(cve.cwe_id);
        bugData.nvdUrl = ctrl.buildNvdUrl(cve.cve_id);
        bugData.edbUrl = "http://www.exploit-db.com/search/?action=search&filter_page=1&filter_description=&filter_exploit_text=&filter_author=&filter_platform=0&filter_type=0&filter_lang_id=0&filter_port=&filter_osvdb=&filter_cve=" + cve.cve_id.substr(4);
        app.getModel("bug").setData(bugData);
        
        // Extract repo URL from CVE refs
        var repo_info = ctrl.extractRepoInfo(cve);
        
        // Update search model after analysis of CVE information (if any)
        var searchData = app.getModel("search").getData();
        searchData.searchString = cve.cve_id;
        //date_helper.setMonth(date_helper.getMonth()-12); // 12 months earlier
        //searchData.asOf = $.datepicker.formatDate('yymmdd', date_helper);
        
        // Set repo host + complete URL
        if(repo_info.repoUrl!=null) {
        	
        	// Complete repo URL (not yet working, can be mapping of repo host + CPE information to various Apache or other repos)
        	if(repo_info.repoUrl.indexOf("svn.apache.org")!=-1) { searchData.svnUrl = "http://svn.apache.org/repos/asf/commons/proper/fileupload"; }
        	else { searchData.svnUrl = repo_info.repoUrl; }
        	
        	// Host info (for revision links constructed in tables of view 3 and 4)
        	searchData.svnHost = repo_info.repoUrl;
        }
        else { searchData.svnUrl = "http://svn.apache.org/repos/asf/commons/proper/fileupload"; }
                
        // Set revisions
        if(repo_info.revisions!=null && repo_info.revisions.length>0) {
        	searchData.revisions = repo_info.revisions.join(", ");
//        	view2.byId("searchMode").setSelectedIndex(1);
//        	view2.byId("searchStringTextField").setVisible(false);
//        	view2.byId("searchDateDatePicker").setVisible(false);
//        	view2.byId("revisionsTextField").setVisible(true);
        }
        else { searchData.revisions = ""; }
        app.getModel("search").setData(searchData);
        
    	// Remove busy dialog
    	this._dialog.close();
    	// Go to next page
    	this.navigation.navTo("idViewApp--idViewSecond");
    },
    
    enterSettings: function(oEvent) {
    	this.navigation.navTo("idViewApp--idViewSettings");
    }
});