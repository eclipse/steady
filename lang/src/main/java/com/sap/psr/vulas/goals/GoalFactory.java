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
package com.sap.psr.vulas.goals;

import javax.validation.constraints.NotNull;

import com.sap.psr.vulas.shared.enums.GoalClient;
import com.sap.psr.vulas.shared.enums.GoalType;

/**
 * <p>GoalFactory class.</p>
 *
 */
public class GoalFactory {

    /**
     * Creates a {@link AbstractGoal} for the given {@link GoalType}.
     *
     * @param _type a {@link com.sap.psr.vulas.shared.enums.GoalType} object.
     * @return a {@link com.sap.psr.vulas.goals.AbstractGoal} object.
     * @throws java.lang.IllegalStateException if any.
     * @throws java.lang.IllegalArgumentException if any.
     */
    public static AbstractGoal create(@NotNull GoalType _type)
            throws IllegalStateException, IllegalArgumentException {
        AbstractGoal goal = null;
        if (_type.equals(GoalType.CLEAN)) {
            goal = new CleanGoal();
        } else if (_type.equals(GoalType.APP)) {
            goal = new BomGoal();
        } else if (_type.equals(GoalType.A2C)) {
            final String clazzname = "com.sap.psr.vulas.cg.A2CGoal";
            try {
                final Class clazz = Class.forName(clazzname);
                goal = (AbstractGoal) clazz.newInstance();
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                        "Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
            } catch (InstantiationException e) {
                throw new IllegalStateException(
                        "Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(
                        "Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
            }
        } else if (_type.equals(GoalType.T2C)) {
            final String clazzname = "com.sap.psr.vulas.cg.T2CGoal";
            try {
                final Class clazz = Class.forName(clazzname);
                goal = (AbstractGoal) clazz.newInstance();
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                        "Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
            } catch (InstantiationException e) {
                throw new IllegalStateException(
                        "Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(
                        "Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
            }
        } else if (_type.equals(GoalType.INSTR)) {
            final String clazzname = "com.sap.psr.vulas.java.goals.InstrGoal";
            try {
                final Class clazz = Class.forName(clazzname);
                goal = (AbstractGoal) clazz.newInstance();
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                        "Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
            } catch (InstantiationException e) {
                throw new IllegalStateException(
                        "Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(
                        "Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
            }
        } else if (_type.equals(GoalType.REPORT)) {
            goal = new ReportGoal();
        } else if (_type.equals(GoalType.UPLOAD)) {
            goal = new UploadGoal();
        } else if (_type.equals(GoalType.SEQUENCE)) {
            goal = new SequenceGoal();
        } else if (_type.equals(GoalType.SPACENEW)) {
            goal = new SpaceNewGoal();
        } else if (_type.equals(GoalType.SPACEMOD)) {
            goal = new SpaceModGoal();
        } else if (_type.equals(GoalType.SPACECLEAN)) {
            goal = new SpaceCleanGoal();
        } else if (_type.equals(GoalType.SPACEDEL)) {
            goal = new SpaceDelGoal();
        } else if (_type.equals(GoalType.CHECKCODE)) {
            final String clazzname = "com.sap.psr.vulas.java.goals.CheckBytecodeGoal";
            try {
                final Class clazz = Class.forName(clazzname);
                goal = (AbstractGoal) clazz.newInstance();
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                        "Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
            } catch (InstantiationException e) {
                throw new IllegalStateException(
                        "Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(
                        "Cannot create instance of class [" + clazzname + "]: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("Goal [" + _type + "] is not supported");
        }
        return goal;
    }

    /**
     * Creates a {@link AbstractGoal} for the given {@link GoalType} and (@link GoalClient}.
     *
     * @param _type a {@link com.sap.psr.vulas.shared.enums.GoalType} object.
     * @param _client a {@link com.sap.psr.vulas.shared.enums.GoalClient} object.
     * @return a {@link com.sap.psr.vulas.goals.AbstractGoal} object.
     * @throws java.lang.IllegalStateException if any.
     * @throws java.lang.IllegalArgumentException if any.
     */
    public static AbstractGoal create(@NotNull GoalType _type, @NotNull GoalClient _client)
            throws IllegalStateException, IllegalArgumentException {
        final AbstractGoal goal = GoalFactory.create(_type);
        goal.setGoalClient(_client);
        return goal;
    }
}
