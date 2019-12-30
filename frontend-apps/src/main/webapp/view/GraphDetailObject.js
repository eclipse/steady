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
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
jQuery.sap.declare("view.GraphDetailObject");

sap.ui.core.Control.extend("view.GraphDetailObject", {

    //TODO: Do not use absolute values but relative to the actual window size
	metadata: {
        properties: {
          width: {type: 'int', defaultValue: "1200" },
          height: {type: 'int', defaultValue: "1200"}
        }
    },
    
    init : function() {
    	this.allnodes = [];
    	this.alledges = [];
    	this.risks = [];
    	this.app = {};
    	this.bugId = "";
    	this.archiveId = "";
    //	this.graphid = "";
    	this.width = this.getWidth()-100, this.height = this.getHeight()-200;
    },
    
    setData : function(_nodes, _edges) {
    	this.allnodes = _nodes;
    	this.alledges = _edges;
    },
    
    setRisks : function(_risks){
    	_risks.sort(function(a, b){return a-b});
    	this.risks = _risks;
    },
    
    setMetadata : function(_bugid, _archiveid, _app, _graphid) {
    	this.budId = _bugid;
    	this.archiveId = _archiveid;
    	this.app = _app;
    	//this.graphid = _graphid;
    	
    	console.log("Set metadata to: \r\nApplication context: " + this.app.groupid + "	" + this.app.artifactid + "	" + this.app.version
    			//+ "\r\ngraphid: " + this.graphid
    			);
    	
    },
    
    createGraph : function() {
    	console.log("view.GraphDetailObject.createGraph()");
        var oGraphLayout = new sap.m.VBox({alignItems:sap.m.FlexAlignItems.Center,justifyContent:sap.m.FlexJustifyContent.Center});
        var oGraphFlexBox = new sap.m.FlexBox({height:"1000px",alignItems:sap.m.FlexAlignItems.Center});
        
        this.sParentId=oGraphFlexBox.getIdForLabel();
        oGraphLayout.addItem(oGraphFlexBox);
     
        return oGraphLayout;
    },
    
    renderer : function(oRm, oControl) {
    	  console.log("view.GraphDetailObject.renderer()");
          var layout = oControl.createGraph();
		  oRm.write("<div");
		  oRm.writeControlData(layout); // writes the Control ID and enables event handling - important!
		  oRm.writeClasses(); 
		  oRm.write(">");
		  oRm.renderControl(layout);
		  oRm.addClass('verticalAlignment');
		  oRm.write("</div>");
    },
    
    onAfterRendering: function(){
        console.log("view.GraphDetailObject.onAfterRendering()");
        console.log("ParentId: " + this.sParentId);
        console.log("Initializing SVG with size: [ " + this.width + ", " + this.height + " ]");
        
	    //svg initialization
        var svg = d3.select("#" + this.sParentId).append("svg")
			        .attr("width", this.width)
					.attr("height", this.height)
					.attr("viewBox", "0 0 " + this.width + " " + this.height );
        
        this.update(svg);
    },

    update : function(svg) {
    	console.log("[GraphDetailObject] Updating graph...");
    	svg.selectAll('*').remove();
    	
    	//variables initialization
    	var nodecolor = d3.scale.category20();
	    var allnodes = this.allnodes, alledges = this.alledges, width = this.width, height = this.height;
	    
	    
    	var force = d3.layout.force(); 		
  		//attach nodes and edges data to force layout
  		force.nodes(allnodes)
  			 .links(alledges)
  	      	 .gravity(0.1)
  			 .charge(-5000)
  			 .linkDistance(70)
  			 .friction(0.1)
  			 .linkStrength(1)
  			 .size([width, height])
  	      	 .start();
		 
  		
		//arrows
		var arrows = svg.append("svg:defs").selectAll("marker")
						.data(["end","red", "orange", "yellow", "black"])
					.enter().append("svg:marker")
					//	.attr("id", "arrow")
						 .attr('id', function(d){ return d})
						.attr("viewBox", "0 -5 10 10")
						.attr("refX", 17)
						.attr("refY", 0)
						.attr("markerWidth", 7)
						.attr("markerHeight", 7)
						.attr("orient", "auto")
					.append("svg:path")
						.attr('fill', function(d) { return d;})
						.attr("d", "M0,-5L10,0L0,5");

		
		//edges
		var links = svg.append("svg:g").selectAll("line.link")
						.data(force.links());
		//console.log(force.links());
		links.enter().append("svg:line");//draw edges between nodes as a line element
							
//							var color = "#000000";
//							if ( !param.visible ) color = "transparent";
//							else color = nodecolor(param.exploitability);
//							return "stroke:" + "#0a7fd1" +"; stroke-width: 4px; stroke-opacity: 0.3"; //marker-end: url(#arrow); 
//						}
						
		links.exit().remove();	
		links.attr("class", "link")
			.attr("style", function(param) {
			if(param.type == "TST") {return "stroke: #FF0000; stroke-width: 7px; stroke-opacity: 0.3;";}
			else if(param.type == "RA") {return "stroke: black; stroke-width: 2px; marker-end: url(#black);";}
			else if(param.type == "NN")	{
			if(param.exploitability == risks[0]) return "stroke: red; stroke-width: 2px; marker-end: url(#red);";
			else if(param.exploitability == risks[1]) return "stroke: orange; stroke-width: 2px;stroke-opacity: 0.6; marker-end: url(#orange);";
			else if(param.exploitability == risks[2]) return "stroke: yellow; stroke-width: 2px;stroke-opacity: 0.4; marker-end: url(#yellow);";
			else return "stroke: black; stroke-width: 2px; marker-end: url(#black);";
			}
			});
		console.log(links.length);
//	

		//nodes
		var nodes = svg.selectAll("g.node")
						.data(force.nodes());
		//draw nodes as a g element				
		var enterNodes	=	nodes.enter().append("svg:g")
						.attr("class", "node")
						.call(force.drag);
		//append a circle for each node				
		enterNodes.append("svg:circle")
			 .attr("r", function(param) { 
				 	if ( !param.visible ) return "0";
				 	else if( param.type == "PACK" ) return "13";
					return ( param.nodetype == "entrypoint" ) ? "9" : ( ( param.nodetype == "target" ) ? "11" : "7"); })
			 .attr("fill", function(param) { 
					var color = "#000000";
					if ( !param.visible ) color = "transparent";
					else if( param.nodetype == "entrypoint" ) color = "#00FF00";
					else if ( param.nodetype == "target" ) color = "#FF0033";
					else color = nodecolor(param.cls);
					return color; })
			 .attr("stroke", function(param) {
				 	if( param.type == "PACK" || param.traced == "NA") return "#ffffcc"
				 	else return "black";
			 })
			 .attr("stroke-width", "2px");

		//append the text/tooltips for each node: method name only
		var tooltips = enterNodes.append("svg:text")
							.attr("x", 7)
							.attr("dy", "1.3em")
							.text( function(param) { 
								if ( !param.visible ) return "";
								else if( param.type == "PACK" ) return param.qname;
								else return param.method;
		});
		
		var tooltips_more = enterNodes.append("svg:text")
										.attr("x", "7")
										.attr("dy", "2.6em")
										.text( function(param) { 
												if ( !param.visible ) return "";
												else if(param.type == "PACK") return param.qname;
												else if(param.type == "CONS") return param.method_param;
												else return (param.cls + "." + param.method_param) })
										.style("visibility", "hidden")
										.style("fill", "black");
		
		//add mouseover and mouseout action
		enterNodes.on("mouseover", function(param) {
		     		  d3.select(tooltips[0][param.index]).text( function(nd) {
		     			  if ( !param.visible ) return "";
		     			  else if( nd.type == "PACK" ) return "Collapsed Node";
		     			  else {
		     				  if(nd.lib!=null){
		     					  if(nd.lib.libraryId!=null){
		     						  return (nd.lib.libraryId.group + ":" + nd.lib.libraryId.artifact + ":" + nd.lib.libraryId.version + " -- " + nd.qname.substring(0, nd.qname.lastIndexOf(".")).replace(("."+nd.cls), ""));
		     					  }
		     					  else if(nd.dep!=null)
		     						 return (nd.dep.filename + " -- " + nd.qname.substring(0, nd.qname.lastIndexOf(".")).replace(("."+nd.cls), ""));
		     					  else
		     						 return (nd.lib.digest + " -- " + nd.qname.substring(0, nd.qname.lastIndexOf(".")).replace(("."+nd.cls), ""));
		     				  }
		     				  else 
		     					 return (nd.qname.substring(0, nd.qname.lastIndexOf(".")).replace(("."+nd.cls), ""));
		     			  }
		     		  })
		     		  
		     		  d3.select(tooltips_more[0][param.index]).style("visibility", "visible");
		     		  d3.select(this).select("circle").attr("r", 13)
		 	 	  })
		 	 	  .on("mouseout", function(param) {
		 	 		  d3.select(tooltips[0][param.index]).text( function(nd) { 
			 	 			if( nd.type == "PACK" ) return nd.qname;
							else return nd.method; })
					  d3.select(tooltips_more[0][param.index]).style("visibility", "hidden");
		 	 		  d3.select(this).select("circle").attr("r", function(nd) {
							if( nd.type == "PACK" ) return "13";
							return ( nd.nodetype == "entrypoint" ) ? "9" : ( ( nd.nodetype == "target" ) ? "11" : "7"); })
		});
				
		nodes.exit().remove();
		
		force.on("tick",function() {
				//calculate the positions for all nodes and all edges
				nodes.attr("transform",function(param) {
						param.x = Math.max(30, Math.min(width - 170, param.x));
						param.y = Math.max(20, Math.min(height - 50, param.y));
						return "translate(" + param.x + "," + param.y + ")"});
				links.attr("x1", function(param) {return param.source.x})
					 .attr("y1", function(param) {if(param.exploitability == risks[1]) return param.source.y + 5;
					 								if(param.exploitability == risks[2]) return param.source.y + 10;
					 								else return param.source.y })
					 .attr("x2", function(param) {return param.target.x})
					 .attr("y2", function(param) {if(param.exploitability == risks[1]) return param.target.y + 5;
					 								if(param.exploitability == risks[2]) return param.target.y + 10;
					 								else return param.target.y})
		});
    }
});