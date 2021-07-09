/**
 * 
 * Dashboard tab
 * 
 */

GW.board = {

    /**
     * Initialize the tab panel, this function should be only called when app is loaded
     * after that, call refresh() directly.
     */
    init: function(){

        this.display();

        this.refresh();

    },

    /**
     * Add values to those charts and tables
     */
    refresh: function(){



    },

    /**
     * Show all the charts and tables (empty)
     */
    display: function(){

        var cont = "<h2>Dashboard</h2>";

        //list the number of hosts, processes, and workflows
        cont += "";

        //list the statistics of each categories of hosts, processes, and workflows
        cont += "";

        //list the status of processes and workflows
        cont += "";

        //show the average time cost and history recorded


        //


        $("#main-dashboard-content").html(cont);

    }

}