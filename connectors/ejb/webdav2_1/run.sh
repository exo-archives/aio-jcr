#!/bin/sh

VERSION=1.10.2-SNAPSHOT

if ( test -z "$JONAS_ROOT" ); then
  echo "ERROR: set \$JONAS_ROOT first"
  exit 1
fi

$JAVA_HOME/bin/java -Djava.security.manager=default  \
-Djava.security.policy=$JONAS_ROOT/conf/java.policy \
-cp $JONAS_ROOT/lib/apps/exo.core.component.security.core-$VERSION.jar:\
$JONAS_ROOT/lib/apps/exo.ws.commons-$VERSION.jar:$JONAS_ROOT/lib/client.jar \
org.objectweb.jonas.client.ClientContainer webdav-ejb-connector-2_1.ear
