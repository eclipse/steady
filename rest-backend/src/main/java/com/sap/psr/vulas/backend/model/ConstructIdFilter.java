package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.psr.vulas.shared.enums.ConstructType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/** TODO: Rename to ConstructTypeStatistics */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConstructIdFilter {

  // TODO: Memory: Save just the counters rather than a collection of all the constructs?
  private Map<ConstructType, SortedSet<ConstructId>> constructIds =
      new HashMap<ConstructType, SortedSet<ConstructId>>();

  /**
   * Constructor for ConstructIdFilter.
   *
   * @param _constructs_ids a {@link java.util.Collection} object.
   */
  public ConstructIdFilter(Collection<ConstructId> _constructs_ids) {
    SortedSet<ConstructId> cids = null;
    if (_constructs_ids != null) {
      for (ConstructId cid : _constructs_ids) {
        this.addConstructId(cid);
      }
    }
  }

  /**
   * addConstructId.
   *
   * @param _cid a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   */
  public void addConstructId(ConstructId _cid) {
    SortedSet<ConstructId> cids = null;
    cids = this.constructIds.get(_cid.getType());
    if (cids == null) {
      cids = new TreeSet<ConstructId>();
      this.constructIds.put(_cid.getType(), cids);
    }
    cids.add(_cid);
  }

  /**
   * countPack.
   *
   * @return a int.
   */
  @JsonProperty(value = "PACK")
  public int countPack() {
    return (this.constructIds.containsKey(ConstructType.PACK)
        ? this.constructIds.get(ConstructType.PACK).size()
        : 0);
  }

  /**
   * countClass.
   *
   * @return a int.
   */
  @JsonProperty(value = "CLASS")
  public int countClass() {
    return (this.constructIds.containsKey(ConstructType.CLAS)
        ? this.constructIds.get(ConstructType.CLAS).size()
        : 0);
  }

  /**
   * countEnum.
   *
   * @return a int.
   */
  @JsonProperty(value = "ENUM")
  public int countEnum() {
    return (this.constructIds.containsKey(ConstructType.ENUM)
        ? this.constructIds.get(ConstructType.ENUM).size()
        : 0);
  }

  /**
   * countInit.
   *
   * @return a int.
   */
  @JsonProperty(value = "INIT")
  public int countInit() {
    return (this.constructIds.containsKey(ConstructType.INIT)
        ? this.constructIds.get(ConstructType.INIT).size()
        : 0);
  }

  /**
   * countCons.
   *
   * @return a int.
   */
  @JsonProperty(value = "CONS")
  public int countCons() {
    return (this.constructIds.containsKey(ConstructType.CONS)
        ? this.constructIds.get(ConstructType.CONS).size()
        : 0);
  }

  /**
   * countMeth.
   *
   * @return a int.
   */
  @JsonProperty(value = "METH")
  public int countMeth() {
    return (this.constructIds.containsKey(ConstructType.METH)
        ? this.constructIds.get(ConstructType.METH).size()
        : 0);
  }

  /**
   * countModule.
   *
   * @return a int.
   */
  @JsonProperty(value = "MODU")
  public int countModule() {
    return (this.constructIds.containsKey(ConstructType.MODU)
        ? this.constructIds.get(ConstructType.MODU).size()
        : 0);
  }

  /**
   * countFunction.
   *
   * @return a int.
   */
  @JsonProperty(value = "FUNC")
  public int countFunction() {
    return (this.constructIds.containsKey(ConstructType.FUNC)
        ? this.constructIds.get(ConstructType.FUNC).size()
        : 0);
  }

  /**
   * Returns the total number of constructs having one of the following types: {@link
   * ConstructId.ConstructType.METH}, {@link ConstructId.ConstructType.CONS} and {@link
   * ConstructId.ConstructType.INIT}. This are all the types whose execution can be observed during
   * test, and for which reachable is checked.
   *
   * @return a int.
   */
  @JsonProperty(value = "countExecutable")
  public int countExecutable() {
    return this.countMeth() + this.countCons() + this.countInit() + this.countFunction();
  }

  /**
   * Returns the total number constructs.
   *
   * @return a int.
   */
  @JsonProperty(value = "countTotal")
  public int countTotal() {
    return this.countPack()
        + this.countClass()
        + this.countEnum()
        + this.countMeth()
        + this.countCons()
        + this.countInit()
        + this.countModule()
        + this.countFunction();
  }
}
