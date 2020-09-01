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
package org.eclipse.steady.java.monitor.trace;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.steady.ConstructId;
import org.eclipse.steady.goals.AbstractGoal;
import org.eclipse.steady.java.JavaId;
import org.eclipse.steady.java.monitor.AbstractInstrumentor;
import org.eclipse.steady.java.monitor.ClassVisitor;
import org.eclipse.steady.java.monitor.DynamicTransformer;
import org.eclipse.steady.java.monitor.IInstrumentor;

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
      Map<String, Set<org.eclipse.steady.shared.json.model.ConstructId>> _map) {
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
      Map<String, Set<org.eclipse.steady.shared.json.model.ConstructId>> _map, boolean _ordered) {
    final Set<ConstructId> set =
        (_ordered ? new TreeSet<ConstructId>() : new HashSet<ConstructId>());
    for (Set<org.eclipse.steady.shared.json.model.ConstructId> s : _map.values()) {
      for (org.eclipse.steady.shared.json.model.ConstructId cid : s) {
        set.add(JavaId.toCoreType(cid));
      }
    }
    return set;
  }
}
