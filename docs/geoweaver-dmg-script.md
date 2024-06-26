## Geoweaver Startup Script Documentation
### Overview

This shell script automates the setup and initialization of the Geoweaver application, ensuring all necessary components are properly configured and handling common setup issues. Below is a high-level explanation of the script's functionality.

### Key Components

#### Logging and Configuration

- Log Directory and File Setup: Creates a directory for logs and sets up logging so that all output is saved to a log file.
- Locale Settings: Configures locale variables to prevent locale-related errors.
- Configuration File: Defines the path to a configuration file that stores setup and state information.

#### Functions

show_loading_screen: Displays a loading screen dialog using AppleScript.

close_loading_screen: Closes the loading screen dialog.

stop_geoweaver: Stops any existing Geoweaver instances running on port 8070.

show_error_message: Displays an error message dialog using AppleScript.

move_existing_data: Moves existing Geoweaver data to a backup directory if necessary.

install_jdk_based_on_architecture: Installs the appropriate JDK version based on system architecture.

install_jdk: Downloads and installs a specified JDK version.

check_jdk: Checks if a compatible JDK is installed and installs it if necessary.

### Main Script Logic

Display Loading Screen: Shows a loading screen to indicate the startup process.

Load Configuration: Loads configuration settings from a file, or creates a new configuration file if it doesn't exist.

Check and Install JDK: Ensures a compatible JDK is installed, installing it if necessary.

Stop Existing Instances: Stops any existing Geoweaver instances to prevent conflicts.

Initial Setup Tasks: Moves existing data if this is the first run, and updates the configuration file.

Password Setup: Prompts the user to set a password if one has not been set, ensuring it is not empty.

Start Geoweaver: Starts the Geoweaver application and logs output.

Wait for Startup: Checks if Geoweaver has started successfully by polling the server and waiting for a 200 HTTP status code.

Close Loading Screen: Closes the loading screen once Geoweaver has started.

Open Geoweaver: Opens the Geoweaver web interface in a browser if the startup was successful; otherwise, shows an error message.