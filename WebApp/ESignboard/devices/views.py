from django.shortcuts import render, redirect, get_object_or_404
from django.http import Http404, JsonResponse, HttpResponse
import zipfile
from hashlib import md5

from .models import Device, Poi
from django.core.urlresolvers import reverse

# tego zdecydowanie tutaj nie powinno być


def generate_md5(fname):
    hash_md5 = md5()
    with open(fname, "rb") as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hash_md5.update(chunk)
    return hash_md5.hexdigest()

# Create your views here.


def dashboard(request):
    if not request.user.is_authenticated():
        return redirect('user:login')
    mdevices = Device.objects.filter(owners__in=[request.user.id])
    pois = Poi.objects.filter(owners__in=[request.user.id])
    return render(request, 'devices/dashboard.html')


def update(request, device_id):
    mdevice = get_object_or_404(Device, pk=device_id)
    return JsonResponse({'data_checksum': mdevice.hash})


def download(request, device_id):
    mdevice = get_object_or_404(Device, pk=device_id)
    pois = Poi.objects.filter(parent_device__in=[mdevice.id])
    zip_file = zipfile.ZipFile('files/'+str(mdevice.id)+'/update.zip', mode='w')
    for poi in pois:
        zip_file.write('files/'+str(mdevice.id)+'/'+poi.uuid + '.html')
        zip_file.write('files/'+str(mdevice.id)+'/'+poi.uuid + '.jpg')
    zip_file.close()

    mdevice.hash = generate_md5('files/'+str(mdevice.id)+'/update.zip')
    mdevice.save()
    
    zip_file = open('files/'+str(mdevice.id)+'/update.zip', 'r')
    #response = HttpResponse(zip_file, content_type='application/force-download')
    #response['Content-Disposition'] = 'attachment; filename="%s"' % 'update.zip'
    #return response


def poi_view(request, device_id):
    if not request.user.is_authenticated():
        return redirect('user:login')
    device = get_object_or_404(Poi, pk=device_id)
    if not device.objects.filter(owners__in=request.user.id).exists()\
            and device.mdevice.objects.filter(owner__in=request.user.id).exists():
        return Http404
    return render(request, 'devices/view.html')


def poi_edit(request, device_id):
    if not request.user.is_authenticated():
        return redirect('user:login')
    device = get_object_or_404(Poi, pk=device_id)
    if not device.objects.filter(owners__in=request.user.id).exists()\
            and device.mdevice.objects.filter(owners__in=request.user.id).exists():
        return Http404
    return render(request, 'devices/edit.html')


def mdevice_view(request, device_id):
    if not request.user.is_authenticated():
        return redirect('user:login')
    device = get_object_or_404(Device, pk=device_id)
    if not device.objects.filter(owners__in=request.user.id).exists():
        return Http404
    return render(request, 'devices/view.html')


def mdevice_edit(request, device_id):
    if not request.user.is_authenticated():
        return redirect('user:login')
    device = get_object_or_404(Device, pk=device_id)
    if not device.objects.filter(owners__in=request.user.id).exists():
        return Http404
    return render(request, 'devices/edit.html')

