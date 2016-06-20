from django.contrib import admin
from .models import Device, Poi, Stat
# Register your models here.

admin.site.register(Device)
admin.site.register(Poi)
admin.site.register(Stat)