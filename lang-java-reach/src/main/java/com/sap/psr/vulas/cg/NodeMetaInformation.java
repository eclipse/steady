/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.cg;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.ConstructId;

/**
 * This class represent the MetaInformation that a node in the {@link CallGraph} can carry with himself
 */
public class NodeMetaInformation {
    private ConstructId originalConstructId = null;            
    private ConstructId modifiedConstructId = null;
    private String jarUrl = null;
    private String archiveID = null;
    
    /**
     * Used for the attackSurface detection in {@link ReachabilityAnalyzer#identifyTouchPoints()}
     */
    private List<NodeMetaInformation> attackSurface = null;
    
    public NodeMetaInformation(ConstructId _originalConstructId, ConstructId _modifiedConstructId, URL _jar_url, String _archiveID){
        this.originalConstructId = _originalConstructId;
        this.modifiedConstructId = _modifiedConstructId;
        this.jarUrl = (_jar_url==null?null:_jar_url.toString());
        this.archiveID = _archiveID;
    }
    
    public NodeMetaInformation(ConstructId _originalConstructId){
        this.originalConstructId = _originalConstructId;
    }
    
    /**
     * Used for the attackSurface detection in {@link ReachabilityAnalyzer#identifyTouchPoints()}
     */        
    public void addToList(NodeMetaInformation _rs){
        if(this.attackSurface == null){
            this.attackSurface = new ArrayList<NodeMetaInformation>();
        }
        this.attackSurface.add(_rs);
    }
    
    /**
     * Used for the attackSurface detection in {@link ReachabilityAnalyzer#identifyTouchPoints()}
     */
    public int getListSize(){
        return attackSurface!=null ? attackSurface.size() : 0;
    }

    /**
    * Compares the construct with the given construct by comparing their qualified name.
    * @param _c
    * @return
    */
    public final int compareTo(NodeMetaInformation _c) { return this.getOriginalConstructId().compareTo(_c.getOriginalConstructId()); }

    @Override
    public String toString() {
    	final StringBuilder b = new StringBuilder();
    	b.append("[oc=").append(this.originalConstructId.getQname());
    	if(this.modifiedConstructId!=null)
    		b.append(", mc=").append(this.modifiedConstructId.getQname());
    	b.append(", jarUrl=").append(this.jarUrl).append("]");
    	return b.toString();
    }
    
    @Override
    public int hashCode() {
           final int prime = 31;
           int result = 1;
           result = prime * result + this.getOriginalConstructId().hashCode();
           return result;
    }

    /**
    * Returns true if the qualified name of the two constructs are equal, false otherwise.
    */
    @Override
    public boolean equals(Object obj) {
           if (this == obj)
                   return true;
           if (obj == null)
                   return false;
           if (getClass() != obj.getClass())
                   return false;
           NodeMetaInformation other = (NodeMetaInformation) obj;
           return this.getOriginalConstructId().equals(other.getOriginalConstructId());
    }

    private ConstructId getOriginalConstructId() {
        return this.originalConstructId;
    }
    
    public ConstructId getConstructId(){
        return this.modifiedConstructId!=null? this.modifiedConstructId : this.originalConstructId;
    }
    
    public String getJarUrl(){
        return this.jarUrl;
    }
    
    public JsonObject toJSON() { return this.toJSON(false); }
    
    public JsonObject toJSON(boolean _addAlsoAttackSurface){
        // create GSON object with the right constructID
        final JsonObject rootObj = new JsonParser().parse(JacksonUtil.asJsonString(this.getConstructId())).getAsJsonObject();
        // add JAR URL
        if(this.jarUrl!=null) rootObj.addProperty("jarUrl", this.jarUrl);
        // add JAR ID
        if(this.archiveID!=null) rootObj.addProperty("archiveID", this.archiveID);
        // add list
        if(_addAlsoAttackSurface && this.getListSize()>0){
            JsonArray myArray = new JsonArray();
            for(NodeMetaInformation element : this.attackSurface){
                JsonObject child = element.toJSON(_addAlsoAttackSurface);
                myArray.add(child);
            }
            rootObj.add("attackSurface", myArray);
        }
        return rootObj;
    }
    
    public String getArchiveId() { return this.archiveID; }
}
