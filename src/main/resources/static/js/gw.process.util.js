/**
 * Separate the utility function shared by process and process.sidepanel
 */

GW.process.util = {

  get_icon_by_process_type: function(process_type){
    // Define icons for each process type
    const icons = {
      python: "🐍", // Python icon (can be replaced with an image)
      shell: "💻",  // Shell icon (can be replaced with an image)
    };
    return icons[process_type] || "❓"
  },

  // Utility function to add key commands for both Ctrl and Cmd on both macOS and Linux
  addClipboardCommand: function(editor, keyCode, commandCallback) {
    // For both macOS and Linux, bind the command for both Ctrl and Cmd keys
    editor.addCommand(monaco.KeyMod.CtrlCmd | keyCode, commandCallback);  // Handles both Cmd on macOS and Ctrl on Linux/Windows
    editor.addCommand(monaco.KeyMod.WinCtrl | keyCode, commandCallback);      // Handles Ctrl on both macOS and Linux/Windows
  },

  add_editor_actions: function(editor){

    // comment out for now because these will break the search bar. 
    // People won't be able to paste things into the search input field
    // we cannot enable this function until that bug is fixed. 

    // // Copy Command (Ctrl+C and Cmd+C on both macOS and Linux)
    // this.addClipboardCommand(editor, monaco.KeyCode.KeyC, function () {
    //   const selectedText = editor.getModel().getValueInRange(editor.getSelection());
    //   navigator.clipboard.writeText(selectedText).then(() => {}).catch(err => {
    //     console.error("Failed to copy text: ", err);
    //   });
    // });

    // // Paste Command (Ctrl+V and Cmd+V on both macOS and Linux)
    // this.addClipboardCommand(editor, monaco.KeyCode.KeyV, function () {
    //   navigator.clipboard.readText().then((clipboardText) => {
    //       const selection = editor.getSelection();
    //       editor.executeEdits('', [
    //           {
    //               range: selection,
    //               text: clipboardText,
    //               forceMoveMarkers: true,
    //           },
    //       ]);
    //   }).catch(err => {
    //       console.error("Failed to paste text: ", err);
    //   });
    // });

    // // Cut Command (Ctrl+X and Cmd+X on both macOS and Linux)
    // this.addClipboardCommand(editor, monaco.KeyCode.KeyX, function () {
    //   const selectedText = editor.getModel().getValueInRange(editor.getSelection());
    //   navigator.clipboard.writeText(selectedText).then(() => {
    //       const selection = editor.getSelection();
    //       editor.executeEdits('', [
    //           {
    //               range: selection,
    //               text: '', // Replace selected text with nothing (cut)
    //               forceMoveMarkers: true,
    //           },
    //       ]);
    //   }).catch(err => {
    //       console.error("Failed to cut text: ", err);
    //   });
    // });

    // // Undo Command (Ctrl+Z and Cmd+Z on both macOS and Linux)
    // this.addClipboardCommand(editor, monaco.KeyCode.KeyZ, function () {
    //   editor.trigger('keyboard', 'undo');  // This triggers the undo action
    // });

  },

  displayCodeArea: function (
    code_type,
    code,
    code_editor_container_id,
    process_window_container_id,
    // cmid,
  ){
    $(code_editor_container_id).html("");

    $(code_editor_container_id).css({ "overflow-y": "scroll" });

    $(process_window_container_id).css("background-color", "white");

    console.log("What is the current theme?" + GW.settings.selected_monaco_theme)


    if (code_type == "builtin") {
      code = code.replace(/\\/g, "\\\\");

      code = GW.general.parseResponse(code);

      var cont =
        '     <label for="builtinprocess" class="col-sm-4 col-form-label control-label" style="font-size:12px;" >Select a process: </label>' +
        '     <div class="col-sm-8"> <select class="form-control builtin-process" id="builtin_processes">';

      for (var i = 0; i < GW.process.builtin_processes.length; i++) {
        var se = "";

        if (GW.process.builtin_processes[i].operation == code.operation) {
          se = ' selected="selected" ';
        }

        cont +=
          '    		<option value="' +
          GW.process.builtin_processes[i].operation +
          '"  ' +
          se +
          " >" +
          GW.process.builtin_processes[i].operation +
          "</option>";
      }

      cont += "  		</select></div>";

      $(code_editor_container_id).html(cont);

      $("#builtin_processes").on("change", function () {
        GW.process.refreshBuiltinParameterList(
          "builtin_processes",
          code_editor_container_id.substring(1),
        );

        // GW.process.updateBuiltin();
      });

      $("#builtin_processes").val(code.operation);

      $("#builtin_processes").trigger("change");

      for (var j = 0; j < code.params.length; j += 1) {
        $("#param_" + code.params[j].name).val(code.params[j].value);
      }
    } else if(code_type=="shell"){

      require.config({ paths: { 'vs': '../js/Monaco-Editor/dev/vs' }});

      require(['vs/editor/editor.main'], function() {

        var editorContainerId = code_editor_container_id.substring(1); // Assuming it starts with '#'
        var container = document.getElementById(editorContainerId);
        
        if (!container) {
            console.error('Editor container not found.');
            return;
        }

        var editor = monaco.editor.create(container, 
          {
            value: code || '#!/bin/bash',
            language: 'shell',
            theme: GW.settings.selected_monaco_theme,
            lineNumbers: true,
            roundedSelection: false,
            scrollBeyondLastLine: false,
            readOnly: false,
            fontSize: 14,
            automaticLayout: true,
            formatOnSave: true,
            formatOnPaste: true,
            folding: true,
            formatOnType: true,
            showFoldingControls: 'always',
            wordWrap: 'on',
            // scrollBeyondLastLine: true,
            contextmenu: true, // Enable the context menu for additional clipboard actions
          }
        );

        GW.process.util.add_editor_actions(editor)
          
        GW.process.editor = editor;
        GW.process.sidepanel.editor = editor;

        // editor.onDidChangeModelContent(function(event) {
        //     console.log('Content changed');
        // });
        
      });

    }else{

      require.config({ paths: { 'vs': '../js/Monaco-Editor/dev/vs' }});

      require(['vs/editor/editor.main'], function() {
          var editorContainerId = code_editor_container_id.substring(1); // Assuming it starts with '#'
          var container = document.getElementById(editorContainerId);

          if (!container) {
              console.error('Editor container not found.');
              return;
          }

          // container.style.height = '820px'; // Set a non-zero height
          // container.style.width = '100%'; // Set the width to fill the container

          var editor = monaco.editor.create(container, {
              value: code || '# Write your first Python code in Geoweaver',
              language: 'python',
              theme: GW.settings.selected_monaco_theme,
              lineNumbers: true,
              roundedSelection: false,
              scrollBeyondLastLine: false,
              readOnly: false,
              fontSize: 14,
              automaticLayout: true,
              formatOnSave: true,
              formatOnPaste: true,
              folding: true,
              formatOnType: true,
              showFoldingControls: 'always',
              wordWrap: 'on',
              // scrollBeyondLastLine: true,
              contextmenu: true, // Enable the context menu for additional clipboard actions
          });
          GW.process.util.add_editor_actions(editor)

          GW.process.editor = editor;
          GW.process.sidepanel.editor = editor;

          GW.settings.syncMonacoStyles(GW.process.editor)

          editor.onDidChangeModelContent(function(event) {
              console.log('Content changed');
          });
      });
    
      
    }
  
  },
  
  activateResizer: function (resizer_line_id) {
    console.log("=== activateResizer START ===", resizer_line_id);
    
    // Query the element
    const resizer = document.getElementById(resizer_line_id);
    if (!resizer) {
      console.error("activateResizer: Resizer element not found:", resizer_line_id);
      return;
    }
    console.log("activateResizer: Resizer element found:", resizer);
    console.log("activateResizer: Resizer computed styles:", {
      display: window.getComputedStyle(resizer).display,
      visibility: window.getComputedStyle(resizer).visibility,
      pointerEvents: window.getComputedStyle(resizer).pointerEvents,
      zIndex: window.getComputedStyle(resizer).zIndex,
      width: window.getComputedStyle(resizer).width,
      height: window.getComputedStyle(resizer).height,
      cursor: window.getComputedStyle(resizer).cursor
    });
    
    const leftSide = resizer.previousElementSibling;
    const rightSide = resizer.nextElementSibling;
    
    // Also try to get by ID as fallback
    const leftSideById = document.getElementById("process_code_window");
    const rightSideById = document.getElementById("single-console-content");
    
    console.log("activateResizer: Element lookup:", {
      previousSibling: leftSide ? (leftSide.id || leftSide.className || "unknown") : null,
      nextSibling: rightSide ? (rightSide.id || rightSide.className || "unknown") : null,
      leftSideById: leftSideById ? leftSideById.id : null,
      rightSideById: rightSideById ? rightSideById.id : null
    });
    
    // Use ID lookup as fallback if sibling lookup fails
    const finalLeftSide = leftSide || leftSideById;
    const finalRightSide = rightSide || rightSideById;
    
    if (!finalLeftSide || !finalRightSide) {
      console.error("activateResizer: Sibling elements not found", {
        leftSide: !!leftSide,
        rightSide: !!rightSide,
        leftSideById: !!leftSideById,
        rightSideById: !!rightSideById,
        previousSibling: resizer.previousElementSibling,
        nextSibling: resizer.nextElementSibling
      });
      return;
    }
    
    console.log("activateResizer: Using elements:", {
      leftSide: finalLeftSide.id || finalLeftSide.className || "unknown",
      rightSide: finalRightSide.id || finalRightSide.className || "unknown",
      leftSideRect: finalLeftSide.getBoundingClientRect(),
      rightSideRect: finalRightSide.getBoundingClientRect()
    });
    
    // Remove any existing event listeners first (must match addEventListener parameters)
    if (GW.process.mouseDownHandler) {
      console.log("activateResizer: Removing existing mousedown handler");
      resizer.removeEventListener("mousedown", GW.process.mouseDownHandler, true);
    }
    
    resizer.style.cursor = "ew-resize";

    // The current position of mouse
    let x = 0;
    let y = 0;
    let leftWidth = 0;
    
    // Store references to elements for use in event handlers
    const leftSideElement = finalLeftSide;
    const rightSideElement = finalRightSide;

    // Handle the mousedown event
    // that's triggered when user drags the resizer
    GW.process.mouseDownHandler = function (e) {
      console.log("=== activateResizer: mousedown triggered ===", {
        resizer_line_id: resizer_line_id,
        target: e.target,
        currentTarget: e.currentTarget,
        clientX: e.clientX,
        clientY: e.clientY,
        button: e.button,
        buttons: e.buttons,
        type: e.type
      });
      
      e.preventDefault();
      e.stopPropagation();
      
      // Get the current mouse position
      x = e.clientX;
      y = e.clientY;
      leftWidth = leftSideElement.getBoundingClientRect().width;
      
      console.log("activateResizer: Initial values:", {
        x: x,
        y: y,
        leftWidth: leftWidth,
        leftSideRect: leftSideElement.getBoundingClientRect(),
        rightSideRect: rightSideElement.getBoundingClientRect(),
        parentRect: resizer.parentNode.getBoundingClientRect(),
        leftSideId: leftSideElement.id,
        rightSideId: rightSideElement.id
      });

      // Remove the handlers of `mousemove` and `mouseup` (must match addEventListener parameters)
      document.removeEventListener(
        "mousemove",
        GW.process.mouseMoveVerticalHandler,
        true
      );
      document.removeEventListener(
        "mouseup",
        GW.process.mouseUpVerticalHandler,
        true
      );
      // Attach the listeners to `document`
      console.log("activateResizer: Adding mousemove and mouseup listeners to document");
      document.addEventListener("mousemove", GW.process.mouseMoveHandler, true);
      document.addEventListener("mouseup", GW.process.mouseUpHandler, true);
    };

    GW.process.mouseMoveHandler = function (e) {
      e.preventDefault();
      
      // How far the mouse has been moved
      const dx = e.clientX - x;
      const dy = e.clientY - y;
      
      console.log("activateResizer: mousemove", {
        clientX: e.clientX,
        clientY: e.clientY,
        dx: dx,
        dy: dy,
        originalX: x,
        originalY: y
      });

      const parentWidth = resizer.parentNode.getBoundingClientRect().width;
      console.log("activateResizer: Parent width:", parentWidth);
      
      if (parentWidth > 0) {
        const newLeftWidth = Math.max(10, Math.min(90, ((leftWidth + dx) * 100) / parentWidth));
        console.log("activateResizer: Setting widths:", {
          newLeftWidth: newLeftWidth,
          newRightWidth: 100 - newLeftWidth,
          calculation: `(${leftWidth} + ${dx}) * 100 / ${parentWidth} = ${((leftWidth + dx) * 100) / parentWidth}`,
          leftSideId: leftSideElement.id,
          rightSideId: rightSideElement.id
        });
        
        // Save the width ratio for later restoration
        GW.process.savedLeftWidthRatio = newLeftWidth;
        
        // Set width on parent containers - override flex property
        leftSideElement.style.setProperty("width", `${newLeftWidth}%`, "important");
        leftSideElement.style.setProperty("min-width", "0", "important");
        leftSideElement.style.setProperty("max-width", "none", "important");
        leftSideElement.style.setProperty("flex", `0 0 ${newLeftWidth}%`, "important");
        leftSideElement.style.setProperty("flex-basis", `${newLeftWidth}%`, "important");
        leftSideElement.style.setProperty("flex-grow", "0", "important");
        leftSideElement.style.setProperty("flex-shrink", "0", "important");
        
        rightSideElement.style.setProperty("width", `${100 - newLeftWidth}%`, "important");
        rightSideElement.style.setProperty("min-width", "0", "important");
        rightSideElement.style.setProperty("max-width", "none", "important");
        rightSideElement.style.setProperty("flex", `0 0 ${100 - newLeftWidth}%`, "important");
        rightSideElement.style.setProperty("flex-basis", `${100 - newLeftWidth}%`, "important");
        rightSideElement.style.setProperty("flex-grow", "0", "important");
        rightSideElement.style.setProperty("flex-shrink", "0", "important");
        
        // Also update child elements to ensure they respect parent width
        const codeEmbed = document.getElementById("code-embed");
        if (codeEmbed) {
          codeEmbed.style.setProperty("width", "100%", "important");
          codeEmbed.style.setProperty("min-width", "0", "important");
          codeEmbed.style.setProperty("max-width", "100%", "important");
          codeEmbed.style.setProperty("box-sizing", "border-box", "important");
        }
        
        const processLogWindow = document.getElementById("process-log-window");
        if (processLogWindow) {
          processLogWindow.style.setProperty("width", "100%", "important");
          processLogWindow.style.setProperty("min-width", "0", "important");
          processLogWindow.style.setProperty("max-width", "100%", "important");
          processLogWindow.style.setProperty("box-sizing", "border-box", "important");
          processLogWindow.style.setProperty("overflow-x", "hidden", "important");
          processLogWindow.style.setProperty("overflow-wrap", "break-word", "important");
          processLogWindow.style.setProperty("word-wrap", "break-word", "important");
          processLogWindow.style.setProperty("word-break", "break-word", "important");
        }
        
        // Also ensure single-console-content has width constraints
        const singleConsoleContent = document.getElementById("single-console-content");
        if (singleConsoleContent) {
          singleConsoleContent.style.setProperty("width", "100%", "important");
          singleConsoleContent.style.setProperty("max-width", "100%", "important");
          singleConsoleContent.style.setProperty("box-sizing", "border-box", "important");
          singleConsoleContent.style.setProperty("overflow-x", "hidden", "important");
        }
        
        // Ensure console-output also respects width
        const consoleOutput = document.getElementById("console-output");
        if (consoleOutput) {
          consoleOutput.style.setProperty("width", "100%", "important");
          consoleOutput.style.setProperty("max-width", "100%", "important");
          consoleOutput.style.setProperty("box-sizing", "border-box", "important");
          consoleOutput.style.setProperty("overflow", "hidden", "important");
        }
        
        // Ensure resizer remains visible and positioned correctly
        resizer.style.setProperty("display", "block", "important");
        resizer.style.setProperty("visibility", "visible", "important");
        resizer.style.setProperty("opacity", "1", "important");
        resizer.style.setProperty("z-index", "100", "important");
        resizer.style.setProperty("position", "relative", "important");
        
        // Refresh Monaco editor layout if it exists
        if (GW.process.editor) {
          GW.process.editor.layout();
        }
        
        console.log("activateResizer: Widths applied successfully", {
          leftSideComputedWidth: window.getComputedStyle(leftSideElement).width,
          rightSideComputedWidth: window.getComputedStyle(rightSideElement).width,
          leftSideFlex: window.getComputedStyle(leftSideElement).flex,
          rightSideFlex: window.getComputedStyle(rightSideElement).flex,
          leftSideRect: leftSideElement.getBoundingClientRect(),
          rightSideRect: rightSideElement.getBoundingClientRect(),
          resizerDisplay: window.getComputedStyle(resizer).display,
          resizerVisibility: window.getComputedStyle(resizer).visibility
        });
      } else {
        console.warn("activateResizer: Parent width is 0 or negative, skipping resize");
      }

      resizer.style.cursor = "ew-resize";
      document.body.style.cursor = "ew-resize";

      leftSideElement.style.userSelect = "none";
      leftSideElement.style.pointerEvents = "none";

      rightSideElement.style.userSelect = "none";
      rightSideElement.style.pointerEvents = "none";
    };

    GW.process.mouseUpHandler = function (e) {
      console.log("=== activateResizer: mouseup triggered ===", {
        clientX: e ? e.clientX : 'N/A',
        clientY: e ? e.clientY : 'N/A',
        type: e ? e.type : 'N/A'
      });
      
      resizer.style.removeProperty("cursor");
      document.body.style.removeProperty("cursor");

      leftSideElement.style.removeProperty("user-select");
      leftSideElement.style.removeProperty("pointer-events");

      rightSideElement.style.removeProperty("user-select");
      rightSideElement.style.removeProperty("pointer-events");
      
      // Ensure resizer remains visible after resize
      resizer.style.setProperty("display", "block", "important");
      resizer.style.setProperty("visibility", "visible", "important");
      resizer.style.setProperty("opacity", "1", "important");
      resizer.style.setProperty("z-index", "100", "important");
      resizer.style.setProperty("position", "relative", "important");
      resizer.style.setProperty("width", "4px", "important");
      resizer.style.setProperty("height", "100%", "important");
      resizer.style.setProperty("background-color", "#cbd5e0", "important");
      resizer.style.setProperty("cursor", "ew-resize", "important");
      resizer.style.setProperty("pointer-events", "auto", "important");

      // Remove the handlers of `mousemove` and `mouseup` (must match addEventListener parameters)
      console.log("activateResizer: Removing mousemove and mouseup listeners from document");
      document.removeEventListener("mousemove", GW.process.mouseMoveHandler, true);
      document.removeEventListener("mouseup", GW.process.mouseUpHandler, true);
    };

    // Attach the handler - use capture phase to ensure it's triggered
    console.log("activateResizer: Attaching mousedown event listener with capture phase");
    resizer.addEventListener("mousedown", GW.process.mouseDownHandler, true);
    
    // Also add pointer-events to ensure it's clickable
    resizer.style.setProperty("pointer-events", "auto", "important");
    resizer.style.setProperty("touch-action", "none", "important");
    
    // Test if element is actually clickable
    console.log("activateResizer: Final resizer state:", {
      element: resizer,
      hasListener: true,
      pointerEvents: resizer.style.pointerEvents || window.getComputedStyle(resizer).pointerEvents,
      cursor: resizer.style.cursor || window.getComputedStyle(resizer).cursor,
      boundingRect: resizer.getBoundingClientRect()
    });
    
    console.log("=== activateResizer END ===", resizer_line_id);
  },

  activateVerticalResizer: function (resizer_line_id) {
    console.log("=== activateVerticalResizer START ===", resizer_line_id);

    // Query the element
    const resizer = document.getElementById(resizer_line_id);
    if (!resizer) {
      console.error("activateVerticalResizer: Resizer element not found:", resizer_line_id);
      return;
    }
    console.log("activateVerticalResizer: Resizer element found:", resizer);
    console.log("activateVerticalResizer: Resizer computed styles:", {
      display: window.getComputedStyle(resizer).display,
      visibility: window.getComputedStyle(resizer).visibility,
      pointerEvents: window.getComputedStyle(resizer).pointerEvents,
      zIndex: window.getComputedStyle(resizer).zIndex,
      width: window.getComputedStyle(resizer).width,
      height: window.getComputedStyle(resizer).height,
      cursor: window.getComputedStyle(resizer).cursor
    });
    
    const topElement = resizer.previousElementSibling;
    const bottomElement = resizer.nextElementSibling;
    
    // Also try to get by ID as fallback
    const topElementById = document.getElementById("process_code_window");
    const bottomElementById = document.getElementById("single-console-content");
    
    console.log("activateVerticalResizer: Element lookup:", {
      previousSibling: topElement ? (topElement.id || topElement.className || "unknown") : null,
      nextSibling: bottomElement ? (bottomElement.id || bottomElement.className || "unknown") : null,
      topElementById: topElementById ? topElementById.id : null,
      bottomElementById: bottomElementById ? bottomElementById.id : null
    });
    
    // Use ID lookup as fallback if sibling lookup fails
    const finalTopElement = topElement || topElementById;
    const finalBottomElement = bottomElement || bottomElementById;
    
    if (!finalTopElement || !finalBottomElement) {
      console.error("activateVerticalResizer: Sibling elements not found", {
        topElement: !!topElement,
        bottomElement: !!bottomElement,
        topElementById: !!topElementById,
        bottomElementById: !!bottomElementById,
        previousSibling: resizer.previousElementSibling,
        nextSibling: resizer.nextElementSibling
      });
      return;
    }
    
    console.log("activateVerticalResizer: Using elements:", {
      topElement: finalTopElement.id || finalTopElement.className || "unknown",
      bottomElement: finalBottomElement.id || finalBottomElement.className || "unknown",
      topElementRect: finalTopElement.getBoundingClientRect(),
      bottomElementRect: finalBottomElement.getBoundingClientRect()
    });
    
    // Remove any existing event listeners first (must match addEventListener parameters)
    if (GW.process.mouseDownVerticalHandler) {
      console.log("activateVerticalResizer: Removing existing mousedown handler");
      resizer.removeEventListener("mousedown", GW.process.mouseDownVerticalHandler, true);
    }
    
    resizer.style.cursor = "ns-resize";

    // The current position of mouse
    let x = 0;
    let y = 0;
    let topHeight = 0;
    
    // Store references to elements for use in event handlers
    const topElementRef = finalTopElement;
    const bottomElementRef = finalBottomElement;

    // Handle the mousedown event
    // that's triggered when user drags the resizer
    GW.process.mouseDownVerticalHandler = function (e) {
      console.log("=== activateVerticalResizer: mousedown triggered ===", {
        resizer_line_id: resizer_line_id,
        target: e.target,
        currentTarget: e.currentTarget,
        clientX: e.clientX,
        clientY: e.clientY,
        button: e.button,
        buttons: e.buttons,
        type: e.type
      });
      
      e.preventDefault();
      e.stopPropagation();
      
      // Get the current mouse position
      x = e.clientX;
      y = e.clientY;
      topHeight = topElementRef.getBoundingClientRect().height;
      
      console.log("activateVerticalResizer: Initial values:", {
        x: x,
        y: y,
        topHeight: topHeight,
        topElementRect: topElementRef.getBoundingClientRect(),
        bottomElementRect: bottomElementRef.getBoundingClientRect(),
        parentRect: resizer.parentNode.getBoundingClientRect(),
        topElementId: topElementRef.id,
        bottomElementId: bottomElementRef.id
      });

      // Remove the handlers of `mousemove` and `mouseup` (must match addEventListener parameters)
      document.removeEventListener("mousemove", GW.process.mouseMoveHandler, true);
      document.removeEventListener("mouseup", GW.process.mouseUpHandler, true);
      // Attach the listeners to `document`
      console.log("activateVerticalResizer: Adding mousemove and mouseup listeners to document");
      document.addEventListener(
        "mousemove",
        GW.process.mouseMoveVerticalHandler,
        true
      );
      document.addEventListener("mouseup", GW.process.mouseUpVerticalHandler, true);
    };

    GW.process.mouseMoveVerticalHandler = function (e) {
      e.preventDefault();
      
      // How far the mouse has been moved
      const dx = e.clientX - x;
      const dy = e.clientY - y;
      
      console.log("activateVerticalResizer: mousemove", {
        clientX: e.clientX,
        clientY: e.clientY,
        dx: dx,
        dy: dy,
        originalX: x,
        originalY: y
      });

      const parentHeight = resizer.parentNode.getBoundingClientRect().height;
      console.log("activateVerticalResizer: Parent height:", parentHeight);
      
      if (parentHeight > 0) {
        const newtopHeight = Math.max(10, Math.min(90, ((topHeight + dy) * 100) / parentHeight));
        console.log("activateVerticalResizer: Setting heights:", {
          newtopHeight: newtopHeight,
          newBottomHeight: 100 - newtopHeight,
          calculation: `(${topHeight} + ${dy}) * 100 / ${parentHeight} = ${((topHeight + dy) * 100) / parentHeight}`,
          topElementId: topElementRef.id,
          bottomElementId: bottomElementRef.id
        });
        
        // Save the height ratio for later restoration
        GW.process.savedTopHeightRatio = newtopHeight;
        
        // Set height on parent containers - override flex property
        topElementRef.style.setProperty("height", `${newtopHeight}%`, "important");
        topElementRef.style.setProperty("min-height", "0", "important");
        topElementRef.style.setProperty("max-height", "none", "important");
        topElementRef.style.setProperty("flex", `0 0 ${newtopHeight}%`, "important");
        topElementRef.style.setProperty("flex-basis", `${newtopHeight}%`, "important");
        topElementRef.style.setProperty("flex-grow", "0", "important");
        topElementRef.style.setProperty("flex-shrink", "0", "important");
        
        bottomElementRef.style.setProperty("height", `${100 - newtopHeight}%`, "important");
        bottomElementRef.style.setProperty("min-height", "0", "important");
        bottomElementRef.style.setProperty("max-height", "none", "important");
        bottomElementRef.style.setProperty("flex", `0 0 ${100 - newtopHeight}%`, "important");
        bottomElementRef.style.setProperty("flex-basis", `${100 - newtopHeight}%`, "important");
        bottomElementRef.style.setProperty("flex-grow", "0", "important");
        bottomElementRef.style.setProperty("flex-shrink", "0", "important");
        
        // Also update child elements to ensure they respect parent height
        const codeEmbed = document.getElementById("code-embed");
        if (codeEmbed) {
          codeEmbed.style.setProperty("height", "100%", "important");
          codeEmbed.style.setProperty("min-height", "0", "important");
          codeEmbed.style.setProperty("max-height", "100%", "important");
          codeEmbed.style.setProperty("box-sizing", "border-box", "important");
        }
        
        const processLogWindow = document.getElementById("process-log-window");
        if (processLogWindow) {
          processLogWindow.style.setProperty("height", "100%", "important");
          processLogWindow.style.setProperty("min-height", "0", "important");
          processLogWindow.style.setProperty("max-height", "100%", "important");
          processLogWindow.style.setProperty("box-sizing", "border-box", "important");
        }
        
        // Ensure resizer remains visible and positioned correctly
        resizer.style.setProperty("display", "block", "important");
        resizer.style.setProperty("visibility", "visible", "important");
        resizer.style.setProperty("opacity", "1", "important");
        resizer.style.setProperty("z-index", "100", "important");
        resizer.style.setProperty("position", "relative", "important");
        
        // Refresh Monaco editor layout if it exists
        if (GW.process.editor) {
          GW.process.editor.layout();
        }
        
        console.log("activateVerticalResizer: Heights applied successfully", {
          topElementComputedHeight: window.getComputedStyle(topElementRef).height,
          bottomElementComputedHeight: window.getComputedStyle(bottomElementRef).height,
          topElementFlex: window.getComputedStyle(topElementRef).flex,
          bottomElementFlex: window.getComputedStyle(bottomElementRef).flex,
          topElementRect: topElementRef.getBoundingClientRect(),
          bottomElementRect: bottomElementRef.getBoundingClientRect(),
          resizerDisplay: window.getComputedStyle(resizer).display,
          resizerVisibility: window.getComputedStyle(resizer).visibility
        });
      } else {
        console.warn("activateVerticalResizer: Parent height is 0 or negative, skipping resize");
      }

      resizer.style.cursor = "ns-resize";
      document.body.style.cursor = "ns-resize";

      topElementRef.style.userSelect = "none";
      topElementRef.style.pointerEvents = "none";

      bottomElementRef.style.userSelect = "none";
      bottomElementRef.style.pointerEvents = "none";
    };

    GW.process.mouseUpVerticalHandler = function (e) {
      console.log("=== activateVerticalResizer: mouseup triggered ===", {
        clientX: e ? e.clientX : 'N/A',
        clientY: e ? e.clientY : 'N/A',
        type: e ? e.type : 'N/A'
      });
      
      resizer.style.removeProperty("cursor");
      document.body.style.removeProperty("cursor");

      topElementRef.style.removeProperty("user-select");
      topElementRef.style.removeProperty("pointer-events");

      bottomElementRef.style.removeProperty("user-select");
      bottomElementRef.style.removeProperty("pointer-events");
      
      // Ensure resizer remains visible after resize
      resizer.style.setProperty("display", "block", "important");
      resizer.style.setProperty("visibility", "visible", "important");
      resizer.style.setProperty("opacity", "1", "important");
      resizer.style.setProperty("z-index", "100", "important");
      resizer.style.setProperty("position", "relative", "important");
      resizer.style.setProperty("width", "100%", "important");
      resizer.style.setProperty("height", "4px", "important");
      resizer.style.setProperty("background-color", "#cbd5e0", "important");
      resizer.style.setProperty("cursor", "ns-resize", "important");
      resizer.style.setProperty("pointer-events", "auto", "important");

      // Remove the handlers of `mousemove` and `mouseup` (must match addEventListener parameters)
      console.log("activateVerticalResizer: Removing mousemove and mouseup listeners from document");
      document.removeEventListener(
        "mousemove",
        GW.process.mouseMoveVerticalHandler,
        true
      );
      document.removeEventListener(
        "mouseup",
        GW.process.mouseUpVerticalHandler,
        true
      );
    };

    // Attach the handler - use capture phase to ensure it's triggered
    console.log("activateVerticalResizer: Attaching mousedown event listener with capture phase");
    resizer.addEventListener("mousedown", GW.process.mouseDownVerticalHandler, true);
    
    // Also add pointer-events to ensure it's clickable
    resizer.style.setProperty("pointer-events", "auto", "important");
    resizer.style.setProperty("touch-action", "none", "important");
    
    // Test if element is actually clickable
    console.log("activateVerticalResizer: Final resizer state:", {
      element: resizer,
      hasListener: true,
      pointerEvents: resizer.style.pointerEvents || window.getComputedStyle(resizer).pointerEvents,
      cursor: resizer.style.cursor || window.getComputedStyle(resizer).cursor,
      boundingRect: resizer.getBoundingClientRect()
    });
    
    console.log("=== activateVerticalResizer END ===", resizer_line_id);
  },

  noDock: function (
    history_section_id,
    code_window_id,
    console_content_id,
    resize_line_id,
  ) {
    var codeContainer = document.getElementById(history_section_id);
    var resizerDrag = document.getElementById(resize_line_id);
    var consoleElement = document.getElementById(console_content_id);
    var codeElement = document.getElementById(code_window_id);
    
    if (!codeContainer || !resizerDrag || !consoleElement || !codeElement) {
      console.error("noDock: Required elements not found");
      return;
    }
    
    // Hide resizer line
    resizerDrag.style.setProperty("height", "0px", "important");
    resizerDrag.style.setProperty("width", "100%", "important");
    resizerDrag.style.setProperty("display", "none", "important");
    
    // Show code container
    codeContainer.style.setProperty("display", "block", "important");
    
    // Hide console completely
    consoleElement.style.setProperty("width", "100%", "important");
    consoleElement.style.setProperty("height", "0%", "important");
    consoleElement.style.setProperty("display", "none", "important");
    consoleElement.style.setProperty("visibility", "hidden", "important");
    consoleElement.style.setProperty("overflow", "hidden", "important");
    consoleElement.style.setProperty("padding", "0", "important");
    consoleElement.style.setProperty("margin", "0", "important");
    
    // Show code window full size
    codeElement.style.setProperty("width", "100%", "important");
    codeElement.style.setProperty("height", "100%", "important");
    codeElement.style.setProperty("display", "flex", "important");
    codeElement.style.setProperty("visibility", "visible", "important");
    codeElement.style.setProperty("overflow", "auto", "important");

    // Show process info bar when not maximized (only if it was expanded)
    var processInfoBar = document.getElementById('process-info-bar');
    if (processInfoBar) {
      var toggleBtn = document.getElementById("toggle-details-btn");
      var icon = toggleBtn ? toggleBtn.querySelector('i') : null;
      // Only show if it was expanded (chevron-up icon)
      if (icon && icon.className.includes("chevron-up")) {
        processInfoBar.style.setProperty("display", "block", "important");
        processInfoBar.style.setProperty("visibility", "visible", "important");
        processInfoBar.style.setProperty("height", "auto", "important");
        processInfoBar.style.setProperty("max-height", "200px", "important");
        processInfoBar.style.setProperty("padding", "12px 20px", "important");
        processInfoBar.style.setProperty("margin", "", "important");
        processInfoBar.style.setProperty("overflow", "visible", "important");
        processInfoBar.style.setProperty("position", "relative", "important");
        processInfoBar.style.setProperty("top", "auto", "important");
        processInfoBar.style.setProperty("left", "auto", "important");
        processInfoBar.style.setProperty("z-index", "auto", "important");
        processInfoBar.style.setProperty("opacity", "1", "important");
        processInfoBar.style.setProperty("pointer-events", "auto", "important");
      }
    }

    // Refresh Monaco editor layout if it exists
    if (GW.process.editor) {
      setTimeout(function() {
        GW.process.editor.layout();
      }, 100);
    }
  },

  bottomDock: function (
    history_section_id,
    code_window_id,
    console_content_id,
    resize_line_id,
  ) {
    var codeContainer = document.getElementById(history_section_id);
    var resizerDrag = document.getElementById(resize_line_id);
    var codeElement = document.getElementById(code_window_id);
    var consoleElement = document.getElementById(console_content_id);
    
    if (!codeContainer || !resizerDrag || !codeElement || !consoleElement) {
      console.error("bottomDock: Required elements not found", {
        codeContainer: !!codeContainer,
        resizerDrag: !!resizerDrag,
        codeElement: !!codeElement,
        consoleElement: !!consoleElement
      });
      return;
    }
    
    // Set container to flex column layout - but don't affect parent tabs container
    codeContainer.style.setProperty("display", "flex", "important");
    codeContainer.style.setProperty("flex-direction", "column", "important");
    codeContainer.style.setProperty("height", "100%", "important");
    codeContainer.style.setProperty("width", "100%", "important");
    
    // Show and configure resizer for vertical dragging (horizontal line)
    // Keep consistent style across all modes
    resizerDrag.style.setProperty("height", "4px", "important");
    resizerDrag.style.setProperty("width", "100%", "important");
    resizerDrag.style.setProperty("display", "block", "important");
    resizerDrag.style.setProperty("visibility", "visible", "important");
    resizerDrag.style.setProperty("cursor", "ns-resize", "important");
    resizerDrag.style.setProperty("background-color", "#cbd5e0", "important");
    resizerDrag.style.setProperty("flex-shrink", "0", "important");
    resizerDrag.style.setProperty("z-index", "100", "important");
    resizerDrag.style.setProperty("user-select", "none", "important");
    resizerDrag.style.setProperty("position", "relative", "important");
    resizerDrag.style.setProperty("pointer-events", "auto", "important");
    resizerDrag.style.setProperty("touch-action", "none", "important");
    resizerDrag.style.setProperty("transition", "background-color 0.2s", "important");
    resizerDrag.style.setProperty("opacity", "1", "important");
    
    // Add hover effect
    resizerDrag.onmouseenter = function() {
      this.style.setProperty("background-color", "#a0aec0", "important");
    };
    resizerDrag.onmouseleave = function() {
      this.style.setProperty("background-color", "#cbd5e0", "important");
    };

    // Use saved height ratio if available, otherwise use default 60/40
    var topHeight = GW.process.savedTopHeightRatio !== null ? GW.process.savedTopHeightRatio : 60;
    var bottomHeight = 100 - topHeight;

    // Set code window to top portion
    codeElement.style.setProperty("width", "100%", "important");
    codeElement.style.setProperty("height", `${topHeight}%`, "important");
    codeElement.style.setProperty("display", "flex", "important");
    codeElement.style.setProperty("flex-shrink", "0", "important");
    codeElement.style.setProperty("overflow", "auto", "important");
    codeElement.style.setProperty("min-height", "0", "important");
    codeElement.style.setProperty("flex", `0 0 ${topHeight}%`, "important");
    codeElement.style.setProperty("flex-basis", `${topHeight}%`, "important");
    codeElement.style.setProperty("flex-grow", "0", "important");

    // Set console to bottom portion
    consoleElement.style.setProperty("width", "100%", "important");
    consoleElement.style.setProperty("height", `${bottomHeight}%`, "important");
    consoleElement.style.setProperty("display", "flex", "important");
    consoleElement.style.setProperty("flex-direction", "column", "important");
    consoleElement.style.setProperty("flex-shrink", "0", "important");
    // Allow scrolling for console content - use auto to show scrollbar only when needed
    consoleElement.style.setProperty("overflow-y", "auto", "important");
    consoleElement.style.setProperty("overflow-x", "hidden", "important");
    consoleElement.style.setProperty("visibility", "visible", "important");
    consoleElement.style.setProperty("min-height", "0", "important");
    consoleElement.style.setProperty("flex", `0 0 ${bottomHeight}%`, "important");
    consoleElement.style.setProperty("flex-basis", `${bottomHeight}%`, "important");
    consoleElement.style.setProperty("flex-grow", "0", "important");

    // Hide process info bar when maximized, but keep header visible
    var processInfoBar = document.getElementById('process-info-bar');
    if (processInfoBar) {
      processInfoBar.style.setProperty("display", "none", "important");
      processInfoBar.style.setProperty("visibility", "hidden", "important");
      processInfoBar.style.setProperty("height", "0", "important");
      processInfoBar.style.setProperty("padding", "0", "important");
      processInfoBar.style.setProperty("margin", "0", "important");
      processInfoBar.style.setProperty("overflow", "hidden", "important");
    }

    // Activating resizer functionality for vertical resizing
    // Use setTimeout to ensure DOM is ready and remove any existing listeners first
    setTimeout(function() {
      var resizer = document.getElementById(resize_line_id);
      if (resizer && GW.process.mouseDownVerticalHandler) {
        resizer.removeEventListener("mousedown", GW.process.mouseDownVerticalHandler, true);
      }
      GW.process.util.activateVerticalResizer(resize_line_id);
    }, 50);
    
    // Refresh Monaco editor layout if it exists
    if (GW.process.editor) {
      setTimeout(function() {
        GW.process.editor.layout();
      }, 100);
    }
  },

  leftDock: function (
    history_section_id,
    code_window_id,
    console_content_id,
    resize_line_id,
  ) {
    GW.process.dockmode = "left";

    var codeContainer = document.getElementById(history_section_id);
    var resizerDrag = document.getElementById(resize_line_id);
    var codeElement = document.getElementById(code_window_id);
    var consoleElement = document.getElementById(console_content_id);
    
    if (!codeContainer || !resizerDrag || !codeElement || !consoleElement) {
      console.error("leftDock: Required elements not found", {
        codeContainer: !!codeContainer,
        resizerDrag: !!resizerDrag,
        codeElement: !!codeElement,
        consoleElement: !!consoleElement
      });
      return;
    }
    
    // Set container to flex row layout - but don't affect parent tabs container
    codeContainer.style.setProperty("display", "flex", "important");
    codeContainer.style.setProperty("flex-direction", "row", "important");
    codeContainer.style.setProperty("width", "100%", "important");
    codeContainer.style.setProperty("height", "100%", "important");

    // Show and configure resizer for horizontal dragging (vertical line)
    // Keep consistent style across all modes
    resizerDrag.style.setProperty("height", "100%", "important");
    resizerDrag.style.setProperty("width", "4px", "important");
    resizerDrag.style.setProperty("display", "block", "important");
    resizerDrag.style.setProperty("visibility", "visible", "important");
    resizerDrag.style.setProperty("cursor", "ew-resize", "important");
    resizerDrag.style.setProperty("background-color", "#cbd5e0", "important");
    resizerDrag.style.setProperty("flex-shrink", "0", "important");
    resizerDrag.style.setProperty("z-index", "100", "important");
    resizerDrag.style.setProperty("user-select", "none", "important");
    resizerDrag.style.setProperty("position", "relative", "important");
    resizerDrag.style.setProperty("pointer-events", "auto", "important");
    resizerDrag.style.setProperty("touch-action", "none", "important");
    resizerDrag.style.setProperty("transition", "background-color 0.2s", "important");
    resizerDrag.style.setProperty("opacity", "1", "important");
    
    // Add hover effect
    resizerDrag.onmouseenter = function() {
      this.style.setProperty("background-color", "#a0aec0", "important");
    };
    resizerDrag.onmouseleave = function() {
      this.style.setProperty("background-color", "#cbd5e0", "important");
    };

    // Use saved width ratio if available, otherwise use default 60/40
    var leftWidth = GW.process.savedLeftWidthRatio !== null ? GW.process.savedLeftWidthRatio : 60;
    var rightWidth = 100 - leftWidth;

    // Set code window to left portion
    codeElement.style.setProperty("width", `${leftWidth}%`, "important");
    codeElement.style.setProperty("height", "100%", "important");
    codeElement.style.setProperty("display", "flex", "important");
    codeElement.style.setProperty("flex-shrink", "0", "important");
    codeElement.style.setProperty("overflow", "auto", "important");
    codeElement.style.setProperty("min-width", "0", "important");
    codeElement.style.setProperty("flex", `0 0 ${leftWidth}%`, "important");
    codeElement.style.setProperty("flex-basis", `${leftWidth}%`, "important");
    codeElement.style.setProperty("flex-grow", "0", "important");

    // Set console to right portion
    consoleElement.style.setProperty("width", `${rightWidth}%`, "important");
    consoleElement.style.setProperty("height", "100%", "important");
    consoleElement.style.setProperty("display", "flex", "important");
    consoleElement.style.setProperty("flex-direction", "column", "important");
    consoleElement.style.setProperty("flex-shrink", "0", "important");
    // Allow scrolling for console content - use auto to show scrollbar only when needed
    consoleElement.style.setProperty("overflow-y", "auto", "important");
    consoleElement.style.setProperty("overflow-x", "hidden", "important");
    consoleElement.style.setProperty("visibility", "visible", "important");
    consoleElement.style.setProperty("min-width", "0", "important");
    consoleElement.style.setProperty("flex", `0 0 ${rightWidth}%`, "important");
    consoleElement.style.setProperty("flex-basis", `${rightWidth}%`, "important");
    consoleElement.style.setProperty("flex-grow", "0", "important");

    // Hide process info bar when maximized, but keep header visible
    var processInfoBar = document.getElementById('process-info-bar');
    if (processInfoBar) {
      processInfoBar.style.setProperty("display", "none", "important");
      processInfoBar.style.setProperty("visibility", "hidden", "important");
      processInfoBar.style.setProperty("height", "0", "important");
      processInfoBar.style.setProperty("padding", "0", "important");
      processInfoBar.style.setProperty("margin", "0", "important");
      processInfoBar.style.setProperty("overflow", "hidden", "important");
    }

    // Activating resizer functionality for horizontal resizing
    // Use setTimeout to ensure DOM is ready and remove any existing listeners first
    setTimeout(function() {
      var resizer = document.getElementById(resize_line_id);
      if (resizer && GW.process.mouseDownHandler) {
        resizer.removeEventListener("mousedown", GW.process.mouseDownHandler, true);
      }
      GW.process.util.activateResizer(resize_line_id);
    }, 50);
    
    // Refresh Monaco editor layout if it exists
    if (GW.process.editor) {
      setTimeout(function() {
        GW.process.editor.layout();
      }, 100);
    }
  },

  displayToolbar: function (
    process_id,
    process_name,
    code_type,
    process_btn_group_id,
  ) {
    var menuItem =
      ' <p class="h6" align="right">' +
      '<button type="button" class="btn btn-outline-primary" onclick="GW.process.history(\'' +
      process_id +
      "', '" +
      process_name +
      '\')"><i class="fa fa-history subalignicon"  data-toggle="tooltip" title="List history logs"></i> History </button>' +
      ' <button type="button" class="btn btn-outline-primary" onclick="GW.process.editSwitch()">' +
      '<i class="fa fa-edit subalignicon"  data-toggle="tooltip" title="Enable Edit"></i> Edit </button>' +
      ' <button type="button" class="btn btn-outline-primary" onclick="GW.process.runProcess(\'' +
      process_id +
      "', '" +
      process_name +
      "', '" +
      code_type +
      '\')" ><i class="fa fa-play subalignicon"  data-toggle="tooltip" title="Run Process"></i> Run </button> ' +
      ' <button type="button" class="btn btn-outline-primary" onclick="GW.menu.del(\'' +
      process_id +
      '\',\'process\')"><i class="fa fa-minus subalignicon" style="color:red;"  data-toggle="tooltip" title="Delete this process" > Delete</i>  </button>' +
      "</p>";

    $(process_btn_group_id).append(menuItem);
  },

  history: function (
    pid,
    pname,
    process_history_container_id,
    process_history_table_id,
    close_history,
    history_tab_id,
    history_tab_target_id,
  ) {
    $.ajax({
      url: "logs",

      method: "POST",

      data: "type=process&id=" + pid + "&pname=" + pname,
    })
      .done(function (msg) {
        // for process dialog
        $("#prompt-panel-process-history-container").css("display", "block");
        $("#history-tab-loader-process-detail").css("display", "none");

        // for main history container
        $("#process-history-container").css("display", "block");
        $("#history-tab-loader-main-detail").css("display", "none");

        if (!msg.length) {
          // Show friendly empty state message instead of alert
          $(process_history_container_id).html(`
            <div style="text-align: center; padding: 40px; color: #666; background-color: #f8f9fa; border-radius: 8px; margin: 20px;">
                <div style="font-size: 48px; margin-bottom: 20px; color: #6c757d;">
                    <i class="glyphicon glyphicon-stats" style="font-size: 48px;"></i>
                </div>
                <h4 style="color: #495057; margin-bottom: 10px; font-weight: 500;">No History Available</h4>
                <p style="color: #6c757d; font-size: 14px; margin: 0;">This process hasn't been executed yet or all history records have been cleared.</p>
            </div>
          `);
          return;
        }

        msg = GW.general.parseResponse(msg);

        GW.history.stopAllTimers();

          $(process_history_container_id).html(
              GW.history.getProcessHistoryTable(msg, pid, pname, process_history_table_id.replace('#', ''), process_history_container_id.replace('#', '')),
          );
          GW.history.startActiveTimer();

        var table_selector = `${process_history_container_id} ${process_history_table_id}`;
        GW.history.applyBootstrapTable(table_selector, pid, pname);

        GW.chart.renderProcessHistoryChart(msg, pname, process_history_container_id);

        $(close_history).click(function () {
          $(process_history_container_id).html("");
        });

        console.log("Scroll to the history section.");

        GW.process.switchTab(
          document.getElementById(history_tab_id),
          history_tab_target_id,
        );
      })
      .fail(function (jxr, status) {
        console.error(status);
      });
  },

  // refreshCodeEditor: function () {
  //   // console.log("Process Code Editor is refreshed..");

  //   if (GW.process.editor != null) GW.process.editor.refresh();
  //   if (GW.process.sidepanel.editor != null)
  //     GW.process.sidepanel.editor.refresh();
  // },
};
