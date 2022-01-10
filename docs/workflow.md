
# Workflow in Geoweaver

## Create and run a workflow

1. Click twice on the plus icon button after any process (e.g., `helloworld` Python process created in the [Process tutorial](process.md)). The main work area will switch to the "Weaver" tab and two circles containing `helloworld` should appear in the workspace.

2. Link the two circles by dragging from one circle and dropping on another circle while pressing `SHIFT` on the keyboard.

3. Click the plus button in the `Options` toolbar floating in the Weaver workspace. In the popup window, input `HelloWorldWorkflow` for the workflow name, and type `Testing hello world` in the Description field. Click `Confirm`.

4. To run the workflow, click . In the popup window, select `1one-host` option. Choose localhost and set the environment to the default. Click `Run`.

5. In the password dialog, enter the password for localhost.

6. While the workflow is in execution mode, blue means the process is waiting, yellow means the corresponding process is running, green means the process execution is finished, and red means the process execution is failed for some reason.

7. Double click on the circles to check the real time output. If users need more details, click `Details` button in the popup information window.

## Export and share a workflow

1. Click  when the workflow is present in the Weaver workspace.

2. In the popup window, there are two options: `workflow with process code` and `workflow with process code and history`. The former will only download source code and workflow json. The latter will download not only source code and workflow, but also all the history details of previous execution of the workflow.

3. Click `Confirm`. A ZIP file will be automatically downloaded to your machine.

4. To import a workflow, click . In the upload window, select the ZIP file, click `Start`.

5. Once the uploading finished, if the workflow file is valid, it will ask `The upload workflow is valid. Do you want to proceed to save it into the database?` Click `OK`.

6. The workflow will be automatically loaded into the Weaver workspace and ready for execution and reuse.

Congratulations you have learnt how to create, run, export, and share a workflow in Geoweaver!
