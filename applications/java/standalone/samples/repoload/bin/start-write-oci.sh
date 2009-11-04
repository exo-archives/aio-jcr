#!/bin/sh

java -Duser.language=en -Duser.region=us \
    -jar repoload-1.10.5.1.jar \
    -conf="configuration-oci.xml" \
    -root="/testStorage/root3" \
    -tree="10-100-100-100" \
    -vdfile="img.tif" \
    -repo="db1" -ws="ws" \
    -readtree="false"
