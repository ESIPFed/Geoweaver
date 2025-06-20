// Extend GW.workspace with additional properties instead of replacing it
GW.workspace = {
  selectedWorkflow: undefined,
  theGraph: null,

  currentmode: 1, //1: normal; 2: monitor

  jsFrame: null,

  svg: null,

  keymap: {},

  if_any_frame_on: false,

  workflow_unsaved: false,

  showNonSaved: function () {
    
    GW.workspace.workflow_unsaved = true;

    $("#main-workspace-tab").html("Weaver *");
  
  },

  closeOtherFrames: function () {
    try {
      if (this.jsFrame) {
        this.jsFrame.closeFrame();
      }
    } finally {
    }
  },

  showSaved: function () {

    GW.workspace.workflow_unsaved = false;

    $("#main-workspace-tab").html("Weaver");
  
  },

  resizeIframe: function (obj) {},

  openModalWindow: function () {
    $("#toolbar-left").css("z-index", 1);

    $("#test-jsframe").addClass("fa-shower");

    var content =
      '<div height="100%">' +
      '<iframe width="100%" height="600" frameborder="0" scrolling="no" onload="GW.workspace.resizeIframe(this)" ' +
      ' id="jupyter-iframe" src="'+GW.path.getBasePath()+'web/jupyter-proxy/"></iframe>' +
      "</div>";

    GW.workspace.jsFrame = GW.process.createJSFrameDialog(
      720,
      640,
      content,
      "Test Jupyter Notebook Server",
    );
  },

  update_skip_process: function (workflow_process_id, skip_status) {
    for (var i = 0; i < GW.workspace.theGraph.nodes.length; i += 1) {
      if (GW.workspace.theGraph.nodes[i].id == workflow_process_id) {
        GW.workspace.theGraph.nodes[i].skip = skip_status;
        break;
      }
    }

    GW.workspace.theGraph.updateGraph();
  },

  saveWorkflow: function () {
    if (GW.workspace.checkIfWorkflowValid()) {
      if (GW.workspace.theGraph.nodes.length != 0) {
        var saveEdges = [];

        GW.workspace.theGraph.edges.forEach(function (val, i) {
          saveEdges.push({ source: val.source, target: val.target });
        });

        GW.workflow.save(GW.workspace.theGraph.nodes, saveEdges);
      } else {
        alert("No nodes are present!");
      }
    }
  },

  /**
   * Create a new GraphCreator object
   * @svg SVG object
   * @nodes
   * @edges
   */
  GraphCreator: function (svg, nodes, edges) {
    var thisGraph = this;
    thisGraph.idct = 0;

    thisGraph.nodes = nodes || [];
    thisGraph.edges = edges || [];

    thisGraph.state = {
      selectedNode: null,
      selectedEdge: null,
      mouseDownNode: null,
      mouseDownLink: null,
      justDragged: false,
      justScaleTransGraph: false,
      lastKeyDown: -1,
      shiftNodeDrag: false,
      selectedText: null,
    };

    GW.workspace.keymap = {};

    // define arrow markers for graph links
    var defs = svg.append("svg:defs");
    defs
      .append("svg:marker")
      .attr("id", "end-arrow")
      .attr("viewBox", "0 -5 10 10")
      .attr("refX", "32")
      .attr("markerWidth", 5)
      .attr("markerHeight", 5)
      .attr("orient", "auto")
      .append("svg:path")
      .attr("d", "M0,-5L10,0L0,5")
      .attr("fill", "#5f6368"); // Modern gray color for edge arrows

    // define arrow markers for leading arrow
    defs
      .append("svg:marker")
      .attr("id", "mark-end-arrow")
      .attr("viewBox", "0 -5 10 10")
      .attr("refX", 7)
      .attr("markerWidth", 3.5)
      .attr("markerHeight", 3.5)
      .attr("orient", "auto")
      .append("svg:path")
      .attr("d", "M0,-5L10,0L0,5");

    defs
      .append("pattern")
      .attr("id", "diagonalHatch")
      .attr("patternUnits", "userSpaceOnUse")
      .attr("width", 4)
      .attr("height", 4)
      .attr("fill", "#FFF")
      .append("path")
      .attr("d", "M-1,1 l2,-2 M0,4 l4,-4 M3,5 l2,-2")
      .attr("stroke", "#000000")
      .attr("stroke-width", 1);

    // Define pattern that includes the gradient + hatch lines
    var skippedPattern = defs.append("pattern")
      .attr("id", "node-gradient-skipped-pattern")
      .attr("patternUnits", "userSpaceOnUse")
      .attr("width", 10)
      .attr("height", 10);

    // Background fill using the gradient
    skippedPattern.append("rect")
      .attr("width", 10)
      .attr("height", 10)
      .attr("fill", "url(#node-gradient-skipped)");

    // Diagonal hatch lines
    // skippedPattern.append("path")
    //   .attr("d", "M -10,10 L 10,-10 M 0,10 L 20,-10 M 10,10 L 30,-10")
    //   .attr("stroke", "#004400")
    //   .attr("stroke-width", 1);
      
    // Create enhanced 3D effect gradients for nodes
    // Default node gradient (gray)
    var nodeGradientDefault = defs.append("radialGradient")
      .attr("id", "node-gradient-default")
      .attr("cx", "25%")
      .attr("cy", "25%")
      .attr("r", "75%");
      
    nodeGradientDefault.append("stop")
      .attr("offset", "0%")
      .attr("stop-color", "#b0b0b0");
      
    nodeGradientDefault.append("stop")
      .attr("offset", "70%")
      .attr("stop-color", "#909090");
      
    nodeGradientDefault.append("stop")
      .attr("offset", "100%")
      .attr("stop-color", "#707070");

    var nodeGradientSkipped = defs.append("radialGradient")
      .attr("id", "node-gradient-skipped")
      .attr("cx", "25%")
      .attr("cy", "25%")
      .attr("r", "75%");
    
    nodeGradientSkipped.append("stop")
      .attr("offset", "0%")
      .attr("stop-color", "#f0f0f0");  // light center
    
    nodeGradientSkipped.append("stop")
      .attr("offset", "70%")
      .attr("stop-color", "#c0c0c0");  // mid gray
    
    nodeGradientSkipped.append("stop")
      .attr("offset", "100%")
      .attr("stop-color", "#a0a0a0");  // outer edge    
      
    // Running node gradient (blue) with animated arrow
    var nodeGradientRunning = defs.append("radialGradient")
        .attr("id", "node-gradient-running")
        .attr("cx", "50%")
        .attr("cy", "50%")
        .attr("r", "50%");

    nodeGradientRunning.append("stop")
        .attr("offset", "0%")
        .attr("stop-color", "#e3f2fd"); // Lighter blue center

    nodeGradientRunning.append("stop")
        .attr("offset", "100%")
        .attr("stop-color", "#4285f4"); // Deeper blue edge

    // Animated arrow for "Running" state
    var runningAnimation = defs.append("g").attr("id", "running-arrow-animation");
    
    // A more professional, curved arrow shape
    var arrowShape = "M-6,-6 L8,0 L-6,6 Q-2,0 -6,-6 Z";
    // The circular path for the arrow to follow
    var animationPath = "M0,-28 A28,28 0 1,1 0,28 A28,28 0 1,1 0,-28";

    runningAnimation.append("path")
        .attr("d", arrowShape)
        .attr("fill", "white")
        .attr("stroke", "#4285f4")
        .attr("stroke-width", 1)
        .append("animateMotion")
        .attr("dur", "2s")
        .attr("repeatCount", "indefinite")
        .attr("rotate", "auto")
        .attr("path", animationPath);
      
    // Done node gradient (green)
    var nodeGradientDone = defs.append("radialGradient")
      .attr("id", "node-gradient-done")
      .attr("cx", "25%")
      .attr("cy", "25%")
      .attr("r", "75%");
      
    nodeGradientDone.append("stop")
      .attr("offset", "0%")
      .attr("stop-color", "#d4f5e9");
      
    nodeGradientDone.append("stop")
      .attr("offset", "70%")
      .attr("stop-color", "#a8e6cf");
      
    nodeGradientDone.append("stop")
      .attr("offset", "100%")
      .attr("stop-color", "#34a853");
      
    // Failed node gradient (red)
    var nodeGradientFailed = defs.append("radialGradient")
      .attr("id", "node-gradient-failed")
      .attr("cx", "25%")
      .attr("cy", "25%")
      .attr("r", "75%");
      
    nodeGradientFailed.append("stop")
      .attr("offset", "0%")
      .attr("stop-color", "#ffd7d7");
      
    nodeGradientFailed.append("stop")
      .attr("offset", "70%")
      .attr("stop-color", "#ffabab");
      
    nodeGradientFailed.append("stop")
      .attr("offset", "100%")
      .attr("stop-color", "#ea4335");
      
    // Edge gradient
    var edgeGradient = defs.append("linearGradient")
      .attr("id", "edge-gradient")
      .attr("x1", "0%")
      .attr("y1", "0%")
      .attr("x2", "100%")
      .attr("y2", "0%");
      
    edgeGradient.append("stop")
      .attr("offset", "0%")
      .attr("stop-color", "#9aa0a6");
      
    edgeGradient.append("stop")
      .attr("offset", "100%")
      .attr("stop-color", "#5f6368");
      
    // Drag edge gradient
    var edgeGradientDrag = defs.append("linearGradient")
      .attr("id", "edge-gradient-drag")
      .attr("x1", "0%")
      .attr("y1", "0%")
      .attr("x2", "100%")
      .attr("y2", "0%");
      
    edgeGradientDrag.append("stop")
      .attr("offset", "0%")
      .attr("stop-color", "#c2c7ca");
      
    edgeGradientDrag.append("stop")
      .attr("offset", "100%")
      .attr("stop-color", "#9aa0a6");
      
    // Enhanced 3D drop shadow for nodes
    var dropShadow = defs.append("filter")
      .attr("id", "drop-shadow")
      .attr("x", "-50%")
      .attr("y", "-50%")
      .attr("width", "200%")
      .attr("height", "200%");
      
    // Create a stronger blur for better 3D effect
    dropShadow.append("feGaussianBlur")
      .attr("in", "SourceAlpha")
      .attr("stdDeviation", 4)
      .attr("result", "blur");
      
    // Add a slight color adjustment for more realistic shadow
    dropShadow.append("feColorMatrix")
      .attr("in", "blur")
      .attr("type", "matrix")
      .attr("values", "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0")
      .attr("result", "coloredBlur");
      
    // Position the shadow to create 3D effect
    dropShadow.append("feOffset")
      .attr("in", "coloredBlur")
      .attr("dx", 4)
      .attr("dy", 5)
      .attr("result", "offsetBlur");
      
    var feMerge = dropShadow.append("feMerge");
    feMerge.append("feMergeNode")
      .attr("in", "offsetBlur");
    feMerge.append("feMergeNode")
      .attr("in", "SourceGraphic");
      
    // Enhanced hover shadow for nodes
    var dropShadowHover = defs.append("filter")
      .attr("id", "drop-shadow-hover")
      .attr("x", "-50%")
      .attr("y", "-50%")
      .attr("width", "200%")
      .attr("height", "200%");
      
    // Larger blur for hover state
    dropShadowHover.append("feGaussianBlur")
      .attr("in", "SourceAlpha")
      .attr("stdDeviation", 6)
      .attr("result", "blur");
      
    // Add a slight color adjustment for more realistic shadow
    dropShadowHover.append("feColorMatrix")
      .attr("in", "blur")
      .attr("type", "matrix")
      .attr("values", "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.6 0")
      .attr("result", "coloredBlur");
      
    // Position the shadow to create 3D effect
    dropShadowHover.append("feOffset")
      .attr("in", "coloredBlur")
      .attr("dx", 5)
      .attr("dy", 6)
      .attr("result", "offsetBlur");
      
    var feMergeHover = dropShadowHover.append("feMerge");
    feMergeHover.append("feMergeNode")
      .attr("in", "offsetBlur");
    feMergeHover.append("feMergeNode")
      .attr("in", "SourceGraphic");
      
    // Enhanced selected shadow for nodes
    var dropShadowSelected = defs.append("filter")
      .attr("id", "drop-shadow-selected")
      .attr("x", "-50%")
      .attr("y", "-50%")
      .attr("width", "200%")
      .attr("height", "200%");
      
    // Larger blur for selected state
    dropShadowSelected.append("feGaussianBlur")
      .attr("in", "SourceAlpha")
      .attr("stdDeviation", 6)
      .attr("result", "blur");
      
    // Add a color adjustment for highlighted selection
    dropShadowSelected.append("feColorMatrix")
      .attr("in", "blur")
      .attr("type", "matrix")
      .attr("values", "0 0 0 0 0.2 0 0 0 0 0.4 0 0 0 0 0.9 0 0 0 0.7 0")
      .attr("result", "coloredBlur");
      
    // Position the shadow to create 3D effect
    dropShadowSelected.append("feOffset")
      .attr("in", "coloredBlur")
      .attr("dx", 4)
      .attr("dy", 5)
      .attr("result", "offsetBlur");
      
    var feMergeSelected = dropShadowSelected.append("feMerge");
    feMergeSelected.append("feMergeNode")
      .attr("in", "offsetBlur");
    feMergeSelected.append("feMergeNode")
      .attr("in", "SourceGraphic");
      
    // Edge shadow
    var edgeShadow = defs.append("filter")
      .attr("id", "edge-shadow")
      .attr("x", "-50%")
      .attr("y", "-50%")
      .attr("width", "200%")
      .attr("height", "200%");
      
    edgeShadow.append("feGaussianBlur")
      .attr("in", "SourceAlpha")
      .attr("stdDeviation", 2)
      .attr("result", "blur");
      
    edgeShadow.append("feOffset")
      .attr("in", "blur")
      .attr("dx", 1)
      .attr("dy", 1)
      .attr("result", "offsetBlur");
      
    var feMergeEdge = edgeShadow.append("feMerge");
    feMergeEdge.append("feMergeNode")
      .attr("in", "offsetBlur");
    feMergeEdge.append("feMergeNode")
      .attr("in", "SourceGraphic");

    thisGraph.svg = svg;
    thisGraph.svgG = svg.append("g").classed(thisGraph.consts.graphClass, true);
    var svgG = thisGraph.svgG;

    // displayed when dragging between nodes
    thisGraph.dragLine = svgG
      .append("svg:path")
      .attr("class", "link dragline hidden")
      .attr("d", "M0,0L0,0")
      .style("marker-end", "url(#mark-end-arrow)");

    // svg nodes and edges
    thisGraph.paths = svgG.append("g").selectAll("g");
    thisGraph.circles = svgG.append("g").selectAll("g");

    // this listens the drag of nodes
    thisGraph.drag = d3.behavior
      .drag()
      .origin(function (d) {
        return { x: d.x, y: d.y };
      })
      .on("drag", function (args) {
        thisGraph.state.justDragged = true;
        thisGraph.dragmove.call(thisGraph, args);
      })
      .on("dragend", function () {
        // todo check if edge-mode is selected
      });

    // listen for key events
    d3.select(window)
      .on("keydown", function () {
        GW.workspace.keymap[d3.event.keyCode] = "keydown";

        switch (d3.event.keyCode) {
          case 8: //backspace = 8
            // BACKSPACE_KEY was fired in <input id="textbox">
            if (d3.event.target.nodeName.toLowerCase() === "input") {
              d3.event.stopPropagation();
              return;
            }
        }

        thisGraph.svgKeyDown.call(thisGraph);
      })
      .on("keyup", function () {
        GW.workspace.keymap[d3.event.keyCode] = "keyup";
        thisGraph.svgKeyUp.call(thisGraph);
      });
    svg.on("mousedown", function (d) {
      thisGraph.svgMouseDown.call(thisGraph, d);
    });
    svg.on("mouseup", function (d) {
      thisGraph.svgMouseUp.call(thisGraph, d);
    });

    // listen for dragging
    thisGraph.zoom = d3.behavior
      .zoom()
      .on("zoom", function () {
        if (d3.event.sourceEvent.shiftKey) {
          return false;
        } else {
          thisGraph.zoomed.call(thisGraph);
        }
        return true;
      })
      .on("zoomstart", function () {
        var ael = d3.select("#" + thisGraph.consts.activeEditId).node();
        if (ael) {
          ael.blur();
        }
        if (!d3.event.sourceEvent.shiftKey)
          d3.select("body").style("cursor", "move");
      })
      .on("zoomend", function () {
        d3.select("body").style("cursor", "auto");
      });

    svg.call(thisGraph.zoom).on("dblclick.zoom", null);

    // listen for resize
    window.onresize = function () {
      thisGraph.updateWindow(svg);
    };

    // handle download data
    d3.select("#download-input").on("click", function () {
      if (GW.workflow.loaded_workflow != null) {
        var content = `<div class="modal-body">
				
					<div class="row">
				
						<div class="col-md-12">

							<div class="form-check">
								<label>
									<input class="form-check-input" type="radio" name="workflowdownloadoption" value="workflowwithprocesscode">
									<i>Workflow with Process Code</i>
								</label>
							</div>
							<div class="form-check">
								<label>
									<input class="form-check-input" type="radio" name="workflowdownloadoption" value="workflowwithprocesscodehistory">
									<i>Workflow with Process Code and Only Workflow History</i>
								</label>
							</div>
							<div class="form-check">
								<label>
									<input class="form-check-input" type="radio" name="workflowdownloadoption" value="workflowwithprocesscodegoodhistory">
									<i>Workflow with Process Code and Only Successfully Done History</i>
								</label>
							</div>
							<div class="form-check">
								<label>
									<input class="form-check-input" type="radio" name="workflowdownloadoption" value="workflowwithprocesscodeallhistory" checked>
									<i>Workflow with Process Code and All Process History (Recommended)</i>
								</label>
							</div>
						</div>
				
					</div>
				
				</div>
		
				<div class="modal-footer">
					<button id="export-loading-waiter" type="button" class="btn btn-outline-secondary" disabled style="display: none">
						<div style="display: inline-flex">
							<img id="export-loading-waiter" src="../gif/loading-spinner-black.gif" style="height: 25px; width: 25px" alt="loading..." />
							<p style="margin: 0; padding-top: 3px">Preparing your Download</p>
						</div>
					</button>
					<button type="button" id="workflow-download-confirm-btn" class="btn btn-outline-secondary">Confirm</button>
					<button type="button" id="workflow-download-cancel-btn" class="btn btn-outline-secondary">Cancel</button>
				</div>`;

        GW.workspace.jsFrame = GW.process.createJSFrameDialog(
          620,
          340,
          content,
          "Workflow Exportation Options",
        );

        $("#workflow-download-confirm-btn").click(function () {
          // disable the button and show loading icon
          $("#export-loading-waiter").css("display", "inline-block");
          $("#workflow-download-confirm-btn").css("display", "none");

          let exportoption = $(
            "input[name='workflowdownloadoption']:checked",
          ).val();

          $.ajax({
            url: "downloadworkflow",

            method: "POST",

            data:
              "id=" + GW.workflow.loaded_workflow + "&option=" + exportoption,
          })
            .done(function (msg) {
              if (msg.startsWith("download/temp/")) {
                let zipurl = "../" + msg;

                window.open(zipurl);

                GW.workspace.jsFrame.closeFrame();
              } else {
                alert("Failed to export workflow.");
              }

              $("#export-loading-waiter").css("display", "none");
              $("#workflow-download-confirm-btn").css(
                "display",
                "inline-block",
              );
            })
            .fail(function (msg) {
              alert("Fail to download workflow: " + msg);
            });
        });

        $("#workflow-download-cancel-btn").click(function () {
          GW.workspace.jsFrame.closeFrame();
        });
      } else {
        alert("No workflow in the workspace to download.");
      }
    });

    d3.select("#new-workflow").on("click", function () {
      thisGraph.nodes = [];
      thisGraph.edges = [];
      thisGraph.selectedWorkflow = null;
      thisGraph.svg = null;
      thisGraph.keymap = {};
      thisGraph.if_any_frame_on = false;
      GW.workflow.setCurrentWorkflowName("");
      GW.workflow.loaded_workflow = null;
      thisGraph.updateGraph();
      $("#main-workspace-tab").html("Weaver");
    });

    d3.select("#add-workflow").on("click", function () {
      if (GW.workflow.loaded_workflow != null) {
        if (
          !confirm(
            "This is an existing workflow. Do you want to create a new one?",
          )
        ) {
          return;
        }
      }
      GW.workflow.newDialog(false);
    });

    d3.select("#save-workflow").on("click", function () {
      GW.workspace.saveWorkflow();
    });

    d3.select("#execute-workflow").on("click", function () {
      if ($("#execute-workflow").hasClass("fa-stop")) {
        GW.workflow.stop(GW.workflow.history_id);
      } else if ($("#execute-workflow").hasClass("fa-play")) {
        GW.workflow.history_id = null;

        //if the current workspace is loaded with an existing workflow, run it directly. Otherwise,
        //save the workflow first.
        if (GW.workflow.loaded_workflow == null) {
          GW.workflow.newDialog(true);
        } else {
          GW.workflow.run(GW.workflow.loaded_workflow);
        }
      }
    });

    d3.select("#geoweaver-result").on("click", function () {
      //get the selected node id

      GW.result.showDialog("");
    });

    d3.select("#geoweaver-log").on("click", function () {
      //get the selected node id

      var selectedNode = GW.workspace.theGraph.state.selectedNode;

      if (selectedNode == null) {
        alert("No process is selected");
      } else {
        GW.process.sidepanel.showProcessLog(
          GW.workflow.history_id,
          selectedNode.id,
          selectedNode.title,
        );
      }
    });

    d3.select("#geoweaver-details").on("click", function () {
      //get the selected node id

      var selectedNode = GW.workspace.theGraph.state.selectedNode;

      if (selectedNode == null) {
        // alert("No process is selected");
        GW.general.switchTab("workflow");
      } else {
        var id = selectedNode.id.split("-")[0];

        GW.menu.details(id, "process");
      }

      GW.process.sidepanel.close(); //always close the side panel when leaving
    });

    d3.select("#workflow_info").on("click", function () {
      if (GW.workflow.loaded_workflow != null) {
        GW.general.switchTab("workflow");
      } else {
        alert(
          "There is no active workflow! Please select one existing workflow or create a new one.",
        );
      }

      GW.process.sidepanel.close(); //always close the side panel when leaving
    });

    d3.select("#upload-input").on("click", function () {
      GW.fileupload.showUploadWorkflowDialog();
    });

    d3.select("#show-full-view").on("click", function () {
      if (GW.workspace.theGraph.nodes.length === 0) return;

      const svgNode = GW.workspace.svg;
      const svgGroup = GW.workspace.theGraph.svgG;
      const viewportWidth = +svgNode.attr("width");
      const viewportHeight = +svgNode.attr("height");
      
      // Get the actual rendered size of the entire graph group
      const graphBBox = svgGroup.node().getBBox();
      const graphWidth = graphBBox.width;
      const graphHeight = graphBBox.height;

      // If the graph has no size, do nothing.
      if (graphWidth === 0 || graphHeight === 0) return;

      // Set padding to 10% of the viewport dimension on each side.
      const horizontalPadding = viewportWidth * 0.1;
      const verticalPadding = viewportHeight * 0.1;

      // The area available for the graph after accounting for padding.
      const availableWidth = viewportWidth - 2 * horizontalPadding;
      const availableHeight = viewportHeight - 2 * verticalPadding;
      
      // Determine the scale to fit the graph into the available area.
      const scale = Math.min(availableWidth / graphWidth, availableHeight / graphHeight);
      
      // Calculate the X and Y translation to center the graph.
      // This positions the top-left of the scaled graph at (horizontalPadding, verticalPadding).
      const translateX = horizontalPadding - (graphBBox.x * scale);
      const translateY = verticalPadding - (graphBBox.y * scale);

      // Apply the new transform with a smooth animation.
      svgGroup.transition()
          .duration(750)
          .attr("transform", `translate(${translateX},${translateY}) scale(${scale})`);
      
      // Update D3's zoom behavior to match the new view.
      thisGraph.zoom.scale(scale);
      thisGraph.zoom.translate([translateX, translateY]);
    });

    d3.select("#hidden-file-upload").on("change", function () {
      console.log("hidden-file-upload is changed");
      if (window.File && window.FileReader && window.FileList && window.Blob) {
        var uploadFile = this.files[0];
        var filereader = new window.FileReader();

        filereader.onload = function () {
          var txtRes = filereader.result;
          // TODO better error handling
          try {
            var jsonObj = JSON.parse(txtRes);
            thisGraph.deleteGraph(true);
            thisGraph.nodes = jsonObj.nodes;
            thisGraph.setIdCt(jsonObj.nodes.length + 1);
            var newEdges = jsonObj.edges;
            newEdges.forEach(function (e, i) {
              newEdges[i] = {
                source: thisGraph.nodes.filter(function (n) {
                  return n.id == e.source;
                })[0],
                target: thisGraph.nodes.filter(function (n) {
                  return n.id == e.target;
                })[0],
              };
            });
            thisGraph.edges = newEdges;
            thisGraph.updateGraph();
          } catch (err) {
            window.alert(
              "Error parsing uploaded file\nerror message: " + err.message,
            );
            return;
          }
        };
        filereader.readAsText(uploadFile);
      } else {
        alert(
          "Your browser won't let you save this graph -- try upgrading your browser to IE 10+ or Chrome or Firefox.",
        );
      }
    });

    // handle delete graph
    d3.select("#delete-graph").on("click", function () {
      GW.workspace.theGraph.deleteSelectedOrAll();
    });
  },

  /**
   * Add listeners to the GraphCreator
   */
  addListeners: function () {
    GW.workspace.GraphCreator.prototype.setIdCt = function (idct) {
      this.idct = idct;
    };

    GW.workspace.GraphCreator.prototype.consts = {
      selectedClass: "selected",
      connectClass: "connect-node",
      circleGClass: "conceptG",
      graphClass: "graph",
      activeEditId: "active-editing",
      BACKSPACE_KEY: 8,
      DELETE_KEY: 46,
      ENTER_KEY: 13,
      nodeRadius: 20,
    };

    /* PROTOTYPE FUNCTIONS */

    // this drag move only works for nodes and lines
    GW.workspace.GraphCreator.prototype.dragmove = function (d) {
      var thisGraph = this;
      if (thisGraph.state.shiftNodeDrag) {
        thisGraph.dragLine.attr(
          "d",
          "M" +
            d.x +
            "," +
            d.y +
            "L" +
            d3.mouse(thisGraph.svgG.node())[0] +
            "," +
            d3.mouse(this.svgG.node())[1],
        );
      } else {
        d.x += d3.event.dx;
        d.y += d3.event.dy;
        thisGraph.updateGraph();
      }
      GW.workspace.showNonSaved();
    };

    GW.workspace.GraphCreator.prototype.deleteGraph = function (skipPrompt) {
      var thisGraph = this;

      //first check if the current view is in the workspace
      if (document.getElementById("workspace").style.display == "flex") {
        //if some objects are selected, delete the selected only. If nothing selected, delete all.

        if (!skipPrompt) {
          doDelete = window.confirm(
            "Warning: everything in work area will be erased!!! Press OK to proceed.",
          );

          if (doDelete) {
            thisGraph.nodes = [];
            thisGraph.edges = [];
            thisGraph.updateGraph();
            GW.workflow.setCurrentWorkflowName("");
            GW.workflow.loaded_workflow = null;
            $("#main-workspace-tab").html("Weaver");

            let currentWorkflow = window.selectedWorkflow;
            if (currentWorkflow !== undefined) {
              $.ajax({
                url: "del",
                method: "POST",
                data: "type=clear_nodes_edges&id=" + currentWorkflow,
              });
            } else {
              alert("Please select a workflow to delete");
            }
          }
        } else {
          if (thisGraph.state.selectedEdge) {
            //removing an edge is much easier than removing a process
            thisGraph.edges.splice(thisGraph.edges.indexOf(selectedEdge), 1);
            state.selectedEdge = null;
            thisGraph.updateGraph();
          } else if (thisGraph.state.selectedNode) {
            var pid = thisGraph.state.selectedNode.id;
            console.log("going to remove process: " + pid);
            thisGraph.removeNode(pid);
          }

          GW.workspace.showNonSaved();
        }
      }
    };

    //add on 11/2/2018
    GW.workspace.GraphCreator.prototype.load = function (workflow) {
      try {
        console.log("Start to load workflow..");

        window.selectedWorkflow = workflow.id;

        var jsonObj = workflow;

        this.deleteGraph(true);

        GW.workspace.showSaved();

        var newNodes = GW.general.parseResponse(jsonObj.nodes);

        //remove the old color status - load a brand new workflow
        newNodes.forEach(function (e, i) {
          newNodes[i].color = "white";
          newNodes[i].status = "none"; // intialize the workflow status
        });

        this.nodes = newNodes;

        this.setIdCt(jsonObj.nodes.length + 1);

        var newEdges = GW.general.parseResponse(jsonObj.edges);
        var validEdges = [];

        newEdges.forEach(function (e, i) {
          var sourceNode = GW.workspace.theGraph.nodes.filter(function (n) {
            return n.id == e.source.id;
          })[0];
          
          var targetNode = GW.workspace.theGraph.nodes.filter(function (n) {
            return n.id == e.target.id;
          })[0];
          
          // Only add edges with valid source and target nodes
          if (sourceNode !== undefined && targetNode !== undefined) {
            validEdges.push({
              source: sourceNode,
              target: targetNode
            });
          } else {
            console.warn("Skipping edge with undefined source or target: ", e);
          }
        });

        this.edges = validEdges;

        this.updateGraph();
      } catch (err) {
        window.alert(
          "Error parsing uploaded file\nerror message: " + err.message,
        );
        return;
      }
    };

    GW.workspace.GraphCreator.prototype.selectElementContents = function (el) {
      var range = document.createRange();
      range.selectNodeContents(el);
      var sel = window.getSelection();
      sel.removeAllRanges();
      sel.addRange(range);
    };

    GW.workspace.GraphCreator.prototype.insertTitleLinebreaks = function (
      gEl,
      title,
    ) {
      var words = title.split(/\s+/g),
        nwords = words.length;
      var el = gEl
        .append("text")
        .attr("text-anchor", "middle")
        .attr("fill", "black")
        .attr("stroke", "black")
        .attr("stroke-width", "1px")
        .attr("stroke-linecap", "butt")
        .attr("stroke-linejoin", "miter")
        .attr("font-weight", 500)
        .attr("font-size", "20px")
        .attr("y", "50px");
      //   .attr("dy", "-" + (nwords-1)*7.5);

      el.append("tspan").text(title);
      // for (var i = 0; i < words.length; i++) {
      //   var tspan = el.append('tspan').text(words[i]);
      //   if (i > 0)
      // 	tspan.attr('x', 0).attr('dy', '15');
      // }
    };

    // remove edges associated with a node
    GW.workspace.GraphCreator.prototype.spliceLinksForNode = function (node) {
      var thisGraph = this,
        toSplice = thisGraph.edges.filter(function (l) {
          return l.source === node || l.target === node;
        });
      toSplice.map(function (l) {
        thisGraph.edges.splice(thisGraph.edges.indexOf(l), 1);
      });
    };

    GW.workspace.GraphCreator.prototype.replaceSelectEdge = function (
      d3Path,
      edgeData,
    ) {
      var thisGraph = this;
      d3Path.classed(thisGraph.consts.selectedClass, true);
      // Add visual feedback for selected edge
      d3Path.style("stroke", "#4285f4");
      d3Path.style("stroke-width", "4px");
      d3Path.style("filter", "url(#drop-shadow-selected)");
      
      if (thisGraph.state.selectedEdge) {
        thisGraph.removeSelectFromEdge();
      }
      thisGraph.state.selectedEdge = edgeData;
    };

    GW.workspace.GraphCreator.prototype.replaceSelectNode = function (
      d3Node,
      nodeData,
    ) {
      var thisGraph = this;
      d3Node.classed(this.consts.selectedClass, true);
      if (thisGraph.state.selectedNode) {
        thisGraph.removeSelectFromNode();
      }
      thisGraph.state.selectedNode = nodeData;
      console.log("selected node changed : " + nodeData.id);

      // show the prompt process panel
      GW.process.sidepanel.close();
      GW.process.sidepanel.open_panel(
        GW.workflow.history_id,
        nodeData.id,
        nodeData.title,
      );
    };

    GW.workspace.GraphCreator.prototype.removeSelectFromNode = function () {
      var thisGraph = this;
      thisGraph.circles
        .filter(function (cd) {
          return cd.id === thisGraph.state.selectedNode.id;
        })
        .classed(thisGraph.consts.selectedClass, false);
      thisGraph.state.selectedNode = null;
      GW.workspace.showNonSaved();
    };

    GW.workspace.GraphCreator.prototype.removeSelectFromEdge = function () {
      var thisGraph = this;
      thisGraph.paths
        .filter(function (cd) {
          return cd === thisGraph.state.selectedEdge;
        })
        .classed(thisGraph.consts.selectedClass, false)
        .style("stroke", "url(#edge-gradient)")
        .style("stroke-width", "2.5px")
        .style("filter", "url(#edge-shadow)");
      thisGraph.state.selectedEdge = null;
      GW.workspace.showNonSaved();
    };

    GW.workspace.GraphCreator.prototype.pathMouseDown = function (d3path, d) {
      var thisGraph = this,
        state = thisGraph.state;
      d3.event.stopPropagation();
      state.mouseDownLink = d;

      if (state.selectedNode) {
        thisGraph.removeSelectFromNode();
      }

      var prevEdge = state.selectedEdge;
      if (!prevEdge || prevEdge !== d) {
        thisGraph.replaceSelectEdge(d3path, d);
      } else {
        thisGraph.removeSelectFromEdge();
      }
    };

    // mousedown on node
    GW.workspace.GraphCreator.prototype.circleMouseDown = function (d3node, d) {
      var thisGraph = this,
        state = thisGraph.state;
      d3.event.stopPropagation();
      state.mouseDownNode = d;
      if (d3.event.shiftKey) {
        state.shiftNodeDrag = d3.event.shiftKey;
        // reposition dragged directed edge
        thisGraph.dragLine
          .classed("hidden", false)
          .attr("d", "M" + d.x + "," + d.y + "L" + d.x + "," + d.y);
        return;
      }
    };

    /* place editable text on node in place of svg text */
    GW.workspace.GraphCreator.prototype.changeTextOfNode = function (
      d3node,
      d,
    ) {
      var thisGraph = this,
        consts = thisGraph.consts,
        htmlEl = d3node.node();
      d3node.selectAll("text").remove();
      var nodeBCR = htmlEl.getBoundingClientRect(),
        curScale = nodeBCR.width / consts.nodeRadius,
        placePad = 5 * curScale,
        useHW = curScale > 1 ? nodeBCR.width * 0.71 : consts.nodeRadius * 1.42;
      // replace with editableconent text
      var d3txt = thisGraph.svg
        .selectAll("foreignObject")
        .data([d])
        .enter()
        .append("foreignObject")
        .attr("x", nodeBCR.left + placePad)
        .attr("y", nodeBCR.top + placePad)
        .attr("height", 2 * useHW)
        .attr("width", useHW)
        .append("xhtml:p")
        .attr("id", consts.activeEditId)
        .attr("contentEditable", "false")
        .text(d.title)
        .on("mousedown", function (d) {
          d3.event.stopPropagation();
        })
        .on("keydown", function (d) {
          d3.event.stopPropagation();
          if (d3.event.keyCode == consts.ENTER_KEY && !d3.event.shiftKey) {
            this.blur();
          }
        })
        .on("blur", function (d) {
          d.title = this.textContent;
          thisGraph.insertTitleLinebreaks(d3node, d.title);
          d3.select(this.parentElement).remove();
        });
      return d3txt;
    };

    // mouseup on nodes
    GW.workspace.GraphCreator.prototype.circleMouseUp = function (d3node, d) {
      var thisGraph = this,
        state = thisGraph.state,
        consts = thisGraph.consts;
      // reset the states
      state.shiftNodeDrag = false;
      d3node.classed(consts.connectClass, false);

      var mouseDownNode = state.mouseDownNode;

      if (!mouseDownNode) return;

      thisGraph.dragLine.classed("hidden", true);

      if (mouseDownNode !== d) {
        // we're in a different node: create new edge for mousedown edge and add to graph
        // Ensure both source and target are defined before creating the edge
        if (mouseDownNode && d) {
          var newEdge = { source: mouseDownNode, target: d };
          var filtRes = thisGraph.paths.filter(function (d) {
            if (d.source === newEdge.target && d.target === newEdge.source) {
              thisGraph.edges.splice(thisGraph.edges.indexOf(d), 1);
            }
            return d.source === newEdge.source && d.target === newEdge.target;
          });
          if (!filtRes[0].length) {
            thisGraph.edges.push(newEdge);
            thisGraph.updateGraph();
          }
        } else {
          console.warn("Attempted to create edge with undefined source or target");
        }
      } else {
        // we're in the same node
        if (state.justDragged) {
          // dragged, not clicked
          state.justDragged = false;
        } else {
          // clicked, not dragged
          if (d3.event.shiftKey) {
            // shift-clicked node: edit text content
            // var d3txt = thisGraph.changeTextOfNode(d3node, d);
            // var txtNode = d3txt.node();
            // thisGraph.selectElementContents(txtNode);
            // txtNode.focus();

          } else {
            if (state.selectedEdge) {
              thisGraph.removeSelectFromEdge();
            }
            var prevNode = state.selectedNode;

            if (!prevNode || prevNode.id !== d.id) {
              thisGraph.replaceSelectNode(d3node, d);
            } else {
              thisGraph.removeSelectFromNode();
            }
          }
        }
      }
      state.mouseDownNode = null;
      return;
    }; // end of circles mouseup

    GW.workspace.GraphCreator.prototype.circleDdlClick = function (d3node, d) {
      // GW.process.sidepanel.showProcessLog(GW.workflow.history_id, d.id, d.title);
      console.log("no action taken");
    };

    // mousedown on main svg
    GW.workspace.GraphCreator.prototype.svgMouseDown = function () {
      this.state.graphMouseDown = true;
    };

    // mouseup on main svg
    GW.workspace.GraphCreator.prototype.svgMouseUp = function () {
      var thisGraph = this,
        state = thisGraph.state;
      if (state.justScaleTransGraph) {
        // dragged not clicked
        state.justScaleTransGraph = false;
      } else if (state.graphMouseDown && d3.event.shiftKey) {
        // do nothing
      } else if (state.shiftNodeDrag) {
        // dragged from node
        state.shiftNodeDrag = false;
        thisGraph.dragLine.classed("hidden", true);
      }
      state.graphMouseDown = false;
    };

    GW.workspace.GraphCreator.prototype.removeNode = function (pid) {
      var thisGraph = this;

      var selectedNode = null;

      for (var i = 0; i < thisGraph.nodes.length; i++) {
        if (thisGraph.nodes[i].id == pid) {
          selectedNode = thisGraph.nodes[i];

          thisGraph.nodes.splice(i, 1);

          break;
        }
      }

      thisGraph.spliceLinksForNode(selectedNode);
      thisGraph.state.selectedNode = null;
      thisGraph.updateGraph();
      GW.workspace.showNonSaved();
    };

    GW.workspace.GraphCreator.prototype.removeNodes = function (pid) {
      var thisGraph = this;
      var selectedNodes = thisGraph.getNodesById(pid);
      for (var i = 0; i < selectedNodes.length; i++) {
        thisGraph.nodes.splice(selectedNodes[i], 1);
        thisGraph.spliceLinksForNode(selectedNodes[i]);
      }
      thisGraph.state.selectedNode = null;
      thisGraph.updateGraph();
      GW.workspace.showNonSaved();
    };

    GW.workspace.GraphCreator.prototype.deleteSelected = function () {
      if (Object.keys(BootstrapDialog.dialogs).length) {
        return; //if there are shown dialogs, key activity will be disconnected from svg
      }
      var thisGraph = this,
        state = thisGraph.state,
        consts = thisGraph.consts;

      var selectedNode = state.selectedNode,
        selectedEdge = state.selectedEdge;

      if (document.getElementById("workspace").style.display == "flex") {
        if (selectedNode) {
          var pid = selectedNode.id;
          console.log("going to remove process: " + pid);
          //	    	    	GW.menu.del(pid, "process");
          thisGraph.removeNode(pid);
        } else if (selectedEdge) {
          //removing an edge is much easier than removing a process
          console.log("Removing selected edge");
          thisGraph.edges.splice(thisGraph.edges.indexOf(selectedEdge), 1);
          state.selectedEdge = null;
          GW.workspace.showNonSaved();
          thisGraph.updateGraph();
        }
      }
    };

    GW.workspace.GraphCreator.prototype.deleteSelectedOrAll = function () {
      if (Object.keys(BootstrapDialog.dialogs).length) {
        return; //if there are shown dialogs, key activity will be disconnected from svg
      }
      var thisGraph = this,
        state = thisGraph.state,
        consts = thisGraph.consts;

      var selectedNode = state.selectedNode,
        selectedEdge = state.selectedEdge;

      if (document.getElementById("workspace").style.display == "flex") {
        if (selectedNode) {
          var pid = selectedNode.id;
          console.log("going to remove process: " + pid);
          thisGraph.removeNode(pid);
        } else if (selectedEdge) {
          //removing an edge is much easier than removing a process
          thisGraph.edges.splice(thisGraph.edges.indexOf(selectedEdge), 1);
          state.selectedEdge = null;
          GW.workspace.showNonSaved();
          thisGraph.updateGraph();
        } else {
          this.deleteGraph(false);
        }
      }
    };

    // keydown on main svg
    GW.workspace.GraphCreator.prototype.svgKeyDown = function () {
      if (Object.keys(BootstrapDialog.dialogs).length) {
        return; //if there are shown dialogs, key activity will be disconnected from svg
      }
      var thisGraph = this,
        state = thisGraph.state,
        consts = thisGraph.consts;
      // make sure repeated key presses don't register for each keydown
      if (state.lastKeyDown !== -1) return;

      state.lastKeyDown = d3.event.keyCode;

      switch (d3.event.keyCode) {
        case consts.BACKSPACE_KEY:
        case consts.DELETE_KEY:
          d3.event.preventDefault();
          //only delete the process nodes or edges when there is no dialog in sight
          if (
            !GW.workspace.if_any_frame_on &&
            !GW.process.sidepanel.isPresent()
          )
            this.deleteSelected();
          break;
      }
    };

    GW.workspace.GraphCreator.prototype.svgKeyUp = function () {
      if (Object.keys(BootstrapDialog.dialogs).length) {
        return;
      }
      this.state.lastKeyDown = -1;
    };

    GW.workspace.GraphCreator.prototype.updateGraph = function () {
      const thisGraph = this;
      const { consts, state } = thisGraph;
    
      this.setIdCt(this.nodes.length);
    
      // Clean edges
      thisGraph.edges = thisGraph.edges.filter(edge => {
        if (!edge.source || !edge.target) {
          console.warn("Removing invalid edge with undefined source or target");
          return false;
        }
        return true;
      });
    
      // Update paths
      thisGraph.paths = thisGraph.paths.data([], d => `${d.source.id}+${d.target.id}`);
      thisGraph.paths.exit().remove();
    
      const validEdges = thisGraph.edges;
      thisGraph.paths = thisGraph.paths.data(validEdges, d => `${d.source.id}+${d.target.id}`);
      const paths = thisGraph.paths;
    
      paths
        .style("marker-end", "url(#end-arrow)")
        .classed(consts.selectedClass, d => d === state.selectedEdge)
        .attr("d", d => `M${d.source.x},${d.source.y}L${d.target.x},${d.target.y}`)
        .style("stroke", "url(#edge-gradient)")
        .style("stroke-width", "2.5px")
        .style("filter", "url(#edge-shadow)");
    
      paths
        .enter()
        .append("path")
        .style("marker-end", "url(#end-arrow)")
        .classed("link", true)
        .attr("d", d => `M${d.source.x},${d.source.y}L${d.target.x},${d.target.y}`)
        .style("stroke", "url(#edge-gradient)")
        .style("stroke-width", "2.5px")
        .style("filter", "url(#edge-shadow)")
        .on("mousedown", function (d) {
          thisGraph.pathMouseDown.call(thisGraph, d3.select(this), d);
        })
        .on("mouseup", () => {
          state.mouseDownLink = null;
        })
        .on("mouseover", function() {
          d3.select(this).style("cursor", "pointer");
          d3.select(this).style("stroke-width", "4px");
        })
        .on("mouseout", function() {
          d3.select(this).style("stroke-width", "2.5px");
        });
    
      // Update nodes
      thisGraph.circles = thisGraph.circles.data([], d => d.id);
      thisGraph.circles.exit().remove();
      thisGraph.circles = thisGraph.circles.data(thisGraph.nodes, d => d.id);
    
      thisGraph.circles
        .attr("stroke", "black")
        // .attr("fill", d => (d.skip ? "url(#diagonalHatch)" : d.color))
        .attr("transform", d => `translate(${d.x},${d.y})`);
    
      thisGraph.circles.each(function (d) {
        if (["Running", "Done", "Failed"].includes(d.status)) {
          d3.select(this).selectAll(".status-icon, .loading-spinner").remove();
          GW.workspaceicons.addStatusIcon(d3.select(this), d.status);
        }
      });
    
      const newGs = thisGraph.circles.enter().append("g");
    
      if (!GW.workspace.tooltipdiv) {
        GW.workspace.tooltipdiv = d3
          .select("body")
          .append("div")
          .attr("class", "processtooltip")
          .style("opacity", 0);
      }
    
      let ismouseinside = false;
    
      newGs
        .classed(consts.circleGClass, true)
        .classed("circle-running", d => d.status === "Running")
        .attr("transform", d => `translate(${d.x},${d.y})`)
        .on("mouseover", function (d) {
          ismouseinside = true;
          if (state.shiftNodeDrag) d3.select(this).classed(consts.connectClass, true);
    
          const process_id = d.id.split("-")[0];
          const [pageX, pageY] = [d3.event.pageX, d3.event.pageY];
    
          GW.menu.details(process_id, "process", msg => {
            if (ismouseinside) {
              GW.workspace.tooltipdiv
                .transition().duration(200).style("opacity", 0.9);
    
              GW.workspace.tooltipdiv
                .html(`
                  <table>
                    <tr><td><b>ID</b></td><td>${msg.id}</td></tr>
                    <tr><td><b>Language</b></td><td>${msg.lang}</td></tr>
                    <tr><td><b>Code</b></td><td>${GW.general.shorten_long_string(GW.general.escapeCodeforHTML(msg.code), 200)}</td></tr>
                  </table>
                `)
                .style("left", `${pageX}px`)
                .style("top", `${pageY}px`);
            }
          });
        })
        .on("mouseout", function () {
          d3.select(this).classed(consts.connectClass, false);
          GW.workspace.tooltipdiv.transition().duration(1).style("opacity", 0);
          ismouseinside = false;
        })
        .on("mousedown", function (d) {
          thisGraph.circleMouseDown.call(thisGraph, d3.select(this), d);
        })
        .on("mouseup", function (d) {
          thisGraph.circleMouseUp.call(thisGraph, d3.select(this), d);
        })
        .on("dblclick", function (d) {
          thisGraph.circleDdlClick.call(thisGraph, d3.select(this), d);
        })
        .call(thisGraph.drag);
    
      newGs
        .append("circle")
        .attr("r", consts.nodeRadius)
        // .attr("class", d => `status-${d.status || "none"}`)
        .attr("stroke-width", 2)
        .attr("stroke", d => d.stroke || "#4285f4")
        .attr("fill", d => {
          // if (d.skip) return "url(#diagonalHatch)";
          if (d.skip) return "url(#node-gradient-skipped-pattern)";
          // console.log("fill color for node: " + d.status);
          switch (d.status) {
            case "Running": return "url(#node-gradient-running)";
            case "Done": return "url(#node-gradient-done)";
            case "Failed": return "url(#node-gradient-failed)";
            default: return "url(#node-gradient-default)";
          }
        })
        .style("filter", "url(#drop-shadow)");
    
      newGs.each(function (d) {
        if (["Running", "Done", "Failed"].includes(d.status)) {
          GW.workspaceicons.addStatusIcon(d3.select(this), d.status);
        }
      });
    
      newGs.each(function (d) {
        thisGraph.insertTitleLinebreaks(d3.select(this), d.title);
      });
    };

    GW.workspace.GraphCreator.prototype.zoomed = function () {
      this.state.justScaleTransGraph = true;
      d3.select("." + this.consts.graphClass).attr(
        "transform",
        "translate(" + d3.event.translate + ") scale(" + d3.event.scale + ")",
      );
    };

    GW.workspace.GraphCreator.prototype.addProcess = function (id, name) {
      if (GW.workspace.currentmode == 1) {
        var thisGraph = this;

        // dynamic way of handling the process circle position:

        // S1: find the total size of the current weaver tab display screen dynamically
        // if needed get the entire window size in JS
        // later divide it in half to get the center position values and assign
        // also handle the additional case, where we check the new process circle is not overlapping the existing non disturbed circle
        // check the existing circle and it's position, if the position is the same, we try to update the new circle with a new position with small change

        var width = window.innerWidth;

        var height = window.innerHeight;

        var x, y;

        if (thisGraph.nodes.length > 0) {
          x = width / 2 + thisGraph.nodes.length + 10;

          y = height / 2 + thisGraph.nodes.length + 10;
        } else {
          x = width / 2;

          y = height / 2;
        }

        // get the div, offsetheight and offset width and calculate and apply if condition.

        var randomid = GW.workspace.makeid();

        var insid = id + "-" + randomid;

        thisGraph.nodes.push({ title: name, id: insid, x: x, y: y });

        thisGraph.updateGraph();

        console.log("new process added: " + insid);

        GW.workspace.showNonSaved();

        GW.general.switchTab("workspace");

        return insid;
      } else {
        alert("Sorry, cannot add process when the workflow is running!");
      }
    };

    GW.workspace.GraphCreator.prototype.addEdge = function (frompid, topid) {
      var thisGraph = this;

      var fromnode = thisGraph.getNodeById(frompid);

      var tonode = thisGraph.getNodeById(topid);

      // Only add edge if both source and target nodes exist
      if (fromnode !== null && tonode !== null) {
        thisGraph.edges.push({ source: fromnode, target: tonode });
      } else {
        console.warn("Cannot add edge: source or target node not found", frompid, topid);
      }
    };

    GW.workspace.GraphCreator.prototype.renderStatus = function (statusList) {

      console.log("monitor workflow status called", statusList);

      // Handle single process update
      if (statusList.message_type == "single_process") {
        var id = statusList.id;
        var history_id = statusList.history_id;
        var flag = statusList.status; //true or false
        var num = this.getNodeNumById(id);
        
        if (num !== null) {
          // Update node status
          GW.workspace.theGraph.nodes[num].status = flag;
          GW.monitor.updateProgress(id, flag);
          
          // Update the node's circle with appropriate 3D gradient based on status
          var nodeSelection = d3.select("g.conceptG[id='" + id + "']");
          if (!nodeSelection.empty()) {
            // Update the status class on the circle for CSS styling
            nodeSelection.select("circle")
              .attr("class", "status-" + flag)
              .attr("fill", function() {
                // Apply appropriate 3D gradient based on status
                if (flag === "Running") return "url(#node-gradient-running)";
                else if (flag === "Done") return "url(#node-gradient-done)";
                else if (flag === "Failed") return "url(#node-gradient-failed)";
                else return "url(#node-gradient-default)";
              })
              .style("filter", "url(#drop-shadow)");
              
            // Remove any existing status icons first
            nodeSelection.selectAll(".status-icon, .loading-spinner").remove();
            // Add status icon
            GW.workspaceicons.addStatusIcon(nodeSelection, flag);
          }
        } else {
          console.error("Node not found with id:", id);
        }

        GW.workspace.theGraph.updateGraph();
      } 
      // Handle array of status updates
      else if (Array.isArray(statusList)) {
        var updatedAnyNode = false;
        
        for (var i = 0; i < statusList.length; i++) {
          var id = statusList[i].id;
          var flag = statusList[i].status; //true or false
          var num = this.getNodeNumById(id);
          
          if (num !== null) {
            // Update node status
            GW.workspace.theGraph.nodes[num].status = flag;
            GW.monitor.updateProgress(id, flag);
            updatedAnyNode = true;
          } else {
            console.error("Node not found with id:", id);
          }
        }
        
        if (updatedAnyNode) {
          GW.workspace.theGraph.updateGraph();
        }
      } else {
        console.error("Unrecognized status update format:", statusList);
      }
    };

    /**
     * NodeS
     */
    GW.workspace.GraphCreator.prototype.getNodesById = function (id) {
      var thisGraph = this;

      var thenodes = [];

      for (var i = 0; i < thisGraph.nodes.length; i++) {
        if (thisGraph.nodes[i].id.startsWith(id)) {
          thenodes.push(thisGraph.nodes[i]);
        }
      }

      return thenodes;
    };

    /**
     * Node
     */
    GW.workspace.GraphCreator.prototype.getNodeById = function (id) {
      var thisGraph = this;

      var thenode = null;

      for (var i = 0; i < thisGraph.nodes.length; i++) {
        if (thisGraph.nodes[i].id == id) {
          thenode = thisGraph.nodes[i];

          break;
        }
      }

      return thenode;
    };

    GW.workspace.GraphCreator.prototype.getNodeNumById = function (id) {
      var thisGraph = this;

      var thenum = -1;

      for (var i = 0; i < thisGraph.nodes.length; i++) {
        if (thisGraph.nodes[i].id == id) {
          thenum = i;

          break;
        }
      }

      return thenum;
    };

    GW.workspace.GraphCreator.prototype.updateWindow = function (svg) {
      var docEl = document.documentElement,
        bodyEl = document.getElementsByTagName("body")[0];
      var x = window.innerWidth || docEl.clientWidth || bodyEl.clientWidth;
      var y = window.innerHeight || docEl.clientHeight || bodyEl.clientHeight;
      svg.attr("width", x).attr("height", y);
    };
  },

  getColorByFlag: function (flag) {
    var color = "#ffffff";

    if (flag == "Ready") {
      color = "#2196f3"; // Professional blue
    } else if (flag == "Running") {
      color = "#e3f2fd"; // Light blue background for running state
    } else if (flag == "Done") {
      color = "#e8f5e9"; // Light green background for success
    } else if (flag == "Failed") {
      color = "#ffebee"; // Light red background for failure
    } else if (flag == "Skipped") {
      color = "#f5f5f5"; // Light gray for skipped
    } else if (flag == "Stopped") {
      color = "#eeeeee"; // Light gray for stopped
    } else if (flag == null) {
      color = "blue";
    }

    return color;
  },

  updateStatus: function (statusList) {
    GW.workspace.theGraph.renderStatus(statusList);
  },

  checkIfWorkspacePanelActive: function () {
    return document.getElementById("workspace").style.display == "flex";
  },

  /**
   * check if the workspace has more than one processes
   */
  checkIfWorkflow: function () {
    var workflow = false;

    if (this.theGraph.nodes.length > 1) {
      workflow = true;
    }

    return workflow;
  },

  checkIfWorkflowValid: function () {
    var isvalid = true;

    if (GW.workspace.checkIfWorkflow()) {
      GW.workspace.theGraph.edges.forEach(function (val, i) {
        if (val.source == null || val.source == null) {
          isvalid = false;
        }
      });
    } else {
      isvalid = false;
    }

    return isvalid;
  },

  makeid: function () {
    var text = "";
    var possible =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for (var i = 0; i < 5; i++)
      text += possible.charAt(Math.floor(Math.random() * possible.length));

    return text;
  },

  init: function () {
    GW.workspace.addListeners();

    /**** MAIN ****/

    // warn the user when leaving
    //   window.onbeforeunload = function(){
    //     return "Make sure to save your graph locally before leaving :-)";
    //   };

    var docEl = document.documentElement,
      //	    	      bodyEl = document.getElementsByTagName('body')[0];
      bodyEl = document.getElementById("workspace");

    var width = window.innerWidth || docEl.clientWidth || bodyEl.clientWidth,
      height = window.innerHeight || docEl.clientHeight || bodyEl.clientHeight;

    var xLoc = width / 2 - 25,
      yLoc = 100;

    var nodes = [];

    var edges = [];

    /** MAIN SVG **/
    var svg = d3
      .select("#workspace")
      .append("svg")
      .attr("width", width)
      .attr("height", height);

    GW.workspace.svg = svg;

    var format = d3.format(",d");

    GW.workspace.theGraph = new GW.workspace.GraphCreator(svg, nodes, edges);

    GW.workspace.theGraph.updateGraph();
  },
};
