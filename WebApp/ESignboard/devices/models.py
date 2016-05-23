from django.db import models
from django.contrib.auth.models import User

# Create your models here.


class Device(models.Model):
    name = models.CharField(max_length=30)
    # user = models.ForeignKey(User, on_delete=models.CASCADE)
    owners = models.ManyToManyField(User)
    hash = models.CharField(max_length=32)

    def __str__(self):
        return self.name

    def compile_cache(self):
        #to do: cache compilation
        return 1


class Poi(models.Model):
    name = models.CharField(max_length=30)
    # user = models.ForeignKey(User, on_delete=models.CASCADE)
    owners = models.ManyToManyField(User)
    parent_device = models.ForeignKey(Device, on_delete=models.CASCADE)
    content = models.TextField()
    uuid = models.CharField(max_length=32)

    def __str__(self):
        return self.name