sap.ui.controller("view.Master",{

onInit : function() {
	jQuery.sap.require("sap.m.MessageBox");
	this.router = sap.ui.core.UIComponent.getRouterFor(this);

	// move the search bar below the pullToRefresh on touch
	// devices
	if (sap.ui.Device.support.touch) {
		var bar = this.getView().byId("searchBar");
		var page = this.getView().byId("page");
		page.insertAggregation("content", bar, 1);
	}
	this.load();
	},

load : function() {
	var list = this.getView().byId("idListBugs");
	var sUrl = model.Config.getBugsAsUrl();
	list.setBusy(true);
	var oModel = new sap.ui.model.json.JSONModel();
	model.Config.loadData(oModel, sUrl, 'GET');
	oModel.attachRequestCompleted(function() {
		list.setModel(oModel);
		list.setVisibleRowCount(oModel.getObject("/").length);
		list.setBusy(false);
	});
},

onExit : function() {},

reloadData : function() {
	var list = this.getView().byId('idListBugs');
	list.setBusy(true);
	var data = [];
	var oldmodel = list.getModel();
	if (oldmodel != undefined) {
		oldmodel.setData(data, false);
		oldmodel.refresh();
	}

	if (!model.Config.isMock) {
		var sUrl = model.Config.getBugsAsUrl();
		var newModel = new sap.ui.model.json.JSONModel();
		model.Config.loadData(newModel, sUrl, 'GET');
		newModel.attachRequestCompleted(function() {
			list.setModel(newModel);
			list.setBusy(false);
		});
	}

},

onListItemTap : function(oEvent) {
	bug = oEvent.getParameters().rowBindingContext.getObject().bugId;
	this.router.navTo("component", {
		bugId : bug
		});
},

handleSearch : function(evt) {
	this.filterApplicationList(evt.getSource().getValue());
},

//					filterApplicationList : function(query) {
//						var filters = [];
//						if (query && query.length > 0) {
//							var orQueryfilters = [];
//							var bugId = new sap.ui.model.Filter("bugId",
//									sap.ui.model.FilterOperator.Contains, query);
//							orQueryfilters.push(bugId);
//							var filter = new sap.ui.model.Filter(orQueryfilters);
//							filters.push(filter);
//						}
//						var list = this.getView().byId("idListApplications");
//						var binding = list.getBinding("items");
//						binding.filter(filters);
//					},

clone : function(obj) {
	if (null == obj || "object" != typeof obj)
		return obj;
	var copy = obj.constructor();
	for ( var attr in obj) {
		if (obj.hasOwnProperty(attr))
			copy[attr] = obj[attr];
	}
	return copy;
},

//					raiseNotificationAlert : function(newData) {
//
//						jQuery.sap.require("sap.m.MessageBox");
//
//						for (var i = 0; i < this.data.length; i++) {
//							if (this.data.hasOwnProperty(i)) {
//								for (var j = 0; j < newData.length; j++) {
//									if (newData.hasOwnProperty(j)) {
//
//										if (this.data[i].component_id == newData[j].component_id) {
//
//											if (this.data[i].status != "alert"
//													&& newData[j].status == "alert") {
//												// console.dir(newData[j]);
//												sap.m.MessageBox
//														.alert("ALERT: "
//																+ newData[j].lastAlertText);
//											}
//										}
//
//									}
//
//								}
//							}
//						}
//					},


					logout : function() {
						$
								.ajax({
									url : "/sap/hana/xs/formLogin/token.xsjs",
									type : "GET",
									beforeSend : function(request) {
										request.setRequestHeader(
												"X-CSRF-Token", "Fetch");
									},
									success : function(data, textStatus,
											XMLHttpRequest) {
										var token = XMLHttpRequest
												.getResponseHeader("X-CSRF-Token");

										$.ajax({
													url : "/sap/hana/xs/formLogin/logout.xscfunc",
													type : "POST",
													headers : {'X-Vulas-Version':model.Version.version,'X-Vulas-Component':'appfrontend'},
													beforeSend : function(
															request) {
														request.setRequestHeader(
																		"X-CSRF-Token",
																		token);
													},
													success : function(data,
															textStatus,
															XMLHttpRequest) {

														var mLayout = sap.ui
																.getCore()
																.byId("Shell");
														// mLayout is the id of
														// main layout. Change
														// it accordingly

														mLayout.destroy();
														sap.ui.getCore()
																.applyChanges();
														// jQuery(document.body).html("<span>Logged
														// out
														// successfully.</span>");
														window.location
																.reload();

													}
												});
									}
								});
					},

					handleSettings : function(oEvent) {
						// create popover
						if (!this.oPopoverSettings) {
							this.oPopoverSettings = new sap.m.Popover(
									"settings_popover",
									{
										title : "Settings",
										// placement: sap.m.PlacementType.Top,
										footer : new sap.m.Bar(
												{
													contentRight : [ new sap.m.Button(
															{
																text : "Save",
																icon : "sap-icon://save",
																press : function() {
																	if (sap.ui.getCore().byId('idHostURL').getValue() != null && sap.ui.getCore().byId('idHostURL').getValue() != "")
																		model.Config.setHost(sap.ui.getCore().byId('idHostURL').getValue());
																	if (sap.ui.getCore().byId('idCiaURL').getValue() != null && sap.ui.getCore().byId('idCiaURL').getValue() != "")
																		model.Config.setCiaHost(sap.ui.getCore().byId('idCiaURL').getValue());
																	if (sap.ui.getCore().byId('idLang').getValue() != null )
																		model.Config.setLang(sap.ui.getCore().byId('idLang').getValue());
																	// if(sap.ui.getCore().byId('idUsr').getValue()
																	// !=null &&
																	// sap.ui.getCore().byId('idUsr').getValue()
																	// !="")
																	// model.Config.setUser(sap.ui.getCore().byId('idUsr').getValue());
																	// if(sap.ui.getCore().byId('idPwd').getValue()
																	// !=null &&
																	// sap.ui.getCore().byId('idPwd').getValue()
																	// !="")
																	// model.Config.setPwd(sap.ui.getCore().byId('idPwd').getValue());
																	/*
																	 * if(sap.ui.getCore().byId('idSkipEmpty').getState()
																	 * !=model.Config.getSkipEmpty())
																	 * model.Config.setSkipEmpty(sap.ui.getCore().byId('idSkipEmpty').getState());
																	 */
																	// this.oPopoverSettings.close();
																	sap.ui
																			.getCore()
																			.byId(
																					'settings_popover')
																			.close();
																}
															}) ],
													contentLeft : [ new sap.m.Button(
															{
																text : "Close",
																icon : "sap-icon://close",
																press : function() {
																	// this.oPopoverSettings.close();
																	sap.ui
																			.getCore()
																			.byId(
																					'settings_popover')
																			.close();
																}
															}) ]
												}),
										content : [
												new sap.m.InputListItem(
														{
															label : "Back-end URL",
															content : new sap.m.Input(
																	{

																		id : "idHostURL",
																		type : sap.m.InputType.Text,
																		// placeholder:
																		// 'Enter
																		// host
																		// address
																		// (default:
																		// 127.0.0.1)
																		// ...',
																		// width:
																		// "100%",
																		value : model.Config
																				.getHost()
																	})
														}),
												new sap.m.InputListItem(
														{
															label : "Cia URL",
															content : new sap.m.Input(
																	{

																		id : "idCiaURL",
																		type : sap.m.InputType.Text,
																		// placeholder:
																		// 'Enter
																		// host
																		// address
																		// (default:
																		// 127.0.0.1)
																		// ...',
																		// width:
																		// "100%",
																		value : model.Config
																				.getCiaHostUrl()
																	})
														}),
														new sap.m.InputListItem(
																{
																	label : "Lang",
																	content : new sap.m.ComboBox("idLang",
														            		{
														            	//showSecondaryValues : true,
														            	//filterSecondaryValues : true,
														            	items: [new sap.ui.core.ListItem({text:'JAVA'}),
														            		new sap.ui.core.ListItem({text:'PY'}),
														            		new sap.ui.core.ListItem({text:''})]
														            		})
																})
										/*
										 * , new sap.m.InputListItem({
										 * label:"Username", content:new
										 * sap.m.Input({ id: "idUsr", type:
										 * sap.m.InputType.Text, // placeholder:
										 * 'biroot', // placeholder: 'Enter
										 * user/login (default: smash) ...', //
										 * width: "100%", value:
										 * model.Config.getUser() }) }), new
										 * sap.m.InputListItem({ label:"Pwd",
										 * content: new sap.m.Input({ id :
										 * "idPwd", type:
										 * sap.m.InputType.Password, //
										 * placeholder: 'Enter password ...',
										 * type:"Password", // width: "100%",
										 * value: "" }) }), new
										 * sap.m.InputListItem({ label:"Skip
										 * Empty Apps", content: new
										 * sap.m.Switch({ id : "idSkipEmpty",
										 * state: model.Config.getSkipEmpty() //
										 * width: "100%", }) })
										 */]
									});
							this.getView().addDependent(this.oPopoverSettings);
						}
						if (this.oPopoverSettings.isOpen()) {
							this.oPopoverSettings.close();
						} else {
							this.oPopoverSettings.openBy(oEvent.getSource());
						}
					},

					openDoc: function(){
						model.Config.openWiki("Bugs-Web-Frontend");
					},
					
					openHelp : function(oEvent) {
				    	model.Config.openWiki("Help");
					},

					onHomePress : function() {
						window.location.href = '/bugs/';
					},

					onInfoPress : function(oEvent) {
						// create popover
						if (!this.oPopover) {
							this.oPopover = sap.ui.xmlfragment("view.Popover",
									this);
							this.getView().addDependent(this.oPopover);
						}
						if (this.oPopover.isOpen()) {
							this.oPopover.close();
						} else {
							this.oPopover.openBy(oEvent.getSource());
						}
					}
				});