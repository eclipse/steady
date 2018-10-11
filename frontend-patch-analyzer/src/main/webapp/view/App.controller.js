sap.ui.controller("sap.psr.patcha.web.view.App", {

    onInit: function(oEvent) {
        this.oApp = this.getView().byId("idAppRoot");

        // Have child views use this controller for navigation
        var that = this;
        this.oApp.getPages().forEach(function(oPage) {
            oPage.getController().navigation = that;
        });

    },

    navTo: function(sPageId, oContext) {
        this.oApp.to(sPageId);
        if (oContext) {
            this.oApp.getPage(sPageId).setBindingContext(oContext);
        }
    },

    navBack: function() {
        this.oApp.back();
    },
    
    enterSettings: function(oEvent) {
    	this.navigation.navTo("idViewApp--idViewSettings");
    },
    
    debugEnabled: function() {
    	var app = sap.ui.getCore().byId('idViewApp');
    	var mod = app.getModel("settings");
    	return mod.getData().debMode;
    },
    
    delTempEnabled: function() {
    	var app = sap.ui.getCore().byId('idViewApp');
    	var mod = app.getModel("settings");
    	return mod.getData().delTemp;
    },
    
    getUploadURL: function() {
    	var app = sap.ui.getCore().byId('idViewApp');
    	var mod = app.getModel("settings");
    	return mod.getData().uploadURL;
    },
    
    buildSvnRevUrl: function(rev) {
    	var rev_url = "";
    	
    	// Get the repo URL from the search form
    	var app = sap.ui.getCore().byId('idViewApp');
    	var svn_url = app.getModel("search").getData().svnUrl;
    	var svn_loc = new Location(svn_url);
    	
    	// Build and return the Url of the revision 
    	var rev_url= svn_loc.protocol + svn_loc.host + "/viewvc?view=revision&revision=" + rev;
    	return rev_url;
    },
    
    buildNvdUrl: function(cve) {
    	return "http://web.nvd.nist.gov/view/vuln/detail?vulnId=" + cve;
    },
    
    buildCweUrl: function(cwe) {
    	if(cwe!=null && cwe.indexOf("-")!=-1) {
    		return "http://cwe.mitre.org/data/definitions/" + cwe.substring(cwe.indexOf("-")+1) + ".html";
    	}
    	else {
    		return "";
    	}
    },
    
    /* Constructs an instance of Date from a date representation in the form yyyy-mm-dd. */
    extractPubDate: function(_cve) {
    	var y = parseInt(_cve.published.substr(0,4));
    	var m = parseInt(_cve.published.substr(5,2)) - 1;
    	var d = parseInt(_cve.published.substr(8,2));
    	return new Date(y, m, d);
    },
    
    /* Goes through all CVE links in order to find a repo URL (SVN) and revision information. */
    extractRepoInfo: function(_cve) {
    	var i = 0, repo_info = { repoUrl : null, revisions : [] }, loc = null, rev = null;
    	for(i=0; i<_cve.references.length; i++) {
    		try {
    			loc = new URI(_cve.references[i].link);
	    		if(loc._parts.hostname.indexOf('svn')!=-1) {
	    			// Reconstruct the repository URL from the single elements
	    			repo_info.repoUrl = loc._parts.protocol + "://" + loc._parts.hostname + (loc._parts.port!=null?":"+loc._parts.port:"");
	    			
	    			// Try to extract the rev number from the query string
	    			// Possibility 1: viewvc (as used by Apache, e.g., http://svn.apache.org/viewvc?view=revision&revision=1564724)
	    			if(loc._parts.query!=null) {
	    				rev = this.extractRevFromViewVCQuery(loc._parts.query);
	    				if(rev!=null) { repo_info.revisions.push(rev); }
	    			}
	    			// Possibility 2: http://svn.apache.org/r1565143
	    			if(rev==null && loc._parts.path!=null) {
	    				rev = this.extractRevFromShortViewVCQuery(loc._parts.path);
	    				if(rev!=null) { repo_info.revisions.push(rev); }
	    			}
	    		}
    		}
    		catch(e) {}    		
    	}
    	return repo_info;
    },
    
    extractRevFromViewVCQuery: function(_q) {
    	var params = _q.split("&"), param = null, rev = null, i = 0, is_viewvc = false;
    	for(i=0; i<params.length; i++) {
    		param = params[i].split("=");
    		if(param[0]=="view" && param[1]=="revision") {
    			is_viewvc = true;
    		}
    		if(param[0]=="revision") {
    			rev = param[1];
    		}
    	}
    	return rev;
    },
    
    extractRevFromShortViewVCQuery: function(_p) {
    	var rev = null, i = null;
    	if(_p.substr(1).split("/").length==1 && _p.substr(1,1)=="r" && !isNaN(_p.substr(2))) {
    		rev = _p.substr(2);
    	}
    	return rev;
    }
});