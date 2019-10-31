package com.sap.psr.vulas.patcheval.representation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sap.psr.vulas.shared.enums.ConstructChangeType;
import com.sap.psr.vulas.shared.json.model.ConstructChange;
import com.sap.psr.vulas.shared.json.model.LibraryId;
import com.sap.psr.vulas.shared.json.model.Version;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** class containing the results on a single archive. */
public class ArtifactResult2 implements Comparable<ArtifactResult2> {
  private static final Log log = LogFactory.getLog(ArtifactResult2.class);

  String group, artifact;

  Version version;

  Boolean sourcesAvailable;

  List<ConstructPathAssessment2> constructPathAssessments;

  Long timestamp;

  /**
   * Constructor for ArtifactResult2.
   *
   * @param _g a {@link java.lang.String} object.
   * @param _a a {@link java.lang.String} object.
   * @param _v a {@link java.lang.String} object.
   * @param _s a boolean.
   * @param timestamp a {@link java.lang.Long} object.
   */
  public ArtifactResult2(String _g, String _a, String _v, boolean _s, Long timestamp) {

    this.group = _g;
    this.artifact = _a;
    this.version = new Version(_v);

    this.timestamp = timestamp;
    this.sourcesAvailable = _s;
    this.constructPathAssessments = new ArrayList<ConstructPathAssessment2>();
  }

  /**
   * Constructor for ArtifactResult2.
   *
   * @param _g a {@link java.lang.String} object.
   * @param _a a {@link java.lang.String} object.
   * @param _v a {@link java.lang.String} object.
   * @param _s a boolean.
   */
  public ArtifactResult2(String _g, String _a, String _v, boolean _s) {

    this.group = _g;
    this.artifact = _a;
    this.version = new Version(_v);

    this.sourcesAvailable = _s;
    this.constructPathAssessments = new ArrayList<ConstructPathAssessment2>();
  }

  /**
   * addConstructPathAssessment.
   *
   * @param constructPathAssessment2 a {@link
   *     com.sap.psr.vulas.patcheval.representation.ConstructPathAssessment2} object.
   */
  public void addConstructPathAssessment(ConstructPathAssessment2 constructPathAssessment2) {
    this.constructPathAssessments.add(constructPathAssessment2);
  }

  /**
   * Getter for the field <code>constructPathAssessments</code>.
   *
   * @return a {@link java.util.List} object.
   */
  public List<ConstructPathAssessment2> getConstructPathAssessments() {
    return constructPathAssessments;
  }

  /**
   * Getter for the field <code>group</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getGroup() {
    return group;
  }

  /**
   * Setter for the field <code>group</code>.
   *
   * @param group a {@link java.lang.String} object.
   */
  public void setGroup(String group) {
    this.group = group;
  }

  /**
   * Getter for the field <code>artifact</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getArtifact() {
    return artifact;
  }

  /**
   * Setter for the field <code>artifact</code>.
   *
   * @param artifact a {@link java.lang.String} object.
   */
  public void setArtifact(String artifact) {
    this.artifact = artifact;
  }

  /**
   * Getter for the field <code>version</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getVersion() {
    return version.getVersion();
  }

  /**
   * getVersionObject.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.Version} object.
   */
  public Version getVersionObject() {
    return version;
  }

  /**
   * Setter for the field <code>version</code>.
   *
   * @param version a {@link com.sap.psr.vulas.shared.json.model.Version} object.
   */
  public void setVersion(Version version) {
    this.version = version;
  }

  /**
   * getSourceAvailable.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean getSourceAvailable() {
    return sourcesAvailable;
  }

  /**
   * setSourceAvailable.
   *
   * @param s a {@link java.lang.Boolean} object.
   */
  public void setSourceAvailable(Boolean s) {
    this.sourcesAvailable = s;
  }

