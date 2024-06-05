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
DIR=\$(dirname "\$0")

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

# Display loading screen
show_loading_screen &

nohup java -jar "\$DIR/../Java/geoweaver.jar" > /dev/null 2>&1 &

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
