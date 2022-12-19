#!/bin/bash

set -e

if [ "$1" == "--local-aapt" ];then
    export LD_LIBRARY_PATH=.
    export PATH=.:$PATH
    shift
fi

script_dir="$(dirname "$(readlink -f -- "$0")")"
if [ "$#" -eq 1 ]; then
    if [ -d "$1" ];then
	    makes="$(find "$1" -name Android.mk -exec readlink -f -- '{}' \;)"

    else
	    makes="$(readlink -f -- "$1")"
    fi
else
    cd "$script_dir"
    makes="$(find "$PWD/.." -name Android.mk)"
fi

if ! command -v aapt > /dev/null;then
    export LD_LIBRARY_PATH=.
    export PATH=$PATH:.
fi

if ! command -v aapt > /dev/null;then
    echo "Please install aapt (apt install aapt should do)"
    exit 1
fi

cd "$script_dir"

if [ $(uname) == 'Darwin' ]; then
    aapt_bin="./aapt-mac"
else
    aapt_bin="./aapt"
fi

echo "$makes" | while read -r f;do
    name="$(sed -nE 's/LOCAL_PACKAGE_NAME.*:\=\s*(.*)/\1/p' "$f")"
    grep -q treble-overlay <<<"$name" || continue
    echo "Generating $name"

    path="$(dirname "$f")"
    $aapt_bin package -f -F ${name}-unsigned.apk -M $path/AndroidManifest.xml -S $path/res -I android.jar
    java -jar signapk/apksigner.jar sign --cert keys/platform.x509.pem --key keys/platform.pk8 --in ${name}-unsigned.apk --out ${name}.apk
    rm -f ${name}-unsigned.apk ${name}.apk.idsig
done
