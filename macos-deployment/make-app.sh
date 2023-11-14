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
nohup java -jar "\$DIR/../Java/geoweaver.jar" > /dev/null 2>&1 &
sleep 7
open http://localhost:8070/Geoweaver
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
