package com.sap.psr.vulas.shared.json.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/** Ratio class. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ratio extends AbstractMetric {

  private double count;

  private double total;

  /** Constructor for Ratio. */
  public Ratio() {
    this(null, 0, 0);
  }

  /**
   * Constructor for Ratio.
   *
   * @param _name a {@link java.lang.String} object.
   */
  public Ratio(String _name) {
    this(_name, 0, 0);
  }

  /**
   * Constructor for Ratio.
   *
   * @param _name a {@link java.lang.String} object.
   * @param _count a double.
   * @param _total a double.
   * @throws java.lang.IllegalArgumentException if count GT total
   */
  public Ratio(String _name, double _count, double _total) throws IllegalArgumentException {
    super(_name);
    if (_count > _total)
      throw new IllegalArgumentException("Count [" + _count + "] GT total [" + _total + "]");
    this.count = _count;
    this.total = _total;
  }

  /**
   * Getter for the field <code>count</code>.
   *
   * @return a double.
   */
  public double getCount() {
    return count;
  }

  /** incrementCount. */
  public void incrementCount() {
    this.count = this.count + 1d;
  }

  /**
   * Setter for the field <code>count</code>.
   *
   * @param count a double.
   */
  public void setCount(double count) {
    this.count = count;
  }

  /**
   * Getter for the field <code>total</code>.
   *
   * @return a double.
   */
  public double getTotal() {
    return total;
  }

  /**
   * incrementTotal.
   *
   * @param _inc a double.
   */
  public void incrementTotal(double _inc) {
    this.total = this.total + _inc;
  }

  /** incrementTotal. */
  public void incrementTotal() {
    this.total = this.total + 1d;
  }

  /**
   * Setter for the field <code>total</code>.
   *
   * @param total a double.
   */
  public void setTotal(double total) {
    this.total = total;
  }

  /**
   * Returns the ratio as percentage, or -1 if total EQ 0.
   *
   * @return a double.
   */
  public double getRatio() {
    return (this.getTotal() == 0d ? -1d : this.getCount() / this.getTotal());
  }
}
