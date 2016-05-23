import requests
import json
import shutil

server_host = 'http://localhost:8000'
update_url = 'update'
device_id = '12345'
version = '0.1'

file = open('/mdevice/server/esignboard_data_checksum', 'r')
esignboard_data_checksum = file.read().rstrip()
file.close()

file = open('/mdevice/uuid', 'r')
device_id = file.read().rstrip()
file.close()

file = open('/mdevice/hostname', 'r')
server_host = file.read().rstrip()
file.close()

req_params = {'device_id': esignboard_data_checksum}
# try:
r = requests.get(server_host + '/devices/update/' + device_id)
print('got request')
json = r.json()
print(json['data_checksum'])
if json['data_checksum'] != esignboard_data_checksum:
    print('got data_checksum')
    data_update_req = requests.get(server_host + '/devices/download/' + device_id)
    file = open('/mdevice/server/esignboard_data.zip', 'wb')
    file.write(data_update_req.content)
    file.close()
    file = open('/mdevice/server/esignboard_data_checksum', 'w')
    file.write(json['data_checksum'])
    file.close()
    exit(0)
# except:
#     exit(0)
