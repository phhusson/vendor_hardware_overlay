#!/bin/bash

base="$(dirname "$(readlink -f -- "$0")")/.."
cd "$base" || exit

#Usage: fail <file> <message> [ignore string]
fail() {
	if [ -z "$3" ] || ! grep -qF "$3" "$1";then
		echo "Fatal: $1: $2"
		touch fail
	else
		echo "Warning: $1: $2"
	fi
}

#Keep knownKeys
rm -f tests/priorities fail
touch tests/priorities tests/knownKeys
find . -name AndroidManifest.xml |while read -r manifest;do
	folder="$(dirname "$manifest")"
	#Ensure this overlay doesn't override blacklist-ed properties
	cat tests/blacklist |while read -r b;do
		if grep -qRF "$b" "$folder";then
			fail "$folder" "Overlay $folder is defining $b which is forbidden"
		fi
	done

	#Everything after that is specifically for static overlays, targetting framework-res
	isStatic="$(xmlstarlet sel -t -m '//overlay' -v @android:isStatic -n "$manifest")"
	[ "$isStatic" != "true" ] && continue

	#Ensure priorities unique-ness
	priority="$(xmlstarlet sel -t -m '//overlay' -v @android:priority -n "$manifest")"
	if grep -qE '^'"$priority"'$' tests/priorities;then
		fail "$manifest" "priority $priority conflicts with another manifest"
	fi
	echo "$priority" >> tests/priorities

	systemPropertyName="$(xmlstarlet sel -t -m '//overlay' -v @android:requiredSystemPropertyName -n "$manifest")"
	if [ "$systemPropertyName" == "ro.vendor.product.name" ] || [ "$systemPropertyName" == "ro.vendor.product.device" ];then
		fail "$manifest" "ro.vendor.product.* is deprecated. Please use ro.vendor.build.fingerprint" \
			'TESTS: Ignore ro.vendor.product.'
	fi

	#Ensure the overloaded properties exist in AOSP
	find "$folder" -name \*.xml |while read -r xml;do
		keys="$(xmlstarlet sel -t -m '//resources/*' -v @name -n "$xml")"
		for key in $keys;do
			grep -qE '^'"$key"'$' tests/knownKeys && continue
			#Run the ag only on phh's machine. Assume that knownKeys is full enough.
			#If it's enough, ask phh to update it
			if [ -d /build/AOSP-9.0 ] && \
				(ag '"'"$key"'"' /build/AOSP-9.0/frameworks/base/core/res/res || \
				ag '"'"$key"'"' /build/AOSP-8.1/frameworks/base/core/res/res)> /dev/null ;then
				echo "$key" >> tests/knownKeys
			else
				fail "$xml" "defines a non-existing attribute $key"
			fi
		done
	done
done

#Help handling with priorities
lastpriority="$(sort -n tests/priorities |grep -E '^[0-9]{2,3}$' | tail -n 1)"
echo 'Note: First high continuous priority available is' $((lastpriority+1))
while true;do
    #We want numbers ranging from 10 to (lastpriority+20)
    v=$((RANDOM%(lastpriority+10)))
    v=$((v+10))
    if ! grep -qE '\b'$v'\b' tests/priorities;then
        echo -e '\tI recommend you use priority' $v
        break
    fi
done
rm -f tests/priorities

#find -name \*.xml |xargs dos2unix -ic |while read f;do
#	fail $f "File is DOS type"
#done

#Check overlay.mk
(
	sorted="$(tail -n +2 overlay.mk |grep -E treble- | LC_ALL=C sort -s | md5sum)"
	unsorted="$(tail -n +2 overlay.mk |grep -E treble- | md5sum)"
	if [ "$sorted" != "$unsorted" ];then
		fail overlay.mk "Keep entries sorted"
	fi
	if grep -E '.+' overlay.mk |grep -qvE '\\$';then
		fail overlay.mk "Keep the \\ at the end of all non-empty lines"
	fi
	if [ "$(tail -n 1 overlay.mk)" != "" ];then
		fail overlay.mk "Keep the empty line at the end"
	fi
)

#Check overlay.mk has all overlays
(
    a=$(mktemp)
    b=$(mktemp)
    find -name \*.mk |xargs sed -n -e 's;LOCAL_PACKAGE_NAME\s*:=\s*;;p' |grep treble-overlay | sort > $a
    sed -nE 's;.*(treble-overlay-[^ ]*).*;\1;p' < overlay.mk|sort > $b
    if diff $a $b |grep -qE .;then
        fail overlay.mk "Not all overlays are listed in overlay.mk"
        diff $a $b
    fi
)

if [ -f fail ];then exit 1; fi
