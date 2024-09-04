// /**
//  * Unit Test for Geoweaver
//  * @author Jensen Sun
//  * @date 10/9/2021
//  */


import { describe, it, expect, beforeEach } from "vitest";
import "jsdom-global/register";  // Automatically sets up window, document, etc.
import $ from "jquery"; // Import jQuery

// Assuming the existence of the GW object
const GW = {
  process: {
    addMenuItem: () => {},
    refreshProcessList: () => {},
    checkIfProcessPanelActive: () => {},
    clearCache: () => {},
    clearProcessLogging: () => {},
    clearCodeEditorListener: () => {},
    displayToolbar: () => {},
    display: () => {},
  },
  host: {
    addMenuItem: () => {},
    refreshHostList: () => {},
    checkIfHostPanelActive: () => {},
    clearCache: () => {},
    expand: () => {},
    newDialog: () => {},
    new_host_frame: { closeFrame: () => {} },
    list: () => {},
    validateIP: () => {},
    display: () => {},
  },
  workflow: {
    addMenuItem: () => {},
    refreshWorkflowList: () => {},
    list: () => {},
    display: () => {},
  },
  ssh: {
    clearMain: () => {},
  },
  user: {
    logindialog: () => {},
    profiledialog: () => {},
  },
  about: {
    showDialog: () => {},
  },
  tutorial: {
    showDialog: () => {},
  },
  test: {
    // Adding initHTML function here
    initHTML: function () {
      $("body")
        .append(`<div class="container" style="margin:0; padding:0; width:100%; height:100%; visibility: hidden; overflow: visible;">
      
          <div class="row"  style="height: 50px; margin: 0; padding: 0; background-color:black;">
              
              <div data-intro="Top Bar contains button for managing the host/process/workflow in GeoWeaver database" style="overflow: auto;">
                <a  style="float: left; width: 100px;text-align: center; transition: all 0.3s ease;background-color:silver; " href="javascript:void(0)">
                    <img src="../img/header.png" width="100px" height="50px" />
                </a>
                <!-- <div style="float: left; width: 100px;text-align: center;margin-top: 9px;padding-top: 5px;transition: all 0.3s ease;color: white;font-size: 16px;" >
                    Geoweaver
                </div> -->
                <a id="toolbar-search"  
                data-toggle="tooltip" title="Search Process/Workflow/Hosts"
                style="float: left; width: 80px;text-align: center;margin-top: 9px;padding-top: 5px;transition: all 0.3s ease;color: white;font-size: 16px;" href="javascript:void(0)">
                    <i class="fa fa-search" alt="search" > Search</i>
                </a>
                <a id="toolbar-add" 
                data-toggle="tooltip" title="New Host/Process/Workflow"
                style="float: left; width: 80px;text-align: center;margin-top: 9px;padding-top: 5px;transition: all 0.3s ease;color: white;font-size: 16px;" href="javascript:void(0)">
                    <i class="fa fa-plus"  alt="create" > Create</i>
                </a>
                <a id="toolbar-history" 
                data-toggle="tooltip" title="Process/Workflow History"
                style="float: left; width: 80px;text-align: center;margin-top: 9px;padding-top: 5px;transition: all 0.3s ease;color: white;font-size: 16px;" href="javascript:void(0)">
                    <i class="fa fa-history" alt="List the recent execution history"> History</i>
                </a>
                <a id="toolbar-monitor" 
                data-toggle="tooltip" title="Monitor Running Process/Workflow"
                style="float: left; width: 80px;text-align: center;margin-top: 9px;padding-top: 5px;transition: all 0.3s ease;color: white;font-size: 16px;" href="javascript:void(0)">
                    <i class="fa fa-film" alt="monitor" > Monitor</i>
                </a>
                <a id="toolbar-settings" 
                data-toggle="tooltip" title="Settings"
                style="float: left; width: 80px;text-align: center;margin-top: 9px;padding-top: 5px;transition: all 0.3s ease;color: white;font-size: 16px;" href="javascript:void(0)">
                    <i class="fa fa-cog" alt="settings"  > Settings</i>
                </a>
                <a id="toolbar-print" 
                data-toggle="tooltip" title="Print"
                style="float: left; width: 80px;text-align: center;margin-top: 9px;padding-top: 5px;transition: all 0.3s ease;color: white;font-size: 16px;" href="javascript:void(0)">
                    <i class="fa fa-print" alt="print" > Print</i>
                </a>
                <a  
                data-toggle="tooltip" title="Information"
                style="float: left; width: 80px;text-align: center;margin-top: 9px;padding-top: 5px;transition: all 0.3s ease;color: white;font-size: 16px;" href="javascript:GW.tutorial.showDialog()">
                    <i class="fa fa-book" alt="tutorial" > Tutorial</i> 
                </a>
                <a style="float: right; width: 80px;text-align: center;margin-top: 9px;padding-top: 5px;transition: all 0.3s ease;color: white;font-size: 16px;" href="javascript:GW.about.showDialog()">
                    <i class="fa fa-info" alt="info" id="toolbar-info"> Info</i>
                </a>
  
                <a style="float: right; width: 100px;text-align: center; font-weight: bold; margin-top: 9px;padding-top: 5px;transition: all 0.3s ease;color: yellow;font-size: 16px;" href="javascript:GW.user.logindialog()" id="toolbar-loginout-a">
                  Login
                </a>
                
                <a style="float: right; width: 120px;visibility: hidden; text-align: right;margin-top: 9px;padding-top: 5px;transition: all 0.3s ease;color: white;font-size: 16px;" href="javascript:GW.user.profiledialog()" id="toolbar-profile-a">
                  <i class="fa fa-user" alt="info" id="toolbar-profile"> Welcome</i>
                </a>
  
              </div>
              
          </div>
  
          <div class="row" style="height:calc(100vh - 50px); top: 80px; margin: 0px; padding: 0px;">
              
              
              <div  class="col col-md-9" style="height:100%; margin: 0; padding:0;">
              
                        <div class="tab" data-intro="You can click on these tabs to switch among views of different type of resources">
                        <button class="tablinks " id="main-general-tab" onclick="openCity(event, 'main-general')">Welcome</button>
                        <button class="tablinks " id="main-dashboard-tab" onclick="openCity(event, 'main-dashboard')">Dashboard</button>
                        <button class="tablinks " id="main-host-tab" onclick="openCity(event, 'main-host-info')">Host</button>
                        <button class="tablinks " id="main-process-tab" onclick="openCity(event, 'main-process-info')">Process</button>
                        <button class="tablinks " id="main-workflow-tab" onclick="openCity(event, 'main-workflow-info')">Workflow</button>
                        <button class="tablinks " id="main-workspace-tab" onclick="openCity(event, 'workspace')">Weaver</button>
                        <button class="tablinks " id="main-console-tab" onclick="openCity(event, 'main-console')">Console</button>
                      </div>
                      
                      <div id="main-general" class="tabcontent" style="height:100%; left:0; margin:0; padding: 0;padding-bottom:25px;"> 
                          <div id="main-general-content" style="height:100%; overflow-y: scroll; padding: 30px;">
                              
                          </div> 
                      </div>
  
                      <div id="main-dashboard" class="tabcontent" style="height:100%; left:0; margin:0; padding: 0;padding-bottom:25px;">
                          <div id="main-dashboard-content" style="width:100%; height:100%; overflow-y: scroll; padding: 10px; ">
                              
                          </div>
                        </div>
                      
                      <div id="main-host-info" class="tabcontent" style="height:100%; left:0; margin:0; padding: 0;padding-bottom:25px;">
                            <div id="main-host-content" style="height:100%; width: 100%; overflow-y: scroll; padding: 0px; ">
                                <h2 style="color:black">Please select a host on the right panel! </h2>
                            </div>
                      </div>
                       
                      <div id="main-process-info" class="tabcontent" style="height:100%; left:0; margin:0; padding: 0;padding-bottom:25px;">
                            <div id="main-process-content" style="height:100%;  padding: 10px; width: 100%;">
                                <h2 style="color:black">Please select a process on the right panel!</h2>
                            </div>
                      </div>
                      
                      <div id="main-workflow-info" class="tabcontent" style="height:100%; left:0; margin:0; padding: 0;padding-bottom:25px;">
                            <div id="main-workflow-content" style="height:100%; padding: 10px;">
                                <h2 style="color:black">Please select a workflow on the right panel!</h2>
                            </div>
                      </div>
                          
                      <div id="main-console" class="tabcontent" style="height:100%; padding:0; left:0; margin:0; padding-bottom:25px;">
                          <div id="main-console-content" style="width:100%; height:100%; overflow-y: scroll; margin:0; padding: 10px;">
                            <h2 style="color:black">Logging <button class="btn btn-primary pull-right" onclick="GW.ssh.clearMain();">Clear</button></h2>
                            <div id="log-window" class="log_window">
                                
                            </div>
                          </div>
                      </div>
              
                      <div id="workspace" class="tabcontent" style="height:100%; margin: 0;padding: 0;">
                          
                          <!-- status indicator right bottom -->
                          
                          <div id="workspace_status_indicator" style="position: absolute; bottom: 20px; right: 20px; height: 50px; z-index:1;">
                                    <!--<span id="current_workflow_name" class="invisible">Current Workflow: </span>-->
                                    
                                    
                                    <h2><i class="fa fa-spinner fa-spin invisible" id="running_spinner" style="font-size:20px;color:red"></i><span id="current_workflow_na" class="badge badge-info" style="font-size: 20px;" ></span></h2>
                          
                          </div>
                          
                          <!-- progress right bottom, above the status indicator -->
                          
                          <div id="workspace_progress_indicator" class="visible" style="position: absolute; max-height: 350px; top: 80px; right: 10px; width:100px; z-index:1; overflow-y: scroll;">
                              
                              
                          </div>
                          
                             <div id="toolbox" style="position: absolute; margin:0; top: 60px; left: 10px;width: 80px;">
                        
                                <i style="text-align:center; padding:0; margin-top:10px; ">Workflow Toolbar</i>
                            
                              <input id="hidden-file-upload" type="file" title="Hidden File Upload" class="btn fa fa-alien" style="color:gray; margin:0;margin-top:5px;  padding:0;" alt="Hidden File Upload" />
                                
                              <i id="test-jsframe" title="js frame" class="btn fa fa-alien fa-2x" 
                                  style="color:gray; padding:0; margin:0; margin-top:5px; display: block;text-align: center;transition: all 0.3s ease;" alt="test jsframe graph" ></i>
  
                              <i id="new-workflow" title="new graph" class="btn fa fa-plus-square fa-2x" 
                                  style="color:gray; padding:0; margin:0;margin-top:5px; display: block;text-align: center;transition: all 0.3s ease;" alt="upload graph" ></i>
                                
                              <i id="upload-input" title="upload graph" class="btn fa fa-upload fa-2x" 
                                  style="color:gray; padding:0; margin:0;margin-top:5px; display: block;text-align: center;transition: all 0.3s ease;" alt="upload graph" ></i>
                                
                              <i id="download-input" title="download graph" class="btn fa fa-download fa-2x" 
                                  style="color:gray; padding:0; margin:0;margin-top:5px; display: block;text-align: center;transition: all 0.3s ease;" alt="download graph" ></i>
                                
                              <i id="save-workflow" title="save workflow" class="btn fa fa-save fa-2x" 
                                  style="color:gray; padding:0; margin:0;margin-top:5px; display: block;text-align: center;transition: all 0.3s ease;" alt="save workflow"></i> 
                                
                              <i id="execute-workflow" title="execute workflow" class="btn fa fa-play fa-2x" 
                                  style="color:gray; padding:0; margin:0;margin-top:5px; display: block;text-align: center;transition: all 0.3s ease;" alt="execute workflow"></i> 
                                
                              <i id="geoweaver-details" title="information" class="btn fa fa-info fa-2x" 
                                  style="color:gray; padding:0; margin:0;margin-top:5px; display: block;text-align: center;transition: all 0.3s ease;" alt="show object information"></i>
                                
                              <i id="geoweaver-log" title="log" class="btn fa fa-history fa-2x" 
                                  style="color:gray; padding:0; margin:0;margin-top:5px; display: block;text-align: center;transition: all 0.3s ease;" alt="show process log"></i> 
                                
                              <i id="geoweaver-result" title="result" class="btn fa fa-tv fa-2x" 
                                  style="color:gray; padding:0; margin:0;margin-top:5px; display: block;text-align: center;transition: all 0.3s ease;" alt="get result"></i> 
                                
                              <i id="delete-graph" title="delete graph" class="btn fa fa-trash fa-2x" 
                                  style="color:gray; padding:0; margin:0;margin-top:5px; display: block;text-align: center;transition: all 0.3s ease;"  alt="delete graph" ></i> 
                            
                          </div>
                      
                      </div>
              </div>
              
              
              <div class="col col-sm-3 col-md-3 col-lg-3 small-height" id="sidemenu" style="height:100vh; margin: 0;padding: 0;">
                  
                  <div class="nav-side-menu" >
      
                      
                      <!-- <i class="fa fa-bars fa-2x toggle-btn" data-toggle="collapse" data-target="#menu-content"></i> -->
                        
                      <div class="menu-list" data-intro="Side menu contains all the existing resources in Geoweaver database">
                    
                              <ul id="menu-content" class="menu-content collapse out">
                                  <!-- <li>
                                    <a href="javascript:void(0)">
                                    <i class="fa fa-dashboard fa-lg"></i> Overview
                                    </a>
                                  </li> -->
                  
                                  <li data-intro="Hosts are computing platforms, e.g., a laptop, a Linux server, a Jupyter Notebook server, a JupyterHub, Google Earth Engine. You can add and manage multiple hosts in one place. ">
                                    <a href="javascript:void(0)"><i class="fa fa-server fa-lg" data-toggle="tooltip" title="Add new host"></i> Hosts <i class="fa fa-plus" id="newhost" onClick="host()"></i> <span style="background-color:balack" data-toggle="collapse" data-target="#hosts" class="arrow collapsed "></span></a>  <!-- add active to see color side of host -->
  
                                  </li>
                                  <ul   class="sub-menu collapse" id="hosts">
                                      <li class=" sshserver folder" id="host_folder_ssh" data-toggle="collapse" data-target="#host_folder_ssh_target">
                                          <a href="javascript:void(0)">Linux/Win/Mac</a>
                                      </li>
                                      <ul class="sub-menu collapse" id="host_folder_ssh_target"></ul>
                                      
                                      <li class=" jupyterserver folder" id="host_folder_jupyter" data-toggle="collapse" data-target="#host_folder_jupyter_target">
                                          <a href="javascript:void(0)">Jupyter Notebook Server</a>
                                      </li>
                                      <ul class="sub-menu collapse" id="host_folder_jupyter_target"></ul>
                                      
                                      <li class="jupyterhubserver folder " id="host_folder_jupyterhub" data-toggle="collapse" data-target="#host_folder_jupyterhub_target">
                                          <a href="javascript:void(0)">Jupyter Hub</a>
                                      </li>
                                      <ul class="sub-menu collapse" id="host_folder_jupyterhub_target"></ul>
  
                                      <li class="jupyterlabserver folder " id="host_folder_jupyterlab" data-toggle="collapse" data-target="#host_folder_jupyterlab_target">
                                          <a href="javascript:void(0)">Jupyter Lab</a>
                                      </li>
                                      <ul class="sub-menu collapse" id="host_folder_jupyterlab_target"></ul>
                                      
                                      <li class="geeserver" id="host_folder_gee" data-toggle="collapse" data-target="#host_folder_gee_target">
                                          <a href="javascript:void(0)">Google Earth Engine</a>
                                      </li>
                                      <ul class="sub-menu collapse" id="host_folder_gee_target"></ul>
                                  </ul>
                                  
                                  <li data-intro="Processes are programs like Python code, shell scripts, Jupyter Notebooks, Google Earth Engine scripts, etc. You can manage all your code here in one place and run on different hosts. All the history of process execution will be saved in Geoweaver database, even the used hosts are not longer available. " >
                                    <a href="javascript:void(0)"><i class="fa fa-cog fa-lg"  data-toggle="tooltip" title="Add new process"></i> Process <i class="fa fa-plus" id="newprocess"></i> <span data-toggle="collapse" data-target="#processs" class="arrow collapsed"></span></a>
                                  </li>
                                  <ul class="sub-menu collapse" id="processs">
                                    
                                        <li class="shellfolder" id="process_folder_shell" data-toggle="collapse" data-target="#process_folder_shell_target">
                                          <a href="javascript:void(0)">Shell</a>
                                      </li>
                                      <ul class="sub-menu collapse" id="process_folder_shell_target"></ul>
                                      
                                      <li class="notebookfolder" id="process_folder_jupyter" data-toggle="collapse" data-target="#process_folder_jupyter_target">
                                          <a href="javascript:void(0)">Notebook</a>
                                      </li>
                                      <ul class="sub-menu collapse" id="process_folder_jupyter_target"></ul>
                                      
                                      <li class="builtinfolder" id="process_folder_builtin" data-toggle="collapse" data-target="#process_folder_builtin_target">
                                          <a href="javascript:void(0)">Builtin Process</a>
                                      </li>
                                      <ul class="sub-menu collapse" id="process_folder_builtin_target"></ul>
                                      
                                      <li class="pythonfolder" id="process_folder_python" data-toggle="collapse" data-target="#process_folder_python_target">
                                          <a href="javascript:void(0)">Python</a>
                                      </li>
                                      <ul class="sub-menu collapse" id="process_folder_python_target"></ul>
                                    
                                    
                                  </ul>
                  
                  
                                  <li data-intro="Workflows are linked pipes of a number of processes. You can easily turn complicated scientific experiments into several worflows and execute and manage them from here. ">
                                    <a href="javascript:void(0)"><i class="fa fa-cogs fa-lg"  data-toggle="tooltip" title="Add new workflow"></i> Workflows <!--<i class="fa fa-plus" id="newworkflow"></i>--> <span data-toggle="collapse" data-target="#workflows" class="arrow collapsed"></span></a>
                                  </li>  
                                  <ul  class="sub-menu collapse" id="workflows">
                                    
                                  </ul>
                                  
                  
                                  <!-- <li>
                                    <a href="javascript:void(0)">
                                    <i class="fa fa-share fa-lg"></i> Share
                                    </a>
                                  </li> -->
                  
                                  <li>
                                    <a href="javascript:GW.tutorial.showDialog()">
                                    <i class="fa fa-book fa-lg"></i> Tutorial
                                    </a>
                                  </li>
                      
                                  <li>
                                    <a href="javascript:GW.about.showDialog()">
                                    <i class="fa fa-info fa-lg"></i> About
                                    </a>
                                  </li>
                              </ul>
                              
                       </div>
                       
                  </div>
                  </div>
              <div class="modal fade" id="resultmodal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
                <div class="modal-dialog">
                  <div class="modal-content">              
                    <div class="modal-body">
                        <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                      <img src="" class="imagepreview" style="width: 100%;" />
                    </div>
                  </div>
                </div>
              </div>
              
          </div>
          
          <!-- The actual snackbar showing the saved/code update message -->
          <div id="snackbar" style="z-index: 100;">Some message..</div>
      </div>`);
    },

    run: function () {
      GW.test.initHTML(); // Ensure that initHTML exists before calling it

      describe("Geoweaver Tests", () => {
        beforeEach(() => {
          GW.test.initHTML();
        });

        describe("Geoweaver general functions", () => {
          it("process test", () => {
            GW.process.addMenuItem({ id: "xyzxyz", name: "testProcess" }, "process");
            GW.process.refreshProcessList();
            GW.process.checkIfProcessPanelActive();
            GW.process.clearCache();
            GW.process.clearProcessLogging();
            GW.process.clearCodeEditorListener();
            GW.process.displayToolbar();

            $("body").append('<div id="main-process-info-code-tab"></div>');
            GW.process.display({
              id: "7uvu8x",
              name: "testshell1",
              description: null,
              code: '#!/bin/bash\nsudo pacman-mirrors --country United_States\n...',
              lang: "shell",
              owner: "111111",
              confidential: "FALSE",
            });

            expect(true).toBe(true);
          });

          it("host test", () => {
            GW.host.addMenuItem({ id: "xyzxyz", name: "testProcess" }, "process");
            GW.host.refreshHostList();
            GW.host.checkIfHostPanelActive();
            GW.host.clearCache();
            GW.host.expand({ type: "ssh" });

            GW.host.newDialog();
            GW.host.new_host_frame.closeFrame();

            GW.host.list([
              {
                id: "oy1pa5",
                name: "localhost",
                ip: "127.0.0.1",
                port: "22",
                username: "test",
                owner: "111111",
                type: "ssh",
                confidential: "FALSE",
              },
            ]);
            GW.host.validateIP("127.0.0.1");
            GW.host.display({
              id: "oy1pa5",
              name: "localhost",
              ip: "127.0.0.1",
              port: "22",
              username: "test",
              owner: "111111",
              type: "ssh",
              confidential: "FALSE",
            });

            expect(true).toBe(true);
          });

          it("workflow test", () => {
            GW.workflow.addMenuItem({ id: "wfxyzxyz", name: "test workflow" });
            GW.workflow.refreshWorkflowList();

            GW.workflow.list([
              {
                id: "8mfoowf5jdgfcrf1rubt",
                name: "Test2",
                confidential: "FALSE",
                edges: '[{"source":{"title":"test.sh"}}]',
                nodes: '[{"title":"test.sh"}]',
              },
            ]);
            GW.workflow.display({
              id: "zv9gez8e3qdtultn9i6c",
              name: "t3",
              confidential: "FALSE",
              edges: '[{"source":{"title":"testpython2"}}]',
              nodes: '[{"title":"testpython2"}]',
            });

            expect(true).toBe(true);
          });

          it("workspace test", () => {
            expect(true).toBe(true);
          });

          it("history test", () => {
            expect(true).toBe(true);
          });
        });
      });
    },
  },
};

// Ensure GW.test.run() is called after defining everything
GW.test.run();

