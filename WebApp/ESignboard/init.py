import pip

def install(package):
    pip.main(['install', package])

install("Django")
install("bbcode")
install("image")
