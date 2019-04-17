package com.sap.psr.vulas.shared.model;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.Test;

import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.Artifact;
import com.sap.psr.vulas.shared.json.model.LibraryId;

public class ArtifactTest {

	@Test
	public void testGetAge() {
		final Artifact a = new Artifact();
		a.setProgrammingLanguage(ProgrammingLanguage.JAVA);
		a.setLibId(new LibraryId("foo",  "bar",  "0.0.1"));
		a.setPackaging("jar");
		a.setClassifier(null);
		a.setTimestamp(1540834172000L); // Timestamp of (com.google.inject:guice:4.2.2), released on 29-Oct-2018
		assertEquals(170L, a.getAgeInDays(LocalDate.of(2019, 4, 17)).longValue());
	}
}
