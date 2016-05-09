#!/bin/bash

/mdevice/connect-to-wifi.sh "Andrzej Wajda" srawopony#2137
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