  /**
   * getMajorRelease.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getMajorRelease() {
    return this.version.getMajorRelease();
    //    	String[] versions = this.version.split("\\.");
    //        return (versions.length>0)?versions[0]:"0";

  }

  /**
   * getMinorRelease.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getMinorRelease() {
    return this.version.getMinorRelease();
    //    	 String[] versions = this.version.split("\\.");
    //         String majorRelease=(versions.length>0)?versions[0]:"0";
    //         //check whether the minor starts with numbers: if it contains also letter just
    // consider the numbers
    //
    //         if(versions.length>1){
    //        	 Pattern p = Pattern.compile("^[0-9]*");
    //        	 Matcher m = p.matcher(versions[1]);
    //        	 if(m.find() ){
    //        		 return majorRelease.concat(".".concat(m.group()));
    //			}
    //         }
    //         return majorRelease.concat(".0");
  }

  /**
   * getMaintenanceRelease.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getMaintenanceRelease() {
    return this.version.getMaintenanceRelease();
    //    	 String[] versions = this.version.split("\\.");
    //         String majorRelease=(versions.length>0)?versions[0]:"0";
    //         String minorRelease =
    // majorRelease.concat(".".concat((versions.length>1)?versions[1]:"0"));
    //         return minorRelease.concat(".".concat((versions.length>2)?versions[2]:"0"));
    //
  }

  //    public Boolean isMicro(){
  //    	if (this.version.split("\\.").length>3)
  //    		return true;
  //    	else
  //    		return false;
  //    }

  /**
   * getVConfidence.
   *
   * @return a {@link java.lang.Double} object.
   */
  public Double getVConfidence() {
    int tot = 0, totV = 0;
    for (ConstructPathAssessment2 cpa : this.getConstructPathAssessments()) {
      if (cpa.getType().equals("MOD")) {
        tot++;
        if (cpa.dToV == 0) totV++;
        else
          ArtifactResult2.log.debug(
              "Construct [" + cpa.getConstruct() + "] not equal to Vulnerable");
      }
    }
    ArtifactResult2.log.debug(
        "Archive ["
            + this.toString()
            + "] is vulnerable for ["
            + totV
            + "] constructs over a total of ["
            + tot
            + "]");
    if (tot != 0) return totV / (double) tot;
    else return new Double(0);
  }

  /**
   * getVPathConfidence.
   *
   * @return a {@link java.lang.Double} object.
   */
  public Double getVPathConfidence() {
    int tot = 0, totV = 0;
    Double confidence = new Double(0);
    List<String> processedPath = new ArrayList<String>();
    for (int i = 0; i < this.getConstructPathAssessments().size(); i++) {
      ConstructPathAssessment2 cpa = this.getConstructPathAssessments().get(i);
      String path = cpa.getPath();
      if (!processedPath.contains(path) && cpa.getType().equals("MOD")) {
        processedPath.add(path);
        tot = 1;
        totV = (cpa.dToV == 0) ? 1 : 0;
        for (int j = i + 1; j < this.getConstructPathAssessments().size(); j++) {
          ConstructPathAssessment2 cpa2 = this.getConstructPathAssessments().get(j);
          if (cpa2.getType().equals("MOD") && cpa2.getPath().equals(path)) {
            tot++;
            if (cpa2.dToV == 0) totV++;
            //		    			else
            //		    				ArtifactResult2.log.debug("Construct [" + cpa2.getConstruct() + "] not equal
            // to Vulnerable");
          }
        }
        ArtifactResult2.log.debug(
            "Archive ["
                + this.toString()
                + "] is vulnerable for ["
                + totV
                + "] constructs over a total of ["
                + tot
                + "]");
        Double c = totV / (double) tot;
        if (c > confidence) confidence = c;
      }
    }
    return confidence;
  }

