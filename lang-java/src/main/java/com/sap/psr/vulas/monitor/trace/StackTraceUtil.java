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
package com.sap.psr.vulas.monitor.trace;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.java.JavaClassId;
import com.sap.psr.vulas.java.JavaConstructorId;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.java.JavaMethodId;
import com.sap.psr.vulas.monitor.ClassVisitor;
import com.sap.psr.vulas.monitor.Loader;
import com.sap.psr.vulas.monitor.LoaderHierarchy;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Transforms a stacktrace, obtained from {@link Throwable} as an array of {@link StackTraceElement},
 * into a linked list of corresponding construct identifiers.
 * <p>
 * Analyzes the bytecode in order to find methods in case of method overloading
 * (thereby using the line numbers provided in the stack trace).
 * <p>
 * More information can be found here:
 * http://stackoverflow.com/questions/421280/how-do-i-find-the-caller-of-a-method-using-stacktrace-or-reflection
 * http://stackoverflow.com/questions/15899518/modifying-line-numbers-in-javassist
 * http://stackoverflow.com/questions/1069066/get-current-stack-trace-in-java
 */
public class StackTraceUtil {

    private static Logger log = null;

    private static final Logger getLog() {
        if (StackTraceUtil.log == null)
            StackTraceUtil.log = org.apache.logging.log4j.LogManager.getLogger();
        return StackTraceUtil.log;
    }

    private Loader loader = null;

    private boolean stopAtJUnit = false;

    private static final String ANNO_JUNIT_TEST = "org.junit.Test";

    /** Remember ClassNotFoundExceptions in order to not print them again and again. */
    private static final Set<String> cnfe = new HashSet<String>();

    /** Remember ConstructIds built from stack trace elements. */
    private static final Map<StackTraceElement, ConstructId> constructIds =
            new HashMap<StackTraceElement, ConstructId>();

    /**
     * <p>Constructor for StackTraceUtil.</p>
     */
    public StackTraceUtil() {}

    /**
     * <p>Constructor for StackTraceUtil.</p>
     *
     * @param _hier a {@link com.sap.psr.vulas.monitor.LoaderHierarchy} object.
     * @param _loader a {@link com.sap.psr.vulas.monitor.Loader} object.
     */
    public StackTraceUtil(LoaderHierarchy _hier, Loader _loader) {
        // _hier.logHierarchy(_hier.getRoot(), 0);
        this.loader = _loader;
    }

    /**
     * <p>Setter for the field <code>stopAtJUnit</code>.</p>
     *
     * @param _b a boolean.
     */
    public void setStopAtJUnit(boolean _b) {
        this.stopAtJUnit = _b;
    }

    /**
     * Returns the JUnit test case representing the starting point of a previously computed path.
     * If no JUnit test case is present, the method returns null.
     *
     * @param _path a {@link java.util.List} object.
     * @return a {@link com.sap.psr.vulas.ConstructId} object.
     */
    public ConstructId getJUnitContext(List<PathNode> _path) {
        ConstructId junit = null, node = null;
        final Iterator<PathNode> iter = _path.iterator();
        while (iter.hasNext()) {
            node = iter.next().getConstructId();
            if (node instanceof JavaId) {
                if (((JavaId) node).hasAnnotation(ANNO_JUNIT_TEST)) {
                    junit = node;
                    break;
                }
            }
        }
        return junit;
    }

