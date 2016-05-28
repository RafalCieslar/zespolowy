import os
import io
import errno
import zipfile
import zlib
from .models import Poi, Device
from hashlib import md5
from shutil import copyfileobj


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


def generate_md5(fname):
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
    #temp_file = io.StringIO('readthis')
    # zip_file_holder = io.BytesIO()

    # zip_file = zipfile.ZipFile(zip_file_holder, mode='w', compression=zipfile.ZIP_DEFLATED)
    zip_file = zipfile.ZipFile(files_dir + zip_filename, mode='w', compression=zipfile.ZIP_DEFLATED)
    # zip_file.writestr('read', temp_file.getvalue())
    for poi in pois:
        if os.path.isfile(files_dir + poi.uuid + html_filename):
            zip_file.write(files_dir + poi.uuid + html_filename, poi.uuid + html_filename)
        if os.path.isfile(files_dir + poi.uuid + html_filename):
            zip_file.write(files_dir + poi.uuid + img_filename, poi.uuid + img_filename)
    zip_file.close()

    # with open(files_dir + zip_filename, mode='wb') as fd:
    #     zip_file_holder.seek(0)
    #     copyfileobj(zip_file_holder, fd)

    mdevice.hash = generate_md5('files/' + str(mdevice.id) + '/update.zip')
    mdevice.save()