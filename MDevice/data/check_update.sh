#!/usr/bin/bash

if [ -f first_run ]; then
    bash first_run.sh
    rm first_run
fi

bash update_data.sh
local_status=$?
if [ local status -eq 2 ]; then
    unzip update.zip
    bash update/update.sh
fi
