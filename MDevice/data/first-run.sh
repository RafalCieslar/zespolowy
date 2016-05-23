#!/bin/bash

#ssid=`cat /mdevice/update_network_ssid`
#pass=`cat /mdevice/update_network_password`
#/mdevice/connect-to-wifi.sh "$ssid" "$pass"
sleep 10
apt-get update
locale-gen
yes | apt-get install git
yes | apt-get install python3-pip
pip3 install virtualenv
virtualenv --python=python3.4 /mdevice/venv
source /mdevice/venv/bin/activate
pip3 install requests
deactivate
/mdevice/install-access-point.sh
crontab -l 2>/dev/null; cat - /mdevice/cron | crontab -
rm /mdevice/first_run