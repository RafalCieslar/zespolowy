from django import forms


class POIform(forms.Form):
    bb_text = forms.CharField(label = 'BB:', widget=forms.Textarea)
    image = forms.ImageField();