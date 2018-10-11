package com.sap.psr.vulas.shared.json.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = false)
public class ConstructChangeInDependency implements Serializable {

    private Trace trace;

    private Boolean traced;
    private Boolean reachable;
    private Boolean inArchive;
    private Boolean affected;
    private Boolean classInArchive;
    private Boolean equalChangeType;
    private String overall_change;

    private ConstructChange constructChange;

    public ConstructChangeInDependency(){super();}

    public ConstructChange getConstructChange() { return constructChange; }
    public void setConstructChange(ConstructChange constructChange) { this.constructChange = constructChange; }

    public Trace getTrace() { return trace; }
    public void setTrace(Trace trace) { this.trace = trace; }

    public Boolean getTraced() { return traced; }
    public void setTraced(Boolean traced) { this.traced = traced; }

    public Boolean getInArchive() { return inArchive; }
    public void setInArchive(Boolean inArchive) { this.inArchive = inArchive; }

    public Boolean getReachable() { return reachable; }
    public void setReachable(Boolean reachable) { this.reachable = reachable; }

    public boolean isAffected() {
        return affected;
    }

    public void setAffected(boolean affected) {
        this.affected = affected;
    }

    public boolean isClassInArchive() {
        return classInArchive;
    }

    public void setClassInArchive(boolean classInArchive) {
        this.classInArchive = classInArchive;
    }

    public boolean isEqualChangeType() {
        return equalChangeType;
    }

    public void setEqualChangeType(boolean equalChangeType) {
        this.equalChangeType = equalChangeType;
    }

    public String getOverall_change() {
        return overall_change;
    }

    public void setOverall_change(String overall_change) {
        this.overall_change = overall_change;
    }

        
}
