package com.sap.psr.vulas.shared.json.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ratio extends AbstractMetric {
	
	private double count;
	
	private double total;
	
	public Ratio() { this(null, 0, 0); }
	
	public Ratio(String _name) { this(_name, 0, 0); }
	
	/**
	 * 
	 * @param _name
	 * @param _count
	 * @param _total
	 * @throws IllegalArgumentException if count GT total
	 */
	public Ratio(String _name, double _count, double _total) throws IllegalArgumentException {
		super(_name);
		if(_count>_total)
			throw new IllegalArgumentException("Count [" + _count + "] GT total [" + _total + "]");
		this.count = _count;
		this.total = _total;
	}

	public double getCount() {
		return count;
	}
	
	public void incrementCount() {
		this.count = this.count + 1d;
	}

	public void setCount(double count) {
		this.count = count;
	}

	public double getTotal() {
		return total;
	}
	
	public void incrementTotal(double _inc) {
		this.total = this.total + _inc;
	}

	public void incrementTotal() {
		this.total = this.total + 1d;
	}
	
	public void setTotal(double total) {
		this.total = total;
	}
	
	/**
	 * Returns the ratio as percentage, or -1 if total EQ 0.
	 * @return
	 */
	public double getRatio() {
		return (this.getTotal()==0d ? -1d : this.getCount()/this.getTotal());
	}
}
