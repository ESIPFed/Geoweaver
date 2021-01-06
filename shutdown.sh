#!/bin/bash

echo "This shuting down script only works for Linux and Mac"

for i in ./install/*/bin/shutdown.sh; do
    echo "Find Command: " $i
    $i
done

echo "Geoweaver is stopped."

