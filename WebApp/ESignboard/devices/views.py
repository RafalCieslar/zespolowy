import bbcode
import os
import io
from django.shortcuts import render, redirect, get_object_or_404
from django.core.files.images import get_image_dimensions
from django.http import Http404, JsonResponse, HttpResponse
from .forms import POIform
from .methods import checkpath, handlefile, createzip
from .models import Device, Poi
from hashlib import md5

# Create your views here.


def dashboard(request):
    if not request.user.is_authenticated():
        return redirect('user:login')
    context = dict()
    context['mdevices'] = Device.objects.filter(owners__in=[request.user.id])
    context['pois'] = Poi.objects.filter(owners__in=[request.user.id])
    context['user'] = request.user
    return render(request, 'devices/dashboard.html', context)


def update(request, device_id):
    mdevice = get_object_or_404(Device, pk=device_id)
    return JsonResponse({'data_checksum': mdevice.hash})


def download(request, device_id):
    mdevice = get_object_or_404(Device, pk=device_id)
    zip_file = open('files/' + str(mdevice.id) + '/update.zip', 'rb')
    response = HttpResponse(zip_file, content_type='application/zip')
    response['Content-Disposition'] = 'attachment; filename="%s"' % 'update.zip'
    return response


def poi_view(request, device_id):
    if not request.user.is_authenticated():
        return redirect('user:login')
    device = get_object_or_404(Poi, pk=device_id)
    if not device.objects.filter(owners__in=request.user.id).exists() \
            and device.mdevice.objects.filter(owner__in=request.user.id).exists():
        return Http404

    return render(request, 'devices/view.html')


def poi_edit(request, device_id):
    message = ""
    if not request.user.is_authenticated():
        return redirect('user:login')
    vPoi = get_object_or_404(Poi, pk=device_id)
    if not vPoi.owners.filter(id=request.user.id).exists():
        raise Http404
    if request.method == 'POST':
        form = POIform(request.POST, request.FILES)
        if form.is_valid():
            vPoi.uuid = form.cleaned_data['uuid']
            vPoi.name = form.cleaned_data['name']
            vPoi.save()

            picture = form.cleaned_data['image']
            w, h = get_image_dimensions(picture)
            if h > 600:
                message = "Wysokość załączonego pliku to " + str(
                    h) + " pikseli. Jego wysokość nie może być większa niż 600 pikseli"
            else:
                path = "files/" + str(vPoi.parent_device.id) + "/" + vPoi.uuid
                checkpath(path)
                handlefile(picture, path)
                bbtext = form.cleaned_data['bb_text']
                vPoi.content = bbtext
                vPoi.save()
                rendered = bbcode.render_html(bbtext)
                renderfile = open(path + "/index.html", "w")
                renderfile.write(
                    '<html><body><img src="file.jpg" style="width: 100%; display: block; max-height: 600px;" /><p>')
                renderfile.write(rendered)
                renderfile.write('</p></body></html>')
                renderfile.close()

                createzip(vPoi)

                # message = "Sukces!"
                return redirect('devices:dashboard')
        else:
            message = form.errors
    else:
        form = POIform(initial={'name': vPoi.name, 'uuid': vPoi.uuid, 'bb_text': vPoi.content})
    return render(request, 'devices/poi.edit.html', {'form': form, 'message': message})


def mdevice_edit(request, device_id):
    if not request.user.is_authenticated():
        return redirect('user:login')
    device = get_object_or_404(Device, pk=device_id)
    if not device.objects.filter(owners__in=request.user.id).exists():
        return Http404
    #
    # form = MDeviceform(device)
    # message = ""
    return render(request, 'devices/edit.html')
