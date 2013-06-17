#!/bin/bash

cp -f AndroidManifest.base.xml AndroidManifest.xml 
ant debug

if [ $? == 0 ]; then
	cp -f AndroidManifest.fast.xml AndroidManifest.xml 
	ant debug
	cp -f bin/classes.jar ~/Fast/Assets/Plugins/Android/android_facebook_sdk.jar
	cp -rf res/ ~/Fast/Assets/Plugins/Android/res/
	cp -f libs/android-support-v4.jar ~/Fast/Assets/Plugins/Android/android-support-v4.jar


	cp -f AndroidManifest.rivets.xml AndroidManifest.xml
	ant debug
	cp -f bin/classes.jar ~/Blastron/Assets/Plugins/Android/android_facebook_sdk.jar
	cp -rf res/ ~/Blastron/Assets/Plugins/Android/res/
	cp -f libs/android-support-v4.jar ~/Blastron/Assets/Plugins/Android/android-support-v4.jar

	cp -f AndroidManifest.base.xml AndroidManifest.xml
fi
