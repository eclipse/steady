sap.ui.jsview("vulasbugfrontend.view.App", {
    getControllerName: function () {
        return "vulasbugfrontend.view.App";
    },
    createContent: function (oController) {
        this.setDisplayBlock(true);
        this.app = new sap.m.SplitApp("splitApp", {
        });
        return this.app;
    }
});
