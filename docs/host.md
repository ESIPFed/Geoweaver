
# Host in Geoweaver

## What is Host?

In Geoweaver, `Host` means computing machines (`Linux/Windows/Mac computers`). It is where the code gets executed, data gets processed, and history is generated.

In most time, `Host` is dedicated to be the local machine where Geoweaver is installed. Once Geoweaver is started, it will add a default Host `Localhost` which means the current machine Geoweaver is sitting on. For most people, `Localhost` is all they need. They can run all their processes on `Localhost`.

For cool kids, they might want to run their processes on remote servers, e.g., AWS EC2 instances, HPC. In that case, Geoweaver provides convenient options to enroll remote servers and run your code there but manage all of these distributed work in one place. This is a life-saving feature for multiple-server users. No kidding.

This tutorial will show how to enroll a new `Host` and make it ready for running processes in Geoweaver.

## Create a Host

1. Click the plus button after `Hosts` on the right panel.

2. Input `Host Name`, `Host IP`, `Port`, and `User Name` of your server in the shown dialog. Here is an example:

```
Host Name: Test Server
Host IP: 127.0.0.1
Port: 22
User Name: testuser
```

3. Click `Add`. A new host node `Test Server` will show under the `Host`>`Linux/Win/Mac Computers` tree node.

## Read Python Environment

1. Select the new host under `Hosts`>`Linux/Win/Mac Computers` on the right panel.

2. Click the python icon button in the toolbar of the shown `Host Details` page.

3. Input the `Host User Password` of your account on the remote server in the shown dialog pop up.

> `Note`: if the selected server is localhost, password instructions are discussed [here](#install.md)

4. An `Environment List` section will appear below the `Host Details` section showing all the available PyEnv python environments on the connected server.

## File Browser

1. Select the new host under `Hosts`>`Linux/Win/Mac Computers` on the right panel.

2. Click the sitemap icon (the one with branches) button in the `Host Details` toolbar.

3. Input the `Host User Password` of your account on the server in the shown dialog pop up.

> `Note`: if the selected server is localhost, password instructions are discussed [here](#install.md)

4. Click Confirm. A `File Browser Section` will appear below showing all the available directories and files.

## File Uploader

1. Select the new host under `Hosts`>`Linux/Win/Mac Computers` on the right panel.

2. Click the uploader icon button in the `Host Details` toolbar.

3. Input the `Host Password` of your account on the server in the shown dialog pop up.

> `Note`: if the selected server is localhost, password instructions are discussed [here](#install.md)

4. A file upload section will appear below the `Host Details` section with drag and drop or file browser capability.
![Screen Shot 2022-01-15 at 12 39 12 AM](https://user-images.githubusercontent.com/34227630/149588755-f1982927-a2b0-453f-8248-65fc5668428c.png)

5. Click on the `Open the File Browser` button to choose files, and click `Start` to initiate the uploading.

6. A progress bar with a status above will indicate the progress of the upload.

7. The status with the message `Upload Complete` in green will show when the file is successfully uploaded, and the file is uploaded to the home directory of the logged in user.

## Edit a Host

1. Click the edit icon (first icon) in the `Host Details` page.

2. Change the field values as you wish.

> `Note`: The Host Id is not editable.

3. Click the edit icon button again. The changes should be updated.

## Supported Hosts

### Linux/Win/Mac Computers

Geoweaver provides a Host category called `Linux/Win/Mac Computers` to manage all the `local`/`remote` machines.

* For `local computers` (where Geoweaver is running at), Geoweaver can directly access, manage, and run commands/programs. For security reasons, it will automatically generate a password at its first boot. Scientists have to input the correct passwords to execute their programs or do any action on the local computers via Geoweaver. The computer's authenticated users can change their Geoweaver password anytime (please refer to [reset password](install.md)).

* For `remote computers` (where Geoweaver is not installed on but can access via networks), Geoweaver can access, manage, run commands/programs on them if the operating systems have SSH service enabled and Geoweaver users have valid accounts on those computers. Geoweaver provides a file browser and uploader for users to transfer the files from/to the remote servers. 

> `Tips`: SSH, or Secure Shell, is a remote administration protocol that allows users to control remote servers over the Internet. It provides a mechanism for authenticating a remote user, transferring inputs from the client to the host, and relaying the output back to the client. 

### Jupyter NoteBook Server Instance

> Jupyter Notebook is an open source web application that allows you to create and share documents that contain live code, equations, visualization, and narrative text.

In Geoweaver, you can manage multiple Jupyter Notebook instances in one place. It will provide a proxy for you to record your usage history during eidting the notebooks. All the history will be automatically recorded when you click Save button or shortcut inside the Jupyter Notebook. The history will be retrievable in Geoweaver database and people can always go back and verify their experiments using the history button of the Jupyter host in Geoweaver.

### Jupyter Hub

> JupyterHub is a multi-user version of the Jupyter Notebook designed for companies, classrooms and research labs.

Geoweaver supports JupyterHub in a similar way to the Jupyter Notebook. People can manage multiple JupyterHub servers in Geoweaver. They can record their usage history in each JupyterHub in Geoweaver database which is queriable in future, even the original JupyterHub is gone. With the history, people can clearly understand what happens and reproduce the experiments with minimum efforts in a new environment.

### Jupyter Lab

> JupyterLab is a web-based interactive development environment for Jupyter notebooks, code, and data. It is more like a combination of Jupyter Notebook and many other useful tools (e.g., file browser, data visualization module, status monitor, etc).

Geoweaver supports Jupyter Lab in a similar to Jupyter Notebook and JupyterHub. People can manage multiple JupyterLab in Geoweaver and Geoweaver provides a proxy to monitor the traffic between users and Jupyter Lab and record the history in its database. The history database will persist even after the computing server the JupyterLab was hosted is gone. Geoweaver will significantly improve the management of distributed JupyterLab and enhance the transparency, reusuability, and reproducibility of all the work done in JupyterLab.
