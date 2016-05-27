import zipfile
import bbcode
import os
import io
from django.shortcuts import render, redirect, get_object_or_404
from django.core.files.images import get_image_dimensions
from django.http import Http404, JsonResponse, HttpResponse
from .forms import POIform
from .checkpath import checkpath
from .filehandler import handlefile
from .models import Device, Poi
from hashlib import md5


def generate_md5(fname):  # tego zdecydowanie tutaj nie powinno być
    hash_md5 = md5()
    with open(fname, "rb") as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hash_md5.update(chunk)
    return hash_md5.hexdigest()


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
                # message = "Sukces!"
                return redirect('devices:dashboard')
        else:
            message = form.errors
    else:
        form = POIform(initial={'name': vPoi.name, 'uuid': vPoi.uuid, 'bb_text': vPoi.content})
    return render(request, 'devices/edit.html', {'form': form, 'message': message})


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
    #
    # form = MDeviceform(device)
    # message = ""
    return render(request, 'devices/edit.html')
