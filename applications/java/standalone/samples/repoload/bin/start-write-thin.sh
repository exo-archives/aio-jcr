#!/bin/sh

java -Duser.language=en -Duser.region=us \
    -jar exo.jcr.applications.repoload-1.8.3.1.jar \
    -conf="./bin/config/configuration-thin.xml" \
    -root="/testStorage/root1" \
    -tree="10-100-100-100" \
    -vdfile="./bin/img.tif" \
    -repo="db1" -ws="ws" \
    -readtree="false" > /dev/null &
