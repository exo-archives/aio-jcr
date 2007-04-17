#!/bin/sh

java -Duser.language=en -Duser.region=us \
    -jar repoload-1.0.jar \
    -conf="configuration-thin.xml" \
    -root="/testStorage/root7" \
    -tree="10-100-100-100" \
    -vdfile="img.tif" \
    -repo="db1" -ws="ws" \
    -readtree="false"