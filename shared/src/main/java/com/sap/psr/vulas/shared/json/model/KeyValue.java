package com.sap.psr.vulas.shared.json.model;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * <p>KeyValue class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class KeyValue {
	
	private String key = null;
	private String[] value = null;
	/**
	 * <p>Constructor for KeyValue.</p>
	 *
	 * @param _k a {@link java.lang.String} object.
	 * @param _v an array of {@link java.lang.String} objects.
	 */
	public KeyValue(String _k, String[] _v) {
		this.key = _k;
		this.value = _v.clone();
	}
	/**
	 * <p>Getter for the field <code>key</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getKey() {
		return key;
	}
	/**
	 * <p>Setter for the field <code>key</code>.</p>
	 *
	 * @param k a {@link java.lang.String} object.
	 */
	public void setKey(String k) {
		this.key = k;
	}
	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return an array of {@link java.lang.String} objects.
	 */
	public String[] getValue() {
		return value.clone();
	}
	/**
	 * <p>Setter for the field <code>value</code>.</p>
	 *
	 * @param value an array of {@link java.lang.String} objects.
	 */
	public void setValue(String[] value) {
		this.value = value.clone();
	}
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : Arrays.hashCode(value));
		return result;
	}
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyValue other = (KeyValue) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!Arrays.equals(value, other.value))
			return false;
		return true;
	}
	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(key).append("=");
		if(value==null)
			b.append("<null>");
		else if(value.length==0)
			b.append("<empty>");
		else if(value.length==1)
			b.append(value[0]);
		else {
			b.append("[");
			for(int i=0; i<value.length; i++) {
				if(i>0) b.append(", ");
				b.append(value[i]);
			}
			b.append("]");
		}
		return b.toString();
	}
	/**
	 * <p>toKeyValue.</p>
	 *
	 * @param _cfg a {@link org.apache.commons.configuration.Configuration} object.
	 * @return an array of {@link com.sap.psr.vulas.shared.json.model.KeyValue} objects.
	 */
	public static KeyValue[] toKeyValue(Configuration _cfg) {
		final Set<KeyValue> values = new HashSet<KeyValue>();
		final Iterator<String> iter = _cfg.getKeys();
		String key = null;
		String[] value = null;
		while(iter.hasNext()) {
			key = iter.next();
			if(!(key.equals("vulas.jira.usr")||key.equals("vulas.jira.pwd")))
				values.add(new KeyValue(key, _cfg.getStringArray(key)));
		}
		return values.toArray(new KeyValue[values.size()]);
	}
}
