package com.sap.psr.vulas.shared.json.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Percentage metrics. Compared to {@link Ratio}, it does not have a counter and total from which
 * the percentage is computed.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Percentage extends AbstractMetric {

  private double percentage;

  /** Constructor for Percentage. */
  public Percentage() {
    this(null, 0);
  }

  /**
   * Constructor for Percentage.
   *
   * @param _name a {@link java.lang.String} object.
   * @param _percentage a double.
   */
  public Percentage(String _name, double _percentage) {
    super(_name);
    this.setPercentage(_percentage);
  }

  /**
   * Sets the metric.
   *
   * @param percentage a double.
   * @throws java.lang.IllegalArgumentException if the value is LT 0 or GT 1
   */
  public void setPercentage(double percentage) throws IllegalArgumentException {
    if (percentage < 0 || percentage > 1)
      throw new IllegalArgumentException("Expected percentage, got [" + percentage + "]");
    this.percentage = percentage;
  }

  /**
   * Getter for the field <code>percentage</code>.
   *
   * @return a double.
   */
  public double getPercentage() {
    return this.percentage;
  }
}
