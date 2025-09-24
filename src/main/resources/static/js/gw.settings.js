GW.settings = {

  // These settings will only be saved in the browser storage. Cleaning cache will reset everything.
  // The server side won't store any of these settings. 

  default_monaco_theme: 'vs-light',

  selected_monaco_theme: null,

  init: function(){

    this.selected_monaco_theme = localStorage.getItem('editorTheme') || GW.settings.default_monaco_theme
    
    // Initialize progress indicator auto-close setting if not set
    if (localStorage.getItem('gw_progress_auto_close') === null) {
      localStorage.setItem('gw_progress_auto_close', 'false'); // Default to manual close
    }
  },

  clearCache: function () {
    if (
      confirm(
        "Do you want to clear all the cached information (including passwords, the connection between process/workflow and host)?",
      )
    ) {
      GW.host.clearCache();

      GW.process.clearCache();

      GW.workflow.clearCache();

      alert("Cache cleared.");
    }
  },

  clearPasswords: function () {
    if (confirm("Do you want to clear the remembered passwords?")) {
      GW.host.clearCache();

      alert("Cache cleared.");
    }
  },

  clearConnections: function () {
    if (
      confirm(
        "Do you want to clear the remembered mappings between processes/workflows and hosts?",
      )
    ) {
      GW.process.clearCache();

      GW.workflow.clearCache();

      alert("Cache cleared.");
    }
  },

  clearProcessConnections: function () {
    if (
      confirm(
        "Do you want to clear the remembered mappings between processes and hosts?",
      )
    ) {
      GW.process.clearCache();

      alert("Cache cleared.");
    }
  },

  clearWorkflowConnections: function () {
    if (
      confirm(
        "Do you want to clear the remembered mappings between workflows and hosts?",
      )
    ) {
      GW.workflow.clearCache();

      alert("Cache cleared.");
    }
  },

  showDialog: function () {
    var content =
      '<div class="list-group" style="padding:10px;"> ' +
      '    <a class="list-group-item clearfix" href="javascript:void(0)"> ' +
      "        Clear Connection between Process and Host " +
      '        <span class="pull-right"> ' +
      '            <span class="btn btn-xs btn-default" onclick="GW.settings.clearProcessConnections();"> ' +
      '                <span class="glyphicon glyphicon-play" aria-hidden="true"></span> ' +
      "            </span> " +
      "        </span> " +
      "    </a> " +
      '    <a class="list-group-item clearfix" href="javascript:void(0)"> ' +
      "        Clear Connection between Workflow and Host " +
      '        <span class="pull-right"> ' +
      '            <span class="btn btn-xs btn-default" onclick="GW.settings.clearWorkflowConnections();"> ' +
      '                <span class="glyphicon glyphicon-play" aria-hidden="true"></span> ' +
      "            </span> " +
      "        </span> " +
      "    </a> " +
      '    <a class="list-group-item clearfix" href="javascript:void(0)"> ' +
      "        Clear Passwords Only" +
      '        <span class="pull-right"> ' +
      '            <span class="btn btn-xs btn-default" onclick="GW.settings.clearPasswords();"> ' +
      '                <span class="glyphicon glyphicon-play" aria-hidden="true"></span> ' +
      "            </span> " +
      "        </span> " +
      "    </a> " +
      '    <a class="list-group-item clearfix" href="javascript:void(0)"> ' +
      "        Clear All Cached Information " +
      '        <span class="pull-right"> ' +
      '            <span class="btn btn-xs btn-default" onclick="GW.settings.clearCache();"> ' +
      '                <span class="glyphicon glyphicon-play" aria-hidden="true"></span> ' +
      "            </span> " +
      "        </span> " +
      "    </a> " +
      '    <div class="list-group-item clearfix"> ' +
      "        Select Editor Theme " +
      '        <span class="pull-right"> ' +
      '            <select id="editor-theme-selector" class="form-control" style="width: auto;">' +
      '                <option value="vs-dark">Dark</option>' +
      '                <option value="vs-light">Light</option>' +
      '                <option value="hc-black">High Contrast</option>' +
      "            </select> " +
      "        </span> " +
      "    </div> " +
      '    <div class="list-group-item clearfix"> ' +
      "        Auto-close Progress Indicator " +
      '        <span class="pull-right"> ' +
      '            <select id="progress-autoclose-selector" class="form-control" style="width: auto;">' +
      '                <option value="true">Yes</option>' +
      '                <option value="false">No</option>' +
      "            </select> " +
      "        </span> " +
      "    </div> " +
      "</div>";

    var frame = GW.process.createJSFrameDialog(360, 320, content, "Settings");

    console.log("GW.settings.selected_monaco_theme = " + GW.settings.selected_monaco_theme)

    // Set the current theme as selected in the dropdown
    $('#editor-theme-selector').val(GW.settings.selected_monaco_theme);

    // Set the current progress auto-close setting
    var autoCloseProgress = localStorage.getItem('gw_progress_auto_close') || 'false';
    $('#progress-autoclose-selector').val(autoCloseProgress);

    // Add event listener to save the selected theme
    $('#editor-theme-selector').on('change', function () {
      var selectedTheme = $(this).val();
      console.log("Current theme is changed to: " + selectedTheme)
      GW.settings.selected_monaco_theme = selectedTheme
      localStorage.setItem('editorTheme', selectedTheme); // Save to local storage
      monaco.editor.setTheme(selectedTheme); // Apply the theme to Monaco Editor
      GW.settings.syncMonacoStyles(GW.process.editor)
      
      // Apply theme to history panels
      if (typeof GW.history !== 'undefined' && GW.history.applyHistoryTheme) {
        GW.history.applyHistoryTheme();
      }
    });
    
    // Add event listener to save the progress auto-close setting
    $('#progress-autoclose-selector').on('change', function () {
      var autoClose = $(this).val();
      console.log("Progress auto-close setting changed to: " + autoClose);
      localStorage.setItem('gw_progress_auto_close', autoClose); // Save to local storage
    });

  },

  syncMonacoStyles: function(monacoEditor) {
      const themeColors = monacoEditor._themeService._theme.colors;
      let colormap = {
        "editor.background": themeColors['editor.foreground']?._toString
      }

      themeColors.forEach((value, key) => {
        // console.log(`${key}: ${value}`);
        colormap[key] = value
      });

      // Example: Set CSS variables based on Monaco theme
      document.documentElement.style.setProperty(
          '--monaco-background-color',
          colormap['editor.background'] || '#1e1e1e'
      );
      document.documentElement.style.setProperty(
          '--monaco-foreground-color',
          colormap['editor.foreground'] || '#d4d4d4'
      );
      document.documentElement.style.setProperty(
          '--monaco-scrollbar-color',
          colormap['scrollbarSlider.background'] || '#797979'
      );
      console.log("Setting style to monaco")
  }

};
