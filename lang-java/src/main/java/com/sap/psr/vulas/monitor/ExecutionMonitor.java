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

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.AbstractGoal;
import com.sap.psr.vulas.goals.GoalConfigurationException;
import com.sap.psr.vulas.goals.TestGoal;
import com.sap.psr.vulas.shared.enums.GoalClient;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Uses the {@link ClassVisitor} to dynamically instrument Java constructors and methods, i.e.,
 * during the class loading process. Receives callbacks from instrumented code that is executed
 * during application tests. The collected information is then uploaded to the central Vulas
 * backend using the {@link UploadScheduler}.
 */
public class ExecutionMonitor {

    // ====================================== STATIC MEMBERS

    private static ExecutionMonitor instance = null;

    private static Logger log = null;

    private static boolean PAUSE_COLLECTION = false;

    // ====================================== INSTANCE MEMBERS

    private String id = new Double(Math.random()).toString();

    private UploadScheduler shutdownUploader = null;
    private UploadScheduler periodicUploader = null;

    /**
     * The goal execution related to a trace collection, will be null for all context determination modes except FIXED.
     * @see ApplicationContextFinder#isFixedMode()
     */
    private AbstractGoal exe = null;

    /**
     * <p>Constructor for ExecutionMonitor.</p>
     */
    public ExecutionMonitor() {
        try {
            final Application app_ctx = CoreConfiguration.getAppContext();
            final Configuration cfg = VulasConfiguration.getGlobal().getConfiguration();

            // Always create and register shutdown uploader
            this.shutdownUploader = new UploadScheduler(this);
            Runtime.getRuntime()
                    .addShutdownHook(
                            new Thread(this.shutdownUploader, "vulas-shutdown-trace-upload"));

            // Configure uploader: Create and start periodic uploader according to configuration
            if (cfg.getBoolean(CoreConfiguration.MONI_PERIODIC_UPL_ENABLED, true))
                this.enablePeriodicUpload(
                        cfg.getInt(CoreConfiguration.MONI_PERIODIC_UPL_INTERVAL, 300000),
                        cfg.getInt(CoreConfiguration.MONI_PERIODIC_UPL_BATCH_SIZE, 1000));

            // Goal execution
            this.exe = new TestGoal();
            this.exe.setGoalClient(GoalClient.AGENT);
            this.startGoal();
        } catch (ConfigurationException ce) {
            ExecutionMonitor.getLog().error(ce.getMessage());
        } catch (GoalConfigurationException gce) {
            ExecutionMonitor.getLog().error(gce.getMessage());
        }
    }

    // ====================================== STATIC METHODS

    /**
     * <p>Getter for the field <code>instance</code>.</p>
     *
     * @return a {@link com.sap.psr.vulas.monitor.ExecutionMonitor} object.
     */
    public static synchronized ExecutionMonitor getInstance() {
        if (ExecutionMonitor.instance == null) ExecutionMonitor.instance = new ExecutionMonitor();
        return ExecutionMonitor.instance;
    }

    private static final Logger getLog() {
        if (ExecutionMonitor.log == null)
            ExecutionMonitor.log = org.apache.logging.log4j.LogManager.getLogger();
        return ExecutionMonitor.log;
    }

    /**
     * <p>isPaused.</p>
     *
     * @return a boolean.
     */
    public static boolean isPaused() {
        return ExecutionMonitor.PAUSE_COLLECTION;
    }
    /**
     * <p>setPaused.</p>
     *
     * @param _bool a boolean.
     */
    public static synchronized void setPaused(boolean _bool) {
        ExecutionMonitor.PAUSE_COLLECTION = _bool;
    }

    // ====================================== INSTANCE METHODS

    /**
     * <p>toString.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        final StringBuffer b = new StringBuffer();
        b.append("ExecutionMonitor [id=").append(this.id);
        b.append(", periodicUpload=").append(this.isPeriodicUploadEnabled());
        if (this.isPeriodicUploadEnabled()) {
            b.append(", interval=").append(this.getPeriodicUploadInterval());
            b.append(", batchSize=").append(this.getPeriodicUploadBatchSize());
        }
        b.append("]");
        return b.toString();
    }

    /**
     * <p>enablePeriodicUpload.</p>
     *
     * @param _interval a long.
     * @param _batch_size a int.
     */
    public void enablePeriodicUpload(long _interval, int _batch_size) {
        this.periodicUploader = new UploadScheduler(this, _interval, _batch_size);
        final Thread thread = new Thread(this.periodicUploader, "vulas-periodic-trace-upload");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        this.shutdownUploader.addObserver(this.periodicUploader);
    }

    /**
     * <p>isPeriodicUploadEnabled.</p>
     *
     * @return a boolean.
     */
    public boolean isPeriodicUploadEnabled() {
        return this.periodicUploader != null && this.periodicUploader.isEnabled();
    }
    /**
     * <p>getPeriodicUploadInterval.</p>
     *
     * @return a long.
     */
    public long getPeriodicUploadInterval() {
        return (this.periodicUploader == null ? -1 : this.periodicUploader.getInterval());
    }
    ;
    /**
     * <p>getPeriodicUploadBatchSize.</p>
     *
     * @return a int.
     */
    public int getPeriodicUploadBatchSize() {
        return (this.periodicUploader == null ? -1 : this.periodicUploader.getBatchSize());
    }
    ;

    /**
     * <p>startGoal.</p>
     *
     * @throws com.sap.psr.vulas.goals.GoalConfigurationException if any.
     */
    public void startGoal() throws GoalConfigurationException {
        if (this.exe != null) this.exe.start();
    }

    /**
     * <p>stopGoal.</p>
     */
    public void stopGoal() {
        if (this.exe != null) {
            this.exe.stop();

            // Add instrumentation stats (if any)
            exe.addGoalStats("test", InstrumentationControl.getOverallStatistics());

            final List<IInstrumentor> instrumentorList = InstrumentorFactory.getInstrumentors();
            final Iterator<IInstrumentor> iter = instrumentorList.iterator();
            while (iter.hasNext()) {
                final IInstrumentor i = iter.next();
                exe.addGoalStats("test." + i.getClass().getSimpleName(), i.getStatistics());
            }

            this.exe.upload(false);
        }
    }

    /**
     * Iterates over all configured {@link IInstrumentor}s and calls {@link IInstrumentor#awaitUpload()} for each of them.
     */
    public void awaitUpload() {
        final List<IInstrumentor> instrumentorList = InstrumentorFactory.getInstrumentors();
        final Iterator<IInstrumentor> iter = instrumentorList.iterator();
        while (iter.hasNext()) {
            final IInstrumentor i = iter.next();
            i.awaitUpload();
        }
    }

    /**
     * Calls {@link ExecutionMonitor#uploadInformation(int)} with a batch size of -1.
     */
    public synchronized void uploadInformation() {
        this.uploadInformation(-1);
    }

    /**
     * Iterates over all configured {@link IInstrumentor}s and calls {@link IInstrumentor#upladInformation(AbstractGoal, int)} for each of them.
     *
     * @param batchSize a int.
     */
    public synchronized void uploadInformation(int batchSize) {
        final List<IInstrumentor> instrumentorList = InstrumentorFactory.getInstrumentors();
        final Iterator<IInstrumentor> iter = instrumentorList.iterator();
        while (iter.hasNext()) {
            final IInstrumentor i = iter.next();
            i.upladInformation(this.exe, batchSize);
        }
    }
}
