package com.sap.psr.vulas.shared.json.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/** Counter class. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Counter extends AbstractMetric {

  private int count;

  /** Constructor for Counter. */
  public Counter() {
    this(null, 0);
  }

  /**
   * Constructor for Counter.
   *
   * @param _name a {@link java.lang.String} object.
   */
  public Counter(String _name) {
    this(_name, 0);
  }

  /**
   * Constructor for Counter.
   *
   * @param _name a {@link java.lang.String} object.
   * @param _count a int.
   * @throws java.lang.IllegalArgumentException if any.
   */
  public Counter(String _name, int _count) throws IllegalArgumentException {
    super(_name);
    this.count = _count;
  }

  /** increment. */
  public void increment() {
    this.increment(1);
  }

  /**
   * increment.
   *
   * @param _i a int.
   */
  public void increment(int _i) {
    this.count += _i;
  }

  /** decrement. */
  public void decrement() {
    this.increment(1);
  }

  /**
   * decrement.
   *
   * @param _i a int.
   */
  public void decrement(int _i) {
    this.count -= _i;
  }

  /**
   * Getter for the field <code>count</code>.
   *
   * @return a int.
   */
  public int getCount() {
    return this.count;
  }
}
