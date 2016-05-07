import requests
import json

server_host = 'http://localhost'
update_url = 'update'
uuid = 'balblablablabl'
version = '0.1'

req_params = {'uuid': uuid, 'version': version}

r = requests.get(server_host, params=req_params)

parsed_json = json.loads(r.json())
if parsed_json['version'] != version:
    version_update_req = requests.get(server_host + update_url)
    file = open('update.zip', 'wb')
    file.write(version_update_req.content)
    file.close()
    file = open('update', 'wb')
    file.close()
    exit(2)
if parsed_json['data_checksum'] != uuid:
    data_update_req = requests.get(parsed_json['data_url'])
    file = open('server/esignboard_data.zip', 'wb')
    file.write(data_update_req.content)
    file.close()
    exit(0)
