package com.sap.psr.vulas.shared.json.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * <p>Counter class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Counter extends AbstractMetric {

	private int count;
	
	/**
	 * <p>Constructor for Counter.</p>
	 */
	public Counter() { this(null, 0); }
	
	/**
	 * <p>Constructor for Counter.</p>
	 *
	 * @param _name a {@link java.lang.String} object.
	 */
	public Counter(String _name) { this(_name, 0); }
	
	/**
	 * <p>Constructor for Counter.</p>
	 *
	 * @param _name a {@link java.lang.String} object.
	 * @param _count a int.
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public Counter(String _name, int _count) throws IllegalArgumentException {
		super(_name);
		this.count = _count;
	}

	/**
	 * <p>increment.</p>
	 */
	public void increment() { this.increment(1); }
	
	/**
	 * <p>increment.</p>
	 *
	 * @param _i a int.
	 */
	public void increment(int _i) { this.count += _i; }
	
	/**
	 * <p>decrement.</p>
	 */
	public void decrement() { this.increment(1); }
	
	/**
	 * <p>decrement.</p>
	 *
	 * @param _i a int.
	 */
	public void decrement(int _i) { this.count -= _i; }
	
	/**
	 * <p>Getter for the field <code>count</code>.</p>
	 *
	 * @return a int.
	 */
	public int getCount() { return this.count; }
}
