/**
 * Geoweaver Process
 */

GW.process = {
  editor: null,

  jupytercode: null,

  current_pid: null,

  last_executed_process_id: null,

  process_id: null,

  history_id: null,

  editOn: false, //false: disable is false, all fields are activated; true: all fields are deactivated.

  // Store width/height ratios to maintain layout consistency
  savedLeftWidthRatio: null, // Percentage width of left side (code editor) in left/right layout
  savedTopHeightRatio: null, // Percentage height of top side (code editor) in top/bottom layout

  jsFrame: new JSFrame({ parentElement: $("#jsframe-container")[0] }),

  env_frame: null,

  isSaved: true,

  envlist: {},

  cmid: null, //the id used to differentiate the dialogs

  replace_jupyter_jsframe: null,

  dockmode: "left",

  builtin_processes: [
    {
      operation: "ShowResultMap",
      params: [{ name: "FilePath", min_occurs: 1, max_occurs: 1 }],
    }, //multiple occurs are something for later

    {
      operation: "DownloadData",
      params: [{ name: "resultfile", min_occurs: 1, max_occurs: 1 }],
    },

    // {"operation":"AWS S3", "params":[{"name":"AWS Parameters", "min_occurs": 1, "max_occurs": 1}]} //not working on remote host, comment out until it is ready
  ],

  connection_cache: [{ p: "xxxx", h: "yyyyy" }],

  init: function () {},

  checkIfProcessPanelActive: function () {
    return document.getElementById("main-process-info").style.display == "flex";
  },

  clearCache: function () {
    this.connection_cache = [];
  },

  setCache: function (pid, hid) {
    var is = false;

    for (var i = 0; i < this.connection_cache.length; i++) {
      if (this.connection_cache[i].p == pid) {
        this.connection_cache[i].h = hid;

        is = true;

        break;
      }
    }

    if (!is) {
      this.connection_cache.push({ p: pid, h: hid });
    }
  },

  findCache: function (pid) {
    var h = null;

    for (var i = 0; i < this.connection_cache.length; i++) {
      if (this.connection_cache[i].p == pid) {
        h = this.connection_cache[i].h;

        break;
      }
    }

    return h;
  },

  precheck: function () {
    var valid = false;

    if ($("#processname-" + GW.process.cmid).val()) {
      //					&&this.editor.getValue()){

      valid = true;
    }

    return valid;
  },

  showShell: function (code, cmid) {
    require.config({ paths: { vs: "../js/Monaco-Editor/dev/vs" } });

    require(["vs/editor/editor.main"], function () {
      var editorContainerId = "codeeditor-" + cmid;
      var container = $("#codearea-" + cmid);
      container.empty(); // Clear previous instances if any
      container.append(
        '<div id="' + editorContainerId + '" style="height:200px;"></div>'
      );

      // Create a dropdown for theme selection
      var themeSelectorId = "theme-selector-" + cmid;
      container.prepend(
        '<select id="' +
          themeSelectorId +
          '" style="margin-bottom: 10px;">' +
          '<option value="vs-dark">Dark</option>' +
          '<option value="vs-light">Light</option>' +
          '<option value="hc-black">High Contrast</option>' +
          "</select>"
      );

      // Initialize the Monaco Editor
      var editor = monaco.editor.create(
        document.getElementById(editorContainerId),
        {
          value: code || "#!/bin/bash",
          language: "shell",
          theme: GW.settings.selected_monaco_theme, // Default theme
          lineNumbers: "on",
          roundedSelection: false,
          scrollBeyondLastLine: false,
          readOnly: false,
          fontSize: 10,
          automaticLayout: true,
          formatOnSave: true,
          formatOnPaste: true,
          folding: true,
          formatOnType: true,
          showFoldingControls: "always",
          wordWrap: "on",
          scrollBeyondLastLine: true,
          contextmenu: true, // Enable the context menu for additional clipboard actions
        }
      );
      GW.process.util.add_editor_actions(editor);

      GW.process.editor = editor;

      // Add event listener to update the theme dynamically
      $("#" + themeSelectorId).on("change", function () {
        var selectedTheme = $(this).val();
        monaco.editor.setTheme(selectedTheme);
      });

      GW.settings.syncMonacoStyles(GW.process.editor);
    });
  },

  showPython: function (code, cmid) {
    // Define the path to the Monaco Editor's package
    require.config({ paths: { vs: "../js/Monaco-Editor/dev/vs" } });

    // Load the main module of Monaco Editor to start its setup
    require(["vs/editor/editor.main"], function () {
      // Ensure the target container for the editor exists and is empty
      var editorContainerId = "codeeditor-" + cmid;
      var container = $("#codearea-" + cmid);
      container.empty(); // Clear previous instances if any
      container.append(
        '<div id="' + editorContainerId + '" style="height:200px;"></div>'
      );

      console.log(
        "What is the current theme?" + GW.settings.selected_monaco_theme
      );

      // Initialize the Monaco Editor with Python configuration
      var editor = monaco.editor.create(
        document.getElementById(editorContainerId),
        {
          value: code || "# Write your first Python code in Geoweaver",
          language: "python",
          theme: GW.settings.selected_monaco_theme,
          lineNumbers: "on",
          roundedSelection: false,
          scrollBeyondLastLine: false,
          readOnly: false,
          fontSize: 10,
          automaticLayout: true,
          formatOnSave: true,
          formatOnPaste: true,
          folding: true,
          formatOnType: true,
          showFoldingControls: "always",
          wordWrap: "on",
          scrollBeyondLastLine: true,
          contextmenu: true, // Enable the context menu for additional clipboard actions
        }
      );
      GW.process.util.add_editor_actions(editor);

      GW.process.editor = editor;
      GW.settings.syncMonacoStyles(GW.process.editor);
    });
  },

  showBuiltinProcess: function (code, cmid) {
    var cont =
      `     <label for="builtinprocess" class="col-sm-4 col-form-label control-label" style="font-size:12px;" >Select a process: </label>
			 <div class="col-sm-8"> <select class="form-control"  id="builtin_processes-` +
      cmid +
      `">`;

    for (var i = 0; i < GW.process.builtin_processes.length; i++) {
      cont +=
        '    		<option value="' +
        GW.process.builtin_processes[i].operation +
        '">' +
        GW.process.builtin_processes[i].operation +
        "</option>";
    }

    cont += "  		</select></div>";

    for (var i = 0; i < GW.process.builtin_processes[0].params.length; i++) {
      cont += '<div class="row paramrow">';
      cont +=
        '     <label for="parameter" class="col-sm-4 col-form-label control-label" style="font-size:12px;" >Parameter <u>' +
        GW.process.builtin_processes[0].params[i].name +
        "</u>: </label>" +
        '     <div class="col-sm-8"> 	<input class="form-control parameter" id="param_' +
        GW.process.builtin_processes[0].params[i].name +
        "-" +
        cmid +
        '"></input>';
      cont += "</div></div>";
    }

    $("#codearea-" + cmid).append(cont);

    $("#builtin_processes-" + cmid).on("change", function () {
      GW.process.refreshBuiltinParameterList(
        "builtin_processes-" + cmid,
        "codearea-" + cmid
      );
    });

    if (code != null) {
      code = GW.general.parseResponse(code);

      $("#builtin_processes-" + cmid).val(code.operation);

      for (var i = 0; i < code.params.length; i++) {
        $("#param_" + code.params[i].name + "-" + cmid).val(
          code.params[i].value
        );
      }
    }
  },

  getCode: function (cmid) {
    cmid = cmid != null ? "-" + cmid : "";

    console.log("Get code cmid:" + cmid);

    var code = null;

    if ($("#processcategory" + cmid).val() == "shell") {
      code = GW.process.editor.getValue();
    } else if ($("#processcategory" + cmid).val() == "builtin") {
      var params = [];

      $(".parameter").each(function () {
        var newparam = {
          name: $(this).attr("id").split("param_")[1].split(cmid)[0],

          value: $(this).val(),
        };

        params.push(newparam);
      });

      code = {
        operation: $("#builtin_processes").val(),

        params: params,
      };

      code = JSON.stringify(code);
    } else if ($("#processcategory" + cmid).val() == "python") {
      code = GW.process.editor.getValue();
      //				code = $("#codeeditor-" + cmid).val();
    }

    return code;
  },

  /**
   * Function to display the current and previous code history
   * @param history_id
   */
  showHistoryDifference: function (
    process_id,
    history_id,
    previous_history_id
  ) {
    // ajax call for the current history id details:
    $.ajax({
      url: "logs",

      method: "POST",

      data: "type=process&id=" + process_id,
    })
      .done(function (history_list_msg) {
        if (history_list_msg == "") {
          alert("Cannot find the process history in the database.");

          return;
        }

        history_list_msg = GW.general.parseResponse(history_list_msg);

        $.ajax({
          url: "log",

          method: "POST",

          data: "type=process&id=" + history_id,
        })
          .done(function (current_msg) {
            if (current_msg == "") {
              alert("Cannot find the process history in the database.");

              return;
            }

            current_msg = GW.general.parseResponse(current_msg);

            current_msg.code = current_msg.input;

            // current code for dialogue box
            console.log("current code: " + current_msg.code);

            // ajax call for the previous history id details
            $.ajax({
              url: "log",

              method: "POST",

              data: "type=process&id=" + previous_history_id,
            })
              .done(function (msg_prv) {
                if (msg_prv == "") {
                  alert("Cannot find the process history in the database.");

                  return;
                }

                msg_prv = GW.general.parseResponse(msg_prv);

                msg_prv.code = msg_prv.input;

                GW.process.diffDialog(current_msg, msg_prv, history_list_msg);
              })
              .fail(function (jxr, status) {
                console.error("Fail to get log.");
              });
          })
          .fail(function (jxr, status) {
            console.error("Fail to get log.");
          });
      })
      .fail(function (jxr, status) {
        console.error("Fail to get log.");
      });
  },

  populateDropdown: function (dropdownId, data) {
    var dropdown = $(dropdownId);
    dropdown.empty();
    data.forEach(function (item) {
      dropdown.append(
        $("<option></option>")
          .attr("value", item.history_id)
          .text(item.history_id)
      );
    });
  },

  /**
   * method to show the popup with current and previous history difference
   * @param current_code
   * @param previous_code
   */
  diffDialog: function (current_history, previous_history, history_id_list) {
    const current_code = current_history.code;
    const previous_code = current_history.code;
    const content = `
      <div class="modal-body">
        <div class="row">
          <div class="col col-md-3"><b>Current ID</b></div>
          <div class="col col-md-3">
              <select class="form-control" id="current_history_id">
                  <!-- Options will be added by jQuery -->
              </select>
          </div>
          <div class="col col-md-3"><b>Previous ID</b></div>
          <div class="col col-md-3">
              <select class="form-control" id="previous_history_id">
                  <!-- Options will be added by jQuery -->
              </select>
          </div>
          
        </div>
        <div class="row">
          <div class="col col-md-3"><b>BeginTime</b></div>
          <div class="col col-md-3">${current_history.begin_time}</div>
          <div class="col col-md-3"><b>BeginTime</b></div>
          <div class="col col-md-3">${previous_history.begin_time}</div>
        </div>
        <div class="row">
          <div class="col col-md-3"><b>EndTime</b></div>
          <div class="col col-md-3">${current_history.end_time}</div>
          <div class="col col-md-3"><b>EndTime</b></div>
          <div class="col col-md-3">${previous_history.end_time}</div>
        </div>
        <div class="row">
          <div class="col col-md-3"><b>Notes</b></div>
          <div class="col col-md-3">${current_history.notes}</div>
          <div class="col col-md-3"><b>Notes</b></div>
          <div class="col col-md-3">${previous_history.notes}</div>
        </div>
        <div class="row">
          <div class="col col-md-3"><b>Status</b></div>
          <div class="col col-md-3">${current_history.status}</div>
          <div class="col col-md-3"><b>Status</b></div>
          <div class="col col-md-3">${previous_history.status}</div>
        </div>
        <br/>
        <label>Code Comparison</label>
        <br/>
        <div id="process-difference-comparison-code-view" style="height:300px;width:100%;border:1px solid #ddd;"></div>
        <br/>
        <br/>
        <label>Result Comparison</label>
        <div id="process-difference-comparison-result-view" style="height:300px;width:100%;border:1px solid #ddd;"></div>
      </div>`;

    const dialog = GW.process.createJSFrameDialog(
      720,
      640,
      content,
      "History Details"
    );

    // Delay the initialization to ensure the modal and the DOM elements are fully loaded
    setTimeout(() => {
      if (window.monaco) {
        GW.process.initDiffEditors(current_history, previous_history);
      } else {
        console.error("Monaco editor is not loaded or initialized!");
      }
    }, 100); // Slightly increased the delay to ensure DOM is ready

    // Add disposal logic to the dialog's close button
    dialog.on("closeButton", "click", function (frame) {
      GW.process.disposeModels();
      frame.closeFrame();
    });

    dialog.control.doMaximize();

    this.populateDropdown("#current_history_id", history_id_list);
    this.populateDropdown("#previous_history_id", history_id_list);

    // Select an option programmatically (example: select the first item)
    $("#current_history_id").val(current_history.hid);
    $("#previous_history_id").val(previous_history.hid);

    $("#current_history_id").change(function () {
      const current_history_id = $("#current_history_id").val();
      dialog.closeFrame();
      GW.process.showHistoryDifference(
        current_history.id,
        current_history_id,
        previous_history.hid
      );
    });

    $("#previous_history_id").change(function () {
      const previous_history_id = $("#previous_history_id").val();
      dialog.closeFrame();
      GW.process.showHistoryDifference(
        current_history.id,
        current_history.hid,
        previous_history_id
      );
    });
  },

  // Function to dispose of models and editors
  disposeModels: function () {
    console.log("Disposing existing models if any...");
    if (GW.process.originalCodeModel) {
      GW.process.originalCodeModel.dispose();
      GW.process.originalCodeModel = null;
    }
    if (GW.process.modifiedCodeModel) {
      GW.process.modifiedCodeModel.dispose();
      GW.process.modifiedCodeModel = null;
    }
    if (GW.process.originalResultModel) {
      GW.process.originalResultModel.dispose();
      GW.process.originalResultModel = null;
    }
    if (GW.process.modifiedResultModel) {
      GW.process.modifiedResultModel.dispose();
      GW.process.modifiedResultModel = null;
    }
    if (GW.process.codeDiffEditor) {
      GW.process.codeDiffEditor.dispose();
      GW.process.codeDiffEditor = null;
    }
    if (GW.process.resultDiffEditor) {
      GW.process.resultDiffEditor.dispose();
      GW.process.resultDiffEditor = null;
    }
  },

  // Initialize the diff editors
  initDiffEditors: function (current_history, previous_history) {
    const codeEditorContainer = document.getElementById(
      "process-difference-comparison-code-view"
    );
    const resultEditorContainer = document.getElementById(
      "process-difference-comparison-result-view"
    );

    const currentLanguage = current_history.language || "plaintext";
    const previousLanguage = previous_history.language || "plaintext";

    console.log(
      `Initializing diff editor for languages: current=${currentLanguage}, previous=${previousLanguage}`
    );

    // disposeModels(); // Ensure previous models are disposed of

    const codeDiffEditor = monaco.editor.createDiffEditor(codeEditorContainer, {
      theme: GW.settings.selected_monaco_theme,
      readOnly: true,
      automaticLayout: true,
    });

    GW.process.originalCodeModel = monaco.editor.createModel(
      previous_history.code || "",
      previousLanguage
    );
    GW.process.modifiedCodeModel = monaco.editor.createModel(
      current_history.code || "",
      currentLanguage
    );

    codeDiffEditor.setModel({
      original: GW.process.originalCodeModel,
      modified: GW.process.modifiedCodeModel,
    });

    const resultDiffEditor = monaco.editor.createDiffEditor(
      resultEditorContainer,
      {
        theme: GW.settings.selected_monaco_theme,
        readOnly: true,
        automaticLayout: true,
      }
    );

    GW.process.originalResultModel = monaco.editor.createModel(
      previous_history.output || "",
      "plaintext"
    );
    GW.process.modifiedResultModel = monaco.editor.createModel(
      current_history.output || "",
      "plaintext"
    );

    resultDiffEditor.setModel({
      original: GW.process.originalResultModel,
      modified: GW.process.modifiedResultModel,
    });

    GW.process.codeDiffEditor = codeDiffEditor;
    GW.process.resultDiffEditor = resultDiffEditor;
  },

  newDialog: function (category) {
    var content =
      '<div class="modal-body">' +
      GW.process.getProcessDialogTemplate() +
      "</div>";

    content +=
      '<div class="modal-footer">' +
      '<button type="button" id="add-process-' +
      GW.process.cmid +
      '" class="btn btn-outline-primary">Add</button> ' +
      '<button type="button" id="run-process-' +
      GW.process.cmid +
      '" class="btn btn-outline-secondary">Run</button>' +
      '<button type="button" id="cancel-process-' +
      GW.process.cmid +
      '" class="btn btn-outline-secondary">Cancel</button>' +
      "</div>";

    var frame = GW.process.createJSFrameDialog(
      720,
      640,
      content,
      "Add new process"
    );

    GW.process.showShell(null, GW.process.cmid);

    $("#processcategory-" + GW.process.cmid).on("change", function () {
      console.log(this.id);

      $("#codearea-" + GW.process.cmid).empty();

      if (this.value == "shell") {
        GW.process.showShell(null, GW.process.cmid);
      } else if (this.value == "builtin") {
        GW.process.showBuiltinProcess(null, GW.process.cmid);
      } else if (this.value == "python") {
        GW.process.showPython(null, GW.process.cmid);
      }
    });

    $("#add-process-" + GW.process.cmid).click(function () {
      if (GW.process.add(false, GW.process.cmid)) frame.closeFrame();
      // cmid = Math.floor(Math.random() * 1000);
    });

    $("#run-process-" + GW.process.cmid).click(function () {
      if (GW.process.add(true, GW.process.cmid)) frame.closeFrame();
    });

    $("#cancel-process-" + GW.process.cmid).click(function () {
      frame.closeFrame();
    });

    //change the category if it is not null
    if (category)
      $("#processcategory-" + GW.process.cmid)
        .val(category)
        .trigger("change");
  },

  recent: function (num, outside) {
    $.ajax({
      url: "recent",

      method: "POST",

      data: "type=process&number=" + num,
    })
      .done(function (msg) {
        if (!msg.length) {
          alert("no history found");

          return;
        }

        msg = GW.general.parseResponse(msg);

        var content =
          '<div class="modal-body" style="font-size: 12px;"><table class="table"> ' +
          "  <thead> " +
          "    <tr> " +
          '      <th scope="col">Process</th> ' +
          '      <th scope="col">Begin Time</th> ' +
          '      <th scope="col">End Time</th> ' +
          '      <th scope="col">Status</th> ' +
          '      <th scope="col">Action</th> ' +
          "    </tr> " +
          "  </thead> " +
          "  <tbody> ";

        for (var i = 0; i < msg.length; i++) {
          var status_col = GW.history.getProcessStatusCol(
            msg[i].id,
            msg[i].status
          );

          var detailbtn = null;
          // var viewChanges = null;

          if (outside) {
            detailbtn =
              "      <td><a href=\"javascript: GW.process.showHistoryDetails('" +
              msg[i].id +
              "')\">Details</a></td> ";
            // viewChanges = "      <td><a href=\"javascript: GW.process.showHistoryDetails('"+msg[i].id+"')\">View Changes</a></td> ";
          } else {
            detailbtn =
              "      <td><a href=\"javascript: GW.process.getHistoryDetails('" +
              msg[i].id +
              "')\">Details</a></td> ";
            // viewChanges = "      <td><a href=\"javascript: GW.process.showHistoryDetails('"+msg[i].id+"')\">View Changes</a></td> ";
          }

          //					detailbtn = "      <td><a href=\"javascript: void(0))\">Details</a></td> ";

          content +=
            "    <tr> " +
            "      <td>" +
            msg[i].name +
            "</td> " +
            "      <td>" +
            msg[i].begin_time +
            "</td> " +
            "      <td>" +
            msg[i].end_time +
            "</td> " +
            status_col +
            detailbtn +
            // viewChanges +
            "    </tr>";
        }

        content += "</tbody></div>";

        var frame = GW.process.createJSFrameDialog(
          720,
          480,
          content,
          "History of " + msg.name
        );
      })
      .fail(function (jxr, status) {
        console.error(status);
      });
  },

  /**
   * list all the history execution of the process
   */
  history: function (pid, pname) {
    GW.process.util.history(
      pid,
      pname,
      "#process-history-container",
      "#process_history_table",
      "#closeHistory",
      "main-process-info-history-tab",
      "main-process-info-history"
    );
  },

  stop: function (history_id) {
    console.log("Send stop request to stop the running process");

    $.ajax({
      url: "stop",

      method: "POST",

      data: "type=process&id=" + history_id,
    }).done(function (msg) {
      msg = GW.general.parseResponse(msg);

      console.log("stop process is called");

      if (msg.ret == "stopped") {
        $("#stopbtn_" + history_id).html(
          '<span class="text-success">Stopped</span>'
        );

        $("#stopbtn_" + history_id)
          .prop("onclick", null)
          .off("click");

        //					<span id=\"status_"+msg[i].id+"\" class=\"label label-warning\">Pending</span>

        $("#status_" + history_id).html(
          '<span class="label label-default">Stopped</span>'
        );

        GW.history.stopOneTimer(history_id);
      } else {
        alert("Fail to stop.");
      }
    });
  },

  /**
   * This function is called after people click on "Details" in the process history table
   * @param {*} history_id
   */
  showHistoryDetails: function (history_id) {
    GW.process.history_id = history_id;

    $.ajax({
      url: "log",

      method: "POST",

      data: "type=process&id=" + history_id,
    })
      .done(function (msg) {
        if (msg == "") {
          alert("Cannot find the process history in the database.");

          return;
        }

        msg = GW.general.parseResponse(msg);

        msg.code = msg.input;

        GW.process.display(msg);

        GW.process.displayOutput(msg);

        GW.process.switchTab(
          document.getElementById("main-process-info-code-tab"),
          "main-process-info-code"
        );

        if (GW.editor.isfullscreen) GW.editor.switchFullScreen();
      })
      .fail(function (jxr, status) {
        console.error("Fail to get log.");
      });
  },

  displayOutput: function (msg) {
    // make sure the current history id is updated
    GW.process.history_id = msg.hid;

    var output = GW.general.escapeCodeforHTML(msg.output);

    if (msg.output == "logfile") {
      output =
        '<div class="spinner-border" role="status"> ' +
        '	  <span class="sr-only">Loading...</span> ' +
        "	</div>";
    }

    console.log("Update the code with the old version");

    if (GW.process.editor) {
      GW.process.editor.setValue(GW.process.unescape(msg.input));

      // GW.process.util.refreshCodeEditor();
    }

    output =
      "<p> Execution started at " +
      msg.begin_time +
      "</p>" +
      "<p> Execution ended at " +
      msg.end_time +
      "</p>" +
      "<p> The old code used has been refreshed in the code editor.</p>" +
      "<div>" +
      output +
      "</div>";

    $("#process-log-window").html(output);

    $("#closeLog").click(function () {
      $("#console-output").html("");
    });

    $("#retrieve-result").click(function () {
      GW.result.showDialog(history_id);
    });

    if (msg.output == "logfile") {
      $.get("../temp/" + msg.id + ".log")
        .success(function (data) {
          if (data != null) $("#log-output").text(data);
          else $("#log-output").text("missing log");
        })
        .error(function () {
          $("#log-output").text("missing log");
        });
    }

    GW.process.switchTab(
      document.getElementById("main-process-info-code-tab"),
      "main-process-info-code"
    );
  },

  /**
   * This function is called after people click on "Delete" in the process history table
   * @param {*} history_id
   */
  deleteHistory: function (history_id) {
    GW.process.history_id = history_id;
    if (!confirm("Are you sure you want to delete this history?")) {
      return;
    }
    $.ajax({
      url: "deleteHistoryById",
      method: "POST",
      data: "type=process&id=" + history_id,
    })
      .done(function (msg) {
        if (msg == "") {
          alert("Cannot find the process history in the database.");
          return;
        }
        console.log("History " + history_id + " is deleted successfully.");
        var row = $('tr[id="history-row-' + history_id + '"]');
        var table = $("#process_history_table").DataTable();
        table.row(row).remove().draw(); // remove the row from the table completely without refresh
      })
      .fail(function (jxr, status) {
        console.error("Fail to Delete History.");
      });
  },

  getHistoryDetails: function (history_id) {
    $.ajax({
      url: "log",

      method: "POST",

      data: "type=process&id=" + history_id,
    })
      .done(function (msg) {
        if (msg == "") {
          alert("Cannot find the process history in the database.");

          return;
        }

        msg = GW.general.parseResponse(msg);

        GW.process.display(msg);

        GW.process.displayOutput(msg);
      })
      .fail(function () {});
  },

  unescape: function (code) {
    if (code != null) {
      // code = code.replaceAll("<br/>", "\n"); //no long needed after using StringEscapeUtils, should remove in v1.0
      var code = code
        .replace(/\\n/g, "\\n")
        .replace(/\\'/g, "\\'")
        .replace(/\\"/g, '\\"')
        .replace(/\\&/g, "\\&")
        .replace(/\\r/g, "\\r")
        .replace(/\\t/g, "\\t")
        .replace(/\\b/g, "\\b")
        .replace(/\\f/g, "\\f");
    }

    return code;
  },

  restoreBackspace: function (event) {
    event.stopPropagation();
  },

  getRandomId: function () {
    return Math.floor(Math.random() * 1000);
  },

  getProcessDialogTemplate: function () {
    GW.process.cmid = Math.floor(Math.random() * 1000);

    var confidential_field =
      '     <label  id="label_conf" for="confidential" style="font-size: 12px;" class="col-sm-2 col-form-label control-label">Confidential</label>' +
      '     <div class="col-sm-4">' +
      '       <input type="radio" name="confidential-' +
      GW.process.cmid +
      '" value="FALSE" checked> ' +
      '		<label  id="public_radio" for="confidential-' +
      GW.process.cmid +
      '">Public</label>';

    if (GW.user.current_userid != null && GW.user.current_userid != "111111")
      confidential_field +=
        '       <input type="radio" name="confidential-' +
        GW.process.cmid +
        '" value="TRUE"> ' +
        '		<label  id="public_radio" for="confidential-' +
        GW.process.cmid +
        '">Private</label>';
    //		       '			<input type="text" class="form-control form-control-sm" ></input>'+
    confidential_field += "     </div>";

    var content =
      "<div><form>" +
      '   <div class="form-group row required">' +
      '     <label for="processcategory" style="font-size: 12px;" class="col-sm-2 col-form-label control-label">Language</label>' +
      '     <div class="col-sm-4">' +
      '			<select class="form-control form-control-sm" id="processcategory-' +
      GW.process.cmid +
      '">' +
      '    			<option value="shell">Shell</option>' +
      '    			<option value="builtin">Built-In Process</option>' +
      '    			<option value="python">Python</option>' +
      /*'    		<option value="python">Python</option>'+
		   '    			<option value="r">R</option>'+
		   '    			<option value="matlab">Matlab</option>'+*/
      "  		</select>" +
      "     </div>" +
      //		       '   </div>'+
      //		       '   <div class="form-group row required">'+
      '     <label for="processname" style="font-size: 12px;" class="col-sm-2 col-form-label control-label">Name</label>' +
      '     <div class="col-sm-4">' +
      '			<input type="text" class="form-control form-control-sm" id="processname-' +
      GW.process.cmid +
      '"></input>' +
      //		       '			<input type="text" class="form-control form-control-sm" ></input>'+
      "     </div>" +
      confidential_field +
      "   </div>" +
      '   <div class="form-group row required new-process-code-area" id="codearea-' +
      GW.process.cmid +
      '"></div>' +
      '   <p class="h6"> <span class="badge badge-secondary">Ctrl+S</span> to save edits. Click <i class="fa fa-edit subalignicon process-edit-icon" onclick="GW.process.editSwitch()" data-toggle="tooltip" title="Enable Edit"></i> to apply edits. </p>' +
      " </form></div>";

    return content;
  },

  createJSFrameDialog: function (width, height, content, title) {
    var frame = GW.process.jsFrame.create({
      title: title,
      left: 0,
      top: 0,
      width: width,
      height: height,
      appearanceName: "yosemite",
      style: {
        backgroundColor: "rgb(255,255,255)",
        fontSize: 12,
        overflow: "auto",
        zIndex: "20000", // Set very high z-index to ensure dialog appears above all fullscreen containers
      },
      html: '<div style="font-size:12px; padding: 1px;">' + content + "</div>",
    });

    frame.setControl({
      styleDisplay: "inline",
      maximizeButton: "zoomButton",
      demaximizeButton: "dezoomButton",
      minimizeButton: "minimizeButton",
      deminimizeButton: "deminimizeButton",
      hideButton: "closeButton",
      animation: true,
      animationDuration: 150,
    });

    frame.on("closeButton", "click", (_frame, evt) => {
      _frame.closeFrame();
      GW.workspace.if_any_frame_on = false;
    });

    //Show the window
    frame.show();

    frame.setPosition(
      (window.innerWidth - width) / 2,
      (window.innerHeight - height) / 2,
      "LEFT_TOP"
    );

    // Function to set z-index on frame and container ONLY
    // Don't interfere with jsFrame's internal positioning
    var setDialogZIndex = function() {
      try {
        var dialogZIndex = "20000";
        
        // Ensure jsframe-container has high z-index and relative positioning
        var jsframeContainer = document.getElementById("jsframe-container");
        if (jsframeContainer) {
          jsframeContainer.style.setProperty("z-index", dialogZIndex, "important");
          jsframeContainer.style.setProperty("position", "relative", "important");
        }
        
        // Try to get the main frame element and set its z-index only
        // Don't change its position - let jsFrame manage that
        try {
          var frameElement = frame.getFrameElement();
          if (frameElement) {
            // Only set z-index, never touch position
            frameElement.style.setProperty("z-index", dialogZIndex, "important");
          }
        } catch (e) {
          // Ignore if getFrameElement fails
        }
      } catch (e) {
        console.log("Could not set z-index for frame:", e);
      }
    };
    
    // Wait a bit for jsFrame to complete its layout before setting z-index
    // This ensures we don't interfere with jsFrame's positioning logic
    setTimeout(setDialogZIndex, 100);
    setTimeout(setDialogZIndex, 300);
    setTimeout(setDialogZIndex, 500);

    GW.workspace.if_any_frame_on = true;

    return frame;
  },

  edit: function (pid) {
    this.current_pid = pid;

    $.ajax({
      url: "detail",

      method: "POST",

      data: "type=process&id=" + pid,
    })
      .done(function (msg) {
        msg = GW.general.parseResponse(msg);
        var content =
          '<div class="modal-body">' +
          GW.process.getProcessDialogTemplate() +
          "</div>";

        content +=
          '<div class="modal-footer">' +
          '	<button type="button" id="edit-save-process-' +
          GW.process.cmid +
          '" class="btn btn-outline-primary">Save</button> ' +
          '	<button type="button" id="edit-run-process-' +
          GW.process.cmid +
          '" class="btn btn-outline-secondary">Run</button>' +
          "</div>";

        var frame = GW.process.createJSFrameDialog(
          720,
          640,
          content,
          "Edit Process"
        );

        var old_name = msg.name;

        var old_lang = msg.lang == null ? msg.desc : msg.lang;

        var old_code = msg.code;

        $("#processcategory-" + GW.process.cmid).val(old_lang);

        $("#processname-" + GW.process.cmid).val(msg.name);

        $("#codearea-" + GW.process.cmid).empty();

        if (old_lang == "shell") {
          GW.process.showShell(old_code, GW.process.cmid);
        } else if (old_lang == "builtin") {
          GW.process.showBuiltinProcess(old_code, GW.process.cmid);
        } else if (old_lang == "python") {
          GW.process.showPython(old_code, GW.process.cmid);
        }

        $("#processcategory-" + GW.process.cmid).on("change", function () {
          $("#codearea-" + GW.process.cmid).empty();

          var old_code_new = null;

          if (this.value == old_lang) {
            old_code_new = old_code;
          }

          if (this.value == "shell") {
            GW.process.showShell(old_code_new, GW.process.cmid);
          } else if (this.value == "builtin") {
            GW.process.showBuiltinProcess(old_code_new, GW.process.cmid);
          } else if (this.value == "python") {
            GW.process.showPython(old_code_new, GW.process.cmid);
          }
        });

        $("#edit-save-process-" + GW.process.cmid).click(function () {
          GW.process.update(msg.id, GW.process.cmid);
        });

        $("#edit-run-process-" + GW.process.cmid).click(function () {
          //not finished yet
          GW.process.runProcess(msg.id, msg.name, msg.lang);
        });
      })
      .fail(function (jxr, status) {
        alert("Fail to get process details");
      });
  },

  display: function (msg) {
    GW.process.sidepanel.close(); //close the side panel when the normal window shows up

    GW.process.editOn = false;

    var code = null;

    var code_type = null;

    var process_id = null;

    var process_name = null;

    GW.workspace.currentmode = 1;

    GW.ssh.process_output_id = "process-log-window";

    GW.process.history_id = null;

    msg = GW.general.parseResponse(msg);

    code_type = msg.lang == null ? msg.description : msg.lang;

    // Store code type for tab title updates
    GW.process.current_code_type = code_type;

    code = msg.code;

    if (code != null && code.includes('\\"')) {
      code = GW.process.unescape(code);
    }

    process_id = msg.id;

    GW.process.process_id = msg.id;

    process_name = msg.name;
    
    // Store process info for tab title updates
    GW.process.current_process_name = process_name;
    GW.process.current_code_type = code_type;

    owner = msg.owner;

    // GW.process.cmid = Math.floor(Math.random() * 1000);

    var confidential_field =
      '     <div class="col-sm-1 col-form-label control-label">Confidential </div>' +
      '     <div class="col-sm-2" style="padding-left:30px;">';

    if (msg.confidential == "FALSE") {
      confidential_field +=
        '       <input type="radio" name="confidential_process" value="FALSE" checked> ';
    } else {
      confidential_field +=
        '       <input type="radio" name="confidential_process" value="FALSE"> ';
    }

    confidential_field +=
      '		<label  id="public_label" for="confidential">Public</label>';

    if (GW.user.current_userid == owner && GW.user.current_userid != "111111") {
      if (msg.confidential == "TRUE") {
        confidential_field +=
          '       <input type="radio"  name="confidential_process" value="TRUE" checked> <label id="private_radio" for="confidential">Private</label>';
      } else {
        confidential_field +=
          '       <input type="radio" name="confidential_process" value="TRUE" checked> <label id="private_radio"  for="confidential">Private</label>';
      }
    }

    confidential_field += "     </div>";

    var content = '<div id="process-main-container" style="width: 100%; height: 100%; display: flex; flex-direction: column; margin: 0; padding: 0;">';
    
    // Collapsible Process Info Bar - hidden by default
    content += `
      <!-- Collapsible Process Info Bar - hidden by default -->
      <div id="process-info-bar" style="background-color: #fff; border-bottom: 1px solid #e0e0e0; padding: 0; margin: 0; flex-shrink: 0; flex-basis: 0; overflow: hidden; max-height: 0; display: none; visibility: hidden; height: 0; min-height: 0; transition: max-height 0.3s ease-out;">
        <div style="padding: 12px 20px;">
          <div class="row" style="margin: 0; align-items: center;">
            <div class="col-md-2" style="padding: 0 8px;">
              <label class="form-label" style="font-weight: 600; margin: 0 0 4px 0; font-size: 11px; color: #6c757d; display: block;">Category</label>
              <select class="form-control form-control-sm" id="processcategory" disabled style="padding: 2px 8px; font-size: 12px; height: 28px;">
                <option value="shell">Shell</option>
                <option value="builtin">Built-In</option>
                <option value="python">Python</option>
              </select>
            </div>
            <div class="col-md-3" style="padding: 0 8px;">
              <label class="form-label" style="font-weight: 600; margin: 0 0 4px 0; font-size: 11px; color: #6c757d; display: block;">Name</label>
              <input type="text" class="form-control form-control-sm" id="processname" style="padding: 2px 8px; font-size: 12px; height: 28px;">
            </div>
            <div class="col-md-2" style="padding: 0 8px;">
              <label class="form-label" style="font-weight: 600; margin: 0 0 4px 0; font-size: 11px; color: #6c757d; display: block;">ID</label>
              <input type="text" class="form-control form-control-sm" id="processid" disabled style="padding: 2px 8px; font-size: 12px; height: 28px;">
            </div>
            <div class="col-md-4" style="padding: 0 8px;">
              <label class="form-label" style="font-weight: 600; margin: 0 0 4px 0; font-size: 11px; color: #6c757d; display: block;">Confidential</label>
              <div style="padding-top: 4px;">
                ${msg.confidential == "FALSE" ? 
                  '<div class="form-check form-check-inline" style="margin: 0;"><input class="form-check-input" type="radio" name="confidential_process" value="FALSE" checked id="public_radio_process" style="margin: 0;"><label class="form-check-label" for="public_radio_process" style="font-size: 12px; margin: 0 0 0 4px;">Public</label></div>' + 
                  (GW.user.current_userid == owner && GW.user.current_userid != "111111" ? 
                    '<div class="form-check form-check-inline" style="margin: 0 0 0 8px;"><input class="form-check-input" type="radio" name="confidential_process" value="TRUE" id="private_radio_process" style="margin: 0;"><label class="form-check-label" for="private_radio_process" style="font-size: 12px; margin: 0 0 0 4px;">Private</label></div>' : '') :
                  '<div class="form-check form-check-inline" style="margin: 0;"><input class="form-check-input" type="radio" name="confidential_process" value="FALSE" id="public_radio_process" style="margin: 0;"><label class="form-check-label" for="public_radio_process" style="font-size: 12px; margin: 0 0 0 4px;">Public</label></div>' + 
                  (GW.user.current_userid == owner && GW.user.current_userid != "111111" ? 
                    '<div class="form-check form-check-inline" style="margin: 0 0 0 8px;"><input class="form-check-input" type="radio" name="confidential_process" value="TRUE" checked id="private_radio_process" style="margin: 0;"><label class="form-check-label" for="private_radio_process" style="font-size: 12px; margin: 0 0 0 4px;">Private</label></div>' : '')
                }
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tabs and Content Parent Container -->
      <div id="process-tabs-content-wrapper" style="flex: 1; display: flex; flex-direction: column; min-height: 0; overflow: hidden;">
        <!-- Compact Tabs Navigation with Integrated Toolbar -->
        <div id="process-tabs-container" style="background-color: #fff; border-bottom: 2px solid #e0e0e0; padding: 0 20px; flex-shrink: 0; height: 38px; display: flex; align-items: center; justify-content: space-between;">
          <!-- Left: Tabs -->
          <ul class="nav nav-tabs process-tabs" role="tablist" style="margin: 0; border-bottom: none; background-color: transparent; height: 100%; flex: 1;">
            <li class="nav-item" role="presentation" style="height: 100%;">
              <button class="nav-link active" id="main-process-info-code-tab" onclick="GW.process.openCity(event, 'main-process-info-code')" 
                      style="border: none; border-bottom: 3px solid #f5576c; color: #f5576c; font-weight: 500; padding: 6px 14px; margin-right: 4px; transition: all 0.3s ease; font-size: 12px; height: 100%; display: flex; align-items: center;">
                ${GW.process.util.get_icon_by_process_type(code_type)} ${process_name || 'Process'} - Code & Log
              </button>
            </li>
            <li class="nav-item" role="presentation" style="height: 100%;">
              <button class="nav-link" id="main-process-info-history-tab" onclick="GW.process.openCity(event, 'main-process-info-history'); GW.process.history('${process_id}', '${process_name}')" 
                      style="border: none; color: #6c757d; font-weight: 500; padding: 6px 14px; margin-right: 4px; transition: all 0.3s ease; font-size: 12px; height: 100%; display: flex; align-items: center;">
                <i class="fas fa-history"></i> History
              </button>
            </li>
          </ul>
          
          <!-- Right: Integrated Toolbar -->
          <div class="d-flex align-items-center" style="gap: 6px; flex-shrink: 0; flex-wrap: nowrap; margin-left: 15px;">
            <!-- Primary Actions -->
            <button class="btn btn-sm" onclick="GW.process.runProcess('${process_id}', '${process_name}', '${code_type}');" title="Run Process" style="padding: 4px 10px; font-size: 11px; font-weight: 500; border-radius: 3px; background-color: #f5576c; border: none; color: #fff; display: inline-flex; align-items: center;">
              <i class="fas fa-play"></i> <span style="margin-left: 4px;">Run</span>
				</button>
            <button class="btn btn-sm process-edit-right-icon" onclick="GW.process.editSwitch()" title="Edit Process" style="padding: 4px 10px; font-size: 11px; font-weight: 500; border-radius: 3px; background-color: #6c757d; border: none; color: #fff; display: inline-flex; align-items: center;">
              <i class="fas fa-edit"></i> <span style="margin-left: 4px;">Edit</span>
				</button>
            
            <!-- Layout Controls -->
            <div style="width: 1px; height: 20px; background-color: #e0e0e0; margin: 0 4px; display: inline-block; flex-shrink: 0; align-self: center;"></div>
            <button class="btn btn-sm" id="bottom-dock-btn" onclick="GW.process.bottomDock()" title="Dock Log to Bottom" style="padding: 4px 8px; font-size: 11px; background-color: #f8f9fa; border: 1px solid #dee2e6; color: #495057; border-radius: 3px; display: inline-flex; align-items: center; justify-content: center;">
              <i class="fas fa-grip-lines"></i>
            <button class="btn btn-sm" id="left-dock-btn" onclick="GW.process.leftDock()" title="Dock Log to Right" style="padding: 4px 8px; font-size: 11px; background-color: #f8f9fa; border: 1px solid #dee2e6; color: #495057; border-radius: 3px; display: inline-flex; align-items: center; justify-content: center;">
            <i class="fas fa-grip-lines-vertical"></i>
				</button>
              
				</button>
            <button class="btn btn-sm" id="no-dock-btn" onclick="GW.process.noDock()" title="Restore Normal Layout" style="padding: 4px 8px; font-size: 11px; background-color: #f8f9fa; border: 1px solid #dee2e6; color: #495057; border-radius: 3px; display: inline-flex; align-items: center; justify-content: center;">
              <i class="fas fa-th"></i>
				</button> 
            
            <!-- Fullscreen Control -->
            <div style="width: 1px; height: 20px; background-color: #e0e0e0; margin: 0 4px; display: inline-block; flex-shrink: 0; align-self: center;"></div>
            <button class="btn btn-sm" id="maximize-btn" onclick="GW.process.maximize()" title="Fullscreen" style="padding: 4px 8px; font-size: 11px; background-color: #f8f9fa; border: 1px solid #dee2e6; color: #495057; border-radius: 3px; display: inline-flex; align-items: center; justify-content: center;">
              <i class="fas fa-expand"></i>
            </button>
            
            <!-- Log Actions -->
            <div style="width: 1px; height: 20px; background-color: #e0e0e0; margin: 0 4px; display: inline-block; flex-shrink: 0; align-self: center;"></div>
            <div class="form-check form-check-inline" style="margin: 0; display: inline-flex; align-items: center; flex-shrink: 0;">
              <input class="form-check-input" type="checkbox" checked id="log_switch" style="margin: 0;">
              <label class="form-check-label" for="log_switch" style="margin: 0 0 0 4px; font-size: 11px; color: #495057; font-weight: 500;">Log</label>
			</div>
            
            <!-- Utility Actions -->
            <div style="width: 1px; height: 20px; background-color: #e0e0e0; margin: 0 4px; display: inline-block; flex-shrink: 0; align-self: center;"></div>
            <button class="btn btn-sm" id="toggle-details-btn" onclick="GW.process.toggleDetails()" title="Show/Hide Details" style="padding: 4px 10px; font-size: 11px; background-color: #f8f9fa; border: 1px solid #dee2e6; color: #495057; border-radius: 3px; display: inline-flex; align-items: center;">
              <i class="fas fa-chevron-down"></i> <span style="margin-left: 4px;">Details</span>
            </button>
            <button class="btn btn-sm" onclick="GW.menu.del('${process_id}', 'process')" title="Delete Process" style="padding: 4px 8px; font-size: 11px; background-color: #dc3545; border: none; color: #fff; border-radius: 3px; display: inline-flex; align-items: center; justify-content: center;">
              <i class="fas fa-trash"></i>
            </button>
            <button class="btn btn-sm" onclick="GW.process.downloadProcess('${process_id}')" title="Download Process" style="padding: 4px 8px; font-size: 11px; background-color: #f8f9fa; border: 1px solid #dee2e6; color: #495057; border-radius: 3px; display: inline-flex; align-items: center; justify-content: center;">
              <i class="fas fa-download"></i>
            </button>
          </div>
        </div>

        <!-- Tabs Content Container - Maximized -->
        <div id="editor-history-tab-panel" style="flex: 1; width: 100%; margin: 0; padding: 0; background-color: var(--monaco-background-color); overflow: hidden; min-height: 0; position: relative; flex-shrink: 0;">
        <div id="main-process-info-code" class="tabcontent-process" style="height: 100%; left: 0; margin: 0; padding: 0; display: block; overflow: hidden;">
          <div class="code__container" style="font-size: 12px; margin: 0; height: 100%; display: flex;" id="process-code-history-section">
            <div id="process_code_window" class="container__left" style="height: 100%; padding: 0; scrollbar-color: var(--monaco-scrollbar-color); flex: 1; min-width: 0;">
              <div class="col col-md-6" id="code-embed" style="width: 100%; height: 100%; padding: 0px; margin: 0px;"></div>
							</div> 
							<div class="resizer" id="dragMe"></div>
            <div id="single-console-content" class="container__right" style="height: 100%; width: 100%; max-width: 100%; overflow-y: auto; overflow-x: hidden; scrollbar-color: var(--monaco-scrollbar-color); background-color: var(--monaco-background-color); color: var(--monaco-foreground-color); box-sizing: border-box;">
              <div style="padding: 8px 12px; border-bottom: 1px solid #e0e0e0; background: #f8f9fa; width: 100%; max-width: 100%; box-sizing: border-box; overflow: hidden;">
                <h5 style="margin: 0; font-size: 13px; font-weight: 600; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                  <i class="fas fa-terminal"></i> Logging
                </h5>
              </div>
              <div id="process-log-window" style="width: 100%; max-width: 100%; overflow-wrap: break-word; word-wrap: break-word; word-break: break-word; overflow: visible; background-color: var(--monaco-editor-background-color); color: var(--monaco-editor-foreground-color); padding: 8px; box-sizing: border-box;"></div>
              <div class="row" style="padding: 0px; margin: 0px; width: 100%; max-width: 100%; box-sizing: border-box; overflow: hidden;">
                <div class="col col-md-12" id="console-output" style="width: 100%; max-width: 100%; padding: 0px; margin: 0px; box-sizing: border-box; overflow: hidden;">
										<div class="d-flex justify-content-center"><div class="dot-flashing invisible"></div></div>
									</div>
								</div>
							</div>
					</div>
        </div>

        <div id="main-process-info-history" class="tabcontent-process" style="height: 100%; overflow-y: auto; left: 0; margin: 0; padding: 15px; display: none; background-color: #f8f9fa;">
          <div class="row" id="process-history-container" style="padding: 0; margin: 0; background-color: #fff; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);"></div>
          <div id="history-tab-loader-main-detail" style="display: flex; flex: 1; height: 100px; width: 100px; position: absolute; top: 0; bottom: 0; left: 0; right: 0; margin: auto; flex-direction: column;">
                	<img src="../gif/loading-spinner-black.gif" style="height: 6rem;" alt="loading..." />
					<h5 style="width: 100%; margin-left: -75px; margin-top: 0">Please wait while we fetch the history</h5>
          </div>
        </div>
      </div>
      </div>
      
		</div>`;

    $("#main-process-content").html(content);

    switchTab(
      document.getElementById("main-process-info-code-tab"),
      "main-process-info-code"
    );

    GW.general.switchTab("process");

    $("#processcategory").val(code_type);

    $("#processname").val(process_name);

    $("#processid").val(process_id);

    GW.process.displayCodeArea(process_id, process_name, code_type, code);
    // GW.process.displayCodeArea(process_id, process_name, code_type, code, GW.process.cmid);

    GW.process.displayToolbar(process_id, process_name, code_type);

    $("#showCurrent").click(function () {
      GW.menu.details(process_id);

      GW.process.showSaved();
    });

    $("#log_switch").change(function () {
      var codeElement = document.getElementById("process_code_window");
      var consoleElement = document.getElementById("single-console-content");
      
      if (GW.process.dockmode == "left") {
        if (!this.checked) {
          if (consoleElement) {
            consoleElement.style.setProperty("display", "none", "important");
            consoleElement.style.setProperty("visibility", "hidden", "important");
          }
          if (codeElement) {
            codeElement.style.setProperty("width", "100%", "important");
            codeElement.style.setProperty("flex", "1 1 100%", "important");
            codeElement.style.setProperty("flex-basis", "100%", "important");
          }
        } else {
          if (consoleElement) {
            consoleElement.style.setProperty("display", "flex", "important");
            consoleElement.style.setProperty("visibility", "visible", "important");
          }
          // Restore saved width ratio or use default
          var leftWidth = GW.process.savedLeftWidthRatio !== null ? GW.process.savedLeftWidthRatio : 60;
          var rightWidth = 100 - leftWidth;
          if (codeElement) {
            codeElement.style.setProperty("width", `${leftWidth}%`, "important");
            codeElement.style.setProperty("flex", `0 0 ${leftWidth}%`, "important");
            codeElement.style.setProperty("flex-basis", `${leftWidth}%`, "important");
            codeElement.style.setProperty("flex-grow", "0", "important");
          }
          if (consoleElement) {
            consoleElement.style.setProperty("width", `${rightWidth}%`, "important");
            consoleElement.style.setProperty("max-width", `${rightWidth}%`, "important");
            consoleElement.style.setProperty("flex", `0 0 ${rightWidth}%`, "important");
            consoleElement.style.setProperty("flex-basis", `${rightWidth}%`, "important");
            consoleElement.style.setProperty("flex-grow", "0", "important");
            consoleElement.style.setProperty("box-sizing", "border-box", "important");
            consoleElement.style.setProperty("overflow-x", "hidden", "important");
            
            // Ensure all child elements respect parent width
            GW.process.ensureConsoleWidthConstraints();
          }
        }
      } else if (GW.process.dockmode == "bottom") {
        if (!this.checked) {
          if (consoleElement) {
            consoleElement.style.setProperty("display", "none", "important");
            consoleElement.style.setProperty("visibility", "hidden", "important");
          }
          if (codeElement) {
            codeElement.style.setProperty("height", "100%", "important");
            codeElement.style.setProperty("flex", "1 1 100%", "important");
            codeElement.style.setProperty("flex-basis", "100%", "important");
          }
        } else {
          if (consoleElement) {
            consoleElement.style.setProperty("display", "flex", "important");
            consoleElement.style.setProperty("visibility", "visible", "important");
          }
          // Restore saved height ratio or use default
          var topHeight = GW.process.savedTopHeightRatio !== null ? GW.process.savedTopHeightRatio : 60;
          var bottomHeight = 100 - topHeight;
          if (codeElement) {
            codeElement.style.setProperty("height", `${topHeight}%`, "important");
            codeElement.style.setProperty("flex", `0 0 ${topHeight}%`, "important");
            codeElement.style.setProperty("flex-basis", `${topHeight}%`, "important");
            codeElement.style.setProperty("flex-grow", "0", "important");
          }
          if (consoleElement) {
            consoleElement.style.setProperty("height", `${bottomHeight}%`, "important");
            consoleElement.style.setProperty("flex", `0 0 ${bottomHeight}%`, "important");
            consoleElement.style.setProperty("flex-basis", `${bottomHeight}%`, "important");
            consoleElement.style.setProperty("flex-grow", "0", "important");
          }
        }
      } else {
        // Default side-by-side layout
        if (!this.checked) {
          if (consoleElement) {
            consoleElement.style.setProperty("display", "none", "important");
            consoleElement.style.setProperty("visibility", "hidden", "important");
          }
          if (codeElement) {
            codeElement.style.setProperty("width", "100%", "important");
            codeElement.style.setProperty("flex", "1 1 100%", "important");
            codeElement.style.setProperty("flex-basis", "100%", "important");
          }
        } else {
          if (consoleElement) {
            consoleElement.style.setProperty("display", "flex", "important");
            consoleElement.style.setProperty("visibility", "visible", "important");
          }
          // Restore saved width ratio or use default
          var leftWidth = GW.process.savedLeftWidthRatio !== null ? GW.process.savedLeftWidthRatio : 60;
          var rightWidth = 100 - leftWidth;
          if (codeElement) {
            codeElement.style.setProperty("width", `${leftWidth}%`, "important");
            codeElement.style.setProperty("flex", `0 0 ${leftWidth}%`, "important");
            codeElement.style.setProperty("flex-basis", `${leftWidth}%`, "important");
            codeElement.style.setProperty("flex-grow", "0", "important");
          }
          if (consoleElement) {
            consoleElement.style.setProperty("width", `${rightWidth}%`, "important");
            consoleElement.style.setProperty("max-width", `${rightWidth}%`, "important");
            consoleElement.style.setProperty("flex", `0 0 ${rightWidth}%`, "important");
            consoleElement.style.setProperty("flex-basis", `${rightWidth}%`, "important");
            consoleElement.style.setProperty("flex-grow", "0", "important");
            consoleElement.style.setProperty("box-sizing", "border-box", "important");
            consoleElement.style.setProperty("overflow-x", "hidden", "important");
            
            // Ensure all child elements respect parent width
            GW.process.ensureConsoleWidthConstraints();
          }
        }
      }
      
      // Ensure console width constraints after log switch
      GW.process.ensureConsoleWidthConstraints();
      
      // Refresh Monaco editor layout if it exists
      if (GW.process.editor) {
        setTimeout(function() {
          GW.process.editor.layout();
        }, 50);
      }
    });

    $("#clearProcessLog").click(GW.ssh.clearProcessLog);

    // Set default resizer style to match left mode style
    setTimeout(function() {
      var dragMe = document.getElementById("dragMe");
      if (dragMe) {
        // Apply consistent resizer style (same as left mode)
        dragMe.style.setProperty("height", "100%", "important");
        dragMe.style.setProperty("width", "4px", "important");
        dragMe.style.setProperty("display", "block", "important");
        dragMe.style.setProperty("visibility", "visible", "important");
        dragMe.style.setProperty("cursor", "ew-resize", "important");
        dragMe.style.setProperty("background-color", "#cbd5e0", "important");
        dragMe.style.setProperty("flex-shrink", "0", "important");
        dragMe.style.setProperty("z-index", "100", "important");
        dragMe.style.setProperty("user-select", "none", "important");
        dragMe.style.setProperty("position", "relative", "important");
        dragMe.style.setProperty("pointer-events", "auto", "important");
        dragMe.style.setProperty("touch-action", "none", "important");
        dragMe.style.setProperty("transition", "background-color 0.2s", "important");
        dragMe.style.setProperty("opacity", "1", "important");
        
        // Add hover effect
        dragMe.onmouseenter = function() {
          this.style.setProperty("background-color", "#a0aec0", "important");
        };
        dragMe.onmouseleave = function() {
          this.style.setProperty("background-color", "#cbd5e0", "important");
        };
        
        // Check current dock mode and activate appropriate resizer
        if (GW.process.dockmode === "bottom") {
          GW.process.util.activateVerticalResizer("dragMe");
        } else if (GW.process.dockmode === "left") {
    GW.process.util.activateResizer("dragMe");
        } else {
          // Default: activate horizontal resizer for side-by-side layout
          GW.process.util.activateResizer("dragMe");
        }
      } else {
        console.warn("dragMe element not found, retrying...");
        setTimeout(function() {
          var dragMe = document.getElementById("dragMe");
          if (dragMe) {
            // Apply consistent resizer style
            dragMe.style.setProperty("height", "100%", "important");
            dragMe.style.setProperty("width", "4px", "important");
            dragMe.style.setProperty("display", "block", "important");
            dragMe.style.setProperty("visibility", "visible", "important");
            dragMe.style.setProperty("cursor", "ew-resize", "important");
            dragMe.style.setProperty("background-color", "#cbd5e0", "important");
            dragMe.style.setProperty("flex-shrink", "0", "important");
            dragMe.style.setProperty("z-index", "100", "important");
            dragMe.style.setProperty("user-select", "none", "important");
            dragMe.style.setProperty("position", "relative", "important");
            dragMe.style.setProperty("pointer-events", "auto", "important");
            dragMe.style.setProperty("touch-action", "none", "important");
            dragMe.style.setProperty("transition", "background-color 0.2s", "important");
            dragMe.style.setProperty("opacity", "1", "important");
            
            // Add hover effect
            dragMe.onmouseenter = function() {
              this.style.setProperty("background-color", "#a0aec0", "important");
            };
            dragMe.onmouseleave = function() {
              this.style.setProperty("background-color", "#cbd5e0", "important");
            };
            
            GW.process.util.activateResizer("dragMe");
          }
        }, 200);
      }
    }, 100);
  },

  openCity: function (evt, name) {
    GW.process.switchTab(evt.currentTarget, name);

    GW.history.stopAllTimers();
  },

  switchTab: function (ele, name) {
    console.log("Turn on the tab " + name);

    // Save current layout ratios before switching tabs to preserve them
    // We save the ratios, not pixel values, so they work regardless of container size
    var wasCodeTabVisible = document.getElementById("main-process-info-code") && 
                            document.getElementById("main-process-info-code").style.display !== "none";

    var i, tabcontent, tablinks;
    tabcontent = document.getElementsByClassName("tabcontent-process");
    for (i = 0; i < tabcontent.length; i++) {
      tabcontent[i].style.display = "none";
    }
    
    // Update tab buttons styling for main process tabs (using nav-link)
    tablinks = document.querySelectorAll(".process-tabs .nav-link");
    for (i = 0; i < tablinks.length; i++) {
      tablinks[i].classList.remove("active");
      tablinks[i].style.borderBottom = "none";
      tablinks[i].style.color = "#6c757d";
    }
    
    // Update tab buttons styling for side panel tabs (using tablinks-process)
    tablinks = document.querySelectorAll(".tablinks-process");
    for (i = 0; i < tablinks.length; i++) {
      tablinks[i].classList.remove("active");
      // Remove any inline styles that might indicate active state
      tablinks[i].style.backgroundColor = "";
      tablinks[i].style.color = "";
      tablinks[i].style.borderBottom = "";
      tablinks[i].style.fontWeight = "";
    }
    
    // Show selected tab content
    var tabElement = document.getElementById(name);
    if (tabElement) {
      tabElement.style.display = "block";
    }
    
    // Update selected tab button
    if (ele) {
      ele.classList.add("active");
      // Check if it's a side panel tab (tablinks-process) or main tab (nav-link)
      if (ele.classList.contains("tablinks-process")) {
        // Side panel tab styling
        ele.style.backgroundColor = "#f0f0f0";
        ele.style.color = "#333";
        ele.style.fontWeight = "bold";
        ele.style.borderBottom = "2px solid #007bff";
      } else {
        // Main process tab styling
        ele.style.borderBottom = "3px solid #f5576c";
        ele.style.color = "#f5576c";
      }
    }
    
    // Ensure editor-history-tab-panel always has correct width (100%, not 100vw)
    // This must be done immediately and with a delay to catch any code that sets it to 100vw later
    var tabPanel = document.getElementById("editor-history-tab-panel");
    if (tabPanel) {
      // Force set width to 100% immediately
      tabPanel.style.setProperty("width", "100%", "important");
      tabPanel.style.setProperty("max-width", "100%", "important");
      tabPanel.style.setProperty("box-sizing", "border-box", "important");
      
      // Also check and fix after a short delay to catch any code that sets it to 100vw
      setTimeout(function() {
        var currentWidth = tabPanel.style.width || window.getComputedStyle(tabPanel).width;
        if (currentWidth.includes('vw') || (!currentWidth.includes('%') && currentWidth !== 'auto')) {
          tabPanel.style.setProperty("width", "100%", "important");
          tabPanel.style.setProperty("max-width", "100%", "important");
          tabPanel.style.setProperty("box-sizing", "border-box", "important");
          console.log("switchTab: Fixed editor-history-tab-panel width from", currentWidth, "to 100%");
        }
      }, 50);
      
      // Check again after a longer delay to catch any async code
      setTimeout(function() {
        var currentWidth = tabPanel.style.width || window.getComputedStyle(tabPanel).width;
        if (currentWidth.includes('vw')) {
          tabPanel.style.setProperty("width", "100%", "important");
          tabPanel.style.setProperty("max-width", "100%", "important");
          tabPanel.style.setProperty("box-sizing", "border-box", "important");
          console.log("switchTab: Fixed editor-history-tab-panel width from", currentWidth, "to 100% (delayed check)");
        }
      }, 200);
    }
    
    // Only restore layout ratios when switching back to code tab
    // This ensures the logging window width is preserved
    if (name === "main-process-info-code") {
      setTimeout(function() {
        // Restore the saved layout ratios (width/height percentages) FIRST
        // This will set the percentage widths on both code editor and console
        GW.process.restoreLayoutRatios();
        
        // Then ensure console child elements respect parent width constraints
        // This must be called AFTER restoreLayoutRatios to preserve the percentage width
        GW.process.ensureConsoleWidthConstraints();
        
        // Refresh Monaco editor layout if it exists
        if (GW.process.editor) {
          GW.process.editor.layout();
        }
      }, 100);
    }
    // When switching to history tab, we don't need to do anything
    // The code tab (with logging window) is hidden, so its layout is preserved

    // GW.process.util.refreshCodeEditor();
  },

  displayToolbar: function (process_id, process_name, code_type) {
    GW.process.util.displayToolbar(
      process_id,
      process_name,
      code_type,
      "#process-btn-group"
    );
  },

  refreshBuiltinParameterList: function (proselectid, codeareaid) {
    $(".paramrow").remove();

    var cont = "";

    var current_operation = $("#" + proselectid)
      .find(":selected")
      .text();

    for (var i = 0; i < GW.process.builtin_processes.length; i++) {
      if (GW.process.builtin_processes[i].operation == current_operation) {
        for (
          var j = 0;
          j < GW.process.builtin_processes[i].params.length;
          j++
        ) {
          cont +=
            '<div class="row paramrow" style="margin-left:5px;margin-right:5px;">';
          cont +=
            '     <label for="parameter" class="col-sm-4 col-form-label control-label" style="font-size:12px;" >Parameter <u>' +
            GW.process.builtin_processes[i].params[j].name +
            "</u>: </label>" +
            '     <div class="col-sm-8"> 	<input type="text" class="form-control builtin-parameter" id="param_' +
            GW.process.builtin_processes[i].params[j].name +
            '" onchange="GW.process.updateBuiltin()" ></input>';

          cont += "</div></div>";
        }

        break;
      }
    }

    $("#" + codeareaid).append(cont);
  },

  displayCodeArea: function (process_id, process_name, code_type, code) {
    GW.process.util.displayCodeArea(
      code_type,
      code,
      "#code-embed",
      "#process_code_window"
    );
  },

  clearCodeEditorListener: function () {
    if (GW.process.editor != null) {
      GW.process.editor.off("change");
    }
  },

  showNonSaved: function () {
    this.isSaved = false;
    console.log("change event called");
    $("#main-process-tab").html("Process*");
    
    // Update code tab with icon, name, and asterisk, preserving original format
    var codeTab = $("#main-process-info-code-tab");
    if (codeTab.length && GW.process.current_process_name && GW.process.current_code_type) {
      var icon = GW.process.util.get_icon_by_process_type(GW.process.current_code_type);
      codeTab.html(icon + " " + GW.process.current_process_name + " - Code & Log*");
    } else {
      codeTab.html("Code*");
    }
  },

  showSaved: function () {
    this.isSaved = true;
    console.log("save event called");
    $("#main-process-tab").html("Process");
    
    // Update code tab with icon and name, preserving original format
    var codeTab = $("#main-process-info-code-tab");
    if (codeTab.length && GW.process.current_process_name && GW.process.current_code_type) {
      var icon = GW.process.util.get_icon_by_process_type(GW.process.current_code_type);
      codeTab.html(icon + " " + GW.process.current_process_name + " - Code & Log");
    } else {
      codeTab.html("Code");
    }
  },

  //edit switch should always be on
  editSwitch: function () {
    if (GW.process.checkIfProcessPanelActive()) {
      console.log("Turn on/off the fields");

      if (typeof $("#processid").val() != undefined) {
        GW.process.current_pid = $("#processid").val();

        GW.process.update(GW.process.current_pid);
        // GW.process.update(GW.process.current_pid, GW.process.cmid);

        $("#processcategory").prop("disabled", true); //don't allow change of process category

        $("#processname").prop("disabled", GW.process.editOn);

        $("#processid").prop("disabled", true); //always cannot edit id

        // if (GW.process.editor) {
        //   GW.process.editor.setOption("readOnly", GW.process.editOn);
        // }

        if ($(".builtin-process")) {
          $(".builtin-process").prop("disabled", GW.process.editOn);
        }

        if ($(".builtin-parameter")) {
          $(".builtin-parameter").prop("disabled", GW.process.editOn);
        }
      }
    }
  },

  noDock: function () {
    GW.process.util.noDock(
      "process-code-history-section",
      "process_code_window",
      "single-console-content",
      "dragMe"
    );
    GW.process.dockmode = "bottom";
  },

  downloadProcess: function(process_id) {
    // Get process code and download it as a file
    $.ajax({
      url: "detail",
      method: "POST",
      data: "type=process&id=" + process_id
    })
    .done(function(msg) {
      try {
        var processData = JSON.parse(msg);
        var code = processData.code || "";
        var name = processData.name || "process";
        var lang = processData.lang || "shell";
        
        // Determine file extension based on language
        var ext = "sh";
        if (lang === "python" || lang === "Python") {
          ext = "py";
        } else if (lang === "builtin") {
          ext = "txt";
        }
        
        // Create a blob with the code
        var blob = new Blob([code], { type: "text/plain" });
        var url = window.URL.createObjectURL(blob);
        var a = document.createElement("a");
        a.href = url;
        a.download = name + "." + ext;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        
        GW.general.showToasts("Process code downloaded successfully");
      } catch (e) {
        console.error("Failed to download process:", e);
        console.error("Response received:", msg);
        alert("Failed to download process: " + e.message);
      }
    })
    .fail(function(xhr, status, error) {
      console.error("Failed to download process:", error);
      console.error("Status:", status);
      console.error("Response:", xhr.responseText);
      alert("Failed to download process: " + error);
    });
  },

  bottomDock: function () {
    // Just change layout to dock log to bottom, no fullscreen
      GW.process.util.bottomDock(
        "process-code-history-section",
        "process_code_window",
        "single-console-content",
        "dragMe"
      );
      GW.process.dockmode = "bottom";
  },

  leftDock: function () {
    // Just change layout, don't maximize
      GW.process.util.leftDock(
        "process-code-history-section",
        "process_code_window",
        "single-console-content",
        "dragMe"
      );
      GW.process.dockmode = "left";
  },

  noDock: function () {
    // Restore normal layout - hide console, show code editor full height
    GW.process.util.noDock(
        "process-code-history-section",
        "process_code_window",
        "single-console-content",
        "dragMe"
      );
    GW.process.dockmode = "no";
    
    // Ensure process-info-bar is visible if it was expanded before
    var infoBar = document.getElementById("process-info-bar");
    var toggleBtn = document.getElementById("toggle-details-btn");
    if (infoBar && toggleBtn) {
      var icon = toggleBtn.querySelector('i');
      // Only show if it was expanded (chevron-up icon)
      if (icon && icon.className.includes("chevron-up")) {
        infoBar.style.setProperty("display", "block", "important");
        infoBar.style.setProperty("visibility", "visible", "important");
        infoBar.style.setProperty("height", "auto", "important");
        infoBar.style.setProperty("max-height", "200px", "important");
        infoBar.style.setProperty("overflow", "visible", "important");
        infoBar.style.setProperty("padding", "12px 20px", "important");
        infoBar.style.setProperty("margin", "", "important");
        infoBar.style.setProperty("position", "relative", "important");
        infoBar.style.setProperty("top", "auto", "important");
        infoBar.style.setProperty("left", "auto", "important");
        infoBar.style.setProperty("z-index", "auto", "important");
        infoBar.style.setProperty("opacity", "1", "important");
        infoBar.style.setProperty("pointer-events", "auto", "important");
      }
    }
    
    // Refresh Monaco editor layout if it exists
    if (GW.process.editor) {
      setTimeout(function() {
        GW.process.editor.layout();
      }, 100);
    }
  },

  maximize: function () {
    // Toggle fullscreen mode - if already fullscreen, exit; otherwise enter fullscreen
    var tabsContentWrapper = document.getElementById("process-tabs-content-wrapper");
    var maximizeBtn = document.getElementById("maximize-btn");
    
    // Check if already in fullscreen mode by checking if position is fixed
    var isFullscreen = tabsContentWrapper && 
                      tabsContentWrapper.style.position === "fixed" &&
                      GW.process.originalStyles;
    
    if (isFullscreen) {
      // Exit fullscreen mode
      GW.process.exitMaximize();
      return;
    }
    
    // Enter fullscreen mode
    var processContainer = document.getElementById("process-main-container");
    var mainContent = document.getElementById("main-process-content");
    var mainProcessInfo = document.getElementById("main-process-info");
    var tabsContainer = document.getElementById("process-tabs-container");
    
    if (tabsContentWrapper) {
      // Store original styles
      if (!GW.process.originalStyles) {
        // Save the log window visibility state before entering fullscreen
        var logSwitch = document.getElementById("log_switch");
        var consoleElement = document.getElementById("single-console-content");
        var logWindowVisible = logSwitch && logSwitch.checked && 
                              consoleElement && 
                              consoleElement.style.display !== "none" &&
                              window.getComputedStyle(consoleElement).display !== "none";
        
        GW.process.originalStyles = {
          tabsContentWrapper: {
            position: tabsContentWrapper.style.position || "",
            top: tabsContentWrapper.style.top || "",
            left: tabsContentWrapper.style.left || "",
            width: tabsContentWrapper.style.width || "",
            height: tabsContentWrapper.style.height || "",
            zIndex: tabsContentWrapper.style.zIndex || "",
            overflow: tabsContentWrapper.style.overflow || "",
            backgroundColor: tabsContentWrapper.style.backgroundColor || ""
          },
          logWindowVisible: logWindowVisible // Save log window visibility state
        };
        if (processContainer) {
          GW.process.originalStyles.processContainer = {
            position: processContainer.style.position || "",
            top: processContainer.style.top || "",
            left: processContainer.style.left || "",
            width: processContainer.style.width || "",
            height: processContainer.style.height || "",
            zIndex: processContainer.style.zIndex || "",
            overflow: processContainer.style.overflow || "",
            backgroundColor: processContainer.style.backgroundColor || ""
          };
        }
        if (mainContent) {
          GW.process.originalStyles.mainContent = {
            position: mainContent.style.position || "",
            top: mainContent.style.top || "",
            left: mainContent.style.left || "",
            width: mainContent.style.width || "",
            height: mainContent.style.height || "",
            zIndex: mainContent.style.zIndex || "",
            overflow: mainContent.style.overflow || ""
          };
        }
        if (mainProcessInfo) {
          GW.process.originalStyles.mainProcessInfo = {
            position: mainProcessInfo.style.position || "",
            top: mainProcessInfo.style.top || "",
            left: mainProcessInfo.style.left || "",
            width: mainProcessInfo.style.width || "",
            height: mainProcessInfo.style.height || "",
            zIndex: mainProcessInfo.style.zIndex || "",
            overflow: mainProcessInfo.style.overflow || ""
          };
        }
        if (tabsContainer) {
          GW.process.originalStyles.tabsContainer = {
            position: tabsContainer.style.position || "",
            top: tabsContainer.style.top || "",
            left: tabsContainer.style.left || "",
            width: tabsContainer.style.width || "",
            zIndex: tabsContainer.style.zIndex || ""
          };
        }
      }
      
      // Make the tabs-content-wrapper fullscreen - cover entire browser window
      // But keep tabs container visible at the top
      // Set z-index much lower than dialog (20000) to ensure dialogs appear on top
      var tabsHeight = tabsContainer ? tabsContainer.offsetHeight : 38;
      tabsContentWrapper.style.position = "fixed";
      tabsContentWrapper.style.top = tabsHeight + "px";
      tabsContentWrapper.style.left = "0";
      tabsContentWrapper.style.width = "100vw";
      tabsContentWrapper.style.height = "calc(100vh - " + tabsHeight + "px)";
      tabsContentWrapper.style.zIndex = "100";
      tabsContentWrapper.style.setProperty("z-index", "100", "important");
      tabsContentWrapper.style.overflow = "hidden";
      tabsContentWrapper.style.backgroundColor = "#fff";
      
      // Ensure tabs container is also fixed and visible
      if (tabsContainer) {
        tabsContainer.style.position = "fixed";
        tabsContainer.style.top = "0";
        tabsContainer.style.left = "0";
        tabsContainer.style.width = "100vw";
        tabsContainer.style.zIndex = "101";
        tabsContainer.style.setProperty("z-index", "101", "important");
      }
      
      // Also make parent containers fullscreen to ensure proper layering
      // But keep z-index low so dialogs can appear on top
      if (processContainer) {
        processContainer.style.position = "fixed";
        processContainer.style.top = "0";
        processContainer.style.left = "0";
        processContainer.style.width = "100vw";
        processContainer.style.height = "100vh";
        processContainer.style.zIndex = "100";
        processContainer.style.setProperty("z-index", "100", "important");
        processContainer.style.overflow = "hidden";
        processContainer.style.backgroundColor = "#fff";
      }
      
      if (mainContent) {
        mainContent.style.position = "fixed";
        mainContent.style.top = "0";
        mainContent.style.left = "0";
        mainContent.style.width = "100vw";
        mainContent.style.height = "100vh";
        mainContent.style.zIndex = "100";
        mainContent.style.setProperty("z-index", "100", "important");
        mainContent.style.overflow = "hidden";
        mainContent.style.backgroundColor = "#fff";
      }
      
      if (mainProcessInfo) {
        mainProcessInfo.style.position = "fixed";
        mainProcessInfo.style.top = "0";
        mainProcessInfo.style.left = "0";
        mainProcessInfo.style.width = "100vw";
        mainProcessInfo.style.height = "100vh";
        mainProcessInfo.style.zIndex = "100";
        mainProcessInfo.style.setProperty("z-index", "100", "important");
        mainProcessInfo.style.overflow = "hidden";
      }
      
      // Force refresh of content panel to adapt to new size
      // Only set height for fullscreen, width should remain 100% (relative to parent)
      var tabPanel = document.getElementById("editor-history-tab-panel");
      if (tabPanel) {
        tabPanel.style.setProperty("width", "100%", "important");
        tabPanel.style.setProperty("max-width", "100%", "important");
        tabPanel.style.setProperty("height", "calc(100vh - " + tabsHeight + "px)", "important");
        tabPanel.style.setProperty("box-sizing", "border-box", "important");
      }
      
      // Update maximize button icon and title to show "exit fullscreen" state
      if (maximizeBtn) {
        var icon = maximizeBtn.querySelector('i');
        if (icon) {
          icon.className = "fas fa-compress";
        }
        maximizeBtn.title = "Exit Fullscreen";
      }
    }
  },

  exitMaximize: function () {
    // Restore original styles
    var tabsContentWrapper = document.getElementById("process-tabs-content-wrapper");
    var processContainer = document.getElementById("process-main-container");
    var mainContent = document.getElementById("main-process-content");
    var mainProcessInfo = document.getElementById("main-process-info");
    var tabsContainer = document.getElementById("process-tabs-container");
    var infoBar = document.getElementById("process-info-bar");
    
    if (tabsContentWrapper && GW.process.originalStyles) {
      // Restore tabs container
      if (tabsContainer && GW.process.originalStyles.tabsContainer) {
        var styles = GW.process.originalStyles.tabsContainer;
        tabsContainer.style.position = styles.position || "";
        tabsContainer.style.top = styles.top || "";
        tabsContainer.style.left = styles.left || "";
        tabsContainer.style.width = styles.width || "";
        tabsContainer.style.zIndex = styles.zIndex || "";
      } else if (tabsContainer) {
        // Reset to default if no saved styles
        tabsContainer.style.position = "";
        tabsContainer.style.top = "";
        tabsContainer.style.left = "";
        tabsContainer.style.width = "";
        tabsContainer.style.zIndex = "";
      }
      
      // Restore tabs-content-wrapper styles
      if (GW.process.originalStyles.tabsContentWrapper) {
        var styles = GW.process.originalStyles.tabsContentWrapper;
        tabsContentWrapper.style.position = styles.position || "";
        tabsContentWrapper.style.top = styles.top || "";
        tabsContentWrapper.style.left = styles.left || "";
        tabsContentWrapper.style.width = styles.width || "";
        tabsContentWrapper.style.height = styles.height || "";
        tabsContentWrapper.style.zIndex = styles.zIndex || "";
        tabsContentWrapper.style.overflow = styles.overflow || "";
        tabsContentWrapper.style.backgroundColor = styles.backgroundColor || "";
    } else {
        // Reset to default if no saved styles
        tabsContentWrapper.style.position = "";
        tabsContentWrapper.style.top = "";
        tabsContentWrapper.style.left = "";
        tabsContentWrapper.style.width = "";
        tabsContentWrapper.style.height = "";
        tabsContentWrapper.style.zIndex = "";
        tabsContentWrapper.style.overflow = "";
        tabsContentWrapper.style.backgroundColor = "";
      }
      
      // Restore process container styles
      if (processContainer && GW.process.originalStyles.processContainer) {
        var styles = GW.process.originalStyles.processContainer;
        processContainer.style.position = styles.position || "";
        processContainer.style.top = styles.top || "";
        processContainer.style.left = styles.left || "";
        processContainer.style.width = styles.width || "";
        processContainer.style.height = styles.height || "";
        processContainer.style.zIndex = styles.zIndex || "";
        processContainer.style.overflow = styles.overflow || "";
        processContainer.style.backgroundColor = styles.backgroundColor || "";
      } else if (processContainer) {
        // Reset to default if no saved styles
        processContainer.style.position = "";
        processContainer.style.top = "";
        processContainer.style.left = "";
        processContainer.style.width = "";
        processContainer.style.height = "";
        processContainer.style.zIndex = "";
        processContainer.style.overflow = "";
        processContainer.style.backgroundColor = "";
      }
      
      // Restore main content styles
      if (mainContent && GW.process.originalStyles.mainContent) {
        var styles = GW.process.originalStyles.mainContent;
        mainContent.style.position = styles.position || "";
        mainContent.style.top = styles.top || "";
        mainContent.style.left = styles.left || "";
        mainContent.style.width = styles.width || "";
        mainContent.style.height = styles.height || "";
        mainContent.style.zIndex = styles.zIndex || "";
        mainContent.style.overflow = styles.overflow || "";
        mainContent.style.backgroundColor = "";
      } else if (mainContent) {
        // Reset to default if no saved styles
        mainContent.style.position = "";
        mainContent.style.top = "";
        mainContent.style.left = "";
        mainContent.style.width = "";
        mainContent.style.height = "";
        mainContent.style.zIndex = "";
        mainContent.style.overflow = "";
        mainContent.style.backgroundColor = "";
      }
      
      // Restore main process info styles
      if (mainProcessInfo && GW.process.originalStyles.mainProcessInfo) {
        var styles = GW.process.originalStyles.mainProcessInfo;
        mainProcessInfo.style.position = styles.position || "";
        mainProcessInfo.style.top = styles.top || "";
        mainProcessInfo.style.left = styles.left || "";
        mainProcessInfo.style.width = styles.width || "";
        mainProcessInfo.style.height = styles.height || "";
        mainProcessInfo.style.zIndex = styles.zIndex || "";
        mainProcessInfo.style.overflow = styles.overflow || "";
      } else if (mainProcessInfo) {
        // Reset to default if no saved styles
        mainProcessInfo.style.position = "";
        mainProcessInfo.style.top = "";
        mainProcessInfo.style.left = "";
        mainProcessInfo.style.width = "";
        mainProcessInfo.style.height = "";
        mainProcessInfo.style.zIndex = "";
        mainProcessInfo.style.overflow = "";
      }
      
      // Reset content panel to original size
      var tabPanel = document.getElementById("editor-history-tab-panel");
      if (tabPanel) {
        tabPanel.style.setProperty("width", "100%", "important");
        tabPanel.style.setProperty("max-width", "100%", "important");
        tabPanel.style.setProperty("height", "", "important");
        tabPanel.style.setProperty("box-sizing", "border-box", "important");
      }
      
      // Force hide metadata panel if it should be collapsed
      // Check if it was originally collapsed by checking the toggle button icon
      if (infoBar) {
        var toggleBtn = document.getElementById("toggle-details-btn");
        var icon = toggleBtn ? toggleBtn.querySelector('i') : null;
        
        // Check if metadata panel should be collapsed based on icon state
        var shouldBeCollapsed = true;
        if (icon) {
          // If icon is chevron-down, it should be collapsed
          shouldBeCollapsed = icon.className.includes("chevron-down");
        }
        
        // Also check current state
        var isCurrentlyCollapsed = infoBar.style.maxHeight === "0px" || 
                                    infoBar.style.display === "none" ||
                                    infoBar.style.visibility === "hidden" ||
                                    infoBar.offsetHeight === 0;
        
        // If it should be collapsed or is currently collapsed, force hide it
        if (shouldBeCollapsed || isCurrentlyCollapsed) {
          // Completely hide metadata panel - use !important to override any conflicting styles
          infoBar.style.setProperty("max-height", "0px", "important");
          infoBar.style.setProperty("display", "none", "important");
          infoBar.style.setProperty("visibility", "hidden", "important");
          infoBar.style.setProperty("overflow", "hidden", "important");
          infoBar.style.setProperty("padding", "0", "important");
          infoBar.style.setProperty("margin", "0", "important");
          infoBar.style.setProperty("height", "0", "important");
          infoBar.style.setProperty("min-height", "0", "important");
          infoBar.style.setProperty("flex-basis", "0", "important");
          infoBar.style.setProperty("flex-shrink", "0", "important");
          infoBar.style.setProperty("flex-grow", "0", "important");
          infoBar.style.setProperty("position", "absolute", "important");
          infoBar.style.setProperty("top", "-9999px", "important");
          infoBar.style.setProperty("left", "-9999px", "important");
          infoBar.style.setProperty("z-index", "-1", "important");
          infoBar.style.setProperty("opacity", "0", "important");
          infoBar.style.setProperty("pointer-events", "none", "important");
          if (icon) {
            icon.className = "fas fa-chevron-down";
          }
        } else {
          // If expanded, ensure it doesn't block tabs container
          infoBar.style.setProperty("position", "", "important");
          infoBar.style.setProperty("top", "", "important");
          infoBar.style.setProperty("left", "", "important");
          infoBar.style.setProperty("z-index", "", "important");
          infoBar.style.setProperty("pointer-events", "auto", "important");
        }
      }
      
      // Force layout recalculation to remove any gaps
      if (processContainer) {
        processContainer.style.setProperty("margin", "0", "important");
        processContainer.style.setProperty("padding", "0", "important");
      }
      
      // Ensure tabs container is at the top with no gap
      if (tabsContainer) {
        tabsContainer.style.setProperty("margin", "0", "important");
        tabsContainer.style.setProperty("margin-top", "0", "important");
        tabsContainer.style.setProperty("padding-top", "0", "important");
        tabsContainer.style.setProperty("position", "", "important");
        tabsContainer.style.setProperty("top", "", "important");
      }
      
      // Force a reflow to ensure styles are applied
      if (infoBar) {
        infoBar.offsetHeight; // Trigger reflow
      }
      if (tabsContainer) {
        tabsContainer.offsetHeight; // Trigger reflow
      }
      
      // Save log window visibility state BEFORE clearing originalStyles
      var savedLogWindowVisible = null;
      if (GW.process.originalStyles && GW.process.originalStyles.logWindowVisible !== undefined) {
        savedLogWindowVisible = GW.process.originalStyles.logWindowVisible;
      }
      
      // Update maximize button icon and title to show "enter fullscreen" state
      var maximizeBtn = document.getElementById("maximize-btn");
      if (maximizeBtn) {
        var icon = maximizeBtn.querySelector('i');
        if (icon) {
          icon.className = "fas fa-expand";
        }
        maximizeBtn.title = "Fullscreen";
      }
      
      GW.process.originalStyles = null;
    }
    
    // Restore log window visibility state if it was saved before fullscreen
    if (savedLogWindowVisible !== null) {
      var logSwitch = document.getElementById("log_switch");
      var consoleElement = document.getElementById("single-console-content");
      
      if (logSwitch && consoleElement) {
        // Restore the checkbox state
        logSwitch.checked = savedLogWindowVisible;
        
        // Restore the console element visibility
        if (savedLogWindowVisible) {
          // Show console - use block display (not flex) to maintain vertical stacking of header, log window, and console-output
          consoleElement.style.setProperty("display", "block", "important");
          consoleElement.style.setProperty("visibility", "visible", "important");
          // Ensure overflow-y is auto for scrolling
          consoleElement.style.setProperty("overflow-y", "auto", "important");
          consoleElement.style.setProperty("overflow-x", "hidden", "important");
          // Ensure child elements are block-level to maintain vertical stacking
          var logHeader = consoleElement.querySelector('div:first-child');
          if (logHeader) {
            logHeader.style.setProperty("display", "block", "important");
          }
          var processLogWindow = document.getElementById("process-log-window");
          if (processLogWindow) {
            processLogWindow.style.setProperty("display", "block", "important");
          }
          var consoleOutputRow = consoleElement.querySelector('.row');
          if (consoleOutputRow) {
            consoleOutputRow.style.setProperty("display", "block", "important");
          }
          // Restore layout ratios to maintain the previous layout
          setTimeout(function() {
            GW.process.restoreLayoutRatios();
            GW.process.ensureConsoleWidthConstraints();
            // Refresh Monaco editor layout if it exists
            if (GW.process.editor) {
              GW.process.editor.layout();
            }
          }, 100);
        } else {
          // Hide console
          consoleElement.style.setProperty("display", "none", "important");
          consoleElement.style.setProperty("visibility", "hidden", "important");
        }
      }
    } else {
      // If no saved state, just restore the dock mode without changing log visibility
      // But don't call noDock if log switch is checked, as it will hide the console
      var logSwitch = document.getElementById("log_switch");
      if (!logSwitch || !logSwitch.checked) {
        GW.process.util.noDock(
          "process-code-history-section",
          "process_code_window",
          "single-console-content",
          "dragMe"
        );
        GW.process.dockmode = "none";
      } else {
        // If log switch is checked, restore layout ratios instead
        setTimeout(function() {
          GW.process.restoreLayoutRatios();
          GW.process.ensureConsoleWidthConstraints();
          if (GW.process.editor) {
            GW.process.editor.layout();
          }
        }, 100);
      }
    }
    
    // Show dock buttons
    var bottomBtn = document.getElementById("bottom-dock-btn");
    var leftBtn = document.getElementById("left-dock-btn");
    if (bottomBtn) bottomBtn.style.display = "inline-block";
    if (leftBtn) leftBtn.style.display = "inline-block";
  },

  toggleDetails: function () {
    var infoBar = document.getElementById("process-info-bar");
    var toggleBtn = document.getElementById("toggle-details-btn");
    if (infoBar && toggleBtn) {
      var icon = toggleBtn.querySelector('i');
      var isCollapsed = infoBar.style.maxHeight === "0px" || 
                        infoBar.style.display === "none" ||
                        infoBar.style.visibility === "hidden" ||
                        infoBar.offsetHeight === 0;
      
      if (isCollapsed) {
        // Expand - show metadata panel
        infoBar.style.setProperty("max-height", "200px", "important");
        infoBar.style.setProperty("display", "block", "important");
        infoBar.style.setProperty("visibility", "visible", "important");
        infoBar.style.setProperty("overflow", "visible", "important");
        infoBar.style.setProperty("height", "auto", "important");
        infoBar.style.setProperty("min-height", "auto", "important");
        infoBar.style.setProperty("flex-basis", "auto", "important");
        infoBar.style.setProperty("position", "relative", "important");
        infoBar.style.setProperty("top", "auto", "important");
        infoBar.style.setProperty("left", "auto", "important");
        infoBar.style.setProperty("z-index", "auto", "important");
        infoBar.style.setProperty("opacity", "1", "important");
        infoBar.style.setProperty("pointer-events", "auto", "important");
        infoBar.style.setProperty("padding", "", "important");
        infoBar.style.setProperty("margin", "", "important");
        if (icon) {
          icon.className = "fas fa-chevron-up";
        }
      } else {
        // Collapse - hide metadata panel completely
        infoBar.style.setProperty("max-height", "0px", "important");
        infoBar.style.setProperty("display", "none", "important");
        infoBar.style.setProperty("visibility", "hidden", "important");
        infoBar.style.setProperty("overflow", "hidden", "important");
        infoBar.style.setProperty("height", "0", "important");
        infoBar.style.setProperty("min-height", "0", "important");
        infoBar.style.setProperty("flex-basis", "0", "important");
        infoBar.style.setProperty("flex-shrink", "0", "important");
        infoBar.style.setProperty("flex-grow", "0", "important");
        infoBar.style.setProperty("padding", "0", "important");
        infoBar.style.setProperty("margin", "0", "important");
        infoBar.style.setProperty("position", "absolute", "important");
        infoBar.style.setProperty("top", "-9999px", "important");
        infoBar.style.setProperty("left", "-9999px", "important");
        infoBar.style.setProperty("z-index", "-1", "important");
        infoBar.style.setProperty("opacity", "0", "important");
        infoBar.style.setProperty("pointer-events", "none", "important");
        if (icon) {
          icon.className = "fas fa-chevron-down";
        }
      }
      
      // Restore width/height ratios after toggling details
      GW.process.restoreLayoutRatios();
    }
  },
  
  ensureConsoleWidthConstraints: function() {
    // Ensure all console child elements respect parent width constraints to prevent exceeding
    // NOTE: Do NOT set width on single-console-content itself - it should maintain its percentage width
    // Only set constraints on its child elements
    
    const processLogWindow = document.getElementById("process-log-window");
    if (processLogWindow) {
      processLogWindow.style.setProperty("width", "100%", "important");
      processLogWindow.style.setProperty("max-width", "100%", "important");
      processLogWindow.style.setProperty("box-sizing", "border-box", "important");
      // Ensure overflow-y is visible so scrolling happens in parent container__right
      processLogWindow.style.setProperty("overflow", "visible", "important");
      processLogWindow.style.setProperty("overflow-wrap", "break-word", "important");
      processLogWindow.style.setProperty("word-wrap", "break-word", "important");
      processLogWindow.style.setProperty("word-break", "break-word", "important");
    }
    
    const consoleOutput = document.getElementById("console-output");
    if (consoleOutput) {
      consoleOutput.style.setProperty("width", "100%", "important");
      consoleOutput.style.setProperty("max-width", "100%", "important");
      consoleOutput.style.setProperty("box-sizing", "border-box", "important");
      consoleOutput.style.setProperty("overflow", "hidden", "important");
    }
    
    // Only set max-width and overflow-x on single-console-content, not width
    // This preserves the percentage width set by restoreLayoutRatios
    const singleConsoleContent = document.getElementById("single-console-content");
    if (singleConsoleContent) {
      // Don't override width - it should be a percentage (e.g., 40%)
      // Only ensure it doesn't exceed its allocated space
      var currentWidth = singleConsoleContent.style.width || window.getComputedStyle(singleConsoleContent).width;
      if (currentWidth && !currentWidth.includes('%')) {
        // If width is not a percentage, we need to preserve the percentage from saved ratio
        if (GW.process.savedLeftWidthRatio !== null) {
          var rightWidth = 100 - GW.process.savedLeftWidthRatio;
          singleConsoleContent.style.setProperty("width", `${rightWidth}%`, "important");
        }
      }
      singleConsoleContent.style.setProperty("max-width", "100%", "important");
      singleConsoleContent.style.setProperty("box-sizing", "border-box", "important");
      singleConsoleContent.style.setProperty("overflow-x", "hidden", "important");
      // Ensure overflow-y is auto so scrolling happens in container__right, not in process-log-window
      singleConsoleContent.style.setProperty("overflow-y", "auto", "important");
    }
  },
  
  restoreLayoutRatios: function() {
    // Restore saved width/height ratios to maintain layout consistency
    // IMPORTANT: Always restore BOTH code editor and logging window widths/heights together
    // to prevent width exceeding problems
    var codeElement = document.getElementById("process_code_window");
    var consoleElement = document.getElementById("single-console-content");
    
    if (!codeElement || !consoleElement) {
      return;
    }
    
    // Check if console is visible
    var consoleVisible = consoleElement.style.display !== "none" && 
                         consoleElement.style.visibility !== "hidden" &&
                         window.getComputedStyle(consoleElement).display !== "none";
    
    if (!consoleVisible) {
      // Console is hidden, code editor should be full width/height
      return;
    }
    
    if (GW.process.dockmode === "left" || GW.process.dockmode === "no" || !GW.process.dockmode) {
      // Left/right layout - restore width ratio for BOTH elements
      var leftWidth, rightWidth;
      
      if (GW.process.savedLeftWidthRatio !== null) {
        // Use saved ratio
        leftWidth = GW.process.savedLeftWidthRatio;
        rightWidth = 100 - leftWidth;
      } else {
        // No saved ratio - calculate from current widths and save it, then apply it
        var codeComputedStyle = window.getComputedStyle(codeElement);
        var consoleComputedStyle = window.getComputedStyle(consoleElement);
        var codeWidth = parseFloat(codeComputedStyle.width);
        var consoleWidth = parseFloat(consoleComputedStyle.width);
        var totalWidth = codeWidth + consoleWidth;
        
        if (totalWidth > 0) {
          leftWidth = (codeWidth / totalWidth) * 100;
          // Save the current ratio so it's preserved
          GW.process.savedLeftWidthRatio = leftWidth;
          rightWidth = 100 - leftWidth;
          console.log("restoreLayoutRatios: Calculated and saved current width ratio:", leftWidth);
        } else {
          // Fallback to default if calculation fails
          leftWidth = 60;
          rightWidth = 40;
          GW.process.savedLeftWidthRatio = leftWidth;
        }
      }
      
      // ALWAYS set BOTH code editor and logging window widths together
      codeElement.style.setProperty("width", `${leftWidth}%`, "important");
      codeElement.style.setProperty("flex", `0 0 ${leftWidth}%`, "important");
      codeElement.style.setProperty("flex-basis", `${leftWidth}%`, "important");
      codeElement.style.setProperty("flex-grow", "0", "important");
      codeElement.style.setProperty("flex-shrink", "0", "important");
      codeElement.style.setProperty("min-width", "0", "important");
      codeElement.style.setProperty("max-width", "none", "important");
      
      consoleElement.style.setProperty("width", `${rightWidth}%`, "important");
      consoleElement.style.setProperty("flex", `0 0 ${rightWidth}%`, "important");
      consoleElement.style.setProperty("flex-basis", `${rightWidth}%`, "important");
      consoleElement.style.setProperty("flex-grow", "0", "important");
      consoleElement.style.setProperty("flex-shrink", "0", "important");
      consoleElement.style.setProperty("min-width", "0", "important");
      consoleElement.style.setProperty("max-width", `${rightWidth}%`, "important");
      consoleElement.style.setProperty("box-sizing", "border-box", "important");
      consoleElement.style.setProperty("overflow-x", "hidden", "important");
      
      // Ensure all child elements of console respect parent width
      GW.process.ensureConsoleWidthConstraints();
      
      console.log("restoreLayoutRatios: Restored widths - code:", leftWidth + "%", "console:", rightWidth + "%");
    } else if (GW.process.dockmode === "bottom") {
      // Top/bottom layout - restore height ratio for BOTH elements
      var topHeight, bottomHeight;
      
      if (GW.process.savedTopHeightRatio !== null) {
        // Use saved ratio
        topHeight = GW.process.savedTopHeightRatio;
        bottomHeight = 100 - topHeight;
      } else {
        // No saved ratio - calculate from current heights and save it, then apply it
        var codeComputedStyle = window.getComputedStyle(codeElement);
        var consoleComputedStyle = window.getComputedStyle(consoleElement);
        var codeHeight = parseFloat(codeComputedStyle.height);
        var consoleHeight = parseFloat(consoleComputedStyle.height);
        var totalHeight = codeHeight + consoleHeight;
        
        if (totalHeight > 0) {
          topHeight = (codeHeight / totalHeight) * 100;
          // Save the current ratio so it's preserved
          GW.process.savedTopHeightRatio = topHeight;
          bottomHeight = 100 - topHeight;
          console.log("restoreLayoutRatios: Calculated and saved current height ratio:", topHeight);
        } else {
          // Fallback to default if calculation fails
          topHeight = 60;
          bottomHeight = 40;
          GW.process.savedTopHeightRatio = topHeight;
        }
      }
      
      // ALWAYS set BOTH code editor and logging window heights together
      codeElement.style.setProperty("height", `${topHeight}%`, "important");
      codeElement.style.setProperty("flex", `0 0 ${topHeight}%`, "important");
      codeElement.style.setProperty("flex-basis", `${topHeight}%`, "important");
      codeElement.style.setProperty("flex-grow", "0", "important");
      codeElement.style.setProperty("flex-shrink", "0", "important");
      codeElement.style.setProperty("min-height", "0", "important");
      codeElement.style.setProperty("max-height", "none", "important");
      
      consoleElement.style.setProperty("height", `${bottomHeight}%`, "important");
      consoleElement.style.setProperty("flex", `0 0 ${bottomHeight}%`, "important");
      consoleElement.style.setProperty("flex-basis", `${bottomHeight}%`, "important");
      consoleElement.style.setProperty("flex-grow", "0", "important");
      consoleElement.style.setProperty("flex-shrink", "0", "important");
      consoleElement.style.setProperty("min-height", "0", "important");
      consoleElement.style.setProperty("max-height", "none", "important");
      
      console.log("restoreLayoutRatios: Restored heights - code:", topHeight + "%", "console:", bottomHeight + "%");
    }
    
    // Refresh Monaco editor layout if it exists
    if (GW.process.editor) {
      setTimeout(function() {
        GW.process.editor.layout();
      }, 50);
    }
  },

  refreshSearchList: function () {
    GW.search.filterMenuListUtil(
      "process_folder_shell_target",
      "processes",
      "process"
    );

    GW.search.filterMenuListUtil(
      "process_folder_builtin_target",
      "processes",
      "process"
    );

    GW.search.filterMenuListUtil(
      "process_folder_python_target",
      "processes",
      "process"
    );
  },

  refreshProcessList: function () {
    $.ajax({
      url: "list",

      method: "POST",

      data: "type=process",
    })
      .done(function (msg) {
        msg = GW.general.parseResponse(msg);

        console.log("Start to refresh the process list..");

        // $("#"+GW.menu.getPanelIdByType("host")).html("");
        $("#process_folder_shell_target").html("");
        $("#process_folder_builtin_target").html("");
        $("#process_folder_python_target").html("");

        GW.process.list(msg);

        // if($(".processselector")) {

        // 	for(var i=0;i<msg.length;i++){

        // 		$(".processselector").append("<option id=\""+msg[i].id+"\">"+msg[i].name+"</option>");

        // 	}

        // }
      })
      .fail(function (jxr, status) {
        console.error("fail to list process");
      });
  },

  /**
   * add a new item under the process menu
   */
  addMenuItem: function (one, folder) {
    var menuItem = `<li class="process" id="process-${one.id}" 
							onclick="var event = arguments[0] || window.event; event.stopPropagation();
							GW.menu.details('${one.id}', 'process')">
							<div class="row bare-window">
								<div class="col-md-8 bare-window" style="overflow: hidden; white-space: nowrap; text-overflow: ellipsis;" title="${one.name}"><span>&nbsp;&nbsp;&nbsp;${one.name}</span></div>
								<div class="col-md-4 bare-window">
									<button type="button" class="btn btn-warning btn-xs pull-right right-button-vertical-center" 
									onclick="var event = arguments[0] || window.event; event.stopPropagation();
									GW.workspace.theGraph.addProcess('${one.id}','${one.name}');">Add to Weaver</button>
								</div>
							</div>
						</li>`;

    if (folder != null) {
      var folder_ul = $("#process_folder_" + folder + "_target");

      if (!folder_ul.length) {
        $("#" + GW.menu.getPanelIdByType("process")).append(
          '<li class="folder" id="process_folder_' +
            folder +
            '" data-toggle="collapse" data-target="#process_folder_' +
            folder +
            '_target"> ' +
            ' <a href="javascript:void(0)"> ' +
            folder +
            " </a>" +
            " </li>" +
            ' <ul class="sub-menu collapse" id="process_folder_' +
            folder +
            '_target"></ul>'
        );

        folder_ul = $("#process_folder_" + folder + "_target");
      }

      folder_ul.prepend(menuItem);
    } else {
      $("#" + GW.menu.getPanelIdByType("process")).prepend(menuItem);
    }
  },

  /**
   * add process object to workspace
   */
  addWorkspace: function (one) {
    //randomly put a new object to the blank space

    var instanceid = GW.workspace.theGraph.addProcess(one.id, one.name);
  },

  expand: function (folder) {
    console.log("EXPAND Process type");

    $("#process_folder_" + folder + "_target").collapse("show");
  },

  list: function (msg) {
    for (var i = 0; i < msg.length; i++) {
      this.addMenuItem(
        msg[i],
        msg[i].lang == null ? msg[i].description : msg[i].lang
      );
    }

    $("#processes").collapse("show");
  },

  updateBuiltin: function () {
    var pid = $("#processid").val();

    var plang = $("#processcategory").val();

    var pname = $("#processname").val();

    var pdesc = $("#processcategory").val();

    var oper = $(".builtin-process").val();

    var pcode = { operation: oper, params: [] };

    var confidential = $('input[name="confidential_process"]:checked').val();

    $(".builtin-parameter").each(function (i, obj) {
      var inputfield = $(this);

      var paramname = inputfield.attr("id");

      var paramval = inputfield.val();

      pcode.params.push({ name: paramname.substring(6), value: paramval });
    });

    pcode = JSON.stringify(pcode);

    this.updateRaw(pid, pname, plang, pdesc, pcode, confidential);
  },

  updateRaw: function (pid, pname, plang, pdesc, pcode, confidential) {
    var req = {
      type: "process",

      lang: plang,

      desc: pdesc, //use the description column to store the process type

      name: pname,

      id: pid,

      owner: GW.user.current_userid,

      confidential: confidential,

      code: pcode,
    };

    $.ajax({
      url: "edit/process",

      method: "POST",

      contentType: "application/json",

      dataType: "json",

      data: JSON.stringify(req),
    })
      .done(function (msg) {
        msg = GW.general.parseResponse(msg);

        console.log("Updated!!");

        GW.general.showToasts("Code updated.");

        console.log(
          "If the process name is changed, the item in the menu should be changed at the same time. "
        );

        GW.process.refreshProcessList();

        GW.process.showSaved();
      })
      .fail(function (jqXHR, textStatus) {
        alert("Fail to update the process.");
      });
  },

  update: function (pid, cmid) {
    console.log("update process id: " + pid);

    // if(this.precheck()){

    var plang = $("#processcategory").val();

    var pname = $("#processname").val();

    var pdesc = $("#processcategory").val();

    // var pcode = GW.process.getCode(cmid);
    var pcode = GW.process.getCode();

    var confidential = $('input[name="confidential_process"]:checked').val();

    if (pid != null) {
      if (plang == "builtin") {
        GW.process.updateBuiltin();
      } else {
        GW.process.updateRaw(pid, pname, plang, pdesc, pcode, confidential);
      }
    }
  },

  add: function (run, cmid) {
    this.current_pid = null;

    if (this.precheck()) {
      var confidential = "FALSE"; //default is public

      if (
        typeof $('input[name="confidential-' + cmid + '"]:checked').val() !=
        "undefined"
      ) {
        confidential = $(
          'input[name="confidential-' + cmid + '"]:checked'
        ).val();
      }

      var req = {
        type: "process",

        lang: $("#processcategory-" + cmid).val(),

        description: $("#processcategory-" + cmid).val(), //use the description column to store the process type

        name: $("#processname-" + cmid).val(),

        code: GW.process.getCode(cmid),

        owner: GW.user.current_userid,

        confidential: confidential,
      };

      $.ajax({
        url: "add/process",

        method: "POST",

        contentType: "application/json",

        dataType: "json",

        data: JSON.stringify(req),
      })
        .done(function (msg) {
          msg = GW.general.parseResponse(msg);

          // msg.desc = req.desc;

          GW.process.addMenuItem(msg, req.lang);

          GW.process.expand(req.lang);

          // GW.menu.details(msg.id, 'process').click();

          // GW.process.display(msg);
          console.log("add process id: " + msg.id);

          setTimeout(function () {
            // if (GW.process.editor) {
            //     GW.process.editor.setValue(req.code); // Set the editor's content to the added process code
            // }
            GW.menu.details(msg.id, "process")?.click();
          }, 100);

          if (run)
            GW.process.runProcess(
              msg.id,
              msg.name,
              $("#processcategory-" + cmid).val()
            );
        })
        .fail(function (jqXHR, textStatus) {
          alert("Fail to add the process.");
        });

      return true;
    } else {
      alert("Process name and code must be non-empty!");

      return false;
    }
    // cmid = Math.floor(Math.random() * 1000);
    // console.log("after add: " + cmid);
  },

  /**
   * create a WebSocket-based dialog for outputting the log of Bash scripts
   */
  showSSHOutputLog: function (msg) {
    GW.ssh.openLog(msg);
  },

  clearProcessLogging: function () {
    if ($("#process-log-window").length) {
      $("#process-log-window").html("");

      GW.ssh.current_process_log_length = 0;
    }
  },

  /**
   * after the server side is done, this callback is called on each builtin process
   */
  callback: function (msg) {
    var oper = msg.operation;

    console.log("Builtin Callback Triggered");
    console.log("{{GW.process.js}}: " + msg.path);
    var filename = msg.path.replace(/^.*[\\\/]/, "");

    GW.ssh.echo('<img src="../download/temp/' + filename + '" width="100%" > ');

    if (oper == "ShowResultMap") {
      //show the map
      GW.result.preview(msg.filename);
    } else if (oper == "DownloadData") {
      //download the map
      GW.result.download(msg.filename);
    }
  },

  sendExecuteRequest: function (req, dialog, button, log_pid) {
    console.log("sendExecRequest-1");
    GW.process.clearProcessLogging();
    console.log("sendExecRequest-2");

    var newhistid = GW.general.makeid(12);

    req.history_id = newhistid;

    console.log("current client token is: " + GW.general.CLIENT_TOKEN);

    req.token = GW.general.CLIENT_TOKEN;

    req.operation = "ShowResultMap";

    GW.process.last_executed_process_id = req.processId;

    GW.process.showSSHOutputLog({
      token: GW.main.getJSessionId(),
      history_id: newhistid,
    });

    $.ajax({
      url: "executeProcess",

      type: "POST",

      data: req,
    })
      .done(function (msg) {
        if (msg) {
          msg = GW.general.parseResponse(msg);

          if (msg.ret == "success") {
            console.log("the process is under execution.");

            GW.process.history_id = msg.history_id;
          } else if (msg.ret == "fail") {
            alert("Fail to execute the process.");

            console.error("fail to execute the process " + msg.reason);
          }

          if (dialog) {
            try {
              dialog.closeFrame();
            } catch (e) {}
          }
        } else {
          console.error("Return Response is Empty");
        }
      })
      .fail(function (jxr, status) {
        alert(
          "Error: unable to log on. Check if your password or the configuration of host is correct."
        );

        if ($("#inputpswd").length) $("#inputpswd").val("");

        if ($("#pswd-confirm-btn").prop("disabled")) {
          $("#pswd-confirm-btn").prop("disabled", false);
        }

        console.error("fail to execute the process " + req.processId);
      });
  },

  executeCallback: function (encrypt, req, dialogItself, button) {
    req.pswd = encrypt;

    GW.ssh.process_output_id = "process-log-window";

    GW.process.sendExecuteRequest(req, dialogItself, button);
  },

  /**
   * This function is to directly execute one process
   */
  executeProcess: function (pid, hid, lang, callback_func) {
    if (callback_func == null) {
      callback_func = GW.process.executeCallback;
    }

    var req = {
      processId: pid,

      hostId: hid,

      desc: lang,

      lang: lang,
    };

    if (req.lang == "python") {
      //check if there is cached environment for this host

      var cached_env = GW.host.findEnvCache(hid);

      if (cached_env != null) {
        req.env = cached_env;

        GW.host.start_auth_single(hid, req, callback_func);
      } else {
        // retrieve the environment list of a host

        $.ajax({
          url: "env",

          method: "POST",

          data: "hid=" + hid,
        })
          .done(function (msg) {
            msg = GW.general.parseResponse(msg);

            if (GW.process.env_frame != null) {
              try {
                GW.process.env_frame.closeFrame();
              } catch (e) {}

              GW.process.env_frame = null;
            }

            var envselector =
              '<div class="form-group">' +
              '<label for="env-select">Select Environment:</label>' +
              '<select id="env-select" class="form-control"> ' +
              '	<option value="default">Default</option>' +
              '	<option value="new">New</option>';

            GW.process.envlist = msg;

            for (var i = 0; i < msg.length; i += 1) {
              envselector +=
                '<option value="' +
                msg[i].id +
                '">' +
                msg[i].name +
                "</option>";
            }

            envselector += "</select>";

            var content =
              '<div class="modal-body" style="font-size: 12px;">' +
              "<form> " +
              '    <div class="row"> ' +
              envselector +
              "    </div>" +
              '	<div class="form-group row"> ' +
              '    <label class="control-label col-sm-4" for="bin">Python Command:</label> ' +
              '    <div class="col-sm-8"> ' +
              '      <input type="text" class="form-control" id="bin" placeholder="python3" disabled> ' +
              "    </div> " +
              "  	</div>" +
              '	<div class="form-group row"> ' +
              '    <label class="control-label col-sm-4" for="env">Environment Name:</label> ' +
              '    <div class="col-sm-8"> ' +
              '      <input type="text" class="form-control" id="env" placeholder="my-conda-env" disabled> ' +
              "    </div> " +
              "  	</div>" +
              '	<div class="form-group row"> ' +
              '    <label class="control-label col-sm-4" for="env">Base Directory:</label> ' +
              '    <div class="col-sm-8"> ' +
              '      <input type="text" class="form-control" id="basedir" placeholder="/tmp/" disabled> ' +
              "    </div> " +
              "  	</div>" +
              "</form>" +
              '	<div class="form-group col-sm-10">' +
              '		<input type="checkbox" class="form-check-input" id="remember" >' +
              '		<label class="form-check-label" for="remember">Don\'t ask again for this host</label>' +
              "   </div></div>";

            content +=
              '<div class="modal-footer">' +
              '	<button type="button" id="process-confirm-btn" class="btn btn-outline-primary">Confirm</button> ' +
              '	<button type="button" id="process-cancel-btn" class="btn btn-outline-secondary">Cancel</button>' +
              "</div>";

            GW.process.env_frame = GW.process.createJSFrameDialog(
              520,
              450,
              content,
              "Set " + req.lang + " environment"
            );

            $("#env-select").change(function (e) {
              if ($(this).val() == "default") {
                $("#bin").prop("disabled", true);

                $("#env").prop("disabled", true);

                $("#basedir").prop("disabled", true);
              } else {
                $("#bin").prop("disabled", false);

                $("#env").prop("disabled", false);

                $("#basedir").prop("disabled", false);

                if ($(this).val() != "new") {
                  var envid = $(this).val();

                  for (var i = 0; i < GW.process.envlist.length; i += 1) {
                    var env = GW.process.envlist[i];

                    if (env.id == envid) {
                      $("#bin").val(env.bin);

                      $("#env").val(env.pyenv);

                      $("#basedir").val(env.basedir);

                      break;
                    }
                  }
                }
              }
            });

            $("#process-confirm-btn").click(function () {
              if ($(this).val() == "default") {
                req.env = {
                  bin: "default",
                  pyenv: "default",
                  basedir: "default",
                };
              } else {
                req.env = {
                  bin: $("#bin").val(),
                  pyenv: $("#env").val(),
                  basedir: $("#basedir").val(),
                };
              }

              if ($("#remember").prop("checked")) {
                GW.host.setEnvCache(hid, req.env);
              }

              GW.host.start_auth_single(hid, req, callback_func);

              GW.process.env_frame.closeFrame();
            });

            $("#process-cancel-btn").click(function () {
              GW.process.env_frame.closeFrame();
            });
          })
          .fail(function (jxr, status) {
            console.error("fail to get the environment on this host");
          });
      }
    } else {
      GW.host.start_auth_single(hid, req, callback_func);
    }
  },

  /**
   * Show a Run process dialog
   * @param {*} pid
   * @param {*} pname
   * @param {*} lang
   */
  runProcess: function (pid, pname, lang, callback_func) {
    GW.process.editSwitch();
    if (!GW.process.isSaved) {
      if (
        confirm(
          "You have non-saved changes in this process. Do you want to continue?"
        )
      ) {
        //continue
      } else {
        return;
      }
    }

    if (callback_func == null) {
      callback_func = GW.process.executeCallback;
    }

    var h = GW.process.findCache(pid);

    if (h == null) {
      var content =
        '<div class="modal-body" style="font-size: 12px; display: flex; flex-direction: column;">' +
        '<div style="margin-bottom: 10px;">Run Process ' +
        pname +
        " on:</div>" +
        '<div style="display: flex; flex-direction: column;">' +
        '   <select class="form-control" id="hostselector"></select>' +
        '   <div style="margin-top: 10px;">' +
        '       <input type="checkbox" class="form-check-input" id="remember" />' +
        '       <label for="remember">Remember this process-host connection</label>' +
        "   </div>" +
        "</div>" +
        "</div>" +
        '<div class="modal-footer">' +
        '   <button type="button" id="host-execute-btn" class="btn btn-outline-primary">Execute</button>' +
        '   <button type="button" id="host-cancel-btn" class="btn btn-outline-secondary">Cancel</button>' +
        "</div>";

      if (GW.process.host_frame != null) {
        try {
          GW.process.host_frame.closeFrame();
        } catch (e) {}

        GW.process.host_frame = null;
      }

      GW.process.host_frame = GW.process.createJSFrameDialog(
        550,
        280,
        content,
        "Select a host"
      );

      $.ajax({
        url: "list",

        method: "POST",

        data: "type=host",
      })
        .done(function (msg) {
          msg = GW.general.parseResponse(msg);

          $("#hostselector").find("option").remove().end();

          for (var i = 0; i < msg.length; i++) {
            if (msg[i].type == "ssh") {
              if (GW.host.isLocal(msg[i])) {
                $("#hostselector").append(
                  '<option id="' +
                    msg[i].id +
                    '" value="' +
                    msg[i].ip +
                    '" selected="selected">' +
                    msg[i].name +
                    "</option>"
                ); // default select localhost
              } else {
                $("#hostselector").append(
                  '<option id="' +
                    msg[i].id +
                    '" value="' +
                    msg[i].ip +
                    '" >' +
                    msg[i].name +
                    "</option>"
                );
              }
            }
          }
        })
        .fail(function (jxr, status) {
          console.error("fail to list host");
        });

      $("#host-execute-btn").click(function () {
        var hostid = $("#hostselector").children(":selected").attr("id");

        var hostip = $("#hostselector").children(":selected").attr("value");

        if (hostip == "127.0.0.1") {
          GW.host.local_hid = hostid;
        }

        //remember the process-host connection

        if (document.getElementById("remember").checked) {
          GW.process.setCache(pid, hostid); //remember s
        }

        GW.process.executeProcess(pid, hostid, lang, callback_func);

        GW.process.host_frame.closeFrame();
      });

      $("#host-cancel-btn").click(function () {
        GW.process.host_frame.closeFrame();
      });
    } else {
      GW.process.executeProcess(pid, h, lang, callback_func);
    }
  },
};
