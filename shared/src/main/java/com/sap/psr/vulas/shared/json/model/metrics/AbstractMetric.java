package com.sap.psr.vulas.shared.json.model.metrics;

public abstract class AbstractMetric implements Comparable {

	private String name;
	
	protected AbstractMetric(String _name) {
		this.name = _name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public int compareTo(Object o) {
		if(o instanceof AbstractMetric)
			return this.getName().compareToIgnoreCase(((AbstractMetric)o).getName());
		else
			throw new IllegalArgumentException("Expected object of type [" + AbstractMetric.class.getSimpleName() + "], got [" + o.getClass().getSimpleName() + "]");
	}
}