    /**
     * Transforms the stacktrace (obtained by, for instance, new Throwable().getStackTrace()) into a linked list of construct identifiers.
     *
     * @param _st an array of {@link java.lang.StackTraceElement} objects.
     * @param _change_list_element the construct ID for which the stack trace was collected (if known by caller, can be null)
     * @return a {@link java.util.List} object.
     */
    public List<PathNode> transformStackTrace(
            StackTraceElement[] _st, PathNode _change_list_element) {
        // The path to be constructed and returned
        final List<PathNode> path = new LinkedList<PathNode>();

        // Loop all elements of the stack trace
        boolean cont = true;
        int i = 0;
        while (cont && i < _st.length) {

            // Ignore the first if it is method getStackTrace
            if (i == 0
                    && _st[i].getClassName().equals("java.lang.Thread")
                    && _st[i].getMethodName().equals("getStackTrace")) i++;
            else {
                // If the path is empty, take the change list element (if provided), herewith avoid
                // overloading problems
                if (path.isEmpty() && _change_list_element != null) path.add(_change_list_element);

                // Other elements: Find and add a construct ID for the next stack trace element to
                // <path>
                else cont = this.getConstructId(path, _st[i]);

                i++;
            }
        }

        // Debug information
        if (StackTraceUtil.getLog().isDebugEnabled()) {
            StackTraceUtil.getLog()
                    .debug(
                            "Stacktrace with ["
                                    + _st.length
                                    + "] elements transformed into path with ["
                                    + path.size()
                                    + "] nodes");
            final StringBuilder b = new StringBuilder();
            int j = 0, k = path.size() - 1;
            while (j < _st.length) {
                b.delete(0, b.length());
                b.append("    ").append(_st[j].toString());
                if (j == 0
                        && _st[j].getClassName().equals("java.lang.Thread")
                        && _st[j].getMethodName().equals("getStackTrace")) {
                    j++;
                } else {
                    j++;
                    if (k >= 0) {
                        b.append(" --> ").append(path.get(k).toString());
                        k--;
                    }
                }
                StackTraceUtil.getLog().debug(b.toString());
            }
        }

        return path;
    }

