#!/bin/bash

gradle assembleDev &&
jarsigner \
  -sigalg SHA1withRSA \
  -digestalg SHA1 \
  -keystore ~/.android/debug.keystore \
  -storepass android \
  app/build/outputs/apk/app-dev-unsigned.apk \
  androiddebugkey &&
adb install -r \
  app/build/outputs/apk/app-dev-unsigned.apk
