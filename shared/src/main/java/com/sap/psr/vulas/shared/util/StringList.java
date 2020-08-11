/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.shared.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
	 *
	 * @param _itemlist an array of {@link java.lang.String} objects.
	 */
	public StringList(String[] _itemlist) {
		if(_itemlist!=null)
			this.addAll(_itemlist, true);
	}
	
	/**
	 * First trims and then adds all strings contained in argument _itemlist to this {@link StringList}.
	 *
	 * @param _itemlist an array of {@link java.lang.String} objects.
	 * @return a {@link com.sap.psr.vulas.shared.util.StringList} object.
	 */
	public StringList addAll(String[] _itemlist) {
		if(_itemlist!=null)
			this.addAll(_itemlist, true);
		return this;
	}
	
	/**
	 * Adds all strings contained in argument _itemlist to this {@link StringList}. The strings are
	 * trimmed depending on the value of the argument _trim.
	 *
	 * @param _itemlist an array of {@link java.lang.String} objects.
	 * @param _trim a boolean.
	 * @return a {@link com.sap.psr.vulas.shared.util.StringList} object.
	 */
	public StringList addAll(String[] _itemlist, boolean _trim) {
		if(_itemlist!=null)
			for(String item: _itemlist)
				this.add(item, _trim);
		return this;
	}
	
	/**
	 * Splits the given string using the given separator and adds all items to the {@link StringList}.
	 *
	 * @param _itemlist string with items separated by _separator
	 * @param _separator a {@link java.lang.String} object.
	 * @param _trim a boolean.
	 * @return a {@link com.sap.psr.vulas.shared.util.StringList} object.
	 */
	public StringList addAll(String _itemlist, String _separator, boolean _trim) {
		this.addAll(_itemlist.split(_separator), _trim);
		return this;
	}
	
	/**
	 * First trims and then adds the given string to this {@link StringList}.
	 *
	 * @param _item a {@link java.lang.String} object.
	 * @return a {@link com.sap.psr.vulas.shared.util.StringList} object.
	 */
	public StringList add(String _item) {
		this.add(_item, true);
		return this;
	}
	
	/**
	 * Adds the given string to this {@link StringList}. The strings is
	 * trimmed depending on the value of the argument _trim. If the provided
	 * string is null or empty (""), it will not be added.
	 *
	 * @param _item a {@link java.lang.String} object.
	 * @param _trim a boolean.
	 * @return a {@link com.sap.psr.vulas.shared.util.StringList} object.
	 */
	public StringList add(String _item, boolean _trim) {
		if(_item!=null && !_item.equals(""))
			this.itemlist.add( (_trim ? _item.trim() : _item) );
		return this;
	}
	
	/**
	 * <p>iterator.</p>
	 *
	 * @return a {@link java.util.Iterator} object.
	 */
	public Iterator<String> iterator() {
		return this.itemlist.iterator();
	}
	
	/**
	 * Returns true if the argument _value is equal (case sensitive) to any of the
	 * items in this {@link StringList}.
	 *
	 * @param _value a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean contains(String _value) { return this.contains(_value, ComparisonMode.EQUALS, CaseSensitivity.CASE_SENSITIVE); }
	
	/**
	 * Returns true if the argument _value matches any of the items in the list
	 * according to the mode.
	 *
	 * @param _value the value to be checked
	 * @return true if the value is among the list items
	 * @param _mode a {@link com.sap.psr.vulas.shared.util.StringList.ComparisonMode} object.
	 * @param _case_sensitivity a {@link com.sap.psr.vulas.shared.util.StringList.CaseSensitivity} object.
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
	
	/**
	 * <p>isEmpty.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isEmpty() { return this.itemlist.isEmpty(); }
	
	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return "[" + this.toString(", ") + "]";
	}

	/**
	 * <p>toString.</p>
	 *
	 * @param _sep a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String toString(String _sep) {
		final StringBuilder b = new StringBuilder();
		int i = 0;
		for(String item: this.itemlist) {
			if(i++>0) b.append(_sep);
			b.append(item);
		}
		return b.toString();
	}
	
	/**
	 * If matches are to be kept (argument _keep_matches equals true), the method returns a new {@link HashMap} containing all keys of the given map that are contained in this {@link StringList}.
	 * If matches are to be dropped (argument _keep_matches equals false), the method returns a new {@link HashMap} containing all keys of the given map NOT contained in this {@link StringList}.
	 *
	 * @param _in the input map whose keys will be checked
	 * @param _keep_matches decides whether keys are to be kept or dropped
	 * @param _mode a {@link com.sap.psr.vulas.shared.util.StringList.ComparisonMode} object.
	 * @param _case_sensitivity a {@link com.sap.psr.vulas.shared.util.StringList.CaseSensitivity} object.
	 * @return a {@link java.util.HashMap} object.
	 */
	public HashMap<String, String> filter(Map<String, String> _in, boolean _keep_matches, ComparisonMode _mode, CaseSensitivity _case_sensitivity) {
		final HashMap<String, String> out = new HashMap<String, String>();
		for(String key: _in.keySet()) {
			if(this.contains(key, _mode, _case_sensitivity)) {
				if(_keep_matches)
					out.put(key, _in.get(key));
			}
			else if(!_keep_matches) {
				out.put(key, _in.get(key));
			}
		}
		return out;
	}
}
