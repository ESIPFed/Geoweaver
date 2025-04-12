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
        // Remove any existing icons
        node.selectAll(".status-icon, .loading-spinner").remove();
        
        const iconOffset = 16; // Offset from center (adjust based on radius)
        
        if (status === "Running") {
            // Add a spinning circle
            node.append("circle")
                .attr("class", "loading-spinner status-icon")
                .attr("r", 5)
                .attr("cx", iconOffset)
                .attr("cy", -iconOffset)
                .style("stroke", "#007bff")
                .style("stroke-width", 2)
                .style("fill", "none")
                .style("stroke-dasharray", "10,4")
                .style("animation", "spin 1s linear infinite");
            
        } else if (status === "Done") {
            // Add a checkmark path
            node.append("path")
                .attr("class", "status-icon")
                .attr("d", "M -6 0 L -2 4 L 6 -4")   // Simple checkmark shape
                .attr("transform", `translate(${iconOffset}, ${-iconOffset})`)
                .style("stroke", "green")
                .style("stroke-width", 2)
                .style("fill", "none");
        
        } else if (status === "Failed") {
            // Add a cross (X) symbol
            node.append("g")
            .attr("class", "status-icon")
            .attr("transform", `translate(${iconOffset}, ${-iconOffset})`)
            .call(g => {
                g.append("line")
                .attr("x1", -5).attr("y1", -5)
                .attr("x2", 5).attr("y2", 5)
                .style("stroke", "red")
                .style("stroke-width", 2);
                g.append("line")
                .attr("x1", -5).attr("y1", 5)
                .attr("x2", 5).attr("y2", -5)
                .style("stroke", "red")
                .style("stroke-width", 2);
            });
        }
    }
      
};