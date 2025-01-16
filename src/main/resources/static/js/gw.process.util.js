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
        console.log("Editor container: " + editorContainerId);

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
    // Query the element
    const resizer = document.getElementById(resizer_line_id);
    const leftSide = resizer.previousElementSibling;
    const rightSide = resizer.nextElementSibling;
    resizer.style.cursor = "ew-resize";

    // The current position of mouse
    let x = 0;
    let y = 0;
    let leftWidth = 0;

    // Handle the mousedown event
    // that's triggered when user drags the resizer
    GW.process.mouseDownHandler = function (e) {
      // Get the current mouse position
      x = e.clientX;
      y = e.clientY;
      leftWidth = leftSide.getBoundingClientRect().width;

      // Remove the handlers of `mousemove` and `mouseup`
      document.removeEventListener(
        "mousemove",
        GW.process.mouseMoveVerticalHandler,
      );
      document.removeEventListener(
        "mouseup",
        GW.process.mouseUpVerticalHandler,
      );
      // Attach the listeners to `document`
      document.addEventListener("mousemove", GW.process.mouseMoveHandler);
      document.addEventListener("mouseup", GW.process.mouseUpHandler);
    };

    GW.process.mouseMoveHandler = function (e) {
      // How far the mouse has been moved
      const dx = e.clientX - x;
      const dy = e.clientY - y;

      const newLeftWidth =
        ((leftWidth + dx) * 100) /
        resizer.parentNode.getBoundingClientRect().width;
      leftSide.style.width = `${newLeftWidth}%`;

      resizer.style.cursor = "ew-resize";
      // document.body.style.cursor = 'col-resize';

      leftSide.style.userSelect = "none";
      leftSide.style.pointerEvents = "none";

      rightSide.style.userSelect = "none";
      rightSide.style.pointerEvents = "none";
    };

    GW.process.mouseUpHandler = function () {
      resizer.style.removeProperty("cursor");
      // document.body.style.removeProperty('cursor');

      leftSide.style.removeProperty("user-select");
      leftSide.style.removeProperty("pointer-events");

      rightSide.style.removeProperty("user-select");
      rightSide.style.removeProperty("pointer-events");

      // Remove the handlers of `mousemove` and `mouseup`
      document.removeEventListener("mousemove", GW.process.mouseMoveHandler);
      document.removeEventListener("mouseup", GW.process.mouseUpHandler);
    };

    // Attach the handler
    resizer.addEventListener("mousedown", GW.process.mouseDownHandler);
  },

  activateVerticalResizer: function (resizer_line_id) {
    console.log("vertical resizer is activated");

    // Query the element
    const resizer = document.getElementById(resizer_line_id);
    const topElement = resizer.previousElementSibling;
    const bottomElement = resizer.nextElementSibling;
    resizer.style.cursor = "ns-resize";

    // The current position of mouse
    let x = 0;
    let y = 0;
    let topHeight = 0;

    // Handle the mousedown event
    // that's triggered when user drags the resizer
    GW.process.mouseDownVerticalHandler = function (e) {
      // Get the current mouse position
      x = e.clientX;
      y = e.clientY;
      topHeight = topElement.getBoundingClientRect().height;

      // Remove the handlers of `mousemove` and `mouseup`
      document.removeEventListener("mousemove", GW.process.mouseMoveHandler);
      document.removeEventListener("mouseup", GW.process.mouseUpHandler);
      // Attach the listeners to `document`
      document.addEventListener(
        "mousemove",
        GW.process.mouseMoveVerticalHandler,
      );
      document.addEventListener("mouseup", GW.process.mouseUpVerticalHandler);
    };

    GW.process.mouseMoveVerticalHandler = function (e) {
      // How far the mouse has been moved
      const dx = e.clientX - x;
      const dy = e.clientY - y;

      const newtopHeight =
        ((topHeight + dy) * 100) /
        resizer.parentNode.getBoundingClientRect().height;
      topElement.style.height = `${newtopHeight}%`;
      bottomElement.style.height = `${100 - newtopHeight}%`;

      resizer.style.cursor = "ns-resize";

      topElement.style.userSelect = "none";
      topElement.style.pointerEvents = "none";

      bottomElement.style.userSelect = "none";
      bottomElement.style.pointerEvents = "none";
    };

    GW.process.mouseUpVerticalHandler = function () {
      resizer.style.removeProperty("cursor");

      topElement.style.removeProperty("user-select");
      topElement.style.removeProperty("pointer-events");

      bottomElement.style.removeProperty("user-select");
      bottomElement.style.removeProperty("pointer-events");

      // Remove the handlers of `mousemove` and `mouseup`
      document.removeEventListener(
        "mousemove",
        GW.process.mouseMoveVerticalHandler,
      );
      document.removeEventListener(
        "mouseup",
        GW.process.mouseUpVerticalHandler,
      );
    };

    // Attach the handler
    resizer.addEventListener("mousedown", GW.process.mouseDownVerticalHandler);
  },

  noDock: function (
    history_section_id,
    code_window_id,
    console_content_id,
    resize_line_id,
  ) {
    var codeContainer = document.getElementById(history_section_id);
    var resizerDrag = document.getElementById(resize_line_id);
    resizerDrag.style.setProperty("height", "0px");
    resizerDrag.style.setProperty("width", "100%");
    codeContainer.style.setProperty("display", "block");

    var element = document.getElementById(console_content_id);
    element.style.setProperty("width", "100%");
    element.style.setProperty("height", "0%");
    element.style.setProperty("display", "none");

    var element = document.getElementById(code_window_id);
    element.style.setProperty("width", "100%");
    element.style.setProperty("height", "100%");
    element.style.setProperty("display", "flex");

    // activating resizer functionality
    GW.process.util.activateVerticalResizer(resize_line_id);

    // GW.process.util.refreshCodeEditor();
  },

  bottomDock: function (
    history_section_id,
    code_window_id,
    console_content_id,
    resize_line_id,
  ) {
    var codeContainer = document.getElementById(history_section_id);
    var resizerDrag = document.getElementById(resize_line_id);
    resizerDrag.style.setProperty("height", "2px");
    resizerDrag.style.setProperty("width", "100%");
    codeContainer.style.setProperty("display", "flow");

    var element = document.getElementById(code_window_id);
    element.style.setProperty("width", "100%");
    element.style.setProperty("height", "60%");
    element.style.setProperty("display", "flex");

    var element = document.getElementById(console_content_id);
    element.style.setProperty("width", "100%");
    element.style.setProperty("height", "40%");
    element.style.setProperty("display", "flow");

    // activating resizer functionality
    GW.process.util.activateVerticalResizer(resize_line_id);
    $("#" + resize_line_id).css("display", "flow");
  },

  leftDock: function (
    history_section_id,
    code_window_id,
    console_content_id,
    resize_line_id,
  ) {
    GW.process.dockmode = "left";

    var codeContainer = document.getElementById(history_section_id);
    codeContainer.style.setProperty("display", "flex");

    var resizerDrag = document.getElementById(resize_line_id);
    resizerDrag.style.setProperty("height", "100%");
    resizerDrag.style.setProperty("width", "2px");

    var element = document.getElementById(code_window_id);
    element.style.setProperty("width", "60%");
    element.style.setProperty("height", "100%");

    var element = document.getElementById(console_content_id);
    element.style.setProperty("width", "40%");
    element.style.setProperty("height", "100%");
    element.style.setProperty("display", "flow");

    // activating resizer functionality
    GW.process.util.activateResizer(resize_line_id);
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
          alert("no history found");

          return;
        }

        msg = GW.general.parseResponse(msg);

        GW.history.stopAllTimers();

          $(process_history_container_id).html(
              GW.history.getProcessHistoryTable(msg),
          );
          GW.history.startActiveTimer();

        var table_selector = `${process_history_container_id} ${process_history_table_id}`;
        GW.history.applyBootstrapTable(table_selector);

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
