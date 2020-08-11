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
package com.sap.psr.vulas.java.sign;

import ch.uzh.ifi.seal.changedistiller.JavaChangeDistillerModule;
import ch.uzh.ifi.seal.changedistiller.distilling.Distiller;
import ch.uzh.ifi.seal.changedistiller.distilling.DistillerFactory;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Required to invoke the Eclipse JDT plugin.
 */
public class DistillerUtil {
	/** Constant <code>mInjector</code> */
	protected static final Injector mInjector = Guice.createInjector(new JavaChangeDistillerModule());
	/** Constant <code>structureEntity</code> */
	protected static final StructureEntityVersion structureEntity = new StructureEntityVersion(JavaEntityType.METHOD, "", 0);
	/** Constant <code>mDistiller</code> */
	protected static final Distiller mDistiller = mInjector.getInstance(DistillerFactory.class).create(structureEntity);
}
