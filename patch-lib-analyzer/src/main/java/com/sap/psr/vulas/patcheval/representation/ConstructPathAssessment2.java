package com.sap.psr.vulas.patcheval.representation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.patcheval2.BugLibManager;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.json.model.LibraryId;

/**
 * Class used for the results obtained for a construct,path.
 */
public class ConstructPathAssessment2 {

    String construct;
    String path;
    
    ConstructType constructType;
    
    String type;
    
	Boolean qnameInJAR;
   // Boolean sources;
    String ast;
    String vulnAst;
    String fixedAst;
    

	Integer dToV, dToF;
	
	Integer doneComparisons;
	
	List<LibraryId> libsSameBytecode;
	

public ConstructPathAssessment2(String _construct, String _path, ConstructType _ctype, Boolean _q, String _ast, int _dToV, int _dToF, String _t,String _vast,String _fast) {
        
        
        this.ast = _ast;
        this.vulnAst = _vast;
        this.fixedAst = _fast;
        this.construct = _construct;
        this.qnameInJAR = _q;
        this.path = _path;
        this.constructType = _ctype;
     //   this.sources = _sources;
        this.dToF = _dToF;
        this.dToV = _dToV;
        this.type = _t;
    }
   
   public ConstructPathAssessment2(String _construct, String _path, Boolean _q,String _t) {
       
       
       this.ast = null;
       this.vulnAst = null;
       this.fixedAst = null;
       this.construct = _construct;
       this.qnameInJAR = _q;
       this.path = _path;
    //   this.sources = _sources;
       this.dToF = null;
       this.dToV = null;
       this.type = _t;
   }

    public String getConstruct() {
        return construct;
    }

    public void setConstruct(String construct) {
        this.construct = construct;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ConstructType getConstructType() {
        return constructType;
    }

    public void setConstructType(ConstructType ct) {
        this.constructType = ct;
    }
    
    public Boolean getQnameInJar() {
        return qnameInJAR;
    }

    public String getVulnAst() {
		return vulnAst;
	}

	public void setVulnAst(String vulnAst) {
		this.vulnAst = vulnAst;
	}

	public String getFixedAst() {
		return fixedAst;
	}

	public void setFixedAst(String fixedAst) {
		this.fixedAst = fixedAst;
	}

	public void setQnameInJar(Boolean q) {
        this.qnameInJAR = q;
    }
    

    public Integer getdToV() {
    	return dToV;
    }

    public void setdToV(int dToV) {
        this.dToV = dToV;
    }

    public Integer getdToF() {
   		return dToF;
    }

    public void setdToF(int dToF) {
        this.dToF = dToF;
    }

    public String getAst() {
		return ast;
	}

	public void setAst(String ast) {
		this.ast = ast;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<LibraryId> getLibsSameBytecode() {
		return libsSameBytecode;
	}

	public void setLibsSameBytecodeAsString(String[] libsSameBytecode) {
		if (this.libsSameBytecode == null)
			this.libsSameBytecode= new ArrayList<LibraryId>();
		for(int i=0;i<libsSameBytecode.length;i++){
			String[] l = libsSameBytecode[i].split(":");
			this.libsSameBytecode.add(new LibraryId(l[0],l[1],l[2]));
		}
	}
	
	public void setLibsSameBytecode(List<LibraryId> libsSameBytecode) {
		this.libsSameBytecode=libsSameBytecode;
	}

	
	public Integer getDoneComparisons() {
		return doneComparisons;
	}

	public void setDoneComparisons(Integer doneComparisons) {
		this.doneComparisons = doneComparisons;
	}
}
