from django.shortcuts import render, redirect
from django.core.urlresolvers import reverse

# Create your views here.


def dashboard(request):
    if not request.user.is_authenticated():
        return redirect('user:login')
    return render(request, 'devices/dashboard.html')

