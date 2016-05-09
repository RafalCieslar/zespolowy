from django.shortcuts import render, redirect, get_object_or_404
from django.http import Http404, JsonResponse, HttpResponse
import zipfile

from .models import Device, Poi
from django.core.urlresolvers import reverse

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
    zip = zipfile.ZipFile('update.zip', mode='w')
    for poi in pois:
        zip.write(poi.uuid + ".html")
        zip.write(poi.uuid + ".jpg")
    zip.close()

    response = HttpResponse(zip, content_type='application/force-download')
    response['Content-Disposition'] = 'attachment; filename="%s"' % 'update.zip'
    return response


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

