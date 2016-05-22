from django.shortcuts import render, redirect
from django.contrib.auth.models import User
from django.contrib.auth.forms import AuthenticationForm, UserCreationForm
from django.contrib.auth import authenticate, login as auth_login, logout as auth_logout
# Create your views here.


def login(request):
    if request.method == 'POST':
        username = request.POST['username']
        password = request.POST['password']
        user = authenticate(username=username, password=password)
        if user is not None:
            if user.is_active:
                auth_login(request, user)
                message = 'Zalogowano'
            else:
                message = 'Użytkownik nieaktywny'
        else:
            message = 'Błędne dane logowania'
    else:
        message = ''

    return render(request, 'user/login.html', {
        'form': AuthenticationForm,
        'message': message,
    })


def register(request):
    if request.method == 'POST':
        form = UserCreationForm(request.POST)
        if form.is_valid():
            user = User.objects.create_user(
                username=form.cleaned_data['username'],
                password=form.cleaned_data['password1'],
            )
            message = 'Stworzono użytkownika'
        else:
            message = 'Błędnie wypełniony formularz'
    else:
        form = UserCreationForm()
        message = ''

    #form = RegisterForm()
    return render(request, 'user/register.html', {
        'form': form,
        'message': message,
    })

def logout(request):
    auth_logout(request)
    return redirect('main:index')
