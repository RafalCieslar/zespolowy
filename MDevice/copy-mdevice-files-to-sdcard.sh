#!/bin/bash

PROGRAM_NAME="$(basename $0)"

if [ $# -eq 2 ]; then
	echo "Mounting ${1}p2 to mdevice..."
	mkdir mdevice	
	mount "${1}p2" mdevice
	echo "Copying files to mdevice..."
	mkdir -p mdevice/mdevice
	mkdir mdevice/mdevice/server
	yes | cp data/* mdevice/mdevice
	$2 > mdevice/mdevice/uuid
	echo "Syncing..."
	sync
	echo "Unmouting mdevice..."
	umount mdevice
	rm -rf mdevice
	echo "Mdevice files were moved to sdcard successfully."
else
	echo "Usage: $PROGRAM_NAME <device> <mdevice_uuid>"
fi
