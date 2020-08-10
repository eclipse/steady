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

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.Test;

public class FileSearchTest {

	@Test
	public void testSearch() {
		final FileSearch fs1 = new FileSearch(new String[] {"bar", "baz", "txt"}, -1);
		assertEquals(3, fs1.search(Paths.get("src/test/resources/foo")).size());	
		final FileSearch fs2 = new FileSearch(new String[] {"bar", "baz", "txt"});
		assertEquals(3, fs2.search(Paths.get("src/test/resources/foo")).size());		
		final FileSearch fs3 = new FileSearch(new String[] {"bar", "baz", "txt"}, 10);
		assertEquals(2, fs3.search(Paths.get("src/test/resources/foo")).size());
	}
}
