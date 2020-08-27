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
package org.eclipse.steady.shared.json.model.metrics;

import static org.junit.Assert.assertEquals;

import org.eclipse.steady.shared.json.JacksonUtil;
import org.eclipse.steady.shared.json.model.metrics.Metrics;
import org.eclipse.steady.shared.json.model.metrics.Percentage;
import org.eclipse.steady.shared.json.model.metrics.Ratio;
import org.junit.Test;

public class MetricsTest {

  @Test
  public void testMetrics() {
    final Metrics m = new Metrics();
    m.addRatio(new Ratio("Foo", 2, 10));
    m.addPercentage(new Percentage("Bar", 0.5D));
    final String expected_m_string =
        "{\"ratios\":[{\"name\":\"Foo\",\"count\":2.0,\"total\":10.0,\"ratio\":0.2}],\"percentages\":[{\"name\":\"Bar\",\"percentage\":0.5}],\"counters\":null}";
    final String m_string = JacksonUtil.asJsonString(m);
    assertEquals(expected_m_string, m_string);
  }
}
