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
package com.sap.psr.vulas.backend.requests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.Logger;


import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.GoalContext;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * <p>Abstract AbstractHttpRequest class.</p>
 *
 */
public abstract class AbstractHttpRequest implements HttpRequest {

	private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

	protected long ms = -1;

	/** Null in case the request does not exist on disk. */
	private String objFile = null;

	/** Goal context, required to set the Http headers. */
	protected transient GoalContext context = null;
	
	/**
	 * <p>Constructor for AbstractHttpRequest.</p>
	 */
	protected AbstractHttpRequest() {
		this.ms = System.nanoTime();
	}

	/**
	 * <p>getObjectFilename.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getObjectFilename() {
		return this.getFilename() + ".obj";
	}

	/**
	 * <p>getObjectPath.</p>
	 *
	 * @return a {@link java.nio.file.Path} object.
	 */
	public Path getObjectPath() {
		return Paths.get(this.getVulasConfiguration().getDir(CoreConfiguration.UPLOAD_DIR).toString(), this.getObjectFilename());
	}
	
	/** {@inheritDoc} */
	@Override
	public HttpRequest setGoalContext(GoalContext _ctx) {
		this.context = _ctx;
		return this;
	}
	
	/** {@inheritDoc} */
	@Override
	public GoalContext getGoalContext() {
		return this.context;
	}
	
	/**
	 * <p>getVulasConfiguration.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.util.VulasConfiguration} object.
	 */
	protected VulasConfiguration getVulasConfiguration() {
		if(this.context!=null && this.context.getVulasConfiguration()!=null)
			return this.context.getVulasConfiguration();
		else
			return VulasConfiguration.getGlobal();
	}

	/**
	 * {@inheritDoc}
	 *
	 * First calls {@link HttpRequest#savePayloadToDisk()}, then serializes the request and writes it to disk.
	 */
	@Override
	public final void saveToDisk() throws IOException {

		// Subclasses can write additional stuff to disk (e.g., payloads)
		this.savePayloadToDisk();

		// Then save the request itself
		final File obj_file  = this.getObjectPath().toFile();
		this.objFile = getObjectFilename();
		final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(obj_file));
		oos.writeObject(this);
		oos.close();
		AbstractHttpRequest.log.info("Request object written to [" + obj_file + "]");
	}

	/**
	 * {@inheritDoc}
	 *
	 * Calls {@link HttpRequest#loadPayloadFromDisk()}.
	 */
	@Override
	public final void loadFromDisk() throws IOException {
		this.loadPayloadFromDisk();
	}

	/**
	 * {@inheritDoc}
	 *
	 * First calls {@link HttpRequest#deletePayloadFromDisk()}, then deletes the saved request itself.
	 */
	@Override
	public final void deleteFromDisk() throws IOException {
		if(this.getVulasConfiguration().getConfiguration().getBoolean(CoreConfiguration.UPLOAD_DEL_AFTER, true)) {
			this.deletePayloadFromDisk();
			if(this.objFile!=null)
				this.getObjectPath().toFile().deleteOnExit();
		}
	}

	/**
	 * Just calls the default method {@link ObjectOutputStream#defaultWriteObject()}.
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject(); 
	}

	/**
	 * Calls the default method {@link ObjectInputStream#defaultReadObject()}.
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}
}
