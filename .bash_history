pkg update && pkg upgrade -y
pkg install python python-pip -y
pip install --upgrade pip
pip install requests scapy beautifulsoup4 paramiko cryptography numpy pandas gtts opencv-python flask colorama psutil pillow selenium pytz
pip install numpy pandas scikit-learn tensorflow keras gtts SpeechRecognition opencv-python matplotlib
pkg install nodejs-lts python-tkinter libjpeg-turbo -y
pip install kivy kivymd flask fastapi python-nmap requests-html sqlalchemy
pkg uninstall python
pkg update && pkg upgrade
pkg install python
pip --version
pkg install git curl wget openssh -y
termux-setup-storage
pkg update && pkg upgrade -y
pkg install python git zip unzip openjdk-17 python-pip clang make binutils -y
pip install --upgrade pip
pip install buildozer cython
buildozer init
dpkg -l | grep -E 'python|git|openjdk|zip|unzip|clang|make|binutils'
pip list | grep -iE 'buildozer|cython|kivy'
export JAVA_HOME=$PREFIX/opt/openjdk-17
python apk
nano main.py
export JAVA_HOME=$PREFIX/opt/openjdk-17
buildozer -v android debug
head -n 5 buildozer.spec
rm buildozer.spec
cat <<EOF > buildozer.spec
[app]
title = TechWizard Alpha
package.name = techwizard
package.domain = hk.prashant
source.dir = .
source.include_exts = py,png,jpg,kv,atlas
version = 0.1
requirements = python3,kivy
orientation = portrait
fullscreen = 1
android.permissions = INTERNET, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, CAMERA
android.api = 34
android.minapi = 21
android.private_storage = True
android.accept_sdk_license = True
android.enable_androidx = True
android.archs = arm64-v8a
android.wakelock = True
android.logcat_filters = *:S python:D

[buildozer]
log_level = 2
warn_on_root = 1
EOF

cat buildozer.spec
export JAVA_HOME=$PREFIX/opt/openjdk-17
buildozer -v android debug
pip install setuptools
buildozer -v android debug
pkg install zlib zlib-static -y
pkg install binutils clang -y
buildozer -v android debug
pkg install zlib zlib-static libjpeg-turbo libandroid-support -y
export CFLAGS="-I$PREFIX/include"
export LDFLAGS="-L$PREFIX/lib"
export JAVA_HOME=$PREFIX/opt/openjdk-17
pkg install zlib zlib-static -y && export CFLAGS="-I$PREFIX/include" && export LDFLAGS="-L$PREFIX/lib" && export JAVA_HOME=$PREFIX/opt/openjdk-17 && buildozer -v android debug
pkg install proot-distro -y
proot-distro install ubuntu
proot-distro login ubuntu
su -c exit
pkg update
pkg install proot-distro
proot-distro install ubuntu
proot-distro login ubuntu
pkg update
pkg install proot-distro
proot-distro install ubuntu-22.04
proot-distro login ubuntu-22.04
apt update
apt install -y python3 python3-venv python3-pip git zip unzip openjdk-17-jdk libffi-dev libssl-dev
ls
python apk
ls
ifconf8g
ifconfig
