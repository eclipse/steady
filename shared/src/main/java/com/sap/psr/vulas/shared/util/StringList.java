package com.sap.psr.vulas.shared.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Can be used for white- and blacklisting {@link String}s. Supports different modes
 * for comparing a to-be-checked value with the list items, either the entire string or its 
 * beginning/end. The latter is used, e.g., for blacklisting classes that belong to certain packages.
 * <p>
 * List items are trimmed by default, unless otherwise specified using the methods
 * {@link StringList#add(String, boolean)} or {@link StringList#addAll(String[], boolean)}.
 */
public class StringList implements Iterable<String> {

	public enum ComparisonMode { EQUALS, STARTSWITH, ENDSWITH, PATTERN };
	
	public enum CaseSensitivity { CASE_SENSITIVE, CASE_INSENSITIVE };
	
	private Set<String> itemlist = new HashSet<String>();
	
	/**
	 * Creates a new instance with an empty item list.
	 */
	public StringList() { this(null); }
		
	/**
	 * Creates a new instance with the give item list. Strings of the item list will be trimmed before
	 * being added to the new {@link StringList}.
	 * @param _itemlist
	 * @param _is_blacklist
	 */
	public StringList(String[] _itemlist) {
		if(_itemlist!=null)
			this.addAll(_itemlist, true);
	}
	
	/**
	 * First trims and then adds all strings contained in argument _itemlist to this {@link StringList}.
	 * @param _itemlist
	 */
	public StringList addAll(String[] _itemlist) {
		if(_itemlist!=null)
			this.addAll(_itemlist, true);
		return this;
	}
	
	/**
	 * Adds all strings contained in argument _itemlist to this {@link StringList}. The strings are
	 * trimmed depending on the value of the argument _trim.
	 * @param _itemlist
	 * @param _trim
	 */
	public StringList addAll(String[] _itemlist, boolean _trim) {
		if(_itemlist!=null)
			for(String item: _itemlist)
				this.add(item, _trim);
		return this;
	}
	
	/**
	 * Splits the given string using the given separator and adds all items to the {@link StringList}.
	 * @param _itemlist string with items separated by _separator
	 * @param _separator
	 * @param _trim
	 */
	public StringList addAll(String _itemlist, String _separator, boolean _trim) {
		this.addAll(_itemlist.split(_separator), _trim);
		return this;
	}
	
	/**
	 * First trims and then adds the given string to this {@link StringList}.
	 * @param _item
	 */
	public StringList add(String _item) {
		this.add(_item, true);
		return this;
	}
	
	/**
	 * Adds the given string to this {@link StringList}. The strings is
	 * trimmed depending on the value of the argument _trim. If the provided
	 * string is null or empty (""), it will not be added.
	 * @param _item
	 */
	public StringList add(String _item, boolean _trim) {
		if(_item!=null && !_item.equals(""))
			this.itemlist.add( (_trim ? _item.trim() : _item) );
		return this;
	}
	
	public Iterator<String> iterator() {
		return this.itemlist.iterator();
	}
	
	/**
	 * Returns true if the argument _value is equal (case sensitive) to any of the
	 * items in this {@link StringList}.
	 * @param _value
	 * @return
	 */
	public boolean contains(String _value) { return this.contains(_value, ComparisonMode.EQUALS, CaseSensitivity.CASE_SENSITIVE); }
	
	/**
	 * Returns true if the argument _value matches any of the items in the list
	 * according to the mode.
	 * @param _value the value to be checked
	 * @return true if the value is among the list items
	 */
	public boolean contains(String _value, ComparisonMode _mode, CaseSensitivity _case_sensitivity) {
		boolean r = false;
		
		// The strings to be compared
		final String value = (_case_sensitivity==CaseSensitivity.CASE_INSENSITIVE ? _value.toLowerCase() : _value);
		String item  = null;
		
		for(String i: this.itemlist) {
			item = (_case_sensitivity==CaseSensitivity.CASE_INSENSITIVE ? i.toLowerCase() : i);			
			switch(_mode) {
				case EQUALS: if(value.equals(item)) return true; else break;
				case STARTSWITH: if(value.startsWith(item)) return true; else break;
				case ENDSWITH: if(value.endsWith(item)) return true; else break;
				case PATTERN:
					if(value.matches(item))
						return true;
					else break;
			}
		}
		return r;
	}
	
	public boolean isEmpty() { return this.itemlist.isEmpty(); }
	
	public String toString() {
		return "[" + this.toString(", ") + "]";
	}

	public String toString(String _sep) {
		final StringBuilder b = new StringBuilder();
		int i = 0;
		for(String item: this.itemlist) {
			if(i++>0) b.append(_sep);
			b.append(item);
		}
		return b.toString();
	}
}