  /**
   * getFConfidence.
   *
   * @return a {@link java.lang.Double} object.
   */
  public Double getFConfidence() {
    int tot = 0, totF = 0;
    for (ConstructPathAssessment2 cpa : this.getConstructPathAssessments()) {
      if (cpa.getType().equals("MOD")) {
        tot++;
        if (cpa.dToF == 0) totF++;
        else ArtifactResult2.log.debug("Construct [" + cpa.getConstruct() + "] not equal to Fixed");
      }
    }
    ArtifactResult2.log.debug(
        "Archive ["
            + this.toString()
            + "] is fixed for ["
            + totF
            + "] over a total of ["
            + tot
            + "]");
    if (tot != 0) return totF / (double) tot;
    else return new Double(0);
  }

  /**
   * getFPathConfidence.
   *
   * @return a {@link java.lang.Double} object.
   */
  public Double getFPathConfidence() {
    int tot = 0, totF = 0;
    Double confidence = new Double(0);
    List<String> processedPath = new ArrayList<String>();
    for (int i = 0; i < this.getConstructPathAssessments().size(); i++) {
      ConstructPathAssessment2 cpa = this.getConstructPathAssessments().get(i);
      String path = cpa.getPath();
      if (!processedPath.contains(path) && cpa.getType().equals("MOD")) {
        processedPath.add(path);
        tot = 1;
        totF = (cpa.dToF == 0) ? 1 : 0;
        for (int j = i + 1; j < this.getConstructPathAssessments().size(); j++) {
          ConstructPathAssessment2 cpa2 = this.getConstructPathAssessments().get(j);
          if (cpa2.getType().equals("MOD") && cpa2.getPath().equals(path)) {
            tot++;
            if (cpa2.dToF == 0) totF++;
            //		    			else
            //		    				ArtifactResult2.log.debug("Construct [" + cpa2.getConstruct() + "] not equal
            // to Vulnerable");
          }
        }
        ArtifactResult2.log.debug(
            "Archive ["
                + this.toString()
                + "] is vulnerable for ["
                + totF
                + "] constructs over a total of ["
                + tot
                + "] for path ["
                + path
                + "]");
        Double c = totF / (double) tot;
        if (c > confidence) confidence = c;
      }
    }

    return confidence;
  }

  /**
   * Getter for the field <code>timestamp</code>.
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getTimestamp() {
    return timestamp;
  }

  /**
   * Setter for the field <code>timestamp</code>.
   *
   * @param timestamp a long.
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  //    public String getMajorRelease() {
  //        return majorRelease;
  //    }
  //
  //    public void setMajorRelease(String majorRelease) {
  //        this.majorRelease = majorRelease;
  //    }

  private boolean constructResultsConsistent() {
    boolean foundV = false;
    boolean foundF = false;
    for (ConstructPathAssessment2 cpa : this.constructPathAssessments) {
      if (cpa.getType().equals("MOD") && cpa.getdToV() == 0 && cpa.getdToF() != 0) foundV = true;
      else if (cpa.getType().equals("MOD") && cpa.getdToV() != 0 && cpa.getdToF() == 0)
        foundF = true;
      else if (cpa.getType().equals("MOD") && cpa.getdToV() == 0 && cpa.getdToF() == 0) {
        // ArtifactResult2.log.warn("Found a construct equal both to VULNERABLE and to FIXED for the
        // same BUG, LIB [" + this.toString()+"]");
        return false;
      }
    }
    if (foundV && foundF) {
      //	ArtifactResult2.log.warn("Found a construct equal to VULNERABLE and another equal to FIXED
      // for the same BUG, LIB [" + this.toString()+"]");
      return false;
    } else return true;
  }

  /**
   * containsAD.
   *
   * @return a boolean.
   */
  public boolean containsAD() {
    for (ConstructPathAssessment2 cpa : this.constructPathAssessments) {
      if (cpa.getType().equals("ADD") || cpa.getType().equals("DEL")) return true;
    }
    return false;
  }

