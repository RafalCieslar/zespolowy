import os
import errno
import zipfile
import zlib
from .models import Poi, Device
from hashlib import md5


def handlefile(f, path):
    """
    Function handles saving data into the file.

    :param f: data to save (e. g. image from form)
    :param path: path for file to be saved
    :return:
    """
    destination = open(path + "/file.jpg", 'wb+')
    for chunk in f.chunks():
        destination.write(chunk)
    destination.close()


def checkpath(path):
    """
    Function creates directory recursively and raises an exception if there's any other error than "dir exists".

    :param path: directory path
    :return:
    """
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise


def generate_md5(fname):
    """
    Function generates MD5 checksum for a given file.

    :param fname: path to a file
    :return: hexadecimal MD5 checksum
    """
    hash_md5 = md5()
    with open(fname, "rb") as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hash_md5.update(chunk)
    return hash_md5.hexdigest()


def createzip(poiobject):
    """
    Function creates .zip file for a MDevice, containing every MDevice's POIs files
    and handles MDevice hash update.

    :param poiobject: POI object, whose parent MDevice is to be updated
    :return:
    """
    mdevice = Device.objects.get(pk=poiobject.parent_device.id)
    pois = Poi.objects.filter(parent_device__in=[mdevice.id])
    files_dir = '../ESignboard/files/' + str(mdevice.id) + '/'
    zip_filename = 'update.zip'  # without trailing slash
    html_filename = '/index.html'  # with trailing slash
    img_filename = '/file.jpg'  # with trailing slash
    zip_file = zipfile.ZipFile(files_dir + zip_filename, mode='w', compression=zipfile.ZIP_DEFLATED)

    for poi in pois:
        if os.path.isfile(files_dir + poi.uuid + html_filename):
            zip_file.write(files_dir + poi.uuid + html_filename, poi.uuid + html_filename)
        if os.path.isfile(files_dir + poi.uuid + html_filename):
            zip_file.write(files_dir + poi.uuid + img_filename, poi.uuid + img_filename)
    zip_file.close()

    mdevice.hash = generate_md5(files_dir + '/update.zip')
    mdevice.save()

