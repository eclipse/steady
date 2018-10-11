package com.sap.psr.vulas.shared.json.model.metrics;

import java.util.Collection;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A collection of different metrics of type {@link Percentage} or {@link Ratio}.
 * 
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metrics {

	private Collection<Ratio> ratios = null;
	
	private Collection<Percentage> percentages = null;
	
	private Collection<Counter> counters = null;
	
	public void addRatio(Ratio _r) {
		if(this.getRatios()==null) {
			this.setRatios(new TreeSet<Ratio>());
		}
		this.getRatios().add(_r);
	}

	public Collection<Ratio> getRatios() { return ratios; }
	public void setRatios(Collection<Ratio> ratios) { this.ratios = ratios; }

	public void addPercentage(Percentage _r) {
		if(this.getPercentages()==null) {
			this.setPercentages(new TreeSet<Percentage>());
		}
		this.getPercentages().add(_r);
	}
	
	public Collection<Percentage> getPercentages() { return this.percentages; }
	public void setPercentages(Collection<Percentage> _percentages) { this.percentages = _percentages; }
	
	public void addCounter(Counter _c) {
		if(this.getCounters()==null) {
			this.setCounters(new TreeSet<Counter>());
		}
		this.getCounters().add(_c);
	}
	
	public Collection<Counter> getCounters() { return this.counters; }
	public void setCounters(Collection<Counter> _counters) { this.counters = _counters; }	
}
