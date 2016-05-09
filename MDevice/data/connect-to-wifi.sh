#!/bin/bash

PROGRAM_NAME="$(basename $0)"

if [ $# -eq 2 ]; then
	touch /mdevice/wpa_supplicant.conf
	echo "ctrl_interface=/var/run/wpa_supplicant" > /mdevice/wpa_supplicant.conf
	echo "network={" >> /mdevice/wpa_supplicant.conf
	echo "	ssid=\"$1\"" >> /mdevice/wpa_supplicant.conf
	echo "  psk=\"$2\"" >> /mdevice/wpa_supplicant.conf
	echo "	key_mgmt=WPA-PSK" >> /mdevice/wpa_supplicant.conf
	echo "	proto=RSN WPA" >> /mdevice/wpa_supplicant.conf
	echo "	pairwise=CCMP TKIP" >> /mdevice/wpa_supplicant.conf
	echo "	group=CCMP TKIP" >> /mdevice/wpa_supplicant.conf
	echo "}" >> /mdevice/wpa_supplicant.conf
	wpa_supplicant -B -iwlan0 -c/mdevice/wpa_supplicant.conf -Dwext
	dhclient wlan0
	rm /mdevice/wpa_supplicant.conf
else
	echo "Usage: $PROGRAM_NAME <ssid> <key>"
fi
