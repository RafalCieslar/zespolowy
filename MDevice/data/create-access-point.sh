#!/bin/bash

PROGRAM_NAME="$(basename $0)"

if [ $# -eq 2 ]; then
	create_ap -n wlan0 $1 $2
else
	echo "Usage: $PROGRAM_NAME <ssid> <password>"
fi
