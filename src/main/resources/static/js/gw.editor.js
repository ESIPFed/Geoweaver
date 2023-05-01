

GW.editor = {

    beforeFullscreen: {},

    isfullscreen: false,

    switchFullScreen: function(){

        this.switchFullScreenUtil('#editor-history-tab-panel', '#main-process-info-code', '#main-process-info-history')

    },

    switchFullScreenUtil: function(editor_history_tab_panel_id, main_process_info_code_id, main_process_info_history_id){

        var editorDiv = $(editor_history_tab_panel_id);
        var subtabCodeDiv = $(main_process_info_code_id);
        var subtabHistoryDiv = $(main_process_info_history_id);
        if (!editorDiv.hasClass('fullscreen')) {
            this.beforeFullscreen = { height: editorDiv.height(), width: editorDiv.width() }
            editorDiv.addClass('fullscreen');
            editorDiv.height('100vh');
            editorDiv.width('100vw');
            // editor.refresh();
            subtabCodeDiv.height('calc(100% - 40px)');
            subtabHistoryDiv.height('calc(100% - 40px)');
            this.isfullscreen = true;
        }
        else {
            editorDiv.removeClass('fullscreen');
            editorDiv.height(this.beforeFullscreen.height);
            editorDiv.width(this.beforeFullscreen.width);
            subtabCodeDiv.height('calc(100% - 150px)');
            subtabHistoryDiv.height('calc(100% - 150px)');
            // editor.refresh();
            this.isfullscreen = false;
        }

    }


}