    /**
     *
     * @param _path
     * @param _e
     * @param _loader
     * @return true if the path construction shall continue
     */
    private boolean getConstructId(List<PathNode> _path, StackTraceElement _e) {

        // To be returned, determines whether the path construction continues after the current
        // element
        boolean cont = true;

        // The one or multiple construct IDs found for <_e>
        List<ConstructId> construct_ids = new ArrayList<ConstructId>();

        // Classloader
        ClassLoader cl = null;
        if (this.loader != null) cl = this.loader.getClassLoader();
        else cl = this.getClass().getClassLoader();

        Class c = null;
        JavaMethodId meth_cid = null;
        JavaConstructorId cons_cid = null;

        try {
            // Get the Java class definition, break if that does not work
            c = cl.loadClass(_e.getClassName());

            // To be built from the
            String qname = null;

            // Constructor
            if (_e.getMethodName().equals("<init>")) {
                for (Constructor con : c.getDeclaredConstructors()) {
                    // Cleanup the constructor's string representation for parsing
                    qname = con.toString();
                    qname = qname.substring(0, qname.lastIndexOf(")") + 1);
                    if (qname.indexOf(" ") != -1)
                        qname = qname.substring(qname.lastIndexOf(" ") + 1);
                    qname = ClassVisitor.removeParameterQualification(qname);

                    try {
                        cons_cid = JavaId.parseConstructorQName(qname);
                        for (Annotation a : con.getAnnotations())
                            cons_cid.addAnnotation(a.annotationType().getName());
                        construct_ids.add(cons_cid);
                    } catch (Exception e) {
                        StackTraceUtil.getLog()
                                .error("Exception while creating construct ID: " + e.getMessage());
                    }
                }

                // There's some likelihood that multiple constructors are defined
                // If we have line number info, use Javassist to find the right one
                if (construct_ids.size() > 1) {
                    if (_e.getLineNumber() <= 0)
                        StackTraceUtil.getLog()
                                .warn(
                                        "Stack trace element w/o line number info, cannot use"
                                                + " Javassist: "
                                                + _e);
                    else {
                        try {
                            final ConstructId the_one = this.filterConstructors(_e, construct_ids);
                            if (the_one != null) {
                                construct_ids.clear();
                                construct_ids.add(the_one);
                            } else {
                                StackTraceUtil.getLog()
                                        .error(
                                                "Could not determine constructor despite line"
                                                        + " information: ["
                                                        + _e.toString()
                                                        + "]");
                            }
                        } catch (NotFoundException e) {
                            StackTraceUtil.getLog()
                                    .error(
                                            "Javassist class not found exception for class ["
                                                    + e.getMessage()
                                                    + "], classloader ["
                                                    + cl.getClass().toString()
                                                    + "]");
                        }
                    }
                }
            }
            // Static initializer
            else if (_e.getMethodName().equals("<clinit>")) {
                final JavaClassId jcid = JavaId.getClassId(c);
                construct_ids.add(jcid.getClassInit());
            }
            // Method
            else {
                for (Method m : c.getDeclaredMethods()) {
                    if (m.getName().equals(_e.getMethodName())) {
                        // Cleanup the method's string representation for parsing
                        qname = m.toString();
                        qname = qname.substring(0, qname.lastIndexOf(")") + 1);
                        if (qname.indexOf(" ") != -1)
                            qname = qname.substring(qname.lastIndexOf(" ") + 1);
                        qname = ClassVisitor.removeParameterQualification(qname);

                        try {
                            meth_cid = JavaId.parseMethodQName(qname);
                            for (Annotation a : m.getAnnotations())
                                meth_cid.addAnnotation(a.annotationType().getName());
                            construct_ids.add(meth_cid);
                        } catch (Exception e) {
                            StackTraceUtil.getLog()
                                    .error(
                                            "Exception while creating construct ID: "
                                                    + e.getMessage());
                        }
                    }
                }

                // If the method is overloaded, multiple methods with the same name are found
                // If we have line number info, use Javassist to find the right one
                if (construct_ids.size() > 1) {
                    // If the path is empty, we can take
                    if (_e.getLineNumber() <= 0)
                        StackTraceUtil.getLog()
                                .warn(
                                        "Stack trace element w/o line number info, cannot use"
                                                + " Javassist: "
                                                + _e);
                    else {
                        try {
                            final ConstructId the_one = this.filterMethods(_e, construct_ids);
                            if (the_one != null) {
                                construct_ids.clear();
                                construct_ids.add(the_one);
                            } else {
                                StackTraceUtil.getLog()
                                        .error(
                                                "Could not determine method despite line"
                                                        + " information: ["
                                                        + _e.toString()
                                                        + "]");
                            }
                        } catch (NotFoundException e) {
                            StackTraceUtil.getLog()
                                    .error(
                                            "Javassist class not found exception for class ["
                                                    + e.getMessage()
                                                    + "], classloader ["
                                                    + cl.getClass().toString()
                                                    + "]");
                        }
                    }
                }
            }

            if (construct_ids.size() == 0) {
                StackTraceUtil.getLog()
                        .warn(
                                "Class ["
                                        + _e.getClassName()
                                        + "] has no method with name ["
                                        + _e.getMethodName()
                                        + "]: Stop path construction");
                cont = false;
            } else if (construct_ids.size() == 1) {
                // StackTraceUtil.getLog().info("    Class [" + ste.getClassName() + "] has 1 method
                // with name [" + ste.getMethodName() + "]");
                _path.add(0, new PathNode(construct_ids.get(0)));
                cont = this.continueStacktraceTransformation((JavaId) construct_ids.get(0));
            } else if (construct_ids.size() > 1) {
                StackTraceUtil.getLog()
                        .warn(
                                "Class ["
                                        + _e.getClassName()
                                        + "] has "
                                        + construct_ids.size()
                                        + " constructs with name ["
                                        + _e.getMethodName()
                                        + "]: Take first");
                _path.add(0, new PathNode(construct_ids.get(0)));
                cont = this.continueStacktraceTransformation((JavaId) construct_ids.get(0));
            }
        } catch (ClassNotFoundException e) {
            if (!StackTraceUtil.cnfe.contains(e.getMessage())) {
                StackTraceUtil.getLog()
                        .warn(
                                "Java class not found exception for class ["
                                        + e.getMessage()
                                        + "], classloader ["
                                        + cl.getClass().toString()
                                        + "]: Stop path construction");
                StackTraceUtil.cnfe.add(e.getMessage());
            }
            cont = false;
        } catch (SecurityException e) {
            StackTraceUtil.getLog()
                    .warn(
                            "Security exception while analyzing class ["
                                    + e.getMessage()
                                    + "]: Stop path construction");
            cont = false;
        } catch (NoClassDefFoundError ncdfe) {
            StackTraceUtil.getLog()
                    .warn(
                            "No class definition exception for class ["
                                    + ncdfe.getMessage()
                                    + "]: Stop path construction");
            cont = false;
        }
        return cont;
    }

