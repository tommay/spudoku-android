#!/bin/bash

type="$1"
[ "$type" ] || type=release

jarsigner \
  -sigalg SHA1withRSA \
  -digestalg SHA1 \
  -keystore ~/.android/debug.keystore \
  -storepass android \
  app/build/outputs/apk/app-${type}-unsigned.apk \
  androiddebugkey

adb install -r \
  app/build/outputs/apk/app-${type}-unsigned.apk
