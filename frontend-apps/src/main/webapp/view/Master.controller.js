sap.ui.controller("view.Master", {
	
	onInit: function(){
		jQuery.sap.require("sap.m.MessageBox");

		this.router = sap.ui.core.UIComponent.getRouterFor(this);
		
		var oStore = jQuery.sap.storage(jQuery.sap.storage.Type.local);
		model.Config.setModel(oStore.get("vulas-frontend-settings"));
		
		model.Config.getTenant();
		model.Config.getDefaultSpace();
		model.Config.loadPropertiesFromBackend();

		// move the search bar below the pullToRefresh on touch devices
		if (sap.ui.Device.support.touch) {
			var bar = this.getView().byId("searchBar");
			var page = this.getView().byId("page");
			page.insertAggregation("content", bar, 0);
		}
		this.reloadData();
	},
	
//	load : function() {
//		var list = this.getView().byId("idListApplications");
//		var sUrl = model.Config.getMyAppsServiceUrl();
//		list.setBusy(true);
//		oModel = new sap.ui.model.json.JSONModel();
//		model.Config.loadData (oModel,sUrl, 'GET');
//		oModel.attachRequestCompleted(function() {
//			list.setModel(oModel);
//			list.setBusy(false);
//		});
//	},

	onExit : function() {
	},
	
	reloadData: function() {
		var list = this.getView().byId('idListApplications');
		var label = this.getView().byId('app-label');
		label.setText("Space " + model.Config.getSpace());
		var labelCount = this.getView().byId('app-count');
		list.setBusy(true);
		var data = [];
		var oldmodel = list.getModel();
		if (oldmodel != undefined) {
			oldmodel.setData(data,false);
			oldmodel.refresh();
		}

		if (!model.Config.isMock) {
			this.vulnerabilityIconQueue.clear()
			var sUrl = model.Config.getMyAppsServiceUrl(false);
			var newModel = new sap.ui.model.json.JSONModel();
			var event;
			if (typeof(Event) === 'function') {
				event = new Event('customevent')
			} else {
				event = document.createEvent('customevent');
				event.initEvent('submit', true, true);
			}
			document.dispatchEvent(event);
			model.Config.loadData(newModel, sUrl, 'GET');
			var that = this;
			newModel.attachRequestCompleted(function() {
				newListApplicationsSize = newModel.oData.length
				newModel.setSizeLimit(Math.min(newListApplicationsSize, model.Config.getListSize()))
				labelCount.setText(Math.min(newListApplicationsSize, model.Config.getListSize()) + ' displayed out of ' + newListApplicationsSize);
				list.setModel(newModel);
				list.setBusy(false);
				if (model.Config.getSpace() != model.Config.getDefaultSpace()) {
					that.loadVulnerabilityIcons(list);
				}
			});
		}
	},
	
	loadVulnerabilityIcon: function(workspace, backendUrl, skipEmpty, app, index, listModel) {
		return new Promise(function(resolve, reject) {
			let url = model.Config.getUsedVulnerabilitiesServiceUrl(app.group, app.artifact, app.version, false, true, true, app.lastChange)
			$.ajax({
				url: url,
				headers: model.Config.defaultHeaders()
			}).done(function(deps) {
				if (listModel.oData.length > 0 && workspace === model.Config.getSpace() && backendUrl === model.Config.getHost() && skipEmpty === model.Config.getSkipEmpty()) {
					if (deps.some(function(dep) {
						return dep.affected_version
					})) {
						listModel.oData[index].hasVulnerabilities = true
					} else {
						listModel.oData[index].hasVulnerabilities = false
					}
					listModel.refresh()
				}
				resolve()
			}).fail(function(err) {
				listModel.oData[index].fetchingError = true
				listModel.refresh()
				reject()
			})
		})
	},

	// To access the queue from the window: sap.ui.getCore().byId("__xmlview0").getController().vulnerabilityIconQueue
	vulnerabilityIconQueue: new PQueue({concurrency: 5, queueClass: BasePriorityQueue}),

	loadVulnerabilityIcons: function(list) {
		let workspace = model.Config.getSpace()
		let backendUrl = model.Config.getHost()
		let skipEmpty = model.Config.getSkipEmpty()
		let listModel = list.getModel()
		listModel.refresh()
		listModel.oData.forEach(function(app, index){
			if (index <= (model.Config.getListSize() - 1)) {
				this.vulnerabilityIconQueue.add(function() {
					return this.loadVulnerabilityIcon(workspace, backendUrl, skipEmpty, app, index, listModel)
				}.bind(this), {
					priority: 1,
					id: index
				})
			}
		}.bind(this))
	},
	
	validateEmail: function (email) {
		var to_test = email.toLowerCase();
				
		var re =  model.Config.getDlRegexList(); 
		
		for(var i in re){
			var regex = new RegExp(re[i]);
			var t = regex.test(to_test);
			if(t)
				return true;
		}
		return false;
	    
	},
	
	validateListSize: function (_size) {
	    return /^\d*$/.test(_size) && _size>0 && _size <= 100 ;
	},
	
	validateSwIdObjectNumber: function (number) {
	    var re = new RegExp(model.Config.getSwIdRegex());
	    return re.test(number);
	},
	
    editSpace: function(_new,_button) {
    	//load space
    	this.urlSpace = model.Config.getSpacesServiceUrl();
    	this.token = null;
    	this.method = "POST";
    	var oSpaceModel = new sap.ui.model.json.JSONModel();
    	if(!_new){
	    	this.token = sap.ui.getCore().byId('idSpace').getSelectedKey();
	    	this.urlSpace = model.Config.getSpaceServiceUrl(this.token);
	    	
	    	model.Config.loadData (oSpaceModel,model.Config.getSpaceServiceUrl(this.token), 'GET');
	    	
	    	this.method = "PUT";
    	}
    	else{
    		//if _new the load is not necessary but we are still doing a GET to use attachRequestCompleted 
    		model.Config.loadData (oSpaceModel,model.Config.getSpaceServiceUrl(model.Config.getDefaultSpace()), 'GET');
    	}
    	    	
    	
    	oSpaceModel.attachRequestCompleted(function() {
    	
    		// create popover
	        if (!this.oPopoverEdit) {
	            this.oPopoverEdit = new sap.m.Popover("editSpace_popover", {
		            title: "Manage Space", 
			         //   placement: sap.m.PlacementType.Top, 
			            footer: new sap.m.Bar({
			                contentRight: [new sap.m.Button({
			                    text: "Save",
			                    icon: "sap-icon://save",
			                    press: function () {
			                    	if(sap.ui.getCore().byId('idSw').getValue()!="" && (model.Config.getSwIdLabel()=="" || model.Config.getSwIdLink()=="" || model.Config.getSwIdRegex()=="" || model.Config.getSwIdDb()=="")){
			                    		sap.m.MessageBox.warning(
			                    				"Cannot create/edit space with software identifier; corresponding properties not configured in the backend."
			                    			);
			                    	}
			                    	else if(sap.ui.getCore().byId('idSpaceDL').getValue()!=""  && ( model.Config.getDlRegexList=="" || model.Config.getDlExample=="")){
			                    		sap.m.MessageBox.warning(
			                    				"Cannot create/edit space with distribution list; corresponding properties not configured in the backend"
			                    			);
			                    	}
			                    	else if(model.Config.getSwIdMandatory()=="true" && sap.ui.getCore().byId('idSpaceDL').getValue()=="" && sap.ui.getCore().byId('idSw').getValue()=="")
			                    		sap.m.MessageBox.warning(
			                    				"Please provide a valid " + model.Config.getSwIdLabel()+ " or a valid distribution list. Other mandatory fields are: name, description, export mode and public/private mode."
			                    			);
			                    	else if(sap.ui.getCore().byId('idSpaceName').getValue() ==null || sap.ui.getCore().byId('idSpaceName').getValue()==""
			                    		|| sap.ui.getCore().byId('idSpaceDescription').getValue() ==null || sap.ui.getCore().byId('idSpaceDescription').getValue()==""
			                    			|| sap.ui.getCore().byId('idSpaceExport').getSelectedItem() ==null || sap.ui.getCore().byId('idSpaceExport').getSelectedKey()=="")
			                    		sap.m.MessageBox.warning(
			                    				"Please provide name, description, export mode for the space to be created."
			                    			);
			                    	else if(sap.ui.getCore().byId('idSpaceDL').getValue()!="" && !this.validateEmail(sap.ui.getCore().byId('idSpaceDL').getValue()))
			                    		sap.m.MessageBox.warning(
			                    				"Please provide an internal distribution list as contact " + model.Config.getDlExample()
			                    			);
			                    	else if(sap.ui.getCore().byId('idSw').getValue()!="" && !this.validateSwIdObjectNumber(sap.ui.getCore().byId('idSw').getValue()))
			                    		sap.m.MessageBox.warning(
			                    				"Please provide a valid " + model.Config.getSwIdLabel()
			                    			);
			                    	else{
			                    		
			                    		//construct body
			                    		var body = '{ "spaceName": "' + sap.ui.getCore().byId("idSpaceName").getValue()+'"';
			                    		body = body + ', "spaceDescription": "' + sap.ui.getCore().byId("idSpaceDescription").getValue()+'"';
			                    		body = body + ', "exportConfiguration": "' + sap.ui.getCore().byId("idSpaceExport").getSelectedKey()+'"';
			                    		body = body + ', "public": "' + sap.ui.getCore().byId("idSpacePublic").getState()+'"';
			                    		body = body + ', "default": "false"';
			                    		if(this.token!=null)
			                    			body = body + ', "spaceToken": "' + this.token +'"';
			                    		if(sap.ui.getCore().byId('idSpaceDL').getValue()!=null && sap.ui.getCore().byId('idSpaceDL').getValue()!=""){
			                    			body = body + ', "spaceOwners": [ "' + sap.ui.getCore().byId("idSpaceDL").getValue()+'" ]';
			                    		}
			                    		if(sap.ui.getCore().byId('idSw').getValue()!=null && sap.ui.getCore().byId('idSw').getValue()!=""){
			                    			body = body + ', "properties":[{"source":"USER","name":"'+model.Config.getSwIdDb()+'","value":"' + sap.ui.getCore().byId("idSw").getValue()+'"}]';
			                    		}
			                    		else{ //alert that SwId id was not provided and they should do it by editing the space
			                    			sap.m.MessageBox.warning(
				                    				"You did not provide the " + model.Config.getSwIdLabel() +
				                    				" Please do so by going to Configuration -> Space Edit. Follow the info link for more information."
				                    			);
			                    		}
			                    		
			                    		body = body + '}'
			                    		
			                    			//todo add properties
			                    		
			                    		
			                    		$.ajax({
			            			        type: this.method,
			            			        url: this.urlSpace,
			            			        async: false,
			            			        data: body,
			            			        headers : {'content-type': "application/json",'cache-control': "no-cache" ,'X-Vulas-Tenant' : model.Config.getTenant() },
	          						        
			            			        success: function(data, status, jqXHR) {
	               						    	sap.m.MessageBox.success(
	        		                    				"Space has been saved, please copy the following token and provide it when analyzing your application: " + data.spaceToken
	        		                    			);
		    	
	               						    	sap.ui.getCore().byId('editSpace_popover').close();},
	               	        			    error: function(data, status, jqXHR) {
	                   						    	sap.m.MessageBox.error(
	            		                    				"Error [" + status + "] while saving the space."
	            		                    			);
	    	    	
	                   						    	},        			
			                    		})
			                    		sap.ui.getCore().byId('editSpace_popover').close();
			                    	}}.bind(this)})],
			                    contentLeft: [ new sap.m.Button({
			                        text: "Close",
			                        icon: "sap-icon://close",
			                        press: function () {
			                        	sap.ui.getCore().byId('editSpace_popover').close();
			                        }
			                      })]
			            }), 
			            content: [
			            new sap.m.InputListItem({
			            	label: "Name",
				            content: new sap.m.Input({
				            	  id: "idSpaceName",
				            	  type: sap.m.InputType.Text,
				            //	  value: oSpaceModel.getObject("/spaceName")
				            	  //liveChange : function() {model.Config.loadSpaces(sap.ui.getCore().byId('idTenant').getValue());}
				        		})
			            }),
			            new sap.m.InputListItem({
			            	label: "Description",
				            content: new sap.m.Input({
				            	  id: "idSpaceDescription",
				            	  type: sap.m.InputType.Text,
				            //	  value: oSpaceModel.getObject("/spaceDescription")
				            	  //liveChange : function() {model.Config.loadSpaces(sap.ui.getCore().byId('idTenant').getValue());}
				        		})
			            }),
			            new sap.m.InputListItem({
			            	label: "Contact (provide a DL)",
				            content: new sap.m.Input({
				            	  id: "idSpaceDL",
				            	  type: sap.m.InputType.Text,
				            //	  value: dl
				            	  //liveChange : function() {model.Config.loadSpaces(sap.ui.getCore().byId('idTenant').getValue());}
				        		})
			            }),
			            new sap.m.InputListItem({
			            	label: model.Config.getSwIdLabel(),
				            content: new sap.ui.layout.HorizontalLayout({ 
				            	content :[ 
									new sap.m.Link(
											{
												text : "info",
												href : model.Config
														.getSwIdLink(),
												target : "_blank"
											}),
									new sap.m.Input(
											{
												id : "idSw",
												type : sap.m.InputType.Text,

											//liveChange : function() {model.Config.loadSpaces(sap.ui.getCore().byId('idTenant').getValue());}
											})
				            ]})
			            }),
			            new sap.m.InputListItem({
			            	label: "Export results",
				            content:  new sap.m.ComboBox("idSpaceExport",
				            		{
				            	items:[
				            		new sap.ui.core.ListItem({key: 'OFF', 
		                            	text:'OFF',
		                            	additionalText:'No export'}),
		                            new sap.ui.core.ListItem("idAggregated",{key: 'AGGREGATED', 
			                           	text:'AGGREGATED',
			                           	additionalText:'Scan results of all apps are aggregated before export'}),
			                        new sap.ui.core.ListItem({key: 'DETAILED', 
				                           	text:'DETAILED',
				                           	additionalText:'Scan results of all apps are exported as is'})
				            		]}
				            )
			            }),
			            new sap.m.InputListItem({
			            	label:"Public",
				            content: new sap.m.Switch({
				            		id : "idSpacePublic",
			        	    	  state: oSpaceModel.getObject("/public")
			        	    //	  width: "100%",
			        	      })
			            })] 
			        });

	         
	            this.getView().addDependent(this.oPopoverEdit);
	        }
	        
	        if(_new){
	        	sap.ui.getCore().byId('idSpaceName').setValue("");
	        	sap.ui.getCore().byId('idSpaceDescription').setValue("");
	        	sap.ui.getCore().byId('idSpaceExport').setSelectedItem(sap.ui.getCore().byId('idAggregated'));
	        	sap.ui.getCore().byId('idSw').setValue("");
		    	sap.ui.getCore().byId('idSpaceDL').setValue("");
	        }
	        else{
	        	sap.ui.getCore().byId('idSpaceName').setValue(oSpaceModel.getObject("/spaceName"));
	        	sap.ui.getCore().byId('idSpaceDescription').setValue(oSpaceModel.getObject("/spaceDescription"));
	        	

	    		//TODO: remove assumption that only the sw id property and email exist
	    		var properties = oSpaceModel.getObject("/properties");
	    		var swId = "";
	    		var dl = "";
	    		if(properties.length>0)
	    			swId = oSpaceModel.getObject("/properties")[0].value;
	    		if(oSpaceModel.getObject("/spaceOwners").length>0)
	    			dl = oSpaceModel.getObject("/spaceOwners")[0];
	    		
	    		sap.ui.getCore().byId('idSw').setValue(swId);
	    		sap.ui.getCore().byId('idSpaceDL').setValue(dl);
		        
		        var items = sap.ui.getCore().byId('idSpaceExport').getItems();
	            var selectedI = null;
	            for(var loop in items){
	            	if(items[loop].getKey()==oSpaceModel.getObject("/exportConfiguration")){
	            		selectedI = items[loop];
	            	}
	            }
	            sap.ui.getCore().byId('idSpaceExport').setSelectedItem(selectedI);
	        }
            
	        if (this.oPopoverEdit.isOpen()) {
	            this.oPopoverEdit.close();
	        } else {
	            this.oPopoverEdit.openBy(_button);
	        }
	    	
    	}.bind(this))
    	
    },


	onListItemTap: function(oEvent) {
		let object = oEvent.getParameter("listItem").getBindingContext().getObject()
		group = object.group;
		artifact = object.artifact;
		version = object.version;
		model.lastChange = new Date(object.lastChange).getTime()
		this.router.navTo("component", {
			group : group,
			artifact : artifact,
			version : version
		});
	},

	handleSearch : function (evt) {
		this.filterApplicationList(evt.getSource().getValue());
	},
	
	filterApplicationList : function (query){
		var filters = [];
		if (query && query.length > 0) {
            var orQueryfilters = [];
            var groupId = new sap.ui.model.Filter("group", sap.ui.model.FilterOperator.Contains, query);
            orQueryfilters.push(groupId);
            var artifactId = new sap.ui.model.Filter("artifact", sap.ui.model.FilterOperator.Contains, query);
            orQueryfilters.push(artifactId);
            var version = new sap.ui.model.Filter("version", sap.ui.model.FilterOperator.Contains, query);
            orQueryfilters.push(version);
            var filter = new sap.ui.model.Filter(orQueryfilters);
            filters.push(filter);
        }
		var list = this.getView().byId("idListApplications");
		var binding = list.getBinding("items");
		let listModel = list.getModel()
		var labelCount = this.getView().byId('app-count');
		binding.filter(filters);
		labelCount.setText(Math.min(binding.aIndices.length, model.Config.getListSize()) + ' displayed out of ' + listModel.oData.length);
		if (query.length >= 1) {
			this.updateFilteredVulnerabilityIcons(binding, listModel)
		}
	},

	updateFilteredVulnerabilityIcons: _.debounce(function(binding, listModel) {
		let workspace = model.Config.getSpace()
		let backendUrl = model.Config.getHost()
		let skipEmpty = model.Config.getSkipEmpty()
		binding.aIndices.forEach(function(id) {
			if (!this.vulnerabilityIconQueue.queue.isInserted(id)) {
				this.vulnerabilityIconQueue.add(function() { 
					return this.loadVulnerabilityIcon(workspace , backendUrl, skipEmpty, binding.oModel.oData[id], id, listModel)
				}.bind(this), {
					priority: 2,
					id: id
				})
			}
		}.bind(this))
		console.log('update triggered')
	}, 1000),

	clone: function(obj) {
		if (null == obj || "object" != typeof obj) return obj;
		var copy = obj.constructor();
		for (var attr in obj) {
			if (obj.hasOwnProperty(attr)) copy[attr] = obj[attr];
		}
		return copy;
	},

	raiseNotificationAlert: function(newData){

		jQuery.sap.require("sap.m.MessageBox");

		for(var i = 0; i< this.data.length; i++){
			if(this.data.hasOwnProperty(i)){
				for(var j = 0; j< newData.length; j++){
					if(newData.hasOwnProperty(j)){

						if(this.data[i].component_id == newData[j].component_id){

							if(this.data[i].status != "alert" && newData[j].status == "alert"){
								// console.dir(newData[j]);
								sap.m.MessageBox.alert(
										"ALERT: " + newData[j].lastAlertText
								);
							}
						}

					}

				}
			}
		}
	},
	

	
	handleSettings: function (oEvent) {
		this.buttonSource = oEvent.getSource();

		// create popover
		if (!this.oPopoverSettings) {
			this.oPopoverSettings = new sap.m.Popover("settings_popover", {
				title: "Settings",
				//   placement: sap.m.PlacementType.Top, 
				footer: new sap.m.Bar({
					contentRight: [
						new sap.m.Button({
							text: "Edit Space",
							icon: "sap-icon://edit",
							press: function () {

								if (sap.ui.getCore().byId('idSpace').getSelectedKey() == "A5344E8A6D26617C92A0CAD02F10C89C")
									sap.m.MessageBox.warning(
										"The public space cannot be modified."
									);
								else
									this.editSpace(false, this.buttonSource);

							}.bind(this)
						}),

						new sap.m.Button({
							text: "Save",
							icon: "sap-icon://save",
							press: function () {
								let config = model.Config
								let core = sap.ui.getCore()
								if (core.byId('idSpace').getSelectedItem() == null && core.byId('idSpace')._lastValue == "") {
									sap.m.MessageBox.warning("No space selected, the default will be used.");
								}
								if (!core.byId('idListSize').getValue() || (!this.validateListSize(sap.ui.getCore().byId('idListSize').getValue()))) {
									sap.m.MessageBox.warning("Only values in the range 1-100 are allowed.");
									return;
								} else {
									config.setListSize(core.byId('idListSize').getValue());
								}
								if (core.byId('idHostURL').getValue() != null && core.byId('idHostURL').getValue() != "" && core.byId('idHostURL').getValue() != config.getHost()) {
									config.setHost(core.byId('idHostURL').getValue());
								}
								if (core.byId('idTenant') != undefined && core.byId('idTenant').getValue() != null && core.byId('idTenant').getValue() != "" && core.byId('idTenant').getValue() != config.getTenant()) {
									config.setTenant(core.byId('idTenant').getValue());
								}
								if (core.byId('idSpace').getSelectedItem() != null && core.byId('idSpace').getSelectedKey() != "" && core.byId('idSpace').getSelectedKey() != config.getSpace()) {
									config.setSpace(core.byId('idSpace').getSelectedKey());
								} else if (core.byId('idSpace').getSelectedItem() == null && core.byId('idSpace')._lastValue != "" && core.byId('idSpace')._lastValue != config.getSpace()) {
									config.setSpace(core.byId('idSpace')._lastValue);
								}
								if (core.byId('idCiaURL').getValue() != null && core.byId('idCiaURL').getValue() != "" && core.byId('idCiaURL').getValue() != config.getCiaHost()) {
									config.setCiaHost(core.byId('idCiaURL').getValue());
								}
//								if (core.byId('idSkipEmpty').getState() != config.getSkipEmpty()) {
//									config.setSkipEmpty(core.byId('idSkipEmpty').getState());
//								}


								//************* clean Component and reset router 
								//TODO: how to get (or set) Component.view.xml id?
								core.byId("splitApp").removeDetailPage("__xmlview1");
								//TODO: how to reset router?
								//window.history.go(-1);
								//this.router = sap.ui.core.UIComponent.getRouterFor(this);
								//this.router.initialize();
								// ************* 

								// Clear the vulnerability icon's queue of possible ongoing calls
								this.vulnerabilityIconQueue.clear()

								window.location.hash = '';
								this.reloadData();
								core.byId('settings_popover').close();
							}.bind(this)
						})
					],
					contentLeft: [new sap.m.Button({
						text: "Close",
						icon: "sap-icon://close",
						press: function () {
							sap.ui.getCore().byId('settings_popover').close();
						}
					})]
				}),
				content: [
					new sap.m.InputListItem({
						label: "Space",
						content: new sap.m.ComboBox("idSpace", {
							showSecondaryValues: true,
							filterSecondaryValues: true,
							items: {
								path: "/",
								template: new sap.ui.core.ListItem({
									key: '{spaceToken}',
									text: '{spaceName}',
									additionalText: '{spaceToken}'
								})
							}
						})
					}),
					new sap.m.InputListItem({
						label: "Backend URL",
						content: new sap.m.Input({

							id: "idHostURL",
							type: sap.m.InputType.Text,
							//  placeholder: 'Enter host address (default: 127.0.0.1) ...',
							//  width: "100%",
							value: model.Config.getHost()
						})
					}),
					new sap.m.InputListItem({
						label: "Artifact Analyzer URL",
						content: new sap.m.Input({

							id: "idCiaURL",
							type: sap.m.InputType.Text,
							//  placeholder: 'Enter host address (default: 127.0.0.1) ...',
							//  width: "100%",
							value: model.Config.getCiaHost()
						})
					})
					,
					new sap.m.InputListItem({
						label: "Application List Size",
						content: new sap.m.Input({

							id: "idListSize",
							type: sap.m.InputType.Text,
							//  placeholder: 'Enter host address (default: 127.0.0.1) ...',
							//  width: "100%",
							placeholder: "(1-100)",
							value: model.Config.getListSize()
						})
					})
//					,
//					new sap.m.InputListItem({
//						label: "Skip Empty Apps",
//						content: new sap.m.Switch({
//							id: "idSkipEmpty",
//							state: model.Config.getSkipEmpty()
//							//	  width: "100%",
//						})
//					})

				]
			});
			if (model.Config.settings.dev == true) {
				var oTenantInput = new sap.m.InputListItem({
					label: "Tenant",
					content: new sap.m.Input({
						id: "idTenant",
						type: sap.m.InputType.Text,
						value: model.Config.getTenant(),
						liveChange: function () {
							model.Config.loadSpaces(sap.ui.getCore().byId('idTenant').getValue());
						}
					})
				});
				this.oPopoverSettings.addContent(oTenantInput);
			}
			this.getView().addDependent(this.oPopoverSettings);
		}
		//refresh form with configured values
		sap.ui.getCore().byId('idHostURL').setValue(model.Config.getHost());
		sap.ui.getCore().byId('idCiaURL').setValue(model.Config.getCiaHost());
		if (sap.ui.getCore().byId('idTenant') != undefined)
			sap.ui.getCore().byId('idTenant').setValue(model.Config.getTenant());
	//	sap.ui.getCore().byId('idSkipEmpty').setState(model.Config.getSkipEmpty());

		//retrieve current spaces
		model.Config.loadSpaces();

		var items = sap.ui.getCore().byId('idSpace').getItems();
		var publicSpace = false;
		for (var i in items) {
			if (items[i].getKey() == model.Config.getSpace()) {
				sap.ui.getCore().byId('idSpace').setSelectedItem(items[i]);
				sap.ui.getCore().byId('idSpace').setSelectedKey(items[i].getKey());
				publicSpace = true;
				break;
			}
		}
		if (!publicSpace) {
			//			sap.m.MessageBox.warning(
			//    				"The configured space is NOT public and is not shown in the list."
			//    			);
			var privSpace = new sap.ui.core.ListItem({
				key: model.Config.getSpace(),
				text: '*private space configured*',
				additionalText: model.Config.getSpace()
			});
			sap.ui.getCore().byId('idSpace').addItem(privSpace);
			sap.ui.getCore().byId('idSpace').setSelectedItem(privSpace);
			sap.ui.getCore().byId('idSpace').setSelectedKey(privSpace.getKey());
			sap.ui.getCore().byId('idSpace').setShowSecondaryValues(true);
			sap.ui.getCore().byId('idSpace').setFilterSecondaryValues(true);

		}
		if (this.oPopoverSettings.isOpen()) {
			this.oPopoverSettings.close();
		} else {
			this.oPopoverSettings.openBy(this.buttonSource);
		}
	},
	
	openDoc: function(){
		model.Config.openWiki("user/manuals/frontend/#start-page");
	},
	
	openHelp : function(oEvent) {
    	model.Config.openWiki("user/support/");
	},
	
	onHomePress: function(){
		window.location.href = '/apps/';
	},
	
	createSpace: function(oEvent){
		this.buttonSourceCrt = oEvent.getSource();
		this.editSpace(true,this.buttonSourceCrt );
   	},
	
	onInfoPress: function(oEvent) {
        // create popover
        if (!this.oPopover) {
            this.oPopover = sap.ui.xmlfragment("view.Popover", this);
            this.getView().addDependent(this.oPopover);
        }
        if (this.oPopover.isOpen()) {
            this.oPopover.close();
        } else {
            this.oPopover.openBy(oEvent.getSource());
        }
    }
});
