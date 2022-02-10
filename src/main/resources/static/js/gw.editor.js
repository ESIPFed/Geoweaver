

GW.editor = {

    beforeFullscreen: {},

    switchFullScreen: function(){

        this.isfullscreen = !this.isfullscreen; //switch

        var editorDiv = $('#editor-history-tab-panel');
        var subtabCodeDiv = $('#main-process-info-code');
        var subtabHistoryDiv = $('#main-process-info-history');
        if (!editorDiv.hasClass('fullscreen')) {
            this.beforeFullscreen = { height: editorDiv.height(), width: editorDiv.width() }
            editorDiv.addClass('fullscreen');
            editorDiv.height('100vh');
            editorDiv.width('100vw');
            // editor.refresh();
            subtabCodeDiv.height('calc(100% - 40px)');
            subtabHistoryDiv.height('calc(100% - 40px)');

        }
        else {
            editorDiv.removeClass('fullscreen');
            editorDiv.height(this.beforeFullscreen.height);
            editorDiv.width(this.beforeFullscreen.width);
            subtabCodeDiv.height('calc(100% - 130px)');
            subtabHistoryDiv.height('calc(100% - 130px)');
            // editor.refresh();
        }

    }

}
