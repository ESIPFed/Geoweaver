

(function () {
	var width = 1160,
	    height = 600;

	var color = d3.scale.category20();

	var moleculeExamples = {};

	var radius = d3.scale.sqrt()
	    .range([0, 6]);

	var selectionGlove = glow("selectionGlove").rgb("#0000A0").stdDeviation(7);
	var atomSelected;
	var atomClicked = function (dataPoint) {
	 	if (dataPoint.symbol === "H")
	 		return;

	 	if (atomSelected)
	 		atomSelected.style("filter", "");

	 	atomSelected = d3.select(this)
	 					 				 .select("circle")
	 						    	 .style("filter", "url(#selectionGlove)");
	};

	var bondSelected;
	var bondClicked = function (dataPoint) {
	 	Messenger().post({
				  message: 'New Bond Selected',
				  type: 'info',
				  hideAfter: 3,
				  showCloseButton: true
				});
	 	
	 	if (bondSelected)
	 		bondSelected.style("filter", "");

	 	bondSelected = d3.select(this)
	 									 .select("line")
	 						    	 .style("filter", "url(#selectionGlove)");
	};

	var generateRandomID = function () {
		return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
		  var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
		  return v.toString(16);
		});
	}

	var svg = d3.select("#moleculeDisplay").append("svg")
	    .attr("width", width)
	    .attr("height", height)
	    .call(selectionGlove);

  var getRandomInt = function (min, max) {
	  return Math.floor(Math.random() * (max - min + 1) + min);
	}

	window.loadMolecule = function () {
	  	vex.dialog.open({
				message: 'Copy your saved molecule data:',
				input: "Molecule: <br/>\n<textarea id=\"molecule\" name=\"molecule\" value=\"\" style=\"height:150px\" placeholder=\"Saved Molecule Data\" required></textarea>",
				buttons: [
					$.extend({}, vex.dialog.buttons.YES, {
					text: 'Load'
				}), $.extend({}, vex.dialog.buttons.NO, {
					text: 'Cancel'
				})
				],
				callback: function(data) {
					if (data !== false) {
						
						newMoleculeSimulation(JSON.parse(data.molecule));
					}
				}
			});
	};

	var newMoleculeSimulation = function (newMolecule, example) {
		// Might be super dirty, but it works!
		$('#moleculeDisplay').empty();
		svg = d3.select("#moleculeDisplay").append("svg")
				    .attr("width", width)
				    .attr("height", height)
				    .call(selectionGlove);
		if (example)
			newMolecule = newMolecule[example];
		newMolecule = $.extend(true, {}, newMolecule);
		orgoShmorgo(newMolecule);
		
		Messenger().post({
		  message: 'New Molecule Loaded',
		  type: 'success',
		  showCloseButton: true,
		  hideAfter: 2
		});
	};

	window.loadMoleculeExample = function () {
		newMoleculeSimulation (moleculeExamples, $('#moleculeExample').val().trim());
	};

	$.getJSON("molecules.json", function(json) {
    moleculeExamples = json;
    newMoleculeSimulation (moleculeExamples, '2-amino-propanoic_acid');
	});
	
	var orgoShmorgo = function(graph) {
	  var nodesList, linksList;
	  nodesList = graph.nodes;
	  linksList = graph.links;
	  		

	  var force = d3.layout.force()
	    						.nodes(nodesList)
	    						.links(linksList)
	    						.size([width, height])
	    						.charge(-400)
	    						.linkStrength(function (d) { return d.bondType * 1;})
	    						.linkDistance(function(d) { return radius(d.source.size) + radius(d.target.size) + 20; })
	    						.on("tick", tick);

	  var links = force.links(),
	  		nodes = force.nodes(),
	  		link = svg.selectAll(".link"),
	  		node = svg.selectAll(".node");

	  buildMolecule();

	  function buildMolecule () {
	  	// Update link data
	  	link = link.data(links, function (d) {return d.id; });

		  // Create new links
		  link.enter().insert("g", ".node")
		      .attr("class", "link")
		      .each(function(d) {
		      	// Add bond line
		      	d3.select(this)
		      		.append("line")
							.style("stroke-width", function(d) { return (d.bondType * 3 - 2) * 2 + "px"; });

						// If double add second line
						d3.select(this)
							.filter(function(d) { return d.bondType >= 2; }).append("line")
							.style("stroke-width", function(d) { return (d.bondType * 2 - 2) * 2 + "px"; })
							.attr("class", "double");

						d3.select(this)
							.filter(function(d) { return d.bondType === 3; }).append("line")
							.attr("class", "triple");

						// Give bond the power to be selected
						d3.select(this)
							.on("click", bondClicked);
		      }); 

		  // Delete removed links
		  link.exit().remove(); 

		  // Update node data
	  	node = node.data(nodes, function (d) {return d.id; });

	    // Create new nodes
		  node.enter().append("g")
		      .attr("class", "node")
		      .each(function(d) {
		      	// Add node circle
			      d3.select(this)
			      	.append("circle")
		      		.attr("r", function(d) { return radius(d.size); })
		      		.style("fill", function(d) { return color(d.symbol); });

		        // Add atom symbol
			      d3.select(this)
			      	.append("text")
							.attr("dy", ".35em")
							.attr("text-anchor", "middle")
							.text(function(d) { return d.symbol; });

						// Give atom the power to be selected
						d3.select(this)
							.on("click", atomClicked);

						// Grant atom the power of gravity	
						d3.select(this)
							.call(force.drag);
			    });

		  // Delete removed nodes
	    node.exit().remove();

		  force.start();
	  }

	  window.saveMolecule = function () {
	  	var specialLinks = [], specialNodes = [], nodeIdArr = [];
	  	for (var i = nodes.length - 1; i >=0; i--) {
	  		specialNodes.push({
	  				symbol: nodes[i].symbol,
						size: nodes[i].size,
						x: nodes[i].x,
						y: nodes[i].y,
						id: nodes[i].id,
						bonds: nodes[i].bonds
					});
	  		nodeIdArr.push(nodes[i].id);
	  	}
	  	for (var i = links.length - 1; i >=0; i--) {
	  		specialLinks.push({
						source: nodeIdArr.indexOf(links[i].source.id),
						target: nodeIdArr.indexOf(links[i].target.id),
						id: links[i].id,
						bondType: links[i].bondType
					});
	  	}
	  	molecule = {
			    nodes: specialNodes,
			    links: specialLinks
			};
	  	vex.dialog.open({
				message: 'To save your current molecule, copy the data below. Next time you visit click on the load molecule and input your saved data:',
				input: "Molecule: <br/>\n<textarea id=\"atoms\" name=\"atoms\" value=\"\" style=\"height:150px\" placeholder=\"Molecule Data\">" + JSON.stringify(molecule) + "</textarea>",
				buttons: [
					$.extend({}, vex.dialog.buttons.YES, {
						text: 'Ok'
					})
				],
				callback: function(data) {}
			});
	  };

	  window.changeBond = function (newBondType) {
	  	if (!bondSelected) {
				Messenger().post({
				  message: 'No Bond Selected',
				  type: 'error',
				  showCloseButton: true
				});
				return;
			}
	  	var bondData = getAtomData(bondSelected);
	  	var changeInCharge = newBondType - bondData.bondType;
	  	var bondChangePossible = function (bond) {
	  		return (bond.target.bonds + changeInCharge <= atomDB[bond.target.symbol].lonePairs && bond.source.bonds + changeInCharge <= atomDB[bond.source.symbol].lonePairs);
	  	};

	  	if (!newBondType || newBondType < 1 || newBondType > 3) {
	  		Messenger().post({
				  message: 'Internal error :(',
				  type: 'error',
				  showCloseButton: true
				});
				return;
	  	}
			else if (!bondChangePossible(bondData, newBondType)) {
				Messenger().post({
				  message: 'That type of bond cannot exist there!',
				  type: 'error',
				  showCloseButton: true
				});
				return;
			}

			for (var i = links.length - 1; i >= 0; i--) {
				if (links[i].id === bondData.id) {
					var changeInCharge = newBondType - bondData.bondType;
					var source = retriveAtom(links[i].source.id),
							target = retriveAtom(links[i].target.id);
					if (changeInCharge === 2) {
						removeHydrogen(source);
						removeHydrogen(source);
						removeHydrogen(target);
						removeHydrogen(target);
					}
					else if (changeInCharge === 1) {
						removeHydrogen(source);
						removeHydrogen(target);
					}
					else if (changeInCharge === -1) {
						addHydrogens(source, 1);
						addHydrogens(target, 1);
					}
					else if (changeInCharge === -2) {
						addHydrogens(source, 1);
						addHydrogens(source, 1);
						addHydrogens(target, 1);
						addHydrogens(target, 1);
					}
					source.bonds += changeInCharge;
					target.bonds += changeInCharge;
					
					// Remove old bond, create new one and add it to links list
					// Simple change of bond value is buggy
					links.splice(i, 1);
					var newBond = {
		 				source: bondData.source,
		 				target: bondData.target, 
		 				bondType: newBondType, 
		 				id: generateRandomID()
		 			};
		 			links.push(newBond);
		 			
		 			// Clear previous bond selection
		 			bondSelected.style("filter", "");
		 			bondSelected = null;
		 			
		 			break;
				}
			}
			buildMolecule();
	  };

	  window.addAtom = function (atomType) {
	  	if (!atomType) {
	  		Messenger().post({
				  message: 'Internal error :(',
				  type: 'error',
				  showCloseButton: true
				});
				return;
	  	}
	  	else if (!atomSelected) {
				Messenger().post({
				  message: 'No Atom Selected',
				  type: 'error',
				  showCloseButton: true
				});
				return;
			}
			else if (!canHaveNewBond(getAtomData(atomSelected))) {
				Messenger().post({
				  message: 'Atom Can\'t Take Anymore Bonds',
				  type: 'error',
				  showCloseButton: true
				});
			}
			else
	  		addNewAtom(atomType, atomDB[atomType].size);
	  };

	 	function canHaveNewBond (atom) {
	 		return atom.bonds < atomDB[atom.symbol].lonePairs;
	 	}

	 	function getAtomData (d3Atom) {
	 		return d3Atom[0][0].parentNode.__data__;
	 	}

	 	function addHydrogens (atom, numHydrogens) {
	 		var newHydrogen = function () {
	 			return {
		 			symbol: 'H',
		 			size: '1',
		 			bonds: 1,
		 			id: generateRandomID (),
		 			x: atom.x + getRandomInt (-15, 15),
		 			y: atom.y + getRandomInt (-15, 15)
		 		};
	 		};
	 		var tempHydrogen;
	 		for (var i = 0; i < numHydrogens; i++) {
	 			tempHydrogen = newHydrogen();
	 			nodes.push(tempHydrogen);
	 			links.push({
	 				source: atom,
	 				target: tempHydrogen, 
	 				bondType: 1, 
	 				id: generateRandomID()
	 			});	
	 		}
	 	}

	 	function removeHydrogen (oldAtom) {
	 		var target, source, bondsArr = getBonds(oldAtom.id);
	 		for (var i = bondsArr.length - 1; i >= 0; i--) {
	 			target = bondsArr[i].target, source = bondsArr[i].source;
				if (target.symbol === 'H' || source.symbol === 'H' ) {
					var hydroId = source.symbol === 'H'? 
																		source.id: 
																		target.id;
					removeAtom(hydroId);
					return;
				}
	 		}
	 	}

	 	function removeAtom (id) {
	 		var atomToRemove = retriveAtom(id);
	 		var bondsArr = getBonds(id);
	 		var atomsArr = [atomToRemove.id];
	 		
	 		for (var i = bondsArr.length - 1; i >= 0; i--) {
	 			// Add atom that is a hydrogen
	 			if (bondsArr[i].source.symbol === 'H')
	 				atomsArr.push(bondsArr[i].source.id);
	 			else if (bondsArr[i].target.symbol === 'H')
	 				atomsArr.push(bondsArr[i].target.id); 
	 			else {
	 					// Give non-hydrogen bonded atom it's lone pairs back
						var nonHydrogenAtom = bondsArr[i].target.id !== id ? 
																									 	'target':
																										'source';
							
						bondsArr[i][nonHydrogenAtom].bonds -= bondsArr[i].bondType;
		 				addHydrogens(bondsArr[i][nonHydrogenAtom], bondsArr[i].bondType);
	 			}
	 			// Convert atom obj to id for later processing
	 			bondsArr[i] = bondsArr[i].id;
	 		}

	 		var spliceOut = function (arr, removeArr) {
		 		for (var i = arr.length - 1; i >= 0; i--) {
		 				if (removeArr.indexOf(arr[i].id) !== -1) {
		 					arr.splice(i, 1);
		 				}
		 		}
		 		return arr;
		 	};

	 		// Remove atoms marked
	 		nodes = spliceOut (nodes, atomsArr);
	 		
	 		// Remove bonds marked
	 		links = spliceOut (links, bondsArr);

	 	};

	 	var retriveAtom = function  (atomID) {
	 		for (var i = nodes.length - 1; i >= 0; i--) {
	 			if (nodes[i].id === atomID)
	 				return nodes[i];
	 		}
	 		return null;
	 	};

	  function addNewAtom (atomType, atomSize) {
			var newAtom = {
						symbol: atomType,
						size: atomSize,
						x: getAtomData(atomSelected).x + getRandomInt (-15, 15),
						y: getAtomData(atomSelected).y + getRandomInt (-15, 15),
						id: generateRandomID (), // Need to make sure is unique
						bonds: 1
					},
		  		n = nodes.push(newAtom);

		  getAtomData(atomSelected).bonds++; // Increment bond count on selected atom
		 	addHydrogens(newAtom, atomDB[atomType].lonePairs - 1); // Adds hydrogens to new atom
		 	removeHydrogen(getAtomData(atomSelected)); // Remove hydrogen from selected atom
		  
		  links.push({
		  	source: newAtom, 
		  	target: getAtomData(atomSelected), 
		  	bondType: 1, 
		  	id: generateRandomID()
		  }); // Need to make sure is unique
		  
	  	buildMolecule();
	  }

	  var getBonds = function (atomID) {
	  	var arr = [];
	  	for (var i = links.length - 1; i >= 0; i--) {
	  		if (links[i].source.id === atomID || links[i].target.id === atomID)
	  			arr.push(links[i]);
	  	}
	  	return arr;
	  }

	  window.deleteAtom = function () {
	  	var oneNonHydrogenBond = function (atom) {
	  		var atomBonds = getBonds(atom.id);
	  		var counter = 0;
	  		for (var i = atomBonds.length - 1; i >= 0; i--) {
	  			if (atomBonds[i].source.symbol !== 'H' && atomBonds[i].target.symbol !== 'H')
	  				counter++;
	  		}
	  		return counter === 1;
	  	};

	  	if (!atomSelected) {
				Messenger().post({
				  message: 'No Atom Selected',
				  type: 'error',
				  showCloseButton: true
				});
				return;
			}
			else if (!oneNonHydrogenBond(getAtomData(atomSelected))) {
				Messenger().post({
				  message: 'Atom Must have only one non-hydrogen bond to be removed',
				  type: 'error',
				  showCloseButton: true
				});
				return;
			}

			removeAtom(getAtomData(atomSelected).id);
			atomSelected = null;
			buildMolecule ();
	  };

	  function tick() {
	  	//Update old and new elements
	    link.selectAll("line")
	        .attr("x1", function(d) { return d.source.x; })
	        .attr("y1", function(d) { return d.source.y; })
	        .attr("x2", function(d) { return d.target.x; })
	        .attr("y2", function(d) { return d.target.y; });

	    node.attr("transform", function(d) {return "translate(" + d.x + "," + d.y + ")"; });
	  }
	};
})();