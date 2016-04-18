#!/bin/bash

PROGRAM_NAME="$(basename $0)"

if [ $# -eq 2 ]; then
	for device in /dev/*
	do
		if [[ $device == "$2*" ]]; then
			echo "Unmounting $device..."
			umount $device
		fi
	done
	echo "Copying $1 to $2..."
	pv "$1" | dd bs=4M of="$2"
	echo "Syncing..."
	sync
	echo "Mdevice sdcard was created successfully."
else
	echo "Usage: $PROGRAM_NAME <image> <device>"
fi

