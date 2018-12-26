#!/bin/bash

# There are currently two targets, dev and debug.

# Use debug so "adb shell / run-as net.tommay.spudoku works and allows
# access to the apps files in /data/data/net.tommay.spudoku.  This
# build also enables create.log and uses the same seed for the random
# number generate to create the same puzzles so the create times are
# consistent.

# Use dev when you want random puzzles.

PATH=~/android-studio/jre/bin:"$PATH"

if [ "$1" ]; then
  target="$1"
  shift
else
  target=debug
fi

case "$target" in
  dev)
    KEYSTORE=~/.android/debug.keystore

    if [ ! -e $KEYSTORE ]; then
      keytool -genkey -v -keystore $KEYSTORE \
        -alias androiddebugkey -storepass android -keypass android \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -dname "CN=Android Debug,O=Android,C=US"
    fi

    gradle "$@" assembleDev &&
    jarsigner \
      -sigalg SHA1withRSA \
      -digestalg SHA1 \
      -keystore $KEYSTORE \
      -storepass android \
      app/build/outputs/apk/dev/app-dev-unsigned.apk \
      androiddebugkey &&
    adb install -r \
      app/build/outputs/apk/dev/app-dev-unsigned.apk
    ;;
  debug)
    gradle "$@" assembleDebug &&
    adb install -r \
      app/build/outputs/apk/debug/app-debug.apk
    ;;
  *)
    echo "usage: $0 dev|debug"
    ;;
esac
