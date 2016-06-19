#!/bin/bash

export PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"

if [ ! -f /mdevice/first_run ]; then

    #ssid=`cat /mdevice/update_network_ssid`
    #pass=`cat /mdevice/update_network_password`
    #/mdevice/connect-to-wifi.sh "$ssid" "$pass"
    sleep 10

    source /mdevice/venv/bin/activate

    #send statistics to the server
    curl -X POST --data-binary @/mdevice/statistics `cat /mdevice/hostname`/devices/upload/`cat /mdevice/uuid`
    rm /mdevice/statistics
    touch /mdevice/statistics

    python /mdevice/update_data.py

    deactivate
fi