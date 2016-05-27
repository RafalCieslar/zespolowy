import os
import errno


def handlefile(f, path):
    destination = open(path + "/file.jpg", 'wb+')
    for chunk in f.chunks():
        destination.write(chunk)
    destination.close()


def checkpath(path):
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise