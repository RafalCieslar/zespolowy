import os
import io
import errno
import zipfile
from .models import Poi, Device
from hashlib import md5


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


def generate_md5(fname):  # tego zdecydowanie tutaj nie powinno być
    hash_md5 = md5()
    with open(fname, "rb") as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hash_md5.update(chunk)
    return hash_md5.hexdigest()


def createzip(poiObject):
    mdevice = Device.objects.get(pk=poiObject.parent_device.id)
    pois = Poi.objects.filter(parent_device__in=[mdevice.id])
    files_dir = 'files/' + str(mdevice.id) + '/'
    zip_filename = 'update.zip'  # without trailing slash
    html_filename = '/index.html'  # with trailing slash
    img_filename = '/file.jpg'  # with trailing slash
    temp_file = io.StringIO('readthis')

    zip_file = zipfile.ZipFile(files_dir + zip_filename, mode='w')
    zip_file.writestr(temp_file.getvalue(), 'read')
    for poi in pois:
        if os.path.isfile(files_dir + poi.uuid + html_filename):
            zip_file.write(files_dir + poi.uuid + html_filename, poi.uuid + html_filename)
        if os.path.isfile(files_dir + poi.uuid + html_filename):
            zip_file.write(files_dir + poi.uuid + img_filename, poi.uuid + img_filename)
    zip_file.close()

    mdevice.hash = generate_md5('files/' + str(mdevice.id) + '/update.zip')
    mdevice.save()