def handlefile(f, path):
    destination = open(path + "/file.jpg", 'wb+') 
    for chunk in f.chunks():
        destination.write(chunk)
    destination.close()
