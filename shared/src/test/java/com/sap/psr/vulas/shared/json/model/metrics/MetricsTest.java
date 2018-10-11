package com.sap.psr.vulas.shared.json.model.metrics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sap.psr.vulas.shared.json.JacksonUtil;

public class MetricsTest {

	@Test
	public void testMetrics() {
		final Metrics m = new Metrics();
		m.addRatio(new Ratio("Foo", 2, 10));
		m.addPercentage(new Percentage("Bar", 0.5D));
		final String expected_m_string = "{\"ratios\":[{\"name\":\"Foo\",\"count\":2.0,\"total\":10.0,\"ratio\":0.2}],\"percentages\":[{\"name\":\"Bar\",\"percentage\":0.5}],\"counters\":null}";
		final String m_string = JacksonUtil.asJsonString(m);
		assertEquals(expected_m_string, m_string);
	}
}
