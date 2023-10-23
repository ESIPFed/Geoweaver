#!/bin/bash
java -jar /usr/local/bin/geoweaver.jar &
SERVER_PID=$!

# Wait for the server to start (adjust the time as needed)
sleep 7

# Open the URL in the default web browser
xdg-open http://localhost:8070/Geoweaver

# Wait for the server to complete before exiting
wait $SERVER_PID
