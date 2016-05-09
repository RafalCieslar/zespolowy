#!/bin/bash

pushd
yes | apt-get install git dnsmasq hostapd
cd /mdevice
git clone https://github.com/oblique/create_ap
cd /mdevice/create_ap
make install
popd
