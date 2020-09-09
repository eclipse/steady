/*
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
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
jQuery.sap.declare("vulasbugfrontend.MyRouter");

vulasbugfrontend.MyRouter = {
	
	/**
	 * to monkey patch the router with the mobile nav back handling
	 */
	myNavBack : function(route, data) {
		var history = sap.ui.core.routing.History.getInstance();
		var url = this.getURL(route, data);
		var direction = history.getDirection(url);
		if ("Backwards" === direction) {
			window.history.go(-1);
		} else {
			var replace = true; // otherwise we go backwards with a forward
			// history
			this.navTo(route, data, replace);
		}
	},

	/**
	 * to monkey patch the router with a nav to method that does not write
	 * hashes but load the views properly
	 */
	myNavToWithoutHash : function(viewName, viewType, master, data) {
		var app = sap.ui.getCore().byId("splitApp");
		var view = this.getView(viewName, viewType);
		app.addPage(view, master);
		app.toDetail(view.getId(), "show", data);
	}
};
