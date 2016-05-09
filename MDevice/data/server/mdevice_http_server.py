import http.server

def start_server(port=80, bind="", cgi=False):
    if cgi == True:
        http.server.test(HandlerClass=http.server.CGIHTTPRequestHandler, port=port, bind=bind)
    else:
        http.server.test(HandlerClass=http.server.SimpleHTTPRequestHandler, port=port, bind=bind)

start_server()