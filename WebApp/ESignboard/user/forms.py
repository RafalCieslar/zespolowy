from django import forms


# class RegisterForm(forms.Form):
#     username = forms.CharField(label="Nazwa użytkownika", required=True, max_length=30)
#     first_name = forms.CharField(label="Imię", required=True, max_length=30)
#     last_name = forms.CharField(label="Nazwisko", required=True, max_length=30)
#     password = forms.CharField(label="Hasło:", required=True, widget=forms.PasswordInput())
#     confirm_password = forms.CharField(label="Powtórz hasło:", required=True, widget=forms.PasswordInput())
#     email = forms.EmailField(label="Adres e-mail", required=True)
#
#
# class LoginForm(forms.Form):
#     username = forms.CharField(label="Nazwa użytkownika", required=True, max_length=30)
#     password = forms.CharField(label="Hasło:", required=True, widget=forms.PasswordInput())
