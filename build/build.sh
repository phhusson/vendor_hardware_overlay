#!/bin/bash

set -e

if [ "$1" == "--local-aapt" ];then
    export LD_LIBRARY_PATH=.
    export PATH=.:$PATH
    shift
fi

if ! which aapt > /dev/null;then
    export LD_LIBRARY_PATH=.
    export PATH=$PATH:.
fi

if ! which aapt > /dev/null;then
    echo "Please install aapt (apt install aapt should do)"
    exit 1
fi

cd "$(dirname "$(readlink -f -- $0)")"

find .. -name Android.mk |while read f;do
    name="$(sed -nE 's/LOCAL_PACKAGE_NAME.*:\=\s*(.*)/\1/p' "$f")"
    grep -q treble-overlay <<<$name || continue
    echo "Generating $name"

    path="$(dirname "$f")"
    aapt package -f -F ${name}-unsigned.apk -M $path/AndroidManifest.xml -S $path/res -I android.jar
    LD_LIBRARY_PATH=./signapk/ java -jar signapk/signapk.jar keys/platform.x509.pem keys/platform.pk8 ${name}-unsigned.apk ${name}.apk
    rm -f ${name}-unsigned.apk
done
