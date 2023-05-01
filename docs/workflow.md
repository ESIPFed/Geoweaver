
# Workflow in Geoweaver

## What is Workflow?

In Geoweaver, Workflow denotes a pipeline linking multiple (>2) Processes. An isolated Process (not connected to any other Processes) is allowed in Geoweaver. 
   
**WARNING: Loop is not allowed among the child Process nodes**. The Workflow graph is directed and acyclic (please refer to this [link](https://en.wikipedia.org/wiki/Directed_acyclic_graph) to learn more about Directed Acyclic Graphs).

## Create and run a workflow

1. Click twice on the plus icon button under any process (e.g., `helloworld` Python process created in the [Process tutorial](process.md)). The main work area will switch to the "Weaver" tab, and two circles containing `helloworld` should appear in the workspace.

2. Link the two circles by dragging from one circle and dropping on another circle while pressing `SHIFT` on the keyboard.

3. Click the plus button in the toolbar floating in the 'Weaver' workspace. In the popup window, input `HelloWorldWorkflow` for the workflow name, and type `Testing hello world` in the Description field. Click `Confirm`.

4. To run the workflow, click the play button in the toolbar floating on the 'Weaver' workspace. In the popup window, select `one-host` option. Choose `localhost` and set the environment to `default`. Click `Run`.

5. In the password dialog box, enter the password for localhost.
    
    >`Note:` If you get an incorrect password error, password resetting instructions are discussed [here](install.md)

6. While the workflow is in execution mode, you can see:
    * Blue - This means the process is waiting
    * Yellow - This means the corresponding process is running
    * Green - This means the process execution is finished
    * Red - This means the process execution failed for some reason.

7. Double-click on the circles to check the real-time output. If users need more details, click `Details` button in the popup Process information window.

## Export and Import a workflow

### To export a workflow into a zip file:

1. Choose a workflow in the Workflow tree menu and click on it. 

2. Click the downward icon button in the floating toolbar when the workflow is present in the Weaver workspace.

3. There will be four downloading options: 
    * `workflow with process code` - This will only download the source code and workflow json. 
    * `Workflow with Process Code and Only Workflow History` - This will only download the source code and the workflow history without the process history.
    * `Workflow with Process Code and Only Successfully Done History` - This will download the source code and all history (including process history) of the successfully completed workflow, without the details of failed executions.
    * `workflow with process code and history (Recommended)`. - This will download not only the source code and workflow but also all the historical details of the previous execution of the workflow.


        >  **The last option is recommended.**

4. Click `Confirm`. A ZIP file will be automatically downloaded to your machine.

### To import a shared workflow:

1. Click the upward icon button in the floating toolbar of Weaver. In the shown window, click `Open the File Browser` and choose the Geoweaver ZIP file you received. Click `Start`.

2. Once the uploading is finished and the workflow file is valid,  it will as, `The upload workflow is valid. Do you want to proceed to save it into the database?` Click `OK`. 

3. The workflow will be automatically loaded into the Weaver workspace and ready for execution and reuse. If you check the process and workflow history, you will see that all the previous logs are also there. 

Congratulations! you have learned how to create, run, export, and share a workflow in Geoweaver!
