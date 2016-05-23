#!/bin/bash

PROGRAM_NAME="$(basename $0)"

if [ $# -eq 2 ]; then
	for device in /dev/*
	do
		if [[ $device == "$1*" ]]; then
			echo "Unmounting $device..."
			umount $device
		fi
	done
	echo "Copying $1 to $2..."
	pv "$1" | dd bs=4M if=$1 | gzip > "$2".gz
	echo "Mdevice sdcard image was created successfully."
else
	echo "Usage: $PROGRAM_NAME <device> <output_file>"
fi