#!/bin/bash

# Function to prompt the user for a password using yad
prompt_password() {
  PASSWORD=$(yad --entry --hide-text --title="Geoweaver Setup" --text="Please set a password for Geoweaver:" --width=300)
  if [ -z "$PASSWORD" ]; then
    yad --error --text="No password entered. Exiting."
    exit 1
  fi
}
PASSWORD_FILE="/usr/local/bin/.password_set"

# Check if the password has already been set
if [ ! -f "$PASSWORD_FILE" ]; then
  prompt_password
  java -jar "$GEOWEAVER_JAR" resetpassword -p "$PASSWORD"
  # Check if the password reset was successful
  if [ $? -eq 0 ]; then
    touch "$PASSWORD_FILE"
  else
    yad --error --text="Failed to set the password. Exiting."
    exit 1
  fi
fi

java -jar /usr/local/bin/geoweaver.jar &
SERVER_PID=$!

# Wait for the server to start (adjust the time as needed)
sleep 7

# Open the URL in the default web browser
xdg-open http://localhost:8070/Geoweaver

# Wait for the server to complete before exiting
wait $SERVER_PID
