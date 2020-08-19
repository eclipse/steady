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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.goals.AbstractGoal;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.monitor.AbstractInstrumentor;
import com.sap.psr.vulas.monitor.ClassVisitor;
import com.sap.psr.vulas.monitor.DynamicTransformer;
import com.sap.psr.vulas.monitor.IInstrumentor;

import javassist.CtBehavior;

/**
 * Common super class of the two trace instrumentors {@link SingleTraceInstrumentor} and
 * {@link StackTraceInstrumentor}. Delegates all class to the {@link TraceCollector}.
 */
public abstract class AbstractTraceInstrumentor extends AbstractInstrumentor {

  /** {@inheritDoc} */
  @Override
  public void upladInformation(AbstractGoal _exe, int _batch_size) {
    TraceCollector.getInstance().uploadInformation(_exe, _batch_size);
  }

  /** {@inheritDoc} */
  @Override
  public void awaitUpload() {
    TraceCollector.getInstance().awaitUpload();
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, Long> getStatistics() {
    return TraceCollector.getInstance().getStatistics();
  }

  /**
   * {@inheritDoc}
   *
   * Accepts every class.
   *
   * Note that the instrumentation involves two levels of filtering:
   * First, {@link DynamicTransformer#transform(ClassLoader, String, Class, java.security.ProtectionDomain, byte[])}
   * filters according to class names, JAR names and
   * JAR directory locations. Second, every {@link IInstrumentor} can apply an additional
   * filter in the implementation of this method.
   * Note that
   */
  @Override
  public boolean acceptToInstrument(JavaId _jid, CtBehavior _behavior, ClassVisitor _cv) {
    return true;
  }

  /**
   * Merges the constructs of the map into a single HashSet, i.e., unordered.
   *
   * @param _map a {@link java.util.Map} object.
   * @return a {@link java.util.Set} object.
   */
  public static final Set<ConstructId> merge(
      Map<String, Set<com.sap.psr.vulas.shared.json.model.ConstructId>> _map) {
    return AbstractTraceInstrumentor.merge(_map, false);
  }

  /**
   * Merges the constructs of the map into a single set. Returns a TreeSet if _ordered is equal to true, a HashSet otherwise.
   *
   * @param _map a {@link java.util.Map} object.
   * @return a single set containing all the constructs passed in _map
   * @param _ordered a boolean.
   */
  public static final Set<ConstructId> merge(
      Map<String, Set<com.sap.psr.vulas.shared.json.model.ConstructId>> _map, boolean _ordered) {
    final Set<ConstructId> set =
        (_ordered ? new TreeSet<ConstructId>() : new HashSet<ConstructId>());
    for (Object key : _map.keySet()) {
      for (com.sap.psr.vulas.shared.json.model.ConstructId cid : _map.get(key)) {
        set.add(JavaId.toCoreType(cid));
      }
    }
    return set;
  }
}
