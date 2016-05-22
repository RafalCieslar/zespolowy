from django.conf.urls import url
from django.contrib.auth import views as auth_views

from . import views

app_name = 'user'
urlpatterns = [
    url(r'^login/$', views.login, name='login'),
    url(r'^register/$', views.register, name='register'),
    url(r'^logout/$', views.logout, name='logout')
]