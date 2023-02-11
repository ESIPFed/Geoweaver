
# Host in Geoweaver

## What is Host?

In Geoweaver, `Host` means computing machines (`Linux/Windows/Mac computers`). It is where the code gets executed, data gets processed, and history is generated.

Mostly, `Host` is dedicated to the local machine where Geoweaver is installed. Once Geoweaver is started, it will add a default Host `Localhost` which means the current machine Geoweaver is sitting on. For most people, `Localhost` is all they need. They can run all their processes on `Localhost`.

Geoweaver has an amazing feature that's a lifesaver for multiple-server users. It allows you to enroll these servers and run your code on them without the hassle of having to manage it all separately. AWS EC2 instances and HPC are perfect examples of servers you can easily integrate with Geoweaver, and you'll be able to benefit from its time-saving benefits.

This tutorial will help how to enroll a new `Host` and make it ready for running processes in Geoweaver.

## Create a Host

1. Click the `New Host` button at the `Hosts` on the left panel.

2. Leave the `Host Type` as default, add `Host Name`, `Host IP`, `Port`, and `User Name` of your server in the shown dialog box.

     > Example:

     ```
      Host Name: Test Server
      Host IP: 127.0.0.1
      Port: 22
      User Name: testuser
     ```

3. Click on `Add`. A new host node `Test Server` (from the above example) will be shown under the `Host`>`Linux/Win/Mac Computers` tree node.


## Read Python Environment

1. Select the New Host under `Hosts`>`Linux/Win/Mac Computers` on the left panel.

2. Click the python icon button in the toolbar of the shown `Host Details` page.

3. Input the `Host User Password` of your account on the remote server in the shown dialog pop-up.

    >  `Note:` if the selected server is localhost, password instructions are discussed [here](install.md)


4. An `Environment List` section will appear below the `Host Details` section showing all the available PyEnv python environments on the connected server.

## File Browser

1. Select the new host under `Hosts`>`Linux/Win/Mac Computers` on the left panel.

2. Click the sitemap icon (the one with branches) button in the `Host Details` toolbar.

3. Input the `Host User Password` of your account on the server in the shown dialog pop-up.

     >  `Note:` if the selected server is localhost, password instructions are discussed [here](install.md)

4. Click Confirm. A `File Browser Section` will appear below, showing all the available directories and files.

## File Uploader

1. Select the new host under `Hosts`>`Linux/Win/Mac Computers` on the left panel.

2. Click the uploader icon button in the `Host Details` toolbar.

3. Input the `Host Password` of your account on the server in the shown dialog pop-up.

     >  `Note`: if the selected server is localhost, password instructions are discussed [here](install.md)

4. A file upload section will appear below the `Host Details` section with drag and drop or file browser capability.

5. Click on the `Open the File Browser` button to choose files, and click `Start` to initiate the uploading.

6. A progress bar with a status above will indicate the progress of the upload.

7. The status with the message `Upload Complete` in green will show when the file is successfully uploaded, and the file is uploaded to the home directory of the logged in user.

## Edit a Host

1. Click the edit icon (first icon) in the toolbar of `Host Details` page.

2. Change the field values as you wish.

     >  `Note:` The Host Id is not editable.

3. Click the edit icon button again. The changes should be updated.

## Supported Hosts

### Linux/Win/Mac Computers

Geoweaver provides a Host category called `Linux/Win/Mac Computers` to manage all the `local`/`remote` machines.

* For `local computers` (where Geoweaver is running), Geoweaver can directly access, manage, and run commands/programs. It will automatically generate a password at its first boot for security reasons. Scientists have to input the correct passwords to execute their programs or do any action on the local computers via Geoweaver. The computer's authenticated users can change their Geoweaver password anytime (please refer to [reset password](install.md)).


* For `remote computers` (where Geoweaver is not installed but can access via networks), Geoweaver can access, manage, and run commands/programs on them if the operating systems have SSH service enabled and Geoweaver users have valid accounts on those computers. Geoweaver provides a file browser and uploader for users to transfer files from/to remote servers.

>  `Tips`: SSH, or Secure Shell, is a remote administration protocol that allows users to control remote servers over the Internet. It provides a mechanism for authenticating a remote user, transferring inputs from the client to the host, and relaying the output back to the client.