  // tobe done per path?
  /**
   * isADFixed.
   *
   * @return a boolean.
   */
  public boolean isADFixed() {
    Boolean fixed = false;
    for (ConstructPathAssessment2 cpa : this.constructPathAssessments) {
      if (cpa.getType().equals("ADD") && cpa.getQnameInJar().equals(Boolean.FALSE)) return false;
      else if (cpa.getType().equals("ADD") && cpa.getQnameInJar().equals(Boolean.TRUE))
        fixed = true;
      else if (cpa.getType().equals("DEL") && cpa.getQnameInJar().equals(Boolean.TRUE))
        return false;
      else if (cpa.getType().equals("DEL") && cpa.getQnameInJar().equals(Boolean.FALSE))
        fixed = true;
    }
    return fixed;
  }

  /**
   * isPathADFixed.
   *
   * @return a {@link java.lang.Boolean} object.
   */
  public Boolean isPathADFixed() {
    Boolean fixed = false;
    for (ConstructPathAssessment2 cpa2 : this.getConstructPathAssessments()) {
      String path = cpa2.getPath();
      for (ConstructPathAssessment2 cpa : this.getConstructPathAssessments()) {
        if (cpa.getPath().equals(path)) {
          if (cpa.getType().equals("ADD") && cpa.getQnameInJar().equals(Boolean.FALSE)) {
            fixed = false;
            break;
          } else if (cpa.getType().equals("ADD") && cpa.getQnameInJar().equals(Boolean.TRUE))
            fixed = true;
          else if (cpa.getType().equals("DEL") && cpa.getQnameInJar().equals(Boolean.TRUE)) {
            fixed = false;
            break;
          } else if (cpa.getType().equals("DEL") && cpa.getQnameInJar().equals(Boolean.FALSE))
            fixed = true;
        }
      }
      if (fixed == true) return true;
    }
    return false;
  }

  /*
   * A LID is equal to vulnerable if at least 1 construct is equal to V
   * and there aren't inconsistencies, i.e., it never happens that equality
   * is found both to V and F across all constructs of the change list
   */
  /**
   * isEqualToV.
   *
   * @return a boolean.
   */
  public boolean isEqualToV() {
    if (constructResultsConsistent()) {
      for (ConstructPathAssessment2 cpa : this.constructPathAssessments) {
        if (cpa.getType().equals("MOD") && cpa.getdToV() == 0) return true;
      }
    }
    return false;
  }

  /*
   * A LID is equal to fixed if at least 1 construct is equal to F
   * and there aren't inconsistencies, i.e., it never happens that equality
   * if found both to V and F across all constructs of the change list
   */

  /**
   * isEqualToF.
   *
   * @return a boolean.
   */
  public boolean isEqualToF() {
    if (constructResultsConsistent()) {
      for (ConstructPathAssessment2 cpa : this.constructPathAssessments) {
        if (cpa.getType().equals("MOD") && cpa.getdToF() == 0) {
          return true;
        }
      }
    }
    return false;
  }

  /* (non-Javadoc)
   *
   * ArtifactResult2 are ordered according to the version of the LibraryId, i.e.,
   * 1) to the numbering schema, if only digits are contained
   * 2) to the timestap (of publication in maven central)
   * 3) alphanumerical comparison
   *  "Note: this class has a natural ordering that is inconsistent with equals."
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  /** {@inheritDoc} */
  @Override
  public int compareTo(ArtifactResult2 other) {

    if (this.group.equals(other.getGroup())) {
      if (this.artifact.equals(other.getArtifact())) {
        return this.compareVersion(other);
      } else return this.artifact.compareTo(other.getArtifact());
    } else return this.group.compareTo(other.getGroup());
  }

  /**
   * This method compares the versions of ArtifactResults2 according to the following priorities 1)
   * compare version numbers of the form major.minor[.maintenance[.build]], if only digits are
   * contained 2) compare timestamp (of latest update in maven central) 3) alphanumerical comparison
   *
   * @param other a {@link com.sap.psr.vulas.patcheval.representation.ArtifactResult2} object.
   * @return a int.
   */
  public int compareVersion(ArtifactResult2 other) {
    int i = this.version.compareNumberVersion(other.getVersionObject());
    if (i == 0 && this.getTimestamp() != null && other.getTimestamp() != null)
      i = this.timestamp.compareTo(other.getTimestamp());
    // if the execution is here it means that at least one of the compared version contains
    // something else than numbers
    // if the version does not only contains numbers, we resort to the timestamp to order them,
    // however if the timestamp is the same we resort to
    // a simple string comparison
    if (i == 0) i = this.version.compareTo(other.getVersionObject());
    return i;
  }

