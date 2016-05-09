#!/bin/bash

PROGRAM_NAME="$(basename $0)"

if [ $# -eq 2 ]; then
    umount "${1}p2"
	echo "Mounting ${1}p2 to mdevice..."
	mkdir mdevice	
	mount "${1}p2" mdevice
	echo "Copying files to mdevice..."
	mkdir -p mdevice/mdevice
	yes | cp -r data/* mdevice/mdevice
	cp ./root mdevice/var/spool/cron/crontabs
	cp ./locale.gen mdevice/etc/locale.gen
	chmod +x mdevice/mdevice/*.sh
	touch mdevice/mdevice/uuid
	echo $2 > mdevice/mdevice/uuid
	echo "Syncing..."
	sync
	echo "Unmouting mdevice..."
	umount mdevice
	rmdir mdevice
	echo "Mdevice files were moved to sdcard successfully."
else
	echo "Usage: $PROGRAM_NAME <device> <mdevice_uuid>"
fi
