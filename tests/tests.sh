#!/bin/bash

base="$(dirname "$(readlink -f -- $0)")/.."
cd $base

#Keep knownKeys
rm -f tests/priorities
touch tests/priorities tests/knownKeys
result=0
find -name AndroidManifest.xml |while read manifest;do
	folder="$(dirname "$manifest")"
	#Ensure this overlay doesn't override blacklist-ed properties
	for b in $(cat tests/blacklist);do
		if grep -qRF "$b" $folder;then
			echo "Overlay $folder is defining $b which is forbidden"
			result=1
		fi
	done

	#Everything after that is specifically for static overlays, targetting framework-res
	isStatic="$(xmlstarlet sel -t -m '//overlay' -v @android:isStatic -n $manifest)"
	[ "$isStatic" != "true" ] && continue

	#Ensure priorities unique-ness
	priority="$(xmlstarlet sel -t -m '//overlay' -v @android:priority -n $manifest)"
	if grep -qE '^'$priority'$' tests/priorities;then
		echo $manifest priority $priority conflicts with another manifest
		result=1
	fi
	echo $priority >> tests/priorities

	systemPropertyName="$(xmlstarlet sel -t -m '//overlay' -v @android:requiredSystemPropertyName -n $manifest)"
	if [ "$systemPropertyName" == "ro.vendor.product.name" ];then
		echo "$manifest: ro.vendor.product.name is deprecated. Please use ro.vendor.build.fingerprint"
	fi

	#Ensure the overloaded properties exist in AOSP
	find "$folder" -name \*.xml |while read xml;do
		keys="$(xmlstarlet sel -t -m '//resources/*' -v @name -n $xml)"

		for key in $keys;do
			grep -q $key tests/knownKeys && continue
			if ag '"'$key'"' /build/AOSP-9.0/frameworks/base/core/res/res > /dev/null;then
				echo $key >> tests/knownKeys
			else
				echo $xml defines a non-existing attribute $key
			fi
		done
	done
done
rm -f tests/priorities

exit $result
