![example workflow](
https://img.shields.io/github/actions/workflow/status/ESIPFed/Geoweaver/release_workflow.yml?branch=v1.5.2-pre&style=for-the-badge
)
[![License](
https://img.shields.io/github/license/ESIPFed/Geoweaver?style=for-the-badge
)](https://github.com/ESIPFed/Geoweaver/blob/main/LICENSE)
[![Stars](
https://img.shields.io/github/stars/ESIPFed%2FGeoweaver?style=for-the-badge
)](https://github.com/ESIPFed/Geoweaver/stargazers) 
[![Forks](
https://img.shields.io/github/forks/ESIPFed/Geoweaver?style=for-the-badge&color=%23f2b40a
)](https://github.com/ESIPFed/Geoweaver/network/members)
[![Issues](
https://img.shields.io/github/issues/ESIPFed/Geoweaver?style=for-the-badge&color=%2363c1ff
)](https://github.com/ESIPFed/Geoweaver/issues) [![Coverage](
https://img.shields.io/codecov/c/github/ESIPFed/Geoweaver?style=for-the-badge
)](https://codecov.io/)
![PyPi](https://img.shields.io/pypi/v/pygeoweaver?style=for-the-badge) 
![Minimum Java Version](https://img.shields.io/badge/Java-11%2B-%23ed8b02?style=for-the-badge&logo=openjdk
)
![Geoweaver Docs](https://img.shields.io/badge/Docs-Geoweaver-%23c4ff7d?style=for-the-badge&logo=readthedocs&link=https%3A%2F%2Fgeoweaver.dev%2F
)

![logo](src/main/resources/static/img/geoweaver-new-banner.png)


### GitHub Repo: https://github.com/ESIPFed/Geoweaver 
### Pygeoweaver (Python Bindings): https://github.com/ESIPFed/pygeoweaver
### Live Demo: https://geobrain.csiss.gmu.edu/Geoweaver



Geoweaver is an in-browser software allowing users to easily compose and execute full-stack data processing workflows via taking advantage of online spatial data facilities, high-performance computation platforms, and open-source deep learning libraries. It provides all-in-one capacity covering server management, code repository, workflow orchestration software, and history recorder. 

It can be run from both local and remote (distributed) machines.

# Why choose Geoweaver?

1) Safely Store all your progress along the way.
2) Stay organised and productive through out your years-long research
4) Seamlessly connect to external servers with SSH.
5) In-Built Web UI with full support for Python.

For further insights into Geoweaver, please explore the website at https://geoweaver.dev.
GeoWeaver is a community effort. Any contribution is welcome and greatly appreciated! 

# Features

1) **Host Management**:
  - Register machines via SSH as hosts for running processes.
  - Add Jupyter Servers as host resources for interaction and workflow editing.

2) **Process Variety**:
  - Add various types of processes, such as bash scripts for data downloading.

3) **Jupyter Notebook Integration**:
  - Upload or import Jupyter notebooks.
  - Intercept websocket traffic to save notebook versions, enabling easy revision history access.

4) **Process History and Logging**:
  - Detailed history of every process run, including logs and outputs, is stored.

5) **Workflow Management**:
  - Link processes to create workflows for parallel or sequential execution across different resources.
  - All aspects of workflow management are centralized within GeoWeaver.

6) **Boosts Data Pipeline's Tangibility**:
  - Geoweaver provides an intuitive, interactive interface for visualizing data workflows, making it easier for users to understand and manage complex data pipelines.
  - This clear visualization helps users to see the connections and dependencies between different components of their workflows.

7) **Enhances Research Productivity and Reduces Work Anxiety**:
  - Geoweaver has automated scheduling and execution of tasks, researchers can set up their workflows to run at specified times or conditions without manual intervention. 
  - This automation reduces the burden of monitoring and manual execution, allowing researchers to focus on analysis and innovation. 

# [Geoweaver Installation Guide](docs/install.md)

Geoweaver is a powerful tool for geospatial data processing, offering a range of features and capabilities. This guide will walk you through the steps to install Geoweaver on your system.

## Prerequisites

Before you begin, ensure that you have the following dependencies installed:

- Java 1.8 or higher (OpenJDK 8 or higher)
- Docker (required only for the Docker installation method)

# Demo

