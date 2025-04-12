/**
 * Geoweaver Workspace Icons
 * Contains SVG icon definitions for process status indicators
 */

GW.workspaceicons = {
    /**
     * SVG path data for a checkmark icon
     */
    checkmark: "M9,16.2L4.8,12l-1.4,1.4L9,19L21,7l-1.4-1.4L9,16.2z",
    
    /**
     * SVG path data for a cross/X icon
     */
    cross: "M19,6.4L17.6,5L12,10.6L6.4,5L5,6.4L10.6,12L5,17.6L6.4,19L12,13.4L17.6,19L19,17.6L13.4,12L19,6.4z",
    
    /**
     * Creates a loading spinner animation element
     * @returns {String} SVG markup for a loading spinner
     */
    getLoadingSpinner: function() {
        return '<g class="loading-spinner"><circle cx="0" cy="-35" r="10" fill="none" stroke="#4285f4" stroke-width="3"><animateTransform attributeName="transform" type="rotate" from="0 0 -35" to="360 0 -35" dur="1s" repeatCount="indefinite"/></circle></g>';
    },
    
    /**
     * Creates a checkmark icon element
     * @returns {String} SVG markup for a checkmark
     */
    getCheckmark: function() {
        return '<g class="status-icon status-done"><circle cx="0" cy="-35" r="12" fill="#34a853"></circle><path d="' + this.checkmark + '" transform="scale(0.7) translate(-12, -62)" fill="white"></path></g>';
    },
    
    /**
     * Creates a cross/X icon element
     * @returns {String} SVG markup for a cross
     */
    getCross: function() {
        return '<g class="status-icon status-failed"><circle cx="0" cy="-35" r="12" fill="#ea4335"></circle><path d="' + this.cross + '" transform="scale(0.7) translate(-12, -62)" fill="white"></path></g>';
    },
    
    /**
     * Adds the appropriate status icon to a node based on its status
     * @param {Object} node - The D3 node selection to add the icon to
     * @param {String} status - The status of the process ("Running", "Done", "Failed")
     */
    addStatusIcon: function(node, status) {
        // First remove any existing status icons
        node.selectAll(".status-icon, .loading-spinner").remove();
        
        // Add the appropriate icon based on status
        if (status === "Running") {
            node.append(function() {
                return new DOMParser().parseFromString(GW.workspaceicons.getLoadingSpinner(), "image/svg+xml").documentElement.firstChild;
            });
        } else if (status === "Done") {
            node.append(function() {
                return new DOMParser().parseFromString(GW.workspaceicons.getCheckmark(), "image/svg+xml").documentElement.firstChild;
            });
        } else if (status === "Failed") {
            node.append(function() {
                return new DOMParser().parseFromString(GW.workspaceicons.getCross(), "image/svg+xml").documentElement.firstChild;
            });
        }
    }
};