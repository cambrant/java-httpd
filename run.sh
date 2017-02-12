#!/usr/bin/env bash

rm -f HTTPd.class

#javac -verbose HTTPd.java
javac HTTPd.java

if [ -f HTTPd.class ]; then
    echo
    # java -verbose:class -verbose:gc -verbose:jni HTTPd
    java HTTPd -r ./
fi
