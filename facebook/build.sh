#!/bin/bash

ant debug
cp -f bin/classes.jar ~/Fast/Assets/Plugins/Android/android_facebook_sdk.jar
cp -f libs/android-support-v4.jar ~/Fast/Assets/Plugins/Android/android-support-v4.jar
cp -rf res/ ~/Fast/Assets/Plugins/Android/res/
