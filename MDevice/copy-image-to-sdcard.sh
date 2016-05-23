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
	pv "$2" | gzip -dc "$1" | dd bs=4M of=$2
	echo "Mdevice sdcard image was copied successfully."
else
	echo "Usage: $PROGRAM_NAME <image.gz> <device>"
fi