    /**
     * Returns true if the given JavaId has the annotation "org.junit.Test" and the stack trace util has been configured to stop at JUnit tests, false otherwise.
     * @param _jid the JavaId whose annotations are checked
     * @return
     */
    private boolean continueStacktraceTransformation(JavaId _jid) {
        boolean cont = true;
        if (_jid == null) cont = false;
        else if (_jid.hasAnnotation(ANNO_JUNIT_TEST)) {
            if (this.stopAtJUnit) {
                StackTraceUtil.getLog()
                        .debug(
                                "Found JUnit test ["
                                        + _jid.getQualifiedName()
                                        + "]: Stop path construction");
                cont = false;
            } else
                StackTraceUtil.getLog().debug("Found JUnit test [" + _jid.getQualifiedName() + "]");
        }
        return cont;
    }

    private ConstructId filterMethods(StackTraceElement _e, List<ConstructId> _methods)
            throws NotFoundException {
        // To be returned
        ConstructId method_found = null;
        final String class_qname =
                ((JavaMethodId) _methods.get(0)).getDefinitionContext().getQualifiedName();

        // Get the CtClass
        ClassPool cp = null;
        CtClass ctclass = null;
        if (this.loader == null) {
            cp = ClassPool.getDefault();
            ctclass = cp.get(class_qname);
        } else {
            boolean search = true;
            Loader l = this.loader;
            NotFoundException nfe = null;
            while (search) {
                try {
                    cp = l.getClassPool();
                    ctclass = cp.get(class_qname);
                    search = false;
                } catch (NotFoundException e) {
                    StackTraceUtil.getLog()
                            .error(
                                    "Class ["
                                            + class_qname
                                            + "] not found with class loader ["
                                            + l
                                            + "]:"
                                            + e.getMessage());
                    if (!l.isRoot()) l = l.getParent();
                    else {
                        search = false;
                        throw e;
                    }
                }
            }
        }

        // Loop all methods of the class having the same name
        int shortest_distance = Integer.MAX_VALUE;
        int current_distance = -1;
        for (ConstructId m : _methods) {
            // Loop all methods of the class and find the one closest to the line number of the
            // stack trace element
            for (CtMethod ctm : ctclass.getDeclaredMethods()) {
                if (m.getQualifiedName()
                                .equals(
                                        ClassVisitor.removeParameterQualification(
                                                ctm.getLongName()))
                        && _e.getLineNumber() >= ctm.getMethodInfo().getLineNumber(0)) {
                    current_distance = _e.getLineNumber() - ctm.getMethodInfo().getLineNumber(0);
                    if (current_distance < shortest_distance) {
                        shortest_distance = current_distance;
                        method_found = m;
                    }
                }
            }
        }

        return method_found;
    }

