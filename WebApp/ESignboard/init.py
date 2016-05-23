import pip


def install(package):
    pip.main(['install', package])

install("bbcode")
install("image")
