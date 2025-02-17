#!/bin/bash

APP_PATH="/opt/APKScanner"
APP_FILE="ApkScanner.jar"

# java 버전 확인
java_ver=$(java -version 2>&1 | sed '1!{d}; /^java version/!d; s/java version \"\([0-9].[0-9]\).*\"/\1/')

if [ "$java_ver" == "" ] || [ "$java_ver" != "$(echo $java_ver | awk '{ if($1 >= 1.8) { print $1 } }')" ]; then
    if [ "$java_ver" != "" ]; then
        echo "Need JDK8..."
        echo "current version : $java_ver"
    fi
    #echo "Please retry after setup JDK8.."
    #echo "You can download the JDK8 from http://www.oracle.com/technetwork/java/javase/downloads/index.html"
    #exit
fi

cat << EOF > ./APKScanner.sh
#!/bin/bash
java -Xms512m -Xmx1024m -jar $APP_PATH/$APP_FILE "\$@" > /dev/null
EOF

jar -xf ApkScanner.jar icons/AppIcon.png

echo "{}" > settings.txt

sudo rm -rf $APP_PATH

sudo mkdir -p $APP_PATH
if [ ! -d $APP_PATH ]; then
    echo Fail : Not create the folder : %APP_PATH%
    exit
fi
sudo cp -rf ./* $APP_PATH

sudo chmod 755 $APP_PATH/tool/linux/adb
sudo chmod 755 $APP_PATH/tool/linux/aapt
sudo chmod 755 $APP_PATH/tool/dex2jar/d2j-dex2jar.sh
sudo chmod 755 $APP_PATH/tool/dex2jar/d2j_invoke.sh
sudo chmod 755 $APP_PATH/tool/jadx/bin/jadx
sudo chmod 755 $APP_PATH/tool/jadx/bin/jadx-gui
sudo chmod 755 $APP_PATH/APKScanner.sh

sudo chmod 666 $APP_PATH/settings.txt
sudo chmod 666 $APP_PATH/plugin/plugins.conf
sudo chmod 666 $APP_PATH/security/trustStore.jks

sudo rm -rf ./icons
sudo rm -rf ./settings.txt
sudo rm -rf ./APKScanner.sh

cat << EOF > ./apkscanner.desktop
[Desktop Entry]
Encoding=UTF-8
Version=1.0
Type=Application
Exec=java -jar $APP_PATH/$APP_FILE %f
Name=APK Scanner
Comment=APK Scanner
Icon=$APP_PATH/icons/AppIcon.png
MimeType=application/apk;application/vnd.android.package-archive;
EOF

sudo mv -f ./apkscanner.desktop /usr/share/applications/apkscanner.desktop

if [ ! -e /usr/share/mime/application/vnd.android.package-archive.xml ]; then
cat << EOF > ./vnd.android.package-archive.xml
<?xml version="1.0" encoding="utf-8"?>
<mime-info xmlns="http://www.freedesktop.org/standards/shared-mime-info">
  <!--Created automatically by update-mime-database. DO NOT EDIT!-->
  <mime-type type="application/vnd.android.package-archive">
    <comment>Android package</comment>
    <comment xml:lang="bg">Пакет — Android</comment>
    <comment xml:lang="ca">paquet d'Android</comment>
    <comment xml:lang="cs">Balíčky systému Android</comment>
    <comment xml:lang="de">Android-Paket</comment>
    <comment xml:lang="en_AU">Android package</comment>
    <comment xml:lang="en_GB">Android package</comment>
    <comment xml:lang="eo">Android-pakaĵo</comment>
    <comment xml:lang="es">Paquete de Android</comment>
    <comment xml:lang="fi">Android-paketti</comment>
    <comment xml:lang="fr">paquet Android</comment>
    <comment xml:lang="gl">paquete de Android</comment>
    <comment xml:lang="he">חבילת אנדרויד</comment>
    <comment xml:lang="hu">Android csomag</comment>
    <comment xml:lang="id">Paket Android</comment>
    <comment xml:lang="it">Pacchetto Android</comment>
    <comment xml:lang="ja">Android パッケージ</comment>
    <comment xml:lang="kk">Android дестесі</comment>
    <comment xml:lang="ko">안드로이드 패키지</comment>
    <comment xml:lang="lv">Android pakotne</comment>
    <comment xml:lang="pl">Pakiet Androida</comment>
    <comment xml:lang="pt_BR">Pacote Android</comment>
    <comment xml:lang="ru">пакет Android</comment>
    <comment xml:lang="sl">Paket Android</comment>
    <comment xml:lang="sv">Android-paket</comment>
    <comment xml:lang="uk">пакунок Android</comment>
    <comment xml:lang="zh_CN">Android</comment>
    <comment xml:lang="zh_TW">Android 套件</comment>
    <sub-class-of type="application/x-java-archive"/>
    <glob pattern="*.apk"/>
    <glob pattern="*.apex"/>
  </mime-type>
</mime-info>
EOF
    sudo cp -f ./vnd.android.package-archive.xml /usr/share/mime/packages/
    sudo update-mime-database /usr/share/mime/
    sudo rm -f /usr/share/mime/packages/vnd.android.package-archive.xml
fi

if [ -e ~/.local/share/applications/mimeapps.list ]; then
cp -f ~/.local/share/applications/mimeapps.list ~/.local/share/applications/mimeapps_old.list
cat ~/.local/share/applications/mimeapps_old.list \
	| sed '/application\/vnd\.android\.package-archive\=/d;/^$/d' \
	| sed 's/^\s*\[.*\]\s*$/&\napplication\/vnd.android.package-archive=apkscanner.desktop;/' > ~/.local/share/applications/mimeapps.list
else
cat << EOF > ~/.local/share/applications/mimeapps.list
[Added Associations]
application/vnd.android.package-archive=apkscanner.desktop;
EOF
fi

if [ -e ~/.p4qt/ApplicationSettings.xml ]; then
    cat ~/.p4qt/ApplicationSettings.xml | sed '/EditorMappings/,/StringList/{/<String>apk/d; /<String>ppk/d; /<String>apex/d; s/.*<\/StringList>.*/  <String>apk\|default\|\/opt\/APKScanner\/APKScanner\.sh<\/String>\n  <String>ppk\|default\|\/opt\/APKScanner\/APKScanner\.sh<\/String>\n  <String>apex\|default\|\/opt\/APKScanner\/APKScanner\.sh<\/String>\n <\/StringList>/}' > .ApplicationSettings.xml
    chmod 666 .ApplicationSettings.xml
    mv .ApplicationSettings.xml ~/.p4qt/ApplicationSettings.xml
fi

echo "Complete"

exit
