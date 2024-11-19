# Setting Up the Python Environment Path in Geoweaver

This guide details steps to configure the Python environment within Geoweaver for seamless execution of geospatial workflows.

---
## Prerequisites
- Geoweaver installed and accessible at `localhost`
- Python installed on your system

---
### 1. Access Python Settings

1. Open Geoweaver in your browser by navigating to `localhost`.
2. Locate the Python symbol in the bottom-right corner and click it.
3. Enter your initial Host User Password.
4. Confirm the Python installation path of your local host.

   > **Note**: If your Python installation path is not visible, proceed to step 2.

### 2. Configuring Python Environment Path

1. Go to the **Processes** section in Geoweaver.
2. Select the process you want to run, an information panel will appear in the main area.
3. Click the **Play** button to execute the process. In the pop-up window, select `Localhost` and click **Execute**.

4. In the dialog box to specify the Python environment:
    - Select `New` from the dropdown.
    - Enter the following details:
      - **Python Command**: Provide the full path to your Python environment's base directory. You can find this by running `which python` on macOS/Linux or `where python` on Windows.
      - **Environment Name**: Choose a unique name for this environment.
      - **Base Directory**: Use tilde symbol (~) and click **Confirm**.
5. When prompted, enter your password for `Localhost` and click **Confirm**.

**Success!** If you see output in the logging window, the Python environment is set up, and your process in Geoweaver has executed successfully.

**🎉 Congratulations! Well done! 🎉**

---

## Troubleshooting

- **Incorrect Password Error**: Refer to the [password reset guide](install.md) for instructions.
- **Creating a New Process**: Refer to the [process creation guide](process.md) for details.

---
