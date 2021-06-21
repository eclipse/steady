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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.java.monitor.touch;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.ConstructId;
import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.backend.BackendConnector;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.java.JavaClassInit;
import org.eclipse.steady.java.JavaConstructorId;
import org.eclipse.steady.java.JavaId;
import org.eclipse.steady.java.JavaMethodId;
import org.eclipse.steady.java.monitor.ClassVisitor;

/**
 * <p>ConstructIdUtil class.</p>
 */
public class ConstructIdUtil {

  private static Logger log = null;

  private static final Logger getLog() {
    if (ConstructIdUtil.log == null)
      ConstructIdUtil.log = org.apache.logging.log4j.LogManager.getLogger();
    return ConstructIdUtil.log;
  }

  private static ConstructIdUtil instance = null;

  private Set<ConstructId> appConstructs = null;

  private ConstructIdUtil() {
    try {
      final Set<org.eclipse.steady.shared.json.model.ConstructId> app_constructs =
          BackendConnector.getInstance()
              .getAppConstructIds(
                  CoreConfiguration.buildGoalContextFromGlobalConfiguration(),
                  CoreConfiguration.getAppContext());
      this.appConstructs = new HashSet<ConstructId>();
      for (org.eclipse.steady.shared.json.model.ConstructId cid : app_constructs) {
        this.appConstructs.add(JavaId.toCoreType(cid));
      }
    } catch (ConfigurationException e) {
      ConstructIdUtil.getLog().error(e.getMessage(), e);
    } catch (BackendConnectionException e) {
      ConstructIdUtil.getLog().error(e.getMessage(), e);
    }
  }

  /**
   * <p>Getter for the field <code>instance</code>.</p>
   *
   * @return a {@link org.eclipse.steady.java.monitor.touch.ConstructIdUtil} object.
   */
  public static synchronized ConstructIdUtil getInstance() {
    if (ConstructIdUtil.instance == null) ConstructIdUtil.instance = new ConstructIdUtil();
    return ConstructIdUtil.instance;
  }

  /**
   * Checks whether the given {@link ConstructId} is part of the application under analysis.
   * The check is implemented by looing at the definition context, which should be either
   * class or enum. The reason is that, e.g., static initializers are not considered at
   * the time of the source code analysis, hence, are not part of the collection.
   *
   * @param _jid a {@link org.eclipse.steady.ConstructId} object.
   * @return a boolean.
   */
  public boolean isAppConstruct(ConstructId _jid) {
    boolean is_app_construct = false;

    // We only instrument clinit, methods and constructors
    if (ConstructIdUtil.isOfInstrumentableType(_jid)) {
      final ConstructId context = _jid.getDefinitionContext();
      is_app_construct = this.appConstructs != null && this.appConstructs.contains(context);
    } else {
      ConstructIdUtil.getLog()
          .error("Expected <CLINIT>, method or constructor, got [" + _jid.toString() + "]");
    }

    return is_app_construct;
  }

  /**
   * Returns true if the given {@link ConstructId} is neither part of the application nor a test method, false otherwise.
   *
   * @param _jid a {@link org.eclipse.steady.ConstructId} object.
   * @return a boolean.
   */
  public boolean isLibConstruct(ConstructId _jid) {
    boolean is_lib_construct = false;

    // We only instrument clinit, methods and constructors
    if (ConstructIdUtil.isOfInstrumentableType(_jid)) {
      final ConstructId context = _jid.getDefinitionContext();
      is_lib_construct = this.appConstructs != null && !this.appConstructs.contains(context);

      // Not part of the app, now check that is not a JUnit test method of the app
      if (is_lib_construct) {
        if (_jid instanceof JavaMethodId) {
          is_lib_construct = !((JavaMethodId) _jid).isTestMethod();
        }
      }
    } else {
      ConstructIdUtil.getLog()
          .error("Expected <CLINIT>, method or constructor, got [" + _jid.toString() + "]");
    }

    return is_lib_construct;
  }

  /**
   * Returns true if the given {@link JavaId} is an instance of {@link JavaClassInit}, {@link JavaMethodId} or
   * {@link JavaConstructorId}, false otherwise.
   *
   * @param _jid a {@link org.eclipse.steady.ConstructId} object.
   * @return a boolean.
   */
  public static boolean isOfInstrumentableType(ConstructId _jid) {
    return _jid instanceof JavaClassInit
        || _jid instanceof JavaConstructorId
        || _jid instanceof JavaMethodId;
  }

  /**
   * Given a qualified name return the ConstructId that represent it. For now is
   * implemented only on constructors and method (also including &lt;clinit&gt; and &lt;init&gt;).
   * If the type requested is null or is not in the range of teh allowed types the method return null
   *
   * @param _qname the qname of the construct.
   * @param type can be CONSTRUCTOR,CLASSINIT,METHOD,CLASS,NESTED_CLASS
   * @return Return the java representation of this constructid or null if is not found
   */
  public ConstructId getConstructidFromQName(String _qname, String type) {
    if (_qname == null || type == null) return null;
    // Constructor
    if (_qname.contains("<init>") || type.contains("CONSTRUCTOR")) {
      _qname = ClassVisitor.removeParameterQualification(_qname);
      return JavaId.parseConstructorQName(_qname);
    }
    // Static initializer
    else if (_qname.contains("<clinit>") || type.contains("CLASSINIT")) {
      return JavaId.parseClassInitQName(_qname.substring(0, _qname.lastIndexOf('>') + 1));
    }
    // Method
    else if (type.contains("METHOD")) {
      // Cleanup the method's string representation for parsing
      _qname = ClassVisitor.removeParameterQualification(_qname);
      return JavaId.parseMethodQName(_qname);
    } else if (type.contains("CLASS") || type.contains("NESTED_CLASS")) {
      return JavaId.parseClassQName(_qname);
    } else if (type.contains("ENUM")) {
      return JavaId.parseEnumQName(_qname);
    } else return null;
  }
}
