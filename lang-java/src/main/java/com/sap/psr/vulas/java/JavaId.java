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
package com.sap.psr.vulas.java;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.Logger;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.monitor.ClassPoolUpdater;
import com.sap.psr.vulas.monitor.ClassVisitor;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.JsonBuilder;


/**
 * <p>Abstract JavaId class.</p>
 *
 */
public abstract class JavaId extends ConstructId {

	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(JavaId.class);

	/** Supported Java programming constructs. */
	public static enum Type { PACKAGE, CLASS, ENUM, INTERFACE, NESTED_CLASS, CONSTRUCTOR, METHOD, CLASSINIT };

	/** Programming language of the construct. */
	protected Type type = null;

	private Set<String> annotations = new HashSet<String>();

	/**
	 * <p>Constructor for JavaId.</p>
	 *
	 * @param _t a {@link com.sap.psr.vulas.java.JavaId.Type} object.
	 */
	protected JavaId(Type _t) {
		super(ProgrammingLanguage.JAVA);
		this.type = _t;	
	}
	/**
	 * <p>Getter for the field <code>type</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.java.JavaId.Type} object.
	 */
	public Type getType() { return this.type; }

	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() { return this.getLanguage() + " " + JavaId.typeToString(this.type) + " [" + this.getQualifiedName() + "]"; }

	/**
	 * <p>Getter for the field <code>annotations</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<String> getAnnotations() { return this.annotations; }

	/**
	 * <p>addAnnotation.</p>
	 *
	 * @param _a a {@link java.lang.String} object.
	 */
	public void addAnnotation(String _a) { this.annotations.add(_a); }

	/**
	 * <p>hasAnnotation.</p>
	 *
	 * @param _a a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean hasAnnotation(String _a) { return this.annotations.contains(_a); }

	/**
	 * Returns the Java package in the context of which the construct is defined.
	 *
	 * @return a {@link com.sap.psr.vulas.java.JavaPackageId} object.
	 */
	public abstract JavaPackageId getJavaPackageId();

	/**
	 * <p>getDefinitionContext.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.ConstructId} object.
	 */
	public abstract ConstructId getDefinitionContext();
	
	/**
	 * <p>getSharedType.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.enums.ConstructType} object.
	 */
	public ConstructType getSharedType() {
		return JavaId.toSharedType(this.getType());
	}

	/**
	 * <p>toJSON.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public final String toJSON() {
		final JsonBuilder jb = new JsonBuilder();
		jb.startObject();
		jb.appendObjectProperty("lang", this.getLanguage().toString());
		jb.appendObjectProperty("type", JavaId.typeToString(this.getType()));
		jb.appendObjectProperty("qname", this.getQualifiedName());
		if(!this.annotations.isEmpty()) {
			jb.startArrayProperty("a");
			for(String a: this.annotations) {
				jb.appendToArray(a);
			}
			jb.endArray();
		}
		jb.endObject();
		return jb.getJson();
	}

	/** {@inheritDoc} */
	@Override
	public JsonObject toGSON(){
		final JsonObject jb = new JsonObject();
		jb.addProperty("lang", this.getLanguage().toString());
		jb.addProperty("type", JavaId.typeToString(this.getType()));
		jb.addProperty("qname", this.getQualifiedName());
		jb.add("a", new Gson().toJsonTree(this.annotations));
		return jb;
	}

