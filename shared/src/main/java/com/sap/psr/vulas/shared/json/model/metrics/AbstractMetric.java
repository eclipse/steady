package com.sap.psr.vulas.shared.json.model.metrics;

/**
 * <p>Abstract AbstractMetric class.</p>
 *
 */
public abstract class AbstractMetric implements Comparable {

	private String name;
	
	/**
	 * <p>Constructor for AbstractMetric.</p>
	 *
	 * @param _name a {@link java.lang.String} object.
	 */
	protected AbstractMetric(String _name) {
		this.name = _name;
	}
	
	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>Setter for the field <code>name</code>.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/** {@inheritDoc} */
	@Override
	public int compareTo(Object o) {
		if(o instanceof AbstractMetric)
			return this.getName().compareToIgnoreCase(((AbstractMetric)o).getName());
		else
			throw new IllegalArgumentException("Expected object of type [" + AbstractMetric.class.getSimpleName() + "], got [" + o.getClass().getSimpleName() + "]");
	}
}
