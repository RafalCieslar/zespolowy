#!/bin/bash

PROGRAM_NAME="$(basename $0)"

if [ $# -eq 1 ]; then
	echo "Mounting ${1}p2 to mdevice..."
	mkdir mdevice	
	mount "${1}p2" mdevice
	echo "Copying files to mdevice..."
	mkdir -p mdevice/mdevice
	yes | cp data/install-access-point.sh data/create-access-point.sh data/connect-to-wifi.sh mdevice/mdevice
	echo "Syncing..."
	sync
	echo "Unmouting mdevice..."
	umount mdevice
	rm -rf mdevice
	echo "Mdevice files were moved to sdcard successfully."
else
	echo "Usage: $PROGRAM_NAME <device>"
fi
