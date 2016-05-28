from django import forms
from .models import Device


class POIform(forms.Form):
    name = forms.CharField(max_length=30, label="Nazwa", widget=forms.TextInput(
        attrs={'class': 'form-control', 'placeholder': 'Nazwa'}))
    bb_text = forms.CharField(label="Opis", widget=forms.Textarea(
        attrs={'class': 'form-control', 'placeholder': 'Opis'}))
    image = forms.ImageField(label="Zdjęcie", widget=forms.FileInput(
        attrs={'class': 'form-control', 'placeholder': 'Zdjęcie'}))
    uuid = forms.CharField(max_length=32, label="Identyfikator", widget=forms.TextInput(
        attrs={'class': 'form-control', 'placeholder': 'Identyfikator'}))


class MDeviceform(forms.Form):
    name = forms.CharField(max_length=30, label="Nazwa", widget=forms.TextInput(
        attrs={'class': 'form-control', 'placeholder': 'Nazwa'}))
