sap.ui.controller("sap.psr.patcha.web.view.Settings", {

    saveSettings: function(oEvent) {
    	this.navigation.navBack();
    },
    
    cancelSettings: function(oEvent) {
    	this.navigation.navBack();
    },

    handleNavButtonPress: function(oEvent) {
        this.navigation.navBack();
    }
});