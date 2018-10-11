package com.sap.psr.vulas.shared.json.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Counter extends AbstractMetric {

	private int count;
	
	public Counter() { this(null, 0); }
	
	public Counter(String _name) { this(_name, 0); }
	
	public Counter(String _name, int _count) throws IllegalArgumentException {
		super(_name);
		this.count = _count;
	}

	public void increment() { this.increment(1); }
	
	public void increment(int _i) { this.count += _i; }
	
	public void decrement() { this.increment(1); }
	
	public void decrement(int _i) { this.count -= _i; }
	
	public int getCount() { return this.count; }
}
