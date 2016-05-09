#!/usr/bin/bash

sudo apt-get update
sudo apt-get install python3-pip
sudo pip3 install virtualenv
virtualenv --python=python3.4 venv
source venv/bin/activate
pip3 install requests
deactivate