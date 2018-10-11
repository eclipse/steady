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