    private ConstructId filterConstructors(StackTraceElement _e, List<ConstructId> _constructors)
            throws NotFoundException {
        // To be returned
        ConstructId constructor_found = null;

        final SortedMap<Integer, ConstructId> constructor_line_numbers =
                new TreeMap<Integer, ConstructId>();

        // Get the CtClass
        ClassPool cp = null;
        if (this.loader != null) cp = this.loader.getClassPool();
        else cp = ClassPool.getDefault();
        final CtClass ctclass =
                cp.get(
                        ((JavaConstructorId) _constructors.get(0))
                                .getDefinitionContext()
                                .getQualifiedName());

        // Loop all constructors of the class and find the one closest to the line number of the
        // stack trace element
        int shortest_distance = Integer.MAX_VALUE;
        int current_distance = -1;
        ConstructId c = null;

        for (CtConstructor ctm : ctclass.getDeclaredConstructors()) {
            c =
                    JavaId.parseConstructorQName(
                            ClassVisitor.removeParameterQualification(ctm.getLongName()));
            constructor_line_numbers.put(new Integer(ctm.getMethodInfo().getLineNumber(0)), c);
            if (_e.getLineNumber() >= ctm.getMethodInfo().getLineNumber(0)) {
                current_distance = _e.getLineNumber() - ctm.getMethodInfo().getLineNumber(0);
                if (current_distance < shortest_distance) {
                    shortest_distance = current_distance;
                    constructor_found = c;
                }
            }
        }

        // Print if no constructor was found (wrong line number in stacktrace?)
        // 2 examples from commons-compress 1.4:
        // [main] WARN  com.sap.psr.vulas.monitor.StackTraceUtil  - None of the constructors at line
        // numbers [132,144,159] matched the stack trace element
        // [org.apache.commons.compress.archivers.zip.ZipArchiveInputStream.<init>(ZipArchiveInputStream.java:87)], return constructor with smallest line number
        // [main] WARN  com.sap.psr.vulas.monitor.StackTraceUtil  - None of the constructors at line
        // numbers [97,111,136,145,158] matched the stack trace element
        // [org.apache.commons.compress.archivers.zip.ZipArchiveEntry.<init>(ZipArchiveEntry.java:86)], return constructor with smallest line number
        if (constructor_found == null && !constructor_line_numbers.isEmpty()) {
            constructor_found =
                    constructor_line_numbers.get(
                            constructor_line_numbers.keySet().iterator().next());
            StackTraceUtil.getLog()
                    .debug(
                            "None of the constructors at line numbers ["
                                    + StringUtils.join(constructor_line_numbers.keySet(), ',')
                                    + "] matched the stack trace element ["
                                    + _e
                                    + "], return constructor with smallest line number");
        }

        return constructor_found;
    }

    /**
     * Given a stacktrace object return the predecessor of the element that called this stacktrace.
     *
     * @param _st an array of {@link java.lang.StackTraceElement} objects.
     * @return a {@link com.sap.psr.vulas.ConstructId} object.
     */
    public ConstructId getPredecessorConstruct(StackTraceElement[] _st) {
        // StackTraceUtil.constructIds can be null if the method is called a second time during
        // instantiation
        if (_st.length > 2 && StackTraceUtil.constructIds != null) {
            // We already searched in the past
            if (StackTraceUtil.constructIds.containsKey(_st[1])) {
                return StackTraceUtil.constructIds.get(_st[1]); // Can be null
            }

            // We did not search before, search now
            else {
                final List<PathNode> path = new LinkedList<PathNode>();
                this.getConstructId(path, _st[1]);
                // We were able to create a constructId for the predecessor
                if (path.size() > 0) {
                    final ConstructId cid = path.get(0).getConstructId();
                    StackTraceUtil.constructIds.put(_st[1], cid);
                    return cid;
                }
                // We failed, e.g., because the class could not be found
                else {
                    StackTraceUtil.constructIds.put(_st[1], null);
                    // StackTraceUtil.getLog().error("Could not create construct from stack trace
                    // element [" + _st[1] + "], which is the predecessor of [" + _st[0] + "]");
                    return null;
                }
            }
        }
        return null;
    }
}