  /**
   * equalGAV.
   *
   * @param _group a {@link java.lang.String} object.
   * @param _artifact a {@link java.lang.String} object.
   * @param _version a {@link java.lang.String} object.
   * @return a boolean.
   */
  public boolean equalGAV(String _group, String _artifact, String _version) {
    if (this.group.equals(_group)
        && this.artifact.equals(_artifact)
        && this.version.equals(new Version(_version))) {
      return true;
    } else return false;
  }

  /**
   * getGa.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getGa() {

    return this.group + ":" + this.artifact;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {

    return this.group + ":" + this.artifact + ":" + this.version;
  }

  /**
   * getAffectedcc.
   *
   * @param ConstructChange a {@link java.util.List} object.
   * @return a {@link com.google.gson.JsonArray} object.
   */
  public JsonArray getAffectedcc(List<ConstructChange> ConstructChange) {
    JsonArray affectedcc = new JsonArray();
    for (ConstructPathAssessment2 cpa : this.getConstructPathAssessments()) {
      for (ConstructChange c : ConstructChange) {

        // should enter only once in the if
        if (cpa.getConstruct().equals(c.getConstructId().getQname())
            && c.getRepoPath().contains(cpa.getPath())
            && c.getConstructChangeType().toString().equals(cpa.getType())) {
          JsonObject thisCc = new JsonObject();

          JsonObject cc = new JsonObject();
          cc.addProperty("repo", c.getRepo());
          cc.addProperty("commit", c.getCommit());
          cc.addProperty("repoPath", c.getRepoPath());
          JsonObject constructId = new JsonObject();
          constructId.addProperty("lang", c.getConstructId().getLang().toString());
          constructId.addProperty("type", c.getConstructId().getType().toString());
          constructId.addProperty("qname", c.getConstructId().getQname());
          cc.add("constructId", constructId);

          thisCc.add("cc", cc);
          thisCc.addProperty("pathGroup", cpa.getPath());
          thisCc.addProperty("overall_chg", cpa.getType().toString());
          thisCc.addProperty("qnameInJar", cpa.getQnameInJar());

          if (c.getConstructChangeType().equals(ConstructChangeType.MOD)) {
            thisCc.addProperty("dtv", cpa.getdToV());
            thisCc.addProperty("dtf", cpa.getdToF());
            // thisCc.addProperty("vulnBody", cpa.getVBody());
            // thisCc.addProperty("fixedBody", cpa.getFBody());

            //  	    if ( cpa.getdToV()==0 ) thisCc.addProperty("astEqual", "vuln");
            //  	    if ( cpa.getdToF()==0 ) thisCc.addProperty("astEqual", "fix");

            if (cpa.getAst() != null) {
              thisCc.addProperty("testedBody", cpa.getAst());
              thisCc.addProperty("vulnBody", cpa.getVulnAst());
              thisCc.addProperty("fixedBody", cpa.getFixedAst());
            } else if (cpa.getLibsSameBytecode() != null) {
              JsonArray sameBytecodeArray = new JsonArray();
              for (LibraryId l : cpa.getLibsSameBytecode()) {
                JsonObject libraryId = new JsonObject();
                libraryId.addProperty("group", l.getMvnGroup());
                libraryId.addProperty("artifact", l.getArtifact());
                libraryId.addProperty("version", l.getVersion());
                sameBytecodeArray.add(libraryId);
              }

              thisCc.add("sameBytecodeLids", sameBytecodeArray);
            }
          }
          affectedcc.add(thisCc);
          break;
        }
      }
    }
    return affectedcc;
  }
}
