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


## Quick Links

- **GitHub Repository**: [ESIPFed/Geoweaver](https://github.com/ESIPFed/Geoweaver)
- **Python Bindings**: [ESIPFed/pygeoweaver](https://github.com/ESIPFed/pygeoweaver)
- **Live Demo**: [Geoweaver Demo](https://geobrain.csiss.gmu.edu/Geoweaver)



Geoweaver is an in-browser software allowing users to easily compose and execute full-stack data processing workflows via taking advantage of online spatial data facilities, high-performance computation platforms, and open-source deep learning libraries. It provides all-in-one capacity covering server management, code repository, workflow orchestration software, and history recorder. 

It can be run from both local and remote (distributed) machines.

## Why Choose Geoweaver?

**Key Benefits**:

1. **Data Safety**: Securely store and track all your research progress
2. **Research Organization**: Stay organized throughout long-term research projects
3. **Seamless Connectivity**: Easy SSH connection to external servers
4. **Python Integration**: Built-in web UI with comprehensive Python support
5. **Community-Driven**: Active community with ongoing development and support

For detailed information, visit [Geoweaver Documentation](https://geoweaver.dev).

> Geoweaver is a community effort. Any contribution is welcome and greatly appreciated!

# Features

## Core Features

### 🖥️ Host Management
- Register and manage SSH-enabled machines for process execution
- Integrate Jupyter Servers for interactive workflow development
- Secure connection management with encrypted credentials

### 🔄 Process Management
- Support for multiple process types (Python, Bash, R, etc.)
- Real-time process monitoring and control
- Version control for process code

### 📓 Jupyter Integration
- Seamless notebook import/export
- Automatic version tracking
- Interactive notebook editing

### 📊 Workflow Orchestration
- Visual workflow builder
- Parallel and sequential execution support
- Cross-resource workflow distribution
- Centralized workflow management

### 📝 History & Logging
- Comprehensive execution history
- Detailed process logs
- Automated backup and recovery

### 🔍 Data Pipeline Visualization
- Interactive workflow diagrams
- Dependency tracking
- Real-time execution monitoring

### ⚡ Research Productivity
- Automated task scheduling
- Reduced manual intervention
- Focus on research, not infrastructure 

## Installation

### Prerequisites

- ☕ Java 11 or higher (OpenJDK 11 or higher)
- 🐳 Docker (optional, for Docker installation)

### Quick Start

#### 🐍 Python Method (Recommended)

```bash
# Install PyGeoweaver
pip install pygeoweaver --upgrade

# Start Geoweaver
gw start
```

#### ☕ Java Method

1. Download [geoweaver.jar](https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar)
2. Run: `java -jar geoweaver.jar`

#### 🐳 Docker Method

```bash
# Pull the image
docker pull geoweaver/geoweaver

# Run Geoweaver
docker run -t -i -p 8070:8070 geoweaver/geoweaver
```

> 📝 Access Geoweaver at http://localhost:8070/Geoweaver

[Detailed Installation Guide](docs/install.md)

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

