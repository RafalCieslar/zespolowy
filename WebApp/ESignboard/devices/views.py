from django.shortcuts import render, redirect, get_object_or_404
from django.core.files.images import get_image_dimensions
from django import forms
from django.http import Http404, JsonResponse, HttpResponse
import zipfile
from hashlib import md5
from .forms import POIform
from .models import Device, Poi
from .checkpath import checkpath
from .filehandler import handlefile
from django.core.urlresolvers import reverse
import bbcode

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
    message = ""
    if not request.user.is_authenticated():
        return redirect('user:login')
    vPoi = get_object_or_404(Poi, pk=device_id)
    if not vPoi.owners.filter(id=request.user.id).exists():
        raise Http404
    if request.method == 'POST':
        form = POIform(request.POST, request.FILES)
        if form.is_valid():
            picture = form.cleaned_data['image']
            w, h = get_image_dimensions(picture)
            if w > 400 or h> 400:
               message = "The image is "+ str(w)+" pixels wide and "+ str(h)+" pixels high. It must be 400x400 or lower"
            else:
                path = "files/"+str(vPoi.parent_device.id) + "/" +vPoi.uuid;
                checkpath(path)
                handlefile(picture, path)
                bbtext =  form.cleaned_data['bb_text']
                rendered = bbcode.render_html(bbtext)
                renderfile = open(path+"/index.html", "w")
                renderfile.write("<img src=file.jpg><br>\n")
                renderfile.write(rendered)
                renderfile.close()
                message = "Success"
       
    else:
        form = POIform();
    return render(request, 'devices/edit.html',{'form': form, 'message' : message})


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
    
