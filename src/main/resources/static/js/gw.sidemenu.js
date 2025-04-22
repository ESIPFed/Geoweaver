/**
 * Geoweaver sidemenu functionality
 * Handles the caret icon rotation when submenus are collapsed or expanded
 */

GW.sidemenu = {
    
    init: function() {
        // Add event listeners to all collapsible menu items
        this.setupCaretToggle();
    },
    
    /**
     * Sets up event listeners for all collapsible menu items to toggle caret icons
     * between up and down states based on collapse/expand actions
     */
    setupCaretToggle: function() {
        // For each collapsible menu section
        $('.collapse').on('show.bs.collapse', function() {
            // Find the caret icon in the closest parent li and change it to up
            $(this).closest('li').find('> a .fas[class*="fa-caret"]').removeClass('fa-caret-down').addClass('fa-caret-up');
        });
        
        $('.collapse').on('hide.bs.collapse', function() {
            // Find the caret icon in the closest parent li and change it to down
            $(this).closest('li').find('> a .fas[class*="fa-caret"]').removeClass('fa-caret-up').addClass('fa-caret-down');
        });
        
        // Initialize all caret icons to the correct state based on current collapse state
        $('.collapse').each(function() {
            var $caret = $(this).closest('li').find('> a .fas[class*="fa-caret"]');
            if ($(this).hasClass('in') || $(this).hasClass('show')) {
                // Menu is expanded
                $caret.removeClass('fa-caret-down').addClass('fa-caret-up');
            } else {
                // Menu is collapsed
                $caret.removeClass('fa-caret-up').addClass('fa-caret-down');
            }
        });
    }
};

// Initialize when document is ready
$(document).ready(function() {
    GW.sidemenu.init();
});