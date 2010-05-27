#!/bin/sh

java -Duser.language=en -Duser.region=us \
    -jar exo.jcr.applications.repoload-binary.jar \
    -conf="configuration-oci.xml" \
    -root="/testStorage/root3" \
    -tree="10-100-100-100" \
    -vdfile="img.tif" \
    -repo="db1" -ws="ws" \
    -readtree="false"
