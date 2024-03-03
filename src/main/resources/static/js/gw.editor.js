

GW.editor = {

    beforeFullscreen: {},

    isfullscreen: false,

    switchSidePanelFullScreen: function(){

        var sidepanel = $('.cd-panel__container')  //.cd-panel__container
        if (!sidepanel.hasClass('fullscreen')) {
            // this.beforeFullscreen = { height: sidepanel.height(), width: sidepanel.width() }
            sidepanel.addClass('fullscreen');
            sidepanel.height('100vh');
            sidepanel.width('100vw');
            sidepanel.css('top', '0');
            $('#prompt-panel-main').height('calc(100% - 55px)')
            $('#prompt-panel-main-process-info-code').height('100%')
            $('#prompt-panel-main-process-info-history').height('100%')
            this.isfullscreen = true;
        }else {
            sidepanel.removeClass('fullscreen');
            // sidepanel.height(this.beforeFullscreen.height);
            // sidepanel.width(this.beforeFullscreen.width);
            sidepanel.css('top', '52px');
            // subtabCodeDiv.height('calc(100% - 150px)');
            // subtabHistoryDiv.height('calc(100% - 150px)');
            sidepanel.height('100%')
            sidepanel.width('40%')
            $('#prompt-panel-main').height('calc(100% - 105px)')
            $('#prompt-panel-main-process-info-code').height('100%')
            $('#prompt-panel-main-process-info-history').height('100%')
            // editor.refresh();
            this.isfullscreen = false;
        }

    },

    switchFullScreen: function(){

        this.switchFullScreenUtil('#editor-history-tab-panel', 
            '#main-process-info-code', 
            '#main-process-info-history')

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
            this.isfullscreen = false;
        }

    }


}