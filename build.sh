#!/bin/bash

PATH=~/android-studio/jre/bin:"$PATH"

KEYSTORE=~/.android/debug.keystore

if [ ! -e $KEYSTORE ]; then
  keytool -genkey -v -keystore $KEYSTORE \
    -alias androiddebugkey -storepass android -keypass android \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US"
fi

gradle assembleDev &&
jarsigner \
  -sigalg SHA1withRSA \
  -digestalg SHA1 \
  -keystore $KEYSTORE \
  -storepass android \
  app/build/outputs/apk/app-dev-unsigned.apk \
  androiddebugkey &&
adb install -r \
  app/build/outputs/apk/app-dev-unsigned.apk
