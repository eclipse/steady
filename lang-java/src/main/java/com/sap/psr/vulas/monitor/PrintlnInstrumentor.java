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

import java.util.HashMap;
import java.util.Map;

import com.sap.psr.vulas.goals.AbstractGoal;
import com.sap.psr.vulas.java.JavaId;

import javassist.CannotCompileException;
import javassist.CtBehavior;

/**
 * <p>PrintlnInstrumentor class.</p>
 *
 */
public class PrintlnInstrumentor extends AbstractInstrumentor {

    /** {@inheritDoc} */
    public void instrument(StringBuffer _code, JavaId _jid, CtBehavior _behavior, ClassVisitor _cv)
            throws CannotCompileException {
        this.injectUrlAndLoader(_code, _jid, _behavior);
        _code.append("System.out.println(\"Call of ")
                .append(_jid.toString())
                .append(", loaded from [\" + vul_cls_res.toString() + \"]\");");
    }

    /** {@inheritDoc} */
    @Override
    public void upladInformation(AbstractGoal _exe, int _batch_size) {
        ;
    }

    /** {@inheritDoc} */
    @Override
    public void awaitUpload() {
        ;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Long> getStatistics() {
        return new HashMap<String, Long>();
    }

    /** {@inheritDoc} */
    @Override
    public boolean acceptToInstrument(JavaId _jid, CtBehavior _behavior, ClassVisitor _cv) {
        return true;
    }
}
