#!/bin/bash

export PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"

/mdevice/create-access-point.sh ESignboard "" &
source /mdevice/venv/bin/activate
pushd
cd /mdevice/server
python /mdevice/server/mdevice_http_server.py
popd
deactivate