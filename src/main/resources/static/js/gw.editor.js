GW.editor = {
  beforeFullscreen: {},

  isfullscreen: true,

  forceSidePanelFullScreen: function(fullscreen) {
    var sidepanel = $(".cd-panel__container");
    var cdPanel = $(".cd-panel");
    var cdPanelHeader = $(".cd-panel__header");
    if (!sidepanel.hasClass("fullscreen") && fullscreen) {
      sidepanel.addClass("fullscreen");
      sidepanel.css('height', '100vh');
      sidepanel.css('width', '100vw');
      sidepanel.css("top", "0");
      // Set z-index much lower than dialog (20000) to ensure dialogs appear on top
      sidepanel.css('z-index', '100');
      sidepanel[0].style.setProperty('z-index', '100', 'important');
      if (cdPanel.length) {
        cdPanel.css('z-index', '100');
        cdPanel[0].style.setProperty('z-index', '100', 'important');
      }
      if (cdPanelHeader.length) {
        cdPanelHeader.css('z-index', '101');
        cdPanelHeader[0].style.setProperty('z-index', '101', 'important');
      }
      $("#prompt-panel-main").css('height', "calc(100% - 45px)");
      $("#prompt-panel-main-process-info-code").css('height', '100%');
      $("#prompt-panel-main-process-info-history").css('height', '100%');
      GW.process.sidepanel.leftDock();
      this.isfullscreen = true;
    } else if (sidepanel.hasClass("fullscreen") && !fullscreen){
      sidepanel.removeClass("fullscreen");
      sidepanel.css("top", "52px");
      sidepanel.css('height', '100%');
      sidepanel.css('width', '40%');
      // Reset z-index when exiting fullscreen
      sidepanel.css('z-index', '');
      if (cdPanel.length) {
        cdPanel.css('z-index', '');
      }
      if (cdPanelHeader.length) {
        cdPanelHeader.css('z-index', '');
      }
      $("#prompt-panel-main").css('height', "calc(100% - 95px)");
      $("#prompt-panel-main-process-info-code").css('height', '100%');
      $("#prompt-panel-main-process-info-history").css('height', '100%');
      GW.process.sidepanel.bottomDock();
      this.isfullscreen = false;
    }
  },

  switchSidePanelFullScreen: function () {
    var sidepanel = $(".cd-panel__container");
    var cdPanel = $(".cd-panel");
    var cdPanelHeader = $(".cd-panel__header");
    if (!sidepanel.hasClass("fullscreen")) {
      sidepanel.addClass("fullscreen");
      sidepanel.css('height', '100vh');
      sidepanel.css('width', '100vw');
      sidepanel.css("top", "0");
      // Set z-index much lower than dialog (20000) to ensure dialogs appear on top
      sidepanel.css('z-index', '100');
      sidepanel[0].style.setProperty('z-index', '100', 'important');
      if (cdPanel.length) {
        cdPanel.css('z-index', '100');
        cdPanel[0].style.setProperty('z-index', '100', 'important');
      }
      if (cdPanelHeader.length) {
        cdPanelHeader.css('z-index', '101');
        cdPanelHeader[0].style.setProperty('z-index', '101', 'important');
      }
      $("#prompt-panel-main").css('height', "calc(100% - 45px)");
      $("#prompt-panel-main-process-info-code").css('height', '100%');
      $("#prompt-panel-main-process-info-history").css('height', '100%');
      GW.process.sidepanel.leftDock();
      this.isfullscreen = true;
    } else {
      sidepanel.removeClass("fullscreen");
      sidepanel.css("top", "52px");
      sidepanel.css('height', '100%');
      sidepanel.css('width', '40%');
      // Reset z-index when exiting fullscreen
      sidepanel.css('z-index', '');
      if (cdPanel.length) {
        cdPanel.css('z-index', '');
      }
      if (cdPanelHeader.length) {
        cdPanelHeader.css('z-index', '');
      }
      $("#prompt-panel-main").css('height', "calc(100% - 95px)");
      $("#prompt-panel-main-process-info-code").css('height', '100%');
      $("#prompt-panel-main-process-info-history").css('height', '100%');
      GW.process.sidepanel.bottomDock();
      this.isfullscreen = false;
    }
  },

  switchFullScreen: function () {
    this.switchFullScreenUtil(
      "#editor-history-tab-panel",
      "#main-process-info-code",
      "#main-process-info-history",
    );
  },

  switchFullScreenUtil: function (
    editor_history_tab_panel_id,
    main_process_info_code_id,
    main_process_info_history_id,
  ) {
    var editorDiv = $(editor_history_tab_panel_id);
    var subtabCodeDiv = $(main_process_info_code_id);
    var subtabHistoryDiv = $(main_process_info_history_id);
    if (!editorDiv.hasClass("fullscreen")) {
      this.beforeFullscreen = {
        height: editorDiv.height(),
        width: editorDiv.width(),
      };
      editorDiv.addClass("fullscreen");
      editorDiv.height("100vh");
      // Use 100% instead of 100vw to respect parent container width
      // This prevents the panel from exceeding its parent container
      editorDiv.css("width", "100%");
      editorDiv.css("max-width", "100%");
      editorDiv.css("box-sizing", "border-box");
      // Set z-index much lower than dialog (20000) to ensure dialogs appear on top
      editorDiv.css('z-index', '100');
      if (editorDiv[0]) {
        editorDiv[0].style.setProperty('z-index', '100', 'important');
      }
      // Also set z-index on child elements to ensure they don't block dialogs
      subtabCodeDiv.css('z-index', '100');
      subtabHistoryDiv.css('z-index', '100');
      if (subtabCodeDiv[0]) {
        subtabCodeDiv[0].style.setProperty('z-index', '100', 'important');
      }
      if (subtabHistoryDiv[0]) {
        subtabHistoryDiv[0].style.setProperty('z-index', '100', 'important');
      }
      subtabCodeDiv.height("calc(100%)");
      subtabHistoryDiv.height("calc(100%)");
      this.isfullscreen = true;
    } else {
      editorDiv.removeClass("fullscreen");
      editorDiv.height(this.beforeFullscreen.height);
      editorDiv.width(this.beforeFullscreen.width);
      // Ensure width is reset to 100% if it was set to 100vw
      editorDiv.css("width", "100%");
      editorDiv.css("max-width", "100%");
      editorDiv.css("box-sizing", "border-box");
      // Reset z-index when exiting fullscreen
      editorDiv.css('z-index', '');
      subtabCodeDiv.css('z-index', '');
      subtabHistoryDiv.css('z-index', '');
      subtabCodeDiv.height("calc(100% - 150px)");
      subtabHistoryDiv.height("calc(100% - 150px)");
      this.isfullscreen = false;
    }
  },
};
