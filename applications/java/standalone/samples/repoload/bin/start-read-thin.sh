#!/bin/sh

java -Duser.language=en -Duser.region=us \
    -jar ..\lib\repoload-1.0.jar \
    -conf=".\config\configuration-thin.xml" \
    -root="/testStorage/roo1" \
    -vdfile="img.tif" \
    -repo="db1" -ws="ws" \
    -read -readdc
