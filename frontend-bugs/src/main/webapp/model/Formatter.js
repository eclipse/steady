jQuery.sap.declare("model.Formatter");

model.Formatter = {

	affectedVersionSource: function(_affected_version_source) {
		
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
			else if(_affected_version_source === "TO_REVIEW")
				return "REVIEW";
			else if(_affected_version_source === "PROPAGATE_MANUAL")
				return "PM";
		
	},
	
	fieldVisibility : function(_val) {
		return _val!==undefined && _val!== null && _val !=="";
	},
	
	
	imagePath : function(sSrc) {
		if (sSrc === true) {
			return "img/maven.png";
		} else {
			return "img/notmaven.png";
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
	
	
	falseToHighlight: function(_boolean) {
	    if (_boolean === false) {
			return "Negative";
		} else {
			return "Default";
		}
	},
	
	mavenIdConfirmed : function(_confirmed) {
		if(_confirmed && _confirmed===1) {
			return true;
		} else {
			return false;
		}
	},
	
	mavenIdConfirmedColor : function(_confirmed) {
		if(_confirmed && _confirmed===1) {
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
		} else {
			return "N/A";
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
			return "Negative";
		} else if (sType === false) {
			return "Positive";
		} else {
			return "Positive";
		}
	},
	
        textForAffected : function(sType) {
		if (sType === true) {
			return "Vulnerable";
		} else if (sType === false) {
			return "Fixed";
		} else {
			return "";
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
                    if (list.length < 20) {
                            return list.length;
                    } else {
                            return 20;
                    }
		} else {
                    return 10;
		}
	},
        
	getCvesListLength : function(list) {
		
                if (list) {
                    if(list.length==0)
                            return 1;
                    if (list.length < 30) {
                            return list.length;
                    } else {
                            return 30;
                    }
		} else {
                    return 30;
		}
	},
        
        getMediumListLength : function(list) {
		
                if (list) {
                    if(list.length==0)
                            return 1;
                    if (list.length < 15) {
                            return list.length;
                    } else {
                            return 15;
                    }
		} else {
                    return 15;
		}
	},
        
        getShortListLength : function(list) {
		
                if (list) {
                    if(list.length==0)
                            return 1;
                    if (list.length < 8) {
                            return list.length;
                    } else {
                            return 8;
                    }
		} else {
                    return 8;
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
	
	getSmashButtonType : function(_smash_search_result) {
	    if(_smash_search_result && _smash_search_result.resultCount>0) {
	        return "Reject";
	    }
	    else {
	        return "Default";
	    }
	},
        
        radioButtonFormatter : function(manual) {
            if ( typeof(manual)==='undefined' ){
                return "mkn";
            } else {
                if ( manual ) {
                    return "mkt";
                } else if ( !manual ) {
                    return "mkf";
                }
            }
        },
        
        rowColorAST : function(value) {
            console.log(value);
            if ( value!= null)
            {   
                switch (value){
                    case '':
                    case 'Move':
                        return 'myUpdateColor';
                        break;
                    case 'Insert':
                        return 'myUpdateColor';
                        break;
                    case 'Delete':
                        return 'myUpdateColor';
                        break;
                    case 'Update':
                        return 'myUpdateColor';
                        break;
                    default:
                        return 'myUpdateColor';
                        break;
                }
            }
        }, 
        
        showImageLink : function(value){
            if ( typeof(value)!=='undefined'){ 
                return true;
            }
            return false;
        },
        
        showImageConstructName : function(value) {
            if ( value != null && (isNaN(value)) && value[0]!='/' ) {
                return true;
            }
            return false;
        },
        
        showImagePE : function(value, affected){
            if ( typeof(affected)!=='undefined' ) {
                //console.log(affected);
                if ( affected === true ) {
                    return "img/alert.png";
                } else {
                    return "img/ok.png";
                }               
            }
            else
            	return "";
        },
        
        tooltipPEFormatter : function (ff,lv,fi,ti,c,pc){
            var s = "";
            
            if ( typeof(lv)!=='undefined' && lv !== null ){
                s += "LV : "+ lv + "\n";
            }
            if ( typeof(ff)!=='undefined' && ff !== null ){
                s += "FF: "+ ff + "\n";
            }
            if ( typeof(fi)!=='undefined' && fi !== null ){
                s += "From I : "+ fi + "\n";
            }
            if ( typeof(ti)!=='undefined' && ti !== null ){
                s += "To I : "+ ti + "\n";
            }
            if ( typeof(c)!=='undefined' && c !== null ){
                s += "Conf : "+ c + "\n";
            }
            if ( typeof(pc)!=='undefined' && pc !== null ){
                s += "Path conf : "+ pc + "\n";
            }
            return s;
        },
        
        mavenIdLink : function(groupid, artifactid, version) {
		if (groupid && artifactid && version) {
			return groupid + " : " + artifactid + " : " + version;
		} else {
			return "";
		}
	},
        
        showImageModTree : function (name, type){
            //console.log(type);
            if (type=="MOD"){
               return "img/linkimage.png";
            }            
        }
};