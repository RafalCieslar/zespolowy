from django.conf.urls import url

from . import views

app_name = 'devices'
urlpatterns = [
    url(r'^dashboard/$', views.dashboard, name='dashboard'),
    url(r'^update/(?P<device_id>[0-9a-z]+)$', views.update, name='update'),
    url(r'^upload/(?P<device_id>[0-9a-z]+)$', views.upload, name='upload'),
    url(r'^download/(?P<device_id>[0-9a-z]+)$', views.download, name='download'),
    url(r'^poi/view/(?P<device_id>[0-9a-z]+)$', views.poi_view, name='poi_view'),
    url(r'^poi/new/(?P<device_id>[0-9a-z]+)$', views.poi_new, name='poi_new'),
    url(r'^poi/edit/(?P<device_id>[0-9a-z]+)$', views.poi_edit, name='poi_edit'),
    url(r'^mdevice/edit/(?P<device_id>[0-9]+)$', views.mdevice_edit, name='mdevice_edit'),
    url(r'^poi/stats/(?P<device_id>[0-9a-z]+)$', views.poi_stats, name='poi_stats')
]
