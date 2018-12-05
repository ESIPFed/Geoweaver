# Welcome to [Geoweaver](https://esipfed.github.io/Geoweaver/)

2018 ESIP Lab Incubator Project

Geoweaver is a web system allowing users to easily compose and execute full-stack deep learning workflows via taking advantage of online spatial data facilities, high-performance computation platforms, and open-source deep learning libraries. 

# Project Goals

1) turning large-scale distributed deep network into manageable modernized workflows;

2) boosting higher utilization ratio of the existing cyberinfrastructures by separating scientists from
tedious technical details;

3) enhancing the frequency and accuracy of classified land cover land use maps for agricultural purposes;

4) enabling the tracking of provenance by recording the execution logs in structured tables to evaluate the
quality of the result maps;

5) proof the effectiveness of operationally using large-scale distributed LSTM network in classifying
Landsat image time series.

# Installation

## Prerequisite

Ubuntu 16.04+

JDK 1.8+

Tomcat 8.0+

MySQL 5.5+ (run the gw.sql to initialize the database. Recommanded command: `mysql -u root -p < gw.sql`)

Maven 3.5+ (for building from source)

## Quick Install

### Tomcat War

To use Geoweaver, [download](https://github.com/ESIPFed/Geoweaver/releases) the latest release war and copy it to the webapps directory of Tomcat. Start Tomcat. 

After the tomcat is fully started, configure the database connection. The configuration files are `WEB-INF/classes/config.properties` (database url, default: jdbc:mysql://localhost:3306/cyberconnector) and `WEB-INF/classes/cc_secret.properties` (database username and password: database_user=root database_password=xxxxxxxx). Fill the fields with correct values. (**Note: the database must be initiated first.**)

Then enter the following URL into browser address bar to open Geoweaver:

`http://your-ip:your-port/Geoweaver-<version>/web/geoweaver`

Replace the `your-ip`, `your-port`, `Geoweaver-<version>` with the real name of your tomcat and downloaded Geoweaver package. For example, `localhost:8080`, `Geoweaver-0.6.6`.

### Cloud VM Template

We provide a ready-to-use cloud template for you to install on mainstream cloud platforms like AWS, Google Cloud, Azure, OpenStack and CloudStack. Please go here to download the template.

### Docker

We use `docker-compose` to establish the containers for Geoweaver. As the DockerHub is not very friendly for docker-compose yaml at present, we only suggest manual to start from GitHub repo. 

First, you need clone this repo to your machine `git clone https://github.com/ESIPFed/Geoweaver.git`. 

Then, enter the repo and create a new folder `target`. Download a Geoweaver war package from the [release page](releases) and save it in the created `target` folder. (Warning: make sure the version number in docker-compose.yml is the same with the one you downloaded. If they are different, update the docker-compose.yml.)

Finally, run `docker-compose up -d`. After the command is finished, Geoweaver should be up and running. The address is the same:

`http://your-ip:your-port/Geoweaver-<version>/web/geoweaver`

Notice: Make sure the local services like mysql and tomcat are better shut down before starting `docker-compose`. Otherwise there might be port conflict error on `3306` and `8080`.

If you don't have docker or docker-compose installed, these documents will help. [docker](https://docs.docker.com/install) [docker-compose](https://docs.docker.com/compose/install/)

## Build from source

Use maven to build. In the command line go to the root folder and execute `mvn install`. After a success build, the Geoweaver war package will be under the directory: `Geoweaver/target/Geoweaver-<version>.war`.

## Usage

### Add A Server

Enroll a server to Geoweaver is simple. The server must have SSH server installed and enabled. The server must be accessible from Geoweaver host server. 

![Add a host](docs/addhost.gif)

### Create A Process

Geoweaver supports Bash Shell scripts as processes. You can write bash command lines in the code area. Note: the commands should exist on the target hosts.

![Add a process](docs/addprocess.gif)

### Create A Workflow

Geoweaver can link the processes together to form a workflow. To connect two processes, press `shift` key while dragging from one process to another.

![Create a workflow](docs/createworkflow.gif)

### Run Workflow

Geoweaver can run the created workflows on the enlisted servers. During the running, Geoweaver is monitoring the status of each process. The color of process text in their circles indicate the process status. Yellow means running, green means completed, and red means failure.

![Run a workflow](docs/runworkflow.gif)

### Browse Provenance

Geoweaver stores all the inputs and outputs of each process run. Users can check the workflow provenance by simply clicking.

![Check provenance](docs/checkprovenance.gif)

### Retrieve and Display Results

Geoweaver can retrieve the result files of the executed workflows and visualize them if the format is supported (png, jpg, bmp, etc. The list is expanding. I am on it.).

![Get result](docs/getresult.gif)

### I/O workflows

The workflows can be exported and move around and imported back.

![Export workflow](docs/exportworkflow.gif)

# Demonstration

A live demo site is available in George Mason University: [I am a link, hit me](http://cube.csiss.gmu.edu/CyberConnector/web/geoweaver).

Here is a use case of Geoweaver, using LSTM RNN to classify landsat images into agricultural land use maps. In this case, Geoweaver can help stakeholders get crop maps with better accuracy and high temporal resolution by providing a deep-learning-powered and web-based workflow system. 

![LSTM-Crop concept](/docs/lstm.png)

![Geoweaver user interface]()

# Documentation

[Project Proposal](docs/geoweaver-proposal-revised-v4.pdf)

[August Report](docs/ESIP-Geoweaver-Report-1.docx)

[September Report](docs/ESIP-Geoweaver-Report-2.docx)

[October Report](docs/ESIP-Geoweaver-Report-3.docx)

[November Report](docs/ESIP-Geoweaver-Report-4.docx)

## Open Source Libraries

This project is impossible without the support of several fantastic open source libraries.

[d3.js](https://github.com/d3/d3) - BSD 3-Clause

[graph-creator](https://github.com/cjrd/directed-graph-creator) - MIT License

[bootstrap](https://github.com/twbs/bootstrap) - MIT License

[CodeMirror](https://github.com/codemirror/CodeMirror) - MIT License

## License

MIT

### Author

[developer list](authors.md)

