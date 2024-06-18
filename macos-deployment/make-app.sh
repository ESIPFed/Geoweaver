#!/bin/bash

APP_NAME="Geoweaver"
REPO_NAME="Geoweaver"
APP_DIR="${GITHUB_WORKSPACE}/${APP_NAME}.app"
JAR_PATH="${GITHUB_WORKSPACE}/target/geoweaver.jar"
ICON_PATH="${GITHUB_WORKSPACE}/linux-deployment/usr/local/bin/geoweaver.png"

mkdir -p "${APP_DIR}/Contents/MacOS"
mkdir -p "${APP_DIR}/Contents/Resources"
mkdir -p "${APP_DIR}/Contents/Java"


cp "${JAR_PATH}" "${APP_DIR}/Contents/Java/"

EXECUTABLE_SCRIPT="${APP_DIR}/Contents/MacOS/${APP_NAME}"
cat > "${EXECUTABLE_SCRIPT}" <<EOF
#!/bin/bash
mkdir ~/Library/Logs/geoweaver/
LOGFILE="\${HOME}/Library/Logs/geoweaver/geoweaver.log"
exec > >(tee -a "\$LOGFILE") 2>&1
DIR=\$(dirname "\$0")

# Set locale variables to avoid locale errors
export LC_ALL=en_US.UTF-8
export LANG=en_US.UTF-8
export LANGUAGE=en_US.UTF-8

# Function to display loading screen
show_loading_screen() {
  osascript <<EOF2 > /dev/null 2>&1 &
  tell application "System Events"
    -- Display a dialog indicating Geoweaver is starting
    display dialog "Geoweaver is starting up... Please wait." with title "Geoweaver Loading" buttons {"OK"} giving up after 8640
  end tell
EOF2
  # The & symbol runs this AppleScript in the background
}

# Function to close loading screen
close_loading_screen() {
  osascript <<EOF2 > /dev/null 2>&1
  tell application "System Events"
    tell process "System Events"
      -- Check if the loading dialog exists and close it by clicking the OK button
      if exists (window "Geoweaver Loading") then
        click button "OK" of window "Geoweaver Loading"
      end if
    end tell
  end tell
EOF2
  # This AppleScript runs synchronously to ensure the dialog is closed before proceeding
}

# Function to stop any running Geoweaver instance on port 8070
stop_geoweaver() {
  GEOWEAVER_PID=\$(lsof -t -i:8070)
  if [ -n "\$GEOWEAVER_PID" ]; then
    echo "Stopping existing Geoweaver instance with PID \$GEOWEAVER_PID..."
    kill -9 \$(lsof -t -i:8070)
  else
    echo "No existing Geoweaver instance running on port 8070."
  fi
}

show_error_message() {
  local message=\$1
  osascript <<EOF2 > /dev/null 2>&1
  tell application "System Events"
    -- Display an error dialog with the provided message
    display dialog "\$message" with title "Geoweaver Error" buttons {"OK"} default button "OK"
  end tell
EOF2
}

# Function to move existing Geoweaver data to old directory
move_existing_data() {
  if [ -d ~/h2 ]; then
    if [ -f ~/h2/gw.mv.db ]; then
      java -jar "\$DIR/../Java/geoweaver.jar" checkH2Compatibility
      if [ \$? -ne 0 ]; then
        mkdir -p ~/h2/old
        mv ~/h2/gw.* ~/h2/old/ 2>/dev/null
        echo "Moved gw.* files to ~/h2/old/"
      else
        echo "gw.* files are compatible with Geoweaver H2 library. No need to move."
      fi
    else
      echo "No gw.mv.db file found."
    fi
  else
    echo "No h2 directory found."
  fi
}

# Function to check if JDK is installed and its version
check_jdk() {
  if type -p java; then
    JAVA_VERSION=\$(java -version 2>&1 | awk -F '"' '/version/ {print \$2}')
    JAVA_MAJOR_VERSION=\$(echo "\$JAVA_VERSION" | awk -F. '{print \$1}')
    if [[ "\$JAVA_MAJOR_VERSION" -ge 11 && "\$JAVA_MAJOR_VERSION" -le 18 ]]; then
      echo "Java version \$JAVA_VERSION found and is between 11 and 18."
    else
      echo "Java version \$JAVA_VERSION is not between 11 and 18. Installing JDK 11..."
      install_jdk_based_on_architecture
    fi
  else
    echo "Java not found in PATH. Installing JDK 11..."
    install_jdk_based_on_architecture
  fi
}

# Function to install JDK based on system architecture
install_jdk_based_on_architecture() {
  ARCHITECTURE=arm64
  if [ "\$ARCHITECTURE" == "x86_64" ]; then
    install_jdk "11.0.18-10" "jdk_x64_mac_hotspot"
  elif [ "\$ARCHITECTURE" == "arm64" ]; then
    install_jdk "11.0.18-10" "jdk_aarch64_mac_hotspot"
  else
    echo "Unsupported architecture: "
    exit 1
  fi
}

