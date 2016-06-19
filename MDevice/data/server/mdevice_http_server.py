import http.server


class MyHandler(http.server.SimpleHTTPRequestHandler):
    def do_POST(self):
        if self.path == '/statistics':
            length = self.headers['content-length']
            data = self.rfile.read(int(length))
            with open('/mdevice/statistics', 'a') as fh:
                fh.write(data.decode().rstrip('\n') + '\n')

            self.send_response(200)


def start_server(port=80, bind=""):
    http.server.test(HandlerClass=MyHandler, port=port, bind=bind)


start_server()
