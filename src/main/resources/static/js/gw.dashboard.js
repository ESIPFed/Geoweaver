/**
 * 
 * Dashboard tab
 * 
 */

GW.board = {

    real_time_status_chart: null,
    time_cost_chart: null,
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

        $.ajax({
				
            url: "dashboard",
            
            method: "POST",
            
            data: ""
            
        }).done(function(msg){
            
            if(!msg.length){
                
                alert("no history found");
                
                return;
                
            }
            
            msg = $.parseJSON(msg);
        }).fail(function(jxr, status){
				
            console.error(status);
            
        });;

    },

    renderRealTimeStatusChart: function(){

        
        var ctx = document.getElementById('real_time_status_canvas').getContext('2d');
        this.real_time_status_chart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Red', 'Blue', 'Yellow', 'Green', 'Purple', 'Orange'],
                datasets: [{
                    label: '# of Votes',
                    data: [12, 19, 3, 5, 2, 3],
                    backgroundColor: [
                        'rgba(255, 99, 132, 0.2)',
                        'rgba(54, 162, 235, 0.2)',
                        'rgba(255, 206, 86, 0.2)',
                        'rgba(75, 192, 192, 0.2)',
                        'rgba(153, 102, 255, 0.2)',
                        'rgba(255, 159, 64, 0.2)'
                    ],
                    borderColor: [
                        'rgba(255, 99, 132, 1)',
                        'rgba(54, 162, 235, 1)',
                        'rgba(255, 206, 86, 1)',
                        'rgba(75, 192, 192, 1)',
                        'rgba(153, 102, 255, 1)',
                        'rgba(255, 159, 64, 1)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });


    },

    renderTimeCostChart: function(){

        var ctx = document.getElementById('time_cost_canvas').getContext('2d');
        this.time_cost_chart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: [
                  'Red',
                  'Blue',
                  'Yellow'
                ],
                datasets: [{
                  label: 'My First Dataset',
                  data: [300, 50, 100],
                  backgroundColor: [
                    'rgb(255, 99, 132)',
                    'rgb(54, 162, 235)',
                    'rgb(255, 205, 86)'
                  ],
                  hoverOffset: 4
                }]
              }
        });

    },

    /**
     * Show all the charts and tables (empty)
     */
    display: function(){

        var cont = "";

        //list the number of hosts, processes, and workflows
        cont += '<div class="row" style="margin:0;">'+

        
        '   <div class="col-md-4" style="padding:5px;">'+
        '       <div class="panel panel-info">'+
        '           <div class="panel-heading">'+
        '               <h3 class="panel-title">Host</h3>'+
        '           </div>'+
        '           <div class="panel-body" style="height:200px;"><ul>'+
        '               <li><p class="panel-text"><span id="host_all_num"></span> hosts</p></li>'+
        '               <li><p class="panel-text"><span id="host_ssh_num"></span> SSH hosts</p></li>'+
        '               <li><p class="panel-text"><span id="host_notebook_num"></span> Jupyter Notebook hosts</p></li>'+
        '               <li><p class="panel-text"><span id="host_hub_num"></span> JupyterHub hosts</p></li>'+
        '               <li><p class="panel-text"><span id="host_lab_num"></span> JupyterLab hosts</p></li>'+
        '               <li><p class="panel-text"><span id="host_gee_num"></span> Earth Engine hosts</p></li>'+
        '           </ul></div>'+
        '       </div>'+
        '   </div>'+
        
        '   <div class="col-md-4" style="padding:5px;">'+
        '       <div class="panel panel-success">'+
        '           <div class="panel-heading">'+
        '               <h3 class="panel-title">Process</h3>'+
        '           </div>'+
        '           <div class="panel-body" style="height:200px;"><ul>'+
        '               <li><p class="panel-text"><span id="process_all_num"></span> processes</p></li>'+
        '               <li><p class="panel-text"><span id="process_shell_num"></span> Shell processes</p></li>'+
        '               <li><p class="panel-text"><span id="process_notebook_num"></span> Jupyter notebooks</p></li>'+
        '               <li><p class="panel-text"><span id="process_builtin_num"></span> builtin processes</p></li>'+
        '               <li><p class="panel-text"><span id="process_python_num"></span> python processes</p></li>'+
        '           </ul></div>'+
        '       </div>'+
        '   </div>'+


    //     <div class="panel-heading">
    //     <h3 class="panel-title">Panel title</h3>
    //   </div>
    //   <div class="panel-body">
    //     Panel content
    //   </div>
    
        
        '   <div class="col-md-4" style="padding:5px;">'+
        '       <div class="panel panel-warning">'+
        '           <div class="panel-heading">'+
        '               <h3 class="panel-title">Workflows</h3>'+
        '           </div>'+
        '           <div class="panel-body" style="height:200px;"><ul>'+
        '               <li><p class="panel-text"><span id="process_all_num"></span> workflows</p></li>'+
        '               <li><p class="panel-text"><span id="process_shell_num"></span> Shell processes</p></li>'+
        '               <li><p class="panel-text"><span id="process_notebook_num"></span> Jupyter notebooks</p></li>'+
        '               <li><p class="panel-text"><span id="process_builtin_num"></span> builtin processes</p></li>'+
        '               <li><p class="panel-text"><span id="process_python_num"></span> python processes</p></li>'+
        '           </ul></div>'+
        '       </div>'+
        '   </div>'+
        
        '</div>';

        //list the statistics of each categories of hosts, processes, and workflows
        cont += '<div class="row" style="margin:0;">'+
        '   <div class="col-md-8" style="padding:5px;">'+
        '       <div class="panel panel-primary">'+
        '           <div class="panel-heading">'+
        '               <h3 class="panel-title">Real Time Status</h3>'+
        '           </div>'+
        '           <div class="panel-body"  style="height:300px;">'+
        '               <canvas id="real_time_status_canvas" height="90"></canvas>'+
        '           </div>'+
        '       </div>'+
        '   </div>'+
        '   <div class="col-md-4" style="padding:5px;">'+
        '       <div class="panel panel-primary">'+
        '           <div class="panel-heading">'+
        '               <h3 class="panel-title">Time Cost</h3>'+
        '           </div>'+
        '           <div class="panel-body"  style="height:300px;">'+
        '               <canvas id="time_cost_canvas" ></canvas>'+
        '           </div>'+
        '       </div>'+
        '   </div>'+
        '</div>';

        //list the status of processes and workflows
        cont += "";

        //show the average time cost and history recorded
        

        $("#main-dashboard-content").html(cont);

        this.renderRealTimeStatusChart();

        this.renderTimeCostChart();

    }

}