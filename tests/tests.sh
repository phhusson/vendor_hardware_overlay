#!/bin/bash

base="$(dirname "$(readlink -f -- $0)")/.."
cd $base

#Keep knownKeys
rm -f tests/priorities fail
touch tests/priorities tests/knownKeys
find -name AndroidManifest.xml |while read manifest;do
	folder="$(dirname "$manifest")"
	#Ensure this overlay doesn't override blacklist-ed properties
	for b in $(cat tests/blacklist);do
		if grep -qRF "$b" $folder;then
			echo "Overlay $folder is defining $b which is forbidden"
			touch fail
		fi
	done

	#Everything after that is specifically for static overlays, targetting framework-res
	isStatic="$(xmlstarlet sel -t -m '//overlay' -v @android:isStatic -n $manifest)"
	[ "$isStatic" != "true" ] && continue

	#Ensure priorities unique-ness
	priority="$(xmlstarlet sel -t -m '//overlay' -v @android:priority -n $manifest)"
	if grep -qE '^'$priority'$' tests/priorities;then
		echo $manifest priority $priority conflicts with another manifest
		touch fail
	fi
	echo $priority >> tests/priorities

	systemPropertyName="$(xmlstarlet sel -t -m '//overlay' -v @android:requiredSystemPropertyName -n $manifest)"
	if [ "$systemPropertyName" == "ro.vendor.product.name" ];then
		echo "$manifest: ro.vendor.product.name is deprecated. Please use ro.vendor.build.fingerprint"
		touch fail
	fi

	#Ensure the overloaded properties exist in AOSP
	find "$folder" -name \*.xml |while read xml;do
		keys="$(xmlstarlet sel -t -m '//resources/*' -v @name -n $xml)"
		for key in $keys;do
			grep -qE '^'$key'$' tests/knownKeys && continue
			#Run the ag only on phh's machine. Assume that knownKeys is full enough.
			#If it's enough, ask phh to update it
			if [ -d /build/AOSP-9.0 ] && ag '"'$key'"' /build/AOSP-9.0/frameworks/base/core/res/res > /dev/null;then
				echo $key >> tests/knownKeys
			else
				echo $xml defines a non-existing attribute $key
				touch fail
			fi
		done
	done
done
rm -f tests/priorities

if find -name \*.xml |xargs dos2unix -ic |grep -qE .;then
	echo "The following files have dos end of lines"
	find -name \*.xml |xargs dos2unix -ic
	touch fail
fi

if [ -f fail ];then exit 1; fi