	/** {@inheritDoc} */
	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + (this.getQualifiedName().hashCode());
		return result;
	}
	/** {@inheritDoc} */
	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaId other = (JavaId) obj;
		if (type != other.type)
			return false;
		if (!this.getQualifiedName().equals(other.getQualifiedName()))
			return false;
		return true;
	}

	/**
	 * <p>parameterTypesToString.</p>
	 *
	 * @return Comma-separated parameter types
	 * @param _p a {@link java.util.List} object.
	 */
	protected static String parameterTypesToString(List<String> _p) {
		return JavaId.parameterTypesToString(_p, false);
	}

	/**
	 * <p>parameterTypesToString.</p>
	 *
	 * @return Comma-separated parameter types
	 * @param _p a {@link java.util.List} object.
	 * @param _rem_qualification a boolean.
	 */
	protected static String parameterTypesToString(List<String> _p, boolean _rem_qualification) {
		if(_p==null || _p.isEmpty()) return "()";
		final StringBuffer str = new StringBuffer();
		str.append("(");
		for(int i=0; i<_p.size(); i++) {
			if(_rem_qualification)
				str.append(JavaId.removePackageContext(_p.get(i)));
			else
				str.append(_p.get(i));
			if(i<_p.size()-1) str.append(",");
		}
		str.append(")");
		return str.toString();
	}

	/**
	 * <p>typeToString.</p>
	 *
	 * @param _t a {@link com.sap.psr.vulas.java.JavaId.Type} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String typeToString(Type _t) {
		switch(_t) {
		case PACKAGE:       return "PACK";
		case CLASS:         return "CLAS";
		case ENUM:          return "ENUM";
		case INTERFACE:		return "INTF";
		case NESTED_CLASS:  return "NCLA";
		case CONSTRUCTOR:   return "CONS";
		case METHOD:        return "METH";
		case CLASSINIT:     return "INIT";
		default:			throw new IllegalArgumentException("Unknown type [" + _t + "]");
		}
	}

	/**
	 * <p>typeFromString.</p>
	 *
	 * @param _t a {@link java.lang.String} object.
	 * @return a {@link com.sap.psr.vulas.java.JavaId.Type} object.
	 */
	public static Type typeFromString(String _t) {
		if("PACK".equalsIgnoreCase(_t))
			return Type.PACKAGE;
		else if("CLAS".equalsIgnoreCase(_t))
			return Type.CLASS;
		else if("ENUM".toString().equalsIgnoreCase(_t))
			return Type.ENUM;
		else if("INTF".toString().equalsIgnoreCase(_t))
			return Type.INTERFACE;
		else if("NCLA".toString().equalsIgnoreCase(_t))
			return Type.NESTED_CLASS;
		else if("CONS".toString().equalsIgnoreCase(_t))
			return Type.CONSTRUCTOR;
		else if("METH".toString().equalsIgnoreCase(_t))
			return Type.METHOD;
		else if("INIT".toString().equalsIgnoreCase(_t))
			return Type.CLASSINIT;
		else
			throw new IllegalArgumentException("Unknown type [" + _t + "]");
	}

	/**
	 * Transforms an object with a given core type (defined in vulas-core) into
	 * an object having the corresponding shared type (defined in vulas-share).
	 *
	 * @param _core_type a {@link com.sap.psr.vulas.java.JavaId.Type} object.
	 * @return a {@link com.sap.psr.vulas.shared.enums.ConstructType} object.
	 */
	public static com.sap.psr.vulas.shared.enums.ConstructType toSharedType(JavaId.Type _core_type) {
		switch(_core_type) {
			case METHOD: return ConstructType.METH;
			case CONSTRUCTOR: return ConstructType.CONS;
			case PACKAGE: return ConstructType.PACK;
			case CLASSINIT: return ConstructType.INIT;
			case ENUM: return ConstructType.ENUM;
			case CLASS: return ConstructType.CLAS;
			default: throw new IllegalArgumentException("Unknown type [" + _core_type + "]");
		}
	}
	
	/**
	 * Transforms an object with a given shared type (defined in vulas-share) into
	 * an object having the corresponding core type (defined in vulas-core).
	 *
	 * @param _cid a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
	 * @return a {@link com.sap.psr.vulas.ConstructId} object.
	 */
	public static com.sap.psr.vulas.ConstructId toCoreType(com.sap.psr.vulas.shared.json.model.ConstructId _cid) {
		switch(_cid.getType()) {
			case METH: return JavaId.parseMethodQName(_cid.getQname());
			case CONS: return JavaId.parseConstructorQName(_cid.getQname());
			case PACK: return new JavaPackageId(_cid.getQname());
			case INIT: return JavaId.parseClassInitQName(_cid.getQname());
			case ENUM: return JavaId.parseEnumQName(_cid.getQname());
			case CLAS: return JavaId.parseClassQName(_cid.getQname());
			default: throw new IllegalArgumentException("Unknown type [" + _cid.getType() + "]");
		}
	}
	
	/**
	 * Transforms a collection of objects with a given shared type (defined in vulas-share) into
	 * a {@link HashSet} of objects having the corresponding core type (defined in vulas-core).
	 *
	 * @param _cids a {@link java.util.Collection} object.
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<com.sap.psr.vulas.ConstructId> toCoreType(Collection<com.sap.psr.vulas.shared.json.model.ConstructId> _cids) {
		final Set<com.sap.psr.vulas.ConstructId> cids = new HashSet<com.sap.psr.vulas.ConstructId>();
		for(com.sap.psr.vulas.shared.json.model.ConstructId cid: _cids)
			cids.add(JavaId.toCoreType(cid));
		return cids;
	}

	/**
	 * Returns a {@link JavaClassId} for the given {@link Class}.
	 *
	 * @param _c a {@link java.lang.Class} object.
	 * @return a {@link com.sap.psr.vulas.java.JavaClassId} object.
	 */
	public static JavaClassId getClassId(@NotNull Class _c) {
		return JavaId.parseClassQName(_c.getName());
	}

	/**
	 * <p>parseEnumQName.</p>
	 *
	 * @param _s a {@link java.lang.String} object.
	 * @return a {@link com.sap.psr.vulas.java.JavaEnumId} object.
	 */
	public static JavaEnumId parseEnumQName(@NotNull String _s) {
		if(_s == null || _s.equals("")) throw new IllegalArgumentException("String null or empty");

		final int i = _s.lastIndexOf('.');
		String class_name = null;
		JavaPackageId pid = null;
		JavaEnumId cid = null;

		// Create Java package id
		if(i!=-1) {
			pid = new JavaPackageId(_s.substring(0, i));
			class_name = _s.substring(i+1);
		}
		else {
			pid = JavaPackageId.DEFAULT_PACKAGE;
			class_name = _s.substring(i+1);
		}

		// Create Java class id (for regular or nested class)
		int j=0, k=0;
		do {
			//HP, 12.09.2015: $ is also permitted in class names, i.e., it can also exist w/o a class context
			j=class_name.indexOf("$", k);
			if(j!=-1) {
				if(cid==null)
					cid = new JavaEnumId(pid, class_name.substring(k, j));
				else
					cid = new JavaEnumId(cid, class_name.substring(k, j));
				k = j+1;
			}
			else {
				if(cid==null)
					cid = new JavaEnumId(pid, class_name.substring(k));
				else
					cid = new JavaEnumId(cid, class_name.substring(k));
				k = j+1;
			}
		}
		while(j!=-1);
		//		if(j!=-1)
		//			cid = new JavaClassId(new JavaClassId(pid, class_name.substring(0, j)), class_name.substring(j+1));
		//		else
		//			cid = new JavaClassId(pid, class_name);
		return cid;
	}

	/**
	 * <p>parseClassQName.</p>
	 *
	 * @param _s a {@link java.lang.String} object.
	 * @return a {@link com.sap.psr.vulas.java.JavaClassId} object.
	 */
	public static JavaClassId parseClassQName(@NotNull String _s) {
		if(_s == null || _s.equals("")) throw new IllegalArgumentException("String null or empty");

		final int i = _s.lastIndexOf('.');
		String class_name = null;
		JavaPackageId pid = null;
		JavaClassId cid = null;

		// Create Java package id
		if(i!=-1) {
			pid = new JavaPackageId(_s.substring(0, i));
			class_name = _s.substring(i+1);
		}
		else {
			pid = JavaPackageId.DEFAULT_PACKAGE;
			class_name = _s.substring(i+1);
		}

		// Create Java class id (for regular or nested class)
		int j=0, k=0;
		do {
			//HP, 12.09.2015: $ is also permitted in class names, i.e., it can also exist w/o a class context
			j=class_name.indexOf("$", k);
			if(j!=-1) {
				if(cid==null)
					cid = new JavaClassId(pid, class_name.substring(k, j));
				else
					cid = new JavaClassId(cid, class_name.substring(k, j));
				k = j+1;
			}
			else {
				if(cid==null)
					cid = new JavaClassId(pid, class_name.substring(k));
				else
					cid = new JavaClassId(cid, class_name.substring(k));
				k = j+1;
			}
		}
		while(j!=-1);
		//		if(j!=-1)
		//			cid = new JavaClassId(new JavaClassId(pid, class_name.substring(0, j)), class_name.substring(j+1));
		//		else
		//			cid = new JavaClassId(pid, class_name);
		return cid;
	}

	/**
	 * Creates a {@link JavaMethodId} from the given string, whereby the definition context is defaulted to
	 * type {@link JavaId.Type#CLASS}.
	 *
	 * @param _s a {@link java.lang.String} object.
	 * @return a {@link com.sap.psr.vulas.java.JavaMethodId} object.
	 */
	public static JavaMethodId parseMethodQName(String _s) { return JavaId.parseMethodQName(JavaId.Type.CLASS, _s); }

	/**
	 * Creates a {@link JavaMethodId} from the given string, with a definition context of the given type.
	 * Accepted types are {@link JavaId.Type#CLASS} and {@link JavaId.Type#ENUM}.
	 *
	 * @param _s a {@link java.lang.String} object.
	 * @param _ctx_type a {@link com.sap.psr.vulas.java.JavaId.Type} object.
	 * @return a {@link com.sap.psr.vulas.java.JavaMethodId} object.
	 */
	public static JavaMethodId parseMethodQName(JavaId.Type _ctx_type, String _s) {

		if(_s == null || _s.equals("")) throw new IllegalArgumentException("String null or empty");
		if(_ctx_type==null || (!_ctx_type.equals(JavaId.Type.CLASS) && !_ctx_type.equals(JavaId.Type.ENUM))) throw new IllegalArgumentException("Accepts context types CLASS or ENUM, got [" + _ctx_type + "]");

		final int i = _s.indexOf('(');
		if(i==-1 || !_s.endsWith(")")) throw new IllegalArgumentException("String does not contain brackets (), as required for qualified names for Java methods");
		final int j = _s.lastIndexOf('.', i);
		if(j==-1) throw new IllegalArgumentException("String does not contain dot (.), as required for qualified names for Java methods (as to separate class and method name)");		

		JavaId  def_ctx = null;
		JavaMethodId mid = null;
		try {
			if(_ctx_type.equals(JavaId.Type.CLASS))
				def_ctx = JavaId.parseClassQName(_s.substring(0, j));
			else if(_ctx_type.equals(JavaId.Type.ENUM))
				def_ctx = JavaId.parseEnumQName(_s.substring(0, j));

			mid = new JavaMethodId(def_ctx, _s.substring(j+1, i), JavaId.parseParameterTypes(_s.substring(i+1, _s.length()-1)));
		}
		catch(StringIndexOutOfBoundsException e) {
			JavaId.log.error("Exception while parsing the string '" + _s + "'");
		}
		return mid;
	}

	/**
	 * Creates a {@link JavaConstructorId} from the given string, whereby the definition context is defaulted to
	 * type {@link JavaId.Type#CLASS}.
	 *
	 * @param _s a {@link java.lang.String} object.
	 * @return a {@link com.sap.psr.vulas.java.JavaConstructorId} object.
	 */
	public static JavaConstructorId parseConstructorQName(String _s) {
		return JavaId.parseConstructorQName(JavaId.Type.CLASS, _s, null);
	}

	/**
	 * Creates a {@link JavaConstructorId} from the given string, with a definition context of the given type.
	 * Accepted types are {@link JavaId.Type#CLASS} and {@link JavaId.Type#ENUM}.
	 *
	 * If the boolean argument is true, the first constructor parameter will be ignored. This functionality is useful for non-static
	 * inner classes, to which the compiler adds the outer class as first parameter.
	 *
	 * @param _s a {@link java.lang.String} object.
	 * @param _ctx_type a {@link com.sap.psr.vulas.java.JavaId.Type} object.
	 * @param _param_to_skip a {@link java.lang.String} object.
	 * @return a {@link com.sap.psr.vulas.java.JavaConstructorId} object.
	 */
	public static JavaConstructorId parseConstructorQName(JavaId.Type _ctx_type, String _s, String _param_to_skip) {

		if(_s == null || _s.equals("")) throw new IllegalArgumentException("String null or empty");
		if(_ctx_type==null || (!_ctx_type.equals(JavaId.Type.CLASS) && !_ctx_type.equals(JavaId.Type.ENUM))) throw new IllegalArgumentException("Accepts context types CLASS or ENUM, got [" + _ctx_type + "]");

		final int i = _s.indexOf('(');
		if(i==-1 || !_s.endsWith(")")) throw new IllegalArgumentException("String does not contain brackets (), as required for qualified names for Java constructors");

		JavaId  def_ctx = null;
		JavaConstructorId coid = null;
		try {
			if(_ctx_type.equals(JavaId.Type.CLASS))
				def_ctx = JavaId.parseClassQName(_s.substring(0, i));
			else if(_ctx_type.equals(JavaId.Type.ENUM))
				def_ctx = JavaId.parseEnumQName(_s.substring(0, i));

			List<String> params = JavaId.parseParameterTypes(_s.substring(i+1, _s.length()-1));
			if(_param_to_skip!=null) {
				if(params.size()==0) {
					JavaId.log.warn("No parameter to skip in argument [" + _s + "]");
				}
				else if(!params.get(0).endsWith(_param_to_skip)) {
					JavaId.log.warn("First parameter in argument [" + _s + "] does not match the to be skipped parameter [" + _param_to_skip + "]");
				}
				else {
					params.remove(0);
				}
			}
			coid = new JavaConstructorId(def_ctx, params);
		}
		catch(StringIndexOutOfBoundsException e) {
			JavaId.log.error("Exception while parsing the string '" + _s + "'");
		}
		return coid;
	}

	/**
	 * <p>parseClassInitQName.</p>
	 *
	 * @param _s a {@link java.lang.String} object.
	 * @return a {@link com.sap.psr.vulas.java.JavaClassInit} object.
	 */
	public static JavaClassInit parseClassInitQName(String _s) {
		if(_s == null || _s.equals("")) throw new IllegalArgumentException("String null or empty");

		final int i = _s.indexOf(JavaClassInit.NAME);
		if(i==-1) throw new IllegalArgumentException("String does not contain brackets " + JavaClassInit.NAME + ", as required for qualified names for Java class initializers");

		JavaClassInit clinit = null;
		try {
			final JavaClassId cid = JavaId.parseClassQName(_s.substring(0, i-1));
			clinit = cid.getClassInit();
		}
		catch(StringIndexOutOfBoundsException e) {
			JavaId.log.error("Exception while parsing the string '" + _s + "'");
		}
		return clinit;
	}

	private static List<String> parseParameterTypes(String _s) {		
		// The list of parameters and a single parameter added one by one
		final List<String> params = new ArrayList<String>();
		StringBuilder param = new StringBuilder();

		// Loop and distinguish the chars
		final char[] chars = _s.toCharArray();
		int counter = 0;
		for(int i=0; i<chars.length; i++) {
			if(chars[i]=='<') {
				param.append(chars[i]);
				counter++;
			} else if(chars[i]=='>') {
				param.append(chars[i]);
				counter--;
			} else if(chars[i]==',') {
				// Next parameter
				if(counter==0) {
					params.add(param.toString());
					param = new StringBuilder();
				}
				// Same parameter, e.g., as in Map<String,String>
				else {
					param.append(chars[i]);
				}
			} else if(chars[i]==' ') {
				// Don't append spaces
			} else {

				param.append(chars[i]);
			}
		}

		// Last param (if any)
		if(param.length()>0)
			params.add(param.toString());

		return params;		
	}	


	/**
	 * Removes package information (if any) from a fully qualified name, and returns the resulting class (or interface) name.
	 *
	 * @param _qname a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String removePackageContext(String _qname) {
		return ClassVisitor.removePackageContext(_qname);
		// Old implementation
		/*String class_name = _qname;
		int i = class_name.lastIndexOf('$');
		if(i!=-1) class_name = class_name.substring(i+1);
		i = class_name.lastIndexOf('.');
		if(i!=-1) class_name = class_name.substring(i+1);
		return class_name;*/
	}

	/**
	 * Returns all {@link JavaId}s whose qualified name starts with any of the given strings.
	 *
	 * @param _set a {@link java.util.Set} object.
	 * @param _filter an array of {@link java.lang.String} objects.
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<ConstructId> filter(Set<ConstructId> _set, String[] _filter) {
		final Set<ConstructId> set = new HashSet<ConstructId>();
		for(String f: _filter) {
			set.addAll(JavaId.filter(_set, f));
		}
		return set;
	}
	
	/**
	 * Returns all {@link JavaId}s whose qualified name starts with the given string.
	 *
	 * @param _set a {@link java.util.Set} object.
	 * @param _filter a {@link java.lang.String} object.
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<ConstructId> filter(Set<ConstructId> _set, String _filter) {
		final Set<ConstructId> set = new HashSet<ConstructId>();
		for(ConstructId c: _set) {
			if(c.getQualifiedName().startsWith(_filter)) {
				set.add(c);
			}
		}
		return set;
	}

	/**
	 * Returns all {@link JavaId}s of the given type(s).
	 *
	 * @param _set a {@link java.util.Set} object.
	 * @param _filter an array of {@link com.sap.psr.vulas.java.JavaId.Type} objects.
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<ConstructId> filter(Set<ConstructId> _set, JavaId.Type[] _filter) {
		final Set<ConstructId> set = new HashSet<ConstructId>();
		JavaId jid = null;
		for(ConstructId c: _set) {
			if(c instanceof com.sap.psr.vulas.java.JavaId) {
				jid = (JavaId)c;
				for(JavaId.Type t: _filter) {
					if(jid.getType()==t) {
						set.add(jid);
						break;
					}
				}
			}
		}
		return set;
	}

	/**
	 * Returns all {@link JavaMethodId}s and {@link JavaConstructorId}s belonging to the given class.
	 *
	 * @param _set a {@link java.util.Set} object.
	 * @param _context a {@link com.sap.psr.vulas.java.JavaId} object.
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<ConstructId> filterWithContext(Set<ConstructId> _set, JavaId _context) {
		final Set<ConstructId> set = new HashSet<ConstructId>();
		JavaId jid = null;
		for(ConstructId c: _set) {
			if(c instanceof JavaMethodId) {
				if( ((JavaMethodId)c).getDefinitionContext().equals(_context) )
					set.add(c);
			}
			else if(c instanceof JavaConstructorId) {
				if( ((JavaConstructorId)c).getDefinitionContext().equals(_context) )
					set.add(c);
			}
		}
		return set;
	}

	/**
	 * Returns the URL of the JAR from which the construct was loaded, or null if it is a package or has not been loaded from a JAR.
	 * The URL has the form "file:/.../foo.jar", hence, it can be transformed into a {@link URI} and {@link File}.
	 *
	 * First, it uses {@link ClassPoolUpdater#getJarResourcePath(ConstructId)} to search for the JAR URL, hence, {@link ClassPoolUpdater}
	 * should contain all the needed resources. If that fails, it uses {@link Class#forName(String)} and {@link Class#getResource(String)} to search
	 * for the JAR URL.
	 *
	 * @return the URL from which the construct was loaded
	 */
	public URL getJarUrl() {
		// The JAR URL will be searched for the following construct (could be JavaClassId, JavaEnumId or JavaInterfaceId)
		ConstructId cid = this;

		// For methods, constructors, etc., take its definition context
		if(cid instanceof JavaConstructorId ||
				cid instanceof JavaMethodId ||
				cid instanceof JavaClassInit){
			cid = cid.getDefinitionContext();
		}
		// Not possible, we cannot get the JAR URL for a package
		else if(cid instanceof JavaPackageId)
			return null;

		// Use Javassist
		URL url = ClassPoolUpdater.getInstance().getJarResourcePath(cid);
		
		// Use Class.forName (if Javassist did not work)
		if(url==null || !url.toString().startsWith("jar:")) {
			try {
				url = Class.forName(this.getQualifiedName()).getResource('/' + this.getQualifiedName().replace('.', '/') + ".class");
			} catch (ClassNotFoundException e) {
				JavaId.log.error("Class [" + this.getQualifiedName() + "] not found: " + e.getMessage());
			} catch (NoClassDefFoundError ex) {
				JavaId.log.error("No class definition found for [" + this.getQualifiedName() + "]");
			} catch(UnsatisfiedLinkError e){
				JavaId.log.error("UnsatisfiedLinkError for [" + this.getQualifiedName() + "]");
			}
		}
		
		// Transform "jar:file:/.../foo.jar!bar.class" into "file:/.../foo.jar"
		if(url!=null && url.toString().startsWith("jar:")) {			
			try {
				return JavaId.getJarUrl(url);
			} catch (MalformedURLException e) {
				JavaId.log.error("Cannot create URL from [" + url.toString() + "]: " + e.getMessage());
			}
		}
		
		// Return null
		return null;
	}
	
	/**
	 * Transform URLs like "jar:file:/.../foo.jar!bar.class" into "file:/.../foo.jar".
	 *
	 * @param _url a {@link java.net.URL} object.
	 * @return a {@link java.net.URL} object.
	 * @throws java.net.MalformedURLException if any.
	 */
	public final static URL getJarUrl(URL _url) throws MalformedURLException {
		if(_url!=null && _url.toString().startsWith("jar:")) {

			// The following creates problem in cases such as org.eclipse.equinox.p2.jarprocessor_1.0.300.v20130327-2119.jar, where ".jar" occurs two times
			//final String url_string = url.toString().substring(4, url.toString().indexOf(".jar")+4);
			
			String url_string = _url.toString();
			final int last_excl = url_string.lastIndexOf("!");
			url_string = url_string.substring(4, url_string.lastIndexOf(".jar", (last_excl==-1 ? url_string.length() : last_excl)) + 4);
			try {
				return new URL(url_string);
			} catch (MalformedURLException e) {
				throw new MalformedURLException("Cannot create URL from [" + _url.toString() + "]: " + e.getMessage());
			}
		}
		return null;
	}
	
	/**
	 * Returns the {@link JavaId} corresponding to the compilation unit of the construct.
	 *
	 * If the construct is an interface, class or enum, the object is returned as is.
	 * If the construct is a method, constructor or static initializer, its definition context will be returned (hence, an interface, class or enum).
	 * If the construct is a package, null will be returned.
	 *
	 * @return a {@link com.sap.psr.vulas.java.JavaId} object.
	 */
	public JavaId getCompilationUnit () {
		JavaId comp_unit = null;
		if(this instanceof com.sap.psr.vulas.java.JavaConstructorId ||
				this instanceof com.sap.psr.vulas.java.JavaClassInit ||
				this instanceof com.sap.psr.vulas.java.JavaMethodId) {
			comp_unit = (JavaId)this.getDefinitionContext();
		}
		else if(this instanceof com.sap.psr.vulas.java.JavaClassId ||
				this instanceof com.sap.psr.vulas.java.JavaInterfaceId ||
				this instanceof com.sap.psr.vulas.java.JavaEnumId) {
			comp_unit = (JavaId)this;
		}
		else if(this instanceof com.sap.psr.vulas.java.JavaPackageId) {
			JavaId.log.warn("[" + this.getQualifiedName() + "] is a package, thus, has no compilation unit");
		}
		else {
			JavaId.log.error("[" + this.getQualifiedName() + "] is of unknown type (class [" + this.getClass().getName() + "]");
		}
		return comp_unit;
	}
	
	/**
	 * 
	 * @param _jid
	 * @return
	 */
	public static JavaId getCompilationUnit(JavaId _jid) {
		// Got it --> return provided object
		if( (_jid.getType().equals(JavaId.Type.CLASS) && !((JavaClassId)_jid).isNestedClass()) ||
				(_jid.getType().equals(JavaId.Type.INTERFACE) && !((JavaInterfaceId)_jid).isNested()) ||
				(_jid.getType().equals(JavaId.Type.ENUM) && !((JavaEnumId)_jid).isNested()) ) {
			return _jid;
		} else {
			return getCompilationUnit((JavaId)_jid.getDefinitionContext());
		}
	}

	public static JavaId getJavaId(String _type, String _qname) {
		JavaId.Type type = JavaId.typeFromString(_type);

		// Check params
		if(JavaId.Type.METHOD!=type && JavaId.Type.CONSTRUCTOR!=type)
			throw new IllegalArgumentException("Only types METH and CONS are supported, got [" + type + "]");

		// Parse JavaId
		JavaId jid = null;
		if(JavaId.Type.CONSTRUCTOR==type)
			jid = JavaId.parseConstructorQName(_qname);
		else if(JavaId.Type.METHOD==type)
			jid = JavaId.parseMethodQName(_qname);

		return jid;
	}
}
