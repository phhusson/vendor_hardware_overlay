#!/bin/bash

set -e


if [ "$1" == "--local-aapt" ];then
    export LD_LIBRARY_PATH=.
    export PATH=.:$PATH
    shift
elif [ "$#" -eq 1 ]; then
    makes="$(realpath $1)"
else
    cd "${0%/*}" || true # Change to script directory
    makes="$(find $(cd ..; pwd) -name Android.mk)"
fi

if ! which aapt > /dev/null;then
    export LD_LIBRARY_PATH=.
    export PATH=$PATH:.
fi

if ! which aapt > /dev/null;then
    echo "Please install aapt (apt install aapt should do)"
    exit 1
fi

cd "${0%/*}" || true # Change to script directory


echo "$makes" | while read -r f;do
    name="$(sed -nE 's/LOCAL_PACKAGE_NAME.*:\=\s*(.*)/\1/p' "$f")"
    grep -q treble-overlay <<<$name || continue
    echo "Generating $name"

    path="$(dirname "$f")"
    aapt package -f -F ${name}-unsigned.apk -M $path/AndroidManifest.xml -S $path/res -I android.jar
    LD_LIBRARY_PATH=./signapk/ java -jar signapk/signapk.jar keys/platform.x509.pem keys/platform.pk8 ${name}-unsigned.apk ${name}.apk
    rm -f ${name}-unsigned.apk
done
