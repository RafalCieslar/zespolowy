from django.db import models
from django.contrib.auth.models import User
# Create your models here.
class Device(models.Model):
    location = models.CharField(max_length=30)
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    def __str__(self):
        return self.location
    def compile_cache(self):
        #to do: cache compilation
        return 1

class Poi(models.Model):
    name = models.CharField(max_length=30)
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    parent_device = models.ForeignKey(Device, on_delete=models.CASCADE)
    content = models.FileField(upload_to='files') #+parent_device_id+'/'+id
    def __str__(self):
        return self.name