# Function to install JDK
install_jdk() {
  local jdk_version=\$1
  local jdk_arch=\$2
  local jdk_url="https://github.com/adoptium/temurin11-binaries/releases/download/jdk-\${jdk_version//-/%2B}/OpenJDK11U-\${jdk_arch}_\${jdk_version//-/_}.tar.gz"
  local jdk_install_dir="\$HOME/jdk"
  local jdk_tar="\$HOME/jdk.tar.gz"

  echo "Downloading JDK from \$jdk_url..."
  curl -L "\$jdk_url" -o "\$jdk_tar"

  if [ \$? -ne 0 ]; then
    echo "Error downloading JDK from \$jdk_url."
    exit 1
  fi

  if file "\$jdk_tar" | grep -q "gzip compressed data"; then
    echo "JDK archive downloaded successfully."
  else
    echo "Downloaded file is not a valid tar.gz archive."
    rm "\$jdk_tar"
    exit 1
  fi

  mkdir -p "\$jdk_install_dir"
  tar -xzf "\$jdk_tar" -C "\$jdk_install_dir" --strip-components=1

  if [ \$? -ne 0 ]; then
    echo "Error extracting JDK archive."
    rm "\$jdk_tar"
    exit 1
  fi

  echo "export JAVA_HOME=\$jdk_install_dir/Contents/Home" >> ~/.zshrc
  echo "export PATH=\\\$JAVA_HOME/bin:\\\$PATH" >> ~/.zshrc
  source ~/.zshrc
  echo "JDK 11 installed and JAVA_HOME set permanently in ~/.zshrc."
  rm "\$jdk_tar"
  java -version
}

# Display loading screen
show_loading_screen &

check_jdk

# Stop any existing Geoweaver instances
stop_geoweaver

# Move existing Geoweaver data
move_existing_data

if [ ! -f "\$DIR/.password_set" ]; then
  PASSWORD=\$(osascript -e 'Tell application "System Events" to display dialog "Password Setup Required\n\nPlease set a password for Geoweaver. This password is required for accessing and using Geoweaver securely.\n\nEnter your new password:" default answer "" with title "Geoweaver Setup" with hidden answer' -e 'text returned of result' 2>/dev/null)
  if [ -n "\$PASSWORD" ]; then
    nohup java -jar "\$DIR/../Java/geoweaver.jar" resetpassword -p "\$PASSWORD" > /dev/null 2>&1 &
    touch "\$DIR/.password_set"
  else
    echo "No password entered. Exiting."
    exit 1
  fi
fi

nohup java -jar "\$DIR/../Java/geoweaver.jar" >> "\$LOGFILE" 2>&1 &

# Initialize status code
STATUS_CODE=0

# Wait for Geoweaver to start with a maximum timeout
MAX_RETRIES=10
RETRY_COUNT=0

# Wait for Geoweaver to start
while true; do
  STATUS_CODE=\$(curl -s -o /dev/null -w "%{http_code}" -L http://localhost:8070/Geoweaver)
  if [ "\$STATUS_CODE" -eq 200 ]; then
    break
  fi
  sleep 3
  RETRY_COUNT=\$((RETRY_COUNT+1))
  if [ "\$RETRY_COUNT" -ge "\$MAX_RETRIES" ]; then
    echo "Geoweaver failed to start within the expected time."
    break
  fi
done

# Close loading screen
close_loading_screen

if [ "\$STATUS_CODE" -eq 200 ]; then
  open http://localhost:8070/Geoweaver
else
  echo "Geoweaver could not be started. Please check the logs for more details."
  show_error_message "Geoweaver failed to start. Please retry or contact our customer service at https://geoweaver.dev. Check log file at \$LOGFILE for more details."
fi

EOF

chmod +x "${EXECUTABLE_SCRIPT}"

INFO_PLIST="${APP_DIR}/Contents/Info.plist"
cat > "${INFO_PLIST}" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>CFBundleExecutable</key>
  <string>${APP_NAME}</string>
  <key>CFBundleIconFile</key>
  <string>AppIcon</string>
  <key>CFBundleIdentifier</key>
  <string>com.gokulprathin.geoweaver</string>
  <key>CFBundleName</key>
  <string>${APP_NAME}</string>
  <key>CFBundleVersion</key>
  <string>1.0.0</string>
  <key>CFBundlePackageType</key>
  <string>APPL</string>
  <key>CFBundleSignature</key>
  <string>????</string>
  <key>CFBundleInfoDictionaryVersion</key>
  <string>6.0</string>
  <key>CFBundleShortVersionString</key>
  <string>1.0</string>
</dict>
</plist>
EOF

ICONSET="${APP_DIR}/Contents/Resources/${APP_NAME}.iconset"
mkdir "${ICONSET}"

sips -z 16 16     "${ICON_PATH}" --out "${ICONSET}/icon_16x16.png"
sips -z 32 32     "${ICON_PATH}" --out "${ICONSET}/icon_32x32.png"
sips -z 128 128   "${ICON_PATH}" --out "${ICONSET}/icon_128x128.png"
sips -z 256 256   "${ICON_PATH}" --out "${ICONSET}/icon_256x256.png"
sips -z 512 512   "${ICON_PATH}" --out "${ICONSET}/icon_512x512.png"
cp "${ICON_PATH}" "${ICONSET}/icon_1024x1024.png"  # Assuming the original PNG is at least 1024x1024

iconutil -c icns "${ICONSET}" --output "${APP_DIR}/Contents/Resources/AppIcon.icns"
rm -rf "${ICONSET}"
echo "${APP_NAME}.app has been created on your Desktop."
