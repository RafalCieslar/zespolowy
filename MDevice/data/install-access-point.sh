#!/bin/bash

apt-get install git dnsmasq hostapd
git clone https://github.com/oblique/create_ap
cd create_ap
make install