[A live demo site](https://geobrain.csiss.gmu.edu/Geoweaver) is available.

# Documentation

Learn more about Geoweaver in its official documentation at https://esipfed.github.io/Geoweaver/docs/install.html

# Creating a New Release

For detailed steps on how to create a new release in Geoweaver, please refer to the [release instructions](docs/release_upgrade.md).


# [PyGeoWeaver](https://github.com/ESIPFed/pygeoweaver)

PyGeoWeaver is a Python package that provides a convenient and user-friendly interface to interact with GeoWeaver, a powerful geospatial data processing application written in Java. With PyGeoWeaver, Jupyter notebook and JupyterLab users can seamlessly integrate and utilize the capabilities of GeoWeaver within their Python workflows.

Please do visit the PyGeoWeaver GitHub repository.

## Contributors

Thanks to our many contributors!

[![Contributors](https://contrib.rocks/image?repo=ESIPFed/Geoweaver)](https://github.com/ESIPFed/Geoweaver/graphs/contributors)


# Geoweaver History

## v0.6.7 - v1.0.0 (2018 - 2023)

Key features included:

* Made GitHub zip importable and added .wci.yml.
* Introduced a process stop button, organized the SSH folder, and enabled shell commands to call Python processes.
* Added buffer size control and exit code usage for process status.
* Fixed numerous issues on remote hosts.
* Included new features such as hovering tips and code comparison.
* Added process history and status.
* Added code search function.
* Enabled run button in side panel.
* Allows reset password from terminal.

## v1.0.1 - v1.2.8 (2023 - 2024)

After incorporating feedback from the user community, the Geoweaver team released new versions. This major update focused on performance improvements and added several highly requested features:

* Log output is real time and web socket channels are untangled.
* Made the local logging real time.
* Created a macOS App for Geoweaver.
* Ability to Restore workflow.
* Run maven tests on github actions.

These versions solidified Geoweaver's position as a powerful open-source GIS solution and attracted interest from various industries and research institutions.

## v1.3.0 - v1.6.1 (2024)

This version focuses on updating features and bug fixing:

* Fixed the chart visibility issue and table actions in side panel.
* Opens dock at bottom by default.
* Updated README.md by including latest features and modern style.
* Navigation fix for process tab and workflow tab in Guide page.
* Added filtering skipped process functionality.
* Responsive Design for lower resultion devices such as ipad / tablets.
* Support for MySQL and PostgreSQL - Production grade DB.
* Autosaves code on Run.
* Added support for Docker. The Geoweaver Docker image can be found here(https://hub.docker.com/repository/docker/geoweaver/geoweaver/general).

For more details, you can check the Geoweaver Releases Page.


# Citation

If you found Geoweaver helpful in your research, please cite: 

Sun, Z. et al., "Geoweaver: Advanced cyberinfrastructure for managing hybrid geoscientific AI workflows." ISPRS International Journal of Geo-Information 9, no. 2 (2020): 119.


# Existing Projects

Sun, Ziheng, Nicoleta C. Cristea, Kehan Yang, Ahmed Alnuaim, Lakshmi Chetana Gomaram Bikshapathireddy, Aji John, Justin Pflug et al. "Making machine learning-based snow water equivalent forecasting research productive and reusable by Geoweaver." In AGU fall meeting abstracts, vol. 2022, pp. IN23A-04. 2022.

Sun, Ziheng, and Nicoleta Cristea. "Geoweaver for Automating ML-based High Resolution Snow Mapping Workflow." In AGU Fall Meeting Abstracts, vol. 2021, pp. IN11C-07. 2021.

Sun, Ziheng, Liping Di, Jason Tullis, Annie Bryant Burgess, and Andrew Magill. "Geoweaver: Connecting Dots for Artificial Intelligence in Geoscience." In AGU Fall Meeting Abstracts, vol. 2020, pp. IN011-02. 2020.

Sun, Ziheng, Liping Di, Annie Burgess, Jason A. Tullis, and Andrew B. Magill. "Geoweaver: Advanced cyberinfrastructure for managing hybrid geoscientific AI workflows." ISPRS International Journal of Geo-Information 9, no. 2 (2020): 119.

Sun, Ziheng, Liping Di, Jason Tullis, Annie Bryant Burgess, and Andrew Magill. "Geoweaver: Connecting Dots for Artificial Intelligence in Geoscience." In AGU Fall Meeting Abstracts, vol. 2020, pp. IN011-02. 2020.


# License

The MIT License (MIT)

Copyright (c) 2015 Chris Kibble

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
