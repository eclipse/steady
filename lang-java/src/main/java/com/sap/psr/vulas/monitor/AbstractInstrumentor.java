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
package com.sap.psr.vulas.monitor;

import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;

/**
 * <p>Abstract AbstractInstrumentor class.</p>
 *
 */
public abstract class AbstractInstrumentor implements IInstrumentor {

    protected VulasConfiguration vulasConfiguration = new VulasConfiguration();

    /**
     * Adds Java code to determine the URL of the resource from which the given class was loaded, and the class loader which loaded it.
     * This information is available in the Java variables vul_cls_res and vul_cls_ldr.
     *
     * @param _buffer a {@link java.lang.StringBuffer} object.
     * @param _jid a {@link com.sap.psr.vulas.java.JavaId} object.
     * @param _behavior a {@link javassist.CtBehavior} object.
     */
    protected synchronized void injectUrlAndLoader(
            StringBuffer _buffer, JavaId _jid, CtBehavior _behavior) {
        // Only add the following if not yet existing
        final StringBuffer buffer = new StringBuffer();
        buffer.append(
                "Class vul_cls = null;"); // Different techniques will be used to determine the
                                          // current method's class
        buffer.append("ClassLoader vul_cls_ldr = null;");
        buffer.append("java.net.URL vul_cls_res = null;");
        if (_buffer.length() == 0 || _buffer.indexOf(buffer.toString()) == -1)
            _buffer.insert(0, buffer);

        // Get the class object of the current class
        int m = _behavior.getModifiers();
        if (Modifier.isStatic(m)) {
            // See
            // http://stackoverflow.com/questions/8275499/how-to-call-getclass-from-a-static-method-in-java
            // Also see https://jboss-javassist.github.io/javassist/tutorial/tutorial2.html

            // Use MethodHandles.lookup().lookupClass()
            _buffer.append(
                    "if(vul_cls==null) { try {"
                        + " vul_cls=java.lang.invoke.MethodHandles.lookup().lookupClass(); }"
                        + " catch(Exception e) {} }");

            // Option 3) Use fully-qualified class name --> fails for static methods in anonymous
            // classes
            // buffer.append("final Class
            // vul_cls=").append(_jid.getDefinitionContext().getQualifiedName().replace('$',
            // '.')).append(".class;");

            // Results in the runtime call of "javassist.runtime.Desc.getClazz(String)"
            // See https://jboss-javassist.github.io/javassist/tutorial/tutorial2.html
            _buffer.append("if(vul_cls==null) { try { vul_cls=$class; } catch(Exception e) {} }");

            // Results as is in a compile exception, commented out
            // buffer.append("if(vul_cls==null) { try { vul_cls=new
            // Object(){}.getClass().getEnclosingClass(); } catch(Exception e) {} }");
        } else {
            // Results in the runtime call of "this.getClass()"
            // For unknown reasons, the getClass method does sometimes not exist (even though every
            // class should inherit from Object).
            // The lack of the method causes a compile exception, which can be avoided by checking
            // for its presence.
            // Example: [pool-2-thread-1] INFO  com.sap.psr.vulas.monitor.ClassVisitor  - Exception
            // while instrumenting JAVA METH
            // [org.apache.struts2.views.jsp.ParamTag.getBean(ValueStack,HttpServletRequest,HttpServletResponse)]: [source error] getClass() not found in org.apache.struts2.views.jsp.ParamTag
            if (this.hasGetClassMethod(_behavior.getDeclaringClass()))
                _buffer.append(
                        "if(vul_cls==null) { try { vul_cls=$0.getClass(); } catch(Exception e)"
                            + " {} }");

            // Use MethodHandles.lookup().lookupClass()
            _buffer.append(
                    "if(vul_cls==null) { try {"
                        + " vul_cls=java.lang.invoke.MethodHandles.lookup().lookupClass(); }"
                        + " catch(Exception e) {} }");

            // Results in the runtime call of "javassist.runtime.Desc.getClazz(String)"
            _buffer.append("if(vul_cls==null) { try { vul_cls=$class; } catch(Exception e) {} }");
        }

        _buffer.append("if(vul_cls!=null && vul_cls_ldr==null && vul_cls_res==null) {");
        _buffer.append("  vul_cls_ldr = vul_cls.getClassLoader();");
        _buffer.append("  if(vul_cls_ldr!=null)");
        _buffer.append(
                "    vul_cls_res = vul_cls_ldr.getResource(vul_cls.getName().replace('.', '/') +"
                    + " \".class\");");
        _buffer.append("}");
    }

    /**
     * Adds Java code to obtain the stacktrace.
     * This information is available in the Java variable vul_st.
     *
     * @param _buffer a {@link java.lang.StringBuffer} object.
     * @param _jid a {@link com.sap.psr.vulas.java.JavaId} object.
     * @param _behavior a {@link javassist.CtBehavior} object.
     */
    protected synchronized void injectStacktrace(
            StringBuffer _buffer, JavaId _jid, CtBehavior _behavior) {
        final String decl = "StackTraceElement[] vul_st = null;";
        if (_buffer.length() == 0 || _buffer.indexOf(decl.toString()) == -1)
            _buffer.insert(0, decl);
        _buffer.append("if(vul_st==null) { vul_st = new Throwable().getStackTrace(); }");
    }

    private boolean hasGetClassMethod(CtClass _c) {
        final CtMethod[] methods = _c.getMethods();
        for (CtMethod m : methods) {
            if (m.getLongName().equals("java.lang.Object.getClass()")) return true;
        }
        return false;
    }
}
