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
                
                console.error("no dashboard info found");
                
                return;
                
            }
            
            msg = $.parseJSON(msg);

            console.log(msg);

            // { "process_num":12,"history_num":99,"host_num":1,"workflow_num":4,"environment_num":39,"process_shell_num":3,"process_notebook_num":0,"process_python_num":4,"process_builtin_num":3,"host_ssh_num":1,"host_jupyter_num":0,"host_jupyterlab_num":0,"host_jupyterhub_num":0,"host_gee_num":0,"running_process_num":2,"failed_process_num":15,"success_process_num":47,"running_workflow_num":0,"failed_workflow_num":0,"success_workflow_num":0}

            $("#host_num").html(msg.host_num)

            $("#host_ssh_num").html(msg.host_ssh_num)

            $("#host_jupyter_num").html(msg.host_jupyter_num)

            $("#host_jupyterlab_num").html(msg.host_jupyterlab_num)

            $("#host_jupyterhub_num").html(msg.host_jupyterhub_num)

            $("#host_gee_num").html(msg.host_gee_num)

            $("#process_num").html(msg.process_num)

            $("#process_shell_num").html(msg.process_shell_num)

            $("#process_notebook_num").html(msg.process_notebook_num)

            $("#process_python_num").html(msg.process_python_num)

            $("#process_builtin_num").html(msg.process_builtin_num)

            $("#workflow_num").html(msg.workflow_num);

            $("#running_workflow_num").html(msg.running_workflow_num)

            $("#failed_workflow_num").html(msg.failed_workflow_num)

            $("#success_workflow_num").html(msg.success_workflow_num)

            GW.board.real_time_status_chart.data.datasets[0].data = [
                msg.running_process_num, msg.failed_process_num, msg.success_process_num,
                msg.running_workflow_num, msg.failed_workflow_num, msg.success_workflow_num
            ]

            GW.board.real_time_status_chart.update();

            var time_costs = msg.time_costs.map(x=>+x);

            time_costs = GW.board.removeMinusOne(time_costs);

            var maxmin = GW.board.findMaxMin(time_costs);

            var max_value = maxmin[0].toFixed();

            var min_value = maxmin[1].toFixed();

            var first_splitter = (max_value-min_value)/3 + min_value; first_splitter = Number(first_splitter).toFixed();

            var second_splitter = (max_value-min_value)*2/3 + min_value;second_splitter = Number(second_splitter).toFixed();

            GW.board.time_cost_chart.data.labels = [min_value + " - " + first_splitter + " ms", first_splitter + 
                                                    " - " + second_splitter + " ms", second_splitter + " - " + max_value + " ms"]

            GW.board.time_cost_chart.data.datasets[0].data = GW.board.calculateFrequency(time_costs, first_splitter, second_splitter);

            // GW.board.time_cost_chart.title.text = "Time Cost (unit: milliseconds)";

            GW.board.time_cost_chart.update();

        }).fail(function(jxr, status){
				
            console.error(status);
            
        });;

    },

    calculateFrequency: function(thearr, first_splitter, second_splitter){

        var lvl1 = 0;
        var lvl2 = 0; 
        var lvl3 = 0;

        for(var i=0;i<thearr.length;i+=1){

            if(thearr[i]<=first_splitter){

                lvl1 += 1

            }else if(thearr[i]>first_splitter && thearr[i]<=second_splitter){

                lvl2 += 1

            }else if(thearr[i]>second_splitter){

                lvl3 += 1

            }

        }

        return [lvl1, lvl2, lvl3]

    },

    findMaxMin: function(thearr){

        var maxv = thearr[0], minv = thearr[0];

        for(var i=0;i<thearr.length;i+=1){

            if(maxv < thearr[i]) maxv = thearr[i];

            if(minv > thearr[i]) minv = thearr[i];

        }

        return [maxv, minv];

    },

    removeMinusOne: function(thearr){

        var newarr = []

        for (var i = 0; i < thearr.length; i++) {

            if (Number(thearr[i]) != -1) {
                newarr.push(Number(thearr[i]))
            }
        }
        

        return newarr;

    },

    renderRealTimeStatusChart: function(){

        var ctx = document.getElementById('real_time_status_canvas').getContext('2d');
        this.real_time_status_chart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Running Process', 'Failed Process', 'Done Process', 'Running Workflow', 'Failed Workflow', 'Done Workflow'],
                datasets: [{
                    label: '# of Processes/Workflows',
                    data: [0, 0, 0, 0, 0, 0],
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
        '               <li><p class="panel-text"><span id="host_num"></span> hosts</p></li>'+
        '               <li><p class="panel-text"><span id="host_ssh_num"></span> SSH hosts</p></li>'+
        '               <li><p class="panel-text"><span id="host_jupyter_num"></span> Jupyter Notebook hosts</p></li>'+
        '               <li><p class="panel-text"><span id="host_jupyterhub_num"></span> JupyterHub hosts</p></li>'+
        '               <li><p class="panel-text"><span id="host_jupyterlab_num"></span> JupyterLab hosts</p></li>'+
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
        '               <li><p class="panel-text"><span id="process_num"></span> processes</p></li>'+
        '               <li><p class="panel-text"><span id="process_shell_num"></span> Shell processes</p></li>'+
        '               <li><p class="panel-text"><span id="process_notebook_num"></span> Jupyter notebooks</p></li>'+
        '               <li><p class="panel-text"><span id="process_builtin_num"></span> builtin processes</p></li>'+
        '               <li><p class="panel-text"><span id="process_python_num"></span> python processes</p></li>'+
        '           </ul></div>'+
        '       </div>'+
        '   </div>'+
        '   <div class="col-md-4" style="padding:5px;">'+
        '       <div class="panel panel-warning">'+
        '           <div class="panel-heading">'+
        '               <h3 class="panel-title">Workflows</h3>'+
        '           </div>'+
        '           <div class="panel-body" style="height:200px;"><ul>'+
        '               <li><p class="panel-text"><span id="workflow_num"></span> workflows</p></li>'+
        '               <li><p class="panel-text"><span id="running_workflow_num"></span> pending workflow runs</p></li>'+
        '               <li><p class="panel-text"><span id="failed_workflow_num"></span> failed workflow runs</p></li>'+
        '               <li><p class="panel-text"><span id="success_workflow_num"></span> successful workflow runs</p></li>'+
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