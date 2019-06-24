jQuery.sap.declare("model.Formatter");

model.Formatter = {

    //
    // The following functions are all responsible for visualizing the assessment levels
    //
    
    // Delivers the img src for an entire app 
//	appSummary : function(_vulns_count, _affected_version, _affected_version_confirmed, _reachable, _reachable_confirmed, _traced, _traced_confirmed, _artifactid) {
//	    // No affected release, all is good
//	    if(_affected_version===0 && _affected_version_confirmed===_vulns_count) { return "img/ok.png"; }
//	    else {
//	        if(_traced > 0) {
//	            return "img/att3.png";
//	        }
//	        else if(_reachable > 0) {
//	            return "img/att2.png";
//	        }
//	        else {
//	            if(_affected_version > 0)
//	                return "img/att1.png";
//	            else
//	                return "img/q.png";
//	        }
//	    }
//	},
	

    // Delivers the img src for an entire app 
//	appSummaryWorst : function(_worst) {
//	        if(_worst===4) {
//	            return "img/att3.png";
//	        }
//	        else if(_worst===3) {
//	            return "img/att2.png";
//	        }
//	        else if(_worst===2) {
//	            return "img/att1.png";
//	        }
//	        else if(_worst===1) {
//	            return "img/q.png";
//	        }
//	        else if(_worst===0) {
//	            return "img/ok.png";
//	        }
//	},
	
    // Delivers the img src to indicate whether the applications has vulnerabilities 
	appHasVuln : function(_hasVulnerabilities) {
			// forcing the transparent icon when displaying the default space
			if (model.Config.getSpace() === model.Config.getDefaultSpace()) {
				return "img/transparent.png";
			}
	        if(_hasVulnerabilities===true) {
	            return "img/version_alert.png";
	        }
	        else if (_hasVulnerabilities===false){
	            return "img/transparent.png";
	        }
			else{
			// in case the content is null, it means we did not ask for this data.
				return "img/loading.png";
			}
	},
	
	// Delivers the img src for a single archive and vulnerability
	excemptedSrc : function(_excemption) {
		if (_excemption === null) {
			return "img/transparent.png";
		} else {
			if(_excemption.excludedBug || _excemption.excludedScope)
				return "img/excempted.png";
			else
				return "img/transparent.png";
		}
	},
	
	// Delivers the img src for a single archive and vulnerability
	excemptedTooltip : function(_excemption, _scope) {
		if (_excemption === null) {
			return "";
		} else {
			if(_excemption.excludedBug)
				return _excemption.excludedBugReason;
			else if(_excemption.excludedScope)
	            return "Scope " + _scope + " excluded";
			else
				return "";
		}
	},
	
	// Delivers the img src for a single archive and vulnerability
	affectedRelease : function(_affected_release, _affected_release_confirmed) {
		if (_affected_release === 1 && _affected_release_confirmed === 1) {
			return "img/version_alert.png";
		} else if (_affected_release === 0 && _affected_release_confirmed === 1) {
			return "img/version_ok.png";
		} else {
			return "img/version_qmark.png";
		}
	},
	

	affectedVersionSource: function(_affected_version_source, _affected_release_confirmed) {
		if (_affected_release_confirmed === 1) {
			if(_affected_version_source === "MANUAL")
				return "M";
			else if(_affected_version_source === "PRE_COMMIT_POM")
				return "P";
			else if(_affected_version_source === "CHECK_VERSION")
				return "C";
			else if(_affected_version_source === "AST_EQUALITY")
				return "E";
			else if(_affected_version_source === "MAJOR_EQUALITY")
				return "ME";
			else if(_affected_version_source === "MINOR_EQUALITY")
				return "mE";
			else if(_affected_version_source === "INTERSECTION")
				return "IE";
			else if(_affected_version_source === "GREATER_RELEASE")
				return "GE";
		} else  
			return "";
	},
	
	// Delivers the img src for a single archive and vulnerability
	vulnReachable : function(_affected_release, _affected_release_confirmed, _reachable, _reachable_confirmed) {
	    // Reachability analysis was NOT run OR release not affected
		if (_reachable_confirmed === 0 || (_affected_release === 0 && _affected_release_confirmed === 1))
			return "img/transparent.png";
		// Reachability analysis was run
		else {
		    // Affected release
		    if(_reachable ===  1 ) {
		        return "img/trace_alert.png";
		    } else {
		        return "img/trace_ok.png";
		    }
		}
	},
	
//	// Delivers the img src for a single archive and vulnerability
//	vulnCode : function(_vuln_code) {
//		if (_vuln_code!==null && _vuln_code===true) {
//		    return "img/att1.png";
//		}
//		else if (_vuln_code!==null && _vuln_code===false){
//		    return "img/ok.png";
//		}
//		else {
//		    return "img/q.png";
//		}
//	},
	
	// Delivers the img src for a single archive and vulnerability
	vulnTraced : function(_affected_release, _affected_release_confirmed, _traced, _traced_confirmed) {
	    // tracing was NOT run
		if (_traced_confirmed === 0 || (_affected_release === 0 && _affected_release_confirmed === 1)) 
		        return "img/transparent.png";
		// tracing was run
		else {
		    // Block to be used in order to not show any icon when release is not affected
		    // Affected release
		    if(_traced === 1) 
		        return "img/trace_alert.png";
		     else 
		        return  "img/trace_ok.png"; //to be replaced with "" to fall back to result for affected release (if the tests were run but didn't trace the vulnerable code we leave it black )
		 }
	},
	
	fieldVisibility : function(_val) {
		return _val!==undefined && _val!== null && _val !=="";
	},
	
//	executionStatus : function(sSrc) {
//		if (sSrc !== null && sSrc !== '') {
//			return "img/notmaven.png";
//		} else {
//			return "";
//		}
//	},
	
//	imagePath : function(sSrc) {
//		if (sSrc === true) {
//			return "img/maven.png";
//		} else {
//			return "img/notmaven.png";
//		}
//	},
	
	directTransitive : function(_t) {
		if (_t === true) {
			return "transitive";
		} else {
			return "direct";
		}
	},

	mavenIdLink : function(groupid, artifactid, version) {
		if (groupid && artifactid && version) {
			return groupid + " : " + artifactid + " : " + version;
		} else {
			return "";
		}
	},
	
	buildmavenIdLink: function(groupid, artifactid) {
		if (groupid && artifactid) {
			return "http://search.maven.org/#search|gav|1|g%3A%22" + groupid + "%22%20AND%20a%3A%22" + artifactid + "%22";
		} else {
			return "";
		}
	},

	countConstructsToBool: function(count) {
		if (count>0) {
			return true;
		} else {
			return false;
		}
	},
	
	falseToHighlight: function(_boolean) {
	    if (_boolean === false) {
			return "Negative";
		} else {
			return "Default";
		}
	},
	
	mavenIdConfirmed : function(_confirmed) {
		if(_confirmed && _confirmed===true) {
			return true;
		} else {
			return false;
		}
	},
	
	mavenIdConfirmedColor : function(_confirmed) {
		if(_confirmed && _confirmed===true) {
			return "Positive";
		} else {
			return "Negative";
		}
	},

	mavenIdVisibility : function(mavenId) {
		if (mavenId) {
			return true;
		} else {
			return false;
		}
	},
	
	constructListVisibility : function(_constructList) {
		if(_constructList!==null && Array.isArray(_constructList) && _constructList.length>0) {
			return true;
		} else {
			return false;
		}
	},
	
	noChangeListVisibility : function(_constructList) {
		if(_constructList!==null && Array.isArray(_constructList) && _constructList.length>0) {
			return false;
		} else {
			return true;
		}
	},


	latestMinorVisibility : function(latestMinor) {
		if (latestMinor) {
			return true;
		} else {
			return false;
		}
	},

	latestMajorVisibility : function(latestMajor) {
		if (latestMajor) {
			return true;
		} else {
			return false;
		}
	},
	
	exceptionVisibility : function(exception) {
		if (exception!==null && exception!=="") {
			return true;
		} else {
			return false;
		}
	},

	changeType : function(sChange) {
		if (sChange === "ADD") {
			return "Added";
		} else if (sChange === "MOD") {
			return "Modified";
		} else if (sChange === "DEL") {
			return "Deleted";
		} else if (sChange === "STB") {
			return "Stable";
		} else {
			return "N/A";
		}
	},
	constructType : function(sType) {
		if (sType === "CONS") {
			return "Constructor";
		} else if (sType === "METH") {
			return "Method";
		} else if(sType === "CLAS"){
			return "Class";
		} else if(sType === "FUNC"){
			return "Function";
		} else if(sType === "MODU"){
			return "Module";
		} else{
			return sType;
		}
	},

	rowColorForConstructs : function(bUsed) {
		if (bUsed) {
			return "Negative";
		} else {
			return "Default";
		}
	},

	rowColorForArchives : function(sType) {
		if (sType === true) {
			return "Positive";
		} else if (sType === false) {
			return "Default";
		} else {
			return "Default";
		}
	},
	
	booleanToSemantic : function(_boolean) {
	    if (_boolean === true) {
			return "Positive";
		} else if (_boolean === false) {
			return "Negative";
		} else {
			return "Default";
		}
	},
	
	digestTimestamp : function(_timestamp) {
	    if(_timestamp!==null && _timestamp!=="") {
			return _timestamp.substring(0, 10);
		} else {
			return "N/A";
		}
	},
	
	rowColorForVuln : function(sType) {
		if (sType === true) {
			return "Negative";
		} else if (sType === false) {
			return "Positive";
		} else {
			return "Default";
		}
	},
	
	designForArchives : function(sType) {
		if (sType === true) {
			return "Underline";
		} else if (sType === false) {
			return "Standard";
		} else {
			return "Standard";
		}
	},

	getListLength : function(list) {
		if (list) {
			if(list.length==0)
				return 1;
			if (list.length < 10) {
				return list.length;
			} else {
				return 10;
			}
		} else {
			return 10;
		}
	},
	
//	affectedColumnImage : function(bAffected) {
//		if (bAffected===true) {
//			return "img/att1.png";
//		} else {
//			return "";
//		}
//	},
	
	reachTracedColumnText : function(type){
		if(type==='CLAS')
			return "N/a";
		else return "";

	}, 
	
	reachableColumnImage : function(bReachable) {
		if (bReachable===true) {
			return "img/trace_alert.png";
		} else if (bReachable===false) {
			return "img/trace_ok.png";
		} else {
			return "";
		}
	},
	
	vulnerableColumnImage : function(_affected) {
        if(_affected!==null){ //&& _version_check.hasOwnProperty("fixed_version")) {
            if(_affected===true) {
                return "img/version_alert.png";
            } else {
                return "img/version_ok.png";
            }
        } else {
            return "";
        }
	},

	usedColumnImage : function(bUsed) {
		if (bUsed) {
			return "img/trace_alert.png";
		} else if(bUsed===false) {
			return "img/trace_ok.png";
			
			return "";
		}
	},

	buildCweUrl : function(cwe) {
		if (cwe) {
			return "http://cwe.mitre.org/data/definitions/"
					+ cwe.substring(cwe.indexOf("-") + 1) + ".html";
		} else {
			return "";
		}
	},
	
	libraryId : function(lib){
		if (lib!=null && lib.libraryId!=null){
			return "Artifact ID: "+lib.libraryId.group+":"+lib.libraryId.artifact+":"+lib.libraryId.version;
		}
		return "Artifact ID: unknown";
	},
	
	vulnDepOrigin : function(origin){
		if(origin == "CC")
			return "Dependency contains vulnerable code";
		else if(origin == "AFFLIBID")
			return "Dependency is known to be affected by the configuration vulnerability (no vulnerable code)";
		else if(origin == "BUNDLEDCC")
			return "Dependency rebundles a library containing vulnerable code (altering the construct signatures)";
		else if(origin == "BUNDLEDAFFLIBID")
			return "Dependency rebundles a library known to be affected by the configuration vulnerability (no vulnerable code)";

	},
	
	bundledGAV : function(libLibId,libid){
		if(libLibId!=null && libLibId!=undefined)
			return "Through rebundled: " + model.Formatter.mavenIdLink(libLibId.group,libLibId.artifact,libLibId.version);
		else if (libid!=null && libid!=undefined)
			return "Through rebundled: " + model.Formatter.mavenIdLink(libid.group,libid.artifact,libid.version);
		else
			return null;
		
	}
};