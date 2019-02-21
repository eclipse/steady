sap.ui.jsview("vulasfrontend.view.App", {

	getControllerName : function() {
		return "vulasfrontend.view.App";
	},

	createContent : function(oController) {
		this.setDisplayBlock(true);
		this.app = new sap.m.SplitApp("splitApp", {
			mode: "StretchCompressMode"
		});
		return this.app;
	}
});
