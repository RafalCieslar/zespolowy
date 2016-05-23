from django import forms
from .models import Device


class POIform(forms.Form):
    name = forms.ImageField(max_length=30, widget=forms.TextInput(attrs={'class' : 'form-control', 'placeholder' : 'Nazwa'}))
    bb_text = forms.CharField(label = 'BB:', widget=forms.Textarea(attrs={'class' : 'form-control', 'placeholder' : 'Opis'}))
    image = forms.ImageField(widget=forms.FileInput(attrs={'class' : 'form-control', 'placeholder' : 'Zdjęcie'}))
    uuid = forms.CharField(max_length=32, widget=forms.TextInput(attrs={'class' : 'form-control', 'placeholder' : 'Identyfikator'}))
