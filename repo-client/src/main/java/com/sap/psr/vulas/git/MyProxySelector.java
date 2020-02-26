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
package com.sap.psr.vulas.git;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * <p>MyProxySelector class.</p>
 *
 */
public class MyProxySelector extends ProxySelector {

    private final Proxy newProxy;

    /**
     * <p>Constructor for MyProxySelector.</p>
     *
     * @param newProxy a {@link java.net.Proxy} object.
     */
    public MyProxySelector( Proxy newProxy ) {
        this.newProxy = newProxy;
    }

    /** {@inheritDoc} */
    @Override
    public List<Proxy> select( URI uri ) {
        return Arrays.asList( this.newProxy );
    }

    /** {@inheritDoc} */
    @Override
    public void connectFailed( URI uri, SocketAddress sa, IOException ioe ) {
        if ( uri == null || sa == null || ioe == null ) {
            throw new IllegalArgumentException( "Arguments can not be null." );
        }
    }

}

