#!/bin/bash

PROGRAM_NAME="$(basename $0)"

if [ $# -eq 2 ]; then
	touch wpa_supplicant.conf
	echo "ctrl_interface=/var/run/wpa_supplicant" > wpa_supplicant.conf
	echo "network={" >> wpa_supplicant.conf
	echo "	ssid=\"$1\" psk=\"$2\"" >> wpa_supplicant.conf
	echo "	key_mgmt=WPA-PSK" >> wpa_supplicant.conf
	echo "	proto=RSN WPA" >> wpa_supplicant.conf
	echo "	pairwise=CCMP TKIP" >> wpa_supplicant.conf
	echo "	group=CCMP TKIP" >> wpa_supplicant.conf
	echo "}" >> wpa_supplicant.conf
	wpa_supplicant -B -iwlan0 -cwpa_supplicant.conf -Dwext
	dhclient wlan0
	rm wpa_supplicant.conf
else
	echo "Usage: $PROGRAM_NAME <ssid> <key>"
fi
